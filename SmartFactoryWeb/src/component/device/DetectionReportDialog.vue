<script setup>
import { watch } from 'vue'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  recordId: {
    type: Number,
    default: null
  },
  report: {
    type: String,
    default: ''
  },
  status: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const getStatusLabel = (status) => {
  if (status === 'SUCCESS') return '分析成功'
  if (status === 'FAILED') return '分析失败'
  return status ?? '-'
}

const getStatusColor = (status) => {
  if (status === 'SUCCESS') return '#67C23A'
  if (status === 'FAILED') return '#F56C6C'
  return '#909399'
}

const formatReport = (text) => {
  if (!text) return '无报告内容'
  return renderMarkdown(text)
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    width="700px"
    top="6vh"
    destroy-on-close
    :close-on-click-modal="false"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template #header>
      <div class="dialog-header">
        <div class="dialog-header-left">
          <span class="dialog-title-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
          </span>
          <span class="dialog-title-text">AI 检测报告</span>
          <span v-if="recordId" class="dialog-title-sub">#{{ recordId }}</span>
        </div>
        <div class="dialog-header-right">
          <span
            class="dialog-status-badge"
            :style="{ background: getStatusColor(status), color: '#fff' }"
          >
            {{ getStatusLabel(status) }}
          </span>
        </div>
      </div>
    </template>

    <div class="report-body">
      <div class="markdown-body report-content" v-html="formatReport(report)"></div>
    </div>
  </el-dialog>
</template>

<style scoped>
@import '@/utils/markdown.css';

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}

.dialog-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dialog-title-icon {
  display: flex;
  align-items: center;
  color: #409EFF;
}

.dialog-title-text {
  font-size: 17px;
  font-weight: 700;
  color: #1d2129;
  letter-spacing: 0.3px;
}

.dialog-title-sub {
  font-size: 14px;
  font-weight: 400;
  color: #86909c;
}

.dialog-header-right {
  display: flex;
  align-items: center;
}

.dialog-status-badge {
  padding: 3px 14px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.report-body {
  min-height: 200px;
  max-height: 520px;
  overflow-y: auto;
}

.report-content {
  padding: 20px 24px;
  background: #f8fafc;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  font-size: 14px;
  color: #303133;
}

:deep(.el-dialog__header) {
  padding: 18px 24px 14px;
  border-bottom: 1px solid #f0f1f3;
  margin-right: 0;
}

:deep(.el-dialog__body) {
  padding: 20px 24px 24px;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

@media (max-width: 768px) {
  .dialog-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .report-content {
    padding: 14px 16px;
  }
}
</style>
