# pyrefly: ignore [missing-import]
from fastapi import APIRouter, Depends, BackgroundTasks
# pyrefly: ignore [missing-import]
from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional
# pyrefly: ignore [missing-import]
from sqlalchemy.orm import Session
from ..core.database import get_db
from ..core.models import UrlScanRecord, EmailScanRecord
from ..services.risk_engine import risk_engine
from .websocket import manager

analyze_router = APIRouter(prefix="/analyze", tags=["General Phishing Analysis Engine"])

class UrlScanRequest(BaseModel):
    url: str = Field(..., example="http://paypal-security-update.xyz/login.php")
    source: Optional[str] = Field("WEB_SCANNER", example="WEB_SCANNER")

class EmailScanRequest(BaseModel):
    sender: str = Field(..., example="security-alert@paypal.notice-billing.com")
    subject: str = Field(..., example="URGENT: Unauthorized Transaction Detected - Account Frozen")
    body: str = Field(..., example="Dear Customer, click here http://192.168.1.1/verify to prevent permanent closure.")

@analyze_router.post("/url")
async def scan_url(
    payload: UrlScanRequest, 
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db)
):
    """Deep Lexical & Domain Feature Scan for URLs."""
    result = risk_engine.evaluate_url(payload.url, source=payload.source)
    
    # Store record
    record = UrlScanRecord(
        url=payload.url,
        normalized_domain=result["features"].get("tld", ""),
        risk_score=result["risk_score"],
        risk_level=result["risk_level"],
        prediction=result["prediction"],
        confidence=result["confidence"],
        features=result["features"],
        reasons=result["reasons"],
        source=payload.source
    )
    db.add(record)
    db.commit()
    db.refresh(record)
    
    # Broadcast scan
    background_tasks.add_task(manager.broadcast, {
        "type": "NEW_URL_SCANNED",
        "data": {
            "id": record.id,
            "url": record.url,
            "risk_score": record.risk_score,
            "risk_level": record.risk_level,
            "prediction": record.prediction,
            "reasons": record.reasons,
            "source": record.source,
            "created_at": record.created_at.isoformat()
        }
    })
    
    result["id"] = record.id
    return result

@analyze_router.post("/email")
async def scan_email(
    payload: EmailScanRequest,
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db)
):
    """Analyzes email headers, sender domain mismatch, urgency, and embedded links."""
    result = risk_engine.evaluate_email(payload.sender, payload.subject, payload.body)
    
    record = EmailScanRecord(
        sender=payload.sender,
        subject=payload.subject,
        body=payload.body,
        risk_score=result["risk_score"],
        risk_level=result["risk_level"],
        prediction=result["prediction"],
        reasons=result["reasons"]
    )
    db.add(record)
    db.commit()
    db.refresh(record)
    
    background_tasks.add_task(manager.broadcast, {
        "type": "NEW_EMAIL_SCANNED",
        "data": {
            "id": record.id,
            "sender": record.sender,
            "subject": record.subject,
            "risk_score": record.risk_score,
            "risk_level": record.risk_level,
            "prediction": record.prediction,
            "created_at": record.created_at.isoformat()
        }
    })
    
    result["id"] = record.id
    return result
