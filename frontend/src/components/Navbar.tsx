import React from 'react';
import { Shield, Radio, Smartphone, Globe, Mail, Activity } from 'lucide-react';

interface NavbarProps {
  activeTab: 'messages' | 'urls' | 'emails' | 'simulator';
  setActiveTab: (tab: 'messages' | 'urls' | 'emails' | 'simulator') => void;
  latencyMs: number;
  activeDevices: number;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  latencyMs,
  activeDevices
}) => {
  return (
    <header className="glass-panel sticky top-4 z-50 mb-8 mx-auto max-w-7xl px-6 py-4 flex flex-wrap items-center justify-between gap-4">
      {/* Brand & Logo */}
      <div className="flex items-center gap-3">
        <div className="p-2.5 rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-600 shadow-lg shadow-blue-500/25 flex items-center justify-center">
          <Shield className="w-6 h-6 text-white" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-white">PhishGuard</h1>
            <span className="text-xs px-2 py-0.5 rounded-full font-semibold bg-cyan-500/10 text-cyan-400 border border-cyan-500/30">
              v2.4 PRO
            </span>
          </div>
          <p className="text-xs text-slate-400">Real-Time Google Messages & Phishing Defense Platform</p>
        </div>
      </div>

      {/* Navigation Tabs */}
      <nav className="flex items-center gap-1.5 p-1 bg-slate-900/80 rounded-xl border border-white/5">
        <button
          onClick={() => setActiveTab('messages')}
          className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-semibold transition-all ${
            activeTab === 'messages'
              ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          <Smartphone className="w-4 h-4" />
          Google Messages Stream
        </button>
        <button
          onClick={() => setActiveTab('simulator')}
          className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-semibold transition-all ${
            activeTab === 'simulator'
              ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          <Radio className="w-4 h-4" />
          Smishing Sandbox
        </button>
        <button
          onClick={() => setActiveTab('urls')}
          className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-semibold transition-all ${
            activeTab === 'urls'
              ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          <Globe className="w-4 h-4" />
          URL Deep Scanner
        </button>
        <button
          onClick={() => setActiveTab('emails')}
          className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-semibold transition-all ${
            activeTab === 'emails'
              ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          <Mail className="w-4 h-4" />
          Email Analyzer
        </button>
      </nav>

      {/* Engine Status Badge */}
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2.5 px-3 py-1.5 rounded-lg bg-slate-900/60 border border-white/5">
          <div className="pulse-live"></div>
          <div className="text-left">
            <div className="flex items-center gap-1.5">
              <span className="text-[11px] font-bold text-emerald-400">ENGINE ONLINE</span>
            </div>
            <span className="text-[10px] text-slate-400 font-mono">{latencyMs}ms • {activeDevices || 1} Device Hooked</span>
          </div>
        </div>
      </div>
    </header>
  );
};
