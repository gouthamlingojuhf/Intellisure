import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { selectUserRole } from '../../core/store/auth/auth.selectors';

interface GuideSection { title: string; summary: string; steps: string[]; }

@Component({
  selector: 'is-platform-guide',
  standalone: true,
  imports: [AsyncPipe, RouterLink],
  template: `
    <main class="guide-page">
      <header class="guide-header"><a routerLink="/" class="back-link">← IntelliSure home</a><p class="eyebrow">IntelliSure documentation</p><h1>Platform guides</h1><p>Clear, role-aware guidance for the insurance lifecycle, vendor recovery, and employee work queues.</p></header>
      <section class="guide-grid" aria-label="Public platform guides">
        @for (section of publicSections; track section.title) { <article class="guide-card"><span class="guide-number">{{ $index + 1 }}</span><h2>{{ section.title }}</h2><p>{{ section.summary }}</p><ol>@for (step of section.steps; track step) { <li>{{ step }}</li> }</ol></article> }
      </section>
      @if (role$ | async; as role) {
        <section class="role-section"><p class="eyebrow">Your workspace guide</p><h2>{{ formatRole(role) }} operating guide</h2><div class="guide-grid">@for (section of roleSections(role); track section.title) { <article class="guide-card"><h3>{{ section.title }}</h3><p>{{ section.summary }}</p><ol>@for (step of section.steps; track step) { <li>{{ step }}</li> }</ol></article> }</div></section>
      } @else { <section class="callout"><strong>Ready to use the platform?</strong><p>Register as a Business Policyholder to manage insurance, or as a Vendor / Service Provider Applicant to begin onboarding.</p><a routerLink="/auth/register">Create an account →</a></section> }
    </main>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; background: var(--warm-light, #f7f5f3); color: var(--ink, #000); } .guide-page { max-width: 1180px; margin: 0 auto; padding: 42px 24px 72px; } .guide-header { max-width: 760px; margin-bottom: 34px; } .back-link { color: var(--claret, #75013f); font-size: 12px; font-weight: 700; text-decoration: none; } .eyebrow { margin: 30px 0 8px; color: var(--claret, #75013f); font-size: 10px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; } h1 { margin: 0; font-size: clamp(32px, 5vw, 54px); letter-spacing: -.055em; } .guide-header > p:last-child { max-width: 680px; margin: 14px 0 0; color: var(--muted, #6f6a6d); font-size: 14px; line-height: 1.7; } .guide-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; } .guide-card { position: relative; padding: 24px; border: 1px solid var(--border, #eae5df); border-radius: 10px; background: #fff; box-shadow: var(--shadow, 0 8px 24px rgba(0,0,0,.05)); } .guide-number { display: grid; place-items: center; width: 28px; height: 28px; margin-bottom: 18px; border-radius: 50%; background: var(--claret-soft, #fde8ed); color: var(--claret, #75013f); font-size: 11px; font-weight: 700; } h2, h3 { margin: 0 0 9px; letter-spacing: -.025em; } .guide-card p { margin: 0 0 15px; color: var(--muted, #6f6a6d); font-size: 11px; line-height: 1.6; } ol { margin: 0; padding-left: 19px; color: #413b3e; font-size: 11px; line-height: 1.8; } .role-section { margin-top: 54px; } .role-section .eyebrow { margin-top: 0; } .role-section > h2 { font-size: 26px; margin-bottom: 18px; } .role-section .guide-card h3 { font-size: 15px; } .callout { margin-top: 32px; padding: 22px 24px; border: 1px solid #d8abc0; border-radius: 10px; background: var(--claret-soft, #fde8ed); } .callout strong { font-size: 15px; } .callout p { margin: 7px 0 12px; color: #5d3d4c; font-size: 12px; } .callout a { color: var(--claret, #75013f); font-size: 12px; font-weight: 700; } @media (max-width: 850px) { .guide-grid { grid-template-columns: 1fr; } .guide-page { padding-inline: 16px; } }
  `],
})
export class PlatformGuideComponent {
  private readonly store = inject(Store);
  readonly role$ = this.store.select(selectUserRole);
  readonly publicSections: GuideSection[] = [
    { title: 'Start with the right account', summary: 'Choose the account type that matches your role so the platform presents the correct profile and workspaces.', steps: ['Register as Business Policyholder or Vendor / Service Provider Applicant.', 'Complete the requested profile information.', 'Sign in again after profile completion when the customer identity must be added to the JWT.'] },
    { title: 'Policyholder lifecycle', summary: 'Move from business profile to protected operations using live Gateway-backed services.', steps: ['Create and submit a quote for underwriting review.', 'Review offered terms, accept the quote, and follow policy issuance.', 'File a claim, choose a recovery path, review documents, and manage notifications.'] },
    { title: 'Vendor network', summary: 'Vendor work is controlled by onboarding verification and explicit recovery dispatch.', steps: ['Submit vendor onboarding details and supporting references.', 'Wait for Vendor Manager verification before appearing in the live directory.', 'Accept assigned work, record progress, attach evidence references, and complete the assignment.'] },
  ];
  roleSections(role: string): GuideSection[] {
    const normalized = role.toUpperCase();
    if (normalized === 'POLICYHOLDER' || normalized === 'USER') return [{ title: 'Policyholder actions', summary: 'Your workspace is limited to your own customer-owned records.', steps: ['Maintain Business Profile before requesting a quote.', 'Use quote and policy numbers when discussing records with the team.', 'Use Claims, Recovery, Documents, and Notifications to follow the active lifecycle.'] }];
    if (normalized === 'VENDOR_APPLICANT') return [{ title: 'Applicant actions', summary: 'Your access is limited to vendor onboarding until a manager verifies the vendor.', steps: ['Submit complete legal, contact, service, and area information.', 'Monitor onboarding status in the Vendor workspace.', 'Do not expect customer policyholder screens or unverified directory access.'] }];
    if (normalized === 'VENDOR_MANAGER') return [{ title: 'Partner operations', summary: 'Manage the live vendor network and explicit dispatch workflow.', steps: ['Review onboarding requests and verify or reject them with a reason.', 'Select only verified active vendors for Network Vendor work.', 'Review assignment completion and record partner performance.'] }];
    if (normalized === 'UNDERWRITER' || normalized === 'RISK_ENGINEER') return [{ title: 'Risk work queue', summary: 'Work only the assessments and quote actions authorized for your role.', steps: ['Review assigned risk and quote context.', normalized === 'UNDERWRITER' ? 'Record decisions and offer approved commercial terms.' : 'Provide read-only risk context without making underwriting decisions.', 'Use policy documents and notifications for governed follow-up.'] }];
    if (normalized === 'CLAIMS_ADJUSTER' || normalized === 'CLAIMS_MANAGER') return [{ title: 'Claims and recovery operations', summary: 'Operate the claims workflow and supported vendor recovery controls.', steps: ['Review assigned or operational claim records.', 'Use coverage, reserve, and recovery actions permitted by your role.', 'Dispatch a vendor only when the selected recovery path is explicitly NETWORK_VENDOR.'] }];
    if (normalized === 'SYSTEM_ADMINISTRATOR' || normalized === 'ADMIN') return [{ title: 'Administration', summary: 'Govern access and review enterprise records without impersonating business decisions.', steps: ['Manage employee accounts and status through the Administration workspace.', 'Review live customers, quotes, policies, claims, and partner records.', 'Leave underwriting, binding, issuance, claim decisions, and vendor fulfillment to their authorized roles.'] }];
    return [{ title: 'Employee workspace', summary: 'Use only the routes and actions authorized for your assigned role.', steps: ['Review the navigation available to your account.', 'Follow the relevant operating queue and its service-level controls.', 'Escalate access issues to a System Administrator.'] }];
  }
  formatRole(role: string): string { return role.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase()); }
}
