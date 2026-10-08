import { Component, HostListener, inject } from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { ButtonComponent } from 'ui-core';
import { selectIsAuthenticated } from '../../../core/store/auth/auth.selectors';
import { authActions } from '../../../core/store/auth/auth.actions';

@Component({
  selector: 'is-landing-header',
  standalone: true,
  imports: [CommonModule, AsyncPipe, RouterLink, ButtonComponent],
  template: `
    <header class="landing-header" [class.scrolled]="isScrolled" role="banner">
      <a href="#main-content" class="skip-link">Skip to main content</a>
      <div class="header-inner">
        <a routerLink="/" class="brand-link" aria-label="IntelliSure Home">
          <span class="brand-mark" aria-hidden="true">I</span>
          <span class="brand-name">IntelliSure</span>
          <span class="brand-tag">Platform</span>
        </a>

        <nav class="desktop-nav" aria-label="Main Navigation">
          <a href="#capabilities" class="nav-link">Capabilities</a>
          <a href="#lifecycle" class="nav-link">Risk to Recovery</a>
          <a href="#how-it-works" class="nav-link">How It Works</a>
          <a href="#intelligence" class="nav-link">Intelligence</a>
          <a href="#roles" class="nav-link">Roles</a>
        </nav>

        <div class="header-actions">
          @if (isAuthenticated$ | async) {
            <a routerLink="/policy" class="cta-link">
              <is-button variant="primary" size="sm">Enter Workspace →</is-button>
            </a>
            <button type="button" class="sign-in-link" (click)="logout()">Sign Out</button>
          } @else {
            <a routerLink="/auth/login" class="sign-in-link">Sign In</a>
            <a routerLink="/auth/register" class="cta-link">
              <is-button variant="primary" size="sm">Get Started</is-button>
            </a>
          }
          <button
            type="button"
            class="mobile-toggle"
            [attr.aria-expanded]="mobileMenuOpen"
            aria-controls="mobile-nav"
            aria-label="Toggle navigation menu"
            (click)="toggleMobileMenu()"
          >
            <span class="bar" [class.open]="mobileMenuOpen"></span>
            <span class="bar" [class.open]="mobileMenuOpen"></span>
            <span class="bar" [class.open]="mobileMenuOpen"></span>
          </button>
        </div>
      </div>

      <!-- Mobile dropdown menu -->
      @if (mobileMenuOpen) {
        <nav id="mobile-nav" class="mobile-nav" aria-label="Mobile Navigation">
          <a href="#capabilities" class="mobile-link" (click)="closeMobileMenu()">Capabilities</a>
          <a href="#lifecycle" class="mobile-link" (click)="closeMobileMenu()">Risk to Recovery</a>
          <a href="#how-it-works" class="mobile-link" (click)="closeMobileMenu()">How It Works</a>
          <a href="#intelligence" class="mobile-link" (click)="closeMobileMenu()">Intelligence</a>
          <a href="#roles" class="mobile-link" (click)="closeMobileMenu()">Roles</a>
          <div class="mobile-actions">
            @if (isAuthenticated$ | async) {
              <a routerLink="/policy" class="mobile-auth-btn primary" (click)="closeMobileMenu()">Enter Workspace</a>
              <button type="button" class="mobile-auth-btn secondary" (click)="logout(); closeMobileMenu()">Sign Out</button>
            } @else {
              <a routerLink="/auth/login" class="mobile-auth-btn secondary" (click)="closeMobileMenu()">Sign In</a>
              <a routerLink="/auth/register" class="mobile-auth-btn primary" (click)="closeMobileMenu()">Get Started</a>
            }
          </div>
        </nav>
      }
    </header>
  `,
  styles: [`
    :host {
      display: block;
      position: sticky;
      top: 0;
      z-index: 50;
      width: 100%;
    }

    .skip-link {
      position: absolute;
      top: -100px;
      left: 16px;
      padding: 8px 16px;
      background: var(--claret, #75013f);
      color: #fff;
      font-size: 11px;
      font-weight: 700;
      border-radius: 4px;
      z-index: 1000;
      text-decoration: none;
      transition: top 0.2s ease;
    }
    .skip-link:focus {
      top: 12px;
    }

    .landing-header {
      width: 100%;
      background: rgba(247, 245, 243, 0.94);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--border, #eae5df);
      transition: background 0.2s ease, box-shadow 0.2s ease;
    }

    .landing-header.scrolled {
      background: rgba(255, 255, 255, 0.98);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    }

    .header-inner {
      max-width: 1380px;
      margin: 0 auto;
      padding: 0 24px;
      height: 68px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 24px;
    }

    .brand-link {
      display: flex;
      align-items: center;
      gap: 10px;
      text-decoration: none;
      color: inherit;
    }

    .brand-mark {
      width: 32px;
      height: 32px;
      border-radius: 6px;
      background: var(--claret, #75013f);
      color: #ffffff;
      display: grid;
      place-items: center;
      font-size: 16px;
      font-weight: 800;
      letter-spacing: -0.02em;
      box-shadow: 0 4px 12px rgba(117, 1, 63, 0.2);
    }

    .brand-name {
      font-size: 18px;
      font-weight: 700;
      letter-spacing: -0.03em;
      color: var(--ink, #000000);
    }

    .brand-tag {
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: var(--claret, #75013f);
      background: var(--claret-soft, #fde8ed);
      padding: 2px 6px;
      border-radius: 3px;
    }

    .desktop-nav {
      display: flex;
      align-items: center;
      gap: 28px;
    }

    .nav-link {
      font-size: 12px;
      font-weight: 600;
      color: #4a4548;
      text-decoration: none;
      transition: color 0.15s ease;
      position: relative;
    }
    .nav-link:hover {
      color: var(--claret, #75013f);
    }
    .nav-link:focus-visible {
      outline: 2px solid var(--fuchsia, #fe3082);
      outline-offset: 4px;
      border-radius: 2px;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .sign-in-link {
      font-size: 12px;
      font-weight: 600;
      color: #3b373a;
      text-decoration: none;
      padding: 8px 12px;
      border-radius: 6px;
      transition: background 0.15s ease, color 0.15s ease;
    }
    .sign-in-link:hover {
      background: var(--warm, #eae5df);
      color: var(--claret, #75013f);
    }

    .cta-link {
      text-decoration: none;
      display: inline-flex;
    }

    .mobile-toggle {
      display: none;
      width: 40px;
      height: 40px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 6px;
      background: #ffffff;
      cursor: pointer;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      gap: 4px;
      padding: 0;
    }

    .bar {
      width: 18px;
      height: 2px;
      background: #272427;
      border-radius: 2px;
      transition: transform 0.2s ease, opacity 0.2s ease;
    }

    .mobile-nav {
      display: none;
    }

    @media (max-width: 900px) {
      .desktop-nav {
        display: none;
      }
      .mobile-toggle {
        display: flex;
      }
      .mobile-nav {
        display: flex;
        flex-direction: column;
        padding: 16px 24px 24px;
        background: #ffffff;
        border-bottom: 1px solid var(--border, #eae5df);
        gap: 12px;
        animation: slideDown 0.2s ease-out;
      }
      .mobile-link {
        font-size: 13px;
        font-weight: 600;
        color: #333033;
        text-decoration: none;
        padding: 8px 0;
        border-bottom: 1px solid #f4f0ec;
      }
      .mobile-link:hover {
        color: var(--claret, #75013f);
      }
      .mobile-actions {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;
        margin-top: 8px;
      }
      .mobile-auth-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 44px;
        border-radius: 6px;
        font-size: 12px;
        font-weight: 700;
        text-decoration: none;
      }
      .mobile-auth-btn.primary {
        background: var(--claret, #75013f);
        color: #ffffff;
      }
      .mobile-auth-btn.secondary {
        border: 1px solid var(--border, #eae5df);
        background: var(--warm-light, #f7f5f3);
        color: #272427;
      }
    }

    @keyframes slideDown {
      from {
        opacity: 0;
        transform: translateY(-8px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .mobile-nav {
        animation: none;
      }
      .bar, .skip-link {
        transition: none;
      }
    }
  `],
})
export class LandingHeaderComponent {
  private readonly store = inject(Store);

  readonly isAuthenticated$ = this.store.select(selectIsAuthenticated);

  isScrolled = false;
  mobileMenuOpen = false;

  @HostListener('window:scroll')
  onWindowScroll(): void {
    if (typeof window !== 'undefined') {
      this.isScrolled = window.scrollY > 20;
    }
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  logout(): void {
    this.store.dispatch(authActions.logout());
  }
}
