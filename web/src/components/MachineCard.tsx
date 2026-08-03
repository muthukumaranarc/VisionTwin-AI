import { Link } from 'react-router-dom';
import { getFileUrl } from '../api/api';
import type { Machine } from '../api/api';

interface MachineCardProps {
  machine: Machine;
}

export default function MachineCard({ machine }: MachineCardProps) {
  const thumbUrl = machine.thumbnailPath ? getFileUrl(machine.thumbnailPath) : null;

  return (
    <Link
      to={`/machines/${machine.id}`}
      className="vt-card group animate-fade-in block overflow-hidden transition-all hover:-translate-y-0.5 hover:border-[#2563eb]/40 hover:shadow-[0_8px_24px_rgba(15,23,42,0.08)]"
    >
      <div className="aspect-video w-full overflow-hidden bg-[#e2e7ff]">
        {thumbUrl ? (
          <img
            src={thumbUrl}
            alt={machine.name}
            className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full items-center justify-center">
            <svg className="h-12 w-12 text-[#2563eb]/40" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
            </svg>
          </div>
        )}
      </div>
      <div className="p-5">
        <h3 className="text-lg font-semibold text-[#131b2e] transition-colors group-hover:text-[#004ac6]">
          {machine.name}
        </h3>
        <p className="mt-1 text-sm text-[#434655]">
          {machine.manufacturer} &middot; {machine.model}
        </p>
        <div className="mt-4 flex items-center gap-3 text-xs font-medium text-[#737686]">
          {machine.referenceImages && (
            <span className="inline-flex items-center gap-1 rounded-full bg-[#f2f3ff] px-2.5 py-1">
              <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
              </svg>
              {machine.referenceImages.length} ref images
            </span>
          )}
          <span className="inline-flex rounded-full border border-[#c3c6d7] px-2.5 py-1">{machine.model}</span>
        </div>
      </div>
    </Link>
  );
}
