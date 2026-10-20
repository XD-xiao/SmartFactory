<template>
  <div class="sensor-panel" :class="{ mounted }">
    <!-- 传感器状态折线图列表 -->
    <div class="sensor-section module-section">
      <div class="section-label">
        <span class="label-line"></span>
        <span class="label-text">传感器实时数据</span>
        <span class="label-line"></span>
      </div>

      <!-- 状态颜色图例 -->
      <div class="status-legend" :class="{ 'legend-enter': mounted }">
        <span class="legend-item">
          <i class="legend-dot dot-normal"></i>正常
        </span>
        <span class="legend-item">
          <i class="legend-dot dot-warning"></i>预警
        </span>
        <span class="legend-item">
          <i class="legend-dot dot-abnormal"></i>异常
        </span>
      </div>

      <el-scrollbar class="sensor-scrollbar">
        <div v-if="sensorList.length === 0" class="empty-state">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
          <span class="empty-text">暂无传感器数据</span>
        </div>
        <div v-else class="sensor-list">
          <div
            v-for="(sensor, index) in sensorList"
            :key="sensor.id"
            class="sensor-card"
            :class="{ 'sensor-card--enter': mounted }"
            :style="{ '--card-delay': index * 0.08 + 's' }"
          >
            <!-- 左侧：传感器基础信息 -->
            <div class="sensor-info">
              <div class="sensor-name-row">
                <span
                  class="status-indicator"
                  :class="statusClass(sensor.latestStatus)"
                ></span>
                <span class="sensor-name">{{ sensor.name }}</span>
              </div>
              <div class="sensor-scene">
                <svg class="scene-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
                  <polyline points="9 22 9 12 15 12 15 22"/>
                </svg>
                <span>场景 {{ sensor.sceneType }}</span>
              </div>
              <div class="sensor-latest-value">
                <span class="value-label">当前值</span>
                <span
                  class="value-num"
                  :class="statusClass(sensor.latestStatus)"
                >{{ sensor.lastValue ?? '--' }}</span>
              </div>
            </div>

            <!-- 右侧：波动折线图 -->
            <div class="sensor-chart">
              <svg
                class="mini-chart-svg"
                :viewBox="`0 0 ${chartW} ${chartH}`"
                preserveAspectRatio="xMidYMid meet"
              >
                <defs>
                  <linearGradient :id="'areaGrad-' + sensor.id" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#00ccff" stop-opacity="0.5"/>
                    <stop offset="100%" stop-color="#00ccff" stop-opacity="0"/>
                  </linearGradient>
                </defs>
                <!-- 网格线 -->
                <line
                  v-for="(y, i) in chartGridYs"
                  :key="'g' + i"
                  :x1="chartPadL" :y1="y" :x2="chartW - chartPadR" :y2="y"
                  stroke="rgba(0, 200, 255, 0.05)" stroke-width="0.5"
                />
                <!-- 面积填充 -->
                <polygon
                  v-if="chartArea(sensor.records)"
                  :points="chartArea(sensor.records)"
                  :fill="'url(#areaGrad-' + sensor.id + ')'"
                  class="chart-area-fade"
                  :style="{ opacity: mounted ? 0.18 : 0 }"
                />
                <!-- 折线段（按状态着色） -->
                <line
                  v-for="(seg, si) in chartSegments(sensor.records)"
                  :key="'seg' + si"
                  :x1="seg.x1" :y1="seg.y1"
                  :x2="seg.x2" :y2="seg.y2"
                  :stroke="seg.color"
                  stroke-width="1.6"
                  stroke-linecap="round"
                  class="chart-line-draw"
                  :stroke-dasharray="seg.len"
                  :stroke-dashoffset="mounted ? 0 : seg.len"
                  :style="{ 'transition-delay': (0.15 + si * 0.02) + 's' }"
                />
              </svg>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { websocketUrl } from '@/config/runtimeConfig'

// ---------- 动画开关 ----------
const mounted = ref(false)

// ---------- WebSocket 数据 ----------
const sensorData = ref({
  sensors: []
})

const sensorList = computed(() => {
  return (sensorData.value.sensors || []).map(s => {
    const records = s.records || []
    const lastRecord = records.length > 0 ? records[records.length - 1] : null
    return {
      ...s,
      lastValue: lastRecord ? lastRecord.value : null,
      latestStatus: lastRecord ? lastRecord.status : 0,
      records: records
    }
  })
})

// ---------- 状态颜色映射 ----------
const STATUS_COLORS = {
  0: { color: '#00e676', class: 'status-normal', label: '正常' },
  1: { color: '#ff9100', class: 'status-warning', label: '预警' },
  2: { color: '#ff4081', class: 'status-abnormal', label: '异常' }
}

function statusClass(status) {
  return STATUS_COLORS[status]?.class || 'status-normal'
}

function statusColor(status) {
  return STATUS_COLORS[status]?.color || '#00e676'
}

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
  })
}

function connectWebSocket() {
  stompClient = new Client({
    webSocketFactory: () => new SockJS(websocketUrl),
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe('/topic/sensor', (message) => {
        try {
          const payload = JSON.parse(message.body)
          sensorData.value = {
            sensors: payload.sensors || []
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

// ---------- 折线图参数 ----------
const chartW = 280
const chartH = 70
const chartPadL = 4
const chartPadR = 4
const chartPadT = 6
const chartPadB = 6

const plotW = chartW - chartPadL - chartPadR
const plotH = chartH - chartPadT - chartPadB

// 水平网格线位置
const chartGridYs = computed(() => {
  const innerH = plotH
  const top = chartPadT
  return [
    top,
    top + innerH * 0.33,
    top + innerH * 0.66,
    top + innerH
  ]
})

function getValueRange(records) {
  if (!records || records.length === 0) return { min: 0, max: 1 }
  const values = records.map(r => r.value).filter(v => v != null)
  if (values.length === 0) return { min: 0, max: 1 }
  const min = Math.min(...values)
  const max = Math.max(...values)
  if (min === max) return { min: min - 1, max: max + 1 }
  const padding = (max - min) * 0.15
  return { min: min - padding, max: max + padding }
}

function valueToY(v, range) {
  if (range.max === range.min) return chartPadT + plotH / 2
  const ratio = (v - range.min) / (range.max - range.min)
  return chartPadT + plotH * (1 - ratio)
}

function chartPoints(records) {
  if (!records || records.length < 2) return []
  const range = getValueRange(records)
  return records.map((r, i) => ({
    x: chartPadL + (i / (records.length - 1)) * plotW,
    y: valueToY(r.value, range),
    status: r.status != null ? r.status : 0,
    value: r.value
  }))
}

function chartSegments(records) {
  const pts = chartPoints(records)
  const segs = []
  for (let i = 0; i < pts.length - 1; i++) {
    const p1 = pts[i]
    const p2 = pts[i + 1]
    const dx = p2.x - p1.x
    const dy = p2.y - p1.y
    const len = Math.sqrt(dx * dx + dy * dy)
    segs.push({
      x1: p1.x, y1: p1.y,
      x2: p2.x, y2: p2.y,
      color: statusColor(p2.status),
      len: len
    })
  }
  return segs
}

function chartArea(records) {
  const pts = chartPoints(records)
  if (pts.length < 2) return ''
  const bottom = chartH - chartPadB
  return `${chartPadL},${bottom} ` + pts.map(p => `${p.x},${p.y}`).join(' ') + ` ${pts[pts.length - 1].x},${bottom}`
}

// ---------- 生命周期 ----------
onMounted(() => {
  connectWebSocket()
  // 兜底：5 秒后强制触发动画
  mountFallbackTimer = setTimeout(() => {
    triggerEnterAnimation()
  }, 5000)
})

onUnmounted(() => {
  if (mountFallbackTimer) {
    clearTimeout(mountFallbackTimer)
  }
  disconnectWebSocket()
})
</script>

<style scoped>
/* ==================== 根容器 ==================== */
.sensor-panel {
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
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, rgba(9, 21, 50, 0.68), rgba(7, 15, 35, 0.5));
  border: 1px solid rgba(0, 200, 255, 0.12);
  border-radius: 12px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    inset 0 0 30px rgba(0, 200, 255, 0.04);
  overflow: hidden;
  padding: 14px 16px;
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

/* ==================== 分区标题 ==================== */
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

/* ==================== 状态图例 ==================== */
.status-legend {
  display: flex;
  justify-content: flex-end;
  gap: 18px;
  margin-bottom: 8px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  opacity: 0;
  transition: opacity 0.46s cubic-bezier(0.22, 1, 0.36, 1);
}

.status-legend.legend-enter {
  opacity: 1;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: rgba(180, 200, 220, 0.7);
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(7, 16, 38, 0.56);
  border: 1px solid rgba(0, 200, 255, 0.08);
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-normal   { background: #00e676; box-shadow: 0 0 5px rgba(0, 230, 118, 0.5); }
.dot-warning  { background: #ff9100; box-shadow: 0 0 5px rgba(255, 145, 0, 0.5); }
.dot-abnormal { background: #ff4081; box-shadow: 0 0 5px rgba(255, 64, 129, 0.5); }

/* ==================== 滚动区域 ==================== */
.sensor-scrollbar {
  flex: 1;
  min-height: 0;
  position: relative;
  z-index: 1;
}

.sensor-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 2px 2px 10px;
}

/* ==================== 空状态 ==================== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  gap: 12px;
}

.empty-icon {
  width: 48px;
  height: 48px;
  color: rgba(0, 200, 255, 0.2);
}

.empty-text {
  font-size: 13px;
  color: rgba(180, 200, 220, 0.4);
  letter-spacing: 1px;
}

/* ==================== 传感器卡片 ==================== */
.sensor-card {
  position: relative;
  display: flex;
  align-items: stretch;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(10, 22, 52, 0.78), rgba(7, 15, 35, 0.56));
  border: 1px solid rgba(0, 200, 255, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
  opacity: 0;
  transform: translateY(14px);
  will-change: transform, opacity;
  overflow: hidden;
}

.sensor-card.sensor-card--enter {
  animation: fadeSlideUp var(--screen-enter-duration, 0.56s) var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
  animation-delay: var(--card-delay);
}

.sensor-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 200, 255, 0.18), transparent);
  pointer-events: none;
}

/* ==================== 左侧传感器信息 ==================== */
.sensor-info {
  flex-shrink: 0;
  width: 120px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.sensor-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-indicator.status-normal {
  background: #00e676;
  box-shadow: 0 0 6px rgba(0, 230, 118, 0.4);
}

.status-indicator.status-warning {
  background: #ff9100;
  box-shadow: 0 0 6px rgba(255, 145, 0, 0.5);
  animation: pulse-warn 1.8s infinite;
}

.status-indicator.status-abnormal {
  background: #ff4081;
  box-shadow: 0 0 8px rgba(255, 64, 129, 0.6);
  animation: pulse-abnormal 1s infinite;
}

.sensor-name {
  font-size: 14px;
  font-weight: 600;
  color: rgba(210, 230, 250, 0.92);
  letter-spacing: 0.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sensor-scene {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: rgba(180, 210, 240, 0.5);
  letter-spacing: 0.5px;
}

.scene-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  color: rgba(0, 200, 255, 0.3);
}

.sensor-latest-value {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.value-label {
  font-size: 10px;
  color: rgba(180, 210, 240, 0.4);
  letter-spacing: 0.5px;
}

.value-num {
  font-size: 20px;
  font-weight: 700;
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  line-height: 1;
}

.value-num.status-normal {
  color: #00e676;
  text-shadow: 0 0 6px rgba(0, 230, 118, 0.35);
}

.value-num.status-warning {
  color: #ff9100;
  text-shadow: 0 0 6px rgba(255, 145, 0, 0.35);
}

.value-num.status-abnormal {
  color: #ff4081;
  text-shadow: 0 0 8px rgba(255, 64, 129, 0.45);
}

/* ==================== 右侧折线图 ==================== */
.sensor-chart {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mini-chart-svg {
  width: 100%;
  height: 100%;
  max-height: 60px;
}

.chart-area-fade {
  transition: opacity 0.72s ease 0.32s;
}

.chart-line-draw {
  transition: stroke-dashoffset 0.72s cubic-bezier(0.22, 1, 0.36, 1) 0.12s;
}

/* ==================== 动画 ==================== */
@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(var(--screen-enter-offset, 14px));
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse-warn {
  0%, 100% { box-shadow: 0 0 4px rgba(255, 145, 0, 0.3), 0 0 8px rgba(255, 145, 0, 0.1); }
  50% { box-shadow: 0 0 6px rgba(255, 145, 0, 0.7), 0 0 12px rgba(255, 145, 0, 0.25); }
}

@keyframes pulse-abnormal {
  0%, 100% { box-shadow: 0 0 4px rgba(255, 64, 129, 0.4), 0 0 10px rgba(255, 64, 129, 0.15); }
  50% { box-shadow: 0 0 8px rgba(255, 64, 129, 0.9), 0 0 16px rgba(255, 64, 129, 0.3); }
}

/* ==================== 响应式适配 ==================== */
@media (max-width: 1600px), (max-height: 1080px) {
  .sensor-panel {
    gap: 10px;
  }

  .module-section {
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

  .status-legend {
    margin-bottom: 6px;
    gap: 12px;
  }

  .legend-item {
    font-size: 10px;
    padding: 2px 8px;
  }

  .sensor-list {
    gap: 8px;
    padding: 1px 1px 8px;
  }

  .sensor-card {
    padding: 10px 12px;
    gap: 10px;
  }

  .sensor-info {
    width: 100px;
    gap: 6px;
  }

  .sensor-name {
    font-size: 13px;
  }

  .value-num {
    font-size: 18px;
  }

  .mini-chart-svg {
    max-height: 50px;
  }
}

@media (max-width: 1366px), (max-height: 820px) {
  .sensor-panel {
    gap: 8px;
  }

  .module-section {
    padding: 8px 10px;
  }

  .sensor-list {
    gap: 6px;
  }

  .sensor-card {
    padding: 8px 10px;
    gap: 8px;
  }

  .sensor-info {
    width: 90px;
    gap: 4px;
  }

  .sensor-name {
    font-size: 12px;
  }

  .sensor-scene {
    font-size: 10px;
  }

  .sensor-latest-value {
    gap: 4px;
  }

  .value-label {
    font-size: 9px;
  }

  .value-num {
    font-size: 16px;
  }

  .status-legend {
    gap: 10px;
  }

  .legend-item {
    font-size: 9px;
    padding: 2px 6px;
  }

  .mini-chart-svg {
    max-height: 44px;
  }
}
</style>
