import React, { useState } from 'react';
import { Globe, Search, ShieldAlert, ShieldCheck, AlertTriangle, Layers, Zap } from 'lucide-react';
import { scanUrl } from '../services/api';
import { UrlScanResult } from '../types';

export const UrlScanner: React.FC = () => {
  const [url, setUrl] = useState('http://paypal-security-update.xyz/login.php');
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<UrlScanResult | null>(null);

  const presets = [
    'http://paypal-security-update.xyz/login.php',
    'http://192.168.1.105/usps/redeliver',
    'http://netflix-billing-update.top/account',
    'https://accounts.google.com/signin'
  ];

  const handleScan = async () => {
    if (!url.trim()) return;
    setIsLoading(true);
    try {
      const res = await scanUrl(url);
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
          <Globe className="w-5 h-5 text-cyan-400" />
          Deep URL Lexical & Domain Analyzer
        </h2>
        <p className="text-xs text-slate-400 mt-0.5">
          Extracts 30+ lexical, structural, Shannon entropy, and brand typosquatting indicators.
        </p>
      </div>

      {/* Preset bar */}
      <div className="flex flex-wrap gap-2 mb-4">
        {presets.map((p, idx) => (
          <button
            key={idx}
            onClick={() => setUrl(p)}
            className="text-[11px] px-3 py-1.5 rounded-lg bg-slate-900/60 hover:bg-slate-800 border border-white/5 text-slate-300 font-mono transition-colors"
          >
            {p}
          </button>
        ))}
      </div>

      {/* Input */}
      <div className="flex gap-3 mb-6">
        <input
          type="text"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          placeholder="https://suspicious-website.com/login"
          className="input-dark font-mono text-xs flex-1"
        />
        <button
          onClick={handleScan}
          disabled={isLoading || !url.trim()}
          className="btn-primary shrink-0"
        >
          <Search className="w-4 h-4" />
          {isLoading ? 'Analyzing...' : 'Scan URL'}
        </button>
      </div>

      {/* Scan Results */}
      {result && (
        <div className="space-y-6">
          {/* Main Risk Gauge Banner */}
          <div
            className={`p-5 rounded-xl border ${
              result.risk_score >= 60
                ? 'bg-red-500/10 border-red-500/40'
                : result.risk_score >= 35
                ? 'bg-amber-500/10 border-amber-500/40'
                : 'bg-emerald-500/10 border-emerald-500/40'
            }`}
          >
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div
                  className={`p-3 rounded-xl ${
                    result.risk_score >= 60
                      ? 'bg-red-500/20 text-red-400'
                      : result.risk_score >= 35
                      ? 'bg-amber-500/20 text-amber-400'
                      : 'bg-emerald-500/20 text-emerald-400'
                  }`}
                >
                  {result.risk_score >= 60 ? (
                    <ShieldAlert className="w-6 h-6" />
                  ) : result.risk_score >= 35 ? (
                    <AlertTriangle className="w-6 h-6" />
                  ) : (
                    <ShieldCheck className="w-6 h-6" />
                  )}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xl font-bold text-white">{result.prediction}</span>
                    <span className="text-xs px-2 py-0.5 rounded bg-black/40 text-slate-300 font-mono">
                      Action: {result.recommended_action}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 mt-1 font-mono truncate max-w-lg">{result.url}</p>
                </div>
              </div>

              <div className="text-right">
                <div className="text-3xl font-black font-mono text-white">{result.risk_score}%</div>
                <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">{result.risk_level} RISK</div>
              </div>
            </div>
          </div>

          {/* Lexical Features Grid */}
          <div>
            <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-3 flex items-center gap-2">
              <Layers className="w-4 h-4 text-cyan-400" />
              Extracted Lexical & Domain Feature Vector:
            </h3>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2.5">
              {Object.entries(result.features).map(([key, val], idx) => (
                <div key={idx} className="p-3 rounded-lg bg-slate-900/50 border border-white/5">
                  <div className="text-[11px] text-slate-400 truncate">{key}</div>
                  <div className="text-xs font-mono font-bold text-cyan-300 mt-0.5 truncate">
                    {typeof val === 'boolean' ? (val ? 'TRUE' : 'FALSE') : String(val ?? 'None')}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Explainability Breakdown */}
          <div>
            <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-3 flex items-center gap-2">
              <Zap className="w-4 h-4 text-amber-400" />
              Explainability & Risk Factor Contributions:
            </h3>
            <div className="space-y-2">
              {result.reasons.map((r, i) => (
                <div key={i} className="p-3 rounded-lg bg-slate-900/40 border border-white/5 flex items-start gap-2.5 text-xs text-slate-200">
                  <span className="text-cyan-400 font-bold">•</span>
                  <span>{r}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
