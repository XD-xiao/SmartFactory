<template>
  <div>
    <svg ref="barcodeRef"></svg>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import JsBarcode from 'jsbarcode';

const props = defineProps({
  value: String
});

const barcodeRef = ref(null);

function renderBarcode() {
  if (barcodeRef.value && props.value) {
    JsBarcode(barcodeRef.value, String(props.value), {
      format: 'CODE128',
      width: 1.5,
      height: 50,
      displayValue: false,
      fontSize: 12,
      margin: 2,
      background: '#fff',
      lineColor: '#333'
    });
  }
}

onMounted(() => {
  renderBarcode();
});

watch(() => props.value, () => {
  renderBarcode();
});
</script>