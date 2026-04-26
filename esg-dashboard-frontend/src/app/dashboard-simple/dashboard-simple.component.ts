import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-dashboard-simple',
  template: `
    <div style="padding: 20px;">
      <h2 style="color: #f1f5f9; margin-bottom: 30px;">Portfolio Overview</h2>
      
      <!-- Portfolio Stats Cards -->
      <div class="row mb-4">
        <div class="col-md-3 mb-3">
          <div class="stat-card" 
               [class.selected]="selectedCard === 'value'"
               [class.value-card]="true"
               (click)="showDetails('value')">
            <h3>$11.1M</h3>
            <p>Total Portfolio Value</p>
            <small>Click for details →</small>
          </div>
        </div>
        <div class="col-md-3 mb-3">
          <div class="stat-card" 
               [class.selected]="selectedCard === 'portfolios'"
               [class.portfolios-card]="true"
               (click)="showDetails('portfolios')">
            <h3>5</h3>
            <p>Portfolios</p>
            <small>Click for details →</small>
          </div>
        </div>
        <div class="col-md-3 mb-3">
          <div class="stat-card" 
               [class.selected]="selectedCard === 'holdings'"
               [class.holdings-card]="true"
               (click)="showDetails('holdings')">
            <h3>23</h3>
            <p>Holdings</p>
            <small>Click for details →</small>
          </div>
        </div>
        <div class="col-md-3 mb-3">
          <div class="stat-card" 
               [class.selected]="selectedCard === 'esg'"
               [class.esg-card]="true"
               (click)="showDetails('esg')">
            <h3>78.5</h3>
            <p>Avg ESG Score</p>
            <small>Click for details →</small>
          </div>
        </div>
      </div>

      <!-- Details Panel -->
      <div *ngIf="selectedCard" style="background: rgba(30, 41, 59, 0.8); padding: 20px; border-radius: 12px; margin-bottom: 20px;">
        <div style="display: flex; justify-content: between; align-items: center; margin-bottom: 20px;">
          <h3 style="color: #f1f5f9; margin: 0;">{{ getDetailsTitle() }}</h3>
          <button (click)="closeDetails()" style="background: none; border: none; color: #94a3b8; font-size: 1.5rem; cursor: pointer;">×</button>
        </div>
        
        <!-- Value Details -->
        <div *ngIf="selectedCard === 'value'">
          <div class="row">
            <div class="col-md-6">
              <p style="color: #94a3b8;">Total Value</p>
              <h4 style="color: #10b981; font-size: 1.5rem;">$11,100,000</h4>
            </div>
            <div class="col-md-6">
              <p style="color: #94a3b8;">Today's Change</p>
              <h4 style="color: #10b981; font-size: 1.5rem;">+$124,500 (+1.13%)</h4>
            </div>
          </div>
          <div class="row mt-3">
            <div class="col-md-6">
              <p style="color: #94a3b8;">Week Change</p>
              <h4 style="color: #10b981; font-size: 1.5rem;">+$342,000 (+3.18%)</h4>
            </div>
            <div class="col-md-6">
              <p style="color: #94a3b8;">Month Change</p>
              <h4 style="color: #10b981; font-size: 1.5rem;">+$1,380,000 (+14.2%)</h4>
            </div>
          </div>
        </div>

        <!-- Portfolios Details -->
        <div *ngIf="selectedCard === 'portfolios'">
          <div class="row">
            <div class="col-md-4" style="text-align: center; padding: 15px; background: rgba(16, 185, 129, 0.1); border-radius: 8px; margin-bottom: 15px;">
              <h5 style="color: #10b981;">Tech Growth</h5>
              <p style="color: #94a3b8; margin: 5px 0;">$4,200,000</p>
              <small style="color: #10b981;">+18.2%</small>
            </div>
            <div class="col-md-4" style="text-align: center; padding: 15px; background: rgba(8, 145, 178, 0.1); border-radius: 8px; margin-bottom: 15px;">
              <h5 style="color: #0891b2;">Sustainable Energy</h5>
              <p style="color: #94a3b8; margin: 5px 0;">$3,100,000</p>
              <small style="color: #0891b2;">+22.7%</small>
            </div>
            <div class="col-md-4" style="text-align: center; padding: 15px; background: rgba(139, 92, 246, 0.1); border-radius: 8px; margin-bottom: 15px;">
              <h5 style="color: #8b5cf6;">Healthcare Innovation</h5>
              <p style="color: #94a3b8; margin: 5px 0;">$2,800,000</p>
              <small style="color: #8b5cf6;">+15.3%</small>
            </div>
          </div>
          <div class="row">
            <div class="col-md-6" style="text-align: center; padding: 15px; background: rgba(245, 158, 11, 0.1); border-radius: 8px;">
              <h5 style="color: #f59e0b;">Financial Services</h5>
              <p style="color: #94a3b8; margin: 5px 0;">$1,000,000</p>
              <small style="color: #f59e0b;">+8.9%</small>
            </div>
            <div class="col-md-6" style="text-align: center; padding: 15px; background: rgba(239, 68, 68, 0.1); border-radius: 8px;">
              <h5 style="color: #ef4444;">Consumer Staples</h5>
              <p style="color: #94a3b8; margin: 5px 0;">$0,000</p>
              <small style="color: #ef4444;">+6.4%</small>
            </div>
          </div>
        </div>

        <!-- Holdings Details -->
        <div *ngIf="selectedCard === 'holdings'">
          <p style="color: #94a3b8;">Top 10 Holdings by Value</p>
          <div style="max-height: 300px; overflow-y: auto;">
            <div *ngFor="let holding of topHoldings" style="display: flex; justify-content: space-between; padding: 10px; border-bottom: 1px solid #334155;">
              <span style="color: #f1f5f9;">{{ holding.symbol }}</span>
              <span style="color: #94a3b8;">{{ holding.company }}</span>
              <span style="color: #10b981;">{{ holding.value }}</span>
            </div>
          </div>
        </div>

        <!-- ESG Details -->
        <div *ngIf="selectedCard === 'esg'">
          <div class="row">
            <div class="col-md-4">
              <h5 style="color: #10b981; margin-bottom: 15px;">Environmental Score: 82.3</h5>
              <ul style="color: #94a3b8; list-style: none; padding: 0;">
                <li>✓ Carbon Neutral Operations</li>
                <li>✓ Renewable Energy Usage</li>
                <li>✓ Waste Reduction Programs</li>
              </ul>
            </div>
            <div class="col-md-4">
              <h5 style="color: #0891b2; margin-bottom: 15px;">Social Score: 76.8</h5>
              <ul style="color: #94a3b8; list-style: none; padding: 0;">
                <li>✓ Employee Satisfaction</li>
                <li>✓ Community Engagement</li>
                <li>✓ Diversity & Inclusion</li>
              </ul>
            </div>
            <div class="col-md-4">
              <h5 style="color: #f59e0b; margin-bottom: 15px;">Governance Score: 71.4</h5>
              <ul style="color: #94a3b8; list-style: none; padding: 0;">
                <li>✓ Board Independence</li>
                <li>✓ Executive Compensation</li>
                <li>✓ Shareholder Rights</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- Holdings Table -->
      <div style="background: rgba(30, 41, 59, 0.8); padding: 20px; border-radius: 12px;">
        <h3 style="color: #f1f5f9; margin-bottom: 20px;">Top Holdings</h3>
        <div style="overflow-x: auto;">
          <table style="width: 100%; color: #f1f5f9;">
            <thead>
              <tr style="border-bottom: 1px solid #475569;">
                <th style="padding: 10px; text-align: left;">Symbol</th>
                <th style="padding: 10px; text-align: left;">Company</th>
                <th style="padding: 10px; text-align: right;">Value</th>
                <th style="padding: 10px; text-align: right;">ESG Score</th>
                <th style="padding: 10px; text-align: right;">Weight</th>
              </tr>
            </thead>
            <tbody>
              <tr style="border-bottom: 1px solid #334155;">
                <td style="padding: 10px;"><strong>AAPL</strong></td>
                <td style="padding: 10px;">Apple Inc.</td>
                <td style="padding: 10px; text-align: right;">$2,245,000</td>
                <td style="padding: 10px; text-align: right; color: #10b981;">85.2</td>
                <td style="padding: 10px; text-align: right;">20.2%</td>
              </tr>
              <tr style="border-bottom: 1px solid #334155;">
                <td style="padding: 10px;"><strong>MSFT</strong></td>
                <td style="padding: 10px;">Microsoft Corp.</td>
                <td style="padding: 10px; text-align: right;">$1,890,000</td>
                <td style="padding: 10px; text-align: right; color: #10b981;">82.7</td>
                <td style="padding: 10px; text-align: right;">17.0%</td>
              </tr>
              <tr style="border-bottom: 1px solid #334155;">
                <td style="padding: 10px;"><strong>TSLA</strong></td>
                <td style="padding: 10px;">Tesla Inc.</td>
                <td style="padding: 10px; text-align: right;">$1,332,000</td>
                <td style="padding: 10px; text-align: right; color: #f59e0b;">72.3</td>
                <td style="padding: 10px; text-align: right;">12.0%</td>
              </tr>
              <tr style="border-bottom: 1px solid #334155;">
                <td style="padding: 10px;"><strong>GOOGL</strong></td>
                <td style="padding: 10px;">Alphabet Inc.</td>
                <td style="padding: 10px; text-align: right;">$1,110,000</td>
                <td style="padding: 10px; text-align: right; color: #10b981;">78.9</td>
                <td style="padding: 10px; text-align: right;">10.0%</td>
              </tr>
              <tr>
                <td style="padding: 10px;"><strong>AMZN</strong></td>
                <td style="padding: 10px;">Amazon.com Inc.</td>
                <td style="padding: 10px; text-align: right;">$999,000</td>
                <td style="padding: 10px; text-align: right; color: #f59e0b;">75.1</td>
                <td style="padding: 10px; text-align: right;">9.0%</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Bottom Stats Section -->
      <div class="row mt-4">
        <div class="col-md-6 mb-3">
          <div style="background: rgba(30, 41, 59, 0.8); padding: 20px; border-radius: 12px;">
            <h3 style="color: #f1f5f9; margin-bottom: 20px;">Portfolio Performance</h3>
            <div style="margin-bottom: 15px;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                <span style="color: #94a3b8;">1 Month Return</span>
                <span style="color: #10b981; font-weight: 600;">+12.4%</span>
              </div>
              <div style="background: #334155; height: 8px; border-radius: 4px; overflow: hidden;">
                <div style="background: #10b981; height: 100%; width: 62%;"></div>
              </div>
            </div>
            <div style="margin-bottom: 15px;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                <span style="color: #94a3b8;">3 Month Return</span>
                <span style="color: #10b981; font-weight: 600;">+18.7%</span>
              </div>
              <div style="background: #334155; height: 8px; border-radius: 4px; overflow: hidden;">
                <div style="background: #10b981; height: 100%; width: 75%;"></div>
              </div>
            </div>
            <div>
              <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                <span style="color: #94a3b8;">YTD Return</span>
                <span style="color: #10b981; font-weight: 600;">+24.2%</span>
              </div>
              <div style="background: #334155; height: 8px; border-radius: 4px; overflow: hidden;">
                <div style="background: #10b981; height: 100%; width: 85%;"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-6 mb-3">
          <div style="background: rgba(30, 41, 59, 0.8); padding: 20px; border-radius: 12px;">
            <h3 style="color: #f1f5f9; margin-bottom: 20px;">ESG Breakdown</h3>
            <div style="margin-bottom: 15px;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                <span style="color: #94a3b8;">Environmental</span>
                <span style="color: #10b981; font-weight: 600;">82.3</span>
              </div>
              <div style="background: #334155; height: 8px; border-radius: 4px; overflow: hidden;">
                <div style="background: #10b981; height: 100%; width: 82%;"></div>
              </div>
            </div>
            <div style="margin-bottom: 15px;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                <span style="color: #94a3b8;">Social</span>
                <span style="color: #0891b2; font-weight: 600;">76.8</span>
              </div>
              <div style="background: #334155; height: 8px; border-radius: 4px; overflow: hidden;">
                <div style="background: #0891b2; height: 100%; width: 77%;"></div>
              </div>
            </div>
            <div>
              <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                <span style="color: #94a3b8;">Governance</span>
                <span style="color: #f59e0b; font-weight: 600;">71.4</span>
              </div>
              <div style="background: #334155; height: 8px; border-radius: 4px; overflow: hidden;">
                <div style="background: #f59e0b; height: 100%; width: 71%;"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Risk Metrics -->
      <div style="background: rgba(30, 41, 59, 0.8); padding: 20px; border-radius: 12px; margin-top: 20px;">
        <h3 style="color: #f1f5f9; margin-bottom: 20px;">Risk Metrics</h3>
        <div class="row">
          <div class="col-md-3">
            <div style="text-align: center;">
              <h4 style="color: #f59e0b; font-size: 1.5rem; margin-bottom: 10px;">0.68</h4>
              <p style="color: #94a3b8; margin: 0;">Beta</p>
            </div>
          </div>
          <div class="col-md-3">
            <div style="text-align: center;">
              <h4 style="color: #10b981; font-size: 1.5rem; margin-bottom: 10px;">14.2%</h4>
              <p style="color: #94a3b8; margin: 0;">Sharpe Ratio</p>
            </div>
          </div>
          <div class="col-md-3">
            <div style="text-align: center;">
              <h4 style="color: #0891b2; font-size: 1.5rem; margin-bottom: 10px;">18.5%</h4>
              <p style="color: #94a3b8; margin: 0;">Volatility</p>
            </div>
          </div>
          <div class="col-md-3">
            <div style="text-align: center;">
              <h4 style="color: #8b5cf6; font-size: 1.5rem; margin-bottom: 10px;">Low</h4>
              <p style="color: #94a3b8; margin: 0;">Risk Level</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }
    
    .row {
      display: flex;
      flex-wrap: wrap;
      margin: 0 -10px;
    }
    
    .col-md-3 {
      flex: 0 0 25%;
      padding: 0 10px;
    }
    
    .col-md-4 {
      flex: 0 0 33.333%;
      padding: 0 10px;
    }
    
    .col-md-6 {
      flex: 0 0 50%;
      padding: 0 10px;
    }
    
    .stat-card {
      background: rgba(30, 41, 59, 0.8);
      padding: 20px;
      border-radius: 12px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 2px solid transparent;
    }
    
    .stat-card:hover {
      background: rgba(30, 41, 59, 1);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    }
    
    .stat-card.selected {
      border-color: currentColor;
    }
    
    .stat-card h3 {
      font-size: 2rem;
      margin-bottom: 10px;
      font-weight: 700;
    }
    
    .stat-card p {
      margin: 0;
      font-size: 0.9rem;
    }
    
    .stat-card small {
      color: #64748b;
      display: block;
      margin-top: 10px;
      font-size: 0.8rem;
    }
    
    .value-card {
      border-color: #10b981;
    }
    
    .value-card h3 {
      color: #10b981;
    }
    
    .portfolios-card {
      border-color: #0891b2;
    }
    
    .portfolios-card h3 {
      color: #0891b2;
    }
    
    .holdings-card {
      border-color: #f59e0b;
    }
    
    .holdings-card h3 {
      color: #f59e0b;
    }
    
    .esg-card {
      border-color: #8b5cf6;
    }
    
    .esg-card h3 {
      color: #8b5cf6;
    }
    
    .stat-card p {
      color: #94a3b8;
    }
    
    @media (max-width: 768px) {
      .col-md-3 {
        flex: 0 0 50%;
      }
      
      .col-md-4 {
        flex: 0 0 50%;
      }
      
      .col-md-6 {
        flex: 0 0 100%;
      }
    }
    
    @media (max-width: 480px) {
      .col-md-3 {
        flex: 0 0 100%;
      }
      
      .col-md-4 {
        flex: 0 0 100%;
      }
    }
  `]
})
export class DashboardSimpleComponent implements OnInit {
  selectedCard: string | null = null;
  
  topHoldings = [
    { symbol: 'AAPL', company: 'Apple Inc.', value: '$2,245,000' },
    { symbol: 'MSFT', company: 'Microsoft Corp.', value: '$1,890,000' },
    { symbol: 'TSLA', company: 'Tesla Inc.', value: '$1,332,000' },
    { symbol: 'GOOGL', company: 'Alphabet Inc.', value: '$1,110,000' },
    { symbol: 'AMZN', company: 'Amazon.com Inc.', value: '$999,000' },
    { symbol: 'META', company: 'Meta Platforms', value: '$890,000' },
    { symbol: 'NVDA', company: 'NVIDIA Corp.', value: '$780,000' },
    { symbol: 'JPM', company: 'JPMorgan Chase', value: '$650,000' },
    { symbol: 'V', company: 'Visa Inc.', value: '$540,000' },
    { symbol: 'JNJ', company: 'Johnson & Johnson', value: '$464,000' }
  ];

  constructor() { }

  ngOnInit(): void {
    // Static data - no external dependencies
  }

  showDetails(card: string): void {
    this.selectedCard = card;
  }

  closeDetails(): void {
    this.selectedCard = null;
  }

  getDetailsTitle(): string {
    switch (this.selectedCard) {
      case 'value':
        return 'Portfolio Value Details';
      case 'portfolios':
        return 'Portfolio Breakdown';
      case 'holdings':
        return 'Holdings Details';
      case 'esg':
        return 'ESG Score Breakdown';
      default:
        return '';
    }
  }
}
