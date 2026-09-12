import React, { useState, useEffect } from 'react';
import { 
  FileText, 
  Copy, 
  Check, 
  Download, 
  BookOpen, 
  Code2, 
  ExternalLink,
  Layers,
  Sparkles
} from 'lucide-react';
import { fetchLatexExports } from '../services/api';
import { LatexExportResponse } from '../types';

export const PublicationHub: React.FC = () => {
  const [latexData, setLatexData] = useState<LatexExportResponse | null>(null);
  const [copiedKey, setCopiedKey] = useState<string | null>(null);

  useEffect(() => {
    fetchLatexExports()
      .then(setLatexData)
      .catch(console.error);
  }, []);

  const handleCopy = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const handleDownloadTelemetry = (format: 'json' | 'csv') => {
    window.open(`http://localhost:8000/api/v1/research/export/telemetry?format=${format}`, '_blank');
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="glass-panel p-6 border-l-4 border-purple-500 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-purple-400 font-semibold text-sm uppercase tracking-wider">
            <BookOpen className="w-4 h-4" /> Academic Research & Publication Hub
          </div>
          <h1 className="text-2xl font-bold mt-1">Paper Artifacts, LaTeX Exporters & Datasets</h1>
          <p className="text-sm text-slate-400 mt-1">
            Pre-formatted LaTeX tables, BibTeX citations, mathematical formulations, and downloadable telemetry data for research papers.
          </p>
        </div>

        {/* Telemetry Dataset Downloads */}
        <div className="flex flex-wrap gap-2 self-start md:self-auto">
          <button 
            onClick={() => handleDownloadTelemetry('csv')}
            className="btn-primary text-xs"
          >
            <Download className="w-4 h-4" /> Download Telemetry CSV
          </button>
          <button 
            onClick={() => handleDownloadTelemetry('json')}
            className="btn-secondary text-xs"
          >
            <Download className="w-4 h-4 text-cyan-400" /> Export JSON
          </button>
        </div>
      </div>

      {/* Abstract & Mathematical Formulation */}
      <div className="glass-panel p-6 space-y-4">
        <div className="flex items-center gap-2 text-cyan-400 text-xs font-semibold uppercase tracking-wider">
          <Sparkles className="w-4 h-4" /> Research Abstract & Mathematical Risk Formulation
        </div>
        <h2 className="text-lg font-bold">System Methodology & Threat Attribution</h2>
        <p className="text-xs text-slate-300 leading-relaxed">
          <strong>Abstract:</strong> Smishing (SMS phishing) poses severe risks in modern mobile operating systems due to limited screen viewport real estate, urgent social engineering psychological coercion, and the rapid propagation of obfuscated URLs. We present <strong>PhishGuard</strong>, a zero-click, real-time distributed defense architecture that pairs an on-device Android OS listener (<code className="font-mono text-cyan-300 text-[11px]">NotificationListenerService</code>) executing sub-5ms local heuristics with an asynchronous cloud risk evaluation engine executing 30+ lexical topological extractors, Shannon entropy calculation, Levenshtein brand typosquatting, and TF-IDF NLP embeddings.
        </p>

        {/* Math Block */}
        <div className="bg-slate-950/60 p-4 rounded-xl border border-white/5 space-y-2">
          <div className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold">
            Unified Risk Scoring Function:
          </div>
          <div className="font-mono text-xs text-cyan-300 overflow-x-auto py-1">
            R(m, u) = min(100, max(0, w_lex * F_lex(u) + w_nlp * F_nlp(m) + w_brand * F_brand(u) + w_urg * F_urg(m) - M_benign))
          </div>
          <div className="text-[11px] text-slate-400">
            Where <code className="text-slate-300">F_lex</code> captures Shannon entropy ($H = -\sum p_i \log_2 p_i$), IP host presence, and TLD abuse; <code className="text-slate-300">F_brand</code> calculates Levenshtein string edit distance against top targeted domains; and <code className="text-slate-300">M_benign</code> suppresses false positives on legitimate 2FA OTP codes.
          </div>
        </div>
      </div>

      {/* LaTeX Table 1: Benchmark Comparison */}
      <div className="glass-panel p-6 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-white/10 pb-3">
          <div className="flex items-center gap-2">
            <Code2 className="w-4 h-4 text-cyan-400" />
            <h3 className="text-sm font-bold text-slate-200">
              LaTeX Code: Table 1 - Empirical Benchmark Performance (IEEE/ACM Format)
            </h3>
          </div>
          <button
            onClick={() => latexData && handleCopy(latexData.benchmark_table, 'table1')}
            className="btn-secondary text-xs self-start sm:self-auto"
          >
            {copiedKey === 'table1' ? (
              <>
                <Check className="w-3.5 h-3.5 text-emerald-400" /> Copied to Clipboard!
              </>
            ) : (
              <>
                <Copy className="w-3.5 h-3.5 text-cyan-400" /> Copy LaTeX Table 1
              </>
            )}
          </button>
        </div>

        <pre className="code-block-dark text-xs">
          <code>{latexData?.benchmark_table || '% Loading LaTeX table...'}</code>
        </pre>
      </div>

      {/* LaTeX Table 2: Ablation Study */}
      <div className="glass-panel p-6 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-white/10 pb-3">
          <div className="flex items-center gap-2">
            <Layers className="w-4 h-4 text-purple-400" />
            <h3 className="text-sm font-bold text-slate-200">
              LaTeX Code: Table 2 - Ablation Analysis of Defense Layers
            </h3>
          </div>
          <button
            onClick={() => latexData && handleCopy(latexData.ablation_table, 'table2')}
            className="btn-secondary text-xs self-start sm:self-auto"
          >
            {copiedKey === 'table2' ? (
              <>
                <Check className="w-3.5 h-3.5 text-emerald-400" /> Copied to Clipboard!
              </>
            ) : (
              <>
                <Copy className="w-3.5 h-3.5 text-purple-400" /> Copy LaTeX Table 2
              </>
            )}
          </button>
        </div>

        <pre className="code-block-dark text-xs">
          <code>{latexData?.ablation_table || '% Loading Ablation table...'}</code>
        </pre>
      </div>

      {/* BibTeX Citation Snippet */}
      <div className="glass-panel p-6 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-white/10 pb-3">
          <div className="flex items-center gap-2">
            <FileText className="w-4 h-4 text-amber-400" />
            <h3 className="text-sm font-bold text-slate-200">BibTeX Citation Reference</h3>
          </div>
          <button
            onClick={() => latexData && handleCopy(latexData.bibtex_citation, 'bibtex')}
            className="btn-secondary text-xs self-start sm:self-auto"
          >
            {copiedKey === 'bibtex' ? (
              <>
                <Check className="w-3.5 h-3.5 text-emerald-400" /> Copied to Clipboard!
              </>
            ) : (
              <>
                <Copy className="w-3.5 h-3.5 text-amber-400" /> Copy BibTeX
              </>
            )}
          </button>
        </div>

        <pre className="code-block-dark text-xs">
          <code>{latexData?.bibtex_citation || '% Loading BibTeX...'}</code>
        </pre>
      </div>
    </div>
  );
};
