import { CommonModule, DatePipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Subscription, interval } from 'rxjs';
import {
  BadgeComponent,
  ButtonComponent,
  CardComponent,
  EmptyStateComponent,
  SkeletonComponent
} from 'ui-core';
import {
  ChatChannelResponse,
  ChatContactResponse,
  ChatMessageResponse,
  CreateChannelRequest
} from '../../core/models/chat.models';
import { ChatService } from '../../core/services/chat.service';
import { selectUserId, selectUserRole } from '../../core/store/auth/auth.selectors';

@Component({
  selector: 'is-chat-workspace',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    FormsModule,
    BadgeComponent,
    ButtonComponent,
    CardComponent,
    EmptyStateComponent,
    SkeletonComponent
  ],
  template: `
    <section class="enterprise-page">
      <!-- Header -->
      <header class="page-header">
        <div>
          <p class="page-eyebrow">Enterprise Communications</p>
          <h1>Secure Operations & Support Chat</h1>
          <p class="page-description">
            Direct real-time collaboration between insurance underwriters, claims adjusters, operational staff, and policyholders.
          </p>
        </div>
        <div class="header-actions">
          <is-button variant="primary" size="sm" (click)="openNewChatModal()">+ New Conversation</is-button>
          <is-button variant="secondary" size="sm" (click)="loadChannels()" [disabled]="loadingChannels">Refresh</is-button>
        </div>
      </header>

      <!-- Main Layout -->
      <div class="chat-workspace-grid">
        <!-- Sidebar Channels & Conversations -->
        <aside class="chat-sidebar">
          <div class="sidebar-header">
            <input
              type="text"
              class="search-input"
              placeholder="Search conversations…"
              [(ngModel)]="searchQuery"
            />
          </div>

          @if (loadingChannels && channels.length === 0) {
            <div class="loading-padding">
              <is-skeleton variant="table-row" />
              <is-skeleton variant="table-row" />
              <is-skeleton variant="table-row" />
            </div>
          } @else if (filteredChannels.length === 0) {
            <div class="empty-channels">
              <p>No conversations found</p>
              <is-button variant="text" size="sm" (click)="openNewChatModal()">Start one now</is-button>
            </div>
          } @else {
            <div class="channel-sections">
              <!-- Internal Channels (if any) -->
              @if (teamChannels.length > 0) {
                <div class="channel-group">
                  <div class="group-title">TEAM CHANNELS</div>
                  @for (ch of teamChannels; track ch.channelId) {
                    <div
                      class="channel-item"
                      [class.active]="selectedChannel?.channelId === ch.channelId"
                      (click)="selectChannel(ch)"
                    >
                      <div class="channel-icon">#</div>
                      <div class="channel-info">
                        <div class="channel-top">
                          <span class="channel-name">{{ ch.name }}</span>
                          @if (ch.unreadCount > 0) {
                            <span class="unread-pill">{{ ch.unreadCount }}</span>
                          }
                        </div>
                        <div class="channel-snippet">
                          {{ ch.lastMessage ? ch.lastMessage.content : 'No messages yet' }}
                        </div>
                      </div>
                    </div>
                  }
                </div>
              }

              <!-- Direct / Support Conversations -->
              @if (directChannels.length > 0) {
                <div class="channel-group">
                  <div class="group-title">DIRECT & SUPPORT CHATS</div>
                  @for (ch of directChannels; track ch.channelId) {
                    <div
                      class="channel-item"
                      [class.active]="selectedChannel?.channelId === ch.channelId"
                      (click)="selectChannel(ch)"
                    >
                      <div class="channel-avatar">
                        {{ getChannelInitial(ch) }}
                      </div>
                      <div class="channel-info">
                        <div class="channel-top">
                          <span class="channel-name">{{ getChannelDisplayName(ch) }}</span>
                          @if (ch.unreadCount > 0) {
                            <span class="unread-pill">{{ ch.unreadCount }}</span>
                          }
                        </div>
                        <div class="channel-meta">
                          <is-badge [variant]="ch.channelType === 'CUSTOMER_SUPPORT' ? 'warning' : 'neutral'" size="sm">
                            {{ ch.channelType === 'CUSTOMER_SUPPORT' ? 'Support' : 'Direct' }}
                          </is-badge>
                          <span class="time-ago">{{ formatTime(ch.updatedAt) }}</span>
                        </div>
                        <div class="channel-snippet">
                          {{ ch.lastMessage ? ch.lastMessage.content : 'No messages yet' }}
                        </div>
                      </div>
                    </div>
                  }
                </div>
              }
            </div>
          }
        </aside>

        <!-- Chat Conversation Area -->
        <main class="chat-main">
          @if (!selectedChannel) {
            <div class="no-channel-selected">
              <is-empty-state
                title="Select a Conversation"
                description="Choose an existing channel or start a new direct inquiry to chat."
                icon="💬"
              />
              <div class="center-action">
                <is-button variant="primary" size="md" (click)="openNewChatModal()">Start New Conversation</is-button>
              </div>
            </div>
          } @else {
            <!-- Chat Header -->
            <div class="chat-header">
              <div class="header-info">
                <div class="header-name-row">
                  <h2>{{ getChannelDisplayName(selectedChannel) }}</h2>
                  <is-badge [variant]="getChannelBadgeVariant(selectedChannel)" size="sm">
                    {{ selectedChannel.channelType.replace('_', ' ') }}
                  </is-badge>
                  @if (selectedChannel.entityType && selectedChannel.entityId) {
                    <span class="context-tag">{{ selectedChannel.entityType }}: {{ selectedChannel.entityId }}</span>
                  }
                </div>
                <div class="participants-summary">
                  @for (p of selectedChannel.participants; track p.participantId) {
                    <span class="participant-pill" [class.is-self]="p.userId === currentUserId">
                      {{ p.displayName }} ({{ p.role }})
                    </span>
                  }
                </div>
              </div>
              <div class="header-refresh">
                <is-button variant="secondary" size="sm" (click)="refreshActiveMessages()" [disabled]="loadingMessages">
                  Refresh
                </is-button>
              </div>
            </div>

            <!-- Messages Thread -->
            <div class="chat-messages-container" #messagesContainer>
              @if (loadingMessages && messages.length === 0) {
                <div class="loading-padding">
                  <is-skeleton variant="card" />
                  <is-skeleton variant="card" />
                </div>
              } @else if (messages.length === 0) {
                <div class="empty-messages">
                  <p class="empty-icon">✉️</p>
                  <h3>No messages yet</h3>
                  <p>Send the first message to initiate this conversation.</p>
                </div>
              } @else {
                <div class="messages-list">
                  @for (msg of messages; track msg.messageId) {
                    <div
                      class="message-row"
                      [class.message-self]="msg.senderUserId === currentUserId"
                      [class.message-other]="msg.senderUserId !== currentUserId"
                    >
                      @if (msg.senderUserId !== currentUserId) {
                        <div class="sender-avatar" [ngClass]="getRoleAvatarClass(msg.senderRole)">
                          {{ (msg.senderDisplayName || 'U').charAt(0).toUpperCase() }}
                        </div>
                      }
                      <div class="message-bubble-wrapper">
                        <div class="message-meta">
                          <span class="sender-name">{{ msg.senderDisplayName }}</span>
                          <span class="sender-role" [ngClass]="getRoleClass(msg.senderRole)">{{ msg.senderRole }}</span>
                          <span class="message-time">{{ msg.sentAt | date:'shortTime' }}</span>
                        </div>
                        <div class="message-bubble">
                          {{ msg.content }}
                        </div>
                      </div>
                    </div>
                  }
                </div>
              }
            </div>

            <!-- Message Composer -->
            <div class="chat-composer">
              <form (ngSubmit)="sendCurrentMessage()" class="composer-form">
                <textarea
                  class="composer-textarea"
                  placeholder="Type a message (Press Enter to send, Shift+Enter for newline)…"
                  rows="2"
                  [(ngModel)]="messageInput"
                  name="messageInput"
                  (keydown.enter)="onEnterKey($event)"
                  [disabled]="sending"
                ></textarea>
                <div class="composer-actions">
                  <is-button
                    type="submit"
                    variant="primary"
                    size="sm"
                    [disabled]="sending || !messageInput.trim()"
                  >
                    {{ sending ? 'Sending…' : 'Send →' }}
                  </is-button>
                </div>
              </form>
            </div>
          }
        </main>
      </div>

      <!-- New Conversation Modal -->
      @if (showNewChatModal) {
        <div class="modal-backdrop" (click)="closeNewChatModal()">
          <div class="modal-card" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h3>Start New Conversation</h3>
              <button class="close-btn" (click)="closeNewChatModal()">&times;</button>
            </div>

            <div class="modal-body">
              <!-- Internal Group Channel Creation (for Staff) -->
              @if (isEmployee) {
                <div class="group-create-box">
                  <h4>Create Operational Team Channel</h4>
                  <div class="group-input-row">
                    <input
                      type="text"
                      class="search-input"
                      placeholder="e.g. #claims-fraud-investigation"
                      [(ngModel)]="newChannelName"
                    />
                    <is-button
                      variant="primary"
                      size="sm"
                      [disabled]="!newChannelName.trim() || creatingChannel"
                      (click)="createInternalGroupChannel()"
                    >
                      Create Channel
                    </is-button>
                  </div>
                </div>
                <div class="divider"><span>OR SELECT A RECIPIENT</span></div>
              }

              <!-- Contact Picker -->
              <h4>Available Contacts & Assigned Representatives</h4>
              @if (loadingContacts) {
                <is-skeleton variant="table-row" />
                <is-skeleton variant="table-row" />
              } @else if (contacts.length === 0) {
                <p class="muted-text">No assigned representatives or contacts found.</p>
              } @else {
                <div class="contacts-list">
                  @for (c of contacts; track c.userId) {
                    <div class="contact-item" (click)="startChatWithContact(c)">
                      <div class="contact-avatar" [ngClass]="getRoleAvatarClass(c.role)">
                        {{ c.displayName.charAt(0).toUpperCase() }}
                      </div>
                      <div class="contact-details">
                        <div class="contact-name-row">
                          <strong>{{ c.displayName }}</strong>
                          <span class="role-badge" [ngClass]="getRoleClass(c.role)">{{ c.role }}</span>
                        </div>
                        <p class="contact-context">{{ c.context }}</p>
                      </div>
                      <is-button variant="secondary" size="sm">Message →</is-button>
                    </div>
                  }
                </div>
              }
            </div>
          </div>
        </div>
      }
    </section>
  `,
  styles: [`
    :host { display: block; padding: 24px; }
    .enterprise-page { display: grid; gap: 20px; max-width: 1200px; margin: 0 auto; }
    .page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; }
    .page-eyebrow { margin: 0 0 6px; color: var(--claret, #75013f); font-size: 9px; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }
    .page-header h1 { margin: 0; color: var(--ink, #000); font-size: clamp(24px, 2.2vw, 32px); letter-spacing: -.04em; }
    .page-description { max-width: 700px; margin: 8px 0 0; color: var(--muted, #6f6a6d); font-size: 13px; line-height: 1.5; }
    .header-actions { display: flex; gap: 8px; }

    /* Workspace Layout */
    .chat-workspace-grid {
      display: grid;
      grid-template-columns: 320px minmax(0, 1fr);
      height: 680px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 12px;
      background: #fff;
      overflow: hidden;
      box-shadow: 0 2px 10px rgba(0,0,0,0.04);
    }

    /* Sidebar */
    .chat-sidebar {
      border-right: 1px solid var(--border, #eae5df);
      background: #faf9f7;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }
    .sidebar-header { padding: 14px; border-bottom: 1px solid var(--border, #eae5df); }
    .search-input {
      width: 100%;
      padding: 8px 12px;
      border: 1px solid #d8d3cb;
      border-radius: 6px;
      font-size: 12px;
      box-sizing: border-box;
      outline: none;
      background: #fff;
    }
    .search-input:focus { border-color: var(--claret, #75013f); }
    .channel-sections { overflow-y: auto; flex: 1; padding: 10px 0; }
    .channel-group { margin-bottom: 16px; }
    .group-title { padding: 6px 16px; font-size: 10px; font-weight: 700; letter-spacing: .08em; color: var(--muted, #6f6a6d); }
    .channel-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 16px;
      cursor: pointer;
      border-left: 3px solid transparent;
      transition: background 0.15s, border-color 0.15s;
    }
    .channel-item:hover { background: #f2eee8; }
    .channel-item.active { background: #eae5df; border-left-color: var(--claret, #75013f); font-weight: 600; }
    .channel-icon {
      width: 28px;
      height: 28px;
      border-radius: 6px;
      background: #eae5df;
      display: grid;
      place-items: center;
      font-weight: 700;
      color: #555;
      font-size: 14px;
    }
    .channel-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #75013f;
      color: #fff;
      display: grid;
      place-items: center;
      font-weight: 700;
      font-size: 13px;
      flex-shrink: 0;
    }
    .channel-info { flex: 1; min-width: 0; }
    .channel-top { display: flex; justify-content: space-between; align-items: center; gap: 6px; }
    .channel-name { font-size: 13px; color: #111; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .unread-pill {
      background: var(--claret, #75013f);
      color: #fff;
      font-size: 10px;
      font-weight: 700;
      padding: 1px 6px;
      border-radius: 10px;
    }
    .channel-meta { display: flex; align-items: center; gap: 8px; margin: 2px 0; }
    .time-ago { font-size: 10px; color: #888; }
    .channel-snippet {
      font-size: 11px;
      color: #666;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .empty-channels { padding: 32px 16px; text-align: center; color: #888; font-size: 13px; }

    /* Main Area */
    .chat-main {
      display: flex;
      flex-direction: column;
      height: 100%;
      background: #fff;
      overflow: hidden;
    }
    .no-channel-selected {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px;
    }
    .center-action { margin-top: 16px; }

    /* Chat Header */
    .chat-header {
      padding: 14px 20px;
      border-bottom: 1px solid var(--border, #eae5df);
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      background: #fff;
    }
    .header-info { display: flex; flex-direction: column; gap: 4px; }
    .header-name-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
    .header-name-row h2 { margin: 0; font-size: 16px; font-weight: 700; color: #111; }
    .context-tag { font-size: 11px; color: #666; background: #f3efe9; padding: 2px 6px; border-radius: 4px; }
    .participants-summary { display: flex; gap: 6px; flex-wrap: wrap; }
    .participant-pill {
      font-size: 10px;
      padding: 2px 6px;
      background: #f5f3f0;
      border-radius: 4px;
      color: #555;
    }
    .participant-pill.is-self { background: #eaf3ff; color: #0b57d0; font-weight: 600; }

    /* Messages Thread */
    .chat-messages-container {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
      display: flex;
      flex-direction: column;
      gap: 16px;
      background: #faf9f8;
    }
    .empty-messages {
      margin: auto;
      text-align: center;
      color: #777;
    }
    .empty-icon { font-size: 32px; margin: 0 0 8px; }
    .empty-messages h3 { margin: 0 0 4px; font-size: 16px; color: #333; }
    .empty-messages p { margin: 0; font-size: 12px; }

    .messages-list { display: flex; flex-direction: column; gap: 14px; }
    .message-row { display: flex; gap: 10px; max-width: 80%; }
    .message-self {
      align-self: flex-end;
      flex-direction: row-reverse;
    }
    .message-other { align-self: flex-start; }
    .sender-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: grid;
      place-items: center;
      font-size: 12px;
      font-weight: 700;
      color: #fff;
      flex-shrink: 0;
    }
    .message-bubble-wrapper { display: flex; flex-direction: column; gap: 3px; }
    .message-self .message-bubble-wrapper { align-items: flex-end; }
    .message-meta { display: flex; align-items: center; gap: 6px; font-size: 11px; }
    .sender-name { font-weight: 600; color: #222; }
    .sender-role { font-size: 9px; padding: 1px 5px; border-radius: 3px; font-weight: 700; text-transform: uppercase; }
    .message-time { color: #888; font-size: 10px; }
    .message-bubble {
      padding: 10px 14px;
      border-radius: 12px;
      font-size: 13px;
      line-height: 1.45;
      word-break: break-word;
      white-space: pre-wrap;
    }
    .message-self .message-bubble {
      background: var(--claret, #75013f);
      color: #fff;
      border-bottom-right-radius: 2px;
    }
    .message-other .message-bubble {
      background: #fff;
      color: #111;
      border: 1px solid #e5e1db;
      border-bottom-left-radius: 2px;
    }

    /* Composer */
    .chat-composer {
      padding: 14px 20px;
      border-top: 1px solid var(--border, #eae5df);
      background: #fff;
    }
    .composer-form { display: flex; gap: 12px; align-items: flex-end; }
    .composer-textarea {
      flex: 1;
      padding: 10px 12px;
      border: 1px solid #d8d3cb;
      border-radius: 8px;
      font-family: inherit;
      font-size: 13px;
      resize: none;
      outline: none;
    }
    .composer-textarea:focus { border-color: var(--claret, #75013f); }
    .composer-actions { display: flex; }

    /* Modal */
    .modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.45);
      backdrop-filter: blur(2px);
      z-index: 1000;
      display: grid;
      place-items: center;
      padding: 20px;
    }
    .modal-card {
      width: 100%;
      max-width: 560px;
      max-height: 85vh;
      background: #fff;
      border-radius: 12px;
      display: flex;
      flex-direction: column;
      box-shadow: 0 10px 30px rgba(0,0,0,0.15);
      overflow: hidden;
    }
    .modal-header {
      padding: 16px 20px;
      border-bottom: 1px solid #eee;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .modal-header h3 { margin: 0; font-size: 16px; color: #111; }
    .close-btn { background: none; border: none; font-size: 22px; cursor: pointer; color: #888; }
    .modal-body { padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; }
    .group-create-box {
      padding: 14px;
      background: #fbf9f6;
      border: 1px solid #eae5df;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .group-create-box h4, .modal-body h4 { margin: 0; font-size: 13px; color: #333; }
    .group-input-row { display: flex; gap: 8px; }
    .divider {
      text-align: center;
      position: relative;
      margin: 8px 0;
    }
    .divider::before {
      content: '';
      position: absolute;
      top: 50%;
      left: 0;
      right: 0;
      border-top: 1px solid #eae5df;
    }
    .divider span {
      position: relative;
      background: #fff;
      padding: 0 10px;
      font-size: 10px;
      font-weight: 700;
      color: #999;
      letter-spacing: .08em;
    }
    .contacts-list { display: flex; flex-direction: column; gap: 8px; }
    .contact-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 14px;
      border: 1px solid #eae5df;
      border-radius: 8px;
      cursor: pointer;
      transition: background 0.15s, border-color 0.15s;
    }
    .contact-item:hover { background: #fbf9f6; border-color: #d0c8be; }
    .contact-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: grid;
      place-items: center;
      color: #fff;
      font-weight: 700;
      font-size: 14px;
      flex-shrink: 0;
    }
    .contact-details { flex: 1; min-width: 0; }
    .contact-name-row { display: flex; align-items: center; gap: 8px; }
    .contact-context { margin: 3px 0 0; font-size: 11px; color: #666; }
    .role-badge { font-size: 9px; font-weight: 700; padding: 1px 5px; border-radius: 3px; text-transform: uppercase; }

    /* Role-based color coding */
    .role-underwriter { background: #e8eaf6; color: #283593; }
    .avatar-underwriter { background: #283593; }
    .role-adjuster { background: #fff3e0; color: #e65100; }
    .avatar-adjuster { background: #e65100; }
    .role-policyholder { background: #e0f2f1; color: #00695c; }
    .avatar-policyholder { background: #00695c; }
    .role-admin { background: #fce4ec; color: #880e4f; }
    .avatar-admin { background: #880e4f; }
    .role-default { background: #f5f5f5; color: #424242; }
    .avatar-default { background: #616161; }

    .loading-padding { padding: 16px; display: flex; flex-direction: column; gap: 8px; }
    .muted-text { color: #888; font-size: 12px; }

    @media (max-width: 768px) {
      .chat-workspace-grid { grid-template-columns: 1fr; height: auto; }
      .chat-sidebar { max-height: 250px; }
      .chat-messages-container { height: 380px; }
    }
  `]
})
export class ChatWorkspaceComponent implements OnInit, OnDestroy {
  private readonly chatService = inject(ChatService);
  private readonly store = inject(Store);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  @ViewChild('messagesContainer') private messagesContainer?: ElementRef;

  currentUserId: string | null = null;
  currentUserRole: string | null = null;

  channels: ChatChannelResponse[] = [];
  selectedChannel: ChatChannelResponse | null = null;
  messages: ChatMessageResponse[] = [];
  contacts: ChatContactResponse[] = [];

  loadingChannels = false;
  loadingMessages = false;
  loadingContacts = false;
  sending = false;
  creatingChannel = false;

  searchQuery = '';
  messageInput = '';
  newChannelName = '';
  showNewChatModal = false;

  private pollSubscription?: Subscription;
  private userSub?: Subscription;

  get isEmployee(): boolean {
    const role = (this.currentUserRole ?? '').toUpperCase();
    return role.includes('UNDERWRITER') ||
           role.includes('ADJUSTER') ||
           role.includes('MANAGER') ||
           role.includes('ADMIN') ||
           role.includes('RISK');
  }

  get filteredChannels(): ChatChannelResponse[] {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return this.channels;
    return this.channels.filter(ch =>
      ch.name.toLowerCase().includes(q) ||
      (ch.lastMessage && ch.lastMessage.content.toLowerCase().includes(q))
    );
  }

  get teamChannels(): ChatChannelResponse[] {
    return this.filteredChannels.filter(ch => ch.channelType === 'INTERNAL_GROUP');
  }

  get directChannels(): ChatChannelResponse[] {
    return this.filteredChannels.filter(ch => ch.channelType !== 'INTERNAL_GROUP');
  }

  ngOnInit(): void {
    this.userSub = this.store.select(selectUserId).subscribe(userId => {
      this.currentUserId = userId;
    });
    this.store.select(selectUserRole).subscribe(role => {
      this.currentUserRole = role;
    });

    this.loadChannels(() => {
      // Handle query parameters if directed from Quote or Claim
      this.route.queryParams.subscribe(params => {
        const targetChannelId = params['channelId'];
        const recipientId = params['recipientId'];
        const entityType = params['entityType'];
        const entityId = params['entityId'];

        if (targetChannelId) {
          const found = this.channels.find(c => c.channelId === targetChannelId);
          if (found) this.selectChannel(found);
        } else if (recipientId) {
          this.initiateChatFromParams(recipientId, params['name'], entityType, entityId);
        } else if (this.channels.length > 0 && !this.selectedChannel) {
          this.selectChannel(this.channels[0]);
        }
      });
    });

    // Start reactive polling every 3 seconds for seamless chat updates
    this.pollSubscription = interval(3000).subscribe(() => {
      if (this.selectedChannel) {
        this.silentRefreshMessages(this.selectedChannel.channelId);
      }
    });
  }

  ngOnDestroy(): void {
    this.pollSubscription?.unsubscribe();
    this.userSub?.unsubscribe();
  }

  loadChannels(callback?: () => void): void {
    this.loadingChannels = true;
    this.chatService.getChannels().subscribe({
      next: (channels) => {
        this.channels = channels;
        this.loadingChannels = false;
        if (callback) callback();
      },
      error: () => {
        this.loadingChannels = false;
      }
    });
  }

  selectChannel(channel: ChatChannelResponse): void {
    this.selectedChannel = channel;
    this.loadMessages(channel.channelId);
  }

  loadMessages(channelId: string): void {
    this.loadingMessages = true;
    this.chatService.getMessages(channelId).subscribe({
      next: (msgs) => {
        this.messages = msgs;
        this.loadingMessages = false;
        this.scrollToBottom();
      },
      error: () => {
        this.loadingMessages = false;
      }
    });
  }

  refreshActiveMessages(): void {
    if (this.selectedChannel) {
      this.loadMessages(this.selectedChannel.channelId);
    }
  }

  private silentRefreshMessages(channelId: string): void {
    this.chatService.getMessages(channelId).subscribe({
      next: (msgs) => {
        if (msgs.length !== this.messages.length) {
          this.messages = msgs;
          this.scrollToBottom();
        }
      }
    });
  }

  onEnterKey(event: Event): void {
    const keyEvent = event as KeyboardEvent;
    if (!keyEvent.shiftKey) {
      event.preventDefault();
      this.sendCurrentMessage();
    }
  }

  sendCurrentMessage(): void {
    if (!this.selectedChannel || !this.messageInput.trim() || this.sending) return;

    const content = this.messageInput.trim();
    this.messageInput = '';
    this.sending = true;

    this.chatService.sendMessage(this.selectedChannel.channelId, content).subscribe({
      next: (sentMsg) => {
        this.messages.push(sentMsg);
        this.sending = false;
        this.scrollToBottom();
      },
      error: () => {
        this.sending = false;
      }
    });
  }

  openNewChatModal(): void {
    this.showNewChatModal = true;
    this.loadingContacts = true;
    this.chatService.getContacts().subscribe({
      next: (contacts) => {
        this.contacts = contacts;
        this.loadingContacts = false;
      },
      error: () => {
        this.loadingContacts = false;
      }
    });
  }

  closeNewChatModal(): void {
    this.showNewChatModal = false;
    this.newChannelName = '';
  }

  createInternalGroupChannel(): void {
    if (!this.newChannelName.trim()) return;
    this.creatingChannel = true;
    const req: CreateChannelRequest = {
      name: this.newChannelName.trim(),
      channelType: 'INTERNAL_GROUP'
    };
    this.chatService.createOrGetChannel(req).subscribe({
      next: (channel) => {
        this.creatingChannel = false;
        this.closeNewChatModal();
        this.channels.unshift(channel);
        this.selectChannel(channel);
      },
      error: () => {
        this.creatingChannel = false;
      }
    });
  }

  startChatWithContact(contact: ChatContactResponse): void {
    const isSupport = !this.isEmployee;
    const req: CreateChannelRequest = {
      name: contact.displayName,
      channelType: isSupport ? 'CUSTOMER_SUPPORT' : 'DIRECT',
      entityType: contact.entityType,
      entityId: contact.entityId,
      participantUserIds: [contact.userId]
    };

    this.chatService.createOrGetChannel(req).subscribe({
      next: (channel) => {
        this.closeNewChatModal();
        const existingIdx = this.channels.findIndex(c => c.channelId === channel.channelId);
        if (existingIdx >= 0) {
          this.channels[existingIdx] = channel;
        } else {
          this.channels.unshift(channel);
        }
        this.selectChannel(channel);
      }
    });
  }

  private initiateChatFromParams(
    recipientId: string,
    name?: string,
    entityType?: string,
    entityId?: string
  ): void {
    const isSupport = !this.isEmployee;
    const req: CreateChannelRequest = {
      name: name || 'Support Conversation',
      channelType: isSupport ? 'CUSTOMER_SUPPORT' : 'DIRECT',
      entityType,
      entityId,
      participantUserIds: [recipientId]
    };

    this.chatService.createOrGetChannel(req).subscribe({
      next: (channel) => {
        const existing = this.channels.find(c => c.channelId === channel.channelId);
        if (!existing) this.channels.unshift(channel);
        this.selectChannel(channel);
      }
    });
  }

  getChannelDisplayName(ch: ChatChannelResponse): string {
    if (ch.channelType === 'INTERNAL_GROUP') return ch.name;
    // For direct or customer support, show other participant's name
    const other = ch.participants?.find(p => p.userId !== this.currentUserId);
    return other ? other.displayName : ch.name;
  }

  getChannelInitial(ch: ChatChannelResponse): string {
    const name = this.getChannelDisplayName(ch);
    return (name || 'C').charAt(0).toUpperCase();
  }

  getChannelBadgeVariant(ch: ChatChannelResponse): 'info' | 'neutral' | 'success' | 'warning' | 'danger' {
    if (ch.channelType === 'INTERNAL_GROUP') return 'info';
    if (ch.channelType === 'CUSTOMER_SUPPORT') return 'warning';
    return 'neutral';
  }

  getRoleClass(role: string): string {
    const r = (role || '').toUpperCase();
    if (r.includes('UNDERWRITER')) return 'role-underwriter';
    if (r.includes('ADJUSTER') || r.includes('CLAIMS')) return 'role-adjuster';
    if (r.includes('POLICYHOLDER') || r.includes('USER')) return 'role-policyholder';
    if (r.includes('ADMIN')) return 'role-admin';
    return 'role-default';
  }

  getRoleAvatarClass(role: string): string {
    const r = (role || '').toUpperCase();
    if (r.includes('UNDERWRITER')) return 'avatar-underwriter';
    if (r.includes('ADJUSTER') || r.includes('CLAIMS')) return 'avatar-adjuster';
    if (r.includes('POLICYHOLDER') || r.includes('USER')) return 'avatar-policyholder';
    if (r.includes('ADMIN')) return 'avatar-admin';
    return 'avatar-default';
  }

  formatTime(iso?: string): string {
    if (!iso) return '';
    try {
      const d = new Date(iso);
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return '';
    }
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.messagesContainer?.nativeElement) {
        this.messagesContainer.nativeElement.scrollTop =
          this.messagesContainer.nativeElement.scrollHeight;
      }
    }, 50);
  }
}
