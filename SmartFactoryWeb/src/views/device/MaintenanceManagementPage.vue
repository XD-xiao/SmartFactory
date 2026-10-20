<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addMaintenanceService,
  deleteMaintenanceService,
  getDeviceListService,
  getMaintenanceListService,
  updateMaintenanceService
} from '@/api/DeviceApi.js'
import MaintenanceAddDialog from '@/component/device/MaintenanceAddDialog.vue'
import MaintenanceEditDialog from '@/component/device/MaintenanceEditDialog.vue'

const searchForm = ref({
  sn: '',
  reason: '',
  failureLevel: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const failureLevelOptions = [
  { label: '紧急', value: 1 },
  { label: '高', value: 2 },
  { label: '中', value: 3 },
  { label: '低', value: 4 }
]
// 添加故障等级映射函数
const getFailureLevelLabel = (level) => {
  const option = failureLevelOptions.find(item => item.value === level)
  return option ? option.label : level
}

const maintenanceList = ref([])
const deviceOptions = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentMaintenance = ref({})

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const getStatusValue = (status) => {
  if (status === '' || status === null || status === undefined) {
    return ''
  }

  if (typeof status === 'number') {
    return status
  }

  const statusMap = {
    待处理: 0,
    待维修: 0,
    pending: 0,
    维修中: 1,
    处理中: 1,
    processing: 1,
    已恢复: 2,
    已完成: 2,
    repaired: 2,
    done: 2
  }

  if (statusMap[status] !== undefined) {
    return statusMap[status]
  }

  const numericValue = Number(status)
  return Number.isNaN(numericValue) ? 0 : numericValue
}

const formatStatusText = (status) => {
  const statusValue = getStatusValue(status)
  if (statusValue === 0) {
    return '待处理'
  }
  if (statusValue === 1) {
    return '维修中'
  }
  if (statusValue === 2) {
    return '已恢复'
  }
  return String(status ?? '未知状态')
}

const normalizeMaintenance = (item) => ({
  ...item,
  id: item?.id ?? '',
  sn: item?.sn ?? '',
  reason: item?.reason ?? '',
  failureLevel: item?.failureLevel ?? '',
  failureTime: item?.failureTime ?? '',
  failureRecoverTime: item?.failureRecoverTime ?? '',
  statusValue: getStatusValue(item?.status)
})

const getDeviceOptions = async () => {
  try {
    const res = await getDeviceListService(undefined, undefined, undefined, undefined, 1, 999, undefined)
    deviceOptions.value = (res.data?.list ?? []).map((item) => ({
      id: item.id,
      sn: item.sn,
      type: item.type
    }))
  } catch (error) {
    deviceOptions.value = []
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取设备选项失败')
    }
  }
}

const getMaintenanceList = async () => {
  tableLoading.value = true
  try {
    const { sn, reason, failureLevel, pageNum, pageSize, status } = searchForm.value
    const res = await getMaintenanceListService(
      sn?.trim() || undefined,
      reason?.trim() || undefined,
      failureLevel === '' ? undefined : failureLevel,
      pageNum,
      pageSize,
      (status == null || status === '') ? undefined : Number(status)
    )

    const list = res.data?.list ?? res.list ?? []
    const totalCount = res.data?.total ?? res.total ?? 0
    maintenanceList.value = list.map(normalizeMaintenance)
    total.value = totalCount
  } catch (error) {
    maintenanceList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取维修工单失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getMaintenanceList()
}

const handleReset = () => {
  searchForm.value = {
    sn: '',
    reason: '',
    failureLevel: '',
    status: '',
    pageNum: 1,
    pageSize: 10
  }
  void getMaintenanceList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getMaintenanceList()
}

const handleAddMaintenance = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addMaintenanceService(formData)
    ElMessage.success(res.message || '新增维修工单成功')
    addDialogVisible.value = false
    await getMaintenanceList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('新增维修工单失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentMaintenance.value = normalizeMaintenance(row)
  editDialogVisible.value = true
}

const handleUpdateMaintenance = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateMaintenanceService(formData.id, formData)
    ElMessage.success(res.message || '更新维修状态成功')
    editDialogVisible.value = false
    await getMaintenanceList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('更新维修状态失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteMaintenance = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除设备“${row.sn}”的维修工单吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    const res = await deleteMaintenanceService(row.id)
    ElMessage.success(res.message || '删除维修工单成功')
    if (maintenanceList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getMaintenanceList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除维修工单失败')
    }
  }
}

onMounted(async () => {
  await getDeviceOptions()
  await getMaintenanceList()
})
</script>

<template>
  <div class="maintenance-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">维修管理</h2>
        <p class="page-subtitle">跟踪设备故障工单，处理维修状态并保留维修记录。</p>
      </div>

      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">新增工单</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent>
        <el-form-item label="设备SN">
          <el-input
            v-model="searchForm.sn"
            placeholder="请输入设备SN"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="故障等级">
          <el-select
            v-model="searchForm.failureLevel"
            placeholder="请选择故障等级"
            clearable
            class="filter-select"
          >
            <el-option v-for="item in failureLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="filter-select">
            <el-option label="待处理" :value="0" />
            <el-option label="维修中" :value="1" />
            <el-option label="已恢复" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="故障原因">
          <el-input
            v-model="searchForm.reason"
            placeholder="请输入故障原因"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
      </el-form>

      <div class="search-actions">
        <el-button type="primary" :loading="tableLoading" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="page-card table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">维修工单列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="maintenanceList"
        row-key="id"
        stripe
        empty-text="暂无维修工单"
        class="maintenance-table"
        height="100%"
      >
        <el-table-column prop="id" label="工单ID" width="100" />
        <el-table-column prop="sn" label="设备SN" min-width="160" show-overflow-tooltip />
        <el-table-column label="故障等级" width="120">
          <template #default="{ row }">
            {{ getFailureLevelLabel(row.failureLevel) }}
          </template>
        </el-table-column>
        <el-table-column prop="failureTime" label="故障时间" min-width="180" show-overflow-tooltip />
        <el-table-column label="处理状态" width="110">
          <template #default="{ row }">
            <el-tag
              :type="row.statusValue === 2 ? 'success' : row.statusValue === 1 ? 'warning' : 'danger'"
            >
              {{ formatStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failureRecoverTime" label="恢复时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="reason" label="故障原因" min-width="220" show-overflow-tooltip />
        <el-table-column fixed="right" label="操作" width="170">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">
              处理
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteMaintenance(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ maintenanceList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <MaintenanceAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      :devices="deviceOptions"
      @submit="handleAddMaintenance"
    />

    <MaintenanceEditDialog
      v-model="editDialogVisible"
      :maintenance="currentMaintenance"
      :loading="submitLoading"
      @submit="handleUpdateMaintenance"
    />
  </div>
</template>

<style scoped>
.maintenance-page {
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

.search-form :deep(.el-input),
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

.maintenance-table {
  flex: 1;
  min-height: 0;
}

.maintenance-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
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
  .maintenance-page {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-card {
    flex: none;
  }

  .search-form :deep(.el-input),
  .filter-select {
    width: 100%;
  }

  .pagination-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
