<template>
  <el-config-provider :locale="zhCn">
    <el-card v-loading="loading">
      <template #header>
        <div class="header">
          <div class="left">
            <el-button link type="primary" @click="goBack">返回</el-button>
            <div class="title">招标项目详情</div>
            <el-tag :type="getTenderStatusTagType(project.status)" class="status-tag">
              {{ getTenderStatusText(project.status) }}
            </el-tag>
          </div>
          <div class="actions">
            <el-button size="small" type="primary" :disabled="!canManage" @click="openEditProject">
              编辑项目
            </el-button>
            <el-button size="small" :disabled="true">操作记录</el-button>
          </div>
        </div>
      </template>

      <!-- 基本信息：与后端 /tender/detail 字段对齐 -->
      <el-descriptions :column="3" border class="summary">
        <el-descriptions-item label="招标编号">{{ project.tenderCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="招标项目名称">{{ project.tenderName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="招标单位">{{ project.purchaserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="招标类型">{{ formatTenderType(project.tenderType) }}</el-descriptions-item>
        <el-descriptions-item label="招标方式">{{ formatTenderMode(project.tenderMode) }}</el-descriptions-item>
        <el-descriptions-item label="币种">{{ formatCurrency(project.currency) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预算金额(万)" :span="2">{{ project.budgetAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联项目">{{ project.projectName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="招标公告发布时间">{{ project.publishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="投标开始时间">{{ project.bidStartTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="投标截止时间">{{ project.bidEndTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开标时间">{{ project.openTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开标地点" :span="3">
          {{ project.openAddress && project.openAddress.trim() ? project.openAddress : '线上' }}
        </el-descriptions-item>
        <el-descriptions-item label="招标项目概要/公告摘要" :span="3">
          {{ project.tenderBrief || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ project.createdByName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ project.createdTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ project.updatedTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ project.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="概览" name="overview">
          <el-alert
            title="这里汇总项目关键指标（投标数、标段数、关键节点、当前状态）。后续接接口后会替换为真实数据。"
            type="info"
            show-icon
          />
          <div class="grid">
            <el-card class="mini" shadow="never">
              <div class="metric">
                <div class="metric-label">投标登记</div>
                <div class="metric-value">{{ metrics.bidCount }}</div>
              </div>
            </el-card>
            <el-card class="mini" shadow="never">
              <div class="metric">
                <div class="metric-label">标段数量</div>
                <div class="metric-value">{{ metrics.lotCount }}</div>
              </div>
            </el-card>
            <el-card class="mini" shadow="never">
              <div class="metric">
                <div class="metric-label">附件数量</div>
                <div class="metric-value">{{ metrics.fileCount }}</div>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <el-tab-pane label="参与方" name="party">
          <el-table :data="partyList" border style="width: 100%">
            <el-table-column prop="type" label="类型" min-width="120" />
            <el-table-column prop="name" label="名称" min-width="220" />
            <el-table-column prop="contact" label="联系人" min-width="140" />
            <el-table-column prop="mobile" label="电话" min-width="140" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="时间节点" name="milestone">
          <el-timeline>
            <el-timeline-item
              v-for="(m, idx) in milestones"
              :key="idx"
              :timestamp="m.time"
              :type="m.type"
            >
              {{ m.title }}
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>

        <el-tab-pane label="投标登记" name="bids">
          <el-table :data="bidList" border style="width: 100%">
            <el-table-column prop="bidder" label="投标人" min-width="220" />
            <el-table-column prop="amount" label="报价(万)" min-width="120" />
            <el-table-column prop="status" label="状态" min-width="120" />
            <el-table-column prop="time" label="登记时间" min-width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="评标/定标" name="evaluation">
          <el-alert
            title="无审批流阶段：支持录入评标结果与推荐中标人（示例）。后续接审批流时可在此页挂上提交/审核动作。"
            type="warning"
            show-icon
          />
          <el-form :model="evaluation" label-width="120px" class="eval-form">
            <el-form-item label="推荐中标人">
              <el-input v-model="evaluation.winner" placeholder="请输入推荐中标人" style="width: 420px" />
            </el-form-item>
            <el-form-item label="综合评分">
              <el-input-number v-model="evaluation.score" :min="0" :max="100" />
            </el-form-item>
            <el-form-item label="评审意见">
              <el-input v-model="evaluation.comment" type="textarea" :rows="4" placeholder="请输入评审意见" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" :disabled="true">保存（待接接口）</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="附件" name="attachments">
          <el-alert
            title="你选了“附件独立库”，这里仅展示与本项目关联的附件摘要，并提供跳转到招投标附件库。"
            type="info"
            show-icon
          />
          <div class="attach-actions">
            <el-button size="small" type="primary" @click="goAttachmentLib">打开招投标附件库</el-button>
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

      <!-- 详情页内编辑项目弹窗 -->
      <el-dialog
        v-model="editDialog.visible"
        title="编辑项目"
        width="860px"
        destroy-on-close
        draggable
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="110px"
          class="dialog-form two-col"
          @submit.prevent="submitForm"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="项目编码" prop="tenderCode">
            <el-input v-model="form.tenderCode" placeholder="请输入项目编码" :disabled="true" />
          </el-form-item>
          <el-form-item label="项目名称" prop="tenderName">
            <el-input v-model="form.tenderName" placeholder="请输入项目名称" />
          </el-form-item>
          <el-form-item label="招标单位" prop="company">
            <CompanySelector v-model="form.company" />
          </el-form-item>
          <el-form-item label="归属组织" prop="orgId">
            <OrgSelector
              v-model="selectedOrg"
              placeholder="请选择归属组织"
              @change="handleOrgChange"
            />
          </el-form-item>
          <el-form-item label="负责人" prop="owner">
            <PersonSelector v-model="form.owner" />
          </el-form-item>
          <el-form-item label="关联项目" prop="relatedProject">
            <ProjectSelector v-model="form.relatedProject" />
          </el-form-item>
          <el-form-item label="预算金额(万)" prop="budgetAmount">
            <div class="budget-row">
              <el-input-number v-model="form.budgetAmount" :min="0" :max="999999999" />
              <el-checkbox v-model="form.isTaxIncluded" style="margin-left: 8px;">含税</el-checkbox>
            </div>
          </el-form-item>
          <el-form-item label="币种" prop="currency">
            <el-select v-model="form.currency" placeholder="请选择币种" clearable style="width: 100%">
              <el-option v-for="c in currencyOptions" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="税率" prop="taxRatePercent">
            <el-select
              v-model="form.taxRatePercent"
              placeholder="请选择税率"
              filterable
              allow-create
              default-first-option
              @change="handleTaxRateChange"
              @blur="handleTaxRateBlur"
              style="width: 100%"
            >
              <el-option
                v-for="opt in taxRateOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="采购性质" prop="purchaseNature">
            <el-select v-model="form.purchaseNature" placeholder="请选择采购性质" clearable style="width: 100%">
              <el-option
                v-for="opt in purchaseNatureOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="招标类型" prop="tenderType">
            <el-select v-model="form.tenderType" clearable style="width: 100%">
              <el-option v-for="t in tenderTypeList" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="招标方式" prop="tenderMode">
            <el-select v-model="form.tenderMode" clearable style="width: 100%">
              <el-option v-for="t in tenderModeList" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="发布时间" prop="publishTime">
            <el-date-picker
              v-model="form.publishTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择发布时间"
              style="width: 100%"
              :disabled="true"
            />
          </el-form-item>
          <el-form-item label="投标开始时间" prop="bidStartTime">
            <el-date-picker
              v-model="form.bidStartTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择投标开始时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="投标截止时间" prop="bidEndTime">
            <el-date-picker
              v-model="form.bidEndTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择投标截止时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="开标时间" prop="openTime">
            <el-date-picker
              v-model="form.openTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择开标时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="开标地点" class="full-row">
            <div class="open-address-row">
              <el-cascader
                v-model="form.openAddressCascader"
                :options="regionData"
                :props="{ value: 'value', label: 'label', checkStrictly: false }"
                placeholder="省 / 市 / 区（选填，不选则为线上开标）"
                clearable
                style="width: 100%; max-width: 360px"
              />
              <el-input
                v-model="form.openAddressDetail"
                placeholder="详细地址（选填）"
                clearable
                style="flex: 1; min-width: 160px"
              />
            </div>
          </el-form-item>
          <el-form-item label="招标项目概要/公告摘要" prop="tenderBrief" class="full-row">
            <el-input
              v-model="form.tenderBrief"
              type="textarea"
              :rows="3"
              placeholder="请输入招标项目概要或公告摘要（可选）"
              @keydown.enter.stop
            />
          </el-form-item>
          <el-form-item label="备注" class="full-row">
            <el-input
              v-model="form.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注（可选）"
              @keydown.enter.stop
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">确认</el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { getMenuLevel } from '@/utils/permission';
import {
  getBiddingProjectDetail,
  updateBiddingProject,
  type TenderVO,
  type BiddingProjectStatus,
  type UpdateTenderReq,
} from '@/api/bidding/project';
import { listDictOptions, type DictOption } from '@/api/system/dict';
import type { ExpPersonVO } from '@/api/system/person';
import PersonSelector from '@/components/Selector/PersonSelector.vue';
import CompanySelector from '@/components/Selector/CompanySelector.vue';
import ProjectSelector from '@/components/Selector/ProjectSelector.vue';
import type { ProjectVO } from '@/api/corpProject/project';
import OrgSelector from '@/components/Selector/OrgSelector.vue';
import type { OrgNode } from '@/api/system/post';
import { parseOpenAddress, buildOpenAddress, findRegionCodesByLabels } from '@/utils/openAddress';
import { useTenderStatusDict } from '@/composables/useTenderStatusDict';
import { regionData, codeToText } from 'element-china-area-data';

const route = useRoute();
const router = useRouter();

const canManage = computed(() => getMenuLevel('bidding:project') >= 3);
const {
  fetchTenderStatusOptions,
  getTenderStatusText,
  getTenderStatusTagType,
} = useTenderStatusDict();

function normalizeDictOptions(res: DictOption[] | { data?: DictOption[] }) {
  if (Array.isArray(res)) return res;
  return Array.isArray(res?.data) ? res.data : [];
}

const tenderModeList = ref<DictOption[]>([]);
const tenderTypeList = ref<DictOption[]>([]);
const currencyOptions = ref<DictOption[]>([]);

function formatTenderMode(value?: string) {
  if (!value) return '-';
  const found = tenderModeList.value.find((x) => x.value === value);
  return found?.label ?? value;
}

function formatTenderType(value?: string) {
  if (!value) return '-';
  const found = tenderTypeList.value.find((x) => x.value === value);
  return found?.label ?? value;
}

function formatCurrency(value?: string) {
  if (!value) return '';
  const found = currencyOptions.value.find((x) => x.value === value);
  return found?.label ?? value;
}

const loading = ref(false);
const activeTab = ref('overview');

/** 招标详情数据，与 /tender/detail 响应字段对齐 */
const project = reactive<Partial<TenderVO> & { updatedTime?: string }>({
  tenderId: '',
  tenderCode: '',
  tenderName: '',
  purchaserName: '',
  tenderType: '',
  tenderMode: '',
  budgetAmount: undefined,
  currency: '',
  tenderBrief: '',
  publishTime: '',
  bidStartTime: '',
  bidEndTime: '',
  openTime: '',
  openAddress: '',
  status: undefined,
  projectName: '',
  createdByName: '',
  createdTime: '',
  updatedTime: '',
  remark: '',
});

const metrics = reactive({
  bidCount: 8,
  lotCount: 2,
  fileCount: 15,
});

const partyList = ref([
  { type: '招标单位', name: '总部', contact: '张三', mobile: '13800000000' },
  { type: '代理机构', name: '示例代理公司', contact: '李四', mobile: '13900000000' },
]);

const milestones = ref([
  { time: '2025-01-01', title: '项目创建', type: 'primary' },
  { time: '2025-01-10', title: '发布招标公告', type: 'success' },
  { time: '2025-01-20', title: '投标截止', type: 'warning' },
  { time: '2025-01-25', title: '评标完成', type: 'info' },
]);

const bidList = ref([
  { bidder: '供应商A', amount: 120, status: '已提交', time: '2025-01-15 10:00:00' },
  { bidder: '供应商B', amount: 118, status: '已提交', time: '2025-01-15 11:20:00' },
]);

const evaluation = reactive({
  winner: '供应商B',
  score: 92,
  comment: '综合评分最高，报价合理。',
});

const fileList = ref([
  { name: '招标文件.pdf', type: '招标文件', time: '2025-01-10 09:00:00' },
  { name: '评标报告.docx', type: '评标报告', time: '2025-01-25 18:00:00' },
]);

const logList = ref([
  { time: '2025-01-01 10:00:00', user: 'admin', action: '创建项目', remark: '初始化项目' },
  { time: '2025-01-10 09:10:00', user: '张三', action: '发布公告', remark: '发布成功' },
]);

// 编辑弹窗相关状态与表单
const purchaseNatureOptions = ref<DictOption[]>([]);
const taxRateOptions = ref<DictOption[]>([
  { label: '3%', value: '3' },
  { label: '6%', value: '6' },
  { label: '9%', value: '9' },
  { label: '13%', value: '13' },
]);

const editDialog = reactive({
  visible: false,
  isEdit: true,
});
const formRef = ref<FormInstance>();

interface CompanyVO {
  companyId: string;
  companyCode?: string;
  companyName: string;
}

const selectedOrg = ref<OrgNode>();

const form = reactive({
  tenderId: '',
  tenderCode: '',
  tenderName: '',
  tenderType: '',
  tenderMode: '',
  owner: undefined as ExpPersonVO | undefined,
  company: undefined as CompanyVO | undefined,
  orgId: undefined as number | undefined,
  budgetAmount: 0,
  isTaxIncluded: false,
  taxRatePercent: '' as string | number,
  purchaseNature: '',
  status: '未开始' as BiddingProjectStatus,
  currency: 'CNY',
  tenderBrief: '',
  publishTime: '',
  bidStartTime: '',
  bidEndTime: '',
  openTime: '',
  openAddressCascader: [] as string[],
  openAddressDetail: '',
  relatedProject: undefined as ProjectVO | undefined,
  remark: '',
});

function normalizeTaxRateValue(value: unknown) {
  const raw = String(value ?? '').trim();
  if (!raw) return '';
  const cleaned = raw.replace(/\s*%$/, '').trim();
  if (!cleaned) return '';
  const num = Number(cleaned);
  if (!Number.isFinite(num)) return '';
  return String(num);
}

function ensureTaxRateOption(value: string) {
  if (!value) return;
  const exists = taxRateOptions.value.some((opt) => String(opt.value) === value);
  if (!exists) {
    taxRateOptions.value.push({ label: `${value}%`, value });
    return;
  }
  const option = taxRateOptions.value.find((opt) => String(opt.value) === value);
  if (option && option.label !== `${value}%`) {
    option.label = `${value}%`;
  }
}

function applyTaxRateDisplay(value: unknown) {
  const normalized = normalizeTaxRateValue(value);
  if (normalized === '') {
    if (value == null || String(value).trim() === '') {
      form.taxRatePercent = '';
    }
    return;
  }
  form.taxRatePercent = normalized;
  ensureTaxRateOption(normalized);
}

function handleTaxRateChange(value: string | number) {
  applyTaxRateDisplay(value);
}

function handleTaxRateBlur() {
  applyTaxRateDisplay(form.taxRatePercent);
}

function validateTaxRatePercent(_rule: any, value: any, callback: (error?: Error) => void) {
  if (value === '' || value === null || value === undefined) {
    callback(new Error('请输入税率'));
    return;
  }
  const num = Number(value);
  if (!Number.isFinite(num) || num < 0 || num > 100) {
    callback(new Error('税率必须是 0~100 之间的数字'));
    return;
  }
  callback();
}

const rules: FormRules = {
  tenderCode: [{ required: true, message: '请输入项目编码', trigger: 'blur' }],
  tenderName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  tenderType: [{ required: true, message: '请选择招标类型', trigger: 'change' }],
  tenderMode: [{ required: true, message: '请选择招标方式', trigger: 'change' }],
  bidStartTime: [{ required: true, message: '请选择投标开始时间', trigger: 'change' }],
  bidEndTime: [{ required: true, message: '请选择投标截止时间', trigger: 'change' }],
  openTime: [{ required: true, message: '请选择开标时间', trigger: 'change' }],
  publishTime: [{ required: true, message: '请选择发布时间', trigger: 'change' }],
  company: [{ required: true, message: '请选择招标人', trigger: 'change' }],
  orgId: [{ required: true, message: '请选择归属组织', trigger: 'change' }],
  budgetAmount: [{ required: true, message: '请输入预算金额', trigger: 'blur' }],
  taxRatePercent: [
    { required: true, message: '请输入税率', trigger: 'change' },
    { validator: validateTaxRatePercent, trigger: 'change' },
  ],
  purchaseNature: [{ required: true, message: '请选择采购性质', trigger: 'change' }],
};

async function fetchDetail() {
  const tenderId = route.params.projectId as string;
  if (!tenderId) return;
  loading.value = true;
  try {
    const res = await getBiddingProjectDetail(tenderId);
    Object.assign(project, res);
  } catch (e) {
    console.error('获取招标详情失败:', e);
  } finally {
    loading.value = false;
  }
}

async function fetchDictOptions() {
  try {
    const [modeRes, typeRes, currencyRes, purchaseNatureRes] = await Promise.all([
      listDictOptions('tender_mode'),
      listDictOptions('tender_type'),
      listDictOptions('currency'),
      listDictOptions('purchase_nature'),
      fetchTenderStatusOptions(),
    ]);
    tenderModeList.value = normalizeDictOptions(modeRes);
    tenderTypeList.value = normalizeDictOptions(typeRes);
    currencyOptions.value = normalizeDictOptions(currencyRes);
    purchaseNatureOptions.value = normalizeDictOptions(purchaseNatureRes);
  } catch {
    tenderModeList.value = [];
    tenderTypeList.value = [];
    currencyOptions.value = [];
    purchaseNatureOptions.value = [];
  }
}

onMounted(async () => {
  await fetchDictOptions();
  await fetchDetail();
});

function goBack() {
  router.push('/bidding/project');
}

function goAttachmentLib() {
  router.push('/bidding/attachments');
}

function openEditProject() {
  // 将详情数据映射到编辑表单
  form.tenderId = String(project.tenderId ?? route.params.projectId ?? '');
  form.tenderCode = project.tenderCode || '';
  form.tenderName = project.tenderName || '';
  form.tenderType = project.tenderType || '';
  form.tenderMode = project.tenderMode || '';
  form.status = (project.status as BiddingProjectStatus) || '未开始';
  form.budgetAmount = project.budgetAmount || 0;
  form.isTaxIncluded = project.isTaxIncluded ?? false;
  form.taxRatePercent =
    project.taxRate != null ? String(Number(project.taxRate) * 100) : '';
  applyTaxRateDisplay(form.taxRatePercent);
  form.purchaseNature = project.purchaseNature || '';
  form.bidStartTime = project.bidStartTime || '';
  form.bidEndTime = project.bidEndTime || '';
  form.openTime = project.openTime || '';
  form.remark = project.remark || '';
  form.tenderBrief = project.tenderBrief ?? '';
  form.currency = project.currency ?? 'CNY';
  form.publishTime = project.publishTime || '';

  // 开标地点回显
  const parsed = parseOpenAddress(project.openAddress || '');
  if (parsed.province || parsed.city || parsed.district) {
    const codes = findRegionCodesByLabels(
      regionData as any,
      parsed.province,
      parsed.city,
      parsed.district
    );
    form.openAddressCascader = codes ?? [];
    form.openAddressDetail = parsed.detail;
  } else {
    form.openAddressCascader = [];
    form.openAddressDetail = '';
  }

  // 负责人回显
  form.owner = project.personId
    ? ({
        personId: project.personId,
        personName: project.personIdName,
      } as any)
    : undefined;

  // 招标单位回显
  form.company = project.purchaserId
    ? ({
        companyId: project.purchaserId,
        companyName: project.purchaserName,
      } as any)
    : undefined;

  // 归属组织回显
  form.orgId = project.orgId != null ? Number(project.orgId) : undefined;
  selectedOrg.value =
    project.orgId != null
      ? ({
          orgId: Number(project.orgId),
          orgName: project.orgName || '',
          orgCode: '',
          children: [],
        } as OrgNode)
      : undefined;

  // 关联项目回显
  form.relatedProject = project.projectId
    ? ({
        projectId: project.projectId,
        projectName: project.projectName,
      } as any)
    : undefined;

  editDialog.visible = true;
}

function handleOrgChange(org: OrgNode | undefined) {
  form.orgId = org?.orgId as number | undefined;
}

async function submitForm() {
  if (!formRef.value) return;

  applyTaxRateDisplay(form.taxRatePercent);
  const valid = await formRef.value.validate();
  if (!valid) return;

  const companyId =
    form.company?.companyId != null ? Number(form.company.companyId) : null;
  if (companyId == null || companyId === 0) {
    ElMessage.warning('请选择招标单位');
    return;
  }

  if (form.orgId == null) {
    ElMessage.warning('请选择归属组织');
    return;
  }

  // 开标地点拼接
  let openAddressValue: string | undefined;
  const cascader = form.openAddressCascader;
  const c0 = cascader?.[0];
  const c1 = cascader?.[1];
  const c2 = cascader?.[2];
  if (
    c0 != null &&
    c1 != null &&
    c2 != null &&
    codeToText[c0] &&
    codeToText[c1] &&
    codeToText[c2]
  ) {
    openAddressValue = buildOpenAddress(
      codeToText[c0],
      codeToText[c1],
      codeToText[c2],
      form.openAddressDetail || ''
    );
    if (openAddressValue.endsWith(', '))
      openAddressValue = openAddressValue.slice(0, -2);
  } else {
    openAddressValue = '';
  }

  const taxRatePercentNum = Number(form.taxRatePercent);
  const taxRateDecimal = taxRatePercentNum / 100;

  const payload: UpdateTenderReq = {
    tenderId: Number(form.tenderId),
    tenderCode: form.tenderCode || '',
    tenderName: form.tenderName || '',
    tenderType: form.tenderType || '',
    tenderMode: form.tenderMode || '',
    companyId,
    budgetAmount: Number(form.budgetAmount) || 0,
    taxRate: taxRateDecimal,
    isTaxIncluded: !!form.isTaxIncluded,
    purchaseNature: form.purchaseNature || '',
    tenderBrief: form.tenderBrief || undefined,
    publishTime: form.publishTime || '',
    bidStartTime: form.bidStartTime || '',
    bidEndTime: form.bidEndTime || '',
    openTime: form.openTime || undefined,
    openAddress: openAddressValue ?? '',
    projectId:
      form.relatedProject?.projectId != null
        ? Number(form.relatedProject.projectId)
        : undefined,
    personId:
      form.owner?.personId != null ? Number(form.owner.personId) : undefined,
    orgId: form.orgId != null ? Number(form.orgId) : undefined,
    remark: form.remark || undefined,
  };

  loading.value = true;
  try {
    await updateBiddingProject(payload);
    ElMessage.success('更新成功');
    editDialog.visible = false;
    await fetchDetail();
  } catch (e: any) {
    ElMessage.error(e?.message || '更新失败');
  } finally {
    loading.value = false;
  }
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

.open-address-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.budget-row {
  display: flex;
  align-items: center;
  width: 100%;

  :deep(.el-input-number) {
    flex: 1;
  }

  :deep(.el-checkbox) {
    margin-left: 12px;
    flex-shrink: 0;
  }
}

.grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.mini :deep(.el-card__body) {
  padding: 14px;
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-label {
  color: #666;
  font-size: 12px;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
}

.eval-form {
  margin-top: 12px;
  max-width: 860px;
}

.attach-actions {
  margin: 10px 0 12px;
  display: flex;
  gap: 8px;
}
</style>
