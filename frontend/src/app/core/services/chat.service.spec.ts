import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ChatService } from './chat.service';
import { ApiService } from './api.service';
import { ChatChannelResponse, ChatMessageResponse, ChatContactResponse } from '../models/chat.models';

describe('ChatService', () => {
  let service: ChatService;
  let apiSpy: jasmine.SpyObj<ApiService>;

  beforeEach(() => {
    apiSpy = jasmine.createSpyObj('ApiService', ['get', 'post']);
    TestBed.configureTestingModule({
      providers: [
        ChatService,
        { provide: ApiService, useValue: apiSpy }
      ]
    });
    service = TestBed.inject(ChatService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getChannels calls /api/chat/channels', (done) => {
    const mockChannels: ChatChannelResponse[] = [];
    apiSpy.get.and.returnValue(of(mockChannels));

    service.getChannels().subscribe(channels => {
      expect(channels).toEqual(mockChannels);
      expect(apiSpy.get).toHaveBeenCalledWith('/api/chat/channels');
      done();
    });
  });

  it('createOrGetChannel calls /api/chat/channels with post body', (done) => {
    const mockChannel = { channelId: 'c1', name: 'General' } as ChatChannelResponse;
    apiSpy.post.and.returnValue(of(mockChannel));

    const req = { name: 'General', channelType: 'INTERNAL_GROUP' as const };
    service.createOrGetChannel(req).subscribe(ch => {
      expect(ch).toEqual(mockChannel);
      expect(apiSpy.post).toHaveBeenCalledWith('/api/chat/channels', req);
      done();
    });
  });

  it('getMessages calls /api/chat/channels/:id/messages', (done) => {
    const mockMsgs: ChatMessageResponse[] = [];
    apiSpy.get.and.returnValue(of(mockMsgs));

    service.getMessages('c1').subscribe(msgs => {
      expect(msgs).toEqual(mockMsgs);
      expect(apiSpy.get).toHaveBeenCalledWith('/api/chat/channels/c1/messages');
      done();
    });
  });

  it('sendMessage calls /api/chat/channels/:id/messages with content', (done) => {
    const mockMsg = { messageId: 'm1', content: 'hello' } as ChatMessageResponse;
    apiSpy.post.and.returnValue(of(mockMsg));

    service.sendMessage('c1', 'hello').subscribe(msg => {
      expect(msg).toEqual(mockMsg);
      expect(apiSpy.post).toHaveBeenCalledWith('/api/chat/channels/c1/messages', { content: 'hello' });
      done();
    });
  });

  it('getContacts calls /api/chat/contacts', (done) => {
    const mockContacts: ChatContactResponse[] = [];
    apiSpy.get.and.returnValue(of(mockContacts));

    service.getContacts().subscribe(contacts => {
      expect(contacts).toEqual(mockContacts);
      expect(apiSpy.get).toHaveBeenCalledWith('/api/chat/contacts');
      done();
    });
  });
});
