import React, { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { StatsCard } from './components/StatsCard';
import { LiveMessageFeed } from './components/LiveMessageFeed';
import { GoogleMessageSimulator } from './components/GoogleMessageSimulator';
import { UrlScanner } from './components/UrlScanner';
import { EmailScanner } from './components/EmailScanner';
import { fetchStats, fetchLiveFeed } from './services/api';
import { DashboardStats, GoogleMessageEvent } from './types';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'messages' | 'urls' | 'emails' | 'simulator'>('messages');
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [messages, setMessages] = useState<GoogleMessageEvent[]>([]);
  const [latencyMs, setLatencyMs] = useState<number>(32);

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
      console.warn('Initial fetch using fallback mock state until backend is ready');
    }
  };

  useEffect(() => {
    loadData();

    // WebSocket real-time event listener
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
            loadData(); // refresh stats counter
          }
        } catch (err) {
          console.error(err);
        }
      };
    } catch (e) {
      console.warn('WebSocket connection not ready, relying on polling.');
    }

    // Interval polling fallback (every 4 seconds)
    const interval = setInterval(loadData, 4000);

    return () => {
      clearInterval(interval);
      if (ws) ws.close();
    };
  }, []);

  return (
    <div className="min-h-screen pb-16 px-4 pt-4">
      {/* Top Command Bar */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        latencyMs={latencyMs}
        activeDevices={stats?.system_status?.active_devices || 1}
      />

      <main className="max-w-7xl mx-auto space-y-6">
        {/* Real-Time Metrics Bar */}
        <StatsCard stats={stats} />

        {/* Dynamic Channel View */}
        {activeTab === 'messages' && (
          <LiveMessageFeed messages={messages} onRefresh={loadData} />
        )}

        {activeTab === 'simulator' && (
          <GoogleMessageSimulator onMessageSent={loadData} />
        )}

        {activeTab === 'urls' && (
          <UrlScanner />
        )}

        {activeTab === 'emails' && (
          <EmailScanner />
        )}
      </main>
    </div>
  );
};
