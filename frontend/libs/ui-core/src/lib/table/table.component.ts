import { Component, Input, Output, EventEmitter, ContentChild, TemplateRef, AfterContentInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../button/button.component';

export interface TableColumn<T> {
  key: string;
  header: string;
  width?: string;
  align?: 'left' | 'center' | 'right';
  render?: (row: T, value: any) => string;
  sticky?: boolean | 'left' | 'right';
}

export interface TableAction<T> {
  label: string;
  icon?: string;
  handler: (row: T) => void;
  variant?: 'primary' | 'secondary' | 'ghost' | 'text';
  disabled?: (row: T) => boolean;
}

@Component({
  selector: 'is-table',
  standalone: true,
  imports: [CommonModule, ButtonComponent],
  template: `
    <div class="table-wrap" [class.table-wrap-sticky]="stickyHeader">
      <table class="table" [style.min-width.px]="minWidth">
        <thead>
          <tr>
            @for (column of columns; track column.key) {
              <th
                [style.width]="column.width"
                [style.text-align]="column.align"
                [class.sticky]="column.sticky"
                [class.sticky-left]="column.sticky === 'left'"
                [class.sticky-right]="column.sticky === 'right'"
              >
                {{ column.header }}
              </th>
            }
            @if (actions.length > 0) {
              <th style="width: 1px; white-space: nowrap;">Actions</th>
            }
          </tr>
        </thead>
        <tbody>
          @for (row of data; track trackByFn($index, row)) {
            <tr [class]="'row-' + rowIndex($index)">
              @for (column of columns; track column.key) {
                <td
                  [style.text-align]="column.align"
                  [class.table-value]="column.align === 'right'"
                >
                  @if (column.render) {
                    {{ column.render(row, getValue(row, column.key)) }}
                  } @else {
                    {{ getValue(row, column.key) }}
                  }
                </td>
              }
              @if (actions.length > 0) {
                <td class="table-actions">
                  <div class="action-group">
                    @for (action of actions; track action.label) {
                      <is-button
                        [variant]="action.variant || 'text'"
                        [size]="'sm'"
                        [disabled]="action.disabled ? action.disabled(row) : false"
                        (click)="action.handler(row)"
                      >
                        @if (action.icon) {
                          <span aria-hidden="true">{{ action.icon }}</span>
                        }
                        {{ action.label }}
                      </is-button>
                    }
                  </div>
                </td>
              }
            </tr>
          } @empty {
            @if (emptyMessage) {
              <tr>
                <td [attr.colspan]="columns.length + (actions.length > 0 ? 1 : 0)" class="table-empty">
                  {{ emptyMessage }}
                </td>
              </tr>
            }
          }
        </tbody>
      </table>
    </div>
    
    @if (pagination && data.length > 0) {
      <div class="table-pagination">
        <div class="pagination-info">
          Showing {{ (page - 1) * pageSize + 1 }} to {{ Math.min(page * pageSize, totalItems) }} of {{ totalItems }} entries
        </div>
        <div class="pagination-controls">
          <is-button variant="ghost" size="sm" [disabled]="page === 1" (click)="pageChange.emit(page - 1)">
            ← Previous
          </is-button>
          <is-button variant="ghost" size="sm" [disabled]="page * pageSize >= totalItems" (click)="pageChange.emit(page + 1)">
            Next →
          </is-button>
        </div>
      </div>
    }
  `,
  styles: [`
    :host {
      display: block;
    }
    .table-wrap {
      width: 100%;
      overflow-x: auto;
      border-radius: 8px;
      border: 1px solid var(--border);
    }
    .table-wrap-sticky {
      overflow: auto;
    }
    
    .table {
      width: 100%;
      min-width: 680px;
      border-collapse: collapse;
      font-size: 10px;
    }
    
    .table th {
      height: 40px;
      padding: 0 16px;
      border-bottom: 1px solid var(--border);
      background: var(--surface-hover);
      color: #777174;
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.04em;
      text-align: left;
      text-transform: uppercase;
      white-space: nowrap;
    }
    
    .table th.sticky {
      position: sticky;
      z-index: 1;
    }
    .table th.sticky-left { left: 0; }
    .table th.sticky-right { right: 0; }
    
    .table td {
      height: 51px;
      padding: 0 16px;
      border-bottom: 1px solid #f0edea;
      color: #625c60;
      vertical-align: middle;
    }
    
    .table tbody tr:last-child td {
      border-bottom: 0;
    }
    
    .table tbody tr:hover {
      background: #fcfbfa;
    }
    
    .table td strong {
      color: #242224;
      font-weight: 600;
    }
    
    .table-value {
      color: #252325 !important;
      font-weight: 700;
      text-align: right;
    }
    
    .table-actions {
      padding: 8px 16px !important;
    }
    
    .action-group {
      display: flex;
      gap: 4px;
      align-items: center;
    }
    
    .table-empty {
      text-align: center;
      color: var(--muted);
      font-size: 11px;
      padding: 40px 16px !important;
    }
    
    .table-pagination {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px;
      border-top: 1px solid var(--border);
      background: var(--surface-hover);
      font-size: 11px;
      color: var(--muted);
    }
    
    .pagination-controls {
      display: flex;
      gap: 8px;
    }
    
    @media (max-width: 700px) {
      .table th,
      .table td {
        padding: 0 12px;
        font-size: 9px;
      }
      .table-pagination {
        flex-direction: column;
        gap: 12px;
        text-align: center;
      }
    }
  `],
})
export class TableComponent<T = any> implements AfterContentInit {
  @Input() columns: TableColumn<T>[] = [];
  @Input() data: T[] = [];
  @Input() trackByFn: (index: number, item: T) => any = (_, item) => item;
  @Input() actions: TableAction<T>[] = [];
  @Input() stickyHeader = false;
  @Input() minWidth = 680;
  @Input() emptyMessage = 'No data available';
  
  // Pagination
  @Input() pagination = false;
  @Input() page = 1;
  @Input() pageSize = 10;
  @Input() totalItems = 0;
  @Output() pageChange = new EventEmitter<number>();

  @ContentChild('rowExpansion') rowExpansionTemplate?: TemplateRef<any>;

  ngAfterContentInit(): void {}

  rowIndex(index: number): string {
    return `row-${index}`;
  }

  getValue(row: T, key: string): any {
    return (row as Record<string, any>)[key];
  }

  Math = Math;
}