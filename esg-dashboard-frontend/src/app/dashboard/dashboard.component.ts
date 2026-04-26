import { Component, OnInit, Input } from '@angular/core';
import { SupabaseService } from '../services/supabase.service';

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

  constructor(
    private supabaseService: SupabaseService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.error = null;

    // Load portfolios
    this.supabaseService.getAllPortfolios().subscribe({
      next: (data) => {
        this.portfolios = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load portfolios';
        this.isLoading = false;
        console.error(err);
      }
    });

    // Load holdings (using first portfolio for demo)
    if (this.portfolios.length > 0) {
      this.supabaseService.getHoldingsByPortfolio(this.portfolios[0].id).subscribe({
        next: (data) => {
          this.holdings = data;
        },
        error: (err) => {
          console.error('Failed to load holdings:', err);
        }
      });
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
