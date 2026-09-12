import React, { useState } from 'react';
import { 
  FileSpreadsheet, 
  Play, 
  Upload, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  Sparkles, 
  Download, 
  RefreshCw,
  AlertTriangle
} from 'lucide-react';
import { runBatchEvaluation } from '../services/api';
import { BatchEvaluateResponse, BatchItem } from '../types';

const SAMPLE_DATASETS: Record<string, BatchItem[]> = {
  smishing_benchmark: [
    { type: 'SMS', sender: '+18005550199', text: '[CHASE-ALERT] Unauthorized wire transfer of $980.00. Verify immediately at http://chase-security-auth.xyz/login', ground_truth: 'SMISHING' },
    { type: 'SMS', sender: 'USPS-TRACKING', text: 'USPS: Your package #US9482710 is on hold due to missing address. Update here: http://192.168.1.105/usps/redeliver', ground_truth: 'SMISHING' },
    { type: 'SMS', sender: 'NETFLIX', text: 'Your Netflix membership has been suspended due to billing error. Update payment: http://netflix-billing-update.top/account', ground_truth: 'SMISHING' },
    { type: 'SMS', sender: 'Google', text: 'G-492810 is your Google verification code. Do not share this code with anyone.', ground_truth: 'BENIGN' },
    { type: 'SMS', sender: '+1 (415) 889-1022', text: 'Hey John, are we still meeting for lunch tomorrow at 12:30 PM at the diner?', ground_truth: 'BENIGN' },
    { type: 'SMS', sender: 'IRS-REFUND', text: 'IRS Alert: You have an unclaimed tax refund of $1,420. Claim direct deposit: http://irs-tax-refund-gov.xyz/form', ground_truth: 'SMISHING' },
    { type: 'SMS', sender: 'Amazon', text: 'Your order #112-9482910 has been delivered to your front porch. Thank you for shopping.', ground_truth: 'BENIGN' },
    { type: 'SMS', sender: 'Apple Support', text: 'Your Apple ID has been locked for security reasons. Reactivate iCloud: http://appleid-unlock-apple.com', ground_truth: 'SMISHING' },
    { type: 'SMS', sender: 'Uber', text: 'Uber: 9482 is your login security code. Never share your code with anyone.', ground_truth: 'BENIGN' },
    { type: 'SMS', sender: 'Wells Fargo', text: 'Your Wells Fargo security code is 193820. Call us if you did not request this.', ground_truth: 'BENIGN' }
  ],
  url_benchmark: [
    { type: 'URL', text: 'http://secure-paypal-login.xyz/update-wallet', ground_truth: 'PHISHING' },
    { type: 'URL', text: 'http://wellsfargo-online-fraud-verification.biz/login.php', ground_truth: 'PHISHING' },
    { type: 'URL', text: 'https://accounts.google.com/signin/v2/identifier', ground_truth: 'BENIGN' },
    { type: 'URL', text: 'http://netflix-billing-renewal.top/account', ground_truth: 'PHISHING' },
    { type: 'URL', text: 'https://www.amazon.com/gp/css/order-history', ground_truth: 'BENIGN' },
    { type: 'URL', text: 'http://192.168.1.100/chase/login', ground_truth: 'PHISHING' },
    { type: 'URL', text: 'https://github.com/login', ground_truth: 'BENIGN' },
    { type: 'URL', text: 'http://appleid-security-validation.info/auth', ground_truth: 'PHISHING' }
  ]
};

export const BatchEvaluator: React.FC = () => {
  const [datasetName, setDatasetName] = useState<string>('Smishing Testbed Benchmark');
  const [rawInputText, setRawInputText] = useState<string>(
    SAMPLE_DATASETS.smishing_benchmark.map(i => `${i.type} | ${i.sender || 'UNKNOWN'} | ${i.text} | ${i.ground_truth}`).join('\n')
  );
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [evaluationResult, setEvaluationResult] = useState<BatchEvaluateResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleLoadPreset = (key: 'smishing_benchmark' | 'url_benchmark') => {
    const items = SAMPLE_DATASETS[key];
    setDatasetName(key === 'smishing_benchmark' ? 'Smishing Testbed Benchmark (10 samples)' : 'Phishing URL Benchmark (8 samples)');
    setRawInputText(
      items.map(i => `${i.type} | ${i.sender || 'UNKNOWN'} | ${i.text} | ${i.ground_truth}`).join('\n')
    );
    setEvaluationResult(null);
    setErrorMessage(null);
  };

  const parseInputToItems = (): BatchItem[] => {
    const lines = rawInputText.split('\n').map(l => l.trim()).filter(l => l.length > 0);
    const parsed: BatchItem[] = [];

    for (let i = 0; i < lines.length; i++) {
      const parts = lines[i].split('|').map(p => p.trim());
      if (parts.length >= 3) {
        // Format: TYPE | SENDER | TEXT | GROUND_TRUTH (or TYPE | TEXT | GROUND_TRUTH)
        if (parts[0].toUpperCase() === 'URL') {
          parsed.push({
            id: `item-${i + 1}`,
            type: 'URL',
            text: parts[1],
            ground_truth: parts[2] || 'PHISHING'
          });
        } else {
          parsed.push({
            id: `item-${i + 1}`,
            type: (parts[0].toUpperCase() as any) || 'SMS',
            sender: parts[1],
            text: parts[2],
            ground_truth: parts[3] || 'SMISHING'
          });
        }
      } else if (parts.length === 2) {
        parsed.push({
          id: `item-${i + 1}`,
          type: parts[0].startsWith('http') ? 'URL' : 'SMS',
          sender: '+18005550199',
          text: parts[0],
          ground_truth: parts[1] || 'SMISHING'
        });
      } else {
        parsed.push({
          id: `item-${i + 1}`,
          type: lines[i].startsWith('http') ? 'URL' : 'SMS',
          sender: '+18005550199',
          text: lines[i],
          ground_truth: 'SMISHING'
        });
      }
    }
    return parsed;
  };

  const handleRunEvaluation = async () => {
    const items = parseInputToItems();
    if (items.length === 0) {
      setErrorMessage('Please provide at least one message or URL to evaluate.');
      return;
    }
    setIsRunning(true);
    setErrorMessage(null);
    try {
      const res = await runBatchEvaluation(datasetName, items);
      setEvaluationResult(res);
    } catch (e: any) {
      setErrorMessage(e.message || 'Batch evaluation failed');
    } finally {
      setIsRunning(false);
    }
  };

  const handleExportCsv = () => {
    if (!evaluationResult) return;
    const headers = ['ID,Type,GroundTruth,Prediction,Verdict,RiskScore,RiskLevel,LatencyMs,Content'];
    const rows = evaluationResult.detailed_results.map(r => 
      `"${r.id}","${r.type}","${r.ground_truth}","${r.prediction}","${r.verdict}",${r.risk_score},"${r.risk_level}",${r.latency_ms},"${r.text.replace(/"/g, '""')}"`
    );
    const csvContent = 'data:text/csv;charset=utf-8,' + [headers, ...rows].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `phishguard_batch_evaluation_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="glass-panel p-6 border-l-4 border-indigo-500 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-indigo-400 font-semibold text-sm uppercase tracking-wider">
            <FileSpreadsheet className="w-4 h-4" /> Real-Time Dataset Batch Evaluator
          </div>
          <h1 className="text-2xl font-bold mt-1">Live Batch Inference & Empirical Metrics</h1>
          <p className="text-sm text-slate-400 mt-1">
            Evaluate custom dataset files or paste multi-sample batches to compute instantaneous Confusion Matrices and F1-Scores.
          </p>
        </div>

        {/* Preset Loaders */}
        <div className="flex flex-wrap gap-2 self-start md:self-auto">
          <button 
            onClick={() => handleLoadPreset('smishing_benchmark')}
            className="btn-secondary text-xs"
          >
            <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
            Load Smishing Set (10)
          </button>
          <button 
            onClick={() => handleLoadPreset('url_benchmark')}
            className="btn-secondary text-xs"
          >
            <Sparkles className="w-3.5 h-3.5 text-amber-400" />
            Load URL Set (8)
          </button>
        </div>
      </div>

      {/* Input Composer & Controls */}
      <div className="glass-panel p-6 space-y-4">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">
              Dataset Experiment Title
            </label>
            <input
              type="text"
              value={datasetName}
              onChange={(e) => setDatasetName(e.target.value)}
              className="input-dark font-mono text-xs w-full md:w-80"
              placeholder="Experiment name"
            />
          </div>

          <div className="text-xs text-slate-400">
            Format: <code className="font-mono text-cyan-300">TYPE | SENDER | MESSAGE / URL | GROUND_TRUTH</code>
          </div>
        </div>

        <div>
          <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">
            Batch Input Samples (One per line)
          </label>
          <textarea
            value={rawInputText}
            onChange={(e) => setRawInputText(e.target.value)}
            rows={7}
            className="input-dark font-mono text-xs resize-none"
            placeholder="SMS | +18005550199 | Your account is locked: http://fake.xyz | SMISHING"
          />
        </div>

        {errorMessage && (
          <div className="p-3 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-rose-400 flex-shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        <div className="flex items-center gap-3">
          <button
            onClick={handleRunEvaluation}
            disabled={isRunning}
            className="btn-primary"
          >
            <Play className={`w-4 h-4 ${isRunning ? 'animate-spin' : ''}`} />
            {isRunning ? 'Running Live Evaluation...' : 'Execute Batch Evaluation'}
          </button>
        </div>
      </div>

      {/* Evaluation Results Card */}
      {evaluationResult && (
        <div className="glass-panel p-6 space-y-6 border-l-4 border-emerald-500">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-white/10 pb-4">
            <div>
              <div className="text-xs font-semibold text-emerald-400 uppercase tracking-wider flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4" /> Empirical Evaluation Completed
              </div>
              <h2 className="text-xl font-bold mt-1">{evaluationResult.dataset_name}</h2>
              <p className="text-xs text-slate-400 mt-0.5">
                Total Samples Tested: <strong className="text-slate-200">{evaluationResult.total_samples}</strong> | Total Execution Time: <strong className="text-cyan-400 font-mono">{evaluationResult.metrics.total_execution_time_ms} ms</strong>
              </p>
            </div>

            <button onClick={handleExportCsv} className="btn-secondary text-xs self-start md:self-auto">
              <Download className="w-4 h-4 text-cyan-400" /> Export Results CSV
            </button>
          </div>

          {/* Quick Metrics & Confusion Matrix */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 font-mono">
            <div className="p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-xl">
              <div className="text-[11px] text-emerald-400 uppercase font-sans">Accuracy</div>
              <div className="text-2xl font-black text-emerald-300 mt-1">{evaluationResult.metrics.accuracy}%</div>
            </div>

            <div className="p-4 bg-cyan-500/10 border border-cyan-500/20 rounded-xl">
              <div className="text-[11px] text-cyan-400 uppercase font-sans">Precision</div>
              <div className="text-2xl font-black text-cyan-300 mt-1">{evaluationResult.metrics.precision}%</div>
            </div>

            <div className="p-4 bg-purple-500/10 border border-purple-500/20 rounded-xl">
              <div className="text-[11px] text-purple-400 uppercase font-sans">Recall</div>
              <div className="text-2xl font-black text-purple-300 mt-1">{evaluationResult.metrics.recall}%</div>
            </div>

            <div className="p-4 bg-amber-500/10 border border-amber-500/20 rounded-xl">
              <div className="text-[11px] text-amber-400 uppercase font-sans">F1-Score</div>
              <div className="text-2xl font-black text-amber-300 mt-1">{(evaluationResult.metrics.f1_score / 100).toFixed(4)}</div>
            </div>
          </div>

          {/* Detailed Item Breakdown Table */}
          <div className="space-y-2">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Sample-by-Sample Inference Log</h3>
            <div className="overflow-x-auto rounded-xl border border-white/5">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="bg-slate-950/60 text-slate-400 uppercase tracking-wider border-b border-white/10">
                    <th className="py-2.5 px-3">Verdict</th>
                    <th className="py-2.5 px-3">Content / Payload</th>
                    <th className="py-2.5 px-3">Ground Truth</th>
                    <th className="py-2.5 px-3">Predicted</th>
                    <th className="py-2.5 px-3 text-center">Score</th>
                    <th className="py-2.5 px-3 text-center">Latency</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/5 font-mono">
                  {evaluationResult.detailed_results.map((r, idx) => {
                    const isCorrect = r.verdict.startsWith('TP') || r.verdict.startsWith('TN');
                    return (
                      <tr key={idx} className="hover:bg-white/5">
                        <td className="py-2.5 px-3">
                          <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                            isCorrect ? 'bg-emerald-500/20 text-emerald-300' : 'bg-rose-500/20 text-rose-300'
                          }`}>
                            {r.verdict}
                          </span>
                        </td>
                        <td className="py-2.5 px-3 font-sans text-slate-300 max-w-xs truncate" title={r.text}>
                          {r.text}
                        </td>
                        <td className="py-2.5 px-3 text-slate-400">{r.ground_truth}</td>
                        <td className="py-2.5 px-3 font-bold text-slate-200">{r.prediction}</td>
                        <td className="py-2.5 px-3 text-center">
                          <span className={`font-bold ${
                            r.risk_score >= 60 ? 'text-rose-400' : (r.risk_score >= 35 ? 'text-amber-400' : 'text-emerald-400')
                          }`}>
                            {r.risk_score}
                          </span>
                        </td>
                        <td className="py-2.5 px-3 text-center text-slate-400">{r.latency_ms} ms</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
