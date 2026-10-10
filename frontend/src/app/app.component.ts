import { AsyncPipe } from '@angular/common';
import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { filter, forkJoin } from 'rxjs';
import { NotificationResponse } from './core/models/notification.models';
import { NotificationService } from './core/services/notification.service';
import { ApiService } from './core/services/api.service';
import { authActions } from './core/store/auth/auth.actions';
import { selectIsAuthenticated, selectUserId, selectUserRole } from './core/store/auth/auth.selectors';
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
export class AppComponent implements OnInit, OnDestroy {
  private readonly store = inject(Store);
  private readonly router = inject(Router);
  private readonly notificationService = inject(NotificationService);
  private readonly api = inject(ApiService);
  
  readonly navigationItems: NavigationItem[] = [
    { label: 'Overview', path: '/', icon: '🏠' },
    { label: 'Dashboard', path: '/dashboard', icon: '📊', roles: ['Policyholder', 'POLICYHOLDER'] },
    { label: 'Business Profile', path: '/profile', icon: '🏢', roles: ['Policyholder', 'POLICYHOLDER'] },
    { label: 'Quotes', path: '/quotes', icon: '📋', roles: ['Admin', 'ADMIN', 'Underwriter', 'UNDERWRITER', 'Policyholder', 'POLICYHOLDER', 'SYSTEM_ADMINISTRATOR', 'Claims Adjuster', 'CLAIMS_ADJUSTER', 'Claims Manager', 'CLAIMS_MANAGER'] },
    { label: 'Policies', path: '/policy', icon: '🛡️', roles: ['Admin', 'ADMIN', 'Underwriter', 'UNDERWRITER', 'Policyholder', 'POLICYHOLDER', 'SYSTEM_ADMINISTRATOR', 'Claims Adjuster', 'CLAIMS_ADJUSTER', 'Claims Manager', 'CLAIMS_MANAGER'] },
    { label: 'Underwriting', path: '/underwriting', icon: '🔍', roles: ['Underwriter', 'UNDERWRITER', 'Risk Engineer', 'RISK_ENGINEER'] },
    { label: 'Claims', path: '/claims', icon: '📄', roles: ['Admin', 'ADMIN', 'Claims Adjuster', 'CLAIMS_ADJUSTER', 'Claims Manager', 'CLAIMS_MANAGER', 'Policyholder', 'POLICYHOLDER'] },
    { label: 'Vendors', path: '/vendor', icon: '🏢', roles: ['Admin', 'ADMIN', 'Vendor Applicant', 'VENDOR_APPLICANT', 'Claims Adjuster', 'CLAIMS_ADJUSTER', 'Claims Manager', 'CLAIMS_MANAGER', 'Vendor Manager', 'VENDOR_MANAGER', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Analytics', path: '/analytics', icon: '📊', roles: ['Admin', 'ADMIN', 'SYSTEM_ADMINISTRATOR', 'Underwriter', 'UNDERWRITER', 'Risk Engineer', 'RISK_ENGINEER', 'Claims Manager', 'CLAIMS_MANAGER'] },
    { label: 'Recovery', path: '/recovery', icon: '💰', roles: ['Admin', 'ADMIN', 'Claims Adjuster', 'CLAIMS_ADJUSTER', 'Claims Manager', 'CLAIMS_MANAGER', 'Policyholder', 'POLICYHOLDER'] },
    { label: 'Documents', path: '/docs', icon: '📁', roles: ['Admin', 'ADMIN', 'Underwriter', 'UNDERWRITER', 'Risk Engineer', 'RISK_ENGINEER', 'Claims Adjuster', 'CLAIMS_ADJUSTER', 'Claims Manager', 'CLAIMS_MANAGER', 'Vendor Manager', 'VENDOR_MANAGER', 'SYSTEM_ADMINISTRATOR', 'Policyholder', 'POLICYHOLDER'] },
    { label: 'Notifications', path: '/notifications', icon: '🔔', roles: ['Admin', 'ADMIN', 'Underwriter', 'UNDERWRITER', 'Risk Engineer', 'RISK_ENGINEER', 'Claims Adjuster', 'CLAIMS_ADJUSTER', 'Claims Manager', 'CLAIMS_MANAGER', 'Vendor Manager', 'VENDOR_MANAGER', 'SYSTEM_ADMINISTRATOR', 'Policyholder', 'POLICYHOLDER'] },
    { label: 'Administration', path: '/admin', icon: '⚙️', roles: ['Admin', 'ADMIN', 'SYSTEM_ADMINISTRATOR'] },
  ];

  readonly isAuthenticated$ = this.store.select(selectIsAuthenticated);
  readonly role$ = this.store.select(selectUserRole);
  readonly loading$ = this.store.select(selectGlobalLoading);
  readonly toasts$ = this.store.select(selectToasts);

  sidebarCollapsed = false;
  mobileSidebarOpen = false;
  notificationOpen = false;
  userMenuOpen = false;
  isDark = false;
  searchTerm = '';
  currentPath = '/';
  breadcrumbs: { label: string; link?: string }[] = [];
  role: string | null = null;
  displayName: string | null = null;
  notifications: Array<{ id: string; title: string; message: string; time: string; kind: 'success' | 'warning' | 'info' | 'danger'; read?: boolean }> = [];
  private notificationUserId: string | null = null;

  private readonly navigationSubscription = this.router.events
    .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
    .subscribe((event) => this.updateNavigation(event.urlAfterRedirects));
  private readonly roleSubscription = this.store.select(selectUserRole).subscribe((role) => (this.role = role));
  private readonly notificationSubscription = this.store.select(selectUserId).subscribe((userId) => {
    this.notificationUserId = userId;
    if (userId) this.loadNotifications(userId);
    else this.notifications = [];
  });

  readonly title = 'IntelliSure Shell';

  get visibleNavigation(): NavigationItem[] {
    const currentRole = (this.role ?? '').toUpperCase();
    return this.navigationItems.filter((item) =>
      !item.roles || item.roles.some((r) => r.toUpperCase() === currentRole)
    );
  }

  get userInitial(): string {
    return this.role?.charAt(0)?.toUpperCase() ?? 'U';
  }

  get userRoleLabel(): string {
    return this.role ?? 'Enterprise workspace';
  }

  get isPolicyholder(): boolean {
    return ['POLICYHOLDER', 'USER'].includes((this.role ?? '').toUpperCase());
  }

  get unreadCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }

  get userMenuItems(): Array<{ label: string; link?: string; action?: () => void; icon?: string; variant?: 'default' | 'danger' }> {
    return [
      ...(this.isPolicyholder ? [
        { label: 'Dashboard', link: '/dashboard', icon: '📊' },
        { label: 'Business profile', link: '/profile', icon: '🏢' },
      ] : []),
      { label: 'Quotes', link: '/quotes', icon: '📋' },
      { label: 'Policies', link: '/policy', icon: '🛡️' },
      { label: 'Sign out', action: () => this.logout(), icon: '🚪', variant: 'danger' },
    ];
  }

  get isWorkspaceRoute(): boolean {
    const path = (this.currentPath || '').split('?')[0].split('#')[0];
    return path !== '/' && !path.startsWith('/auth');
  }

  ngOnInit(): void {
    this.initTheme();
    this.restoreUserSession();
    this.loadCurrentUser();
    this.updateNavigation(this.router.url);
  }

  private restoreUserSession(): void {
    if (typeof localStorage !== 'undefined') {
      const token = localStorage.getItem('is_token');
      if (!token) return;

      let role = localStorage.getItem('is_role');
      let userId = localStorage.getItem('is_user_id');
      let customerId = localStorage.getItem('is_customer_id');
      let email = localStorage.getItem('is_email');

      if (!role || !userId) {
        try {
          const base64Url = token.split('.')[1];
          if (base64Url) {
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(
              atob(base64)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
            );
            const payload = JSON.parse(jsonPayload);
            userId = userId ?? payload.sub ?? payload.userId;
            role = role ?? payload.role ?? (Array.isArray(payload.roles) ? payload.roles[0] : null);
            customerId = customerId ?? payload.customerId;
            email = email ?? payload.email;

            if (customerId && !localStorage.getItem('is_customer_id')) {
              localStorage.setItem('is_customer_id', customerId);
            }
          }
        } catch {
          // Safe fallback if JWT payload cannot be parsed
        }
      }

      this.store.dispatch(authActions.restoreSession({ token, role, email, userId, customerId }));
    }
  }

  ngOnDestroy(): void {
    this.navigationSubscription.unsubscribe();
    this.roleSubscription.unsubscribe();
    this.notificationSubscription.unsubscribe();
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

  initTheme(): void {
    if (typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('is_theme');
      if (saved) {
        this.setTheme(saved === 'dark');
      } else if (typeof window !== 'undefined' && window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        this.setTheme(true);
      } else {
        this.setTheme(false);
      }
    }
  }

  onToggleTheme(): void {
    this.setTheme(!this.isDark);
  }

  setTheme(dark: boolean): void {
    this.isDark = dark;
    if (typeof document !== 'undefined') {
      document.documentElement.classList.toggle('dark', dark);
      document.body.classList.toggle('dark', dark);
      document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    }
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('is_theme', dark ? 'dark' : 'light');
    }
  }

  search(): void {
    const term = this.searchTerm.trim();
    if (!term) return;
    const lower = term.toLowerCase();

    // Direct entity prefix checks
    if (/^qt[-_0-9a-f]/i.test(lower) || lower.startsWith('quote')) {
      this.router.navigate(['/quotes'], { queryParams: { search: term } });
      return;
    }
    if (/^pol[-_0-9a-f]/i.test(lower) || lower.startsWith('polic')) {
      this.router.navigate(['/policy'], { queryParams: { search: term } });
      return;
    }
    if (/^clm[-_0-9a-f]/i.test(lower) || lower.startsWith('claim')) {
      this.router.navigate(['/claims'], { queryParams: { search: term } });
      return;
    }
    if (/^vnd[-_0-9a-f]/i.test(lower) || lower.startsWith('vendor')) {
      this.router.navigate(['/vendor'], { queryParams: { search: term } });
      return;
    }

    const aliases: Record<string, string> = {
      quote: '/quotes', quotes: '/quotes',
      policy: '/policy', policies: '/policy',
      claim: '/claims', claims: '/claims',
      vendor: '/vendor', vendors: '/vendor',
      recovery: '/recovery',
      document: '/docs', documents: '/docs', doc: '/docs',
      notification: '/notifications', notifications: '/notifications',
      admin: '/admin', administration: '/admin',
      underwriting: '/underwriting',
      analytics: '/analytics', intelligence: '/analytics',
      profile: '/profile', dashboard: '/dashboard',
    };

    const aliasMatch = Object.entries(aliases).find(([alias]) => alias.startsWith(lower));
    const destinationPath = aliases[lower]
      ?? aliasMatch?.[1]
      ?? this.navigationItems.find((item) => item.label.toLowerCase().includes(lower))?.path;

    if (destinationPath) {
      const allowed = this.navigationItems.find((item) =>
        item.path === destinationPath && (!item.roles || item.roles.some((role) => role.toUpperCase() === (this.role ?? '').toUpperCase()))
      );
      if (allowed) {
        this.router.navigateByUrl(allowed.path);
        return;
      }
    }

    // Default search routing
    this.router.navigate(['/quotes'], { queryParams: { search: term } });
  }

  logout(): void {
    this.displayName = null;
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
    const unread = this.notifications.filter((notification) => !notification.read);
    if (!unread.length) return;
    forkJoin(unread.map((notification) => this.notificationService.markRead(notification.id))).subscribe({
      next: () => { if (this.notificationUserId) this.loadNotifications(this.notificationUserId); },
    });
  }

  onDismissToast(id: number): void {
    this.dismiss(id);
  }

  private loadNotifications(userId: string): void {
    this.notificationService.getAllNotifications(userId).subscribe({
      next: (notifications) => { this.notifications = notifications.map((notification) => this.toTopbarNotification(notification)); },
      error: () => { this.notifications = []; },
    });
  }

  private loadCurrentUser(): void {
    if (typeof localStorage === 'undefined' || !localStorage.getItem('is_token')) return;
    this.api.get<{ displayName?: string; email?: string }>('/api/auth/me').subscribe({
      next: (user) => { this.displayName = user.displayName || user.email || null; },
      error: () => { this.displayName = localStorage.getItem('is_email'); },
    });
  }

  private toTopbarNotification(notification: NotificationResponse): { id: string; title: string; message: string; time: string; kind: 'success' | 'warning' | 'info' | 'danger'; read?: boolean } {
    return {
      id: notification.notificationId,
      title: notification.title,
      message: notification.message,
      time: notification.createdAt ? new Date(notification.createdAt).toLocaleString() : '',
      kind: this.notificationKind(notification.type),
      read: notification.read,
    };
  }

  private notificationKind(type: string): 'success' | 'warning' | 'info' | 'danger' {
    const value = type.toUpperCase();
    if (value.includes('ERROR') || value.includes('DENIED') || value.includes('FAIL')) return 'danger';
    if (value.includes('REVIEW') || value.includes('ACTION') || value.includes('WARNING')) return 'warning';
    if (value.includes('SUCCESS') || value.includes('COMPLETE') || value.includes('ISSU')) return 'success';
    return 'info';
  }
}
