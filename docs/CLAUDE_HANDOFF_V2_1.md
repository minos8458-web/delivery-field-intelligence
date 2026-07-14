# Claude Handoff Queue v2.1

Last updated: 2026-07-14 Product direction sources: -
`docs/DFI_PRODUCT_VISION_V2.md` -
`docs/DFI_PRODUCT_VISION_V2_1_ADDENDUM.md` Validation source: -
`docs/DFI_FIELD_VALIDATION_PROTOCOL_V1_1.md`

## Purpose

This queue reserves DFI engineering tasks for a focused Claude coding
window.

Treat MVP v0.3 as a field-validation prototype. Do not expand DFI into a
full logistics platform in one pass.

Before implementation, read:

1.  `BOOTSTRAP.md`
2.  `docs/DFI_PRODUCT_VISION_V2.md`
3.  `docs/DFI_PRODUCT_VISION_V2_1_ADDENDUM.md`
4.  `docs/DFI_FIELD_VALIDATION_PROTOCOL_V1_1.md`
5.  the current source tree and existing validation/evidence flow

Preserve these invariants:

-   Unconfirmed AI output never becomes trusted field knowledge
    automatically.
-   Efficiency claims require field evidence.
-   Carrier-specific procedures are not universal shared knowledge.
-   Customer PII and private complaints are not shared field knowledge.
-   Repeated behavior creates a question candidate, not an automatic
    explanation.
-   No help request does not mean no field problem occurred.
-   Stationary duration alone does not mean the driver is lost.
-   DFI must degrade gracefully through Online, Degraded, and Offline
    network states.
-   Credit rewards useful contribution, not tapping volume, accident
    severity, or damage amount.
-   Prefer the smallest implementation that advances field validation.

## C1 --- Mobile Architecture ADR

### Goal

Decide the next mobile architecture after the PWA prototype.

Compare at minimum:

-   PWA continuation
-   React Native
-   Flutter
-   native Android / Kotlin where relevant

### Evaluate against DFI-specific requirements

-   movable floating control over another app
-   quiet passive-assistance indicator
-   short-tap voice capture
-   long-press secondary actions
-   low-interruption factual micro-prompts
-   background location sessions
-   personal behavior baseline storage
-   false-positive feedback labels
-   Route Breakdown Event representation
-   battery consumption
-   Android lifecycle and process termination
-   Korean speech capture
-   camera/video capture
-   privacy-safe photo evidence handling
-   offline-first local capture
-   cached field knowledge
-   durable local draft and media queue
-   Degraded-network retry behavior
-   idempotent synchronization
-   provisional versus confirmed credit state
-   later Field Trace processing
-   Android installation/distribution path
-   privacy and permission UX
-   solo-founder operating and maintenance cost

### Required offline analysis

The ADR must explicitly describe behavior for:

-   Online
-   Degraded
-   Offline

At minimum, evaluate whether these can continue without network access:

-   cached-tip viewing
-   basic field-note capture
-   micro-prompt response
-   photo/video capture
-   draft retention
-   sync queue creation

The design must address duplicate submission and replay after reconnect.

### Deliverable

Create an ADR containing:

-   decision
-   rejected alternatives
-   requirement matrix
-   Android permission constraints
-   offline/degraded behavior matrix
-   local persistence and sync-queue recommendation
-   migration cost from MVP v0.3
-   smallest migration sequence
-   explicit list of reusable MVP v0.3 components

Do not begin full migration before the ADR decision is documented.

## C2 --- Production AI Provider Abstraction

Create a stable provider boundary for field-note structuring and one
production-capable LLM adapter.

Require:

-   strict structured-output schema validation
-   timeout handling
-   bounded retry behavior
-   deterministic safe fallback
-   failure telemetry with data minimization
-   confirmation gate preservation

The model must distinguish:

-   spatial fact
-   vehicle-condition fact
-   facility fact
-   field-risk fact
-   carrier-context statement
-   personal/customer information
-   private complaint
-   unsupported operational conclusion

AI may propose a privacy-safe transformed field fact. The driver must
confirm it.

Deliver implementation, tests, and a short provider-boundary document.

Do not add multiple providers merely for architectural elegance.

## C3 --- Evaluation Harness and Korean Field-note Dataset

Create a fixed anonymized Korean dataset covering:

-   confusing entrance
-   rear entrance
-   stairs/cart restriction
-   poor lighting
-   address/pin mismatch
-   toilet information
-   fuel/charging note
-   repair capability
-   tire service and reported cost
-   vehicle-class access
-   actual contact report
-   near miss
-   reverse-exit risk
-   clearance risk
-   unusual terrain/structure risk
-   carrier-specific statement that must not become universal
-   customer PII
-   private complaint
-   ambiguous note requiring clarification

Report:

-   field extraction accuracy
-   unsafe publication rate
-   PII leakage rate
-   carrier-context misclassification rate
-   field-risk classification accuracy
-   required user corrections
-   average correction burden
-   fallback rate

Deliver dataset, expected outputs, evaluation runner, scoring report,
and prompt comparison summary.

Prompt optimization must be driven by the fixed dataset.

## C4 --- Android Floating Capture and Passive Assistance Feasibility Spike

### Goal

Prove whether DFI's core low-friction interaction is technically and
operationally viable.

Primary capture:

> short tap floating control -\> voice capture normally within 10
> seconds -\> structured preview -\> confirm/edit/cancel

Long press:

> photo / Field Trace / question or other secondary action

Passive assistance:

> relevant knowledge exists -\> quiet indicator -\> stronger
> intervention only after sufficient anomaly evidence -\> one-tap
> factual response

### Example false-positive feedback labels

-   배송지 찾는 중
-   배송지는 찾았으며 배송할 상품 찾는 중
-   주차·진입 문제
-   고객 연락 중
-   잠시 쉬는 중
-   문제 없음

These labels must be treated as training/behavior-baseline feedback, not
simple dismiss buttons.

### Validate

-   overlay permission flow
-   behavior while another app is foregrounded
-   movable floating control
-   drag-to-close
-   quiet availability signal
-   micro-prompt presentation
-   accidental activation
-   microphone permission UX
-   background/process limits
-   battery impact
-   OEM restrictions
-   offline response persistence
-   safe session termination

### Deliverable

A narrow spike/prototype plus findings.

Do not build the full community product.

## C5 --- Background Location, Personal Behavior Baseline, and Route Breakdown Boundary

### Goal

Design the minimum event model needed for future Experience Mining
without pretending GPS reveals intent.

Core rules:

> Repeated behavior creates a question candidate, not an automatic
> explanation.

> Common anomaly signals must be compared with the driver's personal
> behavior baseline.

> No help request does not mean no field problem occurred.

### Candidate anomaly signals

-   route-deviation cluster
-   repeated avoidance
-   repeated alternate entry
-   pre-entry stop pattern
-   revisit
-   repeated pass-by
-   repeated entry/exit
-   direction-reversal cluster
-   unusual backtracking
-   hesitation cluster
-   vehicle-class-specific divergence
-   deviation from the driver's own normal stop pattern

### Route Breakdown Event

Design a representation for a meaningful failure of normal route flow,
including candidate evidence such as:

-   work redistribution caused by route confusion
-   repeated unresolved local searching
-   major backtracking/revisits
-   abandonment or deferral of a confusing section
-   unusual coworker rescue

A single GPS pattern must not prove route breakdown.

### Deliverable

Design note and event schemas covering:

-   sampling strategy
-   battery trade-offs
-   personal baseline model
-   false-positive feedback labels
-   question-candidate generation
-   Route Breakdown Event evidence
-   local versus server processing
-   retention
-   privacy minimization
-   confidence
-   deletion/export expectations

Do not implement automatic shared-knowledge publication.

## C6 --- Field Trace and 5-second Semantic Compression

Assess an in-app route-capture pipeline:

> start capture -\> walk or drive short path -\> stop -\> process -\>
> review -\> publish

The user must not configure timelapse speed.

Preserve:

-   forks
-   turns
-   entrances
-   stairs/slopes
-   vehicle restrictions
-   useful landmarks
-   driver-emphasized moments

Compress or remove:

-   long straight movement
-   repeated scenery
-   stationary sections
-   severe shake
-   redundant movement

Evaluate:

-   CameraX where applicable
-   audio removal by default
-   privacy-risk frame screening
-   face/plate/doorplate/text handling
-   local versus cloud processing
-   offline capture queue
-   storage/upload cost
-   five-second summary plus optional longer trace

Deliver technical design and the smallest proof-of-concept
recommendation.

Do not build a general AI video editor.

## C7 --- Privacy Screening and Risk Photo Evidence Pipeline

Design one safety boundary for text, speech transcripts, images, and
Field Trace contributions.

Handle:

-   customer names
-   phone numbers
-   unit-linked personal statements
-   private complaints about identifiable people
-   vehicle plates where inappropriate
-   doorplates and identifying text
-   unsupported accusations
-   carrier instructions incorrectly represented as universal facts

For field-risk photos, also evaluate whether the image materially
supports the reported spatial condition.

Useful risk-photo targets may include:

-   protruding walls
-   low roofs/overhangs
-   steep slopes
-   narrow turning areas
-   blind structures
-   sharp bends
-   drainage ditches/drop-offs
-   low-clearance structures
-   unusual obstacles or terrain

Deliver:

-   threat model
-   moderation stages
-   block/redact/transform rules
-   human confirmation points
-   risk-photo relevance strategy
-   audit-minimization policy

## C8 --- Contribution Credit State Model

### Goal

Design the smallest abuse-resistant credit state model. Do not build a
redemption economy.

Separate:

-   provisional contribution
-   validated contribution
-   confirmed credit

Candidate contribution roles:

-   valid micro-response
-   later false-positive reduction
-   initial risk discovery
-   concrete incident/near-miss report
-   relevant photo evidence
-   factual confirmation
-   vehicle-class-specific validation
-   later validation of an emerging risk pattern

Rules:

-   photo attachment alone does not create meaningful extra credit
-   relevant risk photo may receive only a modest increment
-   accident severity and damage amount do not increase reward
-   first reporter does not monopolize later value
-   repetitive low-information tapping must not farm credit
-   offline provisional state must synchronize idempotently

Deliver a state model, event schema, abuse cases, and smallest future
implementation boundary.

Do not implement partner rewards, cash conversion, or payments.

## Priority for the next Claude window

1.  C1 --- Mobile Architecture ADR
2.  C4 --- Android Floating Capture and Passive Assistance Feasibility
    Spike
3.  C5 --- Background Location, Personal Baseline, and Route Breakdown
    Boundary
4.  C2 --- Production AI Provider Abstraction
5.  C3 --- Evaluation Harness
6.  C7 --- Privacy Screening and Risk Photo Evidence Pipeline
7.  C8 --- Contribution Credit State Model
8.  C6 --- Field Trace Feasibility

Reason:

DFI's next implementation direction first depends on Android interaction
viability, offline durability, and whether passive assistance can be
implemented without creating unacceptable false positives or battery
cost.

## Do not ask Claude to do yet

-   full route optimization
-   carrier scraping or reverse engineering
-   automatic operational decisions
-   nationwide POI ingestion
-   full multi-driver knowledge graph
-   contribution-credit redemption economy
-   partner reward system
-   payment/subscription system
-   large-scale backend architecture
-   polished final UI design

## Completion rule

A Claude task is complete only when its decision or implementation is
documented, constraints are explicit, and the next smallest engineering
action is clear.

Do not silently change DFI product philosophy or expand scope beyond the
product-direction documents.
