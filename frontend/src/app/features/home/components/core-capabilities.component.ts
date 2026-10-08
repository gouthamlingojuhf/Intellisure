import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BadgeComponent } from 'ui-core';

interface Capability {
  id: string;
  category: string;
  title: string;
  description: string;
  icon: string;
  features: string[];
}

@Component({
  selector: 'is-core-capabilities',
  standalone: true,
  imports: [CommonModule, BadgeComponent],
  template: `
    <section id="capabilities" class="capabilities-section" aria-labelledby="capabilities-heading">
      <div class="section-header">
        <span class="section-kicker">End-to-End Suite</span>
        <h2 id="capabilities-heading">Core Capabilities Built for Precision</h2>
        <p class="section-subhead">
          Each module is purposefully engineered to operate as an autonomous domain while seamlessly exchanging lifecycle signals through our reactive gateway.
        </p>
      </div>

      <div class="capabilities-grid" role="list" aria-label="IntelliSure Platform Capabilities">
        @for (cap of capabilities; track cap.id) {
          <article class="capability-card" role="listitem">
            <div class="card-head">
              <span class="cap-icon" aria-hidden="true">{{ cap.icon }}</span>
              <span class="cap-tag">{{ cap.category }}</span>
            </div>
            <h3 class="cap-title">{{ cap.title }}</h3>
            <p class="cap-desc">{{ cap.description }}</p>
            <ul class="features-list">
              @for (feat of cap.features; track feat) {
                <li>
                  <span class="check-bullet" aria-hidden="true">&check;</span>
                  <span>{{ feat }}</span>
                </li>
              }
            </ul>
          </article>
        }
      </div>
    </section>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }

    .capabilities-section {
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

    .capabilities-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 20px;
    }

    .capability-card {
      background: #ffffff;
      border: 1px solid var(--border, #eae5df);
      border-radius: 12px;
      padding: 26px 22px;
      display: flex;
      flex-direction: column;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
      transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
    }

    .capability-card:hover {
      transform: translateY(-4px);
      border-color: #d6ccd2;
      box-shadow: 0 12px 32px rgba(117, 1, 63, 0.08);
    }

    .card-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
    }

    .cap-icon {
      width: 42px;
      height: 42px;
      border-radius: 8px;
      background: var(--claret-soft, #fde8ed);
      color: var(--claret, #75013f);
      display: grid;
      place-items: center;
      font-size: 18px;
    }

    .cap-tag {
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.1em;
      text-transform: uppercase;
      color: #6d676b;
      background: var(--warm-light, #f7f5f3);
      padding: 3px 8px;
      border-radius: 4px;
      border: 1px solid var(--border, #eae5df);
    }

    .cap-title {
      font-size: 16px;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: var(--ink, #000000);
      margin: 0 0 8px;
    }

    .cap-desc {
      font-size: 12px;
      line-height: 1.6;
      color: var(--muted, #6f6a6d);
      margin: 0 0 18px;
      min-height: 56px;
    }

    .features-list {
      list-style: none;
      padding: 0;
      margin: 0;
      border-top: 1px solid #f2eeea;
      padding-top: 14px;
      display: flex;
      flex-direction: column;
      gap: 9px;
      margin-top: auto;
    }

    .features-list li {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 11px;
      color: #383437;
      font-weight: 500;
    }

    .check-bullet {
      color: var(--claret, #75013f);
      font-weight: 800;
      font-size: 12px;
    }

    @media (max-width: 1024px) {
      .capabilities-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    @media (max-width: 640px) {
      .capabilities-grid {
        grid-template-columns: 1fr;
      }
      .cap-desc {
        min-height: auto;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .capability-card {
        transition: none;
      }
    }
  `],
})
export class CoreCapabilitiesComponent {
  readonly capabilities: Capability[] = [
    {
      id: 'quotes',
      category: 'Intake & Rating',
      title: 'Smart Quotes & Policy Admin',
      description:
        'Streamline complex commercial submissions with structured questionnaires, instant rating calculations, and straight-through policy binding.',
      icon: '📋',
      features: [
        'Structured exposure intake forms',
        'Dynamic premium calculation engine',
        'Immutable policy document binding',
      ],
    },
    {
      id: 'underwriting',
      category: 'Risk Control',
      title: 'Risk & Underwriting Desk',
      description:
        'Empower underwriters with algorithmic hazard scoring, rule-based triage queues, and clear delegation authority for manual referrals.',
      icon: '🔍',
      features: [
        'Multi-factor hazard scoring algorithms',
        'Automatic workload assignment queue',
        'Audit-logged subjective exception gates',
      ],
    },
    {
      id: 'claims',
      category: 'Loss Handling',
      title: 'Claims Operations & FNOL',
      description:
        'Deliver prompt policyholder care through guided 3-step FNOL intake, automated initial reserve adequacy, and full adjuster workbench support.',
      icon: '📄',
      features: [
        'Guided 3-step digital FNOL filing',
        'Automated reserve computation & tracking',
        'Coverage verification against policy terms',
      ],
    },
    {
      id: 'recovery',
      category: 'Continuity',
      title: 'Business Recovery Coordination',
      description:
        'Coordinate emergency mitigation, temporary workspace logistics, equipment leasing, salvage liquidation, and subrogation recovery.',
      icon: '💰',
      features: [
        'Structured business continuity plans',
        'Salvage appraisal & auction tracking',
        'Third-party subrogation recovery pipeline',
      ],
    },
    {
      id: 'vendor',
      category: 'Partner Network',
      title: 'Connected Vendor Network',
      description:
        'Dispatch accredited auto repair, property remediation, and legal specialists with verified onboarding credentials and live task tracking.',
      icon: '🏢',
      features: [
        'Accredited vendor credential verification',
        'Direct electronic dispatch & SLA management',
        'Real-time repair milestone progression',
      ],
    },
    {
      id: 'analytics',
      category: 'Intelligence',
      title: 'Intelligence & Executive Analytics',
      description:
        'Gain immediate portfolio visibility with loss-ratio surveillance, actuarial loss-triangle development, and operational signal alerting.',
      icon: '📊',
      features: [
        'Executive dashboard summaries & loss ratios',
        'Actuarial loss-triangle development views',
        'Proactive hazard watchlist & alert triage',
      ],
    },
  ];
}
