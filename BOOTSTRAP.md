# DFI Bootstrap

Last updated: 2026-07-13

## Project

Delivery Field Intelligence (DFI)

## Current phase

Phase 0/1 — problem framing, schema foundation, and single-loop prototype

## Current milestone

MVP v0.1: Capture → Structure Draft → Driver Confirm → Store → Search

## Current implementation state

- Vite + TypeScript browser prototype
- Text capture
- Browser speech recognition where supported
- Deterministic structuring baseline
- Driver confirmation gate
- LocalStorage persistence
- Search of confirmed memories

## Product invariant

Unconfirmed AI output must never become trusted field knowledge automatically.

## Non-goals for MVP v0.1

- Courier platform replacement
- Coupang or other platform scraping
- Route optimization
- Customer PII ingestion at scale
- Shared multi-driver knowledge graph
- Automatic operational decisions

## Next engineering action

Replace the deterministic structuring baseline behind a provider interface, then compare at least one real LLM structuring provider against the same schema and a fixed field-note evaluation set.

## Claude-deferred work

See `docs/CLAUDE_HANDOFF.md`. Do not expand those tasks during the current ChatGPT-only window unless explicitly reprioritized.
