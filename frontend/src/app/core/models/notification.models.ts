export interface NotificationResponse {
  notificationId: string;
  userId: string;
  type: string;
  title: string;
  message: string;
  referenceType?: string | null;
  referenceId?: string | null;
  read: boolean;
  channel: string;
  readAt?: string | null;
  createdAt?: string | null;
}

export interface NotificationListResponse {
  items: NotificationResponse[];
  page: number;
  size: number;
  totalElements: number;
}
