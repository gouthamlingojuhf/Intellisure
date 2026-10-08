import { Component, Input, Output, EventEmitter, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../button/button.component';

export type ToastKind = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id: number;
  message: string;
  kind: ToastKind;
  title?: string;
  duration?: number;
  action?: { label: string; handler: () => void };
}

@Component({
  selector: 'is-toast',
  standalone: true,
  imports: [CommonModule, ButtonComponent],
  template: `
    <div class="toast-item" [class]="kindClasses" role="alert" aria-live="polite" aria-atomic="true">
      <div class="toast-icon" [class]="iconClasses" aria-hidden="true">
        @switch (toast.kind) {
          @case ('success') { ✓ }
          @case ('error') { ! }
          @case ('warning') { ⚠ }
          @default { i }
        }
      </div>
      <div class="toast-content">
        @if (toast.title) {
          <div class="toast-title">{{ toast.title }}</div>
        }
        <div class="toast-message">{{ toast.message }}</div>
      </div>
      @if (toast.action) {
        <is-button variant="text" size="sm" (click)="toast.action.handler()">
          {{ toast.action.label }}
        </is-button>
      }
      <button
        type="button"
        class="toast-dismiss"
        (click)="dismiss.emit(toast.id)"
        aria-label="Dismiss notification"
      >
        ×
      </button>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      animation: slideIn 0.3s ease;
    }
    
    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateX(20px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }
    
    .toast-item {
      display: grid;
      grid-template-columns: auto 1fr auto auto;
      gap: 10px;
      align-items: start;
      padding: 13px 14px;
      border: 1px solid var(--border);
      border-left: 4px solid;
      border-radius: 8px;
      background: #ffffff;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      color: #3c373a;
      font-size: 11px;
    }
    
    .toast-success { border-left-color: var(--success); }
    .toast-error { border-left-color: var(--danger); }
    .toast-info { border-left-color: var(--claret); }
    .toast-warning { border-left-color: var(--warning); }
    
    .toast-icon {
      width: 24px;
      height: 24px;
      display: grid;
      place-items: center;
      border-radius: 50%;
      background: var(--warm-light);
      font-weight: 700;
      font-size: 12px;
      flex-shrink: 0;
    }
    .toast-icon.success { color: var(--success); background: var(--success-light); }
    .toast-icon.error { color: var(--danger); background: var(--danger-light); }
    .toast-icon.info { color: var(--claret); background: var(--claret-soft); }
    .toast-icon.warning { color: var(--warning); background: var(--warning-light); }
    
    .toast-content {
      display: flex;
      flex-direction: column;
      gap: 2px;
      min-width: 0;
    }
    
    .toast-title {
      font-weight: 600;
      color: #171617;
      font-size: 11px;
    }
    
    .toast-message {
      color: #5d575b;
      line-height: 1.5;
      word-break: break-word;
    }
    
    .toast-dismiss {
      border: 0;
      background: transparent;
      color: #757071;
      cursor: pointer;
      font-size: 18px;
      line-height: 1;
      padding: 0;
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      flex-shrink: 0;
    }
    .toast-dismiss:hover {
      background: var(--warm-light);
      color: var(--ink);
    }
  `],
})
export class ToastComponent {
  @Input() toast!: Toast;
  @Output() dismiss = new EventEmitter<number>();

  get kindClasses(): string {
    return `toast-${this.toast.kind}`;
  }

  get iconClasses(): string {
    return this.toast.kind;
  }
}