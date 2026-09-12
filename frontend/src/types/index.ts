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
    features?: Record<string, any>;
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

export interface BenchmarkMetrics {
  accuracy: number;
  precision: number;
  recall: number;
  f1_score: number;
  false_positive_rate?: number;
  roc_auc?: number;
  avg_latency_ms?: number;
}

export interface ConfusionMatrix {
  true_positive: number;
  false_positive: number;
  true_negative: number;
  false_negative: number;
}

export interface DatasetBenchmark {
  name: string;
  description: string;
  sample_count: number;
  metrics: BenchmarkMetrics;
  confusion_matrix: ConfusionMatrix;
}

export interface AblationStudyItem {
  model_variant: string;
  features_used: string;
  accuracy: number;
  precision: number;
  recall: number;
  f1_score: number;
  latency_ms: number;
}

export interface ResearchBenchmarkResponse {
  summary: {
    overall_accuracy: number;
    overall_precision: number;
    overall_recall: number;
    overall_f1: number;
    overall_fpr: number;
    overall_roc_auc: number;
    total_benchmark_samples: number;
  };
  datasets: Record<string, DatasetBenchmark>;
  ablation_study: AblationStudyItem[];
  latency_distribution: Record<string, Record<string, number>>;
  hardware_testbed: {
    client_device: string;
    server_hardware: string;
    network_condition: string;
  };
}

export interface BatchItem {
  id?: string;
  type: 'SMS' | 'URL' | 'EMAIL';
  sender?: string;
  text: string;
  ground_truth?: string;
}

export interface BatchEvaluateResponse {
  dataset_name: string;
  total_samples: number;
  metrics: {
    accuracy: number;
    precision: number;
    recall: number;
    f1_score: number;
    total_execution_time_ms: number;
    avg_latency_per_sample_ms: number;
  };
  confusion_matrix: ConfusionMatrix;
  detailed_results: Array<{
    id: string;
    text: string;
    type: string;
    risk_score: number;
    risk_level: string;
    prediction: string;
    ground_truth: string;
    verdict: string;
    reasons: string[];
    latency_ms: number;
  }>;
}

export interface AdversarialMutation {
  technique: string;
  description: string;
  perturbed_text: string;
  risk_score: number;
  risk_level: string;
  prediction: string;
  defense_status: string;
  reasons: string[];
}

export interface AdversarialTestResponse {
  baseline: {
    text: string;
    risk_score: number;
    risk_level: string;
    prediction: string;
    reasons: string[];
  };
  adversarial_evaluations: AdversarialMutation[];
  robustness_score: string;
}

export interface LatexExportResponse {
  benchmark_table: string;
  ablation_table: string;
  bibtex_citation: string;
}
