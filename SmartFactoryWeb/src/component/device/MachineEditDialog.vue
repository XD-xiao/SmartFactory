<script setup>
import { reactive, ref, watch } from 'vue'

const typeOptions = ref([
  { label: 'Pad', value: 'Pad' },
  { label: 'Pda', value: 'Pda' },
  { label: 'Adapter', value: 'Adapter' },
  { label: 'EduBox', value: 'EduBox' }
])

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  machine: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({
  id: '',
  sn: '',
  type: '',
  state: 1
})

const form = reactive(defaultForm())

const rules = {
  sn: [
    { required: true, message: '请输入设备SN', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  state: [
    { required: true, message: '请选择设备状态', trigger: 'change' }
  ]
}

const fillForm = () => {
  Object.assign(form, {
    id: props.machine?.id ?? '',
    sn: props.machine?.sn ?? '',
    type: props.machine?.type ?? '',
    state: Number(props.machine?.state ?? 1)
  })
  formRef.value?.clearValidate()
}

watch(
  () => [props.modelValue, props.machine],
  ([visible]) => {
    if (visible) {
      fillForm()
    }
  },
  { deep: true }
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
    id: Number(form.id),
    sn: form.sn.trim(),
    type: form.type.trim(),
    state: Number(form.state)
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="编辑设备"
    width="520px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="设备ID">
        <el-input :model-value="form.id" disabled />
      </el-form-item>

      <el-form-item label="设备SN" prop="sn">
        <el-input v-model="form.sn" placeholder="请输入设备SN" clearable maxlength="50" />
      </el-form-item>

      <el-form-item label="设备类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择设备类型" clearable class="filter-select">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="设备状态" prop="state">
        <el-radio-group v-model="form.state">
          <el-radio :value="1">在线</el-radio>
          <el-radio :value="0">离线</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          保存
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>
