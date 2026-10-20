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
  machine: {
    type: Object,
    default: () => ({})
  },
  groups: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({
  groupId: ''
})

const form = reactive(defaultForm())

const rules = {
  groupId: [
    { required: true, message: '请选择目标展区', trigger: 'change' }
  ]
}

const fillForm = () => {
  Object.assign(form, {
    groupId: props.machine?.groupId ?? ''
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
    groupId: Number(form.groupId),
    deviceSn: props.machine?.sn
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="设备加入展区"
    width="520px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="设备SN">
        <el-input :model-value="machine?.sn || ''" disabled />
      </el-form-item>

      <el-form-item label="当前展区">
        <el-input :model-value="machine?.groupName || '未分组'" disabled />
      </el-form-item>

      <el-form-item label="目标展区" prop="groupId">
        <el-select v-model="form.groupId" placeholder="请选择目标展区" style="width: 100%">
          <el-option
            v-for="item in groups"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
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
