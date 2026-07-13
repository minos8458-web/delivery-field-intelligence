import type { MemoryType, StructuredDraft } from './types';

const TYPE_RULES: Array<{ type: MemoryType; keywords: string[] }> = [
  { type: 'PARKING', keywords: ['주차', '차 세우', '정차'] },
  { type: 'BUILDING_ACCESS', keywords: ['공동현관', '출입', '비밀번호', '호출', '경비실'] },
  { type: 'ACCESS_ROUTE', keywords: ['후문', '정문', '지하주차장', '진입', '들어가', '입구', '길'] },
  { type: 'DELIVERY_ORDER', keywords: ['다음에', '먼저', '순서', '돌면', '동선'] },
  { type: 'CUSTOMER_REQUEST', keywords: ['요청', '문 앞', '경비실 보관', '연락', '전화'] },
  { type: 'ELEVATOR', keywords: ['엘리베이터', '승강기'] },
  { type: 'LOAD_HANDLING', keywords: ['카트', '생수', '무거', '짐', '상차', '하차'] },
  { type: 'TIME_WINDOW', keywords: ['오전', '오후', '시에는', '시간대', '막힌다'] },
];

const LOCATION_PATTERN = /((?:[가-힣A-Za-z0-9]+\s*){1,5}(?:아파트|마을|단지|빌라|오피스텔)?\s*\d{1,4}(?:동|호)?)/;

function inferType(text: string): MemoryType {
  const normalized = text.toLowerCase();
  for (const rule of TYPE_RULES) {
    if (rule.keywords.some((keyword) => normalized.includes(keyword.toLowerCase()))) {
      return rule.type;
    }
  }
  return 'OTHER';
}

function inferLocation(text: string): string {
  const explicit = text.match(/(?:위치|배송지)\s*[:：]\s*([^,。.!\n]+)/)?.[1]?.trim();
  if (explicit) return explicit;

  const match = text.match(LOCATION_PATTERN)?.[1]?.trim();
  return match ?? '';
}

function splitReason(text: string): { recommendation: string; reason: string } {
  const connectors = [' 때문에 ', ' 이유는 ', ' 왜냐하면 ', ' 해서 ', '하면 ', '면 '];
  for (const connector of connectors) {
    const index = text.indexOf(connector);
    if (index > 0) {
      const before = text.slice(0, index).trim();
      const after = text.slice(index + connector.length).trim();
      if (before && after) {
        return { recommendation: before, reason: after };
      }
    }
  }

  const causal = text.match(/(.+?)(?:하면|해서)\s*(.+)/);
  if (causal) {
    return { recommendation: causal[1].trim(), reason: causal[2].trim() };
  }

  return { recommendation: text.trim(), reason: '' };
}

function buildTags(text: string, type: MemoryType): string[] {
  const candidates = ['후문', '정문', '지하주차장', '경비실', '카트', '엘리베이터', '생수', '공동현관'];
  const tags = candidates.filter((candidate) => text.includes(candidate));
  return [...new Set([type, ...tags])];
}

export function structureFieldMemory(text: string): StructuredDraft {
  const trimmed = text.trim();
  if (!trimmed) {
    throw new Error('현장 메모를 입력하세요.');
  }

  const memoryType = inferType(trimmed);
  const location = inferLocation(trimmed);
  const withoutLocation = location ? trimmed.replace(location, '').replace(/^[은는이가\s,]+/, '').trim() : trimmed;
  const { recommendation, reason } = splitReason(withoutLocation || trimmed);

  return {
    location,
    memoryType,
    recommendation,
    reason,
    tags: buildTags(trimmed, memoryType),
  };
}
