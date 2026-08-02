import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getMachineById, getReferenceImages, getFileUrl, generateKnowledgeBase } from '../api/api';
import type { Machine, ReferenceImage } from '../api/api';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import AddReferenceImageModal from '../components/AddReferenceImageModal';

export default function MachineDetail() {
  const { id } = useParams<{ id: string }>();
  const [machine, setMachine] = useState<Machine | null>(null);
  const [refImages, setRefImages] = useState<ReferenceImage[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [kbLoading, setKbLoading] = useState(false);
  const [kbMessage, setKbMessage] = useState<string | null>(null);
  const [isAddRefImageModalOpen, setIsAddRefImageModalOpen] = useState(false);

  const fetchData = () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    Promise.all([
      getMachineById(id),
      getReferenceImages(id),
    ])
      .then(([machineRes, refRes]) => {
        setMachine(machineRes.data);
        setRefImages(refRes.data);
      })
      .catch((err) => setError(err.message || 'Failed to load machine'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, [id]);

  const handleGenerateKB = async () => {
    if (!id) return;
    setKbLoading(true);
    setKbMessage(null);
    try {
      const res = await generateKnowledgeBase(id);
      setKbMessage(res.data.message);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to generate knowledge base';
      setKbMessage(msg);
    } finally {
      setKbLoading(false);
    }
  };

  if (loading) return <LoadingSpinner size="lg" text="Loading machine details..." />;
  if (error) return <div className="mx-auto max-w-4xl px-4 py-8"><ErrorMessage message={error} onRetry={fetchData} /></div>;
  if (!machine) return null;

  const thumbUrl = machine.thumbnailPath ? getFileUrl('thumbnails', machine.thumbnailPath) : null;
  const manualUrl = machine.manualPdfPath ? getFileUrl('manuals', machine.manualPdfPath) : null;
  const guideUrl = machine.userGuidePdfPath ? getFileUrl('userguides', machine.userGuidePdfPath) : null;

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Back link */}
      <Link to="/machines" className="mb-6 inline-flex items-center gap-2 text-sm font-medium text-gray-500 hover:text-gray-900 transition-colors">
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
        </svg>
        Back to Machines
      </Link>

      {/* Machine header */}
      <div className="animate-fade-in overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
        <div className="aspect-[3/1] w-full overflow-hidden bg-gradient-to-br from-indigo-100 to-purple-100">
          {thumbUrl ? (
            <img src={thumbUrl} alt={machine.name} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full items-center justify-center">
              <svg className="h-20 w-20 text-indigo-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
              </svg>
            </div>
          )}
        </div>

        <div className="p-6 sm:p-8">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">{machine.name}</h1>
              <p className="mt-1 text-gray-500">
                {machine.manufacturer} &middot; {machine.model}
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Link
                to={`/diagnose?machineId=${machine.id}`}
                className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-indigo-700"
              >
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 3.75v11.25m0 0l-3-3m3 3l3-3m0 0V3.75M3 12c0 4.243 3.757 8.25 8.25 8.25S19.5 16.243 19.5 12" />
                </svg>
                Diagnose
              </Link>
              <button
                onClick={handleGenerateKB}
                disabled={kbLoading}
                className="inline-flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm transition hover:bg-gray-50 disabled:opacity-50"
              >
                {kbLoading ? (
                  <div className="h-4 w-4 animate-spin rounded-full border-2 border-gray-300 border-t-indigo-600" />
                ) : (
                  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                  </svg>
                )}
                Generate Knowledge Base
              </button>
            </div>
          </div>

          {kbMessage && (
            <div className={`mt-4 rounded-lg p-3 text-sm ${kbMessage.includes('success') ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-600'}`}>
              {kbMessage}
            </div>
          )}

          {/* Info grid */}
          <div className="mt-8 grid gap-4 sm:grid-cols-2">
            <div className="rounded-xl border border-gray-100 bg-gray-50 p-4">
              <p className="text-xs font-medium uppercase tracking-wider text-gray-500">Created</p>
              <p className="mt-1 font-medium text-gray-900">{new Date(machine.createdAt).toLocaleDateString()}</p>
            </div>
            <div className="rounded-xl border border-gray-100 bg-gray-50 p-4">
              <p className="text-xs font-medium uppercase tracking-wider text-gray-500">Last Updated</p>
              <p className="mt-1 font-medium text-gray-900">{new Date(machine.updatedAt).toLocaleDateString()}</p>
            </div>
          </div>

          {/* Documents */}
          <div className="mt-8">
            <h2 className="mb-3 text-lg font-semibold text-gray-900">Documents</h2>
            <div className="flex flex-wrap gap-3">
              {manualUrl ? (
                <a href={manualUrl} target="_blank" rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-4 py-2.5 text-sm font-medium text-gray-700 shadow-sm transition hover:bg-gray-50 hover:border-gray-300">
                  <svg className="h-4 w-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                  </svg>
                  Manual
                </a>
              ) : (
                <span className="inline-flex items-center gap-2 rounded-lg border border-dashed border-gray-300 bg-gray-50 px-4 py-2.5 text-sm text-gray-400">
                  No manual uploaded
                </span>
              )}
              {guideUrl ? (
                <a href={guideUrl} target="_blank" rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-4 py-2.5 text-sm font-medium text-gray-700 shadow-sm transition hover:bg-gray-50 hover:border-gray-300">
                  <svg className="h-4 w-4 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
                  </svg>
                  User Guide
                </a>
              ) : (
                <span className="inline-flex items-center gap-2 rounded-lg border border-dashed border-gray-300 bg-gray-50 px-4 py-2.5 text-sm text-gray-400">
                  No user guide uploaded
                </span>
              )}
            </div>
          </div>

          {/* Reference images */}
          <div className="mt-8">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-900">
                Reference Images ({refImages.length})
              </h2>
              <button
                onClick={() => setIsAddRefImageModalOpen(true)}
                className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 shadow-sm transition hover:bg-gray-50"
              >
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                </svg>
                Add Image
              </button>
            </div>
            {refImages.length > 0 ? (
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {refImages.map((img) => {
                  const imgUrl = getFileUrl('refimages', img.filename);
                  return (
                    <div key={img.id} className="group relative overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
                      <div className="aspect-square overflow-hidden">
                        <img src={imgUrl} alt={img.partName} className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105" />
                      </div>
                      <div className="p-3">
                        <p className="text-sm font-medium text-gray-900">{img.partName}</p>
                        <p className="text-xs text-gray-500">{img.filename}</p>
                      </div>
                      {img.circleX != null && img.circleY != null && img.circleRadius != null && (
                        <div className="absolute inset-0 pointer-events-none">
                          <div
                            className="absolute border-2 border-indigo-500/60 rounded-full"
                            style={{
                              left: `${(img.circleX - img.circleRadius) * 100}%`,
                              top: `${(img.circleY - img.circleRadius) * 100}%`,
                              width: `${img.circleRadius * 200}%`,
                              height: `${img.circleRadius * 200}%`,
                            }}
                          />
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            ) : (
              <p className="text-sm text-gray-400">No reference images available.</p>
            )}
          </div>
        </div>
      </div>

      <AddReferenceImageModal
        machineId={machine.id}
        isOpen={isAddRefImageModalOpen}
        onClose={() => setIsAddRefImageModalOpen(false)}
        onSuccess={fetchData}
      />
    </div>
  );
}
