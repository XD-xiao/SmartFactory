<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addEmployeeService,
  deleteEmployeeService,
  getDepartmentListService,
  getEmployeeInfoService,
  getEmployeeListService,
  getPositionListService,
  updateEmployeeService
} from '@/api/PersonManagementApi'
import StaffAddDialog from '@/component/personnel/StaffAddDialog.vue'
import StaffEditDialog from '@/component/personnel/StaffEditDialog.vue'

const searchForm = ref({
  keyword: '',
  departmentId: '',
  positionId: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const staffList = ref([])
const departmentOptions = ref([])
const positionOptions = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentEmployee = ref({})

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const employeeStatusMap = {
  0: '下班中',
  1: '上班中'
}

const getBaseOptions = async () => {
  try {
    const [departmentRes, positionRes] = await Promise.all([
      getDepartmentListService(undefined, undefined, undefined, 1, 999, undefined),
      getPositionListService(undefined, undefined, 1, 999, undefined)
    ])
    departmentOptions.value = departmentRes.data?.list ?? []
    positionOptions.value = positionRes.data?.list ?? []
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('加载部门或职位选项失败')
    }
  }
}

const getStaffList = async () => {
  tableLoading.value = true
  try {
    const { pageNum, pageSize, keyword, departmentId, positionId, status } = searchForm.value
    const res = await getEmployeeListService(
      pageNum,
      pageSize,
      keyword?.trim() || undefined,
      departmentId === '' ? undefined : departmentId,
      positionId === '' ? undefined : positionId,
      status === '' ? undefined : status
    )
    staffList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    staffList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取员工列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getStaffList()
}

const handleReset = () => {
  searchForm.value = {
    keyword: '',
    departmentId: '',
    positionId: '',
    status: '',
    pageNum: 1,
    pageSize: 10
  }
  void getStaffList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getStaffList()
}

const handleAddEmployee = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addEmployeeService(formData)
    ElMessage.success(res.message || '新增员工成功')
    addDialogVisible.value = false
    await getStaffList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('新增员工失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = async (row) => {
  try {
    const res = await getEmployeeInfoService(row.employeeNumber)
    currentEmployee.value = {
      ...row,
      ...(res.data ?? {})
    }
    editDialogVisible.value = true
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取员工详情失败')
    }
  }
}

const handleUpdateEmployee = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateEmployeeService(formData)
    ElMessage.success(res.message || '更新员工成功')
    editDialogVisible.value = false
    await getStaffList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('更新员工失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteEmployee = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除员工“${row.name}”吗？`,
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
    const res = await deleteEmployeeService(row.employeeId)
    ElMessage.success(res.message || '删除员工成功')
    if (staffList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getStaffList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除员工失败')
    }
  }
}

const formatStatusText = (status) => employeeStatusMap[Number(status)] || '未知状态'

onMounted(async () => {
  await getBaseOptions()
  await getStaffList()
})
</script>

<template>
  <div class="staff-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">人员管理</h2>
      </div>

      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">新增员工</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入姓名或工号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="所属部门">
          <el-select v-model="searchForm.departmentId" placeholder="全部部门" clearable class="filter-select">
            <el-option
              v-for="item in departmentOptions"
              :key="item.departmentId"
              :label="item.departmentName"
              :value="item.departmentId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="所属职位">
          <el-select v-model="searchForm.positionId" placeholder="全部职位" clearable class="filter-select">
            <el-option
              v-for="item in positionOptions"
              :key="item.positionId"
              :label="item.positionName"
              :value="item.positionId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="当前状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="filter-select">
            <el-option label="上班中" :value="1" />
            <el-option label="下班中" :value="0" />
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
        <div class="toolbar-title">员工列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="staffList"
        row-key="employeeId"
        stripe
        empty-text="暂无员工数据"
        class="staff-table"
        height="100%"
      >
        <el-table-column prop="employeeId" label="员工ID" width="110" />
        <el-table-column prop="employeeNumber" label="工号" width="160" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="90" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="departmentName" label="所属部门" width="130" />
        <el-table-column prop="positionName" label="所属职位" width="130" />
        <el-table-column prop="hireDate" label="入职日期" width="130" />
        <el-table-column label="当前状态" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'">
              {{ formatStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="170">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteEmployee(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ staffList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <StaffAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      :departments="departmentOptions"
      :positions="positionOptions"
      @submit="handleAddEmployee"
    />

    <StaffEditDialog
      v-model="editDialogVisible"
      :employee="currentEmployee"
      :loading="submitLoading"
      :departments="departmentOptions"
      :positions="positionOptions"
      @submit="handleUpdateEmployee"
    />
  </div>
</template>

<style scoped>
.staff-page {
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
  background: linear-gradient(135deg, #9c6cff 0%, #c6a3ff 100%);
  color: #fff;
  box-shadow: 0 10px 30px rgba(156, 108, 255, 0.18);
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

.staff-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.staff-table {
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
  .staff-page {
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
