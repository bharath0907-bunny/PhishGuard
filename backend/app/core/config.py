import os
# pyrefly: ignore [missing-import]
from pydantic import BaseModel


class Settings(BaseModel):
    PROJECT_NAME: str = "PhishGuard Real-Time Engine"
    API_V1_STR: str = "/api/v1"
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./phishguard.db")
    JWT_SECRET: str = os.getenv("JWT_SECRET", "super-secret-phishguard-key-change-in-prod")
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8000"))
    LOG_LEVEL: str = "INFO"
    CORS_ORIGINS: list[str] = ["*"]
    
    # Real-Time Smishing & URL detection thresholds
    CRITICAL_RISK_THRESHOLD: float = 80.0
    HIGH_RISK_THRESHOLD: float = 60.0
    MEDIUM_RISK_THRESHOLD: float = 35.0
    LOW_RISK_THRESHOLD: float = 15.0

settings = Settings()
