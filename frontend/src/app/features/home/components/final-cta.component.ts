import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { ButtonComponent } from 'ui-core';
import { selectIsAuthenticated } from '../../../core/store/auth/auth.selectors';
import { authActions } from '../../../core/store/auth/auth.actions';

@Component({
  selector: 'is-final-cta',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonComponent],
  template: `
    <section class="final-cta-section" aria-labelledby="cta-heading">
      <div class="cta-banner">
        <!-- Ambient decorative shapes -->
        <div class="cta-ambient" aria-hidden="true"></div>

        <div class="cta-inner">
          <span class="cta-kicker">Unified Operating Standard</span>
          <h2 id="cta-heading" class="cta-title">Protect Your Business. Recover Smarter. Keep Moving.</h2>
          <p class="cta-copy">
            IntelliSure connects risk, insurance, claims, and recovery in one intelligent platform — helping businesses make better decisions before an incident and recover faster when one happens.
          </p>

          <div class="cta-actions">
            @if (isAuthenticated$ | async) {
              <a routerLink="/policy" class="cta-btn-link">
                <is-button variant="primary" size="lg">Enter Workspace</is-button>
              </a>
              <button type="button" class="cta-btn-link cta-signout" (click)="logout()">Sign Out</button>
            } @else {
              <a routerLink="/auth/register" class="cta-btn-link">
                <is-button variant="primary" size="lg">Get Started</is-button>
              </a>
              <a routerLink="/auth/login" class="cta-btn-link">
                <is-button variant="secondary" size="lg">Sign In</is-button>
              </a>
            }
          </div>

          <div class="reassurance-strip" aria-label="Platform assurance points">
            <span class="reassurance-item">
              <span class="reassurance-dot" aria-hidden="true">&check;</span>
              Audit-ready architecture
            </span>
            <span class="reassurance-item">
              <span class="reassurance-dot" aria-hidden="true">&check;</span>
              Connected vendor network
            </span>
            <span class="reassurance-item">
              <span class="reassurance-dot" aria-hidden="true">&check;</span>
              End-to-end lifecycle visibility
            </span>
            <span class="reassurance-item">
              <span class="reassurance-dot" aria-hidden="true">&check;</span>
              Role-based access control
            </span>
          </div>
        </div>
      </div>
    </section>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }

    .final-cta-section {
      max-width: 1380px;
      margin: 0 auto;
      padding: 40px 0 20px;
    }

    .cta-banner {
      position: relative;
      background: radial-gradient(circle at 80% 20%, rgba(254, 48, 130, 0.18), transparent 40%),
                  linear-gradient(135deg, var(--claret, #75013f) 0%, #4a0128 100%);
      border-radius: 14px;
      padding: clamp(48px, 6vw, 72px) 24px;
      color: #ffffff;
      overflow: hidden;
      box-shadow: 0 16px 48px rgba(117, 1, 63, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.12);
    }

    .cta-ambient {
      position: absolute;
      inset: 0;
      background-image: 
        radial-gradient(circle at 10% 90%, rgba(255, 255, 255, 0.05) 0%, transparent 30%);
      pointer-events: none;
    }

    .cta-inner {
      position: relative;
      z-index: 1;
      max-width: 780px;
      margin: 0 auto;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .cta-kicker {
      display: inline-block;
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.14em;
      text-transform: uppercase;
      color: #ffb8d4;
      background: rgba(255, 255, 255, 0.1);
      padding: 4px 12px;
      border-radius: 4px;
      margin-bottom: 16px;
    }

    .cta-title {
      font-size: clamp(28px, 3.8vw, 44px);
      font-weight: 800;
      letter-spacing: -0.04em;
      line-height: 1.1;
      margin: 0 0 16px;
      color: #ffffff;
    }

    .cta-copy {
      font-size: clamp(13px, 1.6vw, 15px);
      line-height: 1.65;
      color: rgba(255, 255, 255, 0.9);
      margin: 0 0 32px;
      max-width: 680px;
    }

    .cta-actions {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 16px;
      margin-bottom: 36px;
    }

    .cta-btn-link {
      text-decoration: none;
      display: inline-flex;
    }

    .cta-signout {
      align-items: center;
      justify-content: center;
      min-height: 42px;
      padding: 0 20px;
      border: 1px solid rgba(255, 255, 255, 0.45);
      border-radius: 6px;
      background: transparent;
      color: #ffffff;
      font: inherit;
      font-size: 13px;
      font-weight: 700;
      cursor: pointer;
    }

    .reassurance-strip {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 20px;
      font-size: 11px;
      color: rgba(255, 255, 255, 0.82);
    }

    .reassurance-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 500;
    }

    .reassurance-dot {
      color: #86efac;
      font-weight: 800;
      font-size: 12px;
    }

    @media (max-width: 640px) {
      .cta-actions {
        flex-direction: column;
        width: 100%;
      }
      .cta-btn-link {
        width: 100%;
      }
      .cta-btn-link is-button {
        width: 100%;
      }
      .reassurance-strip {
        flex-direction: column;
        align-items: center;
        gap: 10px;
      }
    }
  `],
})
export class FinalCtaComponent {
  private readonly store = inject(Store);
  readonly isAuthenticated$ = this.store.select(selectIsAuthenticated);

  logout(): void {
    this.store.dispatch(authActions.logout());
  }
}
