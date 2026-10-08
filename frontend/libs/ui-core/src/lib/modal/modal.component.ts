import { Component, Input, Output, EventEmitter, HostListener, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../button/button.component';

export type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | 'full';

@Component({
  selector: 'is-modal',
  standalone: true,
  imports: [CommonModule, ButtonComponent],
  template: `
    @if (open) {
      <div class="modal-backdrop" (click)="onBackdropClick()" aria-hidden="true"></div>
      <div
        class="modal-container"
        [class]="sizeClasses"
        role="dialog"
        aria-modal="true"
        [attr.aria-labelledby]="titleId"
        [attr.aria-describedby]="descriptionId"
        #modalContainer
      >
        <div class="modal-content">
          @if (header) {
            <header class="modal-header">
              <div class="modal-header-content">
                @if (icon) {
                  <div class="modal-icon" [class]="iconClasses" aria-hidden="true">{{ icon }}</div>
                }
                <div>
                  <h2 [id]="titleId" class="modal-title">{{ title }}</h2>
                  @if (description) {
                    <p [id]="descriptionId" class="modal-description">{{ description }}</p>
                  }
                </div>
              </div>
              @if (closable !== false) {
                <button
                  type="button"
                  class="modal-close"
                  (click)="close.emit()"
                  aria-label="Close dialog"
                >
                  ×
                </button>
              }
            </header>
          }
          <div class="modal-body">
            <ng-content />
          </div>
          @if (footer) {
            <footer class="modal-footer">
              <ng-content select="[slot=footer]" />
            </footer>
          }
        </div>
      </div>
    }
  `,
  styles: [`
    .modal-backdrop {
      position: fixed;
      inset: 0;
      z-index: 49;
      background: rgba(0, 0, 0, 0.35);
      animation: fadeIn 0.2s ease;
    }
    
    .modal-container {
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      z-index: 50;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      max-width: 100%;
      padding: 24px;
      animation: slideUp 0.2s ease;
      pointer-events: none;
    }
    
    .modal-content {
      pointer-events: auto;
      width: 100%;
      background: #ffffff;
      border-radius: 12px;
      box-shadow: 0 24px 48px rgba(0, 0, 0, 0.18);
      overflow: hidden;
      border: 1px solid var(--border);
    }
    
    /* Sizes */
    .modal-sm .modal-content { max-width: 400px; }
    .modal-md .modal-content { max-width: 560px; }
    .modal-lg .modal-content { max-width: 720px; }
    .modal-xl .modal-content { max-width: 960px; }
    .modal-full .modal-content { max-width: 100%; height: 100vh; border-radius: 0; }
    
    .modal-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 16px;
      padding: 20px 24px;
      border-bottom: 1px solid var(--border);
    }
    
    .modal-header-content {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      flex: 1;
      min-width: 0;
    }
    
    .modal-icon {
      width: 40px;
      height: 40px;
      display: grid;
      place-items: center;
      flex-shrink: 0;
      border-radius: 8px;
      font-size: 18px;
    }
    .modal-icon-info { background: var(--claret-soft); color: var(--claret); }
    .modal-icon-success { background: var(--success-light); color: var(--success); }
    .modal-icon-warning { background: var(--warning-light); color: var(--warning); }
    .modal-icon-error { background: var(--danger-light); color: var(--danger); }
    
    .modal-title {
      margin: 0 0 4px;
      font-size: 17px;
      font-weight: 600;
      color: var(--ink);
      letter-spacing: -0.02em;
    }
    
    .modal-description {
      margin: 0;
      font-size: 12px;
      color: var(--muted);
      line-height: 1.5;
    }
    
    .modal-close {
      width: 32px;
      height: 32px;
      display: grid;
      place-items: center;
      border: 1px solid transparent;
      border-radius: 6px;
      background: transparent;
      color: #777173;
      font-size: 20px;
      cursor: pointer;
      flex-shrink: 0;
      transition: background 0.15s ease, color 0.15s ease;
    }
    .modal-close:hover {
      background: var(--warm-light);
      color: var(--claret);
    }
    .modal-close:focus-visible {
      outline: 3px solid rgba(254, 48, 130, 0.38);
      outline-offset: 2px;
    }
    
    .modal-body {
      padding: 24px;
      max-height: calc(100vh - 200px);
      overflow-y: auto;
    }
    
    .modal-full .modal-body {
      max-height: calc(100vh - 140px);
    }
    
    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      padding: 16px 24px;
      border-top: 1px solid var(--border);
      background: var(--warm-light);
    }
    
    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
    
    @keyframes slideUp {
      from {
        opacity: 0;
        transform: translate(-50%, -48%);
      }
      to {
        opacity: 1;
        transform: translate(-50%, -50%);
      }
    }
    
    @media (max-width: 640px) {
      .modal-container {
        padding: 12px;
        top: 0;
        left: 0;
        transform: none;
        align-items: flex-start;
        min-height: 100vh;
      }
      .modal-content {
        border-radius: 0;
        min-height: 100vh;
      }
      .modal-full .modal-content {
        border-radius: 0;
      }
    }
    
    @media (prefers-reduced-motion: reduce) {
      .modal-backdrop,
      .modal-container {
        animation: none;
      }
    }
  `],
})
export class ModalComponent implements AfterViewInit {
  @Input() open = false;
  @Input() title?: string;
  @Input() description?: string;
  @Input() icon?: string;
  @Input() iconVariant: 'info' | 'success' | 'warning' | 'error' = 'info';
  @Input() size: ModalSize = 'md';
  @Input() closable = true;
  @Input() closeOnBackdrop = true;
  @Input() header = true;
  @Input() footer = true;

  @Output() close = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();

  @ViewChild('modalContainer') modalContainer?: ElementRef<HTMLDivElement>;

  titleId = `modal-title-${Math.random().toString(36).slice(2)}`;
  descriptionId = `modal-desc-${Math.random().toString(36).slice(2)}`;

  ngAfterViewInit(): void {
    if (this.open) {
      this.focusFirstElement();
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.open && this.closable) {
      this.close.emit();
    }
  }

  onBackdropClick(): void {
    if (this.closeOnBackdrop) {
      this.close.emit();
    }
  }

  private focusFirstElement(): void {
    setTimeout(() => {
      const focusable = this.modalContainer?.nativeElement.querySelector<HTMLElement>(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );
      focusable?.focus();
    });
  }

  get sizeClasses(): string {
    return `modal-${this.size}`;
  }

  get iconClasses(): string {
    return `modal-icon-${this.iconVariant}`;
  }
}