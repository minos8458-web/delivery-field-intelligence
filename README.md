# Delivery Field Intelligence (DFI)

AI-powered field memory and tacit knowledge system for independent delivery workers.

## Core idea

Existing delivery tools mainly optimize today's route, stop order, and dispatch workflow. DFI focuses on operational knowledge that accumulates only in the driver's head.

> Driver experience → structured field memory → repeated pattern detection → decision support

## MVP v0.1

1. Enter a field note by text or browser-supported speech recognition.
2. Convert it into a structured field-memory draft.
3. Let the driver review and correct the result.
4. Save only user-confirmed memories locally.
5. Search and reuse confirmed field memories.

## Important MVP limitation

The current parser is a deterministic local baseline, not a production LLM. The product boundary and confirmation workflow are implemented first so that a real AI structuring provider can later be compared against a stable data contract.

## Run

```bash
npm install
npm run dev
```

## Build

```bash
npm run build
```

## Current status

- Phase: 0/1 foundation prototype
- Target: MVP v0.1
- Primary validator: active independent delivery worker
- Source of truth branch: `main`

See `BOOTSTRAP.md` and `docs/`.
