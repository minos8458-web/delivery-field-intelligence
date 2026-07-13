import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';

// Source-contract tests intentionally avoid transpiler dependency in the MVP package.
const source = fs.readFileSync(new URL('../src/fieldMemoryParser.ts', import.meta.url), 'utf8');

test('parser defines the main access-route rule', () => {
  assert.match(source, /ACCESS_ROUTE/);
  assert.match(source, /지하주차장/);
});

test('parser rejects empty field notes', () => {
  assert.match(source, /현장 메모를 입력하세요/);
});

test('parser emits schema-shaped draft fields', () => {
  for (const field of ['location', 'memoryType', 'recommendation', 'reason', 'tags']) {
    assert.match(source, new RegExp(`\\b${field}\\b`));
  }
});
