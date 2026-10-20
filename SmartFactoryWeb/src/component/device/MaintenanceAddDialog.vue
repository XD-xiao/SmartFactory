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
  devices: {
    type: Array,
    default: () => []
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
  sn: '',
  reason: '',
  failureLevel: null,
  failureTime: ''
})

const form = reactive(defaultForm())

const rules = {
  sn: [
    { required: true, message: '请选择设备', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入故障原因', trigger: 'blur' }
  ],
  failureLevel: [
    { required: true, message: '请选择故障等级', trigger: 'change' }
  ],
  failureTime: [
    { required: true, message: '请选择故障时间', trigger: 'change' }
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
    sn: form.sn,
    reason: form.reason.trim(),
    failureLevel: form.failureLevel,
    failureTime: form.failureTime
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="新增维修工单"
    width="620px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="设备SN" prop="sn">
        <el-select
          v-model="form.sn"
          filterable
          clearable
          placeholder="请选择设备"
          style="width: 100%"
        >
          <el-option
            v-for="item in devices"
            :key="item.id || item.sn"
            :label="`${item.sn} / ${item.type || '未知类型'}`"
            :value="item.sn"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="故障等级" prop="failureLevel">
        <el-select
          v-model="form.failureLevel"
          filterable
          allow-create
          default-first-option
          clearable
          placeholder="请选择故障等级"
          style="width: 100%"
        >
          <el-option
            v-for="item in failureLevelOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="故障时间" prop="failureTime">
        <el-date-picker
          v-model="form.failureTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="请选择故障时间"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="故障原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="请输入故障原因"
        />
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
