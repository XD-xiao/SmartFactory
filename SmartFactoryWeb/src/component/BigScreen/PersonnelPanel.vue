<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { websocketUrl } from '@/config/runtimeConfig'

// ---------- 动画开关 ----------
const mounted = ref(false)

// ---------- WebSocket 数据（响应式） ----------
const attendanceData = ref({
  working: 0,
  leave: 0,
  offWork: 0,
  total: 0,
  rate: 0
})

const departmentData = ref([])

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
      stompClient.subscribe('/topic/personnel', (message) => {
        try {
          const payload = JSON.parse(message.body)
          if (payload.attendance) {
            attendanceData.value = payload.attendance
          }
          if (payload.departments) {
            departmentData.value = payload.departments
          }
          // 首帧数据到达后触发入场动画
          triggerEnterAnimation()
        } catch (e) {
          // 解析消息失败，忽略
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

onMounted(() => {
  connectWebSocket()
  // 兜底：5 秒后强制触发动画（防止 WebSocket 连接失败导致永远空白）
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

// ---------- 各部门配色 ----------
const deptColors = ['#00ccff', '#00e676', '#ff9100', '#7c4dff', '#ff4081', '#40c4ff', '#b2ff59', '#ffab40']

// ---------- 环形图 stroke 计算 ----------
const RING_RADIUS = 74
const CIRCUMFERENCE = 2 * Math.PI * RING_RADIUS

const ringStyles = computed(() => {
  if (!mounted.value) {
    return {
      working: { dasharray: `0 ${CIRCUMFERENCE}`, offset: 0 },
      leave: { dasharray: `0 ${CIRCUMFERENCE}`, offset: 0 },
      offWork: { dasharray: `0 ${CIRCUMFERENCE}`, offset: 0 }
    }
  }

  const { working, leave, offWork, total } = attendanceData.value
  if (total === 0) {
    return {
      working: { dasharray: `0 ${CIRCUMFERENCE}`, offset: 0 },
      leave: { dasharray: `0 ${CIRCUMFERENCE}`, offset: 0 },
      offWork: { dasharray: `0 ${CIRCUMFERENCE}`, offset: 0 }
    }
  }
  const wp = working / total
  const lp = leave / total
  const op = offWork / total

  const wLen = wp * CIRCUMFERENCE
  const lLen = lp * CIRCUMFERENCE
  const oLen = op * CIRCUMFERENCE

  return {
    working: {
      dasharray: `${wLen} ${CIRCUMFERENCE}`,
      offset: 0
    },
    leave: {
      dasharray: `${lLen} ${CIRCUMFERENCE}`,
      offset: -wLen
    },
    offWork: {
      dasharray: `${oLen} ${CIRCUMFERENCE}`,
      offset: -(wLen + lLen)
    }
  }
})
</script>


<template>
  <div class="personnel-panel" :class="{ mounted }">
    <!-- ==================== 上半部分：总体出勤环形图 ==================== -->
    <div class="attendance-overview module-section">
      <div class="section-label">
        <span class="label-line"></span>
        <span class="label-text">总体出勤情况</span>
        <span class="label-line"></span>
      </div>
      <div class="ring-container">
        <!-- 四角数据标签 -->
        <div class="corner-label top-left" :class="{ 'corner-enter': mounted }" style="--delay:0s">
          <div class="corner-value onwork">{{ attendanceData.working }}</div>
          <div class="corner-desc">上班人数</div>
        </div>
        <div class="corner-label top-right" :class="{ 'corner-enter': mounted }" style="--delay:0.15s">
          <div class="corner-value leave">{{ attendanceData.leave }}</div>
          <div class="corner-desc">请假人数</div>
        </div>
        <div class="corner-label bottom-left" :class="{ 'corner-enter': mounted }" style="--delay:0.3s">
          <div class="corner-value offwork">{{ attendanceData.offWork }}</div>
          <div class="corner-desc">下班人数</div>
        </div>
        <div class="corner-label bottom-right" :class="{ 'corner-enter': mounted }" style="--delay:0.45s">
          <div class="corner-value total">{{ attendanceData.total }}</div>
          <div class="corner-desc">总人数</div>
        </div>

        <!-- SVG 环形图 -->
        <svg class="ring-svg" viewBox="0 0 200 200">
          <!-- 背景轨道 -->
          <circle
            cx="100" cy="100" r="74"
            fill="none"
            stroke="rgba(0, 200, 255, 0.06)"
            stroke-width="16"
          />
          <!-- 发光底环 -->
          <circle
            cx="100" cy="100" r="74"
            fill="none"
            stroke="rgba(0, 200, 255, 0.08)"
            stroke-width="2"
          />

          <!-- 环形分段 -->
          <g transform="rotate(-90 100 100)">
            <!-- 上班段 -->
            <circle
              cx="100" cy="100" r="74"
              fill="none"
              stroke="#00ccff"
              stroke-width="14"
              stroke-linecap="butt"
              :stroke-dasharray="ringStyles.working.dasharray"
              :stroke-dashoffset="ringStyles.working.offset"
              class="ring-segment"
            />
            <!-- 请假段 -->
            <circle
              cx="100" cy="100" r="74"
              fill="none"
              stroke="#ff9100"
              stroke-width="14"
              stroke-linecap="butt"
              :stroke-dasharray="ringStyles.leave.dasharray"
              :stroke-dashoffset="ringStyles.leave.offset"
              class="ring-segment"
            />
            <!-- 下班段 -->
            <circle
              cx="100" cy="100" r="74"
              fill="none"
              stroke="#7c8aa0"
              stroke-width="14"
              stroke-linecap="butt"
              :stroke-dasharray="ringStyles.offWork.dasharray"
              :stroke-dashoffset="ringStyles.offWork.offset"
              class="ring-segment"
            />
          </g>

          <!-- 内圈装饰环 -->
          <circle
            cx="100" cy="100" r="60"
            fill="none"
            stroke="rgba(0, 200, 255, 0.12)"
            stroke-width="0.5"
            stroke-dasharray="4 6"
          />

          <!-- 中心文字 -->
          <text x="100" y="88" text-anchor="middle" class="ring-rate-label">出勤率</text>
          <text x="100" y="120" text-anchor="middle" class="ring-rate-value">{{ attendanceData.rate }}%</text>
        </svg>
      </div>

      <!-- 图例 -->
      <div class="ring-legend" :class="{ 'legend-enter': mounted }">
        <div class="legend-item">
          <span class="legend-dot" style="background:#00ccff;box-shadow:0 0 6px #00ccff;"></span>
          <span>上班</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background:#ff9100;box-shadow:0 0 6px #ff9100;"></span>
          <span>请假</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background:#7c8aa0;box-shadow:0 0 6px #7c8aa0;"></span>
          <span>下班</span>
        </div>
      </div>
    </div>

    <!-- ==================== 下半部分：各部门出勤 ==================== -->
    <div class="dept-attendance module-section">
      <div class="section-label">
        <span class="label-line"></span>
        <span class="label-text">各部门出勤情况</span>
        <span class="label-line"></span>
      </div>
      <el-scrollbar class="dept-scrollbar">
        <div class="dept-list">
          <div
            v-for="(dept, index) in departmentData"
            :key="index"
            class="dept-item"
            :class="{ 'dept-item--enter': mounted }"
            :style="{ '--delay': index * 0.08 + 's' }"
          >
            <div class="dept-header">
              <span
                class="dept-name"
                :style="{ color: deptColors[index % deptColors.length] }"
              >{{ dept.name }}</span>
              <span class="dept-count">
                <span class="count-working">{{ dept.working }}</span>
                <span class="count-sep">/</span>
                <span class="count-total">{{ dept.total }}</span>
              </span>
              <span
                class="dept-rate"
                :style="{ color: deptColors[index % deptColors.length] }"
              >{{ dept.rate }}%</span>
            </div>
            <div class="dept-bar-track">
              <div
                class="dept-bar-fill"
                :style="{
                  width: mounted ? dept.rate + '%' : '0%',
                  background: deptColors[index % deptColors.length],
                  boxShadow: '0 0 8px ' + deptColors[index % deptColors.length]
                }"
              ></div>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>


<style scoped>
/* ==================== 根容器 ==================== */
.personnel-panel {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  padding: 16px 18px;
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

/* ==================== 环形图容器 ==================== */
.attendance-overview {
  flex-shrink: 0;
}

.ring-container {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  max-height: 280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-svg {
  width: 82%;
  height: 82%;
  filter: drop-shadow(0 0 12px rgba(0, 200, 255, 0.15));
}

.ring-segment {
  transition:
    stroke-dasharray 0.82s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)),
    stroke-dashoffset 0.82s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1));
}

/* 中心文字 */
.ring-rate-label {
  font-size: 13px;
  fill: rgba(0, 200, 255, 0.7);
  letter-spacing: 2px;
}

.ring-rate-value {
  font-size: 26px;
  font-weight: bold;
  fill: #00ccff;
  text-shadow: 0 0 8px rgba(0, 200, 255, 0.6);
  font-family: 'DIN Alternate', 'Arial', sans-serif;
}

/* ==================== 四角标签 ==================== */
.corner-label {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 2;
  padding: 8px 14px;
  background: rgba(9, 20, 46, 0.76);
  border: 1px solid rgba(0, 200, 255, 0.16);
  border-radius: 8px;
  backdrop-filter: blur(6px);
  box-shadow: inset 0 0 18px rgba(0, 200, 255, 0.05);
}

.corner-label.top-left    { top: 12%; left: 16%; }
.corner-label.top-right   { top: 12%; right: 16%; }
.corner-label.bottom-left  { bottom: 12%; left: 16%; }
.corner-label.bottom-right { bottom: 12%; right: 16%; }

.corner-value {
  font-size: 30px;
  font-weight: bold;
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  line-height: 1.2;
}

.corner-value.onwork  { color: #00ccff; text-shadow: 0 0 8px rgba(0, 200, 255, 0.5); }
.corner-value.leave   { color: #ff9100; text-shadow: 0 0 8px rgba(255, 145, 0, 0.5); }
.corner-value.offwork { color: #7c8aa0; text-shadow: 0 0 8px rgba(124, 138, 160, 0.5); }
.corner-value.total   { color: #e0f0ff; text-shadow: 0 0 8px rgba(224, 240, 255, 0.3); }

.corner-desc {
  font-size: 11px;
  color: rgba(180, 200, 220, 0.7);
  letter-spacing: 1px;
  margin-top: 2px;
}

/* ==================== 图例 ==================== */
.ring-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: rgba(180, 200, 220, 0.8);
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(7, 16, 38, 0.56);
  border: 1px solid rgba(0, 200, 255, 0.08);
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

/* ==================== 部门出勤列表 ==================== */
.dept-attendance {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.dept-scrollbar {
  flex: 1;
  min-height: 0;
}

.dept-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 2px 2px 10px;
}

.dept-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(10, 22, 52, 0.74), rgba(7, 15, 35, 0.52));
  border: 1px solid rgba(0, 200, 255, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

.dept-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dept-name {
  font-size: 14px;
  font-weight: bold;
  letter-spacing: 1px;
  min-width: 52px;
}

.dept-count {
  font-size: 12px;
  color: rgba(180, 200, 220, 0.8);
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  flex: 1;
  text-align: center;
}

.count-working { color: rgba(0, 200, 255, 0.9); }
.count-sep      { color: rgba(180, 200, 220, 0.4); margin: 0 2px; }
.count-total    { color: rgba(180, 200, 220, 0.6); }

.dept-rate {
  font-size: 16px;
  font-weight: bold;
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  min-width: 48px;
  text-align: right;
}

/* 进度条轨道 */
.dept-bar-track {
  position: relative;
  height: 14px;
  background: rgba(0, 200, 255, 0.05);
  border: 1px solid rgba(0, 200, 255, 0.08);
  border-radius: 999px;
  overflow: hidden;
}

/* 已填充部分 */
.dept-bar-fill {
  height: 100%;
  border-radius: 999px;
  position: relative;
  transition: width 0.92s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1));
  opacity: 0.85;
}

/* 流光效果 */
.dept-bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: -60%;
  width: 60%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.25), transparent);
  transform: skewX(-25deg);
  animation: bar-shine 2.5s ease-in-out infinite;
}

@keyframes bar-shine {
  0%   { left: -60%; }
  100% { left: 120%; }
}

/* 顶部发光点 */
.dept-bar-fill::before {
  content: '';
  position: absolute;
  top: 2px;
  right: 6px;
  width: 6px;
  height: 2px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 1px;
  box-shadow: 0 0 4px rgba(255, 255, 255, 0.5);
}

/* ==================== 打开动画 ==================== */

/* 四角标签淡入 + 缩放 */
.corner-label {
  opacity: 0;
  transform: scale(0.7);
}
.corner-label.corner-enter {
  animation: corner-pop-in var(--screen-enter-duration, 0.56s) var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
  animation-delay: var(--delay, 0s);
}

@keyframes corner-pop-in {
  0%   { opacity: 0; transform: translateY(calc(var(--screen-enter-offset, 14px) * 0.35)) scale(0.72); }
  60%  { opacity: 1; transform: scale(1.05); }
  100% { opacity: 1; transform: scale(1); }
}

/* 图例淡入 */
.ring-legend {
  opacity: 0;
  transition: opacity 0.46s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1));
}
.ring-legend.legend-enter {
  opacity: 1;
}

/* 中心出勤率数字淡入 */
.ring-rate-label,
.ring-rate-value {
  opacity: 0;
  transition: opacity 0.56s var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) 0.38s;
}
.mounted .ring-rate-value,
.mounted .ring-rate-label {
  opacity: 1;
}

/* 部门列表项逐项淡入 + 上滑 */
.dept-item {
  opacity: 0;
  transform: translateY(16px);
}
.dept-item.dept-item--enter {
  animation: dept-fade-up var(--screen-enter-duration, 0.56s) var(--screen-enter-ease, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
  animation-delay: var(--delay, 0s);
}

@keyframes dept-fade-up {
  0%   { opacity: 0; transform: translateY(var(--screen-enter-offset, 14px)); }
  100% { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1600px), (max-height: 1080px) {
  .personnel-panel {
    gap: 12px;
  }

  .module-section {
    padding: 12px 14px;
  }

  .section-label {
    margin-bottom: 8px;
    gap: 8px;
  }

  .label-text {
    font-size: 12px;
    letter-spacing: 2px;
  }

  .ring-container {
    max-height: 220px;
  }

  .ring-svg {
    width: 78%;
    height: 78%;
  }

  .ring-rate-label {
    font-size: 11px;
  }

  .ring-rate-value {
    font-size: 22px;
  }

  .corner-label {
    padding: 5px 10px;
  }

  .corner-label.top-left,
  .corner-label.top-right {
    top: 8%;
  }

  .corner-label.bottom-left,
  .corner-label.bottom-right {
    bottom: 8%;
  }

  .corner-label.top-left,
  .corner-label.bottom-left {
    left: 10%;
  }

  .corner-label.top-right,
  .corner-label.bottom-right {
    right: 10%;
  }

  .corner-value {
    font-size: 22px;
  }

  .corner-desc {
    font-size: 10px;
  }

  .ring-legend {
    margin-top: 6px;
    gap: 14px;
  }

  .legend-item {
    font-size: 10px;
    padding: 3px 8px;
  }

  .dept-list {
    gap: 8px;
    padding: 2px 2px 8px;
  }

  .dept-item {
    padding: 8px 10px;
    gap: 6px;
  }

  .dept-name {
    font-size: 13px;
  }

  .dept-rate {
    font-size: 14px;
    min-width: 42px;
  }

  .dept-count {
    font-size: 11px;
  }

  .dept-bar-track {
    height: 10px;
  }
}

@media (max-width: 1366px), (max-height: 820px) {
  .personnel-panel {
    gap: 10px;
  }

  .module-section {
    padding: 10px 12px;
  }

  .ring-container {
    max-height: 180px;
  }

  .corner-value {
    font-size: 20px;
  }

  .corner-desc {
    font-size: 9px;
  }

  .corner-label {
    padding: 4px 8px;
  }

  .dept-item {
    padding: 6px 8px;
  }

  .dept-name {
    font-size: 12px;
  }

  .dept-rate {
    font-size: 13px;
    min-width: 40px;
  }
}
</style>
