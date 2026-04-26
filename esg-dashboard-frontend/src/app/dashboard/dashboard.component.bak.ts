import { Component, OnInit } from '@angular/core';
import { SupabaseService } from '../services/supabase.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  portfolios: any[] = [];
  holdings: any[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(
    private supabaseService: SupabaseService
  ) {}

  ngOnInit(): void {
    if (this.portfolio) {
      this.loadDashboardData();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.error = null;

    // Load portfolio summary
    const summarySub = this.portfolioService.getPortfolioSummary(this.portfolio.id)
      .subscribe({
        next: (summary) => {
          this.portfolioSummary = summary;
          this.loadAdditionalData();
        },
        error: (err) => {
          this.error = 'Failed to load portfolio summary';
          this.isLoading = false;
          console.error('Error loading portfolio summary:', err);
        }
      });

    this.subscriptions.push(summarySub);
  }

  loadAdditionalData(): void {
    // Load ESG distribution
    const esgSub = this.esgService.getEsgRatingDistribution(this.portfolio.id)
      .subscribe({
        next: (data) => {
          this.esgDistribution = data;
          this.prepareEsgChart();
        },
        error: (err) => console.error('Error loading ESG distribution:', err)
      });

    // Load high-risk holdings
    const riskSub = this.riskService.getHighRiskHoldings()
      .subscribe({
        next: (data) => {
          this.topRiskHoldings = data.slice(0, 5);
          this.prepareRiskChart();
        },
        error: (err) => console.error('Error loading risk holdings:', err)
      });

    // Load sector allocation
    const sectorSub = this.riskService.getRiskMetricsBySector(this.portfolio.id)
      .subscribe({
        next: (data) => {
          this.sectorAllocation = data;
          this.preparePerformanceChart();
        },
        error: (err) => console.error('Error loading sector allocation:', err)
      });

    this.subscriptions.push(esgSub, riskSub, sectorSub);
    
    setTimeout(() => {
      this.isLoading = false;
    }, 1000);
  }

  prepareEsgChart(): void {
    if (!this.esgDistribution || this.esgDistribution.length === 0) return;

    const labels = this.esgDistribution.map((item: any) => item[0]);
    const data = this.esgDistribution.map((item: any) => item[1]);

    this.esgChartData = {
      labels: labels,
      datasets: [{
        label: 'ESG Rating Distribution',
        data: data,
        backgroundColor: [
          '#28a745', // AAA - Green
          '#20c997', // AA - Teal
          '#17a2b8', // A - Blue
          '#ffc107', // BBB - Yellow
          '#fd7e14', // BB - Orange
          '#dc3545', // B - Red
          '#6f42c1'  // CCC/D - Purple
        ],
        borderWidth: 2,
        borderColor: '#fff'
      }]
    };
  }

  prepareRiskChart(): void {
    if (!this.topRiskHoldings || this.topRiskHoldings.length === 0) return;

    const labels = this.topRiskHoldings.map((holding: any) => 
      holding.holdingId ? `Holding ${holding.holdingId}` : 'Unknown');
    const data = this.topRiskHoldings.map((holding: any) => holding.volatility || 0);

    this.riskChartData = {
      labels: labels,
      datasets: [{
        label: 'Volatility (%)',
        data: data.map(v => v * 100),
        backgroundColor: 'rgba(220, 53, 69, 0.6)',
        borderColor: 'rgba(220, 53, 69, 1)',
        borderWidth: 2
      }]
    };
  }

  preparePerformanceChart(): void {
    if (!this.sectorAllocation || this.sectorAllocation.length === 0) return;

    const labels = this.sectorAllocation.map((item: any) => item[0]);
    const data = this.sectorAllocation.map((item: any) => item[4] || 0); // Count of holdings

    this.performanceChartData = {
      labels: labels,
      datasets: [{
        label: 'Holdings by Sector',
        data: data,
        backgroundColor: [
          '#007bff', '#28a745', '#ffc107', '#dc3545', '#6f42c1',
          '#fd7e14', '#20c997', '#17a2b8', '#e83e8c', '#6c757d'
        ],
        borderWidth: 2,
        borderColor: '#fff'
      }]
    };
  }

  getRiskLevelColor(riskLevel: string): string {
    switch (riskLevel?.toUpperCase()) {
      case 'LOW': return 'text-success';
      case 'MODERATE': return 'text-warning';
      case 'HIGH': return 'text-danger';
      case 'VERY_HIGH': return 'text-danger';
      default: return 'text-secondary';
    }
  }

  getEsgRatingColor(esgRating: string): string {
    switch (esgRating?.toUpperCase()) {
      case 'EXCELLENT':
      case 'AAA': return 'text-success';
      case 'GOOD':
      case 'AA': return 'text-info';
      case 'AVERAGE':
      case 'A': return 'text-primary';
      case 'BELOW_AVERAGE':
      case 'BBB': return 'text-warning';
      case 'POOR':
      case 'BB':
      case 'B':
      case 'CCC':
      case 'D': return 'text-danger';
      default: return 'text-secondary';
    }
  }

  formatPercentage(value: number): string {
    return (value * 100).toFixed(2) + '%';
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  }
}
