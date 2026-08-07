import { useEffect, useState, useRef, useMemo } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import {
  getMachineById,
  getMachines,
  getLearnHistory,
  sendLearnMessage,
  clearLearnHistory,
  getFileUrl,
  fetchFileContent,
  isMarkdownPath,
  safeUUID,
  getDiagnosisModels
} from '../api/api';
import type { Machine, LearnMessage } from '../api/api';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';

marked.setOptions({ gfm: true, breaks: true });

const QUICK_STUDY_TOPICS = [
  {
    title: 'Lubrication Guidelines',
    icon: (
      <svg className="h-4 w-4 text-amber-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 5.25l-7.5 7.5-7.5-7.5m15 6l-7.5 7.5-7.5-7.5" />
      </svg>
    ),
    prompt: 'What are the lubrication guidelines, oil types, and schedules for this machine?',
  },
  {
    title: 'Safety Precautions',
    icon: (
      <svg className="h-4 w-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
    ),
    prompt: 'What safety precautions and emergency stopping procedures should I follow?',
  },
  {
    title: 'Needle & Weft Settings',
    icon: (
      <svg className="h-4 w-4 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
      </svg>
    ),
    prompt: 'How do I replace and calibrate the selector needles on this machine?',
  },
  {
    title: 'Maintenance Checks',
    icon: (
      <svg className="h-4 w-4 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
      </svg>
    ),
    prompt: 'What daily checks and preventative maintenance tasks should be performed?',
  },
];

export default function Learn() {
  const [searchParams, setSearchParams] = useSearchParams();
  const machineId = searchParams.get('machineId');
  const navigate = useNavigate();

  const [allMachines, setAllMachines] = useState<Machine[]>([]);
  const [machine, setMachine] = useState<Machine | null>(null);
  const [messages, setMessages] = useState<LearnMessage[]>([]);
  const [sessionId, setSessionId] = useState<string>('');
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedDoc, setSelectedDoc] = useState<'manual' | 'guide'>('manual');
  const [showTopics, setShowTopics] = useState(true);
  const [docContent, setDocContent] = useState<string | null>(null);
  const [docLoading, setDocLoading] = useState(false);
  const [docError, setDocError] = useState<string | null>(null);
  const [docRetry, setDocRetry] = useState(0);
  const [models, setModels] = useState<string[]>([]);
  const [selectedModel, setSelectedModel] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Load models on mount and default to low (flash) model
  useEffect(() => {
    getDiagnosisModels()
      .then((res) => {
        const FALLBACK_MODELS = ['gemini-3.6-flash', 'gemini-2.5-pro', 'gemini-2.5-flash', 'gemini-2.0-flash'];
        const list = res.data.models.length ? res.data.models : FALLBACK_MODELS;
        setModels(list);
        const defaultFlash = list.find(m => m.toLowerCase().includes('flash')) || list[0];
        setSelectedModel(defaultFlash);
      })
      .catch(() => {
        const FALLBACK_MODELS = ['gemini-3.6-flash', 'gemini-2.5-pro', 'gemini-2.5-flash', 'gemini-2.0-flash'];
        const defaultFlash = FALLBACK_MODELS.find(m => m.toLowerCase().includes('flash')) || FALLBACK_MODELS[0];
        setModels(FALLBACK_MODELS);
        setSelectedModel(defaultFlash);
      });
  }, []);

  // Load all machines if no ID specified
  useEffect(() => {
    if (!machineId) {
      setLoading(true);
      getMachines()
        .then((res) => {
          setAllMachines(res.data);
        })
        .catch((err) => {
          setError(err.message || 'Failed to fetch machines');
        })
        .finally(() => setLoading(false));
    }
  }, [machineId]);

  // Initialize or fetch session ID from localStorage
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

  // Fetch the markdown document content so manuals/guides render as a preview instead of raw source
  useEffect(() => {
    const path = selectedDoc === 'manual' ? machine?.manualPdfPath : machine?.userGuidePdfPath;
    setDocContent(null);
    setDocError(null);
    if (!machine || !path || !isMarkdownPath(path)) {
      setDocLoading(false);
      return;
    }
    setDocLoading(true);
    const sanitizedPath = path.startsWith('/api') ? path.replace('/api', '') : path;
    fetchFileContent(sanitizedPath)
      .then((text) => setDocContent(text))
      .catch((err) => setDocError(err.message || 'Failed to load document'))
      .finally(() => setDocLoading(false));
  }, [machine, selectedDoc, docRetry]);

  // Render markdown to sanitized HTML for the preview pane
  const renderedDocHtml = useMemo(() => {
    if (!docContent) return '';
    try {
      const rawHtml = marked.parse(docContent, { async: false }) as string;
      return DOMPurify.sanitize(rawHtml);
    } catch (e) {
      console.error('Failed to parse markdown', e);
      return `<pre style="white-space: pre-wrap; font-family: monospace; font-size: 13px;">${docContent}</pre>`;
    }
  }, [docContent]);

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
      const res = await sendLearnMessage(machineId, userMsg.messageText, sessionId, selectedModel);
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

  if (loading) return <LoadingSpinner size="lg" text="Loading Interactive AI Workspace..." />;

  // ─── Machine Selection State (Highly accessible/friendly fallback) ───
  if (!machineId) {
    return (
      <div className="vt-page max-w-5xl py-8">
        <div className="mb-8 text-center sm:text-left">
          <p className="vt-label">Interactive AI Environment</p>
          <h1 className="vt-title mt-1">Select a Machine to Start Learning</h1>
          <p className="vt-subtitle max-w-2xl">
            Choose a machine to access its full documentation twin, interact with the specialized AI Study Guide, and explore reference instructions side-by-side.
          </p>
        </div>

        {error && <div className="mb-6"><ErrorMessage message={error} /></div>}

        {allMachines.length === 0 ? (
          <div className="vt-empty py-16">
            <svg className="mx-auto h-12 w-12 text-[#737686]/60" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
            </svg>
            <h3 className="mt-4 text-sm font-bold text-[#131b2e]">No machines registered</h3>
            <p className="mt-1 text-xs text-[#737686]">Get started by registering a machine twin first.</p>
          </div>
        ) : (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {allMachines.map((m) => {
              const hasManual = !!m.manualPdfPath;
              const hasGuide = !!m.userGuidePdfPath;
              const thumb = m.thumbnailPath ? getFileUrl(m.thumbnailPath) : null;
              
              return (
                <div key={m.id} className="vt-panel flex flex-col justify-between overflow-hidden transition-all duration-200 hover:-translate-y-1 hover:shadow-lg">
                  <div>
                    <div className="aspect-[2/1] w-full overflow-hidden bg-[#e2e7ff] relative">
                      {thumb ? (
                        <img src={thumb} alt={m.name} className="h-full w-full object-cover" />
                      ) : (
                        <div className="flex h-full items-center justify-center">
                          <svg className="h-10 w-10 text-[#2563eb]/30" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
                          </svg>
                        </div>
                      )}
                      
                      <div className="absolute top-2.5 right-2.5 flex flex-col gap-1.5">
                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold shadow-sm ${hasManual ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'}`}>
                          Manual: {hasManual ? 'Yes' : 'No'}
                        </span>
                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold shadow-sm ${hasGuide ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'}`}>
                          Guide: {hasGuide ? 'Yes' : 'No'}
                        </span>
                      </div>
                    </div>

                    <div className="p-5">
                      <p className="vt-label">{m.manufacturer}</p>
                      <h3 className="text-lg font-bold text-[#131b2e] mt-1">{m.name}</h3>
                      <p className="text-xs text-[#737686]">{m.model}</p>
                    </div>
                  </div>

                  <div className="p-5 border-t border-[#c3c6d7]/40 bg-[#faf8ff]/50">
                    <button
                      onClick={() => setSearchParams({ machineId: m.id })}
                      className="w-full vt-button-ai text-xs font-bold leading-normal gap-1.5"
                    >
                      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
                      </svg>
                      Launch AI Study Guide
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    );
  }

  if (error && !machine) return <div className="vt-page max-w-3xl"><ErrorMessage message={error} /></div>;
  if (!machine) return null;

  // Determine URL based on selected document tab
  const activePdfPath = selectedDoc === 'manual' ? machine.manualPdfPath : machine.userGuidePdfPath;
  const activePdfUrl = activePdfPath ? getFileUrl(activePdfPath) : null;
  const activeIsMarkdown = isMarkdownPath(activePdfPath);

  return (
    <div className="flex flex-col lg:flex-row gap-6 w-full max-w-[1700px] mx-auto px-4 py-6" style={{ height: 'calc(100vh - 4.5rem)' }}>
      
      {/* ─── Left Side: Large Document Viewer Pane (65% width on desktop) ─── */}
      <div className="flex-1 flex flex-col vt-panel overflow-hidden h-full">
        
        {/* Document Header Toolbar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between border-b border-[#c3c6d7]/60 bg-[#fbfbff] px-5 py-3.5 gap-3">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate('/machines')}
              aria-label="Back to machines"
              className="inline-flex h-8 w-8 items-center justify-center rounded-lg border border-[#c3c6d7] bg-white text-[#434655] hover:text-[#131b2e] transition-colors"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
              </svg>
            </button>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-base font-extrabold text-[#131b2e]">{machine.name} Documentation</h2>
                <span className="text-[10px] font-semibold bg-[#e2e7ff] text-[#004ac6] px-1.5 py-0.5 rounded uppercase">Twin Ready</span>
              </div>
              <p className="text-[10px] text-[#737686]">{machine.manufacturer} &middot; {machine.model}</p>
            </div>
          </div>
          
          {/* Document Tab Toggles */}
          <div className="flex bg-[#f2f3ff] p-1 rounded-lg border border-[#c3c6d7]/50 self-start sm:self-center">
            <button
              onClick={() => setSelectedDoc('manual')}
              className={`px-3 py-1.5 text-xs font-bold rounded-md transition-all ${
                selectedDoc === 'manual'
                  ? 'bg-white text-[#004ac6] shadow-sm'
                  : 'text-[#737686] hover:text-[#131b2e]'
              }`}
            >
              User Manual
            </button>
            <button
              onClick={() => setSelectedDoc('guide')}
              className={`px-3 py-1.5 text-xs font-bold rounded-md transition-all ${
                selectedDoc === 'guide'
                  ? 'bg-white text-[#004ac6] shadow-sm'
                  : 'text-[#737686] hover:text-[#131b2e]'
              }`}
            >
              User Guide
            </button>
          </div>
        </div>
        
        {/* Document Viewer: markdown preview for .md files, iframe fallback for other files */}
        <div className="flex-1 bg-[#f6f8fa] relative">
          {activePdfPath && activeIsMarkdown ? (
            <div className="absolute inset-0 flex flex-col">
              <div className="flex items-center justify-between border-b border-[#c3c6d7]/60 bg-[#fbfbff] px-4 py-2">
                <span className="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-wider text-[#737686]">
                  <svg className="h-3.5 w-3.5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Rendered Markdown Preview
                </span>
                <a
                  href={activePdfUrl ?? undefined}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1.5 text-[10px] font-bold text-[#004ac6] hover:text-[#2563eb] transition-colors"
                >
                  <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
                  </svg>
                  View raw source
                </a>
              </div>
              <div className="flex-1 overflow-y-auto">
                {docLoading ? (
                  <div className="flex h-full items-center justify-center">
                    <LoadingSpinner size="md" text="Rendering document..." />
                  </div>
                ) : docError ? (
                  <div className="p-6">
                    <ErrorMessage message={docError} onRetry={() => setDocRetry((n) => n + 1)} />
                  </div>
                ) : (
                  <article
                    className="markdown-body mx-auto max-w-[860px] px-6 py-7 sm:px-8"
                    dangerouslySetInnerHTML={{ __html: renderedDocHtml }}
                  />
                )}
              </div>
            </div>
          ) : activePdfUrl ? (
            <iframe
              src={`${activePdfUrl}#view=FitH`}
              title={`${selectedDoc === 'manual' ? 'Manual' : 'User Guide'} Viewer`}
              className="w-full h-full border-0"
            />
          ) : (
            <div className="absolute inset-0 flex flex-col items-center justify-center text-center p-8">
              <div className="p-4 rounded-full bg-amber-50 border border-amber-200 text-amber-500 mb-4">
                <svg className="h-10 w-10" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <h4 className="font-extrabold text-[#131b2e] text-base">Document Not Uploaded</h4>
              <p className="text-xs text-[#737686] mt-1 max-w-sm">
                The {selectedDoc === 'manual' ? 'User Manual' : 'User Guide'} PDF is not uploaded for this machine twin. The AI assistant will still use fallback knowledge bases to reply to questions.
              </p>
              <button
                onClick={() => navigate(`/machines/${machine.id}`)}
                className="mt-4 vt-button-secondary text-xs min-h-9"
              >
                Upload Manual in Twin Panel
              </button>
            </div>
          )}
        </div>
      </div>
      
      {/* ─── Right Side: Side Area AI Chat Companion Pane (35% width on desktop) ─── */}
      <div className="w-full lg:w-[450px] shrink-0 flex flex-col gap-4 h-full">
        
        {/* Chat Component */}
        <div className="vt-panel flex-1 flex flex-col overflow-hidden h-full">
          
          {/* Chat Header Banner */}
          <div className="border-b border-[#c3c6d7]/60 px-5 py-4 bg-[#fbfbff] flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <span className="flex h-3 w-3 relative">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#7c3aed] opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-3 w-3 bg-[#7c3aed]"></span>
                </span>
                <div>
                  <h3 className="text-sm font-extrabold text-[#131b2e]">AI Study Guide Companion</h3>
                  <p className="text-[10px] text-[#737686]">Cognitive retrieval from manual twins</p>
                </div>
              </div>
              <button
                onClick={handleClearChat}
                className="text-xs font-bold text-red-600 hover:text-red-800 transition-colors"
              >
                Clear Chat
              </button>
            </div>
            
            {/* Model select dropdown */}
            <div className="flex items-center gap-2 bg-slate-50 px-3 py-1.5 rounded-lg border border-slate-200">
              <span className="text-[10px] font-bold text-gray-500 uppercase shrink-0">Model:</span>
              <select
                value={selectedModel}
                onChange={(e) => setSelectedModel(e.target.value)}
                className="flex-1 text-xs bg-transparent border-none text-gray-800 font-semibold focus:outline-none cursor-pointer"
              >
                {models.length === 0 && <option value="">Loading...</option>}
                {models.map((m) => (
                  <option key={m} value={m}>
                    {m}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Quick Study Topics Accordion (integrated inside sidebar) */}
          <div className="border-b border-[#c3c6d7]/40 bg-[#f9fafb]">
            <button
              onClick={() => setShowTopics(!showTopics)}
              className="w-full px-5 py-2.5 flex items-center justify-between text-xs font-bold text-[#434655] hover:text-[#131b2e] transition-colors"
            >
              <span className="flex items-center gap-2">
                <svg className="h-4 w-4 text-[#7c3aed]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
                </svg>
                Quick Study Topics
              </span>
              <svg
                className={`h-4 w-4 transform transition-transform text-[#737686] ${showTopics ? 'rotate-180' : ''}`}
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" />
              </svg>
            </button>
            
            {showTopics && (
              <div className="px-5 pb-3.5 pt-1.5 grid grid-cols-2 gap-2 animate-fade-in">
                {QUICK_STUDY_TOPICS.map((topic, index) => (
                  <button
                    key={index}
                    onClick={() => handleSend(topic.prompt)}
                    disabled={sending}
                    className="text-left p-2 rounded-lg border border-[#c3c6d7]/60 bg-white hover:border-[#7c3aed] transition-all flex items-start gap-2 group disabled:opacity-50"
                  >
                    <div className="shrink-0 mt-0.5">{topic.icon}</div>
                    <h4 className="text-[10px] font-extrabold text-[#131b2e] group-hover:text-[#7c3aed] transition-colors leading-tight">
                      {topic.title}
                    </h4>
                  </button>
                ))}
              </div>
            )}
          </div>
          
          {/* Messages Feed (Scrollable) */}
          <div className="flex-1 overflow-y-auto bg-gradient-to-b from-white to-[#fafaff] p-5 space-y-4">
            {messages.length === 0 ? (
              <div className="h-full flex flex-col items-center justify-center text-center p-6">
                <div className="p-3.5 rounded-full bg-[#fcfaff] border border-[#7c3aed]/20 mb-3">
                  <svg className="h-8 w-8 text-[#7c3aed]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
                  </svg>
                </div>
                <h4 className="font-extrabold text-[#131b2e] text-sm">Ask Study Buddy</h4>
                <p className="text-[11px] text-[#737686] mt-1 max-w-[280px]">
                  Ask questions about maintenance, needle configuration, or operation guidelines. We'll cross-reference the PDFs on the left.
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {messages.map((msg, idx) => (
                  <div
                    key={msg.id}
                    className={`flex ${msg.sender === 'USER' ? 'justify-end' : 'justify-start'} animate-fade-in`}
                    style={{ animationDelay: `${idx * 0.02}s` }}
                  >
                    <div
                      className={`max-w-[90%] rounded-xl p-3.5 shadow-sm text-xs leading-relaxed ${
                        msg.sender === 'USER'
                          ? 'bg-[#2563eb] text-white rounded-br-none'
                          : 'vt-ai-card text-[#131b2e] rounded-bl-none'
                      }`}
                    >
                      {msg.sender === 'USER' ? (
                        <div className="whitespace-pre-wrap">{msg.messageText}</div>
                      ) : (
                        <div
                          className="markdown-body text-xs"
                          dangerouslySetInnerHTML={{
                            __html: DOMPurify.sanitize(marked.parse(msg.messageText, { async: false }) as string),
                          }}
                        />
                      )}
                      <p className={`mt-1.5 text-[8px] text-right font-medium ${msg.sender === 'USER' ? 'text-blue-100' : 'text-[#737686]'}`}>
                        {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </p>
                    </div>
                  </div>
                ))}

                {sending && (
                  <div className="flex justify-start">
                    <div className="vt-ai-card rounded-xl rounded-bl-none p-3 max-w-[50px] shadow-sm">
                      <div className="flex items-center gap-1 py-1">
                        <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-[#7c3aed]/60" style={{ animationDelay: '0ms' }} />
                        <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-[#7c3aed]/60" style={{ animationDelay: '150ms' }} />
                        <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-[#7c3aed]/60" style={{ animationDelay: '300ms' }} />
                      </div>
                    </div>
                  </div>
                )}
                
                <div ref={messagesEndRef} />
              </div>
            )}
          </div>
          
          {/* Chat Form Input */}
          <div className="border-t border-[#c3c6d7]/60 p-3 bg-white">
            {error && (
              <div className="mb-2.5 rounded-lg bg-[#ffdad6] p-2.5 text-[11px] font-semibold text-[#93000a] animate-fade-in">
                {error}
              </div>
            )}
            
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSend(input);
              }}
              className="flex gap-2"
            >
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder={`Ask about ${machine.name}...`}
                disabled={sending}
                className="vt-input flex-1 focus:border-[#7c3aed] focus:ring-[#7c3aed]/15 disabled:opacity-50 text-xs min-h-10 px-3 py-2"
              />
              
              <button
                type="submit"
                disabled={!input.trim() || sending}
                className="vt-button-ai px-4 rounded-lg text-white text-xs min-h-10"
              >
                {sending ? (
                  <div className="h-3 w-3 animate-spin rounded-full border-2 border-white border-t-transparent" />
                ) : (
                  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5" />
                  </svg>
                )}
              </button>
            </form>
          </div>

        </div>
      </div>
      
    </div>
  );
}
