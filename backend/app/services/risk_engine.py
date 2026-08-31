from typing import Dict, Any, List
from ..ml.url_features import extract_url_features
from ..ml.smishing_classifier import analyze_smishing_message
from ..ml.email_analyzer import analyze_email_content

class RiskEngine:
    """
    Central Explainable Risk Engine for PhishGuard.
    Normalizes, weighs, and attributes risk across Google Messages (SMS), URLs, and Email.
    """

    @staticmethod
    def evaluate_url(url: str, source: str = "WEB_SCANNER") -> Dict[str, Any]:
        feats = extract_url_features(url)
        reasons = []
        contributions = []
        score = 0.0
        
        if feats["is_whitelisted"]:
            return {
                "url": url,
                "risk_score": 0.0,
                "risk_level": "SAFE",
                "prediction": "BENIGN",
                "confidence": 0.99,
                "features": feats,
                "reasons": [f"Domain '{feats['registered_domain']}' is a verified legitimate institution / authority."],
                "feature_contributions": [{"feature": "Whitelisted Master Domain", "impact": -100.0}],
                "recommended_action": "ALLOW"
            }

        # IP Address Host Check
        if feats["has_ip_host"]:
            score += 60.0
            reasons.append("URL uses numerical IP address instead of domain name (common evasion technique)")
            contributions.append({"feature": "IP Address Host", "impact": 60.0})

        # Brand Spoofing / Typosquatting
        if feats["spoofed_brand"]:
            score += 65.0
            reasons.append(f"Domain impersonates protected brand '{feats['spoofed_brand']}' without authorization")
            contributions.append({"feature": f"Brand Spoofing ({feats['spoofed_brand']})", "impact": 65.0})

        # Punycode / IDN Homoglyphs
        if feats["has_punycode"]:
            score += 45.0
            reasons.append("Domain uses Punycode / IDN homograph character substitution")
            contributions.append({"feature": "Punycode (IDN Homograph)", "impact": 45.0})

        # Shorteners
        if feats["is_shortened"]:
            score += 35.0
            reasons.append("URL is hidden behind an anonymous redirection shortener")
            contributions.append({"feature": "Obfuscated URL Shortener", "impact": 35.0})

        # High-risk / suspicious TLD
        if feats["has_suspicious_tld"]:
            score += 40.0
            reasons.append(f"High-abuse top-level domain frequently associated with phishing ({feats['suffix']})")
            contributions.append({"feature": f"Suspicious TLD ({feats['suffix']})", "impact": 40.0})

        # Open Redirect Pattern
        if feats["has_open_redirect"]:
            score += 35.0
            reasons.append("URL contains open redirect parameter targeting external site")
            contributions.append({"feature": "Open Redirect Abuse", "impact": 35.0})

        # High Shannon Entropy
        if feats["domain_entropy"] > 3.75:
            score += 30.0
            reasons.append(f"High entropy detected ({feats['domain_entropy']}), indicating algorithmically generated domain (DGA)")
            contributions.append({"feature": "Domain High Entropy (DGA)", "impact": 30.0})

        # Sensitive Keywords
        if feats["sensitive_keyword_count"] >= 2:
            score += 25.0
            reasons.append(f"URL contains {feats['sensitive_keyword_count']} credential-harvesting tokens ('login', 'verify', 'update')")
            contributions.append({"feature": "Sensitive Phishing Keywords", "impact": 25.0})
        elif feats["sensitive_keyword_count"] == 1:
            score += 15.0
            reasons.append("URL contains authentication keyword in path")
            contributions.append({"feature": "Authentication Keyword", "impact": 15.0})

        # Insecure Protocol
        if not feats["is_https"]:
            score += 15.0
            reasons.append("Insecure plain HTTP connection (lacks SSL/TLS certificate)")
            contributions.append({"feature": "Insecure Protocol (HTTP)", "impact": 15.0})

        # Subdomain Nesting Depth
        if feats["subdomain_count"] >= 3:
            score += 20.0
            reasons.append(f"Deep subdomain nesting ({feats['subdomain_count']} levels) used to obscure real host")
            contributions.append({"feature": "Deep Subdomain Nesting", "impact": 20.0})

        # Digits Ratio
        if feats["digits_ratio"] > 0.30:
            score += 15.0
            reasons.append("Abnormally high ratio of numerical digits in URL string")
            contributions.append({"feature": "High Digit-to-Char Ratio", "impact": 15.0})

        final_score = min(max(score, 5.0), 100.0)

        if final_score >= 75.0:
            risk_level = "CRITICAL"
            prediction = "MALICIOUS"
        elif final_score >= 55.0:
            risk_level = "HIGH"
            prediction = "PHISHING"
        elif final_score >= 30.0:
            risk_level = "MEDIUM"
            prediction = "SUSPICIOUS"
        elif final_score >= 15.0:
            risk_level = "LOW"
            prediction = "LOW_RISK"
        else:
            risk_level = "SAFE"
            prediction = "BENIGN"

        confidence = round(0.88 + (final_score / 900.0) if final_score > 50 else 0.94 - (final_score / 600.0), 3)
        confidence = min(max(confidence, 0.85), 0.99)

        return {
            "url": url,
            "risk_score": round(final_score, 1),
            "risk_level": risk_level,
            "prediction": prediction,
            "confidence": confidence,
            "features": feats,
            "reasons": reasons if reasons else ["URL exhibits standard legitimate lexical and structural characteristics."],
            "feature_contributions": sorted(contributions, key=lambda x: x["impact"], reverse=True),
            "recommended_action": "BLOCK" if final_score >= 55.0 else ("WARNING_PAGE" if final_score >= 30.0 else "ALLOW")
        }

    @staticmethod
    def evaluate_google_message(sender: str, raw_text: str, device_id: str = "android-01") -> Dict[str, Any]:
        result = analyze_smishing_message(sender=sender, raw_text=raw_text)
        result["device_id"] = device_id
        result["source_app"] = "com.google.android.apps.messaging"
        return result

    @staticmethod
    def evaluate_email(sender: str, subject: str, body: str) -> Dict[str, Any]:
        return analyze_email_content(sender=sender, subject=subject, body=body)

risk_engine = RiskEngine()
