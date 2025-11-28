<template>
  <div class="blank-layout">
    <router-view />
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.blank-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(120deg, #f0f5ff, #f9f0ff);
}
</style>



