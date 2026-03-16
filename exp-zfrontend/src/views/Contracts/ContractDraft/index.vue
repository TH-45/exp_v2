<template>
  <el-config-provider :locale="zhCn">
    <el-card v-loading="loading">
      <template #header>
        <div class="header">
          <div class="left">
            <el-button link type="primary" @click="goBack">返回</el-button>
            <div class="title">{{ pageTitle }}</div>
            <el-tag v-if="contractId" :type="statusTagType(detail.status)" class="status-tag">
              {{ statusText(detail.status) }}
            </el-tag>
          </div>
          <div class="actions">
            <template v-if="mode === 'draft'">
              <el-button type="primary" size="small" :loading="saving" @click="handleSave">保存</el-button>
              <el-button size="small" :loading="submitting" @click="handleSubmitApproval">提交审批</el-button>
            </template>
            <template v-else-if="mode === 'approval'">
              <ApprovalActionBar @approve="openApprovalDialog" @reject="openRejectDialog" />
            </template>
            <template v-else-if="mode === 'sign'">
              <SignActionBar @sign="openSignDialog" @unsign="openUnsignDialog" />
            </template>
          </div>
        </div>
      </template>

      <div class="form-container compact-layout" :class="{ readonly: isReadonly }">
        <!-- 分区1：基本信息 -->
        <div class="form-section">
          <div class="section-title">基本信息</div>
          <div class="form-grid">
            <el-form-item label="合同编号" required>
              <el-input v-model="form.contractCode" placeholder="请输入合同编号" :readonly="isReadonly" size="small" />
            </el-form-item>
            <el-form-item label="合同名称" required>
              <el-input v-model="form.contractName" placeholder="请输入合同名称" :readonly="isReadonly" size="small" />
            </el-form-item>
            <el-form-item label="合同类型" required>
              <el-select v-model="form.contractType" placeholder="请选择" clearable style="width: 100%" size="small" :disabled="isReadonly">
                <el-option v-for="opt in contractTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="合同类别" required>
              <el-select v-model="form.contractCategory" placeholder="请选择" clearable style="width: 100%" size="small" :disabled="isReadonly">
                <el-option v-for="opt in contractCategoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="关联项目" class="selector-field">
              <ProjectSelector v-model="form.project" placeholder="请选择" :disabled="isReadonly" />
            </el-form-item>
            <el-form-item label="采购方" required class="selector-field">
              <CompanySelector v-model="form.purchaser" placeholder="请选择" :disabled="isReadonly" />
            </el-form-item>
            <el-form-item label="供应商" required class="selector-field">
              <CompanySelector v-model="form.supplier" placeholder="请选择" :disabled="isReadonly" />
            </el-form-item>
          </div>
        </div>

        <!-- 分区2：金额与日期（金额类字段缩短，日期保持适中） -->
        <div class="form-section">
          <div class="section-title">金额与日期</div>
          <div class="form-grid form-grid-amount">
            <el-form-item label="合同金额(万)" required class="amount-field">
              <el-input-number v-model="form.amount" :min="0" :precision="2" size="small" :disabled="isReadonly" @change="calcTaxFields" />
            </el-form-item>
            <el-form-item label="含税">
              <el-checkbox v-model="form.isTaxIncluded" :disabled="isReadonly" @change="onTaxIncludedChange" size="small">
                含税
              </el-checkbox>
            </el-form-item>
            <el-form-item label="税率(%)" class="amount-field">
              <el-select
                v-model="form.taxRate"
                placeholder="请选择"
                clearable
                size="small"
                :disabled="!form.isTaxIncluded || isReadonly"
                @change="calcTaxFields"
              >
                <el-option v-for="opt in taxRateOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="不含税金额(万)" class="amount-field">
              <el-input
                :model-value="displayAmountWithoutTax"
                readonly
                placeholder="自动计算"
                size="small"
              />
            </el-form-item>
            <el-form-item label="税率金额(万)" class="amount-field">
              <el-input
                :model-value="displayTaxAmount"
                readonly
                placeholder="自动计算"
                size="small"
              />
            </el-form-item>
            <el-form-item label="币种" required class="amount-field">
              <el-select v-model="form.currency" placeholder="请选择" size="small" :disabled="isReadonly">
                <el-option label="人民币" value="CNY" />
                <el-option label="美元" value="USD" />
                <el-option label="欧元" value="EUR" />
              </el-select>
            </el-form-item>
            <el-form-item label="拟定签订日期" required class="date-field">
              <el-date-picker
                v-model="form.signDate"
                type="date"
                value-format="YYYY-MM-DD"
                size="small"
                :disabled="isReadonly"
                :disabled-date="(d) => disabledDateForSign(d, null)"
              />
            </el-form-item>
            <el-form-item label="生效日期" required class="date-field">
              <el-date-picker
                v-model="form.effectiveDate"
                type="date"
                value-format="YYYY-MM-DD"
                size="small"
                :disabled="isReadonly"
                :disabled-date="(d) => disabledDateForEffective(d, form.signDate)"
              />
            </el-form-item>
            <el-form-item label="结束日期" class="date-field">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                value-format="YYYY-MM-DD"
                size="small"
                :disabled="isReadonly"
                :disabled-date="(d) => disabledDateForEnd(d, form.effectiveDate)"
              />
            </el-form-item>
          </div>
        </div>

        <!-- 分区3：付款与结算（结算方式在上，付款条件在下支持多行） -->
        <div class="form-section">
          <div class="section-title">付款与结算</div>
          <div class="form-grid">
            <el-form-item label="结算方式" required class="settle-field">
              <el-select v-model="form.settleMode" placeholder="请选择" clearable size="small" :disabled="isReadonly">
                <el-option v-for="opt in settleModeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="付款条件" class="full-width">
              <el-input v-model="form.payTerms" type="textarea" :rows="2" placeholder="如：预付款+进度款+尾款" :readonly="isReadonly" size="small" />
            </el-form-item>
          </div>
        </div>

        <!-- 分区4：合同附件（仅拟签-签订时展示） -->
        <div v-if="mode === 'sign'" class="form-section">
          <div class="section-title">合同附件</div>
          <div class="form-grid">
            <el-form-item label="合同正文" class="full-width">
              <span class="readonly-text">签订时在弹窗中上传</span>
            </el-form-item>
            <el-form-item label="合同相关附件" class="full-width">
              <span class="readonly-text">签订时在弹窗中上传</span>
            </el-form-item>
          </div>
        </div>

        <!-- 分区5：备注 -->
        <div class="form-section">
          <div class="section-title">备注</div>
          <div class="form-grid">
            <el-form-item label="备注" class="full-width">
              <el-input v-model="form.remark" type="textarea" :rows="1" placeholder="选填" :readonly="isReadonly" size="small" />
            </el-form-item>
          </div>
        </div>

        <!-- 分区6：提单人信息、业务员信息（创建时间：保存后或流转时显示） -->
        <div class="form-section">
          <div class="section-title">提单人信息、业务员信息</div>
          <div class="form-grid">
            <el-form-item label="提单人">
              <span class="readonly-text">{{ creatorDisplay }}</span>
            </el-form-item>
            <el-form-item label="业务员" required class="selector-field">
              <template v-if="!isReadonly">
                <PersonSelector v-model="form.salesman" placeholder="请选择业务员" />
              </template>
              <span v-else class="readonly-text">{{ salesmanDisplay }}</span>
            </el-form-item>
            <el-form-item v-if="contractId && detail.createdTime" label="创建时间">
              <span class="readonly-text">{{ detail.createdTime || '-' }}</span>
            </el-form-item>
          </div>
        </div>
      </div>

      <!-- 弹窗 -->
      <ApprovalDialog
        v-model="approvalDialogVisible"
        :task-id="currentTaskId"
        @confirm="handleApprovalConfirm"
      />
      <RejectDialog
        v-model="rejectDialogVisible"
        :task-id="currentTaskId"
        @confirm="handleRejectConfirm"
      />
      <SignContractDialog
        v-model="signDialogVisible"
        :contract-id="contractId"
        @confirm="handleSignConfirm"
      />
      <UnsignContractDialog
        v-model="unsignDialogVisible"
        @confirm="handleUnsignConfirm"
      />
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import {
  getContractDetail,
  createContract,
  updateContract,
  signContract,
  updateContractStatusAfterProcessStart,
  type CreateContractReq,
  type UpdateContractReq,
  type ContractStatus,
} from '@/api/contracts/contract';
import { startProcess, approveTask, rejectTask, listApprovalTasks } from '@/api/approval';
import { uploadBiddingAttachments, type CreateAttachmentBizReq } from '@/api/bidding/attachments';
import { listDictOptions, type DictOption } from '@/api/system/dict';
import type { CompanySelectorValue } from '@/api/enterprise/company';
import type { ProjectVO } from '@/api/corpProject/project';
import type { ExpPersonVO } from '@/api/system/person';
import ProjectSelector from '@/components/Selector/ProjectSelector.vue';
import CompanySelector from '@/components/Selector/CompanySelector.vue';
import PersonSelector from '@/components/Selector/PersonSelector.vue';
import ApprovalActionBar from '@/components/Approval/ApprovalActionBar.vue';
import SignActionBar from '@/components/Contract/SignActionBar.vue';
import ApprovalDialog from '@/components/Approval/ApprovalDialog.vue';
import RejectDialog from '@/components/Approval/RejectDialog.vue';
import SignContractDialog from '@/components/Contract/SignContractDialog.vue';
import UnsignContractDialog from '@/components/Contract/UnsignContractDialog.vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const PROC_CODE = 'FUND_OUT_CONTRACT_SIGN';

const contractId = computed(() => {
  const id = route.params.contractId;
  return id && id !== 'create' ? Number(id) : undefined;
});

const loading = ref(false);
const saving = ref(false);
const submitting = ref(false);

const detail = reactive<{
  status?: ContractStatus;
  createdTime?: string;
  creatorName?: string;
  creatorPostName?: string;
  creatorMobile?: string;
  salesmanName?: string;
  salesmanPostName?: string;
  salesmanMobile?: string;
  [key: string]: unknown;
}>({});

const form = reactive({
  contractCode: '',
  contractName: '',
  contractType: '',
  contractCategory: '',
  project: undefined as ProjectVO | undefined,
  purchaser: undefined as CompanySelectorValue | undefined,
  supplier: undefined as CompanySelectorValue | undefined,
  amount: 0,
  amountWithoutTax: undefined as number | undefined,
  taxRate: undefined as string | undefined,
  isTaxIncluded: false,
  currency: 'CNY',
  signDate: '',
  effectiveDate: '',
  endDate: '',
  payTerms: '',
  settleMode: '',
  remark: '',
  salesman: undefined as ExpPersonVO | undefined,
});

/** 常规税率选项（%） */
const taxRateOptions: DictOption[] = [
  { label: '3%', value: '3' },
  { label: '6%', value: '6' },
  { label: '9%', value: '9' },
  { label: '13%', value: '13' },
];

const contractTypeOptions = ref<DictOption[]>([]);
const contractCategoryOptions = ref<DictOption[]>([]);
const settleModeOptions = ref<DictOption[]>([]);

const mode = computed<'draft' | 'approval' | 'sign'>(() => {
  const s = detail.status;
  if (s === 'UNDER_REVIEW') return 'approval';
  if (s === 'PENDING_SIGN') return 'sign';
  return 'draft';
});

const isReadonly = computed(() => mode.value !== 'draft');

const pageTitle = computed(() => {
  if (mode.value === 'approval') return '合同审批';
  if (mode.value === 'sign') return '合同拟签';
  return contractId.value ? '合同起草' : '新增合同';
});

const creatorDisplay = computed(() => {
  if (contractId.value && detail.creatorName) {
    const parts = [detail.creatorName];
    if (detail.creatorPostName) parts.push(detail.creatorPostName);
    if (detail.creatorMobile) parts.push(detail.creatorMobile);
    return parts.join(' | ') || '-';
  }
  return `${userStore.username || '-'}（当前登录人）`;
});

const salesmanDisplay = computed(() => {
  if (detail.salesmanName) {
    const parts = [detail.salesmanName];
    if (detail.salesmanPostName) parts.push(detail.salesmanPostName);
    if (detail.salesmanMobile) parts.push(detail.salesmanMobile);
    return parts.join(' | ') || '-';
  }
  return '-';
});

/** 不含税金额显示（含税时自动计算：合同金额/(1+税率%)） */
const displayAmountWithoutTax = computed(() => {
  if (!form.isTaxIncluded || !form.taxRate || form.amount <= 0) return '';
  const rate = Number(form.taxRate) / 100;
  const val = form.amount / (1 + rate);
  return val.toFixed(2);
});

/** 税率金额显示（税额 = 合同金额 - 不含税金额） */
const displayTaxAmount = computed(() => {
  if (!form.isTaxIncluded || !form.taxRate || form.amount <= 0) return '';
  const rate = Number(form.taxRate) / 100;
  const withoutTax = form.amount / (1 + rate);
  const tax = form.amount - withoutTax;
  return tax.toFixed(2);
});

function calcTaxFields() {
  // 计算逻辑由 computed 处理，此处仅触发依赖更新
}

function onTaxIncludedChange() {
  if (!form.isTaxIncluded) {
    form.taxRate = undefined;
  }
}

/** 拟定签订日期：无限制 */
function disabledDateForSign(_d: Date, _signDate: string | null) {
  return false;
}

/** 生效日期：须晚于拟定签订日期 */
function disabledDateForEffective(d: Date, signDate: string | null) {
  if (!signDate) return false;
  const sign = new Date(signDate);
  sign.setHours(0, 0, 0, 0);
  const target = new Date(d);
  target.setHours(0, 0, 0, 0);
  return target.getTime() <= sign.getTime();
}

/** 结束日期：须晚于生效日期 */
function disabledDateForEnd(d: Date, effectiveDate: string | null) {
  if (!effectiveDate) return false;
  const eff = new Date(effectiveDate);
  eff.setHours(0, 0, 0, 0);
  const target = new Date(d);
  target.setHours(0, 0, 0, 0);
  return target.getTime() <= eff.getTime();
}

const currentTaskId = ref<number>();
const approvalDialogVisible = ref(false);
const rejectDialogVisible = ref(false);
const signDialogVisible = ref(false);
const unsignDialogVisible = ref(false);

const statusOptions: Array<{ label: string; value: ContractStatus }> = [
  { label: '起草中', value: 'DRAFT' },
  { label: '审核中', value: 'UNDER_REVIEW' },
  { label: '拟签', value: 'PENDING_SIGN' },
  { label: '正常归档', value: 'ARCHIVED' },
  { label: '异常归档', value: 'ARCHIVED_ABNORMAL' },
];

function statusText(s?: ContractStatus) {
  return statusOptions.find((x) => x.value === s)?.label || s || '-';
}

function statusTagType(s?: ContractStatus) {
  if (s === 'DRAFT') return 'info';
  if (s === 'UNDER_REVIEW' || s === 'PENDING_SIGN') return 'warning';
  return 'info';
}

async function loadDictOptions() {
  for (const code of ['Contract_Type', 'contract_type']) {
    try {
      const res = await listDictOptions(code);
      const opts = Array.isArray(res) ? res : (res as { data?: DictOption[] })?.data ?? [];
      if (opts.length) {
        contractTypeOptions.value = opts;
        break;
      }
    } catch {
      contractTypeOptions.value = [
        { label: '工程合同', value: 'ENGINEERING' },
        { label: '采购合同', value: 'PURCHASE' },
        { label: '服务合同', value: 'SERVICE' },
      ];
      break;
    }
  }
  for (const code of ['Contract_Category', 'contract_category']) {
    try {
      const res = await listDictOptions(code);
      const opts = Array.isArray(res) ? res : (res as { data?: DictOption[] })?.data ?? [];
      if (opts.length) {
        contractCategoryOptions.value = opts;
        break;
      }
    } catch {
      contractCategoryOptions.value = [
        { label: '框架合同', value: 'FRAMEWORK' },
        { label: '一次性合同', value: 'ONCE' },
        { label: '分包合同', value: 'SUBCONTRACT' },
      ];
      break;
    }
  }
  // 结算方式从字典 settlement_type 获取
  for (const code of ['settlement_type', 'Settlement_Type']) {
    try {
      const res = await listDictOptions(code);
      const opts = Array.isArray(res) ? res : (res as { data?: DictOption[] })?.data ?? [];
      if (opts.length) {
        settleModeOptions.value = opts;
        break;
      }
    } catch {
      /* ignore */
    }
  }
  if (settleModeOptions.value.length === 0) {
    settleModeOptions.value = [
      { label: '按月结算', value: 'MONTHLY' },
      { label: '按节点结算', value: 'MILESTONE' },
      { label: '一次性结算', value: 'ONCE' },
      { label: '其他', value: 'OTHER' },
    ];
  }
}

async function resolveTaskId(overrideId?: number) {
  if (currentTaskId.value) return;
  const id = overrideId ?? contractId.value;
  if (!id || detail.status !== 'UNDER_REVIEW') return;
  try {
    const res = await listApprovalTasks({
      tab: 'todo',
      pageNum: 1,
      pageSize: 20,
      keyword: String(id),
    });
    const task = (res?.list ?? []).find(
      (t) => t.busId === String(id) && t.isDone === 0
    );
    if (task) currentTaskId.value = task.taskId;
  } catch {
    // ignore
  }
}

async function fetchDetail(overrideId?: number) {
  const id = overrideId ?? contractId.value;
  if (!id) return;
  loading.value = true;
  try {
    const res = await getContractDetail(id);
    Object.assign(detail, res);
    form.contractCode = res.contractCode || '';
    form.contractName = res.contractName || '';
    form.contractType = res.contractType || '';
    form.contractCategory = res.contractCategory || '';
    form.project = res.projectId && (res as { projectName?: string }).projectName
      ? { projectId: Number(res.projectId), projectName: (res as { projectName?: string }).projectName }
      : undefined;
    form.purchaser = res.purchaserId && (res as { purchaserName?: string }).purchaserName
      ? { companyId: Number(res.purchaserId), companyName: (res as { purchaserName?: string }).purchaserName }
      : undefined;
    form.supplier = res.supplierId && (res as { supplierName?: string }).supplierName
      ? { companyId: Number(res.supplierId), companyName: (res as { supplierName?: string }).supplierName }
      : undefined;
    form.amount = Number((res.amountTotal ?? (res as { amount?: number }).amount ?? 0)) / 10000 || 0;
    form.amountWithoutTax = res.amountWithoutTax != null ? Number(res.amountWithoutTax) / 10000 : undefined;
    const taxPct = res.taxRateDefault != null ? Number(res.taxRateDefault) * 100 : undefined;
    form.taxRate = taxPct != null ? String(Math.round(taxPct * 100) / 100) : undefined;
    form.isTaxIncluded = !!form.taxRate;
    form.currency = (res as { currency?: string }).currency || 'CNY';
    form.signDate = (res as { signDate?: string }).signDate || '';
    form.effectiveDate = (res as { effectiveDate?: string }).effectiveDate || '';
    form.endDate = (res as { endDate?: string }).endDate || '';
    form.payTerms = (res as { payTerms?: string }).payTerms || '';
    form.settleMode = (res as { settleMode?: string }).settleMode || '';
    form.remark = (res as { remark?: string }).remark || '';
    form.salesman = (res as { salesmanPersonId?: number; salesmanName?: string }).salesmanPersonId
      ? {
          personId: (res as { salesmanPersonId?: number }).salesmanPersonId!,
          personName: (res as { salesmanName?: string }).salesmanName || '',
          personCode: '',
        } as ExpPersonVO
      : undefined;
  } catch (e) {
    ElMessage.error((e as Error)?.message || '获取合同详情失败');
  } finally {
    loading.value = false;
  }
  await resolveTaskId(id);
}

function openApprovalDialog() {
  currentTaskId.value = Number(route.query.taskId) || undefined;
  if (!currentTaskId.value) {
    ElMessage.warning('缺少任务ID，请从待办进入');
    return;
  }
  approvalDialogVisible.value = true;
}

function openRejectDialog() {
  currentTaskId.value = Number(route.query.taskId) || undefined;
  if (!currentTaskId.value) {
    ElMessage.warning('缺少任务ID，请从待办进入');
    return;
  }
  rejectDialogVisible.value = true;
}

function openSignDialog() {
  signDialogVisible.value = true;
}

function openUnsignDialog() {
  unsignDialogVisible.value = true;
}

async function handleApprovalConfirm(payload: { taskId: number; comments: string }) {
  try {
    await approveTask({ taskId: payload.taskId, comments: payload.comments });
    ElMessage.success('流转成功');
    await fetchDetail();
    if (detail.status !== 'UNDER_REVIEW' && detail.status !== 'PENDING_SIGN') {
      router.push('/contracts/contract');
    }
  } catch (e) {
    ElMessage.error((e as Error)?.message || '审批失败');
  }
}

async function handleRejectConfirm(payload: { taskId: number; comments: string }) {
  try {
    await rejectTask({ taskId: payload.taskId, comments: payload.comments });
    ElMessage.success('驳回成功');
    await fetchDetail();
    router.push('/contracts/contract');
  } catch (e) {
    ElMessage.error((e as Error)?.message || '驳回失败');
  }
}

async function handleSignConfirm(payload: {
  opinion?: string;
  mainFiles: File[];
  attachFiles: File[];
}) {
  if (!contractId.value) return;
  try {
    const bizMain: CreateAttachmentBizReq = {
      businessType: 'CONTRACT',
      businessId: contractId.value,
      fileType: 'CONTRACT_MAIN',
      fileCategory: 'CONTRACT_MAIN',
    };
    await uploadBiddingAttachments(
      payload.mainFiles,
      payload.mainFiles.map(() => ({ ...bizMain }))
    );
    if (payload.attachFiles.length > 0) {
      const bizAttach: CreateAttachmentBizReq = {
        businessType: 'CONTRACT',
        businessId: contractId.value,
        fileType: 'CONTRACT_ATTACHMENT',
        fileCategory: 'CONTRACT_ATTACHMENT',
      };
      await uploadBiddingAttachments(
        payload.attachFiles,
        payload.attachFiles.map(() => ({ ...bizAttach }))
      );
    }
    await signContract({
      contractId: contractId.value,
      action: 'SIGN',
      opinion: payload.opinion,
    });
    ElMessage.success('签订成功，合同已正常归档');
    router.push('/contracts/contract');
  } catch (e) {
    ElMessage.error((e as Error)?.message || '签订失败');
  }
}

async function handleUnsignConfirm(payload: { opinion?: string; needChange: boolean }) {
  if (!contractId.value) return;
  try {
    await signContract({
      contractId: contractId.value,
      action: 'UNSIGN',
      opinion: payload.opinion,
      needChange: payload.needChange,
    });
    ElMessage.success(
      payload.needChange ? '已返回合同起草，可进行变更' : '已异常归档'
    );
    router.push('/contracts/contract');
  } catch (e) {
    ElMessage.error((e as Error)?.message || '操作失败');
  }
}

function validateBeforeSave(): boolean {
  if (!form.contractCode?.trim()) {
    ElMessage.warning('请输入合同编号');
    return false;
  }
  if (!form.contractName?.trim()) {
    ElMessage.warning('请输入合同名称');
    return false;
  }
  if (!form.contractType) {
    ElMessage.warning('请选择合同类型');
    return false;
  }
  if (!form.contractCategory) {
    ElMessage.warning('请选择合同类别');
    return false;
  }
  if (!form.purchaser?.companyId) {
    ElMessage.warning('请选择采购方');
    return false;
  }
  if (!form.supplier?.companyId) {
    ElMessage.warning('请选择供应商');
    return false;
  }
  if (!form.amount || form.amount <= 0) {
    ElMessage.warning('请输入合同金额');
    return false;
  }
  if (!form.currency) {
    ElMessage.warning('请选择币种');
    return false;
  }
  if (!form.signDate) {
    ElMessage.warning('请选择拟定签订日期');
    return false;
  }
  if (!form.effectiveDate) {
    ElMessage.warning('请选择生效日期');
    return false;
  }
  if (form.effectiveDate && form.signDate && form.effectiveDate <= form.signDate) {
    ElMessage.warning('生效日期须大于拟定签订日期');
    return false;
  }
  if (form.endDate && form.effectiveDate && form.endDate <= form.effectiveDate) {
    ElMessage.warning('结束日期须大于生效日期');
    return false;
  }
  if (!form.salesman?.personId) {
    ElMessage.warning('请选择业务员');
    return false;
  }
  if (!form.settleMode) {
    ElMessage.warning('请选择结算方式');
    return false;
  }
  return true;
}

async function handleSave() {
  if (!validateBeforeSave()) return;

  saving.value = true;
  try {
    const amountTotal = Math.round((form.amount || 0) * 10000);
    // 含税时：不含税金额 = 合同金额/(1+税率%)
    let amountWithoutTax: number | undefined;
    if (form.isTaxIncluded && form.taxRate && form.amount > 0) {
      const rate = Number(form.taxRate) / 100;
      amountWithoutTax = Math.round((form.amount / (1 + rate)) * 10000);
    } else {
      amountWithoutTax = undefined;
    }
    const taxRateDefault = form.taxRate != null ? Number(form.taxRate) / 100 : undefined;

    if (contractId.value) {
      const req: UpdateContractReq = {
        contractId: contractId.value,
        contractCode: form.contractCode,
        contractName: form.contractName,
        contractType: form.contractType || undefined,
        contractCategory: form.contractCategory || undefined,
        projectId: form.project?.projectId,
        purchaserId: form.purchaser?.companyId,
        supplierId: form.supplier?.companyId,
        amountTotal,
        amountWithoutTax,
        taxRateDefault,
        currency: form.currency,
        signDate: form.signDate || undefined,
        effectiveDate: form.effectiveDate || undefined,
        endDate: form.endDate || undefined,
        payTerms: form.payTerms || undefined,
        settleMode: form.settleMode || undefined,
        remark: form.remark || undefined,
        salesmanPersonId: form.salesman?.personId,
      };
      await updateContract(req);
      ElMessage.success('保存成功');
    } else {
      const req: CreateContractReq = {
        contractCode: form.contractCode,
        contractName: form.contractName,
        contractType: form.contractType || undefined,
        contractCategory: form.contractCategory || undefined,
        projectId: form.project?.projectId,
        purchaserId: form.purchaser?.companyId,
        supplierId: form.supplier!.companyId,
        amountTotal,
        amountWithoutTax,
        taxRateDefault,
        currency: form.currency,
        signDate: form.signDate || undefined,
        effectiveDate: form.effectiveDate || undefined,
        endDate: form.endDate || undefined,
        payTerms: form.payTerms || undefined,
        settleMode: form.settleMode || undefined,
        remark: form.remark || undefined,
        salesmanPersonId: form.salesman?.personId,
      };
      const res = await createContract(req);
      const id = res?.contractId ? Number(res.contractId) : (res as { contractId?: number })?.contractId;
      ElMessage.success('保存成功');
      if (id) router.replace(`/contracts/contract/${id}`);
    }
    await fetchDetail();
  } catch (e) {
    ElMessage.error((e as Error)?.message || '保存失败');
  } finally {
    saving.value = false;
  }
}

async function handleSubmitApproval() {
  if (!validateBeforeSave()) return;
  try {
    await ElMessageBox.confirm(
      '确认将合同提交审批吗？提交后将进入合同审批流程。',
      '提交审批',
      { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' }
    );
  } catch {
    return;
  }
  submitting.value = true;
  try {
    let id = contractId.value;
    // 未保存时先保存（创建或更新），再提交
    if (!id) {
      const amountTotal = Math.round((form.amount || 0) * 10000);
      let amountWithoutTax: number | undefined;
      if (form.isTaxIncluded && form.taxRate && form.amount > 0) {
        const rate = Number(form.taxRate) / 100;
        amountWithoutTax = Math.round((form.amount / (1 + rate)) * 10000);
      }
      const taxRateDefault = form.taxRate != null ? Number(form.taxRate) / 100 : undefined;
      const req: CreateContractReq = {
        contractCode: form.contractCode,
        contractName: form.contractName,
        contractType: form.contractType || undefined,
        contractCategory: form.contractCategory || undefined,
        projectId: form.project?.projectId,
        purchaserId: form.purchaser?.companyId,
        supplierId: form.supplier!.companyId,
        amountTotal,
        amountWithoutTax,
        taxRateDefault,
        currency: form.currency,
        signDate: form.signDate || undefined,
        effectiveDate: form.effectiveDate || undefined,
        endDate: form.endDate || undefined,
        payTerms: form.payTerms || undefined,
        settleMode: form.settleMode || undefined,
        remark: form.remark || undefined,
        salesmanPersonId: form.salesman?.personId,
      };
      const res = await createContract(req);
      id = res?.contractId ? Number(res.contractId) : (res as { contractId?: number })?.contractId;
      if (id) {
        router.replace(`/contracts/contract/${id}`);
        await fetchDetail(id);
      }
    }
    if (id) {
      await startProcess({ procCode: PROC_CODE, busId: String(id) });
      await updateContractStatusAfterProcessStart(id);
      ElMessage.success('已提交审批');
      await fetchDetail();
    }
  } catch (e) {
    ElMessage.error((e as Error)?.message || '提交失败');
  } finally {
    submitting.value = false;
  }
}

function goBack() {
  router.push('/contracts/contract');
}

onMounted(async () => {
  await loadDictOptions();
  currentTaskId.value = Number(route.query.taskId) || undefined;
  if (contractId.value) {
    await fetchDetail();
  }
});

watch(
  () => route.params.contractId,
  () => fetchDetail()
);
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
  gap: 8px;
}

.form-container {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  padding-right: 2px;

  /* 缩小滚动条宽度 */
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    border-radius: 3px;
    background: var(--el-border-color-darker);
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &.readonly {
    :deep(.el-input__inner),
    :deep(.el-textarea__inner) {
      user-select: text;
    }
  }
}

/* 紧凑布局：缩小间距、4列网格，一屏展示更多内容 */
.compact-layout .form-section {
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.compact-layout .section-title {
  font-weight: 600;
  font-size: 12px;
  margin-bottom: 6px;
  padding-left: 6px;
  border-left: 3px solid var(--el-color-primary);
}
.compact-layout .form-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px 12px;
}
.compact-layout .form-grid :deep(.el-form-item) {
  margin-bottom: 6px;
}
.compact-layout .form-grid .full-width {
  grid-column: 1 / -1;
}

/* 关联项目、采购方、供应商、业务员：限制宽度与其他输入框一致 */
.compact-layout .form-grid .selector-field :deep(.el-form-item__content) {
  max-width: 160px;
}
.compact-layout .form-grid .selector-field :deep(.el-input),
.compact-layout .form-grid .selector-field :deep(.selector-input) {
  max-width: 160px;
}

/* 金额与日期：金额类字段缩短 */
.compact-layout .form-grid-amount .amount-field :deep(.el-form-item__content) {
  max-width: 130px;
}
.compact-layout .form-grid-amount .amount-field :deep(.el-input-number),
.compact-layout .form-grid-amount .amount-field :deep(.el-input),
.compact-layout .form-grid-amount .amount-field :deep(.el-select) {
  width: 100%;
  max-width: 130px;
}
.compact-layout .form-grid-amount .date-field :deep(.el-form-item__content) {
  max-width: 150px;
}
.compact-layout .form-grid-amount .date-field :deep(.el-date-editor) {
  width: 100%;
  max-width: 150px;
}

/* 结算方式：适中宽度 */
.compact-layout .form-grid .settle-field :deep(.el-form-item__content) {
  max-width: 160px;
}

.readonly-text {
  color: var(--el-text-color-regular);
}
</style>
