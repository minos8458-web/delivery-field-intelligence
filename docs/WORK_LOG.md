# Work Log

## 2026-07-13

- Repository created: `minos8458-web/delivery-field-intelligence`
- Direct GitHub connector file write attempted and blocked by integration-level 403 on Contents API.
- Created local project scaffold using Vite + TypeScript.
- Implemented the first closed validation loop: capture → draft structure → driver confirm → local store → search.
- Added optional browser speech recognition.
- Added field memory schema, vision, validation plan, AI support alignment notes, and Claude handoff queue.

## 2026-07-13 — MVP v0.2

- Added field-validation instrumentation without changing the deterministic structuring baseline.
- Added capture-to-confirm duration measurement.
- Added field-level draft correction tracking.
- Added explicit operational outcome feedback for reused memories.
- Added validation metric dashboard.
- Added JSON and CSV evidence export.
- Kept real LLM provider and prompt evaluation work in the Claude handoff queue.

## 2026-07-13 — MVP v0.3

- Added PWA manifest and service worker shell caching.
- Added refresh-safe unfinished capture draft recovery.
- Added explicit field-validation session start/end logging.
- Added mobile one-hand interaction adjustments and larger touch targets.
- Preserved the deterministic parser boundary; real AI provider remains Claude-deferred.

## 2026-07-14 — C1 Mobile Architecture ADR

- Created `docs/ADR_MOBILE_ARCHITECTURE_V1.md`.
- Decision: Native Android / Kotlin (Compose in-app UI, foreground-service WindowManager overlay, Room + transactional outbox + WorkManager, SpeechRecognizer ko-KR, CameraX).
- PWA continuation, React Native, Flutter, and two hybrid variants rejected with justification.
- MVP v0.3 PWA remains the validation baseline until native capture-loop parity; PWA export format designated as the LocalStorage→Room migration bridge.
- No source code changed. C2–C8 not started.
- Next action: C4 Android floating-capture feasibility spike per ADR §24 step 1.

## 2026-07-14 — C4 Android Floating Capture Spike

- Created isolated native Android/Kotlin spike project at `android-spike/` (Gradle, minSdk 26, targetSdk 35, zero third-party runtime dependencies; PWA MVP v0.3 untouched).
- Implemented: `TYPE_APPLICATION_OVERLAY` floating control + non-touchable render layer, foreground service (microphone type, duplicate-start guard, safe cleanup), sealed-class gesture state machine (short tap → ko-KR `SpeechRecognizer` voice capture with on-device detection on API 31+; long press → press-hold-drag-release radial selection with neutral/outside cancellation; button move with safe-bounds clamp; lower-center drag-to-close), deterministic safe-area-aware radial geometry (2° feasibility scan, grow-before-shrink radius schedule, degraded fallback), append-only event log with press/selection timings and target-change counts, battery-percent + duration session logging, permission flow Activity (overlay deep link, RECORD_AUDIO, POST_NOTIFICATIONS on 33+, no request loops).
- Created `docs/C4_ANDROID_FLOATING_CAPTURE_SPIKE.md` (20 sections). Decision: FEASIBLE WITH CONDITIONS.
- Tests actually run: 27/27 JVM tests pass (geometry 15, gesture state machine 12) via kotlinc 2.0.21 + local runner; commands recorded in the spike doc repo package.
- NOT performed: no Android device or emulator validation; Gradle/AGP build not executed (Google Maven unreachable in the implementation environment). No field validation is claimed.
- Repository main was not modified by the engineering environment (no push credentials); files delivered as a package for founder upload.

## 2026-07-14 — C4 Permission Boundary Fix (narrow correction)

- Fixed `MainActivity.kt`: session start was gated only on `SYSTEM_ALERT_WINDOW`; `RECORD_AUDIO` was not checked before `startForegroundService`. Added `canStartSession(): String?` helper that gates on both required permissions with Korean-language blocking reason.
- Classified permissions explicitly: `SYSTEM_ALERT_WINDOW` and `RECORD_AUDIO` are technically required (missing RECORD_AUDIO causes `SecurityException` on Android 14+ for `FOREGROUND_SERVICE_TYPE_MICROPHONE`); `POST_NOTIFICATIONS` is recommended but does not block service start.
- Removed two genuinely empty directories from workspace: `app/src/main/res/` and `gradle/wrapper/` (no Gradle wrapper or resource files are needed for the spike; the remaining Java package directories are required by Gradle namespace discovery).
- Reran full JVM test suite: 27/27 pass. No test changes needed.
- `docs/C4_ANDROID_FLOATING_CAPTURE_SPIKE.md` §5 already described the corrected permission classification accurately; no documentation change needed.
- Repository main not modified (no push credentials). Refreshed package: `dfi-c4-spike-package.zip`.

## 2026-07-14 — C4 Gradle Wrapper Script Correction (narrow)

- Observed founder-side Windows failure: `'em' is not recognized` and `'""java.exe""' is not recognized` when running `.\gradlew.bat :app:assembleDebug`.
- Root cause 1: custom comment `@rem Generated for DFI C4 spike — Gradle 8.9` contained an em-dash (U+2014, non-ASCII). cmd.exe parsed the non-ASCII bytes as a token and tried to execute `em` as a command.
- Root cause 2: custom `set JAVA_EXE=%JAVA_HOME%/bin/java.exe` without `%JAVA_HOME:"=%` stripping, combined with the outer `"%JAVA_EXE%"` quoting, produced double-double-quoted `""java.exe""` invocation.
- Fix: replaced both `gradlew` and `gradlew.bat` with authentic scripts fetched from `github.com/gradle/gradle` at tag `v8.9.0`. Both verified: `gradlew.bat` is clean ASCII (0 non-ASCII bytes); `gradlew` contains only the UTF-8 copyright © symbol (expected, shell-safe).
- `gradle/wrapper/gradle-wrapper.jar` and `gradle-wrapper.properties` unchanged (previously validated).
- JVM tests actually run: 27/27 pass. No device/emulator validation performed. Windows cmd.exe execution not available in this environment.
- Refreshed manual-upload package: `dfi-c4-spike-package.zip`.

## 2026-07-14 — C4 Real-Device Partial Validation (Samsung Galaxy, Android 16)

- Build confirmed: `.\gradlew.bat :app:assembleDebug` BUILD SUCCESSFUL on Windows (Android SDK Platform 35, OpenJDK 21.0.10). APK installed on Samsung Galaxy Android 16.
- Permissions confirmed: SYSTEM_ALERT_WINDOW, RECORD_AUDIO, POST_NOTIFICATIONS all granted; session start succeeded; floating overlay button appeared.
- Real-device evidence confirmed (log excerpts from device):
  - SHORT_TAP detection: pressMs=32–81 ms across multiple interactions
  - VOICE_CAPTURE_STARTED|onDevice=true|lang=ko-KR confirmed on this device
  - VOICE_RESULT returned with Korean text across all three test utterances (어디 갔어, 오늘 날씨가 좋네, 잃어버렸대 and others)
  - BUTTON_MOVE_STARTED / BUTTON_MOVE_ENDED confirmed; no accidental voice during drag
  - Session survived host-app switching (KakaoTalk, Chrome) with overlay and voice functional over both apps
  - Session survived ~30 s screen lock/unlock; UI showed 세션: 실행 중 after unlock
- NOT tested in this session: long press → radial menu (checklist items 4–5), drag-to-close (item 6), duplicate-start guard (item 7), OEM recents-swipe survival (item 8), delivery-length battery measurement (item 9).
- Radial menu confirmed present in implementation (OverlayViews.showRadial, GestureStateMachine long-press path) but not triggered during this validation session.
- C4 status updated: INCOMPLETE — remaining checklist items 4–9.
- Updated docs/C4_ANDROID_FLOATING_CAPTURE_SPIKE.md §13, §17, §19, §20 with real-device facts.

## 2026-07-14 — C4 Real-Device Full Validation (Samsung Galaxy, Android 16) — Session 2

Additional real-device evidence confirmed. All C4 core interaction criteria now validated.

Confirmed in this session:
- Long press → radial menu opened on physical device.
- Press-hold-drag-release selection worked: RADIAL_SELECTION_CONFIRMED logged, RADIAL_ACTION_SELECTED|action=QUESTION logged.
- Radial cancellation observed and logged.
- Adaptive radial layout confirmed visually inside safe bounds near top-left edge/corner.
- Drag-to-close fully exercised: CLOSE_TARGET_ENTERED, OVERLAY_CLOSED_BY_DRAG, SESSION_STOPPED, SERVICE_DESTROYED logged.
- Battery/session log confirmed functional: SESSION_STOPPED|batteryPct=83|durMs=708199.
- After a new session, app was swiped from Recents: overlay remained visible. One transient non-reproduced drag hesitation noted immediately after swipe; subsequent drag worked normally. Not classified as confirmed defect.

Not tested: duplicate-start guard (checklist item 7). Long-duration battery soak not claimed.

C4 final status: COMPLETE WITH FOLLOW-UP RISK.
Updated docs/C4_ANDROID_FLOATING_CAPTURE_SPIKE.md §17, §18, §19, §20 with final real-device facts.

## 2026-07-14 — C4 Radial Geometry Criterion 6 Fix

Real-device failure: floating button near screen corners produced fewer than 6 visually distinct, non-overlapping radial targets. Criterion 6 (safe-area-aware adaptive radial menu) was not met.

Root cause (two related bugs):
1. The degraded fallback in `RadialGeometry.layout()` placed each slot via `clampCircle` independently. At corners, multiple slots clamped to the same boundary pixel — producing minDist=0–4px between targets, far below the required 2×hitRadius (264px at density 3.0). The existing JVM tests used hitRadius=96px which accidentally avoided this failure; the real service uses 44dp × density ≈ 115–132px.
2. `assertSlotsDistinct(l, 40f)` in tests used a 40px threshold — far below the actual non-overlap requirement — masking the bug.

Fixes:
- `RadialGeometry.kt`: replaced per-slot-clamp degraded fallback with a radius-scaling boundary-arc scan that guarantees pairwise non-overlap. Added post-clamp collision guard in `finalize()` that routes to the boundary fallback if clamping produces collisions.
- `RadialGeometryTest.kt`: replaced `assertSlotsDistinct(40px)` with `assertSlotsNonOverlapping()` using `2 × hitRadius` threshold. Added `assertAllTargetsInsideBounds(layout, bounds)` helper. Added regression tests at density 2.625 and 3.0 (corners and edges), and a `degradedBoundaryFallbackNeverOverlaps` test using the exact Samsung Galaxy Android 16 scenario.

Tests: 31/31 pass (was 27, +4 new geometry tests). No gesture logic changed. No device/emulator validation performed for this fix; new APK required for real-device re-validation of Criterion 6.

## 2026-07-14 — C4 Criterion 6 Hexagonal Geometry Redesign

Real-device failure (multiple positions): radial targets showed inconsistent distances from the DFI control, compressed/colliding layouts at edges, and as few as 5 identifiable targets at corners. Root cause was the prior arc-fan algorithm: it varied angular gap, ring radius, and fan direction per position, then clamped targets individually — producing all three observed symptoms.

Design decision: stable hexagonal ring with inward menu-center shift.
- 6 targets × 60° × one common radius R = consistent geometry at every position.
- On long press, derive nearest valid menuCenter inside safe bounds (margin = R + hitRadius).
- All 6 slot centers computed as menuCenter + R×(cosθ, sinθ). Zero per-target clamping.
- Floating-button anchor never mutated; restored to resting position after selection/cancel.
- nearestSlot neutral zone now tests against menuCenter (not anchor) to match rendered ring.

Files changed:
- `android-spike/app/src/main/java/com/dfi/spike/geometry/RadialGeometry.kt` — full replacement with hexagonal-shift algorithm. RadialLayout gains `menuCenter` and `shifted` fields.
- `android-spike/app/src/main/java/com/dfi/spike/overlay/OverlayService.kt` — updated layout() call (removed actionCount/gap args), neutral zone now `ringRadius * 0.5f` against menuCenter.
- `android-spike/app/src/test/java/com/dfi/spike/geometry/RadialGeometryTest.kt` — full rewrite. 17 geometry tests enforcing complete Criterion 6 contract (6 slots, uniform R, 60° spacing, bounds containment, non-overlap, nearest-menuCenter shift, anchor immutability, determinism, regression cases for Samsung Galaxy top-left and top-right corners).

Tests: 29/29 pass (geometry 17, gesture 12). New APK required for real-device re-validation.

## 2026-07-14 — Documentation: DFI_HAZARD_MEMORY_MODEL_V1 and DFI_DELIVERY_KNOWLEDGE_MODEL_V1

Created two product/architecture design documents capturing founder-approved design decisions from post-C4 field discussion.

- `docs/DFI_HAZARD_MEMORY_MODEL_V1.md` — Hazard Memory Loop, retrospective hazard reporting, Hazard Candidate Window, POINT_HAZARD/SEGMENT_HAZARD data shapes, evidence states (REPORTED/SUPPORTED/CONFIRMED/STALE/RETIRED), post-passage one-action factual verification, warning timing direction, Road Condition Summary, alert suppression, Hazard Identity and clustering, Road Segment Risk History, multidimensional reporter-reliability model, independent evidence principle, speed hump / road infrastructure distinction, and future incident/public-safety boundaries. All sections labeled as APPROVED PRINCIPLE, DESIGN DIRECTION, UNVALIDATED HYPOTHESIS, or FUTURE DIRECTION.
- `docs/DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` — Explicit delivery start, Personal Delivery Baseline, automatic context inference states, context-sensitive interaction budget, three-layer data model (Raw Trace / Personal Delivery Knowledge / Handoff Package), Delivery Knowledge Model (DKM), fact/observation/personal-pattern provenance distinction, Knowledge Handoff design, Handoff authorization, today's work vs. historical knowledge boundary, substitute driver experience principles, user data ownership principle, shared vs. private knowledge boundary, and future validation questions.

No source code changed. No tests changed. No PWA or Android spike modified. C4 acceptance status unchanged. C5 not begun.
