import React, { useState } from 'react';
import { 
  Smartphone, 
  AlertTriangle, 
  ShieldCheck, 
  ShieldAlert, 
  ChevronDown, 
  ChevronUp, 
  Search, 
  Filter, 
  Volume2, 
  VolumeX, 
  Radio, 
  ThumbsUp, 
  ThumbsDown, 
  Maximize2, 
  X, 
  Sparkles,
  ExternalLink,
  RefreshCw
} from 'lucide-react';
import { GoogleMessageEvent } from '../types';
import { submitFeedback } from '../services/api';
import { GoogleMessageSimulator } from './GoogleMessageSimulator';

interface LiveMessageFeedProps {
  messages: GoogleMessageEvent[];
  onRefresh: () => void;
  audioAlertsEnabled?: boolean;
  onToggleAudioAlerts?: () => void;
}

export const LiveMessageFeed: React.FC<LiveMessageFeedProps> = ({ 
  messages, 
  onRefresh, 
  audioAlertsEnabled = true,
  onToggleAudioAlerts
}) => {
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [selectedInspectMsg, setSelectedInspectMsg] = useState<GoogleMessageEvent | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [filterSeverity, setFilterSeverity] = useState<string>('ALL');
  const [feedbackStatus, setFeedbackStatus] = useState<Record<string, string>>({});
  const [showSimulator, setShowSimulator] = useState<boolean>(false);

  const handleFeedback = async (id: string, verdict: string) => {
    try {
      await submitFeedback(id, verdict, 'Reported from Live Stream Manager');
      setFeedbackStatus(prev => ({ ...prev, [id]: verdict }));
    } catch (e) {
      console.error(e);
    }
  };

  const filteredMessages = messages.filter((msg) => {
    const matchesSearch = 
      msg.sender.toLowerCase().includes(searchQuery.toLowerCase()) ||
      msg.text.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (msg.source_app && msg.source_app.toLowerCase().includes(searchQuery.toLowerCase()));

    if (!matchesSearch) return false;

    if (filterSeverity === 'ALL') return true;
    if (filterSeverity === 'MALICIOUS') return msg.risk_score >= 55.0;
    if (filterSeverity === 'SUSPICIOUS') return msg.risk_score >= 30.0 && msg.risk_score < 55.0;
    if (filterSeverity === 'SAFE') return msg.risk_score < 30.0;
    return true;
  });

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
    <div className="space-y-4">
      {/* Top Threat Radar & Header Card */}
      <div className="glass-panel p-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-6 border-l-4 border-cyan-500">
        <div className="space-y-2 max-w-2xl">
          <div className="flex items-center gap-2">
            <span className="pulse-live" />
            <span className="text-xs font-bold uppercase tracking-wider text-cyan-400">
              Live OS Notification Interception Stream
            </span>
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-cyan-500/20 text-cyan-300 font-mono font-bold">
              com.google.android.apps.messaging
            </span>
          </div>
          <h1 className="text-2xl font-bold">Real-Time Mobile Threat Telemetry</h1>
          <p className="text-xs text-slate-400">
            Incoming notifications are intercepted by Android <code className="text-slate-300 font-mono">NotificationListenerService</code>, evaluated in &lt;40ms, and broadcasted via real-time WebSocket.
          </p>
        </div>

        {/* Live Threat Radar Sweep Visualizer */}
        <div className="flex items-center gap-4 self-center md:self-auto">
          <div className="radar-sweep flex items-center justify-center">
            <Radio className="w-6 h-6 text-cyan-400 animate-pulse" />
          </div>
          <div className="text-xs space-y-1 font-mono">
            <div className="text-slate-400">Stream Status:</div>
            <div className="text-emerald-400 font-bold flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
              LIVE TELEMETRY
            </div>
            <div className="text-[11px] text-slate-500">
              Latency: <span className="text-cyan-400 font-bold">&lt; 35ms</span>
            </div>
          </div>
        </div>
      </div>

      {/* Filter & Search Bar */}
      <div className="glass-panel p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2 flex-1 max-w-md">
          <Search className="w-4 h-4 text-slate-400 flex-shrink-0" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search intercepted messages, sender, or URLs..."
            className="input-dark text-xs py-2"
          />
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          <div className="flex items-center gap-1 bg-slate-950 p-1 rounded-lg border border-white/10 text-xs">
            {(['ALL', 'MALICIOUS', 'SUSPICIOUS', 'SAFE'] as const).map((sev) => (
              <button
                key={sev}
                onClick={() => setFilterSeverity(sev)}
                className={`px-3 py-1 rounded-md transition-all font-semibold ${
                  filterSeverity === sev
                    ? 'bg-cyan-500 text-slate-950 font-bold'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {sev}
              </button>
            ))}
          </div>

          {onToggleAudioAlerts && (
            <button
              onClick={onToggleAudioAlerts}
              className="btn-secondary text-xs"
              title={audioAlertsEnabled ? 'Mute Threat Chime' : 'Enable Threat Chime'}
            >
              {audioAlertsEnabled ? (
                <>
                  <Volume2 className="w-3.5 h-3.5 text-cyan-400" /> Chime ON
                </>
              ) : (
                <>
                  <VolumeX className="w-3.5 h-3.5 text-slate-500" /> Chime MUTED
                </>
              )}
            </button>
          )}

          <button
            onClick={() => setShowSimulator(prev => !prev)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 border transition-all ${
              showSimulator 
                ? 'bg-cyan-500/25 text-cyan-300 border-cyan-500/50 shadow-md shadow-cyan-500/25' 
                : 'btn-secondary text-xs'
            }`}
          >
            <Radio className="w-3.5 h-3.5 text-cyan-400" />
            {showSimulator ? 'Hide Simulator' : 'Test / Inject Attack Lures'}
          </button>

          <button onClick={onRefresh} className="btn-secondary text-xs">
            <RefreshCw className="w-3.5 h-3.5" /> Refresh
          </button>
        </div>
      </div>

      {/* Embedded 1-Click Google Messages Simulator Drawer */}
      {showSimulator && (
        <div className="animate-fadeIn">
          <GoogleMessageSimulator onMessageSent={onRefresh} />
        </div>
      )}

      {/* Message Feed List */}
      <div className="glass-panel p-6 space-y-4">
        {filteredMessages.length === 0 ? (
          <div className="text-center py-14 text-slate-400 space-y-2">
            <Smartphone className="w-10 h-10 mx-auto text-slate-600 animate-pulse" />
            <p className="text-sm font-semibold">No intercepted events matching your filter.</p>
            <p className="text-xs text-slate-500">
              Use the "Attack Vector & Adversarial Lab" tab or toggle "Start Live Demo Stream" above to dispatch test vectors.
            </p>
          </div>
        ) : (
          <div className="space-y-3.5">
            {filteredMessages.map((msg) => {
              const isExpanded = expandedId === msg.id;
              const isMalicious = msg.risk_score >= 55.0;
              const isSuspicious = msg.risk_score >= 30.0 && msg.risk_score < 55.0;

              return (
                <div
                  key={msg.id}
                  className={`p-4 rounded-xl border transition-all ${
                    isMalicious
                      ? 'bg-rose-500/10 border-rose-500/30 hover:border-rose-500/60'
                      : isSuspicious
                      ? 'bg-amber-500/10 border-amber-500/30 hover:border-amber-500/60'
                      : 'bg-slate-950/40 border-white/5 hover:border-white/15'
                  }`}
                >
                  {/* Header Row */}
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div className="flex items-center gap-3">
                      <div className={`p-2 rounded-lg ${
                        isMalicious ? 'bg-rose-500/20 text-rose-400' : isSuspicious ? 'bg-amber-500/20 text-amber-400' : 'bg-emerald-500/20 text-emerald-400'
                      }`}>
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
                          {new Date(msg.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })} • Pixel 8 Pro Hooked
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center gap-3">
                      {getRiskBadge(msg.risk_level, msg.risk_score)}

                      <button
                        onClick={() => setSelectedInspectMsg(msg)}
                        className="p-1.5 rounded-lg bg-cyan-500/10 hover:bg-cyan-500/20 text-cyan-300 transition-colors"
                        title="Deep XAI Inspection Modal"
                      >
                        <Maximize2 className="w-4 h-4" />
                      </button>

                      <button
                        onClick={() => setExpandedId(isExpanded ? null : msg.id)}
                        className="p-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-slate-300 transition-colors"
                      >
                        {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                      </button>
                    </div>
                  </div>

                  {/* Message Body Preview */}
                  <div className="mt-3 p-3 rounded-lg bg-slate-950/80 border border-white/5 font-mono text-xs text-slate-200 leading-relaxed">
                    "{msg.text}"
                  </div>

                  {/* Threat Categories */}
                  {msg.threat_categories && msg.threat_categories.length > 0 && (
                    <div className="flex flex-wrap gap-1.5 mt-2.5">
                      {msg.threat_categories.map((cat, idx) => (
                        <span key={idx} className="text-[11px] px-2 py-0.5 rounded-md bg-rose-950/40 text-rose-300 border border-rose-800/40">
                          • {cat}
                        </span>
                      ))}
                    </div>
                  )}

                  {/* Expanded AI Explainability */}
                  {isExpanded && (
                    <div className="mt-4 pt-4 border-t border-white/10 space-y-3">
                      <div>
                        <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2">
                          Explainable AI (XAI) Attribution:
                        </h4>
                        <ul className="space-y-1.5">
                          {msg.reasons.map((r, i) => (
                            <li key={i} className="text-xs text-slate-300 flex items-start gap-2">
                              <span className="text-rose-400 font-bold">•</span>
                              <span>{r}</span>
                            </li>
                          ))}
                        </ul>
                      </div>

                      {/* Actions & Feedback Row */}
                      <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
                        <span className="text-[11px] text-slate-400 font-mono">
                          {isMalicious ? '🚨 Heads-Up Warning Dispatched to Device' : '✅ Communication Allowed'}
                        </span>

                        <div className="flex items-center gap-2">
                          <span className="text-xs text-slate-400 mr-1">Calibrate:</span>
                          <button
                            onClick={() => handleFeedback(msg.id, 'PHISHING')}
                            disabled={Boolean(feedbackStatus[msg.id])}
                            className={`px-2.5 py-1 rounded text-xs flex items-center gap-1.5 border transition-all ${
                              feedbackStatus[msg.id] === 'PHISHING'
                                ? 'bg-rose-500 text-white border-rose-400'
                                : 'bg-slate-800 hover:bg-rose-500/20 text-slate-300 border-white/10'
                            }`}
                          >
                            <ThumbsDown className="w-3 h-3" />
                            Confirm Threat
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

      {/* Deep Inspection Modal */}
      {selectedInspectMsg && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="glass-panel max-w-2xl w-full p-6 space-y-5 border-cyan-500/50 shadow-2xl relative max-h-[90vh] overflow-y-auto">
            <button
              onClick={() => setSelectedInspectMsg(null)}
              className="absolute top-4 right-4 p-2 text-slate-400 hover:text-white rounded-lg hover:bg-white/10 transition-colors"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-2 text-cyan-400 text-xs font-semibold uppercase tracking-wider">
              <Sparkles className="w-4 h-4" /> Explainable AI (XAI) Deep Event Inspection
            </div>

            <div className="flex items-center justify-between border-b border-white/10 pb-3">
              <div>
                <h3 className="text-lg font-bold text-white">Threat Telemetry & Attribution</h3>
                <p className="text-xs text-slate-400 font-mono">Event ID: {selectedInspectMsg.id}</p>
              </div>
              <div>{getRiskBadge(selectedInspectMsg.risk_level, selectedInspectMsg.risk_score)}</div>
            </div>

            <div className="space-y-2">
              <span className="text-xs text-slate-400 uppercase font-semibold">Raw Intercepted Payload:</span>
              <div className="p-3 bg-slate-950 rounded-lg font-mono text-xs text-slate-200 border border-white/10">
                "{selectedInspectMsg.text}"
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs font-mono">
              <div className="p-3 bg-slate-950/60 rounded-lg border border-white/5">
                <span className="text-slate-400 block text-[11px]">Sender:</span>
                <span className="text-cyan-300 font-bold">{selectedInspectMsg.sender}</span>
              </div>
              <div className="p-3 bg-slate-950/60 rounded-lg border border-white/5">
                <span className="text-slate-400 block text-[11px]">Source Android App:</span>
                <span className="text-slate-200">{selectedInspectMsg.source_app}</span>
              </div>
            </div>

            <div className="space-y-2">
              <span className="text-xs text-slate-400 uppercase font-semibold">Detected Risk Factors & Rationale:</span>
              <div className="space-y-1.5">
                {selectedInspectMsg.reasons.map((reason, idx) => (
                  <div key={idx} className="p-2.5 bg-slate-950/70 rounded-lg border border-white/5 text-xs text-slate-200 flex items-start gap-2">
                    <span className="text-rose-400 font-bold">•</span>
                    <span>{reason}</span>
                  </div>
                ))}
              </div>
            </div>

            <button
              onClick={() => setSelectedInspectMsg(null)}
              className="btn-primary w-full justify-center text-xs"
            >
              Close Deep Inspector
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
