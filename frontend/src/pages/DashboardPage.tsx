import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { getStats, type StatsResponse } from '@/api/admin';

export default function DashboardPage() {
  const navigate = useNavigate();
  const [stats, setStats] = useState<StatsResponse | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getStats()
      .then(setStats)
      .catch((err) => setError(err instanceof Error ? err.message : '통계 로드 실패'));
  }, []);

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b p-4 flex items-center justify-between">
        <h1 className="text-xl font-semibold">DocuLens 대시보드</h1>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={() => navigate('/documents')}>
            문서 관리
          </Button>
        </div>
      </header>

      {error && (
        <div className="mx-4 mt-4 p-3 bg-destructive/10 text-destructive text-sm rounded-lg">
          {error}
        </div>
      )}

      {stats && (
        <div className="p-6 space-y-6 max-w-5xl mx-auto">
          {/* 요약 카드 */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard label="총 컬렉션" value={stats.collections.total} />
            <StatCard label="총 문서" value={stats.documents.total} />
            <StatCard label="총 청크" value={stats.documents.totalChunks} />
            <StatCard
              label="처리 상태"
              value={`${stats.documents.completed} / ${stats.documents.total}`}
              sub={stats.documents.failed > 0 ? `${stats.documents.failed}건 실패` : undefined}
            />
          </div>

          {/* 컬렉션별 문서 수 */}
          <Card>
            <CardContent className="pt-6">
              <h3 className="font-semibold mb-4">컬렉션별 문서 수</h3>
              {stats.collections.byCollection.length === 0 ? (
                <p className="text-sm text-muted-foreground">컬렉션이 없습니다</p>
              ) : (
                <div className="space-y-3">
                  {stats.collections.byCollection.map((col) => (
                    <div key={col.name} className="flex items-center gap-3">
                      <span className="text-sm w-40 truncate">{col.name}</span>
                      <div className="flex-1 bg-muted rounded-full h-4 overflow-hidden">
                        <div
                          className="bg-primary h-full rounded-full transition-all"
                          style={{
                            width: `${Math.max(
                              (col.documentCount / Math.max(...stats.collections.byCollection.map((c) => c.documentCount))) * 100,
                              5,
                            )}%`,
                          }}
                        />
                      </div>
                      <span className="text-sm font-mono w-8 text-right">{col.documentCount}</span>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}

function StatCard({ label, value, sub }: { label: string; value: string | number; sub?: string }) {
  return (
    <Card>
      <CardContent className="pt-6">
        <p className="text-sm text-muted-foreground">{label}</p>
        <p className="text-2xl font-bold mt-1">{value}</p>
        {sub && <p className="text-xs text-destructive mt-1">{sub}</p>}
      </CardContent>
    </Card>
  );
}
