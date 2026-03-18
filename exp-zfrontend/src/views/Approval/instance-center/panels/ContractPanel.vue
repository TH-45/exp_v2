<template>
  <div class="contract-panel" v-loading="loading">
    <el-empty v-if="!contract" description="未查询到合同业务数据" />
    <div v-else class="contract-detail readonly">
      <div class="group-title">基本信息</div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="合同ID">{{ contract.contractId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="合同编号">{{ contract.contractCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ contract.contractName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="合同状态">{{ statusText(contract.status) }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ contractTypeText(contract.contractType) }}</el-descriptions-item>
        <el-descriptions-item label="合同类别">{{ contractCategoryText(contract.contractCategory) }}</el-descriptions-item>
        <el-descriptions-item label="关联项目">{{ contract.projectName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采购方">{{ contract.purchaserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ contract.supplierName || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="group-title">金额与日期</div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="合同金额(万)">{{ formatWan(contract.amountTotal) }}</el-descriptions-item>
        <el-descriptions-item label="不含税金额(万)">{{ formatWan(contract.amountWithoutTax) }}</el-descriptions-item>
        <el-descriptions-item label="税率(%)">{{ formatTaxRate(contract.taxRateDefault) }}</el-descriptions-item>
        <el-descriptions-item label="币种">{{ contract.currency || '-' }}</el-descriptions-item>
        <el-descriptions-item label="拟定签订日期">{{ contract.signDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ contract.effectiveDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ contract.endDate || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="group-title">付款与结算</div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="结算方式">{{ settleModeText(contract.settleMode) }}</el-descriptions-item>
        <el-descriptions-item label="付款条件">{{ contract.payTerms || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="group-title">提单人和业务员</div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="提单人">{{ contract.creatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提单人岗位">{{ contract.creatorPostName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提单人电话">{{ contract.creatorMobile || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务员">{{ contract.salesmanName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务员岗位">{{ contract.salesmanPostName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务员电话">{{ contract.salesmanMobile || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="group-title">备注</div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="备注">{{ contract.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getContractDetail, type ContractVO } from '@/api/contracts/contract';
import type { ApprovalDetail } from '@/api/approval';
import { useDictLabelResolver } from '@/composables/useDictLabelResolver';

interface Props {
  busId: number | string;
  busType: string;
  procCode?: string;
  detail: ApprovalDetail | null;
}

const props = defineProps<Props>();
const loading = ref(false);
type ContractPanelVO = ContractVO & {
  contractType?: string;
  contractCategory?: string;
  amountWithoutTax?: number;
  taxRateDefault?: number;
  currency?: string;
  payTerms?: string;
  settleMode?: string;
  creatorName?: string;
  creatorPostName?: string;
  creatorMobile?: string;
  salesmanName?: string;
  salesmanPostName?: string;
  salesmanMobile?: string;
  remark?: string;
};
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

const fallbackStatusMap: Record<string, string> = {
  DRAFT: '起草中',
  UNDER_REVIEW: '审核中',
  PENDING_SIGN: '拟签',
  ARCHIVED: '正常归档',
  ARCHIVED_ABNORMAL: '异常归档',
};

const fallbackContractTypeMap: Record<string, string> = {
  ENGINEERING: '工程合同',
  PURCHASE: '采购合同',
  SERVICE: '服务合同',
};

const fallbackContractCategoryMap: Record<string, string> = {
  FRAMEWORK: '框架合同',
  ONCE: '一次性合同',
  SUBCONTRACT: '分包合同',
  CONTRACT_FUND_OUT: '资金流出类',
  CONTRACT_FUND_IN: '资金流入类',
};

const fallbackSettleModeMap: Record<string, string> = {
  MONTHLY: '按月结算',
  MILESTONE: '按节点结算',
  PAYMENT_NODE: '按节点结算',
  ONCE: '一次性结算',
  OTHER: '其他',
};

const contractTypeResolver = useDictLabelResolver({
  dictCodes: ['Contract_Type', 'contract_type'],
  fallbackOptions: [
    { label: '工程合同', value: 'ENGINEERING' },
    { label: '采购合同', value: 'PURCHASE' },
    { label: '服务合同', value: 'SERVICE' },
  ],
  fallbackLabelMap: fallbackContractTypeMap,
});

const contractCategoryResolver = useDictLabelResolver({
  dictCodes: ['Contract_Category', 'contract_category'],
  fallbackOptions: [
    { label: '框架合同', value: 'FRAMEWORK' },
    { label: '一次性合同', value: 'ONCE' },
    { label: '分包合同', value: 'SUBCONTRACT' },
    { label: '资金流出类', value: 'CONTRACT_FUND_OUT' },
    { label: '资金流入类', value: 'CONTRACT_FUND_IN' },
  ],
  fallbackLabelMap: fallbackContractCategoryMap,
});

const settleModeResolver = useDictLabelResolver({
  dictCodes: ['settlement_type', 'Settlement_Type'],
  fallbackOptions: [
    { label: '按月结算', value: 'MONTHLY' },
    { label: '按节点结算', value: 'MILESTONE' },
    { label: '按节点结算', value: 'PAYMENT_NODE' },
    { label: '一次性结算', value: 'ONCE' },
    { label: '其他', value: 'OTHER' },
  ],
  fallbackLabelMap: fallbackSettleModeMap,
});

const contractStatusResolver = useDictLabelResolver({
  dictCodes: ['Contract_Status', 'contract_status'],
  fallbackLabelMap: fallbackStatusMap,
});

function statusText(status?: string) {
  return contractStatusResolver.getLabel(status);
}

function contractTypeText(contractType?: string) {
  return contractTypeResolver.getLabel(contractType);
}

function contractCategoryText(contractCategory?: string) {
  return contractCategoryResolver.getLabel(contractCategory);
}

function settleModeText(settleMode?: string) {
  return settleModeResolver.getLabel(settleMode);
}

async function loadDictOptions() {
  await Promise.all([
    contractTypeResolver.loadOptions(),
    contractCategoryResolver.loadOptions(),
    settleModeResolver.loadOptions(),
    contractStatusResolver.loadOptions(),
  ]);
}

function formatWan(value?: number) {
  if (value == null) return '-';
  const n = Number(value);
  if (!Number.isFinite(n)) return '-';
  return (n / 10000).toFixed(2);
}

function formatTaxRate(value?: number) {
  if (value == null) return '-';
  const n = Number(value);
  if (!Number.isFinite(n)) return '-';
  return (n * 100).toFixed(2);
}

onMounted(loadDictOptions);
</script>

<style scoped lang="scss">
.contract-panel {
  min-height: 280px;
}

.group-title {
  margin: 8px 0 10px;
  font-weight: 600;
}

.contract-detail :deep(.el-descriptions) {
  margin-bottom: 12px;
}
</style>
