<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { getGroupListService } from '@/api/DeviceApi'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({ barCode: '', planName:'', plannedProduction: null, groupId: null })
const form = reactive(defaultForm())
const groupList = ref([])

const rules = {
  barCode: [{ required: true, message: '请输入产品编号', trigger: 'blur' }],
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  plannedProduction: [{ required: true, message: '请输入计划产量', trigger: 'blur' }]
}

watch(() => props.modelValue, (visible) => {
  if (visible) {
    Object.assign(form, defaultForm())
    formRef.value?.clearValidate()
  }
})

const fetchGroupList = async () => {
  try {
    const res = await getGroupListService(null, null, 1, 999, null)
    groupList.value = res.data?.list ?? []
  } catch {
    groupList.value = []
  }
}

const handleClose = () => emit('update:modelValue', false)

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const data = {
    barCode: form.barCode,
    planName:form.planName,
    plannedProduction: Number(form.plannedProduction)
  }
  if (form.groupId) data.groupId = Number(form.groupId)
  emit('submit', data)
}

onMounted(() => {
  fetchGroupList()
})
</script>

<template>
  <el-dialog :model-value="modelValue" title="设置产品产量" width="500px" destroy-on-close
    @update:model-value="emit('update:modelValue', $event)" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="产品编号" prop="barCode">
        <el-input v-model="form.barCode" placeholder="请输入产品编号（条形码）" clearable />
      </el-form-item>
      <el-form-item label="计划名称" prop="planName">
        <el-input v-model="form.planName" placeholder="请输入计划名称" clearable />
      </el-form-item>
      <el-form-item label="计划产量" prop="plannedProduction">
        <el-input-number v-model="form.plannedProduction" :min="0" :max="999999" style="width:100%" placeholder="请输入计划产量" />
      </el-form-item>
      <el-form-item label="所属分组">
        <el-select v-model="form.groupId" placeholder="请选择分组" clearable style="width:100%">
          <el-option v-for="item in groupList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>
