import React, { useState } from 'react';
import { Smartphone, AlertTriangle, ShieldCheck, ShieldAlert, ChevronDown, ChevronUp, CheckCircle, ExternalLink, ThumbsUp, ThumbsDown } from 'lucide-react';
import { GoogleMessageEvent } from '../types';
import { submitFeedback } from '../services/api';

interface LiveMessageFeedProps {
  messages: GoogleMessageEvent[];
  onRefresh: () => void;
}

export const LiveMessageFeed: React.FC<LiveMessageFeedProps> = ({ messages, onRefresh }) => {
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [feedbackStatus, setFeedbackStatus] = useState<Record<string, string>>({});

  const handleFeedback = async (id: string, verdict: string) => {
    try {
      await submitFeedback(id, verdict, 'Reported from Live Stream Manager');
      setFeedbackStatus(prev => ({ ...prev, [id]: verdict }));
    } catch (e) {
      console.error(e);
    }
  };

  const getRiskBadge = (level: string, score: number) => {
    switch (level) {
      case 'CRITICAL':
        return <span className="badge-critical px-2.5 py-1 rounded-full text-xs font-bold font-mono">🚨 CRITICAL ({score}%)</span>;
      case 'HIGH':
        return <span className="badge-high px-2.5 py-1 rounded-full text-xs font-bold font-mono">⚠️ HIGH ({score}%)</span>;
      case 'MEDIUM':
        return <span className="badge-medium px-2.5 py-1 rounded-full text-xs font-bold font-mono">⚡ SUSPICIOUS ({score}%)</span>;
      default:
        return <span className="badge-safe px-2.5 py-1 rounded-full text-xs font-bold font-mono">✅ SAFE ({score}%)</span>;
    }
  };

  return (
    <div className="glass-panel p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <Smartphone className="w-5 h-5 text-cyan-400" />
            Google Messages Live Interception Stream
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time feed captured by Android NotificationListenerService from <code className="text-cyan-300 font-mono">com.google.android.apps.messaging</code>
          </p>
        </div>
        <button onClick={onRefresh} className="btn-secondary text-xs">
          Refresh Stream
        </button>
      </div>

      {messages.length === 0 ? (
        <div className="text-center py-16 text-slate-400">
          <Smartphone className="w-12 h-12 mx-auto mb-3 text-slate-600 animate-pulse" />
          <p className="text-sm font-semibold">Listening for incoming Google Messages notifications...</p>
          <p className="text-xs text-slate-500 mt-1">Send a test notification or use the Smishing Sandbox tab.</p>
        </div>
      ) : (
        <div className="space-y-3.5">
          {messages.map((msg) => {
            const isExpanded = expandedId === msg.id;
            const isMalicious = msg.risk_score >= 60.0;
            const isSuspicious = msg.risk_score >= 35.0 && msg.risk_score < 60.0;

            return (
              <div
                key={msg.id}
                className={`p-4 rounded-xl border transition-all ${
                  isMalicious
                    ? 'bg-red-500/5 border-red-500/30 hover:border-red-500/50'
                    : isSuspicious
                    ? 'bg-amber-500/5 border-amber-500/30 hover:border-amber-500/50'
                    : 'bg-slate-900/40 border-white/5 hover:border-white/10'
                }`}
              >
                {/* Header Row */}
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div className="flex items-center gap-3">
                    <div className={`p-2 rounded-lg ${isMalicious ? 'bg-red-500/20 text-red-400' : isSuspicious ? 'bg-amber-500/20 text-amber-400' : 'bg-emerald-500/20 text-emerald-400'}`}>
                      {isMalicious ? <ShieldAlert className="w-4 h-4" /> : isSuspicious ? <AlertTriangle className="w-4 h-4" /> : <ShieldCheck className="w-4 h-4" />}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-sm text-white">{msg.sender}</span>
                        <span className="text-[10px] px-2 py-0.5 rounded bg-slate-800 text-slate-400 font-mono">
                          {msg.source_app}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-400 mt-0.5">
                        {new Date(msg.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })} • Hooked from Pixel 8 Pro
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    {getRiskBadge(msg.risk_level, msg.risk_score)}
                    <button
                      onClick={() => setExpandedId(isExpanded ? null : msg.id)}
                      className="p-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-slate-300 transition-colors"
                    >
                      {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                    </button>
                  </div>
                </div>

                {/* Message Body Preview */}
                <div className="mt-3 p-3 rounded-lg bg-slate-950/60 border border-white/5 font-mono text-xs text-slate-200 leading-relaxed">
                  "{msg.text}"
                </div>

                {/* Threat Category Badges */}
                {msg.threat_categories && msg.threat_categories.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 mt-2.5">
                    {msg.threat_categories.map((cat, idx) => (
                      <span key={idx} className="text-[11px] px-2 py-0.5 rounded-md bg-red-950/40 text-red-300 border border-red-800/40">
                        • {cat}
                      </span>
                    ))}
                  </div>
                )}

                {/* Expanded Details & Explainability */}
                {isExpanded && (
                  <div className="mt-4 pt-4 border-t border-white/10 space-y-3">
                    <div>
                      <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2">AI Explainability & Risk Rationale:</h4>
                      <ul className="space-y-1.5">
                        {msg.reasons.map((r, i) => (
                          <li key={i} className="text-xs text-slate-300 flex items-start gap-2">
                            <span className="text-red-400 font-bold">•</span>
                            <span>{r}</span>
                          </li>
                        ))}
                      </ul>
                    </div>

                    {/* Actions & Feedback Row */}
                    <div className="flex items-center justify-between pt-2">
                      <span className="text-[11px] text-slate-400">
                        {isMalicious ? '🚨 Heads-Up Warning Dispatched to Device' : '✅ Communication Allowed Without Alarm'}
                      </span>

                      <div className="flex items-center gap-2">
                        <span className="text-xs text-slate-400 mr-1">Feedback:</span>
                        <button
                          onClick={() => handleFeedback(msg.id, 'PHISHING')}
                          disabled={Boolean(feedbackStatus[msg.id])}
                          className={`px-2.5 py-1 rounded text-xs flex items-center gap-1.5 border transition-all ${
                            feedbackStatus[msg.id] === 'PHISHING'
                              ? 'bg-red-500 text-white border-red-400'
                              : 'bg-slate-800 hover:bg-red-500/20 text-slate-300 border-white/10'
                          }`}
                        >
                          <ThumbsDown className="w-3 h-3" />
                          Confirm Scam
                        </button>
                        <button
                          onClick={() => handleFeedback(msg.id, 'LEGITIMATE')}
                          disabled={Boolean(feedbackStatus[msg.id])}
                          className={`px-2.5 py-1 rounded text-xs flex items-center gap-1.5 border transition-all ${
                            feedbackStatus[msg.id] === 'LEGITIMATE'
                              ? 'bg-emerald-500 text-white border-emerald-400'
                              : 'bg-slate-800 hover:bg-emerald-500/20 text-slate-300 border-white/10'
                          }`}
                        >
                          <ThumbsUp className="w-3 h-3" />
                          Mark Safe
                        </button>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
