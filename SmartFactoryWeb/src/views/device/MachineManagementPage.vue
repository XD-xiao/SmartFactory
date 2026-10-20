<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addDeviceService,
  deleteDeviceService,
  getDeviceListService,
  getGroupListService,
  joinGroupService,
  quitGroupService,
  updateDeviceService
} from '@/api/DeviceApi.js'
import MachineAddDialog from '@/component/device/MachineAddDialog.vue'
import MachineEditDialog from '@/component/device/MachineEditDialog.vue'
import MachineGroupDialog from '@/component/device/MachineGroupDialog.vue'

const searchForm = ref({
  id: '',
  sn: '',
  type: '',
  state: '',
  pageNum: 1,
  pageSize: 10,
  sort: undefined
})

const typeOptions = ref([
  { label: 'Pad', value: 'Pad' },
  { label: 'Pda', value: 'Pda' },
  { label: 'Adapter', value: 'Adapter' },
  { label: 'EduBox', value: 'EduBox' }
])

const machineList = ref([])
const groupOptions = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const groupDialogVisible = ref(false)
const currentMachine = ref({})

const totalPages = computed(() => {
  if (!total.value) {
    return 0
  }
  return Math.ceil(total.value / searchForm.value.pageSize)
})

const normalizeDevice = (device) => {
  const groupId = device?.groupId ?? device?.currentGroupId ?? device?.group?.id ?? ''
  const groupName = device?.groupName ?? device?.currentGroupName ?? device?.group?.name ?? ''

  return {
    ...device,
    id: device?.id ?? '',
    sn: device?.sn ?? '',
    type: device?.type ?? '',
    state: Number(device?.state ?? 0),
    groupId,
    groupName
  }
}

const getGroupOptions = async () => {
  try {
    const res = await getGroupListService(undefined, undefined, 1, 999, undefined)
    groupOptions.value = res.data?.list ?? []
  } catch (error) {
    groupOptions.value = []
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取展区选项失败')
    }
  }
}

const getMachineList = async () => {
  tableLoading.value = true
  try {
    const { id, sn, type, state, pageNum, pageSize, sort } = searchForm.value
    const res = await getDeviceListService(
      id === '' || isNaN(Number(id)) ? undefined : Number(id),
      sn?.trim() || undefined,
      type?.trim() || undefined,
      state === '' ? undefined : Number(state),
      pageNum,
      pageSize,
      sort
    )
    machineList.value = (res.data?.list ?? []).map(normalizeDevice)
    total.value = res.data?.total ?? 0
  } catch (error) {
    machineList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取设备列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getMachineList()
}

const handleReset = () => {
  searchForm.value = {
    id: '',
    sn: '',
    type: '',
    state: '',
    pageNum: 1,
    pageSize: 10,
    sort: undefined
  }
  void getMachineList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getMachineList()
}

const handleAddMachine = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addDeviceService(formData)
    ElMessage.success(res.message || '新增设备成功')
    addDialogVisible.value = false
    await getMachineList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('新增设备失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentMachine.value = normalizeDevice(row)
  editDialogVisible.value = true
}

const handleUpdateMachine = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateDeviceService(formData.id, {
      sn: formData.sn,
      type: formData.type,
      state: formData.state
    })
    ElMessage.success(res.message || '修改设备成功')
    editDialogVisible.value = false
    await getMachineList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('修改设备失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenGroupDialog = (row) => {
  currentMachine.value = normalizeDevice(row)
  groupDialogVisible.value = true
}

const handleJoinGroup = async (formData) => {
  submitLoading.value = true
  try {
    const res = await joinGroupService(formData)
    ElMessage.success(res.message || '设备加入展区成功')
    groupDialogVisible.value = false
    await getMachineList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('设备加入展区失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleQuitGroup = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定让设备“${row.sn}”退出当前展区吗？`,
      '退出确认',
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
    const res = await quitGroupService({ deviceSn: row.sn })
    ElMessage.success(res.message || '设备退出展区成功')
    await getMachineList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('设备退出展区失败')
    }
  }
}

const handleDeleteMachine = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除设备“${row.sn}”吗？`,
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
    const res = await deleteDeviceService(row.id)
    ElMessage.success(res.message || '删除设备成功')
    if (machineList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getMachineList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除设备失败')
    }
  }
}

const formatStateText = (state) => {
  if (Number(state) === 1) {
    return '在线'
  }
  if (Number(state) === 0) {
    return '离线'
  }
  return `状态${state}`
}

onMounted(async () => {
  await getGroupOptions()
  await getMachineList()
})
</script>

<template>
  <div class="machine-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">机器管理</h2>
        <p class="page-subtitle">维护设备档案、在线状态，并支持设备加入或退出展区。</p>
      </div>

      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">新增设备</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent>
        <el-form-item label="设备ID">
          <el-input
            v-model="searchForm.id"
            placeholder="请输入设备ID"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="设备SN">
          <el-input
            v-model="searchForm.sn"
            placeholder="请输入设备SN"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="设备类型">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择设备类型"
            clearable
            class="filter-select"
            @change="handleSearch"
          >
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="设备状态">
          <el-select v-model="searchForm.state" placeholder="全部状态" clearable class="filter-select">
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
          </el-select>
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
        <div class="toolbar-title">设备列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="machineList"
        row-key="id"
        stripe
        empty-text="暂无设备数据"
        class="machine-table"
        height="100%"
      >
        <el-table-column prop="id" label="设备ID" width="100" />
        <el-table-column prop="sn" label="设备SN" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="设备类型" width="140" />
        <el-table-column label="设备状态" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.state) === 1 ? 'success' : 'info'">
              {{ formatStateText(row.state) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属展区" min-width="160">
          <template #default="{ row }">
            <el-tag :type="row.groupName ? 'warning' : 'info'" effect="plain">
              {{ row.groupName || '未分组' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" show-overflow-tooltip />
        <el-table-column fixed="right" label="操作" width="240">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="warning" size="small" @click="handleOpenGroupDialog(row)">
              {{ row.groupName ? '调整展区' : '加入展区' }}
            </el-button>
            <el-button link type="info" size="small" @click="handleQuitGroup(row)">
              退出展区
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteMachine(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ machineList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <MachineAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      @submit="handleAddMachine"
    />

    <MachineEditDialog
      v-model="editDialogVisible"
      :machine="currentMachine"
      :loading="submitLoading"
      @submit="handleUpdateMachine"
    />

    <MachineGroupDialog
      v-model="groupDialogVisible"
      :machine="currentMachine"
      :groups="groupOptions"
      :loading="submitLoading"
      @submit="handleJoinGroup"
    />
  </div>
</template>

<style scoped>
.machine-page {
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

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  flex: 1 1 0;               
  min-width: 0;
}

.search-form :deep(.el-input),
.filter-select {
  width: 100%;
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

.machine-table {
  flex: 1;
  min-height: 0;
}

.machine-table :deep(.el-table__header th) {
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
  .machine-page {
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
