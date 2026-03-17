<template>
  <el-card>
    <el-result icon="info" title="流程分发中" sub-title="正在根据流程类型跳转到对应业务页面，请稍候..." />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { resolveFlowRouteTarget } from './start-center/flow-route-map';

const route = useRoute();
const router = useRouter();

onMounted(async () => {
  const startBusType = String(route.query.startBusType || '');
  const startProcCode = String(route.query.startProcCode || '');
  const startProcName = String(route.query.startProcName || '');

  const target = resolveFlowRouteTarget({
    busType: startBusType,
    procCode: startProcCode,
  });

  if (!target) {
    ElMessage.warning(`未配置流程入口：${startBusType}/${startProcCode}`);
    await router.replace('/approval/start-center');
    return;
  }

  await router.replace({
    path: target.path,
    query: {
      ...(target.query || {}),
      startProcName,
    },
  });
});
</script>
