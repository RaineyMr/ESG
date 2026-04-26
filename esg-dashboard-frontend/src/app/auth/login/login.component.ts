import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    // Redirect if already logged in
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.markFormGroupTouched(this.loginForm);
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    const { email, password } = this.loginForm.value;

    this.authService.signIn(email, password).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.error) {
          this.errorMessage = this.getErrorMessage(response.error);
        } else if (response.user) {
          this.successMessage = 'Login successful! Redirecting...';
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 1500);
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'An unexpected error occurred. Please try again.';
        console.error('Login error:', error);
      }
    });
  }

  private getErrorMessage(error: any): string {
    if (error.message) {
      if (error.message.includes('Invalid login credentials')) {
        return 'Invalid email or password. Please try again.';
      } else if (error.message.includes('Email not confirmed')) {
        return 'Please confirm your email address before logging in.';
      } else if (error.message.includes('Too many requests')) {
        return 'Too many login attempts. Please try again later.';
      }
    }
    return 'Login failed. Please check your credentials and try again.';
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  get email() { return this.loginForm.get('email'); }
  get password() { return this.loginForm.get('password'); }
}
