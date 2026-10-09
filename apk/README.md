# APK Drops

Prebuilt debug APKs for on-device testing and pilot recruitment. **Debug builds are
self-signed with the Android debug key — never distribute these as releases.**

## Current build

| | |
| --- | --- |
| File | `RescueDeskAI-v0.1.0-scaffold-5101b5c-debug.apk` |
| Version | `0.1.0-scaffold` (versionCode 1) |
| Git commit | `5101b5c` |
| Date | 2026-10-10 |
| SHA-256 | `48d8cee6025496fdf492dbaf7108f03c200e6e33ddb32a72ff3bf94ab728d6e9` |
| Size | ~18 MB |
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
**Not included:** the generative on-device LLM (Gemma / llama.cpp) — that is
still gated behind the Phase 1 Go/No-Go decision and lives in the `:poc` module.
**Note:** first launch on a phone that ran a pre-Room-v2 build resets the
household plan / contacts / go-bag once (schema change for the bilingual
checklist).

## Install

USB (Developer Options → USB debugging on):

```bash
adb install -r apk/RescueDeskAI-v0.1.0-scaffold-5101b5c-debug.apk
```

Or copy the file to the phone and open it (allow "install unknown apps" for
your file manager — expected for test builds).

## Rebuild from source

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

## Conventions

- Filename: `RescueDeskAI-v<versionName>-<git-short-hash>-debug.apk`
- One APK per drop; delete superseded files before committing (keep this folder
  to a single current build — debug APKs are ~19 MB each and GitHub blocks
  files over 100 MB).
