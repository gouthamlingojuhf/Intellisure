import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { authRemoteActions } from '../../store/auth.actions';
import { selectRemoteError, selectRemoteLoading } from '../../store/auth.selectors';

/** Policyholder + staff login. Backend: POST /api/auth/login (gateway :8080). */
@Component({
  selector: 'auth-login',
  standalone: true,
  imports: [ReactiveFormsModule, AsyncPipe],
  template: `
    <section class="card max-w-md mx-auto">
      <h1 class="text-xl font-bold text-blue-900">Login</h1>
      <form [formGroup]="form" (ngSubmit)="submit()" class="mt-4 space-y-3">
        <div>
          <label class="block text-sm font-semibold">Email</label>
          <input class="input-field" type="email" formControlName="email" autocomplete="username" />
          @if (form.controls.email.touched && form.controls.email.invalid) {
            <p class="text-sm text-rose-600">Enter a valid email address.</p>
          }
        </div>
        <div>
          <label class="block text-sm font-semibold">Password</label>
          <input class="input-field" type="password" formControlName="password" autocomplete="current-password" />
          @if (form.controls.password.touched && form.controls.password.invalid) {
            <p class="text-sm text-rose-600">Password must be at least 8 characters.</p>
          }
        </div>
        @if (error$ | async; as err) {
          <p class="text-sm text-rose-600">{{ err }}</p>
        }
        <button class="btn-primary w-full" type="submit" [disabled]="form.invalid || (loading$ | async)">
          Login
        </button>
      </form>
    </section>
  `,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly store = inject(Store);
  readonly loading$ = this.store.select(selectRemoteLoading);
  readonly error$ = this.store.select(selectRemoteError);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    alert('login form value: ' + JSON.stringify(this.form.getRawValue()));
    this.store.dispatch(authRemoteActions.login({ request: this.form.getRawValue() }));
  }
}
