# DFI Bootstrap

Last updated: 2026-07-13

## Project

Delivery Field Intelligence (DFI)

## Current phase

Phase 1 — field validation instrumentation

## Current milestone

MVP v0.2: Capture → Structure Draft → Driver Confirm → Store → Reuse → Outcome Feedback → Export Evidence

## Current implementation state

- Vite + TypeScript browser prototype
- Text capture
- Browser speech recognition where supported
- Deterministic structuring baseline
- Driver confirmation gate
- LocalStorage persistence
- Search of confirmed memories
- Capture-to-confirm duration measurement
- Draft correction tracking
- Operational outcome feedback
- Validation dashboard
- JSON and CSV evidence export

## Product invariant

Unconfirmed structured output must never become trusted field knowledge automatically.

## Measurement invariant

No efficiency claim should be made from intuition alone. Early claims require field logs, timing, repeated observations, or explicit outcome feedback.

## Non-goals for MVP v0.2

- Courier platform replacement
- Coupang or other platform scraping
- Route optimization
- Customer PII ingestion at scale
- Shared multi-driver knowledge graph
- Automatic operational decisions

## Next engineering action

Run the MVP with real anonymized founder field notes and inspect whether capture friction, correction burden, and useful recall can be measured reliably before adding a real AI provider.

## Claude-deferred work

See `docs/CLAUDE_HANDOFF.md`. Real AI provider implementation, evaluation-harness/prompt optimization, and mobile architecture decision remain deferred.
