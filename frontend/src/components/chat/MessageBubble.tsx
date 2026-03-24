import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface MessageBubbleProps {
  role: 'user' | 'assistant';
  content: string;
  isStreaming?: boolean;
}

export default function MessageBubble({ role, content, isStreaming }: MessageBubbleProps) {
  if (role === 'user') {
    return (
      <div className="flex justify-end">
        <div className="max-w-[80%] rounded-lg bg-primary px-4 py-2 text-primary-foreground">
          <p className="text-sm">{content}</p>
        </div>
      </div>
    );
  }

  // assistant: 소스 인용을 분리
  const { text, sources } = extractSources(content);

  return (
    <div className="flex justify-start">
      <div className="max-w-[80%] rounded-lg bg-muted px-4 py-2">
        <div className="prose prose-sm dark:prose-invert max-w-none">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>{text}</ReactMarkdown>
          {isStreaming && <span className="animate-pulse">▌</span>}
        </div>
        {sources.length > 0 && (
          <div className="mt-2 flex flex-wrap gap-1 border-t pt-2">
            {sources.map((source, i) => (
              <span
                key={i}
                className="inline-flex items-center rounded-full bg-blue-100 px-2 py-0.5 text-xs text-blue-700 dark:bg-blue-900 dark:text-blue-300"
              >
                📄 {source}
              </span>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

function extractSources(content: string): { text: string; sources: string[] } {
  const sourcePattern = /\[Source:\s*([^\]]+)\]/g;
  const sources: string[] = [];
  let match;

  while ((match = sourcePattern.exec(content)) !== null) {
    const source = match[1].trim();
    if (!sources.includes(source)) {
      sources.push(source);
    }
  }

  const text = content.replace(sourcePattern, '').trim();
  return { text, sources };
}
