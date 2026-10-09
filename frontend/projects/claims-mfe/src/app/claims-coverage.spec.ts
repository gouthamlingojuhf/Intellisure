import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ClaimsService } from './services/claims.service';
import { PolicyService } from './services/policy.service';
import { RecoveryService } from './services/recovery.service';
import { claimsRoleGuard } from './claims-role.guard';
import { ClaimCreateComponent } from './features/claim-create/claim-create.component';
import { ClaimDetailComponent } from './features/claim-detail/claim-detail.component';
import { ClaimListComponent } from './features/claim-list/claim-list.component';

const policyId = '11111111-1111-4111-8111-111111111111';
const claim = {
  claimId: 'claim-1', policyId, customerId: 'customer-1', claimNumber: 'CLM-1001', status: 'OPEN',
  incidentDate: '2026-01-01', reportedDate: '2026-01-02', description: 'Water damage', estimatedLoss: 120000,
  createdAt: '2026-01-02', updatedAt: '2026-01-03', coverageConfirmed: true,
} as any;

describe('ClaimsService and PolicyService', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule], providers: [ClaimsService, PolicyService, RecoveryService] });
    http = TestBed.inject(HttpTestingController);
    localStorage.setItem('is_token', 'jwt');
  });

  afterEach(() => { http.verify(); localStorage.clear(); });

  it('builds filtered claim requests and authenticated lifecycle calls', () => {
    const service = TestBed.inject(ClaimsService);
    service.getClaims(' OPEN ', ' customer-1 ').subscribe();
    let request = http.expectOne(r => r.url.endsWith('/api/claims'));
    expect(request.request.params.get('status')).toBe('OPEN');
    expect(request.request.params.get('customerId')).toBe('customer-1');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt');
    expect(request.request.headers.has('X-Correlation-ID')).toBeTrue(); request.flush([]);
    service.getClaims(' ', ' ').subscribe(); request = http.expectOne(r => r.url.endsWith('/api/claims')); expect(request.request.params.keys()).toEqual([]); request.flush([]);
    service.getClaim('c1').subscribe(); request = http.expectOne(r => r.url.endsWith('/api/claims/c1')); request.flush(claim);
    service.fileClaim({ policyId, incidentDate: '2026-01-01', description: 'x' }).subscribe(); request = http.expectOne(r => r.url.endsWith('/api/claims')); expect(request.request.method).toBe('POST'); request.flush(claim);
    service.updateStatus('c1', 'APPROVED').subscribe(); request = http.expectOne(r => r.url.endsWith('/api/claims/c1/status')); expect(request.request.params.get('value')).toBe('APPROVED'); request.flush(claim);
  });

  it('builds authenticated policy and recovery calls', () => {
    const policies = TestBed.inject(PolicyService);
    policies.getCustomerPolicies('c1').subscribe();
    let request = http.expectOne(r => r.url.endsWith('/api/policies/customer/c1'));
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt'); request.flush([]);
    const recovery = TestBed.inject(RecoveryService);
    recovery.getCases('c1').subscribe(); request = http.expectOne(r => r.url.endsWith('/api/recovery/cases'));
    expect(request.request.params.get('customerId')).toBe('c1'); expect(request.request.params.get('size')).toBe('50'); request.flush({ items: [] });
    recovery.createCase({ claimId: 'c1', customerId: 'c1', severity: 'HIGH', recoveryObjective: 'Restore' }).subscribe();
    request = http.expectOne(r => r.url.endsWith('/api/recovery/cases')); expect(request.request.method).toBe('POST'); request.flush({});
  });
});

describe('claimsRoleGuard', () => {
  it('allows configured roles and redirects others', () => {
    TestBed.configureTestingModule({ providers: [] });
    localStorage.setItem('is_role', 'claims_manager');
    expect(TestBed.runInInjectionContext(() => claimsRoleGuard(['CLAIMS_MANAGER'])({} as any, {} as any))).toBeTrue();
    localStorage.setItem('is_role', 'POLICYHOLDER');
    const result = TestBed.runInInjectionContext(() => claimsRoleGuard(['CLAIMS_MANAGER'], ['/claims'])({} as any, {} as any)) as any;
    expect(result.toString()).toContain('/claims');
    localStorage.clear();
  });
});

describe('ClaimListComponent', () => {
  let claimsService: jasmine.SpyObj<ClaimsService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    claimsService = jasmine.createSpyObj<ClaimsService>('ClaimsService', ['getClaims']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    claimsService.getClaims.and.returnValue(of([claim]));
    TestBed.configureTestingModule({ imports: [ClaimListComponent], providers: [
      { provide: ClaimsService, useValue: claimsService }, { provide: Router, useValue: router },
    ] });
  });

  it('loads policyholder claims, navigates to FNOL, filters and renders business values', () => {
    localStorage.setItem('is_role', 'POLICYHOLDER');
    const component = TestBed.createComponent(ClaimListComponent).componentInstance;
    component.ngOnInit();
    expect(component.isPolicyholder).toBeTrue(); expect(component.claims).toEqual([claim]); expect(component.loading).toBeFalse();
    component.goToFileClaim(); expect(router.navigate).toHaveBeenCalledWith(['/claims', 'new']);
    component.onStatusChange({ target: { value: 'OPEN' } } as any); expect(claimsService.getClaims).toHaveBeenCalledWith('OPEN');
    expect(component.trackByClaimId(0, claim)).toBe('claim-1');
    expect(component.renderStatus(claim, 'APPROVED')).toContain('success');
    expect(component.renderStatus(claim, 'UNKNOWN')).toContain('neutral');
    expect(component.renderAmount(claim, 12345)).toContain('12,345');
    localStorage.clear();
  });

  it('keeps operational users safe on errors and maps all status errors', () => {
    localStorage.setItem('is_role', 'CLAIMS_ADJUSTER');
    claimsService.getClaims.and.returnValue(throwError(() => ({ status: 403 })));
    const component = TestBed.createComponent(ClaimListComponent).componentInstance;
    component.ngOnInit();
    expect(component.isPolicyholder).toBeFalse(); expect(component.error).toContain('permission');
    for (const error of [{ status: 401 }, { status: 404 }, { error: { message: 'backend' } }, { message: 'network' }, {}]) {
      (component as any).error = null;
      claimsService.getClaims.and.returnValue(throwError(() => error));
      component.loadClaims();
      expect(component.error).toBeTruthy();
    }
    localStorage.clear();
  });
});

describe('ClaimCreateComponent', () => {
  const policies = [
    { policyId, policyNumber: 'POL-1', productCode: 'BUSINESS_OWNER_POLICY', status: 'IN_FORCE' },
    { policyId: '22222222-2222-4222-8222-222222222222', policyNumber: 'POL-2', productCode: 'OLD', status: 'EXPIRED' },
  ] as any;
  let policyService: jasmine.SpyObj<PolicyService>;
  let claimsService: jasmine.SpyObj<ClaimsService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    policyService = jasmine.createSpyObj<PolicyService>('PolicyService', ['getCustomerPolicies']);
    claimsService = jasmine.createSpyObj<ClaimsService>('ClaimsService', ['fileClaim']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    policyService.getCustomerPolicies.and.returnValue(of(policies));
    TestBed.configureTestingModule({ imports: [ClaimCreateComponent], providers: [
      { provide: PolicyService, useValue: policyService }, { provide: ClaimsService, useValue: claimsService },
      { provide: Router, useValue: router }, { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => null } } } },
    ] });
  });

  it('loads eligible policies, validates steps, and submits a real FNOL payload', () => {
    localStorage.setItem('is_customer_id', 'customer-1');
    const component = TestBed.createComponent(ClaimCreateComponent).componentInstance;
    expect(component.policyOptions.length).toBe(1); expect(component.selectedPolicy?.policyNumber).toBe('POL-1');
    expect(component.getFieldLabel('policyId')).toBe('Policy number');
    component.onNext(); expect(component.currentStep).toBe(0);
    component.claimForm.setValue({ policyId, incidentDate: '2026-01-01', estimatedLoss: 120000, description: 'Water damage' });
    component.onNext(); expect(component.currentStep).toBe(1); component.onNext(); expect(component.currentStep).toBe(2); component.onPrevious(); expect(component.currentStep).toBe(1);
    claimsService.fileClaim.and.returnValue(of({ claimId: 'claim-1' } as any));
    component.onSubmit();
    expect(claimsService.fileClaim).toHaveBeenCalledWith({ policyId, incidentDate: '2026-01-01', description: 'Water damage', estimatedLoss: 120000 });
    expect(router.navigate).toHaveBeenCalledWith(['/claims', 'claim-1']);
    localStorage.clear();
  });

  it('reports missing profile, policy lookup and submit errors', () => {
    localStorage.clear();
    const component = TestBed.createComponent(ClaimCreateComponent).componentInstance;
    expect(component.policyLookupError).toContain('business profile');
    policyService.getCustomerPolicies.and.returnValue(throwError(() => ({ status: 403 })));
    localStorage.setItem('is_customer_id', 'customer-1');
    const failed = TestBed.createComponent(ClaimCreateComponent).componentInstance;
    expect(failed.policyLookupError).toContain('could not be loaded');
    failed.claimForm.setValue({ policyId, incidentDate: '2026-01-01', estimatedLoss: 0, description: 'Valid loss' });
    claimsService.fileClaim.and.returnValue(throwError(() => ({ status: 401 })));
    failed.onSubmit(); expect(failed.error).toContain('session');
    for (const error of [{ status: 403 }, { status: 404 }, { error: { message: 'backend' } }, { message: 'network' }, {}]) {
      claimsService.fileClaim.and.returnValue(throwError(() => error)); failed.onSubmit(); expect(failed.error).toBeTruthy();
    }
    localStorage.clear();
  });
});

describe('ClaimDetailComponent', () => {
  let claimsService: jasmine.SpyObj<ClaimsService>;
  let recoveryService: jasmine.SpyObj<RecoveryService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    claimsService = jasmine.createSpyObj<ClaimsService>('ClaimsService', ['getClaim']);
    recoveryService = jasmine.createSpyObj<RecoveryService>('RecoveryService', ['getCases', 'createCase']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({ imports: [ClaimDetailComponent], providers: [
      { provide: ClaimsService, useValue: claimsService }, { provide: RecoveryService, useValue: recoveryService },
      { provide: Router, useValue: router }, { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => 'claim-1' } } } },
    ] });
  });

  it('loads claim and recovery, starts recovery, and maps status/severity values', () => {
    claimsService.getClaim.and.returnValue(of(claim));
    recoveryService.getCases.and.returnValue(of({ items: [] } as any));
    const component = TestBed.createComponent(ClaimDetailComponent).componentInstance;
    component.ngOnInit();
    expect(component.claim).toBe(claim); expect(component.recoveryLoading).toBeFalse();
    expect(component.statusVariant('APPROVED')).toBe('success'); expect(component.statusVariant('RESERVED')).toBe('warning');
    expect(component.statusVariant('DENIED')).toBe('danger'); expect(component.statusVariant('OPEN')).toBe('info'); expect(component.statusVariant('X')).toBe('neutral');
    expect(component.formatStatus()).toBe('UNKNOWN');
    recoveryService.createCase.and.returnValue(of({ recoveryCaseId: 'r1', claimId: 'claim-1' } as any));
    component.startRecovery(); expect(component.recoveryCase?.recoveryCaseId).toBe('r1');
    component.goToRecovery(); component.goToClaims();
    expect(router.navigate).toHaveBeenCalledWith(['/claims']);
    expect((component as any).recoverySeverity(300000)).toBe('CRITICAL'); expect((component as any).recoverySeverity(120000)).toBe('HIGH');
    expect((component as any).recoverySeverity(25000)).toBe('MEDIUM'); expect((component as any).recoverySeverity(1)).toBe('LOW');
  });

  it('maps claim, recovery and create errors without exposing internal failures', () => {
    for (const error of [{ status: 404 }, { status: 403 }, { status: 401 }, { error: { message: 'backend' } }, { message: 'network' }, {}]) {
      claimsService.getClaim.and.returnValue(throwError(() => error));
      const component = TestBed.createComponent(ClaimDetailComponent).componentInstance;
      component.ngOnInit();
      expect(component.notFound || component.error).toBeTruthy();
    }
    claimsService.getClaim.and.returnValue(of(claim));
    recoveryService.getCases.and.returnValue(throwError(() => ({ status: 403 })));
    const component = TestBed.createComponent(ClaimDetailComponent).componentInstance; component.ngOnInit();
    expect(component.recoveryError).toContain('not available');
    recoveryService.createCase.and.returnValue(throwError(() => ({ status: 403 })));
    component.startRecovery(); expect(component.recoveryError).toContain('permission');
  });
});
