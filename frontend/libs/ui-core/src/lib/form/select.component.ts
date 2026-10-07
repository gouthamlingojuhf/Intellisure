import { Component, Input, forwardRef, ContentChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'is-select',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="form-field" [class.form-field-error]="error">
      @if (label) {
        <label [for]="id" class="form-label">{{ label }}</label>
      }
      <select
        [id]="id"
        [disabled]="disabled"
        [value]="value"
        [class]="selectClasses"
        (change)="onChange($event)"
        (blur)="onBlur()"
        (focus)="onFocus()"
        [attr.aria-describedby]="error ? errorId : (hint ? hintId : null)"
        [attr.aria-invalid]="!!error"
      >
        @if (placeholder) {
          <option [value]="" disabled selected>{{ placeholder }}</option>
        }
        @for (option of options; track option.value) {
          <option [value]="option.value">{{ option.label }}</option>
        }
      </select>
      @if (hint && !error) {
        <span [id]="hintId" class="form-hint">{{ hint }}</span>
      }
      @if (error) {
        <span [id]="errorId" class="form-error">{{ error }}</span>
      }
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }
    .form-field {
      display: grid;
      gap: 6px;
      width: 100%;
    }
    .form-label {
      color: #393536;
      font-size: 10px;
      font-weight: 600;
    }
    select {
      width: 100%;
      min-height: 39px;
      padding: 9px 11px;
      border: 1px solid var(--warm);
      border-radius: 6px;
      background: var(--surface);
      color: #111;
      font-size: 11px;
      appearance: none;
      background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%236F6A6D' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
      background-repeat: no-repeat;
      background-position: right 12px center;
      padding-right: 36px;
      transition: border-color 0.15s ease, box-shadow 0.15s ease;
    }
    select:hover:not(:disabled) {
      border-color: #d3cbc5;
    }
    select:focus {
      border-color: var(--claret);
      outline: 0;
      box-shadow: var(--focus-ring);
    }
    select:disabled {
      background: var(--warm-light);
      color: var(--muted);
      cursor: not-allowed;
    }
    
    .form-field-error select {
      border-color: var(--danger);
    }
    .form-field-error select:focus {
      box-shadow: 0 0 0 3px rgba(163, 33, 32, 0.08);
    }
    
    .form-hint {
      color: var(--muted);
      font-size: 9px;
    }
    
    .form-error {
      color: var(--danger);
      font-size: 9px;
    }
  `],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true,
    },
  ],
})
export class SelectComponent implements ControlValueAccessor {
  @Input() id = `select-${Math.random().toString(36).slice(2)}`;
  @Input() label?: string;
  @Input() placeholder?: string;
  @Input() disabled = false;
  @Input() error?: string;
  @Input() hint?: string;
  @Input() options: { value: any; label: string }[] = [];

  value: any = '';
  focused = false;

  get selectClasses(): string {
    return '';
  }

  get errorId(): string {
    return `${this.id}-error`;
  }

  get hintId(): string {
    return `${this.id}-hint`;
  }

  onChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    this.value = target.value;
    this.onChangeFn(this.value);
  }

  onBlur(): void {
    this.focused = false;
    this.onTouched();
  }

  onFocus(): void {
    this.focused = true;
  }

  // ControlValueAccessor
  onChangeFn = (value: any) => {};
  onTouched = () => {};

  writeValue(value: any): void {
    this.value = value ?? '';
  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChangeFn = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}