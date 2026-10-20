<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAgentDetectionListService } from '@/api/DeviceApi.js'
import DetectionReportDialog from '@/component/device/DetectionReportDialog.vue'

const searchForm = ref({
  status: '',
  pageNum: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '分析成功', value: 'SUCCESS' },
  { label: '分析失败', value: 'FAILED' }
]

const detectionList = ref([])
const total = ref(0)
const tableLoading = ref(false)

const totalPages = computed(() => {
  if (!total.value) return 0
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const formatTime = (time) => {
  if (!time) return '-'
  return time
}

const truncateReport = (report, maxLen = 40) => {
  if (!report) return '-'
  return report.length > maxLen ? report.substring(0, maxLen) + '...' : report
}

const reportDialogVisible = ref(false)
const currentReport = ref({ id: null, report: '', status: '' })

const handleViewReport = (row) => {
  currentReport.value = {
    id: row.id,
    report: row.resultReport || '',
    status: row.status || ''
  }
  reportDialogVisible.value = true
}

const getStatusType = (status) => {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'danger'
  return 'info'
}

const getStatusLabel = (status) => {
  if (status === 'SUCCESS') return '成功'
  if (status === 'FAILED') return '失败'
  return status ?? '-'
}

const getNotifiedLabel = (isNotified) => {
  return isNotified === 1 ? '已通知' : '未通知'
}

const getNotifiedType = (isNotified) => {
  return isNotified === 1 ? 'warning' : 'info'
}

const getDetectionList = async () => {
  tableLoading.value = true
  try {
    const { status, pageNum, pageSize } = searchForm.value
    const res = await getAgentDetectionListService(
      status || undefined,
      pageNum,
      pageSize
    )

    const list = res.data?.list ?? res.list ?? []
    const totalCount = res.data?.total ?? res.total ?? 0
    detectionList.value = list
    total.value = totalCount
  } catch (error) {
    detectionList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取检测记录失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getDetectionList()
}

const handleReset = () => {
  searchForm.value = {
    status: '',
    pageNum: 1,
    pageSize: 10
  }
  void getDetectionList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getDetectionList()
}

onMounted(() => {
  void getDetectionList()
})
</script>

<template>
  <div class="detection-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">监测记录</h2>
        <p class="page-subtitle">查看 AI 智能检测的历史分析报告，包含预测性维护与异常检测结果。</p>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent>
        <el-form-item label="检测状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            class="filter-select"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="search-actions">
        <el-button type="primary" :loading="tableLoading" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="page-card table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">检测记录列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="detectionList"
        row-key="id"
        stripe
        empty-text="暂无检测记录"
        class="detection-table"
        height="100%"
      >
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column label="检测状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="通知状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getNotifiedType(row.isNotified)">
              {{ getNotifiedLabel(row.isNotified) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataStartTime" label="数据起始时间" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatTime(row.dataStartTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="dataEndTime" label="数据截止时间" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatTime(row.dataEndTime) }}
          </template>
        </el-table-column>
        <el-table-column label="分析报告" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="report-cell">{{ truncateReport(row.resultReport) }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewReport(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="analysisStartTime" label="分析开始时间" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatTime(row.analysisStartTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="analysisEndTime" label="分析结束时间" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatTime(row.analysisEndTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ detectionList.length }}</span> 条，共 <span>{{ total }}</span> 条
        </div>
        <el-pagination
          background
          :current-page="searchForm.pageNum"
          :page-size="searchForm.pageSize"
          layout="prev, pager, next, jumper"
          :total="total"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <DetectionReportDialog
      v-model="reportDialogVisible"
      :record-id="currentReport.id"
      :report="currentReport.report"
      :status="currentReport.status"
    />
  </div>
</template>

<style scoped>
.detection-page {
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

.page-card {
  border: none;
  border-radius: 14px;
  overflow: hidden;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 12px;
  align-items: center;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-select {
  width: 160px;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.table-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.table-card :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.toolbar-tip {
  font-size: 13px;
  color: #909399;
}

.detection-table {
  flex: 1;
  min-height: 0;
}

.detection-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.report-cell {
  color: #606266;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.pagination-summary {
  font-size: 13px;
  color: #909399;
}

.pagination-summary span {
  color: #303133;
  font-weight: 600;
}

@media (max-width: 768px) {
  .detection-page {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-card {
    flex: none;
  }

  .filter-select {
    width: 100%;
  }

  .pagination-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
