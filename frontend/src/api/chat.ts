import { useAuthStore } from '@/store/authStore';

export async function streamChat(
  question: string,
  collectionId: string,
  sessionId: string,
  onChunk: (text: string) => void,
  onDone: () => void,
  onError: (error: Error) => void,
) {
  const token = useAuthStore.getState().token;

  const response = await fetch('/api/v1/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ question, collectionId, sessionId }),
  });

  if (!response.ok) {
    onError(new Error(`HTTP ${response.status}`));
    return;
  }

  const reader = response.body?.getReader();
  if (!reader) {
    onError(new Error('ReadableStream not supported'));
    return;
  }

  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    const text = decoder.decode(value);
    const lines = text.split('\n');

    for (const line of lines) {
      if (line.startsWith('data:')) {
        const data = line.slice(5);
        if (data.trim()) {
          onChunk(data);
        }
      }
    }
  }

  onDone();
}
