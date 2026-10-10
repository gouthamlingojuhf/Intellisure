import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  ChatChannelResponse,
  ChatContactResponse,
  ChatMessageResponse,
  CreateChannelRequest,
  SendMessageRequest
} from '../models/chat.models';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly api = inject(ApiService);

  getChannels(): Observable<ChatChannelResponse[]> {
    return this.api.get<ChatChannelResponse[]>('/api/chat/channels');
  }

  createOrGetChannel(request: CreateChannelRequest): Observable<ChatChannelResponse> {
    return this.api.post<ChatChannelResponse>('/api/chat/channels', request);
  }

  getMessages(channelId: string): Observable<ChatMessageResponse[]> {
    return this.api.get<ChatMessageResponse[]>(`/api/chat/channels/${channelId}/messages`);
  }

  sendMessage(channelId: string, content: string): Observable<ChatMessageResponse> {
    const body: SendMessageRequest = { content };
    return this.api.post<ChatMessageResponse>(`/api/chat/channels/${channelId}/messages`, body);
  }

  getContacts(): Observable<ChatContactResponse[]> {
    return this.api.get<ChatContactResponse[]>('/api/chat/contacts');
  }
}
