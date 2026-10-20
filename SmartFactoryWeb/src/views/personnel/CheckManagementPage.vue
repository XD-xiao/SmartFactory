<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addCheckRecordService,
  deleteCheckRecordService,
  getCheckRecordListService,
  updateCheckRecordService
} from '@/api/PersonManagementApi'
import CheckAddDialog from '@/component/personnel/CheckAddDialog.vue'

const searchForm = ref({
  employeeNumber: '',
  date: '',
  checkinTime: '',
  checkoutTime: '',
  status: '',
  workHours: '',
  pageNum: 1,
  pageSize: 10
})

const checkList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentCheckRecord = ref({})

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const checkStatusMap = {
  1: '正常',
  2: '早退/迟到',
}

const getCheckList = async () => {
  tableLoading.value = true
  try {
    const { pageNum, pageSize, employeeNumber, date, checkinTime, checkoutTime, status, workHours } = searchForm.value
    const res = await getCheckRecordListService(
      pageNum,
      pageSize,
      employeeNumber?.trim() || undefined,
      date || undefined,
      checkinTime || undefined,
      checkoutTime || undefined,
      status === '' ? undefined : status,
      workHours === '' ? undefined : workHours
    )
    checkList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    checkList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取考勤记录失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getCheckList()
}

const handleReset = () => {
  searchForm.value = {
    employeeNumber: '',
    date: '',
    checkinTime: '',
    checkoutTime: '',
    status: '',
    workHours: '',
    pageNum: 1,
    pageSize: 10
  }
  void getCheckList()
}

const handleSizeChange = (pageSize) => {
  searchForm.value.pageSize = pageSize
  searchForm.value.pageNum = 1
  void getCheckList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getCheckList()
}

const handleAddCheckRecord = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addCheckRecordService(formData)
    ElMessage.success(res.message || '新增考勤记录成功')
    addDialogVisible.value = false
    await getCheckList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('新增考勤记录失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// const handleOpenEditDialog = (row) => {
//   currentCheckRecord.value = { ...row }
//   editDialogVisible.value = true
// }
const currentUpdateRecord = ref({
  attendanceID: '',
})

const handleUpdateCheckRecord = async (row) => {
  submitLoading.value = true
  currentUpdateRecord.value.attendanceID = row.attendanceID
  try {
    const res = await updateCheckRecordService(currentUpdateRecord.value)
    ElMessage.success(res.message || '更新考勤记录成功')
    editDialogVisible.value = false
    await getCheckList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('更新考勤记录失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteCheckRecord = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除员工 ${row.employeeName || row.employeeNumber} 的考勤记录吗？`,
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
    const res = await deleteCheckRecordService(row.attendanceID)
    ElMessage.success(res.message || '删除考勤记录成功')
    if (checkList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getCheckList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除考勤记录失败')
    }
  }
}

const formatStatusText = (status) => checkStatusMap[Number(status)] || '未知状态'

onMounted(() => {
  void getCheckList()
})
</script>

<template>
  <div class="check-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">考勤管理</h2>
      </div>

      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">新增考勤</el-button>
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

        <el-form-item label="考勤日期">
          <el-date-picker
            v-model="searchForm.date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择日期"
            class="filter-select"
          />
        </el-form-item>

        <el-form-item label="考勤状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="filter-select">
            <el-option label="正常" :value="1" />
            <el-option label="迟到/早退" :value="2" />
          </el-select>
        </el-form-item>

<!--        <el-form-item label="工作时长">-->
<!--          <el-input-->
<!--            v-model="searchForm.workHours"-->
<!--            placeholder="请输入工时"-->
<!--            clearable-->
<!--            @keyup.enter="handleSearch"-->
<!--          />-->
<!--        </el-form-item>-->
      </el-form>
      <div class="search-actions">
        <el-button type="primary" :loading="tableLoading" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="page-card table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">考勤列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="checkList"
        row-key="attendanceID"
        stripe
        empty-text="暂无考勤数据"
        class="check-table"
        height="100%"
      >
        <el-table-column prop="attendanceID" label="考勤ID" width="120" />
        <el-table-column prop="employeeNumber" label="员工工号" width="160" />
        <el-table-column prop="employeeName" label="员工姓名" min-width="120" />
        <el-table-column prop="date" label="考勤日期" min-width="140" />
        <el-table-column prop="checkinTime" label="上班时间" min-width="180" />
        <el-table-column prop="checkoutTime" label="下班时间" min-width="180" />
        <el-table-column label="考勤状态" width="120">
          <template #default="{ row }">
            <el-tag
              :type="Number(row.status) === 0 ? 'success' : Number(row.status) === 1 ? 'success' : 'warning'"
            >
              {{ formatStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workHours" label="工时" width="120" />
        <el-table-column fixed="right" label="操作" width="170">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleUpdateCheckRecord(row)">
              下班
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteCheckRecord(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ checkList.length }}</span> 条，共 <span>{{ total }}</span> 条
        </div>

        <el-pagination
          background
          :current-page="searchForm.pageNum"
          :page-size="searchForm.pageSize"
          layout="prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <CheckAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      @submit="handleAddCheckRecord"
    />
  </div>
</template>

<style scoped>
.check-page {
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
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  color: #fff;
  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.18);
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
.filter-select {
  width: 150px;
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

.check-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.check-table {
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
  .check-page {
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
  .filter-select {
    width: 100%;
  }

  .pagination-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
