import { Component, OnInit } from '@angular/core';
import { PortfolioService } from './services/portfolio.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'ESG Portfolio Dashboard';
  isLoading = true;
  portfolios: any[] = [];
  selectedPortfolio: any = null;

  constructor(private portfolioService: PortfolioService) {}

  ngOnInit(): void {
    this.loadPortfolios();
  }

  loadPortfolios(): void {
    this.portfolioService.getAllPortfolios().subscribe({
      next: (data) => {
        this.portfolios = data;
        this.isLoading = false;
        if (this.portfolios.length > 0) {
          this.selectedPortfolio = this.portfolios[0];
        }
      },
      error: (error) => {
        console.error('Error loading portfolios:', error);
        this.isLoading = false;
      }
    });
  }

  onPortfolioSelect(portfolio: any): void {
    this.selectedPortfolio = portfolio;
  }
}
