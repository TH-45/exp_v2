<template>
  <el-card shadow="never" class="business-card" v-loading="loading">
    <template #header>
      <div class="panel-title">业务信息（合同）</div>
    </template>
    <el-empty v-if="!contract" description="未查询到合同业务数据" />
    <el-descriptions v-else :column="3" border>
      <el-descriptions-item label="合同ID">{{ contract.contractId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="合同编号">{{ contract.contractCode || '-' }}</el-descriptions-item>
      <el-descriptions-item label="合同名称">{{ contract.contractName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="合同状态">{{ contract.status || '-' }}</el-descriptions-item>
      <el-descriptions-item label="合同类型">{{ contract.contractType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="合同类别">{{ contract.contractCategory || '-' }}</el-descriptions-item>
      <el-descriptions-item label="采购方">{{ contract.purchaserName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="供应商">{{ contract.supplierName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="项目">{{ contract.projectName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="签订日期">{{ contract.signDate || '-' }}</el-descriptions-item>
      <el-descriptions-item label="生效日期">{{ contract.effectiveDate || '-' }}</el-descriptions-item>
      <el-descriptions-item label="结束日期">{{ contract.endDate || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getContractDetail, type ContractVO } from '@/api/contracts/contract';
import type { ApprovalDetail } from '@/api/approval';

interface Props {
  busId: number | string;
  busType: string;
  detail: ApprovalDetail | null;
}

const props = defineProps<Props>();
const loading = ref(false);
type ContractPanelVO = ContractVO & { contractType?: string; contractCategory?: string };
const contract = ref<ContractPanelVO | null>(null);

async function loadContract() {
  const id = Number(props.busId || 0);
  if (!id) {
    contract.value = null;
    return;
  }
  loading.value = true;
  try {
    const res = await getContractDetail(id);
    contract.value = (res as ContractPanelVO) || null;
  } catch (e) {
    contract.value = null;
    ElMessage.error((e as Error)?.message || '查询合同详情失败');
  } finally {
    loading.value = false;
  }
}

onMounted(loadContract);
watch(() => props.busId, loadContract);
</script>

<style scoped lang="scss">
.business-card {
  border: 0;
}

.panel-title {
  font-weight: 600;
}
</style>
