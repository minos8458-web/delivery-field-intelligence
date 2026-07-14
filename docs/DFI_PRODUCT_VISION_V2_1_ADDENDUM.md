# DFI Product Vision v2.1 Addendum --- Field Risk, Passive Assistance, and Offline Intelligence

Last updated: 2026-07-14 Status: Product direction addendum Parent
document: `DFI_PRODUCT_VISION_V2.md`

## Purpose

This addendum records product decisions discovered after the v2 vision
was written.

If this addendum conflicts with the parent document on the topics below,
this addendum is the newer decision.

## 1. Passive Assistance Principle

DFI must not assume a driver will actively ask for help.

Some drivers may continue struggling, avoid calling an experienced
driver, redistribute work, or experience route breakdown without
reporting the problem.

Product principle:

> DFI should make relevant help discoverable before the user explicitly
> asks, while minimizing interruption.

Possible interaction pattern:

-   relevant field knowledge exists nearby
-   floating control shows a quiet availability signal
-   stronger intervention occurs only when multiple anomaly signals
    justify a question candidate
-   the user can dismiss with one factual tap

DFI is not only a question-answering tool for highly proactive users.

## 2. Help-Seeking Bias

Help-request frequency must not be used as a direct measure of route
difficulty.

> No help request does not mean no field problem occurred.

Calls and messages are evidence, but hidden struggle and route breakdown
require separate measurement.

## 3. Personal Behavior Baseline

Experience Mining should compare common anomaly signals with the
driver's own normal behavior.

Stationary duration alone is insufficient.

The system must distinguish, through repeated labeled feedback where
possible:

-   destination search
-   parcel search
-   loading reorganization
-   parking/access difficulty
-   customer contact
-   rest
-   no problem

A factual response such as `배송지는 찾았으며 배송할 상품 찾는 중` is
training feedback for false-positive reduction.

## 4. Route Breakdown Event

DFI should model a Route Breakdown Event as a meaningful failure of
normal route flow.

Examples include:

-   work redistributed to other drivers because the route became
    unmanageable
-   repeated unresolved local-area searching
-   major backtracking/revisit patterns
-   abandonment or deferral of a confusing section
-   unusual coworker rescue caused by route confusion

Route breakdown is a stronger validation signal than absence or presence
of a help call.

## 5. Micro Contribution Credit

Short responses to DFI-initiated factual questions may receive small
contribution credit.

The reward is for useful response labor and model correction, not for
tapping volume.

Possible value layers:

-   valid factual response
-   later false-positive reduction
-   contribution to shared field knowledge

Do not increase question frequency to manufacture engagement or credit
activity.

## 6. Offline-first Product Invariant

DFI must assume unstable and unavailable mobile networks in mountain,
rural, and remote delivery areas.

Core field interaction must degrade gracefully.

Network states:

-   Online
-   Degraded
-   Offline

The product should support local capture and queued synchronization.
Cached field knowledge, drafts, micro-prompt responses, and media
capture should not be lost because the network disappears.

Repeated network instability may become a spatial knowledge candidate
when supported by sufficient evidence and relevant network context.

## 7. Field Risk Intelligence

DFI should create a dedicated field-knowledge layer for commercial
vehicle spatial risk.

Relevant evidence includes:

-   actual contact/collision
-   near miss
-   entry abandonment
-   turning difficulty
-   reverse-exit difficulty
-   blind structures
-   clearance risk
-   repeated side-contact risk
-   unusual terrain or structural hazards

The system should extract the spatial condition rather than publish
accident gossip.

Example:

Raw report: `후진하다 오른쪽 담벼락을 긁었다.`

Structured risk candidate: - vehicle class: 1-ton box truck - maneuver:
reverse exit - risk type: side contact - physical condition candidate:
restricted right-side clearance

A single report does not automatically create a public accident-risk
label.

## 8. Risk Photo Evidence

A relevant photo may receive a small additional contribution credit.

The additional reward must remain modest.

Useful photo targets include:

-   protruding walls
-   low roofs or overhangs
-   steep slopes
-   narrow turning areas
-   blind structures
-   sharp bends
-   drainage ditches or drop-offs
-   low-clearance structures
-   unusual obstacles or terrain features

Photo upload alone is not sufficient.

> A photo is supporting field evidence, not decorative content.

AI and moderation should screen privacy and identification risks before
publication.

## 9. Risk Contribution Allocation

Do not reward damage amount or accident severity.

Reward knowledge contribution.

Contribution roles may include:

-   initial discovery
-   concrete incident or near-miss report
-   relevant visual evidence
-   factual confirmation
-   vehicle-class-specific validation
-   later validation of an emerging pattern

Initial discoverers may receive a small provisional reward and later
validated-discovery credit.

Meaningful validators should also receive credit.

The first reporter does not own the shared risk knowledge.

## 10. Architecture consequence

The next Mobile Architecture ADR and Experience Mining design must
explicitly evaluate:

-   personal behavior baseline storage
-   false-positive feedback labels
-   route-breakdown event representation
-   low-interruption passive assistance
-   offline capture queue
-   degraded-network retry behavior
-   provisional versus confirmed credit
-   risk-event and near-miss schemas
-   privacy-safe photo evidence handling

These requirements should be considered before large-scale mobile
implementation.
