# DFI Field Validation Protocol v1.1

Last updated: 2026-07-14 Status: Pre-field validation protocol Product
baseline: MVP v0.3

## Purpose

Primary question:

> Can accumulated expert field knowledge reduce the adaptation cost and
> hidden route breakdown of a substitute or new driver?

This test does not attempt to prove nationwide market fit.

## Validation phases

### Phase A --- Founder self-use

Use DFI during normal delivery work to identify capture friction, AI
correction burden, genuinely useful information, false-positive
intervention, offline behavior, and functions the founder avoids under
real work pressure.

### Phase B --- Substitute/new-driver use

Target 2 to 5 substitute or less-experienced drivers who actually cover
the route.

Measure whether stored field knowledge reduces confusion, hidden route
breakdown, severe search events, and dependence on informal coworker
rescue.

Do not begin a broad public beta before Phase A produces a usable route
knowledge base.

## Help-Seeking Bias

Do not use help requests as a direct proxy for route difficulty.

Some drivers actively call experienced drivers. Others may continue
alone, avoid asking, redistribute work, or experience route breakdown
without informing the route owner.

Therefore:

> No help request does not mean no field problem occurred.

Calls and messages remain useful evidence, but only as one signal.

## Route Breakdown Event

A Route Breakdown Event is a field event in which the driver can no
longer maintain a normal delivery flow and must materially abandon,
reconstruct, or externally redistribute the route strategy.

Candidate examples:

-   work redistributed to other drivers because the route became
    unmanageable
-   repeated backtracking or revisits across the same local area
-   prolonged unresolved destination search
-   repeated entry and exit from a confusing access area
-   a section or group of deliveries being abandoned or deferred because
    of route confusion
-   unusual dependence on coworker rescue

A single GPS pattern must not automatically prove route breakdown.

## Personal Behavior Baseline

Experience Mining must combine:

> common anomaly signals + the individual driver's normal behavior
> baseline

A stationary vehicle near a destination may mean:

-   destination search
-   parcel search inside the vehicle
-   loading reorganization
-   parking/access difficulty
-   customer contact
-   rest
-   no problem

Therefore, stationary duration alone must not be classified as "lost."

Candidate spatial-confusion signals may include combinations of:

-   repeated pass-by
-   repeated entry/exit
-   U-turn or direction reversal clusters
-   repeated local-area revisits
-   unusual backtracking
-   prolonged map-area dwell
-   deviation from the driver's own normal stop pattern

Repeated behavior creates a question candidate, not an automatic
explanation.

## False Positive Feedback

When DFI is uncertain, use a short factual prompt.

Example:

> 이 장소에서 시간이 조금 지연되고 있어요.

Possible responses:

-   배송지 찾는 중
-   배송지는 찾았으며 배송할 상품 찾는 중
-   주차·진입 문제
-   고객 연락 중
-   잠시 쉬는 중
-   문제 없음

These responses are not merely dismissal buttons. They are labeled
feedback for improving the personal behavior baseline and reducing
future false positives.

DFI must avoid frequent prompts. Question generation is based on
information need, not engagement targets.

## Micro Contribution Credit

If DFI asks a factual training or verification question, the user's
response consumes field time and may receive a small contribution
credit.

Principle:

> DFI-initiated response labor is not assumed to be free.

Credit must remain small and abuse-resistant.

Suggested logic:

-   valid micro-response: small provisional contribution
-   response later shown to reduce false positives: additional validated
    contribution
-   response helps shared field knowledge: additional validated
    contribution

Do not issue unlimited identical rewards for repetitive or
low-information tapping.

Question frequency must never be increased merely to create engagement
or credit activity.

## Knowledge seeding rule

Do not document every delivery destination.

Record only locations that are repeatedly difficult, often create hidden
search loss, have misleading pins or addresses, have hard-to-identify
entrances, create meaningful wrong-entry loss, have non-obvious vehicle
access, contain recurring physical risks, or materially benefit from a
photo or Field Trace.

Initial validation target:

> 10 to 30 high-friction locations

## Field Risk Intelligence

DFI may collect structured field-risk events related to commercial
vehicle movement.

Candidate event types:

-   actual vehicle contact/collision
-   near miss
-   entry abandonment
-   turning difficulty
-   reverse-exit difficulty
-   visibility restriction
-   clearance risk
-   repeated side-contact risk
-   terrain or structure-related vehicle hazard

The purpose is not to publish accident gossip.

The purpose is to identify repeated spatial conditions associated with
vehicle risk.

A single incident must not automatically create a public "accident-prone
area" label.

Risk knowledge should combine incident reports, near misses, vehicle
class, physical conditions, repeated confirmations, recency, and where
appropriate visual evidence.

## Risk Photo Evidence

Photos may receive a small additional contribution credit when they
materially improve understanding or verification of a field-risk fact.

Examples of useful visual evidence:

-   protruding wall
-   low roof or overhang
-   steep slope
-   narrow turning area
-   blind structure
-   sharp bend
-   drainage ditch or drop-off
-   low-clearance structure
-   unusual obstacle or terrain feature

Photo attachment alone does not earn meaningful extra credit.

The photo must be relevant to the reported field fact.

Principle:

> A photo is supporting field evidence, not decorative content.

Before publication, screen for faces, vehicle plates where
inappropriate, doorplates, customer-linked information, and other
identifying text.

## Risk contribution allocation

Do not reward accident severity or damage amount.

Reward contribution to shared risk knowledge.

Possible contribution roles:

-   initial risk discovery
-   concrete near-miss or incident report
-   relevant photo evidence
-   repeated factual confirmation
-   vehicle-class-specific confirmation
-   later validation of an emerging risk pattern

An initial reporter may receive a small provisional contribution. If the
location later becomes a validated risk pattern, the initial discoverer
and meaningful validators may receive additional contribution credit.

Do not allow the first reporter to monopolize all later value.

## Offline-first field validation

DFI must assume some delivery areas have unstable or unavailable mobile
data.

Core field actions should not fail merely because the network is
unavailable.

Expected modes:

-   Online
-   Degraded
-   Offline

Offline-capable field actions should eventually include cached-tip
viewing, local capture, local response to micro-prompts, photo/video
capture, draft retention, and synchronization queueing.

Provisional credit may be shown locally, but server synchronization
should confirm final credit to reduce duplication and manipulation.

Repeated network instability may itself become a spatial knowledge
candidate, separated by carrier/network context where relevant.

## Founder self-use period

Recommended minimum: 4 weeks.

Preferred if practical: 6 to 8 weeks.

Do not create artificial tip volume. Capture only real reusable field
problems or facts.

## Core validation metrics

Measure where feasible:

-   severe destination-search events
-   wrong-entry events
-   repeated local revisits/backtracking
-   route breakdown events
-   workload redistribution caused by route confusion
-   cases spending more than three minutes locating a destination
-   DFI tip views
-   tip views followed by reported resolution
-   recovery after a DFI intervention
-   unresolved locations after tip views
-   voluntary second-session use
-   new field facts contributed by substitute drivers
-   false-positive intervention rate
-   micro-prompt response rate
-   AI correction burden
-   offline capture and later sync success

Calls/messages to the route owner remain secondary evidence, not the
primary metric.

## Outcome feedback

After a tip is viewed, use minimal factual feedback:

-   해결됨
-   일부 도움
-   도움 안 됨
-   정보가 오래됨/다름

Optional reasons:

-   위치 설명 부족
-   사진 필요
-   경로 영상 필요
-   차량 조건이 다름
-   현장 상태 변경
-   정보 자체가 틀림

Do not ask for star ratings.

## End-of-session questions

Use no more than three short, skippable questions:

1.  오늘 배송 흐름이 무너지거나 다른 기사에게 넘긴 구간이 있었나?
2.  정보가 없어서 가장 오래 헤맨 곳은 어디였나?
3.  가장 귀찮거나 방해된 기능은 무엇이었나?

## Evidence interpretation

Do not claim improved efficiency merely because total route time
decreased.

Prefer event-level evidence.

Stronger evidence:

-   fewer severe search events
-   fewer wrong entries
-   fewer route breakdown events
-   less work redistribution caused by route confusion
-   successful recovery after a tip or intervention
-   repeated voluntary reuse

Weaker evidence:

-   subjective praise
-   one unusually fast day
-   founder enthusiasm
-   raw tip count
-   absence of help calls

## Failure signals

Treat these as product warnings:

-   founder frequently postpones capture
-   capture interaction is too long
-   AI output requires repeated rewriting
-   DFI repeatedly mistakes parcel search for location confusion
-   prompts become annoying or interrupt delivery
-   substitute drivers use the app only when reminded
-   tips are viewed but do not resolve confusion
-   useful information cannot be expressed safely without personal data
-   location matching is frequently wrong
-   battery impact causes app disablement
-   offline captures are lost or duplicated

Revise or remove failed features rather than defending them.

## Experiment log

For every meaningful product change, record:

> Date: Observed problem: Hypothesis: Change: Expected effect: Result:
> Decision: keep / revise / delete

Do not change several major interaction variables at once when the
effect needs to be measured.

## Validation order

1.  Founder capture friction
2.  Founder knowledge reuse
3.  False-positive intervention learning
4.  Offline capture reliability
5.  Substitute-driver knowledge reuse
6.  Hidden route-breakdown detection
7.  DFI-assisted recovery
8.  Missing-knowledge discovery
9.  Substitute-driver contribution
10. Experience Mining and EER experiments after the basic loop is
    credible

## Decision rule

> Real field problem → smallest test → evidence → keep, revise, or
> delete

DFI should earn complexity through field evidence.
