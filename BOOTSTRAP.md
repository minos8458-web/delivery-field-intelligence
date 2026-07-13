# DFI Bootstrap

Last updated: 2026-07-13

## Project
Delivery Field Intelligence (DFI)

## Current phase
Phase 1 — mobile field validation readiness

## Current milestone
MVP v0.3: Install → Start Session → Capture → Confirm → Reuse → Outcome Feedback → Export Evidence

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

## Product invariant
Unconfirmed structured output must never become trusted field knowledge automatically.

## Measurement invariant
No efficiency claim should be made from intuition alone. Early claims require field logs, timing, repeated observations, or explicit outcome feedback.

## Non-goals for MVP v0.3
- Courier platform replacement
- Coupang or other platform scraping
- Route optimization
- Customer PII ingestion at scale
- Shared multi-driver knowledge graph
- Automatic operational decisions

## Next engineering action
Deploy the PWA to a HTTPS host, install it on the founder's Android home screen, and run a short field usability trial before adding a real AI provider.

## Claude-deferred work
See `docs/CLAUDE_HANDOFF.md`. Real AI provider implementation, evaluation-harness/prompt optimization, and mobile architecture decision remain deferred.
