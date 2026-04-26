import { Component } from '@angular/core';

@Component({
  selector: 'app-test',
  template: `
    <div style="padding: 20px; text-align: center; background: rgba(30, 41, 59, 0.8); border-radius: 12px; margin: 20px auto; max-width: 600px;">
      <h1 style="color: #0891b2; margin-bottom: 20px;">ESG Dashboard - Test Page</h1>
      <p style="color: #94a3b8; margin-bottom: 20px;">If you can see this page, the application is working correctly.</p>
      <p style="color: #f1f5f9; font-size: 18px; margin-bottom: 20px;">Current time: {{ currentTime }}</p>
      <button (click)="updateTime()" 
              style="background: linear-gradient(135deg, #0891b2 0%, #06b6d4 100%); 
                     color: white; 
                     border: none; 
                     padding: 12px 24px; 
                     border-radius: 8px; 
                     font-weight: 600;
                     cursor: pointer;">
        Update Time
      </button>
    </div>
  `,
  styles: [`
    h1 { 
      font-weight: 700; 
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    }
    p { 
      margin: 10px 0; 
      line-height: 1.6;
    }
    button:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(8, 145, 178, 0.4);
      transition: all 0.3s ease;
    }
  `]
})
export class TestComponent {
  currentTime = new Date().toLocaleString();

  updateTime() {
    this.currentTime = new Date().toLocaleString();
  }
}
