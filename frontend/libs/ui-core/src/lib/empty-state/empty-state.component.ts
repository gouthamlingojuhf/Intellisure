import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../button/button.component';

export type EmptyStateSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'is-empty-state',
  standalone: true,
  imports: [CommonModule, ButtonComponent],
  template: `
    <div class="empty-state" [class]="sizeClasses">
      <div class="empty-icon" [class]="iconClasses" aria-hidden="true">
        {{ icon }}
      </div>
      @if (title) {
        <h2 class="empty-title">{{ title }}</h2>
      }
      @if (description) {
        <p class="empty-description">{{ description }}</p>
      }
      @if (actionLabel) {
        <div class="empty-actions">
          <is-button variant="primary" [size]="actionSize" (click)="action.emit()">
            {{ actionLabel }}
          </is-button>
          @if (secondaryActionLabel) {
            <is-button variant="secondary" [size]="actionSize" (click)="secondaryAction.emit()">
              {{ secondaryActionLabel }}
            </is-button>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }
    .empty-state {
      display: grid;
      place-items: center;
      padding: 40px;
      border: 1px dashed #d3cbc5;
      border-radius: 9px;
      background: var(--surface);
      text-align: center;
    }
    
    .empty-state-sm { min-height: 200px; padding: 24px; }
    .empty-state-md { min-height: 300px; padding: 32px; }
    .empty-state-lg { min-height: 350px; padding: 40px; }
    
    .empty-icon {
      width: 54px;
      height: 54px;
      display: grid;
      place-items: center;
      margin: 0 auto 14px;
      border-radius: 50%;
      font-size: 22px;
    }
    .empty-icon-default {
      background: var(--warm-light);
      color: var(--claret);
    }
    .empty-icon-success {
      background: var(--success-light);
      color: var(--success);
    }
    .empty-icon-warning {
      background: var(--warning-light);
      color: var(--warning);
    }
    .empty-icon-error {
      background: var(--danger-light);
      color: var(--danger);
    }
    
    .empty-title {
      margin: 0 0 7px;
      font-size: 17px;
      color: var(--ink);
    }
    
    .empty-description {
      max-width: 430px;
      margin: 0 auto 17px;
      color: var(--muted);
      font-size: 11px;
      line-height: 1.6;
    }
    
    .empty-actions {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      justify-content: center;
    }
  `],
})
export class EmptyStateComponent {
  @Input() title?: string;
  @Input() description?: string;
  @Input() icon = '📄';
  @Input() iconVariant: 'default' | 'success' | 'warning' | 'error' = 'default';
  @Input() size: EmptyStateSize = 'md';
  @Input() actionLabel?: string;
  @Input() secondaryActionLabel?: string;
  @Input() actionSize: 'sm' | 'md' | 'lg' = 'md';

  @Output() action = new EventEmitter<void>();
  @Output() secondaryAction = new EventEmitter<void>();

  get sizeClasses(): string {
    return `empty-state-${this.size}`;
  }

  get iconClasses(): string {
    return `empty-icon-${this.iconVariant}`;
  }
}