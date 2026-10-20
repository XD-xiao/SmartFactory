<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updatePasswordService } from '@/api/PersonManagementApi'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  employee: {
    type: Object,
    default: () => ({})
  },
  departments: {
    type: Array,
    default: () => []
  },
  positions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const defaultForm = () => ({
  employeeId: '',
  employeeNumber: '',
  hireDate: '',
  name: '',
  gender: '男',
  birthDate: '',
  phone: '',
  email: '',
  address: '',
  idCard: '',
  departmentId: '',
  positionId: '',
  status: 1
})

const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' },{ pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' },{ pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入有效的邮箱地址', trigger: 'blur' }],
  address: [{ required: true, message: '请输入家庭住址', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' },{ pattern: /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入有效的身份证号码', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  positionId: [{ required: true, message: '请选择所属职位', trigger: 'change' }]
}

const fillForm = () => {
  Object.assign(form, {
    employeeId: props.employee?.employeeId ?? '',
    employeeNumber: props.employee?.employeeNumber ?? '',
    hireDate: props.employee?.hireDate ?? '',
    name: props.employee?.name ?? '',
    gender: props.employee?.gender ?? '男',
    birthDate: props.employee?.birthDate ?? '',
    phone: props.employee?.phone ?? '',
    email: props.employee?.email ?? '',
    address: props.employee?.address ?? '',
    idCard: props.employee?.idCard ?? '',
    departmentId: props.employee?.departmentId ?? '',
    positionId: props.employee?.positionId ?? '',
    status: props.employee?.status ?? 1
  })
  formRef.value?.clearValidate()
}

watch(
  () => [props.modelValue, props.employee],
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
    employeeId: Number(form.employeeId),
    employeeNumber: form.employeeNumber,
    hireDate: form.hireDate,
    name: form.name.trim(),
    gender: form.gender,
    birthDate: form.birthDate,
    phone: form.phone.trim(),
    email: form.email.trim(),
    address: form.address.trim(),
    idCard: form.idCard.trim(),
    departmentId: String(form.departmentId),
    positionId: String(form.positionId),
    status: Number(form.status)
  })
}

const showResetPassword = ref(false)
const resetPasswordLoading = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const handleResetPassword = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入旧密码')
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要重置员工「${props.employee?.name}」的密码吗？`,
      '重置密码确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  resetPasswordLoading.value = true
  try {
    await updatePasswordService({
      employeeId: props.employee?.employeeId,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码重置成功')
    showResetPassword.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
  } catch {
    // 接口层已处理错误提示
  } finally {
    resetPasswordLoading.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="编辑员工"
    width="660px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="staff-form">
      <el-form-item label="员工ID">
        <el-input :model-value="form.employeeId" disabled />
      </el-form-item>

      <el-form-item label="员工工号">
        <el-input :model-value="form.employeeNumber || '系统生成'" disabled />
      </el-form-item>

      <el-form-item label="入职日期">
        <el-input :model-value="form.hireDate || '未返回'" disabled />
      </el-form-item>

      <el-form-item label="员工姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入员工姓名" clearable />
      </el-form-item>

      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio value="男">男</el-radio>
          <el-radio value="女">女</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="出生日期" prop="birthDate">
        <el-date-picker
          v-model="form.birthDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择出生日期"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" clearable />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" clearable />
      </el-form-item>

      <el-form-item label="家庭住址" prop="address">
        <el-input v-model="form.address" placeholder="请输入家庭住址" clearable />
      </el-form-item>

      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="form.idCard" placeholder="请输入身份证号" clearable />
      </el-form-item>

      <el-form-item label="所属部门" prop="departmentId">
        <el-select v-model="form.departmentId" placeholder="请选择所属部门" style="width: 100%">
          <el-option
            v-for="item in departments"
            :key="item.departmentId"
            :label="item.departmentName"
            :value="item.departmentId"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="所属职位" prop="positionId">
        <el-select v-model="form.positionId" placeholder="请选择所属职位" style="width: 100%">
          <el-option
            v-for="item in positions"
            :key="item.positionId"
            :label="item.positionName"
            :value="item.positionId"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="当前状态">
        <el-tag :type="Number(form.status) === 1 ? 'success' : 'info'">
          {{ Number(form.status) === 1 ? '上班中' : '下班中' }}
        </el-tag>
      </el-form-item>

      <el-divider />

      <el-form-item label="重置密码">
        <el-button type="warning" @click="showResetPassword = !showResetPassword">
          {{ showResetPassword ? '取消重置' : '重置密码' }}
        </el-button>
      </el-form-item>

      <template v-if="showResetPassword">
        <el-form-item label="旧密码">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="resetPasswordLoading"
            @click="handleResetPassword"
          >
            确认重置
          </el-button>
        </el-form-item>
      </template>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>
