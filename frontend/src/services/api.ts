import { DashboardStats, GoogleMessageEvent, UrlScanResult, EmailScanResult } from '../types';

const API_BASE = 'http://localhost:8000/api/v1';

export async function fetchStats(): Promise<DashboardStats> {
  const res = await fetch(`${API_BASE}/dashboard/stats`);
  if (!res.ok) throw new Error('Failed to fetch stats');
  return res.json();
}

export async function fetchLiveFeed(limit: number = 30): Promise<GoogleMessageEvent[]> {
  const res = await fetch(`${API_BASE}/dashboard/live-feed?limit=${limit}`);
  if (!res.ok) throw new Error('Failed to fetch live feed');
  const data = await res.json();
  // Map API output to GoogleMessageEvent
  return data.map((item: any) => ({
    id: item.id,
    sender: item.title?.replace('Google Message from ', '') || 'SMS Sender',
    text: item.content,
    source_app: item.source_app || 'com.google.android.apps.messaging',
    device_id: 'pixel-8-live',
    risk_score: item.risk_score,
    risk_level: item.risk_level,
    prediction: item.prediction,
    threat_categories: item.threat_categories || [],
    reasons: item.reasons || [],
    created_at: item.created_at
  }));
}

export async function simulateGoogleMessage(sender: string, text: string): Promise<any> {
  const res = await fetch(`${API_BASE}/mobile/analyze-notification`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      sender,
      text,
      device_id: 'manager-simulator-01',
      package_name: 'com.google.android.apps.messaging',
      timestamp: Date.now()
    })
  });
  if (!res.ok) throw new Error('Simulation failed');
  return res.json();
}

export async function scanUrl(url: string): Promise<UrlScanResult> {
  const res = await fetch(`${API_BASE}/analyze/url`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ url, source: 'WEB_SCANNER' })
  });
  if (!res.ok) throw new Error('URL scan failed');
  return res.json();
}

export async function scanEmail(sender: string, subject: string, body: string): Promise<EmailScanResult> {
  const res = await fetch(`${API_BASE}/analyze/email`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sender, subject, body })
  });
  if (!res.ok) throw new Error('Email scan failed');
  return res.json();
}

export async function submitFeedback(targetId: string, verdict: string, comment?: string): Promise<any> {
  const res = await fetch(`${API_BASE}/feedback/submit`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      target_id: targetId,
      target_type: 'MESSAGE',
      verdict,
      comment
    })
  });
  return res.json();
}
