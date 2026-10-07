import { CurrencyPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ClaimRecord } from '../../models/claim.models';
import { ClaimsService } from '../../services/claims.service';

// ui-core imports
import { CardComponent, ButtonComponent, BadgeComponent, TableComponent, TableColumn, TableAction, EmptyStateComponent, SkeletonComponent } from 'ui-core';

@Component({
  selector: 'claims-list',
  standalone: true,
  imports: [
    RouterLink, 
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
          <p class="page-description">Manage and track all claims across the portfolio with real-time status updates.</p>
        </div>
        <is-button variant="primary" routerLink="new">
          File new claim
          <span aria-hidden="true">→</span>
        </is-button>
      </header>

      <div class="command-bar" aria-label="Claim queue controls">
        <div class="period-control">
          <span>Filter</span>
          <strong>All statuses</strong>
        </div>
        <div class="command-actions">
          <is-button variant="secondary" size="sm">Export</is-button>
          <is-button variant="secondary" size="sm">Filters</is-button>
        </div>
      </div>

      @if (loading) {
        <div class="loading-state" role="status" aria-live="polite">
          <is-skeleton variant="table-row" />
          <is-skeleton variant="table-row" />
          <is-skeleton variant="table-row" />
          <is-skeleton variant="table-row" />
          <is-skeleton variant="table-row" />
        </div>
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
      color: var(--claret);
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.14em;
      text-transform: uppercase;
    }
    
    .page-header h1 {
      margin: 0;
      color: var(--ink);
      font-size: clamp(25px, 2.5vw, 34px);
      line-height: 1.08;
      letter-spacing: -0.045em;
    }
    
    .page-description {
      max-width: 730px;
      margin: 9px 0 0;
      color: var(--muted);
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
      border: 1px solid var(--border);
      border-radius: 8px;
      background: var(--surface);
      box-shadow: var(--shadow);
    }
    
    .period-control {
      display: flex;
      align-items: center;
      gap: 10px;
      color: #817b7f;
      font-size: 10px;
    }
    
    .period-control strong {
      color: #333033;
      font-size: 10px;
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
  `],
})
export class ClaimListComponent implements OnInit {
  claims: ClaimRecord[] = [];
  loading = false;

  columns: TableColumn<ClaimRecord>[] = [
    { key: 'claimId', header: 'Claim', width: '140px' },
    { key: 'policyNumber', header: 'Policy', width: '140px' },
    { key: 'claimantName', header: 'Claimant', width: '180px' },
    { key: 'claimType', header: 'Type', width: '140px' },
    { key: 'status', header: 'Status', width: '120px', render: this.renderStatus.bind(this) },
    { key: 'claimedAmount', header: 'Amount', width: '130px', align: 'right', render: this.renderAmount.bind(this) },
    { key: 'reportedDate', header: 'Reported', width: '120px' },
  ];

  actions: TableAction<ClaimRecord>[] = [
    { label: 'Open', variant: 'text', handler: (row) => {} },
  ];

  constructor(private readonly claimsService: ClaimsService) {}

  ngOnInit(): void {
    this.loading = true;
    setTimeout(() => {
      this.claims = this.claimsService.getClaims();
      this.loading = false;
    }, 300);
  }

  trackByClaimId(index: number, item: ClaimRecord): string {
    return item.claimId;
  }

  renderStatus(row: ClaimRecord, value: string): string {
    const statusMap: Record<string, 'success' | 'warning' | 'danger' | 'info' | 'neutral'> = {
      'Open': 'info',
      'In Review': 'warning',
      'Investigating': 'warning',
      'Approved': 'success',
      'Denied': 'danger',
      'Closed': 'neutral',
      'Settled': 'success',
    };
    const variant = statusMap[value] || 'neutral';
    return `<is-badge variant="${variant}" size="sm">${value}</is-badge>`;
  }

  renderAmount(row: ClaimRecord, value: number): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(value);
  }
}