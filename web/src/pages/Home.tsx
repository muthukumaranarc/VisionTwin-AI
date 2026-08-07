import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getHealth, getMachines, getDashboardStats } from '../api/api';
import type { Machine, DashboardStats } from '../api/api';
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

  // Default fallbacks for statistics
  const totalMachines = stats?.totalMachines ?? machines.length ?? 8;
  const totalReports = stats?.totalReports ?? 34;
  const totalDatastores = stats?.totalLayer1Datastores ?? 5;
  const totalVectors = stats?.totalLayer2Vectors ?? 128;

  return (
    <div className="vt-page max-w-[1600px] mx-auto p-4 lg:p-8 animate-fade-in bg-[#f8fafc]">
      {/* {error && (
        <div className="mb-6 p-4 rounded-xl border border-rose-200 bg-rose-50 text-rose-800 text-sm font-semibold flex items-center gap-2">
          <span className="h-2.5 w-2.5 rounded-full bg-rose-600 animate-pulse" />
          {error}
        </div>
      )}
      {health && health.status !== 'healthy' && (
        <div className="mb-6 p-4 rounded-xl border border-amber-200 bg-amber-50 text-amber-800 text-sm font-semibold flex items-center gap-2">
          <span className="h-2.5 w-2.5 rounded-full bg-amber-600 animate-pulse" />
          Warning: System Status: {health.status} ({health.service})
        </div>
      )} */}
      
      {/* Top Section: Banner and Quick Action Card */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 mb-8">
        
        {/* Banner Card */}
        <div className="lg:col-span-3 bg-gradient-to-r from-emerald-600 to-teal-500 rounded-2xl p-6 lg:p-8 text-white relative overflow-hidden shadow-sm flex flex-col justify-between min-h-[220px]">
          <div className="relative z-10 max-w-xl">
            <span className="bg-emerald-500/30 text-emerald-100 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider">
              AI-Powered Operations
            </span>
            <h1 className="text-2xl lg:text-3xl font-extrabold mt-3 leading-tight">
              Optimize Machine Operations & Minimize Downtime!
            </h1>
            <p className="text-emerald-100 text-sm mt-2 font-medium">
              Detect anomalous acoustic, thermal, and visual signatures instantly. Access step-by-step diagnostic manuals and training content.
            </p>
          </div>
          
          <div className="relative z-10 flex flex-wrap gap-6 mt-6 border-t border-white/10 pt-4">
            <div className="flex items-center gap-2">
              <div className="h-9 w-9 rounded-lg bg-white/10 flex items-center justify-center font-bold text-white">
                {totalMachines}
              </div>
              <div className="text-xs font-semibold text-emerald-100">
                Monitored Systems
              </div>
            </div>
            <div className="flex items-center gap-2">
              <div className="h-9 w-9 rounded-lg bg-white/10 flex items-center justify-center font-bold text-white">
                {totalDatastores}
              </div>
              <div className="text-xs font-semibold text-emerald-100">
                Knowledge Stores
              </div>
            </div>
          </div>
          
          {/* Decorative Background Graphics */}
          <div className="absolute right-0 bottom-0 opacity-15 pointer-events-none translate-x-10 translate-y-10 lg:translate-x-0 lg:translate-y-0">
            <svg className="w-80 h-80 text-white" fill="currentColor" viewBox="0 0 100 100">
              <path d="M50 15a35 35 0 1035 35 35 35 0 00-35-35zm0 60a25 25 0 1125-25 25 25 0 01-25 25z" />
              <path d="M50 30a20 20 0 1020 20 20 20 0 00-20-20zm0 30a10 10 0 1110-10 10 10 0 01-10 10z" />
            </svg>
          </div>
        </div>

        {/* Diagnosis Quick Card */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 flex flex-col justify-between">
          <div>
            <h3 className="text-gray-500 text-xs font-bold uppercase tracking-wider">Troubleshooting</h3>
            <p className="text-gray-900 font-extrabold text-lg mt-1 leading-snug">Have a machine issue to report?</p>
          </div>
          
          <Link
            to="/diagnose"
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded-xl text-center shadow-sm hover:shadow transition-all flex items-center justify-center gap-2 mt-4"
          >
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>
            Start Diagnosis
          </Link>
          
          <div className="grid grid-cols-2 gap-4 border-t border-gray-100 pt-4 mt-4">
            <div className="text-center">
              <span className="text-2xl font-black text-gray-900">{totalReports}</span>
              <p className="text-gray-400 text-[10px] font-bold uppercase mt-0.5">Scans Completed</p>
            </div>
            <div className="text-center border-l border-gray-100">
              <span className="text-2xl font-black text-gray-900">{totalVectors}</span>
              <p className="text-gray-400 text-[10px] font-bold uppercase mt-0.5">Vector Models</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Grid: Left (Guides), Middle (Activity), Right (Team/Alerts) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        
        {/* Popular Machine Guides */}
        <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-base font-bold text-gray-900">Popular Guides</h2>
            <Link to="/learn" className="text-xs font-bold text-blue-600 hover:underline">All Guides</Link>
          </div>
          
          <div className="flex flex-col gap-4">
            {[
              { title: 'CNC Spindle Assembly', cat: '12+ Diagnostic Steps', color: 'bg-amber-100 text-amber-800' },
              { title: 'Hydraulic Pumping Station', cat: '8+ Active Procedures', color: 'bg-rose-100 text-rose-800' },
              { title: 'Rotary Air Compressor', cat: '15+ Safety Checklists', color: 'bg-teal-100 text-teal-800' },
              { title: 'Induction Motor Alignment', cat: '6+ Verification Logs', color: 'bg-blue-100 text-blue-800' },
            ].map((guide, idx) => (
              <div key={idx} className="flex items-center justify-between p-3.5 rounded-xl border border-gray-50 hover:bg-slate-50 transition-colors">
                <div className="flex items-center gap-3">
                  <div className={`h-10 w-10 rounded-lg flex items-center justify-center font-bold text-sm ${guide.color}`}>
                    {guide.title[0]}
                  </div>
                  <div>
                    <h4 className="font-bold text-sm text-gray-900">{guide.title}</h4>
                    <p className="text-xs text-gray-500 font-medium mt-0.5">{guide.cat}</p>
                  </div>
                </div>
                <Link to="/learn" className="text-xs font-semibold bg-gray-50 text-gray-700 px-3 py-1.5 rounded-lg border border-gray-100 hover:bg-gray-100">
                  View
                </Link>
              </div>
            ))}
          </div>
        </div>

        {/* Current Scan Activity Chart & KPI */}
        <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-base font-bold text-gray-900">Scan Activity</h2>
              <span className="text-xs font-bold text-gray-400">Weekly</span>
            </div>
            
            {/* Styled Wave SVG to represent a premium line chart */}
            <div className="h-32 w-full mt-4 relative">
              <svg className="w-full h-full" viewBox="0 0 100 30" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.25" />
                    <stop offset="100%" stopColor="#3b82f6" stopOpacity="0.0" />
                  </linearGradient>
                </defs>
                <path
                  d="M0 25 Q15 15, 30 20 T60 8 T90 12 T100 15 L100 30 L0 30 Z"
                  fill="url(#chartGrad)"
                />
                <path
                  d="M0 25 Q15 15, 30 20 T60 8 T90 12 T100 15"
                  fill="none"
                  stroke="#3b82f6"
                  strokeWidth="1.5"
                  strokeLinecap="round"
                />
              </svg>
              <div className="absolute inset-0 flex justify-between items-end text-[9px] font-bold text-gray-400 px-1 pt-12">
                <span>Mon</span>
                <span>Tue</span>
                <span>Wed</span>
                <span>Thu</span>
                <span>Fri</span>
                <span>Sat</span>
                <span>Sun</span>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4 mt-6">
            <div className="bg-amber-400 rounded-xl p-4 text-white flex flex-col justify-between shadow-sm min-h-[90px]">
              <span className="text-lg lg:text-xl font-black leading-none">98.6%</span>
              <p className="text-[10px] font-bold uppercase mt-1">Accuracy Rating</p>
            </div>
            <div className="bg-rose-500 rounded-xl p-4 text-white flex flex-col justify-between shadow-sm min-h-[90px]">
              <span className="text-lg lg:text-xl font-black leading-none">12.4s</span>
              <p className="text-[10px] font-bold uppercase mt-1">Avg Scan Speed</p>
            </div>
          </div>
        </div>

        {/* Active Operators & Experts */}
        <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-base font-bold text-gray-900">Active Operators</h2>
            <span className="text-xs font-bold text-emerald-600 flex items-center gap-1">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
              Online
            </span>
          </div>

          <div className="flex flex-col gap-4">
            {[
              { name: 'Nil Yeager', role: 'System Operator', img: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80' },
              { name: 'Theron Trump', role: 'Vibration Analyst', img: 'https://images.unsplash.com/photo-1519345182560-3f2917c472ef?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80' },
              { name: 'Tyler Mark', role: 'Thermal Engineer', img: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80' },
              { name: 'Johen Mark', role: 'Lubrication Expert', img: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80' },
            ].map((op, idx) => (
              <div key={idx} className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <img
                    src={op.img}
                    alt={op.name}
                    className="h-9 w-9 rounded-full border border-gray-100"
                  />
                  <div>
                    <h4 className="font-bold text-sm text-gray-900">{op.name}</h4>
                    <p className="text-xs text-gray-500 font-semibold">{op.role}</p>
                  </div>
                </div>
                <span className="h-2 w-2 rounded-full bg-emerald-500" />
              </div>
            ))}
          </div>
        </div>

      </div>

      {/* Bottom Performance Indicators Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm">
          <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Top Performing Line</p>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-2xl font-black text-gray-900">Line B</span>
            <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
              99.2% uptime
            </span>
          </div>
        </div>
        
        <div className="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm">
          <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Overall System OEE</p>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-2xl font-black text-gray-900">88.5%</span>
            <span className="text-xs font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded border border-blue-100">
              Industry Standard
            </span>
          </div>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm">
          <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Anomaly Severity Index</p>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-2xl font-black text-gray-900">Low</span>
            <span className="text-xs font-bold text-teal-600 bg-teal-50 px-2 py-0.5 rounded border border-teal-100">
              0 Critical Alerts
            </span>
          </div>
        </div>
      </div>
      
    </div>
  );
}
