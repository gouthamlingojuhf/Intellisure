export type ChatChannelType = 'DIRECT' | 'INTERNAL_GROUP' | 'CUSTOMER_SUPPORT';

export interface ChatParticipantResponse {
  participantId: string;
  userId: string;
  displayName: string;
  role: string;
  joinedAt: string;
  lastReadAt?: string;
}

export interface ChatMessageResponse {
  messageId: string;
  channelId: string;
  senderUserId: string;
  senderDisplayName: string;
  senderRole: string;
  content: string;
  sentAt: string;
}

export interface ChatChannelResponse {
  channelId: string;
  name: string;
  channelType: ChatChannelType;
  entityType?: string;
  entityId?: string;
  createdBy?: string;
  createdAt: string;
  updatedAt: string;
  participants: ChatParticipantResponse[];
  lastMessage?: ChatMessageResponse;
  unreadCount: number;
}

export interface CreateChannelRequest {
  name: string;
  channelType: ChatChannelType;
  entityType?: string;
  entityId?: string;
  participantUserIds?: string[];
}

export interface SendMessageRequest {
  content: string;
}

export interface ChatContactResponse {
  userId: string;
  displayName: string;
  email: string;
  role: string;
  context: string;
  entityType?: string;
  entityId?: string;
}
