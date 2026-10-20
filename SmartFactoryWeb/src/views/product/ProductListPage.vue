<script setup>
import { computed, onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addProductService,
  getProductListService,
  updateProductService,
  deleteProductService,
  getProductTypeListService,
  getProductModelListService,
  getPictureService
} from '@/api/ProductApi'
import ProductAddDialog from '@/component/product/ProductAddDialog.vue'
import ProductEditDialog from '@/component/product/ProductEditDialog.vue'
import QrcodeDialog from '@/component/product/QrcodeDialog.vue'

const searchForm = ref({
  barCode: '',
  typeCode: '',
  modelCode: '',
  productCode: '',
  deviceSn: '',
  pageNum: 1,
  pageSize: 10
})

const productList = ref([])
const total = ref(0)
const tableLoading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentProduct = ref({})
const barcodeDialogVisible = ref(false)
const currentBarCode = ref('')

const typeOptions = ref([])
const modelOptions = ref([])

// 图片缓存：picId -> base64/URL
const picUrlMap = reactive({})

const totalPages = computed(() => {
  if (!total.value) return 0
  return Math.ceil(total.value / searchForm.value.pageSize)
})

// 加载产品类型选项
const loadTypeOptions = async () => {
  try {
    const res = await getProductTypeListService(1, 1000)
    typeOptions.value = res.data?.list ?? []
  } catch { /* ignore */ }
}

// 加载产品型号选项
const loadModelOptions = async () => {
  try {
    const res = await getProductModelListService(1, 1000)
    modelOptions.value = res.data?.list ?? []
  } catch { /* ignore */ }
}

// 根据picId获取图片URL（带缓存）
const getPicUrl = async (picId) => {
  if (!picId) return
  if (picUrlMap[picId]) return
  try {
    const res = await getPictureService(picId)
    if (res.data?.content) {
      picUrlMap[picId] = res.data.content
    }
  } catch { /* ignore */ }
}

// 批量加载产品列表中的图片
const loadProductPics = (list) => {
  list.forEach((item) => {
    if (item.picId) {
      void getPicUrl(item.picId)
    }
  })
}

const getProductList = async () => {
  tableLoading.value = true
  try {
    const { barCode, typeCode, modelCode, productCode, deviceSn, pageNum, pageSize } = searchForm.value
    const res = await getProductListService(
      pageNum, pageSize,
      barCode?.trim() || undefined,
      typeCode || undefined,
      modelCode || undefined,
      productCode?.trim() || undefined,
      deviceSn?.trim() || undefined
    )
    productList.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
    loadProductPics(productList.value)
  } catch (error) {
    productList.value = []
    total.value = 0
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('获取产品列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  void getProductList()
}

const handleReset = () => {
  searchForm.value = {
    barCode: '', typeCode: '', modelCode: '',
    productCode: '', deviceSn: '',
    pageNum: 1, pageSize: 10
  }
  void getProductList()
}

const handleCurrentChange = (pageNum) => {
  searchForm.value.pageNum = pageNum
  void getProductList()
}

const handleAddProduct = async (formData) => {
  submitLoading.value = true
  try {
    const res = await addProductService(formData)
    ElMessage.success(res.message || '添加产品成功')
    addDialogVisible.value = false
    await getProductList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('添加产品失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleOpenEditDialog = (row) => {
  currentProduct.value = { ...row }
  editDialogVisible.value = true
}

const handleUpdateProduct = async (formData) => {
  submitLoading.value = true
  try {
    const res = await updateProductService(formData)
    ElMessage.success(res.message || '修改产品成功')
    editDialogVisible.value = false
    await getProductList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('修改产品失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteProduct = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除产品"${row.barCode || row.id}"吗？`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    const res = await deleteProductService(row.id)
    ElMessage.success(res.message || '删除产品成功')
    if (productList.value.length === 1 && searchForm.value.pageNum > 1) {
      searchForm.value.pageNum -= 1
    }
    await getProductList()
  } catch (error) {
    if (!error?.message && !error?.response?.data?.message) {
      ElMessage.error('删除产品失败')
    }
  }
}

const handleOpenBarcodeDialog = (row) => {
  currentBarCode.value = row.barCode || ''
  barcodeDialogVisible.value = true
}

onMounted(() => {
  loadTypeOptions()
  loadModelOptions()
  getProductList()
})
</script>

<template>
  <div class="product-list-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">产品管理</h2>
      </div>
      <div class="header-right">
        <el-button type="success" @click="addDialogVisible = true">添加产品</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent>
        <el-form-item label="条形码">
          <el-input v-model="searchForm.barCode" placeholder="请输入条形码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="产品类型">
          <el-select v-model="searchForm.typeCode" placeholder="全部类型" clearable style="width:160px">
            <el-option v-for="t in typeOptions" :key="t.code" :label="t.name" :value="t.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品型号">
          <el-select v-model="searchForm.modelCode" placeholder="全部型号" clearable style="width:160px">
            <el-option v-for="m in modelOptions" :key="m.code" :label="m.name" :value="m.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品代号">
          <el-input v-model="searchForm.productCode" placeholder="请输入产品代号" clearable @keyup.enter="handleSearch" />
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
        <div class="toolbar-title">产品列表</div>
        <div class="toolbar-tip">第 {{ searchForm.pageNum }} / {{ totalPages || 1 }} 页</div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="productList"
        row-key="id"
        stripe
        empty-text="暂无产品数据"
        class="product-table"
        height="100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="产品图片" width="90">
          <template #default="{ row }">
            <div class="table-pic-cell">
              <img
                v-if="picUrlMap[row.picId]"
                :src="picUrlMap[row.picId]"
                class="table-pic"
                alt="产品图片"
              />
              <span v-else class="table-pic-empty">-</span>
            </div>
          </template>
        </el-table-column>
        
<!--        <el-table-column prop="barCode" label="条形码" min-width="180" />-->
        <el-table-column prop="typeName" label="产品类型" min-width="120" />
        <el-table-column prop="modelName" label="产品型号" min-width="120" />
        <el-table-column prop="productCode" label="产品代号" min-width="140">
          <template #default="{ row }">{{ row.productCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="deviceSn" label="设备SN" min-width="160">
          <template #default="{ row }">{{ row.deviceSn || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column prop="updateTime" label="更新时间" min-width="170" />
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenBarcodeDialog(row)">二维码</el-button>
            <el-button link type="primary" size="small" @click="handleOpenEditDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteProduct(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          当前页 <span>{{ productList.length }}</span> 条，共 <span>{{ total }}</span> 条
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

    <ProductAddDialog
      v-model="addDialogVisible"
      :loading="submitLoading"
      :type-options="typeOptions"
      :model-options="modelOptions"
      @submit="handleAddProduct"
    />

    <ProductEditDialog
      v-model="editDialogVisible"
      :product-data="currentProduct"
      :loading="submitLoading"
      :type-options="typeOptions"
      :model-options="modelOptions"
      @submit="handleUpdateProduct"
    />

    <QrcodeDialog
      v-model="barcodeDialogVisible"
      :bar-code="currentBarCode"
    />
  </div>
</template>

<style scoped>
.product-list-page {
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

/* 表格图片 */
.table-pic-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
}

.table-pic {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.table-pic-empty {
  color: #c0c4cc;
  font-size: 14px;
}

.product-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #606266;
}

.product-table {
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
  .product-list-page {
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
