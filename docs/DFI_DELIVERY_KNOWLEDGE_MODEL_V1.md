# DFI Delivery Knowledge Model V1

Last updated: 2026-07-14
Status: Approved design direction — pre-field validation
Parent documents: `DFI_PRODUCT_VISION_V2.md`, `DFI_PRODUCT_VISION_V2_1_ADDENDUM.md`

---

## Purpose

This document defines the founder-approved design direction for personal delivery behavior learning, delivery-session context inference, and the temporary transfer of accumulated delivery knowledge to a substitute driver.

Working concepts defined here:

- **Personal Delivery Baseline** — a learned model of an individual driver's normal delivery behavior
- **Delivery Knowledge Model (DKM)** — derived representation of repeated delivery behavior and accumulated field knowledge
- **Knowledge Handoff** — a temporary, explicitly authorized transfer of DKM-derived guidance to another worker

Approved design principles are marked **[APPROVED PRINCIPLE]**.
Current design directions not yet field-validated are marked **[DESIGN DIRECTION]**.
Hypotheses requiring validation are marked **[UNVALIDATED HYPOTHESIS]**.
Future work deferred from V1 is marked **[FUTURE DIRECTION]**.

This document does not define production database schemas or implement backend logic.

---

## 1. Explicit Delivery Start

**[APPROVED PRINCIPLE]** The default authoritative entry into delivery assistance is an explicit user action:

```
배송 안내 시작
```

**[APPROVED PRINCIPLE]** Explicit user intent overrides inferred context. When a driver explicitly declares a delivery session has begun, DFI may enter `ACTIVE_DELIVERY` immediately without waiting for behavior pattern classification.

**[APPROVED PRINCIPLE]** An explicitly labeled session is higher-quality training data for the Personal Delivery Baseline than an automatically inferred one. The system must track which sessions were explicitly labeled and which were automatically inferred.

---

## 2. Personal Delivery Baseline

**[APPROVED PRINCIPLE]** DFI may learn from sessions that are explicitly labeled by the user as delivery sessions. The baseline must not be constructed from unlabeled sessions without user awareness.

**[APPROVED PRINCIPLE]** Do not define a delivery session by a single average stop duration. Delivery patterns are multi-dimensional and vary by:

- day of week and time of day
- delivery volume
- building type and density (dense urban buildings produce longer stops)
- vehicle class
- delivery mode (parcel, food, freight)
- route familiarity

**[DESIGN DIRECTION]** The Personal Delivery Baseline should conceptually consider distributions and sequences including:

| Dimension | Description |
|-----------|-------------|
| Usual delivery time window | When in the day this driver typically works |
| Route/segment familiarity | Which areas are frequently visited |
| Speed distribution | Normal in-delivery speed range |
| Stop-duration distribution | Typical time parked at delivery locations |
| Stop density | How many stops per unit distance or time |
| Movement-stop cycles | Pattern of drive → stop → drive sequences |
| Segment repetition | Which road segments appear frequently |
| Session duration distribution | How long complete delivery sessions last |
| Starting-area patterns | Where sessions typically begin |

**[APPROVED PRINCIPLE]** A single global delivery model is insufficient. Different delivery types produce fundamentally different baseline patterns. Examples:

| Driver type | Characteristic behavior |
|-------------|------------------------|
| Parcel van driver | Many short stops, moderate distances |
| Motorcycle delivery driver | Higher speed transitions, minimal stop time |
| Early-morning fixed-route driver | Very regular timing, familiar segment repetition |

The baseline must be personal first. Cross-driver averages are a much weaker signal and must not override personal history.

---

## 3. Automatic Delivery Context Inference

**[DESIGN DIRECTION]** Automatic context detection should compare current behavior with the user's own historical Personal Delivery Baseline, not with a generic population average.

Define conceptual delivery context states:

| State | Meaning |
|-------|---------|
| `UNKNOWN` | Insufficient evidence to classify current context |
| `DELIVERY_LIKELY` | Behavior matches personal delivery baseline with meaningful confidence |
| `ACTIVE_DELIVERY` | Explicit start declared, or very high confidence inference |
| `NOT_DELIVERY` | Behavior pattern inconsistent with delivery baseline |

**[DESIGN DIRECTION]** Initial product direction: automatic detection should propose delivery assistance rather than silently activating full delivery mode.

Example:

> "배송 중인 것 같아요. 안내를 시작할까요?"

One-action approval. User acceptance is positive context feedback for baseline improvement. User rejection is negative context feedback.

**[APPROVED PRINCIPLE]** Explicit delivery start (`배송 안내 시작`) is stronger labeled evidence than inferred context. The two must not be treated as equivalent data quality.

**[DESIGN DIRECTION]** A future option for experienced users who have a well-established baseline:

```
배송 패턴 감지 시 자동으로 안내 시작
```

**[APPROVED PRINCIPLE]** Do not treat automatic activation as the initial default. It must be opt-in by users who have validated the inference quality against their own experience.

---

## 4. Context-Sensitive Interaction Budget

**[APPROVED PRINCIPLE]**

> "The more hazardous the driving context, the simpler DFI should become."

**[DESIGN DIRECTION]** Define context-appropriate interaction levels:

| Context | Permitted interaction |
|---------|----------------------|
| Likely driving / moving | Voice, brief warning, one factual touch, minimal visible decisions |
| Temporary stop | Short confirmation, limited choices |
| Idle / safe inspection | Detail views, maps, settings, multi-step review |

**[UNVALIDATED HYPOTHESIS]** Exact context-state classification algorithm is not finalized. Speed thresholds, stop-detection criteria, and motion classification require field validation.

This principle is consistent with the post-passage verification design in `DFI_HAZARD_MEMORY_MODEL_V1.md` §5: one factual action must complete the response.

---

## 5. Data Layers

**[DESIGN DIRECTION]** Define three logical data layers with distinct retention, privacy, and sharing characteristics.

### Layer 1 — Raw Personal Trace

Data collected during a delivery session:

- timestamped GPS samples
- speed
- heading
- sensor context events
- session start/stop events
- DFI interaction events

**[APPROVED PRINCIPLE]** Layer 1 is local-first. Short retention by default is the design direction. The exact default retention duration is not finalized and requires user-input and privacy design before production. Raw GPS history must not be retained indefinitely without explicit user consent.

### Layer 2 — Personal Delivery Knowledge

Derived knowledge computed from Layer 1 across multiple labeled sessions:

- repeated route patterns
- stop clusters (frequent delivery locations)
- area-order patterns (typical traversal sequence)
- time-of-day patterns
- Personal Delivery Baseline
- recurring access observations
- personal work notes explicitly captured by the user

**[APPROVED PRINCIPLE]** Layer 2 is the user's personal delivery knowledge asset. Its retention is longer than Layer 1. The user controls its deletion.

### Layer 3 — Handoff View / Handoff Package

A temporary, explicitly authorized view of selected Layer 2 knowledge prepared for another worker.

**[APPROVED PRINCIPLE]** Do not expose raw Layer 1 historical traces in a Handoff by default. The Handoff derives from Layer 2.

---

## 6. Delivery Knowledge Model (DKM)

**[APPROVED PRINCIPLE]** The DKM is not a replay of raw GPS history. It is a derived representation of repeated delivery behavior and accumulated field knowledge.

Examples of DKM content:

| Category | Description |
|----------|-------------|
| Common area traversal order | Which areas are typically visited in which sequence |
| Repeated stop clusters | Where the driver regularly stops for deliveries |
| Frequently paired building visits | Which buildings tend to be visited in sequence |
| Common entry and exit patterns | How the driver typically approaches and leaves locations |
| Time-dependent access patterns | Access routes that differ by time of day |
| Route-flow habits | Normal driving rhythm through a delivery area |
| Recurring field notes | Notes the driver has repeatedly found relevant |

**[APPROVED PRINCIPLE]** DFI should convert repeated behavior into reusable operational knowledge, not store every raw trace.

**[APPROVED PRINCIPLE]** Do not assume that the experienced driver's historical route is always the optimal route for another driver. The DKM describes what has been done repeatedly, not what should always be done.

---

## 7. Fact vs. Observation vs. Personal Pattern

**[APPROVED PRINCIPLE]** The DKM and Handoff system must distinguish information provenance. At minimum distinguish:

| Provenance type | Meaning |
|----------------|---------|
| `FIELD_FACT` | Objectively verifiable physical or access fact |
| `REPEATED_OBSERVATION` | Observed multiple times in similar conditions |
| `PERSONAL_PATTERN` | This particular driver's habitual behavior |

Examples:

**FIELD_FACT:**

> "912동 뒤편 진입로는 높이 제한 정보가 있다."

**REPEATED_OBSERVATION:**

> "이 구간은 오전에 차량 정체가 반복 관찰됐다."

**PERSONAL_PATTERN:**

> "기존 담당자는 보통 911동 다음 912동을 배송했다."

**[APPROVED PRINCIPLE]** DFI language must reflect provenance. Do not convert a personal habit into an authoritative instruction.

Avoid:

> "911동 다음 912동으로 가세요." ← presents personal pattern as directive

Prefer:

> "기존 담당자는 보통 911동 다음 912동을 배송했습니다." ← accurate provenance

---

## 8. Knowledge Handoff

**[APPROVED PRINCIPLE]** Knowledge Handoff is a temporary, explicitly authorized transfer of useful accumulated delivery knowledge from an experienced worker to a substitute worker.

**Founder use case:**

The regular driver is absent and another driver temporarily covers the route. The substitute should not need to relearn every local operational detail from zero. The Handoff provides a starting advantage without claiming to replace the regular driver's experience.

**[DESIGN DIRECTION]** Possible shared categories in a Handoff:

- basic delivery flow (area sequence, general approach)
- repeated stop points (frequently visited delivery locations)
- area-order patterns
- access cautions (noted difficulties with specific locations)
- recurring delivery notes explicitly captured by the regular driver

**[APPROVED PRINCIPLE]** Sensitive and raw categories must be excluded by default:

- raw historical GPS traces (Layer 1)
- personal voice recordings (original audio)
- unrelated historical location detail
- personal session history unrelated to the covered route

---

## 9. Handoff Authorization

**[APPROVED PRINCIPLE]** Knowledge sharing must require explicit authorization from the originating user. Sharing must never occur silently or automatically.

**[DESIGN DIRECTION]** Possible sharing duration options:

```
today only
3 days
7 days
until manually revoked
```

**[UNVALIDATED HYPOTHESIS]** The set of available options and appropriate defaults are not finalized. This requires field observation of how substitute coverage actually works in practice.

**[APPROVED PRINCIPLE]** The originating user must be able to revoke sharing at any time.

**[APPROVED PRINCIPLE]** Knowledge Handoff must not imply public visibility of personal delivery patterns. A Handoff is a directed share to a specific named recipient or session, not publication to a community.

---

## 10. Today's Work vs. Historical Knowledge

**[APPROVED PRINCIPLE]** DFI must not blindly replay the regular driver's historical route as a task list for the substitute.

The conceptual combination for substitute-driver assistance is:

```
Historical DKM
+ Today's actual delivery workload
+ Current location and context
+ Shared field-risk knowledge (from DFI_HAZARD_MEMORY_MODEL_V1.md)
= Today's substitute-driver assistance
```

**[APPROVED PRINCIPLE]** If a historical stop has no delivery assignment today, DFI must not route the substitute there merely because the regular driver often visited it. Historical behavior is context, not today's task list.

---

## 11. Substitute Driver Experience

**[APPROVED PRINCIPLE]** The substitute driver may have different skills, a different vehicle class, and different local familiarity from the regular driver.

**[APPROVED PRINCIPLE]** DFI must not assume:

> "The regular driver did this, therefore the substitute should do exactly the same."

A route maneuver comfortable for an experienced regular driver may be:

- physically inappropriate for a different vehicle class
- navigationally confusing for an unfamiliar driver
- time-inefficient given a different stop sequence today

**[DESIGN DIRECTION]** The Handoff experience should distinguish:

| Category | Treatment |
|----------|----------|
| Operational fact | Present directly |
| Repeated historical observation | Present with provenance |
| Personal pattern or preference | Present as guidance, not directive |

**[APPROVED PRINCIPLE]** The Handoff should aim to approximate useful experienced-driver guidance without claiming the experienced driver is physically present.

---

## 12. User Data Ownership Principle

**[APPROVED PRINCIPLE]**

> "Delivery knowledge accumulated from a user's work is, by default, the user's personal data asset."

The user must control:

- retention duration (including deletion)
- sharing (who receives a Handoff)
- sharing duration
- revocation of active shares

**[APPROVED PRINCIPLE]** DFI must not treat personal delivery knowledge as automatically public or as DFI-owned commercial inventory.

**[APPROVED PRINCIPLE]** Public and shared hazard knowledge (`DFI_HAZARD_MEMORY_MODEL_V1.md`) is a separate logical domain from private delivery knowledge. The two must not be automatically merged.

---

## 13. Shared Knowledge Boundary

**[APPROVED PRINCIPLE]** Clearly distinguish:

| Domain | Examples |
|--------|---------|
| `SHARED_FIELD_KNOWLEDGE` | Jointly validated road hazards, general access-risk observations confirmed by multiple drivers |
| `PRIVATE_DELIVERY_KNOWLEDGE` | Personal route habits, stop patterns, session history, personal work notes |

**[APPROVED PRINCIPLE]** Personal route behavior must not automatically be promoted into shared field knowledge. A route the regular driver uses does not become a public recommendation merely because it was used repeatedly.

**[FUTURE DIRECTION]** Any future derived public statistics about route patterns or delivery density require a separate privacy design, aggregation policy, legal review, and explicit user opt-in.

---

## 14. Future Validation Questions

**[UNVALIDATED HYPOTHESIS]** The following questions require field observation before product decisions can be made. Do not invent answers.

| Question | Why it matters |
|----------|---------------|
| Minimum labeled sessions for a useful Personal Delivery Baseline | Below this threshold, inference is unreliable |
| False-positive rate of `DELIVERY_LIKELY` in the field | Determines whether automatic activation is practical |
| How route variability affects baseline confidence | A driver covering many different areas daily has a harder baseline to establish |
| Whether baseline inference should remain fully local | Privacy and battery implications of on-device vs. server-side computation |
| Retention duration for raw Layer 1 trace | Privacy and storage constraint |
| What knowledge substitute drivers actually find useful | Prevents building a system that provides too much low-value data |
| What information causes overload during the first 30 minutes of substitute coverage | The most critical window where guidance matters most |
| How Handoff should adapt to vehicle type and driver experience | A new driver may need different framing than a driver with 5 years in a different area |
| Which DKM elements are stable vs. day-specific | Determines what is worth including in a Handoff vs. what would be misleading |

---

## Cross-References

- `DFI_PRODUCT_VISION_V2.md` — core product principles, Experience Mining, EER, Field Trust, Contribution Credit, primary validation question
- `DFI_PRODUCT_VISION_V2_1_ADDENDUM.md` — personal behavior baseline, passive assistance, help-seeking bias, Route Breakdown Event
- `DFI_FIELD_VALIDATION_PROTOCOL_V1_1.md` — validation phases A and B, substitute/new-driver measurement
- `DFI_USER_INTELLIGENCE_PROFILE_MODEL_V1.md` — profile privacy boundaries relevant to data layer design
- `DFI_HAZARD_MEMORY_MODEL_V1.md` — shared field knowledge domain, context-sensitive interaction budget (companion document)
