import { AsyncPipe } from '@angular/common';
import { Component, inject, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { filter } from 'rxjs';
import { authActions } from './core/store/auth/auth.actions';
import { selectIsAuthenticated, selectUserRole } from './core/store/auth/auth.selectors';
import { selectGlobalLoading, selectToasts } from './core/store/ui/ui.selectors';
import { uiActions } from './core/store/ui/ui.actions';

// Import ui-core components
import { SidebarComponent, TopbarComponent, ToastStackComponent, Toast, SkeletonComponent } from 'ui-core';

interface NavigationItem {
  label: string;
  path: string;
  icon?: string;
  roles?: string[];
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    AsyncPipe,
    FormsModule,
    RouterOutlet,
    SidebarComponent,
    TopbarComponent,
    ToastStackComponent,
    SkeletonComponent,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnDestroy {
  private readonly store = inject(Store);
  private readonly router = inject(Router);
  
  readonly navigationItems: NavigationItem[] = [
    { label: 'Overview', path: '/', icon: '🏠' },
    { label: 'Quotes & Policies', path: '/policy', icon: '📋', roles: ['Admin', 'Underwriter', 'Policyholder'] },
    { label: 'Underwriting', path: '/underwriting', icon: '🔍', roles: ['Admin', 'Underwriter', 'Risk Engineer'] },
    { label: 'Claims', path: '/claims', icon: '📄', roles: ['Admin', 'Claims Adjuster', 'Claims Manager'] },
    { label: 'Vendors', path: '/vendor', icon: '🏢', roles: ['Admin', 'Claims Manager'] },
    { label: 'Analytics', path: '/analytics', icon: '📊', roles: ['Admin', 'Underwriter', 'Risk Engineer'] },
    { label: 'Recovery', path: '/recovery', icon: '💰', roles: ['Admin', 'Claims Adjuster', 'Claims Manager'] },
    { label: 'Documents', path: '/docs', icon: '📁', roles: ['Admin', 'Policyholder'] },
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
  breadcrumbs: { label: string; link?: string }[] = [];
  role: string | null = null;

  private readonly navigationSubscription = this.router.events
    .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
    .subscribe((event) => this.updateNavigation(event.urlAfterRedirects));
  private readonly roleSubscription = this.store.select(selectUserRole).subscribe((role) => (this.role = role));

  readonly title = 'IntelliSure Shell';

  get visibleNavigation(): NavigationItem[] {
    return this.navigationItems.filter((item) => !item.roles || item.roles.includes(this.role ?? ''));
  }

  get userInitial(): string {
    return this.role?.charAt(0)?.toUpperCase() ?? 'U';
  }

  get userRoleLabel(): string {
    return this.role ?? 'Enterprise workspace';
  }

  get notifications(): Array<{ id: string; title: string; message: string; time: string; kind: 'success' | 'warning' | 'info' | 'danger'; read?: boolean }> {
    return [
      { id: '1', title: 'Policy review complete', message: 'Commercial auto renewal is ready for binding.', time: '5 minutes ago', kind: 'success' },
      { id: '2', title: 'Review due soon', message: 'One risk submission exceeds the standard SLA.', time: '18 minutes ago', kind: 'warning' },
      { id: '3', title: 'System maintenance', message: 'Scheduled maintenance window this weekend.', time: '2 hours ago', kind: 'info' },
    ];
  }

  get unreadCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }

  get userMenuItems(): Array<{ label: string; link?: string; action?: () => void; icon?: string; variant?: 'default' | 'danger' }> {
    return [
      { label: 'Workspace settings', link: '/docs', icon: '⚙️' },
      { label: 'Security & access', link: '/docs', icon: '🔐' },
      { label: 'Sign out', action: () => this.logout(), icon: '🚪', variant: 'danger' },
    ];
  }

  ngOnDestroy(): void {
    this.navigationSubscription.unsubscribe();
    this.roleSubscription.unsubscribe();
  }

  updateNavigation(url: string): void {
    this.currentPath = url || '/';
    const segments = this.currentPath.split('/').filter(Boolean);
    this.breadcrumbs = segments.map((segment, index) => ({
      label: segment.charAt(0).toUpperCase() + segment.slice(1).replace(/-/g, ' '),
      link: index < segments.length - 1 ? '/' + segments.slice(0, index + 1).join('/') : undefined,
    }));
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

  onToggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  onCloseMobileSidebar(): void {
    this.mobileSidebarOpen = false;
  }

  onToggleNotifications(): void {
    this.notificationOpen = !this.notificationOpen;
    this.userMenuOpen = false;
  }

  onToggleUserMenu(): void {
    this.userMenuOpen = !this.userMenuOpen;
    this.notificationOpen = false;
  }

  onMarkAllRead(): void {
    // Mark all notifications as read
  }

  onDismissToast(id: number): void {
    this.dismiss(id);
  }
}