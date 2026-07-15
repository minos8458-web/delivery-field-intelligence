# DFI Validation Export Contract V1

Last updated: 2026-07-16
Status: Pre-field-validation design direction
Parent documents: `DFI_PRODUCT_VISION_V2.md`, `DFI_PRODUCT_VISION_V2_1_ADDENDUM.md`,
  `DFI_FIELD_VALIDATION_PROTOCOL_V1_1.md`, `DFI_HAZARD_MEMORY_MODEL_V1.md`,
  `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md`

---

## Purpose

This document defines what DFI must preserve and export before and during long-duration
field validation so that, after one month or more of real delivery use, a human or AI
reviewer can determine:

- whether DFI learned useful delivery knowledge
- which inferences were incorrect
- whether interaction burden was acceptable
- which functions were used or ignored
- whether hazard memory produced reusable warnings
- whether Delivery Knowledge is supported by adequate evidence
- which collected data had little product value

**The validation export is not a database backup. It is an analysis contract.**

It is not intended to reproduce the complete production database. It is the minimum
structured evidence set required to answer the field validation questions in
`DFI_FIELD_VALIDATION_PROTOCOL_V1_1.md`.

Principle labels used throughout:

- **[PRINCIPLE]** — Approved design direction
- **[DIRECTION]** — Current design direction, not yet validated
- **[HYPOTHESIS]** — Unvalidated; requires field evidence
- **[VALIDATION REQUIREMENT]** — Must be present before a one-month field trial begins
- **[PRIVACY BOUNDARY]** — Data handling constraint
- **[FUTURE]** — Deferred; not required for V1 export

---

## A. Export Goals

The export must make it possible to investigate each of the following questions without
accessing the live production database.

| Question | Required evidence category |
|----------|--------------------------|
| Did DFI learn the founder's repeated delivery patterns? | Delivery baseline + session history |
| What caused incorrect inferences? | User corrections + false-positive feedback |
| Which interaction types were actually used? | Interaction events |
| Which functions were repeatedly ignored? | Prompts shown vs. answered |
| Did explicit delivery start produce better context evidence than inferred detection? | Session start method + context inference log |
| Did Hazard Memory reports become reusable warnings? | Hazard report → warning → verification chain |
| Were hazard verification prompts answered or ignored? | Hazard verifications (YES/NO/NO_RESPONSE) |
| Did DFI generate excessive warnings? | Warning count vs. verification response rate |
| Did Road Condition Summary suppression reduce alert burden? | Alert suppression events |
| Did route-breakdown candidates correspond to real difficulty? | Route breakdown candidates + user labels |
| Did useful Delivery Knowledge emerge? | DKM items + supporting evidence references |
| Is the knowledge sufficient for Knowledge Handoff? | Handoff package evaluation |
| Which collected data had no product value? | Low-signal or never-referenced events |

**[PRINCIPLE]** The export answers specific product hypotheses. Evidence that cannot be
linked to a product question is not required.

---

## B. Export Package Structure

### Package naming

```
DFI_VALIDATION_EXPORT_<FROM>_<TO>_V1.zip
```

Where `<FROM>` and `<TO>` are `YYYY-MM-DD` local delivery dates (the first and last
session dates included in the export).

Rationale: local delivery dates are meaningful to the reviewer. UTC-only naming obscures
the delivery calendar. The `_V1` suffix carries the export schema version, not the app
version — these must be tracked separately (see §Q).

Alternative form for a single-day export:

```
DFI_VALIDATION_EXPORT_<DATE>_V1.zip
```

### Top-level package contents

```
DFI_VALIDATION_EXPORT_<FROM>_<TO>_V1.zip
├── manifest.json
├── README.md
├── validation_report.html
├── summary.json
│
├── sessions/
│   └── delivery_sessions.csv
│
├── events/
│   ├── context_events.csv
│   ├── interaction_events.csv
│   ├── user_corrections.csv
│   ├── false_positive_feedback.csv
│   └── offline_sync_events.csv
│
├── hazards/
│   ├── hazard_reports.csv
│   ├── hazard_verifications.csv
│   └── hazard_state_transitions.csv
│
├── routes/
│   └── route_breakdown_candidates.csv
│
├── knowledge/
│   ├── stop_clusters.csv
│   ├── delivery_baseline.json
│   ├── delivery_knowledge.json
│   └── handoff_knowledge.json
│
└── diagnostics/
    └── app_diagnostics.json
```

### Decisions on the proposed file list

**Merged:** `hazard_candidate_window` is not a separate file. The Hazard Candidate Window
is represented as fields within `hazard_reports.csv` (`candidate_window_start_offset_ms`,
`candidate_window_end_offset_ms`, `temporal_language_hint`). A separate file adds a join
without adding information.

**Excluded from V1:** `trajectory_raw.csv` — raw GPS trace is Layer 1 data and is
excluded from the default export by privacy policy (see §L). Spatial evidence is
preserved at the appropriate privacy level in stop clusters, hazard candidate coordinates,
and route breakdown records.

**Not included in V1 as a separate file:** `road_segment_id` is a field within hazard
and route records, not a standalone export entity.

---

## C. Stable Identifiers and Correlation

**[PRINCIPLE]** Every export record must be traceable to its session and, where
applicable, to the specific evidence that caused a derived conclusion.

| Identifier | Scope | Stability |
|------------|-------|-----------|
| `export_id` | One ZIP package | Unique per export; not required to match across exports |
| `install_id` | Pseudonymous installation identity | Stable across exports from the same installation; not tied to personal identity |
| `session_id` | One delivery session | Stable within and across exports; enables longitudinal comparison |
| `event_id` | One logged event | Stable within one export; may be referenced across files |
| `hazard_id` | One hazard cluster | Stable across exports; tracks state transitions over time |
| `hazard_candidate_id` | One Hazard Candidate Window | Stable within the cluster; referenced by verifications |
| `hazard_verification_id` | One post-passage verification | Unique per verification event |
| `road_segment_id` | Logical road segment (coarse) | **[DIRECTION]** Stable within one export; cross-export stability requires server-side segment registry |
| `stop_cluster_id` | Repeated stop location cluster | Stable across exports from the same installation |
| `knowledge_item_id` | One DKM knowledge item | Stable across exports; required for longitudinal provenance tracking |
| `handoff_package_id` | One Knowledge Handoff package | Unique per generated package |
| `correction_id` | One user correction event | Unique per event; references the `event_id` of the corrected inference |

**[PRINCIPLE]** Do not expose unnecessary direct personal identifiers. `install_id` must
be a locally generated pseudonymous identifier — not a device serial number, phone
number, or user real name.

**[DIRECTION]** `session_id` and `hazard_id` are the primary candidates for cross-export
stability. Cross-export stability requires that the same `hazard_id` not be reused for a
different physical location if the original hazard is retired and a nearby new one opens.

---

## D. Time Model

**[PRINCIPLE]** Every event must carry a machine-readable UTC timestamp. UTC is the
unambiguous reference for cross-session and cross-export ordering.

**[PRINCIPLE]** Every event must carry the local delivery date (`YYYY-MM-DD` in the
driver's configured timezone). Delivery work is organized by local calendar date.
A session starting at 23:50 local time must be attributed to that local delivery date.

**[DIRECTION]** Every session record must carry the local timezone identifier
(e.g. `Asia/Seoul`) so that any reviewer can reconstruct local time from UTC without
relying on implicit assumptions.

**[PRINCIPLE]** Within a session, relative timing (`session_offset_ms`) must be exported
alongside wall-clock timestamps. Wall-clock timestamps alone are unreliable if the device
clock adjusts during a session, the device loses power, or events are queued offline and
flushed later. Monotonic session offset is the authoritative ordering signal within a
session.

**[VALIDATION REQUIREMENT]** The export must make event ordering within a session
unambiguous. A reviewer must be able to reconstruct:

```
session_start → context_inference → user_accepted/rejected
→ interaction_events → voice_capture → hazard_report → ... → session_end
```

without relying solely on wall-clock timestamps.

---

## E. Provenance

**[PRINCIPLE]** Every derived conclusion must be distinguishable from raw observation.
A reviewer must be able to ask "What evidence caused DFI to believe this?" and receive a
traceable answer.

Extend the provenance model from `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md §7`:

| Provenance type | Export tag | Meaning |
|----------------|------------|---------|
| Explicit user input | `explicit` | User stated this fact (voice or typed note) |
| Device observation | `observed` | Sensor, GPS, or app event directly observed |
| Inferred context | `inferred` | Derived from device signals using a model or rule |
| Repeated observation | `repeated` | Observed across multiple sessions in similar conditions |
| Derived knowledge | `derived` | Computed from repeated observations; DKM output |
| Shared field knowledge | `shared` | Received from the shared hazard or field knowledge pool |
| Imported Handoff | `handoff` | Received via authorized Knowledge Handoff from another user |

**[PRINCIPLE]** A knowledge item tagged `derived` must reference the `event_id` values
or `session_id` values of the observations that produced it. A reviewer must be able to
verify that the derivation is supported by at least the claimed evidence count.

**[PRINCIPLE]** Imported Handoff knowledge must remain distinguishable from locally
learned knowledge for the lifetime of the export.

---

## F. User Correction and Disagreement

**[PRINCIPLE]** User corrections are first-class validation evidence.

**[PRINCIPLE]** A correction must not overwrite the original inference record. The
pre-correction state, the correction, and the post-correction state must all be preserved.

### `user_corrections.csv` fields (key)

| Field | Description |
|-------|-------------|
| `correction_id` | Unique correction identifier |
| `session_id` | Session in which the correction occurred |
| `event_id` | The original event being corrected |
| `corrected_event_type` | What type of inference was corrected |
| `original_value` | DFI's original inference |
| `corrected_value` | What the user indicated instead |
| `correction_ts_utc` | UTC timestamp |
| `session_offset_ms` | Session-relative offset |
| `correction_method` | `voice`, `tap`, `form`, `gesture`, or `unknown` |
| `interaction_cost_taps` | Number of taps required if measurable |
| `repeated_same_error` | Boolean — did the same error recur after this correction? |
| `recurrence_count` | Recurrence count if applicable |

**[VALIDATION REQUIREMENT]** The export must preserve enough context to determine whether
DFI repeated the same incorrect inference after a correction was applied. Recurring
corrections on the same pattern are the primary false-positive signal.

---

## G. Interaction Burden

**[PRINCIPLE]** The export must carry enough evidence to estimate driver interaction
burden during delivery. Interaction burden is a product-quality signal only — not a
medical or legal determination of distraction or cognitive load.

### `interaction_events.csv` fields (key)

| Field | Description |
|-------|-------------|
| `event_id` | Unique event identifier |
| `session_id` | Parent session |
| `session_offset_ms` | Monotonic session offset |
| `event_ts_utc` | UTC timestamp |
| `event_type` | `prompt_shown`, `prompt_answered`, `prompt_dismissed`, `prompt_ignored`, `voice_attempt`, `voice_success`, `voice_failure`, `radial_opened`, `radial_selected`, `radial_cancelled`, `alert_suppressed`, `tap_short`, `tap_long_press`, `accidental_activation_candidate` |
| `interaction_channel` | `touch`, `voice`, `auto_dismissed` |
| `response_ms` | Time between prompt display and response (null if no response) |
| `movement_context` | `moving`, `stopped`, `unknown` — estimated at event time |
| `prompt_category` | Category of the prompt |
| `answered` | Boolean |
| `taps_required` | Measured tap count if available |
| `voice_recognition_result` | `success`, `failure`, `timeout`, `unavailable`, or null |
| `accidental_activation` | Boolean candidate flag from gesture log |

**[PRINCIPLE]** Prompts shown with no response must be preserved and clearly distinguishable
from prompts explicitly dismissed.

**[DIRECTION]** Accidental activation candidates come from the C4 gesture event log.
Exact thresholds for classifying an interaction as accidental are **[HYPOTHESIS]** and
must be validated in the field.

**[VALIDATION REQUIREMENT]** Interaction events that occurred while `movement_context =
moving` must be identifiable. A reviewer must be able to assess whether high-burden
interactions disproportionately occurred during vehicle movement.

---

## H. Hazard Memory Validation

Maps to `DFI_HAZARD_MEMORY_MODEL_V1.md`.

The reviewer must be able to reconstruct the full Hazard Memory Loop:

```
voice report
→ Hazard Candidate Window (trajectory + temporal inference)
→ hazard identity / cluster decision
→ evidence state: REPORTED
→ warning issued
→ post-passage verification prompt
→ YES / NO / NO_RESPONSE
→ state update: REPORTED → SUPPORTED / CONFIRMED / DISPUTED
→ state transition log
→ STALE / RETIRED behavior
```

### `hazard_reports.csv` fields (key)

| Field | Description |
|-------|-------------|
| `hazard_candidate_id` | This report's candidate identifier |
| `hazard_id` | Assigned cluster (null if UNRESOLVED) |
| `session_id` | Reporting session |
| `event_id` | Original voice/tap event |
| `report_ts_utc` | UTC timestamp |
| `report_method` | `voice`, `tap`, or `import` |
| `hazard_type` | `POINT_HAZARD` or `SEGMENT_HAZARD` |
| `hazard_category` | e.g., `pothole`, `unpaved_road`, `PHYSICAL_SPEED_HUMP`, `VISUAL_SPEED_HUMP_MARKING` |
| `location_lat_approx` | Approximate latitude (privacy-level dependent; see §L) |
| `location_lon_approx` | Approximate longitude |
| `location_confidence` | `locationConfidence` dimension |
| `existence_confidence` | `hazardExistenceConfidence` dimension |
| `candidate_window_start_offset_ms` | Session-relative start of Hazard Candidate Window |
| `candidate_window_end_offset_ms` | Session-relative end |
| `candidate_window_best_estimate_method` | `temporal_language`, `trajectory`, `sensor_support`, or combination |
| `temporal_language_hint` | Korean phrase contributing to inference (e.g., `방금`, `아까`) |
| `raw_voice_transcript` | Text transcription (subject to §L privacy filter) |
| `normalized_size` | `SMALL`, `MEDIUM`, `LARGE`, `SEVERE`, or null |
| `road_segment_id` | Coarse segment identifier |

### `hazard_verifications.csv` fields (key)

| Field | Description |
|-------|-------------|
| `hazard_verification_id` | Unique identifier |
| `hazard_id` | Parent cluster |
| `hazard_candidate_id` | Specific report being verified |
| `session_id` | Session of the verifying driver |
| `verification_ts_utc` | UTC timestamp |
| `passage_direction` | Travel direction |
| `response` | `YES`, `NO`, or `NO_RESPONSE` |
| `response_ms` | Time to response (null for NO_RESPONSE) |
| `movement_context` | `moving`, `stopped`, or `unknown` |
| `was_warning_shown` | Boolean |
| `warning_distance_m` | Approximate distance at which warning was triggered |
| `independent_evidence` | Boolean — was this passage sufficiently independent of the reporter? |

**[PRINCIPLE]** `NO_RESPONSE` must be a distinct exported value, never collapsed into `NO`.

**[PRINCIPLE]** `independent_evidence` must be preserved. The export must allow a
reviewer to count independent evidence instances separately from total response count,
consistent with the principle in `DFI_HAZARD_MEMORY_MODEL_V1.md §13`:
"Report count is not independent evidence count."

### `hazard_state_transitions.csv` fields

| Field | Description |
|-------|-------------|
| `hazard_id` | Hazard cluster |
| `transition_ts_utc` | UTC timestamp |
| `from_state` | `REPORTED`, `SUPPORTED`, `CONFIRMED`, `STALE`, or `RETIRED` |
| `to_state` | New evidence state |
| `transition_cause` | `yes_verification`, `no_verification`, `time_decay`, `manual_retire`, `repair_report`, `merge`, or `split` |
| `triggering_event_id` | Event that caused the transition |
| `evidence_count_at_transition` | Total responses |
| `independent_evidence_count_at_transition` | Independent responses only |

---

## I. Delivery Knowledge Validation

Maps to `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md`.

The reviewer must be able to compare:

```
Raw Layer 1 evidence (session count, stop frequency)
→ Personal Delivery Baseline (derived distribution)
→ Repeated observations
→ DKM knowledge items (with provenance and supporting evidence references)
→ Knowledge Handoff package
```

### `delivery_baseline.json` structure direction

```json
{
  "export_id": "...",
  "install_id": "...",
  "baseline_computed_at_utc": "...",
  "sessions_used": 12,
  "explicitly_labeled_sessions": 10,
  "inferred_sessions": 2,
  "baseline_dimensions": {
    "typical_start_hour_range": [7, 10],
    "stop_duration_distribution_seconds": { "p25": 45, "p50": 90, "p75": 180 },
    "stop_density_per_km": { "mean": 4.2 },
    "session_duration_distribution_minutes": { "p25": 180, "p50": 240, "p75": 300 }
  },
  "baseline_confidence": "low | medium | high",
  "minimum_sessions_threshold_met": true
}
```

**[HYPOTHESIS]** The minimum session count for a useful baseline is not yet validated.
`minimum_sessions_threshold_met` must be computed against a threshold determined before
the field trial begins (see §S).

### `delivery_knowledge.json` structure direction

```json
{
  "export_id": "...",
  "knowledge_items": [
    {
      "knowledge_item_id": "...",
      "provenance": "derived | repeated | explicit",
      "knowledge_type": "FIELD_FACT | REPEATED_OBSERVATION | PERSONAL_PATTERN",
      "category": "stop_cluster | area_order | entry_pattern | access_caution | ...",
      "description_ko": "...",
      "supporting_session_ids": ["..."],
      "supporting_event_ids": ["..."],
      "observation_count": 7,
      "independent_session_count": 5,
      "first_observed_ts_utc": "...",
      "last_observed_ts_utc": "...",
      "confidence": "low | medium | high"
    }
  ]
}
```

**[PRINCIPLE]** Each knowledge item must reference its supporting evidence. A reviewer
must be able to verify that a `derived` item is backed by the claimed independent
observations. Terminology — `FIELD_FACT`, `REPEATED_OBSERVATION`, `PERSONAL_PATTERN` —
must match `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md §7` exactly.

---

## J. Delivery Terrain and Semantic Compression

**[DIRECTION]** As evidence accumulates, raw delivery events should compress into
meaningful delivery behavior knowledge. Working concept:

**Delivery Terrain** — a delivery area interpreted as a repeated work-behavior space
rather than a collection of address pins.

Possible behavioral node types:

```
ENTRY_NODE        — typical area entry point
STOP_CLUSTER      — cluster of repeated delivery stops
WALK_CLUSTER      — area navigated primarily on foot after parking
BUILDING_ACCESS   — repeated building entry path
RETURN_POINT      — area the driver frequently returns to
EXIT_NODE         — typical area exit
ROUTE_TRANSITION  — handoff between traversal patterns
```

**[DIRECTION]** Semantic Zoom direction:

```
regional view     → delivery knowledge area
zone view         → delivery zone
complex/site view → entry, stopping, exit flow
near view         → building access and walking bundles
field interaction → only currently relevant information
```

**[HYPOTHESIS]** Delivery Terrain and Semantic Zoom are design direction extensions not
yet formally established in current source documents. Field validation must determine
whether Delivery Terrain provides meaningfully better assistance than stop-cluster lists.

**V1 export evidence for Delivery Terrain validation:**

The V1 export preserves the raw and derived evidence that would support Delivery Terrain
analysis — `stop_clusters.csv`, `delivery_knowledge.json` items of category `stop_cluster`
and `entry_pattern`, and session-level traversal sequences. Full Delivery Terrain
rendering is **[FUTURE]**.

---

## K. Carrier Context Overlay

**[DIRECTION]** A physical place or photo should not be duplicated once per carrier.
The preferred model:

```
place evidence / photo  +  carrier-specific operational context
= Carrier Context Overlay
```

Example:

```
Factory A — rear gate access photo (shared physical fact)
  → Carrier X: Coupang receiving at rear gate (repeatedly observed)
  → Carrier Y: uses front gate (different observation)
```

The current user's carrier context should influence information priority:

1. Current carrier context evidence
2. Common delivery evidence
3. Other-carrier-only evidence

**[PRIVACY BOUNDARY]** Do not encode unofficial carrier brand names, logo colors, or
emoji as a core data model. Use stable user-defined carrier context identifiers.

**[DIRECTION]** The V1 export must preserve enough evidence to determine whether
carrier-specific information was correct, whether it became stale, whether the same
physical photo was reused across multiple carrier contexts, and whether carrier context
materially improved usefulness. The `carrier_context` field in `delivery_knowledge.json`
is reserved but not required in V1.

This concept is **[DIRECTION]** — not yet formally established in current source
documents beyond the product vision's carrier context section.

---

## L. Privacy and Sensitive Data Filtering

**[PRIVACY BOUNDARY]** The validation export must not contain:

- raw audio recordings
- exact customer names
- phone numbers
- apartment unit recipient identity
- delivery label images containing personal recipient information
- authentication tokens, API keys, or server credentials
- precise personal home location unrelated to delivery validation

**[PRIVACY BOUNDARY]** Spatial data requires careful handling. Deleting all spatial data
makes Delivery Terrain, stop-cluster, route-breakdown, and hazard analysis impossible.
Define privacy-preserving levels instead.

### Export privacy levels

**LEVEL_1_SUMMARY** — For broad sharing, AI analysis, public review.

Spatial precision: delivery zone (≥ 500 m grid); hazard locations rounded to ≥ 100 m.
No trajectory. Individual stop coordinates excluded. Cluster counts, durations, and
interaction burden statistics retained.

**LEVEL_2_ANALYSIS** — For founder self-review, trusted AI reviewer (Claude, ChatGPT)
with no external sharing.

Spatial precision: stop clusters at ≥ 30 m; hazard locations at ≥ 30 m; simplified
trajectory at road-segment level (not raw GPS samples). Exact customer-facing addresses
excluded beyond road-segment context. DKM items with location context retained.

**LEVEL_3_DEEP_DIVE** — For founder personal analysis only; not shared externally.

Spatial precision: stop cluster centers at full GPS precision; hazard locations at full
GPS precision; simplified trajectory at GPS waypoint level (not raw sample stream).
Raw audio, customer personal data, and delivery label PII excluded.

**[VALIDATION REQUIREMENT]** The `manifest.json` must record which privacy level was
applied. A reviewer must not need to guess whether coordinates have been rounded.

---

## M. Immutable Snapshot Principle

**[PRINCIPLE]** A validation export represents a fixed analysis snapshot. The same ZIP
must produce the same analysis results when reviewed six months later by a different
reviewer.

### `manifest.json` required fields

```json
{
  "export_id": "<uuid>",
  "install_id": "<pseudonymous>",
  "export_schema_version": "1.0",
  "export_created_utc": "2026-08-15T08:30:00Z",
  "export_period_from": "2026-07-15",
  "export_period_to": "2026-08-14",
  "local_timezone": "Asia/Seoul",
  "app_version": "...",
  "data_model_versions": {
    "delivery_knowledge_model": "1",
    "hazard_memory_model": "1"
  },
  "privacy_level": "LEVEL_2_ANALYSIS",
  "export_filters_applied": ["exclude_raw_audio", "round_coordinates_30m"],
  "record_counts": {
    "sessions": 0,
    "context_events": 0,
    "interaction_events": 0,
    "hazard_reports": 0,
    "hazard_verifications": 0,
    "knowledge_items": 0
  },
  "file_checksums": {
    "delivery_sessions.csv": "<sha256>",
    "interaction_events.csv": "<sha256>"
  },
  "missing_data_signals": ["microphone_permission_denied_3_sessions"]
}
```

**[PRINCIPLE]** Per-file SHA-256 checksums must be included. A reviewer must be able to
verify the export has not been modified after generation.

---

## N. Human-Readable Validation Report

**[DIRECTION]** Primary human-readable report format: **HTML**.

Rationale: HTML can be opened in any browser without software installation; it supports
tables, sections, and hyperlinks between summary and detail; it can embed SVG charts; it
is readable by AI systems that accept text; it does not create a proprietary dependency.
PDF may be generated as an optional secondary format for external sharing.

### `validation_report.html` minimum sections

1. Validation period and install context
2. Delivery session count (explicit vs. inferred start)
3. Context inference summary (DELIVERY_LIKELY acceptance/rejection rate)
4. Interaction burden overview (prompts shown, answered, ignored, suppressed)
5. Top 5 most ignored prompt types
6. Top 5 most corrected inference types
7. Recurring false-positive patterns (same inference error after correction)
8. Hazard summary (REPORTED/SUPPORTED/CONFIRMED/STALE/RETIRED counts)
9. Hazard warning and verification summary (YES/NO/NO_RESPONSE rates)
10. Road Condition Summary suppression events (if data available)
11. Route breakdown candidate summary
12. Stop cluster summary
13. Delivery Knowledge emergence (item count by provenance type)
14. Knowledge Handoff candidate assessment (if a Handoff was generated)
15. Data quality warnings (missing signals, offline gaps, failed permissions)

**[PRINCIPLE]** The report is a summary. Machine-readable CSV/JSON files remain the
authoritative source. The report must not replace the structured export; it must guide
the reviewer toward it.

**[PRINCIPLE]** Do not use screenshots as primary evidence. Charts and tables derived
from structured data are preferred.

---

## O. Per-Session Deep-Dive Package

For investigating a specific session in detail:

```
DFI_SESSION_EVIDENCE_<SESSION_ID>_V1.zip
├── session_manifest.json
├── session_timeline.csv
├── trajectory_simplified.json
├── context_timeline.csv
├── interaction_events.csv
├── user_corrections.csv
├── hazard_events.csv
├── voice_event_metadata.csv
└── diagnostics.json
```

**[PRIVACY BOUNDARY]** Raw audio must not be included by default.

**[PRIVACY BOUNDARY]** Trajectory must be at road-segment level in LEVEL_1_SUMMARY and
LEVEL_2_ANALYSIS. Full waypoint-level trajectory is permitted only in LEVEL_3_DEEP_DIVE.

**[DIRECTION]** The per-session package is generated on demand for sessions flagged by
the reviewer or by diagnostic signals (unusually high correction count, route breakdown
candidate, device restart mid-session, offline period > 30 minutes). It is not generated
for every session.

---

## P. Missing-Data Honesty

**[PRINCIPLE]** The export must explicitly document what data is unavailable. A reviewer
must not mistake "no event recorded" for "no event occurred."

`app_diagnostics.json` must report:

| Signal | Description |
|--------|-------------|
| `permission_denied` | Which permissions were denied and in which sessions |
| `offline_periods` | Sessions or periods with no network connectivity |
| `sync_backlog` | Events written locally but not yet confirmed synced |
| `dropped_events` | Events lost due to buffer overflow or process kill |
| `process_kills` | App termination events where detectable |
| `failed_voice_recognitions` | Total failures and error codes |
| `unsupported_sensors` | Sensors absent on this device |
| `export_filters` | What was excluded and why |
| `incomplete_sessions` | Sessions without a clean session_end event |
| `schema_unknown_fields` | Fields present in data but not recognized by this schema version |

**[PRINCIPLE]** Offline periods must appear as explicit gap events in
`offline_sync_events.csv`, not as silent absences in the event stream.

---

## Q. Schema Evolution

**[PRINCIPLE]** The field validation period may cross app version updates. The export
schema must survive these updates gracefully.

**[DIRECTION]** Versioning approach:

- Each export carries `export_schema_version` in `manifest.json`.
- CSV files carry column headers in row 1. Parsers must use header names, not column
  position.
- JSON files use explicit field names. Parsers must tolerate unknown fields
  (forward-compatible) and absent optional fields (backward-compatible).
- A `SCHEMA_CHANGELOG.md` file is bundled inside the export ZIP to document fields
  deprecated between schema versions.
- App version and export schema version are separate. An app update does not
  automatically increment the schema version unless exported fields change.

**[DIRECTION]** V1 export schema is not required to support full migration of historical
exports to V2 format. Cross-version analysis should align on common fields present in
both schema versions.

**[FUTURE]** A formal schema migration framework is out of scope for field validation V1.

---

## R. Validation Metrics Boundary

**[PRINCIPLE]** Do not turn every count into a success metric.

| Category | Examples |
|----------|---------|
| Observation count | Number of hazard reports, number of delivery sessions |
| Quality indicator | Correction rate, false-positive recurrence rate, NO_RESPONSE rate |
| Product hypothesis | "High verification response rate indicates the timing model works" |
| Acceptance criterion | To be defined before the field trial begins |

**[PRINCIPLE]** A high number of hazard reports is not automatically a success signal.
It may indicate excessive false-positive prompts.

**[PRINCIPLE]** A high number of user interactions is not automatically engagement
success. It may indicate excessive interruption burden.

**[PRINCIPLE]** A high number of Delivery Knowledge items is not automatically useful
knowledge. Each item must be traceable to independent observations. A knowledge item
supported by one session is not equivalent to one supported by thirty.

**[PRINCIPLE]** Separate observation counts from quality indicators from product
hypotheses from acceptance criteria. Do not present unvalidated thresholds as confirmed
benchmarks.

Mapping to `DFI_FIELD_VALIDATION_PROTOCOL_V1_1.md` core metrics:

| Protocol metric | Export evidence |
|----------------|----------------|
| Severe destination-search events | `route_breakdown_candidates.csv` event type |
| Wrong-entry events | `route_breakdown_candidates.csv` |
| Repeated local revisits | `route_breakdown_candidates.csv` |
| DFI tip views | `interaction_events.csv event_type = tip_viewed` |
| Tip-assisted resolution | `interaction_events.csv` + `user_corrections.csv` |
| False-positive intervention rate | `false_positive_feedback.csv` |
| Micro-prompt response rate | `interaction_events.csv answered / shown` |
| AI correction burden | `user_corrections.csv` count and recurrence |
| Offline capture and sync success | `offline_sync_events.csv` |

---

## S. Unresolved Questions Before Field Trial

**[VALIDATION REQUIREMENT]** The following questions must be answered or explicitly
accepted as unknowns before a one-month field trial begins.

| Question | Why it blocks the trial |
|----------|------------------------|
| Minimum local event retention before export generation | Without sufficient history, the baseline is too thin to compute |
| Export generation: on-device vs. server-assisted | On-device avoids raw GPS leaving the device; server-assisted enables richer aggregation |
| Acceptable export ZIP size for a one-month period | Determines spatial precision budgets and event sampling rates |
| Spatial precision by privacy level | Must be decided before the first export; cannot be changed retrospectively without invalidating historical exports |
| Whether `install_id` should remain stable across exports from the same device | Cross-export longitudinal analysis requires stability; privacy design requires review |
| Whether validation exports should include server-side aggregated shared hazard knowledge | Shared knowledge is a separate logical domain; including it blurs locally-learned vs. shared provenance |
| How imported Knowledge Handoff evidence is separated from locally learned knowledge | Required by provenance model; must be enforced at collection time, not at export time |
| Whether photos are embedded, hashed, or referenced only | Embedding increases ZIP size significantly; hashes enable deduplication; references require external storage access |
| How failed or incomplete exports are detected | An export that terminates early must not be mistaken for a complete export |
| What minimum dataset confirms the export pipeline is working | A dry run on 2–3 sessions should be completed and reviewed before committing to a full validation period |
| Minimum Personal Delivery Baseline session count before `delivery_baseline.json` is meaningful | Open question in `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md §14` |

---

## Cross-References

- `DFI_PRODUCT_VISION_V2.md` — core product principles, Experience Mining, Field Trust, carrier context, vehicle-class knowledge
- `DFI_PRODUCT_VISION_V2_1_ADDENDUM.md` — Route Breakdown Event, Personal Behavior Baseline, offline capture, risk contribution allocation
- `DFI_FIELD_VALIDATION_PROTOCOL_V1_1.md` — core validation metrics, Phase A/B, outcome feedback labels, false-positive feedback types
- `DFI_HAZARD_MEMORY_MODEL_V1.md` — Hazard Memory Loop, evidence states, Hazard Candidate Window, reporter reliability
- `DFI_DELIVERY_KNOWLEDGE_MODEL_V1.md` — Personal Delivery Baseline, DKM, provenance types, Knowledge Handoff, data layers
