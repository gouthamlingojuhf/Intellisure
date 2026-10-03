import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'is-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="card text-center">
      <h1 class="text-2xl font-bold text-rose-600">404 — Not found</h1>
      <p class="mt-2 text-gray-700">The page you requested does not exist.</p>
      <a routerLink="/" class="btn-primary mt-4 inline-block">Back home</a>
    </section>
  `,
})
export class NotFoundComponent {}
