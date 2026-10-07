import { Component, Input, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'is-textarea',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="form-field" [class.form-field-error]="error">
      @if (label) {
        <label [for]="id" class="form-label">{{ label }}</label>
      }
      <textarea
        [id]="id"
        [placeholder]="placeholder"
        [disabled]="disabled"
        [readonly]="readonly"
        [rows]="rows"
        [value]="value"
        [class]="textareaClasses"
        (input)="onInput($event)"
        (blur)="onBlur()"
        (focus)="onFocus()"
        [attr.aria-describedby]="error ? errorId : (hint ? hintId : null)"
        [attr.aria-invalid]="!!error"
      ></textarea>
      @if (hint && !error) {
        <span [id]="hintId" class="form-hint">{{ hint }}</span>
      }
      @if (error) {
        <span [id]="errorId" class="form-error">{{ error }}</span>
      }
      @if (maxLength) {
        <span class="form-counter" [class.counter-warning]="value.length > maxLength * 0.9">
          {{ value.length }}/{{ maxLength }}
        </span>
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
    textarea {
      width: 100%;
      min-height: 90px;
      padding: 9px 11px;
      border: 1px solid var(--warm);
      border-radius: 6px;
      background: var(--surface);
      color: #111;
      font-size: 11px;
      font-family: inherit;
      resize: vertical;
      transition: border-color 0.15s ease, box-shadow 0.15s ease;
    }
    textarea:hover:not(:disabled):not([readonly]) {
      border-color: #d3cbc5;
    }
    textarea:focus {
      border-color: var(--claret);
      outline: 0;
      box-shadow: var(--focus-ring);
    }
    textarea:disabled {
      background: var(--warm-light);
      color: var(--muted);
      cursor: not-allowed;
    }
    textarea[readonly] {
      background: var(--surface-hover);
    }
    textarea::placeholder {
      color: #948f92;
    }
    
    .form-field-error textarea {
      border-color: var(--danger);
    }
    .form-field-error textarea:focus {
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
    
    .form-counter {
      text-align: right;
      font-size: 9px;
      color: var(--muted);
    }
    .form-counter.counter-warning {
      color: var(--warning);
    }
  `],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TextareaComponent),
      multi: true,
    },
  ],
})
export class TextareaComponent implements ControlValueAccessor {
  @Input() id = `textarea-${Math.random().toString(36).slice(2)}`;
  @Input() label?: string;
  @Input() placeholder = '';
  @Input() disabled = false;
  @Input() readonly = false;
  @Input() rows = 4;
  @Input() maxLength?: number;
  @Input() error?: string;
  @Input() hint?: string;

  value = '';
  focused = false;

  get textareaClasses(): string {
    return '';
  }

  get errorId(): string {
    return `${this.id}-error`;
  }

  get hintId(): string {
    return `${this.id}-hint`;
  }

  onInput(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
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