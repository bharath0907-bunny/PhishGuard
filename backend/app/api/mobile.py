# pyrefly: ignore [missing-import]
from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks
# pyrefly: ignore [missing-import]
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
# pyrefly: ignore [missing-import]
from sqlalchemy.orm import Session
from ..core.database import get_db
from ..core.models import InterceptedMessage
from ..services.risk_engine import risk_engine
from .websocket import manager

mobile_router = APIRouter(prefix="/mobile", tags=["Google Messages Mobile Companion"])

class GoogleMessagePayload(BaseModel):
    sender: str = Field(..., example="+18005550199", description="Sender phone number or alphanumeric title")
    text: str = Field(..., example="URGENT: Wells Fargo Account #4920 Suspended. Verify now at http://wf-security-update.xyz/login", description="Notification text body")
    device_id: Optional[str] = Field("pixel-8-pro", description="Unique device identifier or hash")
    package_name: Optional[str] = Field("com.google.android.apps.messaging", description="Source Android application")
    timestamp: Optional[int] = Field(None, description="Unix timestamp (milliseconds)")

class MobileAnalysisResponse(BaseModel):
    id: str
    sender: str
    risk_score: float
    risk_level: str
    prediction: str
    confidence: float
    threat_categories: List[str]
    reasons: List[str]
    extracted_urls: List[Dict[str, Any]]
    recommended_action: str
    should_alert: bool

@mobile_router.post("/analyze-notification", response_model=MobileAnalysisResponse)
async def analyze_google_message_notification(
    payload: GoogleMessagePayload, 
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db)
):
    """
    Real-Time Interception Endpoint for Android NotificationListenerService.
    Targeting Google Messages (`com.google.android.apps.messaging`).
    Analyzes message in <40ms and dispatches live threat alert.
    """
    # 1. Run through AI Risk Engine
    analysis = risk_engine.evaluate_google_message(
        sender=payload.sender,
        raw_text=payload.text,
        device_id=payload.device_id or "android-device"
    )

    # 2. Persist record to database
    db_record = InterceptedMessage(
        sender=payload.sender,
        raw_text=payload.text,
        source_app=payload.package_name or "com.google.android.apps.messaging",
        device_id=payload.device_id or "android-device",
        extracted_urls=[u["url"] for u in analysis["extracted_urls"]],
        risk_score=analysis["risk_score"],
        risk_level=analysis["risk_level"],
        prediction=analysis["prediction"],
        confidence=analysis["confidence"],
        threat_categories=analysis["threat_categories"],
        reasons=analysis["reasons"],
        action_taken=analysis["recommended_action"]
    )
    db.add(db_record)
    db.commit()
    db.refresh(db_record)

    should_alert = analysis["risk_score"] >= 35.0

    # 3. Broadcast real-time event to Web Management Live Feed over WebSocket
    event_payload = {
        "type": "NEW_GOOGLE_MESSAGE_INTERCEPTED",
        "data": {
            "id": db_record.id,
            "sender": db_record.sender,
            "text": db_record.raw_text,
            "source_app": db_record.source_app,
            "device_id": db_record.device_id,
            "risk_score": db_record.risk_score,
            "risk_level": db_record.risk_level,
            "prediction": db_record.prediction,
            "threat_categories": db_record.threat_categories,
            "reasons": db_record.reasons,
            "extracted_urls": analysis["extracted_urls"],
            "created_at": db_record.created_at.isoformat(),
            "should_alert": should_alert
        }
    }
    background_tasks.add_task(manager.broadcast, event_payload)

    return MobileAnalysisResponse(
        id=db_record.id,
        sender=db_record.sender,
        risk_score=db_record.risk_score,
        risk_level=db_record.risk_level,
        prediction=db_record.prediction,
        confidence=db_record.confidence,
        threat_categories=db_record.threat_categories,
        reasons=db_record.reasons,
        extracted_urls=analysis["extracted_urls"],
        recommended_action=analysis["recommended_action"],
        should_alert=should_alert
    )

@mobile_router.get("/recent-intercepts")
def get_recent_intercepts(limit: int = 20, db: Session = Depends(get_db)):
    """Fetch list of recent Google Messages intercepts for mobile or management view."""
    records = db.query(InterceptedMessage).order_by(InterceptedMessage.created_at.desc()).limit(limit).all()
    return records
