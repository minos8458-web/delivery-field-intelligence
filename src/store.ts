import type { FieldMemory, RetrievalFeedback, ValidationSession } from './types';

const MEMORY_STORAGE_KEY = 'dfi.fieldMemories.v1';
const FEEDBACK_STORAGE_KEY = 'dfi.retrievalFeedback.v1';
const SESSION_STORAGE_KEY = 'dfi.validationSessions.v1';
const DRAFT_STORAGE_KEY = 'dfi.captureDraft.v1';

function loadArray<T>(key: string): T[] {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as unknown;
    return Array.isArray(parsed) ? (parsed as T[]) : [];
  } catch {
    return [];
  }
}

export function loadMemories(): FieldMemory[] {
  return loadArray<FieldMemory>(MEMORY_STORAGE_KEY);
}

export function saveMemories(memories: FieldMemory[]): void {
  localStorage.setItem(MEMORY_STORAGE_KEY, JSON.stringify(memories));
}

export function loadFeedback(): RetrievalFeedback[] {
  return loadArray<RetrievalFeedback>(FEEDBACK_STORAGE_KEY);
}

export function saveFeedback(feedback: RetrievalFeedback[]): void {
  localStorage.setItem(FEEDBACK_STORAGE_KEY, JSON.stringify(feedback));
}

export function loadSessions(): ValidationSession[] {
  return loadArray<ValidationSession>(SESSION_STORAGE_KEY);
}

export function saveSessions(sessions: ValidationSession[]): void {
  localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(sessions));
}

export function loadCaptureDraft(): string {
  return localStorage.getItem(DRAFT_STORAGE_KEY) ?? '';
}

export function saveCaptureDraft(value: string): void {
  if (value.trim()) localStorage.setItem(DRAFT_STORAGE_KEY, value);
  else localStorage.removeItem(DRAFT_STORAGE_KEY);
}

export function clearAllValidationData(): void {
  localStorage.removeItem(MEMORY_STORAGE_KEY);
  localStorage.removeItem(FEEDBACK_STORAGE_KEY);
  localStorage.removeItem(SESSION_STORAGE_KEY);
  localStorage.removeItem(DRAFT_STORAGE_KEY);
}
