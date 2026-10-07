import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

export interface BreadcrumbItem {
  label: string;
  link?: string;
  current?: boolean;
}

@Component({
  selector: 'is-breadcrumb',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <nav class="breadcrumb" aria-label="Breadcrumb">
      <ol class="breadcrumb-list">
        <li class="breadcrumb-item">
          <a routerLink="/" class="breadcrumb-link">IntelliSure</a>
        </li>
        @for (item of items; track item.label; let last = $last) {
          <li class="breadcrumb-item">
            <span class="breadcrumb-separator" aria-hidden="true">/</span>
            @if (item.link && !item.current && !last) {
              <a [routerLink]="item.link" class="breadcrumb-link">{{ item.label }}</a>
            } @else {
              <span class="breadcrumb-current" [attr.aria-current]="item.current ? 'page' : null">{{ item.label }}</span>
            }
          </li>
        }
      </ol>
    </nav>
  `,
  styles: [`
    .breadcrumb {
      display: flex;
      align-items: center;
    }
    
    .breadcrumb-list {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      margin: 0;
      padding: 0;
      list-style: none;
      color: #777173;
      font-size: 12px;
      white-space: nowrap;
    }
    
    .breadcrumb-item {
      display: flex;
      align-items: center;
    }
    
    .breadcrumb-link {
      color: #777173;
      text-decoration: none;
      font-weight: 500;
      transition: color 0.15s ease;
    }
    .breadcrumb-link:hover {
      color: var(--claret);
    }
    
    .breadcrumb-separator {
      margin: 0 9px;
      color: #b5afb2;
    }
    
    .breadcrumb-current {
      color: #242224;
      font-weight: 600;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 200px;
    }
    
    @media (max-width: 900px) {
      .breadcrumb {
        display: none;
      }
    }
  `],
})
export class BreadcrumbComponent {
  @Input() items: BreadcrumbItem[] = [];
}