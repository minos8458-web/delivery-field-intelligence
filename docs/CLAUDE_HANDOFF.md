# Claude Handoff Queue

These tasks are intentionally deferred for a Claude coding window because they are likely to benefit from long-context code generation, comparative implementation, or broad refactoring.

## C1 — Real AI provider implementation

Design and implement a provider abstraction for field-note structuring and add one production-capable LLM adapter with strict JSON schema validation, retry behavior, timeout handling, and safe fallback.

Why deferred: best handled as a focused multi-file implementation with provider-specific API details and error-path review.

## C2 — Evaluation harness and prompt optimization

Create a fixed anonymized Korean field-note dataset, expected structured outputs, scoring rules, and an evaluation runner. Compare prompt variants and report field-level accuracy and correction burden.

Why deferred: requires broad comparative iteration across prompts, schema boundaries, and test cases.

## C3 — Architecture review before mobile expansion

Review whether the validated browser prototype should move to React Native, Flutter, or remain a PWA for the next milestone. Produce an ADR based on offline capability, Korean speech capture, background behavior, Android distribution, and operating cost.

Why deferred: requires a deliberate comparative architecture pass; implementation should not precede the decision.

## Do not ask Claude to do yet

- Full route optimization
- Coupang app scraping or reverse engineering
- Multi-driver shared knowledge graph
- Large-scale backend architecture
- Subscription/payment system
