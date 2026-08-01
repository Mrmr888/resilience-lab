<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'

const overview = ref({
  serviceCount: 4,
  totalRequests: 0,
  successfulRequests: 0,
  failedRequests: 0,
  successRate: 100,
  p95LatencyMs: 0,
  circuitState: 'CLOSED',
})
const events = ref([])
const scenario = ref({ mode: 'NORMAL', delayMs: 0 })
const delayMs = ref(1200)
const sku = ref('SKU-RED-01')
const quantity = ref(1)
const loading = ref(true)
const actionBusy = ref(false)
const lastResult = ref(null)
const notice = ref('')
const noticeTone = ref('info')
let refreshTimer
let noticeTimer

const scenarioOptions = [
  { mode: 'NORMAL', label: '正常', hint: '基准流量', icon: '✓' },
  { mode: 'SLOW', label: '慢响应', hint: '延迟注入', icon: '⌁' },
  { mode: 'ERROR', label: '持续错误', hint: '触发熔断', icon: '!' },
  { mode: 'FLAKY', label: '间歇错误', hint: '50% 失败', icon: '∿' },
]

const metricCards = computed(() => [
  { label: '服务节点', value: overview.value.serviceCount, suffix: '个', tone: 'cyan', note: 'Eureka 动态发现' },
  { label: '请求成功率', value: Number(overview.value.successRate).toFixed(1), suffix: '%', tone: 'green', note: `${overview.value.totalRequests} 次演练请求` },
  { label: 'P95 延迟', value: overview.value.p95LatencyMs, suffix: 'ms', tone: 'amber', note: '最近 100 次请求' },
  { label: '熔断器', value: circuitLabel(overview.value.circuitState), suffix: '', tone: circuitTone.value, note: 'inventory 调用链' },
])

const circuitTone = computed(() => {
  if (overview.value.circuitState === 'OPEN') return 'red'
  if (overview.value.circuitState === 'HALF_OPEN') return 'amber'
  return 'green'
})

const currentScenario = computed(() =>
  scenarioOptions.find((item) => item.mode === scenario.value.mode) ?? scenarioOptions[0],
)

async function api(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {}),
    },
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail || `请求失败（${response.status}）`)
  }
  return response.json()
}

async function refresh(silent = false) {
  if (!silent) loading.value = true
  const [overviewResult, eventsResult, scenarioResult] = await Promise.allSettled([
    api('/api/overview'),
    api('/api/events?limit=20'),
    api('/api/operations/scenario'),
  ])

  if (overviewResult.status === 'fulfilled') overview.value = overviewResult.value
  if (eventsResult.status === 'fulfilled') events.value = eventsResult.value
  if (scenarioResult.status === 'fulfilled') scenario.value = scenarioResult.value

  const rejected = [overviewResult, eventsResult, scenarioResult].find((item) => item.status === 'rejected')
  if (rejected && !silent) showNotice('后端服务尚未就绪，请稍后刷新', 'error')
  loading.value = false
}

async function changeScenario(mode) {
  if (actionBusy.value) return
  actionBusy.value = true
  try {
    scenario.value = await api('/api/operations/scenario', {
      method: 'PUT',
      body: JSON.stringify({ mode, delayMs: mode === 'SLOW' ? delayMs.value : 0 }),
    })
    showNotice(`故障场景已切换为「${currentScenario.value.label}」`, 'success')
  } catch (error) {
    showNotice(error.message, 'error')
  } finally {
    actionBusy.value = false
  }
}

async function runSimulation() {
  if (actionBusy.value) return
  actionBusy.value = true
  lastResult.value = null
  try {
    lastResult.value = await api('/api/orders/simulate', {
      method: 'POST',
      body: JSON.stringify({ sku: sku.value, quantity: Number(quantity.value) }),
    })
    const tone = lastResult.value.outcome === 'SUCCEEDED' ? 'success' : 'error'
    showNotice(lastResult.value.message, tone)
    await refresh(true)
  } catch (error) {
    showNotice(error.message, 'error')
  } finally {
    actionBusy.value = false
  }
}

function showNotice(message, tone = 'info') {
  notice.value = message
  noticeTone.value = tone
  window.clearTimeout(noticeTimer)
  noticeTimer = window.setTimeout(() => {
    notice.value = ''
  }, 3600)
}

function circuitLabel(state) {
  return { CLOSED: '关闭', OPEN: '开启', HALF_OPEN: '半开' }[state] ?? state
}

function formatTime(value) {
  if (!value) return '--:--:--'
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(new Date(value))
}

function eventLabel(type) {
  return {
    ORDER_SUCCEEDED: '请求成功',
    ORDER_REJECTED: '业务拒绝',
    DOWNSTREAM_FAILURE: '下游故障',
  }[type] ?? type
}

onMounted(() => {
  refresh()
  refreshTimer = window.setInterval(() => refresh(true), 5000)
})

onUnmounted(() => {
  window.clearInterval(refreshTimer)
  window.clearTimeout(noticeTimer)
})
</script>

<template>
  <div class="app-shell">
    <div class="ambient ambient-one"></div>
    <div class="ambient ambient-two"></div>

    <header class="topbar page-width">
      <a class="brand" href="#" aria-label="ResilienceLab 首页">
        <span class="brand-mark">R/</span>
        <span>
          <strong>ResilienceLab</strong>
          <small>MICROSERVICE PLAYGROUND</small>
        </span>
      </a>
      <div class="topbar-meta">
        <span class="live-pill"><i></i> LIVE SYSTEM</span>
        <a class="ghost-link" href="https://github.com/" target="_blank" rel="noreferrer">GitHub ↗</a>
      </div>
    </header>

    <main class="page-width">
      <section class="hero">
        <div class="hero-copy">
          <p class="eyebrow">SPRING CLOUD · RESILIENCE4J · VUE 3</p>
          <h1>让微服务故障<br /><span>变得可见、可控。</span></h1>
          <p class="hero-description">
            一个面向学习与面试展示的微服务韧性实验场。注入受控故障，观察调用链、熔断状态与恢复过程。
          </p>
        </div>
        <div class="topology-card panel">
          <div class="panel-heading compact">
            <div>
              <span class="section-kicker">SERVICE TOPOLOGY</span>
              <h2>实时调用拓扑</h2>
            </div>
            <span class="status-dot-text"><i></i> 运行中</span>
          </div>
          <div class="topology-flow">
            <div class="service-node frontend-node"><b>WEB</b><span>Vue Console</span></div>
            <span class="flow-arrow">→</span>
            <div class="service-node gateway-node"><b>GW</b><span>API Gateway</span></div>
            <span class="flow-arrow">→</span>
            <div class="service-stack">
              <div class="service-node"><b>OR</b><span>Order</span></div>
              <div class="service-node" :class="{ danger: scenario.mode !== 'NORMAL' }"><b>IN</b><span>Inventory</span></div>
            </div>
          </div>
          <div class="topology-footer">
            <span>Eureka Registry</span>
            <code>trace: auto</code>
          </div>
        </div>
      </section>

      <section class="metric-grid" aria-label="系统概览">
        <article v-for="metric in metricCards" :key="metric.label" class="metric-card panel">
          <div class="metric-topline">
            <span>{{ metric.label }}</span>
            <i :class="`tone-${metric.tone}`"></i>
          </div>
          <p class="metric-value" :class="`text-${metric.tone}`">
            {{ metric.value }}<small>{{ metric.suffix }}</small>
          </p>
          <span class="metric-note">{{ metric.note }}</span>
        </article>
      </section>

      <section class="workspace-grid">
        <article class="panel control-panel">
          <div class="panel-heading">
            <div>
              <span class="section-kicker">FAULT INJECTION</span>
              <h2>故障场景控制</h2>
            </div>
            <span class="scenario-badge">{{ currentScenario.label }}</span>
          </div>
          <p class="panel-description">故障仅作用于合成库存服务，最大延迟被限制为 2.5 秒。</p>

          <div class="scenario-grid">
            <button
              v-for="option in scenarioOptions"
              :key="option.mode"
              class="scenario-option"
              :class="{ active: scenario.mode === option.mode }"
              :disabled="actionBusy"
              @click="changeScenario(option.mode)"
            >
              <span class="scenario-icon">{{ option.icon }}</span>
              <span><b>{{ option.label }}</b><small>{{ option.hint }}</small></span>
              <i></i>
            </button>
          </div>

          <div class="delay-control">
            <div>
              <span>慢响应延迟</span>
              <strong>{{ delayMs }} ms</strong>
            </div>
            <input
              v-model.number="delayMs"
              aria-label="慢响应延迟"
              type="range"
              min="100"
              max="2500"
              step="100"
              @change="scenario.mode === 'SLOW' && changeScenario('SLOW')"
            />
            <div class="range-labels"><span>100 ms</span><span>2500 ms</span></div>
          </div>
        </article>

        <article class="panel traffic-panel">
          <div class="panel-heading">
            <div>
              <span class="section-kicker">TRAFFIC SIMULATOR</span>
              <h2>发送演练请求</h2>
            </div>
            <span class="request-count"># {{ overview.totalRequests + 1 }}</span>
          </div>
          <p class="panel-description">经网关发起一次订单库存校验，观察故障如何沿调用链传播。</p>

          <form class="simulation-form" @submit.prevent="runSimulation">
            <label>
              <span>商品 SKU</span>
              <input v-model.trim="sku" required pattern="[A-Za-z0-9-]{2,32}" maxlength="32" />
            </label>
            <label>
              <span>数量</span>
              <input v-model.number="quantity" required type="number" min="1" max="20" />
            </label>
            <button class="primary-button" type="submit" :disabled="actionBusy">
              <span v-if="actionBusy" class="button-spinner"></span>
              <span v-else>运行演练</span>
              <b v-if="!actionBusy">→</b>
            </button>
          </form>

          <div class="result-box" :class="lastResult?.outcome?.toLowerCase()">
            <template v-if="lastResult">
              <div><span>OUTCOME</span><b>{{ lastResult.outcome }}</b></div>
              <p>{{ lastResult.message }}</p>
              <code>{{ lastResult.traceId }}</code>
              <small>{{ lastResult.latencyMs }} ms</small>
            </template>
            <template v-else>
              <span class="result-placeholder-icon">⌁</span>
              <p>等待下一次演练请求</p>
            </template>
          </div>
        </article>
      </section>

      <section class="panel timeline-panel">
        <div class="panel-heading timeline-heading">
          <div>
            <span class="section-kicker">EVENT TIMELINE</span>
            <h2>调用事件时间线</h2>
          </div>
          <button class="refresh-button" :disabled="loading" @click="refresh()">↻ 刷新</button>
        </div>

        <div v-if="events.length" class="event-table">
          <div class="event-row event-header">
            <span>状态</span><span>事件</span><span>服务</span><span>Trace ID</span><span>延迟</span><span>时间</span>
          </div>
          <div v-for="event in events" :key="event.id" class="event-row">
            <span><i class="severity-dot" :class="event.severity.toLowerCase()"></i>{{ event.severity }}</span>
            <span><b>{{ eventLabel(event.type) }}</b><small>{{ event.message }}</small></span>
            <span>{{ event.sourceService }}</span>
            <code :title="event.traceId">{{ event.traceId.slice(0, 8) }}</code>
            <span>{{ event.latencyMs }} ms</span>
            <time>{{ formatTime(event.createdAt) }}</time>
          </div>
        </div>
        <div v-else class="empty-state">
          <span>⌁</span>
          <h3>还没有调用事件</h3>
          <p>选择一个故障场景并运行首个演练请求。</p>
        </div>
      </section>
    </main>

    <footer class="page-width">
      <span>ResilienceLab · Synthetic data only</span>
      <span>Built for learning, designed for deployment.</span>
    </footer>

    <transition name="toast">
      <div v-if="notice" class="toast" :class="noticeTone">{{ notice }}</div>
    </transition>
  </div>
</template>
