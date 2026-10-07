import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CardComponent, ButtonComponent, EmptyStateComponent } from 'ui-core';

@Component({
  selector: 'is-not-found',
  standalone: true,
  imports: [RouterLink, CardComponent, ButtonComponent, EmptyStateComponent],
  template: `
    <section class="not-found-page">
      <is-empty-state
        title="404 — Page not found"
        description="The page you're looking for doesn't exist or has been moved."
        icon="🔍"
        iconVariant="default"
        size="lg"
        actionLabel="Back to dashboard"
        secondaryActionLabel="Contact support"
        (action)="goHome()"
        (secondaryAction)="contactSupport()"
      />
    </section>
  `,
  styles: [`
    .not-found-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 48px 24px;
    }
  `],
})
export class NotFoundComponent {
  goHome(): void {
    window.location.href = '/';
  }

  contactSupport(): void {
    window.location.href = '/docs';
  }
}