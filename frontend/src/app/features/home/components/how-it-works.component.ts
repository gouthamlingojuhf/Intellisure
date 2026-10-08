import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Step {
  number: string;
  title: string;
  badge: string;
  description: string;
  outcome: string;
}

@Component({
  selector: 'is-how-it-works',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="how-it-works" class="how-it-works-section" aria-labelledby="how-it-works-heading">
      <div class="section-header">
        <span class="section-kicker">Operating Playbook</span>
        <h2 id="how-it-works-heading">How IntelliSure Works</h2>
        <p class="section-subhead">
          Designed for speed, clarity, and accountability at every stage of commercial property and casualty operations.
        </p>
      </div>

      <div class="timeline-container">
        <!-- Vertical connector line -->
        <div class="timeline-line" aria-hidden="true"></div>

        <div class="steps-wrapper" role="list" aria-label="5-Step IntelliSure Operations Process">
          @for (step of steps; track step.number; let idx = $index) {
            <article
              class="step-item"
              role="listitem"
              [class.active]="selectedStep === idx"
              (click)="selectStep(idx)"
              (keydown.enter)="selectStep(idx)"
              (keydown.space)="selectStep(idx)"
              tabindex="0"
            >
              <div class="step-marker" aria-hidden="true">
                <span class="marker-circle">{{ step.number }}</span>
              </div>
              <div class="step-card">
                <div class="card-head">
                  <span class="step-tag">{{ step.badge }}</span>
                  <span class="step-seq">Step {{ step.number }}</span>
                </div>
                <h3 class="step-title">{{ step.title }}</h3>
                <p class="step-desc">{{ step.description }}</p>
                <div class="step-outcome">
                  <span class="outcome-label">Deliverable:</span>
                  <strong class="outcome-text">{{ step.outcome }}</strong>
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

    .how-it-works-section {
      max-width: 1100px;
      margin: 0 auto;
      padding: 32px 0;
    }

    .section-header {
      text-align: center;
      max-width: 720px;
      margin: 0 auto 48px;
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

    .timeline-container {
      position: relative;
      padding: 10px 0;
    }

    .timeline-line {
      position: absolute;
      left: 24px;
      top: 30px;
      bottom: 30px;
      width: 2px;
      background: var(--border, #eae5df);
      z-index: 1;
    }

    .steps-wrapper {
      position: relative;
      z-index: 2;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .step-item {
      display: flex;
      align-items: flex-start;
      gap: 24px;
      cursor: pointer;
      outline: none;
    }

    .step-marker {
      flex-shrink: 0;
      margin-top: 14px;
    }

    .marker-circle {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: #ffffff;
      border: 2px solid var(--border, #eae5df);
      color: #3b373a;
      display: grid;
      place-items: center;
      font-size: 14px;
      font-weight: 800;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      transition: all 0.2s ease;
    }

    .step-item.active .marker-circle, .step-item:hover .marker-circle {
      background: var(--claret, #75013f);
      border-color: var(--claret, #75013f);
      color: #ffffff;
      box-shadow: 0 4px 14px rgba(117, 1, 63, 0.25);
    }

    .step-card {
      flex: 1;
      background: #ffffff;
      border: 1px solid var(--border, #eae5df);
      border-radius: 12px;
      padding: 22px 24px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
      transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
    }

    .step-item.active .step-card, .step-item:hover .step-card {
      transform: translateX(4px);
      border-color: #d6ccd2;
      box-shadow: 0 8px 24px rgba(117, 1, 63, 0.07);
    }

    .step-item:focus-visible .step-card {
      outline: 2px solid var(--fuchsia, #fe3082);
      outline-offset: 2px;
    }

    .card-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;
    }

    .step-tag {
      font-size: 8px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      color: var(--claret, #75013f);
      background: var(--claret-soft, #fde8ed);
      padding: 3px 8px;
      border-radius: 4px;
    }

    .step-seq {
      font-size: 9px;
      font-weight: 700;
      color: #8c858a;
      text-transform: uppercase;
      letter-spacing: 0.08em;
    }

    .step-title {
      font-size: 16px;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: var(--ink, #000000);
      margin: 0 0 6px;
    }

    .step-desc {
      font-size: 12px;
      line-height: 1.6;
      color: var(--muted, #6f6a6d);
      margin: 0 0 14px;
    }

    .step-outcome {
      display: flex;
      align-items: center;
      gap: 8px;
      background: var(--warm-light, #f7f5f3);
      padding: 8px 12px;
      border-radius: 6px;
      font-size: 11px;
    }

    .outcome-label {
      color: #797377;
      font-weight: 600;
      text-transform: uppercase;
      font-size: 9px;
      letter-spacing: 0.06em;
    }

    .outcome-text {
      color: #1e1b1d;
      font-weight: 700;
    }

    @media (max-width: 640px) {
      .timeline-line {
        left: 18px;
      }
      .marker-circle {
        width: 36px;
        height: 36px;
        font-size: 12px;
      }
      .step-item {
        gap: 16px;
      }
      .step-card {
        padding: 18px 16px;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .marker-circle, .step-card {
        transition: none;
      }
    }
  `],
})
export class HowItWorksComponent {
  selectedStep = 0;

  readonly steps: Step[] = [
    {
      number: '1',
      badge: 'Profile & Exposure',
      title: 'Tell Us About Your Business',
      description:
        'Input operational information, property locations, employee counts, revenue, and historical coverage details via structured digital intake.',
      outcome: 'Standardized Commercial Exposure Packet',
    },
    {
      number: '2',
      badge: 'Automated Scoring',
      title: 'Understand Your Risk',
      description:
        'The IntelliSure risk engine evaluates hazard parameters, benchmarks operational quality, and assigns an objective risk score to your submission.',
      outcome: 'Multi-Factor Risk Assessment Profile',
    },
    {
      number: '3',
      badge: 'Binding & Issuance',
      title: 'Build the Right Protection',
      description:
        'Underwriters provide customized limits and conditions, while eligible submissions receive straight-through approval for instant policy binding.',
      outcome: 'Active Policy & Bound Coverage Documents',
    },
    {
      number: '4',
      badge: 'Rapid Response',
      title: 'Respond When Something Goes Wrong',
      description:
        'When an incident occurs, file an FNOL in minutes. Initial reserves are computed, coverage is validated, and an adjuster is assigned immediately.',
      outcome: 'Triaged Claim File & Verified Reserve Fund',
    },
    {
      number: '5',
      badge: 'Restoration',
      title: 'Get Your Business Moving Again',
      description:
        'Accredited restoration, towing, or repair partners are electronically dispatched with agreed SLAs, tracking work until full operational recovery.',
      outcome: 'Completed Physical Repair & Settlement',
    },
  ];

  selectStep(idx: number): void {
    this.selectedStep = idx;
  }
}
