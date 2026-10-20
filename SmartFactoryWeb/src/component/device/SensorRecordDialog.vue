<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { List, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getSensorRecordListService } from '@/api/DeviceApi.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  sensorConfigId: {
    type: Number,
    default: null
  },
  sensorName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

// ---------- 视图模式 ----------
const viewMode = ref('list') // 'list' | 'chart'

// ---------- 列表数据 ----------
const recordList = ref([])
const total = ref(0)
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')

// ---------- 图表相关 ----------
const chartContainer = ref(null)
let chartInstance = null
const chartLoading = ref(false)
const CHART_PAGE_SIZE = 200

const statusLabel = (val) => {
  const map = { '-1': '超出上下限', '0': '正常', '1': '轻微', '2': '警告', '3': '危险' }
  return map[String(val)] ?? val
}

const statusTagType = (val) => {
  const map = { '-1': 'danger', '0': 'success', '1': '', '2': 'warning', '3': 'danger' }
  return map[String(val)] ?? 'info'
}

const statusColor = (val) => {
  const map = { '-1': '#F56C6C', '0': '#67C23A', '1': '#909399', '2': '#E6A23C', '3': '#F56C6C' }
  return map[String(val)] ?? '#909399'
}

const statusOptions = [
  { label: '全部', value: '' },
  { label: '超出上下限', value: -1 },
  { label: '正常', value: 0 },
  { label: '轻微', value: 1 },
  { label: '警告', value: 2 },
  { label: '危险', value: 3 }
]

// ---------- 列表请求 ----------
const fetchRecords = async () => {
  loading.value = true
  try {
    const params = {
      id: props.sensorConfigId || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    // 空字符串表示"全部"，不传 status 参数
    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }
    const res = await getSensorRecordListService(params)
    recordList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch {
    recordList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// ---------- 图表请求（大 pageSize） ----------
const fetchChartData = async () => {
  chartLoading.value = true
  try {
    const params = {
      id: props.sensorConfigId || undefined,
      pageNum: 1,
      pageSize: CHART_PAGE_SIZE
    }
    const res = await getSensorRecordListService(params)
    const list = res.data?.list ?? []
    renderChart(list)
  } catch {
    renderChart([])
  } finally {
    chartLoading.value = false
  }
}

// ---------- ECharts 渲染 ----------
const renderChart = (data) => {
  if (!chartContainer.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
  }

  const sorted = [...data].sort((a, b) => {
    return new Date(a.datetime) - new Date(b.datetime)
  })

  const xData = sorted.map((r) => r.datetime ?? '')
  const yData = sorted.map((r) => r.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 13 },
      formatter: (params) => {
        const p = params[0]
        return `<div style="font-weight:600;margin-bottom:4px">${p.axisValue}</div>
                <div style="color:#606266">数值：<span style="font-weight:600;color:#409EFF">${p.value}</span></div>`
      }
    },
    grid: {
      left: 48,
      right: 24,
      top: 20,
      bottom: 32
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisTick: { show: false },
      axisLabel: {
        color: '#909399',
        fontSize: 11,
        formatter: (v) => {
          if (!v) return ''
          // 只显示 时:分:秒
          const idx = v.lastIndexOf(' ')
          return idx > -1 ? v.slice(idx + 1) : v
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '数值',
      nameTextStyle: { color: '#909399', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f2f3f5', type: 'dashed' } },
      axisLabel: { color: '#909399', fontSize: 11 }
    },
    series: [
      {
        type: 'line',
        data: yData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 4,
        lineStyle: { color: '#409EFF', width: 2 },
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.25)' },
            { offset: 1, color: 'rgba(64,158,255,0.02)' }
          ])
        }
      }
    ]
  }

  chartInstance.setOption(option, true)
}

const handleResize = () => {
  chartInstance?.resize()
}

// ---------- 事件处理 ----------
const handleStatusChange = () => {
  pageNum.value = 1
  fetchRecords()
}

const handlePageChange = (val) => {
  pageNum.value = val
  fetchRecords()
}

// ---------- 弹窗开关 ----------
watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      pageNum.value = 1
      statusFilter.value = ''
      viewMode.value = 'list'
      fetchRecords()
    }
  }
)

// ---------- 视图切换：chartContainer 就绪后初始化图表 ----------
watch([viewMode, chartContainer], ([mode, container]) => {
  if (mode === 'chart' && container) {
    // 先清理旧实例
    if (chartInstance) {
      chartInstance.dispose()
      chartInstance = null
    }
    fetchChartData()
  }
})

// ---------- 响应式 ----------
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    width="860px"
    top="4vh"
    destroy-on-close
    :close-on-click-modal="false"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <!-- 自定义头部 -->
    <template #header>
      <div class="dialog-header">
        <div class="dialog-header-left">
          <span class="dialog-title-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 3v18h18"/>
              <path d="M7 16l4-8 4 4 4-8"/>
            </svg>
          </span>
          <span class="dialog-title-text">传感器记录</span>
          <span v-if="sensorName" class="dialog-title-sub">- {{ sensorName }}</span>
        </div>
        <div class="dialog-header-right">
          <el-radio-group
            v-model="viewMode"
            size="small"
          >
            <el-radio-button value="list">
              <el-icon class="view-icon"><List /></el-icon>
              <span>列表</span>
            </el-radio-button>
            <el-radio-button value="chart">
              <el-icon class="view-icon"><TrendCharts /></el-icon>
              <span>折线图</span>
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </template>

    <!-- 列表视图 -->
    <transition name="view-fade" mode="out-in">
      <div v-if="viewMode === 'list'" key="list" class="view-panel">
        <div class="record-toolbar">
          <span class="toolbar-label">状态筛选</span>
          <el-select
            v-model="statusFilter"
            placeholder="全部状态"
            size="small"
            clearable
            style="width: 140px"
            @change="handleStatusChange"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.label"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>

        <el-table
          v-loading="loading"
          :data="recordList"
          row-key="id"
          stripe
          empty-text="暂无传感器记录"
          max-height="420"
          size="small"
          :header-cell-style="{
            background: '#f5f7fa',
            color: '#606266',
            fontWeight: 600,
            fontSize: '13px'
          }"
        >
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="sensorId" label="传感器配置ID" width="120" align="center" />
          <el-table-column prop="com" label="串口" width="100" align="center" />
          <el-table-column prop="value" label="数值" width="100" align="center">
            <template #default="{ row }">
              <span class="value-cell">{{ row.value }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag
                :type="statusTagType(row.status)"
                size="small"
                effect="plain"
                round
              >
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="datetime" label="记录时间" min-width="160" show-overflow-tooltip />
        </el-table>

        <div class="pagination-wrapper">
          <div class="pagination-summary">
            共 <span>{{ total }}</span> 条记录，当前页 <span>{{ recordList.length }}</span> 条
          </div>
          <el-pagination
            background
            size="small"
            :current-page="pageNum"
            :page-size="pageSize"
            layout="prev, pager, next, jumper"
            :total="total"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <!-- 折线图视图 -->
      <div v-else key="chart" class="view-panel chart-panel">
        <div class="chart-toolbar">
          <span class="toolbar-label">历史变化趋势</span>
          <span class="toolbar-hint">最近 {{ CHART_PAGE_SIZE }} 条记录</span>
        </div>
        <div
          v-loading="chartLoading"
          class="chart-container"
          element-loading-text="加载图表数据中..."
        >
          <div
            ref="chartContainer"
            class="chart-box"
          />
          <div v-if="!chartLoading" class="chart-legend">
            <span class="legend-dot" style="background:#409EFF" /> 传感器数值
          </div>
        </div>
      </div>
    </transition>
  </el-dialog>
</template>

<style scoped>
/* ===== Dialog Header ===== */
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}

.dialog-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dialog-title-icon {
  display: flex;
  align-items: center;
  color: #409EFF;
}

.dialog-title-text {
  font-size: 17px;
  font-weight: 700;
  color: #1d2129;
  letter-spacing: 0.3px;
}

.dialog-title-sub {
  font-size: 14px;
  font-weight: 400;
  color: #86909c;
}

.dialog-header-right {
  display: flex;
  align-items: center;
}

.view-icon {
  margin-right: 4px;
  font-size: 14px;
}

/* ===== View Panel ===== */
.view-panel {
  min-height: 320px;
}

/* ===== View Transition ===== */
.view-fade-enter-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.view-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.view-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.view-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ===== List Toolbar ===== */
.record-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding: 10px 14px;
  background: #f7f8fa;
  border-radius: 8px;
}

.toolbar-label {
  font-size: 13px;
  font-weight: 600;
  color: #4e5969;
}

/* ===== Table ===== */
.value-cell {
  font-weight: 600;
  color: #409EFF;
  font-variant-numeric: tabular-nums;
}

/* ===== Pagination ===== */
.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 16px;
  padding: 0 4px;
}

.pagination-summary {
  font-size: 13px;
  color: #86909c;
}

.pagination-summary span {
  color: #1d2129;
  font-weight: 600;
}

/* ===== Chart Panel ===== */
.chart-panel {
  min-height: 360px;
}

.chart-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding: 10px 14px;
  background: #f7f8fa;
  border-radius: 8px;
}

.toolbar-hint {
  font-size: 12px;
  color: #a8aebb;
}

.chart-container {
  position: relative;
  width: 100%;
  height: 400px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #f0f1f3;
  overflow: hidden;
}

.chart-box {
  width: 100%;
  height: 100%;
}

.chart-legend {
  position: absolute;
  top: 12px;
  right: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #86909c;
  background: rgba(255, 255, 255, 0.85);
  padding: 4px 10px;
  border-radius: 12px;
  backdrop-filter: blur(4px);
}

.legend-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

/* ===== Radio Group Override ===== */
:deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 6px 14px;
  font-size: 13px;
}

/* ===== Dialog Override ===== */
:deep(.el-dialog__header) {
  padding: 18px 24px 14px;
  border-bottom: 1px solid #f0f1f3;
  margin-right: 0;
}

:deep(.el-dialog__body) {
  padding: 20px 24px 24px;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

/* ===== Responsive ===== */
@media (max-width: 920px) {
  .dialog-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .dialog-header-right {
    width: 100%;
    justify-content: flex-end;
  }

  .pagination-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .chart-container {
    height: 280px;
  }
}
</style>
