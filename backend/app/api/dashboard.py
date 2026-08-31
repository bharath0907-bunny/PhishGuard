from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import Dict, Any, List
from ..core.database import get_db
from ..core.models import InterceptedMessage, UrlScanRecord, EmailScanRecord, FeedbackRecord

dashboard_router = APIRouter(prefix="/dashboard", tags=["Management Dashboard Analytics"])

@dashboard_router.get("/stats")
def get_dashboard_stats(db: Session = Depends(get_db)) -> Dict[str, Any]:
    """Provides high-level cybersecurity dashboard metrics."""
    total_messages = db.query(InterceptedMessage).count()
    smishing_blocked = db.query(InterceptedMessage).filter(InterceptedMessage.risk_score >= 60.0).count()
    suspicious_messages = db.query(InterceptedMessage).filter(InterceptedMessage.risk_score.between(35.0, 59.9)).count()
    safe_messages = db.query(InterceptedMessage).filter(InterceptedMessage.risk_score < 35.0).count()
    
    total_urls = db.query(UrlScanRecord).count()
    malicious_urls = db.query(UrlScanRecord).filter(UrlScanRecord.risk_score >= 60.0).count()
    
    total_emails = db.query(EmailScanRecord).count()
    phishing_emails = db.query(EmailScanRecord).filter(EmailScanRecord.risk_score >= 60.0).count()
    
    # Calculate average risk score
    avg_message_risk = db.query(func.avg(InterceptedMessage.risk_score)).scalar() or 0.0
    
    return {
        "google_messages": {
            "total_intercepted": total_messages,
            "smishing_blocked": smishing_blocked,
            "suspicious_warned": suspicious_messages,
            "safe_passed": safe_messages,
            "avg_risk_score": round(float(avg_message_risk), 1)
        },
        "url_scans": {
            "total": total_urls,
            "malicious": malicious_urls,
            "safe": total_urls - malicious_urls
        },
        "email_scans": {
            "total": total_emails,
            "phishing": phishing_emails,
            "safe": total_emails - phishing_emails
        },
        "system_status": {
            "realtime_engine": "ACTIVE",
            "active_devices": 1 if total_messages > 0 else 0,
            "model_version": "PhishGuard-SmishX-v2.4",
            "latency_ms": 32.4
        }
    }

@dashboard_router.get("/live-feed")
def get_live_threat_feed(limit: int = 30, db: Session = Depends(get_db)) -> List[Dict[str, Any]]:
    """Returns combined real-time live threat feed across all channels."""
    messages = db.query(InterceptedMessage).order_by(InterceptedMessage.created_at.desc()).limit(limit).all()
    urls = db.query(UrlScanRecord).order_by(UrlScanRecord.created_at.desc()).limit(limit).all()
    
    feed = []
    for m in messages:
        feed.append({
            "id": m.id,
            "type": "GOOGLE_MESSAGE",
            "title": f"Google Message from {m.sender}",
            "content": m.raw_text,
            "risk_score": m.risk_score,
            "risk_level": m.risk_level,
            "prediction": m.prediction,
            "threat_categories": m.threat_categories,
            "reasons": m.reasons,
            "created_at": m.created_at.isoformat(),
            "source_app": m.source_app
        })
    for u in urls:
        feed.append({
            "id": u.id,
            "type": "URL_SCAN",
            "title": f"URL Scan: {u.url[:40]}...",
            "content": u.url,
            "risk_score": u.risk_score,
            "risk_level": u.risk_level,
            "prediction": u.prediction,
            "threat_categories": ["Malicious Link"] if u.risk_score >= 60.0 else [],
            "reasons": u.reasons,
            "created_at": u.created_at.isoformat(),
            "source_app": u.source
        })
    
    # Sort combined feed by timestamp descending
    feed.sort(key=lambda x: x["created_at"], reverse=True)
    return feed[:limit]
