import { useState } from 'react';

interface ProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function ProfileModal({ isOpen, onClose }: ProfileModalProps) {
  const [activeTab, setActiveTab] = useState<'personal' | 'security' | 'shift' | 'expertise'>('personal');

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-2xl w-full shadow-2xl border border-gray-100 overflow-hidden flex flex-col md:flex-row h-[550px] animate-scale-up">
        
        {/* Sidebar Tabs */}
        <div className="w-full md:w-56 bg-slate-50 border-r border-gray-100 p-4 flex flex-col justify-between">
          <div className="space-y-1">
            <div className="px-3 py-2">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider">Profile Settings</h3>
            </div>
            
            <button
              onClick={() => setActiveTab('personal')}
              className={`w-full flex items-center gap-2.5 px-3 py-2 text-xs font-semibold rounded-lg text-left transition-all ${
                activeTab === 'personal' ? 'bg-blue-50 text-blue-700 font-bold' : 'text-gray-600 hover:bg-slate-100'
              }`}
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              Personal Info
            </button>

            <button
              onClick={() => setActiveTab('security')}
              className={`w-full flex items-center gap-2.5 px-3 py-2 text-xs font-semibold rounded-lg text-left transition-all ${
                activeTab === 'security' ? 'bg-blue-50 text-blue-700 font-bold' : 'text-gray-600 hover:bg-slate-100'
              }`}
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
              Security
            </button>

            <button
              onClick={() => setActiveTab('shift')}
              className={`w-full flex items-center gap-2.5 px-3 py-2 text-xs font-semibold rounded-lg text-left transition-all ${
                activeTab === 'shift' ? 'bg-blue-50 text-blue-700 font-bold' : 'text-gray-600 hover:bg-slate-100'
              }`}
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Shift & Availability
            </button>

            <button
              onClick={() => setActiveTab('expertise')}
              className={`w-full flex items-center gap-2.5 px-3 py-2 text-xs font-semibold rounded-lg text-left transition-all ${
                activeTab === 'expertise' ? 'bg-blue-50 text-blue-700 font-bold' : 'text-gray-600 hover:bg-slate-100'
              }`}
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
              </svg>
              Expertise & Badges
            </button>
          </div>

          <div className="flex items-center gap-2 px-3">
            <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
            <span className="text-[10px] font-bold text-gray-400 uppercase">Status: Online</span>
          </div>
        </div>

        {/* Content Pane */}
        <div className="flex-1 flex flex-col justify-between bg-white overflow-hidden">
          {/* Header */}
          <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <h2 className="text-base font-extrabold text-gray-900">
              {activeTab === 'personal' && 'Personal Information'}
              {activeTab === 'security' && 'Credentials & Security'}
              {activeTab === 'shift' && 'Work Shift & Availability'}
              {activeTab === 'expertise' && 'Expertise & Certifications'}
            </h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          {/* Body */}
          <div className="flex-1 p-6 overflow-y-auto space-y-4">
            
            {/* Tab: Personal */}
            {activeTab === 'personal' && (
              <div className="space-y-4">
                <div className="flex items-center gap-4 pb-4 border-b border-gray-100">
                  <img
                    src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80"
                    alt="Profile"
                    className="h-16 w-16 rounded-full border border-gray-200"
                  />
                  <div>
                    <h3 className="font-extrabold text-gray-900 text-lg">Nil Yeager</h3>
                    <p className="text-xs text-blue-600 font-bold">System Operator</p>
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Full Name</label>
                    <input type="text" readOnly value="Nil Yeager" className="w-full bg-slate-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-semibold text-gray-800 focus:outline-none" />
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Job Role</label>
                    <input type="text" readOnly value="System Operator" className="w-full bg-slate-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-semibold text-gray-800 focus:outline-none" />
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Employee ID</label>
                    <input type="text" readOnly value="VT-90214" className="w-full bg-slate-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-semibold text-gray-800 focus:outline-none" />
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Department</label>
                    <input type="text" readOnly value="Production Line B / Molding" className="w-full bg-slate-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-semibold text-gray-800 focus:outline-none" />
                  </div>
                </div>
              </div>
            )}

            {/* Tab: Security */}
            {activeTab === 'security' && (
              <div className="space-y-4">
                <div>
                  <h4 className="text-xs font-bold text-gray-900 mb-2">Password Management</h4>
                  <button className="bg-slate-50 hover:bg-slate-100 text-gray-700 font-bold py-2 px-3 border border-gray-200 rounded-xl text-xs transition-colors">
                    Update Security Password
                  </button>
                </div>
                <div className="pt-4 border-t border-gray-100">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="text-xs font-bold text-gray-900">Two-Factor Authentication (2FA)</h4>
                      <p className="text-[10px] text-gray-500 mt-0.5">Secure your employee profile with phone or app authentication.</p>
                    </div>
                    <span className="bg-emerald-100 text-emerald-800 text-[10px] font-bold px-2 py-0.5 rounded">Enabled</span>
                  </div>
                </div>
                <div className="pt-4 border-t border-gray-100">
                  <h4 className="text-xs font-bold text-gray-900 mb-2">Active Session History</h4>
                  <div className="bg-slate-50 rounded-xl p-3 border border-slate-100 space-y-2">
                    <div className="flex justify-between text-[10px] font-semibold text-gray-600">
                      <span>Windows 11 PC &bull; Chrome Web</span>
                      <span className="text-blue-600">Current Session</span>
                    </div>
                    <div className="flex justify-between text-[10px] font-semibold text-gray-400">
                      <span>VisionTwin Mobile App &bull; iOS</span>
                      <span>Active 2h ago</span>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Tab: Shift */}
            {activeTab === 'shift' && (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Current Shift Schedule</label>
                    <span className="text-xs font-bold text-gray-800 block">Morning Shift (06:00 - 14:00 UTC)</span>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Work Status</label>
                    <select className="w-full border border-gray-200 rounded-xl px-3 py-1.5 text-xs font-semibold text-gray-800 focus:outline-none cursor-pointer">
                      <option value="online">Online & Active</option>
                      <option value="maintenance">In Maintenance Mode</option>
                      <option value="oncall">On Call (Emergency Escalation)</option>
                    </select>
                  </div>
                </div>
                <div className="pt-4 border-t border-gray-100">
                  <h4 className="text-xs font-bold text-gray-900 mb-2">Emergency Contact Preferences</h4>
                  <div className="space-y-2">
                    <label className="flex items-center gap-2 text-xs text-gray-700">
                      <input type="checkbox" defaultChecked className="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                      Page me via push notifications for Line B severity indices &gt; High
                    </label>
                    <label className="flex items-center gap-2 text-xs text-gray-700">
                      <input type="checkbox" defaultChecked className="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                      Send urgent SMS requests if AI model fails verification
                    </label>
                  </div>
                </div>
              </div>
            )}

            {/* Tab: Expertise */}
            {activeTab === 'expertise' && (
              <div className="space-y-4">
                <div>
                  <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Machine Specialties</label>
                  <div className="flex flex-wrap gap-2 mt-1">
                    {['CNC Spindles', 'Hydraulic Systems', 'Lubrication Calibration', 'Pneumatic Controls'].map((spec) => (
                      <span key={spec} className="bg-blue-50 text-blue-700 border border-blue-100 text-xs font-bold px-2.5 py-1 rounded-xl">
                        {spec}
                      </span>
                    ))}
                  </div>
                </div>
                <div className="pt-4 border-t border-gray-100 grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Expertise Level</label>
                    <span className="text-xs font-bold text-gray-800 block">Level 3 Senior Technician</span>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Verification Badges</label>
                    <div className="flex items-center gap-2 mt-1">
                      <span className="bg-amber-100 text-amber-800 text-[10px] font-bold px-2.5 py-1 rounded border border-amber-200 flex items-center gap-1">
                        <svg className="h-3 w-3 fill-current text-amber-600" viewBox="0 0 24 24">
                          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17h-2v-2h2v2zm0-4h-2V7h2v6z" />
                        </svg>
                        Certified Safety Expert
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            )}

          </div>

          {/* Footer */}
          <div className="px-6 py-4 border-t border-gray-100 bg-[#fbfbff] flex justify-end">
            <button
              onClick={onClose}
              className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2.5 px-6 rounded-xl text-xs transition-colors"
            >
              Close Profile
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}
