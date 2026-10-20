<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { getGroupListService } from '@/api/DeviceApi'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  productionData: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({ id: null, plannedProduction: null, storeNum: null, qualifiedNum: null, groupId: null })
const form = reactive(defaultForm())
const groupList = ref([])

const rules = {
  plannedProduction: [{ required: true, message: '请输入计划产量', trigger: 'blur' }]
}

const fillForm = () => {
  Object.assign(form, {
    id: props.productionData?.id ?? '',
    plannedProduction: props.productionData?.plannedProduction ?? null,
    storeNum: props.productionData?.storeNum ?? null,
    qualifiedNum: props.productionData?.qualifiedNum ?? null,
    groupId: props.productionData?.groupId ?? null
  })
  formRef.value?.clearValidate()
}

const fetchGroupList = async () => {
  try {
    const res = await getGroupListService(null, null, 1, 999, null)
    groupList.value = res.data?.list ?? []
  } catch {
    groupList.value = []
  }
}

watch(() => [props.modelValue, props.productionData], ([visible]) => {
  if (visible) fillForm()
}, { deep: true })

const handleClose = () => emit('update:modelValue', false)

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const data = { id: form.id, plannedProduction: Number(form.plannedProduction) }
  if (form.storeNum != null) data.storeNum = Number(form.storeNum)
  if (form.qualifiedNum != null) data.qualifiedNum = Number(form.qualifiedNum)
  if (form.groupId) data.groupId = Number(form.groupId)
  emit('submit', data)
}

onMounted(() => {
  fetchGroupList()
})
</script>

<template>
  <el-dialog :model-value="modelValue" title="更新产品产量" width="500px" destroy-on-close
    @update:model-value="emit('update:modelValue', $event)" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="记录ID">
        <el-input :model-value="form.id" disabled />
      </el-form-item>
      <el-form-item label="计划产量" prop="plannedProduction">
        <el-input-number v-model="form.plannedProduction" :min="0" :max="999999" style="width:100%" placeholder="请输入计划产量" />
      </el-form-item>
      <el-form-item label="入库数量">
        <el-input-number v-model="form.storeNum" :min="0" :max="999999" style="width:100%" placeholder="请输入入库数量" />
      </el-form-item>
      <el-form-item label="合格数量">
        <el-input-number v-model="form.qualifiedNum" :min="0" :max="999999" style="width:100%" placeholder="请输入合格数量" />
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
        <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>
