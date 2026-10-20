<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadPictureService } from '@/api/ProductApi.js'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  productData: { type: Object, default: () => ({}) },
  typeOptions: { type: Array, default: () => [] },
  modelOptions: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({
  id: '',
  typeCode: '',
  modelCode: '',
  productCode: '',
  deviceSn: '',
  picId: null
})
const form = reactive(defaultForm())

const picLoading = ref(false)
const previewUrl = ref('')
const fileInputRef = ref(null)

const rules = {
  typeCode: [{ required: true, message: '请选择产品类型', trigger: 'change' }],
  modelCode: [{ required: true, message: '请选择产品型号', trigger: 'change' }]
}

const fillForm = () => {
  const d = props.productData || {}
  Object.assign(form, {
    id: d.id ?? '',
    typeCode: d.typeCode ?? '',
    modelCode: d.modelCode ?? '',
    productCode: d.productCode ?? '',
    deviceSn: d.deviceSn ?? '',
    picId: d.picId ?? null
  })
  previewUrl.value = ''
  if (d.picId) {
    // 尝试加载已有图片
    fetchPicPreview(d.picId)
  }
  formRef.value?.clearValidate()
}

const fetchPicPreview = async (picId) => {
  if (!picId) return
  try {
    const res = await (await import('@/api/ProductApi.js')).getPictureService(picId)
    if (res.data?.content) {
      previewUrl.value = res.data.content
    }
  } catch { /* ignore */ }
}

watch(() => [props.modelValue, props.productData], ([visible]) => {
  if (visible) fillForm()
}, { deep: true })

const handleClose = () => emit('update:modelValue', false)

const compressImage = (file) => {
  return new Promise((resolve, reject) => {
    const maxSize = 800
    const quality = 0.6
    const maxBytes = 200 * 1024
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        let { width, height } = img
        if (width > maxSize || height > maxSize) {
          if (width > height) { height = Math.round((height / width) * maxSize); width = maxSize }
          else { width = Math.round((width / height) * maxSize); height = maxSize }
        }
        const canvas = document.createElement('canvas')
        canvas.width = width; canvas.height = height
        canvas.getContext('2d').drawImage(img, 0, 0, width, height)
        let result = canvas.toDataURL('image/jpeg', quality)
        let tryQ = quality
        while (result.length > maxBytes && tryQ > 0.1) { tryQ -= 0.1; result = canvas.toDataURL('image/jpeg', tryQ) }
        resolve(result)
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = e.target.result
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsDataURL(file)
  })
}

const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('图片大小不能超过 5MB'); return }
  if (!file.type.startsWith('image/')) { ElMessage.warning('请选择图片文件'); return }
  picLoading.value = true
  try {
    const compressed = await compressImage(file)
    previewUrl.value = compressed
    const res = await uploadPictureService({ content: compressed })
    form.picId = res.data?.id
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error(error?.message || '图片上传失败')
  } finally {
    picLoading.value = false
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('submit', {
    id: form.id,
    typeCode: form.typeCode,
    modelCode: form.modelCode,
    productCode: form.productCode?.trim() || undefined,
    deviceSn: form.deviceSn?.trim() || undefined,
    picId: form.picId
  })
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="修改产品" width="560px" destroy-on-close
    @update:model-value="emit('update:modelValue', $event)" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="产品ID">
        <el-input :model-value="form.id" disabled />
      </el-form-item>
      <el-form-item label="产品类型" prop="typeCode">
        <el-select v-model="form.typeCode" placeholder="请选择产品类型" clearable style="width:100%">
          <el-option v-for="t in typeOptions" :key="t.code" :label="t.name" :value="t.code" />
        </el-select>
      </el-form-item>
      <el-form-item label="产品型号" prop="modelCode">
        <el-select v-model="form.modelCode" placeholder="请选择产品型号" clearable style="width:100%">
          <el-option v-for="m in modelOptions" :key="m.code" :label="m.name" :value="m.code" />
        </el-select>
      </el-form-item>
      <el-form-item label="产品代号">
        <el-input v-model="form.productCode" placeholder="请输入产品代号" clearable />
      </el-form-item>
      <el-form-item label="设备SN">
        <el-input v-model="form.deviceSn" placeholder="请输入设备SN" clearable />
      </el-form-item>

      <el-form-item label="产品图片">
        <div class="pic-upload-area">
          <input ref="fileInputRef" type="file" accept="image/*" style="display:none" @change="handleFileChange" />
          <div v-if="!previewUrl" class="pic-placeholder" @click="fileInputRef?.click()">
            <el-icon :size="32"><Plus /></el-icon>
            <span>点击上传图片</span>
          </div>
          <div v-else class="pic-preview-wrapper">
            <img :src="previewUrl" class="pic-preview" alt="产品图片" />
            <div class="pic-overlay">
              <el-button type="primary" size="small" @click.stop="fileInputRef?.click()">更换</el-button>
            </div>
          </div>
          <span v-if="picLoading" class="pic-upload-tip">上传中...</span>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.pic-upload-area {
  display: flex; flex-direction: column; align-items: center; gap: 8px; width: 100%;
}
.pic-placeholder {
  width: 180px; height: 140px; border: 2px dashed #dcdfe6; border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8px; color: #909399; cursor: pointer; transition: border-color .3s;
}
.pic-placeholder:hover { border-color: #409eff; }
.pic-preview-wrapper { position: relative; border-radius: 8px; overflow: hidden; }
.pic-preview { width: 180px; height: 140px; object-fit: cover; display: block; }
.pic-overlay {
  position: absolute; inset: 0; background: rgba(0,0,0,.45);
  display: flex; align-items: center; justify-content: center; gap: 8px; opacity: 0; transition: opacity .3s;
}
.pic-preview-wrapper:hover .pic-overlay { opacity: 1; }
.pic-upload-tip { font-size: 12px; color: #909399; }
</style>
