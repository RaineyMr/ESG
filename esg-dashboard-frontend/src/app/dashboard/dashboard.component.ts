import { Component, OnInit, Input } from '@angular/core';
import { SupabaseRobustService } from '../services/supabase-robust.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  @Input() portfolio: any;
  portfolios: any[] = [];
  holdings: any[] = [];
  isLoading = true;
  error: string | null = null;
  dataSource: 'database' | 'mock' | 'loading' | 'error' = 'loading';
  lastUpdated: Date | null = null;

  constructor(
    private supabaseService: SupabaseRobustService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  async loadDashboardData(): Promise<void> {
    this.isLoading = true;
    this.error = null;
    this.dataSource = 'loading';

    try {
      // Check service health
      const isHealthy = await this.supabaseService.healthCheck();
      
      // Load portfolios
      this.supabaseService.getAllPortfolios().subscribe({
        next: (data) => {
          this.portfolios = data;
          this.dataSource = isHealthy ? 'database' : 'mock';
          this.lastUpdated = new Date();
          this.loadHoldings();
        },
        error: (err) => {
          this.error = 'Failed to load portfolios';
          this.isLoading = false;
          this.dataSource = 'error';
          console.error('Portfolio loading error:', err);
        }
      });

    } catch (err) {
      this.error = 'Service initialization failed';
      this.isLoading = false;
      this.dataSource = 'error';
      console.error('Service initialization error:', err);
    }
  }

  loadHoldings(): void {
    if (this.portfolios.length > 0) {
      // Load holdings for the first portfolio
      this.supabaseService.getHoldingsByPortfolio(this.portfolios[0].id).subscribe({
        next: (data) => {
          this.holdings = data;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Holdings loading error:', err);
          this.isLoading = false; // Still complete loading even if holdings fail
        }
      });
    } else {
      this.isLoading = false;
    }
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  }

  formatPercentage(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'percent',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value / 100);
  }

  getTotalPortfolioValue(): number {
    return this.portfolios.reduce((sum, portfolio) => sum + portfolio.total_value, 0);
  }

  getTotalHoldings(): number {
    return this.holdings.length;
  }
}
