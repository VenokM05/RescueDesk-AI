# APK Drops

Prebuilt debug APKs for on-device testing and pilot recruitment. **Debug builds are
self-signed with the Android debug key — never distribute these as releases.**

## Current build

| | |
| --- | --- |
| File | `RescueDeskAI-v0.1.0-scaffold-63bdbf5-debug.apk` |
| Version | `0.1.0-scaffold` (versionCode 1) |
| Git commit | `63bdbf5` |
| Date | 2026-10-10 |
| SHA-256 | `a009a3e945e0f3215cdd8998242222b719a766b669650e8198b4559bcb67ab83` |
| Size | ~18 MB |
| Min Android | 8.0 (API 26) |

**Included:** onboarding (language + text size), Home, Emergency Help routing,
8 built-in guides in EN + Filipino (typhoon/flood/earthquake/fire — UNREVIEWED,
rights status pending), guide detail with review banners, local search,
Family Plan wizard, emergency contacts (system-dialer calling), bilingual
Go-Bag checklist, full Filipino UI (Settings → Language → Filipino),
Screen L — Offline & Download Manager (guide-pack pipeline present; server URL
is a placeholder until the content repo ships), Settings, and **delete all
personal information** in Settings → Privacy (household plan, contacts, go-bag
wiped atomically; guides and language settings survive).
**Not included:** on-device AI (Ask AI shows the fallback state).
**Note:** first launch on a phone that ran the previous build resets the
household plan / contacts / go-bag once (Room v2 schema change for the
bilingual checklist).

## Install

USB (Developer Options → USB debugging on):

```bash
adb install -r apk/RescueDeskAI-v0.1.0-scaffold-63bdbf5-debug.apk
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
