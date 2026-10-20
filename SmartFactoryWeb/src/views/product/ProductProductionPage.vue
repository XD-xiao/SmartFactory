<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  setProductionService,
  updateProductionService,
  getProductionListService,
  qualityCheckService
} from '@/api/ProductApi'
import ProductProductionAddDialog from '@/component/product/ProductProductionAddDialog.vue'
import ProductProductionEditDialog from '@/component/product/ProductProductionEditDialog.vue'
import ProductProductionQualityCheckDialog from '@/component/product/ProductProductionQualityCheckDialog.vue'

const searchForm = ref({
  barCode: '',
  planName:'',
  pageNum: 1,
  pageSize: 10
})

const productionList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const qualityCheckDialogVisible = ref(false)
const currentProduction = ref({})

const totalPages = computed(() => {
  if (!total.value) return 0
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const getProductionList = async () => {
  tableLoading.value = true
  try {
    const { barCode, planName, pageNum, pageSize } = searchForm.value
    const res = await getProductionListService(
      pageNum, pageSize,
      barCode || undefined,
      planName || undefined
    )
    productionList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    productionList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取产量列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getProductionList()
}

const handleReset = () => {
  searchForm.value = { barCode: '', planName: '', pageNum: 1, pageSize: 10 }
  void getProductionList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getProductionList()
}

const handleSetProduction = async (formData) => {
  submitLoading.value = true
  try {
    const res = await setProductionService(formData)
    ElMessage.success(res.message || '设置产量成功')
    addDialogVisible.value = false
    await getProductionList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('设置产量失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentProduction.value = { ...row }
  editDialogVisible.value = true
}

const handleUpdateProduction = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateProductionService(formData)
    ElMessage.success(res.message || '更新产量成功')
    editDialogVisible.value = false
    await getProductionList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('更新产量失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleQualityCheck = async (formData) => {
  submitLoading.value = true
  try {
    const res = await qualityCheckService(formData)
    ElMessage.success(res.message || '质检通过')
    qualityCheckDialogVisible.value = false
    await getProductionList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('质检失败')
    }
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getProductionList()
})
</script>

<template>
  <div class="production-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">产量管理</h2>
      </div>
      <div class="header-right">
        <el-button type="warning" @click="qualityCheckDialogVisible = true">质检</el-button>
        <el-button type="success" @click="addDialogVisible = true">设置产量</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="100px" class="search-form" @submit.prevent>
        <el-form-item label="条形码">
          <el-input v-model="searchForm.barCode" placeholder="请输入条形码" clearable @keyup.enter="handleSearch" />
        </el-form-item>

        <el-form-item label="计划名称">
          <el-input v-model="searchForm.planName" placeholder="请输入计划名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>

      </el-form>
      <div class="search-actions-wrapper">
        <el-button type="primary" :loading="tableLoading" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="page-card table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">产量列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="productionList"
        row-key="id"
        stripe
        empty-text="暂无产量数据"
        class="production-table"
        height="100%"
      >
        <el-table-column fixed prop="id" label="记录ID" width="100" />
        <el-table-column prop="barCode" label="条形码" min-width="180" />
        <el-table-column prop="productCode" label="产品代号" min-width="140">
          <template #default="{ row }">{{ row.productCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="planName" label="计划名称" min-width="120" />
        <el-table-column prop="plannedProduction" label="计划产量" min-width="120" />
        <el-table-column prop="actualProduction" label="实际产量" min-width="120" />
        <el-table-column prop="storeNum" label="入库数量" min-width="120" />
        <el-table-column prop="qualifiedNum" label="合格数量" min-width="120" />
        <el-table-column prop="groupName" label="分组名称" min-width="120">
          <template #default="{ row }">{{ row.groupName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column fixed="right" label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ productionList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <ProductProductionAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      @submit="handleSetProduction"
    />

    <ProductProductionEditDialog
      v-model="editDialogVisible"
      :production-data="currentProduction"
      :loading="submitLoading"
      @submit="handleUpdateProduction"
    />

    <ProductProductionQualityCheckDialog
      v-model="qualityCheckDialogVisible"
      :loading="submitLoading"
      @submit="handleQualityCheck"
    />
  </div>
</template>

<style scoped>
.production-page {
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

.production-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.production-table {
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
  .production-page {
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
