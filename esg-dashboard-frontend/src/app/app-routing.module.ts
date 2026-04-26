import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { DashboardSimpleComponent } from './dashboard-simple/dashboard-simple.component';
import { TestComponent } from './test.component';

const routes: Routes = [
  { 
    path: '', 
    redirectTo: '/simple', 
    pathMatch: 'full'
  },
  { 
    path: 'dashboard', 
    component: DashboardComponent
  },
  { 
    path: 'simple', 
    component: DashboardSimpleComponent
  },
  { 
    path: 'test', 
    component: TestComponent
  },
  { 
    path: '**', 
    redirectTo: '/simple' 
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
