import { apiFetch } from './client';

export interface StatsResponse {
  documents: {
    total: number;
    totalChunks: number;
    completed: number;
    failed: number;
  };
  collections: {
    total: number;
    byCollection: { name: string; documentCount: number }[];
  };
}

export function getStats(): Promise<StatsResponse> {
  return apiFetch('/admin/stats');
}
