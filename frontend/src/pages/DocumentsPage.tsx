import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent } from '@/components/ui/card';
import { getCollections, createCollection, deleteCollection } from '@/api/collections';
import { getDocuments, uploadDocument, deleteDocument } from '@/api/documents';
import type { Collection, Document } from '@/types/document';

export default function DocumentsPage() {
  const navigate = useNavigate();
  const [collections, setCollections] = useState<Collection[]>([]);
  const [selectedCollection, setSelectedCollection] = useState<Collection | null>(null);
  const [documents, setDocuments] = useState<Document[]>([]);
  const [newCollectionName, setNewCollectionName] = useState('');
  const [newCollectionDesc, setNewCollectionDesc] = useState('');
  const [error, setError] = useState('');

  const loadCollections = useCallback(async () => {
    try {
      const page = await getCollections();
      setCollections(page.content);
    } catch (err) {
      setError(err instanceof Error ? err.message : '컬렉션 로드 실패');
    }
  }, []);

  const loadDocuments = useCallback(async (collectionId: string) => {
    try {
      const page = await getDocuments(collectionId);
      setDocuments(page.content);
    } catch (err) {
      setError(err instanceof Error ? err.message : '문서 로드 실패');
    }
  }, []);

  useEffect(() => {
    loadCollections();
  }, [loadCollections]);

  useEffect(() => {
    if (selectedCollection) {
      loadDocuments(selectedCollection.id);
    }
  }, [selectedCollection, loadDocuments]);

  const handleCreateCollection = async () => {
    if (!newCollectionName.trim()) return;
    try {
      await createCollection(newCollectionName, newCollectionDesc);
      setNewCollectionName('');
      setNewCollectionDesc('');
      loadCollections();
    } catch (err) {
      setError(err instanceof Error ? err.message : '생성 실패');
    }
  };

  const handleDeleteCollection = async (id: string) => {
    if (!confirm('컬렉션과 모든 문서를 삭제하시겠습니까?')) return;
    try {
      await deleteCollection(id, true);
      if (selectedCollection?.id === id) {
        setSelectedCollection(null);
        setDocuments([]);
      }
      loadCollections();
    } catch (err) {
      setError(err instanceof Error ? err.message : '삭제 실패');
    }
  };

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!selectedCollection || !e.target.files?.[0]) return;
    try {
      await uploadDocument(selectedCollection.id, e.target.files[0]);
      loadDocuments(selectedCollection.id);
    } catch (err) {
      setError(err instanceof Error ? err.message : '업로드 실패');
    }
    e.target.value = '';
  };

  const handleDeleteDocument = async (id: string) => {
    try {
      await deleteDocument(id);
      if (selectedCollection) loadDocuments(selectedCollection.id);
    } catch (err) {
      setError(err instanceof Error ? err.message : '삭제 실패');
    }
  };

  const statusLabel = (status: Document['status']) => {
    const map = { PENDING: '대기', PROCESSING: '처리중', COMPLETED: '완료', FAILED: '실패' };
    return map[status];
  };

  const statusColor = (status: Document['status']) => {
    const map = { PENDING: 'text-yellow-500', PROCESSING: 'text-blue-500', COMPLETED: 'text-green-500', FAILED: 'text-red-500' };
    return map[status];
  };

  return (
    <div className="flex h-screen">
      {/* 왼쪽: 컬렉션 목록 */}
      <div className="w-80 border-r flex flex-col">
        <div className="p-4 border-b">
          <h2 className="text-lg font-semibold mb-3">컬렉션</h2>
          <div className="space-y-2">
            <Input
              placeholder="컬렉션 이름"
              value={newCollectionName}
              onChange={(e) => setNewCollectionName(e.target.value)}
            />
            <Input
              placeholder="설명 (선택)"
              value={newCollectionDesc}
              onChange={(e) => setNewCollectionDesc(e.target.value)}
            />
            <Button onClick={handleCreateCollection} className="w-full" size="sm">
              생성
            </Button>
          </div>
        </div>
        <div className="flex-1 overflow-y-auto p-2 space-y-1">
          {collections.map((col) => (
            <div
              key={col.id}
              className={`flex items-center justify-between rounded-lg px-3 py-2 cursor-pointer hover:bg-muted ${
                selectedCollection?.id === col.id ? 'bg-muted' : ''
              }`}
              onClick={() => setSelectedCollection(col)}
            >
              <div className="min-w-0">
                <p className="text-sm font-medium truncate">{col.name}</p>
                <p className="text-xs text-muted-foreground">{col.description}</p>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={(e) => { e.stopPropagation(); handleDeleteCollection(col.id); }}
              >
                ×
              </Button>
            </div>
          ))}
        </div>
        <div className="p-4 border-t">
          <Button
            variant="outline"
            className="w-full"
            disabled={!selectedCollection}
            onClick={() => selectedCollection && navigate(`/chat?collectionId=${selectedCollection.id}`)}
          >
            {selectedCollection ? `"${selectedCollection.name}" 채팅` : '컬렉션을 선택하세요'}
          </Button>
        </div>
      </div>

      {/* 오른쪽: 문서 목록 */}
      <div className="flex-1 flex flex-col">
        <div className="p-4 border-b flex items-center justify-between">
          <h2 className="text-lg font-semibold">
            {selectedCollection ? selectedCollection.name : '컬렉션을 선택하세요'}
          </h2>
          {selectedCollection && (
            <label className="cursor-pointer">
              <Button variant="outline" size="sm" onClick={() => document.getElementById('file-upload')?.click()}>
                파일 업로드
              </Button>
              <input
                id="file-upload"
                type="file"
                accept=".pdf,.md,.txt"
                onChange={handleUpload}
                className="hidden"
              />
            </label>
          )}
        </div>

        {error && (
          <div className="mx-4 mt-4 p-3 bg-destructive/10 text-destructive text-sm rounded-lg">
            {error}
            <Button variant="ghost" size="sm" onClick={() => setError('')}>닫기</Button>
          </div>
        )}

        <div className="flex-1 overflow-y-auto p-4">
          {!selectedCollection ? (
            <p className="text-center text-muted-foreground mt-20">
              왼쪽에서 컬렉션을 선택하세요
            </p>
          ) : documents.length === 0 ? (
            <p className="text-center text-muted-foreground mt-20">
              문서가 없습니다. 파일을 업로드하세요.
            </p>
          ) : (
            <div className="space-y-2">
              {documents.map((doc) => (
                <Card key={doc.id}>
                  <CardContent className="flex items-center justify-between py-3">
                    <div>
                      <p className="font-medium text-sm">{doc.title}</p>
                      <div className="flex gap-3 text-xs text-muted-foreground mt-1">
                        <span>{doc.sourceType}</span>
                        <span className={statusColor(doc.status)}>{statusLabel(doc.status)}</span>
                        {doc.totalChunks > 0 && <span>{doc.totalChunks}개 청크</span>}
                        {doc.errorMessage && <span className="text-red-500">{doc.errorMessage}</span>}
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDeleteDocument(doc.id)}
                    >
                      삭제
                    </Button>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
