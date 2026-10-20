<template>
  <div class="production-panel" :class="{ mounted }">
    
    <div class="line-section module-section">
      <div class="section-label">
        <span class="label-line"></span>
        <span class="label-text">产线运行状态</span>
        <span class="label-line"></span>
      </div>
      <div class="line-grid">
        <div
          v-for="(line, index) in lineList"
          :key="line.id"
          class="line-card"
          :class="{ 'line-running': line.status === 1, 'line-stopped': line.status === 0 }"
          :style="{ '--delay': index * 0.08 + 's' }"
        >
          <div class="line-head">
            <div class="line-identify">
              <div class="line-icon-wrap">
                <div class="line-icon" :class="{ 'icon-glow': line.status === 1 }">
                  <svg viewBox="0 0 48 48" fill="none">
                    <rect x="6" y="12" width="36" height="24" rx="3" stroke="currentColor" stroke-width="2" />
                    <circle cx="16" cy="24" r="5" stroke="currentColor" stroke-width="2" />
                    <circle cx="32" cy="24" r="5" stroke="currentColor" stroke-width="2" />
                    <line x1="21" y1="24" x2="27" y2="24" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                    <line x1="6" y1="36" x2="10" y2="36" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                    <line x1="38" y1="36" x2="42" y2="36" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                  </svg>
                </div>
              </div>
              <div class="line-meta">
                <div class="line-name-row">
                  <span class="indicator-dot" :class="{ 'dot-on': line.status === 1, 'dot-off': line.status === 0 }"></span>
                  <span class="line-name">{{ line.name }}</span>
                </div>
                <div class="line-code">设备组 {{ String(line.id).padStart(2, '0') }}</div>
              </div>
            </div>
            <span class="line-status" :class="line.status === 1 ? 'status-on' : 'status-off'">
              {{ line.status === 1 ? '运行中' : '已停机' }}
            </span>
          </div>

          <div class="line-data-row">
            <div class="metric-card">
              <div class="metric-label">连续运行</div>
              <div class="metric-value runtime-value" :class="{ 'time-glow': line.status === 1 }">
                {{ line.displayTime }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { websocketUrl } from '@/config/runtimeConfig'

// ---------- 动画开关 ----------
const mounted = ref(false)

// ---------- WebSocket 数据（响应式） ----------
const productionData = ref({
  runningCount: 0,
  stoppedCount: 0,
  lines: [],
})

// ---------- 运行时显示数据（附加 displayTime） ----------
const lineList = ref([])

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
      stompClient.subscribe('/topic/production', (message) => {
        try {
          const payload = JSON.parse(message.body)
          productionData.value = {
            runningCount: payload.runningCount || 0,
            stoppedCount: payload.stoppedCount || 0,
            lines: payload.lines || [],
          }
          // 同步产线列表，为每个产线附加初始 displayTime
          lineList.value = (payload.lines || []).map((line) => ({
            ...line,
            startTime: line.startTime ? new Date(line.startTime).getTime() : null,
            displayTime: '--:--:--',
          }))
          // 立即计算一次显示时间
          updateDisplayTime()
          // 首帧数据到达后触发入场动画
          triggerEnterAnimation()
        } catch (e) {
          // 解析失败，忽略
        }
      })
    },
    onStompError: (frame) => {
      console.error('STOMP 错误:', frame.headers['message'])
    },
  })

  stompClient.activate()
}

function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
}

// ---------- 运行时计算 ----------
const timer = ref(null)

const formatRuntime = (ms) => {
  if (ms <= 0) return '--:--:--'
  const hours = Math.floor(ms / 3600000)
  const minutes = Math.floor((ms % 3600000) / 60000)
  const seconds = Math.floor((ms % 60000) / 1000)
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

const updateDisplayTime = () => {
  const now = Date.now()
  lineList.value.forEach((line) => {
    if (line.status === 1 && line.startTime) {
      line.displayTime = formatRuntime(now - line.startTime)
    } else {
      line.displayTime = '--:--:--'
    }
  })
}

// ---------- 生命周期 ----------
onMounted(() => {
  timer.value = setInterval(updateDisplayTime, 1000)
  connectWebSocket()
  // 兜底：5 秒后强制触发动画
  mountFallbackTimer = setTimeout(() => {
    triggerEnterAnimation()
  }, 5000)
})

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
  if (mountFallbackTimer) {
    clearTimeout(mountFallbackTimer)
  }
  disconnectWebSocket()
})
</script>

<style scoped>
.production-panel {
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

.summary-section {
  flex-shrink: 0;
}

.line-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
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

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  position: relative;
  z-index: 1;
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(10, 22, 52, 0.8), rgba(7, 15, 35, 0.56));
  border: 1px solid rgba(0, 200, 255, 0.08);
  opacity: 0;
  transform: translateY(12px);
  animation: fadeUp var(--screen-enter-duration, 0.56s) var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
  animation-delay: var(--delay);
  will-change: transform, opacity;
}

.summary-label {
  font-size: 12px;
  color: rgba(180, 210, 240, 0.55);
  letter-spacing: 1px;
}

.summary-value {
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.summary-value.is-cyan {
  color: #00ccff;
  text-shadow: 0 0 8px rgba(0, 204, 255, 0.45);
}

.summary-value.is-mint {
  color: #00ffd0;
  text-shadow: 0 0 8px rgba(0, 255, 208, 0.4);
}

.summary-value.is-slate {
  color: #b7c9da;
  text-shadow: 0 0 8px rgba(183, 201, 218, 0.2);
}

.line-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  position: relative;
  z-index: 1;
}

.line-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  padding: 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(10, 22, 52, 0.82), rgba(7, 15, 35, 0.6));
  border: 1px solid rgba(0, 200, 255, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
  opacity: 0;
  transform: translateY(16px);
  animation: fadeUp var(--screen-enter-duration, 0.56s) var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
  animation-delay: var(--delay);
  overflow: hidden;
  will-change: transform, opacity;
}

.line-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(0, 200, 255, 0.06), transparent 32%, transparent 68%, rgba(0, 255, 208, 0.05));
  pointer-events: none;
}

.line-running {
  border-color: rgba(0, 255, 208, 0.16);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    0 12px 24px rgba(0, 255, 208, 0.05);
}

.line-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.line-identify {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.line-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 200, 255, 0.08);
  border: 1px solid rgba(0, 200, 255, 0.14);
  flex-shrink: 0;
}

.line-icon {
  width: 28px;
  height: 28px;
  color: rgba(148, 163, 184, 0.78);
  transition: color 0.3s, filter 0.3s;
}

.line-icon svg {
  width: 100%;
  height: 100%;
}

.icon-glow {
  color: #00ffd0;
  filter: drop-shadow(0 0 6px rgba(0, 255, 208, 0.45));
}

.line-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.line-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.line-name {
  font-size: 14px;
  font-weight: 600;
  color: #e2eefb;
  letter-spacing: 0.6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.line-code {
  font-size: 11px;
  color: rgba(180, 210, 240, 0.42);
  letter-spacing: 1px;
}

.indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-on {
  background: #00ffd0;
  box-shadow: 0 0 8px rgba(0, 255, 208, 0.75), 0 0 16px rgba(0, 255, 208, 0.24);
  animation: pulse 1.8s infinite;
}

.dot-off {
  background: #70859b;
}

.line-status {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 999px;
  letter-spacing: 1px;
}

.status-on {
  color: #00ffd0;
  background: rgba(0, 255, 208, 0.1);
  border: 1px solid rgba(0, 255, 208, 0.24);
}

.status-off {
  color: #9db2c6;
  background: rgba(100, 116, 139, 0.12);
  border: 1px solid rgba(100, 116, 139, 0.25);
}

.line-data-row {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 10px;
}

.metric-card {
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(6, 16, 38, 0.56);
  border: 1px solid rgba(0, 200, 255, 0.07);
}

.metric-card--right {
  text-align: right;
}

.metric-label {
  font-size: 11px;
  color: rgba(180, 210, 240, 0.42);
  letter-spacing: 1px;
}

.metric-value {
  margin-top: 6px;
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  font-size: 22px;
  font-weight: 700;
  color: #dbeaf6;
  line-height: 1;
}

.runtime-value {
  font-size: 20px;
  letter-spacing: 1px;
}

.time-glow {
  color: #00ffd0;
  text-shadow: 0 0 10px rgba(0, 255, 208, 0.35);
}

.value-mint {
  color: #00ffd0;
  text-shadow: 0 0 8px rgba(0, 255, 208, 0.35);
}

.value-slate {
  color: #9db2c6;
}

.progress-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  color: rgba(180, 210, 240, 0.52);
  letter-spacing: 1px;
}

.progress-track {
  height: 10px;
  border-radius: 999px;
  background: rgba(0, 200, 255, 0.05);
  border: 1px solid rgba(0, 200, 255, 0.08);
  overflow: hidden;
}

.progress-fill {
  width: var(--load);
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #00a8ff, #00ffd0);
  box-shadow: 0 0 10px rgba(0, 255, 208, 0.3);
  transition: width 0.82s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1));
}

.progress-fill--off {
  background: linear-gradient(90deg, #65798f, #8fa3b6);
  box-shadow: none;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 4px rgba(0, 255, 208, 0.6), 0 0 8px rgba(0, 255, 208, 0.2); }
  50% { box-shadow: 0 0 8px rgba(0, 255, 208, 1), 0 0 16px rgba(0, 255, 208, 0.35); }
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(var(--screen-enter-offset, 14px));
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1600px), (max-height: 1080px) {
  .production-panel {
    gap: 10px;
  }

  .module-section {
    padding: 10px 12px;
  }

  .section-label {
    margin-bottom: 8px;
    gap: 8px;
  }

  .label-text {
    font-size: 12px;
    letter-spacing: 2px;
  }

  .summary-grid {
    gap: 6px;
  }

  .summary-card {
    padding: 8px 10px;
    gap: 4px;
  }

  .summary-value {
    font-size: 22px;
  }

  .summary-label {
    font-size: 11px;
  }

  .line-grid {
    gap: 8px;
  }

  .line-card {
    gap: 8px;
    padding: 10px;
  }

  .line-icon-wrap {
    width: 38px;
    height: 38px;
  }

  .line-icon {
    width: 22px;
    height: 22px;
  }

  .line-name {
    font-size: 13px;
  }

  .line-code {
    font-size: 10px;
  }

  .line-meta {
    gap: 4px;
  }

  .line-data-row {
    gap: 6px;
  }

  .metric-card {
    padding: 6px 8px;
  }

  .metric-value {
    font-size: 18px;
    margin-top: 4px;
  }

  .runtime-value {
    font-size: 16px;
  }

  .metric-label {
    font-size: 10px;
  }

  .progress-row {
    gap: 4px;
  }

  .progress-head {
    font-size: 10px;
  }

  .progress-track {
    height: 8px;
  }

  .line-status {
    font-size: 10px;
    padding: 3px 8px;
  }
}

@media (max-width: 1366px), (max-height: 820px) {
  .production-panel {
    gap: 8px;
  }

  .module-section {
    padding: 8px 10px;
  }

  .summary-grid {
    gap: 4px;
  }

  .summary-card {
    padding: 6px 8px;
  }

  .summary-value {
    font-size: 20px;
  }

  .line-grid {
    gap: 6px;
  }

  .line-card {
    gap: 6px;
    padding: 8px;
  }

  .line-icon-wrap {
    width: 34px;
    height: 34px;
  }

  .line-icon {
    width: 20px;
    height: 20px;
  }

  .line-name {
    font-size: 12px;
  }

  .line-data-row {
    grid-template-columns: 1fr 1fr;
    gap: 4px;
  }

  .metric-card {
    padding: 4px 6px;
  }

  .metric-value {
    font-size: 16px;
  }

  .runtime-value {
    font-size: 14px;
  }
}
</style>
