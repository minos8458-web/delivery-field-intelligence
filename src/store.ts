import type { FieldMemory } from './types';

const STORAGE_KEY = 'dfi.fieldMemories.v1';

export function loadMemories(): FieldMemory[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as unknown;
    return Array.isArray(parsed) ? (parsed as FieldMemory[]) : [];
  } catch {
    return [];
  }
}

export function saveMemories(memories: FieldMemory[]): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(memories));
}

export function clearMemories(): void {
  localStorage.removeItem(STORAGE_KEY);
}
