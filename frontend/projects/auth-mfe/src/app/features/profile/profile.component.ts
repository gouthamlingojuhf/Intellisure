import { AsyncPipe, JsonPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { authRemoteActions } from '../../store/auth.actions';
import { selectRemoteProfile, selectRemoteToken } from '../../store/auth.selectors';

/** Current-user profile. Backend: GET /api/auth/me (gateway :8080). */
@Component({
  selector: 'auth-profile',
  standalone: true,
  imports: [AsyncPipe, JsonPipe],
  template: `
    <section class="card max-w-md mx-auto">
      <h1 class="text-xl font-bold text-blue-900">Profile</h1>
      @if (profile$ | async; as p) {
        <pre class="mt-3 text-sm bg-gray-50 p-3 rounded">{{ p | json }}</pre>
      } @else {
        <p class="mt-3 text-gray-700">Loading profile…</p>
      }
      <button class="btn-secondary mt-4" (click)="logout()">Logout</button>
    </section>
  `,
})
export class ProfileComponent implements OnInit {
  private readonly store = inject(Store);
  readonly profile$ = this.store.select(selectRemoteProfile);
  readonly token$ = this.store.select(selectRemoteToken);

  ngOnInit(): void {
    this.store.dispatch(authRemoteActions.loadProfile());
  }

  logout(): void {
    this.store.dispatch(authRemoteActions.logout());
  }
}
