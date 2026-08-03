import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAllReports } from '../api/api';
import type { DiagnosisReport } from '../api/api';
import LoadingSpinner from '../components/LoadingSpinner';

export default function AdminReports() {
  const [reports, setReports] = useState<DiagnosisReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    getAllReports()
      .then((res) => setReports(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const filtered = reports.filter(
    (r) =>
      r.machineName.toLowerCase().includes(search.toLowerCase()) ||
      r.diagnosisProblem.toLowerCase().includes(search.toLowerCase()),
  );

  return (
    <div className="vt-page">
      <div className="animate-fade-in mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="vt-label">AI Findings</p>
          <h1 className="vt-title mt-1">Diagnosis Reports</h1>
          <p className="vt-subtitle">{reports.length} report{reports.length !== 1 ? 's' : ''} total</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative w-full sm:w-64">
            <svg className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#737686]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
            </svg>
            <input
              type="text"
              placeholder="Search reports..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="vt-input pl-10"
            />
          </div>
          <Link
            to="/admin/dashboard"
            className="vt-button-secondary"
          >
            Dashboard
          </Link>
        </div>
      </div>

      {loading && <LoadingSpinner size="lg" text="Loading reports..." />}

      {!loading && filtered.length > 0 && (
        <div className="animate-fade-in space-y-4">
          {filtered.map((report) => (
            <Link
              key={report.id}
              to={`/diagnosis/${report.id}`}
              className="vt-ai-card block p-5 transition-all hover:-translate-y-0.5 hover:shadow-[0_8px_24px_rgba(124,58,237,0.08)]"
            >
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="truncate font-semibold text-[#131b2e]">{report.machineName}</span>
                    <span className="inline-flex items-center rounded-full bg-[#eaddff] px-2.5 py-0.5 text-xs font-semibold text-[#5a00c6]">
                      Report
                    </span>
                  </div>
                  <p className="mt-1 line-clamp-1 text-sm text-[#434655]">{report.problemDescription}</p>
                </div>
                <div className="flex shrink-0 items-center gap-4 text-sm text-[#737686]">
                  <span>{new Date(report.timestamp).toLocaleDateString()}</span>
                  <span className="font-semibold text-[#004ac6]">
                    View &rarr;
                  </span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}

      {!loading && filtered.length === 0 && (
        <div className="vt-empty animate-fade-in">
          <svg className="mx-auto mb-4 h-12 w-12 text-[#737686]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
          </svg>
          <h3 className="mb-2 text-lg font-semibold text-[#131b2e]">{search ? 'No matching reports' : 'No reports yet'}</h3>
          <p className="text-sm text-[#434655]">{search ? 'Try a different search term.' : 'Reports will appear here after diagnoses are run.'}</p>
        </div>
      )}
    </div>
  );
}
