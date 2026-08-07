import { useState } from 'react';

interface AnnouncementItem {
  id: string;
  title: string;
  category: 'System' | 'Maintenance' | 'Safety' | 'General';
  date: string;
  content: string;
  important: boolean;
}

export default function Announcement() {
  const [announcements] = useState<AnnouncementItem[]>([
    {
      id: '1',
      title: 'AI Diagnostic Engine Upgraded to v2.4',
      category: 'System',
      date: '2026-08-05',
      content: 'We have updated our core neural networks for acoustic and thermal anomaly detection. Accuracy on rotating shafts, gearboxes, and hydraulic systems has been increased by 14%. Please make sure to upload clear audio clips or thermal images when diagnosing.',
      important: true,
    },
    {
      id: '2',
      title: 'Scheduled Maintenance Shutdown: Line 4 and 5',
      category: 'Maintenance',
      date: '2026-08-04',
      content: 'Line 4 (Injection Molding) and Line 5 (Robotic Arm Assembly) will undergo scheduled calibration and diagnostic verification on Sunday, August 9th, from 06:00 to 14:00 UTC. Direct dashboard monitoring for these lines will be temporarily disabled during this period.',
      important: false,
    },
    {
      id: '3',
      title: 'Updated Lockout-Tagout (LOTO) Procedures',
      category: 'Safety',
      date: '2026-08-01',
      content: 'New safety compliance protocols require dual-verification on all high-voltage electrical enclosures and pneumatic pumps. Read the reference guides in the Interactive Learning section before initiating troubleshooting steps on heavy machinery.',
      important: true,
    },
    {
      id: '4',
      title: 'Interactive Learning Modules Added for CNC Diagnostics',
      category: 'General',
      date: '2026-07-28',
      content: 'Three new training modules have been added covering common CNC spindle defects, vibration alignment patterns, and tool-wear diagnostic signatures. Access them directly via the Interactive Learning page.',
      important: false,
    },
  ]);

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'System':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'Maintenance':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'Safety':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      default:
        return 'bg-slate-50 text-slate-700 border-slate-200';
    }
  };

  return (
    <div className="vt-page">
      <div className="animate-fade-in mb-8">
        <p className="vt-label">Broadcast Hub</p>
        <h1 className="mt-2 text-3xl font-bold leading-10 text-[#131b2e] sm:text-4xl">
          Announcements & Alerts
        </h1>
        <p className="mt-2 max-w-3xl text-base leading-7 text-[#434655]">
          Stay updated with the latest system releases, maintenance schedules, safety guidelines, and diagnostic updates for all industrial machinery.
        </p>
      </div>

      <div className="animate-fade-in grid gap-6">
        {announcements.map((item) => (
          <div
            key={item.id}
            className={`vt-card p-6 border-l-4 transition-all hover:shadow-md ${
              item.important ? 'border-l-rose-500 bg-rose-50/10' : 'border-l-[#2563eb]'
            }`}
          >
            <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
              <div className="flex items-center gap-3">
                <span className={`rounded-full border px-2.5 py-0.5 text-xs font-semibold ${getCategoryColor(item.category)}`}>
                  {item.category}
                </span>
                {item.important && (
                  <span className="flex items-center gap-1 text-xs font-bold text-rose-600">
                    <svg className="h-3.5 w-3.5 fill-current" viewBox="0 0 24 24">
                      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
                    </svg>
                    CRITICAL
                  </span>
                )}
              </div>
              <span className="text-xs font-medium text-[#737686]">{item.date}</span>
            </div>
            <h2 className="text-xl font-bold text-[#131b2e] mb-2">{item.title}</h2>
            <p className="text-sm leading-relaxed text-[#434655]">{item.content}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
