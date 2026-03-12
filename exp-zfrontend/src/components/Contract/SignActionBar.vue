<template>
  <div class="sign-action-bar">
    <el-button type="primary" size="small" @click="$emit('sign')">签订</el-button>
    <el-button size="small" @click="$emit('unsign')">不签订</el-button>
  </div>
</template>

<script setup lang="ts">
defineEmits<{
  (e: 'sign'): void;
  (e: 'unsign'): void;
}>();
</script>

<style scoped lang="scss">
.sign-action-bar {
  display: flex;
  gap: 8px;
}
</style>
