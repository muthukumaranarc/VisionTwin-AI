import React, { useState } from 'react';
import { addReferenceImage } from '../api/api';

interface AddReferenceImageModalProps {
  machineId: string;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function AddReferenceImageModal({ machineId, isOpen, onClose, onSuccess }: AddReferenceImageModalProps) {
  const [partName, setPartName] = useState('');
  const [circleX, setCircleX] = useState<number>(0.5);
  const [circleY, setCircleY] = useState<number>(0.5);
  const [circleRadius, setCircleRadius] = useState<number>(0.1);
  const [image, setImage] = useState<File | null>(null);
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!image) {
      setError('Please select an image');
      return;
    }
    
    setLoading(true);
    setError(null);

    const formData = new FormData();
    formData.append('partName', partName);
    formData.append('circleX', circleX.toString());
    formData.append('circleY', circleY.toString());
    formData.append('circleRadius', circleRadius.toString());
    formData.append('image', image);

    try {
      await addReferenceImage(machineId, formData);
      onSuccess();
      onClose();
      // Reset form
      setPartName('');
      setCircleX(0.5);
      setCircleY(0.5);
      setCircleRadius(0.1);
      setImage(null);
    } catch (err: any) {
      setError(err.message || 'Failed to add reference image');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto overflow-x-hidden bg-gray-900/50 p-4 sm:p-0">
      <div className="relative w-full max-w-md rounded-2xl bg-white shadow-xl sm:my-8">
        <div className="flex items-center justify-between border-b border-gray-200 px-6 py-4">
          <h3 className="text-lg font-semibold text-gray-900">Add Reference Image</h3>
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
              <label htmlFor="image" className="mb-1 block text-sm font-medium text-gray-700">Image</label>
              <input
                type="file"
                id="image"
                required
                accept="image/*"
                onChange={(e) => setImage(e.target.files?.[0] || null)}
                className="w-full text-sm text-gray-500 file:mr-4 file:rounded-lg file:border-0 file:bg-indigo-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-indigo-700 hover:file:bg-indigo-100"
              />
            </div>
            <div>
              <label htmlFor="partName" className="mb-1 block text-sm font-medium text-gray-700">Part Name</label>
              <input
                type="text"
                id="partName"
                required
                value={partName}
                onChange={(e) => setPartName(e.target.value)}
                className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                placeholder="e.g. Spindle Motor"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="circleX" className="mb-1 block text-sm font-medium text-gray-700">Center X (0-1)</label>
                <input
                  type="number"
                  id="circleX"
                  required
                  min="0"
                  max="1"
                  step="0.01"
                  value={circleX}
                  onChange={(e) => setCircleX(parseFloat(e.target.value))}
                  className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                />
              </div>
              <div>
                <label htmlFor="circleY" className="mb-1 block text-sm font-medium text-gray-700">Center Y (0-1)</label>
                <input
                  type="number"
                  id="circleY"
                  required
                  min="0"
                  max="1"
                  step="0.01"
                  value={circleY}
                  onChange={(e) => setCircleY(parseFloat(e.target.value))}
                  className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                />
              </div>
            </div>
            <div>
              <label htmlFor="circleRadius" className="mb-1 block text-sm font-medium text-gray-700">Radius (0-1)</label>
              <input
                type="number"
                id="circleRadius"
                required
                min="0"
                max="1"
                step="0.01"
                value={circleRadius}
                onChange={(e) => setCircleRadius(parseFloat(e.target.value))}
                className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
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
              {loading ? 'Uploading...' : 'Upload Image'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
