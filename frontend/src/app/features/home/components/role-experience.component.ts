import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface RolePerspective {
  id: string;
  name: string;
  badge: string;
  mission: string;
  keyTools: string[];
  operationalImpact: string;
}

@Component({
  selector: 'is-role-experience',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="roles" class="roles-section" aria-labelledby="roles-heading">
      <div class="section-header">
        <span class="section-kicker">Unified Stakeholder Platform</span>
        <h2 id="roles-heading">Role-Based Experiences</h2>
        <p class="section-subhead">
          Every participant in the insurance lifecycle operates inside an interface customized for their exact regulatory authority, workflow queue, and decision parameters.
        </p>
      </div>

      <!-- Accessible Role Tabs -->
      <div class="role-tabs-bar" role="tablist" aria-label="Select user perspective">
        @for (role of roles; track role.id; let idx = $index) {
          <button
            type="button"
            role="tab"
            class="role-tab"
            [id]="'tab-' + role.id"
            [class.active]="selectedRole === idx"
            [attr.aria-selected]="selectedRole === idx"
            [attr.aria-controls]="'panel-' + role.id"
            (click)="selectRole(idx)"
          >
            <span class="tab-name">{{ role.name }}</span>
          </button>
        }
      </div>

      <!-- Tabpanel Content for Active Role -->
      @if (currentRole; as active) {
        <div
          role="tabpanel"
          class="role-panel"
          [id]="'panel-' + active.id"
          [attr.aria-labelledby]="'tab-' + active.id"
        >
          <div class="panel-overview">
            <span class="panel-role-badge">{{ active.badge }}</span>
            <h3 class="panel-title">{{ active.name }} Perspective</h3>
            <p class="panel-mission">{{ active.mission }}</p>
          </div>

          <div class="panel-content-grid">
            <div class="tools-card">
              <span class="card-label">Primary Operational Tools</span>
              <ul class="tools-list">
                @for (tool of active.keyTools; track tool) {
                  <li>
                    <span class="tool-bullet" aria-hidden="true">&check;</span>
                    <span>{{ tool }}</span>
                  </li>
                }
              </ul>
            </div>

            <div class="impact-card">
              <span class="card-label">Operational Outcome</span>
              <p class="impact-text">{{ active.operationalImpact }}</p>
            </div>
          </div>
        </div>
      }
    </section>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }

    .roles-section {
      max-width: 1100px;
      margin: 0 auto;
      padding: 32px 0;
    }

    .section-header {
      text-align: center;
      max-width: 720px;
      margin: 0 auto 36px;
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

    .role-tabs-bar {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 8px;
      margin-bottom: 24px;
      background: var(--warm-light, #f7f5f3);
      padding: 6px;
      border-radius: 10px;
      border: 1px solid var(--border, #eae5df);
    }

    .role-tab {
      background: transparent;
      border: none;
      padding: 10px 18px;
      border-radius: 7px;
      font-size: 12px;
      font-weight: 600;
      color: #555053;
      cursor: pointer;
      transition: background 0.18s ease, color 0.18s ease;
    }

    .role-tab:hover {
      color: var(--claret, #75013f);
      background: rgba(255, 255, 255, 0.7);
    }

    .role-tab:focus-visible {
      outline: 2px solid var(--fuchsia, #fe3082);
      outline-offset: 2px;
    }

    .role-tab.active {
      background: #ffffff;
      color: var(--claret, #75013f);
      font-weight: 700;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    }

    .role-panel {
      background: #ffffff;
      border: 1px solid var(--border, #eae5df);
      border-radius: 12px;
      padding: 32px 28px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
      display: flex;
      flex-direction: column;
      gap: 24px;
      animation: fadeIn 0.25s ease-out;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(4px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .panel-overview {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .panel-role-badge {
      display: inline-block;
      width: fit-content;
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: var(--claret, #75013f);
      background: var(--claret-soft, #fde8ed);
      padding: 3px 8px;
      border-radius: 4px;
    }

    .panel-title {
      font-size: 18px;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: var(--ink, #000000);
      margin: 0;
    }

    .panel-mission {
      font-size: 13px;
      line-height: 1.6;
      color: var(--muted, #6f6a6d);
      margin: 0;
      max-width: 800px;
    }

    .panel-content-grid {
      display: grid;
      grid-template-columns: 1.2fr 0.8fr;
      gap: 20px;
    }

    .tools-card, .impact-card {
      background: var(--warm-light, #f7f5f3);
      border: 1px solid var(--border, #eae5df);
      border-radius: 8px;
      padding: 20px;
    }

    .card-label {
      display: block;
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: #797377;
      margin-bottom: 12px;
    }

    .tools-list {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .tools-list li {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 11px;
      font-weight: 500;
      color: #272427;
    }

    .tool-bullet {
      color: var(--claret, #75013f);
      font-weight: 800;
    }

    .impact-text {
      font-size: 12px;
      line-height: 1.65;
      color: #312e30;
      margin: 0;
    }

    @media (max-width: 768px) {
      .panel-content-grid {
        grid-template-columns: 1fr;
      }
      .role-tabs-bar {
        justify-content: flex-start;
        overflow-x: auto;
      }
      .role-tab {
        white-space: nowrap;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .role-panel {
        animation: none;
      }
    }
  `],
})
export class RoleExperienceComponent {
  selectedRole = 0;

  readonly roles: RolePerspective[] = [
    {
      id: 'policyholder',
      name: 'Policyholder',
      badge: 'Commercial Insured',
      mission:
        'Submit digital quote questionnaires, review clear terms, download bound policies, and trigger FNOL notifications in minutes.',
      keyTools: [
        'Structured exposure intake wizard',
        'Transparent quote terms & digital bind confirmation',
        'Direct 3-step FNOL submission portal',
      ],
      operationalImpact:
        'Eliminates opaque quoting wait times; provides immediate transparency from initial quote to verified claim recovery.',
    },
    {
      id: 'underwriter',
      name: 'Underwriter',
      badge: 'Risk Decision Authority',
      mission:
        'Assess exposure hazards, apply custom underwriting rules, review algorithmic recommendations, and issue tailored quote terms.',
      keyTools: [
        'Automated workload balance review queue',
        'Multi-factor hazard scoring and rule exceptions',
        'Formal quote terms and conditions offer desk',
      ],
      operationalImpact:
        'Automates low-hazard straight-through submissions while focusing senior expertise on complex commercial exceptions.',
    },
    {
      id: 'risk-engineer',
      name: 'Risk Engineer',
      badge: 'Technical Inspection',
      mission:
        'Conduct field reviews, audit building physical controls, identify exposure vulnerabilities, and issue formal engineering risk recommendations.',
      keyTools: [
        'Site inspection report logging',
        'Loss prevention subjectivity checklists',
        'Physical hazard classification audits',
      ],
      operationalImpact:
        'Directly links engineering physical surveys to underwriting terms and reserve provisioning.',
    },
    {
      id: 'claims-adjuster',
      name: 'Claims Adjuster',
      badge: 'Loss Triage & Adjustment',
      mission:
        'Review initial FNOL damage descriptions, calculate loss reserves against policy limits, manage investigations, and approve payouts.',
      keyTools: [
        'Active claims queue with priority triaging',
        'Automated reserve computation & adjustment',
        'Coverage determination against policy documents',
      ],
      operationalImpact:
        'Shortens average claims intake cycle from days to hours while ensuring strict audit-logged reserve adequacy.',
    },
    {
      id: 'vendor',
      name: 'Vendor Partner',
      badge: 'Service & Restoration',
      mission:
        'Accept dispatched work orders, update repair and inspection milestones in real time, and submit verified completion evidence.',
      keyTools: [
        'Direct work order dispatch inbox',
        'Assignment status updates (Dispatched, Accepted, In Progress)',
        'Accreditation onboarding & credential verification',
      ],
      operationalImpact:
        'Accelerates field response times through instant electronic dispatch and transparent SLA compliance tracking.',
    },
    {
      id: 'management',
      name: 'Management',
      badge: 'Executive Oversight',
      mission:
        'Monitor portfolio-wide written premiums, loss-development triangles, operational queues, and subrogation recovery performance.',
      keyTools: [
        'Executive portfolio dashboard summaries',
        'Actuarial loss-ratio surveillance',
        'Real-time SLA and exception watchlists',
      ],
      operationalImpact:
        'Delivers enterprise visibility into combined ratios and recovery efficiency across all commercial product lines.',
    },
  ];

  get currentRole(): RolePerspective {
    return this.roles[this.selectedRole];
  }

  selectRole(idx: number): void {
    this.selectedRole = idx;
  }
}
