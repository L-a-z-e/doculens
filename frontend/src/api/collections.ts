import { apiFetch } from './client';
import type { Collection, Page } from '@/types/document';

export function getCollections(page = 0, size = 20): Promise<Page<Collection>> {
  return apiFetch(`/collections?page=${page}&size=${size}`);
}

export function createCollection(name: string, description: string): Promise<Collection> {
  return apiFetch('/collections', {
    method: 'POST',
    body: JSON.stringify({ name, description }),
  });
}

export function deleteCollection(id: string, force = false): Promise<void> {
  return apiFetch(`/collections/${id}?force=${force}`, { method: 'DELETE' });
}
