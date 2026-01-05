<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="left">
            <el-button link type="primary" @click="goBack">返回</el-button>
            <div class="title">合同详情</div>
            <el-tag :type="statusTagType(contract.status)" class="status-tag">
              {{ statusText(contract.status) }}
            </el-tag>
          </div>
          <div class="actions">
            <el-button size="small" type="primary" :disabled="!canManage" @click="openEdit">
              编辑合同
            </el-button>
            <el-button size="small" :disabled="true">操作记录</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="3" border class="summary">
        <el-descriptions-item label="合同编码">{{ contract.contractCode }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ contract.contractName }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ contract.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联项目">{{ contract.projectName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="合同金额(万)">{{ contract.amount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="签订日期">{{ contract.signDate || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="合同信息" name="base">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="开始日期">{{ contract.startDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="结束日期">{{ contract.endDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ contract.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="付款计划" name="payment">
          <el-alert title="付款计划后续可在“收付款台账”中统一维护，此处展示摘要。" type="info" show-icon />
          <el-table :data="paymentList" border style="width: 100%; margin-top: 10px">
            <el-table-column prop="stage" label="期次" min-width="100" />
            <el-table-column prop="planAmount" label="计划金额(万)" min-width="140" />
            <el-table-column prop="planDate" label="计划日期" min-width="140" />
            <el-table-column prop="paidAmount" label="已付金额(万)" min-width="140" />
            <el-table-column prop="status" label="状态" min-width="120" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="合同变更" name="change">
          <el-alert title="合同变更在“合同变更”页面可维护，本处展示与本合同关联的变更摘要。" type="warning" show-icon />
          <el-table :data="changeList" border style="width: 100%; margin-top: 10px">
            <el-table-column prop="changeCode" label="变更单号" min-width="160" />
            <el-table-column prop="reason" label="原因" min-width="220" />
            <el-table-column prop="deltaAmount" label="金额变动(万)" min-width="140" />
            <el-table-column prop="time" label="时间" min-width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="附件" name="attachments">
          <el-alert title="你选了“附件独立库”，这里展示摘要并提供跳转到合同附件库。" type="info" show-icon />
          <div class="attach-actions">
            <el-button size="small" type="primary" @click="goAttachmentLib">打开合同附件库</el-button>
            <el-button size="small" :disabled="true">上传附件</el-button>
          </div>
          <el-table :data="fileList" border style="width: 100%">
            <el-table-column prop="name" label="文件名" min-width="260" />
            <el-table-column prop="type" label="类型" min-width="140" />
            <el-table-column prop="time" label="上传时间" min-width="170" />
            <el-table-column label="操作" fixed="right" width="120">
              <template #default>
                <el-button link size="small" :disabled="true">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="日志" name="logs">
          <el-table :data="logList" border style="width: 100%">
            <el-table-column prop="time" label="时间" min-width="170" />
            <el-table-column prop="user" label="操作人" min-width="140" />
            <el-table-column prop="action" label="动作" min-width="180" />
            <el-table-column prop="remark" label="说明" min-width="240" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { hasPermission } from '@/utils/permission';

type ContractStatus = 'DRAFT' | 'PENDING' | 'EFFECTIVE' | 'CHANGED' | 'TERMINATED' | 'ARCHIVED';

const route = useRoute();
const router = useRouter();

const canManage = computed(() => hasPermission('contracts:contract:manage'));

const statusOptions: Array<{ label: string; value: ContractStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '拟签', value: 'PENDING' },
  { label: '生效', value: 'EFFECTIVE' },
  { label: '已变更', value: 'CHANGED' },
  { label: '终止', value: 'TERMINATED' },
  { label: '归档', value: 'ARCHIVED' },
];

function statusText(s: ContractStatus) {
  return statusOptions.find((x) => x.value === s)?.label || s;
}

function statusTagType(s: ContractStatus) {
  if (s === 'DRAFT') return 'info';
  if (s === 'PENDING') return 'warning';
  if (s === 'EFFECTIVE') return 'success';
  if (s === 'CHANGED') return 'warning';
  if (s === 'TERMINATED') return 'danger';
  if (s === 'ARCHIVED') return 'info';
  return '';
}

const activeTab = ref('base');

const contract = reactive({
  contractId: '',
  contractCode: '',
  contractName: '',
  projectName: '',
  supplierName: '',
  amount: 0,
  status: 'DRAFT' as ContractStatus,
  signDate: '',
  startDate: '',
  endDate: '',
  remark: '',
});

const paymentList = ref([
  { stage: '第1期', planAmount: 60, planDate: '2025-03-01', paidAmount: 30, status: '部分已付' },
  { stage: '第2期', planAmount: 40, planDate: '2025-06-01', paidAmount: 0, status: '未付款' },
]);

const changeList = ref([
  { changeCode: 'BG-2025-0001', reason: '范围调整', deltaAmount: 10, time: '2025-04-01 10:00:00' },
]);

const fileList = ref([
  { name: '合同正文.pdf', type: '合同正文', time: '2025-02-01 10:00:00' },
  { name: '补充协议.docx', type: '补充协议', time: '2025-04-01 18:00:00' },
]);

const logList = ref([
  { time: '2025-02-01 10:00:00', user: 'admin', action: '创建合同', remark: '初始化' },
  { time: '2025-02-10 09:00:00', user: '李四', action: '更新条款', remark: '示例' },
]);

onMounted(() => {
  const id = route.params.contractId as string;
  contract.contractId = id;
  contract.contractCode = `HT-2025-${String(id).padStart(4, '0')}`;
  contract.contractName = `示例合同 ${id}`;
  contract.projectName = `示例招标项目 ${(Number(id) % 6) + 1}`;
  contract.supplierName = `供应商${String.fromCharCode(65 + (Number(id) % 5))}`;
  contract.amount = 200 + Number(id);
  contract.status = statusOptions[Number(id) % statusOptions.length].value;
  contract.signDate = '2025-02-01';
  contract.startDate = '2025-02-01';
  contract.endDate = '2026-02-01';
  contract.remark = '示例备注';
});

function goBack() {
  router.push('/contracts/contract');
}

function goAttachmentLib() {
  router.push('/contracts/attachments');
}

function openEdit() {
  router.push({ path: '/contracts/contract', query: { edit: contract.contractId } });
}
</script>

<style scoped lang="scss">
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-weight: 600;
}

.status-tag {
  margin-left: 4px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary {
  margin-bottom: 12px;
}

.tabs {
  margin-top: 8px;
}

.attach-actions {
  margin: 10px 0 12px;
  display: flex;
  gap: 8px;
}
</style>


