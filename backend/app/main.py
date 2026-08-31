# pyrefly: ignore [missing-import]
from fastapi import FastAPI
# pyrefly: ignore [missing-import]
from fastapi.middleware.cors import CORSMiddleware
from .core.config import settings
from .core.database import engine, Base, SessionLocal
from .core.models import InterceptedMessage, UrlScanRecord
from .api.mobile import mobile_router
from .api.analyze import analyze_router
from .api.dashboard import dashboard_router
from .api.feedback import feedback_router
from .api.websocket import ws_router
from .services.risk_engine import risk_engine

# Create database tables automatically
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="PhishGuard - Real-Time AI Smishing & Phishing Defense Engine",
    description="Real-Time detection and defense platform for Google Messages (SMS), URLs, and Emails.",
    version="2.4.0"
)

# CORS configuration for Web Dashboard, Android App, and Chrome Extension
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register API Routers
app.include_router(mobile_router, prefix=settings.API_V1_STR)
app.include_router(analyze_router, prefix=settings.API_V1_STR)
app.include_router(dashboard_router, prefix=settings.API_V1_STR)
app.include_router(feedback_router, prefix=settings.API_V1_STR)
app.include_router(ws_router)

@app.on_event("startup")
def seed_initial_demo_data():
    """Seeds realistic smishing and phishing detection examples on first run."""
    db = SessionLocal()
    try:
        count = db.query(InterceptedMessage).count()
        if count == 0:
            demo_messages = [
                {
                    "sender": "+1 (800) 555-0199",
                    "text": "[CHASE-ALERT] We detected an unauthorized transaction of $940.00 on your debit card. If this wasn't you, verify immediately: http://chase-security-auth.xyz/verify",
                },
                {
                    "sender": "USPS-TRACKING",
                    "text": "USPS: Your package #US9482710 cannot be delivered due to missing house number. Please update delivery address within 12 hours: http://192.168.1.105/usps/redeliver",
                },
                {
                    "sender": "NETFLIX",
                    "text": "Your Netflix membership has been suspended due to billing error. Please update your payment method: http://netflix-billing-update.top/account",
                },
                {
                    "sender": "Google",
                    "text": "G-492810 is your Google verification code. Do not share this code with anyone.",
                },
                {
                    "sender": "+1 (415) 889-1022",
                    "text": "Hey John, are we still meeting for lunch tomorrow at 12:30 PM at the diner?",
                }
            ]
            
            for item in demo_messages:
                analysis = risk_engine.evaluate_google_message(item["sender"], item["text"])
                msg = InterceptedMessage(
                    sender=item["sender"],
                    raw_text=item["text"],
                    source_app="com.google.android.apps.messaging",
                    device_id="pixel-8-pro-live",
                    extracted_urls=[u["url"] for u in analysis["extracted_urls"]],
                    risk_score=analysis["risk_score"],
                    risk_level=analysis["risk_level"],
                    prediction=analysis["prediction"],
                    confidence=analysis["confidence"],
                    threat_categories=analysis["threat_categories"],
                    reasons=analysis["reasons"],
                    action_taken=analysis["recommended_action"]
                )
                db.add(msg)
            
            # Add demo URL scans
            demo_urls = [
                "http://secure-paypal-login.xyz/update-wallet",
                "http://wellsfargo-online-fraud-verification.biz/login.php",
                "https://accounts.google.com/signin/v2/identifier"
            ]
            for u in demo_urls:
                res = risk_engine.evaluate_url(u)
                record = UrlScanRecord(
                    url=u,
                    normalized_domain=res["features"].get("tld", ""),
                    risk_score=res["risk_score"],
                    risk_level=res["risk_level"],
                    prediction=res["prediction"],
                    confidence=res["confidence"],
                    features=res["features"],
                    reasons=res["reasons"]
                )
                db.add(record)

            db.commit()
    finally:
        db.close()

@app.get("/")
def root_status():
    return {
        "service": "PhishGuard Real-Time Detection Engine",
        "status": "ONLINE",
        "supported_channels": ["Google Messages (com.google.android.apps.messaging)", "URL Scanner", "Email Engine"],
        "docs_url": "/docs",
        "websocket_stream": "/ws/threat-stream"
    }

@app.get("/health")
def health_check():
    return {"status": "HEALTHY", "engine": "ACTIVE", "version": "2.4.0"}

if __name__ == "__main__":
    # pyrefly: ignore [missing-import]
    import uvicorn
    uvicorn.run("backend.app.main:app", host=settings.HOST, port=settings.PORT, reload=True)
