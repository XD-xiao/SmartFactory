<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addSensorConfigService,
  deleteSensorConfigService,
  getSensorConfigListService,
  updateSensorConfigService
} from '@/api/DeviceApi.js'
import SensorConfigAddDialog from '@/component/device/SensorConfigAddDialog.vue'
import SensorConfigEditDialog from '@/component/device/SensorConfigEditDialog.vue'
import SensorRecordDialog from '@/component/device/SensorRecordDialog.vue'

const searchForm = ref({
  sn: '',
  sceneType: '',
  sensor: '',
  pageNum: 1,
  pageSize: 10
})

const configList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentConfig = ref({})
const recordDialogVisible = ref(false)
const currentRecordConfigId = ref(null)
const currentRecordSensorName = ref('')

const totalPages = computed(() => {
  if (!total.value) return 0
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const getConfigList = async () => {
  tableLoading.value = true
  try {
    const { sn, sceneType, sensor, pageNum, pageSize } = searchForm.value
    const res = await getSensorConfigListService(
      pageNum,
      pageSize,
      sn || undefined,
      sceneType ? Number(sceneType) : undefined,
      sensor ? Number(sensor) : undefined
    )
    configList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    configList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取传感器配置列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getConfigList()
}

const handleReset = () => {
  searchForm.value = {
    sn: '',
    sceneType: '',
    sensor: '',
    pageNum: 1,
    pageSize: 10
  }
  void getConfigList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getConfigList()
}

const handleAddConfig = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addSensorConfigService(formData)
    ElMessage.success(res.message || '新增传感器配置成功')
    addDialogVisible.value = false
    await getConfigList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('新增传感器配置失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentConfig.value = { ...row }
  editDialogVisible.value = true
}

const handleOpenRecordDialog = (row) => {
  currentRecordConfigId.value = row.id
  currentRecordSensorName.value = row.sensorName || row.sensor || ''
  recordDialogVisible.value = true
}

const handleUpdateConfig = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateSensorConfigService(formData)
    ElMessage.success(res.message || '更新传感器配置成功')
    editDialogVisible.value = false
    await getConfigList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('更新传感器配置失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteConfig = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除传感器「${row.sensorName || row.sensor}」(SN: ${row.sn}) 的配置吗？`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  try {
    const res = await deleteSensorConfigService(row.id)
    ElMessage.success(res.message || '删除传感器配置成功')
    if (configList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getConfigList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除传感器配置失败')
    }
  }
}

const valueTypeLabel = (val) => {
  if (val === true || val === 1) return '开关'
  if (val === false || val === 0) return '范围'
  return val ?? '-'
}

onMounted(() => {
  void getConfigList()
})
</script>

<template>
  <div class="sensor-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">传感器管理</h2>
        <p class="page-subtitle">管理试验箱各传感器的配置参数，包括阈值设定与场景绑定。</p>
      </div>
      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">新增配置</el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
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
        <el-form-item label="场景编号">
          <el-input
            v-model="searchForm.sceneType"
            placeholder="请输入场景编号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="传感器编号">
          <el-input
            v-model="searchForm.sensor"
            placeholder="请输入传感器编号"
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

    <!-- 表格区域 -->
    <el-card shadow="never" class="page-card table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">配置列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="configList"
        row-key="id"
        stripe
        empty-text="暂无传感器配置数据"
        class="config-table"
        height="100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="sn" label="设备SN" width="130" show-overflow-tooltip />
        <el-table-column prop="sensor" label="传感器编号" width="100" />
        <el-table-column prop="sensorName" label="传感器名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="sceneType" label="场景编号" width="90" />
        <el-table-column label="取值类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.valueType ? 'warning' : 'success'" size="small">
              {{ valueTypeLabel(row.valueType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="max" label="上限" width="80" />
        <el-table-column prop="min" label="下限" width="80" />
        <el-table-column prop="maxTh" label="最大阈值" width="90" />
        <el-table-column prop="midTh" label="中间阈值" width="90" />
        <el-table-column prop="minTh" label="最小阈值" width="90" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" min-width="160" show-overflow-tooltip />
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="success" size="small" @click="handleOpenRecordDialog(row)">
              记录
            </el-button>
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteConfig(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ configList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <SensorConfigAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      @submit="handleAddConfig"
    />

    <SensorConfigEditDialog
      v-model="editDialogVisible"
      :config="currentConfig"
      :loading="submitLoading"
      @submit="handleUpdateConfig"
    />

    <SensorRecordDialog
      v-model="recordDialogVisible"
      :sensor-config-id="currentRecordConfigId"
      :sensor-name="currentRecordSensorName"
    />
  </div>
</template>

<style scoped>
.sensor-page {
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
  flex-wrap: nowrap;
  gap: 6px 12px;
  align-items: center;
}

.search-form :deep(.el-form-item__label) {
  white-space: nowrap;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  flex: 0 0 auto;
}

.search-form :deep(.el-input) {
  width: 180px;
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

.config-table {
  flex: 1;
  min-height: 0;
}

.config-table :deep(.el-table__header th) {
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
  .sensor-page {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-card {
    flex: none;
  }

  .search-form {
    flex-wrap: wrap;
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
