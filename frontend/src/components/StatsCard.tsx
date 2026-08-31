import React from 'react';
import { Smartphone, ShieldAlert, ShieldCheck, Activity } from 'lucide-react';
import { DashboardStats } from '../types';

interface StatsCardProps {
  stats: DashboardStats | null;
}

export const StatsCard: React.FC<StatsCardProps> = ({ stats }) => {
  const gm = stats?.google_messages || {
    total_intercepted: 5,
    smishing_blocked: 3,
    suspicious_warned: 1,
    safe_passed: 1,
    avg_risk_score: 54.2
  };

  const cards = [
    {
      title: 'Google Messages Intercepted',
      value: gm.total_intercepted,
      subtitle: 'Active real-time listener',
      icon: Smartphone,
      color: 'from-blue-500/20 to-cyan-500/5',
      textColor: 'text-cyan-400',
      border: 'border-cyan-500/20'
    },
    {
      title: 'Smishing Attacks Blocked',
      value: gm.smishing_blocked,
      subtitle: 'Critical & High threats neutralized',
      icon: ShieldAlert,
      color: 'from-red-500/20 to-rose-500/5',
      textColor: 'text-red-400',
      border: 'border-red-500/20'
    },
    {
      title: 'Verified Safe / Benign',
      value: gm.safe_passed,
      subtitle: '2FA OTPs & normal SMS passed',
      icon: ShieldCheck,
      color: 'from-emerald-500/20 to-green-500/5',
      textColor: 'text-emerald-400',
      border: 'border-emerald-500/20'
    },
    {
      title: 'Avg Threat Risk Index',
      value: `${gm.avg_risk_score}%`,
      subtitle: 'Across all mobile endpoints',
      icon: Activity,
      color: 'from-purple-500/20 to-indigo-500/5',
      textColor: 'text-purple-400',
      border: 'border-purple-500/20'
    }
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      {cards.map((card, i) => {
        const Icon = card.icon;
        return (
          <div
            key={i}
            className={`glass-panel p-5 relative overflow-hidden bg-gradient-to-br ${card.color} ${card.border}`}
          >
            <div className="flex items-start justify-between">
              <div>
                <p className="text-xs font-semibold text-slate-400 mb-1">{card.title}</p>
                <h3 className={`text-2xl font-bold font-mono ${card.textColor}`}>{card.value}</h3>
                <p className="text-[11px] text-slate-400 mt-1">{card.subtitle}</p>
              </div>
              <div className="p-2.5 rounded-xl bg-slate-900/60 border border-white/5">
                <Icon className={`w-5 h-5 ${card.textColor}`} />
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};
