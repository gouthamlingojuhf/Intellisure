import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CardComponent, ButtonComponent, InputComponent } from 'ui-core';
import { AuthApiService } from './services/auth-api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, CardComponent, ButtonComponent, InputComponent],
  template: `
    <div class="auth-layout">
      <is-card class="auth-card">
        <header class="auth-header">
          <div class="auth-logo" aria-hidden="true">I</div>
          <h1>Welcome to IntelliSure</h1>
          <p>Sign in to access your commercial insurance workspace</p>
        </header>

        <div class="auth-body">
          @if (errorMessage) {
            <div class="alert alert-error" role="alert">{{ errorMessage }}</div>
          }
          @if (successMessage) {
            <div class="alert alert-success" role="alert">{{ successMessage }}</div>
          }

          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="space-y-4">
            <div class="form-group">
              <label for="email" class="form-label">Email address</label>
              <is-input
                id="email"
                type="email"
                formControlName="email"
                placeholder="you@company.com"
                [error]="getError('email')"
                [hint]="'We will never share your email'"
              />
            </div>

            <div class="form-group">
              <label for="password" class="form-label">Password</label>
              <is-input
                id="password"
                type="password"
                formControlName="password"
                placeholder="Enter your password"
                [error]="getError('password')"
              />
            </div>

            <div class="form-group" style="display: flex; align-items: center; justify-content: space-between;">
              <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                <input type="checkbox" formControlName="rememberMe" style="width: 16px; height: 16px; accent-color: var(--claret);">
                <span style="font-size: 12px; color: var(--muted);">Remember me</span>
              </label>
              <a routerLink="/forgot-password" style="font-size: 12px; color: var(--claret); font-weight: 600; text-decoration: none;">Forgot password?</a>
            </div>

            <is-button variant="primary" type="submit" [disabled]="loginForm.invalid || loading">
              @if (loading) {
                <span class="spinner" aria-hidden="true"></span>
                Signing in...
              } @else {
                Sign in
              }
            </is-button>
          </form>

          <div class="divider">or continue with</div>

          <div class="social-buttons">
            <button type="button" class="social-btn" (click)="loginWithProvider('google')">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
              Google
            </button>
            <button type="button" class="social-btn" (click)="loginWithProvider('microsoft')">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#00A1F1" d="M12.5 0h-5v5h5V0zm0 7h-5v5h5V7zm0 7h-5v5h5v-5zm7-7h-5v5h5V7zm0 7h-5v5h5v-5zm7-7h-5v5h5V7zm0 7h-5v5h5v-5zm-7 14h-5v5h5v-5zm0-7h-5v5h5v-5zm0-7h-5v5h5V7z"/></svg>
              Microsoft
            </button>
          </div>
        </div>

        <footer class="auth-footer">
          <p>Don't have an account? <a routerLink="/register">Create one</a></p>
        </footer>
      </is-card>
    </div>
  `,
  styles: [`
    .spinner {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255,255,255,0.3);
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `],
})
export class AppComponent {
  private readonly fb = new FormBuilder();
  private readonly router = inject(Router);
  private readonly authApi = inject(AuthApiService);

  loading = false;
  errorMessage = '';
  successMessage = '';

  loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    rememberMe: [false],
  });

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { email, password } = this.loginForm.getRawValue();

    this.authApi.login({ email, password }).subscribe({
      next: (res) => {
        this.loading = false;
        const token = res?.accessToken ?? res?.token;
        if (typeof localStorage !== 'undefined' && token) {
          localStorage.setItem('is_token', token);
          if (res.role) localStorage.setItem('is_role', res.role);
          if (res.userId) localStorage.setItem('is_user_id', res.userId);
          if (res.customerId) localStorage.setItem('is_customer_id', res.customerId);
          if (res.email) localStorage.setItem('is_email', res.email);
        }
        this.successMessage = 'Sign in successful. Redirecting…';
        this.router.navigateByUrl(res?.customerId ? '/dashboard' : '/profile');
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || err?.message || 'Invalid credentials. Please try again.';
      },
    });
  }

  loginWithProvider(provider: string): void {
    // Implement OAuth flow
    console.log(`Login with ${provider}`);
  }

  getError(controlName: string): string | undefined {
    const control = this.loginForm.get(controlName);
    if (control?.invalid && (control.dirty || control.touched)) {
      if (control.errors?.['required']) return `${controlName === 'email' ? 'Email' : 'Password'} is required`;
      if (control.errors?.['email']) return 'Enter a valid email address';
      if (control.errors?.['minlength']) return 'Password must be at least 8 characters';
    }
    return undefined;
  }
}