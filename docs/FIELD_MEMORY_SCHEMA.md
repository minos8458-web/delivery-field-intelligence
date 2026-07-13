# Field Memory Schema v0.1

## Entity: FieldMemory

| Field | Type | Purpose |
|---|---|---|
| id | UUID string | Local unique identifier |
| rawText | string | Original driver observation |
| location | string | Human-readable field context/location |
| memoryType | enum | Operational category |
| recommendation | string | Reusable action or observation |
| reason | string | Why the action matters |
| tags | string[] | Retrieval helpers |
| source | DRIVER_EXPERIENCE | Provenance |
| confirmationState | AI_DRAFT / USER_CONFIRMED | Trust gate |
| createdAt | ISO timestamp | Creation time |
| updatedAt | ISO timestamp | Last modification time |

## Memory types

- ACCESS_ROUTE
- PARKING
- BUILDING_ACCESS
- DELIVERY_ORDER
- CUSTOMER_REQUEST
- ELEVATOR
- LOAD_HANDLING
- TIME_WINDOW
- OTHER

## Trust rule

AI_DRAFT is not field knowledge.

Only USER_CONFIRMED records are allowed into the durable memory store in MVP v0.1.

## Privacy boundary

MVP field-memory records should describe operational context, not identify individual customers. Avoid customer names, phone numbers, apartment door codes, and unnecessary precise personal details.
