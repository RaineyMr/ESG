import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

// Alpha Vantage API (Free tier: 500 calls/day)
const ALPHA_VANTAGE_API_KEY = 'YOUR_ALPHA_VANTAGE_KEY' // Replace with your key

// Yahoo Finance API (Free, unlimited)
async function getStockPrice(symbol: string): Promise<number> {
  try {
    const response = await fetch(`https://query1.finance.yahoo.com/v8/finance/chart/${symbol}`)
    const data = await response.json()
    const currentPrice = data.chart.result[0].meta.regularMarketPrice
    return currentPrice
  } catch (error) {
    console.error(`Error fetching price for ${symbol}:`, error)
    throw error
  }
}

// Get multiple stock prices
async function getMultiplePrices(symbols: string[]): Promise<{[symbol: string]: number}> {
  const prices: {[symbol: string]: number} = {}
  
  for (const symbol of symbols) {
    try {
      prices[symbol] = await getStockPrice(symbol)
      // Add small delay to avoid rate limiting
      await new Promise(resolve => setTimeout(resolve, 100))
    } catch (error) {
      console.error(`Failed to get price for ${symbol}:`, error)
    }
  }
  
  return prices
}

// Update holdings with current market prices
async function updateHoldingsPrices(supabase: any) {
  try {
    // Get all holdings
    const { data: holdings, error } = await supabase
      .from('holdings')
      .select('id, ticker_symbol, quantity, portfolio_id')
    
    if (error) throw error
    
    // Get unique symbols
    const symbols = [...new Set(holdings.map((h: any) => h.ticker_symbol))]
    
    // Fetch current prices
    const prices = await getMultiplePrices(symbols)
    
    // Update each holding
    for (const holding of holdings) {
      const currentPrice = prices[holding.ticker_symbol]
      if (currentPrice) {
        const marketValue = holding.quantity * currentPrice
        
        await supabase
          .from('holdings')
          .update({
            current_price: currentPrice,
            market_value: marketValue,
            updated_at: new Date().toISOString()
          })
          .eq('id', holding.id)
      }
    }
    
    // Update portfolio total values
    await updatePortfolioValues(supabase)
    
    return { success: true, updated: holdings.length }
  } catch (error) {
    console.error('Error updating holdings:', error)
    return { success: false, error: error.message }
  }
}

// Update portfolio total values
async function updatePortfolioValues(supabase: any) {
  try {
    const { data: portfolios, error } = await supabase
      .from('portfolios')
      .select('id')
    
    if (error) throw error
    
    for (const portfolio of portfolios) {
      const { data: holdings } = await supabase
        .from('holdings')
        .select('market_value')
        .eq('portfolio_id', portfolio.id)
      
      const totalValue = holdings?.reduce((sum: number, h: any) => sum + h.market_value, 0) || 0
      
      await supabase
        .from('portfolios')
        .update({
          total_value: totalValue,
          updated_at: new Date().toISOString()
        })
        .eq('id', portfolio.id)
    }
  } catch (error) {
    console.error('Error updating portfolio values:', error)
    throw error
  }
}

// Calculate portfolio returns
async function calculateReturns(supabase: any, portfolioId: number) {
  try {
    const { data: holdings, error } = await supabase
      .from('holdings')
      .select('*')
      .eq('portfolio_id', portfolioId)
    
    if (error) throw error
    
    let totalUnrealizedGainLoss = 0
    let totalCost = 0
    
    for (const holding of holdings) {
      const unrealizedGainLoss = (holding.current_price - holding.purchase_price) * holding.quantity
      const cost = holding.purchase_price * holding.quantity
      
      totalUnrealizedGainLoss += unrealizedGainLoss
      totalCost += cost
    }
    
    const totalUnrealizedGainLossPercent = totalCost > 0 ? (totalUnrealizedGainLoss / totalCost) * 100 : 0
    
    return {
      totalUnrealizedGainLoss,
      totalUnrealizedGainLossPercent,
      totalCost,
      totalValue: holdings.reduce((sum: number, h: any) => sum + h.market_value, 0)
    }
  } catch (error) {
    console.error('Error calculating returns:', error)
    throw error
  }
}

serve(async (req) => {
  // Handle CORS
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_ANON_KEY') ?? '',
      {
        global: {
          headers: { Authorization: req.headers.get('Authorization')! },
        },
      }
    )

    const { method } = req
    const url = new URL(req.url)
    const path = url.pathname

    if (method === 'POST' && path === '/update-prices') {
      const result = await updateHoldingsPrices(supabaseClient)
      return new Response(JSON.stringify(result), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200
      })
    }

    if (method === 'GET' && path === '/prices') {
      const { data: holdings, error } = await supabaseClient
        .from('holdings')
        .select('ticker_symbol, current_price, market_value, updated_at')
        .order('market_value', { ascending: false })
      
      if (error) throw error
      
      return new Response(JSON.stringify(holdings), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200
      })
    }

    if (method === 'GET' && path.startsWith('/returns/')) {
      const portfolioId = parseInt(path.split('/')[2])
      const returns = await calculateReturns(supabaseClient, portfolioId)
      
      return new Response(JSON.stringify(returns), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200
      })
    }

    return new Response('Not found', {
      headers: corsHeaders,
      status: 404
    })

  } catch (error) {
    console.error('Market data function error:', error)
    return new Response(JSON.stringify({ error: error.message }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 500
    })
  }
})
