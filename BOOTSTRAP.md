# DFI Bootstrap

Last updated: 2026-07-14

## Project

Delivery Field Intelligence (DFI)

## Source of truth

- Branch: `main`
- Product direction: `docs/DFI_PRODUCT_VISION_V2.md`
- Claude task queue: `docs/CLAUDE_HANDOFF_V2.md`
- Current implementation remains MVP v0.3 until an architecture decision is documented.

## Current phase

Phase 1 — product direction consolidation and mobile feasibility preparation

## Current milestone

MVP v0.3 remains the active validation baseline:

> Install → Start Session → Capture → Confirm → Reuse → Outcome Feedback → Export Evidence

The product vision has expanded, but the implementation milestone has not yet been expanded to match the full vision.

## Current implementation state

- Vite + TypeScript browser prototype
- PWA manifest and service worker shell caching
- Mobile one-hand interaction adjustments
- Text and browser-supported Korean speech capture
- Deterministic structuring baseline
- Driver confirmation gate
- LocalStorage persistence
- Unfinished capture draft recovery after refresh
- Validation session start/end logs
- Capture duration and correction tracking
- Operational outcome feedback
- Validation dashboard
- JSON and CSV evidence export

## Product direction v2

DFI is no longer defined as only a personal field-memory tool.

The long-term direction is:

> Commercial transport worker experience → low-friction capture → AI structuring → human confirmation → cross-context validation → privacy-safe shared spatial intelligence

Core intelligence candidates:

- Experience Mining
- EER — Experienced Expert Routing
- Address Anomaly Detection
- Cross-context intersection extraction
- Safety Transformation
- Field Trace semantic route compression

The product must preserve a distinction between:

- personal behavior
- carrier-context-specific patterns
- vehicle-class-specific patterns
- broadly reusable physical-space facts

## Product invariants

1. Unconfirmed AI output must never become trusted field knowledge automatically.
2. DFI does not replace carrier applications or official carrier procedures.
3. Carrier-specific operating know-how must not automatically become universal shared knowledge.
4. Customer names, phone numbers, order patterns, and private complaints are not shared field knowledge.
5. AI may detect, structure, compare, transform, and ask; it must not invent operational authority.
6. Repeated behavior creates a question candidate, not an automatic explanation.
7. DFI prioritizes factual field evidence over abstract star ratings.
8. The driver interaction should be minimized: natural speech or one-tap factual confirmation wherever possible.

## Measurement invariant

No efficiency claim should be made from intuition alone.

Early claims require one or more of:

- field logs
- timing
- repeated observations
- explicit outcome feedback
- substitute/new-driver comparison
- reduction in route-owner help calls
- correction-burden measurement

## Initial validation environment

The founder's route is the first field-validation environment.

Relevant conditions:

- many parcel/lot-address deliveries
- long and physically demanding route movement
- new and substitute drivers often struggle
- founder operates five delivery days per week
- other drivers cover remaining days
- founder already holds route-specific tacit knowledge

Do not document every delivery location.

Capture only locations that are genuinely difficult, repeatedly confusing, or operationally costly.

Primary validation question:

> Can accumulated expert field knowledge reduce the adaptation cost of a substitute or new driver?

## Near-term validation metrics

Track where feasible:

- address-search failures
- customer-contact attempts caused by location confusion
- wrong-entry events
- revisits
- cases spending more than three minutes locating a destination
- DFI tip views followed by successful resolution
- calls/messages to the experienced route owner for help
- capture abandonment
- AI correction burden
- voluntary repeat app use

## Current non-goals

Do not implement yet:

- full route optimization
- Coupang or other carrier scraping/reverse engineering
- automatic operational decisions
- nationwide POI ingestion
- full multi-driver knowledge graph
- contribution-credit redemption economy
- partner reward system
- payment/subscription system
- large-scale backend architecture
- polished final UI design

## Current engineering decision gate

Before expanding implementation beyond MVP v0.3, complete the focused mobile architecture decision.

Highest-priority deferred work:

1. Mobile Architecture ADR
2. Android Floating Capture Feasibility Spike
3. Production AI Provider Abstraction
4. Evaluation Harness and Korean Field-note Dataset
5. Background Location / Experience Mining Boundary
6. Privacy Screening Pipeline
7. Field Trace Feasibility

See `docs/CLAUDE_HANDOFF_V2.md`.

## Next engineering action

Do not add broad product features to the PWA.

At the next focused Claude coding window:

> Complete C1 — Mobile Architecture ADR first.

The ADR must decide whether the next milestone remains PWA-based or moves to React Native, Flutter, or native Android/Kotlin based on DFI-specific requirements including overlay capture, background location, battery, speech, camera/video capture, offline behavior, Android lifecycle constraints, and solo-founder maintenance cost.

Only after the ADR should the smallest next implementation be selected.

## Decision rule

> Founder field observation → product hypothesis → critical review → smallest implementation → field evidence → keep, revise, or delete

Founder intuition discovers problems.

AI helps challenge and structure hypotheses.

Field evidence decides.
