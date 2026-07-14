# ADR — DFI Mobile Architecture v1

Status: Proposed (awaiting founder approval)
Date: 2026-07-14
Task: C1 — Mobile Architecture ADR
Intended repository path: `docs/ADR_MOBILE_ARCHITECTURE_V1.md`
Product baseline at decision time: MVP v0.3 (Vite + TypeScript PWA)

Source documents reviewed:
- `BOOTSTRAP.md`
- `docs/DFI_PRODUCT_VISION_V2.md`
- `docs/DFI_PRODUCT_VISION_V2_1_ADDENDUM.md`
- `docs/DFI_FIELD_VALIDATION_PROTOCOL_V1_1.md`
- `docs/DFI_USER_INTELLIGENCE_PROFILE_MODEL_V1.md`
- `docs/DFI_FLOATING_INTERACTION_FIELD_ASSISTANCE_MODEL_V1.md`
- `docs/CLAUDE_HANDOFF_V2_1.md`
- MVP v0.3 source tree (`src/`, `tests/`, `public/`)

---

## 1. Context

MVP v0.3 is a Vite + TypeScript PWA that validates the core loop
(capture → deterministic structuring → driver confirmation → local
store → reuse → outcome feedback → evidence export) using
LocalStorage persistence, a shell-caching service worker, and
browser speech recognition.

The product direction (Vision v2 + v2.1 addendum + Floating
Interaction Model v1) now requires capabilities that must be
evaluated before any further implementation expansion:

1. A movable floating control drawn **over other apps** (the carrier
   app is foregrounded during real work).
2. Short-tap → voice capture → intent classification.
3. Long-press → **safe-area-aware adaptive radial menu** with
   drag-and-release selection and drag-to-close.
4. Quiet passive-assistance signaling with low-interruption
   micro-prompts and false-positive feedback labels.
5. Background location sessions feeding a future personal behavior
   baseline and Route Breakdown Event evidence.
6. Offline-first operation across Online / Degraded / Offline with
   durable local persistence, media queues, and idempotent sync.
7. Korean speech capture that works in poor-network field areas.
8. Camera/video capture compatible with a future Field Trace
   pipeline (CameraX-class control).
9. Strict data boundaries: Private Intelligence Profile vs Personal
   Field Analytics vs Public Field Profile.
10. Solo-founder maintainability during a 10–30-location,
    2–5-driver field validation.

This ADR decides the mobile architecture. It does not begin
migration, does not implement C2–C8, and does not change product
philosophy.

## 2. Decision drivers

Ordered by decisiveness for DFI specifically:

D1. Overlay floating control over other apps is the *primary*
    interaction surface. An architecture that cannot host it, or can
    only host it through fragile third-party glue, fails the product.
D2. Gesture fidelity: press-hold-drag-release radial selection with
    per-frame hit testing, haptics, and safe-bound geometry.
D3. Background location + foreground service + OEM process survival.
D4. Offline-first: durable storage, process-death-safe queues,
    idempotent replay, offline Korean speech.
D5. Battery discipline (all-day field sessions).
D6. Camera/video path compatible with future Field Trace (CameraX).
D7. Solo-founder maintenance and iteration speed for Phase A/B.
D8. Reuse of MVP v0.3 assets (small: ~300 lines of deterministic
    TS logic + tests + validation metric definitions).
D9. Distribution to 2–5 known drivers now; Play Store later.
D10. Data-boundary enforceability (profile layer separation).

Non-drivers (explicitly rejected as decision criteria): general
framework popularity, hypothetical future iOS parity, hiring-market
considerations, hypothetical million-user scaling.

A structurally important observation for D1: **iOS has no equivalent
of `SYSTEM_ALERT_WINDOW`.** A system-wide floating control over other
apps is not implementable on iOS at all. Therefore the main value
proposition of React Native and Flutter — cross-platform reuse —
buys DFI almost nothing where it matters most. An eventual iOS
product would need a *different interaction model* regardless of
framework choice. This removes the strongest generic argument for a
cross-platform framework.

## 3. Candidate architectures

- **A. Continue PWA** (Vite + TS, deepen with IndexedDB, Web Speech,
  Media Capture, Background Sync).
- **B. React Native** (RN app + native Android modules for overlay,
  services, location).
- **C. Flutter** (Flutter app + `flutter_overlay_window`-class plugin
  or custom platform channels).
- **D. Native Android / Kotlin** (single-module app: Jetpack Compose
  for in-app UI; plain `WindowManager` views for the overlay;
  Room + WorkManager; SpeechRecognizer; CameraX).
- **Hybrid variants** considered in §23.

## 4. Requirement comparison matrix

Scale: ✅ native capability, direct API · ⚠️ possible via plugins /
custom native glue (added fragility) · ❌ not feasible or violates an
invariant. Justification for every ❌/⚠️ appears in §5–§11.

| Requirement | PWA | React Native | Flutter | Native Kotlin |
|---|---|---|---|---|
| Overlay over other apps (D1) | ❌ | ⚠️ custom native Service + headless glue | ⚠️ second engine via plugin | ✅ WindowManager + FGS |
| Floating control lifecycle (survives app UI closed) | ❌ | ⚠️ native Service anyway | ⚠️ native Service anyway | ✅ |
| Adaptive radial geometry + safe bounds | ⚠️ in-page only | ⚠️ overlay gestures cross bridge | ⚠️ overlay engine limits | ✅ WindowInsets + MotionEvent |
| Drag-and-release selection fidelity | ⚠️ | ⚠️ | ⚠️ | ✅ |
| Haptic feedback granularity | ⚠️ vibrate() only | ✅ | ✅ | ✅ VibrationEffect primitives |
| Contextual action-set switching | ✅ | ✅ | ✅ | ✅ |
| Korean speech, offline-capable | ❌ network-bound Web Speech | ⚠️ plugin → SpeechRecognizer | ⚠️ plugin → SpeechRecognizer | ✅ SpeechRecognizer (on-device ko-KR) |
| Background location sessions | ❌ (screen-off geolocation unavailable) | ⚠️ plugin + native FGS config | ⚠️ plugin + native FGS config | ✅ FusedLocationProvider + FGS `location` |
| Foreground service types (mic/location/camera) | ❌ | ⚠️ | ⚠️ | ✅ |
| Battery control (sampling, batching) | ❌ no control | ⚠️ bridge overhead | ⚠️ second engine overhead in overlay | ✅ |
| Process death & recovery | ⚠️ tab discard opaque | ⚠️ | ⚠️ | ✅ WorkManager + Service restart semantics |
| OEM background restrictions handling | ❌ | ⚠️ | ⚠️ | ⚠️ (inherent Android problem; best tools) |
| Offline-first storage | ⚠️ IndexedDB (eviction risk) | ✅ SQLite | ✅ SQLite (drift/sqflite) | ✅ Room/SQLite |
| Durable sync queue | ❌ Background Sync unreliable | ⚠️ JS queue + headless tasks | ⚠️ workmanager plugin | ✅ Room outbox + WorkManager |
| Media capture + queue | ⚠️ | ⚠️ | ⚠️ | ✅ CameraX + scoped storage |
| Idempotent sync | ✅ app-level (any) | ✅ | ✅ | ✅ |
| Future Field Trace (frame-level video) | ❌ | ⚠️ native anyway | ⚠️ native anyway | ✅ CameraX + MediaCodec path |
| Privacy-screening pipeline (on-device ML later) | ⚠️ | ⚠️ | ⚠️ | ✅ (ML Kit / LiteRT direct) |
| Notification & passive assistance | ⚠️ web push only | ✅ | ✅ | ✅ |
| Permission UX control | ❌ browser-mediated | ✅ | ✅ | ✅ |
| Play Store / sideload distribution | ⚠️ TWA wrapping | ✅ | ✅ | ✅ |
| Reuse of MVP v0.3 TS logic | ✅ direct | ✅ direct | ❌ rewrite (Dart) | ⚠️ port (~300 lines) |
| Solo-founder maintenance | ✅ lowest today, dead-end tomorrow | ⚠️ two ecosystems (JS + native) | ⚠️ two ecosystems (Dart + native) | ✅ one ecosystem |
| Migration cost from v0.3 | none | medium | medium-high | medium (small codebase) |
| Testing complexity | low | high (bridge + native) | high (channels + native) | medium (JUnit/Robolectric + instrumented) |

## 5. Detailed analysis — PWA (Candidate A)

Strengths:
- Zero migration cost; already the working validation baseline.
- Direct reuse of all TS logic and tests.
- Fast iteration; no store friction; installable.

Fatal gaps against DFI requirements:
1. **No overlay.** The web cannot draw over other Android apps.
   Picture-in-picture for web is video-element-only; there is no
   API path to a movable floating control above the carrier app.
   This alone eliminates the primary interaction model (D1).
2. **No background location.** Web geolocation stops with the page;
   there is no foreground-service equivalent. Personal behavior
   baseline and Route Breakdown evidence (C5) become unbuildable.
3. **Korean speech is network-bound.** Chrome's Web Speech API is
   server-backed; it fails exactly in the mountain/rural Offline
   state the product invariant targets. Note: `BOOTSTRAP.md` lists
   "browser-supported Korean speech capture" as a current feature —
   it should be understood as **Online-only**, which conflicts with
   invariant 14 for the future product (documented here rather than
   silently changed).
4. **Storage durability.** LocalStorage/IndexedDB are subject to
   browser eviction under storage pressure unless persistent-storage
   permission is granted and honored; Background Sync execution is
   browser-scheduled and not dependable for a media queue.
5. No foreground services, no OEM battery negotiation, no CameraX.

Verdict: correct choice for v0.1–v0.3; a dead end for everything the
handoff queue defines next (C4, C5, C6). Retained temporarily as the
evidence dashboard (§20, §24), not as the forward architecture.

## 6. Detailed analysis — React Native (Candidate B)

Strengths:
- Direct reuse of TS types/parser/metrics.
- Mature ecosystem; good in-app UI productivity.

DFI-specific problems:
1. **The overlay lives outside React.** A floating control over other
   apps must be a native Android `Service` attaching views through
   `WindowManager`. RN's view hierarchy is Activity-bound. Rendering
   RN components inside a Service window requires custom root-view
   hosting or headless-JS orchestration of native views. Community
   packages for floating bubbles are unmaintained and do not support
   press-hold-drag-release radial selection. In practice the entire
   D1/D2 surface — the core of the product — would be written in
   Kotlin anyway, with RN reduced to the in-app screens.
2. **Gesture fidelity across the bridge.** Per-frame drag hit-testing
   with haptic confirmation is exactly the workload where bridge
   latency and the old/new-architecture split add risk. Native
   `MotionEvent` handling is deterministic; RN gesture handlers in an
   overlay window are not a supported path.
3. Background location, foreground service types (Android 14+
   requires declared `foregroundServiceType` for microphone, location,
   camera), OEM survival, WorkManager queues: all native module work.
4. Two build systems, two upgrade treadmills (RN releases + Android),
   heavier test setup — a real cost for a solo founder.

Net: DFI would be a native Android app for its hard parts wearing an
RN shell for its easy parts. The reuse benefit (~300 lines of TS) does
not pay for the added seam.

## 7. Detailed analysis — Flutter (Candidate C)

Strengths:
- Excellent in-app UI toolkit; single-codebase rendering.
- `flutter_overlay_window` demonstrates overlays are possible.

DFI-specific problems:
1. **Overlay = second Flutter engine.** The plugin approach spawns a
   separate engine instance for the overlay window: additional
   memory (tens of MB) and battery in a component that must run all
   delivery day. Communication between overlay engine and main
   engine is message-passing, complicating shared state (contextual
   action sets, passive-assistance signals, queue status).
2. Plugin maintenance risk on the single most critical component;
   safe-area insets, cutouts, and gesture-nav bounds inside an
   overlay window are not first-class in the plugin surface —
   custom platform-channel work returns to Kotlin anyway.
3. **Zero reuse** of TS logic; Dart rewrite of parser/metrics/tests.
4. Same native-glue burden as RN for FGS types, background location,
   WorkManager-grade queues, CameraX (Field Trace would be platform
   channels around CameraX regardless).
5. New language + toolchain for the founder’s stack (current stack is
   TypeScript) with no offsetting platform reach (iOS excluded by D1).

Net: Flutter’s advantages target multi-platform UI breadth, which DFI
cannot use; its costs land directly on DFI’s hardest requirements.

## 8. Detailed analysis — Native Android / Kotlin (Candidate D)

Fit:
1. **Overlay**: `SYSTEM_ALERT_WINDOW` + `TYPE_APPLICATION_OVERLAY`
   window from a foreground service. Movable control, radial menu,
   and close target are plain views with direct `MotionEvent`
   streams — one `ACTION_DOWN`→`ACTION_MOVE`→`ACTION_UP` gesture
   naturally implements press-hold-drag-release (§10).
2. **Safe bounds**: `WindowMetrics` + `WindowInsets`
   (`statusBars`, `navigationBars`, `displayCutout`, `ime`) give the
   exact usable rectangle required by the Floating Interaction Model
   ("actual safe-bound calculation", not nine-zone approximation).
3. **Speech**: `SpeechRecognizer` with `ko-KR`; on-device recognition
   is available on mainstream Korean-market devices (Galaxy line) and
   degrades explicitly — the app can detect on-device availability
   and fall back to "record now, transcribe later" in Offline state,
   which matches invariant 14 instead of fighting it.
4. **Background location**: FusedLocationProvider inside a foreground
   service with `foregroundServiceType="location"`, sampling policy
   owned by DFI code (battery budget per session, batched delivery).
5. **Offline-first**: Room (SQLite) for entities + outbox;
   WorkManager for constraint-aware, process-death-surviving,
   backoff-managed sync (§13–§15); app-private media store.
6. **Camera/Field Trace**: CameraX now for photo evidence; the same
   stack extends to video capture and frame-level access for future
   semantic compression (C6) and privacy screening (C7, via on-device
   ML Kit face/text detection) without an architecture change.
7. **One ecosystem**: Kotlin + Gradle + Jetpack; one upgrade
   treadmill; first-party documentation for every hard requirement.

Costs (honest):
- Kotlin learning curve if the founder’s daily language is TS. Modern
  Kotlin + Compose is close in feel to typed FP-ish TS; the risky
  parts (Service/WindowManager) are risky in *every* candidate, but
  here they are at least first-party-documented.
- Port of ~300 lines of deterministic TS + tests (small, mechanical;
  keyword tables and regexes transfer nearly 1:1).
- In-app UI written twice (once in PWA, once in Compose) — but the
  in-app UI is deliberately minimal during validation.
- Android-only. Accepted: validation cohort is Android; iOS cannot
  host the interaction model anyway.

## 9. Overlay and floating-control feasibility

- Permission: `SYSTEM_ALERT_WINDOW` via
  `ACTION_MANAGE_OVERLAY_PERMISSION` (user grant screen; acceptable
  one-time onboarding cost for 2–5 known validation drivers).
- Host: a foreground service owns the overlay view; the service is
  the floating-control lifecycle. Started when a validation session
  starts; user-visible notification (Android requirement) doubles as
  session state (Online/Degraded/Offline + queue depth).
- Drag-to-close: a distinct lower-center target window shown only
  while the control is being dragged; entering it triggers a visual
  state change + `VibrationEffect.EFFECT_CLICK`; release closes the
  service. Visually and logically separate from the radial menu,
  which appears only from long-press (per Floating Interaction Model).
- Android 14/15 notes: declare `foregroundServiceType`
  (`location|microphone` as used); microphone FGS must start while
  the app/overlay has an eligible state; short-tap capture initiated
  from the visible overlay satisfies this.
- Play policy: `SYSTEM_ALERT_WINDOW` is permitted with justification;
  no accessibility-service abuse is needed (DFI does not read other
  apps’ screens — also correct for invariant 17,
  anti-surveillance). During Phase A/B, distribution is direct APK
  sideload, so store review is not a blocker (§18).

## 10. Adaptive radial-menu feasibility

Implementation principle (matches the product doc exactly):

1. On long-press, compute the desired ring of N action slots around
   the control’s current center.
2. Query usable bounds: `WindowMetrics.bounds` minus
   `WindowInsets` (status bar, nav/gesture area, cutout rects, IME
   when visible) minus the close-target reserve.
3. For each candidate slot, test the full **hit rect** (larger than
   the visible icon; target ≥ 48 dp visible, hit rect padded beyond)
   against the safe region.
4. If any slot fails, rotate the fan and/or compress angular spread
   toward the open half-plane; corners produce an inward quarter-fan;
   center allows a full radial layout. Reflow, don’t clip.
5. During `ACTION_MOVE`: nearest-valid-target computation each frame;
   highlight + enlarge nearest target; `EFFECT_TICK` on target entry;
   returning within the neutral radius clears selection; `ACTION_UP`
   outside any hit rect cancels safely.
6. Accidental-activation instrumentation (long-press threshold,
   cancel rate, wrong-target rate) is logged locally for C4
   measurement.

All of this is directly supported by plain Android views in an
overlay window. No candidate other than D can do step 2 (real inset
rects inside an overlay window) without custom native code — at
which point they *are* candidate D for this component.

Contextual action sets: a `RadialActionSet` value object (default
delivery set; vehicle-problem set; person-injured set) selected by
explicit user navigation for C4. No AI-driven personalization yet
(per scope control).

## 11. Background location and battery implications

- Location runs only during an explicit delivery session (foreground
  service with notification) — never silent tracking (invariant 17).
- Sampling policy owned by app code: balanced-priority requests,
  batched delivery, adaptive interval (coarser while moving steadily,
  finer after a stop event). Raw coordinates are processed into
  event-level signals locally; indefinite high-res movement history
  is not retained (AI memory boundary in the profile model).
- Battery budget is a C4/C5 measurement target, not a guess. The ADR
  commitment: architecture must let DFI *choose* its battery cost;
  only native code provides that control surface.
- OEM restrictions (Samsung sleeping-apps, aggressive task kill):
  mitigations are FGS + user whitelisting guidance + WorkManager for
  anything deferrable. This risk exists in all candidates; candidate
  D minimizes moving parts on top of it.

## 12. Online / Degraded / Offline behavior matrix

Degraded = network present but slow/unstable (timeouts, partial
transfers). Detection: connectivity callbacks + recent request
outcome window, surfaced as an explicit tri-state in the session
notification and UI.

| Function | Online | Degraded | Offline |
|---|---|---|---|
| Cached field-tip viewing | live + cache refresh | serve cache, refresh opportunistically, freshness label | serve cache with freshness label |
| Basic field-note capture | full | full (local-first; sync deferred) | full |
| Voice capture / Korean speech | on-device preferred; cloud allowed | on-device only | on-device if model present; else record audio locally, transcribe later |
| Structuring (deterministic now; LLM via C2 later) | local now; LLM later with timeout→fallback | deterministic fallback, no long waits | deterministic fallback |
| Micro-prompt response (feedback labels) | full, syncs | full, queued | full, queued |
| Photo capture | full, upload queued | capture; upload deferred | capture; upload deferred |
| Video / Field Trace capture (future) | capture; process; upload queued | capture; process locally; upload deferred | capture; process locally; upload deferred |
| Draft retention | durable local | durable local | durable local |
| Sync queue creation | immediate dispatch | enqueue, bounded retries, backoff | enqueue only; dispatch on reconnect |
| Facility / medical info | live | cached + freshness label; **never present stale opening hours as current** | cached + explicit staleness warning; opening-hours shown as "as of <date>" only |
| Emergency call entry | direct dialer intent — no DFI server dependency | same | same (dialer is OS capability) |
| Provisional credit state | provisional→confirmed on server ack | provisional, queued | provisional, queued |

Core rule encoded: no capture function fails because the network
disappeared; nothing stale is presented as current.

## 13. Local persistence recommendation

- **Room (SQLite)** as the single durable store:
  - `field_memory` (port of `FieldMemory`, adding `workerContext`
    and `mobilityClass` nullable columns now — cheap forward
    compatibility, no parcel-only hard-coding, per expansion rule),
  - `retrieval_feedback`, `validation_session` (ports),
  - `capture_draft` (refresh/process-death recovery parity),
  - `micro_prompt_response` (false-positive feedback labels),
  - `outbox` (§14), `media_item` (file URI + state + checksum).
- **Jetpack DataStore** for small preferences (capture mode, overlay
  position).
- Media files in app-private external storage; DB stores references.
- Profile-boundary enforcement in the schema: private-intelligence
  tables are local-only by construction in this phase; any future
  public projection is a separate explicit mapper (allow-list), never
  a query over private tables exposed outward (§17).

LocalStorage → Room migration: one-time JSON import using the
existing v0.3 export format (the export feature becomes the migration
bridge; no throwaway code).

## 14. Durable sync-queue recommendation

Transactional outbox + WorkManager:

- Every syncable event (confirmed memory, feedback label, outcome,
  media upload intent) is written to `outbox` **in the same Room
  transaction** as its domain write. No dual-write gap.
- `outbox` row: `client_id` (UUID/ULID generated at creation),
  `kind`, `payload_ref`, `state` (PENDING → IN_FLIGHT → ACKED |
  FAILED_RETRYABLE | FAILED_PERMANENT), `attempts`, `created_at`.
- One WorkManager unique periodic/expedited worker per queue class
  (small JSON events vs media), constrained on `NETWORK_CONNECTED`,
  exponential backoff, survives process death and reboot.
- Media uploads: single-object PUT with checksum; if validation shows
  large Field Trace files later, upgrade to resumable/chunked upload
  then — not now (smallest architecture rule).
- Duplicate prevention: dispatch is at-least-once; dedupe is
  server-side by `client_id` (§15). Client marks ACKED only on
  server-confirmed receipt, which is also the provisional→confirmed
  credit transition point (C8 compatibility without building C8).

## 15. Idempotency and reconnect strategy

- **Idempotency key = client-generated `client_id`** on every event
  at creation time (offline-safe; no server round-trip needed to
  create identity).
- Server contract (future, C-series backend work): upsert-by-
  `client_id`; replaying the same event is a no-op returning the
  canonical ack. Until a backend exists, the outbox simply
  accumulates — the schema is the commitment, not the server.
- Reconnect: WorkManager constraint firing drains PENDING in
  creation order; IN_FLIGHT rows older than a lease timeout revert to
  PENDING (crash-during-upload recovery); ACK transitions domain rows
  from provisional to confirmed.
- Bounded retries: per-row attempt cap → FAILED_RETRYABLE surfaces in
  Personal Field Analytics ("N items waiting to sync"), never silent
  data loss.

## 16. Media capture and Field Trace compatibility

- Now: CameraX `ImageCapture` for risk-photo evidence; EXIF minimized
  (strip precise metadata not needed for the fact; location attached
  as an explicit structured field, not hidden EXIF).
- Future Field Trace (C6): CameraX `VideoCapture`/frame analysis +
  `MediaCodec` for compression; sensor fusion (location + heading)
  timestamps for fork/turn detection; on-device ML Kit for
  face/plate/doorplate screening before anything leaves the device
  (C7). Candidate D is the only option where this pipeline needs no
  cross-language boundary. Nothing beyond `ImageCapture` is built in
  the first migration.

## 17. Profile and data-boundary implications

- Three-layer model maps to code boundaries, not just policy text:
  - Private Intelligence Profile → local Room tables, no network
    writer exists for them in this phase; future server sync of any
    baseline signal requires an explicit new outbox `kind` (auditable
    choke point).
  - Personal Field Analytics → read-only local projections
    (queries over local tables), rendered only in-app.
  - Public Field Profile → does not exist in this phase; when built,
    it is a serializer over an explicit allow-list DTO. The
    architecture makes "private silently becomes public" require
    deliberate new code in one reviewable place.
- The overlay/service layer never reads private-profile tables except
  through the passive-assistance signal interface (future C4/C5),
  keeping surveillance-adjacent data out of the always-on component.

## 18. Privacy and permission implications

Permissions and their moment-of-need requests:
- `SYSTEM_ALERT_WINDOW` — onboarding, with plain-language rationale.
- `RECORD_AUDIO` — first voice capture.
- `ACCESS_FINE_LOCATION` (+ FGS location type) — first session start
  that enables location; background access is FGS-based, not
  `ACCESS_BACKGROUND_LOCATION`-based, keeping the grant model simpler
  and honest ("only while a delivery session runs").
- `CAMERA` — first photo evidence.
- `POST_NOTIFICATIONS` — session notification (Android 13+).
No accessibility service. No reading other apps. No continuous audio
retention (speech buffers discarded after transcription; raw audio
kept only in the explicit offline record-then-transcribe path, then
deleted after confirmation). This is the permission set of an honest
field tool, defensible for later Play review.

Distribution: Phase A/B via direct APK to known drivers (no store
gate); Play internal testing track when Phase B stabilizes.

## 19. Solo-founder maintenance implications

- One language (Kotlin), one build system (Gradle), one platform,
  first-party libraries only (Compose, Room, WorkManager, CameraX,
  FusedLocation, SpeechRecognizer). Zero critical third-party
  dependencies on the overlay path — the component DFI cannot afford
  to have break on an OS update.
- RN/Flutter would add a second ecosystem whose upgrades interact
  with the native layer (historically the most painful maintenance
  mode for solo devs).
- Testing: JUnit for ported parser/metrics (parity with existing
  `tests/*.test.mjs` cases), Robolectric for Room/queue logic,
  small instrumented set for overlay geometry. The gesture/overlay
  behavior is additionally validated by C4 field measurement, which
  is more informative than UI test automation at this stage.

## 20. Reusable MVP v0.3 components

Directly portable (mechanical Kotlin port, tests carried over):
- `src/types.ts` — domain model (extended with nullable
  worker/mobility context; enums preserved).
- `src/fieldMemoryParser.ts` — deterministic Korean keyword/regex
  structurer (keyword tables and patterns transfer as-is).
- `src/validationMetrics.ts` — metric definitions (median capture
  time, zero-correction rate, useful-feedback rate).
- `tests/parser.test.mjs`, `tests/validationMetrics.test.mjs` —
  cases become JUnit fixtures (behavioral parity gate).

Reusable as-is (not code):
- JSON/CSV export format → becomes the LocalStorage→Room migration
  input and keeps evidence continuity across the migration.
- Validation protocol instrumentation semantics (session logs,
  correction tracking, outcome feedback) — re-implemented 1:1.
- The PWA itself remains temporarily deployed as the evidence
  dashboard / export viewer until native parity, then is archived
  (not deleted) as validation history.

Not reusable: DOM UI (`main.ts`, `style.css`), service worker,
manifest.

## 21. Migration cost and risks

Cost estimate (solo founder, focused windows):
- Kotlin project scaffold + overlay spike (= C4): the largest and
  most uncertain block; it is deliberately first because its result
  can invalidate assumptions cheaply.
- Domain port + Room + parity tests: small (codebase is ~740 LOC
  total including UI).
- Capture/confirm/reuse/export parity: moderate.

Risks:
1. **OEM process management** (Samsung/Xiaomi killing the FGS or
   throttling the overlay) — highest risk; measured in C4 on the
   founder’s actual device before any further investment.
2. **Accidental activation / gesture ergonomics** under real delivery
   pressure — C4 measurement targets already defined in the
   interaction doc.
3. Kotlin ramp-up slowing iteration for the first weeks — mitigated
   by the tiny domain size and by keeping the PWA running as the
   fallback capture tool until native parity.
4. On-device ko-KR recognition quality variance across devices —
   fallback path (record-then-transcribe) designed in from the start.
5. Play policy friction on `SYSTEM_ALERT_WINDOW` — deferred risk;
   sideload distribution decouples validation from store review.

## 22. Final decision

**Adopt Native Android / Kotlin as the DFI mobile architecture.**

Concretely: a single-module Kotlin app — Jetpack Compose for in-app
screens; a foreground-service-hosted `WindowManager` overlay for the
floating control and adaptive radial menu; Room + transactional
outbox + WorkManager for offline-first persistence and idempotent
sync; `SpeechRecognizer` (ko-KR, on-device preferred) for voice
capture; CameraX for evidence media; FusedLocationProvider inside the
session foreground service for future baseline work.

Why it won:
- The floating overlay with high-fidelity drag gestures is the
  product’s primary interface, and it is a native-Android-only
  capability in every candidate — PWA cannot do it at all, and
  RN/Flutter can only do it by embedding the same native code behind
  an extra seam.
- iOS cannot host this interaction model, so cross-platform
  frameworks forfeit their main benefit for DFI.
- Every other hard requirement (offline Korean speech, FGS background
  location, durable process-death-safe queues, CameraX/Field Trace,
  battery control, on-device privacy screening) is first-party
  Jetpack territory.
- One ecosystem is the lowest realistic maintenance burden for a solo
  founder once the overlay requirement is accepted as central.

MVP v0.3 (PWA) remains the active validation baseline until the
native app reaches capture-loop parity (per BOOTSTRAP: implementation
remains v0.3 until the architecture decision is documented — this
document is that decision, pending founder approval).

## 23. Rejected alternatives

- **PWA continuation** — rejected: no overlay, no background
  location, network-bound Korean speech, unreliable background sync.
  Fails decision drivers D1–D4 outright.
- **React Native** — rejected: the core interaction surface must be
  native regardless; RN adds a second ecosystem and a bridge seam to
  save ~300 lines of portable TS. Fails D7 relative to D; no
  compensating platform reach (iOS excluded by D1).
- **Flutter** — rejected: overlay requires a second engine or custom
  channels; zero TS reuse; new language; same native glue burden for
  FGS/location/queues/camera. Fails D7 and D8; no compensating reach.
- **Hybrid A: Kotlin overlay + WebView/PWA in-app UI** — rejected:
  preserves the PWA’s storage-durability and speech weaknesses inside
  the product and creates a permanent two-runtime maintenance seam
  for a UI that is deliberately minimal anyway.
- **Hybrid B: Kotlin Multiplatform for shared logic** — rejected as
  premature: there is no second platform to share with (D1 excludes
  iOS for the core model). Revisit only if a non-overlay iOS product
  is ever approved.

## 24. Smallest migration sequence

Each step is independently stoppable; field evidence gates
continuation (decision rule).

1. **C4 spike as the first native code**: Kotlin project; overlay
   permission flow; movable floating control; long-press adaptive
   radial menu with safe-bound reflow; drag-to-close; short-tap
   `SpeechRecognizer` ko-KR capture into a plain text preview;
   accidental-activation and battery logging. No Room, no sync, no
   product data. Outcome decides everything downstream.
2. Port `types` / `fieldMemoryParser` / `validationMetrics` to Kotlin
   with JUnit parity tests against the existing `.mjs` cases.
3. Room persistence + capture→deterministic structure→confirm→
   list/search→outcome feedback→session logs (v0.3 loop parity),
   plus one-time JSON import from the PWA export.
4. Evidence export parity (JSON/CSV via Android share sheet).
5. Outbox + WorkManager skeleton (schema + local queue only; no
   backend).
6. Switch founder Phase-A capture to the native app; keep the PWA
   read-only for historical evidence; update `BOOTSTRAP.md` and
   `WORK_LOG.md` to move the baseline off v0.3.

Steps 1–2 are the only work authorized by the current queue after
this ADR (C4 is queue priority 2). Steps 3–6 require the founder’s
go after C4 findings.

## 25. Next single engineering action

> Execute **C4 — Android Floating Capture and Passive Assistance
> Feasibility Spike** as a new native Kotlin project, scoped exactly
> to §24 step 1, measuring overlay viability, radial-menu
> accidental-activation rate, and battery impact on the founder’s
> device.

---

## Appendix — Product-document conflict check

Reviewed against all seven source documents. No product-philosophy
conflicts introduced. Two clarifications recorded rather than
silently changed:

1. `BOOTSTRAP.md` lists "browser-supported Korean speech capture" as
   a current capability; it is Online-only (server-backed Web Speech)
   and therefore does not satisfy invariant 14 for the future
   product. The native decision resolves this; the v0.3 limitation
   should be noted in `BOOTSTRAP.md` at the next doc update.
2. The Floating Interaction Model’s emergency rule ("emergency-call
   entry should not depend on DFI server availability") is satisfied
   architecturally via OS dialer intents; DFI never proxies emergency
   calls. Recorded here as a binding constraint for C4.
