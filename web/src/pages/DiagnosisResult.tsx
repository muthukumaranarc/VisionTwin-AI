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
  if (error) return <div className="mx-auto w-full max-w-3xl px-4 py-12 sm:px-6"><ErrorMessage message={error} /></div>;
  if (!report) return null;

  const imgUrl = getFileUrl(report.uploadedImagePath);

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-12 sm:px-6">
      <Link
        to="/diagnose"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-neutral-500 transition-colors hover:text-neutral-900"
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
        </svg>
        New diagnosis
      </Link>

      <header className="mt-8 mb-10">
        <h1 className="text-2xl font-semibold tracking-tight text-neutral-900">{report.machineName}</h1>
        <p className="mt-1 text-sm text-neutral-500">{new Date(report.timestamp).toLocaleString()}</p>
      </header>

      <div className="space-y-10">
        <section>
          <h2 className="mb-2 text-sm font-medium text-neutral-700">Problem</h2>
          <p className="text-[15px] leading-7 text-neutral-800">{report.problemDescription}</p>
        </section>

        {report.highlightX != null && report.highlightY != null && report.highlightRadius != null ? (
          <section>
            <h2 className="mb-3 text-sm font-medium text-neutral-700">Detected issue</h2>
            <div className="relative inline-block overflow-hidden rounded-xl border border-neutral-200 bg-neutral-50">
              <img src={imgUrl} alt="Diagnosed" className="max-h-96 object-contain" />
              <div className="absolute inset-0 pointer-events-none">
                <div
                  className="absolute border-2 border-red-500/80 rounded-full"
                  style={{
                    left: `${(report.highlightX - report.highlightRadius) * 100}%`,
                    top: `${(report.highlightY - report.highlightRadius) * 100}%`,
                    width: `${report.highlightRadius * 200}%`,
                    height: `${report.highlightRadius * 200}%`,
                  }}
                />
              </div>
            </div>
          </section>
        ) : (
          <section>
            <h2 className="mb-3 text-sm font-medium text-neutral-700">Photo</h2>
            <img src={imgUrl} alt="Uploaded" className="max-h-96 rounded-xl border border-neutral-200 bg-neutral-50 object-contain" />
          </section>
        )}

        <section className="grid gap-8 sm:grid-cols-2">
          <div>
            <h2 className="mb-2 text-sm font-medium text-neutral-700">Diagnosis</h2>
            <p className="text-[15px] leading-7 text-neutral-800">{report.diagnosisProblem}</p>
          </div>
          <div>
            <h2 className="mb-2 text-sm font-medium text-neutral-700">Recommended fix</h2>
            <p className="text-[15px] leading-7 text-neutral-800">{report.diagnosisSolution}</p>
          </div>
        </section>

        <section className="rounded-xl border border-neutral-200 p-6">
          <h3 className="font-medium text-neutral-900">Need more detail?</h3>
          <p className="mt-1 text-sm text-neutral-500">Ask the AI assistant about this diagnosis.</p>
          <Link
            to={`/chat/${report.id}`}
            className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-neutral-900 underline underline-offset-4 transition-colors hover:text-neutral-600"
          >
            Open chat
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
            </svg>
          </Link>
        </section>
      </div>
    </div>
  );
}
