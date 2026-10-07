import { Component, Input, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'text' | 'icon';
export type ButtonSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'is-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      [type]="type"
      [disabled]="disabled"
      [class]="computedClasses"
      (click)="onClick($event)"
    >
      @if (icon && iconPosition === 'start') {
        <span class="btn-icon-content" aria-hidden="true">{{ icon }}</span>
      }
      <span class="btn-content"><ng-content /></span>
      @if (icon && iconPosition === 'end') {
        <span class="btn-icon-content" aria-hidden="true">{{ icon }}</span>
      }
    </button>
  `,
  styles: [`
    :host {
      display: inline-flex;
    }
    button {
      font-family: inherit;
      border: none;
      cursor: pointer;
      transition: all 0.15s ease;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      text-decoration: none;
    }
    button:disabled {
      cursor: not-allowed;
      opacity: 0.55;
    }
    button:focus-visible {
      outline: 3px solid rgba(254, 48, 130, 0.38);
      outline-offset: 2px;
    }
    .btn-icon-content {
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }
    .btn-content {
      display: inline-flex;
      align-items: center;
    }
    
    /* Variants */
    .btn-primary {
      border: 1px solid var(--claret);
      background: var(--claret);
      color: #fff;
      box-shadow: 0 4px 12px rgba(117, 30, 63, 0.16);
    }
    .btn-primary:hover:not(:disabled) {
      background: var(--claret-hover);
      transform: translateY(-1px);
    }
    
    .btn-secondary {
      border: 1px solid var(--border);
      background: var(--surface);
      color: #272427;
    }
    .btn-secondary:hover:not(:disabled) {
      border-color: #c9c1bc;
      background: var(--warm-light);
    }
    
    .btn-ghost {
      border: 1px solid transparent;
      background: transparent;
      color: var(--claret);
    }
    .btn-ghost:hover:not(:disabled) {
      background: var(--claret-soft);
    }
    
    .btn-text {
      padding: 0;
      background: none;
      color: var(--claret);
      font-size: 10px;
      font-weight: 700;
      min-height: auto;
    }
    .btn-text:hover:not(:disabled) {
      color: var(--fuchsia);
    }
    
    .btn-icon {
      width: 38px;
      height: 38px;
      padding: 0;
      border-radius: 7px;
      background: transparent;
      color: #4a4548;
    }
    .btn-icon:hover:not(:disabled) {
      background: var(--warm-light);
      color: var(--claret);
    }
    
    /* Sizes */
    .btn-sm {
      min-height: 32px;
      padding: 0 12px;
      font-size: 10px;
      border-radius: 5px;
    }
    .btn-md {
      min-height: 38px;
      padding: 0 15px;
      font-size: 11px;
      border-radius: 6px;
    }
    .btn-lg {
      min-height: 44px;
      padding: 0 20px;
      font-size: 12px;
      border-radius: 7px;
    }
    
    .btn-icon.btn-sm { width: 32px; height: 32px; border-radius: 5px; }
    .btn-icon.btn-md { width: 38px; height: 38px; border-radius: 7px; }
    .btn-icon.btn-lg { width: 44px; height: 44px; border-radius: 8px; }
  `],
})
export class ButtonComponent {
  @Input() variant: ButtonVariant = 'primary';
  @Input() size: ButtonSize = 'md';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() disabled = false;
  @Input() icon?: string;
  @Input() iconPosition: 'start' | 'end' = 'start';
  @Input() fullWidth = false;

  @HostBinding('class') get computedClasses(): string {
    const classes = ['btn', `btn-${this.variant}`, `btn-${this.size}`];
    if (this.fullWidth) classes.push('w-full');
    return classes.join(' ');
  }

  onClick(event: MouseEvent): void {
    if (this.disabled) {
      event.preventDefault();
      event.stopPropagation();
    }
  }
}