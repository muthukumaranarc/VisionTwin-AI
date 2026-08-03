import { useState } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';

const navLinks = [
  { to: '/', label: 'Dashboard', icon: 'M3.75 4.5h6.75v6.75H3.75V4.5zm9.75 0h6.75v3.75H13.5V4.5zm0 6.75h6.75v8.25H13.5v-8.25zM3.75 14.25h6.75v5.25H3.75v-5.25z' },
  { to: '/machines', label: 'Machines', icon: 'M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z' },
  { to: '/diagnose', label: 'AI Diagnosis', icon: 'M9.813 15.904 9 18.75l-.813-2.846a4.5 4.5 0 0 0-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 0 0 3.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 0 0 3.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 0 0-3.09 3.09z' },
  { to: '/admin/reports', label: 'Reports', icon: 'M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5A3.375 3.375 0 0 0 10.125 2.25H5.625A1.125 1.125 0 0 0 4.5 3.375v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V14.25z' },
  { to: '/admin', label: 'Admin', icon: 'M15.75 9V5.25A2.25 2.25 0 0 0 13.5 3h-6a2.25 2.25 0 0 0-2.25 2.25v13.5A2.25 2.25 0 0 0 7.5 21h6a2.25 2.25 0 0 0 2.25-2.25V15m3 0 3-3m0 0-3-3m3 3H9' },
];

export default function Layout() {
  const [open, setOpen] = useState(false);

  return (
    <div className="min-h-screen bg-vt-background text-vt-ink">
      <Sidebar onNavigate={() => setOpen(false)} open={open} />
      {open && <button aria-label="Close navigation" className="fixed inset-0 z-30 bg-slate-950/30 lg:hidden" onClick={() => setOpen(false)} />}

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
            <div className="hidden w-80 items-center rounded-lg border border-[#c3c6d7] bg-white px-3 py-2 text-sm text-[#737686] shadow-sm md:flex">
              <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607z" />
              </svg>
              Search machines, reports...
            </div>
          </div>
          <div className="flex items-center gap-3">
            <Link to="/diagnose" className="vt-button-ai">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9.813 15.904 9 18.75l-.813-2.846a4.5 4.5 0 0 0-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 0 0 3.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 0 0 3.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 0 0-3.09 3.09z" />
              </svg>
              Diagnose
            </Link>
            <div className="hidden h-9 w-9 items-center justify-center rounded-full bg-[#dae2fd] text-xs font-bold text-[#004ac6] sm:flex">FM</div>
          </div>
        </header>

        <main>
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function Sidebar({ onNavigate, open }: { onNavigate: () => void; open: boolean }) {
  const location = useLocation();

  return (
    <aside className={`fixed left-0 top-0 z-40 flex h-screen w-[280px] flex-col border-r border-[#c3c6d7]/80 bg-white p-4 transition-transform duration-200 lg:translate-x-0 ${open ? 'translate-x-0' : '-translate-x-full'}`}>
      <Link to="/" onClick={onNavigate} className="mb-8 mt-2 flex items-center gap-3 px-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-[#2563eb] text-sm font-bold text-white">VT</div>
        <div>
          <h1 className="text-lg font-bold leading-6 text-[#004ac6]">VisionTwin AI</h1>
          <p className="text-[11px] font-medium uppercase leading-4 text-[#737686]">Textile Operations</p>
        </div>
      </Link>

      <nav className="flex flex-1 flex-col gap-1">
        {navLinks.map((link) => {
          const isActive = location.pathname === link.to || (link.to !== '/' && location.pathname.startsWith(link.to));
          return (
            <Link
              key={link.to}
              to={link.to}
              onClick={onNavigate}
              className={`relative flex items-center gap-3 rounded-lg px-4 py-2.5 text-sm font-semibold transition-colors ${
                isActive
                  ? 'bg-[#e2e7ff] text-[#004ac6] before:absolute before:left-0 before:top-2 before:h-6 before:w-0.5 before:rounded-full before:bg-[#2563eb]'
                  : 'text-[#434655] hover:bg-[#f2f3ff] hover:text-[#131b2e]'
              }`}
            >
              <svg className="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                <path strokeLinecap="round" strokeLinejoin="round" d={link.icon} />
              </svg>
              {link.label}
            </Link>
          );
        })}
      </nav>

      <div className="rounded-xl border border-[#c3c6d7]/80 bg-[#f2f3ff] p-4">
        <p className="vt-label">System State</p>
        <div className="mt-3 flex items-center justify-between">
          <span className="text-sm font-semibold text-[#131b2e]">AI monitoring</span>
          <span className="h-2.5 w-2.5 rounded-full bg-[#00788c]" />
        </div>
      </div>
    </aside>
  );
}
