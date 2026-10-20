<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addProductTypeService,
  deleteProductTypeService,
  getProductTypeListService,
  updateProductTypeService
} from '@/api/ProductApi'
import ProductTypeAddDialog from '@/component/product/ProductTypeAddDialog.vue'
import ProductTypeEditDialog from '@/component/product/ProductTypeEditDialog.vue'

const searchForm = ref({
  name: '',
  code: '',
  pageNum: 1,
  pageSize: 10
})

const typeList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentType = ref({})

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const getTypeList = async () => {
  tableLoading.value = true
  try {
    const { name, code, pageNum, pageSize } = searchForm.value
    const res = await getProductTypeListService(
      pageNum,
      pageSize,
      name?.trim() || undefined,
      code?.trim() || undefined
    )
    typeList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    typeList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取产品类型列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getTypeList()
}

const handleReset = () => {
  searchForm.value = {
    name: '',
    code: '',
    pageNum: 1,
    pageSize: 10
  }
  void getTypeList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getTypeList()
}

const handleAddType = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addProductTypeService(formData)
    ElMessage.success(res.message || '添加产品类型成功')
    addDialogVisible.value = false
    await getTypeList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('添加产品类型失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentType.value = { ...row }
  editDialogVisible.value = true
}

const handleUpdateType = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateProductTypeService(formData)
    ElMessage.success(res.message || '修改产品类型成功')
    editDialogVisible.value = false
    await getTypeList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('修改产品类型失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteType = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除产品类型"${row.name}"吗？`,
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
    const res = await deleteProductTypeService(row.id)
    ElMessage.success(res.message || '删除产品类型成功')
    if (typeList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getTypeList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除产品类型失败')
    }
  }
}

onMounted(() => {
  getTypeList()
})
</script>

<template>
  <div class="product-type-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">产品类型管理</h2>
      </div>

      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">添加产品类型</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="100px" class="search-form" @submit.prevent>
        <el-form-item label="类型名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入类型名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="类型码">
          <el-input
            v-model="searchForm.code"
            placeholder="请输入类型码"
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
        <div class="toolbar-title">产品类型列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="typeList"
        row-key="id"
        stripe
        empty-text="暂无产品类型数据"
        class="product-type-table"
        height="100%"
      >
        <el-table-column fixed prop="id" label="类型ID" width="100" />
        <el-table-column prop="name" label="类型名称" min-width="180" />
        <el-table-column prop="code" label="类型码" min-width="150" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteType(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ typeList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <ProductTypeAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      @submit="handleAddType"
    />

    <ProductTypeEditDialog
      v-model="editDialogVisible"
      :type-data="currentType"
      :loading="submitLoading"
      @submit="handleUpdateType"
    />
  </div>
</template>

<style scoped>
.product-type-page {
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

.product-type-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.product-type-table {
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
  .product-type-page {
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
