import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpRequest } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, UrlTree } from '@angular/router';
import { provideRouter } from '@angular/router';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { of, throwError } from 'rxjs';

import { ApiService } from './services/api.service';
import { CustomerProfileService } from './services/customer-profile.service';
import { DashboardService } from './services/dashboard.service';
import { DocumentService } from './services/document.service';
import { NotificationService } from './services/notification.service';
import { PolicyService } from './services/policy.service';
import { QuoteService } from './services/quote.service';
import { RecoveryService } from './services/recovery.service';
import { UnderwritingService } from './services/underwriting.service';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';
import { unauthGuard } from './guards/unauth.guard';
import { correlationIdInterceptor, CORRELATION_ID_HEADER } from './interceptors/correlation-id.interceptor';
import { jwtInterceptor } from './interceptors/jwt.interceptor';
import { errorInterceptor } from './interceptors/error.interceptor';
import { authActions } from './store/auth/auth.actions';
import { authReducer, initialAuthState, AuthState } from './store/auth/auth.reducer';
import { selectAuthLoading, selectAuthToken, selectIsAuthenticated, selectUserRole } from './store/auth/auth.selectors';
import { uiActions } from './store/ui/ui.actions';
import { initialUiState, uiReducer } from './store/ui/ui.reducer';
import { selectGlobalLoading, selectToasts } from './store/ui/ui.selectors';

describe('core API clients', () => {
  let api: jasmine.SpyObj<ApiService>;

  beforeEach(() => {
    api = jasmine.createSpyObj<ApiService>('ApiService', ['get', 'post', 'put', 'patch', 'delete']);
    TestBed.configureTestingModule({ providers: [{ provide: ApiService, useValue: api }] });
  });

  it('builds quote, policy, recovery and underwriting requests', () => {
    api.post.and.returnValue(of({}));
    api.get.and.returnValue(of([]));
    api.patch.and.returnValue(of({}));

    const quotes = TestBed.inject(QuoteService);
    quotes.createDraftQuote({} as any).subscribe();
    quotes.getQuoteById('q1').subscribe();
    quotes.getQuotesByCustomerId('c1').subscribe();
    quotes.submitQuote('q1').subscribe();
    quotes.acceptQuote('q1').subscribe();
    quotes.declineQuote('q1', 'too expensive').subscribe();
    quotes.getUnderwritingDecisions('q1').subscribe();
    quotes.recordUnderwritingDecision('q1', {} as any).subscribe();
    quotes.offerQuoteTerms('q1', {} as any).subscribe();
    quotes.getSubjectivities('q1').subscribe();

    const policies = TestBed.inject(PolicyService);
    policies.getPoliciesByCustomerId('c1').subscribe();
    policies.getPolicyById('p1').subscribe();
    policies.getPolicyByNumber('POL-1').subscribe();

    const recovery = TestBed.inject(RecoveryService);
    recovery.getCases().subscribe();
    recovery.getCases('c1').subscribe();
    recovery.selectPath('r1', {} as any).subscribe();
    recovery.recordProgress('r1', {} as any).subscribe();

    const underwriting = TestBed.inject(UnderwritingService);
    underwriting.getAssignedQueue('u1', 'RISK_ENGINEER').subscribe();
    underwriting.getAssignedQueue('u1', 'UNDERWRITER').subscribe();

    expect(api.post).toHaveBeenCalledWith('/api/quotes', {});
    expect(api.patch).toHaveBeenCalledWith('/api/quotes/q1/decline', { reason: 'too expensive' });
    expect(api.get).toHaveBeenCalledWith('/api/risk-assessments/underwriter/u1');
    expect(api.get).toHaveBeenCalledWith('/api/recovery/cases', { page: 0, size: 50 });
  });

  it('builds document, notification and customer profile requests', () => {
    api.get.and.returnValue(of({ items: [], totalElements: 0 }));
    api.put.and.returnValue(of({ customerId: 'c1' }));
    api.patch.and.returnValue(of({ notificationId: 'n1' }));

    const documents = TestBed.inject(DocumentService);
    documents.getDocuments('p1', 'POLICY').subscribe();
    expect(api.get).toHaveBeenCalledWith('/api/documents', { entityId: 'p1', entityType: 'POLICY' });

    const notifications = TestBed.inject(NotificationService);
    notifications.getNotifications('u1', false).subscribe();
    notifications.getAllNotifications('u1').subscribe();
    notifications.getUnreadCount('u1').subscribe();
    notifications.markRead('n1').subscribe();
    expect(api.patch).toHaveBeenCalledWith('/api/notifications/n1/read', { notificationId: 'n1' });

    const profile = TestBed.inject(CustomerProfileService);
    profile.getProfile().subscribe();
    profile.updateProfile({} as any).subscribe();
    expect(api.put).toHaveBeenCalledWith('/api/customers/me', {});
    expect(localStorage.getItem('is_customer_id')).toBe('c1');
  });

  it('sorts all notifications by newest timestamp and tolerates missing pages', () => {
    api.get.and.callFake(((path: string, params: any) => params.read
      ? of({ items: [{ notificationId: 'old', createdAt: '2025-01-01' }] })
      : of({ items: [{ notificationId: 'new', createdAt: '2026-01-01' }] })) as any);
    const result: string[] = [];
    TestBed.inject(NotificationService).getAllNotifications('u1').subscribe(items => result.push(...items.map(item => item.notificationId)));
    expect(result).toEqual(['new', 'old']);

    api.get.and.returnValue(of({}));
    TestBed.inject(NotificationService).getAllNotifications('u1').subscribe(items => expect(items).toEqual([]));
  });
});

describe('DashboardService', () => {
  it('aggregates real responses into policyholder metrics and applies lifecycle filters', () => {
    const api = jasmine.createSpyObj<ApiService>('ApiService', ['get']);
    const quote = jasmine.createSpyObj<QuoteService>('QuoteService', ['getQuotesByCustomerId']);
    const policy = jasmine.createSpyObj<PolicyService>('PolicyService', ['getPoliciesByCustomerId']);
    const profile = jasmine.createSpyObj<CustomerProfileService>('CustomerProfileService', ['getProfile']);
    profile.getProfile.and.returnValue(of({ customerId: 'c1', businessName: 'Acme' } as any));
    quote.getQuotesByCustomerId.and.returnValue(of([{ status: 'DRAFT' }, { status: 'EXPIRED' }] as any));
    policy.getPoliciesByCustomerId.and.returnValue(of([{ status: 'IN_FORCE' }, { status: 'EXPIRED' }] as any));
    api.get.and.callFake(((path: string) => path.includes('notifications') ? of(3) : path.includes('recovery') ? of({ items: [{ status: 'OPEN' }, { status: 'COMPLETED' }] }) : of([{ status: 'OPEN' }, { status: 'CLOSED' }])) as any);
    TestBed.configureTestingModule({ providers: [
      DashboardService,
      { provide: ApiService, useValue: api },
      { provide: QuoteService, useValue: quote },
      { provide: PolicyService, useValue: policy },
      { provide: CustomerProfileService, useValue: profile },
    ] });

    TestBed.inject(DashboardService).getDashboardData('u1', null).subscribe(metrics => {
      expect(metrics).toEqual(jasmine.objectContaining({
        activePoliciesCount: 1, openQuotesCount: 1, activeClaimsCount: 1,
        ongoingRecoveriesCount: 1, unreadNotificationsCount: 3,
        businessName: 'Acme', customerId: 'c1', hasProfile: true,
      }));
    });
  });

  it('uses stored customer identity and honest empty fallbacks when APIs fail', () => {
    const api = jasmine.createSpyObj<ApiService>('ApiService', ['get']);
    const quote = jasmine.createSpyObj<QuoteService>('QuoteService', ['getQuotesByCustomerId']);
    const policy = jasmine.createSpyObj<PolicyService>('PolicyService', ['getPoliciesByCustomerId']);
    const profile = jasmine.createSpyObj<CustomerProfileService>('CustomerProfileService', ['getProfile']);
    profile.getProfile.and.returnValue(throwError(() => new Error('404')));
    quote.getQuotesByCustomerId.and.returnValue(throwError(() => new Error('down')));
    policy.getPoliciesByCustomerId.and.returnValue(throwError(() => new Error('down')));
    api.get.and.returnValue(throwError(() => new Error('down')));
    TestBed.configureTestingModule({ providers: [
      DashboardService, { provide: ApiService, useValue: api }, { provide: QuoteService, useValue: quote },
      { provide: PolicyService, useValue: policy }, { provide: CustomerProfileService, useValue: profile },
    ] });
    TestBed.inject(DashboardService).getDashboardData(null, 'stored-customer').subscribe(metrics => {
      expect(metrics.customerId).toBe('stored-customer');
      expect(metrics.hasProfile).toBeFalse();
      expect(metrics.openQuotesCount).toBe(0);
      expect(metrics.unreadNotificationsCount).toBe(0);
    });
  });
});

describe('auth and UI reducers/selectors', () => {
  it('handles login lifecycle, token fallback, restore and logout', () => {
    let state = authReducer(initialAuthState, authActions.login({ request: { email: 'a', password: 'b' } }));
    expect(state.loading).toBeTrue();
    state = authReducer(state, authActions.loginSuccess({ response: { token: 't', userId: 'u', role: 'ADMIN', email: 'a' } }));
    expect(state).toEqual(jasmine.objectContaining({ token: 't', userId: 'u', role: 'ADMIN', isAuthenticated: true, loading: false }));
    state = authReducer(state, authActions.loginFailure({ error: 'bad' }));
    expect(state.loginError).toBe('bad');
    state = authReducer(state, authActions.restoreSession({ token: 't2' }));
    expect(state.token).toBe('t2');
    state = authReducer(state, authActions.logout());
    expect(state.isAuthenticated).toBeFalse();
    expect(state.token).toBeNull();
  });

  it('creates and dismisses typed toasts and exposes selectors', () => {
    let state = uiReducer(initialUiState, uiActions.setLoading({ loading: true }));
    state = uiReducer(state, uiActions.showToast({ message: 'Saved', kind: 'success' }));
    expect(state.loading).toBeTrue();
    expect(state.toasts.length).toBe(1);
    state = uiReducer(state, uiActions.dismissToast({ id: state.toasts[0].id }));
    expect(state.toasts).toEqual([]);
    expect(selectGlobalLoading.projector({ loading: true, toasts: [] })).toBeTrue();
    expect(selectToasts.projector({ loading: false, toasts: [{ id: 1, message: 'x', kind: 'info' }] })).toHaveSize(1);
    expect(selectAuthToken.projector({ ...initialAuthState, token: 't' })).toBe('t');
    expect(selectIsAuthenticated.projector({ ...initialAuthState, isAuthenticated: true })).toBeTrue();
    expect(selectAuthLoading.projector({ ...initialAuthState, loading: true })).toBeTrue();
  });
});

describe('route guards', () => {
  let store: MockStore;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideMockStore(), provideRouter([])] });
    store = TestBed.inject(MockStore);
    router = TestBed.inject(Router);
  });

  it('allows authenticated routes and restores a persisted session', (done) => {
    store.overrideSelector(selectIsAuthenticated, false);
    spyOn(localStorage, 'getItem').and.callFake((key: string) => key === 'is_token' ? 'token' : null);
    const dispatch = spyOn(store, 'dispatch').and.callThrough();
    TestBed.runInInjectionContext(() => (authGuard({} as any, {} as any) as any).subscribe((result: any) => {
      expect(result).toBeTrue();
      expect(dispatch).toHaveBeenCalledWith(jasmine.objectContaining({ type: '[Auth] restoreSession' }));
      done();
    }));
  });

  it('redirects unauthenticated and authenticated users to the right destinations', (done) => {
    store.overrideSelector(selectIsAuthenticated, false);
    TestBed.runInInjectionContext(() => (unauthGuard({} as any, {} as any) as any).subscribe((result: any) => {
      expect(result).toBeTrue();
      store.overrideSelector(selectIsAuthenticated, true);
      store.refreshState();
      TestBed.runInInjectionContext(() => (unauthGuard({} as any, {} as any) as any).subscribe((authResult: any) => {
        expect((authResult as UrlTree).toString()).toContain('/policy');
        done();
      }));
    }));
  });

  it('enforces allowed roles and emits a denial toast for unauthorized roles', (done) => {
    store.overrideSelector(selectUserRole, 'POLICYHOLDER');
    const dispatch = spyOn(store, 'dispatch').and.callThrough();
    const route = { data: { roles: ['UNDERWRITER'] } } as unknown as ActivatedRouteSnapshot;
    TestBed.runInInjectionContext(() => (roleGuard(['UNDERWRITER'])(route, {} as any) as any).subscribe((result: any) => {
      expect((result as UrlTree).toString()).toContain('/');
      expect(dispatch).toHaveBeenCalledWith(jasmine.objectContaining({ type: '[UI] showToast' }));
      done();
    }));
  });
});

describe('HTTP interceptors', () => {
  it('adds correlation IDs and preserves an existing ID', () => {
    TestBed.configureTestingModule({ providers: [provideMockStore()] });
    const httpReq = new HttpRequest('GET', '/api/test');
    let forwarded: any;
    TestBed.runInInjectionContext(() => correlationIdInterceptor(httpReq, next => { forwarded = next; return of({} as any); }));
    expect(forwarded.headers.has(CORRELATION_ID_HEADER)).toBeTrue();
    const existing = httpReq.clone({ setHeaders: { [CORRELATION_ID_HEADER]: 'corr-1' } });
    TestBed.runInInjectionContext(() => correlationIdInterceptor(existing, next => { forwarded = next; return of({} as any); }));
    expect(forwarded.headers.get(CORRELATION_ID_HEADER)).toBe('corr-1');
  });

  it('adds JWT only to absolute requests and falls back to local storage', (done) => {
    TestBed.configureTestingModule({ providers: [provideMockStore()] });
    const store = TestBed.inject(MockStore);
    store.overrideSelector(selectAuthToken, null);
    spyOn(localStorage, 'getItem').and.callFake((key: string) => key === 'is_token' ? 'persisted' : null);
    const absolute = new HttpRequest('GET', 'http://api.test/data');
    const relative = new HttpRequest('GET', '/api/data');
    TestBed.runInInjectionContext(() => jwtInterceptor(absolute, req => { expect(req.headers.get('Authorization')).toBe('Bearer persisted'); return of({} as any); }).subscribe());
    TestBed.runInInjectionContext(() => jwtInterceptor(relative, req => { expect(req.headers.has('Authorization')).toBeFalse(); return of({} as any); }).subscribe(() => done()));
  });

  it('dispatches logout, denial and server toasts for HTTP failures', (done) => {
    TestBed.configureTestingModule({ providers: [provideMockStore(), provideRouter([])] });
    const store = TestBed.inject(MockStore);
    const router = TestBed.inject(Router);
    const dispatch = spyOn(store, 'dispatch').and.callThrough();
    const navigate = spyOn(router, 'navigate').and.resolveTo(true);
    const req = new HttpRequest('GET', '/api/test');
    const error = (status: number) => TestBed.runInInjectionContext(() => errorInterceptor(req, () => throwError(() => ({ status } as any))).subscribe({ error: () => undefined }));
    error(401); error(403); error(500);
    expect(dispatch).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(['/auth']);
    done();
  });
});

describe('ApiService HTTP contract', () => {
  let http: HttpTestingController;
  let service: ApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule], providers: [ApiService] });
    http = TestBed.inject(HttpTestingController);
    service = TestBed.inject(ApiService);
  });

  afterEach(() => http.verify());

  it('sends gateway CRUD requests and omits undefined query values', () => {
    service.get('/api/items', { page: 0, active: true, omitted: undefined }).subscribe();
    let request = http.expectOne(r => r.url.includes('/api/items'));
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('active')).toBe('true');
    expect(request.request.params.has('omitted')).toBeFalse();
    request.flush([]);
    service.post('/api/items', { name: 'x' }).subscribe();
    request = http.expectOne(r => r.url.endsWith('/api/items')); expect(request.request.method).toBe('POST'); request.flush({});
    service.put('/api/items/1', {}).subscribe(); request = http.expectOne(r => r.url.endsWith('/api/items/1')); expect(request.request.method).toBe('PUT'); request.flush({});
    service.patch('/api/items/1', {}).subscribe(); request = http.expectOne(r => r.url.endsWith('/api/items/1')); expect(request.request.method).toBe('PATCH'); request.flush({});
    service.delete('/api/items/1').subscribe(); request = http.expectOne(r => r.url.endsWith('/api/items/1')); expect(request.request.method).toBe('DELETE'); request.flush({});
  });
});
