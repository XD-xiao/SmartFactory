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
  leaveRecord: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({
  leaveID: '',
  employeeNumber: '',
  employeeName: '',
  leaveType: 1,
  status: 0,
  startDate: '',
  endDate: '',
  reason: ''
})

const form = reactive(defaultForm())

const rules = {
  leaveType: [
    { required: true, message: '请选择请假类型', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择审批状态', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入请假原因', trigger: 'blur' }
  ]
}

const fillForm = () => {
  Object.assign(form, {
    leaveID: props.leaveRecord?.leaveID ?? props.leaveRecord?.leaveId ?? '',
    employeeNumber: props.leaveRecord?.employeeNumber ?? '',
    employeeName: props.leaveRecord?.employeeName ?? '',
    leaveType: props.leaveRecord?.leaveType ?? 1,
    status: props.leaveRecord?.status ?? 0,
    startDate: props.leaveRecord?.startDate ?? '',
    endDate: props.leaveRecord?.endDate ?? '',
    reason: props.leaveRecord?.reason ?? ''
  })
  formRef.value?.clearValidate()
}

watch(
  () => [props.modelValue, props.leaveRecord],
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
    leave_id: Number(form.leaveID),
    leave_type: form.leaveType,
    start_date: form.startDate,
    end_date: form.endDate,
    reason: form.reason.trim(),
    status: form.status
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="编辑请假记录"
    width="560px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="请假ID">
        <el-input :model-value="form.leaveID" disabled />
      </el-form-item>

      <el-form-item label="员工工号">
        <el-input :model-value="form.employeeNumber || '未返回'" disabled />
      </el-form-item>

      <el-form-item label="员工姓名">
        <el-input :model-value="form.employeeName || '未返回'" disabled />
      </el-form-item>

      <el-form-item label="请假类型" prop="leaveType">
        <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
          <el-option label="年假" :value="1" />
          <el-option label="病假" :value="2" />
          <el-option label="事假" :value="3" />
          <el-option label="调休" :value="4" />
        </el-select>
      </el-form-item>

      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker
          v-model="form.startDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择开始日期"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker
          v-model="form.endDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择结束日期"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="审批状态" prop="status">
        <el-select v-model="form.status" placeholder="请选择审批状态" style="width: 100%">
          <el-option label="待审批" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
      </el-form-item>

      <el-form-item label="请假原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="请输入请假原因"
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
