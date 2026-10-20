<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addPositionService,
  deletePositionService,
  getPositionListService,
  updatePositionService
} from '@/api/PersonManagementApi'
import PositionAddDialog from '@/component/personnel/PositionAddDialog.vue'
import PositionEditDialog from '@/component/personnel/PositionEditDialog.vue'

const searchForm = ref({
  positionId: '',
  positionName: '',
  pageNum: 1,
  pageSize: 10,
  sort: undefined
})

const positionList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentPosition = ref({})

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const getPositionList = async () => {
  tableLoading.value = true
  try {
    const { positionId, positionName, pageNum, pageSize, sort } = searchForm.value
    const res = await getPositionListService(
      positionId?.trim() || undefined,
      positionName?.trim() || undefined,
      pageNum,
      pageSize,
      sort
    )
    positionList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    positionList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取职位列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getPositionList()
}

const handleReset = () => {
  searchForm.value = {
    positionId: '',
    positionName: '',
    pageNum: 1,
    pageSize: 10,
    sort: undefined
  }
  void getPositionList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getPositionList()
}

const handleAddPosition = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addPositionService(formData)
    ElMessage.success(res.message || '添加职位成功')
    addDialogVisible.value = false
    await getPositionList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('添加职位失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentPosition.value = { ...row }
  editDialogVisible.value = true
}

const handleUpdatePosition = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updatePositionService(formData)
    ElMessage.success(res.message || '修改职位成功')
    editDialogVisible.value = false
    await getPositionList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('修改职位失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeletePosition = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除职位“${row.positionName}”吗？`,
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
    const res = await deletePositionService(row.positionId)
    ElMessage.success(res.message || '删除职位成功')
    if (positionList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getPositionList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除职位失败')
    }
  }
}

onMounted(() => {
  void getPositionList()
})
</script>

<template>
  <div class="position-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">职位管理</h2>
      </div>

      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">添加职位</el-button>
      </div>

    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent>
        <el-form-item label="职位ID">
          <el-input
            v-model="searchForm.positionId"
            placeholder="请输入职位ID"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="职位名称">
          <el-input
            v-model="searchForm.positionName"
            placeholder="请输入职位名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="排序">
          <el-select v-model="searchForm.sort" placeholder="默认排序" clearable class="sort-select">
            <el-option label="升序" :value="1" />
            <el-option label="降序" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item class="search-actions">
          <el-button type="primary" :loading="tableLoading" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page-card table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">职位列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="positionList"
        row-key="positionId"
        stripe
        empty-text="暂无职位数据"
        class="position-table"
        height="100%"
      >
        <el-table-column fixed prop="positionId" label="职位ID" width="120" />
        <el-table-column prop="positionName" label="职位名称" min-width="180" />
        <el-table-column prop="remark" label="职位描述" min-width="260" show-overflow-tooltip />
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeletePosition(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ positionList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <PositionAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      @submit="handleAddPosition"
    />

    <PositionEditDialog
      v-model="editDialogVisible"
      :position="currentPosition"
      :loading="submitLoading"
      @submit="handleUpdatePosition"
    />
  </div>
</template>

<style scoped>
.position-page {
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
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: #fff;
  box-shadow: 0 10px 30px rgba(103, 194, 58, 0.18);
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
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-form :deep(.el-input),
.sort-select {
  width: 220px;
}

.search-actions {
  margin-left: auto;
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

.position-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.position-table {
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
  .position-page {
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
  .sort-select {
    width: 100%;
  }

  .pagination-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
