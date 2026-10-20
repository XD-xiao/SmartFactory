<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getSensorConfigService, updateSensorThresholdsService } from '@/api/DeviceApi'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  sensorType: {
    type: String,
    required: true
  },
  sensorName: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const dialogVisible = ref(false)
const loading = ref(false)
const fetchLoading = ref(false)
const config = ref(null)

// 各传感器阈值字段定义
const THRESHOLD_FIELDS = {
  '25': [
    { key: 'temMax', label: '温度上限' },
    { key: 'temMaxth', label: '温度预警' },
    { key: 'temMin', label: '温度下限' },
    { key: 'humMax', label: '湿度上限' },
    { key: 'humMaxth', label: '湿度预警' },
    { key: 'humMin', label: '湿度下限' }
  ],
  '27': [
    { key: 'illuminationMax', label: '光照上限' },
    { key: 'illuminationMaxth', label: '光照预警' },
    { key: 'illuminationMin', label: '光照下限' }
  ],
  '29': [
    { key: 'pressureMax', label: '压强上限' },
    { key: 'pressureMaxth', label: '压强预警' },
    { key: 'pressureMin', label: '压强下限' }
  ],
  '30': [
    { key: 'dioxideMax', label: 'CO2上限' },
    { key: 'dioxideMaxth', label: 'CO2预警' },
    { key: 'dioxideMin', label: 'CO2下限' }
  ],
  '31': [
    { key: 'smokeMax', label: '烟雾上限' },
    { key: 'smokeMaxth', label: '烟雾预警' },
    { key: 'smokeMin', label: '烟雾下限' }
  ],
  '32': [
    { key: 'gasMax', label: '燃气上限' },
    { key: 'gasMaxth', label: '燃气预警' },
    { key: 'gasMin', label: '燃气下限' }
  ],
  '33': [
    { key: 'monoxideMax', label: 'CO上限' },
    { key: 'monoxideMaxth', label: 'CO预警' },
    { key: 'monoxideMin', label: 'CO下限' }
  ],
  '40': [
    { key: 'distanceMax', label: '距离上限' },
    { key: 'distanceMaxth', label: '距离预警' },
    { key: 'distanceMin', label: '距离下限' }
  ]
}

const fields = ref([])

// 显示名映射（去除 snake_case 后的驼峰）
const FIELD_KEY_MAP = {
  'temMax': 'tem_max',
  'temMaxth': 'tem_maxth',
  'temMin': 'tem_min',
  'humMax': 'hum_max',
  'humMaxth': 'hum_maxth',
  'humMin': 'hum_min',
  'illuminationMax': 'illumination_max',
  'illuminationMaxth': 'illumination_maxth',
  'illuminationMin': 'illumination_min',
  'pressureMax': 'pressure_max',
  'pressureMaxth': 'pressure_maxth',
  'pressureMin': 'pressure_min',
  'dioxideMax': 'dioxide_max',
  'dioxideMaxth': 'dioxide_maxth',
  'dioxideMin': 'dioxide_min',
  'smokeMax': 'smoke_max',
  'smokeMaxth': 'smoke_maxth',
  'smokeMin': 'smoke_min',
  'gasMax': 'gas_max',
  'gasMaxth': 'gas_maxth',
  'gasMin': 'gas_min',
  'monoxideMax': 'monoxide_max',
  'monoxideMaxth': 'monoxide_maxth',
  'monoxideMin': 'monoxide_min',
  'distanceMax': 'distance_max',
  'distanceMaxth': 'distance_maxth',
  'distanceMin': 'distance_min'
}

const form = ref({})

const fetchConfig = async () => {
  if (!props.sensorType) return
  fetchLoading.value = true
  try {
    const res = await getSensorConfigService(props.sensorType)
    config.value = res.data
    if (config.value) {
      const f = fields.value
      const initial = {}
      f.forEach(item => {
        initial[item.key] = config.value[item.key]
      })
      form.value = initial
    }
  } catch {
    config.value = null
  } finally {
    fetchLoading.value = false
  }
}

watch(
  () => [props.modelValue, props.sensorType],
  ([visible]) => {
    if (visible) {
      fields.value = THRESHOLD_FIELDS[props.sensorType] || []
      dialogVisible.value = true
      fetchConfig()
    } else {
      dialogVisible.value = false
    }
  }
)

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleSubmit = async () => {
  loading.value = true
  try {
    const data = { sensorType: props.sensorType }
    const f = fields.value
    f.forEach(item => {
      const apiKey = FIELD_KEY_MAP[item.key]
      data[apiKey] = Number(form.value[item.key])
    })
    await updateSensorThresholdsService(data)
    ElMessage.success('传感器阈值修改成功')
    emit('update:modelValue', false)
    emit('refresh')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :title="sensorName + ' - 编辑阈值'"
    width="480px"
    destroy-on-close
    @close="handleClose"
  >
    <div v-loading="fetchLoading">
      <div v-if="!fetchLoading && !config" style="text-align:center;padding:40px 0;color:#999;">
        暂无配置数据，请先触发场景切换
      </div>
      <el-form v-else label-width="100px">
        <el-form-item
          v-for="item in fields"
          :key="item.key"
          :label="item.label"
        >
          <el-input-number
            v-model="form[item.key]"
            :controls="false"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
    </div>

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
