<template>
  <div class="approval-action-bar">
    <el-button type="primary" size="small" @click="$emit('approve')">审批</el-button>
    <el-button size="small" @click="$emit('reject')">驳回上一个审批人</el-button>
  </div>
</template>

<script setup lang="ts">
defineEmits<{
  (e: 'approve'): void;
  (e: 'reject'): void;
}>();
</script>

<style scoped lang="scss">
.approval-action-bar {
  display: flex;
  gap: 8px;
}
</style>
