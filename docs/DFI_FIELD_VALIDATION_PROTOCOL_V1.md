# DFI Field Validation Protocol v1

Last updated: 2026-07-14 Status: Pre-field validation protocol Product
baseline: MVP v0.3

## Purpose

Primary question:

> Can accumulated expert field knowledge reduce the adaptation cost of a
> substitute or new driver?

This test does not attempt to prove nationwide market fit.

## Validation phases

### Phase A --- Founder self-use

Use DFI during normal delivery work to identify capture friction, AI
correction burden, genuinely useful information, and functions the
founder avoids under real work pressure.

### Phase B --- Substitute/new-driver use

Target 2 to 5 substitute or less-experienced drivers who actually cover
the route.

Measure whether stored field knowledge reduces confusion and dependence
on the experienced route owner.

Do not begin a broad public beta before Phase A produces a usable route
knowledge base.

## Knowledge seeding rule

Do not document every delivery destination.

Record only locations that are repeatedly difficult for new drivers,
often cause calls to the route owner, have misleading pins or addresses,
have hard-to-identify entrances, create meaningful wrong-entry loss,
have non-obvious vehicle access, contain recurring physical risks, or
materially benefit from a photo or Field Trace.

Initial validation target:

> 10 to 30 high-friction locations

## Founder self-use period

Recommended minimum: 4 weeks.

Preferred if practical: 6 to 8 weeks.

Do not create artificial tip volume. Capture only real reusable field
problems or facts.

## Capture observation

Where technically feasible, record capture start/completion time, input
mode, AI structuring use, unchanged acceptance, corrections,
cancellation, and optional cancellation reason.

Core founder test:

> Would the founder still use this interaction while tired and under
> delivery pressure?

If the founder repeatedly thinks "I will record it later," the capture
flow is too expensive.

## Substitute-driver metrics

Measure where feasible:

-   address-search failures
-   location-confusion calls/messages to the route owner
-   wrong-entry events
-   backtracking or revisits
-   cases spending more than three minutes locating a destination
-   DFI tip views
-   tip views followed by reported resolution
-   unresolved locations after tip views
-   voluntary second-session use
-   new field facts contributed by substitute drivers

Strongest early metric:

> Reduction in route-owner help calls/messages caused by location
> confusion

## Outcome feedback

After a tip is viewed, use minimal factual feedback:

-   해결됨
-   일부 도움
-   도움 안 됨
-   정보가 오래됨/다름

Optional reason categories:

-   위치 설명 부족
-   사진 필요
-   경로 영상 필요
-   차량 조건이 다름
-   현장 상태 변경
-   정보 자체가 틀림

Do not ask for star ratings.

## End-of-session questions

Use no more than three short, skippable questions:

1.  오늘 DFI 정보로 실제 해결된 장소가 있었나?
2.  정보가 없어서 가장 오래 헤맨 곳은 어디였나?
3.  가장 귀찮거나 방해된 기능은 무엇이었나?

## Evidence interpretation

Do not claim improved efficiency merely because total route time
decreased. Route time is affected by volume, traffic, weather, driver
condition, parking, customer contact, loading quality, and route
composition.

Prefer event-level evidence.

Stronger evidence:

-   fewer location-confusion calls
-   fewer wrong entries
-   fewer search events over three minutes
-   successful resolution after a tip view
-   repeated voluntary reuse

Weaker evidence:

-   subjective praise
-   one unusually fast day
-   founder enthusiasm
-   raw tip count

## Failure signals

Treat these as product warnings:

-   founder frequently postpones capture
-   capture interaction is too long
-   AI output requires repeated rewriting
-   substitute drivers use the app only when reminded
-   tips are viewed but do not resolve confusion
-   useful information cannot be expressed safely without personal data
-   location matching is frequently wrong
-   battery impact causes app disablement
-   notifications or AI questions interrupt delivery work

Revise or remove failed features rather than defending them.

## Phase A exit criteria

Move to substitute-driver testing when:

-   at least 10 genuinely difficult locations contain reusable knowledge
-   basic capture is acceptable during real field work
-   confirmation/correction flow is understandable
-   no known critical PII publication path remains in the tested flow
-   evidence export works
-   repeated real delivery sessions have been completed with DFI

## Phase B success criteria

Do not set one rigid pass/fail number before baseline data exists.

Positive signals include measurable reduction in location-confusion help
calls/messages, repeated tip-assisted resolutions, fewer severe search
events, voluntary repeat use, substitute-driver contribution, and
acceptable correction burden.

## Experiment log

For every meaningful product change, record:

> Date: Observed problem: Hypothesis: Change: Expected effect: Result:
> Decision: keep / revise / delete

Do not change several major interaction variables at once when the
effect needs to be measured.

## Validation order

1.  Founder capture friction
2.  Founder knowledge reuse
3.  Substitute-driver knowledge reuse
4.  Help-call reduction
5.  Missing-knowledge discovery
6.  Substitute-driver contribution
7.  Experience Mining and EER experiments after the basic loop is
    credible

## Decision rule

> Real field problem → smallest test → evidence → keep, revise, or
> delete

DFI should earn complexity through field evidence.
