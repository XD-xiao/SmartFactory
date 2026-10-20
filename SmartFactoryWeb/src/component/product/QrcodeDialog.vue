<template>
  <el-dialog
    :model-value="modelValue"
    title="产品二维码"
    width="420px"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="qrcode-dialog-body">
      <div class="qrcode-wrapper">
        <Qrcode v-if="barCode" :value="barCode" />
        <span v-else class="qrcode-empty">无二维码数据</span>
      </div>
      <div class="qrcode-label">{{ barCode }}</div>
    </div>
  </el-dialog>
</template>

<script setup>
import Qrcode from './Qrcode.vue'

defineProps({
  modelValue: { type: Boolean, default: false },
  barCode: { type: String, default: '' }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.qrcode-dialog-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
}

.qrcode-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-wrapper :deep(canvas) {
  max-width: 100%;
  height: auto;
}

.qrcode-label {
  font-size: 14px;
  color: #606266;
  word-break: break-all;
}

.qrcode-empty {
  color: #c0c4cc;
  font-size: 14px;
}
</style>
