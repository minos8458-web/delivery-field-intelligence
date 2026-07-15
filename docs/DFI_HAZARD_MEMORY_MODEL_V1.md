# DFI Hazard Memory Model V1

Last updated: 2026-07-14
Status: Approved design direction — pre-field validation
Parent documents: `DFI_PRODUCT_VISION_V2.md`, `DFI_PRODUCT_VISION_V2_1_ADDENDUM.md`

---

## Purpose

This document defines the founder-approved design direction for DFI's shared field-road hazard memory system. It establishes the Hazard Memory Loop as a core product concept, defines hazard data shapes, evidence states, warning behavior, and reporter-reliability principles.

Approved design principles are marked **[APPROVED PRINCIPLE]**.
Current design directions not yet field-validated are marked **[DESIGN DIRECTION]**.
Hypotheses requiring validation are marked **[UNVALIDATED HYPOTHESIS]**.
Future work deferred from V1 is marked **[FUTURE DIRECTION]**.

This document does not define production database schemas or implement backend logic.

---

## 1. Core Purpose — Hazard Memory Loop

**[APPROVED PRINCIPLE]**

DFI should allow a field driver to report a hazard after already passing it. The user must not be required to stop at the exact hazard coordinate or manually place a map pin while driving.

Example voice report:

> "방금 큰 포트홀 있었어."

DFI uses recent movement context to derive a hazard location candidate from this report.

The working concept name for this product loop is:

**Hazard Memory Loop**

```
hazard observation
→ low-friction voice report
→ recent trajectory interpretation
→ hazard candidate
→ shared warning candidate
→ later driver passage
→ one-action factual verification
→ evidence update
```

This loop is consistent with the broader DFI principle:

> Driver experience → low-friction capture → AI structuring → human confirmation → cross-context validation → shared field knowledge

---

## 2. Retrospective Hazard Reporting

**[APPROVED PRINCIPLE]** Recent local trajectory buffering is a design requirement. A field driver must be able to report a hazard after passing it.

**[APPROVED PRINCIPLE]** The speech-recognition completion coordinate must not be treated as the hazard coordinate. The driver is reporting something that already passed.

**[DESIGN DIRECTION]** DFI should buffer recent local trajectory (GPS samples, speed, heading) for retrospective hazard location inference. A specific retention duration has not been validated and must not be fixed prematurely. The appropriate retention window is a field-validation parameter.

Temporal language in speech may provide evidence for location inference:

| Korean phrase | Approximate temporal signal |
|---------------|---------------------------|
| 여기 | current or very recent location |
| 방금 | just now, seconds ago |
| 조금 전에 | a short while ago |
| 아까 | some time ago (minutes) |

These are approximate signals, not precise timestamps. Temporal language plus trajectory plus speed provides a candidate inference, not a certainty.

**[APPROVED PRINCIPLE]** A hazard report may initially represent a trajectory/time window and a best-estimate point rather than a confirmed coordinate.

### Hazard Candidate Window

Define the working concept:

**Hazard Candidate Window** — the segment of recent trajectory and time during which a reported hazard most likely occurred, given all available evidence.

Location inference may later incorporate:

- recent GPS trajectory
- vehicle speed
- heading
- temporal language parsed from the voice report
- user-specific reporting-delay pattern learned over time
- optional sensor evidence (see §7)

**[APPROVED PRINCIPLE]** Sensor events are supporting evidence only. A shock or vibration event must not automatically create a pothole report. Sensor data may narrow the Hazard Candidate Window or support a human-initiated report, but must not replace human observation and confirmation.

---

## 3. Hazard Data Shapes

**[DESIGN DIRECTION]** DFI must not force every field road condition into a map point. Define at minimum two hazard shapes:

### POINT_HAZARD

A hazard localized to a point or very short segment.

Examples:
- pothole
- low-clearance point
- isolated severe speed hump
- drainage-cover displacement
- isolated road collapse

### SEGMENT_HAZARD

A hazard condition that persists over a meaningful road segment.

Examples:
- unpaved road surface
- generally poor road surface condition
- recurring flood-prone section
- narrow single-lane access segment
- repeated surface-damage section

**[DESIGN DIRECTION]** Additional shapes may emerge from field validation. Do not pre-define a complete taxonomy before observing real field behavior.

---

## 4. Warning Timing

**[DESIGN DIRECTION]** DFI should warn drivers sufficiently before reaching a hazard to allow safe preparation. The founder's initial product intuition is approximately 50 meters before the reported hazard.

**[APPROVED PRINCIPLE]** 50 meters is a useful initial intuition. It must not become a universal fixed-distance invariant.

**[DESIGN DIRECTION]** Warning timing should eventually consider time-to-arrival based on current speed. At highway speed, 50 meters provides approximately 2–3 seconds. At low urban speed, 50 meters may provide 10–15 seconds. The same metric produces different practical outcomes depending on context.

**[DESIGN DIRECTION]** Direction and road context matter. A hazard on the opposite travel direction of a divided road must not trigger a warning for a driver traveling in the other direction. Route direction is an evidence dimension for hazard relevance.

**[UNVALIDATED HYPOTHESIS]** Exact distance and timing thresholds require field measurement. Do not publish thresholds as validated until field evidence supports them.

---

## 5. Post-Passage Factual Verification

**[APPROVED PRINCIPLE]** After a driver passes the hazard area, DFI may ask one short factual question.

Example:

> "방금 포트홀이 있었나요?"

Visible one-action responses:

```
[예]    [아니오]
```

**[APPROVED PRINCIPLE]** One factual action must complete the verification response. No follow-up form, no star rating, no keyboard, no mandatory explanation.

**[APPROVED PRINCIPLE]** Driving-safety constraint: the interaction cost during vehicle movement must remain minimal. A single touch is the maximum response expected while moving.

**[DESIGN DIRECTION]** Voice response may later supplement or replace touch response. If voice recognition fails or times out, DFI must not repeatedly interrupt the driver to retry.

### Evidence States for Verification Response

Internally distinguish:

| Response | Meaning |
|----------|---------|
| `YES` | Driver confirmed hazard present |
| `NO` | Driver observed no hazard |
| `NO_RESPONSE` | Driver did not interact within the window |

**[APPROVED PRINCIPLE]** `NO_RESPONSE` must not be treated as `NO`. A driver may be occupied, may not have noticed the hazard, or may simply not have interacted. Absence of response is not denial.

---

## 6. Hazard Evidence States

**[DESIGN DIRECTION]** Define the following conceptual evidence states for a hazard:

| State | Meaning |
|-------|---------|
| `REPORTED` | Initial report received, not yet confirmed by independent passage |
| `SUPPORTED` | Multiple independent passage confirmations received |
| `CONFIRMED` | Strong independent evidence; hazard treated as reliable field fact |
| `STALE` | Evidence is aged; hazard may no longer exist |
| `RETIRED` | Sufficient NO evidence, repair confirmation, or time-based expiry |

**[APPROVED PRINCIPLE]** Warning language should reflect evidence strength. Higher-evidence states warrant stronger language. Lower-evidence states warrant appropriately hedged language.

Direction-level language examples (not immutable production copy):

**REPORTED:**
> "이 주변에 포트홀 신고가 있었습니다."

If a size estimate was explicitly reported:
> "이 주변에 약 50cm 크기의 포트홀이 있다는 신고가 있었습니다."

**SUPPORTED:**
> "이 주변에서 포트홀이 여러 차례 확인됐습니다."

**CONFIRMED:**
> "전방에 반복 확인된 포트홀이 있습니다."

**STALE:**
> "이 구간은 오전까지 포트홀 신고가 있었습니다."

**[APPROVED PRINCIPLE]** Audio should communicate meaning and evidence confidence. Detailed numeric evidence counts belong in a detail view that is safe to inspect at a stop, not in a driving-mode voice alert.

---

## 7. Hazard Characteristics

**[DESIGN DIRECTION]** Preserve the raw user description where lawful and appropriate. Normalize hazard characteristics separately from the raw description.

For size and severity, free-form descriptions may be normalized into broad categories such as:

```
SMALL
MEDIUM
LARGE
SEVERE
```

**[APPROVED PRINCIPLE]** Exact measurement thresholds for these categories are not validated. Do not create false precision. Inferred sizes must not be presented as measured facts.

Prefer:

> "큰 포트홀"

or:

> "약 50~70cm 크기로 신고된 큰 포트홀"

Do not fabricate:

> "직경 53.7cm 포트홀" (based solely on inference from a voice report)

**[APPROVED PRINCIPLE]** Separate at minimum three confidence dimensions:

| Dimension | Meaning |
|-----------|---------|
| `locationConfidence` | How confident is the inferred hazard position |
| `hazardExistenceConfidence` | How confident is it that the hazard exists or persists |
| `reporterReliability` | How reliable is this reporter's evidence in this category |

Do not collapse these into a single composite score.

---

## 8. Hazard Saturation and Road Condition Summary

**[DESIGN DIRECTION]** When a road segment contains a high density of ordinary surface hazards, DFI should not announce each one individually. Repeated per-hazard audio alerts on a degraded road segment produce alert fatigue and driving distraction.

Concept: **Road Condition Summary**

Example scenario:

```
many potholes + puddles + broken surface
→ Road Condition Summary rather than individual hazard announcements
```

Example summary language:

> "앞으로 약 500m 동안 노면 상태가 좋지 않은 구간입니다."

For an unpaved segment, describe the segment character rather than individually announcing every ordinary hole.

### Alert Classes

**[DESIGN DIRECTION]** Define two conceptual alert classes:

| Class | Behavior |
|-------|---------|
| `NORMAL_HAZARD` | Subject to Road Condition Summary suppression when density threshold met |
| `CRITICAL_HAZARD` | May override summary suppression even in a degraded segment |

**[UNVALIDATED HYPOTHESIS]** Exact classification rules, density thresholds, and suppression criteria require field measurement. Do not implement as fixed values before field evidence.

---

## 9. Alert Suppression

**[APPROVED PRINCIPLE]** Users must be able to suppress low-value repeated hazard guidance for a road segment.

**[DESIGN DIRECTION]** During driving, presenting three duration choices every time a suppression action occurs adds cognitive load. Prefer one-action suppression:

```
[이 구간 알림 끄기]
```

A user-configured default suppression duration may be applied automatically. Possible duration options:

- 1 day
- 7 days
- 30 days
- always ask

**[UNVALIDATED HYPOTHESIS]** The appropriate default duration is not yet determined. This requires field observation of which suppressed alerts were later regretted by users.

**[FUTURE DIRECTION]** Critical safety alerts (CRITICAL_HAZARD class) may bypass normal hazard suppression according to a future safety policy. The policy must be defined before implementing bypass behavior.

---

## 10. Hazard Clustering and Identity

**[APPROVED PRINCIPLE]** Multiple reports about the same road location must not automatically create multiple separate hazard records. GPS distance alone is insufficient as a clustering criterion.

Define the working concept:

**Hazard Identity** — the determination of whether two or more reports refer to the same physical hazard.

Evidence dimensions that may inform identity:

- trajectory/location proximity
- same road segment context
- travel direction
- hazard taxonomy compatibility
- candidate-window overlap between reports
- characteristic similarity (size, type, description)
- temporal evidence (same time window vs. weeks apart)

**[APPROVED PRINCIPLE]** Do not freeze exact scoring weights for identity determination. The weighting must be validated against real field data.

**[DESIGN DIRECTION]** The system should support at minimum three outcomes for a new report:

```
NEW HAZARD — creates a distinct hazard record
ATTACH EVIDENCE TO EXISTING HAZARD — enriches an existing record
UNRESOLVED REPORT — identity ambiguous; preserve as pending
```

**[APPROVED PRINCIPLE]** When identity is ambiguous, preserve the uncertainty. Do not force a merge merely because geographic proximity suggests it.

**[APPROVED PRINCIPLE]** A hazard object is an evolving evidence cluster, not an immutable coordinate pin. The cluster must support:

```
MERGE — two initially separate clusters determined to be the same hazard
SPLIT — one cluster determined to represent multiple distinct hazards
```

---

## 11. Road Segment Risk History

**[DESIGN DIRECTION]** Separate current hazard existence from the recurring maintenance history of a road segment.

Example progression:

```
January: pothole reported → repaired → RETIRED
February: pothole reported → repaired → RETIRED
April: pothole reported → new hazard record opened
```

**[DESIGN DIRECTION]** The system may derive a **Road Segment Risk History** representing the segment's pattern of recurring damage and repair.

Example language:

> "이 구간은 노면 파손 신고가 반복되는 곳입니다."

This is more actionable than a raw lifetime report count. It signals to drivers and potentially to road authorities that a segment has a structural vulnerability, not merely a single-incident history.

---

## 12. Report Validation and Reporter Reliability

**[APPROVED PRINCIPLE]** Drivers validate field facts, not other people. The factual verification question is about the hazard, not about the reporter:

> "방금 포트홀이 있었나요?"

**[APPROVED PRINCIPLE]** Do not expose public like/dislike scores for reporters. Reporter reputation is an internal signal, not a public attribute.

**[DESIGN DIRECTION]** Define possible report outcomes as evidence evolves:

| Outcome | Meaning |
|---------|---------|
| `VERIFIED` | Strong independent confirmation |
| `SUPPORTED` | Multiple consistent YES responses |
| `UNRESOLVED` | Insufficient evidence to confirm or deny |
| `DISPUTED` | Contradictory evidence without resolution |
| `INVALIDATED` | Strong NO evidence; hazard likely did not exist at reported location |

**[APPROVED PRINCIPLE]** Do not punish a reporter merely because later drivers answered NO. Before reducing reporter reliability, first evaluate:

- location inference error (hazard existed but GPS candidate was shifted)
- hazard disappearance (repaired or temporary)
- wrong direction (hazard exists on opposite direction of divided road)
- clustering error (reported hazard merged with wrong existing record)
- taxonomy error (what was reported differs from what was verified)
- actual false report

**[APPROVED PRINCIPLE]** Reporter reliability must not be reduced to a single public score. Define an internal multidimensional reporter profile. Possible dimensions:

| Dimension | Meaning |
|-----------|---------|
| `hazardDetectionReliability` | Accuracy in this hazard category |
| `temporalReportingBias` | How much reporting delay affects location accuracy |
| `sizeEstimateBias` | Systematic over- or under-estimation of hazard size |
| `severityEstimateBias` | Systematic over- or under-estimation of hazard severity |
| `categoryReliability` | Accuracy in hazard taxonomy classification |
| `abuseRisk` | Signal for coordinated or fraudulent reporting patterns |

**[UNVALIDATED HYPOTHESIS]** The exact model structure is not validated. These are design dimensions, not finalized fields.

**[DESIGN DIRECTION]** Repeated abnormal contradiction patterns may progressively reduce a reporter's influence before any account-level action. Conceptual response stages:

```
normal
→ observation (internal flag, no external effect)
→ reduced standalone influence (reports require more corroboration)
→ independent confirmation required (report only enters shared pool after independent verification)
→ reporting restriction (report creation limited)
→ account review
```

No irreversible steps should occur before account review.

---

## 13. Independent Evidence Principle

**[APPROVED PRINCIPLE]**

> "Report count is not independent evidence count."

> "Trust evidence, not popularity."

One hundred coordinated or correlated reports may represent less independent evidence than four physically independent passages at different times and from different trajectory origins.

**[DESIGN DIRECTION]** DFI should eventually evaluate real-world evidence independence. Factors that reduce evidence independence include:

- impossible geographic movement patterns (reports from accounts that could not physically be present)
- highly duplicated trajectory patterns (multiple accounts following identical GPS traces)
- coordinated confirmation targeting a single hazard without other verifiable activity
- abnormal correlated account behavior

**[FUTURE DIRECTION]** A full anti-Sybil or coordinated-abuse defense system is outside V1 scope. Record this as a future abuse-defense boundary to be designed before DFI operates at scale.

**[APPROVED PRINCIPLE]** No irreversible trust decision. Hazard records, report outcomes, and reporter influence must remain revisable as new evidence arrives.

---

## 14. Speed Hump and Road Infrastructure Knowledge

**[DESIGN DIRECTION]** Road infrastructure knowledge for speed humps requires separate treatment from road-damage hazards. Speed humps are constructed infrastructure with a different existence lifecycle than potholes.

Define conceptually:

| Type | Description |
|------|-------------|
| `PHYSICAL_SPEED_HUMP` | A physical raised structure across the road |
| `VISUAL_SPEED_HUMP_MARKING` | Road markings that appear to indicate a hump but no physical raise |

User-facing language may use:

- 실제 과속방지턱
- 노면 표시형 방지턱

**[DESIGN DIRECTION]** Possible physical hump characteristic categories:

```
NORMAL
STEEP
LOW
POOR_VISIBILITY
DAMAGED
SERIES
```

These are design categories, not a validated taxonomy.

**[DESIGN DIRECTION]** A useful one-action factual verification question after passage:

> "실제 돌출된 방지턱이었나요?"

```
[예]    [아니오]
```

**[APPROVED PRINCIPLE]** Avoid the ambiguous question:

> "가짜 방지턱이었나요?"

This question may be misunderstood or create a negative framing. The preferred question asks about physical reality, not authenticity.

**[APPROVED PRINCIPLE]** Sensor evidence may support physical-hump classification but must not independently determine it. A vibration event does not confirm hump type.

**[APPROVED PRINCIPLE]** Persistent infrastructure (speed humps) and temporary road damage (potholes) must not share identical time-decay assumptions. Infrastructure knowledge should decay much more slowly than damage-hazard knowledge unless a specific change event (construction, removal, repair) is reported.

---

## 15. Long-Term Incident and Public-Safety Boundary

**[FUTURE DIRECTION — NOT V1 SCOPE]**

Record as future direction only. Do not implement in V1.

### Incident Record

A user-controlled preservation and optional export of an accident-adjacent event timeline. Potential evidence sources:

- timestamps
- movement trajectory
- GPS accuracy metadata
- speed and heading estimates
- sensor events
- DFI interaction events
- user speech event timing

**[APPROVED PRINCIPLE]** Raw evidence and derived AI interpretation must remain separate in any incident record.

**[APPROVED PRINCIPLE]** DFI must not declare legal fault or causal responsibility for an incident.

**[APPROVED PRINCIPLE]** No automatic insurer transmission. User-controlled export is the preferred direction.

### Road Risk Intelligence and Aggregated Safety Insight

**[FUTURE DIRECTION]** Long-term, sufficiently aggregated and privacy-protected road-segment signals may become useful to road-safety researchers, public institutions, or other safety stakeholders.

**[APPROVED PRINCIPLE]** Do not define a commercial data-sharing policy in this document.

**[APPROVED PRINCIPLE]** Individual delivery traces must not automatically become third-party driver-risk profiles.

Any future aggregated data product requires a separate privacy design, legal review, and explicit user consent mechanism.

---

## Cross-References

- `DFI_PRODUCT_VISION_V2.md` — core product principles, Field Trust, Contribution Credit
- `DFI_PRODUCT_VISION_V2_1_ADDENDUM.md` — passive assistance, risk contribution allocation, personal behavior baseline
- `DFI_FIELD_VALIDATION_PROTOCOL_V1_1.md` — hazard-type field validation signals
- `DFI_USER_INTELLIGENCE_PROFILE_MODEL_V1.md` — profile privacy boundaries relevant to reporter-reliability model
- `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` — personal delivery baseline and DKM (companion document)
