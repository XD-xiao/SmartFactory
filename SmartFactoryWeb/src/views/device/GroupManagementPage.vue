<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addGroupService,
  deleteGroupService,
  getGroupListService,
  updateGroupService
} from '@/api/DeviceApi.js'
import GroupAddDialog from '@/component/device/GroupAddDialog.vue'
import GroupEditDialog from '@/component/device/GroupEditDialog.vue'

const searchForm = ref({
  id: '',
  name: '',
  pageNum: 1,
  pageSize: 10,
  sort: undefined
})

const groupList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentGroup = ref({})

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const getGroupList = async () => {
  tableLoading.value = true
  try {
    const { id, name, pageNum, pageSize, sort } = searchForm.value
    const res = await getGroupListService(
      id === '' ? undefined : Number(id),
      name?.trim() || undefined,
      pageNum,
      pageSize,
      sort
    )
    groupList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    groupList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取展区列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getGroupList()
}

const handleReset = () => {
  searchForm.value = {
    id: '',
    name: '',
    pageNum: 1,
    pageSize: 10,
    sort: undefined
  }
  void getGroupList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getGroupList()
}

const handleAddGroup = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addGroupService(formData)
    ElMessage.success(res.message || '新增展区成功')
    addDialogVisible.value = false
    await getGroupList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('新增展区失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentGroup.value = { ...row }
  editDialogVisible.value = true
}

const handleUpdateGroup = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateGroupService(formData.id, { name: formData.name })
    ElMessage.success(res.message || '修改展区成功')
    editDialogVisible.value = false
    await getGroupList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('修改展区失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteGroup = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除展区“${row.name}”吗？`,
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
    const res = await deleteGroupService(row.id)
    ElMessage.success(res.message || '删除展区成功')
    if (groupList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getGroupList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除展区失败')
    }
  }
}

onMounted(() => {
  getGroupList()
})
</script>

<template>
  <div class="group-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">展区管理</h2>
        <p class="page-subtitle">维护展区基础信息，并为设备分组提供可选展区。</p>
      </div>

      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">新增展区</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent>
        <el-form-item label="展区ID">
          <el-input
            v-model="searchForm.id"
            placeholder="请输入展区ID"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="展区名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入展区名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="排序">
          <el-select v-model="searchForm.sort" placeholder="默认排序" clearable class="filter-select">
            <el-option label="升序" :value="1" />
            <el-option label="降序" :value="0" />
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
        <div class="toolbar-title">展区列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="groupList"
        row-key="id"
        stripe
        empty-text="暂无展区数据"
        class="group-table"
        height="100%"
      >
        <el-table-column prop="id" label="展区ID" width="120" />
        <el-table-column prop="name" label="展区名称" min-width="220" />
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteGroup(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ groupList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <GroupAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      @submit="handleAddGroup"
    />

    <GroupEditDialog
      v-model="editDialogVisible"
      :group="currentGroup"
      :loading="submitLoading"
      @submit="handleUpdateGroup"
    />
  </div>
</template>

<style scoped>
.group-page {
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

.group-table {
  flex: 1;
  min-height: 0;
}

.group-table :deep(.el-table__header th) {
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
  .group-page {
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
