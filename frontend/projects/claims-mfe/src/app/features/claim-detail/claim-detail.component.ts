import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ClaimResponse } from '../../models/claim.models';
import { ClaimsService } from '../../services/claims.service';
import { PolicyService } from '../../services/policy.service';
import { RecoveryService } from '../../services/recovery.service';
import { RecoveryCaseResponse, RecoverySeverity } from '../../models/recovery.models';

import { CardComponent, ButtonComponent, BadgeComponent, SkeletonComponent } from 'ui-core';

@Component({
  selector: 'claim-detail',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, DecimalPipe, FormsModule, RouterLink, CardComponent, ButtonComponent, BadgeComponent, SkeletonComponent],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div class="header-left">
          <a routerLink="/claims" class="back-link">&larr; Back to claims queue</a>
          <div class="header-title-row">
          <h1>Claim {{ claim?.claimNumber || 'details' }}</h1>
            @if (claim) {
              <is-badge [variant]="statusVariant(claim.status)" size="md">
                {{ formatStatus(claim.status) }}
              </is-badge>
            }
          </div>
          <p class="page-description">Detailed overview of incident report, coverage checks, reserves, and adjuster triage.</p>
        </div>
      </header>

      @if (loading) {
        <div class="loading-state" role="status" aria-live="polite">
          <is-skeleton variant="card" />
        </div>
      } @else if (notFound) {
        <is-card title="Claim Not Found" subtitle="The requested claim identifier does not exist in the claims registry.">
          <div class="state-message">
            <p>No claim record matching the requested reference was found.</p>
            <is-button variant="secondary" (click)="goToClaims()">Return to claims queue</is-button>
          </div>
        </is-card>
      } @else if (error) {
        <div class="error-banner" role="alert">
          <div>
            <strong>Error retrieving claim:</strong>
            <p>{{ error }}</p>
          </div>
          <is-button variant="secondary" size="sm" (click)="loadClaim()">Retry</is-button>
        </div>
      } @else if (claim) {
        @if (recoveryLoading) {
          <div class="recovery-banner" role="status" aria-live="polite">Checking recovery support for this claim…</div>
        } @else if (recoveryCase) {
          <div class="recovery-banner success">
            <div>
            <strong>Recovery case active</strong>
              <p>{{ recoveryCase.currentRestorePercent || 0 }}% restored · {{ formatStatus(recoveryCase.status) }}</p>
            </div>
            <is-button variant="secondary" size="sm" (click)="goToRecovery()">Open recovery</is-button>
          </div>
        } @else {
          <div class="recovery-banner">
            <div>
              <strong>Business recovery support</strong>
              <p>Start a recovery case to track restoration and choose a supported recovery path.</p>
            </div>
            <is-button variant="secondary" size="sm" [disabled]="recoverySaving" (click)="startRecovery()">
              {{ recoverySaving ? 'Starting…' : 'Start recovery case' }}
            </is-button>
          </div>
        }

        @if (recoveryError) {
          <div class="error-banner" role="alert"><p>{{ recoveryError }}</p></div>
        }

        @if (actionSuccess) {
          <div class="recovery-banner success" role="status">
            <div>
              <strong>Action Completed</strong>
              <p>{{ actionSuccess }}</p>
            </div>
            <is-button variant="secondary" size="sm" (click)="actionSuccess = null">Dismiss</is-button>
          </div>
        }

        @if (actionError) {
          <div class="error-banner" role="alert">
            <div>
              <strong>Action Error:</strong>
              <p>{{ actionError }}</p>
            </div>
            <is-button variant="secondary" size="sm" (click)="actionError = null">Dismiss</is-button>
          </div>
        }

        <div class="detail-grid">
          <!-- Main Column -->
          <div class="detail-main">
            <!-- Main Details Card -->
            <article class="detail-card">
              <div class="card-section">
                <span class="section-label">Incident Information</span>
                <dl class="meta-grid">
                  <div>
                    <dt>Claim Number</dt>
                    <dd class="font-mono font-bold">{{ claim.claimNumber }}</dd>
                  </div>
                  <div>
                    <dt>Incident Date</dt>
                    <dd>{{ claim.incidentDate }}</dd>
                  </div>
                  <div>
                    <dt>Reported Date</dt>
                    <dd>{{ claim.reportedDate }}</dd>
                  </div>
                </dl>
              </div>

              <!-- Linked Policy Information -->
              <div class="card-section divider">
                <div class="policy-header-flex">
                  <span class="section-label">Linked Commercial Policy</span>
                  @if (linkedPolicy) {
                    <a [routerLink]="['/policy', linkedPolicy.policyId]" class="policy-link">
                      View Full Policy Contract &rarr;
                    </a>
                  }
                </div>
                @if (loadingPolicy) {
                  <p class="muted-note">Loading linked policy contract…</p>
                } @else if (linkedPolicy) {
                  <dl class="meta-grid">
                    <div>
                      <dt>Policy Number</dt>
                      <dd>
                        <a [routerLink]="['/policy', linkedPolicy.policyId]" class="policy-num-link font-mono font-bold">
                          {{ linkedPolicy.policyNumber }}
                        </a>
                      </dd>
                    </div>
                    <div>
                      <dt>Product Line</dt>
                      <dd>{{ linkedPolicy.productCode || 'Commercial Package' }}</dd>
                    </div>
                    <div>
                      <dt>Policy Status</dt>
                      <dd><span class="policy-status-tag">{{ linkedPolicy.status }}</span></dd>
                    </div>
                    <div>
                      <dt>Term Period</dt>
                      <dd>{{ linkedPolicy.effectiveDate }} &ndash; {{ linkedPolicy.expirationDate }}</dd>
                    </div>
                    <div>
                      <dt>Annual Written Premium</dt>
                      <dd>₹{{ linkedPolicy.totalPremium | number:'1.0-0' }}</dd>
                    </div>
                  </dl>
                } @else if (claim.policyId) {
                  <dl class="meta-grid">
                    <div>
                      <dt>Policy Reference</dt>
                      <dd>
                        <a [routerLink]="['/policy', claim.policyId]" class="policy-link font-bold">
                          Policy Details &rarr;
                        </a>
                      </dd>
                    </div>
                  </dl>
                }
              </div>

              <div class="card-section divider">
                <span class="section-label">Incident Description</span>
                <p class="description-text">{{ claim.description }}</p>
              </div>

              @if (claim.incidentDescription || claim.coverageDecision || claim.coverageDecisionReason || claim.status === 'ADJUSTER_REVIEWED' || claim.status === 'APPROVED' || claim.status === 'SETTLED') {
                <div class="card-section divider">
                  <div class="policy-header-flex">
                    <span class="section-label">Adjuster Technical Assessment & Findings</span>
                    @if (claim.coverageDecision) {
                      <is-badge [variant]="claim.coverageDecision === 'APPROVED' ? 'success' : claim.coverageDecision === 'DENIED' ? 'danger' : 'warning'" size="sm">
                        {{ claim.coverageDecision }}
                      </is-badge>
                    }
                  </div>
                  @if (claim.incidentDescription) {
                    <div style="margin-bottom: 12px;">
                      <span class="sub-label">Investigation Findings & Evidence</span>
                      <p class="findings-box">{{ claim.incidentDescription }}</p>
                    </div>
                  }
                  @if (claim.coverageDecisionReason) {
                    <div style="margin-bottom: 12px;">
                      <span class="sub-label">Coverage Decision Rationale</span>
                      <p class="findings-box">{{ claim.coverageDecisionReason }}</p>
                    </div>
                  }
                  <dl class="meta-grid">
                    <div>
                      <dt>Assessed Covered Loss</dt>
                      <dd>₹{{ (claim.estimatedCoveredLoss || claim.estimatedLoss || 0) | number:'1.0-0' }}</dd>
                    </div>
                    <div>
                      <dt>Recommended Indemnity Payout</dt>
                      <dd class="font-mono font-bold" style="color: var(--claret, #75013f);">₹{{ (claim.payoutAmount || claim.payableAmount || 0) | number:'1.0-0' }}</dd>
                    </div>
                  </dl>
                </div>
              }

              @if (claim.closureReason) {
                <div class="card-section divider">
                  <span class="section-label">Closure Reason</span>
                  <p class="closure-text">{{ claim.closureReason }}</p>
                </div>
              }
            </article>

            <!-- Adjuster Action Workbench -->
            @if (isAdjuster) {
              <article class="detail-card action-card">
                <div class="card-section">
                  <div class="action-card-header">
                    <span class="section-label">Claims Adjuster Workbench</span>
                    <div class="tab-pills">
                      <button type="button" class="tab-pill" [class.active]="activeAdjusterTab === 'reserve'" (click)="activeAdjusterTab = 'reserve'">Reserves</button>
                      <button type="button" class="tab-pill" [class.active]="activeAdjusterTab === 'assessment'" (click)="activeAdjusterTab = 'assessment'">Assessment</button>
                      <button type="button" class="tab-pill" [class.active]="activeAdjusterTab === 'decision'" (click)="activeAdjusterTab = 'decision'">Coverage Decision</button>
                    </div>
                  </div>

                  <!-- Explanatory Context for Active Workbench Tab -->
                  <div class="workbench-guide-banner">
                    @if (activeAdjusterTab === 'reserve') {
                      <span><strong>Indemnity Reserve:</strong> Financial liability estimated and set aside on the carrier balance sheet for anticipated claim payments and expenses.</span>
                    } @else if (activeAdjusterTab === 'assessment') {
                      <span><strong>Technical Assessment:</strong> Adjuster investigation into the physical cause of loss, covered vs excluded damages, and deductible calculation.</span>
                    } @else if (activeAdjusterTab === 'decision') {
                      <span><strong>Coverage Decision:</strong> Contractual confirmation whether the reported loss is accepted (APPROVED) or excluded (DENIED) under the policy terms.</span>
                    }
                  </div>

                  @if (activeAdjusterTab === 'reserve') {
                    <div class="action-form">
                      <div class="form-row">
                        <div class="form-group">
                          <label for="reserveAmt">Reserve Amount (INR)</label>
                          <input id="reserveAmt" type="number" [(ngModel)]="reserveAmount" class="form-input" min="0" />
                        </div>
                        <div class="form-group flex-2">
                          <label for="reserveRsn">Reserve Rationale</label>
                          <input id="reserveRsn" type="text" [(ngModel)]="reserveReason" placeholder="Rationale for initial or updated reserve..." class="form-input" />
                        </div>
                      </div>
                      <div class="form-actions">
                        <is-button variant="primary" size="sm" [disabled]="savingReserve || !reserveAmount" (click)="submitReserve()">
                          {{ savingReserve ? 'Saving...' : 'Set / Update Reserve' }}
                        </is-button>
                      </div>
                    </div>
                  }

                  @if (activeAdjusterTab === 'assessment') {
                    <div class="action-form">
                      <div class="form-row">
                        <div class="form-group flex-2">
                          <label for="causeOfLoss">Cause of Loss</label>
                          <input id="causeOfLoss" type="text" [(ngModel)]="causeOfLoss" placeholder="e.g. Electrical fire in warehouse server room" class="form-input" />
                        </div>
                        <div class="form-group">
                          <label for="covCheck">Covered?</label>
                          <select id="covCheck" [(ngModel)]="assessmentCovered" class="form-input">
                            <option [ngValue]="true">Yes (Covered)</option>
                            <option [ngValue]="false">No (Excluded)</option>
                          </select>
                        </div>
                      </div>
                      <div class="form-group">
                        <label for="findingsText">Investigation Findings & Evidence</label>
                        <textarea id="findingsText" [(ngModel)]="assessmentFindings" rows="3" placeholder="Detailed investigation findings..." class="form-input"></textarea>
                      </div>
                      <div class="form-row grid-4">
                        <div class="form-group">
                          <label for="totLoss">Total Loss</label>
                          <input id="totLoss" type="number" [(ngModel)]="totalLossAmount" (ngModelChange)="onLossCalculationChange()" class="form-input" min="0" />
                        </div>
                        <div class="form-group">
                          <label for="covLoss">Covered Loss</label>
                          <input id="covLoss" type="number" [(ngModel)]="coveredLossAmount" (ngModelChange)="onLossCalculationChange()" class="form-input" min="0" />
                        </div>
                        <div class="form-group">
                          <label for="dedApplied">Deductible</label>
                          <input id="dedApplied" type="number" [(ngModel)]="deductibleApplied" (ngModelChange)="onLossCalculationChange()" class="form-input" min="0" />
                        </div>
                        <div class="form-group">
                          <label for="netLoss">Net Loss</label>
                          <input id="netLoss" type="number" [(ngModel)]="netLossAmount" class="form-input" min="0" />
                        </div>
                      </div>
                      <div class="form-actions">
                        <is-button variant="primary" size="sm" [disabled]="savingAssessment || !causeOfLoss" (click)="submitAssessment()">
                          {{ savingAssessment ? 'Saving...' : 'Record Loss Assessment' }}
                        </is-button>
                      </div>
                    </div>
                  }

                  @if (activeAdjusterTab === 'decision') {
                    <div class="action-form">
                      <div class="form-row">
                        <div class="form-group">
                          <label for="decisionVal">Coverage Decision</label>
                          <select id="decisionVal" [(ngModel)]="decisionValue" class="form-input">
                            <option value="APPROVED">APPROVED</option>
                            <option value="PARTIAL">PARTIAL</option>
                            <option value="DENIED">DENIED</option>
                          </select>
                        </div>
                        <div class="form-group flex-2">
                          <label for="decReason">Decision Rationale</label>
                          <input id="decReason" type="text" [(ngModel)]="decisionReason" placeholder="Policy justification or exclusions..." class="form-input" />
                        </div>
                      </div>
                      <div class="form-actions">
                        <is-button variant="primary" size="sm" [disabled]="savingDecision" (click)="submitDecision()">
                          {{ savingDecision ? 'Submitting...' : 'Record Coverage Decision' }}
                        </is-button>
                      </div>
                    </div>
                  }
                </div>
              </article>
            }

            <!-- Manager Action Card -->
            @if (isManager) {
              <article class="detail-card action-card manager-card">
                <div class="card-section">
                  <span class="section-label">Claims Manager Governance & Settlement</span>
                  <p class="manager-desc">Manager oversight is required for settlement authorization, payment disbursement, and formal claim closure.</p>
                  
                  <div class="manager-grid">
                    <div class="manager-action-block">
                      <h4>1. Settlement Authorization</h4>
                      <p class="helper-subtitle">Authorize indemnity payout to the policyholder.</p>
                      <div class="form-group">
                        <label for="settleAmt">Authorized Amount (INR)</label>
                        <input id="settleAmt" type="number" [(ngModel)]="settlementApprovedAmount" class="form-input" min="0" />
                      </div>
                      <div class="form-actions">
                        <is-button variant="primary" size="sm" [disabled]="savingSettlement || !settlementApprovedAmount" (click)="submitSettlement()">
                          {{ savingSettlement ? 'Approving...' : 'Approve Settlement' }}
                        </is-button>
                      </div>
                    </div>

                    <div class="manager-action-block">
                      <h4>2. Payment Disbursement</h4>
                      <p class="helper-subtitle">Record payment reference to disburse payout and settle.</p>
                      <div class="form-group">
                        <label for="pmtRef">Payment Reference</label>
                        <input id="pmtRef" type="text" [(ngModel)]="paymentReferenceInput" placeholder="e.g. PMT-2026-001 or EFT-9921" class="form-input" />
                      </div>
                      <div class="form-actions">
                        <is-button variant="primary" size="sm" [disabled]="savingPayment || (claim.status !== 'APPROVED' && claim.status !== 'SETTLED')" (click)="submitPayment()">
                          {{ savingPayment ? 'Disbursing...' : 'Disburse & Settle' }}
                        </is-button>
                      </div>
                    </div>

                    <div class="manager-action-block">
                      <h4>3. Claim Finalization & Closure</h4>
                      <p class="helper-subtitle">Archive claim once settlement or recovery is resolved.</p>
                      <div class="form-group">
                        <label for="closeRsn">Closure Reason</label>
                        <input id="closeRsn" type="text" [(ngModel)]="closureReasonInput" placeholder="e.g. Settlement finalized and claim resolved" class="form-input" />
                      </div>
                      <div class="form-actions">
                        <is-button variant="secondary" size="sm" [disabled]="savingClosure || !closureReasonInput" (click)="submitClosure()">
                          {{ savingClosure ? 'Closing...' : 'Close Claim' }}
                        </is-button>
                      </div>
                    </div>
                  </div>
                </div>
              </article>
            }
          </div>

          <!-- Financials & Workflow Sidebar Card -->
          <aside class="detail-sidebar">
            <article class="detail-card">
              <div class="card-section">
                <span class="section-label">Loss Valuation</span>
                <div class="amount-hero">
                  <span class="amount-label">Estimated Loss</span>
                  <strong class="amount-value">{{ claim.estimatedLoss | currency:'INR':'symbol':'1.0-0' }}</strong>
                </div>
                @if (claim.payoutAmount !== undefined && claim.payoutAmount !== null) {
                  <div class="amount-sub">
                    <span class="amount-label">Payout Amount</span>
                    <strong class="amount-subvalue">{{ claim.payoutAmount | currency:'INR':'symbol':'1.0-0' }}</strong>
                  </div>
                }
              </div>

              <div class="card-section divider">
                <span class="section-label">Triage & Coverage</span>
                <dl class="sidebar-meta">
                  <div>
                    <dt>Assigned Adjuster</dt>
                    <dd><strong>{{ adjusterName || 'Claims Adjuster (Assigned)' }}</strong></dd>
                    @if (claim.assignedAdjusterId) {
                      <div style="margin-top: 6px;">
                        <is-button
                          variant="secondary"
                          size="sm"
                          (click)="openAdjusterChat()"
                        >
                          💬 Message Adjuster
                        </is-button>
                      </div>
                    }
                    @if (isAdmin || isManager) {
                      <div style="margin-top: 10px; display: flex; flex-direction: column; gap: 6px;">
                        <is-button
                          variant="secondary"
                          size="sm"
                          [disabled]="assigningAdjuster"
                          (click)="showReassignAdjuster = !showReassignAdjuster"
                        >
                          🔄 {{ showReassignAdjuster ? 'Cancel Reassign' : 'Reassign Adjuster' }}
                        </is-button>
                        @if (showReassignAdjuster) {
                          <div class="reassign-block" style="background: #f8fafc; padding: 10px; border-radius: 6px; border: 1px solid var(--border);">
                            <label style="font-size: 10px; font-weight: 700; display: block; margin-bottom: 4px; text-transform: uppercase;">Select Adjuster</label>
                            <select [(ngModel)]="selectedAdjusterId" style="width: 100%; padding: 6px; font-size: 12px; margin-bottom: 8px; border: 1px solid var(--border); border-radius: 4px;">
                              <option value="">-- Auto-assign least loaded --</option>
                              @for (adj of availableAdjusters; track adj.userId) {
                                <option [value]="adj.userId">{{ adj.displayName || adj.email }}</option>
                              }
                            </select>
                            <div style="display: flex; gap: 6px;">
                              <is-button variant="primary" size="sm" [disabled]="assigningAdjuster" (click)="reassignAdjuster()">
                                {{ assigningAdjuster ? 'Assigning...' : 'Confirm' }}
                              </is-button>
                            </div>
                          </div>
                        }
                      </div>
                    }
                  </div>
                  <div>
                    <dt>Coverage Decision</dt>
                    <dd>{{ claim.coverageDecision || 'Pending Investigation' }}</dd>
                  </div>
                  <div>
                    <dt>Coverage Confirmed</dt>
                    <dd>{{ claim.coverageConfirmed ? 'Yes' : 'Awaiting Review' }}</dd>
                  </div>
                  <div>
                    <dt>Last Updated</dt>
                    <dd>{{ claim.updatedAt | date:'medium' }}</dd>
                  </div>
                </dl>
              </div>
            </article>
          </aside>
        </div>
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
      max-width: 1380px;
      margin: 0 auto;
    }

    .page-header {
      padding: 2px 0 4px;
    }

    .back-link {
      display: inline-block;
      font-size: 11px;
      font-weight: 600;
      color: var(--claret, #75013f);
      text-decoration: none;
      margin-bottom: 8px;
    }
    .back-link:hover {
      text-decoration: underline;
    }

    .header-title-row {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .header-title-row h1 {
      margin: 0;
      color: var(--ink, #000000);
      font-size: clamp(24px, 2.4vw, 32px);
      letter-spacing: -0.04em;
      font-weight: 700;
    }

    .page-description {
      max-width: 730px;
      margin: 6px 0 0;
      color: var(--muted, #6f6a6d);
      font-size: 12px;
      line-height: 1.6;
    }

    .detail-grid {
      display: grid;
      grid-template-columns: 1.4fr 0.8fr;
      gap: 20px;
      align-items: start;
    }

    .detail-card {
      background: #ffffff;
      border: 1px solid var(--border, #eae5df);
      border-radius: 10px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
      overflow: hidden;
    }

    .card-section {
      padding: 20px;
    }
    .card-section.divider {
      border-top: 1px solid var(--border, #eae5df);
    }

    .section-label {
      display: block;
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: var(--claret, #75013f);
      margin-bottom: 12px;
    }

    .meta-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 12px 18px;
      font-size: 11px;
    }
    .meta-grid dt {
      color: var(--muted, #6f6a6d);
      font-size: 10px;
      margin-bottom: 2px;
    }
    .meta-grid dd {
      margin: 0;
      color: var(--ink, #000000);
      font-weight: 600;
      word-break: break-all;
    }

    .description-text, .closure-text {
      margin: 0;
      font-size: 12px;
      line-height: 1.65;
      color: #312e30;
    }

    .sub-label {
      display: block;
      font-size: 10px;
      font-weight: 700;
      color: var(--muted, #6f6a6d);
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin-bottom: 4px;
    }

    .findings-box {
      margin: 0;
      font-size: 12px;
      line-height: 1.6;
      color: #2e2a2c;
      background: #faf8f5;
      padding: 10px 14px;
      border-radius: 6px;
      border: 1px solid var(--border, #eae5df);
    }

    .amount-hero {
      background: var(--warm-light, #f7f5f3);
      padding: 14px 16px;
      border-radius: 8px;
      border: 1px solid var(--border, #eae5df);
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .amount-label {
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: #7d777c;
    }

    .amount-value {
      font-size: 24px;
      font-weight: 800;
      color: var(--ink, #000000);
    }

    .amount-sub {
      margin-top: 10px;
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      font-size: 11px;
      padding: 0 4px;
    }
    .amount-subvalue {
      font-weight: 700;
      color: #176b45;
    }

    .sidebar-meta {
      display: flex;
      flex-direction: column;
      gap: 12px;
      font-size: 11px;
    }
    .sidebar-meta dt {
      color: var(--muted, #6f6a6d);
      font-size: 10px;
      margin-bottom: 2px;
    }
    .sidebar-meta dd {
      margin: 0;
      font-weight: 600;
      color: var(--ink, #000000);
      word-break: break-all;
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

    .recovery-banner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 14px 18px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 8px;
      background: var(--warm-light, #f7f5f3);
      color: var(--ink, #000);
      font-size: 12px;
    }
    .recovery-banner p { margin: 4px 0 0; color: var(--muted, #6f6a6d); font-size: 11px; }
    .recovery-banner.success { border-color: #b8dfc9; background: #effaf3; }

    .state-message {
      display: flex;
      flex-direction: column;
      gap: 12px;
      align-items: flex-start;
      padding: 12px 0;
    }

    .detail-main {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .action-card {
      border: 1px solid var(--border, #eae5df);
    }

    .action-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      flex-wrap: wrap;
      gap: 10px;
    }

    .tab-pills {
      display: flex;
      gap: 6px;
    }

    .tab-pill {
      background: #f3f0ec;
      border: 1px solid var(--border, #eae5df);
      padding: 5px 12px;
      border-radius: 20px;
      font-size: 11px;
      font-weight: 600;
      cursor: pointer;
      color: #555;
      transition: all 0.2s;
    }
    .tab-pill:hover {
      background: #eae5df;
    }
    .tab-pill.active {
      background: var(--claret, #75013f);
      color: #ffffff;
      border-color: var(--claret, #75013f);
    }

    .action-form {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }

    .form-row {
      display: flex;
      gap: 14px;
    }
    .form-row.grid-4 {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 4px;
      flex: 1;
    }
    .form-group.flex-2 {
      flex: 2;
    }
    .form-group label {
      font-size: 10px;
      font-weight: 700;
      color: var(--muted, #6f6a6d);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .form-input {
      border: 1px solid var(--border, #eae5df);
      border-radius: 6px;
      padding: 8px 12px;
      font-size: 12px;
      background: #faf9f7;
      color: var(--ink, #000);
      outline: none;
      font-family: inherit;
    }
    .form-input:focus {
      border-color: var(--claret, #75013f);
      background: #ffffff;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 6px;
    }

    .manager-card {
      border-left: 4px solid var(--claret, #75013f);
    }

    .manager-desc {
      font-size: 11px;
      color: var(--muted, #6f6a6d);
      margin: -4px 0 16px;
    }

    .manager-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 18px;
    }

    .manager-action-block {
      background: var(--warm-light, #f7f5f3);
      padding: 16px;
      border-radius: 8px;
      border: 1px solid var(--border, #eae5df);
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .manager-action-block h4 {
      margin: 0;
      font-size: 12px;
      font-weight: 700;
      color: var(--ink, #000);
    }

    .policy-header-flex {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      margin-bottom: 8px;
    }
    .policy-link, .policy-num-link {
      font-size: 11px;
      font-weight: 600;
      color: var(--claret, #75013f);
      text-decoration: none;
    }
    .policy-link:hover, .policy-num-link:hover {
      text-decoration: underline;
    }
    .policy-status-tag {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 4px;
      background: #effaf3;
      color: #176b45;
      font-size: 10px;
      font-weight: 700;
      letter-spacing: 0.04em;
    }
    .helper-subtitle {
      font-size: 11px;
      color: var(--muted, #6f6a6d);
      margin: -6px 0 6px;
    }
    .workbench-guide-banner {
      background: #f7f5f3;
      border-left: 3px solid var(--claret, #75013f);
      padding: 10px 14px;
      border-radius: 4px;
      margin-bottom: 14px;
      font-size: 11px;
      color: #312e30;
      line-height: 1.5;
    }
    .workbench-guide-banner strong {
      color: var(--ink, #000);
    }
    .muted-note {
      font-size: 11px;
      color: var(--muted, #6f6a6d);
      font-style: italic;
    }

    @media (max-width: 900px) {
      .detail-grid {
        grid-template-columns: 1fr;
      }
      .meta-grid {
        grid-template-columns: 1fr;
      }
      .manager-grid {
        grid-template-columns: 1fr;
      }
      .form-row.grid-4 {
        grid-template-columns: 1fr 1fr;
      }
    }
  `],
})
export class ClaimDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly claimsService = inject(ClaimsService);
  private readonly recoveryService = inject(RecoveryService);
  private readonly policyService = inject(PolicyService);

  claimId = '';
  claim: ClaimResponse | null = null;
  loading = false;
  error: string | null = null;
  notFound = false;
  recoveryCase: RecoveryCaseResponse | null = null;
  recoveryLoading = false;
  recoverySaving = false;
  recoveryError: string | null = null;

  // Role detection
  isAdmin = false;
  isAdjuster = false;
  isManager = false;

  // Adjuster metadata & reassignment state
  adjusterName: string | null = null;
  availableAdjusters: any[] = [];
  showReassignAdjuster = false;
  selectedAdjusterId = '';
  assigningAdjuster = false;

  // Linked policy state
  linkedPolicy: any = null;
  loadingPolicy = false;

  // Adjuster action state
  activeAdjusterTab: 'reserve' | 'assessment' | 'decision' = 'reserve';
  reserveAmount = 0;
  reserveReason = '';
  savingReserve = false;

  causeOfLoss = '';
  assessmentFindings = '';
  assessmentCovered = true;
  totalLossAmount = 0;
  coveredLossAmount = 0;
  deductibleApplied = 1000;
  netLossAmount = 0;
  savingAssessment = false;

  decisionValue = 'APPROVED';
  decisionReason = '';
  savingDecision = false;

  // Manager action state
  settlementApprovedAmount = 0;
  savingSettlement = false;
  paymentReferenceInput = '';
  savingPayment = false;
  closureReasonInput = '';
  savingClosure = false;

  // Banner status
  actionSuccess: string | null = null;
  actionError: string | null = null;

  ngOnInit(): void {
    this.claimId = this.route.snapshot.paramMap.get('claimId') ?? '';
    const rawRole = typeof localStorage !== 'undefined' ? localStorage.getItem('is_role') : null;
    const role = (rawRole ?? '').toUpperCase().replace(/^ROLE_/, '');
    this.isAdmin = ['ADMIN', 'SYSTEM_ADMINISTRATOR'].includes(role);
    this.isAdjuster = ['CLAIMS_ADJUSTER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'].includes(role);
    this.isManager = ['CLAIMS_MANAGER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'].includes(role);

    if (this.isAdmin || this.isManager) {
      this.loadAvailableAdjusters();
    }

    this.loadClaim();
  }

  loadClaim(): void {
    if (!this.claimId) return;

    this.loading = true;
    this.error = null;
    this.notFound = false;

    this.claimsService.getClaim(this.claimId).subscribe({
      next: (data) => {
        this.claim = data;
        this.loading = false;

        // Populate initial form values and preserve adjuster assessment / decision
        if (data.incidentDescription) {
          this.assessmentFindings = data.incidentDescription;
        }
        if (data.coverageDecisionReason) {
          this.decisionReason = data.coverageDecisionReason;
        }
        if (data.coverageDecision) {
          this.decisionValue = data.coverageDecision;
        }
        if (data.estimatedCoveredLoss) {
          this.coveredLossAmount = Number(data.estimatedCoveredLoss);
        }
        if (data.payoutAmount || data.payableAmount) {
          this.netLossAmount = Number(data.payoutAmount || data.payableAmount);
        }

        if (this.reserveAmount === 0 && data.estimatedLoss) {
          this.reserveAmount = Number(data.estimatedLoss);
        }
        if (this.totalLossAmount === 0 && data.estimatedLoss) {
          this.totalLossAmount = Number(data.estimatedLoss);
          if (!data.estimatedCoveredLoss) {
            this.coveredLossAmount = Number(data.estimatedLoss);
          }
          this.onLossCalculationChange();
        }

        // Prefill Claims Manager settlement amount with Adjuster's assessed payout amount or covered loss
        this.settlementApprovedAmount = Number(data.payoutAmount || data.payableAmount || data.estimatedCoveredLoss || data.estimatedLoss || 0);

        this.resolveAdjusterName(data.assignedAdjusterId);
        this.loadRecoveryCase(data);
        if (data.policyId) {
          this.loadLinkedPolicy(data.policyId);
        }
      },
      error: (err) => {
        this.loading = false;
        if (err?.status === 404) {
          this.notFound = true;
        } else if (err?.status === 403) {
          this.error = 'Access denied: You do not have permission to view this claim.';
        } else if (err?.status === 401) {
          this.error = 'Session expired. Please sign in again.';
        } else {
          this.error = err?.error?.message || err?.message || 'Failed to retrieve claim details from the backend.';
        }
      },
    });
  }

  onLossCalculationChange(): void {
    const covered = Number(this.coveredLossAmount || 0);
    const deductible = Number(this.deductibleApplied || 0);
    this.netLossAmount = Math.max(0, covered - deductible);
  }

  submitReserve(): void {
    if (!this.claim || this.savingReserve) return;
    this.savingReserve = true;
    this.actionError = null;
    this.actionSuccess = null;

    this.claimsService.createReserve(this.claim.claimId, this.reserveAmount, this.reserveReason).subscribe({
      next: (updated) => {
        this.claim = updated;
        this.savingReserve = false;
        this.actionSuccess = `Reserve set to ₹${this.reserveAmount.toLocaleString()} successfully.`;
      },
      error: (err) => {
        this.savingReserve = false;
        this.actionError = err?.error?.message || err?.message || 'Failed to update claim reserve.';
      },
    });
  }

  submitAssessment(): void {
    if (!this.claim || this.savingAssessment) return;
    this.savingAssessment = true;
    this.actionError = null;
    this.actionSuccess = null;

    this.claimsService
      .createAssessment(
        this.claim.claimId,
        this.causeOfLoss,
        this.assessmentFindings,
        this.assessmentCovered,
        this.totalLossAmount,
        this.coveredLossAmount,
        this.deductibleApplied,
        this.netLossAmount
      )
      .subscribe({
        next: (updated) => {
          this.claim = updated;
          this.savingAssessment = false;
          this.actionSuccess = 'Loss assessment and damage inspection recorded successfully.';
        },
        error: (err) => {
          this.savingAssessment = false;
          this.actionError = err?.error?.message || err?.message || 'Failed to record loss assessment.';
        },
      });
  }

  submitDecision(): void {
    if (!this.claim || this.savingDecision) return;
    this.savingDecision = true;
    this.actionError = null;
    this.actionSuccess = null;

    this.claimsService.recordDecision(this.claim.claimId, this.decisionValue, this.decisionReason).subscribe({
      next: (updated) => {
        this.claim = updated;
        this.savingDecision = false;
        this.actionSuccess = `Coverage decision [${this.decisionValue}] recorded successfully.`;
      },
      error: (err) => {
        this.savingDecision = false;
        this.actionError = err?.error?.message || err?.message || 'Failed to record coverage decision.';
      },
    });
  }

  submitSettlement(): void {
    if (!this.claim || this.savingSettlement) return;
    this.savingSettlement = true;
    this.actionError = null;
    this.actionSuccess = null;

    this.claimsService.approveSettlement(this.claim.claimId, this.settlementApprovedAmount).subscribe({
      next: (updated) => {
        this.claim = updated;
        this.savingSettlement = false;
        this.actionSuccess = `Settlement of ₹${this.settlementApprovedAmount.toLocaleString()} approved by Claims Manager.`;
      },
      error: (err) => {
        this.savingSettlement = false;
        this.actionError = err?.error?.message || err?.message || 'Failed to approve settlement.';
      },
    });
  }

  submitPayment(): void {
    if (!this.claim || this.savingPayment) return;
    this.savingPayment = true;
    this.actionError = null;
    this.actionSuccess = null;

    const ref = this.paymentReferenceInput.trim() || `PMT-${Date.now().toString(36).toUpperCase()}`;
    this.claimsService.recordPayment(this.claim.claimId, ref).subscribe({
      next: (updated) => {
        this.claim = updated;
        this.savingPayment = false;
        this.actionSuccess = `Payment disbursement of ₹${(updated.payoutAmount || updated.payableAmount || 0).toLocaleString()} recorded with reference ${ref}. Claim status is now ${updated.status}.`;
      },
      error: (err) => {
        this.savingPayment = false;
        this.actionError = err?.error?.message || err?.message || 'Failed to record payment disbursement.';
      }
    });
  }

  submitClosure(): void {
    if (!this.claim || this.savingClosure) return;
    if (!this.closureReasonInput.trim()) {
      this.actionError = 'Please provide a closure rationale before closing the claim.';
      return;
    }

    this.savingClosure = true;
    this.actionError = null;
    this.actionSuccess = null;

    this.claimsService.closeClaim(this.claim.claimId, this.closureReasonInput).subscribe({
      next: (updated) => {
        this.claim = updated;
        this.savingClosure = false;
        this.actionSuccess = 'Claim has been officially closed.';
      },
      error: (err) => {
        this.savingClosure = false;
        this.actionError = err?.error?.message || err?.message || 'Failed to close claim.';
      },
    });
  }

  loadLinkedPolicy(policyId: string): void {
    if (!policyId) return;
    this.loadingPolicy = true;
    this.policyService.getPolicy(policyId).subscribe({
      next: (policy) => {
        this.linkedPolicy = policy;
        this.loadingPolicy = false;
      },
      error: () => {
        this.loadingPolicy = false;
        this.linkedPolicy = null;
      }
    });
  }

  startRecovery(): void {
    if (!this.claim || this.recoverySaving) return;
    this.recoverySaving = true;
    this.recoveryError = null;
    this.recoveryService.createCase({
      claimId: this.claim.claimId,
      customerId: this.claim.customerId,
      severity: this.recoverySeverity(this.claim.estimatedLoss),
      recoveryObjective: `Restore business operations after claim ${this.claim.claimNumber}`,
    }).subscribe({
      next: (recoveryCase) => {
        this.recoveryCase = recoveryCase;
        this.recoverySaving = false;
      },
      error: (err) => {
        this.recoverySaving = false;
        this.recoveryError = err?.status === 403
          ? 'You do not have permission to start recovery for this claim.'
          : err?.error?.message || err?.message || 'The recovery case could not be created.';
      },
    });
  }

  goToRecovery(): void {
    this.router.navigate(['/recovery']);
  }

  statusVariant(status?: string): 'info' | 'success' | 'warning' | 'danger' | 'neutral' {
    const s = (status || '').toUpperCase();
    if (s === 'APPROVED' || s === 'SETTLED' || s === 'PAID') return 'success';
    if (s === 'RESERVED' || s === 'COVERAGE_REVIEW') return 'warning';
    if (s === 'ADJUSTER_REVIEWED') return 'info';
    if (s === 'DENIED') return 'danger';
    if (s === 'OPEN' || s === 'FNOL_RECEIVED') return 'info';
    return 'neutral';
  }

  formatStatus(status?: string): string {
    const s = (status || 'UNKNOWN').toUpperCase();
    if (s === 'ADJUSTER_REVIEWED') return 'ADJUSTER REVIEWED & APPROVED';
    return s.replace(/_/g, ' ');
  }

  goToClaims(): void {
    this.router.navigate(['/claims']);
  }

  private loadRecoveryCase(claim: ClaimResponse): void {
    this.recoveryLoading = true;
    this.recoveryError = null;
    this.recoveryService.getCases(claim.customerId).subscribe({
      next: (response) => {
        this.recoveryCase = (response.items || []).find((item) => item.claimId === claim.claimId) ?? null;
        this.recoveryLoading = false;
      },
      error: (err) => {
        this.recoveryLoading = false;
        if (err?.status !== 404) {
          this.recoveryError = err?.status === 403
            ? 'Recovery support is not available for this claim.'
            : 'Recovery status could not be checked.';
        }
      },
    });
  }

  private recoverySeverity(estimatedLoss: number | null | undefined): RecoverySeverity {
    const loss = Number(estimatedLoss || 0);
    if (loss >= 250000) return 'CRITICAL';
    if (loss >= 100000) return 'HIGH';
    if (loss >= 25000) return 'MEDIUM';
    return 'LOW';
  }

  openAdjusterChat(): void {
    if (!this.claim?.assignedAdjusterId) return;
    this.router.navigate(['/chat'], {
      queryParams: {
        recipientId: this.claim.assignedAdjusterId,
        name: 'Assigned Adjuster',
        entityType: 'CLAIM',
        entityId: this.claim.claimId
      }
    });
  }

  resolveAdjusterName(adjusterId?: string): void {
    if (!adjusterId) {
      this.adjusterName = null;
      return;
    }
    this.claimsService.getUserById(adjusterId).subscribe({
      next: (user) => {
        this.adjusterName = user?.displayName || user?.fullName || user?.email || adjusterId;
      },
      error: () => {
        this.adjusterName = adjusterId;
      }
    });
  }

  loadAvailableAdjusters(): void {
    this.claimsService.getAvailableAdjusters().subscribe({
      next: (adjusters) => {
        this.availableAdjusters = adjusters || [];
      },
      error: () => {
        this.availableAdjusters = [];
      }
    });
  }

  reassignAdjuster(): void {
    if (!this.claim || this.assigningAdjuster) return;
    this.assigningAdjuster = true;
    this.actionError = null;
    this.actionSuccess = null;

    const req$ = this.selectedAdjusterId
      ? this.claimsService.assignAdjuster(this.claim.claimId, this.selectedAdjusterId)
      : this.claimsService.autoAssignAdjuster(this.claim.claimId);

    req$.subscribe({
      next: (updated) => {
        this.claim = updated;
        this.assigningAdjuster = false;
        this.showReassignAdjuster = false;
        this.actionSuccess = 'Claims Adjuster assigned successfully.';
        this.resolveAdjusterName(updated.assignedAdjusterId);
      },
      error: (err) => {
        this.assigningAdjuster = false;
        this.actionError = err?.error?.message || err?.message || 'Failed to reassign adjuster.';
      }
    });
  }
}
