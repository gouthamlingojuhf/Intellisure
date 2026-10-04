import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'is-home',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="brand-home">
      <div class="hero-panel">
        <div class="hero-copy">
          <span class="eyebrow">Insurance platform</span>
          <h1>Modern protection for every policy lifecycle.</h1>
          <p>
            IntelliSure brings quoting, underwriting, claims, recovery, and analytics into a single
            connected experience built for modern insurance operations.
          </p>
          <div class="hero-actions">
            <a routerLink="/auth" class="brand-primary-btn">Login</a>
            <a routerLink="/policy" class="brand-secondary-btn">Explore workflows</a>
          </div>
        </div>

        <div class="hero-metrics">
          <div class="metric-card metric-dark">
            <span>Open quotes</span>
            <strong>184</strong>
            <small>+12% vs last week</small>
          </div>
          <div class="metric-card metric-claret">
            <span>Claims in review</span>
            <strong>39</strong>
            <small>3 escalations</small>
          </div>
          <div class="metric-card metric-fuchsia">
            <span>Recovery yield</span>
            <strong>18.4%</strong>
            <small>Above target</small>
          </div>
        </div>
      </div>

      <div class="feature-grid">
        <article class="feature-card highlight-card">
          <span class="card-tag">Sales</span>
          <h3>Quotes &amp; policy</h3>
          <p>Create, price, and bind policies with streamlined underwriting workflows.</p>
        </article>

        <article class="feature-card">
          <span class="card-tag">Operations</span>
          <h3>Claims center</h3>
          <p>Track FNOLs, reserves, settlements, and recovery actions from one place.</p>
        </article>

        <article class="feature-card">
          <span class="card-tag">Risk</span>
          <h3>Underwriting</h3>
          <p>Review submissions, flag exceptions, and approve risk decisions faster.</p>
        </article>

        <article class="feature-card">
          <span class="card-tag">Insights</span>
          <h3>Analytics</h3>
          <p>Monitor loss ratio, recoveries, performance trends, and portfolio health.</p>
        </article>
      </div>
    </section>
  `,
})
export class HomeComponent {}
