<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getPlcListService } from '@/api/DeviceApi.js'

const searchForm = ref({
  sn: '',
  seqNo: '',
  pageNum: 1,
  pageSize: 10
})

const recordList = ref([])
const total = ref(0)
const tableLoading = ref(false)

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const getRecordList = async () => {
  tableLoading.value = true
  try {
    const { sn, seqNo, pageNum, pageSize } = searchForm.value
    const res = await getPlcListService(
        sn?.trim() || undefined,
        seqNo === '' ? undefined : Number(seqNo),
        pageNum,
        pageSize
    )
    recordList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    recordList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取产线运行记录失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getRecordList()
}

const handleReset = () => {
  searchForm.value = {
    sn: '',
    seqNo: '',
    pageNum: 1,
    pageSize: 10
  }
  void getRecordList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getRecordList()
}

const formatActionText = (action) => {
  if (Number(action) === 1) {
    return '开机'
  }
  if (Number(action) === 0) {
    return '关机'
  }
  return `动作${action}`
}

const formatWorkTime = (workTime) => {
  if (workTime == null) {
    return '-'
  }
  return `${Number(workTime).toFixed(2)} 小时`
}

onMounted(() => {
  void getRecordList()
})
</script>

<template>
  <div class="plc-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">产线运行记录管理</h2>
        <p class="page-subtitle">查看设备开关机运行记录与工作时长统计。</p>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent>
        <el-form-item label="设备SN">
          <el-input
              v-model="searchForm.sn"
              placeholder="请输入设备SN"
              clearable
              @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="序号">
          <el-input
              v-model="searchForm.seqNo"
              placeholder="请输入序号"
              clearable
              @keyup.enter="handleSearch"
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
        <div class="toolbar-title">运行记录列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
          v-loading="tableLoading"
          :data="recordList"
          row-key="id"
          stripe
          empty-text="暂无运行记录"
          class="plc-table"
          height="100%"
      >
        <el-table-column prop="id" label="记录ID" width="100" />
        <el-table-column prop="sn" label="设备SN" min-width="180" show-overflow-tooltip />
        <el-table-column prop="seqNo" label="序号" width="80" />
        <el-table-column label="开关动作" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.action) === 1 ? 'success' : 'info'">
              {{ formatActionText(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workBeginTime" label="工作开始时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="workEndTime" label="工作结束时间" min-width="180" show-overflow-tooltip />
        <el-table-column label="工作时长" width="140">
          <template #default="{ row }">
            {{ formatWorkTime(row.workTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ recordList.length }}</span> 条，共 <span>{{ total }}</span> 条
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
  </div>
</template>

<style scoped>
.plc-page {
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

.search-form :deep(.el-input) {
  width: 200px;
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

.plc-table {
  flex: 1;
  min-height: 0;
}

.plc-table :deep(.el-table__header th) {
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
  .plc-page {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-card {
    flex: none;
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
