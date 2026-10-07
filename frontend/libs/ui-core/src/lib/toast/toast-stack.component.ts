import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastComponent, Toast } from './toast.component';

@Component({
  selector: 'is-toast-stack',
  standalone: true,
  imports: [CommonModule, ToastComponent],
  template: `
    <div class="toast-stack" aria-live="polite" aria-atomic="true">
      @for (toast of toasts; track toast.id) {
        <is-toast [toast]="toast" (dismiss)="onDismiss($event)" />
      }
    </div>
  `,
  styles: [`
    .toast-stack {
      position: fixed;
      right: 24px;
      bottom: 24px;
      z-index: 100;
      width: min(370px, calc(100vw - 32px));
      display: grid;
      gap: 10px;
      pointer-events: none;
    }
    .toast-stack > * {
      pointer-events: auto;
    }
    
    @media (max-width: 640px) {
      .toast-stack {
        right: 10px;
        left: 10px;
        bottom: 10px;
        width: auto;
      }
    }
  `],
})
export class ToastStackComponent {
  @Input() toasts: Toast[] = [];
  @Output() dismiss = new EventEmitter<number>();

  onDismiss(id: number): void {
    this.dismiss.emit(id);
  }
}