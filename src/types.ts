export type MemoryType =
  | 'ACCESS_ROUTE'
  | 'PARKING'
  | 'BUILDING_ACCESS'
  | 'DELIVERY_ORDER'
  | 'CUSTOMER_REQUEST'
  | 'ELEVATOR'
  | 'LOAD_HANDLING'
  | 'TIME_WINDOW'
  | 'OTHER';

export type ConfirmationState = 'AI_DRAFT' | 'USER_CONFIRMED';
export type DraftField = 'location' | 'memoryType' | 'recommendation' | 'reason';

export interface FieldCorrection {
  field: DraftField;
  before: string;
  after: string;
}

export interface CaptureValidation {
  captureStartedAt: string;
  structuredAt: string;
  confirmedAt: string;
  captureDurationMs: number;
  corrections: FieldCorrection[];
}

export interface FieldMemory {
  id: string;
  rawText: string;
  location: string;
  memoryType: MemoryType;
  recommendation: string;
  reason: string;
  tags: string[];
  source: 'DRIVER_EXPERIENCE';
  confirmationState: ConfirmationState;
  validation?: CaptureValidation;
  createdAt: string;
  updatedAt: string;
}

export interface StructuredDraft {
  location: string;
  memoryType: MemoryType;
  recommendation: string;
  reason: string;
  tags: string[];
}

export type OperationalOutcome =
  | 'AVOIDED_REENTRY'
  | 'AVOIDED_WALKING'
  | 'AVOIDED_PARKING_MISTAKE'
  | 'REDUCED_RECALL_TIME'
  | 'CHANGED_DELIVERY_ORDER'
  | 'PREVENTED_REPEAT_ERROR'
  | 'USEFUL_OTHER'
  | 'NO_IMPACT';

export interface RetrievalFeedback {
  id: string;
  memoryId: string;
  outcome: OperationalOutcome;
  recordedAt: string;
}
