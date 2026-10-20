<template>
  <div class="big-screen">
    <!-- ==================== 加载动画遮罩 ==================== -->
    <transition name="loading-fade">
      <div v-if="loading" class="loading-overlay">
        <div class="loading-core">
          <!-- 外圈旋转环 -->
          <div class="rotate-ring outer"></div>
          <div class="rotate-ring inner"></div>
          <!-- 中心六边形 -->
          <div class="hexagon">
            <svg viewBox="0 0 100 100" width="60" height="60">
              <polygon
                points="50,5 90,27 90,73 50,95 10,73 10,27"
                fill="none"
                stroke="#00ccff"
                stroke-width="1.5"
              />
              <polygon
                points="50,12 82,30 82,70 50,88 18,70 18,30"
                fill="none"
                stroke="#00ccff"
                stroke-width="0.5"
                opacity="0.5"
              />
            </svg>
          </div>
          <!-- 扫描线 -->
          <div class="scan-line"></div>
        </div>
        <!-- 加载文字 -->
        <div class="loading-text">
          <div class="loading-title">慧控工场智能监控平台</div>
          <div class="text-wrap">
            <div class="loading-subtitle">Huikong Workshop Intelligent Monitoring Platform</div>
            <div class="loading-progress">
              <div class="progress-track">
                <div class="progress-fill" :style="{ width: progress + '%' }"></div>
              </div>
              <div class="progress-num">{{ progress }}%</div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- ==================== 主界面 ==================== -->
    <div v-if="!loading" class="main-content loaded">
      <div class="screen-glow screen-glow--left"></div>
      <div class="screen-glow screen-glow--right"></div>

      <!-- 头部 -->
      <div class="header-panel">
        <div class="header-side">
          <button class="back-btn" @click="goBack">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 19l-7-7 7-7"/>
            </svg>
            <span>返回</span>
          </button>
        </div>
        <div class="header-title">
          <div class="title-cn">慧控工场智能监控平台</div>
          <div class="title-en">Huikong Workshop Intelligent Monitoring Platform</div>
        </div>
        <div class="header-side header-side--right">
            <span class="chip-value">{{ currentDate }}</span>
            <span class="chip-value">{{ currentTime }}</span>
        </div>
      </div>

      <!-- 主体三栏 -->
      <div class="body-panel">
        <div class="left-panel module-panel" :style="{ animationDelay: '0.2s' }">
          <div class="panel-title">
            <span class="panel-title__text">人事模块</span>
            <span class="panel-title__sub">Personnel</span>
          </div>
          <div class="panel-content">
            <PersonnelPanel />
          </div>
        </div>

        <div class="mid-panel">
          <div class="top-panel module-panel" :style="{ animationDelay: '0.4s' }">
            <div class="panel-title">
              <span class="panel-title__text">产线模块</span>
              <span class="panel-title__sub">Production</span>
            </div>
            <div class="panel-content">
              <ProductionPanel />
            </div>
          </div>
          <div class="bottom-panel module-panel" :style="{ animationDelay: '0.6s' }">
            <div class="panel-title panel-title--switch">
              <span class="panel-title__text">{{ currentView === 'sensor' ? '传感器模块' : '监测报告' }}</span>
              <div class="switch-tabs">
                <span
                  class="switch-tab"
                  :class="{ active: currentView === 'sensor' }"
                  @click="switchToView('sensor')"
                >传感器</span>
                <span
                  class="switch-tab"
                  :class="{ active: currentView === 'report' }"
                  @click="switchToView('report')"
                >报告</span>
              </div>
              <span class="panel-title__sub">{{ currentView === 'sensor' ? 'Sensors' : 'Report' }}</span>
            </div>
            <div class="panel-content panel-content--switch">
              <div class="switch-view-layer" :class="{ 'layer--active': currentView === 'sensor' }">
                <SensorPanel />
              </div>
              <div class="switch-view-layer" :class="{ 'layer--active': currentView === 'report' }">
                <MonitoringReport />
              </div>
            </div>
          </div>
        </div>

        <div class="right-panel module-panel" :style="{ animationDelay: '0.8s' }">
          <div class="panel-title">
            <span class="panel-title__text">产品模块</span>
            <span class="panel-title__sub">Products</span>
          </div>
          <div class="panel-content">
            <ProductPanel />
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
import PersonnelPanel from '@/component/BigScreen/PersonnelPanel.vue'
import ProductionPanel from '@/component/BigScreen/ProductionPanel.vue'
import SensorPanel from '@/component/BigScreen/SensorPanel.vue'
import MonitoringReport from '@/component/BigScreen/MonitoringReport.vue'
import ProductPanel from '@/component/BigScreen/ProductPanel.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/nav')
}

const loading = ref(true)
const progress = ref(0)

// ---------- 传感器/报告切换 ----------
const currentView = ref('sensor')

/** 最小切换间隔（毫秒）= 5 秒，为 AI 推算预留充足处理时间 */
const MIN_SWITCH_INTERVAL = 5000

/** 上一次切换时间戳 */
let lastSwitchTime = 0

/** 是否存在有效的报告数据（由 WebSocket 推送触发） */
const hasReportData = ref(false)

/** 自动切回传感器的定时器 */
let sensorRestoreTimer = null

const currentDate = ref('')
const currentTime = ref('')
let timer = null
let progressTimer = null

/** WebSocket STOMP 客户端（仅用于报告数据感知） */
let stompClient = null

/**
 * 切换视图至指定目标。
 * 受最小切换间隔保护，避免频繁抖动。
 */
function switchToView(target) {
  const now = Date.now()
  if (now - lastSwitchTime < MIN_SWITCH_INTERVAL) {
    return
  }
  lastSwitchTime = now
  currentView.value = target
}

/**
 * 当收到有效报告数据时，自动切换至报告视图。
 * 5 秒后自动切回传感器视图。
 */
function autoSwitchToReport() {
  hasReportData.value = true
  if (currentView.value !== 'report') {
    switchToView('report')
  }
  // 清除旧的恢复定时器，重新计时
  if (sensorRestoreTimer) {
    clearTimeout(sensorRestoreTimer)
  }
  sensorRestoreTimer = setTimeout(() => {
    switchToView('sensor')
  }, MIN_SWITCH_INTERVAL)
}

const updateDateTime = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth() + 1
  const d = now.getDate()
  currentDate.value = `${y}年${m}月${d}日`

  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  const ss = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${hh}:${mm}:${ss}`
}

// ---------- WebSocket 连接（感知报告数据） ----------
function connectWebSocket() {
  stompClient = new Client({
    webSocketFactory: () => new SockJS(websocketUrl),
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe('/topic/monitor-report', (message) => {
        try {
          const payload = JSON.parse(message.body)
          if (payload.records && payload.records.length > 0) {
            autoSwitchToReport()
          }
        } catch (e) {
          // 解析失败，忽略
        }
      })
    },
    onStompError: (frame) => {
      console.error('BigScreenPage STOMP 错误:', frame.headers['message'])
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

onMounted(() => {
  updateDateTime()
  timer = setInterval(updateDateTime, 1000)

  // 建立 WebSocket 监听报告数据
  connectWebSocket()

  // 加载进度动画 (1.2s 内从 0 到 100)
  const totalDuration = 1200
  const stepInterval = 30
  const stepCount = totalDuration / stepInterval
  let step = 0
  progressTimer = setInterval(() => {
    step++
    // 使用缓出曲线模拟真实加载感
    const t = step / stepCount
    progress.value = Math.round((1 - Math.pow(1 - t, 3)) * 100)
    if (step >= stepCount) {
      clearInterval(progressTimer)
      progress.value = 100
      // 短暂停留后隐藏加载层
      setTimeout(() => {
        loading.value = false
      }, 150)
    }
  }, stepInterval)
})

onUnmounted(() => {
  clearInterval(timer)
  clearInterval(progressTimer)
  disconnectWebSocket()
  if (sensorRestoreTimer) {
    clearTimeout(sensorRestoreTimer)
  }
})
</script>

<style scoped>
/* ==================== 全局 ==================== */
.big-screen {
  --cyan: #00ccff;
  --cyan-soft: rgba(0, 204, 255, 0.18);
  --mint: #00ffd0;
  --screen-enter-ease: cubic-bezier(0.22, 1, 0.36, 1);
  --screen-enter-duration: 0.56s;
  --screen-enter-offset: 16px;
  --screen-enter-scale: 0.98;
  --text-main: #e6f4ff;
  --text-soft: rgba(192, 222, 255, 0.72);
  --text-dim: rgba(157, 192, 224, 0.48);
  --panel-bg: linear-gradient(180deg, rgba(9, 19, 48, 0.86), rgba(6, 15, 36, 0.72));
  --panel-border: rgba(0, 204, 255, 0.16);
  --panel-radius: 14px;
  --gap: clamp(12px, 0.95vw, 18px);
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background:
    radial-gradient(circle at 15% 18%, rgba(0, 204, 255, 0.12), transparent 24%),
    radial-gradient(circle at 82% 16%, rgba(0, 255, 208, 0.1), transparent 20%),
    linear-gradient(180deg, #07101f 0%, #091428 42%, #060d1b 100%);
  color: var(--text-main);
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Segoe UI', sans-serif;
  position: relative;
}

.big-screen::after {
  content: '';
  position: fixed;
  inset: 18px;
  border: 1px solid rgba(0, 204, 255, 0.06);
  border-radius: 20px;
  pointer-events: none;
  z-index: 0;
}

/* ==================== 加载遮罩 ==================== */
.loading-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background:
    radial-gradient(circle at center, rgba(0, 204, 255, 0.08), transparent 30%),
    linear-gradient(180deg, #040913, #06101d 58%, #040913);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 40px;
}

.loading-core {
  position: relative;
  width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 旋转外圈 */
.rotate-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid transparent;
}

.rotate-ring.outer {
  width: 140px;
  height: 140px;
  border-top-color: rgba(0, 200, 255, 0.6);
  border-right-color: rgba(0, 200, 255, 0.3);
  animation: ring-spin 2s linear infinite;
}

.rotate-ring.inner {
  width: 110px;
  height: 110px;
  border-bottom-color: rgba(0, 200, 255, 0.4);
  border-left-color: rgba(0, 200, 255, 0.2);
  animation: ring-spin 1.5s linear infinite reverse;
}

@keyframes ring-spin {
  to { transform: rotate(360deg); }
}

/* 中心六边形闪烁 */
.hexagon {
  animation: hexa-pulse 2s ease-in-out infinite;
}

.hexagon svg polygon {
  animation: hexa-glow 2s ease-in-out infinite alternate;
}

@keyframes hexa-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.08); }
}

@keyframes hexa-glow {
  from { stroke: #00ccff; }
  to   { stroke: #00ffcc; }
}

/* 扫描线 */
.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00ccff, transparent);
  box-shadow: 0 0 8px #00ccff;
  animation: scan-down 1.8s ease-in-out infinite;
}

@keyframes scan-down {
  0%   { top: 10%; opacity: 0; }
  20%  { opacity: 1; }
  80%  { opacity: 1; }
  100% { top: 90%; opacity: 0; }
}

/* 加载文字 */
.loading-text {
  text-align: center;
}

/* 统一宽度包裹层 */
.text-wrap {
  width: 440px;
  margin: 0 auto;
  position: relative;
}

.loading-title {
  font-size: 26px;
  font-weight: bold;
  letter-spacing: 8px;
  color: #00ccff;
  text-shadow: 0 0 20px rgba(0, 200, 255, 0.6);
  animation: title-blink 1.5s ease-in-out infinite;
}

@keyframes title-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.loading-subtitle {
  font-size: 12px;
  color: rgba(0, 200, 255, 0.5);
  letter-spacing: 3px;
  margin-top: 8px;
}

/* 进度条 */
.loading-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  width: 100%;
  position: relative;
}

.progress-track {
  width: 100%;
  height: 2px;
  background: rgba(0, 200, 255, 0.15);
  border-radius: 1px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #0066aa, #00ccff, #00ffcc);
  box-shadow: 0 0 6px rgba(0, 200, 255, 0.5);
  border-radius: 1px;
  transition: width 0.05s linear;
}

/* 百分比绝对定位，悬浮在进度条右端外侧 */
.progress-num {
  position: absolute;
  left: calc(100% + 12px);
  font-size: 12px;
  color: #00ccff;
  min-width: 32px;
  font-variant-numeric: tabular-nums;
}

/* 加载层淡出 */
.loading-fade-leave-active {
  transition: opacity 0.6s ease;
}
.loading-fade-leave-to {
  opacity: 0;
}

/* ==================== 主界面入场 ==================== */
.main-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  opacity: 0;
  position: relative;
  z-index: 1;
  padding: clamp(12px, 1vw, 18px);
  gap: var(--gap);
}

.main-content.loaded {
  opacity: 1;
}

.main-content.loaded .header-panel {
  animation: header-slide var(--screen-enter-duration) var(--screen-enter-ease) forwards;
}

.main-content.loaded .left-panel,
.main-content.loaded .top-panel,
.main-content.loaded .bottom-panel,
.main-content.loaded .right-panel {
  animation: panel-rise var(--screen-enter-duration) var(--screen-enter-ease) forwards;
}

/* 头部下落 */
@keyframes header-slide {
  from {
    opacity: 0;
    transform: translateY(calc(var(--screen-enter-offset) * -1.2));
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 面板升起 + 边框流光 */
@keyframes panel-rise {
  from {
    opacity: 0;
    transform: translateY(calc(var(--screen-enter-offset) * 1.5)) scale(var(--screen-enter-scale));
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ==================== 头部 ==================== */
.header-panel {
  height: clamp(80px, 9vh, 100px);
  min-height: clamp(80px, 9vh, 100px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 0 clamp(10px, 1vw, 25px);
  background: linear-gradient(180deg, rgba(7, 22, 58, 0.95), rgba(8, 16, 40, 0.84));
  border: 1px solid var(--panel-border);
  border-radius: var(--panel-radius);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    0 20px 40px rgba(0, 0, 0, 0.24);
  position: relative;
  overflow: hidden;
  will-change: transform, opacity;
}

.header-panel::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, transparent, rgba(0, 204, 255, 0.08), transparent),
    linear-gradient(180deg, rgba(255, 255, 255, 0.04), transparent 38%);
  pointer-events: none;
}

.header-panel::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 8%;
  width: 84%;
  height: 1px;
  background: linear-gradient(90deg,
      transparent,
      rgba(0, 204, 255, 0.6),
      rgba(0, 255, 208, 0.55),
      rgba(0, 204, 255, 0.6),
      transparent);
}

.header-side {
  min-width: clamp(214px, 19vw, 290px);
  display: flex;
  gap: 10px;
  align-items: center;
}

.header-side--right {
  justify-content: flex-end;
}

/* 返回按钮 */
.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: rgba(9, 20, 48, 0.68);
  border: 1px solid rgba(0, 204, 255, 0.14);
  border-radius: 10px;
  color: var(--cyan);
  font-size: 13px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: inset 0 0 20px rgba(0, 204, 255, 0.04);
}

.back-btn:hover {
  background: rgba(0, 204, 255, 0.12);
  border-color: rgba(0, 204, 255, 0.36);
  box-shadow: 0 0 15px rgba(0, 204, 255, 0.2), inset 0 0 20px rgba(0, 204, 255, 0.08);
  transform: translateY(-2px);
}

.back-btn:active {
  transform: translateY(0);
}

.back-btn svg {
  transition: transform 0.3s ease;
}

.back-btn:hover svg {
  transform: translateX(-2px);
}

.header-chip {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  padding: 10px 14px;
  background: rgba(9, 20, 48, 0.68);
  border: 1px solid rgba(0, 204, 255, 0.14);
  border-radius: 10px;
  box-shadow: inset 0 0 20px rgba(0, 204, 255, 0.04);
}

.header-chip--strong {
  background: rgba(10, 26, 58, 0.82);
}

.header-chip--dim {
  opacity: 0.88;
}

.chip-label {
  font-size: 11px;
  color: var(--text-dim);
  letter-spacing: 2px;
}

.chip-value {
  font-size: clamp(14px, 1vw, 18px);
  color: var(--cyan);
  letter-spacing: 1px;
  white-space: nowrap;
}

.header-side--right .chip-value {
  font-variant-numeric: tabular-nums;
}

.header-title {
  text-align: center;
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.title-cn {
  font-size: clamp(24px, 1.9vw, 26px);
  font-weight: bold;
  letter-spacing: clamp(4px, 0.4vw, 8px);
  background: linear-gradient(180deg, #ffffff, #7ddcff 56%, #00ffd0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
}

.title-en {
  font-size: 12px;
  color: rgba(0, 204, 255, 0.62);
  letter-spacing: 3px;
  margin-top: 6px;
}

.title-decoration {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 10px;
}

.title-line {
  width: clamp(48px, 4vw, 88px);
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 204, 255, 0.55), transparent);
}

.title-badge {
  padding: 4px 10px;
  border: 1px solid rgba(0, 204, 255, 0.14);
  border-radius: 999px;
  font-size: 11px;
  color: rgba(174, 221, 255, 0.72);
  letter-spacing: 2px;
  white-space: nowrap;
}

/* ==================== 主体 ==================== */
.body-panel {
  flex: 1;
  display: grid;
  grid-template-columns: minmax(300px, 1.02fr) minmax(360px, 1.16fr) minmax(300px, 1.02fr);
  gap: var(--gap);
  min-height: 0;
}

.left-panel,
.mid-panel,
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
  min-height: 0;
}

.panel-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  font-weight: bold;
  padding: 14px 18px 12px;
  background: linear-gradient(180deg, rgba(0, 204, 255, 0.08), rgba(0, 204, 255, 0.02));
  border-bottom: 1px solid rgba(0, 204, 255, 0.14);
  position: relative;
  z-index: 2;
}

.panel-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--cyan);
  box-shadow: 0 0 8px var(--cyan);
  border-radius: 2px;
}

.panel-title__text {
  font-size: 16px;
  color: var(--cyan);
  letter-spacing: 4px;
  text-shadow: 0 0 8px rgba(0, 204, 255, 0.35);
}

.panel-title__sub {
  font-size: 11px;
  color: var(--text-dim);
  letter-spacing: 2px;
}

.panel-content {
  flex: 1;
  padding: clamp(12px, 1vw, 16px);
  min-height: 0;
  overflow: auto;
  position: relative;
  z-index: 2;
}

/* 子面板通用边框发光 */
.module-panel {
  background: var(--panel-bg);
  border: 1px solid var(--panel-border);
  border-radius: var(--panel-radius);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(8px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    0 16px 30px rgba(0, 0, 0, 0.24);
  will-change: transform, opacity;
}

.module-panel::before {
  content: '';
  position: absolute;
  top: -1px;
  left: -1px;
  right: -1px;
  bottom: -1px;
  border-radius: inherit;
  border: 1px solid transparent;
  background: linear-gradient(135deg,
      rgba(0, 204, 255, 0.34),
      transparent 26%,
      transparent 68%,
      rgba(0, 255, 208, 0.28)) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
  z-index: 1;
}

.module-panel::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.02), transparent),
    radial-gradient(circle at top, rgba(0, 204, 255, 0.05), transparent 36%);
  pointer-events: none;
  z-index: 0;
}

.mid-panel {
  background: transparent;
  border: none;
  gap: var(--gap);
}

.top-panel,
.bottom-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* ==================== 网格背景纹理 ==================== */
.big-screen::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(0, 204, 255, 0.028) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 204, 255, 0.028) 1px, transparent 1px);
  background-size: 42px 42px;
  pointer-events: none;
  z-index: 0;
}

.screen-glow {
  position: absolute;
  top: 104px;
  width: 340px;
  height: 340px;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.18;
  pointer-events: none;
}

.screen-glow--left {
  left: -100px;
  background: rgba(0, 204, 255, 0.42);
}

.screen-glow--right {
  right: -120px;
  background: rgba(0, 255, 208, 0.32);
}

/* ==================== 滚动条 ==================== */
.panel-content::-webkit-scrollbar {
  width: 5px;
}

.panel-content::-webkit-scrollbar-thumb {
  background: rgba(0, 204, 255, 0.28);
  border-radius: 999px;
}

.panel-content::-webkit-scrollbar-track {
  background: transparent;
}

@media (max-width: 1600px), (max-height: 1080px) {
  .main-content {
    padding: 12px;
    gap: 14px;
  }

  .header-panel {
    height: 82px;
    min-height: 82px;
    padding: 0 16px;
  }

  .body-panel {
    grid-template-columns: minmax(280px, 1fr) minmax(340px, 1.08fr) minmax(280px, 1fr);
  }

  .title-cn {
    font-size: 22px;
    letter-spacing: 4px;
  }

  .title-en {
    font-size: 11px;
    margin-top: 4px;
  }

  .header-side {
    min-width: 180px;
  }

  .header-chip {
    padding: 8px 12px;
    gap: 2px;
  }

  .chip-label {
    font-size: 10px;
  }

  .chip-value {
    font-size: 15px;
  }

  .panel-title {
    padding: 10px 14px 8px;
  }

  .panel-title__text {
    font-size: 15px;
    letter-spacing: 3px;
  }

  .panel-content {
    padding: 10px 12px;
  }

  .left-panel,
  .mid-panel,
  .right-panel {
    gap: 10px;
  }
}

.panel-content--switch {
  overflow: hidden;
  position: relative;
}

/* 切换视图层：两个组件始终挂载，通过 opacity 切换实现即时渲染、无加载等待 */
.switch-view-layer {
  position: absolute;
  inset: 0;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.36s cubic-bezier(0.22, 1, 0.36, 1);
}

.switch-view-layer.layer--active {
  opacity: 1;
  pointer-events: auto;
}

/* ==================== 切换标签 ==================== */
.panel-title--switch {
  gap: 8px;
  flex-wrap: wrap;
}

.switch-tabs {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 2px;
  background: rgba(7, 16, 38, 0.56);
  border: 1px solid rgba(0, 200, 255, 0.1);
  border-radius: 999px;
}

.switch-tab {
  font-size: 11px;
  padding: 3px 12px;
  border-radius: 999px;
  color: rgba(180, 200, 220, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
  letter-spacing: 1px;
  white-space: nowrap;
  user-select: none;
}

.switch-tab.active {
  color: #00ccff;
  background: rgba(0, 200, 255, 0.15);
  text-shadow: 0 0 6px rgba(0, 200, 255, 0.3);
}

.switch-tab:hover:not(.active) {
  color: rgba(180, 200, 220, 0.75);
}

/* ==================== 响应式 ==================== */
@media (max-width: 1600px), (max-height: 1080px) {
  .switch-tabs {
    gap: 1px;
  }

  .switch-tab {
    font-size: 10px;
    padding: 2px 10px;
  }
}

@media (max-width: 1366px), (max-height: 820px) {
  .switch-tab {
    font-size: 9px;
    padding: 2px 8px;
  }

  .main-content {
    padding: 6px;
    gap: 10px;
  }

  .body-panel {
    gap: 10px;
  }

  .header-panel {
    padding: 0 12px;
  }

  .panel-title {
    padding: 12px 14px 10px;
  }

  .panel-title__text {
    font-size: 15px;
    letter-spacing: 3px;
  }

  .panel-content {
    padding: 10px 12px;
  }

  .header-chip {
    padding: 8px 12px;
  }

  .title-decoration {
    margin-top: 8px;
  }
}
</style>
