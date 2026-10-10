import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { forkJoin, map, of, switchMap, take, catchError } from 'rxjs';
import { BadgeComponent, ButtonComponent, CardComponent, EmptyStateComponent, SkeletonComponent } from 'ui-core';
import { DocumentResponse } from '../../core/models/document.models';
import { DocumentService } from '../../core/services/document.service';
import { CustomerProfileService } from '../../core/services/customer-profile.service';
import { PolicyService } from '../../core/services/policy.service';
import { QuoteService } from '../../core/services/quote.service';
import { ApiService } from '../../core/services/api.service';
import { selectUserRole } from '../../core/store/auth/auth.selectors';

interface EntityReference { entityId: string; entityType: 'QUOTE' | 'POLICY' | 'CLAIM'; }
interface ClaimReference { claimId: string; }

@Component({
  selector: 'is-policyholder-documents',
  standalone: true,
  imports: [DatePipe, RouterLink, BadgeComponent, ButtonComponent, CardComponent, EmptyStateComponent, SkeletonComponent],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div>
          <p class="page-eyebrow">Document & audit</p>
          <h1>Documents</h1>
          <p class="page-description">{{ isEmployee ? 'Review documents attached to operational claim records.' : 'View documents attached to your quotes, policies, and claims.' }}</p>
        </div>
        <div class="header-actions"><a routerLink="guide" class="guide-link">Open role guides</a><is-button variant="secondary" size="sm" (click)="loadDocuments()" [disabled]="loading">Refresh</is-button></div>
      </header>

      @if (error) {
        <div class="error-banner" role="alert">
          <div><strong>Documents unavailable</strong><p>{{ error }}</p></div>
          <is-button variant="secondary" size="sm" (click)="loadDocuments()">Try again</is-button>
        </div>
      }

      @if (loading) {
        <div class="loading-grid" role="status" aria-live="polite"><is-skeleton variant="table-row" /><is-skeleton variant="table-row" /><is-skeleton variant="table-row" /></div>
      } @else if (!error && documents.length === 0) {
        <is-empty-state title="No documents available" description="Documents attached to your insurance records will appear here when they are available." icon="▤" />
      } @else {
        <is-card [title]="isEmployee ? 'Claim document register' : 'Your document register'" [subtitle]="documents.length + ' document' + (documents.length === 1 ? '' : 's')">
          <div class="document-list">
            @for (document of documents; track document.documentId) {
              <article class="document-row">
                <div class="document-icon" aria-hidden="true">▤</div>
                <div class="document-main">
                  <strong>{{ document.fileName }}</strong>
                  <span>{{ (document.documentType || 'DOCUMENT').replace('_', ' ') }} · {{ document.contentType }}</span>
                </div>
                <div class="document-context">
                  <is-badge variant="info" size="sm">{{ document.entityType }}</is-badge>
                  <span>Added {{ document.createdAt | date:'mediumDate' }}</span>
                </div>
                <div class="document-version">v{{ document.version || 1 }}</div>
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
    .page-eyebrow { margin: 0 0 7px; color: var(--claret, #75013f); font-size: 9px; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }
    .page-header h1 { margin: 0; color: var(--ink, #000); font-size: clamp(25px, 2.5vw, 34px); letter-spacing: -.045em; }
    .page-description { max-width: 720px; margin: 9px 0 0; color: var(--muted, #6f6a6d); font-size: 12px; line-height: 1.6; }
    .header-actions { display: flex; align-items: center; gap: 10px; }
    .guide-link { color: var(--claret, #75013f); font-size: 10px; font-weight: 700; text-decoration: none; }
    .document-list { display: grid; gap: 0; }
    .document-row { display: grid; grid-template-columns: 38px minmax(0, 1fr) auto 36px; align-items: center; gap: 14px; padding: 15px 0; border-bottom: 1px solid var(--border, #eae5df); }
    .document-row:last-child { border-bottom: 0; }
    .document-icon { display: grid; place-items: center; width: 34px; height: 34px; border-radius: 7px; background: var(--warm-light, #f7f5f3); color: var(--claret, #75013f); font-size: 17px; }
    .document-main { display: grid; gap: 4px; min-width: 0; }
    .document-main strong { overflow: hidden; color: var(--ink, #000); font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
    .document-main span, .document-context span, .document-version { color: var(--muted, #6f6a6d); font-size: 10px; }
    .document-context { display: grid; justify-items: end; gap: 5px; }
    .document-version { text-align: right; }
    .error-banner { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 14px 18px; border: 1px solid #f8b4b4; border-radius: 8px; background: #fde8e8; color: #9b1c1c; font-size: 12px; }
    .error-banner p { margin: 3px 0 0; }
    .loading-grid { display: grid; gap: 12px; }
    @media (max-width: 700px) { :host { padding: 16px; } .page-header { align-items: flex-start; flex-direction: column; } .document-row { grid-template-columns: 34px minmax(0, 1fr); } .document-context, .document-version { justify-items: start; grid-column: 2; } }
  `],
})
export class PolicyholderDocumentsComponent implements OnInit {
  private readonly store = inject(Store);
  private readonly profileService = inject(CustomerProfileService);
  private readonly policyService = inject(PolicyService);
  private readonly quoteService = inject(QuoteService);
  private readonly documentService = inject(DocumentService);
  private readonly api = inject(ApiService);

  documents: DocumentResponse[] = [];
  loading = false;
  error: string | null = null;
  isEmployee = false;

  ngOnInit(): void { this.loadDocuments(); }

  loadDocuments(): void {
    this.loading = true;
    this.error = null;
    this.store.select(selectUserRole).pipe(take(1)).subscribe((role) => {
      this.isEmployee = ['UNDERWRITER', 'RISK_ENGINEER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'VENDOR_MANAGER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'].includes((role ?? '').toUpperCase());
    });
    this.documentService.getBoundDocuments().subscribe({
      next: (documents) => {
        this.documents = (documents ?? []).sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''));
        this.loading = false;
      },
      error: (err) => this.handleError(err),
    });
  }

  private handleError(err: { status?: number; error?: { message?: string }; message?: string }): void {
    this.loading = false;
    if (err?.status === 404) {
      this.documents = [];
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
