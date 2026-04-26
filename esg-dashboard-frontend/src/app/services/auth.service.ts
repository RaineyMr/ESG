import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { SupabaseService } from './supabase.service';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface User {
  id: string;
  email: string;
  created_at: string;
  user_metadata?: any;
}

export interface AuthResponse {
  user: User | null;
  session: any | null;
  error: any | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);

  currentUser$ = this.currentUserSubject.asObservable();
  isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private supabaseService: SupabaseService,
    private router: Router
  ) {
    // Initialize with default values
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    
    // Wait for Supabase to be ready before initializing auth
    this.waitForSupabaseAndInit();
  }

  private async waitForSupabaseAndInit(): Promise<void> {
    // Poll for Supabase to be ready
    const maxAttempts = 10;
    let attempts = 0;
    
    while (attempts < maxAttempts) {
      if (this.supabaseService.isReady()) {
        await this.initializeAuth();
        return;
      }
      attempts++;
      await new Promise(resolve => setTimeout(resolve, 500));
    }
    
    console.error('Supabase failed to initialize after multiple attempts');
  }

  private async initializeAuth(): Promise<void> {
    try {
      const { data: { session } } = await this.supabaseService.supabaseClient.auth.getSession();
      
      if (session?.user) {
        this.currentUserSubject.next(session.user as User);
        this.isAuthenticatedSubject.next(true);
      } else {
        this.currentUserSubject.next(null);
        this.isAuthenticatedSubject.next(false);
      }
    } catch (error) {
      console.error('Error initializing auth:', error);
      this.currentUserSubject.next(null);
      this.isAuthenticatedSubject.next(false);
    }

    // Listen for auth changes
    this.supabaseService.supabaseClient.auth.onAuthStateChange((event, session) => {
      if (event === 'SIGNED_IN' && session?.user) {
        this.currentUserSubject.next(session.user as User);
        this.isAuthenticatedSubject.next(true);
      } else if (event === 'SIGNED_OUT') {
        this.currentUserSubject.next(null);
        this.isAuthenticatedSubject.next(false);
      }
    });
  }

  signUp(email: string, password: string, metadata?: any): Observable<AuthResponse> {
    return new Observable<AuthResponse>(observer => {
      this.supabaseService.supabaseClient.auth.signUp({
        email,
        password,
        options: {
          data: metadata
        }
      }).then(response => {
        const authResponse: AuthResponse = {
          user: response.data.user as User | null,
          session: response.data.session,
          error: response.error
        };
        
        if (response.data.user) {
          this.currentUserSubject.next(response.data.user as User);
          this.isAuthenticatedSubject.next(true);
        }
        
        observer.next(authResponse);
        observer.complete();
      }).catch(error => {
        observer.error(error);
        observer.complete();
      });
    }).pipe(
      catchError(error => {
        console.error('Sign up error:', error);
        return throwError(() => error);
      })
    );
  }

  signIn(email: string, password: string): Observable<AuthResponse> {
    return new Observable<AuthResponse>(observer => {
      this.supabaseService.supabaseClient.auth.signInWithPassword({
        email,
        password
      }).then(response => {
        const authResponse: AuthResponse = {
          user: response.data.user as User | null,
          session: response.data.session,
          error: response.error
        };
        
        if (response.data.user) {
          this.currentUserSubject.next(response.data.user as User);
          this.isAuthenticatedSubject.next(true);
        }
        
        observer.next(authResponse);
        observer.complete();
      }).catch(error => {
        observer.error(error);
        observer.complete();
      });
    }).pipe(
      catchError(error => {
        console.error('Sign in error:', error);
        return throwError(() => error);
      })
    );
  }

  signOut(): Observable<void> {
    return new Observable<void>(observer => {
      this.supabaseService.supabaseClient.auth.signOut().then(() => {
        this.currentUserSubject.next(null);
        this.isAuthenticatedSubject.next(false);
        this.router.navigate(['/login']);
        observer.next();
        observer.complete();
      }).catch(error => {
        observer.error(error);
        observer.complete();
      });
    }).pipe(
      catchError(error => {
        console.error('Sign out error:', error);
        return throwError(() => error);
      })
    );
  }

  resetPassword(email: string): Observable<{ success: boolean; error?: any }> {
    return new Observable<{ success: boolean; error?: any }>(observer => {
      this.supabaseService.supabaseClient.auth.resetPasswordForEmail(email).then(response => {
        observer.next({ success: !response.error, error: response.error });
        observer.complete();
      }).catch(error => {
        observer.error(error);
        observer.complete();
      });
    }).pipe(
      catchError(error => {
        console.error('Reset password error:', error);
        return throwError(() => error);
      })
    );
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  getUserId(): string | null {
    const user = this.getCurrentUser();
    return user ? user.id : null;
  }
}
