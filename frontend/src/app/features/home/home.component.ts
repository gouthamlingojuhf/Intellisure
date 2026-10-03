import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'is-home',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="card">
      <h1 class="text-2xl font-bold text-blue-900">IntelliSure Insurance Platform</h1>
      <p class="mt-2 text-gray-700">
        End-to-end lifecycle demo: register &rarr; quote &rarr; risk review &rarr; approve &rarr;
        issue policy &rarr; file claim &rarr; assign vendor &rarr; notify &rarr; audit.
      </p>
      <div class="mt-4 flex flex-wrap gap-2">
        <a routerLink="/auth/login" class="btn-primary">Login</a>
        <a routerLink="/policy" class="btn-secondary">Quotes &amp; Policies</a>
        <a routerLink="/claims" class="btn-secondary">Claims</a>
        <a routerLink="/vendor" class="btn-secondary">Vendors</a>
        <a routerLink="/analytics" class="btn-secondary">Analytics</a>
      </div>
      <p class="mt-4 text-sm text-gray-500">
        Remote MFEs load here via Module Federation once each remote app is added
        (auth :4201, policy :4202, underwriting :4203, claims :4204, vendor :4205, analytics :4206).
      </p>
    </section>
  `,
})
export class HomeComponent {}
