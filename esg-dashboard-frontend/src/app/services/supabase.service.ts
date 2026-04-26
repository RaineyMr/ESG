import { Injectable } from '@angular/core';
import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { environment } from '../../environments/environment';
import { Observable, from, map, catchError, throwError } from 'rxjs';

export interface Portfolio {
  id: number;
  portfolio_name: string;
  description?: string;
  total_value: number;
  base_currency: string;
  inception_date: string;
  created_at: string;
  updated_at: string;
}

export interface PortfolioSummary {
  portfolio_id: number;
  portfolio_name: string;
  total_value: number;
  base_currency: string;
  inception_date: string;
  holding_count: number;
  average_esg_score: number;
  overall_risk_level: string;
  average_beta: number;
  average_volatility: number;
}

export interface Holding {
  id: number;
  portfolio_id: number;
  ticker_symbol: string;
  company_name: string;
  sector: string;
  quantity: number;
  purchase_price: number;
  current_price: number;
  market_value: number;
  weight_in_portfolio: number;
  esg_scores?: any;
  risk_metrics?: any;
}

@Injectable({
  providedIn: 'root'
})
export class SupabaseService {
  private supabase: SupabaseClient | null = null;
  private isInitialized = false;

  constructor() {
    // Delay initialization to avoid timeout issues
    setTimeout(() => {
      try {
        this.supabase = createClient(
          environment.supabaseUrl,
          environment.supabaseKey
        );
        this.isInitialized = true;
      } catch (error) {
        console.error('Supabase initialization error:', error);
      }
    }, 1000);
  }

  get supabaseClient(): SupabaseClient {
    if (!this.isInitialized || !this.supabase) {
      throw new Error('Supabase client not initialized yet');
    }
    return this.supabase;
  }

  isReady(): boolean {
    return this.isInitialized && this.supabase !== null;
  }

  // Portfolio operations
  getAllPortfolios(): Observable<Portfolio[]> {
    return from(this.supabase
      .from('portfolios')
      .select('*')
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return response.data || [];
      }),
      catchError(error => throwError(() => error))
    );
  }

  getPortfolioById(id: number): Observable<Portfolio> {
    return from(this.supabase
      .from('portfolios')
      .select('*')
      .eq('id', id)
      .single()
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return response.data;
      }),
      catchError(error => throwError(() => error))
    );
  }

  createPortfolio(portfolio: Partial<Portfolio>): Observable<Portfolio> {
    return from(this.supabase
      .from('portfolios')
      .insert([portfolio])
      .select()
      .single()
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return response.data;
      }),
      catchError(error => throwError(() => error))
    );
  }

  updatePortfolio(id: number, portfolio: Partial<Portfolio>): Observable<Portfolio> {
    return from(this.supabase
      .from('portfolios')
      .update(portfolio)
      .eq('id', id)
      .select()
      .single()
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return response.data;
      }),
      catchError(error => throwError(() => error))
    );
  }

  deletePortfolio(id: number): Observable<void> {
    return from(this.supabase
      .from('portfolios')
      .delete()
      .eq('id', id)
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return;
      }),
      catchError(error => throwError(() => error))
    );
  }

  getPortfolioSummary(id: number): Observable<PortfolioSummary> {
    return from(this.supabase
      .from('portfolio_summaries')
      .select('*')
      .eq('portfolio_id', id)
      .single()
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return response.data;
      }),
      catchError(error => throwError(() => error))
    );
  }

  // Holdings operations
  getHoldingsByPortfolio(portfolioId: number): Observable<Holding[]> {
    return from(this.supabase
      .from('holdings')
      .select(`
        *,
        esg_scores(*),
        risk_metrics(*)
      `)
      .eq('portfolio_id', portfolioId)
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return response.data || [];
      }),
      catchError(error => throwError(() => error))
    );
  }

  addHolding(holding: Partial<Holding>): Observable<Holding> {
    return from(this.supabase
      .from('holdings')
      .insert([holding])
      .select()
      .single()
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return response.data;
      }),
      catchError(error => throwError(() => error))
    );
  }

  updateHolding(id: number, holding: Partial<Holding>): Observable<Holding> {
    return from(this.supabase
      .from('holdings')
      .update(holding)
      .eq('id', id)
      .select()
      .single()
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return response.data;
      }),
      catchError(error => throwError(() => error))
    );
  }

  deleteHolding(id: number): Observable<void> {
    return from(this.supabase
      .from('holdings')
      .delete()
      .eq('id', id)
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return;
      }),
      catchError(error => throwError(() => error))
    );
  }

  // ESG Scores operations
  updateESGScore(holdingId: number, esgScore: any): Observable<any> {
    return from(this.supabase
      .from('esg_scores')
      .upsert({ holding_id: holdingId, ...esgScore })
      .select()
      .single()
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return response.data;
      }),
      catchError(error => throwError(() => error))
    );
  }

  // Risk Metrics operations
  updateRiskMetric(holdingId: number, riskMetric: any): Observable<any> {
    return from(this.supabase
      .from('risk_metrics')
      .upsert({ holding_id: holdingId, ...riskMetric })
      .select()
      .single()
    ).pipe(
      map(response => {
        if (response.error) throw response.error;
        return response.data;
      }),
      catchError(error => throwError(() => error))
    );
  }

  // Utility method to handle errors
  handleError(error: any): string {
    if (error.code === 'PGRST116') {
      return 'Resource not found.';
    } else if (error.code === 'PGRST301') {
      return 'Unauthorized access.';
    } else if (error.code === '23505') {
      return 'Duplicate entry detected.';
    } else {
      return error.message || 'An unexpected error occurred.';
    }
  }
}
