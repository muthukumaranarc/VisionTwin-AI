import React, { useEffect, useRef, useState } from 'react';
import { addReferenceImage, updateReferenceImage, getFileUrl } from '../api/api';
import type { ReferenceImage } from '../api/api';

interface AddReferenceImageModalProps {
  machineId: string;
  isOpen: boolean;
  editImage?: ReferenceImage | null;
  onClose: () => void;
  onSuccess: () => void;
}

const MIN_RADIUS = 0.02;
const MAX_RADIUS = 0.5;

const clampRadius = (v: number) =>
  Math.round(Math.min(MAX_RADIUS, Math.max(MIN_RADIUS, v)) * 100) / 100;

export default function AddReferenceImageModal({ machineId, isOpen, editImage, onClose, onSuccess }: AddReferenceImageModalProps) {
  const [partName, setPartName] = useState('');
  const [circleX, setCircleX] = useState<number>(0.5);
  const [circleY, setCircleY] = useState<number>(0.5);
  const [circleRadius, setCircleRadius] = useState<number>(0.1);
  const [image, setImage] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const imgRef = useRef<HTMLImageElement>(null);

  useEffect(() => {
    if (isOpen) {
      setPartName(editImage?.partName ?? '');
      setCircleX(editImage?.circleX ?? 0.5);
      setCircleY(editImage?.circleY ?? 0.5);
      setCircleRadius(editImage?.circleRadius ?? 0.1);
      setImage(null);
      setPreviewUrl(null);
      setError(null);
    }
  }, [isOpen, editImage]);

  if (!isOpen) return null;

  const isEditing = !!editImage;
  const displayUrl = previewUrl ?? (editImage ? getFileUrl(editImage.filePath) : null);

  const handleImageSelect = (file: File) => {
    setImage(file);
    setError(null);
    const reader = new FileReader();
    reader.onload = (e) => setPreviewUrl(e.target?.result as string);
    reader.readAsDataURL(file);
  };

  const handleImageClick = (e: React.MouseEvent<HTMLImageElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const x = (e.clientX - rect.left) / rect.width;
    const y = (e.clientY - rect.top) / rect.height;
    setCircleX(Math.round(Math.min(1, Math.max(0, x)) * 100) / 100);
    setCircleY(Math.round(Math.min(1, Math.max(0, y)) * 100) / 100);
  };

  const handleResizePointerDown = (e: React.PointerEvent) => {
    e.preventDefault();
    e.stopPropagation();
    const img = imgRef.current;
    if (!img) return;
    const rect = img.getBoundingClientRect();
    const centerX = rect.left + circleX * rect.width;
    const centerY = rect.top + circleY * rect.height;

    const onMove = (ev: PointerEvent) => {
      const dx = ev.clientX - centerX;
      const dy = ev.clientY - centerY;
      setCircleRadius(clampRadius(Math.sqrt(dx * dx + dy * dy) / rect.width));
    };
    const onUp = () => {
      window.removeEventListener('pointermove', onMove);
      window.removeEventListener('pointerup', onUp);
    };
    window.addEventListener('pointermove', onMove);
    window.addEventListener('pointerup', onUp);
  };

  const handleWheel = (e: React.WheelEvent) => {
    e.preventDefault();
    const delta = e.deltaY < 0 ? 0.01 : -0.01;
    setCircleRadius((prev) => clampRadius(prev + delta));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isEditing && !image) {
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
    if (image) formData.append('image', image);

    try {
      if (isEditing && editImage) {
        await updateReferenceImage(editImage.id, formData);
      } else {
        await addReferenceImage(machineId, formData);
      }
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.message || `Failed to ${isEditing ? 'update' : 'add'} reference image`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto overflow-x-hidden bg-[#131b2e]/45 p-4 backdrop-blur-sm sm:p-0">
      <div className="vt-panel relative w-full max-w-md sm:my-8">
        <div className="flex items-center justify-between border-b border-[#c3c6d7]/80 px-6 py-4">
          <h3 className="text-lg font-semibold text-[#131b2e]">
            {isEditing ? 'Edit Reference Image' : 'Add Reference Image'}
          </h3>
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
              <label htmlFor="image" className="mb-1 block text-sm font-semibold text-[#434655]">
                Image {isEditing && <span className="font-normal text-[#737686]">(optional to keep current)</span>}
              </label>
              <input
                type="file"
                id="image"
                required={!isEditing}
                accept="image/*"
                onChange={(e) => e.target.files?.[0] && handleImageSelect(e.target.files[0])}
                className="w-full text-sm text-[#434655] file:mr-4 file:rounded-lg file:border-0 file:bg-[#eaddff] file:px-4 file:py-2 file:text-sm file:font-semibold file:text-[#5a00c6] hover:file:bg-[#d2bbff]"
              />
            </div>

            {displayUrl && (
              <div>
                <div
                  className="relative inline-block max-w-full"
                  onWheel={handleWheel}
                >
                  <img
                    ref={imgRef}
                    src={displayUrl}
                    alt="Reference"
                    onClick={handleImageClick}
                    className="max-h-72 w-auto cursor-crosshair rounded-lg border border-[#c3c6d7]"
                  />
                  <div className="absolute inset-0 pointer-events-none">
                    <div
                      className="absolute rounded-full border-2 border-indigo-500/80"
                      style={{
                        left: `${(circleX - circleRadius) * 100}%`,
                        top: `${(circleY - circleRadius) * 100}%`,
                        width: `${circleRadius * 200}%`,
                        height: `${circleRadius * 200}%`,
                      }}
                    />
                  </div>
                  <div
                    role="slider"
                    aria-label="Resize indicator"
                    title="Drag to resize indicator"
                    onPointerDown={handleResizePointerDown}
                    className="absolute h-4 w-4 -translate-x-1/2 -translate-y-1/2 cursor-nwse-resize rounded-full border-2 border-white bg-indigo-600 shadow"
                    style={{ left: `${(circleX + circleRadius) * 100}%`, top: `${circleY * 100}%` }}
                  />
                </div>
                <p className="mt-1 text-xs text-[#737686]">
                  Click to set center. Drag the handle or scroll to resize the indicator.
                </p>
              </div>
            )}

            <div>
              <label htmlFor="partName" className="mb-1 block text-sm font-semibold text-[#434655]">Part Name</label>
              <input
                type="text"
                id="partName"
                required
                value={partName}
                onChange={(e) => setPartName(e.target.value)}
                className="vt-input"
                placeholder="e.g. Spindle Motor"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="circleX" className="mb-1 block text-sm font-semibold text-[#434655]">Center X (0-1)</label>
                <input
                  type="number"
                  id="circleX"
                  required
                  min="0"
                  max="1"
                  step="0.01"
                  value={circleX}
                  onChange={(e) => setCircleX(parseFloat(e.target.value))}
                  className="vt-input"
                />
              </div>
              <div>
                <label htmlFor="circleY" className="mb-1 block text-sm font-semibold text-[#434655]">Center Y (0-1)</label>
                <input
                  type="number"
                  id="circleY"
                  required
                  min="0"
                  max="1"
                  step="0.01"
                  value={circleY}
                  onChange={(e) => setCircleY(parseFloat(e.target.value))}
                  className="vt-input"
                />
              </div>
            </div>
            <div>
              <div className="mb-1 flex items-center justify-between">
                <label htmlFor="circleRadius" className="text-sm font-semibold text-[#434655]">Indicator Size</label>
                <span className="text-sm font-medium text-[#434655]">{circleRadius.toFixed(2)}</span>
              </div>
              <input
                type="range"
                id="circleRadius"
                required
                min={MIN_RADIUS}
                max={MAX_RADIUS}
                step="0.01"
                value={circleRadius}
                onChange={(e) => setCircleRadius(clampRadius(parseFloat(e.target.value)))}
                className="w-full accent-indigo-600"
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
              className="vt-button-ai"
            >
              {loading ? 'Saving...' : isEditing ? 'Save Changes' : 'Upload Image'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
