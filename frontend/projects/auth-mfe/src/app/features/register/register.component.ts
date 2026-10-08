import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { authRemoteActions } from '../../store/auth.actions';
import { selectRegistered, selectRemoteError, selectRemoteLoading } from '../../store/auth.selectors';

/** Self-registration (policyholder). Backend: POST /api/auth/register (gateway :8080). */
@Component({
  selector: 'auth-register',
  standalone: true,
  imports: [ReactiveFormsModule, AsyncPipe, RouterLink],
  template: `
    <div class="auth-wrapper">
      <section class="auth-card" aria-labelledby="register-title">
        <header class="auth-header">
          <div class="brand-badge" aria-hidden="true">I</div>
          <h1 id="register-title" class="auth-title">Create account</h1>
          <p class="auth-subtitle">Register a commercial policyholder account</p>
        </header>

        <form [formGroup]="form" (ngSubmit)="submit()" class="auth-form">
          <div class="form-field">
            <label for="displayName" class="form-label">Display name</label>
            <input
              id="displayName"
              class="form-input"
              formControlName="displayName"
              autocomplete="name"
              placeholder="e.g. Alice Customer"
            />
            @if (form.controls.displayName.touched && form.controls.displayName.invalid) {
              <p class="form-error">Display name is required (min 2 characters).</p>
            }
          </div>

          <div class="form-field">
            <label for="email" class="form-label">Email address</label>
            <input
              id="email"
              class="form-input"
              type="email"
              formControlName="email"
              autocomplete="email"
              placeholder="e.g. customer1@demo.com"
            />
            @if (form.controls.email.touched && form.controls.email.invalid) {
              <p class="form-error">Enter a valid email address.</p>
            }
          </div>

          <div class="form-field">
            <label for="password" class="form-label">Password</label>
            <input
              id="password"
              class="form-input"
              type="password"
              formControlName="password"
              autocomplete="new-password"
              placeholder="e.g. Demo@123"
            />
            <p class="form-hint">At least 8 characters with uppercase, lowercase, number, and special character (e.g. Demo&#64;123).</p>
            @if (form.controls.password.touched && form.controls.password.invalid) {
              <p class="form-error">
                Password must be at least 8 characters and include uppercase, lowercase, number, and special character.
              </p>
            }
          </div>

          @if (error$ | async; as err) {
            <div class="alert-error" role="alert">
              <strong>Registration error:</strong>
              <p>{{ err }}</p>
            </div>
          }

          @if (registered$ | async) {
            <div class="alert-success" role="status">
              <strong>Account created successfully!</strong>
              <p><a routerLink="/auth/login" class="underline font-bold">Click here to sign in with your credentials</a>.</p>
            </div>
          }

          <button
            class="submit-button"
            type="submit"
            [disabled]="form.invalid || (loading$ | async)"
          >
            {{ (loading$ | async) ? 'Creating account…' : 'Register' }}
          </button>

          <footer class="form-footer">
            <span>Already have an account?</span>
            <a routerLink="/auth/login" class="signin-link">Sign in here</a>
          </footer>
        </form>
      </section>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }

    .auth-wrapper {
      min-height: calc(100vh - 80px);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 40px 16px;
      background: var(--warm-light, #f7f5f3);
    }

    .auth-card {
      width: 100%;
      max-width: 440px;
      background: #ffffff;
      border: 1px solid var(--border, #eae5df);
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
      padding: 36px 32px;
    }

    .auth-header {
      text-align: center;
      margin-bottom: 28px;
    }

    .brand-badge {
      width: 44px;
      height: 44px;
      margin: 0 auto 14px;
      border-radius: 8px;
      background: var(--claret, #75013f);
      color: #ffffff;
      font-size: 20px;
      font-weight: 800;
      display: grid;
      place-items: center;
      box-shadow: 0 4px 12px rgba(117, 1, 63, 0.2);
    }

    .auth-title {
      font-size: 22px;
      font-weight: 700;
      letter-spacing: -0.03em;
      color: var(--ink, #000000);
      margin: 0 0 6px;
    }

    .auth-subtitle {
      font-size: 12px;
      color: var(--muted, #6f6a6d);
      margin: 0;
    }

    .auth-form {
      display: flex;
      flex-direction: column;
      gap: 18px;
    }

    .form-field {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .form-label {
      font-size: 11px;
      font-weight: 700;
      color: #272427;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .form-input {
      width: 100%;
      height: 44px;
      padding: 0 14px;
      border: 1px solid #d1d5db;
      border-radius: 7px;
      background: #ffffff;
      color: #111827;
      font-size: 13px;
      box-sizing: border-box;
      transition: border-color 0.15s ease, box-shadow 0.15s ease;
    }

    .form-input:focus {
      border-color: var(--claret, #75013f);
      outline: none;
      box-shadow: 0 0 0 3px rgba(117, 1, 63, 0.12);
    }

    .form-error {
      font-size: 11px;
      color: #e11d48;
      margin: 2px 0 0;
      font-weight: 500;
    }

    .form-hint {
      font-size: 10px;
      color: #6b7280;
      margin: 2px 0 0;
      line-height: 1.4;
    }

    .alert-error {
      padding: 12px 14px;
      background: #fde8e8;
      border: 1px solid #f8b4b4;
      border-radius: 6px;
      color: #9b1c1c;
      font-size: 11px;
    }
    .alert-error p {
      margin: 2px 0 0;
    }

    .alert-success {
      padding: 12px 14px;
      background: #eaf6f0;
      border: 1px solid #a7f3d0;
      border-radius: 6px;
      color: #065f46;
      font-size: 11px;
    }
    .alert-success p {
      margin: 2px 0 0;
    }

    .submit-button {
      height: 46px;
      border-radius: 7px;
      border: 1px solid var(--claret, #75013f);
      background: var(--claret, #75013f);
      color: #ffffff;
      font-size: 13px;
      font-weight: 700;
      cursor: pointer;
      transition: background 0.15s ease, transform 0.15s ease;
      box-shadow: 0 4px 12px rgba(117, 1, 63, 0.16);
      margin-top: 6px;
    }

    .submit-button:hover:not(:disabled) {
      background: #8f1750;
      transform: translateY(-1px);
    }

    .submit-button:disabled {
      opacity: 0.55;
      cursor: not-allowed;
      transform: none;
    }

    .form-footer {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      font-size: 12px;
      color: #4b5563;
      margin-top: 8px;
    }

    .signin-link {
      color: var(--claret, #75013f);
      font-weight: 700;
      text-decoration: underline;
    }
  `],
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly store = inject(Store);
  readonly loading$ = this.store.select(selectRemoteLoading);
  readonly error$ = this.store.select(selectRemoteError);
  readonly registered$ = this.store.select(selectRegistered);

  readonly form = this.fb.nonNullable.group({
    displayName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/),
      ],
    ],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.store.dispatch(authRemoteActions.register({ request: this.form.getRawValue() }));
  }
}
