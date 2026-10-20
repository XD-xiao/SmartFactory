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

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({
  positionName: '',
  remark: ''
})

const form = reactive(defaultForm())

const rules = {
  positionName: [
    { required: true, message: '请输入职位名称', trigger: 'blur' }
  ],
  remark: [
    { required: true, message: '请输入职位描述', trigger: 'blur' }
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
    positionName: form.positionName.trim(),
    remark: form.remark.trim()
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="添加职位"
    width="500px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="职位名称" prop="positionName">
        <el-input v-model="form.positionName" placeholder="请输入职位名称" clearable />
      </el-form-item>

      <el-form-item label="职位描述" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="请输入职位描述"
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
