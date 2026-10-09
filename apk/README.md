# APK Drops

Prebuilt debug APKs for on-device testing and pilot recruitment. **Debug builds are
self-signed with the Android debug key — never distribute these as releases.**

## Current build

| | |
| --- | --- |
| File | `RescueDeskAI-v0.1.0-scaffold-e5545d0-debug.apk` |
| Version | `0.1.0-scaffold` (versionCode 1) |
| Git commit | `e5545d0` |
| Date | 2026-10-10 |
| SHA-256 | `76ae446d7f6dca4a234e8105c64fd94f44d86506445408b1bd90578df9289555` |
| Size | ~71 MB (grew from ~18 MB: MediaPipe native libs across 4 ABIs) |
| Min Android | 8.0 (API 26) |

**Included:** onboarding (language + text size), Home, Emergency Help routing,
8 built-in guides in EN + Filipino (typhoon/flood/earthquake/fire — UNREVIEWED,
rights status pending), guide detail with review banners, local search with a
conservative **Filipino morphology** expander (`bumaha` → `baha`, `maglilindol`
→ `lindol`), Family Plan wizard, emergency contacts (system-dialer calling),
bilingual Go-Bag checklist, full Filipino UI (Settings → Language → Filipino),
Screen L — Offline & Download Manager (guide-pack pipeline present; server URL
is a placeholder until the content repo ships), Settings, **delete all personal
information** in Settings → Privacy (household plan, contacts, go-bag wiped
atomically; guides and language settings survive), a bilingual privacy notice,
and **Ask AI in local mode** — offline, retrieval-grounded answers composed
only from the on-device guides, with citations, PRD §5.8 safety refusals
(live-claim + out-of-scope), and a new **medical-emergency escalation**
(bilingual) that routes symptom / urgency wording to 911 + nearest hospital
instead of a bland no-match. A code-side **TalkBack pass** shipped in this
build: roles + selected state on onboarding choices, decorative icons marked,
stable field descriptions, live region on the Ask thread, and minimum tap
targets on chips + Call/Delete.
**Included (new in this build):** an **experimental on-device LLM path** —
Gemma 2 2B IT via MediaPipe LLM Inference, behind
Settings → Experimental → "Try local LLM" (**off by default**). When enabled
AND a model file is present on the device, Ask AI rephrases the
retrieval-grounded answer into a short paragraph with an
"Rephrased by local AI (experimental)" badge; source citations always remain,
and refusals / medical escalation / no-match never reach the model. Without
the toggle or the model file, Ask AI behaves exactly as before. The model
file itself is **not** bundled (see below).
**Not included:** a bundled model asset or automatic download — the LLM path
is candidate evaluation only and still gated behind the Phase 1 Go/No-Go
decision (PRD §7.2).
**Note:** first launch on a phone that ran a pre-Room-v2 build resets the
household plan / contacts / go-bag once (schema change for the bilingual
checklist).

## Install

USB (Developer Options → USB debugging on):

```bash
adb install -r apk/RescueDeskAI-v0.1.0-scaffold-e5545d0-debug.apk
```

Or copy the file to the phone and open it (allow "install unknown apps" for
your file manager — expected for test builds).

### Optional: enable the experimental local LLM

The Gemma 2 2B IT int8 `.task` file (~1.4 GB) is not in the APK. To test the
LLM path on a physical phone (recommend 4 GB+ RAM, ~2 GB free storage):

1. Download the Gemma 2 2B IT int8 `.task` — Google distributes it via
   Kaggle (`google/gemma2/on-device`) or the litert-community mirror on
   Hugging Face (`litert-community/Gemma2-2B-IT`).
2. Rename it and push it to the debug lookup path:

```bash
adb shell mkdir -p /data/local/tmp/rescuedesk
adb push gemma2b.task /data/local/tmp/rescuedesk/
```

3. Open the app → Settings → Experimental → toggle **"Try local LLM
   (Gemma 2 2B IT)"**. Status should flip to *Ready* after the load.
4. Ask a grounded question (e.g. "What should I do during an earthquake?").
   The answer appears as one paragraph with the experimental badge.

The app also checks `<app files>/models/gemma2b.task` and
`Android/data/com.rescuedesk.ai/files/models/gemma2b.task`. Files under
500 MB are treated as truncated and ignored.

## Rebuild from source

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

## Conventions

- Filename: `RescueDeskAI-v<versionName>-<git-short-hash>-debug.apk`
- One APK per drop; delete superseded files before committing (keep this folder
  to a single current build — GitHub blocks files over 100 MB, and the MediaPipe
  native libs push the debug APK to ~71 MB; ABI splits are the lever if it grows
  further).
