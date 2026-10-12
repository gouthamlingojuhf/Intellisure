import { Injectable, inject } from '@angular/core';
import { forkJoin, map, Observable } from 'rxjs';
import { ApiService } from './api.service';
import { NotificationListResponse, NotificationResponse } from '../models/notification.models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly api = inject(ApiService);

  getNotifications(userId: string, read?: boolean): Observable<NotificationListResponse> {
    const params: any = { userId, page: 0, size: 100 };
    if (read !== undefined) {
      params.read = read;
    }
    return this.api.get<NotificationListResponse>('/api/notifications', params);
  }

  getAllNotifications(userId: string): Observable<NotificationResponse[]> {
    return this.getNotifications(userId).pipe(
      map((res) => (res?.items ?? []).sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? '')))
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
