import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getHealth, getMachines, getDashboardStats } from '../api/api';
import type { Machine, DashboardStats } from '../api/api';
import MachineCard from '../components/MachineCard';
import LoadingSpinner from '../components/LoadingSpinner';

export default function Home() {
  const [health, setHealth] = useState<{ status: string; service: string } | null>(null);
  const [machines, setMachines] = useState<Machine[]>([]);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [healthRes, machinesRes, statsRes] = await Promise.allSettled([
          getHealth(),
          getMachines(),
          getDashboardStats().catch(() => null), // admin endpoint may fail
        ]);

        if (healthRes.status === 'fulfilled') setHealth(healthRes.value.data);
        if (machinesRes.status === 'fulfilled') setMachines(machinesRes.value.data);
        if (statsRes.status === 'fulfilled' && statsRes.value) setStats(statsRes.value.data);
      } catch {
        setError('Failed to connect to the backend. Make sure the server is running.');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <LoadingSpinner size="lg" text="Connecting to VisionTwin backend..." />;

  return (
    <div className="vt-page">
      <div className="animate-fade-in mb-8 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="vt-label">Operations Dashboard</p>
          <h1 className="mt-2 text-3xl font-bold leading-10 text-[#131b2e] sm:text-4xl">
            Intelligent Machine Diagnostics
          </h1>
          <p className="mt-2 max-w-3xl text-base leading-7 text-[#434655]">
            AI-powered fault detection, diagnosis, reference imagery, and knowledge management for textile machine operations.
          </p>
        </div>
        {health && (
          <div className="inline-flex items-center gap-2 rounded-full border border-[#00788c]/25 bg-[#acedff]/35 px-4 py-2 text-sm font-semibold text-[#005e6e]">
            <span className="h-2 w-2 rounded-full bg-[#00788c] animate-pulse-glow" />
            {health.status} &mdash; {health.service}
          </div>
        )}
        {error && (
          <div className="inline-flex items-center gap-2 rounded-full border border-[#ba1a1a]/20 bg-[#ffdad6] px-4 py-2 text-sm font-semibold text-[#93000a]">
            <span className="h-2 w-2 rounded-full bg-[#ba1a1a]" />
            {error}
          </div>
        )}
      </div>

      {/* Stats row */}
      {stats && (
        <div className="animate-fade-in mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {[
            { label: 'Machines', value: stats.totalMachines, icon: 'M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z' },
            { label: 'Diagnosis Reports', value: stats.totalReports, icon: 'M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z' },
            { label: 'Knowledge Stores', value: stats.totalLayer1Datastores, icon: 'M4.26 10.147a60.438 60.438 0 016.207-6.153 1.5 1.5 0 012.066 0 60.75 60.75 0 016.207 6.153M12.75 18a.75.75 0 11-1.5 0 .75.75 0 011.5 0zM12 9.75v4.5' },
            { label: 'Vector Embeddings', value: stats.totalLayer2Vectors, icon: 'M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z' },
          ].map((stat) => (
            <div key={stat.label} className="vt-card p-5">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="vt-label">{stat.label}</p>
                  <p className="mt-3 text-4xl font-bold leading-none text-[#131b2e]">{stat.value}</p>
                </div>
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-[#e2e7ff]">
                  <svg className="h-5 w-5 text-[#2563eb]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d={stat.icon} />
                  </svg>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Quick actions */}
      <div className="animate-fade-in mb-10 grid gap-4 lg:grid-cols-3">
        <Link
          to="/machines"
          className="vt-card group p-6 transition-all hover:-translate-y-0.5 hover:border-[#2563eb]/40 hover:shadow-[0_8px_24px_rgba(15,23,42,0.08)]"
        >
          <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-50 group-hover:bg-indigo-100 transition-colors">
            <svg className="h-5 w-5 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
            </svg>
          </div>
          <h3 className="mb-1 font-semibold text-gray-900">Browse Machines</h3>
          <p className="text-sm text-gray-500">View all registered machines and their reference data</p>
        </Link>

        <Link
          to="/diagnose"
          className="vt-ai-card group p-6 transition-all hover:-translate-y-0.5 hover:shadow-[0_8px_24px_rgba(124,58,237,0.08)]"
        >
          <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-lg bg-purple-50 group-hover:bg-purple-100 transition-colors">
            <svg className="h-5 w-5 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 3.75v11.25m0 0l-3-3m3 3l3-3m0 0V3.75M3 12c0 4.243 3.757 8.25 8.25 8.25S19.5 16.243 19.5 12" />
            </svg>
          </div>
          <h3 className="mb-1 font-semibold text-gray-900">Diagnose a Problem</h3>
          <p className="text-sm text-gray-500">Upload an image and describe the issue for AI analysis</p>
        </Link>

        <Link
          to="/admin"
          className="vt-card group p-6 transition-all hover:-translate-y-0.5 hover:border-[#2563eb]/40 hover:shadow-[0_8px_24px_rgba(15,23,42,0.08)]"
        >
          <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-lg bg-amber-50 group-hover:bg-amber-100 transition-colors">
            <svg className="h-5 w-5 text-amber-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9" />
            </svg>
          </div>
          <h3 className="mb-1 font-semibold text-gray-900">Admin Panel</h3>
          <p className="text-sm text-gray-500">View reports and manage the system</p>
        </Link>
      </div>

      {/* Recent machines */}
      {machines.length > 0 && (
        <>
          <div className="mb-6 flex items-center justify-between">
            <h2 className="text-xl font-bold text-[#131b2e]">Registered Machines</h2>
            <Link to="/machines" className="text-sm font-semibold text-[#004ac6] transition-colors hover:text-[#2563eb]">
              View All &rarr;
            </Link>
          </div>
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {machines.slice(0, 6).map((machine) => (
              <MachineCard key={machine.id} machine={machine} />
            ))}
          </div>
        </>
      )}

      {machines.length === 0 && !error && (
        <div className="vt-empty animate-fade-in">
          <svg className="mx-auto mb-4 h-12 w-12 text-[#737686]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
          </svg>
          <h3 className="mb-2 text-lg font-semibold text-[#131b2e]">No machines yet</h3>
          <p className="mb-6 text-sm text-[#434655]">Get started by adding machines through the API or mobile app.</p>
        </div>
      )}
    </div>
  );
}
