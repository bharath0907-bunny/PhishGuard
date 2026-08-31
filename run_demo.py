"""
PhishGuard - Real-Time AI Smishing & Phishing Defense Platform
Master Demo & Execution Script
"""

import sys
import os
import uvicorn

# Ensure project root is in sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

def main():
    print("===================================================================")
    print("🛡️  PHISHGUARD • REAL-TIME GOOGLE MESSAGES & PHISHING DEFENSE ENGINE")
    print("===================================================================")
    print("• FastAPI Backend:       http://localhost:8000")
    print("• Interactive Swagger:   http://localhost:8000/docs")
    print("• Live Threat Stream:    ws://localhost:8000/ws/threat-stream")
    print("• Mobile API Endpoint:   POST /api/v1/mobile/analyze-notification")
    print("===================================================================\n")

    uvicorn.run("backend.app.main:app", host="0.0.0.0", port=8000, reload=True)

if __name__ == "__main__":
    main()
