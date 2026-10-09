# APK Drops

Prebuilt debug APKs for on-device testing and pilot recruitment. **Debug builds are
self-signed with the Android debug key — never distribute these as releases.**

## Current build

| | |
| --- | --- |
| File | `RescueDeskAI-v0.1.0-scaffold-ad367e5-debug.apk` |
| Version | `0.1.0-scaffold` (versionCode 1) |
| Git commit | `ad367e5` |
| Date | 2026-10-09 |
| SHA-256 | `516ea8698e67578053e306515add5c5cd9e0ec312771cc1904a2e6d65397a2c0` |
| Size | ~18.6 MB |
| Min Android | 8.0 (API 26) |

**Included:** onboarding (language + text size), Home, Emergency Help routing,
4 built-in placeholder guides (typhoon/flood/earthquake/fire — UNREVIEWED,
rights status pending), guide detail with review banners, local search,
Family Plan wizard, emergency contacts (system-dialer calling), Go-Bag checklist,
Settings. **Not included:** on-device AI (Ask AI shows the fallback state),
guide-pack downloads.

## Install

USB (Developer Options → USB debugging on):

```bash
adb install -r apk/RescueDeskAI-v0.1.0-scaffold-ad367e5-debug.apk
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
