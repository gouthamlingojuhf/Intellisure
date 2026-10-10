import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap, take } from 'rxjs/operators';
import { Store } from '@ngrx/store';

import { QuoteService } from '../../core/services/quote.service';
import { CustomerProfileService } from '../../core/services/customer-profile.service';
import { ApiService } from '../../core/services/api.service';
import { QuoteResponse } from '../../core/models/quote.models';
import { selectUserId, selectUserRole } from '../../core/store/auth/auth.selectors';
import { uiActions } from '../../core/store/ui/ui.actions';

import {
  CardComponent,
  ButtonComponent,
  BadgeComponent,
  BadgeVariant,
  SkeletonComponent,
  EmptyStateComponent,
  ModalComponent,
} from 'ui-core';

interface UnderwriterUser {
  userId: string;
  email?: string;
  displayName?: string;
  firstName?: string;
  lastName?: string;
  accountStatus?: string;
}

@Component({
  selector: 'is-quotes-list',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    DatePipe,
    RouterLink,
    FormsModule,
    CardComponent,
    ButtonComponent,
    BadgeComponent,
    SkeletonComponent,
    EmptyStateComponent,
    ModalComponent,
  ],
  template: `
    <div class="quotes-page">
      <header class="page-header">
        <div class="header-main">
          <p class="page-eyebrow">Underwriting & Submissions</p>
          <div class="title-row">
            <h1>Commercial Insurance Quotes</h1>
            @if (!loading) {
              <is-badge variant="info" size="md">
                {{ filteredQuotes.length }} Quotes
              </is-badge>
            }
          </div>
          <p class="page-description">
            {{ isUnderwriter
              ? 'View commercial insurance quotes assigned to you for underwriting review and terms offering.'
              : isAdmin
                ? 'Manage enterprise submissions, track review status, and assign or reassign underwriters.'
                : 'Manage your commercial insurance applications, view offered terms, and accept coverage.'
            }}
          </p>
        </div>
        @if (!isEmployee) {
          <div class="header-actions">
            <is-button variant="primary" routerLink="/quotes/new">
              Request New Quote &rarr;
            </is-button>
          </div>
        }
      </header>

      @if (loading) {
        <div class="loading-state" role="status" aria-live="polite">
          <is-skeleton variant="text" width="30%" height="24px" />
          <is-skeleton variant="card" />
        </div>
      } @else if (profileMissing && !isEmployee) {
        <div class="notice-card warning">
          <div class="notice-icon" aria-hidden="true">🏢</div>
          <div>
            <h3>Business Profile Incomplete</h3>
            <p>Setup your legal business details and address before creating and viewing commercial quotes.</p>
          </div>
          <is-button variant="primary" size="sm" routerLink="/profile">Complete Profile &rarr;</is-button>
        </div>
      } @else {
        <!-- Filter Bar -->
        <div class="filter-bar">
          <div class="search-box">
            <input
              type="text"
              [(ngModel)]="searchQuery"
              placeholder="Search by quote # or product code…"
              aria-label="Filter quotes"
            />
          </div>
          <div class="status-filter">
            <select [(ngModel)]="statusFilter" aria-label="Filter by status">
              <option value="">All statuses</option>
              <option value="SUBMITTED">Submitted</option>
              <option value="IN_REVIEW">In Review</option>
              <option value="QUOTED">Quoted</option>
              <option value="ACCEPTED">Accepted</option>
              <option value="BOUND">Bound</option>
              <option value="DECLINED">Declined</option>
            </select>
          </div>
          <is-button variant="secondary" size="sm" (click)="loadQuotes()">
            Refresh ⟳
          </is-button>
        </div>

        <is-card
          [title]="isUnderwriter ? 'My Assigned Quotes Queue' : 'Commercial Insurance Quotes'"
          [subtitle]="isUnderwriter ? 'Active applications assigned to your underwriting desk' : 'Commercial applications and review status'"
        >
          @if (filteredQuotes.length === 0) {
            <is-empty-state
              title="No quotes found"
              [description]="searchQuery || statusFilter ? 'No quotes matched your current filter criteria.' : 'No quotes currently available.'"
              icon="📝"
              [actionLabel]="isEmployee ? '' : 'Request New Quote'"
              (action)="isEmployee ? null : navigate('/quotes/new')"
            />
          } @else {
            <div class="table-container">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Quote Number</th>
                    <th>Product</th>
                    <th>Status</th>
                    <th>Effective Date</th>
                    <th>Total Premium</th>
                    @if (isAdmin) {
                      <th>Assigned Underwriter</th>
                    }
                    <th class="text-right">Action</th>
                  </tr>
                </thead>
                <tbody>
                  @for (quote of filteredQuotes; track quote.quoteId) {
                    <tr>
                      <td>
                        <strong>{{ quote.quoteNumber }}</strong>
                      </td>
                      <td>
                        <span class="code-pill">{{ quote.productCode }}</span>
                      </td>
                      <td>
                        <is-badge [variant]="getQuoteStatusVariant(quote.status)" size="sm">
                          {{ formatStatus(quote.status) }}
                        </is-badge>
                      </td>
                      <td>{{ quote.requestedEffectiveDate }}</td>
                      <td>
                        @if (quote.totalPremium !== null && quote.totalPremium !== undefined) {
                          <strong>{{ quote.totalPremium | currency:'INR':'symbol':'1.0-0' }}</strong>
                        } @else {
                          <span class="text-muted">Awaiting Rating</span>
                        }
                      </td>
                      @if (isAdmin) {
                        <td>
                          <div class="underwriter-cell">
                            @if (quote.assignedUnderwriterId) {
                              <span class="uw-name" [title]="quote.assignedUnderwriterId">
                                {{ getUnderwriterLabel(quote.assignedUnderwriterId) }}
                              </span>
                            } @else {
                              <span class="unassigned-badge">Unassigned</span>
                            }
                            @if (canReassign(quote.status)) {
                              <button
                                type="button"
                                class="btn-reassign"
                                (click)="openReassignModal(quote)"
                                title="Assign or reassign an underwriter"
                              >
                                {{ quote.assignedUnderwriterId ? 'Reassign' : 'Assign' }}
                              </button>
                            }
                          </div>
                        </td>
                      }
                      <td class="text-right">
                        <is-button
                          variant="text"
                          size="sm"
                          (click)="navigate('/quotes/' + quote.quoteId)"
                        >
                          View Details &rarr;
                        </is-button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </is-card>
      }

      <!-- Underwriter Reassignment Modal for Admin -->
      @if (showReassignModal && selectedQuoteForReassign) {
        <is-modal
          [open]="showReassignModal"
          title="Assign / Reassign Underwriter"
          [size]="'md'"
          (close)="closeReassignModal()"
        >
          <div class="reassign-modal-body">
            <p class="modal-desc">
              Assign or reassign an underwriter for Quote <strong>{{ selectedQuoteForReassign.quoteNumber }}</strong>
              ({{ formatStatus(selectedQuoteForReassign.status) }}).
            </p>

            <div class="form-group">
              <label for="underwriterSelect">Select Underwriter:</label>
              @if (loadingUnderwriters) {
                <p class="loading-uw">Loading eligible underwriters…</p>
              } @else if (underwriters.length === 0) {
                <div class="manual-input-box">
                  <p class="warning-text">No active underwriters found automatically. Enter Underwriter User ID (UUID):</p>
                  <input
                    id="underwriterIdInput"
                    type="text"
                    [(ngModel)]="manualUnderwriterId"
                    placeholder="e.g. 5d946cd5-fe42-420d-a970-39b350308eb5"
                    class="uw-input"
                  />
                </div>
              } @else {
                <select
                  id="underwriterSelect"
                  [(ngModel)]="selectedUnderwriterId"
                  class="uw-select"
                >
                  <option value="" disabled>Choose an underwriter</option>
                  @for (uw of underwriters; track uw.userId) {
                    <option [value]="uw.userId">
                      {{ uw.displayName ? uw.displayName : (uw.firstName ? (uw.firstName + ' ' + (uw.lastName || '')) : (uw.email || uw.userId)) }}
                    </option>
                  }
                </select>
              }
            </div>

            @if (reassignError) {
              <div class="error-msg" role="alert">{{ reassignError }}</div>
            }

            <div class="modal-actions">
              <is-button variant="secondary" size="sm" (click)="closeReassignModal()">
                Cancel
              </is-button>
              <is-button
                variant="primary"
                size="sm"
                [disabled]="(!selectedUnderwriterId && !manualUnderwriterId) || assigningInProgress"
                (click)="confirmReassignment()"
              >
                {{ assigningInProgress ? 'Assigning…' : 'Confirm Assignment' }}
              </is-button>
            </div>
          </div>
        </is-modal>
      }
    </div>
  `,
  styles: [`
    :host { display: block; }
    .quotes-page { display: flex; flex-direction: column; gap: 24px; }
    .page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; }
    .page-eyebrow { margin: 0 0 6px; color: var(--claret); font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.12em; }
    .title-row { display: flex; align-items: center; gap: 12px; }
    .title-row h1 { margin: 0; font-size: clamp(24px, 2.5vw, 32px); color: var(--ink); font-weight: 700; letter-spacing: -0.03em; }
    .page-description { margin: 8px 0 0; color: var(--muted); font-size: 13px; max-width: 720px; line-height: 1.5; }
    .filter-bar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; background: var(--surface); padding: 12px 16px; border: 1px solid var(--border); border-radius: 8px; }
    .search-box { flex: 1; min-width: 220px; }
    .search-box input { width: 100%; height: 38px; padding: 0 12px; border: 1px solid var(--border); border-radius: 6px; background: var(--surface); color: var(--ink); font-size: 12px; box-sizing: border-box; }
    .search-box input:focus { outline: none; border-color: var(--claret); box-shadow: 0 0 0 3px rgba(117,1,63,0.1); }
    .status-filter select { height: 38px; padding: 0 12px; border: 1px solid var(--border); border-radius: 6px; background: var(--surface); color: var(--ink); font-size: 12px; }
    .status-filter select:focus { outline: none; border-color: var(--claret); }
    .table-container { width: 100%; overflow-x: auto; margin-top: 12px; }
    .data-table { width: 100%; border-collapse: collapse; font-size: 12px; }
    .data-table th { padding: 12px 16px; text-align: left; font-size: 10px; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase; color: var(--muted); border-bottom: 1px solid var(--border); background: var(--surface-hover); }
    .data-table td { padding: 14px 16px; border-bottom: 1px solid var(--border); color: var(--ink); vertical-align: middle; }
    .data-table tr:hover td { background: var(--surface-hover); }
    .code-pill { display: inline-block; padding: 3px 7px; border-radius: 4px; font-family: ui-monospace, monospace; font-size: 10px; background: var(--warm-light); color: var(--claret); font-weight: 600; }
    .text-muted { color: var(--muted); }
    .text-right { text-align: right; }
    .notice-card { display: flex; align-items: flex-start; gap: 16px; padding: 20px; border-radius: 9px; border: 1px solid var(--border); background: var(--surface); }
    .notice-card.warning { border-color: #f7dca3; background: #fffdf8; }
    .notice-card h3 { margin: 0 0 4px; font-size: 14px; }
    .notice-card p { margin: 0; font-size: 11px; color: var(--muted); }
    .notice-icon { width: 36px; height: 36px; display: grid; place-items: center; border-radius: 50%; font-size: 16px; background: var(--warm-light); flex-shrink: 0; }
    .underwriter-cell { display: flex; align-items: center; gap: 8px; }
    .uw-name { font-size: 11px; font-weight: 600; color: var(--ink); max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .unassigned-badge { display: inline-flex; padding: 2px 7px; border-radius: 999px; font-size: 9px; font-weight: 700; background: #fff5dc; color: #9a5b00; }
    .btn-reassign { border: 1px solid var(--border); border-radius: 4px; background: var(--surface); color: var(--claret); font-size: 10px; font-weight: 700; padding: 3px 8px; cursor: pointer; }
    .btn-reassign:hover { background: var(--claret-soft); border-color: var(--claret); }
    .reassign-modal-body { display: flex; flex-direction: column; gap: 16px; }
    .modal-desc { margin: 0; font-size: 13px; color: var(--muted); line-height: 1.5; }
    .form-group { display: flex; flex-direction: column; gap: 6px; }
    .form-group label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; color: var(--muted); }
    .uw-select, .uw-input { width: 100%; height: 38px; padding: 0 10px; border: 1px solid var(--border); border-radius: 6px; background: var(--surface); color: var(--ink); font-size: 12px; box-sizing: border-box; }
    .uw-select:focus, .uw-input:focus { outline: none; border-color: var(--claret); }
    .manual-input-box { display: flex; flex-direction: column; gap: 6px; }
    .warning-text { margin: 0; font-size: 11px; color: #9a5b00; }
    .loading-uw { margin: 0; font-size: 12px; color: var(--muted); }
    .error-msg { color: var(--danger); font-size: 11px; }
    .modal-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }
  `],
})
export class QuotesListComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly store = inject(Store);
  private readonly quoteService = inject(QuoteService);
  private readonly customerProfileService = inject(CustomerProfileService);
  private readonly api = inject(ApiService);

  quotes: QuoteResponse[] = [];
  loading = true;
  profileMissing = false;
  isEmployee = false;
  isAdmin = false;
  isUnderwriter = false;
  currentUserId: string | null = null;
  searchQuery = '';
  statusFilter = '';

  // Reassignment modal state
  showReassignModal = false;
  selectedQuoteForReassign: QuoteResponse | null = null;
  underwriters: UnderwriterUser[] = [];
  loadingUnderwriters = false;
  selectedUnderwriterId = '';
  manualUnderwriterId = '';
  assigningInProgress = false;
  reassignError: string | null = null;

  get filteredQuotes(): QuoteResponse[] {
    return this.quotes.filter((q) => {
      const matchesSearch = !this.searchQuery
        || (q.quoteNumber && q.quoteNumber.toLowerCase().includes(this.searchQuery.toLowerCase()))
        || (q.productCode && q.productCode.toLowerCase().includes(this.searchQuery.toLowerCase()));

      const matchesStatus = !this.statusFilter
        || q.status === this.statusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  ngOnInit(): void {
    this.store.select(selectUserId).pipe(take(1)).subscribe((uid) => {
      this.currentUserId = uid;
    });

    this.store.select(selectUserRole).pipe(take(1)).subscribe((role) => {
      const upperRole = (role ?? '').toUpperCase();
      this.isAdmin = ['ADMIN', 'SYSTEM_ADMINISTRATOR'].includes(upperRole);
      this.isUnderwriter = upperRole === 'UNDERWRITER';
      this.isEmployee = this.isAdmin || this.isUnderwriter || ['RISK_ENGINEER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'VENDOR_MANAGER'].includes(upperRole);

      if (this.isAdmin) {
        this.loadAvailableUnderwriters();
      }
      this.loadQuotes();
    });
  }

  loadQuotes(): void {
    this.loading = true;
    if (this.isEmployee) {
      this.quoteService.getAllQuotesForAdministration().pipe(
        catchError(() => of([]))
      ).subscribe({
        next: (items) => {
          this.quotes = items;
          this.loading = false;
        },
        error: () => {
          this.quotes = [];
          this.loading = false;
        },
      });
    } else {
      // Customer flow
      this.customerProfileService.getProfile().pipe(
        catchError(() => of(null)),
        switchMap((profile) => {
          if (!profile) {
            this.profileMissing = true;
            return of([]);
          }
          return this.quoteService.getQuotesByCustomerId(profile.customerId).pipe(catchError(() => of([])));
        })
      ).subscribe({
        next: (items) => {
          this.quotes = items;
          this.loading = false;
        },
        error: () => {
          this.quotes = [];
          this.loading = false;
        },
      });
    }
  }

  loadAvailableUnderwriters(): void {
    this.loadingUnderwriters = true;
    this.api.get<UnderwriterUser[]>('/api/users/available', { role: 'UNDERWRITER', status: 'ACTIVE' }).pipe(
      catchError(() => of([]))
    ).subscribe({
      next: (list) => {
        this.underwriters = list ?? [];
        this.loadingUnderwriters = false;
      },
      error: () => {
        this.underwriters = [];
        this.loadingUnderwriters = false;
      },
    });
  }

  canReassign(status: string): boolean {
    return ['SUBMITTED', 'IN_REVIEW', 'NEEDS_INFORMATION', 'RISK_ASSESSMENT'].includes(status);
  }

  getUnderwriterLabel(underwriterId: string): string {
    const found = this.underwriters.find((uw) => uw.userId === underwriterId);
    if (found) {
      return found.displayName ? found.displayName : (found.firstName ? `${found.firstName} ${found.lastName || ''}`.trim() : (found.email || underwriterId.substring(0, 8)));
    }
    return underwriterId.substring(0, 8) + '…';
  }

  openReassignModal(quote: QuoteResponse): void {
    this.selectedQuoteForReassign = quote;
    this.selectedUnderwriterId = quote.assignedUnderwriterId || '';
    this.manualUnderwriterId = '';
    this.reassignError = null;
    this.showReassignModal = true;
  }

  closeReassignModal(): void {
    this.showReassignModal = false;
    this.selectedQuoteForReassign = null;
    this.reassignError = null;
  }

  confirmReassignment(): void {
    if (!this.selectedQuoteForReassign) return;
    const targetId = this.selectedUnderwriterId || this.manualUnderwriterId;
    if (!targetId.trim()) return;

    this.assigningInProgress = true;
    this.reassignError = null;

    this.quoteService.reassignUnderwriter(this.selectedQuoteForReassign.quoteId, targetId.trim()).subscribe({
      next: (updatedQuote) => {
        this.assigningInProgress = false;
        this.store.dispatch(uiActions.showToast({
          kind: 'success',
          message: `Quote ${updatedQuote.quoteNumber} assigned successfully.`,
        }));
        this.closeReassignModal();
        this.loadQuotes();
      },
      error: (err) => {
        this.assigningInProgress = false;
        this.reassignError = err?.error?.message || err?.message || 'Underwriter reassignment failed.';
      },
    });
  }

  navigate(path: string): void {
    this.router.navigateByUrl(path);
  }

  getQuoteStatusVariant(status: string): BadgeVariant {
    switch (status) {
      case 'DRAFT': return 'neutral';
      case 'SUBMITTED':
      case 'IN_REVIEW':
      case 'RISK_ASSESSMENT': return 'warning';
      case 'QUOTED':
      case 'ACCEPTED': return 'info';
      case 'BOUND':
      case 'ISSUED': return 'success';
      case 'DECLINED_BY_CUSTOMER':
      case 'DECLINED_BY_INSURER': return 'danger';
      default: return 'neutral';
    }
  }

  formatStatus(status: string): string {
    return status.replace(/_/g, ' ');
  }
}
