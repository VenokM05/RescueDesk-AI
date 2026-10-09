# RescueDesk AI — Phasing & Roadmap

**Companion to:** `prd.md` Section 14 (Development Roadmap)
**Status:** Planning baseline v1.0
**Owner assumption:** One experienced Android developer, with content review, legal clearance, and design running in parallel or part-time.

This document expands the PRD roadmap into an executable phase plan with entry/exit criteria, tasks, and decision gates. All durations are planning estimates, not delivery guarantees.

---

## Guiding Rules (from PRD)

1. **Preparedness app first, AI assistant second.** Every phase must keep the guide-only path shippable.
2. **Gate before build.** Phase 1 ends in a documented GO/NO-GO decision on on-device AI; no Phase 3 AI work starts before a GO.
3. **Safety defects block release.** Any critical unsafe AI output or missing safety instruction blocks the pilot gate (PRD §13.5).
4. **Nothing ships without rights.** A content item ships only when its rights status is *cleared* (PRD §10.3).

---

## Timeline Overview

```
Week:  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16
       [P1 ]
             [===== P2 =====]
                         [======== P3 (GO only) ========]
                               [==== P4 ====]
                                          [====== P5 ======]
       ^GATE                                     ^PILOT GATE ^RELEASE CANDIDATE
```

| Phase | Focus | Duration | Depends on |
| --- | --- | --- | --- |
| 0 | Environment & rights kickoff | Concurrent with P1 | — |
| 1 | PoC + Go/No-Go gate | 2 weeks | — |
| 2 | UI & core application | 1–2 weeks | P1 (library/search only) |
| 3 | Offline AI & downloads (GO path) | 2–3 weeks | P1 GO decision |
| 4 | Household features | 1 week | P2 |
| 5 | Safety, accessibility, field testing | 1–2 weeks | P2–P4 |
| — | Pilot → public release | outside estimate | P5 gate passed |

**Total: 12–16 weeks (GO path). NO-GO path removes Phase 3 → roughly 9–13 weeks for a guide-only v1.**

---

## Phase 0 — Environment & Rights Kickoff (parallel, no cost to critical path)

**Entry:** This document approved.

Tasks:

- [ ] Install JDK 17 + Android Studio; open the scaffolded project; run one successful debug build on a physical device.
- [ ] Register or designate the app's operator identity (needed for the privacy notice, PRD §10.1) and Philippine counsel contact for RA 10173 confirmation.
- [ ] Send written redistribution-permission requests to PAGASA, DOST-PHIVOLCS, OCD/NDRRMC, BFP, DOH, and Philippine Red Cross (PRD §10.3). Track responses in the rights register.
- [ ] Recruit the content review roles (PRD §8.3): at least one Subject-Matter Reviewer and one Approving Authority for first-aid content.
- [ ] Assemble the pilot device list: minimum two 6 GB-class and one 4 GB-class Android phones representative of the Philippine market.

**Exit:** Build runs on a device; permission requests sent; reviewers named; devices on hand or scheduled.

---

## Phase 1 — Discovery, Proof of Concept, and Go/No-Go Gate (2 weeks)

**Entry:** Phase 0 started (device list available by end of week 1).

### Track A — On-device AI PoC

- [ ] Evaluate Google LiteRT-LM and llama.cpp/MLC-style alternatives against the target devices.
- [ ] Test Gemma 3 1B IT (quantized) and Qwen3-0.6B as candidates; record tokens/sec, peak RSS memory, battery drain over 10 consecutive queries, and surface temperature on each pilot device.
- [ ] Verify each model license for redistribution of quantized derivatives; produce the **license matrix** deliverable.
- [ ] Build a keyword-retrieval prototype over 3 sample reviewed guides (PRD §7.3 "start with local keyword retrieval").
- [ ] Draft the initial seed safety test set (PRD §13.4): 25+ Filipino/English questions including out-of-scope and dangerous-misconception cases.

### Track B — Content & storage foundations

- [ ] Author the four built-in minimum guides (Typhoon, Flood, Earthquake, Fire) with full metadata (PRD §8.2); submit for review.
- [ ] Validate Room + FTS4/FTS5 build support, app-private model storage sizing, and resumable download approach (PRD §7.4) in throwaway code.

### Track C — Rights & legal

- [ ] Confirm with counsel: NPC registration presumption for a no-upload app (PRD §10.1).
- [ ] Confirm Play target-API requirements for the chosen minSdk/compileSdk.

### 🔴 Phase 1 Gate — Go/No-Go (PRD §14.2)

Decision recorded in writing with test data attached. GO requires **all five**:

1. Model runs on ≥2 representative devices (incl. one 4 GB-class) without memory-pressure crashes.
2. Usable answer speed; no unacceptable heat/battery drain under repeated inference.
3. Meets the agreed grounding threshold on the seed safety set with **zero** critical unsafe outputs.
4. Model + runtime licenses cleared for the distribution mechanism planned.
5. Fits the storage budget alongside guide packs and user data.

| Outcome | Consequence |
| --- | --- |
| **GO** | Phase 3 proceeds as scoped below. |
| **NO-GO** | Phase 3 is replaced by the *guide-only track*: enhanced categorization, curated Q&A content, improved search. AI deferred to v1.1+ until a model passes this gate. Phases 2, 4, 5 unchanged. |

**Exit:** Signed gate decision + PoC report + verified built-in guide content (reviewed, rights cleared or explicitly pending for the 4 sample guides).

---

## Phase 2 — UI & Core Application (1–2 weeks) — *identical in GO and NO-GO paths*

**Entry:** Gate decision recorded (Phase 2 does not depend on it).

- [ ] Implement the design system: color tokens (PRD §4.2), typography scale (PRD §4.3), 48/56 dp touch targets (§4.4), status-with-label+icon rule (§4.2).
- [ ] Onboarding flow: Welcome → Language → Accessibility → Download/Offline Setup (Screens A–D), including the "built-in guides available now" messaging.
- [ ] Home dashboard (Screen E) with Emergency Help as the dominant action.
- [ ] Emergency Help (Screen F) and Guide Detail (Screen G), including the Section 8.3 freshness warning and source/review chrome.
- [ ] Guides Library (Screen I) with local search, Filipino + English, and graceful handling of Taglish queries.
- [ ] Settings shell (Screen M): language, text size, privacy deletion entry points.
- [x] TalkBack pass at maximum font scale on all implemented screens (code-side a11y pass shipped: roles + selected state on onboarding choices, decorative icons marked, stable field descriptions, live region on Ask thread, minimum tap targets on chips + Call/Delete). Field verification on a physical device with TalkBack enabled is still scheduled for Phase 5.

**Definition of check:** a fresh install in airplane mode is a fully usable guide app (PRD §13.2 first cases pass).

**Exit:** Usable Android application **without requiring the AI model**; guide-only path demoable to stakeholders.

---

## Phase 3 — Offline AI & Downloads (2–3 weeks) — ⚠ GO PATH ONLY

**Entry:** Phase 1 gate = GO, with the winning model/runtime pair named.

- [~] **Experimental on-device LLM wiring shipped (feature-flagged)** —
  `ai/engine/MediaPipeEngine` implements the `AiEngine` contract against
  MediaPipe LLM Inference 0.10.27, targeting **Gemma 2 2B IT** (~2.7 GB
  `.task`, q8 — `Gemma2-2B-IT_multi-prefill-seq_q8_ekv1280.task` from the
  license-gated `litert-community/Gemma2-2B-IT` repo that Google's LLM
  Inference docs link to). Off by default; Settings → Experimental →
  "Try local LLM"
  enables it. Only ever invoked for a Grounded result — the
  live-claim / out-of-scope / medical-emergency refusals still short-circuit
  before the model is asked, and the strict safety prompt forbids inventing
  hotlines, dosages, prices, dates, distances, or current conditions. On any
  load or inference error Ask AI silently stays on the composed
  retrieval-grounded answer. This gives the Phase 1 gate a real candidate to
  evaluate on physical hardware without pre-empting the decision.
- [ ] Resumable download manager: Wi-Fi/mobile-data choice, progress, pause/retry, checksum verification, atomic activation, keep-previous-version rule (PRD §5.4, §13.3). **Note for model file specifically:** the experimental path expects `adb push gemma2b.task /data/local/tmp/rescuedesk/`; a proper download flow is post-gate work.
- [ ] On-device inference session management: load-on-demand, unload under memory pressure, no app-level crashes (PRD §FR-02). **Partially covered** — `MediaPipeEngine` is lazy (loads only on Settings toggle → on) and can `unload()` when the switch flips off; on-OS memory-pressure callback is still missing.
- [x] Retrieval layer: keyword index over the content pack, source metadata retained end-to-end (PRD §7.3, FR-04).
- [x] Ask RescueDesk AI (Screen H): grounded response structure (short answer → steps → caution → sources), fallback state, model-readiness indicator.
- [x] Prompt hardening + refusal behaviors per PRD §5.8 AI Safety Behavior; verify against the seed safety set continuously (this is a standing test, not a one-time check).
- [x] Offline & Download Manager screen (Screen L) incl. model removal preserving guides and user data.

**Exit:** Working local assistant answering only from cleared, reviewed content, with visible source cards; all §13.3 download tests pass; no critical unsafe outputs in the safety set.

### Phase 3 — NO-GO alternative track (1 week)

- [x] Enhanced guide categorization and curated question→guide routing (keyword-based "Ask" that jumps to guides instead of generating text).
- [x] Polish search morphology handling for Filipino (`data/search/FilipinoQueryExpander`: conservative prefix / infix / suffix stripping + de-reduplication; exact original token is always preserved first so precision is never lost).

### Local retrieval-grounded Ask (shipped, gate-independent)

Screen H now runs a **local answer engine** (`ai/ask/LocalAskEngine`): FTS
retrieval over the on-device guides → structured answer composed strictly
from guide text, with citations and the PRD §5.8 live-claim / out-of-scope
refusals. It needs no model and no network, so it is safe under both the GO
and NO-GO paths and does **not** pre-empt the Phase 1 gate. The generative
LLM path (Gemma / llama.cpp) remains the `:poc` experiment until the gate
returns GO and a winning runtime × model pair is named.

**Safety additions (this pass):**

- **Medical-emergency escalation** — symptom / urgency wording ("bleeding",
  "unconscious", "lagnat", "kagat ng aso", "gamot sa", …) is detected before
  retrieval and answered with a seek-professional-help message (calls 911 / nearest
  hospital) instead of a bland no-match. Deliberately conservative — over-
  escalating is the safe direction (PRD §13.4).
- **Live-claim refusal refinements** — dropped the over-broad `raised` marker
  (was misfiring on preparedness-ordering questions like "…when a typhoon
  signal #4 is raised"), added `signal number` / `storm signal` variants.
- **Filipino morphology** — Tagalog verbs ("bumaha", "maglilindol",
  "umuuulan") now reach their guide roots ("baha", "lindol", "ulan") via the
  query expander above.

---

## Phase 4 — Household Features (1 week)

- [x] My Family (Screen J) five-step plan wizard with autosave (FR-05).
- [x] Emergency contacts with ACTION_DIAL deliberate-call flow (FR-06, PRD §10.2).
- [x] Meeting places and out-of-area contact.
- [x] Go-bag checklist (Screen K) with custom items (Should-have per PRD §14.1 — first item to cut if the schedule slips).
- [x] Local persistence, edit, and delete paths; "delete all personal information" wired end-to-end (FR-07 privacy side, Screen M).

**Exit:** Plan survives app restart; deletion removes data completely including any auto-backup surface decided in Phase 0.

---

## Phase 5 — Safety, Accessibility & Field Testing (1–2 weeks)

- [ ] Full device compatibility matrix run (PRD §13.2, §11.1) on the pilot device list.
- [ ] Complete AI safety evaluation against the expanded reviewed test set; Filipino + English + Taglish (PRD §13.4).
- [ ] Moderated usability sessions: seniors, first-time smartphone users, parents; record metrics per the Measurement Plan (PRD §2.3). **No telemetry — observer checklists and manual logs only.**
- [ ] Content freeze: all shipped items *cleared* rights + reviewer recorded; quarterly review calendar started.
- [ ] Legal pass: privacy notice text (Filipino + English), Play Data Safety form draft, store listing claims scrubbed against PRD §8.5 prohibited claims.

### 🔴 Pilot Release Gate (PRD §13.5)

All must be true before broader distribution:

- [ ] All critical offline scenarios pass.
- [ ] Zero unresolved critical safety defects.
- [ ] Model + runtime pass device compatibility tests (GO path) or guide-only path shipped (NO-GO).
- [ ] Content reviewers approved the initial pack.
- [ ] Accessibility and privacy/security checks complete.
- [ ] Users can reach essential guides without AI (verified in usability sessions, not just QA).

**Exit:** Pilot-ready build + test reports + content update process documented.

---

## Risk Register & Mitigations

| # | Risk | Likelihood | Impact | Mitigation |
| --- | --- | --- | --- | --- |
| R1 | No candidate model passes the Phase 1 gate | Medium | High (AI scope lost) | Gate exists precisely for this; NO-GO path predefined; v1 still ships value |
| R2 | Agency content permissions delayed or denied | Medium | High (pack content thins) | Request in Phase 0 week 1; built-in guides authored from independently reviewed summaries; rights-status field blocks shipping uncleared items |
| R3 | 4 GB devices unusable for inference | Medium | Medium | Constrained-mode definition or gate NO-GO on those devices; 6 GB is the primary target |
| R4 | Content review slower than engineering | High | Medium | Reviewer roles staffed in Phase 0; quarterly cadence starts before launch; freshness warnings make pending review visible, not silent |
| R5 | Single-developer schedule slips | High | Medium | MoSCoW cutting order: Could (TTS polish) → Should (go-bag checklist) → Should (AI, via gate) — Must items are protected |
| R6 | Taglish/Filipino search quality below expectations | Medium | Medium | Phase 3 NO-GO track already includes morphology work; usability sessions surface real query wording |
| R7 | Play policy or API-level shifts during build | Low | Low | Pin to current policy in Phase 0; re-check before submission |

---

## Deliverables Checklist (PRD §15 mapped to phases)

| Deliverable | Due |
| --- | --- |
| PoC report, model license matrix, gate decision | End of Phase 1 |
| Product/UI spec updates from findings | Continuous |
| Android source + reusable component system | End of Phase 4 |
| Offline guide pack (built-in + downloadable) | Reviewed content by Phase 3 exit; freeze in Phase 5 |
| Model/content download manager | End of Phase 3 (GO path) |
| Family preparedness tools | End of Phase 4 |
| Safety & privacy documentation, test reports | End of Phase 5 |
| Pilot deployment guide + bilingual user instructions | Pre-pilot |
| Content update & review process (operational) | Started Phase 0, formalized Phase 5 |
