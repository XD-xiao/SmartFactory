<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { getDeviceListService } from '@/api/DeviceApi.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  config: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const deviceSnOptions = ref([])
const snLoading = ref(false)

const fetchEduBoxDevices = async () => {
  snLoading.value = true
  try {
    const res = await getDeviceListService(undefined, undefined, 'EduBox', undefined, 1, 999, undefined)
    deviceSnOptions.value = (res.data?.list ?? []).map(d => ({
      label: d.sn,
      value: d.sn
    }))
  } catch {
    deviceSnOptions.value = []
  } finally {
    snLoading.value = false
  }
}

const formRef = ref()
const form = reactive({
  sn: '',
  sensor: null,
  sensorName: '',
  sceneType: null,
  valueType: null,
  max: null,
  min: null,
  maxTh: null,
  midTh: null,
  minTh: null
})

const valueTypeOptions = [
  { label: '范围', value: 0 },
  { label: '开关', value: 1 }
]

const isSwitchType = computed(() => form.valueType === 1)

watch(
  () => form.valueType,
  (val) => {
    if (val === 1) {
      form.max = null
      form.min = null
      form.maxTh = null
      form.midTh = null
      form.minTh = null
    }
  }
)

const rules = {
  sn: [{ required: true, message: '请输入设备SN', trigger: 'blur' }],
  sensor: [{ required: true, message: '请输入传感器编号', trigger: 'blur' }],
  sceneType: [{ required: true, message: '请输入场景编号', trigger: 'blur' }]
}

watch(
  () => [props.modelValue, props.config],
  ([visible, cfg]) => {
    if (visible && cfg) {
      form.sn = cfg.sn ?? ''
      form.sensor = cfg.sensor ?? null
      form.sensorName = cfg.sensorName ?? ''
      form.sceneType = cfg.sceneType ?? null
      form.valueType = cfg.valueType ?? null
      form.max = cfg.max ?? null
      form.min = cfg.min ?? null
      form.maxTh = cfg.maxTh ?? null
      form.midTh = cfg.midTh ?? null
      form.minTh = cfg.minTh ?? null
      formRef.value?.clearValidate()
      fetchEduBoxDevices()
    }
  }
)

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  emit('submit', {
    id: props.config.id,
    sn: form.sn,
    sensor: form.sensor,
    sensorName: form.sensorName || undefined,
    sceneType: form.sceneType,
    valueType: form.valueType !== null ? form.valueType : undefined,
    max: isSwitchType.value ? undefined : form.max,
    min: isSwitchType.value ? undefined : form.min,
    maxTh: isSwitchType.value ? undefined : form.maxTh,
    midTh: isSwitchType.value ? undefined : form.midTh,
    minTh: isSwitchType.value ? undefined : form.minTh
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="编辑传感器配置"
    width="560px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="设备SN" prop="sn">
            <el-select
              v-model="form.sn"
              placeholder="请选择设备SN"
              clearable
              filterable
              :loading="snLoading"
              style="width:100%"
            >
              <el-option v-for="item in deviceSnOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="传感器编号" prop="sensor">
            <el-input-number v-model="form.sensor" :controls="false" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="传感器名称">
            <el-input v-model="form.sensorName" placeholder="如 温湿度传感器" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="场景编号" prop="sceneType">
            <el-input-number v-model="form.sceneType" :controls="false" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="取值类型">
        <el-select v-model="form.valueType" placeholder="请选择取值类型" clearable style="width:100%">
          <el-option v-for="item in valueTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-row v-if="!isSwitchType" :gutter="16">
        <el-col :span="12">
          <el-form-item label="上限值">
            <el-input-number v-model="form.max" :controls="false" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="下限值">
            <el-input-number v-model="form.min" :controls="false" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="!isSwitchType" :gutter="16">
        <el-col :span="12">
          <el-form-item label="最大阈值">
            <el-input-number v-model="form.maxTh" :controls="false" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="中间阈值">
            <el-input-number v-model="form.midTh" :controls="false" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="!isSwitchType" :gutter="16">
        <el-col :span="12">
          <el-form-item label="最小阈值">
            <el-input-number v-model="form.minTh" :controls="false" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>
