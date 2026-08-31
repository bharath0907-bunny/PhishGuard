import re
import os
import json
import math
from typing import Dict, Any, List, Tuple
from .threat_bank import URGENCY_PATTERNS, TARGETED_BRANDS
from .url_features import extract_url_features

# Regular expressions for high-accuracy URL extraction from raw SMS text
URL_REGEX = re.compile(
    r"(?:https?://|www\.)[^\s/$.?#].[^\s]*|"
    r"\b(?:[a-zA-Z0-9-]+\.)+(?:com|net|org|xyz|top|tk|ml|ga|cf|gq|info|co|online|site|app|cc|link|live|buzz|icu|rest|sbs|cfd|click|bond|quest|vip|fit|shop|tech|store)(?:/[^\s]*)?",
    re.IGNORECASE
)

# Common SMS smishing category patterns with high sensitivity & specificity
CATEGORY_RULES = {
    "Financial & Banking Scam": [
        r"\b(bank|wire|account|debit|credit\s*card|zelle|venmo|paypal|cashapp|checking|savings|balance|transaction|overdraft)\b",
        r"\b(declined|unauthorized|frozen|suspended|charge|fraud|refund|locked|hold|verification)\b"
    ],
    "Package & Delivery Lure": [
        r"\b(usps|fedex|ups|dhl|postal|package|parcel|shipment|delivery|tracking|customs\s*fee|redeliver|courier|consignment)\b",
        r"\b(address\s*incomplete|delivery\s*failed|held\s*at\s*warehouse|pending\s*delivery|update\s*address|missing\s*street)\b"
    ],
    "Security & Account Compromise": [
        r"\b(security\s*alert|apple\s*id|netflix\s*bill|amazon\s*order|verification\s*code|otp|2fa|password\s*reset|unrecognized\s*device)\b",
        r"\b(logged\s*in\s*from|session\s*expired|confirm\s*identity|reactivate|suspicious\s*sign-in)\b"
    ],
    "Government & Tax Lure": [
        r"\b(irs|tax\s*refund|stimulus|gov|court\s*notice|penalty|fine|unemployment|warrant|department\s*of\s*revenue)\b"
    ],
    "Lottery, Prize & Crypto Giveaway": [
        r"\b(winner|won|congratulations|claim\s*prize|bitcoin|btc|eth|crypto|airdrop|free\s*gift|reward\s*points)\b"
    ],
    "Job & Work-From-Home Scam": [
        r"\b(work\s*from\s*home|part-time\s*job|earn\s*\$\d+|daily\s*income|hiring\s*immediately|interview\s*online)\b"
    ]
}

# Authentic 2FA / Transaction notifications pattern (lowers risk if genuine)
SAFE_TRANSACTION_PATTERNS = [
    r"your\s+(verification|security|login|otp|access|confirmation)\s+code\s+is\s*:\s*\d{4,8}",
    r"do\s+not\s+share\s+this\s+code\s+with\s+anyone",
    r"use\s+code\s+\d{4,8}\s+to\s+verify",
    r"g-\d{6}\s+is\s+your\s+google\s+verification\s+code",
    r"never\s+share\s+your\s+(pin|password|passcode)\s+with\s+anyone"
]

# Normal conversational phrases to avoid false positives on innocent personal SMS
CONVERSATIONAL_PATTERNS = [
    r"\b(are\s+we\s+still\s+meeting|how\s+are\s+you|see\s+you\s+(tomorrow|later|tonight)|dinner|lunch|breakfast|call\s+me|happy\s+birthday)\b"
]

# Load ML Weights
_MODEL_WEIGHTS = {}
_MODEL_INTERCEPT = -0.85

def _load_weights():
    global _MODEL_WEIGHTS, _MODEL_INTERCEPT
    current_dir = os.path.dirname(os.path.abspath(__file__))
    weights_path = os.path.join(current_dir, "model_weights.json")
    if os.path.exists(weights_path):
        try:
            with open(weights_path, "r", encoding="utf-8") as f:
                data = json.load(f)
                _MODEL_WEIGHTS = data.get("weights", {})
                _MODEL_INTERCEPT = data.get("intercept", -0.85)
        except Exception:
            pass

_load_weights()

def sigmoid(z: float) -> float:
    """Logistic sigmoid function."""
    if z < -20.0:
        return 0.0
    if z > 20.0:
        return 1.0
    return 1.0 / (1.0 + math.exp(-z))

def predict_ml_probability(text: str) -> Tuple[float, List[Dict[str, Any]]]:
    """
    On-device / fast vector inference calculating smishing probability and top token impacts.
    """
    tokens = re.findall(r"\b[a-zA-Z0-9]+\b|https?://[^\s]+", text.lower())
    z = _MODEL_INTERCEPT
    token_impacts = []

    for t in tokens:
        if t in _MODEL_WEIGHTS:
            weight = _MODEL_WEIGHTS[t]
            z += weight
            token_impacts.append({"token": t, "weight": weight})

    prob = sigmoid(z)
    token_impacts.sort(key=lambda x: abs(x["weight"]), reverse=True)
    return prob, token_impacts[:5]

def extract_urls_from_text(text: str) -> List[str]:
    """Extracts and normalizes all embedded URLs from text."""
    matches = URL_REGEX.findall(text)
    cleaned_urls = []
    for match in matches:
        m = match.strip(".,;:)'\"<>[]")
        if not m.startswith("http://") and not m.startswith("https://"):
            m = "http://" + m
        cleaned_urls.append(m)
    return cleaned_urls

def analyze_smishing_message(sender: str, raw_text: str) -> Dict[str, Any]:
    """
    Comprehensive, explainable real-time analysis of SMS/Google Messages text.
    Combines rule-based heuristics with Statistical ML probability into a calibrated risk score.
    """
    text_lower = raw_text.lower().strip()
    extracted_urls = extract_urls_from_text(raw_text)
    
    reasons = []
    threat_categories = []
    feature_contributions = []
    base_score = 0.0
    
    # 1. Statistical ML Vector Inference
    ml_prob, top_tokens = predict_ml_probability(raw_text)
    ml_score = ml_prob * 100.0

    if top_tokens:
        for item in top_tokens:
            impact = item["weight"] * 8.0
            feature_contributions.append({
                "feature": f"NLP Token: '{item['token']}'",
                "impact": round(impact, 1)
            })
    
    # 2. Evaluate Embedded Links
    url_results = []
    highest_url_risk = 0.0
    has_malicious_or_unknown_url = False
    
    if extracted_urls:
        reasons.append(f"Contains {len(extracted_urls)} embedded link(s) within SMS")
        feature_contributions.append({"feature": "Embedded URL in SMS", "impact": 20.0})
        base_score += 20.0

        for u in extracted_urls:
            u_feats = extract_url_features(u)
            url_risk = 0.0
            
            if u_feats["is_whitelisted"]:
                url_risk = 0.0
                reasons.append(f"Embedded link '{u}' resolves to verified safe domain ({u_feats['registered_domain']})")
            else:
                has_malicious_or_unknown_url = True
                if u_feats["has_ip_host"]:
                    url_risk += 60.0
                    reasons.append(f"Link uses direct numerical IP host instead of registered domain: {u}")
                if u_feats["is_shortened"]:
                    url_risk += 35.0
                    reasons.append("Link uses anonymous URL shortener to hide destination")
                if u_feats["has_suspicious_tld"]:
                    url_risk += 45.0
                    reasons.append(f"Link hosted on high-abuse TLD ({u_feats['suffix']})")
                if u_feats["spoofed_brand"]:
                    url_risk += 65.0
                    reasons.append(f"Link deceives user by spoofing legitimate brand: '{u_feats['spoofed_brand']}'")
                if u_feats["has_punycode"]:
                    url_risk += 40.0
                    reasons.append("Link uses Punycode/IDN homograph obfuscation")
                if u_feats["sensitive_keyword_count"] >= 1:
                    url_risk += 25.0
                    reasons.append("Link path contains credential-harvesting triggers (e.g. login, verify)")
                if u_feats["domain_entropy"] > 3.75:
                    url_risk += 30.0
                    reasons.append("Domain name exhibits algorithmic randomness (DGA pattern)")
                
                url_risk = min(max(url_risk, 15.0), 100.0)

            if url_risk > highest_url_risk:
                highest_url_risk = url_risk
            url_results.append({"url": u, "features": u_feats, "risk": url_risk})
        
        base_score += highest_url_risk * 0.50

    # 3. Urgency and Panic Induction Detection
    urgency_matches = sum(1 for pattern in URGENCY_PATTERNS if re.search(pattern, text_lower))
    if urgency_matches > 0:
        boost = min(urgency_matches * 18.0, 45.0)
        base_score += boost
        reasons.append("High-pressure psychological coercion / urgency language detected")
        threat_categories.append("Psychological Coercion / Urgency")
        feature_contributions.append({"feature": "Coercive / Panic Phrasing", "impact": boost})

    # 4. Signature Rule Evaluation
    for category_name, rule_pair in CATEGORY_RULES.items():
        if all(re.search(p, text_lower) for p in rule_pair):
            threat_categories.append(category_name)
            base_score += 25.0
            reasons.append(f"Matches established threat signature: '{category_name}'")
            feature_contributions.append({"feature": f"Signature: {category_name}", "impact": 25.0})

    # 5. Brand Impersonation in Text
    for brand, info in TARGETED_BRANDS.items():
        if any(k in text_lower for k in info["keywords"]):
            threat_categories.append(f"Brand Impersonation ({brand.upper()})")
            if has_malicious_or_unknown_url:
                base_score += 35.0
                reasons.append(f"Message impersonates '{brand.upper()}' and directs to an unverified external link")
                feature_contributions.append({"feature": f"Impersonation: {brand.upper()}", "impact": 35.0})
            break

    # 6. Sender Origin Analysis
    sender_clean = sender.strip()
    if sender_clean.startswith("+") and not (sender_clean.startswith("+1") or sender_clean.startswith("+44") or sender_clean.startswith("+91") or sender_clean.startswith("+61")):
        base_score += 15.0
        reasons.append(f"High-risk international sender origin: {sender_clean}")
        feature_contributions.append({"feature": "Foreign Sender Origin", "impact": 15.0})

    # 7. Benign Mitigation (Legitimate OTP or Personal Conversation)
    is_safe_2fa = False
    if not extracted_urls and any(re.search(p, text_lower) for p in SAFE_TRANSACTION_PATTERNS):
        is_safe_2fa = True
        base_score = max(0.0, base_score - 60.0)
        reasons.append("Structure matches standard legitimate 2FA / Authentication notification")
        feature_contributions.append({"feature": "Legitimate OTP Format", "impact": -60.0})

    if not extracted_urls and any(re.search(p, text_lower) for p in CONVERSATIONAL_PATTERNS):
        base_score = max(0.0, base_score - 40.0)
        reasons.append("Conversational pattern consistent with standard interpersonal messaging")
        feature_contributions.append({"feature": "Interpersonal Conversation", "impact": -40.0})

    # 8. Hybrid Ensemble Fusion: 55% Lexical Heuristics + 45% Statistical ML Score
    if is_safe_2fa:
        hybrid_score = min(base_score, 15.0)
    else:
        hybrid_score = (0.55 * base_score) + (0.45 * ml_score)

    final_score = min(max(hybrid_score, 0.0), 100.0)
    
    # Classification Thresholds
    if final_score >= 75.0:
        risk_level = "CRITICAL"
        prediction = "SMISHING"
    elif final_score >= 55.0:
        risk_level = "HIGH"
        prediction = "SMISHING"
    elif final_score >= 30.0:
        risk_level = "MEDIUM"
        prediction = "SUSPICIOUS"
    elif final_score >= 15.0:
        risk_level = "LOW"
        prediction = "LOW_RISK"
    else:
        risk_level = "SAFE"
        prediction = "BENIGN"

    # Confidence Calculation
    confidence = round(0.88 + (final_score / 800.0) if final_score > 50 else 0.95 - (final_score / 500.0), 3)
    confidence = min(max(confidence, 0.85), 0.99)

    return {
        "sender": sender,
        "raw_text": raw_text,
        "risk_score": round(final_score, 1),
        "ml_probability": round(ml_prob, 3),
        "risk_level": risk_level,
        "prediction": prediction,
        "confidence": confidence,
        "threat_categories": list(set(threat_categories)),
        "extracted_urls": url_results,
        "feature_contributions": sorted(feature_contributions, key=lambda x: abs(x["impact"]), reverse=True),
        "reasons": reasons if reasons else ["No threat indicators detected. Standard message."],
        "is_safe_2fa": is_safe_2fa,
        "recommended_action": "BLOCK_AND_ALERT" if final_score >= 55.0 else ("WARNING_BANNER" if final_score >= 30.0 else "ALLOW")
    }
