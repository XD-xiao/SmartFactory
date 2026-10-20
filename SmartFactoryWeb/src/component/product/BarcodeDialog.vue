<template>
  <el-dialog
    :model-value="modelValue"
    title="产品条形码"
    width="520px"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="barcode-dialog-body">
      <div class="barcode-label">条形码：{{ barCode }}</div>
      <div class="barcode-wrapper">
        <Barcode v-if="barCode" :value="barCode" />
        <span v-else class="barcode-empty">无条形码数据</span>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import Barcode from './Barcode.vue'

defineProps({
  modelValue: { type: Boolean, default: false },
  barCode: { type: String, default: '' }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.barcode-dialog-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 16px 0;
}

.barcode-label {
  font-size: 14px;
  color: #606266;
  word-break: break-all;
}

.barcode-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 80px;
  overflow-x: auto;
}

.barcode-wrapper :deep(svg) {
  max-width: 100%;
  height: auto;
}

.barcode-empty {
  color: #c0c4cc;
  font-size: 14px;
}
</style>
