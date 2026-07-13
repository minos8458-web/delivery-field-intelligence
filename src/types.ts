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
