import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { authRemoteActions } from '../../store/auth.actions';
import { selectRemoteProfile, selectRemoteToken } from '../../store/auth.selectors';

/** Redirects to central shell Business Profile (/profile). */
@Component({
  selector: 'auth-profile',
  standalone: true,
  imports: [CommonModule, AsyncPipe, RouterLink],
  template: `
    <section class="max-w-md mx-auto p-6 bg-white rounded-lg shadow-sm border border-gray-200 text-center">
      <h1 class="text-xl font-bold text-gray-900 mb-2">Redirecting to Business Profile…</h1>
      <p class="text-sm text-gray-600 mb-4">
        Taking you to the business customer profile workspace.
      </p>
      <a routerLink="/profile" class="inline-block px-4 py-2 bg-claret text-white font-semibold rounded hover:bg-opacity-90">
        Open Business Profile →
      </a>
    </section>
  `,
})
export class ProfileComponent implements OnInit {
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.router.navigateByUrl('/profile');
  }
}
