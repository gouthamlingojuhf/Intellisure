import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { forkJoin, Subscription } from 'rxjs';
import { BadgeComponent, ButtonComponent, CardComponent, EmptyStateComponent, SkeletonComponent } from 'ui-core';
import { NotificationResponse } from '../../core/models/notification.models';
import { NotificationService } from '../../core/services/notification.service';
import { selectUserId } from '../../core/store/auth/auth.selectors';

@Component({
  selector: 'is-notifications',
  standalone: true,
  imports: [DatePipe, BadgeComponent, ButtonComponent, CardComponent, EmptyStateComponent, SkeletonComponent],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div>
          <p class="page-eyebrow">Workspace messages</p>
          <h1>Notifications</h1>
          <p class="page-description">Review messages associated with your authenticated account and mark them read when finished.</p>
        </div>
        <div class="header-actions">
          @if (unreadCount > 0) { <is-button variant="secondary" size="sm" (click)="markAllRead()" [disabled]="saving">Mark all read</is-button> }
          <is-button variant="secondary" size="sm" (click)="loadNotifications()" [disabled]="loading">Refresh</is-button>
        </div>
      </header>

      @if (error) {
        <div class="error-banner" role="alert"><div><strong>Notifications unavailable</strong><p>{{ error }}</p></div><is-button variant="secondary" size="sm" (click)="loadNotifications()">Try again</is-button></div>
      }
      @if (loading) {
        <div class="loading-grid" role="status" aria-live="polite"><is-skeleton variant="table-row" /><is-skeleton variant="table-row" /><is-skeleton variant="table-row" /></div>
      } @else if (!error && notifications.length === 0) {
        <is-empty-state title="No notifications" description="Messages sent to your account will appear here." icon="◌" />
      } @else {
        <is-card title="Inbox" [subtitle]="unreadCount + ' unread'">
          <div class="notification-list">
            @for (notification of notifications; track notification.notificationId) {
              <article class="notification-row" [class.unread]="!notification.read">
                <div class="notification-mark" [class]="'mark-' + kind(notification.type)" aria-hidden="true">{{ notification.read ? '✓' : '•' }}</div>
                <div class="notification-copy">
                  <div class="notification-title"><strong>{{ notification.title }}</strong><is-badge [variant]="badgeVariant(notification.type)" size="sm">{{ notification.type.replace('_', ' ') }}</is-badge></div>
                  <p>{{ notification.message }}</p>
                  <small>{{ notification.createdAt | date:'medium' }} · {{ notification.channel }}</small>
                </div>
                @if (!notification.read) { <is-button variant="text" size="sm" (click)="markRead(notification)">Mark read</is-button> }
              </article>
            }
          </div>
        </is-card>
      }
    </section>
  `,
  styles: [`
    :host { display: block; padding: 24px; }
    .enterprise-page { display: grid; gap: 20px; max-width: 1000px; margin: 0 auto; }
    .page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; }
    .page-eyebrow { margin: 0 0 7px; color: var(--claret, #75013f); font-size: 9px; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }
    .page-header h1 { margin: 0; color: var(--ink, #000); font-size: clamp(25px, 2.5vw, 34px); letter-spacing: -.045em; }
    .page-description { max-width: 720px; margin: 9px 0 0; color: var(--muted, #6f6a6d); font-size: 12px; line-height: 1.6; }
    .header-actions { display: flex; gap: 8px; }
    .notification-list { display: grid; gap: 0; }
    .notification-row { display: grid; grid-template-columns: 28px minmax(0, 1fr) auto; align-items: start; gap: 12px; padding: 16px 0; border-bottom: 1px solid var(--border, #eae5df); }
    .notification-row:last-child { border-bottom: 0; }
    .notification-row.unread { background: #fffafc; }
    .notification-mark { display: grid; place-items: center; width: 25px; height: 25px; border-radius: 50%; background: var(--warm-light, #f7f5f3); color: var(--claret, #75013f); font-weight: 700; }
    .mark-success { color: #176b45; background: #e9f6ef; } .mark-warning { color: #9a6500; background: #fff5d9; } .mark-danger { color: #9b1c1c; background: #fde8e8; }
    .notification-copy { display: grid; gap: 5px; min-width: 0; }
    .notification-title { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .notification-title strong { color: var(--ink, #000); font-size: 12px; }
    .notification-copy p { margin: 0; color: #322e30; font-size: 12px; line-height: 1.5; }
    .notification-copy small { color: var(--muted, #6f6a6d); font-size: 10px; }
    .error-banner { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 14px 18px; border: 1px solid #f8b4b4; border-radius: 8px; background: #fde8e8; color: #9b1c1c; font-size: 12px; }
    .error-banner p { margin: 3px 0 0; }
    .loading-grid { display: grid; gap: 12px; }
    @media (max-width: 680px) { :host { padding: 16px; } .page-header { align-items: flex-start; flex-direction: column; } .notification-row { grid-template-columns: 28px minmax(0, 1fr); } .notification-row is-button { grid-column: 2; justify-self: start; } }
  `],
})
export class NotificationsComponent implements OnInit, OnDestroy {
  private readonly store = inject(Store);
  private readonly notificationService = inject(NotificationService);

  notifications: NotificationResponse[] = [];
  loading = false;
  saving = false;
  error: string | null = null;
  userId: string | null = null;
  private readonly subscription = new Subscription();

  ngOnInit(): void {
    this.subscription.add(this.store.select(selectUserId).subscribe((userId) => {
      this.userId = userId || (typeof localStorage !== 'undefined' ? localStorage.getItem('is_user_id') : null);
      if (this.userId) this.loadNotifications();
    }));
  }

  ngOnDestroy(): void { this.subscription.unsubscribe(); }

  get unreadCount(): number { return this.notifications.filter((notification) => !notification.read).length; }

  loadNotifications(): void {
    if (!this.userId) { this.error = 'No authenticated user was found for this session.'; return; }
    this.loading = true;
    this.error = null;
    this.notificationService.getAllNotifications(this.userId).subscribe({
      next: (notifications) => { this.notifications = notifications; this.loading = false; },
      error: (err) => this.handleError(err),
    });
  }

  markRead(notification: NotificationResponse): void {
    this.notificationService.markRead(notification.notificationId).subscribe({
      next: (updated) => { this.notifications = this.notifications.map((item) => item.notificationId === updated.notificationId ? updated : item); },
      error: (err) => this.handleError(err),
    });
  }

  markAllRead(): void {
    const unread = this.notifications.filter((notification) => !notification.read);
    if (!unread.length) return;
    this.saving = true;
    forkJoin(unread.map((notification) => this.notificationService.markRead(notification.notificationId))).subscribe({
      next: () => { this.saving = false; this.loadNotifications(); },
      error: (err) => this.handleError(err),
    });
  }

  kind(type: string): 'success' | 'warning' | 'danger' | 'info' {
    const value = type.toUpperCase();
    if (value.includes('ERROR') || value.includes('DENIED') || value.includes('FAIL')) return 'danger';
    if (value.includes('REVIEW') || value.includes('ACTION') || value.includes('WARNING')) return 'warning';
    if (value.includes('SUCCESS') || value.includes('COMPLETE') || value.includes('ISSU')) return 'success';
    return 'info';
  }

  badgeVariant(type: string): 'success' | 'warning' | 'danger' | 'info' {
    return this.kind(type);
  }

  private handleError(err: { status?: number; error?: { message?: string }; message?: string }): void {
    this.loading = false;
    this.saving = false;
    if (err?.status === 404) {
      this.notifications = [];
      this.error = null;
      return;
    }
    this.error = err?.status === 401 ? 'Your session has expired. Please sign in again.' : err?.status === 403 ? 'You do not have permission to view these notifications.' : err?.error?.message || err?.message || 'The notification service could not be reached.';
  }
}
