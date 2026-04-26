import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  // Handle CORS preflight requests
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL')!,
      Deno.env.get('SUPABASE_ANON_KEY')!
    )

    const url = new URL(req.url)
    const method = req.method
    const path = url.pathname.split('/').filter(Boolean)
    
    // Handle different endpoints
    if (path.includes('portfolios')) {
      if (method === 'GET') {
        // Get all portfolios
        const { data, error } = await supabaseClient
          .from('portfolios')
          .select(`
            *,
            holdings(count),
            esg_metrics(*)
          `)

        if (error) throw error
        return new Response(JSON.stringify(data), {
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          status: 200,
        })
      }

      if (method === 'POST') {
        // Create new portfolio
        const body = await req.json()
        const { data, error } = await supabaseClient
          .from('portfolios')
          .insert([body])
          .select()

        if (error) throw error
        return new Response(JSON.stringify(data[0]), {
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          status: 201,
        })
      }
    }

    if (path.includes('portfolio') && path.length > 2) {
      const portfolioId = path[2]
      
      if (method === 'GET') {
        // Get specific portfolio
        const { data, error } = await supabaseClient
          .from('portfolios')
          .select(`
            *,
            holdings(*),
            esg_metrics(*)
          `)
          .eq('id', portfolioId)
          .single()

        if (error) throw error
        return new Response(JSON.stringify(data), {
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          status: 200,
        })
      }

      if (method === 'PUT') {
        // Update portfolio
        const body = await req.json()
        const { data, error } = await supabaseClient
          .from('portfolios')
          .update(body)
          .eq('id', portfolioId)
          .select()

        if (error) throw error
        return new Response(JSON.stringify(data[0]), {
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          status: 200,
        })
      }

      if (method === 'DELETE') {
        // Delete portfolio
        const { error } = await supabaseClient
          .from('portfolios')
          .delete()
          .eq('id', portfolioId)

        if (error) throw error
        return new Response(null, {
          headers: corsHeaders,
          status: 204,
        })
      }
    }

    if (path.includes('summary') && path.includes('portfolio')) {
      const portfolioId = path[2]
      
      // Get portfolio summary using the view
      const { data, error } = await supabaseClient
        .from('portfolio_summaries')
        .select('*')
        .eq('portfolio_id', portfolioId)
        .single()

      if (error) throw error
      return new Response(JSON.stringify(data), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200,
      })
    }

    return new Response(JSON.stringify({ error: 'Endpoint not found' }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 404,
    })

  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 400,
    })
  }
})
