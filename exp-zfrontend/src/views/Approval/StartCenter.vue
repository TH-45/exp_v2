<template>
  <el-card>
    <template #header>
      <div class="header">
        <div class="title">流程发起中心</div>
        <el-button size="small" @click="loadData">刷新</el-button>
      </div>
    </template>

    <el-empty v-if="groups.length === 0" description="暂无可发起流程" />

    <div v-for="group in groups" :key="group.busType" class="group">
      <div class="group-title">{{ group.businessName }}</div>
      <el-row :gutter="12">
        <el-col :span="8" v-for="def in group.items" :key="def.procDefId">
          <el-card shadow="hover" class="flow-card" @click="goCreate(def)">
            <div class="flow-name">{{ def.procName }}</div>
            <div class="flow-desc">{{ group.description }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { listProcessDefinitions, type ProcessDefinition } from '@/api/process/definition';
import { findStartEntry } from './start-center/registry';

const router = useRouter();
const defs = ref<ProcessDefinition[]>([]);

const groups = computed(() => {
  const result: Array<{
    busType: string;
    businessName: string;
    description: string;
    items: ProcessDefinition[];
  }> = [];
  const map = new Map<string, ProcessDefinition[]>();
  for (const d of defs.value) {
    if (!d.busType) continue;
    const list = map.get(d.busType) || [];
    list.push(d);
    map.set(d.busType, list);
  }
  for (const [busType, items] of map.entries()) {
    const entry = findStartEntry(busType);
    result.push({
      busType,
      businessName: entry?.businessName || busType,
      description: entry?.description || '进入业务页面创建单据并发起审批',
      items,
    });
  }
  return result;
});

async function loadData() {
  const res = await listProcessDefinitions({
    pageNum: 1,
    pageSize: 500,
    isActive: 1,
  });
  defs.value = (res.list || []).filter((x) => x.isActive === 1);
}

function goCreate(def: ProcessDefinition) {
  if (!def.busType || !def.procCode) {
    ElMessage.warning('流程定义缺少 busType 或 procCode，无法发起');
    return;
  }
  router.push({
    path: '/approval/start-dispatch',
    query: {
      startProcCode: def.procCode,
      startProcName: def.procName,
      startBusType: def.busType,
    },
  });
}

loadData();
</script>

<style scoped lang="scss">
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title {
  font-weight: 600;
  font-size: 18px;
}
.group {
  margin-bottom: 16px;
}
.group-title {
  font-weight: 600;
  margin-bottom: 10px;
}
.flow-card {
  cursor: pointer;
}
.flow-name {
  font-weight: 600;
  margin-bottom: 6px;
}
.flow-desc {
  color: #909399;
  font-size: 13px;
}
</style>
