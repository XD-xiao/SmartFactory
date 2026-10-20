<script setup>
import { reactive, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({ barCode: '', deviceSn: '' })
const form = reactive(defaultForm())

const rules = {
  barCode: [{ required: true, message: '请输入产品条形码', trigger: 'blur' }],
  deviceSn: [{ required: true, message: '请输入设备SN', trigger: 'blur' }]
}

watch(() => props.modelValue, (visible) => {
  if (visible) {
    Object.assign(form, defaultForm())
    formRef.value?.clearValidate()
  }
})

const handleClose = () => emit('update:modelValue', false)

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('submit', {
    barCode: form.barCode?.trim(),
    deviceSn: form.deviceSn?.trim(),
  })
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="产品出库" width="480px" destroy-on-close
    @update:model-value="emit('update:modelValue', $event)" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="条形码" prop="barCode">
        <el-input v-model="form.barCode" placeholder="请输入产品条形码" clearable />
      </el-form-item>
      <el-form-item label="设备SN" prop="deviceSn">
        <el-input v-model="form.deviceSn" placeholder="请输入设备SN" clearable />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">确认出库</el-button>
      </div>
    </template>
  </el-dialog>
</template>
