import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideMockStore } from '@ngrx/store/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { ChatWorkspaceComponent } from './chat-workspace.component';
import { ChatService } from '../../core/services/chat.service';
import { ChatChannelResponse, ChatMessageResponse } from '../../core/models/chat.models';

describe('ChatWorkspaceComponent', () => {
  let component: ChatWorkspaceComponent;
  let fixture: ComponentFixture<ChatWorkspaceComponent>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;

  beforeEach(async () => {
    chatServiceSpy = jasmine.createSpyObj('ChatService', [
      'getChannels',
      'createOrGetChannel',
      'getMessages',
      'sendMessage',
      'getContacts'
    ]);

    chatServiceSpy.getChannels.and.returnValue(of([]));
    chatServiceSpy.getMessages.and.returnValue(of([]));
    chatServiceSpy.getContacts.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [ChatWorkspaceComponent],
      providers: [
        { provide: ChatService, useValue: chatServiceSpy },
        provideMockStore({
          initialState: {
            auth: { userId: 'u1', role: 'POLICYHOLDER' }
          }
        }),
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({}) }
        },
        {
          provide: Router,
          useValue: jasmine.createSpyObj('Router', ['navigate'])
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatWorkspaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads channels on init', () => {
    expect(chatServiceSpy.getChannels).toHaveBeenCalled();
  });

  it('selects channel and loads messages', () => {
    const channel: ChatChannelResponse = {
      channelId: 'c1',
      name: 'General',
      channelType: 'INTERNAL_GROUP',
      createdAt: '2026-10-10T10:00:00',
      updatedAt: '2026-10-10T10:00:00',
      participants: [],
      unreadCount: 0
    };
    const msgs: ChatMessageResponse[] = [
      {
        messageId: 'm1',
        channelId: 'c1',
        senderUserId: 'u2',
        senderDisplayName: 'Alice',
        senderRole: 'UNDERWRITER',
        content: 'Welcome',
        sentAt: '2026-10-10T10:05:00'
      }
    ];
    chatServiceSpy.getMessages.and.returnValue(of(msgs));

    component.selectChannel(channel);
    expect(component.selectedChannel).toBe(channel);
    expect(chatServiceSpy.getMessages).toHaveBeenCalledWith('c1');
    expect(component.messages).toEqual(msgs);
  });

  it('sends message to selected channel', () => {
    const channel: ChatChannelResponse = {
      channelId: 'c1',
      name: 'General',
      channelType: 'INTERNAL_GROUP',
      createdAt: '2026-10-10T10:00:00',
      updatedAt: '2026-10-10T10:00:00',
      participants: [],
      unreadCount: 0
    };
    component.selectedChannel = channel;
    component.messageInput = 'Hello team';

    const sent: ChatMessageResponse = {
      messageId: 'm2',
      channelId: 'c1',
      senderUserId: 'u1',
      senderDisplayName: 'Me',
      senderRole: 'POLICYHOLDER',
      content: 'Hello team',
      sentAt: '2026-10-10T10:10:00'
    };
    chatServiceSpy.sendMessage.and.returnValue(of(sent));

    component.sendCurrentMessage();
    expect(chatServiceSpy.sendMessage).toHaveBeenCalledWith('c1', 'Hello team');
    expect(component.messages).toContain(sent);
    expect(component.messageInput).toBe('');
  });
});
