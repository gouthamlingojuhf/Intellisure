import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { of, throwError } from 'rxjs';

import { AuthApiService } from './services/auth-api.service';
import { LoginComponent } from './features/login/login.component';
import { RegisterComponent } from './features/register/register.component';
import { ProfileComponent } from './features/profile/profile.component';
import { authRemoteActions } from './store/auth.actions';
import { authRemoteReducer, initialAuthRemoteState } from './store/auth.reducer';
import { selectRemoteError, selectRemoteLoading, selectRegistered } from './store/auth.selectors';
import { AppComponent } from './app.component';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule], providers: [AuthApiService] });
    service = TestBed.inject(AuthApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('calls login and registration with correlation IDs', () => {
    service.login({ email: 'u@example.com', password: 'Password@123' }).subscribe();
    let request = http.expectOne(r => r.url.endsWith('/api/auth/login'));
    expect(request.request.method).toBe('POST');
    expect(request.request.headers.has('X-Correlation-ID')).toBeTrue();
    request.flush({ accessToken: 'token' });

    service.register({ email: 'u@example.com', password: 'Password@123', displayName: 'User' }).subscribe();
    request = http.expectOne(r => r.url.endsWith('/api/auth/register'));
    expect(request.request.headers.has('X-Correlation-ID')).toBeTrue();
    request.flush({});
  });

  it('calls profile with a bearer token and correlation ID', () => {
    service.me('jwt-token').subscribe();
    const request = http.expectOne(r => r.url.endsWith('/api/auth/me'));
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    expect(request.request.headers.has('X-Correlation-ID')).toBeTrue();
    request.flush({ userId: 'u1' });
  });
});

describe('auth remote reducer and selectors', () => {
  it('handles login, register, profile, errors and logout', () => {
    let state = authRemoteReducer(initialAuthRemoteState, authRemoteActions.login({ request: { email: 'a', password: 'b' } }));
    expect(state.loading).toBeTrue();
    state = authRemoteReducer(state, authRemoteActions.loginSuccess({ response: { token: 't' } }));
    expect(state.token).toBe('t');
    state = authRemoteReducer(state, authRemoteActions.loginFailure({ error: 'bad login' }));
    expect(state.error).toBe('bad login');
    state = authRemoteReducer(state, authRemoteActions.register({ request: { email: 'a', password: 'b', displayName: 'A' } }));
    expect(state.registered).toBeFalse();
    state = authRemoteReducer(state, authRemoteActions.registerSuccess());
    expect(state.registered).toBeTrue();
    state = authRemoteReducer(state, authRemoteActions.registerFailure({ error: 'bad register' }));
    expect(state.error).toBe('bad register');
    state = authRemoteReducer(state, authRemoteActions.profileLoaded({ profile: { email: 'a' } }));
    expect(state.profile?.email).toBe('a');
    state = authRemoteReducer(state, authRemoteActions.logout());
    expect(state.token).toBeNull();
    expect(selectRemoteError.projector(undefined)).toBeNull();
    expect(selectRemoteLoading.projector(undefined)).toBeFalse();
    expect(selectRegistered.projector(undefined)).toBeFalse();
  });
});

describe('auth route components', () => {
  let store: MockStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoginComponent, RegisterComponent, ProfileComponent],
      providers: [provideMockStore({ selectors: [
        { selector: selectRemoteLoading, value: false },
        { selector: selectRemoteError, value: null },
        { selector: selectRegistered, value: false },
      ] }), provideRouter([])],
    });
    store = TestBed.inject(MockStore);
  });

  it('validates login and dispatches a complete request only when valid', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    const dispatch = spyOn(store, 'dispatch').and.callThrough();
    component.submit();
    expect(dispatch).not.toHaveBeenCalled();
    component.form.setValue({ email: 'u@example.com', password: 'Password@123' });
    component.submit();
    expect(dispatch).toHaveBeenCalledWith(authRemoteActions.login({ request: { email: 'u@example.com', password: 'Password@123' } }));
  });

  it('validates registration and preserves Vendor Applicant selection', () => {
    const fixture = TestBed.createComponent(RegisterComponent);
    const component = fixture.componentInstance;
    const dispatch = spyOn(store, 'dispatch').and.callThrough();
    component.submit();
    expect(dispatch).not.toHaveBeenCalled();
    component.form.setValue({
      displayName: 'Vendor User', email: 'vendor@example.com', password: 'Password@123', registrationType: 'VENDOR_APPLICANT',
    });
    component.submit();
    expect(dispatch).toHaveBeenCalledWith(authRemoteActions.register({ request: component.form.getRawValue() }));
  });

  it('redirects the profile remote to the shell profile', () => {
    const router = TestBed.inject(Router);
    const navigate = spyOn(router, 'navigateByUrl').and.resolveTo(true);
    TestBed.createComponent(ProfileComponent).componentInstance.ngOnInit();
    expect(navigate).toHaveBeenCalledWith('/profile');
  });
});

describe('auth remote shell login component', () => {
  let api: jasmine.SpyObj<AuthApiService>;
  let router: Router;

  beforeEach(async () => {
    api = jasmine.createSpyObj<AuthApiService>('AuthApiService', ['login']);
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [{ provide: AuthApiService, useValue: api }, provideRouter([])],
    }).compileComponents();
    router = TestBed.inject(Router);
  });

  it('marks invalid login forms and reports precise control errors', () => {
    const component = TestBed.createComponent(AppComponent).componentInstance;
    component.onSubmit();
    expect(component.loginForm.controls.email.touched).toBeTrue();
    component.loginForm.controls.email.setValue('bad');
    component.loginForm.controls.email.markAsTouched();
    expect(component.getError('email')).toBe('Enter a valid email address');
    component.loginForm.controls.email.setValue('');
    expect(component.getError('email')).toBe('Email is required');
    component.loginForm.controls.password.setValue('short');
    component.loginForm.controls.password.markAsTouched();
    expect(component.getError('password')).toBe('Password must be at least 8 characters');
    expect(component.getError('unknown')).toBeUndefined();
  });

  it('stores customer login and routes to dashboard, or profile without a customer', () => {
    const navigate = spyOn(router, 'navigateByUrl').and.resolveTo(true);
    api.login.and.returnValue(of({ accessToken: 't', customerId: 'c1', userId: 'u1', role: 'POLICYHOLDER', email: 'u@example.com' }));
    const component = TestBed.createComponent(AppComponent).componentInstance;
    component.loginForm.setValue({ email: 'u@example.com', password: 'Password@123', rememberMe: true });
    component.onSubmit();
    expect(component.successMessage).toContain('successful');
    expect(navigate).toHaveBeenCalledWith('/dashboard');
    expect(localStorage.getItem('is_customer_id')).toBe('c1');

    api.login.and.returnValue(of({ token: 't2', role: 'UNDERWRITER' }));
    component.onSubmit();
    expect(navigate).toHaveBeenCalledWith('/profile');
    localStorage.clear();
  });

  it('uses backend, generic error, and provider branches', () => {
    const component = TestBed.createComponent(AppComponent).componentInstance;
    api.login.and.returnValue(throwError(() => ({ error: { message: 'Backend rejected' } })));
    component.loginForm.setValue({ email: 'u@example.com', password: 'Password@123', rememberMe: false });
    component.onSubmit();
    expect(component.errorMessage).toBe('Backend rejected');
    api.login.and.returnValue(throwError(() => ({ message: 'Network failed' })));
    component.onSubmit();
    expect(component.errorMessage).toBe('Network failed');
    api.login.and.returnValue(throwError(() => ({})));
    component.onSubmit();
    expect(component.errorMessage).toContain('Invalid credentials');
    expect(() => component.loginWithProvider('google')).not.toThrow();
  });
});
