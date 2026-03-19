<template>
  <el-config-provider :locale="zhCn">
    <el-card v-loading="loading">
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
            <el-button
              v-if="canSign"
              size="small"
              type="primary"
              :disabled="!canManage"
              @click="openSignDialog"
            >
              操作
            </el-button>
            <el-button
              size="small"
              type="primary"
              :disabled="!canManage || !canEdit"
              @click="openEdit"
            >
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
        <el-descriptions-item label="合同金额(万)">
          {{ formatAmount(contract.amountTotal) }}
        </el-descriptions-item>
        <el-descriptions-item label="签订日期">{{ contract.signDate || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="合同信息" name="base">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="生效日期">{{ contract.effectiveDate || '-' }}</el-descriptions-item>
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

      <!-- 签订/不签订操作弹窗 -->
      <el-dialog
        v-model="signDialog.visible"
        title="合同签订操作"
        width="480px"
        destroy-on-close
        draggable
        @close="resetSignForm"
      >
        <el-form :model="signForm" label-width="100px">
          <el-form-item label="操作类型" required>
            <el-radio-group v-model="signForm.action">
              <el-radio label="SIGN">签订</el-radio>
              <el-radio label="UNSIGN">不签订</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="签订意见">
            <el-input v-model="signForm.opinion" type="textarea" :rows="3" placeholder="请输入签订意见（选填）" />
          </el-form-item>
          <el-form-item v-show="signForm.action === 'UNSIGN'" label="是否变更" required>
            <el-checkbox v-model="signForm.needChange">是，返回合同起草进行变更</el-checkbox>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="signDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="signSaving" @click="submitSign">确认</el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage } from 'element-plus';
import { getMenuLevel } from '@/utils/permission';
import { getContractDetail, signContract, type ContractStatus } from '@/api/contracts/contract';

const route = useRoute();
const router = useRouter();

const canManage = computed(() => getMenuLevel('contracts:contract') >= 3);

const statusOptions: Array<{ label: string; value: ContractStatus }> = [
  { label: '起草中', value: 'DRAFT' },
  { label: '审核中', value: 'UNDER_REVIEW' },
  { label: '拟签', value: 'PENDING_SIGN' },
  { label: '履行中', value: 'EFFECTIVE' },
  { label: '正常归档', value: 'ARCHIVED' },
  { label: '异常归档', value: 'ARCHIVED_ABNORMAL' },
  { label: '已变更', value: 'CHANGED' },
  { label: '已终止', value: 'TERMINATED' },
];

function statusText(s: ContractStatus) {
  return statusOptions.find((x) => x.value === s)?.label || s || '-';
}

function statusTagType(s: ContractStatus) {
  if (s === 'DRAFT') return 'info';
  if (s === 'UNDER_REVIEW' || s === 'PENDING_SIGN') return 'warning';
  if (s === 'EFFECTIVE') return 'success';
  if (s === 'ARCHIVED' || s === 'ARCHIVED_ABNORMAL') return 'info';
  if (s === 'TERMINATED') return 'danger';
  return '';
}

function formatAmount(val?: number) {
  if (val == null) return '-';
  const n = Number(val);
  if (!Number.isFinite(n)) return '-';
  return (n / 10000).toFixed(2);
}

const activeTab = ref('base');
const loading = ref(false);
const signSaving = ref(false);

const contract = reactive<{
  contractId?: string;
  contractCode?: string;
  contractName?: string;
  projectName?: string;
  supplierName?: string;
  amountTotal?: number;
  status?: ContractStatus;
  signDate?: string;
  effectiveDate?: string;
  endDate?: string;
  remark?: string;
}>({});

/** 仅起草中可编辑 */
const canEdit = computed(() => contract.status === 'DRAFT');

/** 仅拟签状态可进行签订/不签订操作 */
const canSign = computed(() => contract.status === 'PENDING_SIGN');

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

const signDialog = reactive({ visible: false });
const signForm = reactive({
  action: 'SIGN' as 'SIGN' | 'UNSIGN',
  opinion: '',
  needChange: false,
});

async function fetchDetail() {
  const id = route.params.contractId as string;
  if (!id) return;
  loading.value = true;
  try {
    const res = await getContractDetail(id);
    if (res) {
      Object.assign(contract, {
        contractId: String(res.contractId),
        contractCode: res.contractCode,
        contractName: res.contractName,
        projectName: res.projectName,
        supplierName: res.supplierName,
        amountTotal: res.amountTotal ?? res.amount,
        status: res.status as ContractStatus,
        signDate: res.signDate,
        effectiveDate: res.effectiveDate,
        endDate: res.endDate,
        remark: res.remark,
      });
    }
  } catch (e) {
    console.error('获取合同详情失败:', e);
    ElMessage.error((e as Error)?.message || '获取合同详情失败');
  } finally {
    loading.value = false;
  }
}

function openSignDialog() {
  signForm.action = 'SIGN';
  signForm.opinion = '';
  signForm.needChange = false;
  signDialog.visible = true;
}

function resetSignForm() {
  signForm.action = 'SIGN';
  signForm.opinion = '';
  signForm.needChange = false;
}

async function submitSign() {
  if (!contract.contractId) return;
  try {
    signSaving.value = true;
    await signContract({
      contractId: Number(contract.contractId),
      action: signForm.action,
      opinion: signForm.opinion?.trim() || undefined,
      needChange: signForm.action === 'UNSIGN' ? signForm.needChange : undefined,
    });
    ElMessage.success(
      signForm.action === 'SIGN'
        ? '签订成功，合同已正常归档'
        : signForm.needChange
          ? '已返回合同起草，可进行变更'
          : '已异常归档'
    );
    signDialog.visible = false;
    await fetchDetail();
  } catch (e) {
    ElMessage.error((e as Error)?.message || '操作失败');
  } finally {
    signSaving.value = false;
  }
}

onMounted(() => fetchDetail());

watch(
  () => route.params.contractId,
  () => fetchDetail()
);

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
