import { useEffect, useState, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getChatHistory, sendChatMessage, getReportDetails, safeUUID } from '../api/api';
import type { ChatMessage, DiagnosisReport } from '../api/api';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';

export default function Chat() {
  const { reportId } = useParams<{ reportId: string }>();
  const [report, setReport] = useState<DiagnosisReport | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (!reportId) return;
    setLoading(true);
    setError(null);
    Promise.all([
      getReportDetails(reportId),
      getChatHistory(reportId),
    ])
      .then(([reportRes, chatRes]) => {
        setReport(reportRes.data);
        setMessages(chatRes.data);
      })
      .catch((err) => setError(err.message || 'Failed to load chat'))
      .finally(() => setLoading(false));
  }, [reportId]);

  useEffect(() => { scrollToBottom(); }, [messages]);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || !reportId || sending) return;

    const userMsg: ChatMessage = {
      id: safeUUID(),
      sender: 'USER',
      messageText: input.trim(),
      timestamp: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setSending(true);

    try {
      const res = await sendChatMessage(reportId, userMsg.messageText);
      setMessages((prev) => [...prev, res.data]);
    } catch {
      setError('Failed to send message. Please try again.');
    } finally {
      setSending(false);
    }
  };

  if (loading) return <LoadingSpinner size="lg" text="Loading chat..." />;
  if (error && !report) return <div className="mx-auto max-w-3xl px-4 py-8"><ErrorMessage message={error} /></div>;

  return (
    <div className="mx-auto flex max-w-4xl flex-col px-4 py-8 sm:px-6 lg:px-8" style={{ height: 'calc(100vh - 8rem)' }}>
      {/* Header */}
      <div className="animate-fade-in mb-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Link to={`/diagnosis/${reportId}`} className="text-gray-400 hover:text-gray-600 transition-colors">
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
            </svg>
          </Link>
          <div>
            <h1 className="text-lg font-bold text-gray-900">Chat Assistant</h1>
            <p className="text-xs text-gray-500">{report?.machineName || 'Diagnosis Report'}</p>
          </div>
        </div>
      </div>

      {/* Messages area */}
      <div className="flex-1 overflow-y-auto rounded-2xl border border-gray-200 bg-white shadow-sm">
        <div className="flex h-full flex-col">
          {messages.length === 0 ? (
            <div className="flex flex-1 items-center justify-center p-8 text-center">
              <div>
                <svg className="mx-auto mb-4 h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8.625 12a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H8.25m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H12m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 01-2.555-.337A5.972 5.972 0 015.41 20.97a5.969 5.969 0 01-.474-.065 4.48 4.48 0 00.978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25z" />
                </svg>
                <p className="text-gray-500 font-medium">No messages yet</p>
                <p className="mt-1 text-sm text-gray-400">
                  Ask questions about the diagnosis report above.
                </p>
              </div>
            </div>
          ) : (
            <div className="flex-1 space-y-4 p-4 sm:p-6 overflow-y-auto">
              {messages.map((msg, i) => (
                <div
                  key={msg.id}
                  className={`animate-fade-in flex ${msg.sender === 'USER' ? 'justify-end' : 'justify-start'}`}
                  style={{ animationDelay: `${i * 0.05}s` }}
                >
                  <div
                    className={`max-w-[80%] rounded-2xl px-4 py-3 text-sm ${
                      msg.sender === 'USER'
                        ? 'bg-indigo-600 text-white rounded-br-md'
                        : 'bg-gray-100 text-gray-800 rounded-bl-md'
                    }`}
                  >
                    <p className="whitespace-pre-wrap">{msg.messageText}</p>
                    <p className={`mt-1 text-xs ${msg.sender === 'USER' ? 'text-indigo-200' : 'text-gray-400'}`}>
                      {new Date(msg.timestamp).toLocaleTimeString()}
                    </p>
                  </div>
                </div>
              ))}
              {sending && (
                <div className="flex justify-start">
                  <div className="max-w-[80%] rounded-2xl rounded-bl-md bg-gray-100 px-4 py-3">
                    <div className="flex gap-1">
                      <span className="h-2 w-2 animate-bounce rounded-full bg-gray-400" style={{ animationDelay: '0ms' }} />
                      <span className="h-2 w-2 animate-bounce rounded-full bg-gray-400" style={{ animationDelay: '150ms' }} />
                      <span className="h-2 w-2 animate-bounce rounded-full bg-gray-400" style={{ animationDelay: '300ms' }} />
                    </div>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>
          )}
        </div>

        {/* Input area */}
        <div className="border-t border-gray-200 p-4">
          {error && (
            <div className="mb-3 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</div>
          )}
          <form onSubmit={handleSend} className="flex gap-3">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type your message..."
              disabled={sending}
              className="flex-1 rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm placeholder-gray-400 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200 transition-all disabled:opacity-50"
            />
            <button
              type="submit"
              disabled={!input.trim() || sending}
              className="inline-flex items-center justify-center rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white shadow-sm transition hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {sending ? (
                <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
              ) : (
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5" />
                </svg>
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
