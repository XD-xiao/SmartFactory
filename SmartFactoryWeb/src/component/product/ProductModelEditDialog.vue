<script setup>
import { reactive, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  modelData: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({ id: '', name: '', code: '' })
const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入型号名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入型号码', trigger: 'blur' },{ pattern: /^[A-Z]{3}\d{3}$/, message: '型号码必须是3位大写字母加3位数字', trigger: 'blur' }]
}

const fillForm = () => {
  Object.assign(form, { id: props.modelData?.id ?? '', name: props.modelData?.name ?? '', code: props.modelData?.code ?? '' })
  formRef.value?.clearValidate()
}

watch(() => [props.modelValue, props.modelData], ([visible]) => {
  if (visible) fillForm()
}, { deep: true })

const handleClose = () => emit('update:modelValue', false)

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('submit', { id: form.id, name: form.name.trim(), code: form.code.trim() })
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="修改产品型号" width="500px" destroy-on-close
    @update:model-value="emit('update:modelValue', $event)" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="型号ID">
        <el-input :model-value="form.id" disabled />
      </el-form-item>
      <el-form-item label="型号名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入型号名称" clearable />
      </el-form-item>
      <el-form-item label="型号码" prop="code">
        <el-input v-model="form.code" placeholder="请输入型号码（唯一）" clearable />
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
