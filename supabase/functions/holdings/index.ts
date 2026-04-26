import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
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
    
    if (path.includes('holdings')) {
      if (method === 'GET') {
        const portfolioId = url.searchParams.get('portfolio_id')
        
        let query = supabaseClient
          .from('holdings')
          .select(`
            *,
            esg_scores(*),
            risk_metrics(*)
          `)

        if (portfolioId) {
          query = query.eq('portfolio_id', portfolioId)
        }

        const { data, error } = await query
        if (error) throw error
        
        return new Response(JSON.stringify(data), {
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          status: 200,
        })
      }

      if (method === 'POST') {
        const body = await req.json()
        const { data, error } = await supabaseClient
          .from('holdings')
          .insert([body])
          .select()

        if (error) throw error
        return new Response(JSON.stringify(data[0]), {
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          status: 201,
        })
      }
    }

    if (path.includes('holding') && path.length > 2) {
      const holdingId = path[2]
      
      if (method === 'PUT') {
        const body = await req.json()
        const { data, error } = await supabaseClient
          .from('holdings')
          .update(body)
          .eq('id', holdingId)
          .select()

        if (error) throw error
        return new Response(JSON.stringify(data[0]), {
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
          status: 200,
        })
      }

      if (method === 'DELETE') {
        const { error } = await supabaseClient
          .from('holdings')
          .delete()
          .eq('id', holdingId)

        if (error) throw error
        return new Response(null, {
          headers: corsHeaders,
          status: 204,
        })
      }
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
