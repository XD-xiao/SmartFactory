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
  },
  maintenance: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const failureLevelOptions = [
  { label: '紧急', value: 1 },
  { label: '高', value: 2 },
  { label: '中', value: 3 },
  { label: '低', value: 4 }
]
const defaultForm = () => ({
  id: '',
  sn: '',
  reason: '',
  failureLevel: '',
  failureTime: '',
  failureRecoverTime: '',
  status: 0
})

const form = reactive(defaultForm())

const rules = {
  status: [
    { required: true, message: '请选择处理状态', trigger: 'change' }
  ]
}

const fillForm = () => {
  Object.assign(form, {
    id: props.maintenance?.id ?? '',
    sn: props.maintenance?.sn ?? '',
    reason: props.maintenance?.reason ?? '',
    failureLevel: props.maintenance?.failureLevel ?? '',
    failureTime: props.maintenance?.failureTime ?? '',
    failureRecoverTime: props.maintenance?.failureRecoverTime ?? '',
    status: Number(props.maintenance?.statusValue ?? props.maintenance?.status ?? 0)
  })
  formRef.value?.clearValidate()
}

watch(
  () => [props.modelValue, props.maintenance],
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
    status: Number(form.status)
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="处理维修工单"
    width="620px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="工单ID">
        <el-input :model-value="form.id" disabled />
      </el-form-item>

      <el-form-item label="设备SN">
        <el-input :model-value="form.sn" disabled />
      </el-form-item>

      <el-form-item label="故障等级">
        <el-select v-model="form.failureLevel" filterable allow-create default-first-option clearable placeholder="请选择故障等级" style="width: 100%">
          <el-option v-for="item in failureLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="故障时间">
        <el-input :model-value="form.failureTime || '未填写'" disabled />
      </el-form-item>

      <el-form-item label="恢复时间">
        <el-input :model-value="form.failureRecoverTime || '未恢复'" disabled />
      </el-form-item>

      <el-form-item label="故障原因">
        <el-input
          :model-value="form.reason || '未填写'"
          type="textarea"
          :rows="4"
          disabled
        />
      </el-form-item>

      <el-form-item label="处理状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="0">待处理</el-radio>
          <el-radio :value="1">维修中</el-radio>
          <el-radio :value="2">已恢复</el-radio>
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
