import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getMachineById, getReferenceImages, getFileUrl, generateKnowledgeBase, deleteReferenceImage } from '../api/api';
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
  const [editingRefImage, setEditingRefImage] = useState<ReferenceImage | null>(null);

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

  const handleDeleteRefImage = async (refImage: ReferenceImage) => {
    if (!window.confirm(`Delete reference image "${refImage.partName}"?`)) return;
    try {
      await deleteReferenceImage(refImage.id);
      setRefImages((prev) => prev.filter((img) => img.id !== refImage.id));
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to delete reference image';
      window.alert(msg);
    }
  };

  if (loading) return <LoadingSpinner size="lg" text="Loading machine details..." />;
  if (error) return <div className="vt-page max-w-4xl"><ErrorMessage message={error} onRetry={fetchData} /></div>;
  if (!machine) return null;

  const thumbUrl = machine.thumbnailPath ? getFileUrl(machine.thumbnailPath) : null;
  const manualUrl = machine.manualPdfPath ? getFileUrl(machine.manualPdfPath) : null;
  const guideUrl = machine.userGuidePdfPath ? getFileUrl(machine.userGuidePdfPath) : null;

  return (
    <div className="vt-page max-w-6xl">
      {/* Back link */}
      <Link to="/machines" className="mb-6 inline-flex items-center gap-2 text-sm font-semibold text-[#434655] transition-colors hover:text-[#131b2e]">
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
        </svg>
        Back to Machines
      </Link>

      {/* Machine header */}
      <div className="vt-panel animate-fade-in overflow-hidden">
        <div className="aspect-[3/1] w-full overflow-hidden bg-[#e2e7ff]">
          {thumbUrl ? (
            <img src={thumbUrl} alt={machine.name} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full items-center justify-center">
              <svg className="h-20 w-20 text-[#2563eb]/40" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
              </svg>
            </div>
          )}
        </div>

        <div className="p-6 sm:p-8">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <p className="vt-label">Machine Twin</p>
              <h1 className="vt-title mt-1">{machine.name}</h1>
              <p className="vt-subtitle">
                {machine.manufacturer} &middot; {machine.model}
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Link
                to={`/diagnose?machineId=${machine.id}`}
                className="vt-button-ai"
              >
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 3.75v11.25m0 0l-3-3m3 3l3-3m0 0V3.75M3 12c0 4.243 3.757 8.25 8.25 8.25S19.5 16.243 19.5 12" />
                </svg>
                Diagnose
              </Link>
              <Link
                to={`/learn?machineId=${machine.id}`}
                className="vt-button-primary"
              >
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
                </svg>
                Learn
              </Link>
              <button
                onClick={handleGenerateKB}
                disabled={kbLoading}
                className="vt-button-secondary"
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
            <div className={`mt-4 rounded-lg p-3 text-sm font-medium ${kbMessage.includes('success') ? 'bg-[#acedff]/35 text-[#005e6e]' : 'bg-[#ffdad6] text-[#93000a]'}`}>
              {kbMessage}
            </div>
          )}

          {/* Info grid */}
          <div className="mt-8 grid gap-4 sm:grid-cols-2">
            <div className="rounded-lg border border-[#c3c6d7]/80 bg-[#f2f3ff] p-4">
              <p className="vt-label">Created</p>
              <p className="mt-1 font-semibold text-[#131b2e]">{new Date(machine.createdAt).toLocaleDateString()}</p>
            </div>
            <div className="rounded-lg border border-[#c3c6d7]/80 bg-[#f2f3ff] p-4">
              <p className="vt-label">Last Updated</p>
              <p className="mt-1 font-semibold text-[#131b2e]">{new Date(machine.updatedAt).toLocaleDateString()}</p>
            </div>
          </div>

          {/* Documents */}
          <div className="mt-8">
            <h2 className="mb-3 text-lg font-semibold text-[#131b2e]">Documents</h2>
            <div className="flex flex-wrap gap-3">
              {manualUrl ? (
                <a href={manualUrl} target="_blank" rel="noopener noreferrer"
                  className="vt-button-secondary">
                  <svg className="h-4 w-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                  </svg>
                  Manual
                </a>
              ) : (
                <span className="inline-flex min-h-11 items-center gap-2 rounded-lg border border-dashed border-[#c3c6d7] bg-[#f2f3ff] px-4 py-2.5 text-sm text-[#737686]">
                  No manual uploaded
                </span>
              )}
              {guideUrl ? (
                <a href={guideUrl} target="_blank" rel="noopener noreferrer"
                  className="vt-button-secondary">
                  <svg className="h-4 w-4 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
                  </svg>
                  User Guide
                </a>
              ) : (
                <span className="inline-flex min-h-11 items-center gap-2 rounded-lg border border-dashed border-[#c3c6d7] bg-[#f2f3ff] px-4 py-2.5 text-sm text-[#737686]">
                  No user guide uploaded
                </span>
              )}
            </div>
          </div>

          {/* Reference images */}
          <div className="mt-8">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-[#131b2e]">
                Reference Images ({refImages.length})
              </h2>
              <button
                onClick={() => { setEditingRefImage(null); setIsAddRefImageModalOpen(true); }}
                className="vt-button-secondary min-h-9 px-3 py-1.5"
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
                  const imgUrl = getFileUrl(img.filePath);
                  return (
                    <div key={img.id} className="vt-card group relative overflow-hidden">
                      <div className="aspect-square overflow-hidden">
                        <img src={imgUrl} alt={img.partName} className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105" />
                      </div>
                      <div className="p-3">
                        <p className="text-sm font-semibold text-[#131b2e]">{img.partName}</p>
                        <p className="text-xs text-[#737686]">{img.filename}</p>
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
                      <div className="absolute right-2 top-2 flex gap-1.5">
                        <button
                          onClick={() => { setEditingRefImage(img); setIsAddRefImageModalOpen(true); }}
                          title="Edit"
                          className="flex h-8 w-8 items-center justify-center rounded-lg bg-white/90 text-[#434655] shadow-sm transition-colors hover:bg-white hover:text-[#004ac6]"
                        >
                          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
                          </svg>
                        </button>
                        <button
                          onClick={() => handleDeleteRefImage(img)}
                          title="Delete"
                          className="flex h-8 w-8 items-center justify-center rounded-lg bg-white/90 text-[#ba1a1a] shadow-sm transition-colors hover:bg-white hover:text-[#93000a]"
                        >
                          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
                          </svg>
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="vt-empty py-8 text-sm text-[#737686]">No reference images available.</div>
            )}
          </div>
        </div>
      </div>

      <AddReferenceImageModal
        machineId={machine.id}
        isOpen={isAddRefImageModalOpen}
        editImage={editingRefImage}
        onClose={() => { setIsAddRefImageModalOpen(false); setEditingRefImage(null); }}
        onSuccess={fetchData}
      />
    </div>
  );
}
