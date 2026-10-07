import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'is-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <article class="card" [class.card-hover]="hoverable" [class.card-bordered]="bordered">
      @if (header) {
        <header class="card-header">
          <div class="card-header-content">
            @if (kicker) {
              <p class="panel-kicker">{{ kicker }}</p>
            }
            @if (title) {
              <h2 class="card-title">{{ title }}</h2>
            }
            @if (subtitle) {
              <p class="card-subtitle">{{ subtitle }}</p>
            }
          </div>
          <div class="card-header-actions">
            <ng-content select="[slot=header-actions]" />
          </div>
        </header>
      }
      <div class="card-body" [class.card-body-padded]="padded">
        <ng-content />
      </div>
      @if (hasFooter) {
        <footer class="card-footer">
          <ng-content select="[slot=footer]" />
        </footer>
      }
    </article>
  `,
  styles: [`
    :host {
      display: block;
    }
    .card {
      border: 1px solid var(--border);
      border-radius: 9px;
      background: var(--surface);
      box-shadow: var(--shadow);
      overflow: hidden;
    }
    .card-hover:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-lg);
      border-color: #d8cdd4;
      transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
    }
    .card-bordered {
      border-width: 1px;
    }
    
    .card-header {
      min-height: 65px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 15px 18px;
      border-bottom: 1px solid var(--border);
      gap: 16px;
    }
    .card-header-content {
      display: flex;
      flex-direction: column;
      gap: 4px;
      min-width: 0;
    }
    .panel-kicker {
      margin: 0;
      color: var(--claret);
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.14em;
      text-transform: uppercase;
    }
    .card-title {
      margin: 0;
      color: #171617;
      font-size: 15px;
      letter-spacing: -0.02em;
      font-weight: 600;
    }
    .card-subtitle {
      margin: 0;
      color: var(--muted);
      font-size: 11px;
      line-height: 1.5;
    }
    .card-header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-shrink: 0;
    }
    
    .card-body {
      padding: 20px;
    }
    .card-body-padded {
      padding: 24px;
    }
    
    .card-footer {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      padding: 15px 20px;
      border-top: 1px solid var(--border);
      background: var(--warm-light);
    }
  `],
})
export class CardComponent {
  @Input() title?: string;
  @Input() subtitle?: string;
  @Input() kicker?: string;
  @Input() header = true;
  @Input() padded = false;
  @Input() hoverable = false;
  @Input() bordered = true;

  get hasFooter(): boolean {
    return true;
  }
}