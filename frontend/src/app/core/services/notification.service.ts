import { Injectable, inject } from '@angular/core';
import { forkJoin, map, Observable } from 'rxjs';
import { ApiService } from './api.service';
import { NotificationListResponse, NotificationResponse } from '../models/notification.models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly api = inject(ApiService);

  getNotifications(userId: string, read: boolean): Observable<NotificationListResponse> {
    return this.api.get<NotificationListResponse>('/api/notifications', {
      userId,
      read,
      page: 0,
      size: 50,
    });
  }

  getAllNotifications(userId: string): Observable<NotificationResponse[]> {
    return forkJoin({
      unread: this.getNotifications(userId, false),
      read: this.getNotifications(userId, true),
    }).pipe(
      map(({ unread, read }) => [...(unread?.items ?? []), ...(read?.items ?? [])]
        .sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? '')))
    );
  }

  getUnreadCount(userId: string): Observable<number> {
    return this.api.get<number>('/api/notifications/unread-count', { userId });
  }

  markRead(notificationId: string): Observable<NotificationResponse> {
    return this.api.patch<NotificationResponse>(`/api/notifications/${notificationId}/read`, {
      notificationId,
    });
  }
}
