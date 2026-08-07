import { useEffect, useState, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getMachines, diagnose, getDiagnosisModels } from '../api/api';
import type { Machine } from '../api/api';

const FALLBACK_MODELS = ['gemini-3.6-flash', 'gemini-2.5-pro', 'gemini-2.5-flash', 'gemini-2.0-flash'];
const MODEL_STORAGE_KEY = 'visiontwin-diagnose-model';

export default function Diagnose() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const preselectedMachineId = searchParams.get('machineId');

  const [machines, setMachines] = useState<Machine[]>([]);
  const [machineId, setMachineId] = useState(preselectedMachineId || '');
  const [problemDesc, setProblemDesc] = useState('');
  const [image, setImage] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [models, setModels] = useState<string[]>(FALLBACK_MODELS);
  const [selectedModel, setSelectedModel] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    getMachines().then((res) => setMachines(res.data)).catch(() => {});
    getDiagnosisModels()
      .then((res) => {
        const list = res.data.models.length ? res.data.models : FALLBACK_MODELS;
        setModels(list);
        const defaultPro = list.find(m => m.toLowerCase().includes('pro')) || list[0];
        setSelectedModel((prev) => prev || localStorage.getItem(MODEL_STORAGE_KEY) || defaultPro);
      })
      .catch(() => {
        const defaultPro = FALLBACK_MODELS.find(m => m.toLowerCase().includes('pro')) || FALLBACK_MODELS[0];
        setSelectedModel((prev) => prev || localStorage.getItem(MODEL_STORAGE_KEY) || defaultPro);
      });
  }, []);

  useEffect(() => {
    if (preselectedMachineId) setMachineId(preselectedMachineId);
  }, [preselectedMachineId]);

  const handleModelChange = (value: string) => {
    setSelectedModel(value);
    localStorage.setItem(MODEL_STORAGE_KEY, value);
  };

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
      const res = await diagnose(machineId, problemDesc.trim(), image, selectedModel);
      navigate(`/diagnosis/${res.data.id}`, { state: { report: res.data } });
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Diagnosis failed. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto w-full max-w-2xl px-4 py-12 sm:px-6">
      <header className="mb-10">
        <h1 className="text-2xl font-semibold tracking-tight text-neutral-900">Diagnose a machine</h1>
        <p className="mt-1.5 text-sm leading-6 text-neutral-500">
          Describe the issue and attach a photo. The AI will identify the problem and suggest a fix.
        </p>
      </header>

      <form onSubmit={handleSubmit} className="space-y-8">
        <div>
          <label htmlFor="machine" className="mb-1.5 block text-sm font-medium text-neutral-700">
            Machine
          </label>
          <select
            id="machine"
            value={machineId}
            onChange={(e) => setMachineId(e.target.value)}
            className="w-full rounded-lg border border-neutral-300 bg-white px-3 py-2.5 text-sm text-neutral-900 shadow-none outline-none transition focus:border-neutral-900 focus:ring-1 focus:ring-neutral-900"
            required
          >
            <option value="">Select a machine...</option>
            {machines.map((m) => (
              <option key={m.id} value={m.id}>{m.name} ({m.manufacturer} {m.model})</option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="aiModel" className="mb-1.5 block text-sm font-medium text-neutral-700">
            AI model
          </label>
          <select
            id="aiModel"
            value={selectedModel}
            onChange={(e) => handleModelChange(e.target.value)}
            className="w-full rounded-lg border border-neutral-300 bg-white px-3 py-2.5 text-sm text-neutral-900 shadow-none outline-none transition focus:border-neutral-900 focus:ring-1 focus:ring-neutral-900"
            required
          >
            {models.length === 0 && <option value="">Loading...</option>}
            {models.map((m) => (
              <option key={m} value={m}>{m}</option>
            ))}
          </select>
          <p className="mt-1.5 text-xs text-neutral-500">
            Gemini model used for vision analysis and reasoning. Uses the backend's configured Gemini key.
          </p>
        </div>

        <div>
          <label htmlFor="problem" className="mb-1.5 block text-sm font-medium text-neutral-700">
            Problem
          </label>
          <textarea
            id="problem"
            rows={4}
            value={problemDesc}
            onChange={(e) => setProblemDesc(e.target.value)}
            placeholder="What's wrong with the machine?"
            className="w-full resize-y rounded-lg border border-neutral-300 bg-white px-3 py-2.5 text-sm text-neutral-900 shadow-none outline-none transition placeholder:text-neutral-400 focus:border-neutral-900 focus:ring-1 focus:ring-neutral-900"
            required
          />
        </div>

        <div>
          <label className="mb-1.5 block text-sm font-medium text-neutral-700">Photo</label>
          <div
            onDrop={handleDrop}
            onDragOver={(e) => e.preventDefault()}
            onClick={() => fileInputRef.current?.click()}
            className={`cursor-pointer rounded-lg border border-dashed p-8 text-center transition-colors ${
              imagePreview ? 'border-neutral-400' : 'border-neutral-300 hover:border-neutral-400'
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
                <img src={imagePreview} alt="Preview" className="mx-auto max-h-72 rounded-lg object-contain" />
                <p className="text-xs text-neutral-400">Click to replace</p>
              </div>
            ) : (
              <p className="text-sm text-neutral-500">
                <span className="font-medium text-neutral-900 underline underline-offset-4">Click to upload</span> or drag
                an image here
              </p>
            )}
          </div>
        </div>

        {error && <p className="text-sm font-medium text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg bg-neutral-900 px-4 py-3 text-sm font-medium text-white transition hover:bg-neutral-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {loading ? 'Analyzing...' : 'Start diagnosis'}
        </button>
      </form>
    </div>
  );
}
