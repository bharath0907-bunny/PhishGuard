import pytest
try:
    from backend.app.ml.url_features import extract_url_features
    from backend.app.ml.smishing_classifier import analyze_smishing_message, extract_urls_from_text
    from backend.app.ml.email_analyzer import analyze_email_content
    from backend.app.services.risk_engine import risk_engine
except ImportError:
    from app.ml.url_features import extract_url_features
    from app.ml.smishing_classifier import analyze_smishing_message, extract_urls_from_text
    from app.ml.email_analyzer import analyze_email_content
    from app.services.risk_engine import risk_engine


def test_extract_urls_from_text():
    sample = "URGENT: Verify your account at http://chase-security.xyz/login now!"
    urls = extract_urls_from_text(sample)
    assert len(urls) == 1
    assert "chase-security.xyz" in urls[0]

def test_smishing_classifier_chase_scam():
    sender = "+1 (800) 555-0199"
    text = "[CHASE-ALERT] Unauthorized transaction of $940.00 detected. Verify identity immediately: http://chase-security-auth.xyz/verify"
    result = analyze_smishing_message(sender, text)
    
    assert result["risk_score"] >= 60.0
    assert result["risk_level"] in ["HIGH", "CRITICAL"]
    assert result["prediction"] == "SMISHING"
    assert "Psychological Coercion / Urgency" in result["threat_categories"]
    assert len(result["extracted_urls"]) == 1

def test_smishing_classifier_safe_otp():
    sender = "Google"
    text = "G-492810 is your Google verification code. Do not share this code with anyone."
    result = analyze_smishing_message(sender, text)
    
    assert result["risk_score"] < 35.0
    assert result["risk_level"] == "SAFE"
    assert result["prediction"] == "BENIGN"
    assert result["is_safe_2fa"] is True

def test_url_features_ip_host():
    url = "http://192.168.1.105/usps/redeliver"
    feats = extract_url_features(url)
    assert feats["has_ip_host"] is True
    
    eval_res = risk_engine.evaluate_url(url)
    assert eval_res["risk_score"] >= 50.0

def test_url_features_whitelisted_domain():
    url = "https://accounts.google.com/signin/v2"
    feats = extract_url_features(url)
    assert feats["is_whitelisted"] is True
    
    eval_res = risk_engine.evaluate_url(url)
    assert eval_res["risk_score"] == 0.0
    assert eval_res["risk_level"] == "SAFE"

def test_email_analyzer_spoofing():
    sender = "support@paypal.notice-billing.com"
    subject = "URGENT: Unauthorized Transaction Detected - Account Frozen"
    body = "Please verify your account details at http://paypal-update.xyz/login within 24 hours."
    
    res = analyze_email_content(sender, subject, body)
    assert res["risk_score"] >= 60.0
    assert res["risk_level"] in ["HIGH", "CRITICAL"]
    assert any("Sender Spoofing" in cat for cat in res["threat_categories"])
