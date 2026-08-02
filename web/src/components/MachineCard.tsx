import { Link } from 'react-router-dom';
import { getFileUrl } from '../api/api';
import type { Machine } from '../api/api';

interface MachineCardProps {
  machine: Machine;
}

export default function MachineCard({ machine }: MachineCardProps) {
  const thumbUrl = machine.thumbnailPath
    ? getFileUrl('thumbnails', machine.thumbnailPath)
    : null;

  return (
    <Link
      to={`/machines/${machine.id}`}
      className="group animate-fade-in block overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition-all hover:shadow-lg hover:-translate-y-1"
    >
      <div className="aspect-video w-full overflow-hidden bg-gradient-to-br from-indigo-50 to-purple-50">
        {thumbUrl ? (
          <img
            src={thumbUrl}
            alt={machine.name}
            className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full items-center justify-center">
            <svg className="h-12 w-12 text-indigo-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
            </svg>
          </div>
        )}
      </div>
      <div className="p-5">
        <h3 className="text-lg font-semibold text-gray-900 group-hover:text-indigo-600 transition-colors">
          {machine.name}
        </h3>
        <p className="mt-1 text-sm text-gray-500">
          {machine.manufacturer} &middot; {machine.model}
        </p>
        <div className="mt-3 flex items-center gap-4 text-xs text-gray-400">
          {machine.referenceImages && (
            <span className="flex items-center gap-1">
              <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
              </svg>
              {machine.referenceImages.length} ref images
            </span>
          )}
          <span>{machine.model}</span>
        </div>
      </div>
    </Link>
  );
}
