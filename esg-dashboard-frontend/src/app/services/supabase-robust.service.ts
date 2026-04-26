import { Injectable } from '@angular/core';
import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { environment } from '../../environments/environment';
import { Observable, from, map, catchError, throwError, of, delay } from 'rxjs';

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
export class SupabaseRobustService {
  private supabase: SupabaseClient | null = null;
  private isInitialized = false;
  private initializationPromise: Promise<void> | null = null;

  constructor() {
    // Initialize with a delay to avoid timeout issues
    this.initializationPromise = this.initializeWithRetry();
  }

  private async initializeWithRetry(maxRetries: number = 3): Promise<void> {
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        console.log(`Supabase initialization attempt ${attempt}/${maxRetries}`);
        
        // Add delay between retries
        if (attempt > 1) {
          await new Promise(resolve => setTimeout(resolve, 1000 * attempt));
        }

        this.supabase = createClient(environment.supabaseUrl, environment.supabaseKey);
        
        // Test the connection with a simple query
        const { data, error } = await this.supabase.from('portfolios').select('count').limit(1);
        
        if (error) {
          throw error;
        }

        this.isInitialized = true;
        console.log('Supabase initialized successfully');
        return;

      } catch (error) {
        console.error(`Supabase initialization attempt ${attempt} failed:`, error);
        
        if (attempt === maxRetries) {
          console.error('Supabase initialization failed after all retries');
          // Don't throw - allow fallback to mock data
          return;
        }
      }
    }
  }

  private async waitForInitialization(): Promise<void> {
    if (this.initializationPromise) {
      await this.initializationPromise;
    }
  }

  private isReady(): boolean {
    return this.isInitialized && this.supabase !== null;
  }

  get supabaseClient(): SupabaseClient {
    if (!this.isReady()) {
      throw new Error('Supabase client not initialized yet');
    }
    return this.supabase!;
  }

  // Mock data fallback methods
  private getMockPortfolios(): Portfolio[] {
    return [
      {
        id: 1,
        portfolio_name: 'Tech Growth Portfolio',
        description: 'High-growth technology companies with strong ESG scores',
        total_value: 4200000,
        base_currency: 'USD',
        inception_date: '2022-01-15',
        created_at: '2022-01-15T00:00:00Z',
        updated_at: '2024-04-25T00:00:00Z'
      },
      {
        id: 2,
        portfolio_name: 'Sustainable Energy',
        description: 'Renewable energy and clean technology investments',
        total_value: 3100000,
        base_currency: 'USD',
        inception_date: '2022-03-20',
        created_at: '2022-03-20T00:00:00Z',
        updated_at: '2024-04-25T00:00:00Z'
      },
      {
        id: 3,
        portfolio_name: 'Healthcare Innovation',
        description: 'Healthcare companies with innovative treatments and strong governance',
        total_value: 2800000,
        base_currency: 'USD',
        inception_date: '2022-02-10',
        created_at: '2022-02-10T00:00:00Z',
        updated_at: '2024-04-25T00:00:00Z'
      },
      {
        id: 4,
        portfolio_name: 'Financial Services',
        description: 'Banks and financial institutions with strong governance practices',
        total_value: 1000000,
        base_currency: 'USD',
        inception_date: '2022-04-05',
        created_at: '2022-04-05T00:00:00Z',
        updated_at: '2024-04-25T00:00:00Z'
      },
      {
        id: 5,
        portfolio_name: 'Consumer Staples',
        description: 'Essential goods companies with stable ESG performance',
        total_value: 0,
        base_currency: 'USD',
        inception_date: '2022-05-12',
        created_at: '2022-05-12T00:00:00Z',
        updated_at: '2024-04-25T00:00:00Z'
      }
    ];
  }

  private getMockHoldings(): Holding[] {
    return [
      {
        id: 1,
        portfolio_id: 1,
        ticker_symbol: 'AAPL',
        company_name: 'Apple Inc.',
        sector: 'Technology',
        quantity: 10000,
        purchase_price: 150.00,
        current_price: 224.50,
        market_value: 2245000,
        weight_in_portfolio: 0.5345,
        esg_scores: { environmental: 85.2, social: 82.1, governance: 88.3 },
        risk_metrics: { beta: 1.2, volatility: 0.25 }
      },
      {
        id: 2,
        portfolio_id: 1,
        ticker_symbol: 'MSFT',
        company_name: 'Microsoft Corp.',
        sector: 'Technology',
        quantity: 8000,
        purchase_price: 200.00,
        current_price: 236.25,
        market_value: 1890000,
        weight_in_portfolio: 0.4500,
        esg_scores: { environmental: 82.7, social: 85.3, governance: 80.1 },
        risk_metrics: { beta: 0.9, volatility: 0.22 }
      },
      {
        id: 3,
        portfolio_id: 1,
        ticker_symbol: 'GOOGL',
        company_name: 'Alphabet Inc.',
        sector: 'Technology',
        quantity: 5000,
        purchase_price: 180.00,
        current_price: 222.00,
        market_value: 1110000,
        weight_in_portfolio: 0.2643,
        esg_scores: { environmental: 78.9, social: 76.5, governance: 81.3 },
        risk_metrics: { beta: 1.1, volatility: 0.28 }
      },
      {
        id: 4,
        portfolio_id: 2,
        ticker_symbol: 'TSLA',
        company_name: 'Tesla Inc.',
        sector: 'Automotive/Energy',
        quantity: 3000,
        purchase_price: 200.00,
        current_price: 444.00,
        market_value: 1332000,
        weight_in_portfolio: 0.4297,
        esg_scores: { environmental: 92.1, social: 68.4, governance: 56.4 },
        risk_metrics: { beta: 2.1, volatility: 0.52 }
      },
      {
        id: 5,
        portfolio_id: 2,
        ticker_symbol: 'NEE',
        company_name: 'NextEra Energy',
        sector: 'Utilities',
        quantity: 8000,
        purchase_price: 60.00,
        current_price: 70.50,
        market_value: 564000,
        weight_in_portfolio: 0.1819,
        esg_scores: { environmental: 88.7, social: 79.2, governance: 74.8 },
        risk_metrics: { beta: 0.4, volatility: 0.18 }
      }
    ];
  }

  // Portfolio methods with fallback
  getAllPortfolios(): Observable<Portfolio[]> {
    return from(this.waitForInitialization()).pipe(
      delay(500), // Add small delay to prevent timeout
      map(() => {
        if (this.isReady()) {
          // Try to get real data
          return this.supabaseClient.from('portfolios').select('*');
        } else {
          // Fallback to mock data
          console.log('Using mock portfolio data due to Supabase initialization failure');
          return { data: this.getMockPortfolios(), error: null };
        }
      }),
      map((result: any) => {
        if (result.error) {
          console.error('Supabase error, falling back to mock data:', result.error);
          return this.getMockPortfolios();
        }
        return result.data || this.getMockPortfolios();
      }),
      catchError((error) => {
        console.error('Portfolio loading error, using mock data:', error);
        return of(this.getMockPortfolios());
      })
    );
  }

  getHoldingsByPortfolio(portfolioId: number): Observable<Holding[]> {
    return from(this.waitForInitialization()).pipe(
      delay(300),
      map(() => {
        if (this.isReady()) {
          return this.supabaseClient.from('holdings').select('*').eq('portfolio_id', portfolioId);
        } else {
          console.log('Using mock holdings data due to Supabase initialization failure');
          return { data: this.getMockHoldings().filter(h => h.portfolio_id === portfolioId), error: null };
        }
      }),
      map((result: any) => {
        if (result.error) {
          console.error('Supabase error, falling back to mock holdings:', result.error);
          return this.getMockHoldings().filter(h => h.portfolio_id === portfolioId);
        }
        return result.data || this.getMockHoldings().filter(h => h.portfolio_id === portfolioId);
      }),
      catchError((error) => {
        console.error('Holdings loading error, using mock data:', error);
        return of(this.getMockHoldings().filter(h => h.portfolio_id === portfolioId));
      })
    );
  }

  // Health check method
  async healthCheck(): Promise<boolean> {
    try {
      await this.waitForInitialization();
      return this.isReady();
    } catch (error) {
      console.error('Health check failed:', error);
      return false;
    }
  }
}
