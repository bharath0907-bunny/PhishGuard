export interface GoogleMessageEvent {
  id: string;
  sender: string;
  text: string;
  source_app: string;
  device_id: string;
  risk_score: number;
  risk_level: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE';
  prediction: string;
  threat_categories: string[];
  reasons: string[];
  extracted_urls?: Array<{
    url: string;
    risk: number;
    features: Record<string, any>;
  }>;
  created_at: string;
  should_alert?: boolean;
}

export interface DashboardStats {
  google_messages: {
    total_intercepted: number;
    smishing_blocked: number;
    suspicious_warned: number;
    safe_passed: number;
    avg_risk_score: number;
  };
  url_scans: {
    total: number;
    malicious: number;
    safe: number;
  };
  email_scans: {
    total: number;
    phishing: number;
    safe: number;
  };
  system_status: {
    realtime_engine: string;
    active_devices: number;
    model_version: string;
    latency_ms: number;
  };
}

export interface UrlScanResult {
  id?: string;
  url: string;
  risk_score: number;
  risk_level: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE';
  prediction: string;
  confidence: number;
  features: Record<string, any>;
  reasons: string[];
  feature_contributions?: Array<{
    feature: string;
    impact: number;
  }>;
  recommended_action: string;
}

export interface EmailScanResult {
  id?: string;
  sender: string;
  subject: string;
  risk_score: number;
  risk_level: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE';
  prediction: string;
  threat_categories: string[];
  reasons: string[];
  extracted_urls?: Array<{
    url: string;
    risk: number;
  }>;
  recommended_action: string;
}
