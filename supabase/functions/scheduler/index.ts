import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

// Trigger updates for market data and ESG scores
async function triggerAllUpdates(supabase: any) {
  const results = {
    marketData: { success: false, message: '' },
    esgData: { success: false, message: '' }
  }
  
  try {
    // Update market prices
    const marketResponse = await fetch(`${Deno.env.get('SUPABASE_URL')}/functions/v1/market-data/update-prices`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${Deno.env.get('SUPABASE_ANON_KEY')}`,
        'Content-Type': 'application/json'
      }
    })
    
    if (marketResponse.ok) {
      const marketResult = await marketResponse.json()
      results.marketData = { success: true, message: `Updated ${marketResult.updated} holdings` }
    } else {
      results.marketData = { success: false, message: 'Market data update failed' }
    }
    
    // Update ESG scores
    const esgResponse = await fetch(`${Deno.env.get('SUPABASE_URL')}/functions/v1/esg-data/update-esg`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${Deno.env.get('SUPABASE_ANON_KEY')}`,
        'Content-Type': 'application/json'
      }
    })
    
    if (esgResponse.ok) {
      const esgResult = await esgResponse.json()
      results.esgData = { success: true, message: `Updated ${esgResult.updated} ESG scores` }
    } else {
      results.esgData = { success: false, message: 'ESG data update failed' }
    }
    
  } catch (error) {
    console.error('Error triggering updates:', error)
    results.marketData = { success: false, message: error.message }
    results.esgData = { success: false, message: error.message }
  }
  
  return results
}

// Get last update timestamps
async function getLastUpdates(supabase: any) {
  try {
    const { data: holdings, error: holdingsError } = await supabase
      .from('holdings')
      .select('updated_at')
      .order('updated_at', { ascending: false })
      .limit(1)
    
    const { data: esgScores, error: esgError } = await supabase
      .from('esg_scores')
      .select('last_updated')
      .order('last_updated', { ascending: false })
      .limit(1)
    
    const { data: portfolios, error: portfoliosError } = await supabase
      .from('portfolios')
      .select('updated_at')
      .order('updated_at', { ascending: false })
      .limit(1)
    
    return {
      lastMarketUpdate: holdings?.[0]?.updated_at || null,
      lastESGUpdate: esgScores?.[0]?.last_updated || null,
      lastPortfolioUpdate: portfolios?.[0]?.updated_at || null,
      errors: {
        holdings: holdingsError?.message,
        esg: esgError?.message,
        portfolios: portfoliosError?.message
      }
    }
  } catch (error) {
    console.error('Error getting last updates:', error)
    return {
      lastMarketUpdate: null,
      lastESGUpdate: null,
      lastPortfolioUpdate: null,
      errors: { general: error.message }
    }
  }
}

// Schedule regular updates (this would be called by a cron job or webhook)
async function scheduleUpdates() {
  const supabase = createClient(
    Deno.env.get('SUPABASE_URL') ?? '',
    Deno.env.get('SUPABASE_ANON_KEY') ?? ''
  )
  
  // Check if updates are needed (last update more than 15 minutes ago)
  const lastUpdates = await getLastUpdates(supabase)
  const now = new Date()
  const fifteenMinutesAgo = new Date(now.getTime() - 15 * 60 * 1000)
  
  const needsMarketUpdate = !lastUpdates.lastMarketUpdate || 
    new Date(lastUpdates.lastMarketUpdate) < fifteenMinutesAgo
  
  const needsESGUpdate = !lastUpdates.lastESGUpdate || 
    new Date(lastUpdates.lastESGUpdate) < new Date(now.getTime() - 24 * 60 * 60 * 1000) // 24 hours
  
  if (needsMarketUpdate || needsESGUpdate) {
    console.log('Triggering scheduled updates...')
    return await triggerAllUpdates(supabase)
  } else {
    return {
      marketData: { success: true, message: 'Market data is up to date' },
      esgData: { success: true, message: 'ESG data is up to date' }
    }
  }
}

serve(async (req) => {
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

    if (method === 'POST' && path === '/update-all') {
      const results = await triggerAllUpdates(supabaseClient)
      return new Response(JSON.stringify(results), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200
      })
    }

    if (method === 'POST' && path === '/scheduled-update') {
      const results = await scheduleUpdates()
      return new Response(JSON.stringify(results), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200
      })
    }

    if (method === 'GET' && path === '/status') {
      const lastUpdates = await getLastUpdates(supabaseClient)
      return new Response(JSON.stringify(lastUpdates), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200
      })
    }

    return new Response('Not found', {
      headers: corsHeaders,
      status: 404
    })

  } catch (error) {
    console.error('Scheduler function error:', error)
    return new Response(JSON.stringify({ error: error.message }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 500
    })
  }
})
