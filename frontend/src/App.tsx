import React, { useState, useEffect, useRef } from 'react';
import { Navbar, NavTabType } from './components/Navbar';
import { StatsCard } from './components/StatsCard';
import { LiveMessageFeed } from './components/LiveMessageFeed';
import { ResearchBenchmarkView } from './components/ResearchBenchmarkView';
import { AdversarialAttackStudio } from './components/AdversarialAttackStudio';
import { UrlScanner } from './components/UrlScanner';
import { EmailScanner } from './components/EmailScanner';
import { BatchEvaluator } from './components/BatchEvaluator';
import { PublicationHub } from './components/PublicationHub';
import { ApiIntegrationGateway } from './components/ApiIntegrationGateway';
import { fetchStats, fetchLiveFeed, triggerLiveStreamEvent } from './services/api';
import { DashboardStats, GoogleMessageEvent } from './types';

// Web Audio API Threat Chime Synthesizer
function playThreatChime() {
  try {
    const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
    if (!AudioCtx) return;
    const ctx = new AudioCtx();
    
    // Create an oscillator for high-tech security alert sound
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    
    osc.type = 'sine';
    osc.frequency.setValueAtTime(880, ctx.currentTime); // A5 note
    osc.frequency.exponentialRampToValueAtTime(440, ctx.currentTime + 0.18); // drop to A4
    
    gain.gain.setValueAtTime(0.15, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.22);
    
    osc.connect(gain);
    gain.connect(ctx.destination);
    
    osc.start();
    osc.stop(ctx.currentTime + 0.25);
  } catch (e) {
    // Audio context may be restricted by browser until user gesture
  }
}

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<NavTabType>('messages');
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [messages, setMessages] = useState<GoogleMessageEvent[]>([]);
  const [latencyMs, setLatencyMs] = useState<number>(32.4);
  const [audioAlertsEnabled, setAudioAlertsEnabled] = useState<boolean>(true);
  const [isSimulatingStream, setIsSimulatingStream] = useState<boolean>(false);
  const simulationIntervalRef = useRef<any>(null);

  // Fetch initial stats and live feed
  const loadData = async () => {
    try {
      const [s, m] = await Promise.all([fetchStats(), fetchLiveFeed(30)]);
      setStats(s);
      setMessages(m);
      if (s.system_status?.latency_ms) {
        setLatencyMs(s.system_status.latency_ms);
      }
    } catch (e) {
      console.warn('Initial fetch using fallback state until backend is reachable.');
    }
  };

  useEffect(() => {
    loadData();

    // WebSocket real-time threat stream listener
    let ws: WebSocket | null = null;
    try {
      ws = new WebSocket('ws://localhost:8000/ws/threat-stream');
      ws.onmessage = (event) => {
        try {
          const payload = JSON.parse(event.data);
          if (payload.type === 'NEW_GOOGLE_MESSAGE_INTERCEPTED') {
            const newMsg: GoogleMessageEvent = {
              id: payload.data.id,
              sender: payload.data.sender,
              text: payload.data.text,
              source_app: payload.data.source_app,
              device_id: payload.data.device_id,
              risk_score: payload.data.risk_score,
              risk_level: payload.data.risk_level,
              prediction: payload.data.prediction,
              threat_categories: payload.data.threat_categories || [],
              reasons: payload.data.reasons || [],
              extracted_urls: payload.data.extracted_urls || [],
              created_at: payload.data.created_at,
              should_alert: payload.data.should_alert
            };

            setMessages((prev) => [newMsg, ...prev]);
            loadData();

            // Play alert chime if critical / high risk
            if (audioAlertsEnabled && newMsg.risk_score >= 50.0) {
              playThreatChime();
            }
          }
        } catch (err) {
          console.error('WebSocket parse error:', err);
        }
      };
    } catch (e) {
      console.warn('WebSocket stream not ready, falling back to polling.');
    }

    // Interval polling fallback (every 3 seconds)
    const interval = setInterval(loadData, 3000);

    return () => {
      clearInterval(interval);
      if (ws) ws.close();
    };
  }, [audioAlertsEnabled]);

  // Handle Automatic Live Stream Simulation Toggle
  useEffect(() => {
    if (isSimulatingStream) {
      // Dispatch immediately and then every 4 seconds
      triggerLiveStreamEvent().catch(console.error);
      simulationIntervalRef.current = setInterval(() => {
        triggerLiveStreamEvent().catch(console.error);
      }, 4000);
    } else {
      if (simulationIntervalRef.current) {
        clearInterval(simulationIntervalRef.current);
        simulationIntervalRef.current = null;
      }
    }

    return () => {
      if (simulationIntervalRef.current) {
        clearInterval(simulationIntervalRef.current);
      }
    };
  }, [isSimulatingStream]);

  return (
    <div className="min-h-screen pb-16 px-4 pt-4">
      {/* Top Command Bar */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        latencyMs={latencyMs}
        activeDevices={stats?.system_status?.active_devices || 1}
        isSimulatingStream={isSimulatingStream}
        onToggleSimulateStream={() => setIsSimulatingStream(prev => !prev)}
      />

      <main className="max-w-7xl mx-auto space-y-6">
        {/* Real-Time Metric Counters (Always visible across all tabs) */}
        <StatsCard stats={stats} />

        {/* Dynamic Tab Views */}
        {activeTab === 'messages' && (
          <LiveMessageFeed 
            messages={messages} 
            onRefresh={loadData}
            audioAlertsEnabled={audioAlertsEnabled}
            onToggleAudioAlerts={() => setAudioAlertsEnabled(prev => !prev)}
          />
        )}

        {activeTab === 'benchmarks' && (
          <ResearchBenchmarkView />
        )}

        {activeTab === 'attack_studio' && (
          <AdversarialAttackStudio onMessageSent={loadData} />
        )}

        {activeTab === 'urls' && (
          <UrlScanner />
        )}

        {activeTab === 'emails' && (
          <EmailScanner />
        )}

        {activeTab === 'batch_evaluator' && (
          <BatchEvaluator />
        )}

        {activeTab === 'publications' && (
          <PublicationHub />
        )}

        {activeTab === 'api_gateway' && (
          <ApiIntegrationGateway />
        )}
      </main>
    </div>
  );
};
