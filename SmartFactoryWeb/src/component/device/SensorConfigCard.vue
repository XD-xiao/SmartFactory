<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSensorConfigService, deleteSensorConfigService } from '@/api/DeviceApi'
import SensorEditDialog from './SensorEditDialog.vue'

const props = defineProps({
  sensorType: {
    type: String,
    required: true
  },
  sensorName: {
    type: String,
    required: true
  },
  active: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['refresh'])

// ==================== 状态指示器 ====================

const statusText = computed(() => props.active ? '在线' : '离线')

// ==================== 业务逻辑 ====================

// 无阈值传感器（仅删除，不可编辑）
const NO_THRESHOLD_TYPES = ['23', '24', '39']
const hasThreshold = !NO_THRESHOLD_TYPES.includes(props.sensorType)

const loading = ref(false)
const config = ref(null)
const showEditDialog = ref(false)

const fetchConfig = async () => {
  loading.value = true
  try {
    const res = await getSensorConfigService(props.sensorType)
    config.value = res.data
  } catch {
    config.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchConfig()
})

const displayGroups = computed(() => {
  if (!config.value) return []
  const data = config.value
  const excludeKeys = ['id', 'sceneId', 'createTime', 'updateTime']

  // 温湿度传感器(type 25)：分立温度 / 湿度两组展示
  if (props.sensorType === '25') {
    const tempKeys = ['temMax', 'temMaxth', 'temMin']
    const humKeys = ['humMax', 'humMaxth', 'humMin']
    return [
      {
        category: '温度',
        items: tempKeys.filter(k => k in data).map(k => ({ key: k, value: data[k] }))
      },
      {
        category: '湿度',
        items: humKeys.filter(k => k in data).map(k => ({ key: k, value: data[k] }))
      }
    ]
  }

  // 其余传感器：单组展示
  const allKeys = Object.keys(data).filter(k => !excludeKeys.includes(k))
  return [{
    category: '',
    items: allKeys.map(k => ({ key: k, value: data[k] }))
  }]
})

const getFieldLabel = (key) => {
  const map = {
    'temMax': '温度上限', 'temMaxth': '温度预警', 'temMin': '温度下限',
    'humMax': '湿度上限', 'humMaxth': '湿度预警', 'humMin': '湿度下限',
    'dioxideMax': 'CO2上限', 'dioxideMaxth': 'CO2预警', 'dioxideMin': 'CO2下限',
    'distanceMax': '距离上限', 'distanceMaxth': '距离预警', 'distanceMin': '距离下限',
    'smokeMax': '烟雾上限', 'smokeMaxth': '烟雾预警', 'smokeMin': '烟雾下限',
    'illuminationMax': '光照上限', 'illuminationMaxth': '光照预警', 'illuminationMin': '光照下限',
    'pressureMax': '压强上限', 'pressureMaxth': '压强预警', 'pressureMin': '压强下限',
    'gasMax': '燃气上限', 'gasMaxth': '燃气预警', 'gasMin': '燃气下限',
    'monoxideMax': 'CO上限', 'monoxideMaxth': 'CO预警', 'monoxideMin': 'CO下限'
  }
  return map[key] || key
}

const handleEdit = () => {
  showEditDialog.value = true
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除「${props.sensorName}」的传感器配置吗？`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteSensorConfigService(props.sensorType)
    ElMessage.success('删除成功')
    config.value = null
    emit('refresh')
  } catch {
    // 取消或错误
  }
}

const handleEditClose = () => {
  showEditDialog.value = false
}

const handleRefresh = () => {
  fetchConfig()
  emit('refresh')
}

defineExpose({ fetchConfig })
</script>

<template>
  <div class="sensor-card" v-loading="loading">
    <div class="card-header">
      <span class="card-title">{{ sensorName }}</span>
      <span class="card-type">编号: {{ sensorType }}</span>
      <span class="status-dot" :class="{ 'status-online': active, 'status-offline': !active }">
        <i class="status-icon"></i>
        {{ statusText }}
      </span>
    </div>

    <div class="card-body">
      <div v-if="!config" class="card-empty">暂无配置数据</div>
      <div v-else class="card-fields">
        <div
          v-for="(group, gIdx) in displayGroups"
          :key="gIdx"
          class="field-group"
        >
          <div v-if="group.category" class="group-label">{{ group.category }}</div>
          <div class="field-row">
            <div
              v-for="item in group.items"
              :key="item.key"
              class="field-item"
            >
              <span class="field-label">{{ getFieldLabel(item.key) }}</span>
              <span class="field-value">{{ item.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card-actions">
      <el-button
        v-if="hasThreshold"
        type="primary"
        size="small"
        :disabled="!config"
        @click="handleEdit"
      >
        编辑
      </el-button>
      <el-button
        type="danger"
        size="small"
        plain
        :disabled="!config"
        @click="handleDelete"
      >
        删除
      </el-button>
    </div>

    <SensorEditDialog
      v-if="hasThreshold"
      v-model="showEditDialog"
      :sensor-type="sensorType"
      :sensor-name="sensorName"
      @refresh="handleRefresh"
      @update:model-value="handleEditClose"
    />
  </div>
</template>

<style scoped>
.sensor-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: box-shadow 0.25s, transform 0.25s;
}

.sensor-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 110px;
  padding-right: 16px;
  border-right: 1px solid #ebeef5;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.card-type {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.status-dot {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  margin-top: 6px;
  color: #909399;
}

.status-icon {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}

.status-online {
  color: #67c23a;
}

.status-online .status-icon {
  background: #67c23a;
  box-shadow: 0 0 4px rgba(103, 194, 58, 0.5);
}

.status-offline {
  color: #c0c4cc;
}

.status-offline .status-icon {
  background: #c0c4cc;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-empty {
  color: #909399;
  font-size: 13px;
  padding: 8px 0;
}

.card-fields {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-label {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
  padding-left: 2px;
}

.field-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px 18px;
  align-items: center;
}

.field-item {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.field-label {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.field-value {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}

.card-actions {
  flex-shrink: 0;
  display: flex;
  gap: 8px;
}

/* ==================== 响应式 ==================== */

@media (max-width: 768px) {
  .sensor-card {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 14px 16px;
  }

  .card-header {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    min-width: 0;
    padding-right: 0;
    padding-bottom: 10px;
    border-right: none;
    border-bottom: 1px solid #ebeef5;
  }

  .card-type {
    margin-top: 0;
  }

  .card-actions {
    justify-content: flex-end;
  }

  .field-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 4px 12px;
  }
}
</style>
