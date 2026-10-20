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
  department: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({
  departmentId: '',
  departmentName: '',
  status: 1,
  remark: ''
})

const form = reactive(defaultForm())

const rules = {
  departmentName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择部门状态', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入备注', trigger: 'blur' }
  ]
}

const fillForm = () => {
  Object.assign(form, {
    departmentId: props.department?.departmentId ?? '',
    departmentName: props.department?.departmentName ?? '',
    status: props.department?.status ?? props.department?.state ?? 1,
    remark: props.department?.remark ?? ''
  })
  formRef.value?.clearValidate()
}

watch(
  () => [props.modelValue, props.department],
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
    departmentId: form.departmentId,
    departmentName: form.departmentName.trim(),
    status: form.status,
    remark: form.remark.trim()
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="修改部门"
    width="500px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="部门ID">
        <el-input :model-value="form.departmentId" disabled />
      </el-form-item>

      <el-form-item label="部门名称" prop="departmentName">
        <el-input v-model="form.departmentName" placeholder="请输入部门名称" clearable />
      </el-form-item>

      <el-form-item label="部门状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="请输入备注"
        />
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
