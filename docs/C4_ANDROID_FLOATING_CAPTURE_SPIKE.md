# C4 — Android Floating Capture and Passive Assistance Feasibility Spike

Status: Implemented (code + JVM tests). Device validation pending (see §17).
Date: 2026-07-14
Depends on: docs/ADR_MOBILE_ARCHITECTURE_V1.md (C1, approved)

---

## 1. Spike goal

Answer: can DFI's core Android field interaction — a floating control over a
foreground carrier app, short-tap Korean voice capture, long-press
press-hold-drag-release radial action selection, drag-to-close, and
foreground-service session lifecycle — work reliably enough to justify
continued native-mobile investment?

## 2. Exact scope

In scope: overlay permission flow, movable floating control, short tap →
ko-KR SpeechRecognizer capture (result logged, no production intent routing),
long press → adaptive radial menu with the fixed six-action set (Photo, Field
Trace, Question, Risk, Vehicle Problem, Person Injured; selection only logs
`RADIAL_ACTION_SELECTED`), press-hold-drag-release selection with neutral and
outside-release cancellation, safe-area-aware radial geometry, drag-to-close,
explicit gesture state machine, accidental-activation event log, battery
percentage at session start/stop, foreground-service start/stop/cleanup.

Out of scope: C2/C3/C5–C8, PWA migration, background location, camera
permission (Photo / Field Trace are placeholders), Room/WorkManager/CameraX/
Hilt/networking, cloud transcription, production AI routing, polished UI.

The PWA MVP v0.3 is untouched.

## 3. Android project location

`android-spike/` at the repository root — a self-contained Gradle project,
structurally separate from the Vite PWA.

```
android-spike/
  settings.gradle.kts, build.gradle.kts, gradle.properties
  app/build.gradle.kts
  app/src/main/AndroidManifest.xml
  app/src/main/java/com/dfi/spike/
    MainActivity.kt
    geometry/Geom.kt, geometry/RadialGeometry.kt      (pure Kotlin, JVM-tested)
    gesture/GestureModel.kt, gesture/GestureStateMachine.kt (pure Kotlin, JVM-tested)
    log/SpikeEventLog.kt
    speech/VoiceCaptureController.kt
    overlay/OverlayViews.kt, overlay/OverlayService.kt
  app/src/test/java/com/dfi/spike/
    geometry/RadialGeometryTest.kt
    gesture/GestureStateMachineTest.kt
  tools/                       (local JVM test harness only, not in Gradle build)
```

## 4. Minimum supported Android/API assumptions

- minSdk 26 (Android 8.0): `TYPE_APPLICATION_OVERLAY` floor. Below O the
  required overlay window type does not exist.
- targetSdk / compileSdk 35.
- API 30+ gets exact safe bounds via `WindowMetrics` + insets; API 26–29 uses
  a documented approximate fallback (§10).
- API 31+ gets explicit on-device speech detection; below 31 the spike can
  only prefer offline and report the ambiguity (§13).
- Android 14+ (API 34): a microphone-type foreground service may only be
  started while the app is in the foreground. The spike always starts the
  session from `MainActivity`, which satisfies this.

## 5. Overlay permission flow

`MainActivity` shows permission state and three user-initiated request
buttons with Korean rationale text:

1. Overlay: `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` deep link
   (`SYSTEM_ALERT_WINDOW` cannot be granted via a runtime dialog).
2. Microphone: standard `RECORD_AUDIO` runtime request.
3. Notifications: `POST_NOTIFICATIONS` runtime request on API 33+.

No request loops: each request fires once per button tap; denial is shown in
the status text, never auto-retried.

### Permission classification

**Technically required — session start is blocked if missing:**

- `SYSTEM_ALERT_WINDOW`: `OverlayService` calls `WindowManager.addView` with
  `TYPE_APPLICATION_OVERLAY` immediately on start. Without this the service
  crashes or the window is silently rejected.
- `RECORD_AUDIO`: Android 14+ (API 34) throws `SecurityException` when
  `startForegroundService` is called for a `FOREGROUND_SERVICE_TYPE_MICROPHONE`
  service without this permission granted. Below API 34 the service starts but
  `SpeechRecognizer` fails, producing a broken session with no voice capture.
  The gate applies on all API levels to prevent a partially-functioning session.

**Strongly recommended, NOT technically required to start the service:**

- `POST_NOTIFICATIONS` (API 33+): denial only silences the persistent
  notification. The service starts, the overlay appears, and the session runs
  normally. The Activity status text warns the tester but does not block start.

### Start-eligibility check

`canStartSession(): String?` in `MainActivity` checks `SYSTEM_ALERT_WINDOW`
then `RECORD_AUDIO` in order. Returns `null` (proceed) or a Korean-language
reason string displayed as a Toast and in the status text. On API 34+ the
reason string notes the `SecurityException` risk explicitly. Session start
button is not disabled in advance (to avoid SDK-level polling), but the
inline check fires on every tap and never proceeds silently past a missing
required permission.

## 6. Foreground-service design

`OverlayService`:

- Explicit start via `startForegroundService` from the Activity; explicit
  stop via `stopService` or the notification's "세션 종료" action
  (`ACTION_STOP`).
- Persistent low-importance notification on channel `dfi_spike` describing
  the active session and interaction hints.
- `startForeground(…, FOREGROUND_SERVICE_TYPE_MICROPHONE)` on API 29+;
  manifest declares `foregroundServiceType="microphone"` and
  `FOREGROUND_SERVICE_MICROPHONE`.
- A `@Volatile running` flag makes repeated start intents no-ops, preventing
  duplicate overlay windows.
- `onDestroy` removes both windows with `runCatching`, destroys the speech
  recognizer, routes `StopRequested` through the state machine so cleanup
  effects run exactly once, logs `SESSION_STOPPED` (battery, duration) and
  `SERVICE_DESTROYED`, and clears `running`.
- Activity closure does not stop the service; the session outlives the
  launcher UI, which is the intended field behavior.

## 7. Gesture state model

Sealed `GestureState` in `gesture/GestureModel.kt`; pure transition function
in `GestureStateMachine.on(event): List<Effect>`. The service feeds raw touch
and timer events and executes returned effects (window moves, haptics,
logging, voice start, teardown). No behavior lives in scattered flags.

States: `Idle`, `Pressed`, `MovingButton(closeArmed)`,
`RadialActive(highlighted, openedAt, targetChanges)`, `VoiceCapture`,
`Stopped`. (`RADIAL_TARGET_HIGHLIGHTED` and `CLOSE_TARGET_ACTIVE` from the
task brief are modeled as fields of `RadialActive` / `MovingButton` rather
than separate states — fewer states, identical observable behavior.)

Transitions:

```
Idle          --Down-------------------------→ Pressed        [StartLongPressTimer]
Pressed       --Up (within slop, < timer)----→ VoiceCapture   [SHORT_TAP, StartVoice]
Pressed       --LongPressElapsed-------------→ RadialActive   [LONG_PRESS_STARTED, OpenRadial, RADIAL_OPENED, haptic]
Pressed       --Move (beyond slop)-----------→ MovingButton   [cancel timer, BUTTON_MOVE_STARTED, ShowCloseTarget]
MovingButton  --Move-------------------------→ MovingButton   [MoveButtonTo; CLOSE_TARGET_ENTERED / CLOSE_CANCELLED on arm change]
MovingButton  --Up (close armed)-------------→ Stopped        [OVERLAY_CLOSED_BY_DRAG, CloseOverlay]
MovingButton  --Up (not armed)---------------→ Idle           [SettleButton(clamp), BUTTON_MOVE_ENDED]
MovingButton  --LongPressElapsed (stale)-----→ (ignored)
RadialActive  --Move-------------------------→ RadialActive   [HighlightTarget, RADIAL_TARGET_ENTERED + tick on change]
RadialActive  --Up over target---------------→ Idle           [RADIAL_SELECTION_CONFIRMED, SelectAction, CloseRadial]
RadialActive  --Up neutral/outside-----------→ Idle           [RADIAL_SELECTION_CANCELLED, CloseRadial]
VoiceCapture  --Down/Move/Up-----------------→ (ignored)
VoiceCapture  --VoiceEnded-------------------→ Idle
any state     --StopRequested----------------→ Stopped        [cancel timer, HideCloseTarget, CloseRadial]
Stopped       --anything---------------------→ (absorbed)
```

Move-drag and radial-drag cannot conflict: `MovingButton` is only reachable
before the long-press timer fires (which is cancelled on slop exit), and
`RadialActive` is only reachable while still within slop.

## 8. Floating-button move behavior

Drag beyond touch slop (12 dp) before the 400 ms long-press threshold moves
the button window (`MoveButtonTo` → `updateViewLayout`). On release,
`SettleButton` clamps the resting center into current safe bounds via
`Bounds.clampCircle`. Position persistence across process death was not
implemented (explicitly optional for C4).

## 9. Radial-menu geometry algorithm

`RadialGeometry.layout(safe, anchor, actionCount, preferredRadius, hitRadius,
preferredGapDeg, exclusions, minSeparation)` — deterministic, pure Kotlin:

1. Sample candidate angles in 2° steps around the anchor.
2. An angle is feasible at a given radius if the full hit circle placed there
   fits inside safe bounds and avoids exclusion circles.
3. Find the longest circular run (wrap-aware) of feasible angles.
4. Radius schedule `[1.0, 1.3, 1.6, 2.0, 2.5, 0.85, 0.7, 0.55] ×
   preferredRadius` — grow before shrink, because near corners the feasible
   arc is ~90° at any radius, so adjacent-target separation requires a larger
   ring, not a smaller one.
5. Required angular gap per radius is derived from the physical
   `minSeparation` (default two hit radii, i.e. tangent circles), so targets
   never overlap regardless of radius.
6. Slots are fanned symmetrically around the feasible arc's bisector. A fully
   free anchor yields an upward-centered fan.
7. If no radius admits all targets, a documented degraded fallback spaces
   slots evenly and clamps each into bounds (`degraded = true`; the service
   logs `RADIAL_LAYOUT_DEGRADED`).

Not a nine-zone table; the fan direction and span emerge from actual bounds.
`nearestSlot(touch, layout, neutralRadius, maxSelectDist)` returns the
highlighted slot or null (neutral zone / too far), giving the required
neutral-center cancellation.

## 10. Safe-bounds handling

`SafeBoundsProvider.current()`:

- API 30+: `WindowManager.currentWindowMetrics.bounds` minus
  `getInsetsIgnoringVisibility(statusBars | navigationBars | displayCutout)`.
  Covers status bar, navigation/gesture area, and cutouts exactly.
- API 26–29: real display size minus conservative estimated insets (32 dp
  top, 48 dp bottom). Exact cutout rects are not reliably available to
  overlay windows on these versions. Documented limitation; acceptable for a
  spike because the estimate errs toward shrinking usable space.

Safe bounds are re-read at radial open and at close-target display, so
rotation between gestures is handled without a configuration listener.

## 11. Drag-to-close behavior

Only in `MovingButton`: a lower-center close target (48 dp radius, 32 dp
above safe bottom) appears. Entering its hit area (1.3× visual radius) arms
it — visual highlight + double-click haptic + `CLOSE_TARGET_ENTERED`. Leaving
disarms (`CLOSE_CANCELLED`). Release while armed logs
`OVERLAY_CLOSED_BY_DRAG` and stops the service (full cleanup in §6). Closing
is structurally distinct from radial selection: the close target exists only
in `MovingButton`, radial targets only in `RadialActive`, and the two states
are mutually unreachable within one gesture. No unrelated app state is
touched.

## 12. Voice-capture behavior

Short tap → `VoiceCaptureController.start()`:

- API 31+ with `SpeechRecognizer.isOnDeviceRecognitionAvailable`:
  `createOnDeviceSpeechRecognizer` (offline-capable).
- Otherwise: default recognizer with `RecognizerIntent.EXTRA_PREFER_OFFLINE`,
  and the on-device limitation is logged, not hidden.
- Language `ko-KR`. Results log `VOICE_RESULT` with recognized text and
  `onDevice` flag; a 4 s banner shows the text. Errors log `VOICE_ERROR` with
  code and mapped name (e.g. `ERROR_LANGUAGE_UNAVAILABLE`,
  `ERROR_INSUFFICIENT_PERMISSIONS`). No production intent routing; the
  deterministic demonstration is text-in → `VOICE_RESULT` logged.

## 13. Offline speech limitation/availability findings

Static findings (no device in this environment — §17):

- On-device ko-KR availability is detectable only on API 31+, and actual
  language-pack presence is device/user dependent. On Samsung/Google devices
  with the Korean offline pack installed, on-device recognition is expected;
  without it, `ERROR_LANGUAGE_UNAVAILABLE` or network-dependent recognition
  results.
- Below API 31 the spike can only request `EXTRA_PREFER_OFFLINE` and cannot
  verify it was honored. `describeAvailability()` reports this ambiguity in
  the Activity status text.
- The spike does not fake offline speech and adds no cloud provider.
- Follow-up requirement (documented, not implemented): "record now,
  transcribe later" — persist raw audio locally when recognition is
  unavailable offline and transcribe on reconnect. This belongs to the future
  offline-first pipeline, not C4.

## 14. Accidental-activation event logging

`SpikeEventLog` appends `epochMs|EVENT|k=v` lines to
`filesDir/spike_events.log` (viewable/clearable in the Activity). Events:

`SESSION_STARTED`, `SESSION_STOPPED`, `SERVICE_DESTROYED`, `SHORT_TAP`
(pressMs), `LONG_PRESS_STARTED` (pressMs), `RADIAL_OPENED`,
`RADIAL_TARGET_ENTERED` (index), `RADIAL_SELECTION_CONFIRMED` (index,
openToSelectMs, targetChanges), `RADIAL_SELECTION_CANCELLED` (openToCancelMs,
targetChanges), `BUTTON_MOVE_STARTED`, `BUTTON_MOVE_ENDED` (moveMs),
`CLOSE_TARGET_ENTERED`, `CLOSE_CANCELLED`, `OVERLAY_CLOSED_BY_DRAG`,
`VOICE_CAPTURE_STARTED` (onDevice, lang), `VOICE_RESULT` (text, onDevice),
`VOICE_ERROR` (code, name), `RADIAL_LAYOUT_DEGRADED`, `RADIAL_ACTION_SELECTED`
(action).

Press duration, open-to-selection duration, and per-interaction target-change
count are recorded as required. This is a local spike log, not the DFI
evidence schema.

## 15. Battery/session logging

`SESSION_STARTED` and `SESSION_STOPPED` record
`BatteryManager.BATTERY_PROPERTY_CAPACITY` (percent) plus elapsed-realtime
session duration. Sufficient for the manual field question "did an overlay
session across a delivery period cost obviously unacceptable battery?".
Noted but not implemented (scope): `BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER`
deltas give µAh-level resolution on devices that support it; a follow-up
could log it alongside percentage at zero extra complexity. No analytics
platform. No battery-efficiency claim is made.

## 16. Automated test coverage

27 JVM tests, all passing (pure-Kotlin modules only):

- `RadialGeometryTest` (15): left/right/top/bottom edges, all four corners,
  center (non-degraded at preferred radius), determinism for identical input,
  exclusion avoidance, tiny-bounds degraded fallback still inside bounds,
  `nearestSlot` neutral-zone null / closest selection / far-outside null. All
  layouts assert every hit target fully inside supplied safe bounds.
- `GestureStateMachineTest` (12): short tap → voice; touches ignored during
  voice; long press opens radial; drag-release selects without a second tap;
  neutral-return cancel; outside-release cancel; target-change counting; slop
  exit enters move + stale timer ignored; settle+clamp on move end;
  drag-to-close; close disarm on exit; StopRequested wins from any state and
  absorbs further events.

Device-only behavior (WindowManager, SpeechRecognizer, service lifecycle,
haptics, insets) is intentionally isolated in `overlay/`, `speech/`, `log/`
and `MainActivity`, and is identified as instrumented/manual-validation
territory.

## 17. Device/emulator validation actually performed

None. No Android device or emulator was available in the implementation
environment, and the Gradle/AGP build itself could not be executed there
(Google Maven unreachable). Verified in this environment: 27/27 JVM tests on
the geometry and gesture modules via kotlinc 2.0.21 / Java 21. All
device-dependent claims in this document are design-level, not observed.

Required on-device checklist (founder, one physical device first):

1. `./gradlew :app:assembleDebug` builds; APK installs.
2. Overlay permission deep link works; button appears over another app.
3. Short tap → Korean utterance → `VOICE_RESULT` logged; check `onDevice`.
4. Long press → radial opens; drag highlights with haptic ticks; release
   selects; neutral/outside release cancels.
5. Radial near all four corners and edges stays inside safe bounds on a
   cutout + gesture-nav device.
6. Drag-to-close arms, disarms, closes; notification disappears; no orphan
   window.
7. Reopen session: exactly one button (duplicate-start guard).
8. Swipe app from recents during a session; observe service survival per OEM.
9. 2–4 h delivery-length session; compare battery percentages in the log.

## 18. Known OEM/background risks

- Aggressive OEM process management (Samsung "sleeping apps", Xiaomi MIUI,
  OnePlus) can kill even microphone-type foreground services; per-OEM
  battery-optimization exemption may be needed for real field sessions.
- Android 14+ forbids starting a microphone FGS from the background: if the
  OS kills and restarts the process, the service cannot self-resurrect with
  microphone type; the driver must reopen the app. Acceptable for validation;
  a product-phase mitigation is a `specialUse`/`mediaProjection`-free design
  review in C5+.
- Some OEM launchers delay or coalesce `updateViewLayout`, making button
  drag jerky; needs on-device confirmation.
- SpeechRecognizer behavior varies by default recognizer app (Google vs
  Samsung); ko-KR offline pack presence is not guaranteed.
- API 26–29 inset fallback is approximate near cutouts.

## 19. Findings

- The entire interaction core (geometry + gesture semantics) fits in pure
  Kotlin with zero Android dependencies and is fully unit-testable — the
  riskiest logic is the most tested part.
- Press-hold-drag-release, neutral cancellation, drag-to-close, and
  move-vs-radial disambiguation compose cleanly in one small state machine
  (6 states, ~180 lines) with no flag soup.
- Adaptive safe-area radial layout does not need an optimizer; a 2°-sampled
  feasibility scan with a grow-before-shrink radius schedule handles edges,
  corners, and exclusions deterministically. Corners specifically require
  growing the ring — a shrink-only heuristic fails there (caught by tests).
- Everything that remains unproven is Android-runtime behavior: OEM service
  survival, SpeechRecognizer ko-KR offline reality, overlay touch latency,
  and battery cost. These are exactly what the on-device checklist measures.
- Total spike size ~1,400 lines of Kotlin with zero third-party runtime
  dependencies — solo-founder maintainable.

## 20. C4 feasibility decision

**FEASIBLE WITH CONDITIONS**

The interaction model is implementable with direct, supported Android APIs
and its logic core is proven by deterministic tests. Conditions that must be
confirmed on a real device before treating C4 as closed:

1. Gradle build + install succeeds (unbuildable in the implementation
   environment; no exotic configuration is used).
2. On-device gesture checklist in §17 passes on at least one gesture-nav,
   cutout device.
3. ko-KR recognition produces usable text on the founder's device class;
   record on-device vs network-dependent status.
4. A delivery-length session does not show obviously unacceptable battery
   drain, and the foreground service survives typical OEM management (or the
   required exemption is documented).

If condition 2 or 4 fails on mainstream Korean-market devices (Samsung), the
decision degrades to NOT FEASIBLE for the overlay-centric model and C1's
fallback discussion reopens. No current evidence suggests this.
