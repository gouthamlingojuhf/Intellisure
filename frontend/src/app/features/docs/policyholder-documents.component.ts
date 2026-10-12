import { DatePipe, CurrencyPipe, CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap, take } from 'rxjs/operators';
import { BadgeComponent, ButtonComponent, CardComponent, EmptyStateComponent, SkeletonComponent } from 'ui-core';
import { DocumentResponse } from '../../core/models/document.models';
import { DocumentService } from '../../core/services/document.service';
import { CustomerProfileService } from '../../core/services/customer-profile.service';
import { PolicyService } from '../../core/services/policy.service';
import { QuoteService } from '../../core/services/quote.service';
import { ApiService } from '../../core/services/api.service';
import { selectUserRole } from '../../core/store/auth/auth.selectors';
import { QuoteResponse } from '../../core/models/quote.models';
import { PolicyResponse } from '../../core/models/policy.models';

export type DocumentCategory = 'ALL' | 'POLICIES' | 'QUOTES' | 'CLAIMS' | 'RISK' | 'ATTACHMENTS';

export interface DocumentItem {
  id: string;
  title: string;
  fileName: string;
  category: 'POLICIES' | 'QUOTES' | 'CLAIMS' | 'RISK' | 'ATTACHMENTS';
  documentType: string;
  referenceNumber: string;
  targetRoute?: string;
  createdAt?: string | null;
  fileSizeLabel?: string;
  statusBadge: string;
  statusVariant: 'success' | 'warning' | 'danger' | 'info' | 'neutral';
  description?: string;
  itemData?: any;
  details?: Record<string, any>;
}

@Component({
  selector: 'is-policyholder-documents',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    CurrencyPipe,
    FormsModule,
    RouterLink,
    BadgeComponent,
    ButtonComponent,
    CardComponent,
    EmptyStateComponent,
    SkeletonComponent,
  ],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div>
          <p class="page-eyebrow">Document & Audit Repository</p>
          <h1>Enterprise Insurance Documents</h1>
          <p class="page-description">
            {{ isEmployee 
              ? 'Complete multi-tenant document repository across policyholder quotes, contracts, loss reports, and risk engineering evaluations.' 
              : 'Official commercial insurance records, binding declarations, loss notices, and risk recommendations linked to your account.' }}
          </p>
        </div>
        <div class="header-actions">
          <a routerLink="/docs/guide" class="guide-link">Role Guides &rarr;</a>
          <is-button variant="secondary" size="sm" (click)="loadDocuments()" [disabled]="loading">Refresh</is-button>
        </div>
      </header>

      <!-- Metric Stat Cards -->
      <div class="stat-grid">
        <div class="stat-card">
          <span class="stat-label">Total Documents</span>
          <span class="stat-value">{{ allDocuments.length }}</span>
          <span class="stat-sub">Across all business lines</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Policies & Schedules</span>
          <span class="stat-value">{{ policyDocCount }}</span>
          <span class="stat-sub">Active declarations & endorsements</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Quotes & Proposals</span>
          <span class="stat-value">{{ quoteDocCount }}</span>
          <span class="stat-sub">Offered terms & submissions</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Claim Filings & FNOL</span>
          <span class="stat-value">{{ claimDocCount }}</span>
          <span class="stat-sub">First notice of loss records</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Risk Engineering</span>
          <span class="stat-value">{{ riskDocCount }}</span>
          <span class="stat-sub">Recommendations & audits</span>
        </div>
      </div>

      <!-- Filters & Search Bar -->
      <div class="filter-bar">
        <div class="category-tabs" role="tablist">
          <button 
            type="button" 
            class="tab-btn" 
            [class.active]="selectedCategory === 'ALL'" 
            (click)="setCategory('ALL')">
            All ({{ allDocuments.length }})
          </button>
          <button 
            type="button" 
            class="tab-btn" 
            [class.active]="selectedCategory === 'POLICIES'" 
            (click)="setCategory('POLICIES')">
            Policies ({{ policyDocCount }})
          </button>
          <button 
            type="button" 
            class="tab-btn" 
            [class.active]="selectedCategory === 'QUOTES'" 
            (click)="setCategory('QUOTES')">
            Quotes ({{ quoteDocCount }})
          </button>
          <button 
            type="button" 
            class="tab-btn" 
            [class.active]="selectedCategory === 'CLAIMS'" 
            (click)="setCategory('CLAIMS')">
            Claims ({{ claimDocCount }})
          </button>
          <button 
            type="button" 
            class="tab-btn" 
            [class.active]="selectedCategory === 'RISK'" 
            (click)="setCategory('RISK')">
            Risk Reports ({{ riskDocCount }})
          </button>
          @if (attachmentDocCount > 0) {
            <button 
              type="button" 
              class="tab-btn" 
              [class.active]="selectedCategory === 'ATTACHMENTS'" 
              (click)="setCategory('ATTACHMENTS')">
              Attachments ({{ attachmentDocCount }})
            </button>
          }
        </div>

        <div class="search-box">
          <input 
            type="text" 
            [(ngModel)]="searchQuery" 
            placeholder="Search by title, number, or product..." 
            class="search-input" />
          @if (searchQuery) {
            <button class="clear-btn" (click)="searchQuery = ''">&times;</button>
          }
        </div>
      </div>

      @if (error) {
        <div class="error-banner" role="alert">
          <div><strong>Documents unavailable</strong><p>{{ error }}</p></div>
          <is-button variant="secondary" size="sm" (click)="loadDocuments()">Try again</is-button>
        </div>
      }

      @if (loading) {
        <div class="loading-grid" role="status" aria-live="polite">
          <is-skeleton variant="table-row" />
          <is-skeleton variant="table-row" />
          <is-skeleton variant="table-row" />
        </div>
      } @else if (!error && filteredDocuments.length === 0) {
        <is-empty-state 
          title="No documents found" 
          [description]="searchQuery ? 'No documents matched your search filter.' : 'No insurance documents are available under this section.'" 
          icon="▤" />
      } @else {
        <is-card 
          [title]="isEmployee ? 'Operational Document Register' : 'Your Insurance Document Register'" 
          [subtitle]="filteredDocuments.length + ' record' + (filteredDocuments.length === 1 ? '' : 's') + ' available for audit and download'">
          
          <div class="document-list">
            @for (document of filteredDocuments; track document.id) {
              <article class="document-row">
                <div class="document-icon" [ngClass]="document.category.toLowerCase()">
                  @switch (document.category) {
                    @case ('POLICIES') { 🛡️ }
                    @case ('QUOTES') { 📝 }
                    @case ('CLAIMS') { 📄 }
                    @case ('RISK') { 🔍 }
                    @default { ▤ }
                  }
                </div>

                <div class="document-main">
                  <div class="title-row">
                    <strong>{{ document.title }}</strong>
                    <is-badge [variant]="document.statusVariant" size="sm">
                      {{ document.statusBadge }}
                    </is-badge>
                  </div>
                  <div class="doc-meta">
                    <span class="file-name">{{ document.fileName }}</span>
                    <span class="meta-dot">·</span>
                    <span class="doc-type">{{ formatDocType(document.documentType) }}</span>
                    @if (document.description) {
                      <span class="meta-dot">·</span>
                      <span class="doc-desc">{{ document.description }}</span>
                    }
                  </div>
                </div>

                <div class="document-context">
                  <span class="ref-badge">{{ document.referenceNumber }}</span>
                  <span class="date-text">
                    {{ document.createdAt ? (document.createdAt | date:'mediumDate') : 'Active' }}
                  </span>
                </div>

                <div class="document-actions">
                  @if (document.targetRoute) {
                    <a [routerLink]="document.targetRoute" class="view-link">View Record &rarr;</a>
                  }
                  <button type="button" class="print-btn" (click)="printDocument(document)" title="Print / Download Summary">
                    Print PDF
                  </button>
                </div>
              </article>
            }
          </div>
        </is-card>
      }
    </section>
  `,
  styles: [`
    :host { display: block; padding: 24px; }
    .enterprise-page { display: grid; gap: 20px; max-width: 1200px; margin: 0 auto; }
    .page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; }
    .page-eyebrow { margin: 0 0 7px; color: var(--claret, #75013f); font-size: 10px; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }
    .page-header h1 { margin: 0; color: var(--ink, #000); font-size: clamp(24px, 2.5vw, 32px); letter-spacing: -.03em; }
    .page-description { max-width: 760px; margin: 8px 0 0; color: var(--muted, #6f6a6d); font-size: 13px; line-height: 1.6; }
    .header-actions { display: flex; align-items: center; gap: 12px; }
    .guide-link { color: var(--claret, #75013f); font-size: 11px; font-weight: 700; text-decoration: none; }
    .guide-link:hover { text-decoration: underline; }

    /* Stat Grid */
    .stat-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; }
    .stat-card { background: var(--surface, #fff); border: 1px solid var(--border, #eae5df); border-radius: 8px; padding: 14px 16px; display: flex; flex-direction: column; gap: 4px; }
    .stat-label { font-size: 10px; font-weight: 700; color: var(--muted, #6f6a6d); text-transform: uppercase; letter-spacing: 0.05em; }
    .stat-value { font-size: 22px; font-weight: 800; color: var(--ink, #000); }
    .stat-sub { font-size: 10px; color: var(--muted, #6f6a6d); }

    /* Filter & Search */
    .filter-bar { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
    .category-tabs { display: flex; gap: 6px; flex-wrap: wrap; }
    .tab-btn { background: var(--warm-light, #f7f5f3); border: 1px solid var(--border, #eae5df); border-radius: 6px; padding: 6px 12px; font-size: 11px; font-weight: 600; color: var(--muted, #6f6a6d); cursor: pointer; transition: all 0.2s; }
    .tab-btn:hover { background: #ede8e3; color: var(--ink, #000); }
    .tab-btn.active { background: var(--claret, #75013f); color: #fff; border-color: var(--claret, #75013f); }

    .search-box { position: relative; min-width: 280px; }
    .search-input { width: 100%; height: 34px; padding: 0 28px 0 10px; border: 1px solid var(--border, #eae5df); border-radius: 6px; font-size: 12px; background: var(--surface, #fff); box-sizing: border-box; }
    .search-input:focus { outline: none; border-color: var(--claret, #75013f); }
    .clear-btn { position: absolute; right: 8px; top: 50%; transform: translateY(-50%); background: none; border: none; font-size: 16px; color: var(--muted, #6f6a6d); cursor: pointer; }

    /* Document Row List */
    .document-list { display: grid; gap: 0; }
    .document-row { display: grid; grid-template-columns: 42px minmax(0, 1fr) auto auto; align-items: center; gap: 16px; padding: 14px 0; border-bottom: 1px solid var(--border, #eae5df); }
    .document-row:last-child { border-bottom: 0; }
    .document-icon { display: grid; place-items: center; width: 38px; height: 38px; border-radius: 8px; background: var(--warm-light, #f7f5f3); font-size: 18px; }
    .document-icon.policies { background: #e8f5e9; }
    .document-icon.quotes { background: #e3f2fd; }
    .document-icon.claims { background: #fbe9e7; }
    .document-icon.risk { background: #fff3e0; }

    .document-main { display: grid; gap: 4px; min-width: 0; }
    .title-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .title-row strong { font-size: 13px; color: var(--ink, #000); }
    .doc-meta { display: flex; align-items: center; gap: 6px; font-size: 11px; color: var(--muted, #6f6a6d); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .file-name { font-weight: 600; color: var(--ink, #333); font-family: ui-monospace, monospace; font-size: 10.5px; }
    .meta-dot { color: #ccc; }
    .doc-type { text-transform: uppercase; font-size: 9.5px; letter-spacing: 0.04em; }
    .doc-desc { color: var(--muted, #6f6a6d); }

    .document-context { display: grid; justify-items: end; gap: 4px; text-align: right; }
    .ref-badge { font-family: ui-monospace, monospace; font-size: 10.5px; font-weight: 700; color: var(--claret, #75013f); background: var(--warm-light, #f7f5f3); padding: 2px 6px; border-radius: 4px; }
    .date-text { font-size: 10px; color: var(--muted, #6f6a6d); }

    .document-actions { display: flex; align-items: center; gap: 8px; }
    .view-link { font-size: 11px; font-weight: 700; color: var(--claret, #75013f); text-decoration: none; padding: 4px 8px; border-radius: 4px; }
    .view-link:hover { text-decoration: underline; background: rgba(117, 1, 63, 0.05); }
    .print-btn { font-size: 10.5px; font-weight: 600; background: var(--surface, #fff); border: 1px solid var(--border, #eae5df); border-radius: 4px; padding: 4px 8px; cursor: pointer; color: var(--ink, #333); }
    .print-btn:hover { background: var(--warm-light, #f7f5f3); border-color: var(--claret, #75013f); }

    .error-banner { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 14px 18px; border: 1px solid #f8b4b4; border-radius: 8px; background: #fde8e8; color: #9b1c1c; font-size: 12px; }
    .error-banner p { margin: 3px 0 0; }
    .loading-grid { display: grid; gap: 12px; }

    @media (max-width: 768px) {
      :host { padding: 16px; }
      .page-header { align-items: flex-start; flex-direction: column; }
      .document-row { grid-template-columns: 36px minmax(0, 1fr); gap: 10px; }
      .document-context, .document-actions { grid-column: 2; justify-items: start; }
      .document-context { text-align: left; }
    }
  `],
})
export class PolicyholderDocumentsComponent implements OnInit {
  private readonly store = inject(Store);
  private readonly profileService = inject(CustomerProfileService);
  private readonly policyService = inject(PolicyService);
  private readonly quoteService = inject(QuoteService);
  private readonly documentService = inject(DocumentService);
  private readonly api = inject(ApiService);

  allDocuments: DocumentItem[] = [];
  loading = false;
  error: string | null = null;
  isEmployee = false;
  selectedCategory: DocumentCategory = 'ALL';
  searchQuery = '';

  get policyDocCount(): number {
    return this.allDocuments.filter((d) => d.category === 'POLICIES').length;
  }

  get quoteDocCount(): number {
    return this.allDocuments.filter((d) => d.category === 'QUOTES').length;
  }

  get claimDocCount(): number {
    return this.allDocuments.filter((d) => d.category === 'CLAIMS').length;
  }

  get riskDocCount(): number {
    return this.allDocuments.filter((d) => d.category === 'RISK').length;
  }

  get attachmentDocCount(): number {
    return this.allDocuments.filter((d) => d.category === 'ATTACHMENTS').length;
  }

  get filteredDocuments(): DocumentItem[] {
    return this.allDocuments.filter((doc) => {
      const matchesCategory = this.selectedCategory === 'ALL' || doc.category === this.selectedCategory;
      const q = this.searchQuery.trim().toLowerCase();
      const matchesSearch = !q
        || doc.title.toLowerCase().includes(q)
        || doc.fileName.toLowerCase().includes(q)
        || doc.referenceNumber.toLowerCase().includes(q)
        || (doc.description && doc.description.toLowerCase().includes(q))
        || doc.documentType.toLowerCase().includes(q);

      return matchesCategory && matchesSearch;
    });
  }

  ngOnInit(): void {
    this.loadDocuments();
  }

  setCategory(cat: DocumentCategory): void {
    this.selectedCategory = cat;
  }

  formatDocType(type: string): string {
    return (type || 'DOCUMENT').replace(/_/g, ' ');
  }

  loadDocuments(): void {
    this.loading = true;
    this.error = null;

    this.store.select(selectUserRole).pipe(take(1)).subscribe((role) => {
      const upper = (role ?? '').toUpperCase();
      this.isEmployee = ['UNDERWRITER', 'RISK_ENGINEER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'VENDOR_MANAGER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'].includes(upper);

      if (this.isEmployee) {
        this.loadEmployeeDocuments();
      } else {
        this.loadPolicyholderDocuments();
      }
    });
  }

  private loadPolicyholderDocuments(): void {
    this.profileService.getProfile().pipe(
      catchError(() => of(null)),
      switchMap((profile) => {
        const customerId = profile?.customerId;
        const quotes$ = customerId 
          ? this.quoteService.getQuotesByCustomerId(customerId).pipe(catchError(() => of([])))
          : of([]);
        const policies$ = customerId 
          ? this.policyService.getPoliciesByCustomerId(customerId).pipe(catchError(() => of([])))
          : of([]);
        const claims$ = this.api.get<any[]>('/api/claims').pipe(catchError(() => of([])));
        const boundDocs$ = this.documentService.getBoundDocuments().pipe(catchError(() => of([])));

        return forkJoin({ quotes: quotes$, policies: policies$, claims: claims$, boundDocs: boundDocs$ });
      })
    ).subscribe({
      next: ({ quotes, policies, claims, boundDocs }) => {
        this.processCombinedDocuments(quotes, policies, claims, boundDocs);
      },
      error: (err) => this.handleError(err),
    });
  }

  private loadEmployeeDocuments(): void {
    const quotes$ = this.quoteService.getAllQuotesForAdministration().pipe(catchError(() => of([])));
    const policies$ = this.policyService.getAllPolicies().pipe(catchError(() => of([])));
    const claims$ = this.api.get<any[]>('/api/claims').pipe(catchError(() => of([])));
    const boundDocs$ = this.documentService.getBoundDocuments().pipe(catchError(() => of([])));

    forkJoin({ quotes: quotes$, policies: policies$, claims: claims$, boundDocs: boundDocs$ }).subscribe({
      next: ({ quotes, policies, claims, boundDocs }) => {
        this.processCombinedDocuments(quotes, policies, claims, boundDocs);
      },
      error: (err) => this.handleError(err),
    });
  }

  private processCombinedDocuments(
    quotes: QuoteResponse[],
    policies: PolicyResponse[],
    claims: any[],
    boundDocs: DocumentResponse[]
  ): void {
    const items: DocumentItem[] = [];

    // 1. Process Policies
    (policies || []).forEach((p) => {
      const status = p.status || 'ACTIVE';
      const variant = status === 'ACTIVE' ? 'success'
        : status === 'BOUND' ? 'info'
        : status === 'CANCELLED' ? 'danger' : 'warning';

      items.push({
        id: `pol-${p.policyId}`,
        title: `Policy Contract Declaration — ${p.policyNumber}`,
        fileName: `Policy-Declaration-${p.policyNumber}.pdf`,
        category: 'POLICIES',
        documentType: 'POLICY_SCHEDULE',
        referenceNumber: p.policyNumber,
        targetRoute: `/policy/${p.policyId}`,
        createdAt: p.issuedAt || p.boundAt || p.createdAt,
        statusBadge: status,
        statusVariant: variant,
        description: `${p.productCode || 'Commercial Insurance'} · Coverage Period ${p.startDate || 'N/A'} to ${p.endDate || 'N/A'}${p.totalPremium ? ' · Premium: ₹' + p.totalPremium : ''}`,
        itemData: p,
      });
    });

    // 2. Process Quotes & synthesize associated risk documents
    (quotes || []).forEach((q) => {
      const status = q.status || 'DRAFT';
      const variant = ['OFFERED', 'BOUND', 'ACCEPTED'].includes(status) ? 'success'
        : status === 'SUBMITTED' ? 'info'
        : status === 'DECLINED' ? 'danger' : 'neutral';

      items.push({
        id: `quote-${q.quoteId}`,
        title: `Commercial Underwriting Proposal — ${q.quoteNumber}`,
        fileName: `Quote-Proposal-${q.quoteNumber}.pdf`,
        category: 'QUOTES',
        documentType: 'QUOTE_PROPOSAL',
        referenceNumber: q.quoteNumber,
        targetRoute: `/quotes/${q.quoteId}`,
        createdAt: q.createdAt || q.requestedEffectiveDate,
        statusBadge: status,
        statusVariant: variant,
        description: `${q.productCode || 'Commercial Package'} · Effective ${q.requestedEffectiveDate || 'N/A'}${q.totalPremium ? ' · Premium: ₹' + q.totalPremium : ''}`,
        itemData: q,
      });

      // Synthesize Risk Assessment & Recommendation record if quote was submitted/offered/bound
      if (['SUBMITTED', 'UNDERWRITING_REVIEW', 'OFFERED', 'BOUND', 'ACCEPTED'].includes(q.status)) {
        items.push({
          id: `risk-quote-${q.quoteId}`,
          title: `Risk Engineering Assessment & Recommendations — ${q.quoteNumber}`,
          fileName: `Risk-Evaluation-${q.quoteNumber}.pdf`,
          category: 'RISK',
          documentType: 'RISK_ENGINEERING_REPORT',
          referenceNumber: q.quoteNumber,
          targetRoute: `/quotes/${q.quoteId}`,
          createdAt: q.createdAt,
          statusBadge: 'EVALUATED',
          statusVariant: 'info',
          description: `Loss prevention, property protection & engineering recommendations for ${q.quoteNumber}`,
          itemData: q,
        });
      }
    });

    // 3. Process Claims
    (claims || []).forEach((c) => {
      const status = c.status || 'OPEN';
      const variant = ['SETTLED', 'PAID', 'CLOSED'].includes(status) ? 'success'
        : status === 'APPROVED' ? 'info'
        : status === 'REJECTED' ? 'danger' : 'warning';

      items.push({
        id: `claim-${c.claimId}`,
        title: `First Notice of Loss Report — ${c.claimNumber}`,
        fileName: `FNOL-Report-${c.claimNumber}.pdf`,
        category: 'CLAIMS',
        documentType: 'CLAIM_FNOL_REPORT',
        referenceNumber: c.claimNumber,
        targetRoute: `/claims/${c.claimId}`,
        createdAt: c.incidentDate || c.createdAt,
        statusBadge: status,
        statusVariant: variant,
        description: `${c.lossType || 'Property Loss'} · ${c.lossDescription || 'Claim filing'}${c.totalIncurred ? ' · Incurred: ₹' + c.totalIncurred : ''}`,
        itemData: c,
      });
    });

    // 4. Process Uploaded / Bound Attachment Documents
    (boundDocs || []).forEach((doc) => {
      const cat = doc.entityType === 'POLICY' ? 'POLICIES'
        : doc.entityType === 'QUOTE' ? 'QUOTES'
        : doc.entityType === 'CLAIM' ? 'CLAIMS' : 'ATTACHMENTS';

      const route = doc.entityType === 'POLICY' ? `/policy/${doc.entityId}`
        : doc.entityType === 'QUOTE' ? `/quotes/${doc.entityId}`
        : doc.entityType === 'CLAIM' ? `/claims/${doc.entityId}` : undefined;

      items.push({
        id: `doc-${doc.documentId}`,
        title: doc.fileName,
        fileName: doc.fileName,
        category: cat as any,
        documentType: doc.documentType || 'ATTACHED_FILE',
        referenceNumber: doc.entityId,
        targetRoute: route,
        createdAt: doc.createdAt,
        statusBadge: 'ATTACHED',
        statusVariant: 'neutral',
        description: `Attached ${doc.entityType.toLowerCase()} file (${doc.contentType || 'document'})`,
        itemData: doc,
      });
    });

    // Sort descending by date
    this.allDocuments = items.sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''));
    this.loading = false;
  }

  printDocument(doc: DocumentItem): void {
    const printWindow = window.open('', '_blank', 'width=800,height=900');
    if (!printWindow) {
      alert('Pop-up window blocked. Please allow pop-ups to print official document statements.');
      return;
    }

    const html = `
      <!DOCTYPE html>
      <html>
      <head>
        <title>${doc.title}</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; padding: 40px; color: #1a1a1a; line-height: 1.5; }
          .header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 2px solid #75013f; padding-bottom: 20px; margin-bottom: 30px; }
          .logo { font-size: 24px; font-weight: 800; color: #75013f; letter-spacing: -0.03em; }
          .logo-sub { font-size: 11px; text-transform: uppercase; color: #666; letter-spacing: 0.1em; }
          .doc-title { font-size: 20px; font-weight: 700; margin: 0 0 10px; }
          .badge { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; background: #e3f2fd; color: #0d47a1; }
          .details-table { width: 100%; border-collapse: collapse; margin-top: 25px; }
          .details-table th, .details-table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
          .details-table th { background: #f9f9f9; color: #555; font-size: 11px; text-transform: uppercase; }
          .footer { margin-top: 50px; border-top: 1px solid #ddd; padding-top: 15px; font-size: 11px; color: #888; text-align: center; }
          @media print {
            .no-print { display: none; }
          }
        </style>
      </head>
      <body>
        <div class="header">
          <div>
            <div class="logo">IntelliSure Commercial Insurance</div>
            <div class="logo-sub">Enterprise Insurance Lifecycle Repository</div>
          </div>
          <div style="text-align: right;">
            <div style="font-size: 11px; color: #777;">RECORD AUDIT ID</div>
            <div style="font-family: monospace; font-weight: 700;">${doc.id}</div>
          </div>
        </div>

        <h1 class="doc-title">${doc.title}</h1>
        <div>
          <span class="badge">${doc.statusBadge}</span>
          <span style="font-size: 12px; color: #666; margin-left: 10px;">Classification: ${doc.documentType}</span>
        </div>

        <table class="details-table">
          <tr><th>Attribute</th><th>Specification Details</th></tr>
          <tr><td>Reference Number</td><td><strong>${doc.referenceNumber}</strong></td></tr>
          <tr><td>Category</td><td>${doc.category}</td></tr>
          <tr><td>Document File</td><td>${doc.fileName}</td></tr>
          <tr><td>Effective / Record Date</td><td>${doc.createdAt || new Date().toISOString()}</td></tr>
          <tr><td>Summary Scope</td><td>${doc.description || 'Verified enterprise insurance document'}</td></tr>
        </table>

        <div style="margin-top: 35px; padding: 15px; background: #fdfaf6; border-left: 4px solid #75013f; font-size: 12px; color: #555;">
          <strong>Legal & Audit Notice:</strong> This document represents an authorized enterprise commercial insurance record within the IntelliSure system. All endorsements, coverages, and schedules are governed by the underlying policy agreements.
        </div>

        <div class="footer">
          IntelliSure Risk & Insurance Services &bull; Generated from Official Document Register &bull; ${new Date().toLocaleString()}
        </div>

        <div class="no-print" style="margin-top: 25px; text-align: center;">
          <button onclick="window.print()" style="padding: 10px 20px; font-weight: 700; background: #75013f; color: #fff; border: none; border-radius: 6px; cursor: pointer;">
            Print / Save as PDF
          </button>
        </div>
      </body>
      </html>
    `;

    printWindow.document.write(html);
    printWindow.document.close();
  }

  private handleError(err: { status?: number; error?: { message?: string }; message?: string }): void {
    this.loading = false;
    if (err?.status === 404) {
      this.allDocuments = [];
      this.error = null;
      return;
    }
    this.error = err?.status === 401
      ? 'Your session has expired. Please sign in again.'
      : err?.status === 403
        ? 'You do not have permission to view these documents.'
        : err?.error?.message || err?.message || 'The document service could not be reached.';
  }
}
