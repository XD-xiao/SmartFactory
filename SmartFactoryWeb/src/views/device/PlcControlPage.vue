<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useWebSocket } from '@/utils/useWebSocket'
import { controlPlcService } from '@/api/DeviceApi'
import { ElMessage } from 'element-plus'

// ==================== WebSocket 相关 ====================

const subscriptionTopic = ref('/topic/plc/status')
const { connect, subscribe, disconnect } = useWebSocket()
const statusMessage = ref('正在连接...')
const deviceSn = ref('')
const deviceList = ref([])
const lastUpdateTime = ref('')
const controlling = ref(false)

const handleIncomingMessage = (message) => {
  try {
    const body = JSON.parse(message.body)
    if (body.type !== 'plc_status') return

    deviceSn.value = body.sn || ''
    const devices = body.devices || []
    deviceList.value = devices.map((d) => ({
      seqNo: d.seqNo ?? d.seq_no,
      action: d.action
    }))
    lastUpdateTime.value = body.timestamp || ''
  } catch {
    // 忽略解析失败的消息
  }
}

const connectWebSocket = async () => {
  try {
    await connect(() => {
      statusMessage.value = `已连接，订阅 ${subscriptionTopic.value}`
      subscribe(subscriptionTopic.value, handleIncomingMessage)
    })
  } catch {
    statusMessage.value = '连接失败，请检查后端服务是否已启动'
  }
}

const disconnectWebSocket = async () => {
  await disconnect()
  statusMessage.value = '已断开连接'
}

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

// ==================== 产线控制 ====================

const handleToggle = async (device) => {
  const newAction = device.action === 1 ? 0 : 1
  controlling.value = true
  try {
    await controlPlcService({
      sn: deviceSn.value,
      index: device.seqNo,
      action: newAction
    })
    ElMessage.success(`产线 ${device.seqNo} 控制指令已发送`)
  } catch {
    ElMessage.error('控制指令发送失败')
  } finally {
    controlling.value = false
  }
}

// ==================== 计算属性 ====================

const connected = computed(() => statusMessage.value.includes('已连接'))

const deviceStatusTag = (action) => {
  return action === 1 ? 'success' : 'info'
}

const deviceStatusText = (action) => {
  return action === 1 ? '运行中' : '已停机'
}

const deviceStatusIcon = (action) => {
  return action === 1 ? '⚡' : '⏸'
}

const formattedTime = computed(() => {
  if (!lastUpdateTime.value) return '-'
  return lastUpdateTime.value.replace('T', ' ').substring(0, 19)
})
</script>

<template>
  <div class="plc-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">PLC 控制面板</h2>
        <p class="page-subtitle">实时查看各产线开关状态，设备连接后自动同步。</p>
      </div>
      <div class="header-right">
        <el-tag
          size="small"
          :type="connected ? 'success' : 'warning'"
          effect="plain"
        >
          {{ statusMessage }}
        </el-tag>
      </div>
    </div>

    <el-card v-if="deviceSn" shadow="never" class="page-card sn-card">
      <div class="sn-info">
        <span class="sn-label">设备 SN</span>
        <span class="sn-value">{{ deviceSn }}</span>
        <span class="sn-time">最后更新：{{ formattedTime }}</span>
      </div>
    </el-card>

    <el-card shadow="never" class="page-card device-card">
      <template #header>
        <div class="card-header-bar">
          <span class="card-title">产线状态</span>
          <span class="card-count">共 {{ deviceList.length }} 条产线</span>
        </div>
      </template>

      <el-empty v-if="deviceList.length === 0" description="暂无产线数据，请等待 PLC 设备同步" />

      <div v-else class="device-grid">
        <div
          v-for="device in deviceList"
          :key="device.seqNo"
          class="device-item"
          :class="{ 'device-running': device.action === 1, 'device-stopped': device.action === 0 }"
        >
          <div class="device-badge">
            <span class="device-icon">{{ deviceStatusIcon(device.action) }}</span>
            <span class="device-seq">产线 {{ device.seqNo }}</span>
          </div>
          <el-tag
            :type="deviceStatusTag(device.action)"
            size="small"
            effect="dark"
            class="device-tag"
          >
            {{ deviceStatusText(device.action) }}
          </el-tag>
          <el-switch
            :model-value="device.action === 1"
            :loading="controlling"
            :disabled="!deviceSn"
            active-text="开"
            inactive-text="关"
            size="small"
            @change="handleToggle(device)"
          />
          <div class="device-indicator">
            <span
              class="indicator-dot"
              :class="{ 'dot-on': device.action === 1, 'dot-off': device.action === 0 }"
            ></span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.plc-page {
  height: 100%;
  min-height: 0;
  padding: 20px;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-right {
  flex-shrink: 0;
}

.page-card {
  border: none;
  border-radius: 14px;
  overflow: hidden;
}

/* ==================== SN 信息卡片 ==================== */

.sn-card {
  flex-shrink: 0;
}

.sn-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.sn-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sn-label {
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

.sn-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  font-family: 'JetBrains Mono', 'Courier New', monospace;
  letter-spacing: 0.5px;
}

.sn-time {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}

/* ==================== 产线卡片 ==================== */

.device-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.device-card :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  overflow-y: auto;
}

.card-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-count {
  font-size: 13px;
  color: #909399;
}

/* ==================== 设备网格 ==================== */

.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  padding: 4px 0;
}

.device-item {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  transition: box-shadow 0.25s, transform 0.25s, border-color 0.25s;
}

.device-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.device-running {
  border-color: #b3e19d;
  background: linear-gradient(135deg, #f0f9eb 0%, #fff 100%);
}

.device-stopped {
  border-color: #e4e7ed;
  background: linear-gradient(135deg, #fafafa 0%, #fff 100%);
}

.device-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.device-icon {
  font-size: 28px;
  line-height: 1;
}

.device-seq {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.device-tag {
  font-size: 12px;
}

.device-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.dot-on {
  background: #67c23a;
  box-shadow: 0 0 6px rgba(103, 194, 58, 0.6);
  animation: pulse 1.5s infinite;
}

.dot-off {
  background: #c0c4cc;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 6px rgba(103, 194, 58, 0.6);
  }
  50% {
    box-shadow: 0 0 14px rgba(103, 194, 58, 0.9);
  }
}

/* ==================== 响应式 ==================== */

@media (max-width: 768px) {
  .plc-page {
    padding: 12px;
    gap: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .device-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }

  .sn-info {
    flex-wrap: wrap;
  }

  .sn-time {
    margin-left: 0;
    width: 100%;
  }
}
</style>
