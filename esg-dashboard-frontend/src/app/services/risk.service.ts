import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RiskMetric {
  id: number;
  holdingId: number;
  calculationDate: string;
  beta?: number;
  volatility?: number;
  valueAtRisk?: number;
  maxDrawdown?: number;
  sharpeRatio?: number;
  riskRating: string;
  timeHorizonDays: number;
  createdAt: string;
  riskLevelDescription?: string;
  annualizedVolatility?: number;
  isHighBeta?: boolean;
  isHighVolatility?: boolean;
  hasNegativeSharpeRatio?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class RiskService {
  private apiUrl = 'http://localhost:8080/api/risk';

  constructor(private http: HttpClient) {}

  addRiskMetric(holdingId: number, riskMetric: Partial<RiskMetric>): Observable<RiskMetric> {
    return this.http.post<RiskMetric>(`${this.apiUrl}/holdings/${holdingId}/risk-metric`, riskMetric);
  }

  getRiskMetricsForHolding(holdingId: number): Observable<RiskMetric[]> {
    return this.http.get<RiskMetric[]>(`${this.apiUrl}/holdings/${holdingId}/metrics`);
  }

  getLatestRiskMetricForHolding(holdingId: number): Observable<RiskMetric> {
    return this.http.get<RiskMetric>(`${this.apiUrl}/holdings/${holdingId}/latest-metric`);
  }

  getRiskMetricsForPortfolio(portfolioId: number): Observable<RiskMetric[]> {
    return this.http.get<RiskMetric[]>(`${this.apiUrl}/portfolios/${portfolioId}/metrics`);
  }

  getRiskTrendForHolding(holdingId: number, startDate?: string, endDate?: string): Observable<RiskMetric[]> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    
    return this.http.get<RiskMetric[]>(`${this.apiUrl}/holdings/${holdingId}/trend`, { params });
  }

  getRiskRatingDistribution(portfolioId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/portfolios/${portfolioId}/rating-distribution`);
  }

  getRiskAverages(portfolioId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/portfolios/${portfolioId}/averages`);
  }

  getHighRiskHoldings(riskRatings: string = 'HIGH,VERY_HIGH'): Observable<RiskMetric[]> {
    return this.http.get<RiskMetric[]>(`${this.apiUrl}/holdings/high-risk?riskRatings=${riskRatings}`);
  }

  getHighBetaHoldings(minBeta: number = 1.5): Observable<RiskMetric[]> {
    return this.http.get<RiskMetric[]>(`${this.apiUrl}/holdings/high-beta?minBeta=${minBeta}`);
  }

  getHighVolatilityHoldings(minVolatility: number = 0.30): Observable<RiskMetric[]> {
    return this.http.get<RiskMetric[]>(`${this.apiUrl}/holdings/high-volatility?minVolatility=${minVolatility}`);
  }

  getNegativeSharpeHoldings(): Observable<RiskMetric[]> {
    return this.http.get<RiskMetric[]>(`${this.apiUrl}/holdings/negative-sharpe`);
  }

  getRiskMetricsBySector(portfolioId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/portfolios/${portfolioId}/by-sector`);
  }

  calculatePortfolioRiskScore(portfolioId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/portfolios/${portfolioId}/risk-score`);
  }

  calculatePortfolioBeta(portfolioId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/portfolios/${portfolioId}/beta`);
  }

  calculatePortfolioValueAtRisk(portfolioId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/portfolios/${portfolioId}/var`);
  }
}
