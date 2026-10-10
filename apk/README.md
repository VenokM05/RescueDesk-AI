# APK Drops

Prebuilt debug APKs for on-device testing and pilot recruitment. **Debug builds are
self-signed with the Android debug key — never distribute these as releases.**

## Current build

| | |
| --- | --- |
| File | `RescueDeskAI-v0.1.0-scaffold-859144d-debug.apk` |
| Version | `0.1.0-scaffold` (versionCode 1) |
| Git commit | `859144d` |
| Date | 2026-10-10 |
| SHA-256 | `2eab292fa58577cbfcb8ca79433d482b073d6f197ba2dd5a41193a14bd9667eb` |
| Size | 74,025,479 bytes (~71 MB; MediaPipe native libs across 4 ABIs) |
| Min Android | 8.0 (API 26) |

**New in this build:** the Ask Juan LLM rewrite no longer **crashes the app**
mid-generation. The tombstone was explicit: SIGABRT — `JNI NewByteArray called
with pending exception … OUT_OF_RANGE: Input is too long … current_step(319) +
input_size(7) was not less than maxTokens(320)`. MediaPipe's maxTokens budgets
input + output together, so the grounding prompt alone ate most of the ceiling;
hitting that boundary inside native inference is an uncatchable CheckJNI abort
on debug builds — which is what really made every answer "stay the same".
Fix: a token pre-flight (measured with MediaPipe's own tokenizer, guide context
trimmed before it can explode), a fresh session per question (the shared
default session accumulated history across questions), and a 1024-token budget
(within the model's 1280 KV positions). Failed generations also no longer flip
Settings to Error — the composed retrieval answer always survives.

Earlier: **Ask Juan stopped answering every question with
earthquake.** Root cause: Room's FTS4 search cannot `ORDER BY bm25 rank`, so
OR-matched guides came back in insertion order — and since stopword tokens hit
nearly all guides, the first-seeded guide (earthquake) always won
`matches.first()`. Search results are now re-ranked in memory by real term
presence (title ×6 / summary ×3 / body ×1), so a typhoon question grounds on
the typhoon guide — in Ask Juan, the search screen, and the Gemma rewrite
(which now also receives the real source-guide title; watch logcat tag
`LLM-ask` for the grounding + timing trace).

Earlier: the LLM toggle row shows a bold **ON/OFF text mirror** next to the
Switch and the **whole row is tappable** — the Switch glyph alone proved easy
to miss on this unit. (2) The Ask Juan LLM rewrite pass is no longer silently
optional: every skip branch logs under logcat tag **LLM-ask** (enabled flag,
engine status, load/generate failures, timings), and `MAX_TOKENS` dropped
600 → 320 so CPU inference rewrites arrive in tens of seconds, not minutes.

Earlier: **Re-check for model** can no longer look dead: every
tap now fires an instant "Checking this device…" Toast, ends with a Toast
stating the concrete result (detected / truncated / not found — Toasts are
`runCatching`-wrapped because MIUI/HyperOS can suppress app notifications), and
a persistent red "No model file found — checked: …" line appears on screen
when the probe comes up empty (previously a not-found result rendered NOTHING,
which is exactly what looked like a broken button on the Redmi test unit).
`detectModel()` additionally logs every candidate path with
exists/canRead/length under logcat tag **LLM-detect** for remote diagnosis.

Earlier: the **Settings → Experimental** local-LLM switch became
impossible to miss and impossible to mislead: explicit `SwitchDefaults` colors
(primary track when on, outlined surface track when off, dimmed when locked)
keep it clearly visible in both light and dark themes, and it only unlocks when
a **complete, valid model file is actually detected** on the device — until
then, a "Switch locked until a complete model file is found…" line plus the
existing download guidance explain exactly what to do, and **Re-check for
model** unlocks it without an app restart.

Earlier builds: the four built-in **Emergency Guides** (Typhoon, Flood,
Earthquake, Fire — EN + Filipino) now read as real, complete guidance: the
leftover "PLACEHOLDER" wording is gone from every summary, source attribution is
honest, and Flood gained an early-evacuation step. Guides still show the
*"Pending review"* banner on purpose — that is the PRD safety gate (sections 8.3
and 10.3): the content stays unapproved until a qualified reviewer signs off and
redistribution rights are confirmed, and we will not fake that. **A fresh
install (or Settings → clear app data) is needed to re-seed the updated guide
text** — an existing install keeps its previously seeded copies.
Earlier this session: the **Settings → Experimental** local-LLM card now detects
a locally placed Gemma model at runtime (path + size + truncation warning) with a
one-tap **Re-check for model** button, and its guidance leads with the
**cable-free** setup (browser download → move to
`Android/data/com.rescuedesk.ai/files/models/gemma2b.task` → Re-check → toggle
Ready), so testing no longer needs a computer or `adb push`. The 2.7 GB `.task`
is intentionally *not* committed to the repo — see "enable the experimental local
LLM" below for why.
Even earlier: assistant renamed **Ask Juan** everywhere (with real tappable guide
chips on no-match instead of a dead-end message); splash screen with the app
logo; About mission + credits (*Created by Elvin Manuel, Team ELOHIM Creator*);
`§` replaced with the word "section"; launcher display name **"RescueDesk AI"**.

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
and **Ask Juan in local mode** — offline, retrieval-grounded answers composed
only from the on-device guides, with citations, PRD section 5.8 safety refusals
(live-claim + out-of-scope), and a new **medical-emergency escalation**
(bilingual) that routes symptom / urgency wording to 911 + nearest hospital
instead of a bland no-match. A code-side **TalkBack pass** shipped in this
build: roles + selected state on onboarding choices, decorative icons marked,
stable field descriptions, live region on the Ask thread, and minimum tap
targets on chips + Call/Delete.
**Included (new in this build):** an **experimental on-device LLM path** —
Gemma 2 2B IT via MediaPipe LLM Inference, behind
Settings → Experimental → "Try local LLM" (**off by default**). When enabled
AND a model file is present on the device, Ask Juan rephrases the
retrieval-grounded answer into a short paragraph with an
"Rephrased by local AI (experimental)" badge; source citations always remain,
and refusals / medical escalation / no-match never reach the model. Without
the toggle or the model file, Ask Juan behaves exactly as before. The model
file itself is **not** bundled (see below).
**Not included:** a bundled model asset or automatic download — the LLM path
is candidate evaluation only and still gated behind the Phase 1 Go/No-Go
decision (PRD section 7.2).
**Note:** first launch on a phone that ran a pre-Room-v2 build resets the
household plan / contacts / go-bag once (schema change for the bilingual
checklist).

## Install

USB (Developer Options → USB debugging on):

```bash
adb install -r apk/RescueDeskAI-v0.1.0-scaffold-859144d-debug.apk
```

Or copy the file to the phone and open it (allow "install unknown apps" for
your file manager — expected for test builds).

### Optional: enable the experimental local LLM

The Gemma 2 2B IT `.task` file is **not** in the APK, and is deliberately **not
tracked in this Git repo** either. Committing a 2.7 GB binary is not viable here:
GitHub hard-rejects any single file over 100 MB (and the free LFS tier is only
1 GiB of storage total, so the weights could not land even via LFS); forcing
every clone, CI run, and pilot reviewer to pull 2.7 GB contradicts a
low-bandwidth, offline-first app; and most importantly the **Gemma Terms of Use
restrict redistributing the weights** — the file lives behind a Hugging Face
license gate for exactly that reason. So we ship the *app*, and each user
authenticates against Google's official gated source with their own account.

**Cable-free method (no computer, no `adb push`) — recommended for testing:**

1. On the phone, open
   [`litert-community/Gemma2-2B-IT`](https://huggingface.co/litert-community/Gemma2-2B-IT)
   in a browser, log in, and tap *Agree and access repository* once.
2. Download `Gemma2-2B-IT_multi-prefill-seq_q8_ekv1280.task` (~2.7 GB).
3. In the phone's **Files** app, move it to
   `Android/data/com.rescuedesk.ai/files/models/` and rename it to `gemma2b.task`.
4. Open the app → **Settings → Experimental → Re-check for model**. The card
   shows the detected path + size, then toggle **Try local LLM** → *Ready*.

> Some Android 13+ builds stop third-party file managers from writing into
> `Android/data/…`. If step 3 is blocked on your device, use the USB method
> below instead — the app also reads `/data/local/tmp/rescuedesk/gemma2b.task`.

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
