import { Component, Input, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';

export type SkeletonVariant = 'text' | 'circular' | 'rectangular' | 'card' | 'table-row';

@Component({
  selector: 'is-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton" [class]="variantClasses" [style.width]="width" [style.height]="height" [style.border-radius]="borderRadius">
      <div class="skeleton-shimmer" aria-hidden="true"></div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }
    .skeleton {
      position: relative;
      overflow: hidden;
      background: #eeeae7;
      border-radius: 7px;
    }
    .skeleton-shimmer {
      position: absolute;
      inset: 0;
      transform: translateX(-100%);
      background: linear-gradient(90deg, transparent, rgba(255,255,255,0.75), transparent);
      animation: shimmer 1.4s infinite;
    }
    
    @keyframes shimmer {
      to {
        transform: translateX(100%);
      }
    }
    
    /* Variants */
    .skeleton-text {
      height: 16px;
      border-radius: 4px;
    }
    .skeleton-title {
      height: 38px;
      border-radius: 6px;
    }
    .skeleton-circular {
      border-radius: 50%;
    }
    .skeleton-rectangular {
      border-radius: 8px;
    }
    .skeleton-card {
      width: 100%;
      height: 180px;
      border-radius: 10px;
    }
    .skeleton-table-row {
      height: 51px;
      border-radius: 0;
    }
    
    @media (prefers-reduced-motion: reduce) {
      .skeleton-shimmer {
        animation: none;
        opacity: 0.5;
      }
    }
  `],
})
export class SkeletonComponent {
  @Input() variant: SkeletonVariant = 'rectangular';
  @Input() width: string | number = '100%';
  @Input() height?: string | number;
  @Input() borderRadius?: string;

  @HostBinding('class') get variantClasses(): string {
    return `skeleton-${this.variant}`;
  }
}