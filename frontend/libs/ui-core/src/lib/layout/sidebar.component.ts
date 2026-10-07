import { Component, Input, Output, EventEmitter, HostListener, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ButtonComponent } from '../button/button.component';
import { BadgeComponent } from '../badge/badge.component';

export interface NavItem {
  path: string;
  label: string;
  icon?: string;
  children?: NavItem[];
  badge?: string;
  badgeVariant?: 'success' | 'warning' | 'danger' | 'info' | 'neutral';
}

@Component({
  selector: 'is-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ButtonComponent, BadgeComponent],
  template: `
    <aside
      class="app-sidebar"
      [class.collapsed]="collapsed"
      [class.mobile-open]="mobileOpen"
      aria-label="Primary navigation"
    >
      <div class="sidebar-brand">
        <a routerLink="/" class="sidebar-logo" [attr.aria-label]="brandLabel">
          <span class="logo-mark" aria-hidden="true">{{ brandIcon }}</span>
          <span class="logo-wordmark">{{ brandName }}</span>
        </a>
        <is-button
          variant="icon"
          size="md"
          [attr.aria-label]="collapsed ? 'Expand sidebar' : 'Collapse sidebar'"
          [attr.aria-expanded]="!collapsed"
          (click)="toggleSidebar.emit()"
        >
          <span aria-hidden="true">‹</span>
        </is-button>
      </div>

      <nav class="sidebar-nav">
        @if (!collapsed) {
          @for (section of navigationSections; track section.title) {
            @if (section.title) {
              <p class="sidebar-label">{{ section.title }}</p>
            }
            @for (item of section.items; track item.path) {
              <a
                class="sidebar-link"
                [routerLink]="item.path"
                routerLinkActive="active"
                [routerLinkActiveOptions]="{ exact: !item.children?.length }"
                [attr.aria-current]="isActive(item) ? 'page' : null"
                (click)="onNavClick()"
              >
                <span class="nav-icon" aria-hidden="true">
                  @if (item.icon) {
                    {{ item.icon }}
                  }
                </span>
                <span class="nav-label">{{ item.label }}</span>
                @if (item.badge) {
                  <is-badge [variant]="item.badgeVariant || 'info'" size="sm">{{ item.badge }}</is-badge>
                }
              </a>
            }
          }
        } @else {
          @for (section of navigationSections; track section.title) {
            @for (item of section.items; track item.path) {
              <a
                class="sidebar-link collapsed"
                [routerLink]="item.path"
                routerLinkActive="active"
                [routerLinkActiveOptions]="{ exact: !item.children?.length }"
                [attr.aria-label]="item.label"
                [attr.aria-current]="isActive(item) ? 'page' : null"
                (click)="onNavClick()"
              >
                <span class="nav-icon" aria-hidden="true">
                  @if (item.icon) {
                    {{ item.icon }}
                  }
                </span>
              </a>
            }
          }
        }
      </nav>

      <div class="sidebar-footer">
        <div class="system-status">
          <span class="status-dot" aria-hidden="true"></span>
          <span>All systems operational</span>
        </div>
        <div class="sidebar-help">
          <span aria-hidden="true">?</span>
          <span>Help center</span>
        </div>
      </div>
    </aside>

    @if (mobileOpen) {
      <div class="sidebar-backdrop" (click)="closeMobile.emit()" aria-hidden="true"></div>
    }
  `,
  styles: [`
    :host {
      display: block;
    }
    
    .app-sidebar {
      position: fixed;
      inset: 0 auto 0 0;
      z-index: 40;
      width: 264px;
      display: flex;
      flex-direction: column;
      background: var(--surface);
      border-right: 1px solid var(--border);
      transition: width 0.22s ease, transform 0.22s ease;
    }
    
    .app-sidebar.collapsed:not(.mobile-open) {
      width: 76px;
    }
    
    .app-sidebar.mobile-open {
      transform: translateX(0);
      box-shadow: 18px 0 48px rgba(0, 0, 0, 0.18);
    }
    
    .sidebar-brand {
      min-height: 72px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 18px;
      border-bottom: 1px solid var(--border);
    }
    
    .sidebar-logo {
      display: flex;
      align-items: center;
      gap: 11px;
      min-width: 0;
      text-decoration: none;
    }
    
    .logo-mark {
      width: 34px;
      height: 34px;
      display: grid;
      place-items: center;
      flex: 0 0 34px;
      border-radius: 8px 8px 8px 2px;
      color: #fff;
      background: var(--claret);
      font-size: 17px;
      font-weight: 700;
      box-shadow: 0 5px 14px rgba(117, 30, 63, 0.2);
    }
    
    .logo-wordmark {
      color: var(--ink);
      font-size: 17px;
      font-weight: 700;
      letter-spacing: -0.035em;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .app-sidebar.collapsed:not(.mobile-open) .logo-wordmark,
    .app-sidebar.collapsed:not(.mobile-open) .sidebar-label,
    .app-sidebar.collapsed:not(.mobile-open) .nav-label,
    .app-sidebar.collapsed:not(.mobile-open) .sidebar-footer {
      display: none;
    }
    
    .app-sidebar.collapsed:not(.mobile-open) .sidebar-brand {
      justify-content: center;
      padding-inline: 10px;
    }
    
    .sidebar-nav {
      flex: 1;
      padding: 24px 12px;
      overflow-y: auto;
    }
    
    .sidebar-label {
      margin: 0 12px 10px;
      color: #8a8487;
      font-size: 10px;
      font-weight: 700;
      letter-spacing: 0.13em;
      text-transform: uppercase;
    }
    
    .sidebar-link {
      position: relative;
      min-height: 43px;
      display: flex;
      align-items: center;
      gap: 12px;
      margin: 3px 0;
      padding: 0 12px;
      border-radius: 7px;
      color: #555052;
      text-decoration: none;
      font-size: 13px;
      font-weight: 500;
      transition: all 0.15s ease;
    }
    
    .sidebar-link:hover {
      background: var(--warm-light);
      color: var(--claret);
    }
    
    .sidebar-link.active {
      background: var(--claret-soft);
      color: var(--claret);
      font-weight: 600;
    }
    
    .sidebar-link.active::before {
      content: '';
      position: absolute;
      left: -12px;
      width: 3px;
      height: 24px;
      border-radius: 0 3px 3px 0;
      background: var(--claret);
    }
    
    .nav-icon {
      width: 24px;
      height: 24px;
      display: grid;
      place-items: center;
      flex: 0 0 24px;
      border: 1px solid var(--border);
      border-radius: 6px;
      background: var(--surface);
      color: currentColor;
      font-size: 10px;
      font-weight: 700;
    }
    
    .sidebar-link.active .nav-icon {
      border-color: transparent;
      background: var(--claret);
      color: #fff;
    }
    
    .sidebar-link.collapsed {
      justify-content: center;
      padding: 0 10px;
    }
    
    .sidebar-link.collapsed .nav-label {
      display: none;
    }
    
    .sidebar-footer {
      padding: 16px 18px;
      border-top: 1px solid var(--border);
      color: #716c70;
      font-size: 11px;
    }
    
    .system-status,
    .sidebar-help {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 7px 0;
    }
    
    .status-dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: #2f8b5f;
      box-shadow: 0 0 0 3px #e7f4ed;
    }
    
    .sidebar-help {
      color: var(--claret);
      font-weight: 600;
    }
    
    .sidebar-help span:first-child {
      width: 20px;
      height: 20px;
      display: grid;
      place-items: center;
      border: 1px solid #d8abc0;
      border-radius: 50%;
    }
    
    .sidebar-backdrop {
      display: none;
      position: fixed;
      inset: 0;
      z-index: 35;
      background: rgba(0, 0, 0, 0.35);
    }
    
    @media (max-width: 900px) {
      .app-sidebar {
        transform: translateX(-100%);
        width: 264px;
      }
      .app-sidebar.collapsed {
        width: 264px;
      }
      .app-sidebar.collapsed .logo-wordmark,
      .app-sidebar.collapsed .sidebar-label,
      .app-sidebar.collapsed .nav-label,
      .app-sidebar.collapsed .sidebar-footer {
        display: initial;
      }
      .app-sidebar.collapsed .sidebar-brand {
        justify-content: space-between;
        padding-inline: 18px;
      }
      .sidebar-backdrop {
        display: block;
        opacity: 0;
        pointer-events: none;
      }
      .app-sidebar.mobile-open ~ .sidebar-backdrop,
      .app-sidebar.mobile-open + .sidebar-backdrop {
        opacity: 1;
        pointer-events: auto;
      }
    }
  `],
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Input() mobileOpen = false;
  @Input() brandName = 'IntelliSure';
  @Input() brandIcon = 'I';
  @Input() brandLabel = 'IntelliSure home';
  @Input() navigationSections: { title?: string; items: NavItem[] }[] = [];
  @Input() currentPath = '';

  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() closeMobile = new EventEmitter<void>();

  onNavClick(): void {
    if (this.mobileOpen) {
      this.closeMobile.emit();
    }
  }

  isActive(item: NavItem): boolean {
    return this.currentPath === item.path || this.currentPath.startsWith(item.path + '/');
  }
}