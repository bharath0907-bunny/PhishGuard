import React from 'react';
import { 
  Shield, 
  Radio, 
  Smartphone, 
  Globe, 
  Mail, 
  Award, 
  Flame, 
  FileSpreadsheet, 
  BookOpen, 
  Server, 
  Play, 
  Pause,
  Activity,
  Cpu
} from 'lucide-react';

export type NavTabType = 
  | 'messages' 
  | 'benchmarks' 
  | 'attack_studio' 
  | 'urls' 
  | 'emails' 
  | 'batch_evaluator' 
  | 'publications' 
  | 'api_gateway';

interface NavbarProps {
  activeTab: NavTabType;
  setActiveTab: (tab: NavTabType) => void;
  latencyMs: number;
  activeDevices: number;
  isSimulatingStream: boolean;
  onToggleSimulateStream: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  latencyMs,
  activeDevices,
  isSimulatingStream,
  onToggleSimulateStream
}) => {
  const tabs: Array<{ id: NavTabType; label: string; icon: React.ReactNode; badge?: string }> = [
    { id: 'messages', label: 'Threat Stream & Radar', icon: <Smartphone className="w-3.5 h-3.5" /> },
    { id: 'benchmarks', label: 'Model Benchmarks', icon: <Award className="w-3.5 h-3.5 text-cyan-400" />, badge: '98.4%' },
    { id: 'attack_studio', label: 'Evasion & Attack Lab', icon: <Flame className="w-3.5 h-3.5 text-amber-400" /> },
    { id: 'urls', label: 'URL Deep Inspector', icon: <Globe className="w-3.5 h-3.5" /> },
    { id: 'emails', label: 'Email Scanner', icon: <Mail className="w-3.5 h-3.5" /> },
    { id: 'batch_evaluator', label: 'Batch Dataset Testing', icon: <FileSpreadsheet className="w-3.5 h-3.5 text-indigo-400" /> },
    { id: 'publications', label: 'Research & LaTeX Hub', icon: <BookOpen className="w-3.5 h-3.5 text-purple-400" /> },
    { id: 'api_gateway', label: 'API & Mobile Gateway', icon: <Server className="w-3.5 h-3.5" /> },
  ];

  return (
    <header className="glass-panel sticky top-3 z-50 mb-6 mx-auto max-w-7xl px-5 py-3.5 border border-slate-800 bg-slate-950/90 flex flex-wrap items-center justify-between gap-4">
      {/* Brand & Identity */}
      <div className="flex items-center gap-3">
        <div className="p-2.5 rounded-lg bg-slate-900 border border-cyan-500/30 shadow-md shadow-cyan-500/10 flex items-center justify-center">
          <Shield className="w-5 h-5 text-cyan-400" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <span className="text-base font-black tracking-wider text-white font-mono uppercase">PhishGuard SOC</span>
            <span className="text-[10px] px-2 py-0.5 rounded-md font-mono font-bold bg-cyan-500/10 text-cyan-300 border border-cyan-500/30">
              5,971 DATASET // v2.4
            </span>
          </div>
          <p className="text-[11px] text-slate-400 font-sans">Central Security Operations & Threat Defense Console</p>
        </div>
      </div>

      {/* Operational Controls & Engine Telemetry */}
      <div className="flex items-center gap-3">
        <button
          onClick={onToggleSimulateStream}
          className={`px-3 py-1.5 rounded-lg text-xs font-mono font-bold transition-all flex items-center gap-2 border ${
            isSimulatingStream
              ? 'bg-amber-500/15 text-amber-300 border-amber-500/40 animate-pulse'
              : 'bg-slate-900 text-slate-300 border-slate-700 hover:border-slate-500'
          }`}
          title="Toggle synthetic background event dispatcher"
        >
          {isSimulatingStream ? (
            <>
              <Pause className="w-3.5 h-3.5 text-amber-400" />
              <span>Simulating Stream</span>
            </>
          ) : (
            <>
              <Play className="w-3.5 h-3.5 text-cyan-400" />
              <span>Test Stream Feed</span>
            </>
          )}
        </button>

        {/* Engine Status & Latency Badge */}
        <div className="flex items-center gap-2.5 px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-800">
          <div className="pulse-live" />
          <div className="text-left font-mono">
            <div className="text-[10px] font-bold text-emerald-400 uppercase tracking-wider">
              ONLINE
            </div>
            <div className="text-[10px] text-slate-400">
              <span className="text-cyan-300 font-bold">{latencyMs} ms</span> • {activeDevices || 1} Device
            </div>
          </div>
        </div>
      </div>

      {/* Segmented Navigation Tab Bar */}
      <nav className="w-full flex items-center gap-1.5 p-1 bg-slate-900/90 rounded-lg border border-slate-800 overflow-x-auto">
        {tabs.map((t) => {
          const isActive = activeTab === t.id;
          return (
            <button
              key={t.id}
              onClick={() => setActiveTab(t.id)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium whitespace-nowrap transition-all border ${
                isActive
                  ? 'bg-cyan-500/15 text-cyan-300 border-cyan-500/40 font-bold shadow-sm'
                  : 'text-slate-400 border-transparent hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              {t.icon}
              <span>{t.label}</span>
              {t.badge && (
                <span className="ml-1 text-[9px] px-1.5 py-0.2 rounded font-mono font-bold bg-cyan-500/20 text-cyan-200 border border-cyan-500/30">
                  {t.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>
    </header>
  );
};
