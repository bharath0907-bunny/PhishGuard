import re
from typing import Dict, Any, List
from .threat_bank import URGENCY_PATTERNS, TARGETED_BRANDS
from .smishing_classifier import extract_urls_from_text
from .url_features import extract_url_features

def analyze_email_content(sender: str, subject: str, body: str) -> Dict[str, Any]:
    """
    Analyzes email sender, subject, and body for spear-phishing, BEC, lookalike domains, and malicious links.
    """
    combined_text = f"{subject} {body}".lower()
    extracted_urls = extract_urls_from_text(body)
    
    reasons = []
    threat_categories = []
    feature_contributions = []
    base_score = 0.0
    
    # 1. Subject Line Threat Signals
    subject_lower = subject.lower()
    if any(re.search(p, subject_lower) for p in URGENCY_PATTERNS):
        base_score += 30.0
        reasons.append("Subject line utilizes artificial urgency or account intimidation")
        threat_categories.append("Urgent Coercion Subject")
        feature_contributions.append({"feature": "Subject Panic Inducer", "impact": 30.0})
        
    if re.search(r"\b(fwd:|re:)\s*.*\b(invoice|payment|wire|remittance|swift|overdue|po#)\b", subject_lower):
        base_score += 25.0
        reasons.append("Subject mimics existing enterprise accounting thread (BEC Signature)")
        threat_categories.append("Business Email Compromise (BEC)")
        feature_contributions.append({"feature": "Forged Invoice Thread", "impact": 25.0})

    # 2. Sender Domain Discrepancy & Spoofing
    sender_lower = sender.lower()
    for brand, info in TARGETED_BRANDS.items():
        if any(k in sender_lower for k in info["keywords"]):
            # Check if domain matches authentic official domain
            is_legit_sender = any(sender_lower.endswith("@" + dom) or sender_lower.endswith("." + dom) for dom in info["official_domains"])
            if not is_legit_sender:
                base_score += 55.0
                reasons.append(f"Sender email mimics '{brand.upper()}' from unverified third-party domain: {sender}")
                threat_categories.append(f"Sender Spoofing ({brand.upper()})")
                feature_contributions.append({"feature": f"Spoofed {brand.upper()} Address", "impact": 55.0})
                break

    # 3. URL Analysis in Email Body
    url_results = []
    if extracted_urls:
        reasons.append(f"Contains {len(extracted_urls)} link(s) in email body")
        highest_url_risk = 0.0
        for u in extracted_urls:
            u_feats = extract_url_features(u)
            url_risk = 0.0
            if u_feats["is_whitelisted"]:
                url_risk = 0.0
                reasons.append(f"Link points to verified legitimate domain ({u_feats['registered_domain']})")
            else:
                if u_feats["has_ip_host"]:
                    url_risk += 60.0
                    reasons.append(f"Email links directly to bare IP address: {u}")
                if u_feats["is_shortened"]:
                    url_risk += 35.0
                    reasons.append(f"Email conceals destination with URL shortener: {u}")
                if u_feats["spoofed_brand"]:
                    url_risk += 65.0
                    reasons.append(f"Email link directs to unauthorized {u_feats['spoofed_brand']} mirror")
                if u_feats["has_suspicious_tld"]:
                    url_risk += 40.0
                    reasons.append(f"Link hosted on high-risk domain ({u_feats['suffix']})")
                if u_feats["sensitive_keyword_count"] >= 1:
                    url_risk += 25.0
                    reasons.append("Link leads to credential input / login portal")
                
                url_risk = min(max(url_risk, 15.0), 100.0)
                
            if url_risk > highest_url_risk:
                highest_url_risk = url_risk
            url_results.append({"url": u, "features": u_feats, "risk": url_risk})
        
        base_score += highest_url_risk * 0.50

    # 4. Body Content Credential Harvesting & Threat Language
    if re.search(r"\b(password\s*(expires|expired)|verify\s*your\s*login|click\s*below\s*to\s*confirm|reactivate\s*account|fill\s*out\s*attached\s*form)\b", combined_text):
        base_score += 30.0
        reasons.append("Body contains explicit credential harvesting triggers")
        threat_categories.append("Credential Harvesting")
        feature_contributions.append({"feature": "Credential Baiting Phrasing", "impact": 30.0})

    final_score = min(max(base_score, 0.0), 100.0)
    
    if final_score >= 75.0:
        risk_level = "CRITICAL"
        prediction = "PHISHING_EMAIL"
    elif final_score >= 55.0:
        risk_level = "HIGH"
        prediction = "PHISHING_EMAIL"
    elif final_score >= 30.0:
        risk_level = "MEDIUM"
        prediction = "SUSPICIOUS_EMAIL"
    elif final_score >= 15.0:
        risk_level = "LOW"
        prediction = "LOW_RISK"
    else:
        risk_level = "SAFE"
        prediction = "BENIGN_EMAIL"

    confidence = round(0.88 + (final_score / 800.0) if final_score > 50 else 0.95 - (final_score / 500.0), 3)
    confidence = min(max(confidence, 0.85), 0.99)

    return {
        "sender": sender,
        "subject": subject,
        "risk_score": round(final_score, 1),
        "risk_level": risk_level,
        "prediction": prediction,
        "confidence": confidence,
        "threat_categories": list(set(threat_categories)),
        "extracted_urls": url_results,
        "feature_contributions": sorted(feature_contributions, key=lambda x: abs(x["impact"]), reverse=True),
        "reasons": reasons if reasons else ["Email content matches standard communication patterns."],
        "recommended_action": "QUARANTINE" if final_score >= 55.0 else ("BANNER_WARNING" if final_score >= 30.0 else "INBOX")
    }
