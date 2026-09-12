import React, { useState } from 'react';
import { 
  Zap, 
  ShieldAlert, 
  ShieldCheck, 
  Flame, 
  Sparkles, 
  Send, 
  Check, 
  AlertCircle, 
  Eye, 
  Bug, 
  Copy,
  RefreshCw
} from 'lucide-react';
import { simulateGoogleMessage, runAdversarialTest } from '../services/api';
import { AdversarialTestResponse } from '../types';

const ATTACK_VECTORS = [
  {
    category: 'Financial / Banking Wire Fraud',
    sender: '+1 (800) 555-0199',
    text: '[CHASE-SECURITY] Unauthorized wire transfer of $1,250.00 detected on your debit card. Cancel transaction immediately: http://chase-security-auth.xyz/verify',
    type: 'HIGH_PRESSURE_FINANCIAL'
  },
  {
    category: 'Package Delivery & Redirection Lure',
    sender: 'USPS-NOTICES',
    text: 'USPS: Package #US94821 cannot be dispatched due to invalid street address. Update delivery address within 12 hours: http://192.168.1.102/usps/redeliver',
    type: 'PARCEL_COURIER'
  },
  {
    category: 'Account Suspension & Credential Trap',
    sender: 'NETFLIX-ALERT',
    text: 'Your Netflix subscription has been placed on hold due to a billing error. Update your payment method: http://netflix-billing-update.top/account',
    type: 'SUBSCRIPTION_SPOOF'
  },
  {
    category: 'Apple ID / iCloud Security Lockdown',
    sender: 'AppleSupport',
    text: 'Apple ID Security: Your account was accessed from an unknown device in Moscow. Reactivate your iCloud account: http://appleid-unlock-apple.com',
    type: 'ACCOUNT_TAKEOVER'
  },
  {
    category: 'Legitimate 2FA OTP Code (Benign Baseline)',
    sender: 'Google',
    text: 'G-492810 is your Google verification code. Do not share this code with anyone.',
    type: 'BENIGN_OTP'
  },
  {
    category: 'Interpersonal Social Chat (Benign Baseline)',
    sender: '+1 (415) 889-1022',
    text: 'Hey John, are we still meeting for lunch tomorrow at 12:30 PM at the diner?',
    type: 'BENIGN_CHAT'
  }
];

interface Props {
  onMessageSent?: () => void;
}

export const AdversarialAttackStudio: React.FC<Props> = ({ onMessageSent }) => {
  const [sender, setSender] = useState<string>('+1 (800) 555-0199');
  const [text, setText] = useState<string>(
    '[CHASE-ALERT] Unauthorized wire transfer of $940.00 detected. Verify immediately at http://chase-security-auth.xyz/verify'
  );
  const [isInjecting, setIsInjecting] = useState<boolean>(false);
  const [statusMessage, setStatusMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Adversarial Evaluation State
  const [adversarialResult, setAdversarialResult] = useState<AdversarialTestResponse | null>(null);
  const [isTestingAdversarial, setIsTestingAdversarial] = useState<boolean>(false);
  const [copiedIndex, setCopiedIndex] = useState<number | null>(null);

  const handleSelectVector = (vec: typeof ATTACK_VECTORS[0]) => {
    setSender(vec.sender);
    setText(vec.text);
    setStatusMessage(null);
    setAdversarialResult(null);
  };

  const handleDispatchLive = async () => {
    if (!text.trim()) return;
    setIsInjecting(true);
    setStatusMessage(null);
    try {
      const res = await simulateGoogleMessage(sender, text);
      setStatusMessage({
        type: 'success',
        text: `Intercepted & Processed! Risk Score: ${res.risk_score} (${res.risk_level}) -> Broadcasted to Live Stream.`
      });
      if (onMessageSent) onMessageSent();
    } catch (e: any) {
      setStatusMessage({ type: 'error', text: 'Dispatch failed: ' + (e.message || 'Check backend connection') });
    } finally {
      setIsInjecting(false);
    }
  };

  const handleRunAdversarialRobustness = async () => {
    if (!text.trim()) return;
    setIsTestingAdversarial(true);
    setStatusMessage(null);
    try {
      const res = await runAdversarialTest(text);
      setAdversarialResult(res);
    } catch (e: any) {
      setStatusMessage({ type: 'error', text: 'Adversarial evaluation failed: ' + e.message });
    } finally {
      setIsTestingAdversarial(false);
    }
  };

  const handleCopy = (content: string, idx: number) => {
    navigator.clipboard.writeText(content);
    setCopiedIndex(idx);
    setTimeout(() => setCopiedIndex(null), 2000);
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="glass-panel p-6 border-l-4 border-amber-500 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-amber-400 font-semibold text-sm uppercase tracking-wider">
            <Flame className="w-4 h-4" /> Smishing Attack Vector & Adversarial Studio
          </div>
          <h1 className="text-2xl font-bold mt-1">Attack Simulator & Evasion Defense Lab</h1>
          <p className="text-sm text-slate-400 mt-1">
            Dispatch zero-click mobile smishing payloads and evaluate defense resilience against adversarial perturbations.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Preset Attack Vectors */}
        <div className="glass-panel p-5 space-y-4">
          <div className="flex items-center justify-between border-b border-white/10 pb-3">
            <h2 className="text-sm font-bold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-cyan-400" /> Pre-Loaded Attack Vectors
            </h2>
            <span className="text-[11px] text-slate-400 font-mono">1-Click Load</span>
          </div>

          <div className="space-y-2.5">
            {ATTACK_VECTORS.map((vec, idx) => {
              const isBenign = vec.type.startsWith('BENIGN');
              return (
                <button
                  key={idx}
                  onClick={() => handleSelectVector(vec)}
                  className="w-full text-left p-3 rounded-xl bg-slate-950/40 hover:bg-slate-900 border border-white/5 hover:border-cyan-500/40 transition-all text-xs group"
                >
                  <div className="flex items-center justify-between">
                    <span className={`font-semibold ${isBenign ? 'text-emerald-400' : 'text-amber-400'}`}>
                      {vec.category}
                    </span>
                    <span className="text-[10px] text-slate-500 font-mono">{vec.sender}</span>
                  </div>
                  <p className="text-slate-400 mt-1.5 line-clamp-2 text-[11px] group-hover:text-slate-200 transition-colors">
                    {vec.text}
                  </p>
                </button>
              );
            })}
          </div>
        </div>

        {/* Right 2 Columns: Live Payload Composer & Actions */}
        <div className="lg:col-span-2 space-y-5">
          <div className="glass-panel p-6 space-y-4">
            <h2 className="text-sm font-bold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <Zap className="w-4 h-4 text-amber-400" /> Interactive Smishing Payload Composer
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-semibold text-slate-400 block mb-1">Sender ID / Phone Number</label>
                <input
                  type="text"
                  value={sender}
                  onChange={(e) => setSender(e.target.value)}
                  placeholder="+1 (800) 555-0199 or USPS-ALERT"
                  className="input-dark font-mono text-xs"
                />
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-400 block mb-1">Target Package / Channel</label>
                <div className="input-dark bg-slate-950/80 text-slate-400 font-mono text-xs flex items-center justify-between">
                  <span>com.google.android.apps.messaging</span>
                  <span className="text-cyan-400 text-[10px] font-sans font-bold">LOCKED</span>
                </div>
              </div>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-400 block mb-1">Message Content / Smishing Payload</label>
              <textarea
                value={text}
                onChange={(e) => setText(e.target.value)}
                rows={4}
                placeholder="Enter SMS text with link or lure..."
                className="input-dark font-mono text-xs resize-none"
              />
            </div>

            {/* Status Message */}
            {statusMessage && (
              <div
                className={`p-3 rounded-xl text-xs flex items-center gap-2 ${
                  statusMessage.type === 'success'
                    ? 'bg-emerald-500/15 border border-emerald-500/30 text-emerald-300'
                    : 'bg-rose-500/15 border border-rose-500/30 text-rose-300'
                }`}
              >
                {statusMessage.type === 'success' ? (
                  <Check className="w-4 h-4 text-emerald-400 flex-shrink-0" />
                ) : (
                  <AlertCircle className="w-4 h-4 text-rose-400 flex-shrink-0" />
                )}
                <span>{statusMessage.text}</span>
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex flex-wrap items-center gap-3 pt-2">
              <button
                onClick={handleDispatchLive}
                disabled={isInjecting || !text.trim()}
                className="btn-primary"
              >
                <Send className={`w-4 h-4 ${isInjecting ? 'animate-spin' : ''}`} />
                {isInjecting ? 'Broadcasting Event...' : 'Dispatch to Live Intercept Stream'}
              </button>

              <button
                onClick={handleRunAdversarialRobustness}
                disabled={isTestingAdversarial || !text.trim()}
                className="btn-secondary"
              >
                <Bug className={`w-4 h-4 text-amber-400 ${isTestingAdversarial ? 'animate-spin' : ''}`} />
                {isTestingAdversarial ? 'Testing Evasion...' : 'Run Adversarial Robustness Test'}
              </button>
            </div>
          </div>

          {/* Adversarial Robustness Evaluation Breakdown */}
          {adversarialResult && (
            <div className="glass-panel p-6 space-y-5 border-l-4 border-cyan-500">
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-2 border-b border-white/10 pb-4">
                <div>
                  <div className="flex items-center gap-2 text-cyan-400 text-xs font-semibold uppercase tracking-wider">
                    <ShieldCheck className="w-4 h-4" /> Adversarial Evasion Robustness Report
                  </div>
                  <h3 className="text-lg font-bold mt-1">Multi-Vector Defense Resilience</h3>
                </div>
                <div className="px-3 py-1.5 bg-emerald-500/20 border border-emerald-500/40 rounded-xl text-emerald-300 font-mono font-bold text-xs self-start md:self-auto">
                  {adversarialResult.robustness_score}
                </div>
              </div>

              {/* Baseline Evaluation */}
              <div className="p-4 rounded-xl bg-slate-950/50 border border-white/5 space-y-2">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-semibold text-slate-300 uppercase tracking-wider">Baseline Input Evaluation</span>
                  <span className="font-mono font-bold text-rose-400">
                    Risk Score: {adversarialResult.baseline.risk_score} ({adversarialResult.baseline.risk_level})
                  </span>
                </div>
                <p className="text-xs font-mono text-slate-300 bg-slate-950/80 p-2.5 rounded-lg">
                  {adversarialResult.baseline.text}
                </p>
              </div>

              {/* Mutated Perturbations List */}
              <div className="space-y-3">
                <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                  Adversarial Mutation Techniques Applied:
                </h4>

                {adversarialResult.adversarial_evaluations.map((mut, idx) => {
                  const isDefended = mut.defense_status.startsWith('DEFENDED');
                  return (
                    <div
                      key={idx}
                      className={`p-4 rounded-xl border transition-all space-y-2.5 ${
                        isDefended
                          ? 'bg-emerald-500/10 border-emerald-500/30'
                          : 'bg-rose-500/10 border-rose-500/30'
                      }`}
                    >
                      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                        <div>
                          <div className="text-xs font-bold text-slate-200 flex items-center gap-2">
                            {isDefended ? (
                              <ShieldCheck className="w-4 h-4 text-emerald-400" />
                            ) : (
                              <ShieldAlert className="w-4 h-4 text-rose-400" />
                            )}
                            {mut.technique}
                          </div>
                          <p className="text-[11px] text-slate-400 mt-0.5">{mut.description}</p>
                        </div>
                        <div className="flex items-center gap-2 font-mono text-xs self-start sm:self-auto">
                          <span className={`px-2 py-1 rounded-lg font-bold text-[11px] ${
                            isDefended ? 'bg-emerald-500/20 text-emerald-300' : 'bg-rose-500/20 text-rose-300'
                          }`}>
                            {mut.defense_status}
                          </span>
                          <span className="text-slate-300">Risk: {mut.risk_score}</span>
                        </div>
                      </div>

                      {/* Perturbed Text with Copy button */}
                      <div className="flex items-center justify-between gap-2 p-2.5 bg-slate-950/80 rounded-lg text-xs font-mono text-slate-300">
                        <span className="truncate">{mut.perturbed_text}</span>
                        <button
                          onClick={() => handleCopy(mut.perturbed_text, idx)}
                          className="p-1 hover:bg-white/10 rounded text-slate-400 hover:text-slate-200 transition-colors flex-shrink-0"
                          title="Copy perturbed payload"
                        >
                          {copiedIndex === idx ? (
                            <Check className="w-3.5 h-3.5 text-emerald-400" />
                          ) : (
                            <Copy className="w-3.5 h-3.5" />
                          )}
                        </button>
                      </div>

                      {/* XAI Detected Reasons */}
                      {mut.reasons && mut.reasons.length > 0 && (
                        <div className="text-[11px] text-slate-400 flex flex-wrap gap-1.5 pt-1">
                          {mut.reasons.map((r, rIdx) => (
                            <span key={rIdx} className="px-2 py-0.5 bg-white/5 rounded text-slate-300">
                              • {r}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
