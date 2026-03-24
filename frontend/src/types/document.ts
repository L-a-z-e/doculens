export interface Collection {
  id: string;
  name: string;
  description: string;
  documentCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Document {
  id: string;
  collectionId: string;
  title: string;
  sourceType: 'PDF' | 'MARKDOWN' | 'TEXT' | 'WEB';
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  totalChunks: number;
  fileSize: number;
  errorMessage: string | null;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
