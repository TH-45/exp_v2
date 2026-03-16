<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <el-button link type="primary" @click="goBack">返回</el-button>
          <div class="title">新增合同</div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        size="small"
        class="contract-form compact-form compact-layout"
        @submit.prevent="handleSubmit"
      >
        <button type="submit" style="display: none" aria-hidden="true" tabindex="-1" />

        <!-- 基本信息 -->
        <div class="form-section">
          <div class="section-title">基本信息</div>
          <div class="form-grid">
            <el-form-item label="合同编号" prop="contractCode">
              <el-input v-model="form.contractCode" placeholder="请输入合同编号" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="合同名称" prop="contractName">
              <el-input v-model="form.contractName" placeholder="请输入合同名称" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item label="合同类型" prop="contractType">
              <el-select v-model="form.contractType" placeholder="请选择合同类型" clearable style="width: 100%">
                <el-option v-for="opt in contractTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="合同类别" prop="contractCategory">
              <el-select v-model="form.contractCategory" placeholder="请选择合同类别" clearable style="width: 100%">
                <el-option v-for="opt in contractCategoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="关联项目" prop="project" class="selector-field">
              <ProjectSelector v-model="form.project" placeholder="请选择关联项目" />
            </el-form-item>
            <el-form-item label="采购方" prop="purchaser" class="selector-field">
              <CompanySelector v-model="form.purchaser" placeholder="请选择采购方" />
            </el-form-item>
            <el-form-item label="供应商" prop="supplier" class="selector-field">
              <CompanySelector v-model="form.supplier" placeholder="请选择供应商" />
            </el-form-item>
          </div>
        </div>

        <!-- 金额与日期（金额类字段缩短，日期保持适中） -->
        <div class="form-section">
          <div class="section-title">金额与日期</div>
          <div class="form-grid form-grid-amount">
            <el-form-item label="合同金额(万)" prop="amount" class="amount-field">
              <el-input-number v-model="form.amount" :min="0" :max="999999999" :precision="2" size="small" @change="calcTaxFields" />
            </el-form-item>
            <el-form-item label="含税">
              <el-checkbox v-model="form.isTaxIncluded" @change="onTaxIncludedChange">
                含税
              </el-checkbox>
            </el-form-item>
            <el-form-item label="税率(%)" prop="taxRate" class="amount-field">
              <el-select
                v-model="form.taxRate"
                placeholder="请选择"
                clearable
                :disabled="!form.isTaxIncluded"
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
            <el-form-item label="币种" prop="currency" class="amount-field">
              <el-select v-model="form.currency" placeholder="请选择币种">
                <el-option label="人民币" value="CNY" />
                <el-option label="美元" value="USD" />
                <el-option label="欧元" value="EUR" />
              </el-select>
            </el-form-item>
            <el-form-item label="拟定签订日期" prop="signDate" class="date-field">
              <el-date-picker
                v-model="form.signDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择"
                :disabled-date="disabledDateForSign"
              />
            </el-form-item>
            <el-form-item label="生效日期" prop="effectiveDate" class="date-field">
              <el-date-picker
                v-model="form.effectiveDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择"
                :disabled-date="(d: Date) => disabledDateForEffective(d, form.signDate)"
              />
            </el-form-item>
            <el-form-item label="结束日期" prop="endDate" class="date-field">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选填"
                :disabled-date="(d: Date) => disabledDateForEnd(d, form.effectiveDate)"
              />
            </el-form-item>
          </div>
        </div>

        <!-- 付款与结算（结算方式在上，付款条件在下支持多行） -->
        <div class="form-section">
          <div class="section-title">付款与结算</div>
          <div class="form-grid">
            <el-form-item label="结算方式" prop="settleMode" required class="settle-field">
              <el-select v-model="form.settleMode" placeholder="请选择结算方式" clearable>
                <el-option v-for="opt in settleModeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="付款条件" prop="payTerms" class="full-width">
              <el-input v-model="form.payTerms" type="textarea" :rows="2" placeholder="如：预付款+进度款+尾款" size="small" />
            </el-form-item>
          </div>
        </div>

        <!-- 附件上传 -->
        <div class="form-section">
          <div class="section-title">合同附件</div>
          <div class="upload-row">
            <el-form-item label="文件类型" class="upload-type-item">
              <el-select v-model="uploadForm.fileType" style="width: 160px">
                <el-option v-for="t in fileTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="上传" class="upload-file-item">
              <el-upload
                :auto-upload="false"
                :limit="20"
                multiple
                drag
                :file-list="uploadFileList"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                class="compact-upload"
              >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽或点击上传（最多20个）</div>
              <template #file="{ file }">
                <span class="el-upload-list__item-name">{{ file.name }}（{{ formatFileSize(file.size ?? (file.raw as File)?.size) }}）</span>
                <el-icon class="el-upload-list__item-delete" @click="removeFile(file)"><Delete /></el-icon>
              </template>
              </el-upload>
            </el-form-item>
          </div>
        </div>

        <!-- 备注 -->
        <div class="form-section">
          <div class="section-title">备注</div>
          <el-form-item prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="1" placeholder="请输入备注信息（选填）" size="small" />
          </el-form-item>
        </div>

        <!-- 业务员（必填） -->
        <div class="form-section form-section-last">
          <div class="section-title">业务员信息</div>
          <div class="form-grid">
            <el-form-item label="业务员" prop="salesman" class="selector-field">
              <PersonSelector v-model="form.salesman" placeholder="请选择业务员" />
            </el-form-item>
          </div>
        </div>

        <!-- 提交 -->
        <div class="form-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSubmit">保存并提交</el-button>
        </div>
      </el-form>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { UploadFilled, Delete } from '@element-plus/icons-vue';
import type { UploadFile, UploadFiles } from 'element-plus';
import { useRouter } from 'vue-router';
import {
  createContractBusiness,
  queryContractList,
  type CreateContractReq,
} from '@/api/contracts/contract';
import { uploadBiddingAttachments, type CreateAttachmentBizReq } from '@/api/bidding/attachments';
import { listDictOptions, type DictOption } from '@/api/system/dict';
import type { CompanySelectorValue } from '@/api/enterprise/company';
import type { ProjectVO } from '@/api/corpProject/project';
import type { ExpPersonVO } from '@/api/system/person';
import ProjectSelector from '@/components/Selector/ProjectSelector.vue';
import CompanySelector from '@/components/Selector/CompanySelector.vue';
import PersonSelector from '@/components/Selector/PersonSelector.vue';

const router = useRouter();
const formRef = ref<FormInstance>();
const saving = ref(false);

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
const fileTypeOptions = ref<DictOption[]>([]);

const form = reactive({
  contractCode: '',
  contractName: '',
  contractType: '',
  contractCategory: '',
  project: undefined as ProjectVO | undefined,
  purchaser: undefined as CompanySelectorValue | undefined,
  supplier: undefined as CompanySelectorValue | undefined,
  amount: 0,
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

/** 不含税金额显示（含税时自动计算） */
const displayAmountWithoutTax = computed(() => {
  if (!form.isTaxIncluded || !form.taxRate || form.amount <= 0) return '';
  const rate = Number(form.taxRate) / 100;
  return (form.amount / (1 + rate)).toFixed(2);
});

/** 税率金额显示 */
const displayTaxAmount = computed(() => {
  if (!form.isTaxIncluded || !form.taxRate || form.amount <= 0) return '';
  const rate = Number(form.taxRate) / 100;
  const withoutTax = form.amount / (1 + rate);
  return (form.amount - withoutTax).toFixed(2);
});

function calcTaxFields() {
  /* 计算由 computed 处理 */
}

function onTaxIncludedChange() {
  if (!form.isTaxIncluded) form.taxRate = undefined;
}

function disabledDateForSign() {
  return false;
}

function disabledDateForEffective(d: Date, signDate: string | null) {
  if (!signDate) return false;
  const sign = new Date(signDate);
  sign.setHours(0, 0, 0, 0);
  const target = new Date(d);
  target.setHours(0, 0, 0, 0);
  return target.getTime() <= sign.getTime();
}

function disabledDateForEnd(d: Date, effectiveDate: string | null) {
  if (!effectiveDate) return false;
  const eff = new Date(effectiveDate);
  eff.setHours(0, 0, 0, 0);
  const target = new Date(d);
  target.setHours(0, 0, 0, 0);
  return target.getTime() <= eff.getTime();
}

const rules: FormRules = {
  contractCode: [{ required: true, message: '请输入合同编号', trigger: 'blur' }],
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  contractType: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
  contractCategory: [{ required: true, message: '请选择合同类别', trigger: 'change' }],
  purchaser: [{ required: true, message: '请选择采购方', trigger: 'change' }],
  supplier: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  amount: [{ required: true, message: '请输入合同金额', trigger: 'change' }],
  currency: [{ required: true, message: '请选择币种', trigger: 'change' }],
  signDate: [{ required: true, message: '请选择拟定签订日期', trigger: 'change' }],
  effectiveDate: [
    { required: true, message: '请选择生效日期', trigger: 'change' },
    {
      validator: (_rule, val, cb) => {
        if (val && form.signDate && val <= form.signDate) {
          cb(new Error('生效日期须大于拟定签订日期'));
        } else {
          cb();
        }
      },
      trigger: 'change',
    },
  ],
  endDate: [
    {
      validator: (_rule, val, cb) => {
        if (val && form.effectiveDate && val <= form.effectiveDate) {
          cb(new Error('结束日期须大于生效日期'));
        } else {
          cb();
        }
      },
      trigger: 'change',
    },
  ],
  settleMode: [{ required: true, message: '请选择结算方式', trigger: 'change' }],
  salesman: [
    {
      validator: (_rule, v, cb) => {
        if (!v?.personId) cb(new Error('请选择业务员'));
        else cb();
      },
      trigger: 'change',
    },
  ],
};

const uploadForm = reactive({ fileType: 'CONTRACT_MAIN' });
const uploadFileList = ref<UploadFile[]>([]);
const selectedFiles = ref<File[]>([]);

function formatFileSize(bytes?: number): string {
  if (bytes == null || bytes === 0) return '0 B';
  const k = 1024;
  const units = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${units[i]}`;
}

function handleFileChange(_file: UploadFile, files: UploadFiles) {
  uploadFileList.value = [...files];
  selectedFiles.value = files.map((f) => f.raw).filter(Boolean) as File[];
}

function handleFileRemove(_file: UploadFile, files: UploadFiles) {
  uploadFileList.value = [...files];
  selectedFiles.value = files.map((f) => f.raw).filter(Boolean) as File[];
}

function removeFile(file: UploadFile) {
  const next = uploadFileList.value.filter((f) => f.uid !== file.uid);
  uploadFileList.value = next;
  selectedFiles.value = next.map((f) => f.raw).filter(Boolean) as File[];
}

async function loadDictOptions() {
  for (const code of ['Contract_Type', 'contract_type']) {
    try {
      const res = await listDictOptions(code);
      const opts = Array.isArray(res) ? res : Array.isArray((res as { data?: DictOption[] })?.data) ? (res as { data?: DictOption[] }).data : [];
      if (opts.length) {
        contractTypeOptions.value = opts;
        break;
      }
    } catch (_e) {
      /* ignore */
    }
  }
  for (const code of ['Contract_Category', 'contract_category']) {
    try {
      const res = await listDictOptions(code);
      const opts = Array.isArray(res) ? res : Array.isArray((res as { data?: DictOption[] })?.data) ? (res as { data?: DictOption[] }).data : [];
      if (opts.length) {
        contractCategoryOptions.value = opts;
        break;
      }
    } catch (_e) {
      /* ignore */
    }
  }
  try {
    const res = await listDictOptions('Contract_File_Type');
    const opts = Array.isArray(res) ? res : Array.isArray((res as { data?: DictOption[] })?.data) ? (res as { data?: DictOption[] }).data : [];
    if (opts.length) fileTypeOptions.value = opts;
  } catch (_e) {
    /* ignore */
  }
  if (contractTypeOptions.value.length === 0) {
    contractTypeOptions.value = [
      { label: '工程合同', value: 'ENGINEERING' },
      { label: '采购合同', value: 'PURCHASE' },
      { label: '服务合同', value: 'SERVICE' },
    ];
  }
  if (contractCategoryOptions.value.length === 0) {
    contractCategoryOptions.value = [
      { label: '框架合同', value: 'FRAMEWORK' },
      { label: '一次性合同', value: 'ONCE' },
      { label: '分包合同', value: 'SUBCONTRACT' },
    ];
  }
  if (fileTypeOptions.value.length === 0) {
    fileTypeOptions.value = [
      { label: '合同正文', value: 'CONTRACT_MAIN' },
      { label: '补充协议', value: 'SUPPLEMENT' },
      { label: '正式合同扫描件', value: 'SCAN' },
      { label: '其他', value: 'OTHER' },
    ];
  }
  // 结算方式从字典 settlement_type 获取
  for (const code of ['settlement_type', 'Settlement_Type']) {
    try {
      const res = await listDictOptions(code);
      const opts = Array.isArray(res) ? res : Array.isArray((res as { data?: DictOption[] })?.data) ? (res as { data?: DictOption[] }).data : [];
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
  if (uploadForm.fileType === 'CONTRACT_MAIN' && fileTypeOptions.value[0]) {
    uploadForm.fileType = fileTypeOptions.value[0].value;
  }
}

async function handleSubmit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;
  // 日期逻辑校验（提交时二次确认）
  if (form.effectiveDate && form.signDate && form.effectiveDate <= form.signDate) {
    ElMessage.warning('生效日期须大于拟定签订日期');
    return;
  }
  if (form.endDate && form.effectiveDate && form.endDate <= form.effectiveDate) {
    ElMessage.warning('结束日期须大于生效日期');
    return;
  }
  saving.value = true;
  try {
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
      contractType: form.contractType!,
      contractCategory: form.contractCategory!,
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
      action: 'SAVE',
    };

    await createContractBusiness(req);
    const listRes = await queryContractList({
      pageNum: 1,
      pageSize: 1,
      contractCode: form.contractCode,
    });
    const first = listRes?.list?.[0] ?? listRes?.records?.[0];
    const contractId = first ? Number((first as { contractId?: string }).contractId) : undefined;

    if (selectedFiles.value.length > 0 && contractId) {
      const biz: CreateAttachmentBizReq = {
        businessType: 'CONTRACT',
        businessId: Number(contractId),
        fileType: uploadForm.fileType,
        fileCategory: uploadForm.fileType,
      };
      const bizList = selectedFiles.value.map(() => ({ ...biz }));
      await uploadBiddingAttachments(selectedFiles.value, bizList);
    }

    ElMessage.success(selectedFiles.value.length > 0 ? '合同创建成功，附件已上传' : '合同创建成功');
    router.push('/contracts/contract');
  } catch (e) {
    ElMessage.error((e as Error)?.message || '保存失败');
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.push('/contracts/contract');
}

onMounted(() => loadDictOptions());
</script>

<style scoped lang="scss">
.header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.title {
  font-weight: 600;
  font-size: 15px;
}

/* 紧凑表单：缩小间距 */
.contract-form.compact-form :deep(.el-form-item) {
  margin-bottom: 6px;
}
.form-section {
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.form-section-last {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}
.section-title {
  font-weight: 600;
  font-size: 12px;
  margin-bottom: 6px;
  padding-left: 6px;
  border-left: 3px solid var(--el-color-primary);
  color: var(--el-text-color-primary);
}
/* 紧凑布局：4列网格，一屏展示更多内容 */
.contract-form.compact-layout .form-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px 12px;
}
.compact-layout .form-grid :deep(.el-form-item) {
  margin-bottom: 6px;
}
.form-grid .full-width {
  grid-column: 1 / -1;
}

/* 关联项目、采购方、供应商、业务员：限制宽度 */
.compact-layout .form-grid .selector-field :deep(.el-form-item__content) {
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

/* 附件区：文件类型与上传同行 */
.upload-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}
.upload-type-item {
  flex-shrink: 0;
}
.upload-file-item {
  flex: 1;
  margin-bottom: 0 !important;
}
.compact-upload :deep(.el-upload-dragger) {
  padding: 12px;
  width: 100%;
}
.compact-upload :deep(.el-icon--upload) {
  font-size: 28px;
  margin-bottom: 4px;
}
.compact-upload :deep(.el-upload__text) {
  font-size: 12px;
}
.el-upload-list__item-name {
  margin-right: 8px;
}
.el-upload-list__item-delete {
  cursor: pointer;
  color: var(--el-color-danger);
}

.form-actions {
  margin-top: 16px;
  padding-top: 12px;
  display: flex;
  gap: 10px;
}
</style>
