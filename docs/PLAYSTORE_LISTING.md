# Google Play Store Listing & Compliance Guide: Lia AI

## 1. Release Artifact
- Target build: `assemblePlayRelease` (Play Store distribution)
- Output format: Android App Bundle (`app-play-release.aab`)
- Package Name: `com.Lia.assistant`

---

## 2. Listing Metadata

### App Title (<= 30 characters)
**Lia AI: Voice Companion**

### Short Description (<= 80 characters)
**Real-time voice AI companion with 10 personalities and 3D Website Forge.**

### Full Description
Meet **Lia AI**, your intelligent, voice-first personal companion powered by real-time speech and advanced intelligence.

Whether you converse in Hindi, Hinglish, or English, Lia adapts to your cadence, responds instantly with natural voice duplexing, and brings a delightful 3D presence to your phone.

**Key Features:**
- **Full-Duplex Voice Conversation:** Speak naturally with zero awkward wait times. Barge in and interrupt smoothly anytime.
- **Hindi, Hinglish & English Fluency:** Speaks your chosen language naturally turn by turn.
- **10 Distinct Personalities:** Choose between Friendly, Professional, Funny, Companion, Teacher, Developer, Foodie, Nautanki, and more.
- **3D Website Forge:** Request a website (e.g., "Lia, build a cafe website") and watch a responsive, interactive 3D landing page stream and compile live.
- **Phone Productivity:** Launch installed apps, lookup contacts, and prepare messages with voice ease.
- **100% Privacy Focused:** API keys and conversational memories reside securely on your device.

---

## 3. Data Safety Declarations

| Category | Collected? | Shared? | Purpose |
|---|---|---|---|
| Audio recordings / voice | No (Streamed ephemerally for transcription) | Shared with Google Gemini API during active session | App functionality |
| Contacts | No (Read locally on-device) | No | App functionality (Contact lookup) |
| Personal Info | No | No | N/A |
| Financial info / Health | No | No | N/A |
| Location | No | No | N/A |

---

## 4. Google Play Permission Justifications

1. **`RECORD_AUDIO` & `FOREGROUND_SERVICE_MICROPHONE`**
   - *Justification:* Essential for continuous, full-duplex conversational voice mode. An ongoing notification with a prominent Stop button is displayed while the microphone is active.

2. **`READ_CONTACTS`**
   - *Justification:* Used strictly when the user gives a spoken or typed request to contact someone (e.g. "Call Mom" or "Send WhatsApp to Rahul"). Contacts are queried on-device only.

3. **`CALL_PHONE`**
   - *Justification:* Allows initiating a direct phone call when the user commands the assistant to place an outgoing call.

---

## 5. Pre-Submission Checklist
- [x] All Google Play Accessibility policy compliance verified (Direct flavor contains accessibility automation; Play flavor excludes all accessibility and overlay services).
- [x] Adaptive icon configured with zero duplicate webp files.
- [x] Material 3 design tokens with 48dp minimum touch targets.
- [x] ProGuard/R8 configuration preserves org.json and WebKit bindings.
- [x] Release signing configured via git-ignored `keystore.properties`.
