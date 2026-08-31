import React, { useState } from 'react';
import { Mail, Search, ShieldAlert, ShieldCheck, AlertTriangle } from 'lucide-react';
import { scanEmail } from '../services/api';
import { EmailScanResult } from '../types';

export const EmailScanner: React.FC = () => {
  const [sender, setSender] = useState('security-update@paypal.notice-billing.com');
  const [subject, setSubject] = useState('URGENT: Unauthorized Transaction Detected - Account Frozen');
  const [body, setBody] = useState(
    'Dear Customer, we detected suspicious activity. Please verify your billing details at http://paypal-verify-billing.xyz/login within 24 hours to prevent permanent account closure.'
  );
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<EmailScanResult | null>(null);

  const handleScan = async () => {
    setIsLoading(true);
    try {
      const res = await scanEmail(sender, subject, body);
      setResult(res);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="glass-panel p-6">
      <div className="mb-6">
        <h2 className="text-lg font-bold text-white flex items-center gap-2">
          <Mail className="w-5 h-5 text-cyan-400" />
          Email Phishing & BEC Analyzer
        </h2>
        <p className="text-xs text-slate-400 mt-0.5">
          Analyzes header sender spoofing, invoice fraud lures, urgency triggers, and embedded credential harvesters.
        </p>
      </div>

      <div className="space-y-4 mb-6">
        <div>
          <label className="text-xs font-semibold text-slate-300 mb-1.5 block">Sender Header</label>
          <input
            type="text"
            value={sender}
            onChange={(e) => setSender(e.target.value)}
            className="input-dark text-xs"
          />
        </div>
        <div>
          <label className="text-xs font-semibold text-slate-300 mb-1.5 block">Subject Line</label>
          <input
            type="text"
            value={subject}
            onChange={(e) => setSubject(e.target.value)}
            className="input-dark text-xs"
          />
        </div>
        <div>
          <label className="text-xs font-semibold text-slate-300 mb-1.5 block">Email Body</label>
          <textarea
            rows={4}
            value={body}
            onChange={(e) => setBody(e.target.value)}
            className="input-dark text-xs font-mono"
          />
        </div>

        <button onClick={handleScan} disabled={isLoading} className="btn-primary w-full justify-center py-3">
          <Search className="w-4 h-4" />
          {isLoading ? 'Analyzing Email...' : 'Run Phishing & BEC Scan'}
        </button>
      </div>

      {result && (
        <div
          className={`p-5 rounded-xl border ${
            result.risk_score >= 60
              ? 'bg-red-500/10 border-red-500/40'
              : result.risk_score >= 35
              ? 'bg-amber-500/10 border-amber-500/40'
              : 'bg-emerald-500/10 border-emerald-500/40'
          }`}
        >
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              {result.risk_score >= 60 ? (
                <ShieldAlert className="w-6 h-6 text-red-400" />
              ) : result.risk_score >= 35 ? (
                <AlertTriangle className="w-6 h-6 text-amber-400" />
              ) : (
                <ShieldCheck className="w-6 h-6 text-emerald-400" />
              )}
              <div>
                <h3 className="text-base font-bold text-white">{result.prediction}</h3>
                <p className="text-xs text-slate-400">Action: {result.recommended_action}</p>
              </div>
            </div>
            <div className="text-right">
              <div className="text-2xl font-bold font-mono text-white">{result.risk_score}%</div>
              <div className="text-[10px] font-semibold text-slate-400 uppercase">{result.risk_level} RISK</div>
            </div>
          </div>

          <div className="space-y-1.5">
            <div className="text-xs font-bold text-slate-300">Flags & Detection Reasons:</div>
            {result.reasons.map((r, i) => (
              <div key={i} className="text-xs text-slate-300 flex items-start gap-2">
                <span className="text-cyan-400 font-bold">•</span>
                <span>{r}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
