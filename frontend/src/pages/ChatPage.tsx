import { useState, useRef, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { ScrollArea } from '@/components/ui/scroll-area';
import { streamChat } from '@/api/chat';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

export default function ChatPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const collectionId = searchParams.get('collectionId');

  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [sessionId] = useState(() => crypto.randomUUID());
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  if (!collectionId) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-center space-y-4">
          <p className="text-muted-foreground">컬렉션을 먼저 선택하세요</p>
          <Button onClick={() => navigate('/documents')}>문서 관리로 이동</Button>
        </div>
      </div>
    );
  }

  const handleSend = async () => {
    if (!input.trim() || isLoading) return;

    const question = input.trim();
    setInput('');
    setMessages((prev) => [...prev, { role: 'user', content: question }]);
    setIsLoading(true);

    setMessages((prev) => [...prev, { role: 'assistant', content: '' }]);

    await streamChat(
      question,
      collectionId,
      sessionId,
      (chunk) => {
        setMessages((prev) => {
          const updated = [...prev];
          const last = updated[updated.length - 1];
          if (last.role === 'assistant') {
            last.content += chunk;
          }
          return updated;
        });
      },
      () => setIsLoading(false),
      (error) => {
        setMessages((prev) => {
          const updated = [...prev];
          const last = updated[updated.length - 1];
          if (last.role === 'assistant') {
            last.content = `오류: ${error.message}`;
          }
          return updated;
        });
        setIsLoading(false);
      },
    );
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="flex h-screen flex-col">
      <header className="border-b p-4 flex items-center justify-between">
        <h1 className="text-xl font-semibold">DocuLens Chat</h1>
        <Button variant="outline" size="sm" onClick={() => navigate('/documents')}>
          문서 관리
        </Button>
      </header>

      <ScrollArea className="flex-1 p-4">
        <div className="mx-auto max-w-3xl space-y-4">
          {messages.length === 0 && (
            <p className="text-center text-muted-foreground mt-20">
              업로드된 문서에 대해 질문해보세요
            </p>
          )}
          {messages.map((msg, i) => (
            <div
              key={i}
              className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`max-w-[80%] rounded-lg px-4 py-2 ${
                  msg.role === 'user'
                    ? 'bg-primary text-primary-foreground'
                    : 'bg-muted'
                }`}
              >
                <pre className="whitespace-pre-wrap font-sans text-sm">
                  {msg.content}
                  {isLoading && i === messages.length - 1 && msg.role === 'assistant' && (
                    <span className="animate-pulse">▌</span>
                  )}
                </pre>
              </div>
            </div>
          ))}
          <div ref={scrollRef} />
        </div>
      </ScrollArea>

      <div className="border-t p-4">
        <div className="mx-auto flex max-w-3xl gap-2">
          <Textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="질문을 입력하세요..."
            className="min-h-[44px] resize-none"
            rows={1}
            disabled={isLoading}
          />
          <Button onClick={handleSend} disabled={isLoading || !input.trim()}>
            전송
          </Button>
        </div>
      </div>
    </div>
  );
}
