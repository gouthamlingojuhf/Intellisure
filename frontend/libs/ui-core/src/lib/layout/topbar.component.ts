import { Component, Input, Output, EventEmitter, HostListener, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonComponent } from '../button/button.component';
import { BadgeComponent } from '../badge/badge.component';

export interface BreadcrumbItem {
  label: string;
  link?: string;
}

export interface Notification {
  id: string;
  title: string;
  message: string;
  time: string;
  kind: 'success' | 'warning' | 'info' | 'danger';
  read?: boolean;
}

export interface UserMenuItem {
  label: string;
  link?: string;
  action?: () => void;
  icon?: string;
  variant?: 'default' | 'danger';
}

@Component({
  selector: 'is-topbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ButtonComponent, BadgeComponent],
  template: `
    <header class="topbar" [class.topbar-mobile]="mobile">
      <div class="topbar-left">
        <is-button
          variant="icon"
          size="md"
          class="mobile-menu"
          (click)="toggleSidebar.emit()"
          aria-label="Open navigation"
          aria-expanded="true"
        >
          ☰
        </is-button>
        
        <nav class="breadcrumbs" aria-label="Breadcrumb">
          <a routerLink="/" class="breadcrumb-root">IntelliSure</a>
          @for (item of breadcrumbs; track item.label; let last = $last) {
            <span class="breadcrumb-separator" aria-hidden="true">/</span>
            @if (item.link && !last) {
              <a [routerLink]="item.link" class="breadcrumb-link">{{ item.label }}</a>
            } @else {
              <span class="breadcrumb-current">{{ item.label }}</span>
            }
          }
        </nav>
      </div>

      <div class="topbar-search" (focusin)="searchFocused = true" (focusout)="searchFocused = false">
        <span class="search-icon" aria-hidden="true">⌕</span>
        <input
          type="search"
          [(ngModel)]="searchTerm"
          (keydown.enter)="onSearch()"
          [placeholder]="searchPlaceholder"
          aria-label="Search workspace"
          class="search-input"
        />
        <kbd class="search-shortcut">⌘ K</kbd>
      </div>

      <div class="topbar-actions">
        <div class="notification-button-wrapper">
          <is-button
            variant="icon"
            size="md"
            class="notification-button"
            (click)="toggleNotifications.emit()"
            [attr.aria-expanded]="notificationsOpen"
            aria-label="Open notifications"
          >
            <span aria-hidden="true">◌</span>
            @if (unreadCount > 0) {
              <span class="notification-dot" [attr.aria-label]="unreadCount + ' unread notifications'"></span>
            }
          </is-button>
        </div>

        <div class="user-menu-wrapper" (keydown.escape)="closeUserMenu()">
          <is-button
            variant="ghost"
            size="md"
            class="user-menu-button"
            (click)="toggleUserMenu.emit()"
            [attr.aria-expanded]="userMenuOpen"
            aria-haspopup="menu"
          >
            <span class="user-avatar">{{ userInitial }}</span>
            @if (!mobile) {
              <span class="user-copy">
                <strong>{{ userName }}</strong>
                <small>{{ userRole }}</small>
              </span>
              <span aria-hidden="true">⌄</span>
            }
          </is-button>
        </div>
      </div>

      @if (notificationsOpen) {
        <div class="popover notification-popover" role="dialog" aria-label="Notifications">
          <div class="popover-header">
            <strong>Notifications</strong>
            @if (unreadCount > 0) {
              <is-button variant="text" size="sm" (click)="markAllRead.emit()">Mark all read</is-button>
            }
          </div>
          <div class="notification-list">
            @for (notification of notifications; track notification.id) {
              <div class="notification-item" [class.unread]="!notification.read">
                <span class="notification-icon" [class]="'notification-icon-' + notification.kind" aria-hidden="true">
                  @switch (notification.kind) {
                    @case ('success') { ✓ }
                    @case ('warning') { ! }
                    @case ('danger') { ! }
                    @default { i }
                  }
                </span>
                <div class="notification-content">
                  <strong>{{ notification.title }}</strong>
                  <p>{{ notification.message }}</p>
                  <small>{{ notification.time }}</small>
                </div>
              </div>
            } @empty {
              <div class="notification-empty">No notifications</div>
            }
          </div>
          <a routerLink="/notifications" class="popover-link">View all notifications</a>
        </div>
      }

      @if (userMenuOpen) {
        <div class="popover user-popover" role="menu" aria-label="User menu">
          <div class="user-popover-profile">
            <span class="user-avatar large">{{ userInitial }}</span>
            <div>
              <strong>{{ userName }}</strong>
              <small>{{ userRole }}</small>
            </div>
          </div>
          <div class="user-popover-divider"></div>
          @for (item of userMenuItems; track item.label) {
            @if (item.link) {
              <a [routerLink]="item.link" role="menuitem" (click)="closeUserMenu()">
                @if (item.icon) { <span class="menu-item-icon" aria-hidden="true">{{ item.icon }}</span> }
                {{ item.label }}
              </a>
            } @else {
              <button
                type="button"
                role="menuitem"
                class="user-menu-action"
                [class.danger]="item.variant === 'danger'"
                (click)="item.action?.(); closeUserMenu()"
              >
                @if (item.icon) { <span class="menu-item-icon" aria-hidden="true">{{ item.icon }}</span> }
                {{ item.label }}
              </button>
            }
          }
        </div>
      }
    </header>
  `,
  styles: [`
    .topbar {
      position: sticky;
      top: 0;
      z-index: 30;
      height: 72px;
      display: flex;
      align-items: center;
      gap: 24px;
      padding: 0 24px;
      background: rgba(255, 255, 255, 0.94);
      border-bottom: 1px solid var(--border);
      backdrop-filter: blur(16px);
    }
    
    .topbar-mobile {
      height: 64px;
      padding: 0 18px;
      gap: 10px;
    }
    
    .topbar-left {
      min-width: 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .mobile-menu {
      display: none;
    }
    
    .breadcrumbs {
      display: flex;
      align-items: center;
      min-width: 0;
      color: #777173;
      font-size: 12px;
      white-space: nowrap;
    }
    
    .breadcrumb-root {
      color: #777173;
      text-decoration: none;
      font-weight: 500;
    }
    .breadcrumb-root:hover {
      color: var(--claret);
    }
    
    .breadcrumb-separator {
      margin: 0 9px;
      color: #b5afb2;
    }
    
    .breadcrumb-link {
      color: #777173;
      text-decoration: none;
    }
    .breadcrumb-link:hover {
      color: var(--claret);
    }
    
    .breadcrumb-current {
      color: #242224;
      font-weight: 600;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 200px;
    }
    
    .topbar-search {
      width: min(420px, 36vw);
      height: 39px;
      display: flex;
      align-items: center;
      gap: 9px;
      margin-left: auto;
      padding: 0 11px;
      border: 1px solid var(--border);
      border-radius: 7px;
      background: var(--warm-light);
      color: #777173;
      transition: border-color 0.15s ease, box-shadow 0.15s ease;
    }
    
    .topbar-search:focus-within,
    .topbar-search.search-focused {
      border-color: var(--claret);
      box-shadow: 0 0 0 3px rgba(117, 30, 63, 0.08);
    }
    
    .search-icon {
      flex-shrink: 0;
    }
    
    .search-input {
      min-width: 0;
      flex: 1;
      border: 0;
      outline: 0;
      background: transparent;
      color: #111;
      font-size: 12px;
      font-family: inherit;
    }
    
    .search-input::placeholder {
      color: #948f92;
    }
    
    .search-shortcut {
      padding: 2px 5px;
      border: 1px solid var(--warm);
      border-radius: 4px;
      background: var(--surface);
      color: #8a8487;
      font-family: inherit;
      font-size: 10px;
      white-space: nowrap;
    }
    
    .topbar-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .notification-button-wrapper {
      position: relative;
    }
    
    .notification-dot {
      position: absolute;
      top: 7px;
      right: 7px;
      width: 7px;
      height: 7px;
      border: 2px solid var(--surface);
      border-radius: 50%;
      background: var(--fuchsia);
    }
    
    .user-menu-button {
      height: 44px;
      display: flex;
      align-items: center;
      gap: 9px;
      padding: 4px 8px 4px 5px;
      border: 0;
      border-radius: 7px;
      background: transparent;
      color: #3f3b3e;
      cursor: pointer;
    }
    
    .user-menu-button:hover {
      background: var(--warm-light);
    }
    
    .user-avatar {
      width: 34px;
      height: 34px;
      display: grid;
      place-items: center;
      flex: 0 0 34px;
      border-radius: 50%;
      background: var(--claret);
      color: #fff;
      font-size: 12px;
      font-weight: 700;
    }
    
    .user-avatar.large {
      width: 38px;
      height: 38px;
      flex-basis: 38px;
    }
    
    .user-copy {
      display: grid;
      text-align: left;
      line-height: 1.2;
    }
    
    .user-copy strong {
      font-size: 11px;
    }
    
    .user-copy small {
      margin-top: 3px;
      color: #817b7f;
      font-size: 9px;
    }
    
    .popover {
      position: absolute;
      top: 64px;
      right: 22px;
      width: 330px;
      padding: 8px;
      border: 1px solid var(--border);
      border-radius: 10px;
      background: #ffffff;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.12), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
      z-index: 70;
    }
    
    .notification-popover {
      right: 86px;
      background: #ffffff;
    }
    
    .popover-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 10px 12px;
      border-bottom: 1px solid var(--border);
      background: #ffffff;
    }
    
    .popover-header strong {
      font-size: 13px;
    }
    
    .notification-list {
      max-height: 300px;
      overflow-y: auto;
      background: #ffffff;
    }
    
    .notification-item {
      display: flex;
      gap: 11px;
      padding: 13px 10px;
      border-bottom: 1px solid var(--warm-light);
      background: #ffffff;
    }
    
    .notification-item.unread {
      background: var(--claret-soft);
    }
    
    .notification-icon {
      width: 25px;
      height: 25px;
      display: grid;
      place-items: center;
      flex: 0 0 25px;
      border-radius: 50%;
      font-size: 11px;
      font-weight: 700;
    }
    
    .notification-icon-success {
      color: var(--success);
      background: var(--success-light);
    }
    
    .notification-icon-warning {
      color: var(--warning);
      background: var(--warning-light);
    }
    
    .notification-icon-info {
      color: var(--claret);
      background: var(--claret-soft);
    }
    
    .notification-icon-danger {
      color: var(--danger);
      background: var(--danger-light);
    }
    
    .notification-content {
      flex: 1;
      min-width: 0;
    }
    
    .notification-content strong {
      font-size: 11px;
      display: block;
    }
    
    .notification-content p {
      margin: 3px 0;
      color: #5d585c;
      font-size: 10px;
      line-height: 1.4;
    }
    
    .notification-content small {
      color: #999395;
      font-size: 9px;
    }
    
    .notification-empty {
      padding: 20px;
      text-align: center;
      color: var(--muted);
      font-size: 11px;
      background: #ffffff;
    }
    
    .popover-link {
      display: block;
      padding: 10px;
      color: var(--claret);
      font-size: 10px;
      font-weight: 600;
      text-align: center;
      text-decoration: none;
      background: #ffffff;
    }
    
    .user-popover {
      width: 240px;
      background: #ffffff;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.15), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
      z-index: 70;
    }
    
    .user-popover-profile {
      display: flex;
      gap: 10px;
      align-items: center;
      padding: 10px 8px 14px;
      border-bottom: 1px solid var(--border);
      background: #ffffff;
    }
    
    .user-popover-profile div {
      display: grid;
    }
    
    .user-popover-profile strong {
      font-size: 11px;
    }
    
    .user-popover-profile small {
      margin-top: 3px;
      color: #817b7f;
      font-size: 9px;
    }
    
    .user-popover-divider {
      height: 1px;
      background: var(--border);
      margin: 4px 0;
    }
    
    .user-popover a,
    .user-popover button {
      width: 100%;
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px;
      border: 0;
      border-radius: 5px;
      background: transparent;
      color: #4b4649;
      font-size: 11px;
      text-align: left;
      text-decoration: none;
      cursor: pointer;
    }
    
    .user-popover a:hover,
    .user-popover button:hover {
      background: var(--warm-light);
      color: var(--claret);
    }
    
    .user-menu-action {
      width: 100%;
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px;
      border: 0;
      border-radius: 5px;
      background: transparent;
      color: #4b4649;
      font-size: 11px;
      text-align: left;
      cursor: pointer;
    }
    
    .user-menu-action.danger {
      color: var(--danger);
    }
    
    .user-menu-action.danger:hover {
      background: var(--danger-light);
    }
    
    .menu-item-icon {
      width: 18px;
      height: 18px;
      display: grid;
      place-items: center;
      flex-shrink: 0;
    }
    
    @media (max-width: 1100px) {
      .topbar-search {
        width: min(280px, 28vw);
      }
      .user-copy {
        display: none;
      }
      .notification-popover {
        right: 62px;
      }
    }
    
    @media (max-width: 900px) {
      .mobile-menu {
        display: inline-grid;
      }
      .breadcrumbs {
        display: none;
      }
      .topbar-search {
        margin-left: 0;
        width: auto;
        flex: 1;
      }
      .topbar {
        padding: 0 18px;
        gap: 10px;
      }
    }
    
    @media (max-width: 640px) {
      .topbar-search kbd {
        display: none;
      }
      .user-menu-button {
        padding-right: 0;
      }
      .user-menu-button > span:last-child {
        display: none;
      }
      .notification-popover,
      .user-popover {
        position: fixed;
        top: 58px;
        right: 10px;
        left: 10px;
        width: auto;
      }
    }
  `],
})
export class TopbarComponent {
  private readonly elementRef = inject(ElementRef);

  @Input() breadcrumbs: BreadcrumbItem[] = [];
  @Input() searchTerm = '';
  @Input() searchPlaceholder = 'Search policies, claims, vendors…';
  @Input() userName = 'IntelliSure User';
  @Input() userRole = 'Enterprise workspace';
  @Input() userInitial = 'U';
  @Input() notifications: Notification[] = [];
  @Input() unreadCount = 0;
  @Input() notificationsOpen = false;
  @Input() userMenuOpen = false;
  @Input() userMenuItems: UserMenuItem[] = [];
  @Input() mobile = false;

  @Output() searchTermChange = new EventEmitter<string>();
  @Output() search = new EventEmitter<string>();
  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() toggleNotifications = new EventEmitter<void>();
  @Output() toggleUserMenu = new EventEmitter<void>();
  @Output() markAllRead = new EventEmitter<void>();

  searchFocused = false;

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      if (this.userMenuOpen) {
        this.closeUserMenu();
      }
      if (this.notificationsOpen) {
        this.toggleNotifications.emit();
      }
    }
  }

  onSearch(): void {
    this.search.emit(this.searchTerm);
  }

  closeUserMenu(): void {
    this.userMenuOpen = false;
    this.toggleUserMenu.emit();
  }
}