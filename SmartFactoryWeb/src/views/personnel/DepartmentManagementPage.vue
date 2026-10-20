<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addDepartmentService,
  deleteDepartmentService,
  getDepartmentListService,
  updateDepartmentService
} from '@/api/PersonManagementApi'
import DepartmentAddDialog from '@/component/personnel/DepartmentAddDialog.vue'
import DepartmentEditDialog from '@/component/personnel/DepartmentEditDialog.vue'

const searchForm = ref({
  departmentId: '',
  departmentName: '',
  status: '',
  pageNum: 1,
  pageSize: 10,
  sort: undefined
})

const departmentList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentDepartment = ref({})

const normalizeDepartment = (department) => ({
  ...department,
  status: department?.status ?? department?.state ?? 1
})

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const getDepartmentList = async () => {
  tableLoading.value = true
  try {
    const { departmentId, departmentName, status, pageNum, pageSize, sort } = searchForm.value
    const res = await getDepartmentListService(
      departmentId?.trim() || undefined,
      departmentName?.trim() || undefined,
      status === '' ? undefined : status,
      pageNum,
      pageSize,
      sort
    )
    departmentList.value = (res.data?.list ?? []).map(normalizeDepartment)
    total.value = res.data?.total ?? 0
  } catch (error) {
    departmentList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取部门列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getDepartmentList()
}

const handleReset = () => {
  searchForm.value = {
    departmentId: '',
    departmentName: '',
    status: '',
    pageNum: 1,
    pageSize: 10,
    sort: undefined
  }
  void getDepartmentList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getDepartmentList()
}

const handleAddDepartment = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addDepartmentService(formData)
    ElMessage.success(res.message || '添加部门成功')
    addDialogVisible.value = false
    await getDepartmentList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('添加部门失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentDepartment.value = normalizeDepartment(row)
  editDialogVisible.value = true
}

const handleUpdateDepartment = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateDepartmentService(formData)
    ElMessage.success(res.message || '修改部门成功')
    editDialogVisible.value = false
    await getDepartmentList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('修改部门失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteDepartment = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除部门“${row.departmentName}”吗？`,
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
    const res = await deleteDepartmentService(row.departmentId)
    ElMessage.success(res.message || '删除部门成功')
    if (departmentList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getDepartmentList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除部门失败')
    }
  }
}

const formatStatusText = (status) => (Number(status) === 1 ? '启用' : '停用')

onMounted(() => {
  getDepartmentList()
})
</script>

<template>
  <div class="department-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">部门管理</h2>
      </div>

      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">添加部门</el-button>
      </div>

    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent>
        <el-form-item label="部门ID">
          <el-input
            v-model="searchForm.departmentId"
            placeholder="请输入部门ID"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="部门名称">
          <el-input
            v-model="searchForm.departmentName"
            placeholder="请输入部门名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="status-select">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="排序">
          <el-select v-model="searchForm.sort" placeholder="默认排序" clearable class="status-select">
            <el-option label="升序" :value="1" />
            <el-option label="降序" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="search-actions-wrapper">
        <el-button type="primary" :loading="tableLoading" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="page-card table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">部门列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="departmentList"
        row-key="departmentId"
        stripe
        empty-text="暂无部门数据"
        class="department-table"
        height="100%"
      >
        <el-table-column fixed prop="departmentId" label="部门ID" width="120" />
        <el-table-column prop="departmentName" label="部门名称" min-width="180" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'">
              {{ formatStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" />
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteDepartment(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ departmentList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <DepartmentAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      @submit="handleAddDepartment"
    />

    <DepartmentEditDialog
      v-model="editDialogVisible"
      :department="currentDepartment"
      :loading="submitLoading"
      @submit="handleUpdateDepartment"
    />
  </div>
</template>

<style scoped>
.department-page {
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
  flex-wrap: wrap;
  gap: 6px 12px;
  align-items: center;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}


.search-form :deep(.el-input),
.status-select {
  width: 150px;
}

.search-actions-wrapper {
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

.department-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.department-table {
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
  .department-page {
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
  .status-select {
    width: 100%;
  }

  .pagination-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
