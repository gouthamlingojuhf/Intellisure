import { AsyncPipe, DatePipe, DecimalPipe, NgClass } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject, catchError, forkJoin, map, of, startWith, switchMap, take, tap } from 'rxjs';
import {
  ControlStatus,
  FindingSeverity,
  RecommendationPriority,
  RecommendationStatus,
  RiskAssessment,
  RiskFinding,
  RiskRecommendation,
} from '../../core/models/underwriting.models';
import { OfferQuoteTermsRequest, OfferedCoverageRequest, QuoteResponse, RecordUnderwritingDecisionRequest, UnderwritingDecisionType } from '../../core/models/quote.models';
import { UnderwritingService } from '../../core/services/underwriting.service';
import { QuoteService } from '../../core/services/quote.service';
import { ApiService } from '../../core/services/api.service';
import { selectUserId, selectUserRole } from '../../core/store/auth/auth.selectors';

interface UnderwritingQueueState {
  loading: boolean;
  error: string | null;
  items: RiskAssessment[];
  assignedQuotes: QuoteResponse[];
}

interface OfferedCoverageForm extends OfferedCoverageRequest {
  coverageName: string;
}

@Component({
  selector: 'is-underwriting-dashboard',
  standalone: true,
  imports: [AsyncPipe, DatePipe, DecimalPipe, NgClass, RouterLink],
  template: `
    <section class="page-shell">
      <header class="page-header">
        <div>
          <p class="eyebrow">Risk review</p>
          <h1>Underwriting & Assessment Queue</h1>
          <p class="description">Commercial quotes and risk assessments assigned to the signed-in underwriter or risk engineer.</p>
        </div>
        <a class="secondary-link" routerLink="/policy">View quotes and policies <span aria-hidden="true">→</span></a>
      </header>

      @if (queue$ | async; as state) {
        @if (state.loading) {
          <div class="state-card" role="status" aria-live="polite">Loading assigned queue…</div>
        } @else if (state.error) {
          <div class="state-card error" role="alert">
            <strong>Underwriting queue unavailable</strong>
            <p>{{ state.error }}</p>
            <button type="button" (click)="reload()">Try again</button>
          </div>
        } @else if (!state.items.length && !state.assignedQuotes.length) {
          <div class="state-card empty" role="status">
            <strong>No assigned items</strong>
            <p>New quotes and assessments assigned to this user will appear here.</p>
          </div>
        } @else {
          <section class="summary-grid" aria-label="Assigned queue summary">
            <article><span>Assigned Quotes</span><strong>{{ state.assignedQuotes.length }}</strong></article>
            <article><span>Quotes In Review</span><strong>{{ countQuotesByStatus(state.assignedQuotes, 'IN_REVIEW') }}</strong></article>
            <article><span>Assigned Assessments</span><strong>{{ state.items.length }}</strong></article>
          </section>

          @if (selectedAssessment || reviewQuote) {
            <section class="review-card" aria-labelledby="review-heading">
              <div class="review-heading">
                <div>
                  <p class="eyebrow">Employee action</p>
                  <h2 id="review-heading">Review {{ selectedAssessment ? (selectedAssessment.assessmentNumber || selectedAssessment.assessmentId) : (reviewQuote?.quoteNumber || 'Quote') }}</h2>
                  <p class="review-meta">Quote {{ reviewQuote?.quoteNumber || selectedAssessment?.quoteId }} · {{ reviewQuote?.productCode || 'Loading quote details…' }} · Status: {{ reviewQuote?.status }}</p>
                </div>
                <button type="button" class="secondary-button" (click)="clearReview()">Close review</button>
              </div>

              @if (reviewLoading) {
                <div class="inline-state" role="status">Loading the assigned quote…</div>
              } @else if (reviewError) {
                <div class="inline-state error" role="alert">{{ reviewError }}</div>
              } @else if (reviewQuote) {
                @if (customerProfile || reviewQuote.businessOperations) {
                  <div class="business-profile-box" style="background: #faf8f5; border: 1px solid var(--border); border-radius: 8px; padding: 14px 18px; margin-bottom: 16px;">
                    <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 8px;">
                      <span style="font-size: 10px; font-weight: 700; text-transform: uppercase; color: var(--claret, #75013f); letter-spacing: 0.08em;">Applicant Business Profile</span>
                      @if (customerProfile?.businessType) {
                        <span style="font-size: 11px; background: #eee8e2; padding: 2px 8px; border-radius: 4px; font-weight: 600;">{{ customerProfile.businessType }}</span>
                      }
                    </div>
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px; font-size: 12px; margin-bottom: 10px;">
                      <div>
                        <strong style="display: block; font-size: 10px; color: var(--muted); text-transform: uppercase;">Legal Entity</strong>
                        <span>{{ customerProfile?.businessName || 'Commercial Applicant' }}</span>
                      </div>
                      <div>
                        <strong style="display: block; font-size: 10px; color: var(--muted); text-transform: uppercase;">Principal & Phone</strong>
                        <span>{{ customerProfile?.ownerName || '—' }} · {{ customerProfile?.phone || '—' }}</span>
                      </div>
                      <div>
                        <strong style="display: block; font-size: 10px; color: var(--muted); text-transform: uppercase;">Operating Location</strong>
                        <span>{{ customerProfile?.city || '—' }}, {{ customerProfile?.state || '—' }} ({{ customerProfile?.country || 'India' }})</span>
                      </div>
                      <div>
                        <strong style="display: block; font-size: 10px; color: var(--muted); text-transform: uppercase;">Financial Scale</strong>
                        <span>Turnover: ₹{{ (customerProfile?.annualTurnover || 0) | number:'1.0-0' }} | Employees: {{ customerProfile?.employeeCount || '—' }}</span>
                      </div>
                    </div>
                    @if (reviewQuote.businessOperations) {
                      <div style="border-top: 1px solid #eae5df; padding-top: 8px; margin-top: 6px; font-size: 11px; color: #444;">
                        <strong style="color: var(--ink);">Operations & Declared Assets:</strong> {{ reviewQuote.businessOperations }}
                      </div>
                    }
                  </div>
                }

                <!-- Risk Engineer Assignment & Dispatch Panel -->
                @if (selectedAssessment || currentAssessmentId) {
                  <div class="findings-panel" style="margin-bottom: 14px;">
                    <div class="findings-panel-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                      <div>
                        <h3>Assigned Risk Engineer</h3>
                        <p class="helper">
                          Inspection engineer:
                          <strong>{{ engineerName || selectedAssessment?.assignedRiskEngineerId || 'None assigned' }}</strong>
                        </p>
                      </div>
                      @if (canManageQuote) {
                        <div style="display: flex; gap: 8px; align-items: center;">
                          <select [value]="selectedEngineerId" (change)="selectedEngineerId = readValue($event)" style="padding: 6px 10px; font-size: 12px; border: 1px solid var(--border); border-radius: 6px;">
                            <option value="">-- Auto-Assign Least Loaded --</option>
                            @for (eng of availableEngineers; track eng.userId) {
                              <option [value]="eng.userId">{{ eng.email }} ({{ eng.userId }})</option>
                            }
                          </select>
                          <button type="button" class="primary-button" [disabled]="actionLoading === 'assign-engineer'" (click)="assignEngineer()">
                            {{ actionLoading === 'assign-engineer' ? 'Assigning…' : 'Assign Engineer' }}
                          </button>
                        </div>
                      }
                    </div>
                  </div>
                }

                <!-- Risk Engineering Findings & Technical Controls (Visible to both Underwriters and Risk Engineers) -->
                <div class="findings-panel">
                  <div class="findings-panel-header">
                    <div>
                      <h3>Risk Engineering Evidence & Loss Controls</h3>
                      <p class="helper">Technical inspection findings, hazard severities, and required loss prevention recommendations.</p>
                    </div>
                    @if (findingsLoading) {
                      <span class="helper">Loading inspection evidence…</span>
                    }
                  </div>

                  @if (findings.length > 0) {
                    <div class="table-wrap" style="margin-bottom: 14px;">
                      <table>
                        <thead>
                          <tr>
                            <th>Finding Type</th>
                            <th>Description</th>
                            <th>Severity</th>
                            <th>Control Status</th>
                            <th>Recorded</th>
                          </tr>
                        </thead>
                        <tbody>
                          @for (f of findings; track f.findingId) {
                            <tr>
                              <td><strong>{{ f.findingType }}</strong></td>
                              <td>{{ f.description }}</td>
                              <td>
                                <span class="badge-severity" [ngClass]="'sev-' + f.severity.toLowerCase()">
                                  {{ f.severity }}
                                </span>
                              </td>
                              <td><span class="control-status">{{ f.controlStatus }}</span></td>
                              <td>{{ f.createdAt ? (f.createdAt | date:'shortDate') : '—' }}</td>
                            </tr>
                          }
                        </tbody>
                      </table>
                    </div>
                  } @else {
                    <div class="empty-panel-note">No technical risk findings recorded for this assessment yet.</div>
                  }

                  <!-- Recommendations -->
                  @if (recommendations.length > 0) {
                    <div class="recommendations-box">
                      <h4>Loss Control Recommendations</h4>
                      <div class="recs-list">
                        @for (rec of recommendations; track rec.recommendationId) {
                          <div class="rec-card">
                            <div class="rec-header">
                              <div>
                                <strong>{{ rec.recommendationType }}</strong>
                                <span class="badge-priority" [ngClass]="'prio-' + rec.priority.toLowerCase()">{{ rec.priority }} Priority</span>
                                @if (rec.requiredBeforeBind) {
                                  <span class="badge-bind-req">Required Before Bind</span>
                                }
                              </div>
                              <div class="rec-status-wrap">
                                <span class="status" [ngClass]="statusClass(rec.status)">{{ rec.status }}</span>
                                @if (canManageQuote && rec.status === 'PROPOSED') {
                                  <button type="button" class="action-btn-sm" (click)="updateRecommendationStatus(rec.recommendationId, 'ACCEPTED')">Accept</button>
                                }
                                @if (canManageQuote && rec.status === 'ACCEPTED') {
                                  <button type="button" class="action-btn-sm" (click)="updateRecommendationStatus(rec.recommendationId, 'VERIFIED')">Verify</button>
                                }
                              </div>
                            </div>
                            <p class="rec-desc">{{ rec.description }}</p>
                            @if (rec.targetDate) {
                              <small class="rec-target">Target Remediation: {{ rec.targetDate | date:'mediumDate' }}</small>
                            }
                          </div>
                        }
                      </div>
                    </div>
                  }

                  <!-- Risk Engineer Action Forms (for recording findings & recommendations) -->
                  @if (isRiskEngineer || canManageQuote) {
                    <div class="engineer-workbench">
                      <div class="workbench-tabs">
                        <button type="button" class="tab-btn" [class.active]="activeFormTab === 'finding'" (click)="activeFormTab = 'finding'">+ Record Finding</button>
                        <button type="button" class="tab-btn" [class.active]="activeFormTab === 'rec'" (click)="activeFormTab = 'rec'">+ Issue Recommendation</button>
                        <button type="button" class="tab-btn" [class.active]="activeFormTab === 'none'" (click)="activeFormTab = 'none'">Hide Form</button>
                      </div>

                      @if (activeFormTab === 'finding') {
                        <div class="workbench-form">
                          <h4>Record New Technical Finding</h4>
                          <div class="form-row-2">
                            <label>Finding Category
                              <select [value]="newFindingType" (change)="newFindingType = readValue($event)">
                                <option value="PROPERTY_HAZARD">Property & Structural Hazard</option>
                                <option value="FIRE_PROTECTION">Fire Suppression & Life Safety</option>
                                <option value="ELECTRICAL_RISK">Electrical & Mechanical Systems</option>
                                <option value="ENVIRONMENTAL_EXPOSURE">Environmental & Natural Hazard</option>
                                <option value="SECURITY_MEASURES">Site Security & Theft Controls</option>
                                <option value="OPERATIONAL_SAFETY">Operational Safety Protocols</option>
                              </select>
                            </label>
                            <label>Severity Level
                              <select [value]="newFindingSeverity" (change)="setFindingSeverity(readValue($event))">
                                <option value="LOW">Low</option>
                                <option value="MEDIUM">Medium</option>
                                <option value="HIGH">High</option>
                                <option value="CRITICAL">Critical</option>
                              </select>
                            </label>
                          </div>
                          <div class="form-row-2">
                            <label>Control Status
                              <select [value]="newFindingControl" (change)="setFindingControl(readValue($event))">
                                <option value="ADEQUATE">Adequate</option>
                                <option value="PARTIALLY_ADEQUATE">Partially Adequate</option>
                                <option value="INADEQUATE">Inadequate</option>
                                <option value="NOT_PRESENT">Not Present</option>
                                <option value="NOT_ASSESSED">Not Assessed</option>
                              </select>
                            </label>
                          </div>
                          <label>Finding Details & Observed Hazards
                            <textarea rows="3" [value]="newFindingDesc" (input)="newFindingDesc = readValue($event)" placeholder="Describe specific hazardous conditions observed during inspection..."></textarea>
                          </label>
                          <button type="button" class="primary-button" [disabled]="findingSubmitting || !newFindingDesc.trim()" (click)="submitFinding()">
                            {{ findingSubmitting ? 'Saving Finding…' : 'Save Risk Finding' }}
                          </button>
                        </div>
                      }

                      @if (activeFormTab === 'rec') {
                        <div class="workbench-form">
                          <h4>Issue Loss Control Recommendation</h4>
                          <div class="form-row-2">
                            <label>Recommendation Type
                              <select [value]="newRecType" (change)="newRecType = readValue($event)">
                                <option value="LOSS_PREVENTION">Loss Prevention Standard</option>
                                <option value="FIRE_SAFETY_UPGRADE">Fire Safety & Suppression Upgrade</option>
                                <option value="EQUIPMENT_MAINTENANCE">Equipment Maintenance & Certification</option>
                                <option value="SECURITY_ENHANCEMENT">Security & Access Control Enhancement</option>
                                <option value="HAZARD_REMEDIATION">Physical Hazard Remediation</option>
                              </select>
                            </label>
                            <label>Priority
                              <select [value]="newRecPriority" (change)="setRecPriority(readValue($event))">
                                <option value="LOW">Low</option>
                                <option value="MEDIUM">Medium</option>
                                <option value="HIGH">High</option>
                                <option value="CRITICAL">Critical</option>
                              </select>
                            </label>
                          </div>
                          <div class="form-row-2">
                            <label class="checkbox-label">
                              <input type="checkbox" [checked]="newRecRequiredBeforeBind" (change)="newRecRequiredBeforeBind = readChecked($event)" />
                              Required Before Binding Policy
                            </label>
                            <label>Target Completion Date
                              <input type="date" [value]="newRecTargetDate" (input)="newRecTargetDate = readValue($event)" />
                            </label>
                          </div>
                          <label>Recommendation Action Plan
                            <textarea rows="3" [value]="newRecDesc" (input)="newRecDesc = readValue($event)" placeholder="Specify corrective measures and engineering controls required..."></textarea>
                          </label>
                          <button type="button" class="primary-button" [disabled]="recSubmitting || !newRecDesc.trim()" (click)="submitRecommendation()">
                            {{ recSubmitting ? 'Saving Recommendation…' : 'Publish Recommendation' }}
                          </button>
                        </div>
                      }
                    </div>
                  }
                </div>

                @if (canManageQuote) {
                  @if (reviewQuote.status === 'ACCEPTED') {
                    <div class="review-grid" style="grid-template-columns: 1fr;">
                      <div class="review-section">
                        <h3>Customer Accepted Terms — Ready for Binding</h3>
                        <p class="helper">The customer accepted the terms on {{ reviewQuote.acceptedAt | date:'medium' }}. You can now bind this quote into an active commercial policy.</p>
                        <div style="display: flex; gap: 12px; align-items: center; margin-top: 10px;">
                          <button type="button" class="primary-button" [disabled]="actionLoading" (click)="bindPolicy()">
                            {{ actionLoading === 'bind' ? 'Binding policy…' : 'Bind & Issue Policy →' }}
                          </button>
                          <a class="secondary-button" [routerLink]="['/quotes', reviewQuote.quoteId]" style="text-decoration: none; display: inline-block;">
                            View full quote specifications
                          </a>
                        </div>
                      </div>
                    </div>
                  } @else if (reviewQuote.status === 'BOUND' || reviewQuote.status === 'ISSUED') {
                    <div class="review-grid" style="grid-template-columns: 1fr;">
                      <div class="review-section">
                        <h3>Coverage Bound & Active</h3>
                        <p class="helper">This quote has been bound into an active policy contract. Commercial coverage is in effect.</p>
                        <div style="display: flex; gap: 12px; align-items: center; margin-top: 10px;">
                          <a class="secondary-button" [routerLink]="['/quotes', reviewQuote.quoteId]" style="text-decoration: none; display: inline-block;">
                            View quote details
                          </a>
                          <a class="primary-button" routerLink="/policy" style="text-decoration: none; display: inline-block;">
                            View policy portfolio
                          </a>
                        </div>
                      </div>
                    </div>
                  } @else {
                  <div class="review-grid">
                    <div class="review-section">
                    <h3>Underwriting Decision</h3>
                    <label>Decision Outcome *
                      <select [value]="decision" (change)="setDecision(readValue($event))">
                        <option value="APPROVED">Approved — Accept risk application as submitted</option>
                        <option value="APPROVED_WITH_MODIFIED_TERMS">Approved with modified terms — Require adjusted limits or rates</option>
                        <option value="MORE_INFORMATION_REQUIRED">More information required — Request applicant or inspection data</option>
                        <option value="REFERRED">Referred — Escalate for senior underwriter concurrence</option>
                        <option value="DECLINED">Declined — Risk outside current commercial appetite</option>
                      </select>
                      <small class="helper-text">Formal underwriting disposition for this commercial quote application.</small>
                    </label>
                    <label>Underwriting Authority Level *
                      <select [value]="authorityLevel" (change)="authorityLevel = readValue($event)">
                        <option value="STANDARD_UW">STANDARD_UW — Standard Underwriter (Exposure up to ₹1 Crore)</option>
                        <option value="SENIOR_UW">SENIOR_UW — Senior Underwriter (Exposure up to ₹5 Crore)</option>
                        <option value="CHIEF_UW">CHIEF_UW — Chief Underwriting Officer (₹10+ Crore exposure)</option>
                        <option value="BRANCH_UW">BRANCH_UW — Regional Branch Underwriter</option>
                      </select>
                      <small class="helper-text">Your delegated underwriting authorization tier authorizing this commercial risk limit.</small>
                    </label>
                    <label>Decision Rationale *
                      <textarea rows="3" [value]="decisionReason" (input)="decisionReason = readValue($event)" placeholder="Record the underwriting analysis, appetite assessment, and pricing rationale..."></textarea>
                      <small class="helper-text">Document key risk characteristics, hazard controls, and rate justification.</small>
                    </label>
                    <label>Binding Conditions / Subjectivities (optional)
                      <textarea rows="2" [value]="decisionConditions" (input)="decisionConditions = readValue($event)" placeholder="e.g. Verified fire suppression inspection certificate required prior to policy binding..."></textarea>
                      <small class="helper-text">Conditions that must be met before policy issuance can proceed.</small>
                    </label>
                    <button type="button" class="primary-button" [disabled]="actionLoading || !canSubmitDecision()" (click)="submitDecision()">
                      {{ actionLoading === 'decision' ? 'Saving decision…' : 'Record decision' }}
                    </button>
                  </div>

                  <div class="review-section terms-section">
                    <h3>Offered Policy Terms</h3>
                    <p class="helper">Publish binding commercial terms to the applicant. Values below are loaded from the quote application and can be adjusted per underwriting guidelines.</p>
                    <label>Quote Terms Expiration Date *
                      <input type="datetime-local" [value]="quoteExpiresAt" (input)="quoteExpiresAt = readValue($event)" />
                      <small class="helper-text">Date and time until which these guaranteed premium rates and terms remain open for acceptance.</small>
                    </label>
                    <div class="coverage-forms">
                      @for (coverage of offeredCoverages; track coverage.coverageCode; let index = $index) {
                        <div class="coverage-form">
                          <strong>{{ coverage.coverageName }} <small>{{ coverage.coverageCode }}</small></strong>
                          <div class="field-grid">
                            <label>Offered Limit (₹)<input type="number" min="0.01" [value]="coverage.offeredLimit" (input)="updateCoverage(index, 'offeredLimit', $event)" /><small class="helper-text">Max indemnity payable</small></label>
                            <label>Deductible (₹)<input type="number" min="0" [value]="coverage.offeredDeductible" (input)="updateCoverage(index, 'offeredDeductible', $event)" /><small class="helper-text">First-party retention</small></label>
                            <label>Annual Premium (₹)<input type="number" min="0.01" [value]="coverage.coveragePremium" (input)="updateCoverage(index, 'coveragePremium', $event)" /><small class="helper-text">Calculated premium charge</small></label>
                          </div>
                          <label>Conditions / Endorsements<input [value]="coverage.conditions || ''" (input)="updateCoverage(index, 'conditions', $event)" placeholder="e.g. Mandatory sprinkler warranty" /></label>
                          <label>Specific Exclusions<input [value]="coverage.exclusions || ''" (input)="updateCoverage(index, 'exclusions', $event)" placeholder="e.g. Excludes flood and earth movement" /></label>
                        </div>
                      }
                    </div>
                    <button type="button" class="primary-button" [disabled]="actionLoading || !canOfferTerms()" (click)="offerTerms()">
                      {{ actionLoading === 'terms' ? 'Publishing terms…' : 'Offer quote terms' }}
                    </button>
                  </div>
                </div>
                }
                } @else {
                  <div class="read-only-note" role="status">
                    <strong>Risk review access</strong>
                    <p>This assessment and quote are available for risk-engineering review. Underwriting decisions and commercial terms can only be published by the assigned underwriter.</p>
                  </div>
                }
                @if (actionMessage) { <div class="success-message" role="status">{{ actionMessage }}</div> }
                @if (actionError) { <div class="inline-state error" role="alert">{{ actionError }}</div> }
              }
            </section>
          }

          @if (state.assignedQuotes.length) {
            <section class="table-card" aria-labelledby="quotes-heading">
              <div class="table-heading"><div><p class="eyebrow">Assigned Work</p><h2 id="quotes-heading">Assigned commercial quotes</h2></div></div>
              <div class="table-wrap">
                <table>
                  <thead><tr><th>Quote #</th><th>Product</th><th>Status</th><th>Total Premium</th><th>Updated</th><th>Action</th></tr></thead>
                  <tbody>
                    @for (quote of state.assignedQuotes; track quote.quoteId) {
                      <tr>
                        <td><strong>{{ quote.quoteNumber }}</strong><small>{{ quote.quoteId }}</small></td>
                        <td>{{ quote.productCode }}</td>
                        <td><span class="status" [ngClass]="statusClass(quote.status)">{{ formatStatus(quote.status) }}</span></td>
                        <td>{{ quote.totalPremium ? ('₹' + (quote.totalPremium | number:'1.2-2')) : 'Pending calculation' }}</td>
                        <td>{{ quote.updatedAt ? (quote.updatedAt | date:'mediumDate') : '—' }}</td>
                        <td>
                          <button type="button" class="review-link" (click)="selectQuote(quote)">Review</button>
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            </section>
          }

          @if (state.items.length) {
            <section class="table-card" aria-labelledby="queue-heading">
              <div class="table-heading"><div><p class="eyebrow">Live records</p><h2 id="queue-heading">Assigned assessments</h2></div></div>
              <div class="table-wrap">
                <table>
                  <thead><tr><th>Assessment</th><th>Location</th><th>Status</th><th>Risk band</th><th>Updated</th><th>Action</th></tr></thead>
                  <tbody>
                    @for (assessment of state.items; track assessment.assessmentId) {
                      <tr>
                        <td><strong>{{ assessment.assessmentNumber || assessment.assessmentId }}</strong><small>{{ assessment.assessmentType || 'Risk assessment' }}</small></td>
                        <td>{{ assessment.location || '—' }}</td>
                        <td><span class="status" [ngClass]="statusClass(assessment.status)">{{ formatStatus(assessment.status) }}</span></td>
                        <td>{{ assessment.riskBand || 'Not scored' }}</td>
                        <td>{{ assessment.updatedAt ? (assessment.updatedAt | date:'mediumDate') : '—' }}</td>
                        <td>
                          <button type="button" class="review-link" (click)="selectAssessment(assessment)">Review</button>
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            </section>
          }
        }
      }
    </section>
  `,
  styles: [`
    :host { display: block; padding: 24px; }
    .page-shell { max-width: 1540px; margin: 0 auto; display: grid; gap: 20px; }
    .page-header { display: flex; align-items: end; justify-content: space-between; gap: 20px; }
    .eyebrow { margin: 0 0 7px; color: var(--claret); font-size: 9px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; }
    h1, h2 { margin: 0; color: var(--ink); letter-spacing: -.04em; }
    h1 { font-size: clamp(25px, 2.5vw, 34px); }
    h2 { font-size: 18px; }
    .description { margin: 9px 0 0; color: var(--muted); font-size: 12px; }
    .secondary-link { color: var(--claret); font-size: 12px; font-weight: 700; text-decoration: none; }
    .summary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
    .summary-grid article, .table-card, .state-card { border: 1px solid var(--border); border-radius: 9px; background: var(--surface); box-shadow: var(--shadow); }
    .summary-grid article { padding: 18px; display: grid; gap: 12px; }
    .summary-grid span { color: var(--muted); font-size: 11px; }
    .summary-grid strong { color: var(--ink); font-size: 26px; }
    .table-card { overflow: hidden; }
    .table-heading { padding: 18px 20px; border-bottom: 1px solid var(--border); }
    .table-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; min-width: 720px; }
    th, td { padding: 14px 20px; text-align: left; border-bottom: 1px solid var(--border); font-size: 12px; color: var(--ink); }
    th { color: var(--muted); font-size: 10px; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; background: var(--surface-alt, #faf8f7); }
    tr:last-child td { border-bottom: 0; }
    td strong, td small { display: block; }
    td small { margin-top: 4px; color: var(--muted); font-size: 10px; }
    .status { display: inline-flex; padding: 5px 8px; border-radius: 999px; font-size: 10px; font-weight: 700; }
    .status-review { color: #875b00; background: #fff4d6; }
    .status-active { color: #1d6270; background: #e2f5f6; }
    .status-complete { color: #28704e; background: #e7f5ed; }
    .status-neutral { color: #5f5a5d; background: #f0eded; }
    .state-card { padding: 30px; color: var(--muted); }
    .state-card strong { color: var(--ink); }
    .state-card p { margin: 8px 0 0; font-size: 12px; }
    .state-card.error { border-color: #e8bcbc; }
    .state-card button { margin-top: 16px; padding: 9px 13px; border: 0; border-radius: 6px; color: white; background: var(--claret); cursor: pointer; }
    .review-card { border: 1px solid var(--border); border-radius: 9px; background: var(--surface); box-shadow: var(--shadow); padding: 20px; }
    .review-heading { display: flex; align-items: start; justify-content: space-between; gap: 16px; padding-bottom: 16px; border-bottom: 1px solid var(--border); }
    .review-meta, .helper { margin: 7px 0 0; color: var(--muted); font-size: 11px; }
    .review-grid { display: grid; grid-template-columns: minmax(0, .85fr) minmax(0, 1.15fr); gap: 24px; padding-top: 18px; }
    .review-section { display: grid; align-content: start; gap: 12px; }
    .review-section h3 { margin: 0; color: var(--ink); font-size: 15px; }
    label { display: grid; gap: 6px; color: var(--muted); font-size: 10px; font-weight: 700; letter-spacing: .04em; text-transform: uppercase; }
    input, select, textarea { width: 100%; box-sizing: border-box; border: 1px solid var(--border); border-radius: 6px; padding: 9px 10px; color: var(--ink); background: var(--surface); font: inherit; font-size: 12px; text-transform: none; letter-spacing: normal; }
    textarea { resize: vertical; }
    .primary-button, .secondary-button { border: 0; border-radius: 6px; padding: 10px 13px; font-size: 11px; font-weight: 700; cursor: pointer; }
    .primary-button { color: #fff; background: var(--claret); }
    .primary-button:disabled { opacity: .5; cursor: not-allowed; }
    .secondary-button { color: var(--claret); background: var(--surface-alt, #faf8f7); border: 1px solid var(--border); }
    .review-link { display: block; margin-top: 7px; border: 0; padding: 0; color: var(--claret); background: transparent; cursor: pointer; font-size: 10px; font-weight: 700; }
    .coverage-forms { display: grid; gap: 10px; }
    .coverage-form { display: grid; gap: 10px; padding: 12px; border: 1px solid var(--border); border-radius: 7px; background: var(--surface-alt, #faf8f7); }
    .coverage-form strong { color: var(--ink); font-size: 12px; }
    .coverage-form small { margin-left: 5px; color: var(--muted); font-weight: 400; }
    .field-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; }
    .inline-state { padding: 14px; color: var(--muted); font-size: 12px; }
    .inline-state.error { color: #9c2d2d; background: #fff7f7; border-radius: 6px; }
    .findings-panel { margin-top: 18px; padding-top: 18px; border-top: 1px solid var(--border); }
    .findings-panel-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
    .findings-panel-header h3 { margin: 0; color: var(--ink); font-size: 15px; }
    .empty-panel-note { font-size: 11px; color: var(--muted); padding: 12px; background: var(--surface-alt, #faf8f7); border-radius: 6px; border: 1px dashed var(--border); margin-bottom: 14px; }
    .badge-severity { display: inline-block; padding: 2px 7px; border-radius: 999px; font-size: 10px; font-weight: 700; text-transform: uppercase; }
    .sev-low { color: #28704e; background: #e7f5ed; }
    .sev-medium { color: #875b00; background: #fff4d6; }
    .sev-high { color: #b45309; background: #ffedd5; }
    .sev-critical { color: #9c2d2d; background: #fee2e2; }
    .control-status { font-size: 10px; font-weight: 600; color: var(--muted); }
    .recommendations-box { margin-top: 14px; }
    .recommendations-box h4 { margin: 0 0 10px; font-size: 13px; color: var(--ink); }
    .recs-list { display: grid; gap: 8px; }
    .rec-card { border: 1px solid var(--border); border-radius: 7px; background: var(--surface-alt, #faf8f7); padding: 10px 14px; }
    .rec-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
    .badge-priority { margin-left: 6px; font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px; }
    .prio-low { background: #e7f5ed; color: #28704e; }
    .prio-medium { background: #fff4d6; color: #875b00; }
    .prio-high { background: #ffedd5; color: #b45309; }
    .prio-critical { background: #fee2e2; color: #9c2d2d; }
    .badge-bind-req { margin-left: 6px; font-size: 9px; font-weight: 700; background: #fee2e2; color: #991b1b; padding: 2px 5px; border-radius: 3px; text-transform: uppercase; }
    .rec-desc { margin: 4px 0 2px; font-size: 11px; color: var(--ink); line-height: 1.4; }
    .rec-target { font-size: 10px; color: var(--muted); }
    .rec-status-wrap { display: flex; align-items: center; gap: 6px; }
    .action-btn-sm { padding: 3px 8px; border: 1px solid var(--border); border-radius: 4px; background: #fff; color: var(--claret); font-size: 10px; font-weight: 700; cursor: pointer; }
    .engineer-workbench { margin-top: 16px; padding: 14px; border: 1px solid var(--border); border-radius: 8px; background: #fff; }
    .workbench-tabs { display: flex; gap: 8px; margin-bottom: 12px; }
    .tab-btn { border: 1px solid var(--border); border-radius: 5px; padding: 6px 12px; font-size: 11px; font-weight: 600; cursor: pointer; background: var(--surface-alt, #faf8f7); color: var(--muted); }
    .tab-btn.active { background: var(--claret); color: #fff; border-color: var(--claret); }
    .workbench-form { display: grid; gap: 10px; }
    .workbench-form h4 { margin: 0; font-size: 13px; color: var(--ink); }
    .form-row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
    .checkbox-label { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 11px; text-transform: none; font-weight: 600; color: var(--ink); }
    .checkbox-label input { width: auto; }
    @media (max-width: 760px) { .page-header { align-items: start; flex-direction: column; } .summary-grid { grid-template-columns: 1fr; } }
    @media (max-width: 920px) { .review-grid { grid-template-columns: 1fr; } .field-grid { grid-template-columns: 1fr; } }
  `],
})

export class UnderwritingDashboardComponent {
  private readonly store = inject(Store);
  private readonly underwriting = inject(UnderwritingService);
  private readonly quotes = inject(QuoteService);
  private readonly apiService = inject(ApiService);
  private readonly reloadTrigger = new Subject<void>();

  selectedAssessment: RiskAssessment | null = null;
  reviewQuote: QuoteResponse | null = null;
  offeredCoverages: OfferedCoverageForm[] = [];
  reviewLoading = false;
  reviewError: string | null = null;
  actionLoading: 'decision' | 'terms' | 'bind' | 'assign-engineer' | false = false;
  actionMessage: string | null = null;
  actionError: string | null = null;

  engineerName: string | null = null;
  availableEngineers: any[] = [];
  selectedEngineerId = '';
  customerProfile: any = null;
  decision: UnderwritingDecisionType = 'APPROVED';
  decisionReason = '';
  authorityLevel = 'STANDARD_UW';
  decisionConditions = '';
  quoteExpiresAt = '';
  userRole: string | null = null;
  currentUserId: string | null = null;

  findings: RiskFinding[] = [];
  recommendations: RiskRecommendation[] = [];
  findingsLoading = false;
  activeFormTab: 'finding' | 'rec' | 'none' = 'none';

  newFindingType = 'PROPERTY_HAZARD';
  newFindingDesc = '';
  newFindingSeverity: FindingSeverity = 'MEDIUM';
  newFindingControl: ControlStatus = 'ADEQUATE';
  findingSubmitting = false;

  newRecType = 'LOSS_PREVENTION';
  newRecDesc = '';
  newRecPriority: RecommendationPriority = 'MEDIUM';
  newRecRequiredBeforeBind = true;
  newRecTargetDate = '';
  recSubmitting = false;

  currentAssessmentId: string | null = null;

  readonly queue$ = this.reloadTrigger.pipe(
    startWith(void 0),
    switchMap(() => this.store.select(selectUserId).pipe(take(1))),
    switchMap((userId): Observable<UnderwritingQueueState> => {
      if (!userId) {
        return of({ loading: false, error: 'Your signed-in user identity is unavailable.', items: [], assignedQuotes: [] });
      }
      this.currentUserId = userId;
      return this.store.select(selectUserRole).pipe(
        take(1),
        switchMap((role) => {
          this.userRole = role;
          const normRole = (role ?? '').toUpperCase().replace(/^ROLE_/, '');
          const assessments$ = this.underwriting.getAssignedQueue(userId, normRole).pipe(
            catchError(() => of([] as RiskAssessment[]))
          );
          const isStaff = ['UNDERWRITER', 'RISK_ENGINEER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'].includes(normRole);
          if (isStaff) {
            this.loadAvailableEngineers();
          }
          const quotes$ = isStaff
            ? this.quotes.getAllQuotesForAdministration().pipe(
                catchError(() => of([] as QuoteResponse[]))
              )
            : of([] as QuoteResponse[]);

          return forkJoin({
            items: assessments$,
            assignedQuotes: quotes$,
          }).pipe(
            map(({ items, assignedQuotes }) => ({
              loading: false,
              error: null,
              items,
              assignedQuotes,
            }))
          );
        })
      );
    }),
    catchError((error: HttpErrorResponse) => of({ loading: false, error: this.errorMessage(error), items: [], assignedQuotes: [] })),
    startWith({ loading: true, error: null, items: [] as RiskAssessment[], assignedQuotes: [] as QuoteResponse[] })
  );

  reload(): void { this.reloadTrigger.next(); }

  get canManageQuote(): boolean {
    const norm = (this.userRole ?? '').toUpperCase().replace(/^ROLE_/, '');
    return ['UNDERWRITER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'].includes(norm);
  }

  get isRiskEngineer(): boolean {
    const norm = (this.userRole ?? '').toUpperCase().replace(/^ROLE_/, '');
    return norm === 'RISK_ENGINEER';
  }

  selectQuote(quote: QuoteResponse): void {
    this.selectedAssessment = null;
    this.engineerName = null;
    this.customerProfile = null;
    this.reviewQuote = quote;
    this.offeredCoverages = (quote.coverages || []).map((coverage) => ({
      coverageCode: coverage.coverageCode,
      coverageName: coverage.coverageName,
      offeredLimit: coverage.offeredLimit ?? coverage.requestedLimit,
      offeredDeductible: coverage.offeredDeductible ?? coverage.requestedDeductible,
      coveragePremium: coverage.coveragePremium ?? 0,
      conditions: coverage.conditions ?? '',
      exclusions: coverage.exclusions ?? '',
      waitingPeriodDays: coverage.waitingPeriodDays ?? 0,
    }));
    this.reviewLoading = false;
    this.reviewError = null;
    this.actionMessage = null;
    this.actionError = null;

    if (quote.customerId) {
      this.apiService.get<any>(`/api/customers/${quote.customerId}`).subscribe({
        next: (cust) => { this.customerProfile = cust; },
        error: () => { this.customerProfile = null; }
      });
    }

    if (quote.riskAssessmentId) {
      this.currentAssessmentId = quote.riskAssessmentId;
      this.underwriting.getAssessmentById(quote.riskAssessmentId).subscribe({
        next: (assessment) => {
          this.selectedAssessment = assessment;
          this.resolveEngineerName(assessment.assignedRiskEngineerId);
        },
        error: () => {},
      });
      this.loadFindingsAndRecommendations(quote.riskAssessmentId);
    } else {
      this.underwriting.getAssessmentByQuoteId(quote.quoteId).subscribe({
        next: (assessment) => {
          if (assessment && assessment.assessmentId) {
            this.currentAssessmentId = assessment.assessmentId;
            this.selectedAssessment = assessment;
            this.resolveEngineerName(assessment.assignedRiskEngineerId);
            this.loadFindingsAndRecommendations(assessment.assessmentId);
          } else {
            this.findings = [];
            this.recommendations = [];
            this.currentAssessmentId = null;
            this.engineerName = null;
          }
        },
        error: () => {
          this.findings = [];
          this.recommendations = [];
          this.currentAssessmentId = null;
          this.engineerName = null;
        },
      });
    }
  }

  countQuotesByStatus(quotes: QuoteResponse[], status: string): number {
    return quotes.filter((q) => q.status === status).length;
  }

  selectAssessment(assessment: RiskAssessment): void {
    this.selectedAssessment = assessment;
    this.resolveEngineerName(assessment.assignedRiskEngineerId);
    this.reviewQuote = null;
    this.offeredCoverages = [];
    this.reviewLoading = true;
    this.reviewError = null;
    this.actionMessage = null;
    this.actionError = null;
    this.loadFindingsAndRecommendations(assessment.assessmentId);
    this.quotes.getQuoteById(assessment.quoteId).subscribe({
      next: (quote) => {
        this.reviewQuote = quote;
        this.customerProfile = null;
        if (quote.customerId) {
          this.apiService.get<any>(`/api/customers/${quote.customerId}`).subscribe({
            next: (cust) => { this.customerProfile = cust; },
            error: () => { this.customerProfile = null; }
          });
        }
        this.offeredCoverages = quote.coverages.map((coverage) => ({
          coverageCode: coverage.coverageCode,
          coverageName: coverage.coverageName,
          offeredLimit: coverage.offeredLimit ?? coverage.requestedLimit,
          offeredDeductible: coverage.offeredDeductible ?? coverage.requestedDeductible,
          coveragePremium: coverage.coveragePremium ?? 0,
          conditions: coverage.conditions ?? '',
          exclusions: coverage.exclusions ?? '',
          waitingPeriodDays: coverage.waitingPeriodDays ?? 0,
        }));
        this.reviewLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.reviewLoading = false;
        this.reviewError = this.errorMessage(error);
      },
    });
  }

  loadFindingsAndRecommendations(assessmentId: string): void {
    this.currentAssessmentId = assessmentId;
    this.findingsLoading = true;
    forkJoin({
      findings: this.underwriting.getFindings(assessmentId).pipe(catchError(() => of([] as RiskFinding[]))),
      recommendations: this.underwriting.getRecommendations(assessmentId).pipe(catchError(() => of([] as RiskRecommendation[]))),
    }).subscribe({
      next: ({ findings, recommendations }) => {
        this.findings = findings;
        this.recommendations = recommendations;
        this.findingsLoading = false;
      },
      error: () => {
        this.findingsLoading = false;
      },
    });
  }

  submitFinding(): void {
    const assessmentId = this.selectedAssessment?.assessmentId || this.reviewQuote?.riskAssessmentId || this.currentAssessmentId;
    if (!assessmentId || !this.currentUserId || !this.newFindingDesc.trim()) return;
    this.findingSubmitting = true;
    this.underwriting.createFinding(assessmentId, {
      findingType: this.newFindingType,
      description: this.newFindingDesc.trim(),
      severity: this.newFindingSeverity,
      controlStatus: this.newFindingControl,
      createdBy: this.currentUserId,
    }).subscribe({
      next: (created) => {
        this.findings = [created, ...this.findings];
        this.findingSubmitting = false;
        this.newFindingDesc = '';
        this.actionMessage = 'Risk finding recorded successfully.';
      },
      error: (err: HttpErrorResponse) => {
        this.findingSubmitting = false;
        this.actionError = err?.error?.message || err?.message || 'Failed to record risk finding.';
      },
    });
  }

  submitRecommendation(): void {
    const assessmentId = this.selectedAssessment?.assessmentId || this.reviewQuote?.riskAssessmentId || this.currentAssessmentId;
    if (!assessmentId || !this.currentUserId || !this.newRecDesc.trim()) return;
    this.recSubmitting = true;
    this.underwriting.createRecommendation(assessmentId, {
      recommendationType: this.newRecType,
      description: this.newRecDesc.trim(),
      priority: this.newRecPriority,
      requiredBeforeBind: this.newRecRequiredBeforeBind,
      targetDate: this.newRecTargetDate || undefined,
      createdBy: this.currentUserId,
    }).subscribe({
      next: (created) => {
        this.recommendations = [created, ...this.recommendations];
        this.recSubmitting = false;
        this.newRecDesc = '';
        this.actionMessage = 'Loss control recommendation recorded successfully.';
      },
      error: (err: HttpErrorResponse) => {
        this.recSubmitting = false;
        this.actionError = err?.error?.message || err?.message || 'Failed to record recommendation.';
      },
    });
  }

  updateRecommendationStatus(recId: string, status: string): void {
    this.underwriting.updateRecommendationStatus(recId, status).subscribe({
      next: (updated) => {
        this.recommendations = this.recommendations.map((r) => r.recommendationId === recId ? updated : r);
        this.actionMessage = `Recommendation status updated to ${status}.`;
      },
      error: (err: HttpErrorResponse) => {
        this.actionError = err?.error?.message || err?.message || 'Failed to update recommendation status.';
      },
    });
  }

  setFindingSeverity(val: string): void { this.newFindingSeverity = val as FindingSeverity; }
  setFindingControl(val: string): void { this.newFindingControl = val as ControlStatus; }
  setRecPriority(val: string): void { this.newRecPriority = val as RecommendationPriority; }
  readChecked(event: Event): boolean { return (event.target as HTMLInputElement).checked; }

  clearReview(): void {
    this.selectedAssessment = null;
    this.reviewQuote = null;
    this.customerProfile = null;
    this.offeredCoverages = [];
    this.findings = [];
    this.recommendations = [];
    this.currentAssessmentId = null;
    this.activeFormTab = 'none';
    this.actionLoading = false;
    this.actionMessage = null;
    this.actionError = null;
  }

  submitDecision(): void {
    if (!this.reviewQuote || !this.canSubmitDecision()) return;
    const request: RecordUnderwritingDecisionRequest = {
      decision: this.decision,
      decisionReason: this.decisionReason.trim(),
      authorityLevel: this.authorityLevel.trim(),
      conditions: this.decisionConditions.trim() || undefined,
    };
    this.actionLoading = 'decision';
    this.actionError = null;
    this.quotes.recordUnderwritingDecision(this.reviewQuote.quoteId, request).subscribe({
      next: () => {
        this.actionLoading = false;
        this.actionMessage = 'Decision recorded. Confirm the final commercial terms before publishing an offer.';
        this.refreshReviewQuote();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoading = false;
        this.actionError = this.errorMessage(error);
      },
    });
  }

  offerTerms(): void {
    if (!this.reviewQuote || !this.canOfferTerms()) return;
    const request: OfferQuoteTermsRequest = {
      quoteExpiresAt: this.quoteExpiresAt,
      coverages: this.offeredCoverages.map(({ coverageName, ...coverage }) => coverage),
    };
    this.actionLoading = 'terms';
    this.actionError = null;
    this.quotes.offerQuoteTerms(this.reviewQuote.quoteId, request).subscribe({
      next: (quote) => {
        this.actionLoading = false;
        this.reviewQuote = quote;
        this.actionMessage = 'Quote terms published. The policyholder can now review and accept the offer.';
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoading = false;
        this.actionError = this.errorMessage(error);
      },
    });
  }

  bindPolicy(): void {
    if (!this.reviewQuote) return;
    this.actionLoading = 'bind';
    this.actionError = null;
    this.quotes.bindQuote(this.reviewQuote.quoteId).subscribe({
      next: (policy) => {
        this.actionLoading = false;
        this.actionMessage = `Policy ${policy.policyNumber} bound successfully! Coverage is now active.`;
        this.refreshReviewQuote();
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoading = false;
        this.actionError = this.errorMessage(error);
      },
    });
  }

  canSubmitDecision(): boolean {
    return !!this.reviewQuote && !!this.decisionReason.trim() && !!this.authorityLevel.trim() && this.reviewQuote.status === 'IN_REVIEW';
  }

  canOfferTerms(): boolean {
    return !!this.reviewQuote && this.reviewQuote.status === 'IN_REVIEW' && !!this.quoteExpiresAt && this.offeredCoverages.length > 0
      && this.offeredCoverages.every((coverage) => coverage.offeredLimit > 0 && coverage.offeredDeductible >= 0 && coverage.coveragePremium > 0);
  }

  updateCoverage(index: number, field: keyof OfferedCoverageRequest, event: Event): void {
    const value = this.readValue(event);
    const coverage = this.offeredCoverages[index];
    if (!coverage) return;
    if (field === 'offeredLimit' || field === 'offeredDeductible' || field === 'coveragePremium' || field === 'waitingPeriodDays') {
      coverage[field] = Number(value);
    } else {
      coverage[field] = value;
    }
  }

  readValue(event: Event): string { return (event.target as HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement).value; }

  setDecision(value: string): void { this.decision = value as UnderwritingDecisionType; }

  private refreshReviewQuote(): void {
    if (!this.reviewQuote) return;
    this.quotes.getQuoteById(this.reviewQuote.quoteId).subscribe({
      next: (quote) => { this.reviewQuote = quote; },
      error: () => { /* The successful mutation remains visible; the next reload will reconcile the quote. */ },
    });
  }

  countByStatus(items: RiskAssessment[], status: string): number {
    return items.filter((item) => item.status === status).length;
  }

  formatStatus(status: string): string { return status.replaceAll('_', ' ').toLowerCase().replace(/^./, (value) => value.toUpperCase()); }

  statusClass(status: string): string {
    if (status === 'UNDER_REVIEW' || status === 'REFERRED' || status === 'NEEDS_INFORMATION') return 'status-review';
    if (status === 'COMPLETED') return 'status-complete';
    if (status === 'IN_PROGRESS' || status === 'RISK_ENGINEERING_REQUIRED') return 'status-active';
    return 'status-neutral';
  }

  private errorMessage(error: HttpErrorResponse): string {
    if (error.status === 401) return 'Please sign in again to view your assigned assessments.';
    if (error.status === 403) return 'Your role or assignment does not allow access to this queue.';
    return error.error?.message || 'The underwriting service did not return the assigned queue.';
  }

  loadAvailableEngineers(): void {
    this.underwriting.getAvailableRiskEngineers().subscribe({
      next: (list) => { this.availableEngineers = list || []; },
      error: () => { this.availableEngineers = []; }
    });
  }

  resolveEngineerName(engineerId?: string | null): void {
    if (!engineerId) {
      this.engineerName = null;
      return;
    }
    this.apiService.get<any>(`/api/users/${engineerId}`).subscribe({
      next: (u) => {
        this.engineerName = u?.displayName || u?.fullName || u?.email || engineerId;
      },
      error: () => {
        this.engineerName = engineerId;
      }
    });
  }

  assignEngineer(): void {
    const assessmentId = this.selectedAssessment?.assessmentId || this.currentAssessmentId;
    if (!assessmentId) return;

    this.actionLoading = 'assign-engineer';
    this.actionError = null;
    this.actionMessage = null;

    const req$ = this.selectedEngineerId
      ? this.underwriting.assignRiskEngineer(assessmentId, this.selectedEngineerId)
      : this.underwriting.autoAssignRiskEngineer(assessmentId);

    req$.subscribe({
      next: (updated) => {
        this.actionLoading = false;
        this.actionMessage = 'Risk Engineer assigned successfully.';
        this.selectedAssessment = updated;
        this.resolveEngineerName(updated.assignedRiskEngineerId);
      },
      error: (err) => {
        this.actionLoading = false;
        this.actionError = err?.error?.message || err?.message || 'Failed to assign risk engineer.';
      }
    });
  }
}
