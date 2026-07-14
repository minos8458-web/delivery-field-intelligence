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
