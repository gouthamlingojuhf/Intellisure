import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface LifecycleNode {
  step: string;
  name: string;
  role: string;
  summary: string;
  highlight: string;
}

@Component({
  selector: 'is-lifecycle-flow',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="lifecycle" class="lifecycle-section" aria-labelledby="lifecycle-heading">
      <div class="section-header">
        <span class="section-kicker">Unified Operating Model</span>
        <h2 id="lifecycle-heading">From Risk to Recovery</h2>
        <p class="section-subhead">
          Commercial insurance has historically operated in isolated silos. IntelliSure connects every milestone—from exposure profiling through physical business restoration—into one accountable data pipeline.
        </p>
      </div>

      <div class="lifecycle-track-wrapper">
        <!-- Connecting SVG Pipeline (Desktop) -->
        <svg class="pipeline-svg" viewBox="0 0 1000 60" preserveAspectRatio="none" aria-hidden="true">
          <line x1="100" y1="30" x2="900" y2="30" class="base-line" />
          <line x1="100" y1="30" x2="900" y2="30" class="animated-flow-line" />
        </svg>

        <!-- Node cards -->
        <div class="nodes-container" role="list" aria-label="Insurance lifecycle stages">
          @for (node of nodes; track node.step; let idx = $index) {
            <article
              class="node-card"
              role="listitem"
              [class.active]="activeNode === idx"
              (mouseenter)="setActiveNode(idx)"
              (focus)="setActiveNode(idx)"
              tabindex="0"
            >
              <div class="node-indicator">
                <span class="node-number">{{ node.step }}</span>
                <span class="node-pulse" aria-hidden="true"></span>
              </div>
              <div class="node-content">
                <span class="node-role">{{ node.role }}</span>
                <h3 class="node-name">{{ node.name }}</h3>
                <p class="node-summary">{{ node.summary }}</p>
                <div class="node-footer">
                  <span class="node-highlight">{{ node.highlight }}</span>
                </div>
              </div>
            </article>
          }
        </div>
      </div>
    </section>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }

    .lifecycle-section {
      max-width: 1380px;
      margin: 0 auto;
      padding: 24px 0;
    }

    .section-header {
      text-align: center;
      max-width: 720px;
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

    .lifecycle-track-wrapper {
      position: relative;
      width: 100%;
      padding-top: 20px;
    }

    .pipeline-svg {
      position: absolute;
      top: 36px;
      left: 0;
      width: 100%;
      height: 24px;
      z-index: 1;
      pointer-events: none;
    }

    .base-line {
      stroke: var(--border, #eae5df);
      stroke-width: 3;
      stroke-linecap: round;
    }

    .animated-flow-line {
      stroke: var(--claret, #75013f);
      stroke-width: 3;
      stroke-dasharray: 24 16;
      stroke-linecap: round;
      animation: dashMove 4s linear infinite;
    }

    @keyframes dashMove {
      from {
        stroke-dashoffset: 0;
      }
      to {
        stroke-dashoffset: -80;
      }
    }

    .nodes-container {
      position: relative;
      z-index: 2;
      display: grid;
      grid-template-columns: repeat(5, minmax(0, 1fr));
      gap: 16px;
    }

    .node-card {
      background: #ffffff;
      border: 1px solid var(--border, #eae5df);
      border-radius: 10px;
      padding: 20px 16px;
      display: flex;
      flex-direction: column;
      gap: 14px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
      cursor: pointer;
      transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
      outline: none;
    }

    .node-card:hover, .node-card:focus-visible, .node-card.active {
      transform: translateY(-4px);
      border-color: var(--claret, #75013f);
      box-shadow: 0 10px 28px rgba(117, 1, 63, 0.09);
    }

    .node-card:focus-visible {
      outline: 2px solid var(--fuchsia, #fe3082);
      outline-offset: 2px;
    }

    .node-indicator {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .node-number {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: var(--warm-light, #f7f5f3);
      border: 1.5px solid var(--border, #eae5df);
      color: #3b3639;
      display: grid;
      place-items: center;
      font-size: 11px;
      font-weight: 800;
      transition: background 0.2s ease, color 0.2s ease, border-color 0.2s ease;
    }

    .node-card.active .node-number, .node-card:hover .node-number {
      background: var(--claret, #75013f);
      color: #ffffff;
      border-color: var(--claret, #75013f);
    }

    .node-pulse {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--fuchsia, #fe3082);
      box-shadow: 0 0 0 2px rgba(254, 48, 130, 0.2);
    }

    .node-content {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .node-role {
      font-size: 8px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      color: #837d82;
    }

    .node-name {
      font-size: 15px;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: var(--ink, #000000);
      margin: 0;
    }

    .node-summary {
      font-size: 11px;
      line-height: 1.55;
      color: var(--muted, #6f6a6d);
      margin: 0;
      min-height: 52px;
    }

    .node-footer {
      border-top: 1px solid #f4f0ec;
      padding-top: 10px;
      margin-top: auto;
    }

    .node-highlight {
      font-size: 9px;
      font-weight: 700;
      color: var(--claret, #75013f);
    }

    @media (max-width: 1024px) {
      .pipeline-svg {
        display: none;
      }
      .nodes-container {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    @media (max-width: 640px) {
      .nodes-container {
        grid-template-columns: 1fr;
      }
      .node-summary {
        min-height: auto;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .animated-flow-line {
        animation: none;
      }
      .node-card {
        transition: none;
      }
    }
  `],
})
export class LifecycleFlowComponent {
  activeNode = 0;

  readonly nodes: LifecycleNode[] = [
    {
      step: '01',
      name: 'Business Intake',
      role: 'Commercial Profile',
      summary: 'Structured exposure capture, location registry, and operational hazard classification.',
      highlight: 'Validated Entity Data',
    },
    {
      step: '02',
      name: 'Risk Engine',
      role: 'Underwriting Desk',
      summary: 'Automated hazard scoring, underwriting authority limits, and exception triage queues.',
      highlight: 'Algorithmic Risk Rating',
    },
    {
      step: '03',
      name: 'Protection Bound',
      role: 'Quote & Policy',
      summary: 'Instant quotation terms, electronic acceptance, and immutable policy document issuance.',
      highlight: 'Active Bound Coverage',
    },
    {
      step: '04',
      name: 'Claims Response',
      role: 'FNOL & Adjusting',
      summary: 'Digital FNOL filing, automated reserve calculation, and rapid adjuster investigation.',
      highlight: 'Controlled Loss Reserves',
    },
    {
      step: '05',
      name: 'Business Recovery',
      role: 'Continuity & Vendor',
      summary: 'Certified vendor dispatch, temporary facilities, salvage liquidation, and subrogation yield.',
      highlight: 'Operational Restoration',
    },
  ];

  setActiveNode(idx: number): void {
    this.activeNode = idx;
  }
}
