import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'is-intelligence-demo',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="intelligence" class="intelligence-section" aria-labelledby="intelligence-heading">
      <div class="section-header">
        <span class="section-kicker">Analytical Decisioning</span>
        <h2 id="intelligence-heading">Explainable Risk Intelligence</h2>
        <p class="section-subhead">
          Eliminate black-box decisions. IntelliSure pairs transparent actuarial factors with real-time operational telemetry so underwriters, adjusters, and leaders act with clarity.
        </p>
      </div>

      <div class="intelligence-grid">
        <!-- Panel 1: Multi-Factor Scoring Model -->
        <article class="intel-card" aria-labelledby="model-title">
          <div class="card-header">
            <div>
              <span class="card-kicker">Risk Engine Framework</span>
              <h3 id="model-title">Transparent Factor Weighting</h3>
            </div>
            <span class="audit-badge">&check; Audit-Traceable</span>
          </div>
          <p class="card-desc">Every automated triage score is decomposed into explicit rating components for regulatory accountability.</p>
          
          <div class="factor-list">
            <div class="factor-row">
              <div class="factor-info">
                <strong>Hazard & Operational Exposure</strong>
                <span>Location vulnerability, revenue tier, payroll</span>
              </div>
              <div class="factor-bar-wrap">
                <span class="factor-bar" style="width: 35%"></span>
                <span class="factor-val">35%</span>
              </div>
            </div>

            <div class="factor-row">
              <div class="factor-info">
                <strong>Loss & Claims History</strong>
                <span>Prior 36-month frequency and severity</span>
              </div>
              <div class="factor-bar-wrap">
                <span class="factor-bar" style="width: 25%"></span>
                <span class="factor-val">25%</span>
              </div>
            </div>

            <div class="factor-row">
              <div class="factor-info">
                <strong>Operational & Safety Controls</strong>
                <span>Fire suppression, telematics, physical security</span>
              </div>
              <div class="factor-bar-wrap">
                <span class="factor-bar" style="width: 25%"></span>
                <span class="factor-val">25%</span>
              </div>
            </div>

            <div class="factor-row">
              <div class="factor-info">
                <strong>Industry Peer Baseline</strong>
                <span>Regional sector risk benchmarks</span>
              </div>
              <div class="factor-bar-wrap">
                <span class="factor-bar" style="width: 15%"></span>
                <span class="factor-val">15%</span>
              </div>
            </div>
          </div>
        </article>

        <!-- Panel 2: Actuarial Curve & Workflow Signals -->
        <article class="intel-card" aria-labelledby="signal-title">
          <div class="card-header">
            <div>
              <span class="card-kicker">Continuous Monitoring</span>
              <h3 id="signal-title">Operational Loss Surveillance</h3>
            </div>
            <span class="live-badge">Reactive Gateway</span>
          </div>
          <p class="card-desc">Compare loss trajectory benchmarks against active portfolio runoff to preserve target combined ratios.</p>

          <!-- SVG Trend visualization -->
          <div class="chart-container" role="img" aria-label="Portfolio loss development comparison curve">
            <svg class="curve-svg" viewBox="0 0 400 160">
              <!-- Grid lines -->
              <line x1="40" y1="20" x2="380" y2="20" class="chart-grid" />
              <line x1="40" y1="60" x2="380" y2="60" class="chart-grid" />
              <line x1="40" y1="100" x2="380" y2="100" class="chart-grid" />
              <line x1="40" y1="140" x2="380" y2="140" class="chart-axis" />

              <!-- Baseline trend curve -->
              <path
                d="M 40,130 C 120,115 200,90 380,45"
                fill="none"
                class="curve-baseline"
              />

              <!-- Mitigated portfolio curve -->
              <path
                d="M 40,130 C 120,125 220,110 380,85"
                fill="none"
                class="curve-managed"
              />

              <!-- Active pulse indicator on managed curve -->
              <circle cx="380" cy="85" r="5" class="curve-dot" />
            </svg>
            <div class="chart-legend">
              <span class="legend-item"><i class="dot baseline"></i> Industry Unmitigated</span>
              <span class="legend-item"><i class="dot managed"></i> IntelliSure Coordinated</span>
            </div>
          </div>

          <!-- Real-time Decision signals -->
          <div class="signals-feed">
            <div class="signal-item">
              <span class="signal-dot claret"></span>
              <div>
                <strong>Auto-referral threshold verified</strong>
                <span>Submissions &le; 45 risk score cleared for instant quoting</span>
              </div>
            </div>
            <div class="signal-item">
              <span class="signal-dot emerald"></span>
              <div>
                <strong>Vendor assignment dispatched</strong>
                <span>Salvage estimator assigned within 15 minutes of loss report</span>
              </div>
            </div>
          </div>
        </article>
      </div>
    </section>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }

    .intelligence-section {
      max-width: 1380px;
      margin: 0 auto;
      padding: 32px 0;
    }

    .section-header {
      text-align: center;
      max-width: 740px;
      margin: 0 auto 44px;
    }

    .section-kicker {
      display: inline-block;
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.14em;
      text-transform: uppercase;
      color: var(--claret, #75013f);
      background: var(--claret-soft, #fde8ed);
      padding: 4px 10px;
      border-radius: 4px;
      margin-bottom: 10px;
    }

    .section-header h2 {
      font-size: clamp(26px, 3.2vw, 36px);
      font-weight: 700;
      letter-spacing: -0.04em;
      color: var(--ink, #000000);
      margin: 0 0 12px;
      line-height: 1.15;
    }

    .section-subhead {
      font-size: 13px;
      line-height: 1.65;
      color: var(--muted, #6f6a6d);
      margin: 0;
    }

    .intelligence-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 24px;
    }

    .intel-card {
      background: #ffffff;
      border: 1px solid var(--border, #eae5df);
      border-radius: 12px;
      padding: 28px 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
      display: flex;
      flex-direction: column;
    }

    .card-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 6px;
    }

    .card-kicker {
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: var(--claret, #75013f);
    }

    .card-header h3 {
      font-size: 17px;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: var(--ink, #000000);
      margin: 2px 0 0;
    }

    .audit-badge, .live-badge {
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      padding: 4px 8px;
      border-radius: 4px;
    }
    .audit-badge {
      color: #176b45;
      background: #eaf6f0;
    }
    .live-badge {
      color: var(--claret, #75013f);
      background: var(--claret-soft, #fde8ed);
    }

    .card-desc {
      font-size: 11px;
      line-height: 1.55;
      color: var(--muted, #6f6a6d);
      margin: 0 0 20px;
    }

    .factor-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-top: 4px;
    }

    .factor-row {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .factor-info {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      font-size: 11px;
    }
    .factor-info strong {
      color: #242123;
      font-weight: 600;
    }
    .factor-info span {
      color: #7d777b;
      font-size: 10px;
    }

    .factor-bar-wrap {
      display: flex;
      align-items: center;
      gap: 10px;
      background: var(--warm-light, #f7f5f3);
      border-radius: 999px;
      height: 12px;
      padding: 2px 4px;
    }

    .factor-bar {
      height: 6px;
      border-radius: 999px;
      background: linear-gradient(to right, var(--claret, #75013f), var(--fuchsia, #fe3082));
      transition: width 0.4s ease;
    }

    .factor-val {
      font-size: 9px;
      font-weight: 700;
      color: #443e42;
      margin-left: auto;
      padding-right: 4px;
    }

    .chart-container {
      background: var(--warm-light, #f7f5f3);
      border: 1px solid var(--border, #eae5df);
      border-radius: 8px;
      padding: 14px 14px 10px;
      margin-bottom: 20px;
    }

    .curve-svg {
      width: 100%;
      height: 120px;
    }

    .chart-grid {
      stroke: #e4ded9;
      stroke-width: 1;
      stroke-dasharray: 2 4;
    }
    .chart-axis {
      stroke: #d0c8c2;
      stroke-width: 1.5;
    }

    .curve-baseline {
      stroke: #b5acb2;
      stroke-width: 2;
      stroke-dasharray: 4 4;
    }

    .curve-managed {
      stroke: var(--claret, #75013f);
      stroke-width: 2.5;
    }

    .curve-dot {
      fill: var(--fuchsia, #fe3082);
      filter: drop-shadow(0 0 3px rgba(254, 48, 130, 0.6));
    }

    .chart-legend {
      display: flex;
      justify-content: flex-end;
      gap: 16px;
      margin-top: 6px;
      font-size: 9px;
      font-weight: 600;
      color: #635d61;
    }

    .legend-item {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
    }
    .dot.baseline { background: #b5acb2; }
    .dot.managed { background: var(--claret, #75013f); }

    .signals-feed {
      display: flex;
      flex-direction: column;
      gap: 10px;
      border-top: 1px solid #f2eeea;
      padding-top: 16px;
      margin-top: auto;
    }

    .signal-item {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      font-size: 11px;
    }
    .signal-item strong {
      display: block;
      color: #1f1d1e;
      font-weight: 600;
    }
    .signal-item span {
      color: #6d676b;
      font-size: 10px;
    }

    .signal-dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      margin-top: 4px;
      flex-shrink: 0;
    }
    .signal-dot.claret { background: var(--claret, #75013f); }
    .signal-dot.emerald { background: #176b45; }

    @media (max-width: 900px) {
      .intelligence-grid {
        grid-template-columns: 1fr;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .factor-bar {
        transition: none;
      }
    }
  `],
})
export class IntelligenceDemoComponent {}
