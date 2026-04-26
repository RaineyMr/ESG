import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'ESG Portfolio Dashboard';

  constructor() {}

  // Temporarily disabled for troubleshooting
  isLoggedIn(): boolean {
    return true; // Always show navigation for now
  }

  getCurrentUserEmail(): string {
    return 'Guest User';
  }

  signOut(): void {
    // Temporarily disabled
    console.log('Sign out clicked - authentication disabled for troubleshooting');
  }
}
