<template>
  <div class="monitoring-report" :class="{ mounted }">
    <div class="report-section module-section">
      <div class="section-label">
        <span class="label-line"></span>
        <span class="label-text">监测报告</span>
        <span class="label-line"></span>
      </div>


      <!-- 报告正文（单条记录，完整展示） -->
      <el-scrollbar class="report-scrollbar">
        <div v-if="!currentRecord" class="empty-state">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M9 12h6m-3-3v6m-7 4h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
          </svg>
          <span class="empty-text">暂无监测报告</span>
        </div>
        <div v-else class="report-full">
          <!-- 状态头部 -->
          <div class="report-card" :class="{ 'report-card--enter': mounted }" style="--card-delay: 0s">
            <div class="report-status-col">
              <span class="report-dot" :class="statusDotClass(currentRecord.level)"></span>
            </div>
            <div class="report-content">
              <div class="report-title-row">
                <span class="report-title">{{ currentRecord.title }}</span>
                <span class="report-badge" :class="statusBadgeClass(currentRecord.level)">
                  {{ statusLabel(currentRecord.level) }}
                </span>
              </div>
              <div class="report-detail">
                <span class="report-source">{{ currentRecord.source }}</span>
                <span class="report-time">{{ currentRecord.time }}</span>
              </div>
            </div>
          </div>
          <!-- 完整报告正文 -->
          <div class="report-full-body" v-if="currentRecord.reportContent">
            <div class="markdown-body report-text" v-html="renderedReportContent"></div>
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
import { renderMarkdown } from '@/utils/markdown'

// ---------- 动画开关 ----------
const mounted = ref(false)

// ---------- 报告数据 ----------
const rawReport = ref({
  totalAlerts: 0,
  warnCount: 0,
  abnormalCount: 0,
  records: []
})

const reportData = computed(() => {
  return {
    totalAlerts: rawReport.value.totalAlerts ?? 0,
    warnCount: rawReport.value.warnCount ?? 0,
    abnormalCount: rawReport.value.abnormalCount ?? 0,
    records: rawReport.value.records || []
  }
})

/** 当前单条记录（取 records 第一项） */
const currentRecord = computed(() => {
  const list = rawReport.value.records
  return list && list.length > 0 ? list[0] : null
})

/** 渲染后的 markdown 报告正文 */
const renderedReportContent = computed(() => {
  return renderMarkdown(currentRecord.value?.reportContent)
})

// ---------- 状态辅助 ----------
const LEVEL_MAP = {
  1: { label: '预警', dotClass: 'dot-warning', badgeClass: 'badge-warning' },
  2: { label: '异常', dotClass: 'dot-abnormal', badgeClass: 'badge-abnormal' }
}

function statusLabel(level) {
  return LEVEL_MAP[level]?.label || '正常'
}

function statusDotClass(level) {
  return LEVEL_MAP[level]?.dotClass || 'dot-normal'
}

function statusBadgeClass(level) {
  return LEVEL_MAP[level]?.badgeClass || 'badge-normal'
}

// ---------- WebSocket 客户端 ----------
let stompClient = null

function connectWebSocket() {
  stompClient = new Client({
    webSocketFactory: () => new SockJS(websocketUrl),
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe('/topic/monitor-report', (message) => {
        try {
          const payload = JSON.parse(message.body)
          rawReport.value = {
            totalAlerts: payload.totalAlerts ?? 0,
            warnCount: payload.warnCount ?? 0,
            abnormalCount: payload.abnormalCount ?? 0,
            records: payload.records || []
          }
        } catch (e) {
          // 解析失败，忽略
        }
      })
    },
    onStompError: (frame) => {
      console.error('MonitoringReport STOMP 错误:', frame.headers['message'])
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

// ---------- 生命周期 ----------
onMounted(() => {
  requestAnimationFrame(() => {
    mounted.value = true
  })
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
@import '@/utils/markdown.css';

/* ==================== 根容器 ==================== */
.monitoring-report {
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

/* ==================== 报告统计概览 ==================== */
.report-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 10px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  opacity: 0;
  transition: opacity 0.46s cubic-bezier(0.22, 1, 0.36, 1);
}

.report-summary.summary-enter {
  opacity: 1;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 6px 24px;
}

.summary-num {
  font-size: 26px;
  font-weight: 700;
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  color: rgba(0, 200, 255, 0.9);
  text-shadow: 0 0 8px rgba(0, 200, 255, 0.35);
  line-height: 1;
}

.summary-num.warning {
  color: #ff9100;
  text-shadow: 0 0 8px rgba(255, 145, 0, 0.35);
}

.summary-num.abnormal {
  color: #ff4081;
  text-shadow: 0 0 8px rgba(255, 64, 129, 0.45);
}

.summary-label {
  font-size: 11px;
  color: rgba(180, 200, 220, 0.5);
  letter-spacing: 1px;
}

.summary-divider {
  width: 1px;
  height: 36px;
  background: linear-gradient(180deg, transparent, rgba(0, 200, 255, 0.2), transparent);
}

/* ==================== 滚动区域 ==================== */
.report-scrollbar {
  flex: 1;
  min-height: 0;
  position: relative;
  z-index: 1;
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

/* ==================== 报告正文（单条记录完整展示） ==================== */
.report-full {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 2px 2px 10px;
}

.report-full-body {
  padding: 14px 16px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(10, 22, 52, 0.78), rgba(7, 15, 35, 0.56));
  border: 1px solid rgba(0, 200, 255, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

.report-text {
  font-size: 12px;
  color: rgba(200, 220, 245, 0.8);
}

/* 大屏暗色主题下的 markdown 样式覆盖 */
.report-text :deep(h1),
.report-text :deep(h2),
.report-text :deep(h3),
.report-text :deep(h4),
.report-text :deep(h5),
.report-text :deep(h6) {
  color: rgba(210, 230, 250, 0.95);
}

.report-text :deep(h1) { border-bottom-color: rgba(0, 200, 255, 0.18); }
.report-text :deep(h2) { border-bottom-color: rgba(0, 200, 255, 0.12); }

.report-text :deep(code) {
  background: rgba(0, 200, 255, 0.08);
  color: #ff9100;
}

.report-text :deep(pre) {
  background: rgba(7, 15, 35, 0.6);
  border-color: rgba(0, 200, 255, 0.1);
}

.report-text :deep(pre code) {
  color: rgba(200, 220, 245, 0.85);
}

.report-text :deep(blockquote) {
  background: rgba(0, 200, 255, 0.06);
  border-left-color: rgba(0, 200, 255, 0.5);
  color: rgba(180, 210, 240, 0.8);
}

.report-text :deep(a) {
  color: rgba(0, 200, 255, 0.9);
}

.report-text :deep(strong) {
  color: rgba(220, 235, 250, 0.95);
}

.report-text :deep(th) {
  background: rgba(0, 200, 255, 0.08);
  color: rgba(210, 230, 250, 0.95);
}

.report-text :deep(td) {
  border-color: rgba(0, 200, 255, 0.1);
}

.report-text :deep(tr:nth-child(even)) {
  background: rgba(0, 200, 255, 0.03);
}

.report-text :deep(hr) {
  background: rgba(0, 200, 255, 0.15);
}

.report-text :deep(del) {
  color: rgba(180, 200, 220, 0.4);
}

.report-card {
  position: relative;
  display: flex;
  align-items: flex-start;
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

.report-card.report-card--enter {
  animation: fadeSlideUp var(--screen-enter-duration, 0.56s) var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
  animation-delay: var(--card-delay);
}

.report-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 200, 255, 0.18), transparent);
  pointer-events: none;
}

/* ==================== 状态指示点 ==================== */
.report-status-col {
  flex-shrink: 0;
}

.report-dot {
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.report-dot.dot-normal {
  background: #00e676;
  box-shadow: 0 0 6px rgba(0, 230, 118, 0.4);
}

.report-dot.dot-warning {
  background: #ff9100;
  box-shadow: 0 0 6px rgba(255, 145, 0, 0.5);
  animation: pulse-warn 1.8s infinite;
}

.report-dot.dot-abnormal {
  background: #ff4081;
  box-shadow: 0 0 8px rgba(255, 64, 129, 0.6);
  animation: pulse-abnormal 1s infinite;
}

/* ==================== 报告内容 ==================== */
.report-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.report-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.report-title {
  font-size: 13px;
  font-weight: 500;
  color: rgba(210, 230, 250, 0.9);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.report-badge {
  flex-shrink: 0;
  font-size: 10px;
  padding: 2px 10px;
  border-radius: 999px;
  letter-spacing: 1px;
}

.report-badge.badge-normal {
  color: #00e676;
  background: rgba(0, 230, 118, 0.1);
  border: 1px solid rgba(0, 230, 118, 0.2);
}

.report-badge.badge-warning {
  color: #ff9100;
  background: rgba(255, 145, 0, 0.1);
  border: 1px solid rgba(255, 145, 0, 0.2);
}

.report-badge.badge-abnormal {
  color: #ff4081;
  background: rgba(255, 64, 129, 0.1);
  border: 1px solid rgba(255, 64, 129, 0.2);
}

.report-detail {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.report-source {
  font-size: 11px;
  color: rgba(180, 210, 240, 0.45);
  letter-spacing: 0.5px;
}

.report-time {
  font-size: 11px;
  color: rgba(180, 210, 240, 0.35);
  font-variant-numeric: tabular-nums;
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
  .monitoring-report {
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

  .report-summary {
    margin-bottom: 6px;
  }

  .summary-item {
    padding: 4px 18px;
  }

  .summary-num {
    font-size: 22px;
  }

  .summary-divider {
    height: 30px;
  }

  .report-full {
    gap: 6px;
    padding: 1px 1px 8px;
  }

  .report-card {
    padding: 10px 12px;
    gap: 10px;
  }

  .report-title {
    font-size: 12px;
  }

  .report-text {
    font-size: 11px;
  }
}

@media (max-width: 1366px), (max-height: 820px) {
  .monitoring-report {
    gap: 8px;
  }

  .module-section {
    padding: 8px 10px;
  }

  .report-full {
    gap: 5px;
    padding: 1px 1px 6px;
  }

  .report-card {
    padding: 8px 10px;
    gap: 8px;
  }

  .summary-item {
    padding: 3px 14px;
  }

  .summary-num {
    font-size: 20px;
  }

  .summary-label {
    font-size: 10px;
  }

  .summary-divider {
    height: 26px;
  }

  .report-badge {
    font-size: 9px;
    padding: 1px 8px;
  }

  .report-title {
    font-size: 11px;
  }

  .report-text {
    font-size: 10px;
  }

  .report-source,
  .report-time {
    font-size: 10px;
  }
}
</style>
