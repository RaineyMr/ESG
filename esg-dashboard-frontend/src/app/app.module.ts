import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppComponent } from './app.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { PortfolioListComponent } from './portfolio-list/portfolio-list.component';
import { PortfolioDetailComponent } from './portfolio-detail/portfolio-detail.component';
import { EsgMetricsComponent } from './esg-metrics/esg-metrics.component';
import { RiskAnalysisComponent } from './risk-analysis/risk-analysis.component';
import { HoldingsTableComponent } from './holdings-table/holdings-table.component';
import { ChartsComponent } from './charts/charts.component';

import { PortfolioService } from './services/portfolio.service';
import { EsgService } from './services/esg.service';
import { RiskService } from './services/risk.service';

import { Ng2ChartsModule } from 'ng2-charts';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    PortfolioListComponent,
    PortfolioDetailComponent,
    EsgMetricsComponent,
    RiskAnalysisComponent,
    HoldingsTableComponent,
    ChartsComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    Ng2ChartsModule
  ],
  providers: [
    PortfolioService,
    EsgService,
    RiskService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
