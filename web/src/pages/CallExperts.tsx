import { useState, useEffect } from 'react';
import { getMachines } from '../api/api';
import type { Machine } from '../api/api';

interface Expert {
  id: string;
  name: string;
  role: string;
  avatar: string;
  status: 'available' | 'busy';
  nextFreeTime?: string;
  specialties: string[];
}

export default function CallExperts() {
  const [machines, setMachines] = useState<Machine[]>([]);
  const [selectedExpert, setSelectedExpert] = useState<Expert | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [targetMachineId, setTargetMachineId] = useState('');
  const [urgency, setUrgency] = useState('Medium');
  const [appointmentTime, setAppointmentTime] = useState('');
  const [description, setDescription] = useState('');
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const [experts] = useState<Expert[]>([
    {
      id: '1',
      name: 'Nil Yeager',
      role: 'Chief Vibration Analyst',
      avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
      status: 'available',
      specialties: ['CNC Spindles', 'High-speed Gearboxes', 'Laser Alignments'],
    },
    {
      id: '2',
      name: 'Theron Trump',
      role: 'Lead Predictive Maintenance Engineer',
      avatar: 'https://images.unsplash.com/photo-1519345182560-3f2917c472ef?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
      status: 'available',
      specialties: ['Pneumatics', 'Acoustic Anomaly Detection'],
    },
    {
      id: '3',
      name: 'Tyler Mark',
      role: 'Senior Automation Specialist',
      avatar: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
      status: 'busy',
      nextFreeTime: 'Free in 45 mins (at 11:30 AM)',
      specialties: ['PLC Programming', 'Robotic Arm Calibrations'],
    },
    {
      id: '4',
      name: 'Johen Mark',
      role: 'Hydraulic Systems Supervisor',
      avatar: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
      status: 'busy',
      nextFreeTime: 'Free at 2:00 PM today',
      specialties: ['High-Pressure Pumps', 'Fluid Dynamics', 'LOTO Protocol'],
    },
  ]);

  useEffect(() => {
    getMachines()
      .then((res) => {
        setMachines(res.data);
        if (res.data.length > 0) {
          setTargetMachineId(res.data[0].id);
        }
      })
      .catch(() => {});
  }, []);

  const handleOpenAction = (expert: Expert) => {
    setSelectedExpert(expert);
    // Set default appointment time if they are busy
    if (expert.status === 'busy') {
      const now = new Date();
      now.setHours(now.getHours() + 2);
      setAppointmentTime(now.toISOString().slice(0, 16));
    } else {
      setAppointmentTime('');
    }
    setIsModalOpen(true);
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedExpert) return;

    if (selectedExpert.status === 'available') {
      setSuccessMessage(
        `Dispatched Alert! Senior expert ${selectedExpert.name} has been paged to help with manual diagnosis at the selected machine site immediately.`
      );
    } else {
      const formattedTime = appointmentTime ? new Date(appointmentTime).toLocaleString() : 'scheduled time';
      setSuccessMessage(
        `Appointment scheduled! ${selectedExpert.name} has been booked for manual diagnosis on ${formattedTime}.`
      );
    }

    setIsModalOpen(false);
    setDescription('');
    setTimeout(() => {
      setSuccessMessage(null);
    }, 8000);
  };

  return (
    <div className="vt-page max-w-5xl mx-auto p-4 lg:p-8 animate-fade-in">
      <div className="mb-8">
        <p className="vt-label">Human Expert Escalation</p>
        <h1 className="mt-2 text-3xl font-bold leading-10 text-[#131b2e] sm:text-4xl">
          Call Senior Experts
        </h1>
        <p className="mt-2 max-w-3xl text-sm leading-7 text-[#434655]">
          When AI-assisted diagnostics can't resolve the anomaly, page a senior engineer to your location or schedule a hands-on manual verification.
        </p>
      </div>

      {successMessage && (
        <div className="mb-6 p-4 rounded-xl border border-emerald-200 bg-emerald-50 text-emerald-800 font-semibold text-sm flex items-start gap-3 shadow-sm animate-fade-in">
          <svg className="h-5 w-5 text-emerald-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>{successMessage}</div>
        </div>
      )}

      {/* Grid: Available vs Busy */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        
        {/* Available Now */}
        <div className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 flex items-center gap-2">
            <span className="h-2.5 w-2.5 rounded-full bg-emerald-500 animate-ping" />
            Available Now (Free)
          </h3>
          <div className="flex flex-col gap-4">
            {experts
              .filter((e) => e.status === 'available')
              .map((expert) => (
                <div key={expert.id} className="vt-card p-5 border border-gray-100 flex flex-col justify-between h-full bg-white transition-all hover:shadow-md">
                  <div className="flex items-start gap-4">
                    <img src={expert.avatar} alt={expert.name} className="h-12 w-12 rounded-full border border-slate-100" />
                    <div>
                      <h4 className="font-extrabold text-[#131b2e] text-base">{expert.name}</h4>
                      <p className="text-xs font-semibold text-gray-500">{expert.role}</p>
                      <div className="flex flex-wrap gap-1.5 mt-2">
                        {expert.specialties.map((s, idx) => (
                          <span key={idx} className="bg-slate-50 text-slate-600 text-[10px] font-bold px-2 py-0.5 rounded border border-slate-100">
                            {s}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => handleOpenAction(expert)}
                    className="mt-5 w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-2.5 px-4 rounded-xl text-xs transition-colors flex items-center justify-center gap-2"
                  >
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                    </svg>
                    Dispatch to My Site
                  </button>
                </div>
              ))}
          </div>
        </div>

        {/* Busy / Bookings */}
        <div className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 flex items-center gap-2">
            <span className="h-2.5 w-2.5 rounded-full bg-amber-500" />
            Busy (Available Later)
          </h3>
          <div className="flex flex-col gap-4">
            {experts
              .filter((e) => e.status === 'busy')
              .map((expert) => (
                <div key={expert.id} className="vt-card p-5 border border-gray-100 flex flex-col justify-between h-full bg-white transition-all hover:shadow-md">
                  <div className="flex items-start gap-4">
                    <img src={expert.avatar} alt={expert.name} className="h-12 w-12 rounded-full border border-slate-100 filter grayscale" />
                    <div className="flex-1">
                      <h4 className="font-extrabold text-[#131b2e] text-base">{expert.name}</h4>
                      <p className="text-xs font-semibold text-gray-500">{expert.role}</p>
                      <p className="text-xs text-amber-700 font-bold bg-amber-50 border border-amber-100 rounded-lg px-2.5 py-1 mt-2 inline-flex items-center gap-1.5">
                        <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        {expert.nextFreeTime}
                      </p>
                    </div>
                  </div>
                  <button
                    onClick={() => handleOpenAction(expert)}
                    className="mt-5 w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2.5 px-4 rounded-xl text-xs transition-colors flex items-center justify-center gap-2"
                  >
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    Schedule Appointment
                  </button>
                </div>
              ))}
          </div>
        </div>

      </div>

      {/* Booking / Request Modal */}
      {isModalOpen && selectedExpert && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-xl border border-gray-100 overflow-hidden animate-scale-up">
            <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-[#faf8ff]">
              <div>
                <h3 className="font-extrabold text-gray-900 text-lg">
                  {selectedExpert.status === 'available' ? 'Page Expert' : 'Request Appointment'}
                </h3>
                <p className="text-xs text-gray-500">Expert: {selectedExpert.name}</p>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleFormSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase mb-1.5">Troubleshoot Target Machine</label>
                <select
                  value={targetMachineId}
                  onChange={(e) => setTargetMachineId(e.target.value)}
                  required
                  className="w-full rounded-xl border border-gray-200 bg-white px-3 py-2.5 text-sm text-gray-800 shadow-sm focus:border-blue-500 focus:outline-none"
                >
                  {machines.length === 0 && <option value="">Loading machines...</option>}
                  {machines.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.name} ({m.manufacturer} {m.model})
                    </option>
                  ))}
                </select>
              </div>

              {selectedExpert.status === 'busy' && (
                <div>
                  <label className="block text-xs font-bold text-gray-500 uppercase mb-1.5">Scheduled Appointment Time</label>
                  <input
                    type="datetime-local"
                    value={appointmentTime}
                    onChange={(e) => setAppointmentTime(e.target.value)}
                    required
                    className="w-full rounded-xl border border-gray-200 bg-white px-3 py-2.5 text-sm text-gray-800 shadow-sm focus:border-blue-500 focus:outline-none"
                  />
                </div>
              )}

              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase mb-1.5">Urgency Level</label>
                <div className="grid grid-cols-3 gap-2">
                  {['Low', 'Medium', 'High'].map((u) => (
                    <button
                      key={u}
                      type="button"
                      onClick={() => setUrgency(u)}
                      className={`py-2 text-xs font-bold rounded-lg border text-center transition-all ${
                        urgency === u
                          ? 'border-blue-600 bg-blue-50 text-blue-700'
                          : 'border-gray-200 bg-white text-gray-600 hover:bg-slate-50'
                      }`}
                    >
                      {u}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase mb-1.5">Issue Summary / Notes</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Describe the diagnostic anomalies or why manual intervention is needed..."
                  rows={3}
                  required
                  className="w-full rounded-xl border border-gray-200 bg-white px-3 py-2.5 text-sm text-gray-800 shadow-sm focus:border-blue-500 focus:outline-none placeholder-gray-400"
                />
              </div>

              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 bg-white hover:bg-gray-50 text-gray-700 font-bold py-3 px-4 rounded-xl border border-gray-200 text-xs transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className={`flex-1 text-white font-bold py-3 px-4 rounded-xl text-xs transition-colors ${
                    selectedExpert.status === 'available' ? 'bg-emerald-600 hover:bg-emerald-700' : 'bg-blue-600 hover:bg-blue-700'
                  }`}
                >
                  {selectedExpert.status === 'available' ? 'Dispatch Now' : 'Schedule'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
