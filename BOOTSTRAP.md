# DFI Bootstrap

Last updated: 2026-07-14

## Project

Delivery Field Intelligence (DFI)

## Source of truth

-   Branch: `main`
-   Product direction:
    -   `docs/DFI_PRODUCT_VISION_V2.md`
    -   `docs/DFI_PRODUCT_VISION_V2_1_ADDENDUM.md`
-   Field validation protocol:
    `docs/DFI_FIELD_VALIDATION_PROTOCOL_V1_1.md`
-   Claude task queue: `docs/CLAUDE_HANDOFF_V2_1.md`
-   Current implementation remains MVP v0.3 until an architecture
    decision is documented.

## Current phase

Phase 1 --- product direction consolidation and mobile feasibility
preparation

## Current milestone

MVP v0.3 remains the active validation baseline:

> Install → Start Session → Capture → Confirm → Reuse → Outcome Feedback
> → Export Evidence

The product vision has expanded, but the implementation milestone has
not yet been expanded to match the full vision.

## Current implementation state

-   Vite + TypeScript browser prototype
-   PWA manifest and service worker shell caching
-   Mobile one-hand interaction adjustments
-   Text and browser-supported Korean speech capture
-   Deterministic structuring baseline
-   Driver confirmation gate
-   LocalStorage persistence
-   Unfinished capture draft recovery after refresh
-   Validation session start/end logs
-   Capture duration and correction tracking
-   Operational outcome feedback
-   Validation dashboard
-   JSON and CSV evidence export

## Product direction

DFI is a commercial-transport field-intelligence system.

Long-term direction:

> Commercial transport worker experience → low-friction capture → AI
> structuring → human confirmation → cross-context validation →
> privacy-safe shared spatial intelligence

Core intelligence candidates:

-   Experience Mining
-   EER --- Experienced Expert Routing
-   Address Anomaly Detection
-   Cross-context intersection extraction
-   Safety Transformation
-   Field Trace semantic route compression
-   Field Risk Intelligence
-   Personal Behavior Baseline
-   Route Breakdown Event detection candidates
-   Passive Assistance

The product must preserve distinctions between:

-   personal behavior
-   carrier-context-specific patterns
-   vehicle-class-specific patterns
-   broadly reusable physical-space facts

## Product invariants

1.  Unconfirmed AI output must never become trusted field knowledge
    automatically.
2.  DFI does not replace carrier applications or official carrier
    procedures.
3.  Carrier-specific operating know-how must not automatically become
    universal shared knowledge.
4.  Customer names, phone numbers, order patterns, and private
    complaints are not shared field knowledge.
5.  AI may detect, structure, compare, transform, and ask; it must not
    invent operational authority.
6.  Repeated behavior creates a question candidate, not an automatic
    explanation.
7.  No help request does not mean no field problem occurred.
8.  Stationary duration alone does not mean a driver is lost.
9.  Common anomaly signals should be compared with the driver's personal
    behavior baseline.
10. DFI prioritizes factual field evidence over abstract star ratings.
11. DFI-initiated response labor may receive small abuse-resistant
    contribution credit.
12. Credit rewards useful knowledge contribution, not tapping volume,
    accident severity, or damage amount.
13. A relevant field-risk photo may receive only a modest additional
    contribution.
14. DFI must degrade gracefully through Online, Degraded, and Offline
    network states.
15. The driver interaction should be minimized: natural speech or
    one-tap factual confirmation wherever possible.

## Measurement invariant

No efficiency claim should be made from intuition alone.

Early claims require one or more of:

-   field logs
-   timing
-   repeated observations
-   explicit outcome feedback
-   substitute/new-driver comparison
-   route-breakdown evidence
-   severe destination-search events
-   workload redistribution caused by route confusion
-   correction-burden measurement
-   false-positive intervention measurement
-   offline capture and later synchronization evidence

## Initial validation environment

The founder's route is the first field-validation environment.

Relevant conditions:

-   many parcel/lot-address deliveries
-   long and physically demanding route movement
-   new and substitute drivers often struggle
-   founder operates five delivery days per week
-   other drivers cover remaining days
-   founder already holds route-specific tacit knowledge
-   substitute drivers may struggle without contacting the founder
-   route confusion has previously contributed to work redistribution to
    coworkers

Do not document every delivery location.

Capture only locations that are genuinely difficult, repeatedly
confusing, operationally costly, or materially risky.

Primary validation question:

> Can accumulated expert field knowledge reduce the adaptation cost and
> hidden route breakdown of a substitute or new driver?

## Near-term validation metrics

Track where feasible:

-   severe destination-search events
-   wrong-entry events
-   repeated local revisits or backtracking
-   Route Breakdown Events
-   workload redistribution caused by route confusion
-   cases spending more than three minutes locating a destination
-   DFI tip views followed by successful resolution
-   recovery after a DFI intervention
-   unresolved locations after a tip view
-   false-positive intervention rate
-   micro-prompt response rate
-   AI correction burden
-   offline capture and later sync success
-   voluntary repeat app use

Calls or messages to the experienced route owner are secondary evidence
only.

## Current non-goals

Do not implement yet:

-   full route optimization
-   Coupang or other carrier scraping/reverse engineering
-   automatic operational decisions
-   nationwide POI ingestion
-   full multi-driver knowledge graph
-   contribution-credit redemption economy
-   partner reward system
-   payment/subscription system
-   large-scale backend architecture
-   polished final UI design

## Current engineering decision gate

Before expanding implementation beyond MVP v0.3, complete the focused
mobile architecture decision.

Highest-priority deferred work:

1.  Mobile Architecture ADR
2.  Android Floating Capture and Passive Assistance Feasibility Spike
3.  Background Location, Personal Baseline, and Route Breakdown Boundary
4.  Production AI Provider Abstraction
5.  Evaluation Harness and Korean Field-note Dataset
6.  Privacy Screening and Risk Photo Evidence Pipeline
7.  Contribution Credit State Model
8.  Field Trace Feasibility

See `docs/CLAUDE_HANDOFF_V2_1.md`.

## Next engineering action

Do not add broad product features to the PWA.

At the next focused Claude coding window:

> Complete C1 --- Mobile Architecture ADR first.

The ADR must decide whether the next milestone remains PWA-based or
moves to React Native, Flutter, or native Android/Kotlin.

The decision must explicitly evaluate:

-   overlay capture
-   passive assistance
-   background location
-   personal behavior baseline storage
-   false-positive feedback
-   Route Breakdown Event representation
-   Online / Degraded / Offline behavior
-   durable local queues
-   idempotent synchronization
-   battery
-   Korean speech
-   camera/video capture
-   privacy-safe field-risk photo evidence
-   Android lifecycle constraints
-   solo-founder maintenance cost

Only after the ADR should the smallest next implementation be selected.

## Decision rule

> Founder field observation → product hypothesis → critical review →
> smallest implementation → field evidence → keep, revise, or delete

Founder intuition discovers problems.

AI helps challenge and structure hypotheses.

Field evidence decides.
