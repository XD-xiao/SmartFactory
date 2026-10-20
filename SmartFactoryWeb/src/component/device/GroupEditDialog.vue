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
  group: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({
  id: '',
  name: ''
})

const form = reactive(defaultForm())

const rules = {
  name: [
    { required: true, message: '请输入展区名称', trigger: 'blur' }
  ]
}

const fillForm = () => {
  Object.assign(form, {
    id: props.group?.id ?? '',
    name: props.group?.name ?? ''
  })
  formRef.value?.clearValidate()
}

watch(
  () => [props.modelValue, props.group],
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
    name: form.name.trim()
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="编辑展区"
    width="500px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="展区ID">
        <el-input :model-value="form.id" disabled />
      </el-form-item>

      <el-form-item label="展区名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入展区名称" clearable maxlength="50" />
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
