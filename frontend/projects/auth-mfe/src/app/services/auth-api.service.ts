import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse, RegisterRequest, UserProfile } from '../models/auth.models';

const CORRELATION_ID_HEADER = 'X-Correlation-ID';

function correlationId(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) return crypto.randomUUID();
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}

/** Standalone remote client: talks to the gateway directly (no shell imports). */
@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  private headers() {
    return { [CORRELATION_ID_HEADER]: correlationId() };
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    alert('login request: ' + JSON.stringify(request));
    return this.http.post<LoginResponse>(`${this.base}/api/auth/login`, request, {
      headers: this.headers(),
    });
  }

  register(request: RegisterRequest): Observable<unknown> {
    return this.http.post(`${this.base}/api/auth/register`, request, { headers: this.headers() });
  }

  me(token: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.base}/api/auth/me`, {
      headers: { ...this.headers(), Authorization: `Bearer ${token}` },
    });
  }
}
