import { useEffect, useState, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getMachines, diagnose } from '../api/api';
import type { Machine } from '../api/api';

export default function Diagnose() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const preselectedMachineId = searchParams.get('machineId');

  const [machines, setMachines] = useState<Machine[]>([]);
  const [machineId, setMachineId] = useState(preselectedMachineId || '');
  const [problemDesc, setProblemDesc] = useState('');
  const [image, setImage] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const dropRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    getMachines().then((res) => setMachines(res.data)).catch(() => {});
  }, []);

  useEffect(() => {
    if (preselectedMachineId) setMachineId(preselectedMachineId);
  }, [preselectedMachineId]);

  const handleImageSelect = (file: File) => {
    if (!file.type.startsWith('image/')) {
      setError('Please select a valid image file.');
      return;
    }
    setImage(file);
    setError(null);
    const reader = new FileReader();
    reader.onload = (e) => setImagePreview(e.target?.result as string);
    reader.readAsDataURL(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file) handleImageSelect(file);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!machineId || !problemDesc.trim() || !image) {
      setError('Please fill in all fields and select an image.');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await diagnose(machineId, problemDesc.trim(), image);
      navigate(`/diagnosis/${res.data.id}`, { state: { report: res.data } });
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Diagnosis failed. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="animate-fade-in mb-8 text-center">
        <h1 className="text-2xl font-bold text-gray-900">Machine Diagnosis</h1>
        <p className="mt-2 text-gray-500">
          Upload an image of the issue and describe the problem. Our AI will analyze it and provide a diagnosis.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="animate-fade-in space-y-6">
        {/* Machine selection */}
        <div>
          <label htmlFor="machine" className="mb-1.5 block text-sm font-medium text-gray-700">Machine</label>
          <select
            id="machine"
            value={machineId}
            onChange={(e) => setMachineId(e.target.value)}
            className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200 transition-all"
            required
          >
            <option value="">Select a machine...</option>
            {machines.map((m) => (
              <option key={m.id} value={m.id}>{m.name} ({m.manufacturer} {m.model})</option>
            ))}
          </select>
        </div>

        {/* Problem description */}
        <div>
          <label htmlFor="problem" className="mb-1.5 block text-sm font-medium text-gray-700">Problem Description</label>
          <textarea
            id="problem"
            rows={4}
            value={problemDesc}
            onChange={(e) => setProblemDesc(e.target.value)}
            placeholder="Describe the issue you're experiencing with the machine..."
            className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm shadow-sm placeholder-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200 transition-all resize-y"
            required
          />
        </div>

        {/* Image upload */}
        <div>
          <label className="mb-1.5 block text-sm font-medium text-gray-700">Upload Image</label>
          <div
            ref={dropRef}
            onDrop={handleDrop}
            onDragOver={(e) => e.preventDefault()}
            onClick={() => fileInputRef.current?.click()}
            className={`cursor-pointer rounded-xl border-2 border-dashed p-8 text-center transition-all hover:border-indigo-400 hover:bg-indigo-50/50 ${
              imagePreview ? 'border-indigo-400 bg-indigo-50/30' : 'border-gray-300 bg-gray-50'
            }`}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={(e) => e.target.files?.[0] && handleImageSelect(e.target.files[0])}
            />
            {imagePreview ? (
              <div className="space-y-3">
                <img src={imagePreview} alt="Preview" className="mx-auto max-h-64 rounded-lg object-contain shadow-sm" />
                <p className="text-sm text-gray-500">Click or drop to replace image</p>
              </div>
            ) : (
              <div className="space-y-3">
                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
                </svg>
                <p className="text-sm text-gray-500">
                  <span className="font-medium text-indigo-600">Click to upload</span> or drag and drop
                </p>
                <p className="text-xs text-gray-400">PNG, JPG, GIF up to 10MB</p>
              </div>
            )}
          </div>
        </div>

        {error && (
          <div className="animate-fade-in rounded-lg bg-red-50 p-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-indigo-200 transition-all hover:from-indigo-700 hover:to-purple-700 hover:shadow-xl disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {loading ? (
            <span className="inline-flex items-center gap-2">
              <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
              Analyzing with AI...
            </span>
          ) : (
            <span className="inline-flex items-center gap-2">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
              </svg>
              Start Diagnosis
            </span>
          )}
        </button>
      </form>
    </div>
  );
}
