import { useEffect, useState, useRef } from 'react';
import { useSearchParams, Link, useNavigate } from 'react-router-dom';
import { getMachineById, getLearnHistory, sendLearnMessage, clearLearnHistory, safeUUID } from '../api/api';
import type { Machine, LearnMessage } from '../api/api';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';

const QUICK_STUDY_TOPICS = [
  {
    title: 'Lubrication Guidelines',
    icon: (
      <svg className="h-5 w-5 text-amber-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 5.25l-7.5 7.5-7.5-7.5m15 6l-7.5 7.5-7.5-7.5" />
      </svg>
    ),
    prompt: 'What are the lubrication guidelines, oil types, and schedules for this machine?',
  },
  {
    title: 'Safety Precautions',
    icon: (
      <svg className="h-5 w-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
    ),
    prompt: 'What safety precautions and emergency stopping procedures should I follow?',
  },
  {
    title: 'Needle & Weft Settings',
    icon: (
      <svg className="h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
      </svg>
    ),
    prompt: 'How do I replace and calibrate the selector needles on this machine?',
  },
  {
    title: 'Daily Maintenance Checks',
    icon: (
      <svg className="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
      </svg>
    ),
    prompt: 'What daily checks and preventative maintenance tasks should be performed?',
  },
];

export default function Learn() {
  const [searchParams] = useSearchParams();
  const machineId = searchParams.get('machineId');
  const navigate = useNavigate();

  const [machine, setMachine] = useState<Machine | null>(null);
  const [messages, setMessages] = useState<LearnMessage[]>([]);
  const [sessionId, setSessionId] = useState<string>('');
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Initialize or fetch session ID from localStorage to persist user's learn chat session per machine
  useEffect(() => {
    if (!machineId) return;
    const storageKey = `vt_learn_session_${machineId}`;
    let sId = localStorage.getItem(storageKey);
    if (!sId) {
      sId = safeUUID();
      localStorage.setItem(storageKey, sId);
    }
    setSessionId(sId);
  }, [machineId]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (!machineId || !sessionId) return;
    setLoading(true);
    setError(null);
    Promise.all([
      getMachineById(machineId),
      getLearnHistory(machineId, sessionId),
    ])
      .then(([machineRes, historyRes]) => {
        setMachine(machineRes.data);
        setMessages(historyRes.data);
      })
      .catch((err) => {
        setError(err.message || 'Failed to load learning context');
      })
      .finally(() => setLoading(false));
  }, [machineId, sessionId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages, sending]);

  const handleSend = async (messageText: string) => {
    if (!messageText.trim() || !machineId || !sessionId || sending) return;

    const userMsg: LearnMessage = {
      id: safeUUID(),
      machineId,
      sessionId,
      sender: 'USER',
      messageText: messageText.trim(),
      timestamp: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setSending(true);
    setError(null);

    try {
      const res = await sendLearnMessage(machineId, userMsg.messageText, sessionId);
      setMessages((prev) => [...prev, res.data]);
    } catch {
      setError('Failed to send message. Please try again.');
    } finally {
      setSending(false);
    }
  };

  const handleClearChat = async () => {
    if (!machineId || !sessionId) return;
    if (!window.confirm('Are you sure you want to clear your learning history for this machine?')) return;
    
    setLoading(true);
    try {
      await clearLearnHistory(machineId, sessionId);
      // Start a fresh session ID to ensure complete separation
      const newSessionId = safeUUID();
      localStorage.setItem(`vt_learn_session_${machineId}`, newSessionId);
      setSessionId(newSessionId);
      setMessages([]);
    } catch (err: any) {
      setError(err.message || 'Failed to clear learning history');
    } finally {
      setLoading(false);
    }
  };

  if (!machineId) {
    return (
      <div className="vt-page max-w-xl py-12 text-center">
        <ErrorMessage message="No machine ID specified. Please select a machine first." />
        <Link to="/machines" className="vt-button-primary mt-6">Go to Machines</Link>
      </div>
    );
  }

  if (loading) return <LoadingSpinner size="lg" text="Loading AI Study Environment..." />;
  if (error && !machine) return <div className="vt-page max-w-3xl"><ErrorMessage message={error} /></div>;
  if (!machine) return null;

  return (
    <div className="vt-page flex max-w-6xl flex-col gap-6 md:flex-row" style={{ minHeight: 'calc(100vh - 8rem)' }}>
      
      {/* Left Panel: Sidebar containing study info & quick prompts */}
      <div className="flex w-full flex-col gap-4 md:w-80 lg:w-96 shrink-0">
        
        {/* Machine Summary Card */}
        <div className="vt-panel animate-fade-in p-5">
          <button
            onClick={() => navigate(`/machines/${machine.id}`)}
            className="mb-4 inline-flex items-center gap-2 text-xs font-bold text-[#434655] hover:text-[#131b2e] transition-colors"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
            </svg>
            Back to Machine
          </button>
          
          <p className="vt-label">Interactive Study Guide</p>
          <h2 className="text-xl font-extrabold text-[#131b2e] mt-1">{machine.name}</h2>
          <p className="text-xs text-[#737686]">{machine.manufacturer} &middot; {machine.model}</p>

          <div className="mt-4 border-t border-[#c3c6d7]/50 pt-4 space-y-2">
            <div className="flex justify-between text-xs">
              <span className="text-[#737686]">Manual Status:</span>
              <span className={`font-semibold ${machine.manualPdfPath ? 'text-green-600' : 'text-amber-500'}`}>
                {machine.manualPdfPath ? 'Available' : 'Using Fallback'}
              </span>
            </div>
            <div className="flex justify-between text-xs">
              <span className="text-[#737686]">User Guide Status:</span>
              <span className={`font-semibold ${machine.userGuidePdfPath ? 'text-green-600' : 'text-amber-500'}`}>
                {machine.userGuidePdfPath ? 'Available' : 'Using Fallback'}
              </span>
            </div>
          </div>
        </div>

        {/* Quick Study Topics */}
        <div className="vt-panel animate-fade-in p-5 flex-1" style={{ animationDelay: '0.1s' }}>
          <h3 className="text-sm font-bold text-[#131b2e] mb-3">Quick Study Topics</h3>
          <p className="text-xs text-[#737686] mb-4">Select a topic below to automatically ask the AI assistant details from the guides.</p>
          
          <div className="space-y-2.5">
            {QUICK_STUDY_TOPICS.map((topic, index) => (
              <button
                key={index}
                onClick={() => handleSend(topic.prompt)}
                disabled={sending}
                className="w-full text-left p-3 rounded-lg border border-[#c3c6d7]/60 hover:border-[#7c3aed] hover:bg-[#fcfaff] transition-all flex items-start gap-3 group disabled:opacity-50"
              >
                <div className="shrink-0 mt-0.5">{topic.icon}</div>
                <div>
                  <h4 className="text-xs font-bold text-[#131b2e] group-hover:text-[#7c3aed] transition-colors">
                    {topic.title}
                  </h4>
                  <p className="text-[10px] text-[#737686] mt-0.5 line-clamp-2">
                    {topic.prompt}
                  </p>
                </div>
              </button>
            ))}
          </div>

          <div className="mt-6 border-t border-[#c3c6d7]/50 pt-4">
            <button
              onClick={handleClearChat}
              className="w-full vt-button-secondary py-2 min-h-9 text-xs justify-center hover:bg-red-50 hover:text-red-600 hover:border-red-200 transition-colors"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Clear Study History
            </button>
          </div>
        </div>

      </div>

      {/* Right Panel: Chat Dialogue interface */}
      <div className="vt-panel flex-1 flex flex-col overflow-hidden" style={{ minHeight: '450px' }}>
        
        {/* Chat Title / Banner */}
        <div className="border-b border-[#c3c6d7]/60 px-5 py-4 bg-[#fbfbff] flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <span className="flex h-3 w-3 relative">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#7c3aed] opacity-75"></span>
              <span className="relative inline-flex rounded-full h-3 w-3 bg-[#7c3aed]"></span>
            </span>
            <div>
              <h3 className="text-sm font-extrabold text-[#131b2e]">AI Copilot Study Buddy</h3>
              <p className="text-[10px] text-[#737686]">Retrieving from manuals & user guide docs</p>
            </div>
          </div>
        </div>

        {/* Message Feeds Area */}
        <div className="flex-1 overflow-y-auto bg-gradient-to-b from-white to-[#fafaff] p-5 space-y-4">
          {messages.length === 0 ? (
            <div className="h-full flex flex-col items-center justify-center text-center p-8">
              <div className="p-4 rounded-full bg-[#fcfaff] border border-[#7c3aed]/20 mb-4">
                <svg className="h-10 w-10 text-[#7c3aed]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
                </svg>
              </div>
              <h4 className="font-extrabold text-[#131b2e] text-base">Start Learning!</h4>
              <p className="text-xs text-[#737686] mt-1 max-w-sm">
                Ask specific questions about operating, maintaining, or troubleshooting this machine. You can also select a study topic on the left to begin.
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {messages.map((msg, idx) => (
                <div
                  key={msg.id}
                  className={`flex ${msg.sender === 'USER' ? 'justify-end' : 'justify-start'} animate-fade-in`}
                  style={{ animationDelay: `${idx * 0.03}s` }}
                >
                  <div
                    className={`max-w-[85%] sm:max-w-[75%] rounded-2xl p-4 shadow-sm text-sm ${
                      msg.sender === 'USER'
                        ? 'bg-[#2563eb] text-white rounded-br-none'
                        : 'vt-ai-card text-[#131b2e] rounded-bl-none'
                    }`}
                  >
                    {/* Message text with basic paragraph formatting support */}
                    <div className="whitespace-pre-wrap leading-relaxed">
                      {msg.messageText}
                    </div>
                    
                    <p className={`mt-2 text-[9px] text-right font-medium ${msg.sender === 'USER' ? 'text-blue-100' : 'text-[#737686]'}`}>
                      {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </p>
                  </div>
                </div>
              ))}

              {sending && (
                <div className="flex justify-start">
                  <div className="vt-ai-card rounded-2xl rounded-bl-none p-4 max-w-[80%] shadow-sm">
                    <div className="flex items-center gap-1.5 py-1">
                      <span className="h-2 w-2 animate-bounce rounded-full bg-[#7c3aed]/60" style={{ animationDelay: '0ms' }} />
                      <span className="h-2 w-2 animate-bounce rounded-full bg-[#7c3aed]/60" style={{ animationDelay: '150ms' }} />
                      <span className="h-2 w-2 animate-bounce rounded-full bg-[#7c3aed]/60" style={{ animationDelay: '300ms' }} />
                    </div>
                  </div>
                </div>
              )}
              
              <div ref={messagesEndRef} />
            </div>
          )}
        </div>

        {/* Input Bar Section */}
        <div className="border-t border-[#c3c6d7]/60 p-4 bg-white">
          {error && (
            <div className="mb-3 rounded-lg bg-[#ffdad6] p-3 text-xs font-semibold text-[#93000a] animate-fade-in">
              {error}
            </div>
          )}
          
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSend(input);
            }}
            className="flex gap-2.5"
          >
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder={`Ask a question about the ${machine.name} guides...`}
              disabled={sending}
              className="vt-input flex-1 focus:border-[#7c3aed] focus:ring-[#7c3aed]/15 disabled:opacity-50"
            />
            
            <button
              type="submit"
              disabled={!input.trim() || sending}
              className="vt-button-ai px-5 rounded-lg text-white"
            >
              {sending ? (
                <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
              ) : (
                <span className="flex items-center gap-1.5">
                  Send
                  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5" />
                  </svg>
                </span>
              )}
            </button>
          </form>
        </div>

      </div>

    </div>
  );
}
