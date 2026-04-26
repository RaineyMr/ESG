import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardSimpleComponent } from './dashboard-simple/dashboard-simple.component';
import { TestComponent } from './test.component';

const routes: Routes = [
  { 
    path: '', 
    redirectTo: '/dashboard', 
    pathMatch: 'full'
  },
  { 
    path: 'dashboard', 
    component: DashboardSimpleComponent
  },
  { 
    path: 'test', 
    component: TestComponent
  },
  { 
    path: '**', 
    redirectTo: '/dashboard' 
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
