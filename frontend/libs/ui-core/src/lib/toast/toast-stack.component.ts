import { Component, Input, Output, EventEmitter, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
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
export class ToastStackComponent implements OnChanges, OnDestroy {
  @Input() toasts: Toast[] = [];
  @Output() dismiss = new EventEmitter<number>();
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();

  ngOnChanges(_changes: SimpleChanges): void {
    const activeIds = new Set(this.toasts.map((toast) => toast.id));
    for (const [id, timer] of this.timers) {
      if (!activeIds.has(id)) {
        clearTimeout(timer);
        this.timers.delete(id);
      }
    }

    for (const toast of this.toasts) {
      if (this.timers.has(toast.id)) continue;
      this.timers.set(toast.id, setTimeout(() => {
        this.timers.delete(toast.id);
        this.dismiss.emit(toast.id);
      }, toast.duration ?? 32_000));
    }
  }

  ngOnDestroy(): void {
    for (const timer of this.timers.values()) clearTimeout(timer);
    this.timers.clear();
  }

  onDismiss(id: number): void {
    this.dismiss.emit(id);
  }
}
