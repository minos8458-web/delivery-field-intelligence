# DFI Floating Interaction and Field Assistance Model v1

Last updated: 2026-07-14 Status: Product interaction design

## Purpose

Define the minimum interaction model for DFI's Android floating control
and field-assistance entry points.

The design goal is one-hand, low-interruption use during commercial
field work.

## Core interaction

### Short tap

> short tap -\> voice capture -\> command/intention classification -\>
> safe execution candidate or structured preview

Examples: - record a field tip - report a risk - ask about nearby field
knowledge - request a vehicle-service search - request a
medical/facility search

AI must not silently perform a high-impact action when intent is
ambiguous.

### Long press

> long press -\> adaptive radial action menu

The user should be able to keep the finger down, move toward an action,
and release to select.

Avoid:

> long press -\> release -\> tap another button

The additional interaction step is undesirable during field work.

### Drag to close

While the floating control is held or dragged, a dedicated close target
may appear near the lower center safe area.

> drag to close target -\> clear visual confirmation -\> release -\>
> close floating control

Closing the floating control must be visually distinct from selecting a
field action.

## Safe-area-aware adaptive radial action menu

The radial menu must not use one fixed expansion direction.

The menu should adapt to the floating button's current screen position
and available safe bounds.

Consider: - screen edges - top status area - display cutout or camera
hole - bottom navigation/gesture area - keyboard where relevant - close
target - one-hand reachability

Candidate behavior: - right edge -\> actions expand inward/left - left
edge -\> actions expand inward/right - bottom edge -\> actions expand
upward - top edge -\> actions expand downward - corner -\> actions use
an inward fan - center -\> broader radial layout may be used

Implementation principle:

> calculate candidate action positions -\> test against safe bounds -\>
> rotate/reflow action angles -\> preserve all selectable hit targets
> inside the usable screen

Do not rely only on a coarse nine-zone screen classification if actual
safe-bound calculation is available.

## Hit target and selection feedback

Visible icon size and selectable hit area are separate concepts.

The hit target should be larger than the visible icon where platform
guidance and layout permit.

During drag selection: - nearest valid action becomes highlighted -
selected action may enlarge - haptic feedback may confirm target entry -
moving back to the neutral center cancels selection - releasing outside
valid targets cancels safely

Accidental activation must be measured during the C4 feasibility spike.

## Contextual action sets

The radial menu is not a complete app launcher.

Show only the actions most likely to matter in the current field
context.

Candidate default delivery set: - photo evidence - Field Trace - field
question - risk report - vehicle problem - person injured

Candidate vehicle-problem set: - nearby vehicle service - tire/wheel -
emergency/roadside service - specialty/commercial vehicle service

Candidate person-injured set: - emergency call entry - nearby emergency
medical facility - night/holiday care - pharmacy

The initial spike should use a small fixed action set before attempting
complex AI-driven menu personalization.

## Driver Emergency and Medical POI

DFI should separate vehicle continuity from worker health assistance.

Vehicle problems belong to Vehicle Service POI.

Human injury or urgent health-related facility search belongs to Driver
Emergency and Medical POI.

Candidate facility categories: - emergency medical facility - emergency
department - night/holiday care - orthopedic or trauma-relevant care
where externally categorized - pharmacy

DFI may add field-worker access context such as: - 1-ton vehicle access
difficulty - temporary commercial-vehicle stopping experience - rear
parking/access experience - recent field-worker facility-use
confirmation

DFI must not publish personal diagnosis, treatment history, or
identifiable medical narratives.

Prefer transformed facility facts.

Example:

Raw private report: `배송 중 넘어져 발목을 다쳐 여기서 진료받았다.`

Potential shared fact:
`배송 중 발생한 외상 상황에서 해당 의료시설 이용 경험 보고.`

The individual injury history remains private.

## Emergency boundary

DFI must not diagnose injury severity or claim that a community-ranked
facility is medically appropriate for an emergency.

For a potentially serious emergency, official emergency response should
be prominent.

Community field knowledge is secondary to official emergency
information.

The product should distinguish: - emergency action - nearby
medical-facility discovery - field-worker access context

Do not rank medical facilities by unverified treatment outcome.

## Voice-command routing

Short-tap voice input may identify a field-assistance intent.

Examples: - `차가 이상해` - `타이어가 터진 것 같아` - `사람이 다쳤어` -
`근처 응급실` - `약국 찾아줘`

The command router should return: - detected intent - confidence -
proposed action - whether confirmation is required

Ambiguous input should produce a minimal clarification.

Example:

> `다쳤어`

Possible response:

> `사람 부상인가요, 차량 파손인가요?`

Do not infer a medical emergency from weak evidence and silently place
calls.

## Offline and degraded-network behavior

The floating interaction itself must remain responsive offline.

When network data is unavailable: - local capture remains available -
risk report may be queued - photo/video capture may be queued -
micro-feedback may be queued - cached facility information may be shown
with freshness labeling

Do not present stale cached medical opening information as current.

Emergency-call entry should not depend on DFI server availability,
subject to platform capability.

## Credit boundary

Emergency actions do not earn contribution credit merely because they
were used.

A later privacy-safe field fact may earn contribution credit if the user
intentionally contributes useful information.

Examples: - commercial-vehicle access fact for a repair facility -
relevant risk photo - verified field-access fact for a medical facility

Do not create incentives to report injuries, accidents, or emergencies
for reward.

## C1 and C4 architecture requirements

The Mobile Architecture ADR and Android Floating Capture Spike must
evaluate: - overlay support - adaptive radial geometry - safe-area
calculation - drag-and-release selection - close-target interaction -
haptic feedback - accidental activation - contextual action-set
switching - offline responsiveness - voice-intent routing boundary -
emergency-action separation

## Validation questions

Measure: - Can the founder select an action one-handed without looking
for long? - Do edge and corner button positions preserve all action
targets? - How often is the wrong action highlighted or selected? - Is
drag-to-close confused with action selection? - Are six actions too many
under delivery pressure? - Does the user remember short tap versus long
press? - Does voice capture replace menu use for common actions? - Which
actions are actually used in the field?

## Decision rule

> The floating control should reduce interaction cost, not become a
> miniature complicated app menu.

DFI should expose the smallest safe action set needed at the moment.
