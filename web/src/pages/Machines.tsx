import { useEffect, useState } from 'react';
import { getMachines } from '../api/api';
import type { Machine } from '../api/api';
import MachineCard from '../components/MachineCard';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import AddMachineModal from '../components/AddMachineModal';

export default function Machines() {
  const [machines, setMachines] = useState<Machine[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  const fetchMachines = () => {
    setLoading(true);
    setError(null);
    getMachines()
      .then((res) => setMachines(res.data))
      .catch((err) => setError(err.message || 'Failed to load machines'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchMachines(); }, []);

  const filtered = machines.filter(
    (m) =>
      m.name.toLowerCase().includes(search.toLowerCase()) ||
      m.manufacturer.toLowerCase().includes(search.toLowerCase()) ||
      m.model.toLowerCase().includes(search.toLowerCase()),
  );

  return (
    <div className="vt-page">
      <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="vt-label">Asset Registry</p>
          <h1 className="vt-title mt-1">Machines</h1>
          <p className="vt-subtitle">{machines.length} machine{machines.length !== 1 ? 's' : ''} registered</p>
        </div>
        <div className="relative w-full sm:w-72">
          <svg className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#737686]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
          </svg>
          <input
            type="text"
            placeholder="Search machines..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="vt-input pl-10"
          />
        </div>
        <button
          onClick={() => setIsAddModalOpen(true)}
          className="vt-button-primary"
        >
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
          </svg>
          Add Machine
        </button>
      </div>

      {loading && <LoadingSpinner size="lg" text="Loading machines..." />}
      {error && <ErrorMessage message={error} onRetry={fetchMachines} />}

      {!loading && !error && filtered.length > 0 && (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((machine) => (
            <MachineCard key={machine.id} machine={machine} />
          ))}
        </div>
      )}

      {!loading && !error && filtered.length === 0 && (
        <div className="vt-empty">
          <svg className="mx-auto mb-4 h-12 w-12 text-[#737686]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
          </svg>
          <h3 className="mb-2 text-lg font-semibold text-[#131b2e]">{search ? 'No matching machines' : 'No machines yet'}</h3>
          <p className="text-sm text-[#434655]">{search ? 'Try a different search term.' : 'Machines will appear here once added.'}</p>
        </div>
      )}

      <AddMachineModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSuccess={fetchMachines}
      />
    </div>
  );
}
