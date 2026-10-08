import { Component, Input, Output, EventEmitter, ContentChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../button/button.component';

export interface StepperStep {
  label: string;
  description?: string;
  icon?: string;
  complete?: boolean;
  disabled?: boolean;
}

@Component({
  selector: 'is-stepper',
  standalone: true,
  imports: [CommonModule, ButtonComponent],
  template: `
    <div class="stepper" [class.vertical]="orientation === 'vertical'">
      <div class="stepper-steps" role="tablist" [attr.aria-label]="ariaLabel">
        @for (step of steps; track step.label; let i = $index) {
          <div class="stepper-step" [class]="{'active': i === currentStep, 'complete': step.complete, 'disabled': step.disabled}">
            <button
              type="button"
              class="stepper-trigger"
              [attr.aria-selected]="i === currentStep"
              [attr.aria-disabled]="step.disabled"
              [attr.aria-controls]="'stepper-panel-' + i"
              [attr.id]="'stepper-tab-' + i"
              role="tab"
              (click)="onStepClick(i)"
              [disabled]="step.disabled"
            >
              <span class="stepper-indicator">
                @if (step.complete && i !== currentStep) {
                  <span class="stepper-check" aria-hidden="true">✓</span>
                } @else {
                  <span class="stepper-number">{{ i + 1 }}</span>
                }
              </span>
              @if (orientation === 'horizontal') {
                <div class="stepper-content">
                  <span class="stepper-label">{{ step.label }}</span>
                  @if (step.description) {
                    <span class="stepper-description">{{ step.description }}</span>
                  }
                </div>
              }
            </button>
            @if (i < steps.length - 1 && orientation === 'horizontal') {
              <div class="stepper-connector" [class.complete]="step.complete"></div>
            }
          </div>
        }
      </div>

      @if (orientation === 'vertical') {
        <div class="stepper-panels">
          @for (step of steps; track step.label; let i = $index) {
            <div
              class="stepper-panel"
              [class.active]="i === currentStep"
              role="tabpanel"
              [attr.aria-labelledby]="'stepper-tab-' + i"
              [attr.id]="'stepper-panel-' + i"
            >
              <div class="stepper-panel-header">
                <span class="stepper-indicator">
                  @if (step.complete && i !== currentStep) {
                    <span class="stepper-check" aria-hidden="true">✓</span>
                  } @else {
                    <span class="stepper-number">{{ i + 1 }}</span>
                  }
                </span>
                <div class="stepper-content">
                  <span class="stepper-label">{{ step.label }}</span>
                  @if (step.description) {
                    <span class="stepper-description">{{ step.description }}</span>
                  }
                </div>
              </div>
              <div class="stepper-panel-content">
                <ng-container *ngTemplateOutlet="stepTemplate || defaultTemplate" />
              </div>
            </div>
          }
        </div>
      } @else {
        <div class="stepper-horizontal-panel" role="tabpanel" [attr.id]="'stepper-panel-' + currentStep">
          <ng-container *ngTemplateOutlet="stepTemplate || defaultTemplate" />
        </div>
      }
    </div>

    <ng-template #defaultTemplate>
      <div class="stepper-panel-placeholder">Step content goes here</div>
    </ng-template>

    <div class="stepper-actions">
      <is-button
        variant="secondary"
        (click)="previous.emit()"
        [disabled]="currentStep === 0"
      >
        Previous
      </is-button>
      @if (currentStep === steps.length - 1) {
        <is-button variant="primary" (click)="submit.emit()">
          {{ submitLabel }}
        </is-button>
      } @else {
        <is-button variant="primary" (click)="next.emit()">
          Next
        </is-button>
      }
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }
    
    .stepper {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }
    
    .stepper.vertical {
      flex-direction: row;
      align-items: flex-start;
    }
    
    .stepper-steps {
      display: flex;
      flex-direction: column;
      gap: 0;
    }
    
    .stepper.vertical .stepper-steps {
      width: 200px;
      flex-shrink: 0;
      padding-top: 4px;
    }
    
    .stepper-step {
      position: relative;
    }
    
    .stepper-step:not(:last-child)::before {
      content: '';
      position: absolute;
      left: 12px;
      top: 28px;
      bottom: 0;
      width: 2px;
      background: var(--warm);
    }
    
    .stepper.vertical .stepper-step:not(:last-child)::before {
      left: 12px;
      top: 40px;
      bottom: 0;
    }
    
    .stepper-step.complete:not(:last-child)::before {
      background: var(--claret);
    }
    
    .stepper-trigger {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 0;
      background: none;
      border: none;
      cursor: pointer;
      text-align: left;
      width: 100%;
    }
    
    .stepper-trigger:disabled {
      cursor: not-allowed;
      opacity: 0.5;
    }
    
    .stepper-trigger:focus-visible {
      outline: 3px solid rgba(254, 48, 130, 0.38);
      outline-offset: 4px;
      border-radius: 8px;
    }
    
    .stepper-indicator {
      position: relative;
      z-index: 1;
      width: 24px;
      height: 24px;
      display: grid;
      place-items: center;
      border: 2px solid var(--warm);
      border-radius: 50%;
      background: var(--surface);
      color: #807a7e;
      font-size: 10px;
      font-weight: 700;
      flex-shrink: 0;
      transition: all 0.15s ease;
    }
    
    .stepper-step.active .stepper-indicator {
      border-color: var(--claret);
      background: var(--claret);
      color: #fff;
    }
    
    .stepper-step.complete .stepper-indicator {
      border-color: var(--claret);
      background: var(--claret);
      color: #fff;
    }
    
    .stepper-step.disabled .stepper-indicator {
      opacity: 0.5;
    }
    
    .stepper-check {
      font-size: 12px;
    }
    
    .stepper-content {
      display: flex;
      flex-direction: column;
      gap: 2px;
      min-width: 0;
    }
    
    .stepper-label {
      font-size: 13px;
      font-weight: 500;
      color: #555052;
    }
    
    .stepper-step.active .stepper-label {
      color: var(--claret);
      font-weight: 600;
    }
    
    .stepper-step.complete .stepper-label {
      color: var(--claret);
    }
    
    .stepper-description {
      font-size: 11px;
      color: var(--muted);
    }
    
    .stepper-connector {
      display: none;
    }
    
    .stepper.vertical .stepper-connector {
      display: none;
    }
    
    .stepper-panels {
      flex: 1;
      min-width: 0;
    }
    
    .stepper-panel {
      display: none;
    }
    
    .stepper-panel.active {
      display: block;
      animation: fadeIn 0.2s ease;
    }
    
    .stepper-panel-header {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding-bottom: 16px;
      border-bottom: 1px solid var(--border);
      margin-bottom: 20px;
    }
    
    .stepper-panel-content,
    .stepper-horizontal-panel {
      min-height: 200px;
    }
    
    .stepper-panel-placeholder {
      color: var(--muted);
      font-style: italic;
      padding: 40px 0;
      text-align: center;
    }
    
    .stepper-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      padding-top: 16px;
      border-top: 1px solid var(--border);
      margin-top: 24px;
    }
    
    .stepper.vertical .stepper-actions {
      padding-top: 0;
      border-top: none;
      margin-top: 0;
      align-self: flex-end;
    }
    
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(8px); }
      to { opacity: 1; transform: translateY(0); }
    }
    
    @media (max-width: 700px) {
      .stepper.vertical {
        flex-direction: column;
      }
      .stepper.vertical .stepper-steps {
        width: 100%;
        flex-direction: row;
        overflow-x: auto;
        padding-bottom: 8px;
      }
      .stepper.vertical .stepper-step:not(:last-child)::before {
        display: none;
      }
      .stepper.vertical .stepper-step {
        min-width: 140px;
      }
    }
  `],
})
export class StepperComponent {
  @Input() steps: StepperStep[] = [];
  @Input() currentStep = 0;
  @Input() orientation: 'horizontal' | 'vertical' = 'horizontal';
  @Input() submitLabel = 'Submit';
  @Input() ariaLabel = 'Form steps';

  @Output() currentStepChange = new EventEmitter<number>();
  @Output() next = new EventEmitter<void>();
  @Output() previous = new EventEmitter<void>();
  @Output() submit = new EventEmitter<void>();

  @ContentChild('stepContent') stepTemplate?: TemplateRef<any>;

  onStepClick(index: number): void {
    const step = this.steps[index];
    if (!step.disabled && index <= this.currentStep + 1) {
      this.currentStepChange.emit(index);
    }
  }
}