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

The Gemma 2 2B IT `.task` file is **not** in the APK.

**Verified source (Oct 2026):** Google's MediaPipe LLM Inference docs link
"Download Gemma-2 2B" to the Hugging Face repo
[`litert-community/Gemma2-2B-IT`](https://huggingface.co/litert-community/Gemma2-2B-IT).
The file to use is:

- `Gemma2-2B-IT_multi-prefill-seq_q8_ekv1280.task` — **2.71 GB** (q8; the
  earlier ~1.4 GB estimate in these notes was wrong — int8 bundles here
  include the tokenizer and KV cache). It exceeds the original 2 GB budget;
  plan for ~3 GB free device storage and realistically 8 GB RAM (Pixel-class).
- ⚠ The repo is **license-gated**: anonymous `curl` gets `401 GatedRepo`.
  You must be logged in to Hugging Face, click *Agree and access repository*
  on the repo page, and create a read token
  (Settings → Access Tokens → `hf_…`).
- The 2024 Kaggle route (`google/gemma-2` → on-device `.task` variants, e.g.
  `gemma-2-2b-it-cpu-int8`) is superseded — Google's docs now point to
  litert-community. Use it only if the HF flow is blocked for you.

**One-shot download + push (macOS/Linux, phone connected over USB):**

```bash
# 0) prerequisites, once:
#    - pip install "huggingface_hub[cli]"   (provides `hf`; older builds: `huggingface-cli`)
#    - accept the Gemma license on the repo page above
#    - export HF_TOKEN=hf_yourReadTokenHere
export HF_TOKEN=hf_YOUR_TOKEN_HERE

FILE=Gemma2-2B-IT_multi-prefill-seq_q8_ekv1280.task

# 1) download (~2.7 GB, resumable) — hf CLI handles the gated auth:
hf download litert-community/Gemma2-2B-IT "$FILE" --local-dir /tmp/gemma2b

# 2) push to the app's debug lookup path, renamed to what the app expects:
adb shell mkdir -p /data/local/tmp/rescuedesk
adb push "/tmp/gemma2b/$FILE" /data/local/tmp/rescuedesk/gemma2b.task

# 3) sanity-check the size landed whole (expect ~2.7 GB, not a truncated file):
adb shell ls -la /data/local/tmp/rescuedesk/
```

Plain-`curl` variant (same token required):

```bash
curl -L -H "Authorization: Bearer $HF_TOKEN" -o gemma2b.task \
  "https://huggingface.co/litert-community/Gemma2-2B-IT/resolve/main/Gemma2-2B-IT_multi-prefill-seq_q8_ekv1280.task"
adb shell mkdir -p /data/local/tmp/rescuedesk
adb push gemma2b.task /data/local/tmp/rescuedesk/
```

Then: open the app → Settings → Experimental → toggle **"Try local LLM
(Gemma 2 2B IT)"**. First load takes 10–30 s on CPU; status should flip to
*Ready*. Ask a grounded question (e.g. "What should I do during an
earthquake?" / "Ano ang dapat gawin kapag baha?") — the answer renders as one
paragraph with the experimental badge.

The app also accepts the file at `<app files>/models/gemma2b.task` or
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
