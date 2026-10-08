import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { ApiService } from './api.service';
import { QuoteService } from './quote.service';
import { PolicyService } from './policy.service';
import { CustomerProfileService } from './customer-profile.service';
import { QuoteResponse } from '../models/quote.models';
import { PolicyResponse } from '../models/policy.models';

export interface DashboardMetrics {
  activePoliciesCount: number;
  openQuotesCount: number;
  activeClaimsCount: number;
  ongoingRecoveriesCount: number;
  unreadNotificationsCount: number;
  businessName: string;
  customerId: string | null;
  recentQuotes: QuoteResponse[];
  activePolicies: PolicyResponse[];
  hasProfile: boolean;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly api = inject(ApiService);
  private readonly quoteService = inject(QuoteService);
  private readonly policyService = inject(PolicyService);
  private readonly profileService = inject(CustomerProfileService);

  getDashboardData(userId: string | null, storedCustomerId: string | null): Observable<DashboardMetrics> {
    return this.profileService.getProfile().pipe(
      catchError(() => of(null)),
      switchMap((profile) => {
        const customerId = profile?.customerId || storedCustomerId || null;
        const businessName = profile?.businessName || '';
        const hasProfile = !!profile;

        const quotes$ = customerId
          ? this.quoteService.getQuotesByCustomerId(customerId).pipe(catchError(() => of([])))
          : of([]);
        const policies$ = customerId
          ? this.policyService.getPoliciesByCustomerId(customerId).pipe(catchError(() => of([])))
          : of([]);
        const claims$ = this.api.get<any[]>('/api/claims').pipe(catchError(() => of([])));
        const recoveryCases$ = customerId
          ? this.api.get<any>('/api/recovery/cases', { customerId }).pipe(
              map((res) => (res && Array.isArray(res.items) ? res.items : [])),
              catchError(() => of([]))
            )
          : of([]);
        const unreadCount$ = userId
          ? this.api.get<number>('/api/notifications/unread-count', { userId }).pipe(catchError(() => of(0)))
          : of(0);

        return forkJoin({
          quotes: quotes$,
          policies: policies$,
          claims: claims$,
          recoveries: recoveryCases$,
          unreadCount: unreadCount$,
        }).pipe(
          map(({ quotes, policies, claims, recoveries, unreadCount }) => {
            const openQuotes = (quotes || []).filter(
              (q) => !['DECLINED_BY_CUSTOMER', 'DECLINED_BY_INSURER', 'EXPIRED', 'WITHDRAWN'].includes(q.status)
            );
            const activePolicies = (policies || []).filter((p) =>
              ['IN_FORCE', 'BOUND'].includes(p.status)
            );
            const claimList: Array<{ status?: string }> = Array.isArray(claims) ? claims : [];
            const activeClaims = claimList.filter(
              (c: { status?: string }) => !['CLOSED', 'REJECTED', 'DENIED'].includes(c.status || '')
            );
            const recoveryList: Array<{ status?: string }> = Array.isArray(recoveries) ? recoveries : [];
            const ongoingRecoveries = recoveryList.filter(
              (r: { status?: string }) => !['COMPLETED', 'CANCELLED'].includes(r.status || '')
            );

            return {
              activePoliciesCount: activePolicies.length,
              openQuotesCount: openQuotes.length,
              activeClaimsCount: activeClaims.length,
              ongoingRecoveriesCount: ongoingRecoveries.length,
              unreadNotificationsCount: typeof unreadCount === 'number' ? unreadCount : 0,
              businessName,
              customerId,
              recentQuotes: (quotes || []).slice(0, 5),
              activePolicies: activePolicies.slice(0, 5),
              hasProfile,
            };
          })
        );
      })
    );
  }
}
