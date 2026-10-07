import { Component, Input, forwardRef, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'is-input',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="form-field" [class.form-field-error]="error">
      @if (label) {
        <label [for]="id" class="form-label">{{ label }}</label>
      }
      <input
        [id]="id"
        [type]="type"
        [placeholder]="placeholder"
        [disabled]="disabled"
        [readonly]="readonly"
        [value]="value"
        [class]="inputClasses"
        (input)="onInput($event)"
        (blur)="onBlur()"
        (focus)="onFocus()"
        [attr.aria-describedby]="error ? errorId : (hint ? hintId : null)"
        [attr.aria-invalid]="!!error"
      />
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
    input {
      width: 100%;
      min-height: 39px;
      padding: 9px 11px;
      border: 1px solid var(--warm);
      border-radius: 6px;
      background: var(--surface);
      color: #111;
      font-size: 11px;
      transition: border-color 0.15s ease, box-shadow 0.15s ease;
    }
    input:hover:not(:disabled):not([readonly]) {
      border-color: #d3cbc5;
    }
    input:focus {
      border-color: var(--claret);
      outline: 0;
      box-shadow: var(--focus-ring);
    }
    input:disabled {
      background: var(--warm-light);
      color: var(--muted);
      cursor: not-allowed;
    }
    input[readonly] {
      background: var(--surface-hover);
    }
    input::placeholder {
      color: #948f92;
    }
    
    .form-field-error input {
      border-color: var(--danger);
    }
    .form-field-error input:focus {
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
      useExisting: forwardRef(() => InputComponent),
      multi: true,
    },
  ],
})
export class InputComponent implements ControlValueAccessor {
  @Input() id = `input-${Math.random().toString(36).slice(2)}`;
  @Input() label?: string;
  @Input() type: 'text' | 'email' | 'password' | 'number' | 'tel' | 'url' | 'date' = 'text';
  @Input() placeholder = '';
  @Input() disabled = false;
  @Input() readonly = false;
  @Input() error?: string;
  @Input() hint?: string;

  value = '';
  focused = false;

  get inputClasses(): string {
    return '';
  }

  get errorId(): string {
    return `${this.id}-error`;
  }

  get hintId(): string {
    return `${this.id}-hint`;
  }

  onInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.value = target.value;
    this.onChange(this.value);
  }

  onBlur(): void {
    this.focused = false;
    this.onTouched();
  }

  onFocus(): void {
    this.focused = true;
  }

  // ControlValueAccessor
  onChange = (value: string) => {};
  onTouched = () => {};

  writeValue(value: string): void {
    this.value = value ?? '';
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}