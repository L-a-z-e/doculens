import { useAuthStore } from '@/store/authStore';
import type { Document, Page } from '@/types/document';
import { apiFetch } from './client';

export function getDocuments(collectionId: string, page = 0, size = 20): Promise<Page<Document>> {
  return apiFetch(`/documents?collectionId=${collectionId}&page=${page}&size=${size}`);
}

export async function uploadDocument(
  collectionId: string,
  file: File,
  title?: string,
): Promise<Document> {
  const token = useAuthStore.getState().token;

  const formData = new FormData();
  formData.append('file', file);
  formData.append('collectionId', collectionId);
  if (title) formData.append('title', title);

  const response = await fetch('/api/v1/documents', {
    method: 'POST',
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: formData,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Upload failed' }));
    throw new Error(error.message || `HTTP ${response.status}`);
  }

  return response.json();
}

export function deleteDocument(id: string): Promise<void> {
  return apiFetch(`/documents/${id}`, { method: 'DELETE' });
}
