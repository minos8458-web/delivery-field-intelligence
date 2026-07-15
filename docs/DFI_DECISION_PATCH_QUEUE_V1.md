# DFI Decision Patch Queue V1

Last updated: 2026-07-16
Status: Active queue — no items incorporated yet
Parent documents: `DFI_PRODUCT_VISION_V2.md`, `DFI_HAZARD_MEMORY_MODEL_V1.md`,
  `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md`, `DFI_VALIDATION_EXPORT_CONTRACT_V1.md`,
  `C4_ANDROID_FLOATING_CAPTURE_SPIKE.md`

---

## Purpose

This document is a **patch queue**. It preserves founder-approved or strongly supported design directions that emerged after the current formal documents were written, while clearly separating:

1. Documentation patches waiting to be incorporated into established design documents
2. Implementation/UI patches waiting for a future code batch
3. Long-term polish items
4. Hypotheses that still require field validation before any incorporation

**This document is not a new product vision.**
**It is not a replacement architecture document.**
**It must not promote every queued idea into a confirmed production requirement.**

An item in this queue is a direction held in reserve, not an approved specification.

---

## Queue Classification

Every item has one classification:

| Classification | Meaning |
|----------------|---------|
| `DOC_PATCH` | Ready or nearly ready for incorporation into a target design document |
| `CODE_PATCH` | A confirmed direction requiring Android or PWA code changes in a future batch |
| `POLISH_BACKLOG` | Long-term polish; must not interrupt current feasibility validation |
| `VALIDATION_HYPOTHESIS` | Requires field data before any incorporation |

Every item has one status:

| Status | Meaning |
|--------|---------|
| `QUEUED` | Recorded, waiting for incorporation batch |
| `BLOCKED` | Depends on an unresolved prior item or external decision |
| `REQUIRES_VALIDATION` | Field evidence must exist before incorporating |
| `READY_FOR_DOC_PATCH` | Approved direction; target document identified; no code needed |
| `READY_FOR_CODE_BATCH` | Direction confirmed; implementation can proceed in next code batch |
| `REJECTED` | Reviewed and decided against; preserved for traceability |

---

## Queue Execution Rules

1. `DOC_PATCH` items may be incorporated into their target design documents in a future documentation batch.
2. `CODE_PATCH` items should be grouped when practical to reduce repeated APK build/install/revalidation cycles.
3. `POLISH_BACKLOG` items must not interrupt current feasibility validation.
4. `VALIDATION_HYPOTHESIS` items must not be promoted to confirmed product behavior without field evidence.
5. A queue item must not be marked incorporated merely because it was discussed in conversation.
6. When incorporated: update the target document, append a `WORK_LOG` entry, update the item status here, preserve the original Queue ID for traceability.
7. If a queue item is rejected: do not delete it silently. Change status to `REJECTED` and record the reason.

---

## Section A — Hazard Memory Document Patch Queue

---

### HM-PATCH-001 — Wheel-Contact Corridor

**Queue ID:** HM-PATCH-001
**Classification:** DOC_PATCH
**Status:** QUEUED
**Target document:** `docs/DFI_HAZARD_MEMORY_MODEL_V1.md` (new subsection under §7 Hazard Characteristics or new §16)

**Decision summary:**

Add the working concept **Wheel-Contact Corridor** to the Hazard Memory Model. A vehicle's wheel travel is not one exact line. Vehicle width, track width, tyre width, vehicle class, and lane position vary. The useful spatial concept is a corridor or repeated contact band representing road surface areas repeatedly traversed by vehicle tyres under normal lane travel.

Design direction:

> Potholes intersecting the outer/common repeated tyre-contact areas of a lane deserve stronger active-warning priority than hazards outside the likely contact zone.

Possible conceptual relevance states (direction, not production-final):

```
CONTACT_CORRIDOR_INTERSECTING
CONTACT_CORRIDOR_APPROACHING
NON_CONTACT_ROAD_HAZARD
```

**Rationale:**

- Repeated tyre contact creates real encounter probability
- Potholes may expand through traffic and pavement deterioration, increasing contact-zone relevance over time
- Warning every reported pothole equally creates alert overload
- The concept is a relevance model, not centimetre-accurate tyre localization

**Validation boundary:**

Do not claim exact wheel-path geometry can be inferred from ordinary smartphone GPS alone. The corridor is an approximate relevance band, not a precisely measured physical path.

**Dependency:** None. Extends existing §7 hazard-characteristic model.

**Recommended incorporation point:** Next documentation batch after HM-PATCH-002 (Pothole Growth Signal) because the two interact — a growing pothole may transition from `NON_CONTACT` to `CONTACT_CORRIDOR_INTERSECTING`.

---

### HM-PATCH-002 — Pothole Growth Signal

**Queue ID:** HM-PATCH-002
**Classification:** DOC_PATCH
**Status:** QUEUED
**Target document:** `docs/DFI_HAZARD_MEMORY_MODEL_V1.md` (extend §7 Hazard Characteristics)

**Decision summary:**

Add the working concept **Pothole Growth Signal** to the Hazard Memory Model. A pothole is not a static hazard. Evidence over time may indicate one of:

```
STABLE
INCREASING
DECREASING
UNKNOWN
```

Evidence types that may support the signal:

- Explicit user statement that the pothole became larger
- Repeated size-category reports shifting over time (SMALL → MEDIUM → LARGE)
- Changing contact-corridor relevance (see HM-PATCH-001)
- Changing avoidance behavior patterns
- Later confirmation or contradiction of prior severity estimate

The Pothole Growth Signal should influence hazard priority only when supported by adequate evidence. It must not be inferred from a single report.

**Rationale:**

- A size sequence (SMALL → MEDIUM → LARGE across multiple reports) is more meaningful than an artificially precise centimetre estimate
- Growing potholes expand both the contact-corridor relevance and the risk severity
- Hazard evidence states (REPORTED → SUPPORTED → CONFIRMED) do not capture within-hazard change; this is a separate dimension

**Validation boundary:**

Do not infer false measurement precision. Do not present the growth signal as confirmed without a minimum number of independent reports showing consistent direction. Exact evidence thresholds are not finalized.

**Dependency:** Interacts with HM-PATCH-001 (Wheel-Contact Corridor) — growth may change contact-corridor classification. Incorporate together.

**Recommended incorporation point:** Next documentation batch. Incorporate with HM-PATCH-001.

---

### HM-PATCH-003 — Avoidance Behavior Relevance

**Queue ID:** HM-PATCH-003
**Classification:** VALIDATION_HYPOTHESIS
**Status:** REQUIRES_VALIDATION
**Target document:** `docs/DFI_HAZARD_MEMORY_MODEL_V1.md` (after field validation, add to §7 or new subsection)

**Decision summary:**

A pothole slightly outside the normal contact corridor may still create meaningful risk if drivers repeatedly alter trajectory to avoid it. Working concept: **Avoidance Behavior Relevance**.

Possible evidence signals (direction only):

- Repeated lateral trajectory deviation near the same hazard candidate
- Path splitting around a candidate hazard location
- Repeated slowing plus lateral deviation within the hazard candidate window
- Explicit user report of active avoidance

The design direction is that active warning priority may eventually consider both:

```
DIRECT_CONTACT_RELEVANCE
AVOIDANCE_BEHAVIOR_RELEVANCE
```

**Rationale:**

Even a pothole that most vehicles miss directly may cause secondary risk if it forces repeated trajectory correction, creating unpredictable lane behavior near the hazard.

**Validation boundary:**

Do not treat one trajectory deviation as proof of avoidance. Do not claim causation from trajectory correlation alone. This concept requires real field trajectory data before any implementation. It is classified as a hypothesis until avoidance-pattern evidence can be distinguished from ordinary lane variation.

**Dependency:** HM-PATCH-001 (Wheel-Contact Corridor) — avoidance relevance is only meaningful relative to the contact corridor concept.

**Recommended incorporation point:** After Phase A field data provides trajectory evidence near reported hazards. Do not incorporate before.

---

### HM-PATCH-004 — Traffic Signal Operating State

**Queue ID:** HM-PATCH-004
**Classification:** DOC_PATCH
**Status:** QUEUED
**Target document:** `docs/DFI_HAZARD_MEMORY_MODEL_V1.md` (new subsection, extending §3 Hazard Data Shapes or §14 Speed Hump section as a parallel infrastructure type)

**Decision summary:**

Add a field-infrastructure knowledge direction for intersections where traffic signals may operate differently from ordinary full-cycle signals. Working concept: **Traffic Signal Operating State**.

Possible state taxonomy (direction, not production-final):

```
RECURRING_RED_FLASH
RECURRING_YELLOW_FLASH
TEMPORARY_SIGNAL_FAILURE
SIGNAL_DARK
ABNORMAL_SIGNAL_BEHAVIOR
```

The model must distinguish:

- **Persistent or recurring operating pattern** — a signal repeatedly confirmed to operate in a non-standard mode (e.g., known late-night flash operation)
- **Temporary failure or abnormal state** — a signal that has recently failed or is currently behaving abnormally

Evidence-strength language direction examples (not immutable production copy):

> "전방은 적색 점멸 신호로 반복 확인된 교차로입니다."

> "전방 신호등 비작동 신고가 최근 있었습니다."

**Rationale:**

Traffic signal state is time-sensitive field-infrastructure knowledge that materially affects driving safety decisions, particularly at night or during inclement weather. The existing Hazard Memory Model covers road-surface hazards and speed humps; traffic signals are a third infrastructure category requiring separate treatment.

**Validation boundary:**

Do not create a single alert message that applies regardless of recency. See HM-PATCH-005 for the temporal model requirement.

**Dependency:** HM-PATCH-005 (Recency × Recurrence for Stateful Infrastructure). Incorporate together.

**Recommended incorporation point:** Next documentation batch. Incorporate with HM-PATCH-005.

---

### HM-PATCH-005 — Recency × Recurrence for Stateful Infrastructure

**Queue ID:** HM-PATCH-005
**Classification:** DOC_PATCH
**Status:** QUEUED
**Target document:** `docs/DFI_HAZARD_MEMORY_MODEL_V1.md` (new subsection on temporal modeling for stateful infrastructure; also extends §6 Evidence States and §14)

**Decision summary:**

Traffic signal state must not be determined by lifetime report count alone. The evidence model for stateful infrastructure must separately consider:

```
RECENCY     — how recent is the most relevant evidence?
RECURRENCE  — how repeatedly has this state been confirmed?
```

Counter-example that the model must avoid:

> 100 signal-failure reports from yesterday  
> + 12 recent normal-operation confirmations today  
> = must NOT remain a permanent "signal failure" warning

The temporal model must allow recent contradicting evidence to appropriately reduce confidence in a prior state.

Design principle extension:

Persistent infrastructure (speed humps — already in §14 of the Hazard Memory Model) and **stateful infrastructure** (traffic signals, temporary road works) require different temporal models. A speed hump does not spontaneously appear and disappear; a traffic signal can fail and be repaired within hours.

Pothole growth/change evidence (HM-PATCH-002) and traffic-signal state transitions must not automatically share one decay model.

**Rationale:**

An alert for a signal failure that was repaired hours ago is worse than no alert — it erodes trust in the whole hazard system. Evidence freshness must be a first-class model dimension, not an afterthought.

**Validation boundary:**

Exact decay rates and recency windows require field measurement. Do not hard-code specific hour or day thresholds as final.

**Dependency:** HM-PATCH-004 (Traffic Signal Operating State). Incorporate together.

**Recommended incorporation point:** Next documentation batch with HM-PATCH-004.

---

## Section B — Delivery Knowledge Document Patch Queue

---

### DKM-PATCH-001 — Delivery Terrain

**Queue ID:** DKM-PATCH-001
**Classification:** DOC_PATCH
**Status:** QUEUED
**Target document:** `docs/DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` (extend §6 DKM and add new subsection)

**Note:** This concept appears in `docs/DFI_VALIDATION_EXPORT_CONTRACT_V1.md` §J as `[DIRECTION]` and `[HYPOTHESIS]`, and in `docs/DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` §I-ext as a direction extension. This patch queue item formalizes it as a pending DOC_PATCH specifically for the DKM document.

**Decision summary:**

Add the working concept **Delivery Terrain** to the Delivery Knowledge Model. DFI should interpret a delivery area as a repeated work-behavior space, not merely a collection of address pins.

A normal map emphasizes: roads, buildings, addresses, POIs.

Delivery Terrain emphasizes:

- vehicle entry and exit patterns
- vehicle stopping locations
- walking bundles (where the driver parks and walks to multiple addresses)
- building access paths
- vehicle return points
- route transitions between sub-areas

Core principle:

> As evidence density increases, the user interface must not automatically become more visually complex. More raw events should be compressed into fewer meaningful delivery patterns where evidence supports that compression.

Possible behavioral node types (direction, not production-final schema):

```
ENTRY_NODE
STOP_CLUSTER
WALK_CLUSTER
BUILDING_ACCESS
RETURN_POINT
EXIT_NODE
ROUTE_TRANSITION
```

**Rationale:**

A dense delivery-pin map becomes cognitively unmanageable. Delivery Terrain compresses accumulated evidence into a structured representation that reflects how the delivery area is actually worked, not just where addresses are located.

**Validation boundary:**

Do not declare these node types production-final before field validation. It is not yet known which node granularity is most useful, how stable nodes are across day-to-day delivery variation, or whether the taxonomy fits all vehicle/delivery-type combinations.

**Dependency:** Requires Personal Delivery Baseline (already in DKM §2) to provide the repeated-session evidence.

**Recommended incorporation point:** Next documentation batch, as a formalized addition to the DKM document. The export contract already preserves the raw evidence needed to validate it.

---

### DKM-PATCH-002 — Semantic Zoom

**Queue ID:** DKM-PATCH-002
**Classification:** VALIDATION_HYPOTHESIS
**Status:** REQUIRES_VALIDATION
**Target document:** `docs/DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` (UI/product direction section)

**Decision summary:**

Add the working concept **Semantic Zoom** as a product and UI direction. Map zoom level should affect the **type** of delivery information shown, not merely the size of markers.

Possible zoom-level direction:

```
Regional view      → delivery-knowledge areas
Zone view          → Delivery Zones
Site/complex view  → entry, stopping, exit flow
Near view          → building access and walking bundles
Field-interaction  → only currently relevant information
```

**Rationale:**

Conventional map zoom merely scales visual elements. Semantic Zoom changes what information category is relevant at each scale. A driver approaching a complex does not need regional-level knowledge; a driver planning a delivery zone sequence does not need building-level access detail.

**Validation boundary:**

This concept is a UI/product design direction. It requires:

1. Delivery Terrain validation (DKM-PATCH-001) first — without meaningful terrain nodes, semantic zoom has nothing to show at each level
2. Field observation of whether zoom-level-appropriate information reduces cognitive load
3. Confirmation that the zoom level taxonomy matches real driver information-seeking behavior

Do not incorporate as a confirmed design before Delivery Terrain field validation.

**Dependency:** DKM-PATCH-001 (Delivery Terrain) must be at least partially validated before Semantic Zoom can be evaluated.

**Recommended incorporation point:** After DKM-PATCH-001 field validation. Not before.

---

### DKM-PATCH-003 — Delivery Behavior Nodes (Provenance Preservation)

**Queue ID:** DKM-PATCH-003
**Classification:** DOC_PATCH
**Status:** QUEUED
**Target document:** `docs/DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` (reinforce §7 Provenance and §6 DKM)

**Decision summary:**

Add an explicit statement to the DKM document that behavioral node derivation (from DKM-PATCH-001) must preserve the existing provenance distinction:

```
FIELD_FACT
REPEATED_OBSERVATION
PERSONAL_PATTERN
```

A behavioral node must carry the provenance type of the evidence that created it. A `STOP_CLUSTER` derived entirely from one driver's personal pattern must be labeled `PERSONAL_PATTERN`, not `FIELD_FACT`.

The existing language direction from §7 must extend explicitly to node-level knowledge:

Avoid:

> "911동 다음 912동으로 가세요."

Prefer:

> "기존 담당자는 보통 911동 다음 912동을 배송했습니다."

The system must not assume every repeated behavior is optimal or mandatory.

**Rationale:**

Behavioral node derivation (DKM-PATCH-001) could silently convert `PERSONAL_PATTERN` evidence into apparently authoritative `FIELD_FACT` nodes if provenance is not explicitly preserved at the node level. This patch explicitly prevents that drift.

**Validation boundary:**

No field validation required for the principle itself. The principle is already approved (see DKM §7). This patch extends its explicit application to the behavioral node domain.

**Dependency:** DKM-PATCH-001.

**Recommended incorporation point:** At the same time as DKM-PATCH-001, as a sub-section of that incorporation.

---

### DKM-PATCH-004 — Carrier Context Overlay

**Queue ID:** DKM-PATCH-004
**Classification:** DOC_PATCH
**Status:** QUEUED
**Target document:** `docs/DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` (new subsection, extending §13 Shared Knowledge Boundary)

**Note:** This concept also appears in `docs/DFI_VALIDATION_EXPORT_CONTRACT_V1.md` §K as `[DIRECTION]`. This patch formalizes it as a pending DOC_PATCH for the DKM document specifically.

**Decision summary:**

Add the working concept **Carrier Context Overlay** to the DKM. A physical place or photo must not automatically be duplicated once per delivery carrier.

Preferred model direction:

```
PLACE EVIDENCE / PHOTO
+
CARRIER-SPECIFIC OPERATIONAL CONTEXT
= Carrier Context Overlay
```

Example:

> Factory A  
> Physical fact: rear gate exists  
> Physical access: 1-ton vehicle access observed  
> Carrier context (Coupang): receiving observed at rear gate, weekday mornings  
> Carrier context (another carrier): front entrance typically used

Information priority direction for the current user's carrier context:

1. Current carrier-context evidence
2. Common delivery evidence (cross-carrier physical facts)
3. Other-carrier-only evidence

**Rationale:**

Storing one physical fact (rear gate) per carrier wastes storage and creates divergent fact records for the same physical reality. Carrier context overlays the shared physical fact with carrier-specific operational context, avoiding duplication while preserving carrier-specific utility.

**Validation boundary:**

Do not encode unofficial carrier brand colors, logos, or emoji mappings as a core data model element. The model must use a neutral `carrier_context_label` as a string, not a carrier-specific UI property.

**Dependency:** None directly; relates to §13 Shared Knowledge Boundary in DKM.

**Recommended incorporation point:** Next documentation batch after the Carrier Context Overlay concept receives additional field-use evidence.

---

### DKM-PATCH-005 — Photo Evidence Reuse

**Queue ID:** DKM-PATCH-005
**Classification:** DOC_PATCH
**Status:** QUEUED
**Target document:** `docs/DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` (extend §5 Data Layers or add to DKM-PATCH-004 Carrier Context Overlay section)

**Decision summary:**

The same physical photo may support multiple operational contexts. Do not duplicate the image binary because multiple carriers use the same place.

Conceptual direction:

```
photo evidence
→ physical subject
→ place relationship
→ one or more carrier-context relationships
```

Fields the system should preserve per photo evidence item (direction, exact schema not finalized):

- `observed_at` — when the photo was taken
- `last_confirmed_at` — when the photo was last confirmed still accurate
- `confirmation_evidence` — references to later reports that agreed with the photo
- `contradiction_evidence` — references to later reports that contradicted it

Evidence-freshness language direction (not immutable copy):

Recent repeated evidence:

> "쿠팡 배송 위치로 후문이 반복 확인됐습니다."

Older evidence:

> "쿠팡 배송 위치로 과거 후문 사용 정보가 있습니다."

**Rationale:**

Photo duplication per carrier creates storage waste and makes it impossible to determine whether the same physical photo has been separately confirmed or contradicted per carrier context. A single photo reference with multiple carrier-context overlays allows confirmation and contradiction to be tracked cleanly.

**Validation boundary:**

Exact schema is not finalized. Photo freshness thresholds (when "recent" becomes "older") require field observation.

**Dependency:** DKM-PATCH-004 (Carrier Context Overlay).

**Recommended incorporation point:** Incorporate with DKM-PATCH-004.

---

## Section C — Android / Floating Control Code Patch Queue

---

### UI-PATCH-001 — Floating Control Opacity Adjustment

**Queue ID:** UI-PATCH-001
**Classification:** CODE_PATCH
**Status:** QUEUED
**Target document / area:** `android-spike/` → future production Android implementation; settings screen

**Decision summary:**

Add floating-control opacity adjustment accessible from an options/settings screen.

Preferred UX direction:

- Slider control
- Live preview of the floating control's appearance as the slider is adjusted
- The user understands the resulting opacity before leaving settings

Range direction:

- Minimum: strongly faded but still operationally visible during delivery
- Maximum: fully opaque (current behavior)

The property is **opacity**, not brightness. Do not use the term brightness in UX copy or code.

**Rationale:**

Some drivers may find a fully opaque floating control visually intrusive when overlaid on their carrier app. An opacity option allows the driver to reduce visual distraction while preserving operational access.

**Validation boundary:**

Exact minimum alpha value is not finalized. Too low risks the control becoming invisible in low-contrast environments. The minimum must preserve operational visibility. This requires testing on real devices in field lighting conditions.

**Dependency:** UI-PATCH-003 (Live Preview). Implement together to avoid a disconnected settings UX.

**Recommended incorporation point:** Next Android code batch after C4 Criterion 6 real-device revalidation is confirmed complete.

---

### UI-PATCH-002 — Floating Control Size Adjustment

**Queue ID:** UI-PATCH-002
**Classification:** CODE_PATCH
**Status:** QUEUED
**Target document / area:** `android-spike/` → future production Android implementation; settings screen

**Decision summary:**

Add floating-control size adjustment accessible from options/settings.

Direction:

- Slider control
- The current control size remains the default and must be available at the slider's maximum (or a clearly indicated default position)
- Minimum direction: approximately half the current control's tap/visual area (not smaller, to preserve operability)

**Scope clarification:**

This adjustment applies to the floating button/control only. Long-press radial menu target size and radial menu geometry must remain unchanged unless separately approved. These are governed by Criterion 6 geometry, which is independently validated.

**Rationale:**

Different drivers have different hands, vehicles, and mounting positions. A smaller floating control reduces screen occlusion for drivers who are comfortable with precise taps.

**Validation boundary:**

Exact dp limits for minimum and maximum are not finalized. The minimum must remain operationally tappable under delivery-pressure conditions. Field testing required.

**Dependency:** UI-PATCH-003 (Live Preview). UI-PATCH-004 (Criterion 6 Revalidation) should be complete first to avoid geometry interaction.

**Recommended incorporation point:** Next Android code batch after UI-PATCH-004 is confirmed.

---

### UI-PATCH-003 — Live Preview in Settings

**Queue ID:** UI-PATCH-003
**Classification:** CODE_PATCH
**Status:** QUEUED
**Target document / area:** `android-spike/` → future production Android implementation; settings screen

**Decision summary:**

Opacity (UI-PATCH-001) and floating-control size (UI-PATCH-002) settings must both provide immediate visual preview. The user must be able to see the resulting control appearance before confirming the setting.

Implementation note: "live preview" means the floating control (or a faithful in-settings rendering of it) visually updates in real time as the slider is dragged. This does not require displaying the actual overlay while the settings screen is open; an in-settings preview widget is sufficient.

Exact settings-screen design is not finalized.

**Rationale:**

Without live preview, a user setting opacity or size cannot accurately judge the result until they leave settings and open the floating control. This creates a frustrating trial-and-error loop, especially for a control that must remain operationally visible during delivery.

**Validation boundary:**

Whether an in-settings preview widget is sufficient or whether the actual overlay must be shown requires UX testing.

**Dependency:** UI-PATCH-001 and UI-PATCH-002. Should be implemented together with both.

**Recommended incorporation point:** Next Android code batch with UI-PATCH-001 and UI-PATCH-002.

---

### UI-PATCH-004 — Hexagonal Radial Geometry Real-Device Revalidation

**Queue ID:** UI-PATCH-004
**Classification:** CODE_PATCH
**Status:** READY_FOR_CODE_BATCH
**Target document / area:** `android-spike/` → APK build and physical device test; `docs/C4_ANDROID_FLOATING_CAPTURE_SPIKE.md` §17 and §20 update

**Decision summary:**

The hexagonal radial geometry redesign (replacing the prior arc-fan algorithm) is implemented in the current work package. A new APK build and real-device revalidation are required before Criterion 6 can be marked complete.

Required on-device checks (minimum):

- Floating button at center of screen
- Floating button at top-left corner
- Floating button at top-right corner
- Floating button at bottom-left corner
- Floating button at bottom-right corner

At each position, confirm:

1. Exactly 6 visually distinct targets appear
2. All 6 targets appear at a uniform ring radius from the menu center (visually consistent distance)
3. No targets overlap
4. No target is partially or fully outside the visible screen area
5. Floating button anchor is restored to original resting position after selection or cancellation

If any check fails, do not mark C4 Criterion 6 PASS. Report the specific failure.

**Current implementation state (from WORK_LOG):**

The hexagonal shift geometry (menuCenter + R × (cos θ, sin θ), 60° fixed spacing) is implemented. Tests: 29/29 pass. A new APK has not yet been built or installed. No device validation of the redesign has occurred.

**Rationale:**

The prior geometry produced visual failures (fewer than 6 distinct targets, overlapping targets) at corner and edge positions on a real Samsung Galaxy Android 16 device. The redesign is fully unit-tested but not yet physically validated.

**Validation boundary:**

Do not mark Criterion 6 PASS until the physical device checks above are confirmed by the founder on a real device.

**Dependency:** None blocking. Requires `.\gradlew.bat :app:assembleDebug` to build a new APK.

**Recommended incorporation point:** Immediate — this is the top-priority open item for C4 completion.

---

### UI-PATCH-005 — Interaction Sound Polish

**Queue ID:** UI-PATCH-005
**Classification:** POLISH_BACKLOG
**Status:** QUEUED
**Target document / area:** Future production Android implementation; sound design

**Decision summary:**

When DFI approaches production-completion stage, consider adding distinct subtle sound feedback for:

- Short tap (voice capture start)
- Floating control movement
- Long press / radial menu open
- Optionally, radial selection confirmation (if later justified as non-intrusive)

**Rationale:**

Subtle audio feedback can reduce the need for the driver to visually confirm that an interaction registered. However, sound must not become excessive or disruptive during delivery.

**Validation boundary:**

Do not implement during current feasibility validation. Sound design in a delivery vehicle context (engine noise, ambient road noise, carrier app audio) requires real-condition testing to avoid creating more distraction than benefit.

**Dependency:** All Criterion 6 and interaction-path revalidation must be complete first.

**Recommended incorporation point:** Post-field-validation, when the core interaction design is confirmed stable. Earliest after Phase A field validation is complete.

---

## Section D — Queue Summary Table

| Queue ID | Classification | Status | Target |
|----------|---------------|--------|--------|
| HM-PATCH-001 | DOC_PATCH | QUEUED | `DFI_HAZARD_MEMORY_MODEL_V1.md` |
| HM-PATCH-002 | DOC_PATCH | QUEUED | `DFI_HAZARD_MEMORY_MODEL_V1.md` |
| HM-PATCH-003 | VALIDATION_HYPOTHESIS | REQUIRES_VALIDATION | `DFI_HAZARD_MEMORY_MODEL_V1.md` |
| HM-PATCH-004 | DOC_PATCH | QUEUED | `DFI_HAZARD_MEMORY_MODEL_V1.md` |
| HM-PATCH-005 | DOC_PATCH | QUEUED | `DFI_HAZARD_MEMORY_MODEL_V1.md` |
| DKM-PATCH-001 | DOC_PATCH | QUEUED | `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` |
| DKM-PATCH-002 | VALIDATION_HYPOTHESIS | REQUIRES_VALIDATION | `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` |
| DKM-PATCH-003 | DOC_PATCH | QUEUED | `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` |
| DKM-PATCH-004 | DOC_PATCH | QUEUED | `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` |
| DKM-PATCH-005 | DOC_PATCH | QUEUED | `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` |
| UI-PATCH-001 | CODE_PATCH | QUEUED | Android settings — opacity |
| UI-PATCH-002 | CODE_PATCH | QUEUED | Android settings — size |
| UI-PATCH-003 | CODE_PATCH | QUEUED | Android settings — live preview |
| UI-PATCH-004 | CODE_PATCH | READY_FOR_CODE_BATCH | Android APK build + device check |
| UI-PATCH-005 | POLISH_BACKLOG | QUEUED | Future production sound design |

**Counts:** 5 DOC_PATCH (QUEUED) · 2 VALIDATION_HYPOTHESIS (REQUIRES_VALIDATION) · 3 CODE_PATCH (QUEUED) · 1 CODE_PATCH (READY_FOR_CODE_BATCH) · 1 POLISH_BACKLOG (QUEUED)

---

## Cross-References

- `DFI_HAZARD_MEMORY_MODEL_V1.md` — target for HM-PATCH-001 through HM-PATCH-005
- `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` — target for DKM-PATCH-001 through DKM-PATCH-005
- `DFI_VALIDATION_EXPORT_CONTRACT_V1.md` — Delivery Terrain (§J), Carrier Context Overlay (§K) already labeled DIRECTION; this queue formalizes the doc-patch path
- `C4_ANDROID_FLOATING_CAPTURE_SPIKE.md` — UI-PATCH-004 updates §17 and §20 upon completion
- `WORK_LOG.md` — all incorporation events must append an entry referencing the Queue ID
