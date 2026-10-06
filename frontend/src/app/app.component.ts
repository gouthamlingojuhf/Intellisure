import { AsyncPipe } from '@angular/common';
import { Component, inject, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { filter } from 'rxjs';
import { authActions } from './core/store/auth/auth.actions';
import { selectIsAuthenticated, selectUserRole } from './core/store/auth/auth.selectors';
import { selectGlobalLoading, selectToasts } from './core/store/ui/ui.selectors';
import { uiActions } from './core/store/ui/ui.actions';

interface NavigationItem {
  label: string;
  path: string;
  roles?: string[];
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AsyncPipe, FormsModule, RouterLink, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnDestroy {
  private readonly store = inject(Store);
  private readonly router = inject(Router);
  private readonly navigationItems: NavigationItem[] = [
    { label: 'Overview', path: '/' },
    { label: 'Quotes & Policies', path: '/policy', roles: ['Admin', 'Underwriter', 'Policyholder'] },
    { label: 'Underwriting', path: '/underwriting', roles: ['Admin', 'Underwriter', 'Risk Engineer'] },
    { label: 'Claims', path: '/claims', roles: ['Admin', 'Claims Adjuster', 'Claims Manager'] },
    { label: 'Vendors', path: '/vendor', roles: ['Admin', 'Claims Manager'] },
    { label: 'Analytics', path: '/analytics', roles: ['Admin', 'Underwriter', 'Risk Engineer'] },
    { label: 'Recovery', path: '/recovery', roles: ['Admin', 'Claims Adjuster', 'Claims Manager'] },
    { label: 'Documents', path: '/docs', roles: ['Admin', 'Policyholder'] },
  ];

  readonly isAuthenticated$ = this.store.select(selectIsAuthenticated);
  readonly role$ = this.store.select(selectUserRole);
  readonly loading$ = this.store.select(selectGlobalLoading);
  readonly toasts$ = this.store.select(selectToasts);
  sidebarCollapsed = false;
  mobileSidebarOpen = false;
  notificationOpen = false;
  userMenuOpen = false;
  searchTerm = '';
  currentPath = '/';
  breadcrumbs: string[] = [];
  role: string | null = null;

  private readonly navigationSubscription = this.router.events
    .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
    .subscribe((event) => this.updateNavigation(event.urlAfterRedirects));
  private readonly roleSubscription = this.store.select(selectUserRole).subscribe((role) => (this.role = role));

  readonly title = 'IntelliSure Shell';

  get visibleNavigation(): NavigationItem[] {
    return this.navigationItems.filter((item) => !item.roles || item.roles.includes(this.role ?? ''));
  }

  get currentPageLabel(): string {
    const route = this.currentPath.split('/').filter(Boolean).join(' · ');
    return route
      .split(' · ')
      .map((segment) => segment.replace(/\b\w/g, (letter) => letter.toUpperCase()))
      .pop() ?? 'IntelliSure';
  }

  ngOnDestroy(): void {
    this.navigationSubscription.unsubscribe();
    this.roleSubscription.unsubscribe();
  }

  updateNavigation(url: string): void {
    this.currentPath = url || '/';
    this.breadcrumbs = this.currentPath.split('/').filter(Boolean);
    this.mobileSidebarOpen = false;
    this.notificationOpen = false;
    this.userMenuOpen = false;
  }

  search(): void {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) return;
    const destination = this.navigationItems.find((item) => item.label.toLowerCase().includes(term));
    if (destination) this.router.navigateByUrl(destination.path);
  }

  logout(): void {
    this.store.dispatch(authActions.logout());
    this.router.navigateByUrl('/');
  }

  dismiss(id: number): void {
    this.store.dispatch(uiActions.dismissToast({ id }));
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  toggleMenu(menu: 'notifications' | 'user'): void {
    if (menu === 'notifications') {
      this.notificationOpen = !this.notificationOpen;
      this.userMenuOpen = false;
    } else {
      this.userMenuOpen = !this.userMenuOpen;
      this.notificationOpen = false;
    }
  }

  closeMobileSidebar(): void {
    this.mobileSidebarOpen = false;
  }
}
