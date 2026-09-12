import React, { useState } from 'react';
import { 
  Terminal, 
  Smartphone, 
  Chrome, 
  Copy, 
  Check, 
  Play, 
  Radio, 
  Server, 
  Globe 
} from 'lucide-react';

export const ApiIntegrationGateway: React.FC = () => {
  const [selectedLanguage, setSelectedLanguage] = useState<'curl' | 'python' | 'javascript'>('curl');
  const [copiedKey, setCopiedKey] = useState<string | null>(null);
  const [apiResponse, setApiResponse] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const codeSnippets = {
    curl: `curl -X POST "http://localhost:8000/api/v1/mobile/analyze-notification" \\
  -H "Content-Type: application/json" \\
  -d '{
    "sender": "+18005550199",
    "text": "[CHASE] Unauthorized wire of $980.00. Verify immediately at http://chase-security-auth.xyz/login",
    "package_name": "com.google.android.apps.messaging",
    "device_id": "pixel-8-pro"
  }'`,
    python: `import requests

payload = {
    "sender": "+18005550199",
    "text": "[CHASE] Unauthorized wire of $980.00. Verify immediately at http://chase-security-auth.xyz/login",
    "package_name": "com.google.android.apps.messaging",
    "device_id": "pixel-8-pro"
}

response = requests.post(
    "http://localhost:8000/api/v1/mobile/analyze-notification",
    json=payload
)
print("Risk Assessment:", response.json())`,
    javascript: `const response = await fetch('http://localhost:8000/api/v1/mobile/analyze-notification', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    sender: '+18005550199',
    text: '[CHASE] Unauthorized wire of $980.00. Verify immediately at http://chase-security-auth.xyz/login',
    package_name: 'com.google.android.apps.messaging',
    device_id: 'pixel-8-pro'
  })
});
const data = await response.json();
console.log('Risk Assessment:', data);`
  };

  const handleCopy = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const handleTestApi = async () => {
    setIsLoading(true);
    setApiResponse(null);
    try {
      const res = await fetch('http://localhost:8000/api/v1/mobile/analyze-notification', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sender: '+18005550199',
          text: '[CHASE] Unauthorized wire of $980.00. Verify immediately at http://chase-security-auth.xyz/login',
          package_name: 'com.google.android.apps.messaging',
          device_id: 'api-gateway-tester'
        })
      });
      const data = await res.json();
      setApiResponse(JSON.stringify(data, null, 2));
    } catch (e: any) {
      setApiResponse(JSON.stringify({ error: e.message || 'Failed to connect to backend' }, null, 2));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="glass-panel p-6 border-l-4 border-cyan-500 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-cyan-400 font-semibold text-sm uppercase tracking-wider">
            <Server className="w-4 h-4" /> Multi-Tier Integration Gateway
          </div>
          <h1 className="text-2xl font-bold mt-1">Client Telemetry & Developer API Suite</h1>
          <p className="text-sm text-slate-400 mt-1">
            Live telemetry endpoints for Android OS companion services, Chrome Manifest V3 extensions, and REST clients.
          </p>
        </div>
      </div>

      {/* Connected Devices & Channels Status */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="glass-panel p-5 space-y-3 border-emerald-500/30">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-emerald-400 text-xs font-semibold uppercase">
              <Smartphone className="w-4 h-4" /> Android Companion App
            </div>
            <span className="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 text-[10px] font-bold">
              <Radio className="w-3 h-3 animate-pulse" /> LISTENING
            </span>
          </div>
          <p className="text-xs text-slate-300">
            Targeting Google Messages (<code className="font-mono text-[11px] text-cyan-300">com.google.android.apps.messaging</code>).
          </p>
          <div className="pt-2 border-t border-white/5 text-[11px] text-slate-400 font-mono">
            Listener: GoogleMessagesListenerService.kt
          </div>
        </div>

        <div className="glass-panel p-5 space-y-3 border-cyan-500/30">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-cyan-400 text-xs font-semibold uppercase">
              <Chrome className="w-4 h-4" /> Chrome MV3 Extension
            </div>
            <span className="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-cyan-500/20 text-cyan-300 text-[10px] font-bold">
              <Radio className="w-3 h-3 animate-pulse" /> READY
            </span>
          </div>
          <p className="text-xs text-slate-300">
            Active tab URL interceptor with sub-40ms malicious site blocking injection.
          </p>
          <div className="pt-2 border-t border-white/5 text-[11px] text-slate-400 font-mono">
            Worker: extension/background.js
          </div>
        </div>

        <div className="glass-panel p-5 space-y-3 border-purple-500/30">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-purple-400 text-xs font-semibold uppercase">
              <Globe className="w-4 h-4" /> Real-Time WebSocket Channel
            </div>
            <span className="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-purple-500/20 text-purple-300 text-[10px] font-bold">
              <Radio className="w-3 h-3 animate-pulse" /> BROADCASTING
            </span>
          </div>
          <p className="text-xs text-slate-300">
            Zero-latency bi-directional threat stream on <code className="font-mono text-[11px] text-purple-300">/ws/threat-stream</code>.
          </p>
          <div className="pt-2 border-t border-white/5 text-[11px] text-slate-400 font-mono">
            Status: Active Stream (Port 8000)
          </div>
        </div>
      </div>

      {/* Interactive Code Generator & Tester */}
      <div className="glass-panel p-6 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-white/10 pb-3">
          <div className="flex items-center gap-2 text-slate-200 font-bold text-sm">
            <Terminal className="w-4 h-4 text-cyan-400" />
            Interactive Code Snippet Generator
          </div>

          <div className="flex items-center gap-2">
            <div className="flex rounded-lg bg-slate-950 p-1 border border-white/10 text-xs font-semibold">
              {(['curl', 'python', 'javascript'] as const).map((lang) => (
                <button
                  key={lang}
                  onClick={() => setSelectedLanguage(lang)}
                  className={`px-3 py-1 rounded-md transition-all ${
                    selectedLanguage === lang
                      ? 'bg-cyan-500 text-slate-950 font-bold'
                      : 'text-slate-400 hover:text-slate-200'
                  }`}
                >
                  {lang.toUpperCase()}
                </button>
              ))}
            </div>

            <button
              onClick={() => handleCopy(codeSnippets[selectedLanguage], 'code')}
              className="btn-secondary text-xs"
            >
              {copiedKey === 'code' ? (
                <>
                  <Check className="w-3.5 h-3.5 text-emerald-400" /> Copied!
                </>
              ) : (
                <>
                  <Copy className="w-3.5 h-3.5 text-cyan-400" /> Copy
                </>
              )}
            </button>
          </div>
        </div>

        <pre className="code-block-dark text-xs">
          <code>{codeSnippets[selectedLanguage]}</code>
        </pre>

        <div className="flex items-center gap-3">
          <button
            onClick={handleTestApi}
            disabled={isLoading}
            className="btn-primary text-xs"
          >
            <Play className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            {isLoading ? 'Executing Test Call...' : 'Execute Live API Request'}
          </button>
        </div>

        {apiResponse && (
          <div className="space-y-2 pt-2">
            <div className="text-xs font-bold uppercase tracking-wider text-slate-400">
              Live Server Response (JSON):
            </div>
            <pre className="code-block-dark text-xs font-mono text-emerald-300">
              <code>{apiResponse}</code>
            </pre>
          </div>
        )}
      </div>
    </div>
  );
};
