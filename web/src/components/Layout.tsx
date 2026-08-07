import { useState } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import ProfileModal from './ProfileModal';
import SettingsModal from './SettingsModal';

const navLinks = [
  { to: '/', label: 'Dashboard', icon: 'M3.75 4.5h6.75v6.75H3.75V4.5zm9.75 0h6.75v3.75H13.5V4.5zm0 6.75h6.75v8.25H13.5v-8.25zM3.75 14.25h6.75v5.25H3.75v-5.25z' },
  { to: '/announcements', label: 'Announcement', icon: 'M14.857 17.082a9.001 9.001 0 01-7.143 0M18 10a6 6 0 00-12 0c0 7 1 9 1 9h10s1-2 1-9z' },
  { to: '/diagnose', label: 'Machine Diagnosis', icon: 'M9.813 15.904 9 18.75l-.813-2.846a4.5 4.5 0 0 0-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 0 0 3.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 0 0 3.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 0 0-3.09 3.09z', badge: 'AI' },
  { to: '/learn', label: 'Interactive Learning', icon: 'M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25', badge: 'AI' },
  { to: '/call-experts', label: 'Call Experts', icon: 'M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z' }
];

export default function Layout() {
  const [open, setOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  return (
    <div className="min-h-screen bg-vt-background text-vt-ink">
      <Sidebar
        onNavigate={() => setOpen(false)}
        open={open}
        onProfileClick={() => setIsProfileOpen(true)}
        onSettingsClick={() => setIsSettingsOpen(true)}
      />
      {open && <button aria-label="Close navigation" className="fixed inset-0 z-30 bg-slate-950/30 lg:hidden" onClick={() => setOpen(false)} />}

      <ProfileModal isOpen={isProfileOpen} onClose={() => setIsProfileOpen(false)} />
      <SettingsModal isOpen={isSettingsOpen} onClose={() => setIsSettingsOpen(false)} />

      <div className="min-h-screen lg:pl-[280px]">
        <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b border-[#c3c6d7]/80 bg-[#faf8ff]/85 px-4 backdrop-blur-md sm:px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => setOpen(true)}
              className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-[#c3c6d7] bg-white text-[#434655] lg:hidden"
              aria-label="Open navigation"
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
              </svg>
            </button>
            {/* Removed Search in the top header */}
            <span className="text-sm font-semibold text-[#434655]">VisionTwin AI Diagnostics Dashboard</span>
          </div>
          <div className="flex items-center gap-3">
            {/* Removed Diagnose and Profile FM options in the top header */}
            <div className="flex items-center gap-2">
              <span className="h-2 w-2 rounded-full bg-[#10b981] animate-pulse" />
              <span className="text-xs font-semibold text-gray-500">Systems Online</span>
            </div>
          </div>
        </header>

        <main>
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function Sidebar({
  onNavigate,
  open,
  onProfileClick,
  onSettingsClick
}: {
  onNavigate: () => void;
  open: boolean;
  onProfileClick: () => void;
  onSettingsClick: () => void;
}) {
  const location = useLocation();

  const renderLink = (link: { to: string; label: string; icon: string; badge?: string }) => {
    const isActive = location.pathname === link.to || (link.to !== '/' && location.pathname.startsWith(link.to));
    return (
      <Link
        key={link.to}
        to={link.to}
        onClick={onNavigate}
        className={`relative flex items-center justify-between rounded-lg px-4 py-2.5 text-sm font-semibold transition-all ${
          isActive
            ? 'bg-[#e2e7ff] text-[#004ac6] before:absolute before:left-0 before:top-2 before:h-6 before:w-0.5 before:rounded-full before:bg-[#2563eb]'
            : 'text-[#434655] hover:bg-[#f2f3ff] hover:text-[#131b2e]'
        }`}
      >
        <span className="flex items-center gap-3">
          <svg className="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
            <path strokeLinecap="round" strokeLinejoin="round" d={link.icon} />
          </svg>
          {link.label}
        </span>
        {link.badge && (
          <span className={`rounded px-1.5 py-0.5 text-[10px] font-bold ${
            isActive ? 'bg-[#7c3aed] text-white' : 'bg-[#7c3aed]/15 text-[#7c3aed]'
          }`}>
            {link.badge}
          </span>
        )}
      </Link>
    );
  };

  return (
    <aside className={`fixed left-0 top-0 z-40 flex h-screen w-[280px] flex-col border-r border-[#c3c6d7]/80 bg-white p-4 transition-transform duration-200 lg:translate-x-0 ${open ? 'translate-x-0' : '-translate-x-full'}`}>
      <Link to="/" onClick={onNavigate} className="mb-6 mt-2 flex items-center gap-3 px-3">
        <img
          src="/VisionTwinLogo.png"
          alt="VisionTwin logo"
          className="h-10 w-10 rounded-lg border border-[#c3c6d7]/80 bg-white object-contain p-1 shadow-sm"
        />
        <div>
          <h1 className="text-lg font-bold leading-6 text-[#004ac6]">VisionTwin AI</h1>
          <p className="text-[11px] font-medium uppercase leading-4 text-[#737686]">Operations Center</p>
        </div>
      </Link>

      <div className="flex flex-1 flex-col gap-6 overflow-y-auto">
        <nav className="flex flex-col gap-1">
          {navLinks.map((link) => renderLink(link))}
        </nav>
      </div>

      <div className="mt-auto pt-4 border-t border-[#c3c6d7]/50 flex flex-col gap-3">
        {/* Settings button */}
        <button
          onClick={onSettingsClick}
          className="flex items-center gap-3 w-full rounded-lg px-4 py-2 text-sm font-semibold text-[#434655] hover:bg-[#f2f3ff] hover:text-[#131b2e] transition-all"
        >
          <svg className="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9.594 3.94c.09-.542.56-.94 1.11-.94h2.593c.55 0 1.02.398 1.11.94l.213 1.281c.063.374.313.686.645.87.074.04.147.083.22.127.324.196.72.257 1.075.124l1.217-.456a1.125 1.125 0 011.37.49l1.296 2.247a1.125 1.125 0 01-.26 1.43l-1.003.828c-.293.241-.438.613-.43.992a7.723 7.723 0 010 .255c-.008.378.137.75.43.991l1.004.827c.424.35.534.954.26 1.43l-1.298 2.247a1.125 1.125 0 01-1.369.491l-1.217-.456c-.355-.133-.75-.072-1.076.124a6.57 6.57 0 01-.22.128c-.331.183-.581.495-.644.869l-.213 1.28c-.09.543-.56.94-1.11.94h-2.594c-.55 0-1.02-.398-1.11-.94l-.213-1.281c-.062-.374-.312-.686-.644-.87a6.52 6.52 0 01-.22-.127c-.325-.196-.72-.257-1.076-.124l-1.217.456a1.125 1.125 0 01-1.369-.49l-1.297-2.247a1.125 1.125 0 01.26-1.43l1.004-.827c.292-.24.437-.613.43-.992a6.932 6.932 0 010-.255c.007-.378-.138-.75-.43-.991l-1.004-.827a1.125 1.125 0 01-.26-1.43l1.297-2.247a1.125 1.125 0 011.37-.491l1.216.456c.356.133.751.072 1.076-.124.072-.044.146-.087.22-.128.332-.183.582-.495.644-.869l.214-1.28z" />
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          Settings
        </button>

        {/* Profile button */}
        <button
          onClick={onProfileClick}
          className="flex items-center gap-3 w-full rounded-xl border border-[#c3c6d7]/50 bg-slate-50/50 p-2.5 text-left hover:bg-slate-100/70 transition-all"
        >
          <img
            src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80"
            alt="Profile Avatar"
            className="h-10 w-10 rounded-full border border-gray-200"
          />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-gray-900 truncate">Nil Yeager</p>
            <p className="text-xs text-gray-500 truncate">System Operator</p>
          </div>
        </button>
      </div>
    </aside>
  );
}
