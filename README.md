<div align="center">

# 🛡️ PhishGuard
### **Next-Gen Real-Time AI Smishing & Phishing Defense Platform**

[![FastAPI](https://img.shields.io/badge/FastAPI-0.109+-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com)
[![React](https://img.shields.io/badge/React-18.2+-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org)
[![Kotlin](https://img.shields.io/badge/Kotlin-Android_14+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com)
[![Manifest V3](https://img.shields.io/badge/Chrome_Extension-Manifest_V3-4285F4?style=for-the-badge&logo=googlechrome&logoColor=white)](https://developer.chrome.com/docs/extensions/mv3/intro/)
[![Python](https://img.shields.io/badge/Python-3.10+-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

<p align="center">
  <b>Intercepting, Analyzing, and Neutralizing Malicious Links & SMS Threats in Real-Time (&lt;40ms) Across Mobile, Web, and Cloud.</b>
</p>

[Key Capabilities](#-key-capabilities) •
[Architecture](#-system-architecture) •
[How It Works Step-by-Step](#-how-it-works-step-by-step) •
[Detection Engine](#-ai--detection-engine-deep-dive) •
[Quick Start](#-quick-start-guide) •
[Deployment Guide](#-production-deployment-guide) •
[API Reference](#-api-endpoints)

---

</div>

## 📌 Executive Overview

**PhishGuard** is an end-to-end, multi-channel cyber-defense platform engineered to address the fastest-growing mobile attack vector: **Smishing (SMS Phishing)** and social engineering delivered through **Google Messages**, default SMS apps, and cross-platform messengers.

While traditional defenses only inspect desktop web traffic or inbox emails, PhishGuard introduces **real-time on-device OS-level notification interception** combined with a high-throughput **Hybrid Statistical-Neural Risk Engine**, a **live WebSocket threat operations console**, and a **Chrome MV3 browser extension**.

```
📱 Android SMS Hook  ──►  ⚡ Sub-1ms On-Device ML  ──►  🧠 FastAPI Cloud AI (<40ms)  ──►  🚨 Instant Heads-Up Alarm
                                                                     │
                                                                     ▼
                                                      💻 Real-Time Analyst Dashboard
```

---

## 🌟 Key Capabilities

### 1. 📱 Real-Time Android & Google Messages Interception
- **`NotificationListenerService`**: Direct OS-level interception of incoming message notifications from Google Messages (`com.google.android.apps.messaging`), Samsung Messages, WhatsApp, and Telegram.
- **Sub-1ms On-Device ML Engine (`OnDeviceMLClassifier.kt`)**: Native Kotlin statistical NLP vector classifier calculating smishing probabilities offline without network latency.
- **High-Priority Heads-Up Alerts**: Triggers real-time Android warning dialogs and vibration alarms before the user clicks a deceptive link.
- **In-App Smishing Sandbox**: Built-in simulator with pre-loaded real-world attack vectors (Chase fraud, USPS redelivery, fake 2FA OTP).

### 2. ⚡ FastAPI Hybrid AI Risk Engine & XAI
- **30+ Lexical & Domain Features**: Shannon entropy analysis, IP host detection, URL shortener unmasking, Punycode homographs, and deep subdomain inspection.
- **Brand Typosquatting Matcher**: Levenshtein distance metrics against top-50 targeted global brands (Chase, Netflix, Apple, Google, PayPal, Amazon, etc.).
- **Hybrid AI Classifier**: Blends **55% Lexical/URL Heuristics + 45% Statistical NLP ML Probability**.
- **Legitimate 2FA Mitigation**: Suppresses false positives for genuine OTP codes (e.g., Google, Bank 2FA) and personal interpersonal messaging.
- **Explainable AI (XAI)**: Generates human-readable rationales detailing exact risk factors and percentage contributions.

### 3. 💻 Web Threat Operations Dashboard
- Built with **React 18 + TypeScript + Vite + Custom Glassmorphism Dark Theme**.
- **Live Threat Stream (`/ws/threat-stream`)**: WebSocket-powered live feed of all mobile and browser events.
- **Attack Simulator Studio**: 1-click test suite to dispatch customizable attack vectors and view instant AI feature breakdowns.
- **Deep Scanners**: Interactive URL and Email scanners with feature gauges, risk meters, and analyst feedback loops.

### 4. 🌐 Chrome Browser Extension (Manifest V3)
- Background service worker monitoring active tab navigation.
- Real-time tab risk scoring and badge warnings.
- Injects active threat overlay banners on high-risk credential-harvesting pages.

---

## 🏗️ System Architecture

```mermaid
flowchart TD
    subgraph "📱 Mobile Layer (Android)"
        A[Google Messages / SMS] -->|Notification Event| B[NotificationListenerService]
        B -->|Check On-Device ML <1ms| C{Local Critical?}
        C -->|Yes| D[🚨 Heads-Up Warning Alert]
        B -->|Async REST <40ms| E[FastAPI Gateway]
    end

    subgraph "🌐 Client Ecosystem"
        F[Chrome Extension MV3] -->|Tab URL Scan| E
        G[React Operations Console] -->|Manual Scanners| E
    end

    subgraph "🧠 Hybrid AI Risk Engine"
        E --> H[URL Lexical Feature Extractor]
        E --> I[NLP Smishing & Urgency Classifier]
        E --> J[Email Spoof & Header Analyzer]
        E --> K[Brand Typosquatting & Threat Bank]
        H & I & J & K --> L[Unified Risk Engine & XAI Generator]
    end

    subgraph "📊 Telemetry & Storage"
        L -->|Broadcast Alert| M[WebSocket Stream /ws/threat-stream]
        M --> G
        L -->|Persist Threat Event| N[(SQLite / PostgreSQL Database)]
        L -->|Risk Result JSON| B
    end
```

---

## 🔄 How It Works Step-by-Step

### 1. Attacker Dispatches Smishing SMS
An attacker sends a fraudulent SMS, e.g.:
> `"[CHASE-ALERT] Unauthorized transaction of $940.00 detected. Verify immediately: http://chase-security-auth.xyz/verify"`

### 2. OS Notification Interception (`<1ms`)
PhishGuard's [`GoogleMessagesListenerService.kt`](file:///c:/Users/appal/OneDrive/Attachments/Desktop/phising/android/app/src/main/java/com/phishguard/mobile/service/GoogleMessagesListenerService.kt) captures the notification before the user opens the SMS app.

### 3. On-Device ML Evaluation (`<1ms`)
[`OnDeviceMLClassifier.kt`](file:///c:/Users/appal/OneDrive/Attachments/Desktop/phising/android/app/src/main/java/com/phishguard/mobile/analyzer/OnDeviceMLClassifier.kt) computes token weights (`chase: +2.85`, `unauthorized: +3.10`, `xyz: +3.80`) and generates an offline risk score.

### 4. Async Cloud AI Verification (`<40ms`)
The backend calculates 30+ lexical features, brand typosquatting, and NLP urgency, returning an Explainable AI (XAI) risk score of **92.5 (CRITICAL)**.

### 5. Instant Protection
- **Phone:** Displays a high-priority Heads-Up warning alarm.
- **Web Dashboard:** Shows the threat in the live WebSocket feed with XAI breakdown.
- **Chrome Extension:** Injects a blocking banner if the link is opened on desktop.

---

## 🔬 AI & Detection Engine Deep Dive

### 📐 1. URL Lexical & Topological Features (`url_features.py`)

| Feature | Description | Threat Indicator |
|---|---|---|
| **Shannon Entropy** | Measures character randomness: $H(X) = -\sum P(x)\log_2 P(x)$ | $H > 3.75$ indicates DGA domains |
| **Brand Typosquatting** | Normalized Levenshtein distance against known brand keywords | Detects lookalikes like `chase-security-login.com` |
| **IP Host Detection** | Flags URLs using direct IPv4/IPv6 addresses instead of domains | Common in disposable phishing kits |
| **Punycode / Homograph** | Identifies `xn--` internationalized domain name spoofing | Visual deception attacks |
| **Suspicious TLD Bank** | Checks against high-abuse TLDs (`.xyz`, `.top`, `.tk`, `.icu`, `.buzz`, `.click`) | High correlation with malicious campaigns |

### 💬 2. NLP Smishing & Urgency Classification (`smishing_classifier.py`)
- **Multi-Category Rules:** Financial Fraud, Package Delivery Lures, Account Compromise, Government/IRS Lures, Crypto Giveaways.
- **Psychological Coercion Detection:** Flags urgent phrases (*"immediate action required"*, *"within 24 hours"*, *"account terminated"*).
- **False-Positive Mitigation:** Accurately isolates genuine 2FA OTP codes and casual chat phrases.

### ⚖️ 3. Calibrated Risk Matrix (`risk_engine.py`)

| Score Range | Risk Level | Action Recommended | UI Badge |
|:---:|:---:|:---:|:---:|
| **0 – 14** | `SAFE` | Allow / Benign | 🟢 Safe |
| **15 – 29** | `LOW` | Informational | 🔵 Low Risk |
| **30 – 54** | `MEDIUM` | Warning Banner | 🟡 Suspicious |
| **55 – 74** | `HIGH` | Block & Alert | 🟠 High Threat |
| **75 – 100** | `CRITICAL` | Heads-Up Alarm & Quarantine | 🔴 Critical Smishing |

---

## 📂 Repository Structure

```
phising/
├── 📱 android/                   # Native Android Companion Application (Kotlin + Jetpack Compose)
│   ├── app/src/main/
│   │   ├── java/com/phishguard/mobile/
│   │   │   ├── analyzer/         # LocalHeuristicEngine & OnDeviceMLClassifier (<1ms)
│   │   │   ├── network/          # Retrofit2 REST API Client & Data Models
│   │   │   ├── notification/     # ThreatNotificationHelper (Heads-Up Alert)
│   │   │   ├── service/          # GoogleMessagesListenerService (NotificationListenerService)
│   │   │   └── ui/               # Jetpack Compose Screens (Home, Feed, Simulator, Settings)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── ⚡ backend/                   # FastAPI Real-Time Risk & AI Engine (Python)
│   ├── app/
│   │   ├── api/                  # Endpoints (mobile, analyze, dashboard, feedback, websockets)
│   │   ├── core/                 # Config, DB connection, SQLAlchemy models
│   │   ├── ml/                   # Model weights, train_model.py, Smishing & Email analyzers
│   │   ├── services/             # Unified RiskEngine & Explainability (XAI) generator
│   │   └── main.py               # FastAPI application entrypoint & lifespan
│   ├── Dockerfile                # Production Backend Dockerfile
│   ├── tests/                    # Pytest test suite for ML and API layers
│   └── requirements.txt
│
├── 💻 frontend/                  # Security Operations Dashboard (React 18 + Vite + TypeScript)
│   ├── src/
│   │   ├── components/           # LiveFeed, Simulator, UrlScanner, EmailScanner, Navbar
│   │   ├── services/             # Axios API client & WebSocket connector
│   │   ├── types/                # TypeScript interface contracts
│   │   ├── App.tsx               # Main Dashboard application
│   │   └── index.css             # Custom Glassmorphism Cyber-Dark Design System
│   ├── Dockerfile                # Production Frontend Multi-Stage Dockerfile
│   ├── package.json
│   └── vite.config.ts
│
├── 🌐 extension/                 # Chrome Browser Extension (Manifest V3)
│   ├── manifest.json
│   ├── background.js             # Tab listener & async risk querier
│   ├── content.js                # Inline threat warning injection
│   ├── popup.html / popup.js     # Safety status popup widget
│   └── popup.css
│
├── docker-compose.yml            # 1-Command Full Stack Docker Deployment
├── PROJECT_REPORT.md             # In-depth executive & technical presentation report
└── README.md                     # Main repository documentation
```

---

## 🚀 Quick Start Guide (Local Development)

### 1. ⚡ Start Backend (FastAPI)
```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
- **API:** `http://localhost:8000` | **Docs:** `http://localhost:8000/docs`

### 2. 💻 Start Web Dashboard (React)
```bash
cd frontend
npm install
npm run dev
```
- **Dashboard:** `http://localhost:5173`

### 3. 📱 Start Android Companion App
1. Open `android/` in **Android Studio**.
2. Run on device or emulator (API 24+).
3. Tap **"Grant Google Messages Access"** on first launch.

### 4. 🌐 Load Chrome Extension
1. Go to `chrome://extensions/` -> Enable **Developer mode**.
2. Click **Load unpacked** and select the `extension/` folder.

---

## 🚢 Production Deployment Guide

### Option A: 🐳 1-Command Full-Stack Docker Deployment (Recommended)

PhishGuard includes production-ready Dockerfiles and a root `docker-compose.yml`.

1. **Build and Run All Services:**
   ```bash
   docker compose up -d --build
   ```

2. **Verify Containers are Running:**
   ```bash
   docker compose ps
   ```

3. **Access Services:**
   - **Backend API & Swagger Docs:** `http://localhost:8000/docs`
   - **Web Management Dashboard:** `http://localhost:5173`

4. **Stop Services:**
   ```bash
   docker compose down
   ```

---

### Option B: ☁️ Cloud Production Deployment

#### 1. Deploy FastAPI Backend (Render / Railway / AWS / GCP)
- **Runtime:** Python 3.10+ or Docker
- **Build Command:** `pip install -r backend/requirements.txt`
- **Start Command:** `uvicorn app.main:app --host 0.0.0.0 --port $PORT`
- **Environment Variables:**
  ```env
  PORT=8000
  HOST=0.0.0.0
  DATABASE_URL=sqlite:///./phishguard.db   # or postgresql://user:pass@host:5432/phishguard
  CORS_ORIGINS=*
  ```

#### 2. Deploy React Frontend (Vercel / Netlify / Cloudflare Pages)
- **Framework Preset:** Vite
- **Root Directory:** `frontend`
- **Build Command:** `npm run build`
- **Output Directory:** `dist`
- **Environment Variables:**
  ```env
  VITE_API_URL=https://your-backend-api.onrender.com
  VITE_WS_URL=wss://your-backend-api.onrender.com/ws/threat-stream
  ```

---

### Option C: 📱 Building Android Release APK / AAB

To package the Android app for physical distribution or the Google Play Store:

1. **Navigate to Android directory:**
   ```bash
   cd android
   ```

2. **Build Debug APK:**
   ```bash
   ./gradlew assembleDebug
   # APK output: android/app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Build Release APK:**
   ```bash
   ./gradlew assembleRelease
   # APK output: android/app/build/outputs/apk/release/app-release-unsigned.apk
   ```

4. **Build Android App Bundle (for Google Play):**
   ```bash
   ./gradlew bundleRelease
   # AAB output: android/app/build/outputs/bundle/release/app-release.aab
   ```

---

### Option D: 🌐 Deploying Chrome Browser Extension

1. **Package Extension for Distribution:**
   - Compress the `extension/` folder into a `.zip` archive:
     ```bash
     zip -r phishguard-extension.zip extension/ -x "*.DS_Store"
     ```
2. **Publish to Chrome Web Store:**
   - Log in to the [Chrome Developer Dashboard](https://chrome.google.com/webstore/devconsole).
   - Click **Add new item** and upload `phishguard-extension.zip`.
   - Fill in store metadata, screenshots, and submit for review.

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/mobile/analyze-message` | High-throughput mobile SMS analysis endpoint for Android service |
| `POST` | `/api/analyze/url` | Deep 30+ feature URL scanner with typosquatting detection |
| `POST` | `/api/analyze/sms` | Standalone SMS/text smishing classifier |
| `POST` | `/api/analyze/email` | Email header, domain spoofing, and attachment risk scanner |
| `GET` | `/api/dashboard/stats` | Global threat statistics, risk levels, and category breakdown |
| `GET` | `/api/dashboard/recent-threats` | Recent intercepted threat logs |
| `POST` | `/api/feedback/submit` | Human-in-the-loop analyst feedback submission |
| `WS` | `/ws/threat-stream` | Real-time WebSocket event feed for live threat monitoring |

---

## 🧪 Automated Testing

Run the automated test suite to verify ML classifiers and feature extractors:

```bash
pytest backend/tests/test_engine.py -v
```

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

<div align="center">
  <sub>Built with ❤️ by <b>Bharath</b> | Powered by FastAPI, React, Kotlin, Docker & Chrome MV3</sub>
</div>