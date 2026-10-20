<template>
  <div class="product-panel" :class="{ mounted }">
    <!-- 上部分：数据总览 -->
    <div class="stats-wrap module-section">
      <div class="section-label">
        <span class="label-line"></span>
        <span class="label-text">产品数据总览</span>
        <span class="label-line"></span>
      </div>
      <div class="stats-section">
        <div class="stat-item stat-item--cyan" :class="{ 'stat-item--enter': mounted }" :style="{ '--delay': '0s' }">
        <div class="stat-icon inbound-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">总入库数</span>
          <span class="stat-value">{{ formatNum(productData.totalInbound) }}</span>
        </div>
      </div>
      <div class="stat-item stat-item--green" :class="{ 'stat-item--enter': mounted }" :style="{ '--delay': '0.1s' }">
        <div class="stat-icon qualified-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">总合格数</span>
          <span class="stat-value accent-green">{{ formatNum(productData.totalQualified) }}</span>
          <span class="stat-sub">合格率 {{ productData.qualifyRate }}%</span>
        </div>
      </div>
      <div class="stat-item stat-item--orange" :class="{ 'stat-item--enter': mounted }" :style="{ '--delay': '0.2s' }">
        <div class="stat-icon output-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
            <line x1="8" y1="21" x2="16" y2="21"/>
            <line x1="12" y1="17" x2="12" y2="21"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">总产量</span>
          <span class="stat-value accent-orange">{{ formatNum(productData.totalOutput) }}</span>
          <span class="stat-sub">计划 {{ formatNum(productData.totalPlanned) }}</span>
        </div>
      </div>
      </div>
    </div>

    <!-- 中部分：折线图 -->
    <div class="chart-section module-section">
      <div class="section-label">
        <span class="label-line"></span>
        <span class="label-text">近30日出入库趋势</span>
        <span class="label-line"></span>
      </div>
      <div class="chart-container">
        <!-- 图例 -->
        <div class="chart-legend">
          <span class="legend-item">
            <i class="legend-dot inbound-dot"></i>入库
          </span>
          <span class="legend-item">
            <i class="legend-dot outbound-dot"></i>出库
          </span>
        </div>
        <svg ref="chartSvgRef" class="line-chart-svg" :viewBox="`0 0 ${chartW} ${chartH}`" preserveAspectRatio="xMidYMid meet">
          <!-- Y轴网格线 -->
          <line
            v-for="(y, i) in yTicks"
            :key="'grid-' + i"
            :x1="chartPadL" :y1="y.y" :x2="chartW - chartPadR" :y2="y.y"
            stroke="rgba(0, 200, 255, 0.06)" stroke-width="0.5"
          />
          <!-- X轴网格线 -->
          <line
            v-for="(x, i) in xTicksPos"
            :key="'xgrid-' + i"
            :x1="x" :y1="chartPadT" :x2="x" :y2="chartH - chartPadB"
            stroke="rgba(0, 200, 255, 0.06)" stroke-width="0.5"
          />
          <!-- 轴线 -->
          <line :x1="chartPadL" :y1="chartPadT" :x2="chartPadL" :y2="chartH - chartPadB" stroke="rgba(0, 200, 255, 0.2)" stroke-width="1"/>
          <line :x1="chartPadL" :y1="chartH - chartPadB" :x2="chartW - chartPadR" :y2="chartH - chartPadB" stroke="rgba(0, 200, 255, 0.2)" stroke-width="1"/>
          <!-- Y轴刻度 -->
          <text
            v-for="(y, i) in yTicks"
            :key="'ylbl-' + i"
            :x="chartPadL - 8" :y="y.y + 1"
            text-anchor="end" dominant-baseline="middle"
            fill="rgba(180, 210, 240, 0.45)" font-size="8"
          >{{ y.label }}</text>
          <!-- X轴刻度 -->
          <text
            v-for="(x, i) in xLabels"
            :key="'xlbl-' + i"
            :x="x.pos" :y="chartH - chartPadB + 14"
            text-anchor="middle" dominant-baseline="middle"
            fill="rgba(180, 210, 240, 0.45)" font-size="7"
          >{{ x.text }}</text>
          <!-- 面积填充 - 入库 -->
          <polygon :points="inboundArea" fill="url(#inboundGrad)" class="chart-area-fade" :style="{ opacity: mounted ? 0.25 : 0 }"/>
          <!-- 面积填充 - 出库 -->
          <polygon :points="outboundArea" fill="url(#outboundGrad)" class="chart-area-fade" :style="{ opacity: mounted ? 0.2 : 0 }"/>
          <!-- 入库折线 -->
          <polyline :points="inboundPoints" fill="none" stroke="#00ccff" stroke-width="1.8" stroke-linejoin="round" stroke-linecap="round"
            class="chart-line-draw"
            :stroke-dasharray="inboundPathLen"
            :stroke-dashoffset="mounted ? 0 : inboundPathLen"
          />
          <!-- 出库折线 -->
          <polyline :points="outboundPoints" fill="none" stroke="#ff9100" stroke-width="1.8" stroke-linejoin="round" stroke-linecap="round"
            class="chart-line-draw chart-line-draw-out"
            :stroke-dasharray="outboundPathLen"
            :stroke-dashoffset="mounted ? 0 : outboundPathLen"
          />
          <!-- 入库数据点 -->
          <circle
            v-for="(p, i) in inboundPts"
            :key="'idot-' + i"
            :cx="p.x" :cy="p.y" r="2.5"
            fill="#00ccff" stroke="#0a0e27" stroke-width="1"
            class="chart-dot-pop"
            :style="{ '--dot-delay': (0.6 + i * 0.04) + 's' }"
          />
          <!-- 出库数据点 -->
          <circle
            v-for="(p, i) in outboundPts"
            :key="'odot-' + i"
            :cx="p.x" :cy="p.y" r="2.5"
            fill="#ff9100" stroke="#0a0e27" stroke-width="1"
            class="chart-dot-pop"
            :style="{ '--dot-delay': (0.9 + i * 0.04) + 's' }"
          />
          <defs>
            <linearGradient id="inboundGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#00ccff" stop-opacity="0.6"/>
              <stop offset="100%" stop-color="#00ccff" stop-opacity="0"/>
            </linearGradient>
            <linearGradient id="outboundGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#ff9100" stop-opacity="0.5"/>
              <stop offset="100%" stop-color="#ff9100" stop-opacity="0"/>
            </linearGradient>
            <filter id="glow-inbound">
              <feGaussianBlur in="SourceGraphic" stdDeviation="1.5" result="blur"/>
              <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
            </filter>
            <filter id="glow-outbound">
              <feGaussianBlur in="SourceGraphic" stdDeviation="1.5" result="blur"/>
              <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
            </filter>
          </defs>
          <!-- 透明交互层 -->
          <rect
            :x="chartPadL" :y="chartPadT"
            :width="plotW" :height="plotH"
            fill="transparent"
            @mousemove="onChartMouseMove"
            @mouseleave="onChartMouseLeave"
          />
          <!-- hover 竖线指示器 -->
          <line
            v-if="hoverIndex !== null"
            :x1="hoverLineX" :y1="chartPadT"
            :x2="hoverLineX" :y2="chartH - chartPadB"
            stroke="rgba(255,255,255,0.35)" stroke-width="1"
            stroke-dasharray="3,3"
          />
          <!-- hover 高亮圆点 - 入库 -->
          <circle
            v-if="hoverIndex !== null"
            :cx="inboundPts[hoverIndex].x"
            :cy="inboundPts[hoverIndex].y"
            r="4" fill="#00ccff" stroke="#fff" stroke-width="1.5"
            filter="url(#glow-inbound)"
          />
          <!-- hover 高亮圆点 - 出库 -->
          <circle
            v-if="hoverIndex !== null"
            :cx="outboundPts[hoverIndex].x"
            :cy="outboundPts[hoverIndex].y"
            r="4" fill="#ff9100" stroke="#fff" stroke-width="1.5"
            filter="url(#glow-outbound)"
          />
          <!-- hover 提示框 -->
          <g v-if="hoverIndex !== null">
            <rect
              :x="tooltipX" :y="tooltipY"
              width="90" height="34"
              rx="4" fill="rgba(8, 16, 40, 0.92)"
              stroke="rgba(0, 200, 255, 0.25)" stroke-width="0.5"
            />
            <text
              :x="tooltipX + 6" :y="tooltipY + 13"
              fill="rgba(200,220,240,0.7)" font-size="8"
            >第{{ hoverIndex + 1 }}日</text>
            <text
              :x="tooltipX + 6" :y="tooltipY + 27"
              fill="#00ccff" font-size="9" font-weight="bold"
            >入库 {{ hoverInboundVal }}</text>
            <text
              :x="tooltipX + 52" :y="tooltipY + 27"
              fill="#ff9100" font-size="9" font-weight="bold"
            >出库 {{ hoverOutboundVal }}</text>
          </g>
        </svg>
        <!-- 当日出入库数量 -->
        <div class="today-summary">
          <span class="today-item inbound-today" :class="{ 'today-item--enter': mounted }">
            <i class="legend-dot inbound-dot"></i>今日入库 <strong>{{ formatNum(todayInbound) }}</strong> 件
          </span>
          <span class="today-item outbound-today" :class="{ 'today-item--enter': mounted }">
            <i class="legend-dot outbound-dot"></i>今日出库 <strong>{{ formatNum(todayOutbound) }}</strong> 件
          </span>
        </div>
      </div>
    </div>

    <!-- 下部分：生产计划完成列表 -->
    <div class="plan-section module-section">
      <div class="plan-top-bar">
        <div class="section-label">
          <span class="label-line"></span>
          <span class="label-text">生产计划完成情况</span>
          <span class="label-line"></span>
        </div>
        <div class="plan-color-legend">
          <span class="legend-tag tag-output">产量</span>
          <span class="legend-tag tag-qualified">合格</span>
          <span class="legend-tag tag-inbound">入库</span>
        </div>
      </div>
      <div class="plan-scroll">
        <div class="plan-list">
          <div
            v-for="(plan, idx) in enrichedPlans"
            :key="plan.id + '-' + idx"
            class="plan-item"
            :class="{ 'plan-item--enter': mounted }"
            :style="{ '--delay': (idx * 0.06) + 's' }"
          >
            <!-- 左侧：计划信息 -->
            <div class="plan-info">
              <div class="plan-name-row">
                <span class="plan-index">{{ String(idx + 1).padStart(2, '0') }}</span>
                <span class="plan-name">{{ plan.name }}</span>
              </div>
              <div class="plan-nums">
                <span class="num-label">产量</span>
                <span class="num-val">{{ formatNum(plan.output) }}</span>
                <span class="num-sep">/</span>
                <span class="num-label">计划</span>
                <span class="num-total">{{ formatNum(plan.planQty) }}</span>
                <span class="num-unit">件</span>
              </div>
              <div class="plan-extra">
                <span class="extra-item extra-qualified">
                  <span class="extra-dot"></span>
                  <span class="extra-label">合格</span>
                  <span class="extra-val">{{ formatNum(plan.qualified) }}</span>
                </span>
                <span class="extra-item extra-inbound">
                  <span class="extra-dot"></span>
                  <span class="extra-label">入库</span>
                  <span class="extra-val">{{ formatNum(plan.inbound) }}</span>
                </span>
              </div>
            </div>
            <!-- 右侧：三层嵌套环形图 -->
            <div class="plan-ring-wrap">
              <svg class="plan-ring-svg" viewBox="0 0 100 100">
                <!-- 背景环 -->
                <circle cx="50" cy="50" :r="ringROuter"
                  fill="none" stroke="rgba(255,255,255,0.05)" stroke-width="1.5" />
                <!-- 产量（外环） -->
                <circle
                  cx="50" cy="50" :r="ringROuter"
                  fill="none" stroke="#00aaff" stroke-width="6" stroke-linecap="round"
                  :stroke-dasharray="ringArc(animPct(plan.outputPct), ringROuter) + ' ' + circOf(ringROuter)"
                  stroke-dashoffset="0"
                  transform="rotate(-90 50 50)"
                />
                <!-- 合格（中环） -->
                <circle
                  cx="50" cy="50" :r="ringRMiddle"
                  fill="none" stroke="#00c853" stroke-width="6" stroke-linecap="round"
                  :stroke-dasharray="ringArc(animPct(plan.qualifiedPct), ringRMiddle) + ' ' + circOf(ringRMiddle)"
                  stroke-dashoffset="0"
                  transform="rotate(-90 50 50)"
                />
                <!-- 入库（内环） -->
                <circle
                  cx="50" cy="50" :r="ringRInner"
                  fill="none" stroke="#ff9100" stroke-width="6" stroke-linecap="round"
                  :stroke-dasharray="ringArc(animPct(plan.inboundPct), ringRInner) + ' ' + circOf(ringRInner)"
                  stroke-dashoffset="0"
                  transform="rotate(-90 50 50)"
                />
              </svg>
            </div>
            <!-- 完成率标识 -->
            <div class="plan-rate-badge" :class="{ 'is-done': plan.rate >= 100 }">
              <template v-if="plan.rate >= 100">
                <svg class="rate-done-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
                  <polyline points="22 4 12 14.01 9 11.01"/>
                </svg>
                <span>已完成</span>
              </template>
              <template v-else>
                <span class="rate-pct">{{ plan.rate }}%</span>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { websocketUrl } from '@/config/runtimeConfig'

// ---------- WebSocket 数据（响应式） ----------
const productData = ref({
  totalInbound: 0,
  totalQualified: 0,
  totalOutput: 0,
  totalPlanned: 0,
  qualifyRate: 0,
  inboundSeries: [],
  outboundSeries: [],
  plans: [],
})

// ---------- WebSocket 客户端 ----------
let stompClient = null
let hasReceivedData = false
let mountFallbackTimer = null

function triggerEnterAnimation() {
  if (hasReceivedData) return
  hasReceivedData = true
  if (mountFallbackTimer) {
    clearTimeout(mountFallbackTimer)
    mountFallbackTimer = null
  }
  requestAnimationFrame(() => {
    mounted.value = true
    // 环形图动画延迟启动，与 CSS 入场动画衔接
    ringTimer = setTimeout(() => {
      ringAnimProgress.value = 1
    }, 180)
  })
}

function connectWebSocket() {
  stompClient = new Client({
    webSocketFactory: () => new SockJS(websocketUrl),
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe('/topic/product', (message) => {
        try {
          const payload = JSON.parse(message.body)
          productData.value = {
            totalInbound: payload.totalInbound || 0,
            totalQualified: payload.totalQualified || 0,
            totalOutput: payload.totalOutput || 0,
            totalPlanned: payload.totalPlanned || 0,
            qualifyRate: payload.qualifyRate || 0,
            inboundSeries: payload.inboundSeries || [],
            outboundSeries: payload.outboundSeries || [],
            plans: payload.plans || [],
          }
          // 首帧数据到达后触发入场动画
          triggerEnterAnimation()
        } catch (e) {
          // 解析失败，忽略
        }
      })
    },
    onStompError: (frame) => {
      console.error('STOMP 错误:', frame.headers['message'])
    }
  })

  stompClient.activate()
}

function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
}

// ---------- 格式化数字 ----------
function formatNum(n) {
  return n != null ? n.toLocaleString('en-US') : '0'
}

// ---------- 折线图参数 ----------
const chartW = 400
const chartH = 200
const chartPadL = 40
const chartPadR = 12
const chartPadT = 16
const chartPadB = 24

const plotW = chartW - chartPadL - chartPadR
const plotH = chartH - chartPadT - chartPadB

// ---------- 环形图半径 ----------
const ringROuter = 38
const ringRMiddle = 28
const ringRInner = 18
function circOf(r) { return 2 * Math.PI * r }
function ringArc(pct, r) { return circOf(r) * pct / 100 }

const yTicks = computed(() => {
  const inbound = productData.value.inboundSeries
  const outbound = productData.value.outboundSeries
  if (!inbound.length && !outbound.length) return []
  const allVals = [...inbound, ...outbound]
  const max = Math.max(...allVals) || 1
  const step = Math.ceil(max / 100) * 100 / 4
  const ticks = []
  for (let i = 0; i <= 4; i++) {
    const val = Math.ceil(step * i / 50) * 50
    ticks.push({
      label: val,
      y: chartPadT + plotH - (val / (step * 4)) * plotH,
    })
  }
  return ticks
})

function valueToY(v) {
  const inbound = productData.value.inboundSeries
  const outbound = productData.value.outboundSeries
  const allVals = [...inbound, ...outbound]
  const max = Math.max(...allVals) || 1
  const step = Math.ceil(max / 100) * 100
  return chartPadT + plotH - (v / step) * plotH
}

const xLabels = computed(() => {
  const labels = []
  const series = productData.value.inboundSeries
  if (!series.length) return labels
  const step = Math.ceil(series.length / 6)
  for (let i = 0; i < series.length; i++) {
    if (i % step === 0 || i === series.length - 1) {
      labels.push({ text: (i + 1) + '日', pos: chartPadL + (i / (series.length - 1)) * plotW })
    }
  }
  return labels
})

const xTicksPos = computed(() => {
  const series = productData.value.inboundSeries
  return series.map((_, i) => chartPadL + (i / (series.length - 1)) * plotW)
})

const inboundPts = computed(() => {
  const series = productData.value.inboundSeries
  if (!series.length) return []
  return series.map((v, i) => ({
    x: chartPadL + (i / (series.length - 1)) * plotW,
    y: valueToY(v),
  }))
})

const outboundPts = computed(() => {
  const series = productData.value.outboundSeries
  if (!series.length) return []
  return series.map((v, i) => ({
    x: chartPadL + (i / (series.length - 1)) * plotW,
    y: valueToY(v),
  }))
})

const inboundPoints = computed(() =>
  inboundPts.value.map(p => `${p.x},${p.y}`).join(' ')
)

const outboundPoints = computed(() =>
  outboundPts.value.map(p => `${p.x},${p.y}`).join(' ')
)

const inboundArea = computed(() => {
  const pts = inboundPts.value
  if (!pts.length) return ''
  const bottom = chartH - chartPadB
  return `${chartPadL},${bottom} ` + pts.map(p => `${p.x},${p.y}`).join(' ') + ` ${pts[pts.length - 1].x},${bottom}`
})

const outboundArea = computed(() => {
  const pts = outboundPts.value
  if (!pts.length) return ''
  const bottom = chartH - chartPadB
  return `${chartPadL},${bottom} ` + pts.map(p => `${p.x},${p.y}`).join(' ') + ` ${pts[pts.length - 1].x},${bottom}`
})

// 当日出入库数量（取最新一天）
const todayInbound = computed(() => {
  const arr = productData.value.inboundSeries
  return arr.length ? arr[arr.length - 1] : 0
})
const todayOutbound = computed(() => {
  const arr = productData.value.outboundSeries
  return arr.length ? arr[arr.length - 1] : 0
})

// ---------- 折线图 hover 交互 ----------
const chartSvgRef = ref(null)
const hoverIndex = ref(null)

const hoverLineX = computed(() => {
  if (hoverIndex.value === null) return 0
  const series = productData.value.inboundSeries
  if (!series.length) return 0
  return chartPadL + (hoverIndex.value / (series.length - 1)) * plotW
})

const hoverInboundVal = computed(() => {
  if (hoverIndex.value === null) return 0
  return formatNum(productData.value.inboundSeries[hoverIndex.value])
})

const hoverOutboundVal = computed(() => {
  if (hoverIndex.value === null) return 0
  return formatNum(productData.value.outboundSeries[hoverIndex.value])
})

const tooltipX = computed(() => {
  if (hoverIndex.value === null) return 0
  const x = hoverLineX.value
  return Math.min(Math.max(x - 45, chartPadL), chartW - chartPadR - 95)
})

const tooltipY = computed(() => chartPadT + 4)

function onChartMouseMove(e) {
  const svg = chartSvgRef.value
  if (!svg) return
  const rect = svg.getBoundingClientRect()
  const scaleX = chartW / rect.width
  const mx = (e.clientX - rect.left) * scaleX

  if (mx < chartPadL || mx > chartW - chartPadR) {
    hoverIndex.value = null
    return
  }

  const series = productData.value.inboundSeries
  if (!series.length) return
  const idx = Math.round(((mx - chartPadL) / plotW) * (series.length - 1))
  hoverIndex.value = Math.max(0, Math.min(idx, series.length - 1))
}

function onChartMouseLeave() {
  hoverIndex.value = null
}

// 环形图百分比（计算属性：自动附加 computed fields）
const enrichedPlans = computed(() => {
  return productData.value.plans.map(p => {
    const total = p.planQty || 1
    const output = p.output || 0
    const qualified = p.qualified || 0
    const inbound = p.inbound || 0
    return {
      ...p,
      outputPct: Math.round((output / total) * 100),
      qualifiedPct: Math.round((qualified / total) * 100),
      inboundPct: Math.round((inbound / total) * 100),
    }
  })
})

// ---------- 开场动画控制 ----------
const mounted = ref(false)
const ringAnimProgress = ref(0)
let ringTimer = null

onMounted(() => {
  connectWebSocket()
  // 兜底：5 秒后强制触发动画
  mountFallbackTimer = setTimeout(() => {
    triggerEnterAnimation()
  }, 5000)
})

// 监听 plans 数据变化，重新触发环形图动画
let ringAnimFrame = null
watch(
  () => productData.value.plans,
  () => {
    // 取消待执行的动画帧
    if (ringAnimFrame !== null) {
      cancelAnimationFrame(ringAnimFrame)
    }
    // 归零 → 浏览器绘制空环 → 再过渡到目标值
    ringAnimProgress.value = 0
    ringAnimFrame = requestAnimationFrame(() => {
      ringAnimFrame = requestAnimationFrame(() => {
        ringAnimProgress.value = 1
      })
    })
  },
  { deep: true }
)

onUnmounted(() => {
  if (mountFallbackTimer) {
    clearTimeout(mountFallbackTimer)
  }
  if (ringTimer !== null) {
    clearTimeout(ringTimer)
  }
  if (ringAnimFrame !== null) {
    cancelAnimationFrame(ringAnimFrame)
  }
  disconnectWebSocket()
})

// 折线路径长度（用于 stroke-dasharray 绘制动画）
function polylineLen(pts) {
  let len = 0
  for (let i = 1; i < pts.length; i++) {
    const dx = pts[i].x - pts[i - 1].x
    const dy = pts[i].y - pts[i - 1].y
    len += Math.sqrt(dx * dx + dy * dy)
  }
  return len
}
const inboundPathLen = computed(() => polylineLen(inboundPts.value))
const outboundPathLen = computed(() => polylineLen(outboundPts.value))

// 环形图动画百分比：未触发时返回 0
function animPct(pct) {
  return ringAnimProgress.value * pct
}
</script>

<style scoped>
.product-panel {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow: hidden;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Segoe UI', sans-serif;
}

.module-section {
  position: relative;
  background: linear-gradient(180deg, rgba(9, 21, 50, 0.68), rgba(7, 15, 35, 0.5));
  border: 1px solid rgba(0, 200, 255, 0.12);
  border-radius: 12px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    inset 0 0 30px rgba(0, 200, 255, 0.04);
  overflow: hidden;
}

.module-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.02), transparent),
    radial-gradient(circle at top, rgba(0, 200, 255, 0.05), transparent 42%);
  pointer-events: none;
}

.module-section::after {
  content: '';
  position: absolute;
  top: 0;
  left: 14%;
  right: 14%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 200, 255, 0.28), transparent);
  pointer-events: none;
}

/* ========== 上部分：数据总览 ========== */
.stats-wrap {
  flex-shrink: 0;
  padding: 14px 16px 16px;
}

.stats-section {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, rgba(10, 22, 52, 0.8), rgba(7, 15, 35, 0.56));
  border: 1px solid rgba(0, 200, 255, 0.08);
  border-radius: 10px;
  padding: 16px 18px;
  position: relative;
  overflow: hidden;
  opacity: 0;
  transform: translateY(12px);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
  will-change: transform, opacity;
}

.stat-item.stat-item--enter {
  animation: fadeSlideUp var(--screen-enter-duration, 0.56s) var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
  animation-delay: var(--delay);
}
.stat-item::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 200, 255, 0.3), transparent);
}

.stat-item::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(0, 200, 255, 0.05), transparent 32%, transparent 70%, rgba(0, 255, 208, 0.04));
  pointer-events: none;
}

.stat-item--green::before {
  background: linear-gradient(120deg, rgba(0, 230, 118, 0.06), transparent 32%, transparent 70%, rgba(0, 255, 208, 0.04));
}

.stat-item--orange::before {
  background: linear-gradient(120deg, rgba(255, 145, 0, 0.06), transparent 32%, transparent 70%, rgba(255, 145, 0, 0.03));
}

.stat-icon {
  width: 50px;
  height: 50px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.stat-icon svg { width: 26px; height: 26px; }
.inbound-icon  { background: rgba(0, 204, 255, 0.1);  color: #00ccff; }
.qualified-icon{ background: rgba(0, 230, 118, 0.1);  color: #00e676; }
.output-icon   { background: rgba(255, 145, 0, 0.1);  color: #ff9100; }

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.stat-label {
  font-size: 13px;
  color: rgba(180, 210, 240, 0.65);
  letter-spacing: 0.5px;
  white-space: nowrap;
}
.stat-sub {
  font-size: 11px;
  color: rgba(180, 210, 240, 0.4);
  letter-spacing: 0;
  margin-top: -2px;
}
.stat-value {
  font-size: 30px;
  font-weight: 700;
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  color: #e0f0ff;
  text-shadow: 0 0 8px rgba(0, 200, 255, 0.4);
  line-height: 1.1;
  letter-spacing: 1px;
  animation: statGlow 1.6s ease 0.3s;
  animation-play-state: paused;
}
@keyframes statGlow {
  0%   { text-shadow: 0 0 2px rgba(0, 200, 255, 0); }
  40%  { text-shadow: 0 0 20px rgba(0, 200, 255, 0.7); }
  100% { text-shadow: 0 0 8px rgba(0, 200, 255, 0.4); }
}
.stat-value.accent-green { color: #00e676; text-shadow: 0 0 8px rgba(0, 230, 118, 0.4); animation-name: statGlowGreen; }
.stat-value.accent-orange { color: #ff9100; text-shadow: 0 0 8px rgba(255, 145, 0, 0.4); animation-name: statGlowOrange; }
.mounted .stat-value {
  animation-play-state: running;
}
@keyframes statGlowGreen {
  0%   { text-shadow: 0 0 2px rgba(0, 230, 118, 0); }
  40%  { text-shadow: 0 0 20px rgba(0, 230, 118, 0.7); }
  100% { text-shadow: 0 0 8px rgba(0, 230, 118, 0.4); }
}
@keyframes statGlowOrange {
  0%   { text-shadow: 0 0 2px rgba(255, 145, 0, 0); }
  40%  { text-shadow: 0 0 20px rgba(255, 145, 0, 0.7); }
  100% { text-shadow: 0 0 8px rgba(255, 145, 0, 0.4); }
}

/* ========== 中部分：折线图 ========== */
.chart-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 14px 16px 12px;
  overflow: hidden;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}
.label-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 200, 255, 0.3), transparent);
}
.label-text {
  font-size: 13px;
  color: rgba(0, 200, 255, 0.8);
  letter-spacing: 3px;
  white-space: nowrap;
  text-shadow: 0 0 6px rgba(0, 200, 255, 0.3);
}

.chart-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  min-height: 0;
  z-index: 1;
}

.chart-legend {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  margin-bottom: 4px;
  flex-shrink: 0;
}
.legend-item {
  font-size: 12px;
  font-weight: 600;
  color: rgba(180, 210, 240, 0.7);
  display: flex;
  align-items: center;
  gap: 5px;
}
.legend-dot {
  width: 10px;
  height: 4px;
  border-radius: 2px;
  display: inline-block;
}
.inbound-dot  { background: #00ccff; box-shadow: 0 0 6px rgba(0, 204, 255, 0.6); }
.outbound-dot { background: #ff9100; box-shadow: 0 0 6px rgba(255, 145, 0, 0.6); }

.line-chart-svg {
  flex: 1;
  width: 100%;
  min-height: 0;
}

/* 当日出入库数量 */
.today-summary {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-top: 8px;
  flex-shrink: 0;
  flex-wrap: wrap;
}
.today-item {
  font-size: 12px;
  color: rgba(180, 210, 240, 0.6);
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(7, 16, 38, 0.56);
  border: 1px solid rgba(0, 200, 255, 0.08);
  opacity: 0;
  transform: translateY(10px);
  will-change: transform, opacity;
}
.today-item.today-item--enter {
  animation: fadeSlideUp 0.46s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
}
.inbound-today.today-item--enter { animation-delay: 0.56s; }
.outbound-today.today-item--enter { animation-delay: 0.68s; }
.today-item strong {
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  font-size: 16px;
  font-weight: 700;
  margin: 0 2px;
}
.inbound-today strong { color: #00ccff; text-shadow: 0 0 6px rgba(0, 204, 255, 0.5); }
.outbound-today strong { color: #ff9100; text-shadow: 0 0 6px rgba(255, 145, 0, 0.5); }

/* ========== 下部分：生产计划列表 ========== */
.plan-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 14px 16px 16px;
}

.plan-top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
  flex-shrink: 0;
}
.plan-top-bar .section-label {
  flex: 1;
  margin-bottom: 0;
}

.plan-color-legend {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}
.plan-color-legend .legend-tag {
  font-size: 12px;
  color: rgba(180, 210, 240, 0.5);
}

.plan-list {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
  padding: 8px 8px 12px;
  width: 100%;
}

.plan-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
}
.plan-scroll::-webkit-scrollbar {
  width: 4px;
}
.plan-scroll::-webkit-scrollbar-track {
  background: transparent;
}
.plan-scroll::-webkit-scrollbar-thumb {
  background: rgba(0, 200, 255, 0.18);
  border-radius: 999px;
}

.plan-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
  width: 90%;
  opacity: 0;
  transform: translateY(14px);
  padding: 10px 16px;
  background: linear-gradient(135deg, rgba(10, 22, 55, 0.78), rgba(7, 15, 35, 0.56));
  border: 1px solid rgba(0, 200, 255, 0.08);
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
  will-change: transform, opacity;
}

.plan-item.plan-item--enter {
  animation: fadeSlideUp var(--screen-enter-duration, 0.56s) var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
  animation-delay: calc(0.18s + var(--delay));
}
.plan-item::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 200, 255, 0.25), transparent);
  opacity: 0.6;
}

/* 环形图容器 */
.plan-ring-wrap {
  flex-shrink: 0;
  width: 88px;
  height: 88px;
}
.plan-ring-svg {
  width: 100%;
  height: 100%;
  display: block;
}

/* 环形图-弧段动画（由下方动画区统一定义） */

/* 完成率标识 */
.plan-rate-badge {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 56px;
}
.rate-pct {
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  font-size: 22px;
  font-weight: 700;
  color: rgba(200, 225, 250, 0.75);
}
.plan-rate-badge.is-done {
  gap: 3px;
}
.plan-rate-badge.is-done span {
  font-size: 12px;
  color: #00e676;
  font-weight: 600;
  text-shadow: 0 0 6px rgba(0, 230, 118, 0.3);
  white-space: nowrap;
}
.rate-done-icon {
  width: 24px;
  height: 24px;
  color: #00e676;
  filter: drop-shadow(0 0 4px rgba(0, 230, 118, 0.4));
}

/* 信息区 */
.plan-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 计划名称行 */
.plan-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.plan-index {
  flex-shrink: 0;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  font-size: 13px;
  font-weight: 700;
  color: rgba(0, 200, 255, 0.7);
  background: rgba(0, 200, 255, 0.08);
  border: 1px solid rgba(0, 200, 255, 0.2);
  border-radius: 4px;
  letter-spacing: 0;
}
.plan-name {
  font-size: 15px;
  font-weight: 600;
  color: rgba(210, 230, 250, 0.92);
  letter-spacing: 0.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

/* 产量/计划 数字行 */
.plan-nums {
  display: flex;
  align-items: baseline;
  gap: 6px;
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  white-space: nowrap;
  padding-left: 36px;
}
.num-label {
  font-size: 12px;
  color: rgba(180, 210, 240, 0.4);
  letter-spacing: 1px;
}
.num-val {
  color: #00aaff;
  font-weight: 700;
  font-size: 24px;
  text-shadow: 0 0 8px rgba(0, 170, 255, 0.5);
  line-height: 1;
}
.num-sep {
  color: rgba(180, 210, 240, 0.2);
  font-size: 16px;
  margin: 0 2px;
}
.num-total {
  color: rgba(180, 210, 240, 0.55);
  font-size: 16px;
  font-weight: 600;
}
.num-unit {
  color: rgba(180, 210, 240, 0.3);
  font-size: 12px;
  margin-left: 2px;
}

/* 额外信息行 */
.plan-extra {
  display: flex;
  gap: 20px;
  padding-left: 36px;
}
.extra-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  background: rgba(10, 18, 40, 0.6);
  border: 1px solid rgba(0, 200, 255, 0.08);
  border-radius: 999px;
}
.extra-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.extra-label {
  font-size: 12px;
  color: rgba(180, 210, 240, 0.5);
  letter-spacing: 0.5px;
}
.extra-val {
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  font-size: 14px;
  font-weight: 700;
  color: rgba(210, 230, 250, 0.8);
}
.extra-qualified .extra-dot { background: #00c853; box-shadow: 0 0 4px rgba(0, 200, 83, 0.5); }
.extra-inbound .extra-dot   { background: #ff9100; box-shadow: 0 0 4px rgba(255, 145, 0, 0.5); }

/* 颜色图例标签（统一样式） */
.legend-tag {
  font-size: 11px;
  color: rgba(180, 210, 240, 0.5);
  display: flex;
  align-items: center;
  gap: 5px;
}
.legend-tag::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 2px;
  flex-shrink: 0;
}
.tag-output::before    { background: #00aaff; box-shadow: 0 0 3px rgba(0, 170, 255, 0.5); }
.tag-qualified::before { background: #00c853; box-shadow: 0 0 3px rgba(0, 200, 83, 0.5); }
.tag-inbound::before   { background: #ff9100; box-shadow: 0 0 3px rgba(255, 145, 0, 0.5); }

/* ========== 动画 ========== */
@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(var(--screen-enter-offset, 14px)); }
  to   { opacity: 1; transform: translateY(0); }
}

/* 折线绘制动画 */
.chart-line-draw {
  transition: stroke-dashoffset 1.05s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) 0.12s;
}
.chart-line-draw-out {
  transition-delay: 0.24s;
}

/* 面积渐显动画 */
.chart-area-fade {
  transition: opacity 0.72s ease 0.32s;
}

/* 数据点弹出动画 */
.chart-dot-pop {
  opacity: 0;
}
.mounted .chart-dot-pop {
  animation: dotPopIn 0.34s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
  animation-delay: var(--dot-delay);
}
@keyframes dotPopIn {
  from { opacity: 0; r: 0; }
  to   { opacity: 1; r: 2.5; }
}

/* 环形图弧段动画增强 */
.plan-ring-svg circle[stroke-dasharray] {
  transition: stroke-dashoffset 0.9s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)),
              stroke-dasharray 0.9s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1));
}

@media (max-width: 1600px), (max-height: 1080px) {
  .product-panel {
    gap: 10px;
  }

  .stats-wrap,
  .chart-section,
  .plan-section {
    padding: 10px 12px;
  }

  .section-label {
    margin-bottom: 6px;
    gap: 8px;
  }

  .label-text {
    font-size: 12px;
    letter-spacing: 2px;
  }

  .stats-section {
    gap: 8px;
  }

  .stat-item {
    padding: 10px 12px;
    gap: 8px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
  }

  .stat-icon svg {
    width: 20px;
    height: 20px;
  }

  .stat-label {
    font-size: 11px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-sub {
    font-size: 10px;
  }

  .chart-legend {
    gap: 12px;
  }

  .legend-item {
    font-size: 11px;
  }

  .today-summary {
    gap: 10px;
    margin-top: 4px;
  }

  .today-item {
    font-size: 11px;
    padding: 4px 10px;
  }

  .today-item strong {
    font-size: 14px;
  }

  .plan-top-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .plan-color-legend {
    justify-content: flex-end;
  }

  .plan-list {
    gap: 6px;
    padding: 4px 4px 8px;
  }

  .plan-item {
    gap: 10px;
    padding: 8px 10px;
    width: 95%;
  }

  .plan-ring-wrap {
    width: 72px;
    height: 72px;
  }

  .plan-info {
    gap: 4px;
  }

  .plan-name-row {
    gap: 6px;
  }

  .plan-name {
    font-size: 13px;
  }

  .plan-index {
    width: 22px;
    height: 22px;
    font-size: 11px;
  }

  .plan-nums {
    gap: 4px;
    padding-left: 28px;
  }

  .num-val {
    font-size: 18px;
  }

  .num-total {
    font-size: 14px;
  }

  .plan-extra {
    gap: 8px;
    padding-left: 28px;
    flex-wrap: wrap;
  }

  .extra-item {
    padding: 2px 8px;
    gap: 4px;
  }

  .extra-val {
    font-size: 12px;
  }

  .plan-rate-badge {
    min-width: 48px;
  }

  .rate-pct {
    font-size: 18px;
  }

  .rate-done-icon {
    width: 20px;
    height: 20px;
  }

  .plan-rate-badge.is-done span {
    font-size: 10px;
  }
}

@media (max-width: 1366px), (max-height: 820px) {
  .product-panel {
    gap: 8px;
  }

  .stats-section {
    gap: 6px;
  }

  .stat-item {
    padding: 8px 10px;
    gap: 6px;
  }

  .stat-icon {
    width: 36px;
    height: 36px;
  }

  .stat-icon svg {
    width: 18px;
    height: 18px;
  }

  .stat-value {
    font-size: 22px;
  }

  .chart-legend {
    gap: 10px;
  }

  .today-summary {
    gap: 8px;
  }

  .plan-item {
    gap: 8px;
    padding: 6px 8px;
  }

  .plan-ring-wrap {
    width: 64px;
    height: 64px;
  }

  .plan-name {
    font-size: 12px;
  }

  .num-val {
    font-size: 16px;
  }

  .num-total {
    font-size: 13px;
  }

  .plan-extra {
    gap: 6px;
  }
}
</style>
