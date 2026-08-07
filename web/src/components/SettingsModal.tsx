import { useState } from 'react';

interface SettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function SettingsModal({ isOpen, onClose }: SettingsModalProps) {
  const [activeTab, setActiveTab] = useState<'diagnostic' | 'notifications' | 'data' | 'interface'>('diagnostic');
  
  // States for interactive demo controls
  const [acousticSensitivity, setAcousticSensitivity] = useState(70);
  const [thermalSensitivity, setThermalSensitivity] = useState(85);
  const [visualSensitivity, setVisualSensitivity] = useState(60);
  const [themeMode, setThemeMode] = useState<'light' | 'dark'>('light');

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-2xl w-full shadow-2xl border border-gray-100 overflow-hidden flex flex-col md:flex-row h-[550px] animate-scale-up">
        
        {/* Sidebar Tabs */}
        <div className="w-full md:w-56 bg-slate-50 border-r border-gray-100 p-4 flex flex-col justify-between">
          <div className="space-y-1">
            <div className="px-3 py-2">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider">Settings Panel</h3>
            </div>
            
            <button
              onClick={() => setActiveTab('diagnostic')}
              className={`w-full flex items-center gap-2.5 px-3 py-2 text-xs font-semibold rounded-lg text-left transition-all ${
                activeTab === 'diagnostic' ? 'bg-blue-50 text-blue-700 font-bold' : 'text-gray-600 hover:bg-slate-100'
              }`}
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
              </svg>
              Diagnostics
            </button>

            <button
              onClick={() => setActiveTab('notifications')}
              className={`w-full flex items-center gap-2.5 px-3 py-2 text-xs font-semibold rounded-lg text-left transition-all ${
                activeTab === 'notifications' ? 'bg-blue-50 text-blue-700 font-bold' : 'text-gray-600 hover:bg-slate-100'
              }`}
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
              Notifications
            </button>

            <button
              onClick={() => setActiveTab('data')}
              className={`w-full flex items-center gap-2.5 px-3 py-2 text-xs font-semibold rounded-lg text-left transition-all ${
                activeTab === 'data' ? 'bg-blue-50 text-blue-700 font-bold' : 'text-gray-600 hover:bg-slate-100'
              }`}
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4" />
              </svg>
              Data & Integrations
            </button>

            <button
              onClick={() => setActiveTab('interface')}
              className={`w-full flex items-center gap-2.5 px-3 py-2 text-xs font-semibold rounded-lg text-left transition-all ${
                activeTab === 'interface' ? 'bg-blue-50 text-blue-700 font-bold' : 'text-gray-600 hover:bg-slate-100'
              }`}
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12.066 11.2a1 1 0 000 1.6l5.334 4A1 1 0 0019 16V8a1 1 0 00-1.6-.8l-5.334 4z" />
                <path strokeLinecap="round" strokeLinejoin="round" d="M4.066 11.2a1 1 0 000 1.6l5.334 4A1 1 0 0011 16V8a1 1 0 00-1.6-.8l-5.334 4z" />
              </svg>
              Interface & Regional
            </button>
          </div>
        </div>

        {/* Content Pane */}
        <div className="flex-1 flex flex-col justify-between bg-white overflow-hidden">
          {/* Header */}
          <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <h2 className="text-base font-extrabold text-gray-900">
              {activeTab === 'diagnostic' && 'System & Diagnostic Preferences'}
              {activeTab === 'notifications' && 'Notification & Alert Controls'}
              {activeTab === 'data' && 'Data & Integration Management'}
              {activeTab === 'interface' && 'Interface & Localizations'}
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
            
            {/* Tab: Diagnostic */}
            {activeTab === 'diagnostic' && (
              <div className="space-y-4">
                {/* Sensitivity Thresholds */}
                <div>
                  <h4 className="text-xs font-bold text-gray-900 mb-2">Sensitivity Thresholds</h4>
                  <div className="space-y-2">
                    <div className="flex items-center gap-4">
                      <span className="text-[10px] font-semibold text-gray-500 w-24">Acoustic: {acousticSensitivity}%</span>
                      <input type="range" min="10" max="100" value={acousticSensitivity} onChange={(e) => setAcousticSensitivity(Number(e.target.value))} className="flex-1 accent-blue-600" />
                    </div>
                    <div className="flex items-center gap-4">
                      <span className="text-[10px] font-semibold text-gray-500 w-24">Thermal: {thermalSensitivity}%</span>
                      <input type="range" min="10" max="100" value={thermalSensitivity} onChange={(e) => setThermalSensitivity(Number(e.target.value))} className="flex-1 accent-blue-600" />
                    </div>
                    <div className="flex items-center gap-4">
                      <span className="text-[10px] font-semibold text-gray-500 w-24">Visual: {visualSensitivity}%</span>
                      <input type="range" min="10" max="100" value={visualSensitivity} onChange={(e) => setVisualSensitivity(Number(e.target.value))} className="flex-1 accent-blue-600" />
                    </div>
                  </div>
                </div>

                {/* Default Inspection Views */}
                <div className="pt-4 border-t border-gray-100">
                  <h4 className="text-xs font-bold text-gray-900 mb-2">Default Inspection Views</h4>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Diagnostic Mode View</label>
                      <select className="w-full border border-gray-200 rounded-xl px-3 py-1.5 text-xs font-semibold text-gray-800 focus:outline-none cursor-pointer">
                        <option value="overlay">Reference Image Overlay</option>
                        <option value="split">Side-by-Side Comparison</option>
                        <option value="raw">Raw Diagnostics Output</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Interactive Learning</label>
                      <select className="w-full border border-gray-200 rounded-xl px-3 py-1.5 text-xs font-semibold text-gray-800 focus:outline-none cursor-pointer">
                        <option value="split">Split Workspace (Chat + Document)</option>
                        <option value="chat">Study Buddy Chat Only</option>
                      </select>
                    </div>
                  </div>
                </div>

                {/* Vector Model Controls */}
                <div className="pt-4 border-t border-gray-100 grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Active AI Reasoner Version</label>
                    <select className="w-full border border-gray-200 rounded-xl px-3 py-1.5 text-xs font-semibold text-gray-800 focus:outline-none cursor-pointer">
                      <option value="v2">VisionTwin Core v2.4 (Latest)</option>
                      <option value="v1">VisionTwin Core v1.9 (Legacy)</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Dataset Sync Frequency</label>
                    <select className="w-full border border-gray-200 rounded-xl px-3 py-1.5 text-xs font-semibold text-gray-800 focus:outline-none cursor-pointer">
                      <option value="realtime">Real-time IoT Streams</option>
                      <option value="daily">Daily Batch Aggregations</option>
                      <option value="weekly">Weekly Synced Archives</option>
                    </select>
                  </div>
                </div>
              </div>
            )}

            {/* Tab: Notifications */}
            {activeTab === 'notifications' && (
              <div className="space-y-4">
                {/* Alert Severity Triggers */}
                <div>
                  <h4 className="text-xs font-bold text-gray-900 mb-2">Alert Severity Triggers</h4>
                  <div className="space-y-2">
                    <div className="flex justify-between items-center bg-slate-50 p-2.5 rounded-xl border border-slate-100 text-xs">
                      <span className="font-bold text-gray-700">Critical / Emergency Anomaly</span>
                      <span className="text-blue-600 font-bold">Push, SMS, Email</span>
                    </div>
                    <div className="flex justify-between items-center bg-slate-50 p-2.5 rounded-xl border border-slate-100 text-xs">
                      <span className="font-bold text-gray-700">System Warning Notifications</span>
                      <span className="text-blue-600 font-bold">Push, Email</span>
                    </div>
                    <div className="flex justify-between items-center bg-slate-50 p-2.5 rounded-xl border border-slate-100 text-xs">
                      <span className="font-bold text-gray-700">Info & Calibration Logs</span>
                      <span className="text-gray-500 font-bold">Email Digest Only</span>
                    </div>
                  </div>
                </div>

                {/* Line & Machine Subscriptions */}
                <div className="pt-4 border-t border-gray-100">
                  <h4 className="text-xs font-bold text-gray-900 mb-2">Line & Machine Subscriptions</h4>
                  <div className="grid grid-cols-2 gap-4">
                    <label className="flex items-center gap-2 text-xs text-gray-700">
                      <input type="checkbox" defaultChecked className="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                      Subscribe to Production Line B
                    </label>
                    <label className="flex items-center gap-2 text-xs text-gray-700">
                      <input type="checkbox" defaultChecked className="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                      CNC Spindle alerts
                    </label>
                  </div>
                </div>
              </div>
            )}

            {/* Tab: Data */}
            {activeTab === 'data' && (
              <div className="space-y-4">
                {/* Telemetry & Knowledge Stores */}
                <div>
                  <h4 className="text-xs font-bold text-gray-900 mb-2">Telemetry & Knowledge Stores</h4>
                  <div className="space-y-2">
                    <div className="flex justify-between items-center text-xs">
                      <div>
                        <span className="font-bold block text-gray-700">External Pinecone / Vector DB</span>
                        <span className="text-[10px] text-gray-400">RAG reference databases</span>
                      </div>
                      <span className="text-emerald-600 font-bold">Connected</span>
                    </div>
                    <div className="flex justify-between items-center text-xs pt-2 border-t border-slate-50">
                      <div>
                        <span className="font-bold block text-gray-700">Industrial IoT Sensors Feed</span>
                        <span className="text-[10px] text-gray-400">Modbus & OPC UA endpoints</span>
                      </div>
                      <span className="text-emerald-600 font-bold">Streaming</span>
                    </div>
                  </div>
                </div>

                {/* Export Settings */}
                <div className="pt-4 border-t border-gray-100">
                  <h4 className="text-xs font-bold text-gray-900 mb-2">Default Export Format</h4>
                  <div className="grid grid-cols-3 gap-2">
                    {['PDF (Detailed report)', 'CSV (Raw datasets)', 'JSON (Model embeddings)'].map((fmt, idx) => (
                      <div key={idx} className="p-2 border border-gray-100 rounded-lg bg-slate-50 text-center text-[10px] font-bold text-gray-600 cursor-pointer hover:border-blue-500 hover:text-blue-600">
                        {fmt}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* Tab: Interface */}
            {activeTab === 'interface' && (
              <div className="space-y-4">
                {/* Theme & Display */}
                <div>
                  <h4 className="text-xs font-bold text-gray-900 mb-2">Theme & Display Settings</h4>
                  <div className="flex gap-4">
                    <button
                      onClick={() => setThemeMode('light')}
                      className={`flex-1 py-2 text-xs font-bold rounded-lg border text-center transition-all ${
                        themeMode === 'light'
                          ? 'border-blue-600 bg-blue-50 text-blue-700'
                          : 'border-gray-200 bg-white text-gray-600 hover:bg-slate-50'
                      }`}
                    >
                      Light Mode
                    </button>
                    <button
                      onClick={() => setThemeMode('dark')}
                      className={`flex-1 py-2 text-xs font-bold rounded-lg border text-center transition-all ${
                        themeMode === 'dark'
                          ? 'border-blue-600 bg-blue-50 text-blue-700'
                          : 'border-gray-200 bg-white text-gray-600 hover:bg-slate-50'
                      }`}
                    >
                      Dark Mode (Glassmorphism)
                    </button>
                  </div>
                </div>

                {/* Language & Regional Settings */}
                <div className="pt-4 border-t border-gray-100 grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Display Language</label>
                    <select className="w-full border border-gray-200 rounded-xl px-3 py-1.5 text-xs font-semibold text-gray-800 focus:outline-none cursor-pointer">
                      <option value="en">English (US)</option>
                      <option value="de">Deutsch (German)</option>
                      <option value="es">Español (Spanish)</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Timezone</label>
                    <select className="w-full border border-gray-200 rounded-xl px-3 py-1.5 text-xs font-semibold text-gray-800 focus:outline-none cursor-pointer">
                      <option value="utc">UTC (Coordinated Universal Time)</option>
                      <option value="est">EST (Eastern Standard Time)</option>
                    </select>
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
              Save Settings
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}
