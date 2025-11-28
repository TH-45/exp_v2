<template>
  <div class="forbidden">
    <el-result
      icon="warning"
      title="无访问权限"
      sub-title="您没有访问当前页面的权限，如需开通请联系管理员。"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

const goHome = () => {
  router.push('/');
};
</script>

<style scoped lang="scss">
.forbidden {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>


