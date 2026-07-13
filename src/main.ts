import './style.css';
import { structureFieldMemory } from './fieldMemoryParser';
import { clearAllValidationData, loadCaptureDraft, loadFeedback, loadMemories, loadSessions, saveCaptureDraft, saveFeedback, saveMemories, saveSessions } from './store';
import { buildValidationCsv, calculateValidationMetrics } from './validationMetrics';
import type {
  DraftField,
  FieldCorrection,
  FieldMemory,
  MemoryType,
  OperationalOutcome,
  RetrievalFeedback,
  StructuredDraft,
  ValidationSession,
} from './types';

const TYPE_LABELS: Record<MemoryType, string> = {
  ACCESS_ROUTE: '진입 경로',
  PARKING: '주차/정차',
  BUILDING_ACCESS: '건물 출입',
  DELIVERY_ORDER: '배송 순서',
  CUSTOMER_REQUEST: '고객 요청',
  ELEVATOR: '엘리베이터',
  LOAD_HANDLING: '짐 처리',
  TIME_WINDOW: '시간대',
  OTHER: '기타',
};

const OUTCOME_LABELS: Record<OperationalOutcome, string> = {
  AVOIDED_REENTRY: '재진입 방지',
  AVOIDED_WALKING: '불필요한 보행 방지',
  AVOIDED_PARKING_MISTAKE: '주차 실수 방지',
  REDUCED_RECALL_TIME: '기억·탐색 시간 단축',
  CHANGED_DELIVERY_ORDER: '배송 순서 변경',
  PREVENTED_REPEAT_ERROR: '반복 실수 방지',
  USEFUL_OTHER: '기타 도움',
  NO_IMPACT: '영향 없음',
};

let memories = loadMemories();
let feedback = loadFeedback();
let sessions = loadSessions();
let activeSession: ValidationSession | null = sessions.find((session) => !session.endedAt) ?? null;
let currentDraft: StructuredDraft | null = null;
let currentRawText = '';
let captureStartedAt: number | null = null;
let structuredAt: number | null = null;

const app = document.querySelector<HTMLDivElement>('#app');
if (!app) throw new Error('App root not found');

app.innerHTML = `
  <main class="shell">
    <header class="hero">
      <p class="eyebrow">DELIVERY FIELD INTELLIGENCE · MVP v0.2</p>
      <h1>기사의 경험을<br />현장 데이터로 바꾼다.</h1>
      <p class="hero-copy">현장 기억을 수집·구조화·재사용하고, 실제로 도움이 됐는지 측정하는 실증용 MVP입니다.</p>
      <div class="session-strip">
        <div><span class="session-label">FIELD SESSION</span><strong id="sessionStatus">세션 미시작</strong></div>
        <button id="sessionButton" class="ghost-button session-button" type="button">실증 세션 시작</button>
      </div>
    </header>

    <section class="panel capture-panel">
      <div class="section-heading">
        <div><span class="step">01</span><h2>현장 메모 기록</h2></div>
        <button id="voiceButton" class="ghost-button" type="button">🎙 음성 입력</button>
      </div>
      <textarea id="rawText" rows="5" placeholder="예: 탄현 9단지 903동은 지하주차장으로 들어가면 카트 이동이 빠름"></textarea>
      <div class="action-row">
        <span id="statusText" class="status-text">개인정보는 최소한으로 입력하세요.</span>
        <button id="structureButton" class="primary-button" type="button">구조화 초안 만들기</button>
      </div>
    </section>

    <section id="draftPanel" class="panel draft-panel hidden">
      <div class="section-heading">
        <div><span class="step">02</span><h2>구조화 결과 확인</h2></div>
        <span class="draft-badge">STRUCTURED DRAFT</span>
      </div>
      <div class="form-grid">
        <label>위치<input id="locationInput" /></label>
        <label>기억 유형<select id="typeInput"></select></label>
        <label class="wide">권장 행동<input id="recommendationInput" /></label>
        <label class="wide">이유/효과<input id="reasonInput" /></label>
      </div>
      <div class="action-row">
        <span class="status-text">수정 내용과 확정까지 걸린 시간은 실증 지표로 기록됩니다.</span>
        <button id="confirmButton" class="primary-button" type="button">확인하고 저장</button>
      </div>
    </section>

    <section class="panel memory-panel">
      <div class="section-heading">
        <div><span class="step">03</span><h2>현장 기억 검색·활용</h2></div>
        <button id="clearButton" class="ghost-button danger" type="button">전체 삭제</button>
      </div>
      <input id="searchInput" class="search-input" placeholder="장소, 행동, 이유로 검색" />
      <div id="memoryStats" class="memory-stats"></div>
      <div id="memoryList" class="memory-list"></div>
    </section>

    <section class="panel validation-panel">
      <div class="section-heading">
        <div><span class="step">04</span><h2>현장 실증 지표</h2></div>
        <span class="validation-badge">FIELD VALIDATION</span>
      </div>
      <p class="panel-copy">추정 절감률이 아니라 실제 사용 로그를 쌓습니다. 지원사업 제출 전 익명화·검토가 필요합니다.</p>
      <div id="metricGrid" class="metric-grid"></div>
      <div class="export-row">
        <button id="exportJsonButton" class="ghost-button" type="button">JSON 내보내기</button>
        <button id="exportCsvButton" class="ghost-button" type="button">CSV 내보내기</button>
      </div>
    </section>
  </main>
`;

const rawText = document.querySelector<HTMLTextAreaElement>('#rawText')!;
const structureButton = document.querySelector<HTMLButtonElement>('#structureButton')!;
const draftPanel = document.querySelector<HTMLElement>('#draftPanel')!;
const locationInput = document.querySelector<HTMLInputElement>('#locationInput')!;
const typeInput = document.querySelector<HTMLSelectElement>('#typeInput')!;
const recommendationInput = document.querySelector<HTMLInputElement>('#recommendationInput')!;
const reasonInput = document.querySelector<HTMLInputElement>('#reasonInput')!;
const confirmButton = document.querySelector<HTMLButtonElement>('#confirmButton')!;
const searchInput = document.querySelector<HTMLInputElement>('#searchInput')!;
const memoryList = document.querySelector<HTMLDivElement>('#memoryList')!;
const memoryStats = document.querySelector<HTMLDivElement>('#memoryStats')!;
const clearButton = document.querySelector<HTMLButtonElement>('#clearButton')!;
const voiceButton = document.querySelector<HTMLButtonElement>('#voiceButton')!;
const statusText = document.querySelector<HTMLSpanElement>('#statusText')!;
const metricGrid = document.querySelector<HTMLDivElement>('#metricGrid')!;
const exportJsonButton = document.querySelector<HTMLButtonElement>('#exportJsonButton')!;
const exportCsvButton = document.querySelector<HTMLButtonElement>('#exportCsvButton')!;
const sessionStatus = document.querySelector<HTMLElement>('#sessionStatus')!;
const sessionButton = document.querySelector<HTMLButtonElement>('#sessionButton')!;

Object.entries(TYPE_LABELS).forEach(([value, label]) => {
  const option = document.createElement('option');
  option.value = value;
  option.textContent = `${label} · ${value}`;
  typeInput.append(option);
});

function renderSession(): void {
  if (!activeSession) {
    sessionStatus.textContent = '세션 미시작';
    sessionButton.textContent = '실증 세션 시작';
    return;
  }
  const started = new Date(activeSession.startedAt);
  sessionStatus.textContent = `${started.toLocaleDateString('ko-KR')} ${started.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })} 시작`;
  sessionButton.textContent = '실증 세션 종료';
}

function startCaptureClock(): void {
  if (captureStartedAt === null && rawText.value.trim()) captureStartedAt = Date.now();
}

function fillDraft(draft: StructuredDraft): void {
  locationInput.value = draft.location;
  typeInput.value = draft.memoryType;
  recommendationInput.value = draft.recommendation;
  reasonInput.value = draft.reason;
  draftPanel.classList.remove('hidden');
  draftPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function escapeHtml(value: string): string {
  return value.replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[char]!);
}

function collectCorrections(draft: StructuredDraft): FieldCorrection[] {
  const finalValues: Record<DraftField, string> = {
    location: locationInput.value.trim(),
    memoryType: typeInput.value,
    recommendation: recommendationInput.value.trim(),
    reason: reasonInput.value.trim(),
  };
  const draftValues: Record<DraftField, string> = {
    location: draft.location,
    memoryType: draft.memoryType,
    recommendation: draft.recommendation,
    reason: draft.reason,
  };

  return (Object.keys(finalValues) as DraftField[])
    .filter((field) => finalValues[field] !== draftValues[field])
    .map((field) => ({ field, before: draftValues[field], after: finalValues[field] }));
}

function formatRate(value: number | null): string {
  return value === null ? '측정 전' : `${Math.round(value * 100)}%`;
}

function renderMetrics(): void {
  const metrics = calculateValidationMetrics(memories, feedback);
  const values = [
    ['확정 기억', `${metrics.confirmedMemories}건`],
    ['측정된 입력', `${metrics.measuredCaptures}건`],
    ['입력→확정 중앙값', metrics.medianCaptureSeconds === null ? '측정 전' : `${metrics.medianCaptureSeconds.toFixed(1)}초`],
    ['무수정 확정률', formatRate(metrics.acceptedWithoutCorrectionRate)],
    ['활용 피드백', `${metrics.totalFeedback}건`],
    ['도움 확인 비율', formatRate(metrics.usefulFeedbackRate)],
  ];
  metricGrid.innerHTML = values.map(([label, value]) => `<div class="metric-card"><span>${label}</span><strong>${value}</strong></div>`).join('');
}

function feedbackCount(memoryId: string): number {
  return feedback.filter((item) => item.memoryId === memoryId).length;
}

function renderMemories(query = ''): void {
  const normalized = query.trim().toLowerCase();
  const filtered = memories.filter((memory) => {
    const searchable = [memory.location, memory.recommendation, memory.reason, memory.memoryType, ...memory.tags].join(' ').toLowerCase();
    return searchable.includes(normalized);
  });

  memoryStats.textContent = `확정 기억 ${memories.length}건 · 현재 표시 ${filtered.length}건`;

  if (filtered.length === 0) {
    memoryList.innerHTML = `<div class="empty-state">아직 저장된 현장 기억이 없습니다.<br />실제 배송 중 반복해서 기억해야 하는 정보를 하나 기록해 보세요.</div>`;
    return;
  }

  memoryList.innerHTML = filtered.map((memory) => `
    <article class="memory-card" data-memory-id="${memory.id}">
      <div class="memory-card-top">
        <span class="type-chip">${TYPE_LABELS[memory.memoryType]}</span>
        <span class="confirmed-chip">USER CONFIRMED</span>
      </div>
      <h3>${escapeHtml(memory.location || '위치 미지정')}</h3>
      <p class="recommendation">${escapeHtml(memory.recommendation)}</p>
      ${memory.reason ? `<p class="reason">왜: ${escapeHtml(memory.reason)}</p>` : ''}
      <p class="raw-note">원문: ${escapeHtml(memory.rawText)}</p>
      <div class="feedback-box">
        <span>이 기억이 실제 판단에 영향을 줬나요? · 기록 ${feedbackCount(memory.id)}회</span>
        <div class="feedback-actions">
          <select class="outcome-select" aria-label="활용 결과">
            ${Object.entries(OUTCOME_LABELS).map(([value, label]) => `<option value="${value}">${label}</option>`).join('')}
          </select>
          <button class="feedback-button ghost-button" type="button">결과 기록</button>
        </div>
      </div>
    </article>
  `).join('');
}

function downloadText(filename: string, content: string, mimeType: string): void {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}

rawText.value = loadCaptureDraft();
if (rawText.value.trim()) {
  captureStartedAt = Date.now();
  statusText.textContent = '저장된 미완료 메모를 복구했습니다.';
}

rawText.addEventListener('input', () => {
  startCaptureClock();
  saveCaptureDraft(rawText.value);
});

sessionButton.addEventListener('click', () => {
  if (!activeSession) {
    activeSession = { id: crypto.randomUUID(), startedAt: new Date().toISOString(), noteCountAtStart: memories.length };
    sessions = [activeSession, ...sessions];
  } else {
    const ended: ValidationSession = { ...activeSession, endedAt: new Date().toISOString(), noteCountAtEnd: memories.length };
    sessions = sessions.map((session) => session.id === ended.id ? ended : session);
    activeSession = null;
  }
  saveSessions(sessions);
  renderSession();
});

structureButton.addEventListener('click', () => {
  try {
    currentRawText = rawText.value.trim();
    if (!captureStartedAt && currentRawText) captureStartedAt = Date.now();
    currentDraft = structureFieldMemory(currentRawText);
    structuredAt = Date.now();
    fillDraft(currentDraft);
    statusText.textContent = '초안 생성 완료. 추출 결과를 기사 본인이 확인하세요.';
  } catch (error) {
    statusText.textContent = error instanceof Error ? error.message : '구조화에 실패했습니다.';
  }
});

confirmButton.addEventListener('click', () => {
  if (!currentDraft || !currentRawText) return;
  const confirmedAt = Date.now();
  const startedAt = captureStartedAt ?? structuredAt ?? confirmedAt;
  const structuredTime = structuredAt ?? confirmedAt;
  const now = new Date(confirmedAt).toISOString();
  const memory: FieldMemory = {
    id: crypto.randomUUID(),
    rawText: currentRawText,
    location: locationInput.value.trim(),
    memoryType: typeInput.value as MemoryType,
    recommendation: recommendationInput.value.trim(),
    reason: reasonInput.value.trim(),
    tags: currentDraft.tags,
    source: 'DRIVER_EXPERIENCE',
    confirmationState: 'USER_CONFIRMED',
    validation: {
      captureStartedAt: new Date(startedAt).toISOString(),
      structuredAt: new Date(structuredTime).toISOString(),
      confirmedAt: now,
      captureDurationMs: confirmedAt - startedAt,
      corrections: collectCorrections(currentDraft),
    },
    createdAt: now,
    updatedAt: now,
  };

  memories = [memory, ...memories];
  saveMemories(memories);
  currentDraft = null;
  currentRawText = '';
  captureStartedAt = null;
  structuredAt = null;
  rawText.value = '';
  saveCaptureDraft('');
  draftPanel.classList.add('hidden');
  statusText.textContent = '확정 기억과 실증 로그를 로컬에 저장했습니다.';
  renderMemories(searchInput.value);
  renderMetrics();
});

searchInput.addEventListener('input', () => renderMemories(searchInput.value));

memoryList.addEventListener('click', (event) => {
  const button = (event.target as HTMLElement).closest<HTMLButtonElement>('.feedback-button');
  if (!button) return;
  const card = button.closest<HTMLElement>('.memory-card');
  const select = card?.querySelector<HTMLSelectElement>('.outcome-select');
  const memoryId = card?.dataset.memoryId;
  if (!memoryId || !select) return;

  const item: RetrievalFeedback = {
    id: crypto.randomUUID(),
    memoryId,
    outcome: select.value as OperationalOutcome,
    recordedAt: new Date().toISOString(),
  };
  feedback = [item, ...feedback];
  saveFeedback(feedback);
  renderMemories(searchInput.value);
  renderMetrics();
});

clearButton.addEventListener('click', () => {
  if (!confirm('이 브라우저에 저장된 모든 현장 기억과 실증 로그를 삭제할까요?')) return;
  clearAllValidationData();
  memories = [];
  feedback = [];
  renderMemories();
  renderMetrics();
});

exportJsonButton.addEventListener('click', () => {
  const payload = {
    schemaVersion: 'dfi.validation.export.v1',
    exportedAt: new Date().toISOString(),
    memories,
    feedback,
    metrics: calculateValidationMetrics(memories, feedback),
    sessions,
  };
  downloadText(`dfi-validation-${new Date().toISOString().slice(0, 10)}.json`, JSON.stringify(payload, null, 2), 'application/json');
});

exportCsvButton.addEventListener('click', () => {
  downloadText(`dfi-validation-${new Date().toISOString().slice(0, 10)}.csv`, `\uFEFF${buildValidationCsv(memories, feedback)}`, 'text/csv;charset=utf-8');
});

interface SpeechRecognitionEventLike extends Event {
  results: { [index: number]: { [index: number]: { transcript: string } } };
}
interface SpeechRecognitionLike extends EventTarget {
  lang: string;
  interimResults: boolean;
  maxAlternatives: number;
  start(): void;
  onresult: ((event: SpeechRecognitionEventLike) => void) | null;
  onerror: (() => void) | null;
  onend: (() => void) | null;
}
type SpeechRecognitionConstructor = new () => SpeechRecognitionLike;

voiceButton.addEventListener('click', () => {
  const speechWindow = window as Window & {
    SpeechRecognition?: SpeechRecognitionConstructor;
    webkitSpeechRecognition?: SpeechRecognitionConstructor;
  };
  const Recognition = speechWindow.SpeechRecognition ?? speechWindow.webkitSpeechRecognition;
  if (!Recognition) {
    statusText.textContent = '이 브라우저는 음성 인식을 지원하지 않습니다. 텍스트 입력을 사용하세요.';
    return;
  }

  const recognition = new Recognition();
  recognition.lang = 'ko-KR';
  recognition.interimResults = false;
  recognition.maxAlternatives = 1;
  voiceButton.disabled = true;
  voiceButton.textContent = '듣는 중…';
  statusText.textContent = '배송지 개인정보를 과도하게 말하지 마세요.';
  if (captureStartedAt === null) captureStartedAt = Date.now();

  recognition.onresult = (event) => {
    rawText.value = event.results[0][0].transcript;
  };
  recognition.onerror = () => {
    statusText.textContent = '음성 인식에 실패했습니다. 다시 시도하거나 텍스트로 입력하세요.';
  };
  recognition.onend = () => {
    voiceButton.disabled = false;
    voiceButton.textContent = '🎙 음성 입력';
  };
  recognition.start();
});

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {
      statusText.textContent = '오프라인 준비에 실패했습니다. 온라인 상태에서 다시 실행하세요.';
    });
  });
}

renderMemories();
renderMetrics();
renderSession();
