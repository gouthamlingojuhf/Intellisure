import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { NavigationEnd, provideRouter, Router } from '@angular/router';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { AppComponent } from './app.component';
import { ApiService } from './core/services/api.service';
import { NotificationService } from './core/services/notification.service';
import { selectIsAuthenticated, selectUserId, selectUserRole } from './core/store/auth/auth.selectors';
import { authActions } from './core/store/auth/auth.actions';
import { uiActions } from './core/store/ui/ui.actions';
import { of, throwError } from 'rxjs';

describe('AppComponent', () => {
  let api: jasmine.SpyObj<ApiService>;
  let notifications: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    api = jasmine.createSpyObj<ApiService>('ApiService', ['get']);
    api.get.and.returnValue(of({ displayName: 'Goutham' }));
    notifications = jasmine.createSpyObj<NotificationService>('NotificationService', ['getAllNotifications', 'markRead']);
    notifications.getAllNotifications.and.returnValue(of([]));
    notifications.markRead.and.returnValue(of({} as any));
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideHttpClient(), provideRouter([]),
        provideMockStore({ selectors: [
          { selector: selectIsAuthenticated, value: false },
          { selector: selectUserId, value: null },
          { selector: selectUserRole, value: null },
        ] }),
        { provide: ApiService, useValue: api },
        { provide: NotificationService, useValue: notifications },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should expose the shell title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('IntelliSure Shell');
  });

  it('should render the application shell', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('router-outlet')).not.toBeNull();
  });

  it('exposes role-aware navigation, user menu and workspace state', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    app.role = 'POLICYHOLDER';
    app.currentPath = '/policy?view=active';
    expect(app.visibleNavigation.some(item => item.path === '/dashboard')).toBeTrue();
    expect(app.visibleNavigation.some(item => item.path === '/admin')).toBeFalse();
    expect(app.isPolicyholder).toBeTrue();
    expect(app.userInitial).toBe('P');
    expect(app.userRoleLabel).toBe('POLICYHOLDER');
    expect(app.isWorkspaceRoute).toBeTrue();
    expect(app.userMenuItems.map(item => item.label)).toContain('Business profile');
    app.role = 'UNDERWRITER';
    expect(app.isPolicyholder).toBeFalse();
    expect(app.userMenuItems.map(item => item.label)).not.toContain('Business profile');
    app.currentPath = '/';
    expect(app.isWorkspaceRoute).toBeFalse();
    app.currentPath = '/auth/login';
    expect(app.isWorkspaceRoute).toBeFalse();
    app.role = null;
    expect(app.userInitial).toBe('U');
    expect(app.userRoleLabel).toBe('Enterprise workspace');
    expect(app.visibleNavigation.length).toBeGreaterThan(0);
    app.currentPath = '';
    expect(app.isWorkspaceRoute).toBeTrue();
  });

  it('updates breadcrumbs, closes overlays and searches only permitted destinations', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    const router = TestBed.inject(Router);
    const navigate = spyOn(router, 'navigateByUrl').and.resolveTo(true);
    app.mobileSidebarOpen = true;
    app.notificationOpen = true;
    app.userMenuOpen = true;
    app.updateNavigation('/policy/quotes/new');
    expect(app.breadcrumbs.map(item => item.label)).toEqual(['Policy', 'Quotes', 'New']);
    expect(app.mobileSidebarOpen).toBeFalse();
    expect(app.notificationOpen).toBeFalse();
    expect(app.userMenuOpen).toBeFalse();
    app.updateNavigation('');
    expect(app.currentPath).toBe('/');
    app.role = 'POLICYHOLDER';
    app.searchTerm = 'claims';
    app.search();
    expect(navigate).toHaveBeenCalledWith('/claims');
    app.searchTerm = '   ';
    app.search();
    app.searchTerm = 'analytics';
    app.search();
    expect(navigate).not.toHaveBeenCalledWith('/analytics');
    app.searchTerm = 'pol';
    app.search();
    expect(navigate).toHaveBeenCalledWith('/policy');
    app.searchTerm = 'over';
    app.search();
    expect(navigate).toHaveBeenCalledWith('/');
    app.role = null;
    app.searchTerm = 'dashboard';
    app.search();
    expect(navigate).not.toHaveBeenCalledWith('/dashboard');
  });

  it('toggles shell controls and dispatches logout/toast actions', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    const store = TestBed.inject(MockStore);
    const dispatch = spyOn(store, 'dispatch').and.callThrough();
    const router = TestBed.inject(Router);
    const navigate = spyOn(router, 'navigateByUrl').and.resolveTo(true);
    app.onToggleSidebar(); expect(app.sidebarCollapsed).toBeTrue();
    app.onToggleSidebar(); expect(app.sidebarCollapsed).toBeFalse();
    app.onToggleNotifications(); expect(app.notificationOpen).toBeTrue(); expect(app.userMenuOpen).toBeFalse();
    app.onToggleUserMenu(); expect(app.userMenuOpen).toBeTrue(); expect(app.notificationOpen).toBeFalse();
    app.onCloseMobileSidebar();
    app.onDismissToast(4);
    app.notifications = [];
    app.onMarkAllRead();
    app.userMenuItems.find(item => item.label === 'Sign out')?.action?.();
    app.logout();
    expect(dispatch).toHaveBeenCalledWith(authActions.logout());
    expect(dispatch).toHaveBeenCalledWith(uiActions.dismissToast({ id: 4 }));
    expect(navigate).toHaveBeenCalledWith('/');
  });

  it('loads the current user and notifications, including error fallback and mark-all-read', () => {
    const store = TestBed.inject(MockStore);
    store.overrideSelector(selectIsAuthenticated, true);
    store.overrideSelector(selectUserId, 'u1');
    store.overrideSelector(selectUserRole, 'CLAIMS_MANAGER');
    store.refreshState();
    notifications.getAllNotifications.and.returnValue(of([
      { notificationId: 'n1', title: 'Review', message: 'Review claim', type: 'ACTION_REQUIRED', read: false, createdAt: '2026-01-01' },
      { notificationId: 'n2', title: 'Done', message: 'Completed', type: 'SUCCESS', read: true, createdAt: null },
    ] as any));
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    localStorage.setItem('is_token', 'token');
    app.ngOnInit();
    expect(app.displayName).toBe('Goutham');
    expect(app.notifications.length).toBeGreaterThan(0);
    expect(app.unreadCount).toBe(1);
    app.onMarkAllRead();
    expect(notifications.markRead).toHaveBeenCalledWith('n1');
    notifications.getAllNotifications.and.returnValue(throwError(() => new Error('offline')));
    (app as any).loadNotifications('u1');
    expect(app.notifications).toEqual([]);
    api.get.and.returnValue(of({ email: 'fallback@example.com' }));
    (app as any).loadCurrentUser();
    expect(app.displayName).toBe('fallback@example.com');
    api.get.and.returnValue(of({}));
    (app as any).loadCurrentUser();
    expect(app.displayName).toBeNull();
    localStorage.removeItem('is_token');
  });

  it('uses email fallback when the current-user request fails and maps notification kinds', () => {
    api.get.and.returnValue(throwError(() => new Error('unauthorized')));
    localStorage.setItem('is_token', 'token');
    localStorage.setItem('is_email', 'user@example.com');
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    app.ngOnInit();
    expect(app.displayName).toBe('user@example.com');
    expect((app as any).notificationKind('ERROR')).toBe('danger');
    expect((app as any).notificationKind('WARNING')).toBe('warning');
    expect((app as any).notificationKind('COMPLETE')).toBe('success');
    expect((app as any).notificationKind('INFO')).toBe('info');
    localStorage.removeItem('is_token');
    localStorage.removeItem('is_email');
  });

  it('restores role and identity values from a valid JWT payload when storage is incomplete', () => {
    const payload = btoa(JSON.stringify({ sub: 'u1', role: 'UNDERWRITER', customerId: 'c1', email: 'u@example.com' }));
    localStorage.setItem('is_token', `header.${payload}.signature`);
    localStorage.removeItem('is_role');
    localStorage.removeItem('is_user_id');
    const store = TestBed.inject(MockStore);
    const dispatch = spyOn(store, 'dispatch').and.callThrough();
    const fixture = TestBed.createComponent(AppComponent);
    fixture.componentInstance.ngOnInit();
    expect(dispatch).toHaveBeenCalledWith(jasmine.objectContaining({ token: `header.${payload}.signature`, role: 'UNDERWRITER', userId: 'u1' }));
    expect(localStorage.getItem('is_customer_id')).toBe('c1');
    localStorage.clear();
  });

  it('returns safely without a token and restores roles-array JWT claims', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    app.ngOnInit();
    expect(app.displayName).toBeNull();
    const payload = btoa(JSON.stringify({ userId: 'u2', roles: ['RISK_ENGINEER'] }));
    localStorage.setItem('is_token', `header.${payload}.signature`);
    localStorage.removeItem('is_role');
    localStorage.removeItem('is_user_id');
    const store = TestBed.inject(MockStore);
    const dispatch = spyOn(store, 'dispatch').and.callThrough();
    app.ngOnInit();
    expect(dispatch).toHaveBeenCalledWith(jasmine.objectContaining({ role: 'RISK_ENGINEER', userId: 'u2' }));
    localStorage.clear();
  });

  it('covers JWT fallback paths and navigation-end breadcrumb updates', () => {
    const payload = btoa(JSON.stringify({ sub: 'u3' }));
    localStorage.setItem('is_token', `header.${payload}.signature`);
    localStorage.setItem('is_role', 'UNDERWRITER');
    localStorage.removeItem('is_user_id');
    localStorage.removeItem('is_customer_id');
    const store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch').and.callThrough();
    const router = TestBed.inject(Router);
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    (router as any)._events.next(new NavigationEnd(1, '/claims/123', '/claims/123'));
    expect(app.currentPath).toBe('/claims/123');
    app.ngOnInit();
    expect(app.currentPath).toBe(router.url);
    localStorage.clear();
  });
});
