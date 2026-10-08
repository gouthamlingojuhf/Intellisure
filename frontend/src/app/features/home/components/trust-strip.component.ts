import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface TrustPillar {
  icon: string;
  title: string;
  description: string;
  badge: string;
}

@Component({
  selector: 'is-trust-strip',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="trust-strip" aria-labelledby="trust-strip-heading">
      <h2 id="trust-strip-heading" class="sr-only">Platform Value Pillars</h2>
      <div class="trust-container">
        @for (pillar of pillars; track pillar.title) {
          <article class="trust-card">
            <div class="card-top">
              <span class="trust-icon" aria-hidden="true">{{ pillar.icon }}</span>
              <span class="pillar-badge">{{ pillar.badge }}</span>
            </div>
            <h3 class="trust-title">{{ pillar.title }}</h3>
            <p class="trust-desc">{{ pillar.description }}</p>
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

    .sr-only {
      position: absolute;
      width: 1px;
      height: 1px;
      padding: 0;
      margin: -1px;
      overflow: hidden;
      clip: rect(0, 0, 0, 0);
      white-space: nowrap;
      border: 0;
    }

    .trust-strip {
      padding: 0;
    }

    .trust-container {
      max-width: 1380px;
      margin: 0 auto;
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 16px;
    }

    .trust-card {
      background: #ffffff;
      border: 1px solid var(--border, #eae5df);
      border-radius: 10px;
      padding: 22px 20px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
      display: flex;
      flex-direction: column;
      gap: 8px;
      transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
    }

    .trust-card:hover {
      transform: translateY(-2px);
      border-color: #d6ccd2;
      box-shadow: 0 8px 24px rgba(117, 1, 63, 0.06);
    }

    .card-top {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 4px;
    }

    .trust-icon {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      background: var(--claret-soft, #fde8ed);
      color: var(--claret, #75013f);
      display: grid;
      place-items: center;
      font-size: 16px;
      font-weight: 700;
    }

    .pillar-badge {
      font-size: 8px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      color: #736d71;
      background: var(--warm-light, #f7f5f3);
      padding: 3px 7px;
      border-radius: 4px;
      border: 1px solid var(--border, #eae5df);
    }

    .trust-title {
      font-size: 14px;
      font-weight: 700;
      color: var(--ink, #000000);
      letter-spacing: -0.015em;
      margin: 0;
    }

    .trust-desc {
      font-size: 11px;
      line-height: 1.55;
      color: var(--muted, #6f6a6d);
      margin: 0;
    }

    @media (max-width: 1024px) {
      .trust-container {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    @media (max-width: 640px) {
      .trust-container {
        grid-template-columns: 1fr;
      }
      .trust-card {
        padding: 18px 16px;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .trust-card {
        transition: none;
      }
    }
  `],
})
export class TrustStripComponent {
  readonly pillars: TrustPillar[] = [
    {
      icon: '🛡️',
      badge: 'Evaluation',
      title: 'Intelligent Risk Assessment',
      description:
        'Continuous multi-factor exposure scoring and algorithmic rule validation surface portfolio hazards before policies are bound.',
    },
    {
      icon: '⚡',
      badge: 'FNOL Velocity',
      title: 'Faster Claims Response',
      description:
        'Guided intake triage, instant reserve calculation, and automated adjuster workload balancing streamline claims settlement.',
    },
    {
      icon: '🔄',
      badge: 'Continuity',
      title: 'Business Recovery Coordination',
      description:
        'Structured restoration roadmaps connect salvage, temporary facility logistics, and subrogation to get operations moving again.',
    },
    {
      icon: '🌐',
      badge: 'Operations',
      title: 'Connected Vendor Ecosystem',
      description:
        'Vetted partner networks dispatch towing, repair, and legal specialists with verified credentialing and live milestone tracking.',
    },
  ];
}
