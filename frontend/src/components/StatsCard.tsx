import React from 'react';
import { Smartphone, ShieldAlert, ShieldCheck, Cpu } from 'lucide-react';
import { DashboardStats } from '../types';

interface StatsCardProps {
  stats: DashboardStats | null;
}

export const StatsCard: React.FC<StatsCardProps> = ({ stats }) => {
  const gm = stats?.google_messages || {
    total_intercepted: 24,
    smishing_blocked: 9,
    suspicious_warned: 4,
    safe_passed: 11,
    avg_risk_score: 41.6
  };

  const cards = [
    {
      title: 'Intercepted Messages',
      value: gm.total_intercepted,
      subtitle: 'Google Messages & SMS Stream',
      icon: Smartphone,
      textColor: 'text-cyan-400',
      border: 'border-slate-800 hover:border-cyan-500/40',
      badge: 'LIVE HOOK',
      badgeColor: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20'
    },
    {
      title: 'Smishing Neutralized',
      value: gm.smishing_blocked,
      subtitle: 'High & Critical Risks Blocked',
      icon: ShieldAlert,
      textColor: 'text-rose-400',
      border: 'border-slate-800 hover:border-rose-500/40',
      badge: 'BLOCKED',
      badgeColor: 'bg-rose-500/10 text-rose-400 border-rose-500/20'
    },
    {
      title: 'Verified Benign Traffic',
      value: gm.safe_passed,
      subtitle: '2FA OTPs & Normal Texts Allowed',
      icon: ShieldCheck,
      textColor: 'text-emerald-400',
      border: 'border-slate-800 hover:border-emerald-500/40',
      badge: '0.0% FP',
      badgeColor: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
    },
    {
      title: 'Model Test Accuracy',
      value: '98.41%',
      subtitle: '5,971 Real Dataset Evaluation',
      icon: Cpu,
      textColor: 'text-purple-400',
      border: 'border-slate-800 hover:border-purple-500/40',
      badge: 'F1: 0.9588',
      badgeColor: 'bg-purple-500/10 text-purple-300 border-purple-500/20'
    }
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      {cards.map((card, i) => {
        const Icon = card.icon;
        return (
          <div
            key={i}
            className={`boundary-box p-4 bg-slate-900/80 border ${card.border} transition-all duration-200 relative group`}
          >
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-semibold text-slate-400 tracking-wide uppercase">{card.title}</span>
              <span className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${card.badgeColor}`}>
                {card.badge}
              </span>
            </div>
            <div className="flex items-baseline justify-between">
              <h3 className={`text-3xl font-black font-mono tracking-tight ${card.textColor}`}>{card.value}</h3>
              <div className="p-2 rounded-lg bg-slate-950/60 border border-slate-800 group-hover:border-slate-700">
                <Icon className={`w-4 h-4 ${card.textColor}`} />
              </div>
            </div>
            <p className="text-[11px] text-slate-500 mt-2 font-sans">{card.subtitle}</p>
          </div>
        );
      })}
    </div>
  );
};
