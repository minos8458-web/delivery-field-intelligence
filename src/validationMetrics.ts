import type { FieldMemory, RetrievalFeedback } from './types';

export interface ValidationMetrics {
  confirmedMemories: number;
  measuredCaptures: number;
  medianCaptureSeconds: number | null;
  acceptedWithoutCorrectionRate: number | null;
  totalFeedback: number;
  usefulFeedbackRate: number | null;
}

function median(values: number[]): number | null {
  if (values.length === 0) return null;
  const sorted = [...values].sort((a, b) => a - b);
  const middle = Math.floor(sorted.length / 2);
  if (sorted.length % 2 === 1) return sorted[middle];
  return (sorted[middle - 1] + sorted[middle]) / 2;
}

export function calculateValidationMetrics(
  memories: FieldMemory[],
  feedback: RetrievalFeedback[],
): ValidationMetrics {
  const measured = memories.filter((memory) => memory.validation);
  const durations = measured.map((memory) => memory.validation!.captureDurationMs / 1000);
  const zeroCorrection = measured.filter((memory) => memory.validation!.corrections.length === 0).length;
  const usefulFeedback = feedback.filter((item) => item.outcome !== 'NO_IMPACT').length;

  return {
    confirmedMemories: memories.length,
    measuredCaptures: measured.length,
    medianCaptureSeconds: median(durations),
    acceptedWithoutCorrectionRate: measured.length === 0 ? null : zeroCorrection / measured.length,
    totalFeedback: feedback.length,
    usefulFeedbackRate: feedback.length === 0 ? null : usefulFeedback / feedback.length,
  };
}

function csvEscape(value: unknown): string {
  const text = String(value ?? '');
  return `"${text.replaceAll('"', '""')}"`;
}

export function buildValidationCsv(memories: FieldMemory[], feedback: RetrievalFeedback[]): string {
  const feedbackByMemory = new Map<string, RetrievalFeedback[]>();
  feedback.forEach((item) => {
    const current = feedbackByMemory.get(item.memoryId) ?? [];
    current.push(item);
    feedbackByMemory.set(item.memoryId, current);
  });

  const header = [
    'memory_id',
    'created_at',
    'location',
    'memory_type',
    'raw_text',
    'recommendation',
    'reason',
    'capture_duration_ms',
    'correction_count',
    'corrected_fields',
    'feedback_count',
    'useful_feedback_count',
    'outcomes',
  ];

  const rows = memories.map((memory) => {
    const memoryFeedback = feedbackByMemory.get(memory.id) ?? [];
    const corrections = memory.validation?.corrections ?? [];
    return [
      memory.id,
      memory.createdAt,
      memory.location,
      memory.memoryType,
      memory.rawText,
      memory.recommendation,
      memory.reason,
      memory.validation?.captureDurationMs ?? '',
      corrections.length,
      corrections.map((item) => item.field).join('|'),
      memoryFeedback.length,
      memoryFeedback.filter((item) => item.outcome !== 'NO_IMPACT').length,
      memoryFeedback.map((item) => item.outcome).join('|'),
    ].map(csvEscape).join(',');
  });

  return [header.map(csvEscape).join(','), ...rows].join('\n');
}
