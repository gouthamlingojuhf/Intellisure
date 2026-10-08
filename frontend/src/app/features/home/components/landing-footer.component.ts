import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'is-landing-footer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <footer class="landing-footer" role="contentinfo">
      <div class="footer-container">
        <!-- Brand & Description Column -->
        <div class="footer-brand-col">
          <div class="brand-line">
            <span class="brand-mark" aria-hidden="true">I</span>
            <span class="brand-name">IntelliSure</span>
          </div>
          <p class="brand-desc">
            Enterprise commercial insurance intelligence platform unifying quotes, risk scoring, underwriting decisions, claims operations, and business restoration.
          </p>
          <div class="system-status-indicator" aria-label="System operational status">
            <span class="status-pulse" aria-hidden="true"></span>
            <span>Platform Gateway: Active (Port 8080)</span>
          </div>
        </div>

        <!-- Links Columns -->
        <div class="footer-links-grid">
          <div class="links-col">
            <h3 class="col-title">Platform Domains</h3>
            <ul class="links-list">
              <li><a routerLink="/auth/login">Smart Quoting & Rating</a></li>
              <li><a routerLink="/auth/login">Underwriting Risk Desk</a></li>
              <li><a routerLink="/auth/login">Claims Operations & FNOL</a></li>
              <li><a routerLink="/auth/login">Business Recovery Suite</a></li>
              <li><a routerLink="/auth/login">Vendor Partner Portal</a></li>
              <li><a routerLink="/auth/login">Portfolio Analytics</a></li>
            </ul>
          </div>

          <div class="links-col">
            <h3 class="col-title">Architecture</h3>
            <ul class="links-list">
              <li><span>Spring Cloud Gateway</span></li>
              <li><span>Eureka Discovery</span></li>
              <li><span>Angular 17 Module Federation</span></li>
              <li><span>JWT Bearer Security</span></li>
              <li><span>Distributed Tracing</span></li>
              <li><span>Audit-Logged Event Trail</span></li>
            </ul>
          </div>

          <div class="links-col">
            <h3 class="col-title">Governance & Access</h3>
            <ul class="links-list">
              <li><span>Role-Based Access Control</span></li>
              <li><span>Separation of Authority</span></li>
              <li><span>Immutable Policy Docs</span></li>
              <li><span>Reserve Adequacy Audit</span></li>
              <li><span>Credentialed Partners</span></li>
              <li><a routerLink="/auth/login">Enterprise Login</a></li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Copyright & Bottom Strip -->
      <div class="footer-bottom">
        <div class="bottom-container">
          <p class="copyright-text">&copy; 2026 IntelliSure Inc. All rights reserved. Commercial Insurance Technology.</p>
          <div class="bottom-links">
            <a routerLink="/">Privacy Policy</a>
            <span class="separator" aria-hidden="true">&bull;</span>
            <a routerLink="/">Terms of Service</a>
            <span class="separator" aria-hidden="true">&bull;</span>
            <a routerLink="/">Security Architecture</a>
          </div>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }

    .landing-footer {
      background: #171517;
      color: #eae5df;
      border-top: 1px solid #2b2729;
      padding-top: 56px;
    }

    .footer-container {
      max-width: 1380px;
      margin: 0 auto;
      padding: 0 24px 48px;
      display: grid;
      grid-template-columns: 1.2fr 2fr;
      gap: 48px;
    }

    .footer-brand-col {
      display: flex;
      flex-direction: column;
      gap: 16px;
      max-width: 380px;
    }

    .brand-line {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .brand-mark {
      width: 28px;
      height: 28px;
      border-radius: 6px;
      background: var(--claret, #75013f);
      color: #ffffff;
      display: grid;
      place-items: center;
      font-size: 14px;
      font-weight: 800;
    }

    .brand-name {
      font-size: 18px;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: #ffffff;
    }

    .brand-desc {
      font-size: 11px;
      line-height: 1.6;
      color: #a49da2;
      margin: 0;
    }

    .system-status-indicator {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      padding: 6px 12px;
      border-radius: 999px;
      font-size: 10px;
      color: #86efac;
      font-weight: 600;
      width: fit-content;
    }

    .status-pulse {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #22c55e;
      box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.25);
    }

    .footer-links-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 28px;
    }

    .col-title {
      font-size: 11px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      color: #ffffff;
      margin: 0 0 16px;
    }

    .links-list {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .links-list li a, .links-list li span {
      font-size: 11px;
      color: #a39ca1;
      text-decoration: none;
      transition: color 0.15s ease;
    }

    .links-list li a:hover {
      color: #ffffff;
    }

    .footer-bottom {
      border-top: 1px solid #242022;
      padding: 22px 0;
      background: #110f11;
    }

    .bottom-container {
      max-width: 1380px;
      margin: 0 auto;
      padding: 0 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      font-size: 11px;
      color: #797377;
    }

    .copyright-text {
      margin: 0;
    }

    .bottom-links {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .bottom-links a {
      color: #797377;
      text-decoration: none;
      transition: color 0.15s ease;
    }

    .bottom-links a:hover {
      color: #c9c3c7;
    }

    .separator {
      color: #4b4549;
    }

    @media (max-width: 900px) {
      .footer-container {
        grid-template-columns: 1fr;
        gap: 36px;
      }
      .footer-brand-col {
        max-width: 100%;
      }
      .bottom-container {
        flex-direction: column;
        align-items: flex-start;
      }
    }

    @media (max-width: 640px) {
      .footer-links-grid {
        grid-template-columns: 1fr;
        gap: 28px;
      }
    }
  `],
})
export class LandingFooterComponent {}
