<template>
  <div class="dashboard">
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card>
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="mt16">
      <div>这里预留 ECharts 图表位置，后续可接入招投标、合同、项目统计数据。</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const cards = ref([
  { label: '本年招标数量', value: 0 },
  { label: '本年投标数量', value: 0 },
  { label: '中标率', value: '0%' },
  { label: '合同总金额', value: '0.00' },
]);
</script>

<style scoped lang="scss">
.dashboard {
  .stat-label {
    color: #666;
  }

  .stat-value {
    font-size: 24px;
    font-weight: bold;
    margin-top: 8px;
  }

  .mt16 {
    margin-top: 16px;
  }
}
</style>



