<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addProductModelService,
  deleteProductModelService,
  getProductModelListService,
  updateProductModelService
} from '@/api/ProductApi'
import ProductModelAddDialog from '@/component/product/ProductModelAddDialog.vue'
import ProductModelEditDialog from '@/component/product/ProductModelEditDialog.vue'

const searchForm = ref({
  name: '',
  code: '',
  pageNum: 1,
  pageSize: 10
})

const modelList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentModel = ref({})

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const getModelList = async () => {
  tableLoading.value = true
  try {
    const { name, code, pageNum, pageSize } = searchForm.value
    const res = await getProductModelListService(
      pageNum,
      pageSize,
      name?.trim() || undefined,
      code?.trim() || undefined
    )
    modelList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    modelList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取产品型号列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getModelList()
}

const handleReset = () => {
  searchForm.value = {
    name: '',
    code: '',
    pageNum: 1,
    pageSize: 10
  }
  void getModelList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getModelList()
}

const handleAddModel = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addProductModelService(formData)
    ElMessage.success(res.message || '添加产品型号成功')
    addDialogVisible.value = false
    await getModelList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('添加产品型号失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentModel.value = { ...row }
  editDialogVisible.value = true
}

const handleUpdateModel = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateProductModelService(formData)
    ElMessage.success(res.message || '修改产品型号成功')
    editDialogVisible.value = false
    await getModelList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('修改产品型号失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteModel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除产品型号"${row.name}"吗？`,
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
    const res = await deleteProductModelService(row.id)
    ElMessage.success(res.message || '删除产品型号成功')
    if (modelList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getModelList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除产品型号失败')
    }
  }
}

onMounted(() => {
  getModelList()
})
</script>

<template>
  <div class="product-model-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">产品型号管理</h2>
      </div>

      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">添加产品型号</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="100px" class="search-form" @submit.prevent>
        <el-form-item label="型号名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入型号名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="型号码">
          <el-input
            v-model="searchForm.code"
            placeholder="请输入型号码"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
      </el-form>

      <div class="search-actions-wrapper">
        <el-button type="primary" :loading="tableLoading" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="page-card table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">产品型号列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="modelList"
        row-key="id"
        stripe
        empty-text="暂无产品型号数据"
        class="product-model-table"
        height="100%"
      >
        <el-table-column fixed prop="id" label="型号ID" width="100" />
        <el-table-column prop="name" label="型号名称" min-width="180" />
        <el-table-column prop="code" label="型号码" min-width="150" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteModel(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ modelList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <ProductModelAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      @submit="handleAddModel"
    />

    <ProductModelEditDialog
      v-model="editDialogVisible"
      :model-data="currentModel"
      :loading="submitLoading"
      @submit="handleUpdateModel"
    />
  </div>
</template>

<style scoped>
.product-model-page {
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

.search-form :deep(.el-input) {
  width: 180px;
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

.product-model-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.product-model-table {
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
  .product-model-page {
    padding: 12px;
  }

  .table-card {
    flex: none;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-form :deep(.el-input) {
    width: 100%;
  }

  .pagination-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
