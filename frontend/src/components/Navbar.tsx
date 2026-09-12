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
  Activity
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
  const tabs: Array<{ id: NavTabType; label: string; icon: React.ReactNode; isResearch?: boolean }> = [
    { id: 'messages', label: 'Threat Radar & Stream', icon: <Smartphone className="w-3.5 h-3.5" /> },
    { id: 'benchmarks', label: 'Research & Benchmarks', icon: <Award className="w-3.5 h-3.5 text-cyan-400" />, isResearch: true },
    { id: 'attack_studio', label: 'Attack Vector & Adversarial Lab', icon: <Flame className="w-3.5 h-3.5 text-amber-400" /> },
    { id: 'urls', label: 'URL Deep Scanner', icon: <Globe className="w-3.5 h-3.5" /> },
    { id: 'emails', label: 'Email Inspector', icon: <Mail className="w-3.5 h-3.5" /> },
    { id: 'batch_evaluator', label: 'Batch Dataset Evaluator', icon: <FileSpreadsheet className="w-3.5 h-3.5 text-indigo-400" />, isResearch: true },
    { id: 'publications', label: 'Paper & LaTeX Hub', icon: <BookOpen className="w-3.5 h-3.5 text-purple-400" />, isResearch: true },
    { id: 'api_gateway', label: 'API & Gateway', icon: <Server className="w-3.5 h-3.5" /> },
  ];

  return (
    <header className="glass-panel sticky top-3 z-50 mb-6 mx-auto max-w-7xl px-5 py-3 flex flex-wrap items-center justify-between gap-4">
      {/* Brand & Identity */}
      <div className="flex items-center gap-3">
        <div className="p-2.5 rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-600 shadow-lg shadow-cyan-500/25 flex items-center justify-center">
          <Shield className="w-5 h-5 text-white" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <span className="text-lg font-bold tracking-tight text-white font-sans">PhishGuard</span>
            <span className="text-[10px] px-2 py-0.5 rounded-full font-bold bg-cyan-500/15 text-cyan-300 border border-cyan-500/30">
              RESEARCH v2.4
            </span>
          </div>
          <p className="text-[11px] text-slate-400">Real-Time Multi-Vector Smishing & Threat Intelligence Platform</p>
        </div>
      </div>

      {/* Live Simulation Control Button */}
      <button
        onClick={onToggleSimulateStream}
        className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center gap-2 border ${
          isSimulatingStream
            ? 'bg-amber-500/20 text-amber-300 border-amber-500/40 shadow-lg shadow-amber-500/20 animate-pulse'
            : 'bg-cyan-500/15 text-cyan-300 border-cyan-500/30 hover:bg-cyan-500/25'
        }`}
        title={isSimulatingStream ? 'Pause automatic live stream events' : 'Automatically dispatch realistic mobile events to live stream for demonstration'}
      >
        {isSimulatingStream ? (
          <>
            <Pause className="w-3.5 h-3.5 text-amber-400" />
            <span>Simulating Live Stream (Active)</span>
          </>
        ) : (
          <>
            <Play className="w-3.5 h-3.5 text-cyan-400" />
            <span>Start Live Demo Stream</span>
          </>
        )}
      </button>

      {/* Engine Status & Latency Badge */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl bg-slate-950/70 border border-white/10">
          <div className="pulse-live" />
          <div className="text-left text-xs font-mono">
            <div className="text-[10px] font-bold text-emerald-400 flex items-center gap-1">
              ENGINE ONLINE
            </div>
            <div className="text-[10px] text-slate-400">
              <span className="text-cyan-300 font-bold">{latencyMs} ms</span> • {activeDevices || 1} Device
            </div>
          </div>
        </div>
      </div>

      {/* Navigation Tab Bar */}
      <nav className="w-full flex items-center gap-1.5 p-1 bg-slate-950/80 rounded-xl border border-white/5 overflow-x-auto">
        {tabs.map((t) => (
          <button
            key={t.id}
            onClick={() => setActiveTab(t.id)}
            className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-semibold whitespace-nowrap transition-all ${
              activeTab === t.id
                ? 'bg-gradient-to-r from-cyan-500 to-blue-600 text-slate-950 font-bold shadow-md shadow-cyan-500/25'
                : 'text-slate-400 hover:text-white hover:bg-white/5'
            }`}
          >
            {t.icon}
            <span>{t.label}</span>
          </button>
        ))}
      </nav>
    </header>
  );
};
