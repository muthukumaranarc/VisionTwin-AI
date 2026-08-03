import React, { useState } from 'react';
import { createMachine } from '../api/api';

interface AddMachineModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function AddMachineModal({ isOpen, onClose, onSuccess }: AddMachineModalProps) {
  const [name, setName] = useState('');
  const [manufacturer, setManufacturer] = useState('');
  const [model, setModel] = useState('');
  const [thumbnail, setThumbnail] = useState<File | null>(null);
  const [manual, setManual] = useState<File | null>(null);
  const [userGuide, setUserGuide] = useState<File | null>(null);
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    const formData = new FormData();
    formData.append('name', name);
    formData.append('manufacturer', manufacturer);
    formData.append('model', model);
    if (thumbnail) formData.append('thumbnail', thumbnail);
    if (manual) formData.append('manual', manual);
    if (userGuide) formData.append('userGuide', userGuide);

    try {
      await createMachine(formData);
      onSuccess();
      onClose();
      // Reset form
      setName('');
      setManufacturer('');
      setModel('');
      setThumbnail(null);
      setManual(null);
      setUserGuide(null);
    } catch (err: any) {
      setError(err.message || 'Failed to create machine');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto overflow-x-hidden bg-[#131b2e]/45 p-4 backdrop-blur-sm sm:p-0">
      <div className="vt-panel relative w-full max-w-md sm:my-8">
        <div className="flex items-center justify-between border-b border-[#c3c6d7]/80 px-6 py-4">
          <h3 className="text-lg font-semibold text-[#131b2e]">Add New Machine</h3>
          <button
            onClick={onClose}
            className="rounded-lg p-1 text-[#737686] transition-colors hover:bg-[#f2f3ff] hover:text-[#131b2e]"
          >
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <form onSubmit={handleSubmit} className="px-6 py-4">
          <div className="space-y-4">
            <div>
              <label htmlFor="name" className="mb-1 block text-sm font-semibold text-[#434655]">Name</label>
              <input
                type="text"
                id="name"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="vt-input"
                placeholder="e.g. CNC Milling Machine"
              />
            </div>
            <div>
              <label htmlFor="manufacturer" className="mb-1 block text-sm font-semibold text-[#434655]">Manufacturer</label>
              <input
                type="text"
                id="manufacturer"
                required
                value={manufacturer}
                onChange={(e) => setManufacturer(e.target.value)}
                className="vt-input"
                placeholder="e.g. Haas"
              />
            </div>
            <div>
              <label htmlFor="model" className="mb-1 block text-sm font-semibold text-[#434655]">Model</label>
              <input
                type="text"
                id="model"
                required
                value={model}
                onChange={(e) => setModel(e.target.value)}
                className="vt-input"
                placeholder="e.g. VF-2"
              />
            </div>
            <div>
              <label htmlFor="thumbnail" className="mb-1 block text-sm font-semibold text-[#434655]">Thumbnail Image (Optional)</label>
              <input
                type="file"
                id="thumbnail"
                accept="image/*"
                onChange={(e) => setThumbnail(e.target.files?.[0] || null)}
                className="w-full text-sm text-[#434655] file:mr-4 file:rounded-lg file:border-0 file:bg-[#e2e7ff] file:px-4 file:py-2 file:text-sm file:font-semibold file:text-[#004ac6] hover:file:bg-[#dae2fd]"
              />
            </div>
            <div>
              <label htmlFor="manual" className="mb-1 block text-sm font-semibold text-[#434655]">Manual (Optional)</label>
              <input
                type="file"
                id="manual"
                accept=".pdf,.md,.markdown,.txt"
                onChange={(e) => setManual(e.target.files?.[0] || null)}
                className="w-full text-sm text-[#434655] file:mr-4 file:rounded-lg file:border-0 file:bg-[#e2e7ff] file:px-4 file:py-2 file:text-sm file:font-semibold file:text-[#004ac6] hover:file:bg-[#dae2fd]"
              />
            </div>
            <div>
              <label htmlFor="userGuide" className="mb-1 block text-sm font-semibold text-[#434655]">User Guide (Optional)</label>
              <input
                type="file"
                id="userGuide"
                accept=".pdf,.md,.markdown,.txt"
                onChange={(e) => setUserGuide(e.target.files?.[0] || null)}
                className="w-full text-sm text-[#434655] file:mr-4 file:rounded-lg file:border-0 file:bg-[#e2e7ff] file:px-4 file:py-2 file:text-sm file:font-semibold file:text-[#004ac6] hover:file:bg-[#dae2fd]"
              />
            </div>
          </div>

          {error && <div className="mt-4 rounded-lg bg-[#ffdad6] p-3 text-sm font-medium text-[#93000a]">{error}</div>}

          <div className="mt-6 flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="vt-button-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="vt-button-primary"
            >
              {loading ? 'Creating...' : 'Create Machine'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
