# DFI User Intelligence and Profile Model v1

Last updated: 2026-07-14 Status: Product and data-boundary design

## Purpose

Define how DFI learns from field activity while separating AI
personalization, private self-analytics, and public trust information.

Core rule:

> Data usefulness does not imply public visibility.

## Three-layer profile model

### 1. Private Intelligence Profile

Visibility: system and AI processing only.

Purpose: - reduce false positives - personalize passive assistance -
compare current behavior with a personal baseline - improve question
timing - understand relevant field context

Candidate signals: - field-worker context - mobility profile - confirmed
vehicle class - normal stop-duration distribution - parcel-search
feedback patterns - destination-search feedback patterns -
revisit/backtracking baseline - micro-prompt response patterns -
preferred capture mode - correction burden - contribution categories -
offline/degraded-network frequency

Prohibited use: - public personality labeling - public behavioral
ranking - public route exposure - employment-performance scoring -
claims about laziness, competence, honesty, or mental state

The system must not publicly label a user as someone who "does not ask
for help" or "easily panics."

### 2. Personal Field Analytics

Visibility: user only.

Purpose: - return useful field patterns to the worker - explain AI
personalization - allow correction of wrong interpretations - create
personal value from accumulated data

Candidate views: - severe-search candidates - parcel-search stop
patterns - false-positive intervention trend - DFI-assisted
resolutions - risk contributions - offline/degraded areas encountered -
contribution-credit history - AI correction-burden trend - frequently
contributed knowledge categories

Analytics must distinguish: - confirmed - user-reported - inferred
candidate - insufficient evidence

The user should be able to correct important inferred categories.

### 3. Public Field Profile

Visibility: other DFI users.

Purpose: - communicate contribution history - provide context for
field-information trust - make cross-field participation visible

Recommended public fields: - nickname - selected field-worker context -
selected carrier/transport-context badge - broad mobility or
vehicle-class experience - broad activity region - Field Trust -
contribution level - validated contribution count - field-risk discovery
count - useful photo-evidence count - accepted-answer count - major
contribution domains - DFI activity duration

Example:

> 탄현러너 택배 · 1톤 탑차 수도권 북서부 활동 Field Trust 82 기여 레벨
> 14 검증된 정보 121건 위험정보 발견 7건 유효 사진자료 23건 주요 기여:
> 복잡한 진입로 / 지번지역 / 1톤 차량 접근

## Public-profile exclusions

Do not publicly expose by default: - legal name - phone number - exact
delivery route - exact frequent delivery addresses - real-time
location - work start/end pattern - exact camp or depot -
customer-related viewing history - accident history - injury or
medical-use history - personal behavior baseline - AI-inferred
help-seeking behavior - exact network-disconnection movement history -
private correction history

## Field Worker Profile

DFI should not permanently model every user as a parcel-delivery driver.

Candidate contexts: - parcel delivery - postal delivery - food
delivery - motorcycle quick service - light commercial delivery - medium
freight - heavy freight

The initial product validates parcel delivery first.

Other contexts are architecture expansion targets, not current MVP
scope.

## Mobility Profile

Mobility context should be separated from occupation.

Candidate classes: - walking - bicycle - motorcycle - passenger
car/van - 1-ton commercial vehicle - 2.5-ton vehicle - 5-ton vehicle -
heavy commercial vehicle

Optional physical constraints may include height, width, length, and
weight-related class where relevant.

The same place may produce different field knowledge for different
mobility profiles.

## Carrier or organization context

A self-selected carrier badge means user-selected operating context. It
does not mean verified employment.

If verified affiliation is introduced later, it must use a separate
verification state and visual indicator.

Carrier context may help separate carrier-specific, occupation-specific,
vehicle-specific, and broadly reusable spatial facts.

## Trust weighting

Relevant experience context may support contribution evaluation.

Recommended logic:

> contribution claim + relevant experience context + validation
> history + independent confirmations + recency

Do not create a universal "better driver" score.

Field Trust measures contribution reliability within DFI, not human
worth or job competence.

## Privacy and consent boundary

Users should be clearly told that DFI may use field interaction history
to personalize assistance and reduce false positives.

Public visibility must be separately controlled.

Recommended controls: - public profile preview - broad-region visibility
setting - vehicle-experience visibility setting - carrier-context
visibility setting - contribution-stat visibility where practical -
personal analytics access - data export - account/data deletion path

Core rule:

> Private intelligence must not silently become public profile content.

## AI memory boundary

Prefer structured field signals over unrestricted narrative
surveillance.

Prefer: - stop-pattern category - confirmed false-positive label -
contribution category - correction count - mobility context

Avoid unnecessary retention of: - continuous audio - private free-form
complaints - customer-linked text - indefinite high-resolution movement
history

Retention must be purpose-limited.

## Architecture consequence

Future architecture and data-model work must separate: - private
intelligence data - personal analytics projections - public profile
projection - shared field knowledge - contribution-credit state

Public profile views must not directly expose the private intelligence
store.

The public profile must be generated from an explicit allow-listed
projection.

## Validation questions

Evaluate: - Does personal analytics provide value beyond curiosity? -
Can users understand why DFI asked a false-positive question? - Do users
correct inferred categories? - Does public contribution context improve
trust decisions? - Do carrier or vehicle badges create misleading
assumptions? - Are users comfortable with broad-region visibility? -
Which profile fields are useful to other field workers?

## Decision rule

> Learn deeply enough to assist the user, expose only what is necessary
> to create trust.

DFI should become more useful as it learns without becoming a
worker-surveillance product.
