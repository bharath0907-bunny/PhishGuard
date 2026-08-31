from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session
from ..core.database import get_db
from ..core.models import FeedbackRecord, InterceptedMessage

feedback_router = APIRouter(prefix="/feedback", tags=["Model Retraining & User Feedback"])

class FeedbackPayload(BaseModel):
    target_id: str = Field(..., description="ID of the intercepted message or URL")
    target_type: str = Field("MESSAGE", example="MESSAGE", description="MESSAGE, URL, or EMAIL")
    verdict: str = Field(..., example="PHISHING", description="PHISHING or LEGITIMATE")
    comment: str = Field(None, example="Confirmed fake FedEx SMS tracking scam.")

@feedback_router.post("/submit")
def submit_feedback(payload: FeedbackPayload, db: Session = Depends(get_db)):
    """Accepts user feedback to train and calibrate the AI detection model."""
    record = FeedbackRecord(
        target_id=payload.target_id,
        target_type=payload.target_type,
        user_verdict=payload.verdict,
        comment=payload.comment
    )
    db.add(record)
    
    # If target is a message, update its feedback flag
    if payload.target_type == "MESSAGE":
        msg = db.query(InterceptedMessage).filter(InterceptedMessage.id == payload.target_id).first()
        if msg:
            msg.user_feedback = payload.verdict
            
    db.commit()
    return {"status": "SUCCESS", "message": "Feedback recorded for continuous model retraining."}

@feedback_router.get("/all")
def get_all_feedback(db: Session = Depends(get_db)):
    return db.query(FeedbackRecord).order_by(FeedbackRecord.created_at.desc()).all()
