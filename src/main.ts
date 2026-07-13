import './style.css';
import { structureFieldMemory } from './fieldMemoryParser';
import { clearMemories, loadMemories, saveMemories } from './store';
import type { FieldMemory, MemoryType, StructuredDraft } from './types';

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

let memories = loadMemories();
let currentDraft: StructuredDraft | null = null;
let currentRawText = '';

const app = document.querySelector<HTMLDivElement>('#app');
if (!app) throw new Error('App root not found');

app.innerHTML = `
  <main class="shell">
    <header class="hero">
      <p class="eyebrow">DELIVERY FIELD INTELLIGENCE</p>
      <h1>기사의 경험을<br />현장 데이터로 바꾼다.</h1>
      <p class="hero-copy">오늘의 경로가 아니라, 베테랑 기사 머릿속에 쌓이는 현장 기억을 수집·구조화·재사용하는 MVP입니다.</p>
    </header>

    <section class="panel capture-panel">
      <div class="section-heading">
        <div>
          <span class="step">01</span>
          <h2>현장 메모 기록</h2>
        </div>
        <button id="voiceButton" class="ghost-button" type="button">🎙 음성 입력</button>
      </div>
      <textarea id="rawText" rows="5" placeholder="예: 탄현 9단지 903동은 지하주차장으로 들어가면 카트 이동이 빠름"></textarea>
      <div class="action-row">
        <span id="statusText" class="status-text">개인정보는 최소한으로 입력하세요.</span>
        <button id="structureButton" class="primary-button" type="button">AI 구조화 초안 만들기</button>
      </div>
    </section>

    <section id="draftPanel" class="panel draft-panel hidden">
      <div class="section-heading">
        <div>
          <span class="step">02</span>
          <h2>구조화 결과 확인</h2>
        </div>
        <span class="draft-badge">AI DRAFT</span>
      </div>
      <div class="form-grid">
        <label>위치<input id="locationInput" /></label>
        <label>기억 유형<select id="typeInput"></select></label>
        <label class="wide">권장 행동<input id="recommendationInput" /></label>
        <label class="wide">이유/효과<input id="reasonInput" /></label>
      </div>
      <div class="action-row">
        <span class="status-text">기사 확인 전에는 확정 지식으로 취급하지 않습니다.</span>
        <button id="confirmButton" class="primary-button" type="button">확인하고 저장</button>
      </div>
    </section>

    <section class="panel memory-panel">
      <div class="section-heading">
        <div>
          <span class="step">03</span>
          <h2>현장 기억 검색</h2>
        </div>
        <button id="clearButton" class="ghost-button danger" type="button">전체 삭제</button>
      </div>
      <input id="searchInput" class="search-input" placeholder="장소, 행동, 이유로 검색" />
      <div id="memoryStats" class="memory-stats"></div>
      <div id="memoryList" class="memory-list"></div>
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

Object.entries(TYPE_LABELS).forEach(([value, label]) => {
  const option = document.createElement('option');
  option.value = value;
  option.textContent = `${label} · ${value}`;
  typeInput.append(option);
});

function fillDraft(draft: StructuredDraft): void {
  locationInput.value = draft.location;
  typeInput.value = draft.memoryType;
  recommendationInput.value = draft.recommendation;
  reasonInput.value = draft.reason;
  draftPanel.classList.remove('hidden');
  draftPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function renderMemories(query = ''): void {
  const normalized = query.trim().toLowerCase();
  const filtered = memories.filter((memory) => {
    const searchable = [memory.location, memory.recommendation, memory.reason, memory.memoryType, ...memory.tags]
      .join(' ')
      .toLowerCase();
    return searchable.includes(normalized);
  });

  memoryStats.textContent = `확정 기억 ${memories.length}건 · 현재 표시 ${filtered.length}건`;

  if (filtered.length === 0) {
    memoryList.innerHTML = `<div class="empty-state">아직 저장된 현장 기억이 없습니다.<br />실제 배송 중 반복해서 기억해야 하는 정보를 하나 기록해 보세요.</div>`;
    return;
  }

  memoryList.innerHTML = filtered
    .map(
      (memory) => `
        <article class="memory-card">
          <div class="memory-card-top">
            <span class="type-chip">${TYPE_LABELS[memory.memoryType]}</span>
            <span class="confirmed-chip">USER CONFIRMED</span>
          </div>
          <h3>${escapeHtml(memory.location || '위치 미지정')}</h3>
          <p class="recommendation">${escapeHtml(memory.recommendation)}</p>
          ${memory.reason ? `<p class="reason">왜: ${escapeHtml(memory.reason)}</p>` : ''}
          <p class="raw-note">원문: ${escapeHtml(memory.rawText)}</p>
        </article>
      `,
    )
    .join('');
}

function escapeHtml(value: string): string {
  return value.replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[char]!);
}

structureButton.addEventListener('click', () => {
  try {
    currentRawText = rawText.value.trim();
    currentDraft = structureFieldMemory(currentRawText);
    fillDraft(currentDraft);
    statusText.textContent = '초안 생성 완료. 추출 결과를 기사 본인이 확인하세요.';
  } catch (error) {
    statusText.textContent = error instanceof Error ? error.message : '구조화에 실패했습니다.';
  }
});

confirmButton.addEventListener('click', () => {
  if (!currentDraft || !currentRawText) return;
  const now = new Date().toISOString();
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
    createdAt: now,
    updatedAt: now,
  };

  memories = [memory, ...memories];
  saveMemories(memories);
  currentDraft = null;
  currentRawText = '';
  rawText.value = '';
  draftPanel.classList.add('hidden');
  statusText.textContent = '확정된 현장 기억을 로컬에 저장했습니다.';
  renderMemories(searchInput.value);
});

searchInput.addEventListener('input', () => renderMemories(searchInput.value));

clearButton.addEventListener('click', () => {
  if (!confirm('이 브라우저에 저장된 모든 현장 기억을 삭제할까요?')) return;
  clearMemories();
  memories = [];
  renderMemories();
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

renderMemories();
