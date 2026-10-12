import { AsyncPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { ButtonComponent, ModalComponent } from 'ui-core';
import { selectUserRole } from '../../core/store/auth/auth.selectors';

interface RoleGuideData {
  id: string;
  name: string;
  badge: string;
  tagline: string;
  objective: string;
  keyResponsibilities: string[];
  workflowSteps: Array<{ stepNumber: number; title: string; detail: string; tip?: string }>;
  fieldReference?: Array<{ field: string; meaning: string; allowedValuesOrGuidance: string }>;
  guardrails: string[];
}

@Component({
  selector: 'is-platform-guide',
  standalone: true,
  imports: [AsyncPipe, RouterLink, ModalComponent, ButtonComponent],
  template: `
    <main class="guide-page">
      <header class="guide-header">
        <div class="header-top">
          <a routerLink="/" class="back-link">← IntelliSure home</a>
          <button type="button" class="runbook-header-btn" (click)="showDemoModal = true">
            ★ Evaluation Demo Runbook
          </button>
        </div>
        <p class="eyebrow">Enterprise Operations Manual</p>
        <h1>Role Guides & Lifecycle SOP</h1>
        <p class="guide-lead">
          Comprehensive, step-by-step operational standard operating procedures for each role across the IntelliSure
          commercial insurance and continuity platform.
        </p>

        <!-- Evaluation Presentation Banner -->
        <div class="runbook-callout-card">
          <div class="callout-content">
            <span class="callout-badge">Evaluation Presentation Manual</span>
            <h3>One Complete Insurance Lifecycle (End-to-End Golden Path)</h3>
            <p>
              Presenting to the evaluation committee? Review the official 17-step master demonstration script
              covering policyholder onboarding, risk engineering inspection, custom quote rating, policy binding,
              FNOL filing, adjuster reserve investigation, manager payout disbursement, and network vendor restoration.
            </p>
          </div>
          <button type="button" class="btn-launch-runbook" (click)="showDemoModal = true">
            Launch Evaluation Runbook &rarr;
          </button>
        </div>
      </header>

      <!-- Role Selector Tabs -->
      <nav class="role-tabs-bar" aria-label="Role Guides Navigation">
        @for (r of allRoleGuides; track r.id) {
          <button
            type="button"
            class="role-tab-btn"
            [class.active]="selectedRoleId === r.id"
            (click)="selectRole(r.id)"
          >
            <span class="role-tab-badge">{{ r.badge }}</span>
            <span class="role-tab-name">{{ r.name }}</span>
          </button>
        }
      </nav>

      <!-- Active Role Detailed SOP -->
      @if (activeGuide; as g) {
        <article class="role-guide-container">
          <header class="guide-hero-card">
            <div class="hero-meta">
              <span class="role-tag">{{ g.badge }}</span>
              <h2>{{ g.name }} Standard Operating Procedure</h2>
              <p class="hero-tagline">{{ g.tagline }}</p>
            </div>
            <div class="objective-box">
              <strong>Core Mission Objective</strong>
              <p>{{ g.objective }}</p>
            </div>
          </header>

          <div class="guide-grid-two">
            <!-- Left: Step-by-Step Operating Workflow -->
            <section class="guide-col workflow-col">
              <h3 class="col-title">Step-by-Step Operating Workflow</h3>
              <div class="workflow-steps-list">
                @for (step of g.workflowSteps; track step.stepNumber) {
                  <div class="step-card">
                    <div class="step-num-bubble">{{ step.stepNumber }}</div>
                    <div class="step-content">
                      <h4>{{ step.title }}</h4>
                      <p>{{ step.detail }}</p>
                      @if (step.tip) {
                        <div class="step-tip-box">
                          <strong>💡 Pro-Tip / Policy Note:</strong> {{ step.tip }}
                        </div>
                      }
                    </div>
                  </div>
                }
              </div>
            </section>

            <!-- Right: Responsibilities, Field Definitions & Guardrails -->
            <section class="guide-col reference-col">
              <!-- Key Responsibilities -->
              <div class="card-box">
                <h3 class="box-title">Key Responsibilities</h3>
                <ul class="styled-list">
                  @for (resp of g.keyResponsibilities; track resp) {
                    <li>{{ resp }}</li>
                  }
                </ul>
              </div>

              <!-- Field Reference (if applicable) -->
              @if (g.fieldReference && g.fieldReference.length > 0) {
                <div class="card-box fields-box">
                  <h3 class="box-title">Field Glossary & Parameter Explanations</h3>
                  <div class="field-table-wrapper">
                    <table class="field-table">
                      <thead>
                        <tr>
                          <th>Field Name</th>
                          <th>Operational Meaning</th>
                          <th>Standard Values / Guidance</th>
                        </tr>
                      </thead>
                      <tbody>
                        @for (f of g.fieldReference; track f.field) {
                          <tr>
                            <td class="field-name-cell"><code>{{ f.field }}</code></td>
                            <td>{{ f.meaning }}</td>
                            <td><span class="field-guidance">{{ f.allowedValuesOrGuidance }}</span></td>
                          </tr>
                        }
                      </tbody>
                    </table>
                  </div>
                </div>
              }

              <!-- Operational Guardrails -->
              <div class="card-box guardrails-box">
                <h3 class="box-title">Security & Role Guardrails</h3>
                <ul class="styled-list guardrail-list">
                  @for (guard of g.guardrails; track guard) {
                    <li>{{ guard }}</li>
                  }
                </ul>
              </div>
            </section>
          </div>
        </article>
      }

      <!-- Master Demo Runbook Modal -->
      @if (showDemoModal) {
        <is-modal
          [open]="showDemoModal"
          title="IntelliSure Evaluation Demo Runbook"
          description="Complete 17-Step Golden Path presentation script across all 8 roles & 1 continuous insurance cycle"
          [size]="'xl'"
          (close)="showDemoModal = false"
        >
          <div class="runbook-modal-body">
            <!-- Universal Credentials Grid -->
            <div class="runbook-section">
              <h4 class="section-title">1. Master Credentials Directory (Password for all: <code>Password&#64;123</code>)</h4>
              <div class="credentials-table-box">
                <table class="runbook-table">
                  <thead>
                    <tr>
                      <th>Role</th>
                      <th>Email Login</th>
                      <th>Platform Responsibility</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr><td><strong>Policyholder</strong></td><td><em>Register fresh in Step 1</em></td><td>Applies for quote, binds policy, reports FNOL, selects vendor</td></tr>
                    <tr><td><strong>Risk Engineer</strong></td><td><code>riskengineer1&#64;intellisure.com</code></td><td>Site safety inspection, risk scoring (0-100), hazard recommendations</td></tr>
                    <tr><td><strong>Underwriter</strong></td><td><code>underwriter1&#64;intellisure.com</code></td><td>Exposure analysis, customized terms (INR), quote issuance</td></tr>
                    <tr><td><strong>Claims Adjuster</strong></td><td><code>claimsadjuster1&#64;intellisure.com</code></td><td>FNOL investigation, linked policy audit, reserves, coverage decision</td></tr>
                    <tr><td><strong>Claims Manager</strong></td><td><code>claimsmanager1&#64;intellisure.com</code></td><td>Dual-control governance, payout disbursement (NEFT), file closure</td></tr>
                    <tr><td><strong>Network Vendor</strong></td><td><code>vendor1&#64;intellisure.com</code></td><td>Emergency contractor; accepts job, reports repair progress</td></tr>
                    <tr><td><strong>Administrator</strong></td><td><code>admin1&#64;intellisure.com</code></td><td>Desk workload reassignment, role auditing, system health</td></tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Golden Path 17-Step Script -->
            <div class="runbook-section">
              <h4 class="section-title">2. Golden Path Presentation Sequence (17 Steps)</h4>
              <div class="steps-flow">
                <div class="demo-step-row">
                  <span class="step-num">Step 1</span>
                  <div class="step-detail">
                    <strong>Policyholder Registration & Login</strong>
                    <p>Go to <code>/login</code> &rarr; Create account: <code>balaji&#64;acmefabrication.com</code> / <code>Password&#64;123</code>.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 2</span>
                  <div class="step-detail">
                    <strong>Establish Business Customer Profile</strong>
                    <p>Go to <code>/profile</code> &rarr; Enter <em>Acme Precision Fabrication Works Pvt Ltd</em>, CIN/GST, Peenya Industrial Area, ₹45,00,000 turnover &rarr; Save.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 3</span>
                  <div class="step-detail">
                    <strong>Submit Commercial Quote Proposal</strong>
                    <p>Go to <code>/quotes/new</code> &rarr; Choose <em>Business Owner's Policy (BOP)</em> &rarr; Select Building, Contents, and Interruption &rarr; Submit Application.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 4</span>
                  <div class="step-detail">
                    <strong>Risk Engineer Site Inspection & Recommendations</strong>
                    <p>Log in as <code>riskengineer1&#64;intellisure.com</code> (Incognito) &rarr; Open quote &rarr; Record Risk Score 78 (Band B) & fire extinguisher recommendation &rarr; Complete.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 5</span>
                  <div class="step-detail">
                    <strong>Underwriter Terms Rating & Quote Offer</strong>
                    <p>Log in as <code>underwriter1&#64;intellisure.com</code> &rarr; Review inspection &rarr; Offer terms with ₹45,500 annual premium &rarr; Issue Quote Offer.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 6</span>
                  <div class="step-detail">
                    <strong>(Optional) Admin Desk Reassignment</strong>
                    <p>Log in as <code>admin1&#64;intellisure.com</code> &rarr; Go to <code>/quotes</code> &rarr; Click Reassign on pending quote &rarr; Choose alternate underwriter.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 7</span>
                  <div class="step-detail">
                    <strong>Policyholder Quote Review & Acceptance</strong>
                    <p>Switch back to Policyholder &rarr; Open quote &rarr; Note sidebar notification &rarr; Click <em>Accept & Bind Policy</em>.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 8</span>
                  <div class="step-detail">
                    <strong>Active Policy Contract & Document Repository in /docs</strong>
                    <p>Open <code>/docs</code> &rarr; Point out auto-generated <em>Policy Declaration (POL-XXXXXX)</em>, <em>Quote Proposal</em>, and <em>Risk Assessment</em> &rarr; Click <em>Print PDF</em>.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 9</span>
                  <div class="step-detail">
                    <strong>First Notice of Loss (FNOL) Incident Reporting</strong>
                    <p>Go to <code>/claims</code> &rarr; File Claim &rarr; Select active policy &rarr; Report hydraulic pipe fire damage in Bay 2 (Est: ₹3,50,000) &rarr; Submit.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 10</span>
                  <div class="step-detail">
                    <strong>Adjuster Investigation & Reserve Allocation</strong>
                    <p>Log in as <code>claimsadjuster1&#64;intellisure.com</code> &rarr; Open claim &rarr; Point out Linked Policy card &rarr; Set initial reserve of ₹3,00,000.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 11</span>
                  <div class="step-detail">
                    <strong>Adjuster Formal Coverage Decision (APPROVED)</strong>
                    <p>On claim detail &rarr; Select Decision <em>APPROVED</em> &rarr; Apply ₹25,000 deductible on BPP &rarr; Net payout: ₹2,95,000 &rarr; Record Decision.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 12</span>
                  <div class="step-detail">
                    <strong>Claims Manager Financial Governance & Disbursement</strong>
                    <p>Log in as <code>claimsmanager1&#64;intellisure.com</code> &rarr; Open claim &rarr; Authorize ₹2,95,000 payout via NEFT-SBI-2026-981240 &rarr; Status becomes SETTLED.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 13</span>
                  <div class="step-detail">
                    <strong>Policyholder Recovery Path & Vendor Selection</strong>
                    <p>Switch to Policyholder &rarr; Go to <code>/recovery</code> &rarr; Choose <em>NETWORK_VENDOR</em> &rarr; Pick <em>Apex Property Restoration</em> (₹85,000) &rarr; Dispatch.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 14</span>
                  <div class="step-detail">
                    <strong>Network Vendor Accepts Assignment & Logs Milestones</strong>
                    <p>Log in as <code>vendor1&#64;intellisure.com</code> &rarr; Accept assignment &rarr; Update status to In Progress with cleanup crew mobilization.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 15</span>
                  <div class="step-detail">
                    <strong>Omnichannel Chat with 10s Unread Badge Polling</strong>
                    <p>Go to <code>/chat</code> &rarr; Send update message between Policyholder & Employee &rarr; Point out red unread dot on sidebar without page reload.</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 16</span>
                  <div class="step-detail">
                    <strong>Review Chronological Stacked Audit Notifications</strong>
                    <p>Go to <code>/notifications</code> &rarr; Showcase stacked timeline of all events (QUOTE, POLICY, CLAIM, SETTLEMENT, RECOVERY).</p>
                  </div>
                </div>
                <div class="demo-step-row">
                  <span class="step-num">Step 17</span>
                  <div class="step-detail">
                    <strong>Formal Claim Closure & Final Document Audit</strong>
                    <p>As Claims Manager or Adjuster &rarr; Click <em>Close Claim File</em> &rarr; Confirm status changes to CLOSED &rarr; Review final audit records in <code>/docs</code>.</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="runbook-footer-note">
              <p>📄 Complete detailed presentation guide is saved in repository: <code>Docs/INTELLISURE_EVALUATION_DEMO_RUNBOOK.md</code></p>
            </div>
          </div>
        </is-modal>
      }
    </main>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background: var(--warm-light, #f7f5f3);
      color: var(--ink, #000);
      font-family: inherit;
    }
    .guide-page {
      max-width: 1240px;
      margin: 0 auto;
      padding: 36px 24px 80px;
    }
    .guide-header {
      margin-bottom: 28px;
    }
    .back-link {
      color: var(--claret, #75013f);
      font-size: 12px;
      font-weight: 700;
      text-decoration: none;
    }
    .eyebrow {
      margin: 18px 0 6px;
      color: var(--claret, #75013f);
      font-size: 10px;
      font-weight: 700;
      letter-spacing: .14em;
      text-transform: uppercase;
    }
    h1 {
      margin: 0;
      font-size: clamp(28px, 4vw, 42px);
      letter-spacing: -.04em;
      line-height: 1.15;
    }
    .guide-lead {
      max-width: 760px;
      margin: 10px 0 0;
      color: var(--muted, #6f6a6d);
      font-size: 14px;
      line-height: 1.6;
    }

    /* Tabs */
    .role-tabs-bar {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      padding-bottom: 20px;
      margin-bottom: 24px;
      border-bottom: 1px solid var(--border, #eae5df);
    }
    .role-tab-btn {
      display: inline-flex;
      align-items: center;
      gap: 7px;
      padding: 8px 14px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 99px;
      background: #fff;
      color: #3b3639;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      transition: all .15s ease;
    }
    .role-tab-btn:hover {
      border-color: #cbbfc7;
      background: #fdfcfa;
    }
    .role-tab-btn.active {
      border-color: var(--claret, #75013f);
      background: var(--claret, #75013f);
      color: #fff;
      box-shadow: 0 2px 8px rgba(117, 1, 63, .25);
    }
    .role-tab-badge {
      font-size: 10px;
      padding: 1px 6px;
      border-radius: 99px;
      background: #eee9e5;
      color: #524d50;
      font-weight: 700;
      text-transform: uppercase;
    }
    .role-tab-btn.active .role-tab-badge {
      background: rgba(255, 255, 255, .25);
      color: #fff;
    }

    /* Role Guide Container */
    .role-guide-container {
      display: grid;
      gap: 24px;
    }
    .guide-hero-card {
      display: grid;
      grid-template-columns: 1fr 340px;
      gap: 24px;
      padding: 24px 28px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 12px;
      background: #fff;
      box-shadow: 0 4px 16px rgba(0,0,0,.03);
    }
    .role-tag {
      display: inline-block;
      margin-bottom: 8px;
      padding: 3px 10px;
      border-radius: 99px;
      background: #fde8ed;
      color: var(--claret, #75013f);
      font-size: 10px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: .08em;
    }
    .guide-hero-card h2 {
      margin: 0 0 6px;
      font-size: 24px;
      letter-spacing: -.03em;
    }
    .hero-tagline {
      margin: 0;
      color: var(--muted, #6f6a6d);
      font-size: 13px;
    }
    .objective-box {
      padding: 14px 16px;
      border-radius: 8px;
      background: #faf8f6;
      border-left: 3px solid var(--claret, #75013f);
    }
    .objective-box strong {
      display: block;
      font-size: 11px;
      text-transform: uppercase;
      letter-spacing: .06em;
      color: var(--claret, #75013f);
      margin-bottom: 4px;
    }
    .objective-box p {
      margin: 0;
      font-size: 12px;
      color: #4a4447;
      line-height: 1.5;
    }

    /* Two column grid */
    .guide-grid-two {
      display: grid;
      grid-template-columns: 1.15fr .85fr;
      gap: 24px;
      align-items: start;
    }
    .guide-col {
      display: grid;
      gap: 18px;
    }
    .col-title {
      margin: 0 0 4px;
      font-size: 17px;
      letter-spacing: -.02em;
      color: var(--ink, #000);
    }

    /* Workflow Steps */
    .workflow-steps-list {
      display: grid;
      gap: 14px;
    }
    .step-card {
      display: flex;
      align-items: flex-start;
      gap: 14px;
      padding: 16px 18px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 10px;
      background: #fff;
    }
    .step-num-bubble {
      display: grid;
      place-items: center;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: var(--claret-soft, #fde8ed);
      color: var(--claret, #75013f);
      font-size: 12px;
      font-weight: 700;
      flex-shrink: 0;
    }
    .step-content {
      flex: 1;
      min-width: 0;
    }
    .step-content h4 {
      margin: 0 0 4px;
      font-size: 13px;
      font-weight: 700;
      color: var(--ink, #000);
    }
    .step-content p {
      margin: 0;
      color: #4b4548;
      font-size: 12px;
      line-height: 1.6;
    }
    .step-tip-box {
      margin-top: 8px;
      padding: 8px 12px;
      border-radius: 6px;
      background: #eff6ff;
      border-left: 3px solid #3b82f6;
      color: #1e40af;
      font-size: 11px;
      line-height: 1.5;
    }

    /* Reference Cards */
    .card-box {
      padding: 18px 20px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 10px;
      background: #fff;
    }
    .box-title {
      margin: 0 0 12px;
      font-size: 14px;
      font-weight: 700;
      letter-spacing: -.02em;
    }
    .styled-list {
      margin: 0;
      padding-left: 18px;
      color: #413b3e;
      font-size: 12px;
      line-height: 1.7;
    }
    .guardrail-list li {
      color: #7f1d1d;
    }
    .guardrails-box {
      border-color: #fecaca;
      background: #fef2f2;
    }
    .guardrails-box .box-title {
      color: #991b1b;
    }

    /* Field table */
    .field-table-wrapper {
      overflow-x: auto;
    }
    .field-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 11px;
      text-align: left;
    }
    .field-table th {
      padding: 8px 10px;
      background: #f9f8f6;
      border-bottom: 1px solid var(--border, #eae5df);
      font-weight: 700;
      color: #524d50;
      font-size: 10px;
      text-transform: uppercase;
      letter-spacing: .06em;
    }
    .field-table td {
      padding: 9px 10px;
      border-bottom: 1px solid #f0ece9;
      color: #3b3639;
      vertical-align: top;
      line-height: 1.45;
    }
    .field-name-cell code {
      font-family: ui-monospace, SFMono-Regular, monospace;
      color: var(--claret, #75013f);
      background: #fdf2f4;
      padding: 1px 4px;
      border-radius: 3px;
      font-size: 11px;
    }
    .field-guidance {
      font-weight: 600;
      color: #047857;
    }

    /* Evaluation Presentation Callout Banner */
    .header-top {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
    }
    .runbook-header-btn {
      background: var(--claret-soft, #fde8ed);
      color: var(--claret, #75013f);
      border: 1px solid var(--claret, #75013f);
      padding: 6px 14px;
      border-radius: 99px;
      font-size: 11px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.2s;
    }
    .runbook-header-btn:hover {
      background: var(--claret, #75013f);
      color: #fff;
    }
    .runbook-callout-card {
      margin-top: 20px;
      background: linear-gradient(135deg, #2b0416 0%, #4a0627 100%);
      border: 1px solid rgba(255, 255, 255, 0.15);
      border-radius: 12px;
      padding: 22px 26px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 24px;
      color: #fff;
      box-shadow: 0 4px 20px rgba(74, 6, 39, 0.2);
    }
    .callout-badge {
      display: inline-block;
      font-size: 10px;
      font-weight: 800;
      letter-spacing: 0.1em;
      text-transform: uppercase;
      background: rgba(255, 255, 255, 0.2);
      color: #ffd8e4;
      padding: 3px 8px;
      border-radius: 4px;
      margin-bottom: 8px;
    }
    .callout-content h3 {
      margin: 0 0 6px;
      font-size: 17px;
      color: #fff;
    }
    .callout-content p {
      margin: 0;
      font-size: 12.5px;
      line-height: 1.5;
      color: #f7dbe4;
      max-width: 800px;
    }
    .btn-launch-runbook {
      background: #fff;
      color: var(--claret, #75013f);
      border: none;
      padding: 10px 18px;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 700;
      white-space: nowrap;
      cursor: pointer;
      transition: all 0.2s;
    }
    .btn-launch-runbook:hover {
      background: #fdf2f4;
      transform: translateY(-1px);
    }

    /* Modal Interior Styles */
    .runbook-modal-body {
      display: flex;
      flex-direction: column;
      gap: 24px;
      padding: 10px 0;
    }
    .section-title {
      margin: 0 0 12px;
      font-size: 14px;
      color: var(--ink, #000);
      font-weight: 700;
    }
    .credentials-table-box {
      border: 1px solid var(--border, #eae5df);
      border-radius: 8px;
      overflow: hidden;
    }
    .runbook-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 12px;
    }
    .runbook-table th {
      background: var(--warm-light, #f7f5f3);
      padding: 9px 12px;
      text-align: left;
      font-size: 10.5px;
      text-transform: uppercase;
      color: var(--muted, #6f6a6d);
      border-bottom: 1px solid var(--border, #eae5df);
    }
    .runbook-table td {
      padding: 10px 12px;
      border-bottom: 1px solid #f0ece9;
    }
    .runbook-table code {
      font-family: ui-monospace, monospace;
      color: var(--claret, #75013f);
      background: #fdf2f4;
      padding: 2px 5px;
      border-radius: 4px;
      font-size: 11px;
    }
    .steps-flow {
      display: grid;
      gap: 10px;
    }
    .demo-step-row {
      display: flex;
      align-items: flex-start;
      gap: 14px;
      padding: 12px 14px;
      background: #fff;
      border: 1px solid var(--border, #eae5df);
      border-radius: 8px;
    }
    .step-num {
      background: var(--claret, #75013f);
      color: #fff;
      font-size: 10px;
      font-weight: 800;
      padding: 4px 8px;
      border-radius: 4px;
      white-space: nowrap;
      text-transform: uppercase;
    }
    .step-detail strong {
      font-size: 12.5px;
      color: var(--ink, #000);
      display: block;
      margin-bottom: 3px;
    }
    .step-detail p {
      margin: 0;
      font-size: 11.5px;
      color: var(--muted, #6f6a6d);
      line-height: 1.45;
    }
    .step-detail code {
      font-family: ui-monospace, monospace;
      color: var(--claret, #75013f);
    }
    .runbook-footer-note {
      background: var(--warm-light, #f7f5f3);
      padding: 12px 16px;
      border-radius: 8px;
      font-size: 12px;
      color: var(--muted, #6f6a6d);
    }
    .runbook-footer-note p { margin: 0; }

    @media (max-width: 960px) {
      .guide-hero-card {
        grid-template-columns: 1fr;
      }
      .guide-grid-two {
        grid-template-columns: 1fr;
      }
      .runbook-callout-card {
        flex-direction: column;
        align-items: flex-start;
      }
    }
  `],
})
export class PlatformGuideComponent implements OnInit {
  private readonly store = inject(Store);
  readonly role$ = this.store.select(selectUserRole);

  selectedRoleId = 'UNDERWRITER';
  showDemoModal = false;

  readonly allRoleGuides: RoleGuideData[] = [
    {
      id: 'UNDERWRITER',
      name: 'Underwriter',
      badge: 'Underwriting',
      tagline: 'Risk selection, terms definition, and commercial policy quote offering.',
      objective:
        'Evaluate incoming commercial quote submissions, assess risk findings and engineering scorecards, enforce authority limits, and formulate binding terms and conditions.',
      keyResponsibilities: [
        'Review quotes queued in the Underwriting workspace (/underwriting).',
        'Inspect automated risk engineering scores, findings, and subjectivities.',
        'Record underwriting decision outcomes (APPROVE, REFER, DECLINE, REQUEST_INFO).',
        'Specify commercial terms: offered limit, deductible, gross premium, expiration window, and conditions.',
        'Issue formal quotes to policyholders and monitor quote acceptance.',
        'Authorize policy binding and issuance once all pre-bind subjectivities are fulfilled.'
      ],
      workflowSteps: [
        {
          stepNumber: 1,
          title: 'Access the Underwriting Work Queue',
          detail: 'Sign in and navigate to /underwriting in the sidebar. The queue lists submitted quotes awaiting underwriter review, showing requested coverages, product lines, and submission timestamps.',
          tip: 'Quotes with urgent requested effective dates or high risk scores are flagged for priority attention.'
        },
        {
          stepNumber: 2,
          title: 'Select Quote & Inspect Risk Engineering Scorecard',
          detail: 'Click on a submitted quote. The detail pane loads the business operations, insurance need, and the linked Risk Engineering Assessment (risk score 0-100, hazard findings, and recommendations).',
          tip: 'Even if the initial quote payload has no pre-linked risk assessment ID, the workspace automatically retrieves the latest assessment by quote ID.'
        },
        {
          stepNumber: 3,
          title: 'Select Underwriting Authority Level',
          detail: 'Select your authorized underwriting level from the dropdown. Authority levels determine your maximum approval limits under underwriting governance.',
          tip: 'STANDARD_UW ($1M limit), SENIOR_UW ($5M limit), CHIEF_UW (over $5M), or BRANCH_UW (regional standard).'
        },
        {
          stepNumber: 4,
          title: 'Formulate Commercial Terms & Expiration',
          detail: 'Input Offered Limit ($), Offered Deductible ($), Offered Annual Premium ($), and Expiration Days (default 30 calendar days). Attach any specific exclusions or warranty conditions.',
          tip: 'Offered limit must not exceed your selected authority tier. Deductibles cannot exceed the offered limit.'
        },
        {
          stepNumber: 5,
          title: 'Submit Offer Terms to Policyholder',
          detail: 'Click "Offer Quote Terms". The quote status transitions to QUOTED. The policyholder receives an automated notification and can review and accept terms.',
          tip: 'Once the policyholder accepts, the quote transitions to ACCEPTED and is eligible for binding into an active policy.'
        }
      ],
      fieldReference: [
        {
          field: 'Authority Level',
          meaning: 'Governance authority tier assigned to the underwriter evaluating the account.',
          allowedValuesOrGuidance: 'STANDARD_UW (≤$1M), SENIOR_UW (≤$5M), CHIEF_UW (>$5M), BRANCH_UW'
        },
        {
          field: 'Offered Limit',
          meaning: 'Maximum total coverage amount insurer agrees to pay under the policy schedule.',
          allowedValuesOrGuidance: 'Must be positive numeric value within authority limit tier.'
        },
        {
          field: 'Offered Deductible',
          meaning: 'Policyholder retention amount that must be paid before insurance coverage applies.',
          allowedValuesOrGuidance: 'Typically 1%–5% of limit ($2,500 – $50,000 baseline).'
        },
        {
          field: 'Offered Premium',
          meaning: 'Gross annual premium invoiced for the specified coverage and risk profile.',
          allowedValuesOrGuidance: 'Calculated using base rate, exposures, and risk score credits/debits.'
        },
        {
          field: 'Expiration Days',
          meaning: 'Number of calendar days the quote remains valid for policyholder acceptance.',
          allowedValuesOrGuidance: 'Defaults to 30 days (valid range: 7 – 90 days).'
        },
        {
          field: 'Conditions / Exclusions',
          meaning: 'Binding endorsements, protective safeguard warranties, and operational limits.',
          allowedValuesOrGuidance: 'Clear textual statements (e.g., "Sprinkler maintenance warranty required").'
        }
      ],
      guardrails: [
        'Never offer coverage limits that exceed your delegated underwriting authority level.',
        'Do not bind policies if mandatory pre-bind subjectivities remain unsatisfied.',
        'Never modify policyholder business profile data; return the quote for revision if disclosures are inaccurate.',
        'Do not file FNOL or claim payments on behalf of policyholders from the underwriting workspace.'
      ]
    },
    {
      id: 'POLICYHOLDER',
      name: 'Business Policyholder',
      badge: 'Customer',
      tagline: 'Maintain business profile, request coverage, accept terms, file claims, and select recovery vendors.',
      objective:
        'Manage commercial property and business interruption insurance portfolio, submit accurate operational profiles, review offered quotes, file First Notice of Loss (FNOL), and direct business continuity restoration.',
      keyResponsibilities: [
        'Maintain an active, verified Business Customer Profile (/profile).',
        'Request commercial insurance quotes with coverage limits and operation details.',
        'Review underwriter-offered quotes and formally accept terms.',
        'Access active policies and download declarations documents (/policy).',
        'File First Notice of Loss (FNOL) immediately upon property damage or business interruption.',
        'Choose business recovery path (NETWORK_VENDOR, CUSTOMER_VENDOR, CUSTOMER_MANAGED).',
        'Select accredited network restoration vendors with pre-negotiated commercial rates.'
      ],
      workflowSteps: [
        {
          stepNumber: 1,
          title: 'Complete Business Profile',
          detail: 'Navigate to /profile. Enter legal business name, owner name, business entity type, address, contact details, and operational description. Save profile before requesting quotes.',
          tip: 'An active business customer profile is strictly required by the gateway before quote generation.'
        },
        {
          stepNumber: 2,
          title: 'Request a Commercial Quote',
          detail: 'Navigate to /quotes/new. Select product line (Property, General Liability, Business Interruption), describe operations, specify requested limits and deductibles, and submit for review.',
          tip: 'Provide detailed business operations to help risk engineers generate favorable safety ratings.'
        },
        {
          stepNumber: 3,
          title: 'Review Terms and Accept Quote',
          detail: 'Once the underwriter responds, review offered limit, deductible, and premium in /quotes/:id. If terms are agreeable, click "Accept Quote". Policy issuance begins.',
          tip: 'Review all attached conditions and exclusions carefully before accepting.'
        },
        {
          stepNumber: 4,
          title: 'File FNOL (First Notice of Loss)',
          detail: 'When an incident occurs, navigate to /claims/new. Select the active eligible policy covering the loss date, describe damage, date of loss, estimated amount, and submit.',
          tip: 'You must select an eligible active policy to advance past step 1 of the FNOL filing wizard.'
        },
        {
          stepNumber: 5,
          title: 'Designate Recovery Path & Select Vendor',
          detail: 'Open /recovery to track business continuity. Select your preferred recovery path. If selecting "Network Vendor", browse pre-approved accredited vendors with negotiated rates and confirm your selection.',
          tip: 'Choosing an accredited network vendor ensures direct insurer billing and prioritized 24/7 emergency dispatch.'
        }
      ],
      fieldReference: [
        {
          field: 'Recovery Path',
          meaning: 'Method by which property and continuity restoration will be managed.',
          allowedValuesOrGuidance: 'NETWORK_VENDOR (Insurer-vetted partner), CUSTOMER_VENDOR, CUSTOMER_MANAGED'
        },
        {
          field: 'FNOL Severity',
          meaning: 'Estimated impact of the loss event on ongoing business operations.',
          allowedValuesOrGuidance: 'LOW (Minor cosmetic), MEDIUM, HIGH, CRITICAL (Total shutdown)'
        }
      ],
      guardrails: [
        'Only the policyholder can select or alter the recovery path for active business continuity cases.',
        'FNOL filing is restricted to policies that are in ACTIVE status on the loss date.',
        'Do not share login credentials; use individual customer account authentication.'
      ]
    },
    {
      id: 'RISK_ENGINEER',
      name: 'Risk Engineer',
      badge: 'Risk Analysis',
      tagline: 'Site surveys, vulnerability evaluations, automated scorecards, and mitigation recommendations.',
      objective:
        'Conduct physical and operational risk surveys, evaluate building construction, fire protection, and flood exposures, generate risk scorecards, and propose loss-prevention subjectivities.',
      keyResponsibilities: [
        'Review risk assessment queues linked to submitted quotes.',
        'Perform operational hazard analyses and calculate aggregate risk scores (0-100).',
        'Record structured risk findings across Construction, Occupancy, Protection, and Exposure (COPE).',
        'Publish actionable loss-mitigation recommendations for underwriters and policyholders.',
        'Define pre-bind subjectivities (e.g. fire suppression certification, electrical compliance).'
      ],
      workflowSteps: [
        {
          stepNumber: 1,
          title: 'Receive Risk Survey Request',
          detail: 'Assessments are generated automatically when quotes are submitted. Access the survey in /underwriting or /analytics.',
          tip: 'High-hazard industry categories trigger mandatory detailed engineering evaluations.'
        },
        {
          stepNumber: 2,
          title: 'Analyze COPE Factors',
          detail: 'Evaluate Construction type (fire-resistive vs joisted masonry), Occupancy hazards, Protection systems (sprinklers, alarms), and External exposures.',
          tip: 'Input verifiable metrics such as sprinkler pressure, distance to fire hydrants, and flood elevation.'
        },
        {
          stepNumber: 3,
          title: 'Publish Risk Findings & Scorecard',
          detail: 'Document high, medium, and low severity findings. Assign risk score from 1 (severe exposure) to 100 (exemplary protection).',
          tip: 'Scores above 75 qualify the insured for favorable underwriter pricing credits.'
        },
        {
          stepNumber: 4,
          title: 'Establish Pre-Bind Subjectivities',
          detail: 'Specify mandatory conditions that the policyholder must satisfy before policy binding (e.g., annual fire sprinkler test report).',
          tip: 'Subjectivities prevent the underwriter from binding the policy until evidence is uploaded and verified.'
        }
      ],
      guardrails: [
        'Risk engineers provide risk context and recommendations; they do not make commercial underwriting decisions.',
        'Never alter coverage limits or premium calculations directly.',
        'Ensure all risk findings are substantiated by photos, inspection reports, or verifiable public data.'
      ]
    },
    {
      id: 'CLAIMS_ADJUSTER',
      name: 'Claims Adjuster',
      badge: 'Claims',
      tagline: 'Investigation, reserve allocation, coverage validation, and loss settlement.',
      objective:
        'Investigate First Notice of Loss (FNOL) filings, confirm active coverage, estimate initial loss reserves, coordinate recovery assistance, and prepare settlement payouts.',
      keyResponsibilities: [
        'Investigate newly filed claims in /claims.',
        'Verify loss date against policy coverage periods and active policy endorsements.',
        'Establish and adjust loss reserves (indemnity and expense reserves).',
        'Coordinate evidence documents, photos, invoices, and police/fire reports.',
        'Initiate continuity recovery cases and track repair progress.',
        'Issue payment requests within assigned adjuster settlement authority limits.'
      ],
      workflowSteps: [
        {
          stepNumber: 1,
          title: 'Review Assigned FNOL',
          detail: 'Open /claims. Filter by newly filed claims. Review claimant statement, incident date, damage category, and initial severity rating.',
          tip: 'Contact claimant within 24 business hours of FNOL submission to acknowledge receipt.'
        },
        {
          stepNumber: 2,
          title: 'Coverage Determination',
          detail: 'Cross-reference incident details with policy terms in /policy/:id. Verify that policy was active on the date of loss and that the cause of loss is not excluded.',
          tip: 'Document coverage determination notes in the claim audit timeline.'
        },
        {
          stepNumber: 3,
          title: 'Post Initial Reserve',
          detail: 'Set indemnity reserve based on estimated scope of damage, repair estimates, and applicable deductible.',
          tip: 'Adjusters can post reserves up to $50,000 without manager escalation.'
        },
        {
          stepNumber: 4,
          title: 'Monitor Recovery Continuity',
          detail: 'Inspect the linked recovery case in /recovery. Confirm policyholder recovery path designation and track restoration percentage updates.',
          tip: 'Do not override the policyholder recovery path without explicit written agreement.'
        },
        {
          stepNumber: 5,
          title: 'Process Claim Payout',
          detail: 'Review verified vendor invoices or policyholder repair receipts, deduct policy retention, and execute claim payment.',
          tip: 'Payouts exceeding adjuster authority require Claims Manager co-signature.'
        }
      ],
      guardrails: [
        'Never deny a claim without documenting the exact policy exclusion clause.',
        'Do not exceed individual financial settlement authority limits.',
        'Keep claim files updated with chronological audit notes for regulatory compliance.'
      ]
    },
    {
      id: 'CLAIMS_MANAGER',
      name: 'Claims Manager',
      badge: 'Management',
      tagline: 'Portfolio oversight, reserve approvals, litigation management, and payout authorization.',
      objective:
        'Supervise claims department operations, approve high-dollar reserves (>$50,000), authorize large loss settlements, monitor loss ratios, and ensure compliance with regulatory fair claims settlement practices.',
      keyResponsibilities: [
        'Review and approve reserve adjustments and settlements exceeding adjuster authority.',
        'Audit claim handling timelines and First Notice of Loss response metrics.',
        'Manage contested coverage determinations and potential litigation files.',
        'Analyze loss trends and loss ratio analytics in /analytics.',
        'Oversee vendor assignment distribution and quality performance.'
      ],
      workflowSteps: [
        {
          stepNumber: 1,
          title: 'Review Management Queue',
          detail: 'Monitor claims flagged with high severity, high financial reserve requests, or complex multi-party liability.',
          tip: 'Prioritize claims where reserve recommendations exceed $50,000.'
        },
        {
          stepNumber: 2,
          title: 'Authorize High-Value Reserves & Payouts',
          detail: 'Examine adjuster investigation reports, expert witness statements, and vendor estimates. Execute formal management approval.',
          tip: 'Review vendor estimate line items against local industry market rate standards.'
        },
        {
          stepNumber: 3,
          title: 'Audit Recovery Case Resolution',
          detail: 'Verify that business continuity recovery cases are completed promptly to minimize business interruption loss payments.',
          tip: 'Track restoration milestones to minimize additional living or operating expense costs.'
        }
      ],
      guardrails: [
        'Enforce dual-authorization on all settlement disbursements over $100,000.',
        'Ensure regulatory compliance with state and federal prompt claim payment statutes.'
      ]
    },
    {
      id: 'VENDOR_MANAGER',
      name: 'Vendor Manager',
      badge: 'Partnerships',
      tagline: 'Network partner recruitment, onboarding verification, compliance audits, and performance tracking.',
      objective:
        'Vetting commercial restoration, remediation, and equipment vendors, approving network onboarding applications, managing service areas, and tracking partner SLA ratings.',
      keyResponsibilities: [
        'Review vendor onboarding applications in /vendor.',
        'Verify professional licenses, business insurance coverage, and industry certifications.',
        'Approve or reject partner onboarding applications with specific criteria.',
        'Manage active vendor catalog, pre-negotiated commercial rates, and service specializations.',
        'Evaluate partner performance scorecards based on on-time delivery and customer satisfaction.'
      ],
      workflowSteps: [
        {
          stepNumber: 1,
          title: 'Process Pending Onboarding Applications',
          detail: 'Access /vendor onboarding queue. Review legal entity name, trade licenses, general liability coverage limits, and workers compensation proof.',
          tip: 'Verify that applicant maintains minimum $2,000,000 commercial liability coverage.'
        },
        {
          stepNumber: 2,
          title: 'Verify & Activate Partner',
          detail: 'Validate contact phone and emergency response hotline. Click "Verify Vendor" to transition status to VERIFIED and ACTIVE.',
          tip: 'Once verified, the vendor automatically appears in the accredited policyholder recovery catalog.'
        },
        {
          stepNumber: 3,
          title: 'Monitor Performance & SLA Ratings',
          detail: 'Audit completed assignments, review cycle times, customer feedback, and invoice accuracy. Record periodic performance reviews.',
          tip: 'Vendors with ratings below 3.5 stars are suspended from emergency dispatch queues.'
        }
      ],
      guardrails: [
        'Never activate a vendor without verifying active proof of insurance.',
        'Do not dispatch unverified or suspended vendors to customer recovery cases.'
      ]
    },
    {
      id: 'VENDOR',
      name: 'Network Vendor / Service Provider',
      badge: 'Partner',
      tagline: 'Emergency restoration, site stabilization, equipment provisioning, and progress reporting.',
      objective:
        'Deliver prompt restoration, environmental cleanup, and emergency repairs to policyholders, submit milestone progress reports, upload photographic evidence, and fulfill insurance recovery assignments.',
      keyResponsibilities: [
        'Submit comprehensive onboarding application with service types and capabilities.',
        'Acknowledge and accept assigned recovery dispatches promptly.',
        'Provide initial on-site response within contract SLA windows (e.g. 2–4 hours for emergency stabilization).',
        'Record restoration progress percentage and upload completion evidence.',
        'Submit final itemized invoice and mark assignment completed.'
      ],
      workflowSteps: [
        {
          stepNumber: 1,
          title: 'Complete Vendor Profile Onboarding',
          detail: 'Register as Vendor Applicant. Enter legal name, display name, trade categories, emergency dispatch phone, and service zip codes.',
          tip: 'Clearly list equipment capabilities (e.g., thermal imaging, dessicant dryers) to qualify for high-tier dispatch.'
        },
        {
          stepNumber: 2,
          title: 'Receive & Accept Assignment',
          detail: 'When a claim or recovery case requires network service, an assignment appears in your vendor workspace. Click "Accept Assignment" and enter expected start date.',
          tip: 'Prompt acceptance within 60 minutes boosts your partner performance score.'
        },
        {
          stepNumber: 3,
          title: 'Deploy Crew & Report Restoration Progress',
          detail: 'Mobilize emergency response team. Update the progress percentage as milestones are achieved (e.g. 25% water extracted, 75% dried, 100% rebuild complete).',
          tip: 'Accurate progress notes keep the claims adjuster informed and expedite payment authorization.'
        },
        {
          stepNumber: 4,
          title: 'Upload Evidence & Complete Assignment',
          detail: 'Upload moisture mapping readings, before/after photographs, and signed customer satisfaction certificates. Click "Mark Completed".',
          tip: 'Ensure all supporting documents are attached to prevent billing audit delays.'
        }
      ],
      guardrails: [
        'Do not undertake structural work beyond the approved assignment scope without adjuster pre-authorization.',
        'Comply strictly with OSHA and local safety regulations on all customer job sites.'
      ]
    },
    {
      id: 'SYSTEM_ADMINISTRATOR',
      name: 'System Administrator',
      badge: 'Administration',
      tagline: 'Identity provisioning, role assignments, audit log review, and platform governance.',
      objective:
        'Manage enterprise user accounts, assign role permissions, audit security changes, ensure cryptographic password integrity, and oversee gateway microservice health.',
      keyResponsibilities: [
        'Manage employee user accounts and credentials in /admin.',
        'Assign and update role privileges (Underwriter, Claims Adjuster, Vendor Manager, etc.).',
        'Review security role audit trails and user access events.',
        'Monitor API gateway routes, Eureka registry status, and reactive service health.',
        'Ensure separation of duties across underwriting, claims, and financial authorization.'
      ],
      workflowSteps: [
        {
          stepNumber: 1,
          title: 'Access Enterprise Administration',
          detail: 'Sign in with administrative credentials and navigate to /admin. The workspace provides user directory, role assignment, and system governance consoles.',
          tip: 'All administrative actions are permanently logged in the document and audit microservice.'
        },
        {
          stepNumber: 2,
          title: 'Manage User Accounts & Roles',
          detail: 'Search users by email or display name. Update account status (ACTIVE, SUSPENDED) or assign delegated roles from the approved roles catalog.',
          tip: 'Enforce principle of least privilege; assign only the exact roles required for the employee operational duties.'
        },
        {
          stepNumber: 3,
          title: 'Audit Role Modifications',
          detail: 'Review the User Role Audit table to inspect timestamped records of who modified roles, old roles, new roles, and reason codes.',
          tip: 'Regular weekly audit reviews ensure compliance with enterprise SOC2 and insurance regulatory standards.'
        }
      ],
      guardrails: [
        'Never bypass authorization filters or disable JWT validation checks.',
        'Admins do not adjudicate insurance claims or offer commercial underwriting terms directly.'
      ]
    }
  ];

  ngOnInit(): void {
    this.role$.subscribe((role) => {
      if (role) {
        const normalized = role.toUpperCase();
        const found = this.allRoleGuides.find((g) => g.id === normalized || (normalized === 'ADMIN' && g.id === 'SYSTEM_ADMINISTRATOR'));
        if (found) {
          this.selectedRoleId = found.id;
        }
      }
    });
  }

  get activeGuide(): RoleGuideData | undefined {
    return this.allRoleGuides.find((g) => g.id === this.selectedRoleId) ?? this.allRoleGuides[0];
  }

  selectRole(roleId: string): void {
    this.selectedRoleId = roleId;
  }
}
