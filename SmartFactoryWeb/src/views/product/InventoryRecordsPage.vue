<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  storeInService,
  storeOutService,
  getStoreListService
} from '@/api/ProductApi'
import StoreInDialog from '@/component/product/StoreInDialog.vue'
import StoreOutDialog from '@/component/product/StoreOutDialog.vue'

const searchForm = ref({
  barCode: '',
  action: '',
  groupId: '',
  employeeNumber: '',
  deviceSn: '',
  pageNum: 1,
  pageSize: 10
})

const storeList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const inDialogVisible = ref(false)
const outDialogVisible = ref(false)

const totalPages = computed(() => {
  if (!total.value) return 0
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const getStoreList = async () => {
  tableLoading.value = true
  try {
    const { barCode, action, groupId, employeeNumber, deviceSn, pageNum, pageSize } = searchForm.value
    const res = await getStoreListService(
      pageNum, pageSize,
      barCode || undefined,
      action === '' ? undefined : action,
      groupId || undefined,
      employeeNumber?.trim() || undefined,
      deviceSn?.trim() || undefined
    )
    storeList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    storeList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取出入库记录失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getStoreList()
}

const handleReset = () => {
  searchForm.value = {
    barCode: '', action: '', groupId: '',
    employeeNumber: '', deviceSn: '',
    pageNum: 1, pageSize: 10
  }
  void getStoreList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getStoreList()
}

const handleStoreIn = async (formData) => {
  submitLoading.value = true
  try {
    const res = await storeInService(formData)
    ElMessage.success(res.message || '入库成功')
    inDialogVisible.value = false
    await getStoreList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('入库失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleStoreOut = async (formData) => {
  submitLoading.value = true
  try {
    const res = await storeOutService(formData)
    ElMessage.success(res.message || '出库成功')
    outDialogVisible.value = false
    await getStoreList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('出库失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const formatAction = (action) => (Number(action) === 1 ? '入库' : '出库')

onMounted(() => {
  getStoreList()
})
</script>

<template>
  <div class="store-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">出入库管理</h2>
      </div>
      <div class="header-right">
        <el-button type="success" @click="inDialogVisible = true">产品入库</el-button>
        <el-button type="warning" @click="outDialogVisible = true">产品出库</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="100px" class="search-form" @submit.prevent>
        <el-form-item label="条形码">
          <el-input v-model="searchForm.barCode" placeholder="请输入条形码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.action" placeholder="全部操作" clearable style="width:140px">
            <el-option label="入库" :value="1" />
            <el-option label="出库" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="分组ID">
          <el-input v-model="searchForm.groupId" placeholder="请输入分组ID" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="员工工号">
          <el-input v-model="searchForm.employeeNumber" placeholder="请输入员工工号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="设备SN">
          <el-input v-model="searchForm.deviceSn" placeholder="请输入设备SN" clearable @keyup.enter="handleSearch" />
        </el-form-item>
      </el-form>
      <div class="search-actions-wrapper">
        <el-button type="primary" :loading="tableLoading" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="page-card table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">出入库记录</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="storeList"
        row-key="id"
        stripe
        empty-text="暂无出入库记录"
        class="store-table"
        height="100%"
      >
        <el-table-column fixed prop="id" label="记录ID" width="100" />
        <el-table-column prop="barCode" label="条形码" min-width="180" />
        <el-table-column prop="productCode" label="产品代号" min-width="140">
          <template #default="{ row }">{{ row.productCode || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="Number(row.action) === 1 ? 'success' : 'danger'" size="small">
              {{ formatAction(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceSn" label="设备SN" min-width="160">
          <template #default="{ row }">{{ row.deviceSn || '-' }}</template>
        </el-table-column>
        <el-table-column prop="groupName" label="分组名称" min-width="130">
          <template #default="{ row }">{{ row.groupName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="employeeName" label="员工名称" min-width="120">
          <template #default="{ row }">{{ row.employeeName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="datetime" label="操作时间" min-width="170">
          <template #default="{ row }">{{ row.datetime || '-' }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ storeList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <StoreInDialog
      v-model="inDialogVisible"
      :loading="submitLoading"
      @submit="handleStoreIn"
    />

    <StoreOutDialog
      v-model="outDialogVisible"
      :loading="submitLoading"
      @submit="handleStoreOut"
    />
  </div>
</template>

<style scoped>
.store-page {
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

.header-right {
  display: flex;
  gap: 8px;
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
  width: 160px;
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

.store-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.store-table {
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
  .store-page {
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
