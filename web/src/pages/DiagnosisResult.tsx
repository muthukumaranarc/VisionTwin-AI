import { useEffect, useState } from 'react';
import { useParams, useLocation, Link } from 'react-router-dom';
import { getReportDetails, getFileUrl } from '../api/api';
import type { DiagnosisReport } from '../api/api';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';

export default function DiagnosisResult() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const [report, setReport] = useState<DiagnosisReport | null>(location.state?.report || null);
  const [loading, setLoading] = useState(!report);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!report && id) {
      getReportDetails(id)
        .then((res) => setReport(res.data))
        .catch((err) => setError(err.message || 'Failed to load report'))
        .finally(() => setLoading(false));
    }
  }, [id, report]);

  if (loading) return <LoadingSpinner size="lg" text="Loading diagnosis report..." />;
  if (error) return <div className="mx-auto max-w-4xl px-4 py-8"><ErrorMessage message={error} /></div>;
  if (!report) return null;

  const imgUrl = getFileUrl('refimages', report.uploadedImagePath);

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="animate-fade-in mb-6">
        <Link to="/diagnose" className="inline-flex items-center gap-2 text-sm font-medium text-gray-500 hover:text-gray-900 transition-colors">
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
          </svg>
          New Diagnosis
        </Link>
      </div>

      <div className="animate-fade-in overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
        {/* Header */}
        <div className="border-b border-gray-100 bg-gradient-to-r from-indigo-50 to-purple-50 px-6 py-5 sm:px-8">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-100">
              <svg className="h-5 w-5 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
              </svg>
            </div>
            <div>
              <h1 className="text-xl font-bold text-gray-900">Diagnosis Report</h1>
              <p className="text-sm text-gray-500">
                {report.machineName} &middot; {new Date(report.timestamp).toLocaleString()}
              </p>
            </div>
          </div>
        </div>

        <div className="p-6 sm:p-8 space-y-6">
          {/* Problem description */}
          <div>
            <h2 className="mb-2 text-sm font-semibold uppercase tracking-wider text-gray-500">Problem Description</h2>
            <p className="rounded-lg bg-gray-50 p-4 text-gray-700">{report.problemDescription}</p>
          </div>

          {/* Diagnosis result */}
          <div className="grid gap-6 md:grid-cols-2">
            <div>
              <h2 className="mb-2 text-sm font-semibold uppercase tracking-wider text-red-500">Identified Problem</h2>
              <div className="rounded-lg border border-red-100 bg-red-50 p-4 text-gray-800">
                {report.diagnosisProblem}
              </div>
            </div>
            <div>
              <h2 className="mb-2 text-sm font-semibold uppercase tracking-wider text-green-600">Recommended Solution</h2>
              <div className="rounded-lg border border-green-100 bg-green-50 p-4 text-gray-800">
                {report.diagnosisSolution}
              </div>
            </div>
          </div>

          {/* Highlight overlay if available */}
          {report.highlightX != null && report.highlightY != null && report.highlightRadius != null && (
            <div>
              <h2 className="mb-2 text-sm font-semibold uppercase tracking-wider text-gray-500">Annotated Image</h2>
              <div className="relative inline-block overflow-hidden rounded-xl border border-gray-200">
                <img src={imgUrl} alt="Diagnosed" className="max-h-96 object-contain" />
                <div className="absolute inset-0 pointer-events-none">
                  <div
                    className="absolute border-3 border-indigo-500/70 rounded-full animate-pulse-glow"
                    style={{
                      left: `${(report.highlightX - report.highlightRadius) * 100}%`,
                      top: `${(report.highlightY - report.highlightRadius) * 100}%`,
                      width: `${report.highlightRadius * 200}%`,
                      height: `${report.highlightRadius * 200}%`,
                    }}
                  />
                </div>
              </div>
            </div>
          )}

          {/* Uploaded image */}
          {!report.highlightX && (
            <div>
              <h2 className="mb-2 text-sm font-semibold uppercase tracking-wider text-gray-500">Uploaded Image</h2>
              <img src={imgUrl} alt="Uploaded" className="max-h-80 rounded-xl border border-gray-200 object-contain" />
            </div>
          )}

          {/* Chat CTA */}
          <div className="rounded-xl border border-indigo-100 bg-indigo-50/50 p-6 text-center">
            <h3 className="mb-2 font-semibold text-gray-900">Need more details?</h3>
            <p className="mb-4 text-sm text-gray-600">
              Chat with our AI assistant to get more information about this diagnosis.
            </p>
            <Link
              to={`/chat/${report.id}`}
              className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white shadow-sm transition hover:bg-indigo-700"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M8.625 12a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H8.25m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H12m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 01-2.555-.337A5.972 5.972 0 015.41 20.97a5.969 5.969 0 01-.474-.065 4.48 4.48 0 00.978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25z" />
              </svg>
              Chat About This Report
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
