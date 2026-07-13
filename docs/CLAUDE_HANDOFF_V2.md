Claude Handoff Queue v2
Last updated: 2026-07-14 Source: docs/DFI_PRODUCT_VISION_V2.md
Purpose
This queue reserves DFI engineering tasks for a focused Claude coding window. Treat MVP v0.3 as a field-validation prototype.
Before implementation read BOOTSTRAP.md, docs/DFI_PRODUCT_VISION_V2.md, and the current source tree.
Preserve these invariants:
Unconfirmed AI output never becomes trusted field knowledge automatically.
Efficiency claims require field evidence.
Carrier-specific procedures are not universal shared knowledge.
Customer PII and private complaints are not shared field knowledge.
Prefer the smallest implementation that advances field validation.
C1 - Mobile Architecture ADR
Decide the next mobile architecture after the PWA prototype. Compare PWA continuation, React Native, Flutter, and native Android/Kotlin where relevant.
Evaluate movable floating control over another app, short-tap voice capture, long-press secondary actions, background location, battery consumption, Android lifecycle/process termination, Korean speech capture, camera/video capture, offline-first behavior, draft recovery, later Field Trace processing, Android distribution, permission UX, and solo-founder maintenance cost.
Deliver an ADR with the decision, rejected alternatives, requirement matrix, Android permission constraints, migration cost, smallest migration sequence, and reusable MVP v0.3 components.
Do not begin full migration before the ADR.
C2 - Production AI Provider Abstraction
Create a stable provider boundary for field-note structuring and one production-capable LLM adapter.
Require strict schema validation, timeout handling, bounded retries, deterministic safe fallback, failure telemetry with data minimization, and preservation of the confirmation gate.
The model must distinguish spatial facts, vehicle-condition facts, facility facts, carrier-context statements, personal/customer information, private complaints, and unsupported operational conclusions.
AI may propose a privacy-safe transformed field fact. The driver must confirm it.
Deliver implementation, tests, and a short provider-boundary document. Do not add multiple providers merely for architectural elegance.
C3 - Evaluation Harness and Korean Field-note Dataset
Create a fixed anonymized Korean dataset covering confusing entrances, rear entrances, stairs/cart restrictions, poor lighting, address-pin mismatch, toilets, fuel/charging notes, repair capabilities, tire service and reported cost, vehicle-class access, carrier-specific statements, customer PII, private complaints, and ambiguous notes requiring clarification.
Report field extraction accuracy, unsafe publication rate, PII leakage rate, carrier-context misclassification rate, required user corrections, average correction burden, and fallback rate.
Deliver the dataset, expected outputs, runner, scoring report, and prompt comparison summary. Prompt optimization must be driven by the fixed dataset.
C4 - Android Floating Capture Feasibility Spike
Prove whether this interaction is viable:
short tap floating control -> voice capture normally within 10 seconds -> structured preview -> confirm/edit/cancel
Long press should expose secondary actions such as photo, Field Trace, or question.
Validate overlay permission flow, behavior while another app is foregrounded, movable control, drag-to-close, background/process limits, accidental activation, microphone permission UX, battery impact, OEM restrictions, and safe session termination.
Deliver a narrow spike/prototype and findings. Do not build the full community product.
C5 - Background Location and Experience Mining Boundary
Design the minimum location-event model needed for future Experience Mining.
Core rule:
Repeated behavior creates a question candidate, not an automatic explanation.
Candidate events may include route-deviation clusters, repeated avoidance, repeated alternate entry, pre-entry stop patterns, revisits, hesitation clusters, and vehicle-class-specific divergence.
Deliver sampling strategy, battery trade-offs, local/server processing boundary, retention, privacy minimization, confidence model, question-candidate generation, and deletion/export expectations.
Do not implement automatic shared-knowledge publication.
C6 - Field Trace and 5-second Semantic Compression
Assess an in-app route-capture pipeline:
start capture -> walk or drive short path -> stop -> process -> review -> publish
The user must not configure timelapse speed.
Preserve forks, turns, entrances, stairs/slopes, vehicle restrictions, useful landmarks, and driver-emphasized moments. Compress long straight movement, repeated scenery, stationary sections, severe shake, and redundant movement.
Evaluate CameraX where applicable, audio removal by default, privacy-risk frame screening, face/plate/doorplate/text handling, local vs cloud processing, storage/upload cost, and a five-second summary plus optional longer trace.
Deliver technical design and the smallest proof-of-concept recommendation. Do not build a general AI video editor.
C7 - Privacy Screening Pipeline
Design one safety boundary for text, speech transcripts, images, and Field Trace contributions.
Handle customer names, phone numbers, unit-linked personal statements, private complaints about identifiable people, vehicle plates where inappropriate, doorplates/identifying text, unsupported accusations, and carrier instructions incorrectly represented as universal facts.
Deliver a threat model, moderation stages, block/redact/transform rules, human confirmation points, and audit-minimization policy.
Priority for the next Claude window
C1 - Mobile Architecture ADR
C4 - Android Floating Capture Feasibility Spike
C2 - Production AI Provider Abstraction
C3 - Evaluation Harness
C5 - Background Location / Experience Mining Boundary
C7 - Privacy Screening Pipeline
C6 - Field Trace Feasibility
The next implementation direction depends first on whether DFI's core mobile interaction can work reliably on Android.
Do not ask Claude to do yet
full route optimization
carrier scraping or reverse engineering
automatic operational decisions
nationwide POI ingestion
full multi-driver knowledge graph
contribution-credit redemption economy
partner reward system
payment/subscription system
large-scale backend architecture
polished final UI design
Completion rule
A Claude task is complete only when its decision or implementation is documented, constraints are explicit, and the next smallest engineering action is clear.
Do not silently change DFI product philosophy or expand scope beyond DFI_PRODUCT_VISION_V2.md.