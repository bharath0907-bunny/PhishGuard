import uuid
from datetime import datetime
# pyrefly: ignore [missing-import]
from sqlalchemy import Column, String, Float, Integer, Boolean, Text, DateTime, JSON
from .database import Base

class InterceptedMessage(Base):
    __tablename__ = "intercepted_messages"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    sender = Column(String, index=True, nullable=False) # e.g. "+18005550199", "WELLS FARGO", "USPS-ALERT"
    raw_text = Column(Text, nullable=False)
    source_app = Column(String, default="com.google.android.apps.messaging") # Google Messages
    device_id = Column(String, index=True, default="android-device-01")
    extracted_urls = Column(JSON, default=list) # List of extracted URLs
    
    risk_score = Column(Float, nullable=False) # 0 to 100
    risk_level = Column(String, nullable=False) # CRITICAL, HIGH, MEDIUM, LOW, SAFE
    prediction = Column(String, nullable=False) # PHISHING / SMISHING / BENIGN
    confidence = Column(Float, default=0.95)
    
    threat_categories = Column(JSON, default=list) # e.g. ["Banking Impersonation", "Urgent Coercion", "Malicious Link"]
    reasons = Column(JSON, default=list) # Human explainable reasons
    action_taken = Column(String, default="HEADS_UP_ALERT_DISPATCHED") # BLOCKED, ALERTED, PASSED
    
    created_at = Column(DateTime, default=datetime.utcnow)
    is_read = Column(Boolean, default=False)
    user_feedback = Column(String, nullable=True) # "CONFIRMED_THREAT", "FALSE_POSITIVE"

class UrlScanRecord(Base):
    __tablename__ = "url_scans"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    url = Column(String, index=True, nullable=False)
    normalized_domain = Column(String, index=True, nullable=False)
    
    risk_score = Column(Float, nullable=False)
    risk_level = Column(String, nullable=False)
    prediction = Column(String, nullable=False)
    confidence = Column(Float, default=0.95)
    
    features = Column(JSON, default=dict) # 30+ lexical & domain features
    reasons = Column(JSON, default=list)
    threat_intel_match = Column(Boolean, default=False)
    threat_intel_source = Column(String, nullable=True)
    
    source = Column(String, default="WEB_SCANNER") # WEB_SCANNER, EXTENSION, GOOGLE_MESSAGES
    created_at = Column(DateTime, default=datetime.utcnow)

class EmailScanRecord(Base):
    __tablename__ = "email_scans"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    sender = Column(String, nullable=False)
    subject = Column(String, nullable=False)
    body = Column(Text, nullable=False)
    
    risk_score = Column(Float, nullable=False)
    risk_level = Column(String, nullable=False)
    prediction = Column(String, nullable=False)
    reasons = Column(JSON, default=list)
    created_at = Column(DateTime, default=datetime.utcnow)

class ThreatRule(Base):
    __tablename__ = "threat_rules"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    rule_type = Column(String, nullable=False) # KEYWORD, REGEX, SENDER_BLACKLIST, DOMAIN_BLACKLIST, WHITELIST
    pattern = Column(String, nullable=False)
    risk_modifier = Column(Float, default=30.0)
    category = Column(String, default="General Threat")
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)

class FeedbackRecord(Base):
    __tablename__ = "feedback"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    target_id = Column(String, nullable=False) # ID of message/url
    target_type = Column(String, nullable=False) # MESSAGE, URL, EMAIL
    user_verdict = Column(String, nullable=False) # PHISHING, LEGITIMATE
    comment = Column(Text, nullable=True)
    status = Column(String, default="PENDING_REVIEW") # PENDING_REVIEW, APPROVED, REJECTED
    created_at = Column(DateTime, default=datetime.utcnow)
