# `:poc` — Phase 1 model PoC harness

Spike module for the **Phase 1 Go/No-Go gate** (PRD §14.2). Separate
`applicationId` (`com.rescuedesk.poc`), no dependency on `:app`, and never
included in any release artifact of the main app. Full plan:
[`docs/PHASE1-MODEL-POC.md`](../docs/PHASE1-MODEL-POC.md).

## What's here

| Piece | File | PoC plan ref |
| --- | --- | --- |
| Engine contract (mirrored, not shared) | `engine/AiEngine.kt` | §5.1 / ARCHITECTURE §4 |
| LiteRT-LM adapter + wiring checklist | `engine/LiteRtEngine.kt` | §2.1 runtime #1 |
| llama.cpp adapter + wiring checklist | `engine/LlamaCppEngine.kt` | §2.1 runtime #2 |
| 24-question seed bank | `assets/questions.txt` | §6 |
| JSONL runner (tok/s, TTFB, citations) | `run/Runner.kt` | §5.2 |
| Memory + thermal sampler, unload discipline | `run/ResourceSampler.kt` | §5.3 / §5.4 |

## Week-1 execution steps

1. Build & install: `./gradlew :poc:installDebug`
2. Push a model artifact (§5.1 — each adapter's KDoc has the exact checklist):
   `adb push gemma3_1b-it-int4.task /sdcard/Android/data/com.rescuedesk.poc/files/models/`
3. Wire the native binding where the adapter's `TODO(spike)` markers are,
   pin dependency versions, rebuild.
4. Open **RescueDesk PoC**, pick the engine, tap **Run 24-question bank**.
5. Pull evidence: `adb shell run-as com.rescuedesk.poc cat files/results/poc-*.jsonl`
   → paste metrics into the §7 results sheet; send JSONL to the two raters.

Until the native bindings exist the app still runs end-to-end: the bank
executes, every row records the load/generate failure, and the JSONL lands on
disk — proving harness plumbing on a real device before any model ships.
