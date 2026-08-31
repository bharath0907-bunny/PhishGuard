@echo off
title PhishGuard Backend Server
echo ===================================================
echo Starting PhishGuard Real-Time Defense Backend...
echo ===================================================
pip install -r backend\requirements.txt
python run_demo.py
pause
