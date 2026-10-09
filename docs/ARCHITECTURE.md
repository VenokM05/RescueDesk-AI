# RescueDesk AI — Architecture Design

**Companion to:** `prd.md` Sections 6 (FRs), 7 (Technical Architecture), 8 (Content Governance), 9–10 (Privacy/Legal)
**Stack:** Kotlin · Jetpack Compose · MVVM · Room · DataStore · WorkManager
**Status:** Design baseline v1.0 — the inference runtime and model artifact remain open until the Phase 1 gate (see `docs/ROADMAP.md`).

---

## 1. System Context

```
┌────────────────────────────── Android Device ──────────────────────────────┐
│                                                                             │
│  ┌──────────────── RescueDesk AI App ────────────────┐   ┌──────────────┐  │
│  │  UI (Compose)  ←  ViewModels  ←  Repositories     │   │  Play Store  │  │
│  │                              ↓                     │   └──────────────┘  │
│  │  Room (guides·FTS·plan·contacts)  DataStore        │         ↑ app updates only │
│  │  AiEngine (runtime+model, app-private storage)     │   ┌──────────────┐  │
│  │  DownloadManager (resumable, checksum)             │──→│ Optional CDN │  │
│  └────────────────────────────────────────────────────┘   │ static files │  │
│            ↑ built-in guides (APK assets)                 └──────────────┘  │
│  TTS engine (system)  ←  read-aloud          NO accounts, NO telemetry,    │
│  Dialer (ACTION_DIAL) ←  contacts            NO location permission        │
└─────────────────────────────────────────────────────────────────────────────┘
```

- **No backend dependency at runtime.** The optional service hosts only static files: model artifacts, content-pack manifests/zips, and (later) a consented feedback endpoint. If it is unreachable or removed, every installed feature keeps working.
- **App updates** arrive via Play Store; **content/model updates** arrive via the in-app download manager — deliberately separate lifecycles so safety content can be revised without an app release.

## 2. Module & Layer Structure

Single Gradle module (`:app`) with enforced package layers — right-sized for one developer; can be split into true modules later if a second consumer (e.g., LGU build flavor) appears.

```
com.rescuedesk.ai
├── app/                 Application, ServiceLocator (manual DI), deep-link entry
├── ui/
│   ├── theme/           Design tokens from PRD §4 (colors, type scale, spacing)
│   ├── navigation/      Bottom-nav graph: Home · Guides · Ask AI · My Family
│   │                    + Emergency Help & detail routes outside the tab stack
│   └── screens/         One package per PRD Screen A–M; each = Screen + ViewModel
├── domain/
│   ├── model/           Guide, GuideChunk, FamilyPlan, EmergencyContact,
│   │                    GoBagItem, ModelStatus, PackStatus, ReviewFlags
│   └── usecase/         SearchGuides, GetGuideByCategory, BuildGroundedAnswer,
│                        CompletePlanStep, CheckFreshness (PRD §8.3 rules)
├── data/
│   ├── local/           Room db, DAOs, DataStore (settings), entity definitions
│   ├── repository/      GuideRepository, PlanRepository, ContactRepository,
│   │                    GoBagRepository, SettingsRepository (impls)
│   ├── seed/            Built-in guide seeder (assets JSON → Room, first launch)
│   └── pack/            Content-pack installer (unpack, verify, atomic swap)
├── ai/
│   ├── engine/          AiEngine interface + runtime adapter (Phase 1 decides)
│   ├── retrieval/       Keyword retriever (FTS), chunk selector, prompt builder
│   └── download/        Model + pack download manager (WorkManager + resumable HTTP)
└── safety/              Guardrails: refusal rules, freshness gating, claim filter
```

**Dependency rule:** `ui → domain → data/ai/safety` abstractions; implementations never leak upward. ViewModels expose `StateFlow<UiState>` only.

## 3. Data Design (Room)

### 3.1 Entities

| Entity | Key fields | Notes |
| --- | --- | --- |
| `guide` | id, publicId, title, category, summary, body, language, sourceName, sourceRef, publishedDate, lastReviewed, nextReview, version, rightsStatus, isBuiltin, packVersion | PRD §8.2 metadata is *stored, not display-derived* |
| `guide_fts` | title, body, category (virtual) | Room `@Fts4` (FTS5 needs a custom SQLite build — evaluate in PoC; FTS4 is the safe default) |
| `family_plan` | id, nickname, memberCount, notes, stepStatus, completedAt, updatedAt | Wizard progress autosaved per step (FR-05) |
| `meeting_place` | id, planId?, label, details | Nearby / alternative / out-of-area |
| `emergency_contact` | id, name, relationship, phone, isSecondary | Never auto-dialed; ACTION_DIAL only (PRD §10.2) |
| `go_bag_item` | id, category, label, quantity, checked, custom | Six PRD §5.11 categories + user items |
| `chat_session` / `chat_message` | role, text, sourcesJson, createdAt | Local only; clearable from Settings; **never included in logs or crash reports** (PRD §9) |
| `model_state` | name, version, path, checksum, status | Points into app-private files dir, never cache (PRD §7.4) |

### 3.2 Freshness & rights as runtime state

`ReviewFlags` is computed on read by `CheckFreshness` + rights filter (PRD §8.3):

- `needsReview` → `nextReview` passed **or** >12 months since `lastReviewed` **or** pending event-driven review ⇒ UI warning banner (Screen G) **and** automatic exclusion from AI retrieval grounding.
- `rightsStatus != CLEARED` ⇒ item excluded from downloadable pack builds; built-in items can never ship in this state (release-check enforced).

Built-in guides (PRD §5.4) are seeded from `assets/guides/built-in/*.json` into the same tables on first launch with `isBuiltin = true`, so search, detail, and grounding treat built-in and pack content uniformly. An installed pack supersedes the built-in row with the same `publicId`.

### 3.3 Settings (DataStore, not Room)

Language (fil/en), text scale (standard/large/xlarge), high-contrast, read-aloud preference, download-over-mobile-data consent, seed-complete flag. All survive restart (FR-07); none are personal-sensitive enough to complicate backup decisions.

## 4. Offline Download & Update Pipeline

```
manifest.json (version, files[], sha256, sizes, minAppVersion)
      │  1. fetch manifest (HTTPS)          ┌────────────────────┐
      ├→ size check vs free storage ───────▶│ paused / retryable │
      │  2. resumable download → *.part    └────────────────────┘
      │  3. sha256 verify    ✗ → delete .part, surface retry
      │  4. unpack to staging dir, validate schema/index integrity
      │  5. atomic activation: rename staging → active, flip pointer in Room
      │  6. keep previous active version until step 5 succeeds (PRD §5.4)
      └ 7. GC superseded versions when storage is tight (never the active one)
```

- WorkManager handles background continuation; a foreground service shows progress for large model downloads.
- Model and pack pipelines share this code path with different manifests.
- **Failure modes are user-visible and recoverable**: insufficient storage (need/available/skippable, PRD §5.4), checksum mismatch, interrupted update (previous version still active), incompatible model for device (compatibility status from PoC ruleset).
- Deleting the model (Screen L) removes only model files; guides, plans, contacts untouched (FR-03).

## 5. On-Device AI Subsystem

### 5.1 AiEngine abstraction

```kotlin
interface AiEngine {
    val status: StateFlow<ModelStatus>   // NotInstalled / Installing / Ready / Incompatible / Error
    suspend fun ensureLoaded(): Result<Unit>
    suspend fun generate(prompt: Prompt, onToken: (String) -> Unit): Result<Answer>
    fun unload()                          // called under memory pressure / after idle timeout
}
```

The runtime adapter (LiteRT-LM, llama.cpp-on-Android, or other — **Phase 1 decides**) is the only code allowed to touch native libraries. Everything else — UI, retrieval, safety — is engine-agnostic, which is what makes the PRD's guide-only NO-GO path cheap: the adapter is never written, the interface is never implemented, and `Ask AI` renders the PRD §5.8 fallback state.

### 5.2 Grounded RAG flow (PRD §7.3)

```
question → normalize (lowercase, light Taglish tokenization)
  → FTS keyword search over active+cleared, not-needsReview chunks
  → top-k chunks WITH metadata (title, source, version, lastReviewed)
  → controlled prompt: system rules + chunks + question, capped context
  → generate (token-streamed to UI, long responses off main thread)
  → answer post-validate: citation ids must ⊆ retrieved set; refuse if empty retrieval
  → persist locally + render source cards from original metadata (FR-04)
```

Hard rules enforced in `safety/`:

- Zero retrieved chunks ⇒ fixed message (never a free-form "I don't know").
- Model output claiming live data ("current storm warnings", "evacuation order", "I have contacted…") ⇒ response rejected pre-render; replaced with escalation text. This is a filter, not a prompt request.
- Medical-diagnosis-shaped questions ⇒ routed to a prepared "seek professional help" response.

### 5.3 Memory & lifecycle posture

Load model lazily on first question in a session; unload on screen exit + timeout and on `onTrimMemory`. Inference runs on a dedicated dispatcher; UI consumes token stream via `SharedFlow`. A crash inside native code must degrade to `ModelStatus.Error` with the fallback state — never kill the process (FR-02).

## 6. UI Architecture

- **Navigation:** single `NavHost`; bottom tabs (Home, Guides, Ask AI, My Family) preserved via nested graph; Emergency Help and Guide Detail are routes reachable from anywhere but **never require AI state** (PRD §4.6, principle 1).
- **State:** per-screen `ViewModel` + immutable `UiState`; no framework types in domain models.
- **Design tokens:** PRD §4.2 palette as `ColorScheme`; red reserved for genuine urgency per PRD rule; every status color paired with label+icon (color-not-alone rule, §4.2/§4.5).
- **Accessibility as an architectural feature:** `fontScale` buckets applied through our own `Type` (plus system scaling respected), `ContentDescription`/`semantics` on all icon actions, minimum 48/56 dp targets enforced via a custom `Button` component library so screens can't opt out.
- **Read-aloud:** one `TtsController` at `ui` layer wrapping Android `TextToSpeech`; absent engine ⇒ feature hidden, content still readable (PRD §5.3).

## 7. Privacy & Security Architecture

| Concern | Design decision |
| --- | --- |
| Personal data at rest | Room in app-private storage; SQLCipher evaluation logged as a post-MVP decision (PRD §9 acknowledges local storage limits) |
| Backups | Explicit `dataExtractionRules`: household tables excluded from cloud/device-transfer backup until reviewed (PRD §10.2 last bullet) |
| Deletion | Single `UserPurge` use case: plan + contacts + checklist + chat history + model files, invoked from Settings; verified by re-query post-delete |
| Logging | No logging of chat bodies or household fields; crash reports scrubbed allowlist-only |
| Network | HTTPS-only; no keys embedded (manifest endpoints need no auth); TLS validation default |
| Permissions | None beyond internet (normal) and foreground-service type for downloads; **no location, no CALL_PHONE** (ACTION_DIAL) |

## 8. FR → Architecture Traceability

| FR | Where implemented |
| --- | --- |
| FR-01 Offline library | `guide`/`guide_fts`, seed pipeline, Screen I; works with zero network from first launch |
| FR-02 On-device AI | `ai/engine` adapter + status states; error isolation requirement in §5.3 |
| FR-03 Downloadable model | §4 pipeline, `model_state`, Screen L |
| FR-04 Grounded responses | §5.2 flow + `safety` post-validation; source cards from stored metadata |
| FR-05 Household plan | `family_plan` + wizard state machine, autosave per step |
| FR-06 Emergency contacts | `emergency_contact` + ACTION_DIAL flow |
| FR-07 Language/accessibility | DataStore settings, `ui/theme` type scale, semantics rules |
| FR-08 Content updates | Manifest/pack pipeline §4, freshness rules §3.2 |

## 9. Open Decisions (tracked to Phase 1 gate)

1. **Inference runtime + model artifact** (LiteRT-LM vs alternatives; Gemma 3 1B vs Qwen3-0.6B vs third candidate) — gate owns this; `AiEngine` isolates the blast radius.
2. **FTS5 via custom SQLite build vs FTS4 via Room** — try FTS5 in PoC; fall back to FTS4 (Room-supported) without schema change.
3. **minSdk** — 26 assumed in scaffold; confirm against runtime + FTS requirements before freezing.
4. **Embedding-based retrieval** — deferred per PRD §7.3 unless keyword retrieval measurably fails on the Filipino/Taglish test set.
5. **External distribution (APK sideloading for LGU/school deployments)** — architecture permits it (no Play-only dependencies in core paths); policy and integrity story to be defined before any institutional pilot.
