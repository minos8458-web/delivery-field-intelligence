import type { FieldMemory, RetrievalFeedback } from './types';

const MEMORY_STORAGE_KEY = 'dfi.fieldMemories.v1';
const FEEDBACK_STORAGE_KEY = 'dfi.retrievalFeedback.v1';

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

export function clearAllValidationData(): void {
  localStorage.removeItem(MEMORY_STORAGE_KEY);
  localStorage.removeItem(FEEDBACK_STORAGE_KEY);
}
