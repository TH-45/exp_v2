<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">合同台账</div>
          <div class="actions">
            <el-button type="primary" size="small" @click="goCreate" :disabled="!canManage">
              新增合同
            </el-button>
            <el-button size="small" :disabled="true">导入</el-button>
            <el-button size="small" :disabled="true">导出</el-button>
          </div>
        </div>
      </template>

      <!-- 查询栏：主筛选 + 高级筛选 -->
      <el-form :model="query" class="search-bar" @submit.prevent>
        <div class="search-row search-row-primary">
          <el-form-item label="合同名称" class="search-item search-item-keyword">
            <el-input v-model="query.contractName" placeholder="请输入合同名称" clearable />
          </el-form-item>
          <el-form-item label="关联项目" class="search-item search-item-keyword">
            <el-input v-model="query.projectName" placeholder="请输入项目名称" clearable />
          </el-form-item>
          <el-form-item label="状态" class="search-item search-item-status">
            <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <div class="search-actions">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button link type="primary" @click="toggleAdvancedSearch">
              {{ advancedSearchVisible ? '收起高级筛选' : '展开高级筛选' }}
            </el-button>
            <span v-if="activeFilterCount > 0" class="filter-summary">已筛选 {{ activeFilterCount }} 项</span>
          </div>
        </div>
        <div v-show="advancedSearchVisible" class="search-row search-row-advanced">
          <el-form-item label="合同编号" class="search-item search-item-keyword">
            <el-input v-model="query.contractCode" placeholder="请输入合同编号" clearable />
          </el-form-item>
          <el-form-item label="合同金额(万)" class="search-item search-item-amount">
            <el-input-number v-model="query.amountMin" :min="0" :precision="2" placeholder="最小" clearable style="width: 120px" />
            <span class="amount-sep">-</span>
            <el-input-number v-model="query.amountMax" :min="0" :precision="2" placeholder="最大" clearable style="width: 120px" />
          </el-form-item>
          <el-form-item label="签订日期" class="search-item search-item-date">
            <el-date-picker
              v-model="signDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item label="合作方类型" class="search-item search-item-status">
            <el-select v-model="query.partnerType" clearable placeholder="全部" style="width: 120px">
              <el-option v-for="opt in partnerTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="合作方" class="search-item search-item-keyword">
            <el-input v-model="query.partnerName" placeholder="请输入合作方名称" clearable />
          </el-form-item>
        </div>
      </el-form>

      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="contractId"
        border
        style="width: 100%"
        @row-dblclick="(row: ContractVO) => goDetail(row)"
      >
        <el-table-column prop="contractCode" label="合同编号" min-width="160" />
        <el-table-column prop="contractName" label="合同名称" min-width="220" />
        <el-table-column prop="projectName" label="关联项目" min-width="200" />
        <el-table-column label="合作方" min-width="220">
          <template #default="{ row }">
            <div class="partner-cell">
              <span v-if="row.purchaserName" class="partner-purchaser">甲方：{{ row.purchaserName }}</span>
              <span v-if="row.supplierName" class="partner-supplier">供应商：{{ row.supplierName }}</span>
              <span v-if="!row.purchaserName && !row.supplierName" class="partner-empty">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="合同金额(万)" min-width="140">
          <template #default="{ row }">
            {{ formatAmount(row.amountTotal ?? row.amount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signDate" label="签订日期" min-width="120" />
        <el-table-column prop="createdTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="340">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="openSignDialog(row)"
              :disabled="!canManage || !canSign(row)"
            >
              操作
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="goEdit(row)"
              :disabled="!canManage || !canEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="handleSubmitApproval(row)"
              :disabled="!canSubmitApproval(row)"
            >
              提交审批
            </el-button>
            <el-button link size="small" @click="goDetail(row)">详情</el-button>
            <el-button link type="danger" size="small" @click="deleteById(row)" :disabled="!canManage || !canEdit(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>

      <!-- 新增/编辑弹窗 -->
      <el-dialog
        v-model="editDialog.visible"
        :title="editDialog.isEdit ? '编辑合同' : '新增合同'"
        width="900px"
        destroy-on-close
        draggable
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          class="dialog-form two-col"
          @submit.prevent="submitForm"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="合同编号" prop="contractCode">
            <el-input v-model="form.contractCode" placeholder="请输入合同编号" :disabled="editDialog.isEdit" />
          </el-form-item>
          <el-form-item label="合同名称" prop="contractName">
            <el-input v-model="form.contractName" placeholder="请输入合同名称" />
          </el-form-item>
          <el-form-item v-if="!editDialog.isEdit" label="合同类型" prop="contractType" required>
            <el-select v-model="form.contractType" placeholder="请选择" clearable style="width: 100%">
              <el-option v-for="opt in contractTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="!editDialog.isEdit" label="合同类别" prop="contractCategory" required>
            <el-select v-model="form.contractCategory" placeholder="请选择" clearable style="width: 100%">
              <el-option v-for="opt in contractCategoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="关联项目" prop="project">
            <ProjectSelector v-model="form.project" placeholder="请选择关联项目" />
          </el-form-item>
          <el-form-item label="供应商" prop="supplier">
            <CompanySelector v-model="form.supplier" placeholder="请选择供应商" />
          </el-form-item>
          <el-form-item label="合同金额(万)" prop="amount">
            <el-input-number v-model="form.amount" :min="0" :max="999999999" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="币种" prop="currency">
            <el-select v-model="form.currency" placeholder="请选择币种" clearable style="width: 100%">
              <el-option label="人民币" value="CNY" />
              <el-option label="美元" value="USD" />
            </el-select>
          </el-form-item>
          <el-form-item label="签订日期" prop="signDate">
            <el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="生效日期" prop="effectiveDate">
            <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="结束日期" prop="endDate">
            <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="备注" class="full-row">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选" @keydown.enter.stop />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">确认</el-button>
        </template>
      </el-dialog>

      <!-- 签订/不签订操作弹窗 -->
      <el-dialog
        v-model="signDialog.visible"
        title="合同签订操作"
        width="480px"
        destroy-on-close
        draggable
        @close="resetSignForm"
      >
        <el-form ref="signFormRef" :model="signForm" label-width="100px">
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
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { useRouter, useRoute } from 'vue-router';
import { getMenuLevel } from '@/utils/permission';
import {
  queryContractList,
  getContractDetail,
  createContractBusiness,
  updateContract,
  deleteContract,
  updateContractStatusAfterProcessStart,
  signContract,
  type ContractVO,
  type ContractStatus,
  type CreateContractReq,
  type UpdateContractReq,
} from '@/api/contracts/contract';
import { listDictOptions, type DictOption } from '@/api/system/dict';
import type { CompanySelectorValue } from '@/api/enterprise/company';
import type { ProjectVO } from '@/api/corpProject/project';
import CompanySelector from '@/components/Selector/CompanySelector.vue';
import ProjectSelector from '@/components/Selector/ProjectSelector.vue';

const router = useRouter();
const route = useRoute();
const canManage = computed(() => getMenuLevel('contracts:contract') >= 3);

/** 合作方类型字典（Partner_Type：1-甲方，2-供应商） */
const partnerTypeOptions = ref<DictOption[]>([]);
/** 合同类型、合同类别（用于新增弹窗必填） */
const contractTypeOptions = ref<DictOption[]>([]);
const contractCategoryOptions = ref<DictOption[]>([]);

async function loadPartnerTypeOptions() {
  try {
    const res = await listDictOptions('Partner_Type');
    partnerTypeOptions.value = Array.isArray(res) ? res : (res as { data?: DictOption[] })?.data ?? [];
  } catch {
    partnerTypeOptions.value = [
      { label: '甲方', value: '1' },
      { label: '供应商', value: '2' },
    ];
  }
}

async function loadContractTypeAndCategoryOptions() {
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
}

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
  return statusOptions.find((x) => x.value === s)?.label || s;
}

function statusTagType(s: ContractStatus) {
  if (s === 'DRAFT') return 'info';
  if (s === 'UNDER_REVIEW') return 'warning';
  if (s === 'PENDING_SIGN') return 'warning';
  if (s === 'EFFECTIVE') return 'success';
  if (s === 'ARCHIVED' || s === 'ARCHIVED_ABNORMAL') return 'info';
  if (s === 'TERMINATED') return 'danger';
  return '';
}

/** 仅起草中可编辑、删除 */
function canEdit(row: ContractVO) {
  return row.status === 'DRAFT';
}

/** 仅起草中可提交审批 */
function canSubmitApproval(row: ContractVO) {
  return row.status === 'DRAFT';
}

/** 仅拟签状态可进行签订/不签订操作 */
function canSign(row: ContractVO) {
  return row.status === 'PENDING_SIGN';
}

function formatAmount(val?: number) {
  if (val == null) return '';
  const n = Number(val);
  if (!Number.isFinite(n)) return '';
  return (n / 10000).toFixed(2);
}

const loading = ref(false);
const saving = ref(false);
const signSaving = ref(false);
const total = ref(0);
const advancedSearchVisible = ref(false);

const query = reactive({
  contractCode: '',
  contractName: '',
  projectName: '',
  partnerType: '' as string,
  partnerName: '',
  amountMin: undefined as number | undefined,
  amountMax: undefined as number | undefined,
  signDateStart: '',
  signDateEnd: '',
  status: undefined as ContractStatus | undefined,
  pageNum: 1,
  pageSize: 10,
});

/** 签订日期范围（用于 el-date-picker 双向绑定） */
const signDateRange = computed({
  get: () => (query.signDateStart && query.signDateEnd ? [query.signDateStart, query.signDateEnd] : null),
  set: (val: string[] | null) => {
    if (val && val.length === 2) {
      query.signDateStart = val[0] ?? '';
      query.signDateEnd = val[1] ?? '';
    } else {
      query.signDateStart = '';
      query.signDateEnd = '';
    }
  },
});

const activeFilterCount = computed(() => {
  let count = 0;
  if ((query.contractName || '').trim()) count += 1;
  if ((query.projectName || '').trim()) count += 1;
  if (query.status) count += 1;
  if ((query.contractCode || '').trim()) count += 1;
  if (query.amountMin != null || query.amountMax != null) count += 1;
  if (query.signDateStart || query.signDateEnd) count += 1;
  if (query.partnerType) count += 1;
  if ((query.partnerName || '').trim()) count += 1;
  return count;
});

const tableData = ref<ContractVO[]>([]);

async function fetchList() {
  loading.value = true;
  try {
    const req: Record<string, unknown> = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      contractCode: (query.contractCode || '').trim() || undefined,
      contractName: (query.contractName || '').trim() || undefined,
      projectName: (query.projectName || '').trim() || undefined,
      partnerType: query.partnerType || undefined,
      partnerName: (query.partnerName || '').trim() || undefined,
      amountMin: query.amountMin != null ? query.amountMin * 10000 : undefined,
      amountMax: query.amountMax != null ? query.amountMax * 10000 : undefined,
      signDateStart: query.signDateStart || undefined,
      signDateEnd: query.signDateEnd || undefined,
      status: query.status,
    };
    const res = await queryContractList(req);
    const list = res?.list ?? res?.records ?? [];
    tableData.value = Array.isArray(list) ? list : [];
    total.value = Number(res?.total ?? 0) || 0;
  } catch (e) {
    console.error('查询合同失败:', e);
    tableData.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  fetchList();
}

function handleReset() {
  query.contractCode = '';
  query.contractName = '';
  query.projectName = '';
  query.partnerType = '';
  query.partnerName = '';
  query.amountMin = undefined;
  query.amountMax = undefined;
  query.signDateStart = '';
  query.signDateEnd = '';
  query.status = undefined;
  query.pageNum = 1;
  fetchList();
}

function toggleAdvancedSearch() {
  advancedSearchVisible.value = !advancedSearchVisible.value;
}

function handleCurrentChange(page: number) {
  query.pageNum = page;
  fetchList();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.pageNum = 1;
  fetchList();
}

async function deleteById(row: ContractVO) {
  if (!row?.contractId) return;
  try {
    await ElMessageBox.confirm(`确认删除合同「${row.contractName}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
    });
    loading.value = true;
    await deleteContract(Number(row.contractId));
    ElMessage.success('删除成功');
    if (tableData.value.length === 1 && query.pageNum > 1) query.pageNum -= 1;
    await fetchList();
  } catch (err: unknown) {
    if (err !== 'cancel') ElMessage.error((err as Error)?.message || '删除失败');
  } finally {
    loading.value = false;
  }
}

/** 提交审批：确认后调用接口（需先拉取详情构建完整 CreateContractReq） */
async function handleSubmitApproval(row: ContractVO) {
  if (!row?.contractId || row.status !== 'DRAFT') return;
  try {
    await ElMessageBox.confirm(
      `确认将合同「${row.contractName}」提交审批吗？提交后将进入合同审批流程，合同状态变更为审核中。`,
      '提交审批',
      {
        type: 'warning',
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
      }
    );
    loading.value = true;
    const detail = await getContractDetail(row.contractId);
    const d = detail as Record<string, unknown>;
    const submitReq: CreateContractReq = {
      contractCode: String(d.contractCode ?? ''),
      contractName: String(d.contractName ?? ''),
      contractType: String(d.contractType ?? ''),
      contractCategory: String(d.contractCategory ?? ''),
      tenderId: d.tenderId != null ? Number(d.tenderId) : undefined,
      bidId: d.bidId != null ? Number(d.bidId) : undefined,
      projectId: d.projectId != null ? Number(d.projectId) : undefined,
      purchaserId: d.purchaserId != null ? Number(d.purchaserId) : undefined,
      supplierId: Number(d.supplierId ?? 0),
      amountTotal: Number(d.amountTotal ?? d.amount ?? 0),
      amountWithoutTax: d.amountWithoutTax != null ? Number(d.amountWithoutTax) : undefined,
      taxRateDefault: d.taxRateDefault != null ? Number(d.taxRateDefault) : undefined,
      currency: (d.currency as string) ?? 'CNY',
      signDate: (d.signDate as string) ?? undefined,
      effectiveDate: (d.effectiveDate as string) ?? undefined,
      endDate: (d.endDate as string) ?? undefined,
      payTerms: (d.payTerms as string) ?? undefined,
      settleMode: (d.settleMode as string) ?? undefined,
      remark: (d.remark as string) ?? undefined,
      salesmanPersonId: d.salesmanPersonId != null ? Number(d.salesmanPersonId) : undefined,
      action: 'SUBMIT',
    };
    await createContractBusiness(submitReq);
    await updateContractStatusAfterProcessStart(Number(row.contractId));
    ElMessage.success('已提交审批');
    await fetchList();
  } catch (err: unknown) {
    if (err !== 'cancel') ElMessage.error((err as Error)?.message || '提交失败');
  } finally {
    loading.value = false;
  }
}

function goDetail(row: ContractVO) {
  router.push(`/contracts/contract/${row.contractId}`);
}

function goEdit(row: ContractVO) {
  router.push(`/contracts/contract/${row.contractId}`);
}

function goCreate() {
  router.push('/contracts/contract/create');
}

// 弹窗表单
const editDialog = reactive({ visible: false, isEdit: false });

// 签订/不签订弹窗
const signDialog = reactive<{ visible: boolean; row?: ContractVO }>({ visible: false });
const signFormRef = ref<FormInstance>();
const signForm = reactive({
  action: 'SIGN' as 'SIGN' | 'UNSIGN',
  opinion: '',
  needChange: false,
});

function openSignDialog(row: ContractVO) {
  signDialog.row = row;
  signForm.action = 'SIGN';
  signForm.opinion = '';
  signForm.needChange = false;
  signDialog.visible = true;
}

function resetSignForm() {
  signDialog.row = undefined;
  signForm.action = 'SIGN';
  signForm.opinion = '';
  signForm.needChange = false;
}

async function submitSign() {
  if (!signDialog.row?.contractId) return;
  if (signForm.action === 'UNSIGN') {
    // 不签订时 needChange 必须明确，后端会校验
    // 前端已通过 checkbox 绑定，提交时传 needChange 即可
  }
  try {
    signSaving.value = true;
    await signContract({
      contractId: Number(signDialog.row.contractId),
      action: signForm.action,
      opinion: signForm.opinion?.trim() || undefined,
      needChange: signForm.action === 'UNSIGN' ? signForm.needChange : undefined,
    });
    ElMessage.success(signForm.action === 'SIGN' ? '签订成功，合同已正常归档' : signForm.needChange ? '已返回合同起草，可进行变更' : '已异常归档');
    signDialog.visible = false;
    await fetchList();
  } catch (e) {
    ElMessage.error((e as Error)?.message || '操作失败');
  } finally {
    signSaving.value = false;
  }
}
const formRef = ref<FormInstance>();
const form = reactive({
  contractId: '',
  contractCode: '',
  contractName: '',
  contractType: '',
  contractCategory: '',
  project: undefined as ProjectVO | undefined,
  supplier: undefined as CompanySelectorValue | undefined,
  amount: 0,
  currency: 'CNY',
  signDate: '',
  effectiveDate: '',
  endDate: '',
  remark: '',
});

const rules: FormRules = {
  contractCode: [{ required: true, message: '请输入合同编号', trigger: 'blur' }],
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  supplier: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  amount: [{ required: true, message: '请输入合同金额', trigger: 'change' }],
};

function openEdit(isEdit: boolean, row?: ContractVO) {
  editDialog.isEdit = isEdit;
  if (isEdit && row) {
    form.contractId = String(row.contractId);
    form.contractCode = row.contractCode || '';
    form.contractName = row.contractName || '';
    form.contractType = (row as { contractType?: string }).contractType || '';
    form.contractCategory = (row as { contractCategory?: string }).contractCategory || '';
    form.project = row.projectId && row.projectName ? { projectId: Number(row.projectId), projectName: row.projectName } : undefined;
    form.supplier = row.supplierId && row.supplierName ? { companyId: Number(row.supplierId), companyName: row.supplierName } : undefined;
    form.amount = Number((row.amountTotal ?? row.amount ?? 0)) / 10000 || 0;
    form.currency = 'CNY';
    form.signDate = row.signDate || '';
    form.effectiveDate = (row as { effectiveDate?: string }).effectiveDate || '';
    form.endDate = (row as { endDate?: string }).endDate || '';
    form.remark = '';
  } else {
    form.contractId = '';
    form.contractCode = '';
    form.contractName = '';
    form.contractType = contractTypeOptions.value[0]?.value ?? '';
    form.contractCategory = contractCategoryOptions.value[0]?.value ?? '';
    form.project = undefined;
    form.supplier = undefined;
    form.amount = 0;
    form.currency = 'CNY';
    form.signDate = '';
    form.effectiveDate = '';
    form.endDate = '';
    form.remark = '';
  }
  editDialog.visible = true;
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
    const amountTotal = Math.round((form.amount || 0) * 10000); // 万元转元
    if (editDialog.isEdit) {
      const req: UpdateContractReq = {
        contractId: Number(form.contractId),
        contractCode: form.contractCode,
        contractName: form.contractName,
        projectId: form.project?.projectId,
        supplierId: form.supplier?.companyId,
        amountTotal,
        currency: form.currency,
        signDate: form.signDate || undefined,
        effectiveDate: form.effectiveDate || undefined,
        endDate: form.endDate || undefined,
        remark: form.remark || undefined,
      };
      await updateContract(req);
      ElMessage.success('保存成功');
    } else {
      const ct = form.contractType || contractTypeOptions.value[0]?.value;
      const cc = form.contractCategory || contractCategoryOptions.value[0]?.value;
      if (!ct || !cc) {
        ElMessage.warning('请选择合同类型和合同类别');
        return;
      }
      const req: CreateContractReq = {
        contractCode: form.contractCode,
        contractName: form.contractName,
        contractType: ct,
        contractCategory: cc,
        projectId: form.project?.projectId,
        purchaserId: undefined,
        supplierId: form.supplier!.companyId,
        amountTotal,
        currency: form.currency,
        signDate: form.signDate || undefined,
        effectiveDate: form.effectiveDate || undefined,
        endDate: form.endDate || undefined,
        remark: form.remark || undefined,
        action: 'SAVE',
      };
      await createContractBusiness(req);
      ElMessage.success('新增成功');
    }
    editDialog.visible = false;
    fetchList();
  } catch (e) {
    ElMessage.error((e as Error)?.message || '保存失败');
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  loadPartnerTypeOptions();
  loadContractTypeAndCategoryOptions();
  fetchList();
});

watch(
  () => route.query.edit,
  (val) => {
    const id = typeof val === 'string' ? val : Array.isArray(val) ? val[0] : '';
    if (!id) return;
    router.replace(`/contracts/contract/${id}`);
  },
  { immediate: true }
);
</script>

<style scoped lang="scss">
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title {
  font-weight: 600;
}
.actions {
  display: flex;
  gap: 8px;
}
.search-bar {
  margin-bottom: 12px;
}
.search-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}
.search-row-advanced {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--el-border-color-lighter);
}
.search-item {
  margin-bottom: 0;
}
.search-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-summary {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.amount-sep {
  margin: 0 6px;
  color: var(--el-text-color-secondary);
}
.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.dialog-form.two-col {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
  row-gap: 12px;
}
.dialog-form.two-col :deep(.el-form-item) {
  margin-bottom: 0;
}
.dialog-form.two-col .full-row {
  grid-column: 1 / span 2;
}

/* 合作方列：甲方与供应商不同颜色 */
.partner-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.partner-purchaser {
  color: var(--el-color-primary);
  font-weight: 500;
}
.partner-supplier {
  color: var(--el-color-success);
  font-weight: 500;
}
.partner-empty {
  color: var(--el-text-color-placeholder);
}
</style>
