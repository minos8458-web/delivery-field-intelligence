DFI PRODUCT VISION v2
Last updated: 2026-07-14 Status: Product direction draft for field validation
Product definition
Delivery Field Intelligence (DFI) converts fragmented driver experience and repeated field behavior into privacy-safe, verifiable, reusable spatial knowledge.
Driver experience -> low-friction capture -> AI structuring -> human confirmation -> cross-context validation -> shared field knowledge
DFI does not replace carrier apps, copy carrier-specific operating rules, or begin as a generic route optimizer.
Product principles
Common field knowledge over carrier-specific know-how. Extract physical access, spatial hazards, vehicle constraints, facility usability, location ambiguity, and other cross-carrier field facts.
Facts over ratings. Ask what vehicle was serviced, what work was performed, what cost was reported, and when it was confirmed. Avoid abstract star-rating logic.
AI supports decisions but does not invent authority. AI may detect, structure, compare, and ask. Official carrier procedures and human confirmation remain authoritative.
Privacy-safe spatial knowledge. Customer names, phone numbers, order patterns, and private complaints are excluded. Personal context should be transformed into reusable spatial facts where safe.
Low-friction contribution. The driver speaks naturally; AI creates structure; the driver confirms.
Core intelligence engines
Experience Mining
Detect repeated detours, rear-entrance use, pre-entry parking, vehicle-class-specific avoidance, hesitation, and revisits. Behavior creates a question candidate, not an automatic explanation.
EER - Experienced Expert Routing
Route unresolved field questions to users likely to know the place based on visits, route exposure, vehicle class, carrier context, recency, and Field Trust.
Address Anomaly Detection
Detect inconsistencies among address, parcel/lot address, building name, map pin, and observed location patterns. AAD is decision support, not a delivery-location authority.
Cross-context intersection extraction
Distinguish personal patterns, carrier-context patterns, vehicle-class patterns, and broadly repeated physical-space facts. DFI must not average all carrier routes together.
Safety Transformation
Screen text, speech, images, and comments for personal information, private complaints, unsupported accusations, and unsafe operational claims. Block, redact, or transform before publication.
Field Trace - 5-second route summary
DFI should provide in-app route capture. The user records a short confusing access path without configuring timelapse settings.
Default output goal: a route explanation understandable in about five seconds.
Compress long straight movement, repeated scenery, stationary sections, severe shake, and redundant movement. Preserve forks, turns, entrances, stairs, slopes, vehicle restrictions, landmarks, and driver-emphasized moments.
If five seconds is insufficient, provide a five-second key summary plus an optional longer trace.
The goal is semantic route compression, not fixed-speed timelapse.
Spatial entity model
Delivery Location
Confusing entrances, rear access, stairs, cart restrictions, poor lighting, address/pin mismatch reports, and physical placement hazards.
Driver Utility POI
Public/open toilets, rest areas, and drowsy-driving rest areas. DFI adds driver-specific experience to external POI data instead of rebuilding basic place databases.
Energy POI
Fuel stations and EV charging stations. External data may supply location, fuel price, charger location, and charger status. DFI adds vehicle access, route-deviation cost, stopping convenience, and recent driver confirmation.
Vehicle Service POI
General repair; tire and wheel; roadside/emergency repair; specialty body and box-truck repair; refrigeration units; lift equipment; medium/heavy commercial vehicle service; electric commercial vehicle service.
Tire and wheel is a first-class category, not merely a generic repair tag.
Vehicle-aware field knowledge
The data model must not assume every driver uses a 1-ton truck.
Vehicle profiles may include class, height, width, length, and optional weight-related constraints.
Knowledge may state: - 1-ton access confirmed - 2.5-ton access confirmed - 5-ton turning difficulty reported - height above 3.2 m requires caution - large-truck service confirmed
Official restrictions and community field reports must remain separate.
Vehicle service data philosophy
DFI prioritizes observed service facts over star ratings.
Useful facts include vehicle classes actually serviced, bay/access suitability, work categories performed, roadside service experience, waiting-time reports, paid-price reports, receipt-supported reports, recent tire-size stock observations, and night/holiday service experience.
Prices are reported experience, not guaranteed official prices. Tire inventory observations must be time-stamped and never presented as current-stock guarantees.
Carrier context
Users may select a carrier or transport context. Carrier badges can make cross-carrier participation visible.
Self-selected carrier context is not employment verification. Carrier identity and verified affiliation are separate concepts.
Carrier context is also an analytical signal for separating carrier-specific patterns from common spatial facts.
Trust and contribution
Field Trust
Reliability signal based on validation history. It cannot be purchased or transferred.
Contribution Credit
Measures useful contribution to shared knowledge. Reward validated usefulness, not posting volume.
Possible long-term partner benefits include vehicle maintenance, engine-oil, convenience-store, car-wash, tire, and other commercial-driver benefits. Cash-like rewards require legal, tax, accounting, fraud, and terms review.
Initial field validation
The founder's route is the first validation environment because it has many parcel/lot-address deliveries, long and physically demanding movement, frequent new/substitute-driver difficulty, five founder delivery days per week, and substitute drivers on remaining days.
Do not document every destination. Capture only genuinely difficult or repeatedly confusing places first.
Primary validation question:
Can accumulated expert field knowledge reduce the adaptation cost of a substitute or new driver?
Measure address-search failures, location-confusion calls, wrong entries, revisits, cases spending over three minutes locating a destination, DFI-tip-assisted resolutions, calls to the experienced route owner, capture abandonment, AI correction burden, and voluntary repeat use.
Product moat hypothesis
Individual features can be copied. DFI's defensibility must come from its feedback loop and cross-context field knowledge graph.
Field behavior -> anomaly detection -> experienced-user routing -> factual confirmation -> privacy-safe knowledge -> reuse -> new field evidence
The long-term advantage is identifying physical spatial facts repeated across parcel delivery, independent delivery, light commercial vehicles, call freight, medium trucks, and heavy trucks.
MVP boundary
The current MVP v0.3 remains a field-memory validation prototype.
Current priority:
Install -> Start Session -> Capture -> Confirm -> Reuse -> Outcome Feedback -> Export Evidence
Do not immediately implement every feature in this vision.
Near-term validation focuses on low-friction capture, useful recall, correction burden, founder-route validation, and substitute/new-driver usefulness.
Architecture requirements or staged expansion areas include floating Android overlay, background location, Experience Mining, EER, AAD, shared multi-driver knowledge, Field Trace, external POI integration, fuel/charging data, commercial vehicle service POI, and contribution credits.
Claude-deferred engineering work
Keep these in the focused engineering queue: - production AI provider abstraction and adapter - evaluation harness and prompt optimization - mobile architecture ADR - Android overlay feasibility - background location and battery strategy - CameraX route capture feasibility - five-second Field Trace media pipeline - privacy screening pipeline design
Do not begin large-scale backend architecture, full route optimization, carrier scraping, or payment systems before field validation justifies them.
Decision rule
Founder field observation -> product hypothesis -> critical review -> smallest implementation -> field evidence -> keep, revise, or delete
Founder intuition discovers problems. AI challenges and structures hypotheses. Field evidence decides.