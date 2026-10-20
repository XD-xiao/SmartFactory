<template>
  <div>
    <canvas ref="qrcodeRef"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import QRCode from 'qrcode'

const props = defineProps({
  value: String
})

const qrcodeRef = ref(null)

function renderQrcode() {
  if (qrcodeRef.value && props.value) {
    QRCode.toCanvas(qrcodeRef.value, String(props.value), {
      width: 200,
      margin: 2,
      color: {
        dark: '#333',
        light: '#fff'
      }
    })
  }
}

onMounted(() => {
  renderQrcode()
})

watch(() => props.value, () => {
  renderQrcode()
})
</script>
