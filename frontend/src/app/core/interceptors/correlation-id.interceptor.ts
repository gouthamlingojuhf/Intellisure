import { HttpInterceptorFn } from '@angular/common/http';

export const CORRELATION_ID_HEADER = 'X-Correlation-ID';

function newId(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) return crypto.randomUUID();
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}

/** Ensures every outbound request carries X-Correlation-ID (matches gateway filter). */
export const correlationIdInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.headers.has(CORRELATION_ID_HEADER)) {
    req = req.clone({ setHeaders: { [CORRELATION_ID_HEADER]: newId() } });
  }
  return next(req);
};
