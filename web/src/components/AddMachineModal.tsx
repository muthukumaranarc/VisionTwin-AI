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
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto overflow-x-hidden bg-gray-900/50 p-4 sm:p-0">
      <div className="relative w-full max-w-md rounded-2xl bg-white shadow-xl sm:my-8">
        <div className="flex items-center justify-between border-b border-gray-200 px-6 py-4">
          <h3 className="text-lg font-semibold text-gray-900">Add New Machine</h3>
          <button
            onClick={onClose}
            className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-500 transition-colors"
          >
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <form onSubmit={handleSubmit} className="px-6 py-4">
          <div className="space-y-4">
            <div>
              <label htmlFor="name" className="mb-1 block text-sm font-medium text-gray-700">Name</label>
              <input
                type="text"
                id="name"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                placeholder="e.g. CNC Milling Machine"
              />
            </div>
            <div>
              <label htmlFor="manufacturer" className="mb-1 block text-sm font-medium text-gray-700">Manufacturer</label>
              <input
                type="text"
                id="manufacturer"
                required
                value={manufacturer}
                onChange={(e) => setManufacturer(e.target.value)}
                className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                placeholder="e.g. Haas"
              />
            </div>
            <div>
              <label htmlFor="model" className="mb-1 block text-sm font-medium text-gray-700">Model</label>
              <input
                type="text"
                id="model"
                required
                value={model}
                onChange={(e) => setModel(e.target.value)}
                className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                placeholder="e.g. VF-2"
              />
            </div>
            <div>
              <label htmlFor="thumbnail" className="mb-1 block text-sm font-medium text-gray-700">Thumbnail Image (Optional)</label>
              <input
                type="file"
                id="thumbnail"
                accept="image/*"
                onChange={(e) => setThumbnail(e.target.files?.[0] || null)}
                className="w-full text-sm text-gray-500 file:mr-4 file:rounded-lg file:border-0 file:bg-indigo-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-indigo-700 hover:file:bg-indigo-100"
              />
            </div>
            <div>
              <label htmlFor="manual" className="mb-1 block text-sm font-medium text-gray-700">Manual PDF (Optional)</label>
              <input
                type="file"
                id="manual"
                accept="application/pdf"
                onChange={(e) => setManual(e.target.files?.[0] || null)}
                className="w-full text-sm text-gray-500 file:mr-4 file:rounded-lg file:border-0 file:bg-indigo-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-indigo-700 hover:file:bg-indigo-100"
              />
            </div>
            <div>
              <label htmlFor="userGuide" className="mb-1 block text-sm font-medium text-gray-700">User Guide PDF (Optional)</label>
              <input
                type="file"
                id="userGuide"
                accept="application/pdf"
                onChange={(e) => setUserGuide(e.target.files?.[0] || null)}
                className="w-full text-sm text-gray-500 file:mr-4 file:rounded-lg file:border-0 file:bg-indigo-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-indigo-700 hover:file:bg-indigo-100"
              />
            </div>
          </div>

          {error && <div className="mt-4 text-sm text-red-600">{error}</div>}

          <div className="mt-6 flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm transition hover:bg-gray-50 disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-indigo-700 disabled:opacity-50"
            >
              {loading ? 'Creating...' : 'Create Machine'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
