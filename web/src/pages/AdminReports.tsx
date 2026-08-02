import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getAllReports } from '../api/api';
import type { DiagnosisReport } from '../api/api';
import LoadingSpinner from '../components/LoadingSpinner';

export default function AdminReports() {
  const navigate = useNavigate();
  const [reports, setReports] = useState<DiagnosisReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    const token = sessionStorage.getItem('admin_token');
    if (!token) {
      navigate('/admin');
      return;
    }
    getAllReports()
      .then((res) => setReports(res.data))
      .catch(() => {
        sessionStorage.removeItem('admin_token');
        navigate('/admin');
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  const filtered = reports.filter(
    (r) =>
      r.machineName.toLowerCase().includes(search.toLowerCase()) ||
      r.diagnosisProblem.toLowerCase().includes(search.toLowerCase()),
  );

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="animate-fade-in mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Diagnosis Reports</h1>
          <p className="mt-1 text-sm text-gray-500">{reports.length} report{reports.length !== 1 ? 's' : ''} total</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative w-full sm:w-64">
            <svg className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
            </svg>
            <input
              type="text"
              placeholder="Search reports..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full rounded-xl border border-gray-300 bg-white py-2.5 pl-10 pr-4 text-sm placeholder-gray-400 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200 transition-all"
            />
          </div>
          <Link
            to="/admin/dashboard"
            className="rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-sm font-medium text-gray-700 shadow-sm transition hover:bg-gray-50"
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
              to={`/admin/reports/${report.id}`}
              className="block rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-all hover:shadow-md hover:-translate-y-0.5"
            >
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-gray-900 truncate">{report.machineName}</span>
                    <span className="inline-flex items-center rounded-full bg-indigo-100 px-2.5 py-0.5 text-xs font-medium text-indigo-700">
                      Report
                    </span>
                  </div>
                  <p className="mt-1 text-sm text-gray-600 line-clamp-1">{report.problemDescription}</p>
                </div>
                <div className="flex items-center gap-4 text-sm text-gray-500 shrink-0">
                  <span>{new Date(report.timestamp).toLocaleDateString()}</span>
                  <span className="text-indigo-600 font-medium">
                    View &rarr;
                  </span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}

      {!loading && filtered.length === 0 && (
        <div className="animate-fade-in rounded-xl border-2 border-dashed border-gray-300 p-12 text-center">
          <svg className="mx-auto mb-4 h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
          </svg>
          <h3 className="mb-2 text-lg font-semibold text-gray-900">{search ? 'No matching reports' : 'No reports yet'}</h3>
          <p className="text-sm text-gray-500">{search ? 'Try a different search term.' : 'Reports will appear here after diagnoses are run.'}</p>
        </div>
      )}
    </div>
  );
}
