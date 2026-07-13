import assert from 'node:assert/strict';
import test from 'node:test';
import { readFile } from 'node:fs/promises';
import ts from 'typescript';
import vm from 'node:vm';

async function loadValidationModule() {
  const source = await readFile(new URL('../src/validationMetrics.ts', import.meta.url), 'utf8');
  const js = ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 },
  }).outputText;
  const module = { exports: {} };
  vm.runInNewContext(`(function(exports,module){${js}\n})(module.exports,module);`, { module });
  return module.exports;
}

const memory = (id, durationMs, corrections = []) => ({
  id,
  rawText: '테스트',
  location: '장소',
  memoryType: 'OTHER',
  recommendation: '행동',
  reason: '',
  tags: [],
  source: 'DRIVER_EXPERIENCE',
  confirmationState: 'USER_CONFIRMED',
  validation: durationMs === null ? undefined : {
    captureStartedAt: '2026-07-13T00:00:00.000Z',
    structuredAt: '2026-07-13T00:00:01.000Z',
    confirmedAt: '2026-07-13T00:00:02.000Z',
    captureDurationMs: durationMs,
    corrections,
  },
  createdAt: '2026-07-13T00:00:02.000Z',
  updatedAt: '2026-07-13T00:00:02.000Z',
});

test('calculates median capture time and zero-correction rate', async () => {
  const { calculateValidationMetrics } = await loadValidationModule();
  const result = calculateValidationMetrics([
    memory('a', 10000),
    memory('b', 20000, [{ field: 'location', before: 'A', after: 'B' }]),
    memory('c', 30000),
  ], []);
  assert.equal(result.medianCaptureSeconds, 20);
  assert.equal(result.acceptedWithoutCorrectionRate, 2 / 3);
});

test('calculates useful retrieval feedback rate', async () => {
  const { calculateValidationMetrics } = await loadValidationModule();
  const result = calculateValidationMetrics([memory('a', 10000)], [
    { id: '1', memoryId: 'a', outcome: 'REDUCED_RECALL_TIME', recordedAt: 'x' },
    { id: '2', memoryId: 'a', outcome: 'NO_IMPACT', recordedAt: 'x' },
  ]);
  assert.equal(result.usefulFeedbackRate, 0.5);
});

test('CSV export contains measurement and feedback columns', async () => {
  const { buildValidationCsv } = await loadValidationModule();
  const csv = buildValidationCsv([memory('a', 15000)], [
    { id: '1', memoryId: 'a', outcome: 'AVOIDED_REENTRY', recordedAt: 'x' },
  ]);
  assert.match(csv, /capture_duration_ms/);
  assert.match(csv, /useful_feedback_count/);
  assert.match(csv, /AVOIDED_REENTRY/);
});
