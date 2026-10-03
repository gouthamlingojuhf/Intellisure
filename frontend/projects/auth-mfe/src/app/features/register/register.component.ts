import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { authRemoteActions } from '../../store/auth.actions';
import { selectRegistered, selectRemoteError, selectRemoteLoading } from '../../store/auth.selectors';

/** Self-registration (policyholder). Backend: POST /api/auth/register (gateway :8080). */
@Component({
  selector: 'auth-register',
  standalone: true,
  imports: [ReactiveFormsModule, AsyncPipe],
  template: `
    <section class="card max-w-md mx-auto">
      <h1 class="text-xl font-bold text-blue-900">Create account</h1>
      <form [formGroup]="form" (ngSubmit)="submit()" class="mt-4 space-y-3">
        <div>
          <label class="block text-sm font-semibold">Display name</label>
          <input class="input-field" formControlName="displayName" autocomplete="name" />
          @if (form.controls.displayName.touched && form.controls.displayName.invalid) {
            <p class="text-sm text-rose-600">Display name is required.</p>
          }
        </div>
        <div>
          <label class="block text-sm font-semibold">Email</label>
          <input class="input-field" type="email" formControlName="email" autocomplete="email" />
          @if (form.controls.email.touched && form.controls.email.invalid) {
            <p class="text-sm text-rose-600">Enter a valid email address.</p>
          }
        </div>
        <div>
          <label class="block text-sm font-semibold">Password</label>
          <input class="input-field" type="password" formControlName="password" autocomplete="new-password" />
          @if (form.controls.password.touched && form.controls.password.invalid) {
            <p class="text-sm text-rose-600">
              Min 8 chars, with upper, lower, digit and special character.
            </p>
          }
        </div>
        @if (error$ | async; as err) {
          <p class="text-sm text-rose-600">{{ err }}</p>
        }
        @if (registered$ | async) {
          <p class="text-sm text-emerald-600">Account created — please login.</p>
        }
        <button class="btn-primary w-full" type="submit" [disabled]="form.invalid || (loading$ | async)">
          Register
        </button>
      </form>
    </section>
  `,
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
