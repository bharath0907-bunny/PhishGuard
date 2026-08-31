import React, { useState } from 'react';
import { Radio, Send, Play, CheckCircle2, AlertOctagon, Sparkles } from 'lucide-react';
import { simulateGoogleMessage } from '../services/api';

interface GoogleMessageSimulatorProps {
  onMessageSent: () => void;
}

export const GoogleMessageSimulator: React.FC<GoogleMessageSimulatorProps> = ({ onMessageSent }) => {
  const [sender, setSender] = useState('+1 (800) 555-0199');
  const [text, setText] = useState(
    '[CHASE-ALERT] We detected an unauthorized transaction of $940.00 on your debit card. If this wasn\'t you, verify identity immediately: http://chase-security-auth.xyz/verify'
  );
  const [isLoading, setIsLoading] = useState(false);
  const [latestResult, setLatestResult] = useState<any>(null);

  const presets = [
    {
      label: 'Chase Bank Smishing',
      sender: '+1 (800) 555-0199',
      text: '[CHASE-ALERT] Unauthorized transaction of $940.00 detected on your debit card. Verify identity immediately: http://chase-security-auth.xyz/verify'
    },
    {
      label: 'USPS Redelivery Scam',
      sender: 'USPS-TRACKING',
      text: 'USPS: Package #US9482710 cannot be delivered due to missing house number. Update delivery address within 12 hours: http://192.168.1.105/usps/redeliver'
    },
    {
      label: 'Netflix Billing Suspension',
      sender: 'NETFLIX-ALERT',
      text: 'Your Netflix account is locked due to an expired payment method. Update payment immediately to avoid deletion: http://netflix-billing-update.top/account'
    },
    {
      label: 'Legitimate 2FA Code',
      sender: 'Google',
      text: 'G-492810 is your Google verification code. Do not share this code with anyone.'
    }
  ];

  const handleSimulate = async () => {
    setIsLoading(true);
    try {
      const result = await simulateGoogleMessage(sender, text);
      setLatestResult(result);
      onMessageSent();
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="glass-panel p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <Radio className="w-5 h-5 text-cyan-400" />
            Google Messages Smishing Simulation Studio
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Test and trigger simulated Google Messages notifications to benchmark real-time AI classification & alert dispatches.
          </p>
        </div>
      </div>

      {/* Preset Buttons */}
      <div className="mb-6">
        <label className="text-xs font-semibold text-slate-300 mb-2 block flex items-center gap-1.5">
          <Sparkles className="w-3.5 h-3.5 text-amber-400" />
          1-Click Attack & Safe Templates:
        </label>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2.5">
          {presets.map((p, idx) => (
            <button
              key={idx}
              onClick={() => {
                setSender(p.sender);
                setText(p.text);
              }}
              className="p-3 rounded-xl bg-slate-900/60 hover:bg-slate-800/80 border border-white/5 hover:border-cyan-500/30 text-left transition-all group"
            >
              <div className="text-xs font-bold text-slate-200 group-hover:text-cyan-400">{p.label}</div>
              <div className="text-[11px] text-slate-400 truncate mt-1">{p.sender}</div>
            </button>
          ))}
        </div>
      </div>

      {/* Input Form */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
        <div>
          <label className="text-xs font-semibold text-slate-300 mb-1.5 block">Sender (Phone / Alphanumeric Title)</label>
          <input
            type="text"
            value={sender}
            onChange={(e) => setSender(e.target.value)}
            placeholder="+18005550199 or WELLS FARGO"
            className="input-dark"
          />
        </div>
        <div className="md:col-span-2">
          <label className="text-xs font-semibold text-slate-300 mb-1.5 block">Target Android Package</label>
          <input
            type="text"
            disabled
            value="com.google.android.apps.messaging (Google Messages Hook)"
            className="input-dark text-slate-500 bg-slate-950/60 cursor-not-allowed"
          />
        </div>
      </div>

      <div className="mb-5">
        <label className="text-xs font-semibold text-slate-300 mb-1.5 block">SMS Notification Text Body</label>
        <textarea
          rows={3}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Paste or type SMS content..."
          className="input-dark font-mono text-xs"
        />
      </div>

      <button
        onClick={handleSimulate}
        disabled={isLoading || !text.trim()}
        className="btn-primary w-full justify-center py-3"
      >
        <Play className="w-4 h-4" />
        {isLoading ? 'Processing via Real-Time AI Engine...' : 'Simulate Google Message Arrival'}
      </button>

      {/* Instant Result Box */}
      {latestResult && (
        <div className={`mt-6 p-5 rounded-xl border ${latestResult.risk_score >= 60 ? 'bg-red-500/10 border-red-500/40' : latestResult.risk_score >= 35 ? 'bg-amber-500/10 border-amber-500/40' : 'bg-emerald-500/10 border-emerald-500/40'}`}>
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-2">
              {latestResult.risk_score >= 60 ? (
                <AlertOctagon className="w-5 h-5 text-red-400" />
              ) : (
                <CheckCircle2 className="w-5 h-5 text-emerald-400" />
              )}
              <h3 className="font-bold text-sm text-white">
                Interception Result: {latestResult.prediction} ({latestResult.risk_score}% Risk)
              </h3>
            </div>
            <span className="text-xs font-mono px-2.5 py-1 rounded bg-black/40 text-slate-200">
              Action: {latestResult.recommended_action}
            </span>
          </div>

          <p className="text-xs text-slate-300 mb-3">
            <strong>Heads-Up Alert Status:</strong> {latestResult.should_alert ? '🚨 Real-Time Warning Alert Dispatched to Android Notification Channel' : '✅ Allowed Safely to Inbox'}
          </p>

          <div className="space-y-1">
            <div className="text-[11px] font-bold text-slate-400 uppercase">Detection Rationale:</div>
            {latestResult.reasons?.map((r: string, idx: number) => (
              <div key={idx} className="text-xs text-slate-200 flex items-start gap-2">
                <span className="text-cyan-400">•</span>
                <span>{r}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
