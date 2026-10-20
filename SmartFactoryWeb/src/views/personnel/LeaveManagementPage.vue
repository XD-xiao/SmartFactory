<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addLeaveRecordService,
  deleteLeaveRecordService,
  getLeaveListService,
  updateLeaveRecordService
} from '@/api/PersonManagementApi'
import LeaveAddDialog from '@/component/personnel/LeaveAddDialog.vue'
import LeaveEditDialog from '@/component/personnel/LeaveEditDialog.vue'

const searchForm = ref({
  pageNum: 1,
  pageSize: 10,
  employeeNumber: '',
  leaveType: '',
  status: '',
  startDate: '',
  endDate: ''
})

const leaveList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentLeaveRecord = ref({})

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const leaveTypeMap = {
  1: '年假',
  2: '病假',
  3: '事假',
  4: '调休'
}

const leaveStatusMap = {
  0: '待审批',
  1: '已通过',
  2: '已驳回'
}

const getLeaveList = async () => {
  tableLoading.value = true
  try {
    const { pageNum, pageSize, employeeNumber, leaveType, status, startDate, endDate } = searchForm.value
    const res = await getLeaveListService(
      pageNum,
      pageSize,
      employeeNumber?.trim() || undefined,
      leaveType === '' ? undefined : leaveType,
      status === '' ? undefined : status,
      startDate || undefined,
      endDate || undefined
    )
    leaveList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    leaveList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取请假记录失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getLeaveList()
}

const handleReset = () => {
  searchForm.value = {
    pageNum: 1,
    pageSize: 10,
    employeeNumber: '',
    leaveType: '',
    status: '',
    startDate: '',
    endDate: ''
  }
  void getLeaveList()
}

const handleDateRangeChange = (value) => {
  searchForm.value.startDate = value?.[0] ?? ''
  searchForm.value.endDate = value?.[1] ?? ''
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getLeaveList()
}

const handleAddLeaveRecord = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addLeaveRecordService(formData)
    ElMessage.success(res.message || '新增请假记录成功')
    addDialogVisible.value = false
    await getLeaveList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('新增请假记录失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentLeaveRecord.value = { ...row }
  editDialogVisible.value = true
}

const handleUpdateLeaveRecord = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateLeaveRecordService(formData)
    ElMessage.success(res.message || '更新请假记录成功')
    editDialogVisible.value = false
    await getLeaveList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('更新请假记录失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteLeaveRecord = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除员工 ${row.employeeName || row.employeeNumber} 的请假记录吗？`,
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
    const res = await deleteLeaveRecordService(row.leaveID ?? row.leaveId)
    ElMessage.success(res.message || '删除请假记录成功')
    if (leaveList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getLeaveList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除请假记录失败')
    }
  }
}

const formatLeaveType = (leaveType) => leaveTypeMap[Number(leaveType)] || '未知类型'
const formatStatusText = (status) => leaveStatusMap[Number(status)] || '未知状态'
const formatDateRange = (row) => `${row.startDate || '--'} 至 ${row.endDate || '--'}`

onMounted(() => {
  void getLeaveList()
})
</script>

<template>
  <div class="leave-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">请假管理</h2>
      </div>

      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">新增请假</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent>
        <el-form-item label="员工工号">
          <el-input
            v-model="searchForm.employeeNumber"
            placeholder="请输入员工工号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="请假类型">
          <el-select v-model="searchForm.leaveType" placeholder="全部类型" clearable class="filter-select">
            <el-option label="年假" :value="1" />
            <el-option label="病假" :value="2" />
            <el-option label="事假" :value="3" />
            <el-option label="调休" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="审批状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="filter-select">
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="日期范围">
          <el-date-picker
            :model-value="[searchForm.startDate, searchForm.endDate]"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="range-picker"
            style="width: 200px"
            @update:model-value="handleDateRangeChange"
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
        <div class="toolbar-title">请假列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="leaveList"
        row-key="leaveID"
        stripe
        empty-text="暂无请假数据"
        class="leave-table"
        height="100%"
      >
        <el-table-column prop="leaveID" label="请假ID" width="120" />
        <el-table-column prop="employeeNumber" label="员工工号" width="160" />
        <el-table-column prop="employeeName" label="员工姓名" min-width="120" />
        <el-table-column label="请假类型" width="120">
          <template #default="{ row }">
            {{ formatLeaveType(row.leaveType) }}
          </template>
        </el-table-column>
        <el-table-column label="审批状态" width="120">
          <template #default="{ row }">
            <el-tag
              :type="Number(row.status) === 1 ? 'success' : Number(row.status) === 2 ? 'danger' : 'warning'"
            >
              {{ formatStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请假区间" min-width="220">
          <template #default="{ row }">
            {{ formatDateRange(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="请假原因" min-width="240" show-overflow-tooltip />
        <el-table-column fixed="right" label="操作" width="170">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteLeaveRecord(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ leaveList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <LeaveAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      @submit="handleAddLeaveRecord"
    />

    <LeaveEditDialog
      v-model="editDialogVisible"
      :leave-record="currentLeaveRecord"
      :loading="submitLoading"
      @submit="handleUpdateLeaveRecord"
    />
  </div>
</template>

<style scoped>
.leave-page {
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

.page-overview {
  display: flex;
  align-items: baseline;
  gap: 6px;
  padding: 16px 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
  color: #fff;
  box-shadow: 0 10px 30px rgba(230, 162, 60, 0.18);
}

.overview-label {
  font-size: 14px;
}

.overview-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.page-card {
  border: none;
  border-radius: 14px;
  overflow: hidden;
}

.search-form {
  display: flex;
  flex-wrap: nowrap;
  gap: 6px 12px;
  align-items: center;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  flex: 0 0 auto;
}

.search-form :deep(.el-input),
.filter-select{
  width: 145px;
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

.leave-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.leave-table {
  flex: 1;
  min-height: 0;
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
  .leave-page {
    padding: 12px;
  }

  .table-card {
    flex: none;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-overview {
    width: 100%;
    justify-content: center;
  }

  .search-actions {
    margin-left: 0;
  }

  .search-form :deep(.el-input),
  .filter-select,
  .range-picker {
    width: 100%;
  }

  .pagination-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
