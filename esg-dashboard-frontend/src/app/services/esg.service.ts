import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EsgScore {
  id: number;
  holdingId: number;
  scoreDate: string;
  overallScore: number;
  environmentalPillar: number;
  socialPillar: number;
  governancePillar: number;
  controversyLevel: string;
  dataProvider?: string;
  createdAt: string;
  esgRating?: string;
  pillarAverage?: number;
}

export interface EsgMetric {
  id: number;
  portfolioId: number;
  metricDate: string;
  environmentalScore: number;
  socialScore: number;
  governanceScore: number;
  overallEsgScore: number;
  controversyCount: number;
  createdAt: string;
  esgRating?: string;
  controversyLevel?: string;
}

@Injectable({
  providedIn: 'root'
})
export class EsgService {
  private apiUrl = 'http://localhost:8080/api/esg';

  constructor(private http: HttpClient) {}

  updateEsgScore(holdingId: number, esgScore: Partial<EsgScore>): Observable<EsgScore> {
    return this.http.post<EsgScore>(`${this.apiUrl}/holdings/${holdingId}/esg-score`, esgScore);
  }

  getEsgScoresForHolding(holdingId: number): Observable<EsgScore[]> {
    return this.http.get<EsgScore[]>(`${this.apiUrl}/holdings/${holdingId}/scores`);
  }

  getLatestEsgScoreForHolding(holdingId: number): Observable<EsgScore> {
    return this.http.get<EsgScore>(`${this.apiUrl}/holdings/${holdingId}/latest-score`);
  }

  getEsgScoresForPortfolio(portfolioId: number): Observable<EsgScore[]> {
    return this.http.get<EsgScore[]>(`${this.apiUrl}/portfolios/${portfolioId}/scores`);
  }

  getEsgTrendForHolding(holdingId: number, startDate?: string, endDate?: string): Observable<EsgScore[]> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    
    return this.http.get<EsgScore[]>(`${this.apiUrl}/holdings/${holdingId}/trend`, { params });
  }

  getEsgRatingDistribution(portfolioId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/portfolios/${portfolioId}/rating-distribution`);
  }

  getEsgPillarAverages(portfolioId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/portfolios/${portfolioId}/pillar-averages`);
  }

  searchEsgScores(params: {
    holdingId?: number;
    startDate?: string;
    endDate?: string;
    minOverallScore?: number;
    maxOverallScore?: number;
    controversyLevel?: string;
    dataProvider?: string;
  }): Observable<EsgScore[]> {
    let httpParams = new HttpParams();
    
    Object.keys(params).forEach(key => {
      const value = params[key as keyof typeof params];
      if (value !== undefined && value !== null) {
        httpParams = httpParams.set(key, value.toString());
      }
    });

    return this.http.get<EsgScore[]>(`${this.apiUrl}/scores/search`, { params: httpParams });
  }

  getHoldingsWithHighEsgScores(minScore: number = 70): Observable<EsgScore[]> {
    return this.http.get<EsgScore[]>(`${this.apiUrl}/holdings/high-scores?minScore=${minScore}`);
  }

  getHoldingsWithControversies(): Observable<EsgScore[]> {
    return this.http.get<EsgScore[]>(`${this.apiUrl}/holdings/controversies`);
  }
}
