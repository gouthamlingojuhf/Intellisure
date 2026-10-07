import { Component, Input, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';

export type BadgeVariant = 'success' | 'warning' | 'danger' | 'info' | 'neutral';
export type BadgeSize = 'sm' | 'md';

@Component({
  selector: 'is-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="badge" [class]="variantClasses">
      @if (icon) {
        <span class="badge-icon" aria-hidden="true">{{ icon }}</span>
      }
      <span class="badge-text"><ng-content /></span>
      @if (dot) {
        <span class="badge-dot" [class]="'badge-dot-' + variant" aria-hidden="true"></span>
      }
    </span>
  `,
  styles: [`
    :host {
      display: inline-flex;
    }
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      border-radius: 999px;
      font-weight: 700;
      white-space: nowrap;
      line-height: 1;
    }
    
    /* Sizes */
    .badge-sm {
      min-height: 20px;
      padding: 0 8px;
      font-size: 8px;
      letter-spacing: 0.04em;
      text-transform: uppercase;
    }
    .badge-md {
      min-height: 24px;
      padding: 0 10px;
      font-size: 9px;
      letter-spacing: 0.04em;
      text-transform: uppercase;
    }
    
    /* Variants */
    .badge-success {
      color: var(--success);
      background: var(--success-light);
    }
    .badge-warning {
      color: var(--warning);
      background: var(--warning-light);
    }
    .badge-danger {
      color: var(--danger);
      background: var(--danger-light);
    }
    .badge-info {
      color: var(--claret);
      background: var(--claret-soft);
    }
    .badge-neutral {
      color: #5d575b;
      background: #f0eeec;
    }
    
    .badge-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 10px;
    }
    
    .badge-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      flex-shrink: 0;
    }
    .badge-dot-success { background: var(--success); }
    .badge-dot-warning { background: var(--warning); }
    .badge-dot-danger { background: var(--danger); }
    .badge-dot-info { background: var(--claret); }
    .badge-dot-neutral { background: #777; }
  `],
})
export class BadgeComponent {
  @Input() variant: BadgeVariant = 'info';
  @Input() size: BadgeSize = 'sm';
  @Input() icon?: string;
  @Input() dot = false;

  @HostBinding('class') get variantClasses(): string {
    return `badge-${this.size} badge-${this.variant}`;
  }
}