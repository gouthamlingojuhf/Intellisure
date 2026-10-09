export interface DocumentResponse {
  documentId: string;
  entityId: string;
  entityType: string;
  documentType: string;
  fileName: string;
  fileSize: number;
  contentType: string;
  storagePath: string;
  sha256Hash?: string | null;
  version?: number | null;
  uploadedBy?: string | null;
  createdAt?: string | null;
}
