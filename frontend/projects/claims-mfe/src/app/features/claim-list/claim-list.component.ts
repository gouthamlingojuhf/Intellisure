import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ClaimResponse } from '../../models/claim.models';
import { ClaimsService } from '../../services/claims.service';

// ui-core imports
import { CardComponent, ButtonComponent, BadgeComponent, TableComponent, TableColumn, TableAction, EmptyStateComponent, SkeletonComponent } from 'ui-core';

@Component({
  selector: 'claims-list',
  standalone: true,
  imports: [
    CurrencyPipe,
    CardComponent,
    ButtonComponent,
    BadgeComponent,
    TableComponent,
    EmptyStateComponent,
    SkeletonComponent
  ],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div>
          <p class="page-eyebrow">Claims Operations</p>
          <h1>Claim queue</h1>
          <p class="page-description">Manage and track all claims across the portfolio with real-time status updates from the gateway.</p>
        </div>
        <is-button variant="primary" (click)="goToFileClaim()">
          File new claim
          <span aria-hidden="true">&rarr;</span>
        </is-button>
      </header>

      <div class="command-bar" aria-label="Claim queue controls">
        <div class="period-control">
          <span>Status Filter</span>
          <select class="filter-select" (change)="onStatusChange($event)">
            <option value="">All statuses</option>
            <option value="FNOL_RECEIVED">FNOL Received</option>
            <option value="OPEN">Open</option>
            <option value="COVERAGE_REVIEW">Coverage Review</option>
            <option value="RESERVED">Reserved</option>
            <option value="APPROVED">Approved</option>
            <option value="SETTLED">Settled</option>
            <option value="CLOSED">Closed</option>
          </select>
        </div>
        <div class="command-actions">
          <is-button variant="secondary" size="sm" (click)="loadClaims()">Refresh</is-button>
        </div>
      </div>

      @if (error) {
        <div class="error-banner" role="alert">
          <div>
            <strong>Error loading claims:</strong>
            <p>{{ error }}</p>
          </div>
          <is-button variant="secondary" size="sm" (click)="loadClaims()">Try again</is-button>
        </div>
      }

      @if (loading) {
        <div class="loading-state" role="status" aria-live="polite">
          <is-skeleton variant="table-row" />
          <is-skeleton variant="table-row" />
          <is-skeleton variant="table-row" />
          <is-skeleton variant="table-row" />
          <is-skeleton variant="table-row" />
        </div>
      } @else if (claims.length === 0 && !error) {
        <is-empty-state
          title="No claims found"
          description="There are currently no claims matching your selection."
          actionLabel="File new claim"
          (action)="goToFileClaim()"
        />
      } @else {
        <is-table
          [columns]="columns"
          [data]="claims"
          [actions]="actions"
          [trackByFn]="trackByClaimId"
          [emptyMessage]="'No claims found'"
        />
      }
    </section>
  `,
  styles: [`
    :host {
      display: block;
      padding: 24px;
    }
    
    .enterprise-page {
      display: grid;
      gap: 20px;
      max-width: 1540px;
      margin: 0 auto;
    }
    
    .page-header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 24px;
      padding: 2px 0 4px;
    }
    
    .page-eyebrow {
      margin: 0 0 7px;
      color: var(--claret, #75013f);
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.14em;
      text-transform: uppercase;
    }
    
    .page-header h1 {
      margin: 0;
      color: var(--ink, #000000);
      font-size: clamp(25px, 2.5vw, 34px);
      line-height: 1.08;
      letter-spacing: -0.045em;
    }
    
    .page-description {
      max-width: 730px;
      margin: 9px 0 0;
      color: var(--muted, #6f6a6d);
      font-size: 12px;
      line-height: 1.6;
    }
    
    .command-bar {
      min-height: 48px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 7px 12px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 8px;
      background: var(--surface, #ffffff);
      box-shadow: var(--shadow);
    }
    
    .period-control {
      display: flex;
      align-items: center;
      gap: 10px;
      color: #817b7f;
      font-size: 10px;
    }

    .filter-select {
      height: 32px;
      padding: 0 8px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 5px;
      background: #ffffff;
      font-size: 11px;
      color: #272427;
      outline: none;
    }
    .filter-select:focus {
      border-color: var(--claret, #75013f);
    }
    
    .command-actions {
      display: flex;
      gap: 6px;
    }
    
    .loading-state {
      min-height: 300px;
      display: grid;
      gap: 12px;
    }

    .error-banner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 14px 18px;
      background: #fde8e8;
      border: 1px solid #f8b4b4;
      border-radius: 8px;
      color: #9b1c1c;
      font-size: 12px;
    }
    .error-banner p {
      margin: 2px 0 0;
    }
  `],
})
export class ClaimListComponent implements OnInit {
  private readonly claimsService = inject(ClaimsService);
  private readonly router = inject(Router);

  claims: ClaimResponse[] = [];
  loading = false;
  error: string | null = null;
  selectedStatus = '';

  columns: TableColumn<ClaimResponse>[] = [
    { key: 'claimNumber', header: 'Claim #', width: '150px' },
    { key: 'policyId', header: 'Policy ID', width: '170px' },
    { key: 'status', header: 'Status', width: '130px', render: this.renderStatus.bind(this) },
    { key: 'estimatedLoss', header: 'Estimated Loss', width: '130px', align: 'right', render: this.renderAmount.bind(this) },
    { key: 'incidentDate', header: 'Incident Date', width: '120px' },
    { key: 'reportedDate', header: 'Reported', width: '120px' },
  ];

  actions: TableAction<ClaimResponse>[] = [
    {
      label: 'Open',
      variant: 'text',
      handler: (row) => this.router.navigate(['/claims', row.claimId]),
    },
  ];

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.loading = true;
    this.error = null;
    this.claimsService.getClaims(this.selectedStatus).subscribe({
      next: (data) => {
        this.claims = data || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = this.describeError(err);
        this.loading = false;
      },
    });
  }

  onStatusChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedStatus = select?.value || '';
    this.loadClaims();
  }

  goToFileClaim(): void {
    this.router.navigate(['/claims', 'new']);
  }

  trackByClaimId(index: number, item: ClaimResponse): string {
    return item.claimId;
  }

  renderStatus(row: ClaimResponse, value: string): string {
    const val = (value || '').toUpperCase();
    const statusMap: Record<string, 'success' | 'warning' | 'danger' | 'info' | 'neutral'> = {
      'OPEN': 'info',
      'FNOL_RECEIVED': 'info',
      'COVERAGE_REVIEW': 'warning',
      'RESERVED': 'warning',
      'APPROVED': 'success',
      'SETTLED': 'success',
      'PAID': 'success',
      'DENIED': 'danger',
      'CLOSED': 'neutral',
    };
    const variant = statusMap[val] || 'neutral';
    const label = val.replace(/_/g, ' ');
    return `<is-badge variant="${variant}" size="sm">${label}</is-badge>`;
  }

  renderAmount(row: ClaimResponse, value: number): string {
    const amount = typeof value === 'number' ? value : 0;
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(amount);
  }

  private describeError(err: { status?: number; error?: { message?: string }; message?: string }): string {
    if (err?.status === 401) return 'Your session has expired. Please sign in again.';
    if (err?.status === 403) return 'You do not have permission to view these claims.';
    if (err?.status === 404) return 'The claims endpoint could not be found.';
    return err?.error?.message || err?.message || 'Failed to load claims from backend.';
  }
}
