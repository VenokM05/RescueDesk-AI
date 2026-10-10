<p align="center">
  <img src="docs/images/rescuedesk-logo-horizontal.png" alt="RescueDesk AI logo" width="520"/>
</p>

# RescueDesk AI

**Offline-first emergency preparedness for Philippine households — in Filipino and English, on your phone, with no accounts, no tracking, and no internet required.**

RescueDesk AI puts tested, life-safety guidance within reach when the signal is down — during a typhoon, an earthquake, a flood, or a house fire. Everything core to the app runs fully on the device. You can open it in airplane mode and still read the guides, search in either language, build a family plan, and ask "Ask Juan" a question.

> **Status:** v0.1.0 scaffold — a working, demoable Android app. Emergency-guide content is present but **pending review by qualified responders**. The on-device AI model has been **successfully loaded and run on a real test phone** (Redmi Note 14 5G) and is now in active on-device evaluation toward the Phase 1 Go/No-Go decision — still experimental and gated. See [Project status](#project-status--roadmap).

---

## Why this app exists

The Philippines faces roughly **twenty typhoons a year**, plus earthquakes, volcanic activity, fires, and flash floods. In those first critical minutes, the difference between panic and a calm, correct action is often just *knowing what to do* — but a lot of lifesaving information lives in apps that need internet, an account, a data plan, or English fluency.

RescueDesk AI is built for the household that doesn't have those things:

- **Works with zero connectivity.** The guides, search, family plan, contacts, and the default Ask Juan answers are all on-device.
- **Built for everyone.** Full Filipino and English support, large adjustable text, senior-friendly sizing, and TalkBack accessibility pass.
- **Private by design.** No accounts, no sign-up, no analytics, no cloud. Nothing about you or your family ever leaves the phone. There is a one-tap "delete all personal information" control.
- **Prepared, not just reactive.** Go-bag checklists, a household plan wizard, and emergency contacts help families get ready *before* the disaster — not just during it.

Because being prepared should not depend on signal strength.

---

## Key features

### Emergency guides
A built-in library of step-by-step guides for the hazards that matter most in the Philippines — **Typhoon, Flood, Earthquake, and Fire** — each with a quick summary, a "Do this first" action list, and an "Avoid these actions" caution list, in **both Filipino and English**. The guides are seeded into the app so they open instantly and offline.

> Content rights note: the built-in guides currently ship with a **"Pending review"** status. They are original, practical summaries, but they have not yet been validated by a qualified subject-matter reviewer or cleared for official redistribution. In a real emergency, always follow instructions from local authorities (PRD sections 8.3 and 10.3).

### Ask Juan — on-device emergency assistant
Ask Juan answers emergency-preparedness questions using **retrieval-grounded** responses composed only from the guides installed on your device, with the source guide shown for every answer. It runs offline by default and never invents hotline numbers, dosages, prices, distances, dates, or current conditions. See [What the AI can do](#what-the-ai-can-do) for details and its honest limits.

### Family & household planning
- **Household plan wizard** — a guided, multi-step plan (members, meeting places, an out-of-area contact, reminders) that is saved locally and survives restarts.
- **Emergency contacts** — quick calling through the system dialer (e.g. 911 and local numbers), never a background auto-dial.
- **Go-bag checklist** — a bilingual "grab-bag" checklist you can customize with your own items.

### Privacy & data control
- No accounts and no tracking. No telemetry or analytics are collected.
- **Delete all personal information** in Settings → Privacy — wipes the household plan, contacts, and go-bag atomically, while keeping your language and text-size preferences.
- A plain-language privacy notice in your chosen language.

### Accessibility & bilingual support
- Full **Filipino** and **English** user interface and content.
- Adjustable in-app text size (Normal / Large / Extra large) on top of the system font scale.
- A TalkBack pass: meaningful roles and labels, decorative icons hidden from readers, live announcements for assistant replies, and large tap targets.
- **Filipino-aware search** that understands common verb forms (e.g. *bumaha* → *baha*, *maglilindol* → *lindol*), plus tolerant Taglish queries.

---

## What the AI can do

Ask Juan has two modes. The first is always available; the second is experimental.

### 1. Retrieval-grounded answers (default, fully offline)
This is the shipping behavior and requires **no model and no internet**:

- Your question is matched against the on-device guides using **relevance-ranked local search** (title, summary, and body are scored against your exact words, with Filipino morphology expansion), so a typhoon question grounds on the typhoon guide and an earthquake question on the earthquake guide.
- The answer is assembled **strictly from guide text** — a short lead, the steps to take, any caution, and the source guide(s) — so it cannot fabricate instructions that aren't in a guide. Source guides are tappable chips that open the full guide.
- **Safety refusals are enforced first.** If you ask about *current/live* conditions ("Ano ang current storm signal ngayon?", "is it raining right now?"), Ask Juan refuses and points you to official channels rather than guessing. If a question is **out of scope** (not emergency preparedness), it declines politely. If the wording suggests a **medical emergency** (heavy bleeding, unconsciousness, difficulty breathing, seizures, poisoning, etc.), it escalates to professional help (call 911 / nearest hospital) instead of answering.
- When nothing matches, Ask Juan shows the **real guide topics installed on your device** as tappable chips — so you're redirected to actual help instead of a dead-end "no results."

### 2. On-device generative rephrasing (experimental, optional, off by default)
Behind **Settings → Experimental → "Try local LLM"**, an on-device language model can rephrase the grounded answer into a shorter, more natural paragraph (the answer is marked with a small "rephrased on-device" badge). This is **candidate evaluation only**:

- It is only ever invoked *after* the safety refusals above, and only for answers already grounded in your guides. Source citations always remain; refusals, medical escalations, and no-match never reach the model.
- If the model isn't present or fails, Ask Juan silently stays on the composed grounded answer.
- The toggle **enables itself only when the full model file is detected on the device** (it shows a clear ON/OFF state and explains why when locked), and Settings includes a **Re-check** button with instant feedback — no reinstall needed after placing the model file.
- Runs **fully offline** — the question and your guides are never sent anywhere.

**Real-device status (October 2026).** The Gemma 2 2B q8 model loads and generates on an 8 GB-class mid-range phone (Redmi Note 14 5G, CPU backend). Rewrites take roughly **30–90 seconds** on CPU and the model legitimately uses most of the free RAM while running — close other apps before testing. Generation stability fixes shipped in the current build: a token pre-flight, a fresh inference session per question, and a crash-proof token budget (crossing MediaPipe's maxTokens boundary used to abort the app natively on debug builds).

**Honest limits.** Ask Juan does not know what is happening right now (no live weather, road, or signal data) and is not a substitute for professional medical or emergency services. Use it to prepare and to understand what to do — not as a live information feed.

---

## On-device AI model — candidate & requirements

The experimental generative path is currently built around **Gemma 2 2B IT (int8/q8)**, run through **Google MediaPipe LLM Inference** (`com.google.mediapipe:tasks-genai:0.10.27`), on the CPU backend.

**Model availability depends on your device.** The q8 Gemma 2 2B file is **~2.7 GB** and realistically needs:

| Factor | Practical requirement |
| --- | --- |
| RAM | ~8 GB-class phone for comfortable use (verified on a 7.7 GB RAM device); 4 GB-class phones will not fit the model |
| Free storage | At least ~3 GB for the model, alongside guides and your data |
| Android | 8.0 (API 26) or newer |
| Patience | CPU-only generation is ~2–5 tokens/second — a rephrase takes 30–90 s |

> The model file is **not** bundled in the APK and is **not** committed to this repository. It is a license-gated Google artifact (Hugging Face `litert-community/Gemma2-2B-IT`) whose terms restrict redistribution, and a multi-GB binary would bloat every clone. Users who want to test the experimental path obtain it themselves — see [`apk/README.md`](apk/README.md) for the **cable-free** download-and-enable steps.

**The Phase 1 Go/No-Go gate decides its future.** Whether local LLM features become an officially supported part of the app depends on a documented evaluation across representative devices — stability, speed/heat/battery, an emergency safety test set, license clearance, and the storage budget (see `docs/PHASE1-GATE.md` and `docs/ROADMAP.md`). Until that gate returns GO, the retrieval-grounded engine is what ships, and the model stays behind the off-by-default toggle. A smaller model (e.g. Gemma 3 1B, ~0.9 GB) is the leading option for widening device support if the gate returns CONDITIONAL.

---

## Tech stack

- **Language:** Kotlin 2.0.21
- **UI:** Jetpack Compose (Material 3), Compose BOM 2024.10.01, Material Icons
- **Architecture:** single-activity, ViewModel + state flow, layered (ui / domain / data / ai)
- **Persistence:** Room 2.6.1 (with FTS search) for guides and household data; DataStore 1.1.1 for preferences
- **Background work:** WorkManager 2.9.1 (content-pack sync pipeline)
- **On-device AI (experimental):** Google MediaPipe LLM Inference `tasks-genai` 0.10.27
- **Splash:** androidx core-splashscreen 1.0.1
- **Build:** Android Gradle Plugin 8.7.3, compileSdk 35, minSdk 26, targetSdk 35

`minSdk 26` (Android 8.0) is a scaffold assumption to be confirmed against the Phase 1 runtime/device matrix.

---

## Get started

### Try the app
A prebuilt debug APK is available in the [`apk/`](apk/) folder — see [`apk/README.md`](apk/README.md) for the current build, checksum, install steps, and how to enable the experimental on-device model. Debug builds are self-signed for testing and are **not** release artifacts.

### Build from source
Requires **JDK 17** and the Android SDK. From the repository root:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17   # adjust to your JDK 17 location
./gradlew :app:assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

### Repository layout
- `app/` — the Android application (Kotlin + Compose)
- `docs/` — roadmap, architecture notes, and the Phase 1 Go/No-Go scorecard
- `apk/` — prebuilt debug APK drops and testing/setup instructions
- `prd.md` — the product requirements document this build implements

---

## Project status & roadmap

RescueDesk AI follows a phase-gated plan (full detail in `docs/ROADMAP.md`):

- **Phase 0 — Environment & rights kickoff:** project setup, content-rights requests to agencies, reviewer recruitment, pilot device list.
- **Phase 1 — Proof of concept & Go/No-Go gate:** evaluate on-device AI candidates and content; record a written GO/CONDITIONAL/NO-GO decision.
- **Phase 2 — UI & core application:** the guide-only app, fully usable with no model (largely in place).
- **Phase 3 — Offline AI & downloads (GO path only):** download manager, memory management, first-run model choice.
- **Phase 4 — Household features:** plan wizard, contacts, go-bag, deletion (in place).
- **Phase 5 — Safety, accessibility & field testing:** device matrix, expanded safety evaluation, moderated usability sessions, content freeze.

**Guiding principles:** the app must stay useful as a *guide-only* product at every phase; the AI is gated behind a real on-device test rather than assumed; and nothing ships without clear rights.

### Done so far (v0.1.0 scaffold)

- **Core app in place:** bilingual emergency guides (Typhoon, Flood, Earthquake, Fire) with real, complete step-by-step content in English and Filipino; Ask Juan with relevance-ranked retrieval grounding; household plan wizard, emergency contacts, go-bag checklist; privacy controls and one-tap data deletion; adjustable text size and a TalkBack pass; Filipino-aware search.
- **Ask Juan shipped as retrieval-grounded first** — usable with zero connectivity and zero model — then layered the optional LLM rewrite behind the experimental toggle.
- **Experimental LLM path proven on real hardware:** cable-free model install (browser download → place file → **Re-check**, no adb reinstall needed), detection-gated toggle, model load in ~15 s, and on-device generation with hardened token/session handling.
- **Safety refusals live:** live-condition questions refuse and point to official channels; medical emergencies escalate to 911/hospital; out-of-scope declines; no-match shows tappable real topics.
- **Current debug APK** is tracked in [`apk/`](apk/) with checksum, install steps, and the full model-enable walkthrough.

### In progress / next

- **Phase 1 gate measurements** — speed, heat, battery drain, and the emergency safety test set on representative devices, recorded in `docs/PHASE1-GATE.md`.
- Content review and rights clearance with qualified responders and agencies (guide "Pending review" status stays until sign-off).
- Wider-device evaluation; a smaller model (Gemma 3 1B class) is the leading fallback if the gate returns CONDITIONAL.

---

## Disclaimer

RescueDesk AI is an emergency-**preparedness** aid. It is **not** a replacement for professional emergency, medical, or government services, and its guides are pending review by qualified responders. Always follow the instructions of local authorities (NDRRMC, OCD, PAGASA, PHIVOLCS, BFP, your LGU) during an actual emergency. If you are in immediate danger, call your local emergency hotline.

---

## Credits

**Created by Elvin Manuel**
**Team ELOHIM Creator**

Built with open technologies — Jetpack Compose, Room, and Google MediaPipe LLM Inference. The experimental Gemma model is provided by Google under its own license and terms of use, and is not redistributed by this project.
