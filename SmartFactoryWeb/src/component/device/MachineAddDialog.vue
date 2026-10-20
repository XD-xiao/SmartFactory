<script setup>
import { reactive, ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const typeOptions = ref([
  { label: 'Pad', value: 'Pad' },
  { label: 'Pda', value: 'Pda' },
  { label: 'Adapter', value: 'Adapter' },
  { label: 'EduBox', value: 'EduBox' }
])

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({
  sn: '',
  type: ''
})

const form = reactive(defaultForm())

const rules = {
  sn: [
    { required: true, message: '请输入设备SN', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ]
}

const resetForm = () => {
  Object.assign(form, defaultForm())
  formRef.value?.clearValidate()
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      resetForm()
    }
  }
)

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  emit('submit', {
    sn: form.sn.trim(),
    type: form.type.trim()
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="新增设备"
    width="520px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="设备SN" prop="sn">
        <el-input v-model="form.sn" placeholder="请输入设备SN" clearable maxlength="50" />
      </el-form-item>

      <el-form-item label="设备类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择设备类型" clearable class="filter-select">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>
