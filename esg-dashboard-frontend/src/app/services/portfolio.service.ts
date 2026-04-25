import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Portfolio {
  id: number;
  portfolioName: string;
  description?: string;
  totalValue: number;
  baseCurrency: string;
  inceptionDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface PortfolioSummary {
  portfolioId: number;
  portfolioName: string;
  totalValue: number;
  baseCurrency: string;
  inceptionDate: string;
  holdingCount: number;
  averageEsgScore: number;
  overallRiskLevel: string;
  averageBeta: number;
  averageVolatility: number;
  esgRating?: string;
  controversyCount?: number;
  totalUnrealizedGainLoss?: number;
  totalUnrealizedGainLossPercentage?: number;
}

export interface Holding {
  id: number;
  portfolioId: number;
  tickerSymbol: string;
  companyName: string;
  sector: string;
  quantity: number;
  purchasePrice: number;
  currentPrice: number;
  marketValue: number;
  weightInPortfolio: number;
  unrealizedGainLoss?: number;
  unrealizedGainLossPercentage?: number;
}

@Injectable({
  providedIn: 'root'
})
export class PortfolioService {
  private apiUrl = 'http://localhost:8080/api/portfolios';

  constructor(private http: HttpClient) {}

  getAllPortfolios(): Observable<Portfolio[]> {
    return this.http.get<Portfolio[]>(this.apiUrl);
  }

  getPortfolioById(id: number): Observable<Portfolio> {
    return this.http.get<Portfolio>(`${this.apiUrl}/${id}`);
  }

  createPortfolio(portfolio: Partial<Portfolio>): Observable<Portfolio> {
    return this.http.post<Portfolio>(this.apiUrl, portfolio);
  }

  updatePortfolio(id: number, portfolio: Partial<Portfolio>): Observable<Portfolio> {
    return this.http.put<Portfolio>(`${this.apiUrl}/${id}`, portfolio);
  }

  deletePortfolio(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getPortfolioSummary(id: number): Observable<PortfolioSummary> {
    return this.http.get<PortfolioSummary>(`${this.apiUrl}/${id}/summary`);
  }

  addHoldingToPortfolio(portfolioId: number, holding: Partial<Holding>): Observable<Holding> {
    return this.http.post<Holding>(`${this.apiUrl}/${portfolioId}/holdings`, holding);
  }

  searchPortfolios(params: {
    portfolioName?: string;
    baseCurrency?: string;
    minValue?: number;
    maxValue?: number;
    page?: number;
    size?: number;
  }): Observable<any> {
    let httpParams = new HttpParams();
    
    Object.keys(params).forEach(key => {
      const value = params[key as keyof typeof params];
      if (value !== undefined && value !== null) {
        httpParams = httpParams.set(key, value.toString());
      }
    });

    return this.http.get(`${this.apiUrl}/search`, { params: httpParams });
  }

  getPortfolioStatistics(): Observable<any> {
    return this.http.get(`${this.apiUrl}/statistics`);
  }
}
