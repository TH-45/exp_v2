<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">招标项目</div>
          <div class="actions">
            <el-button type="primary" size="small" @click="openEdit(false)" :disabled="!canManage">
              录入项目
            </el-button>
            <el-button size="small" :disabled="true">导入</el-button>
            <el-button size="small" :disabled="true">导出</el-button>
          </div>
        </div>
      </template>

      <!-- 查询栏 -->
      <el-form :model="query" class="search-bar" @submit.prevent>
        <div class="search-row search-row-primary">
          <el-form-item label="项目编码" class="search-item search-item-keyword">
            <el-input v-model="query.tenderCode" placeholder="请输入项目编码" clearable />
          </el-form-item>
          <el-form-item label="项目名称" class="search-item search-item-keyword">
            <el-input v-model="query.tenderName" placeholder="请输入项目名称" clearable />
          </el-form-item>
          <el-form-item label="查询状态" class="search-item search-item-status">
            <el-select v-model="query.status" clearable placeholder="全部状态">
              <el-option v-for="s in tenderStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="招标方式" class="search-item search-item-select">
            <el-select v-model="query.tenderMode" clearable placeholder="全部方式">
              <el-option v-for="t in tenderModeList" :key="t.value" :label="t.label" :value="t.value" />
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
          <el-form-item label="招标单位" class="search-item search-item-keyword">
            <el-input v-model="query.purchaserName" placeholder="请输入招标单位" clearable />
          </el-form-item>
          <el-form-item label="招标类型" class="search-item search-item-select">
            <el-select v-model="query.tenderType" clearable placeholder="全部类型">
              <el-option v-for="t in tenderTypeList" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="年度" class="search-item search-item-year">
            <el-select v-model="yearSelectValue" clearable>
              <el-option label="全部" :value="YEAR_ALL" />
              <el-option v-for="y in yearOptions" :key="y" :label="String(y)" :value="y" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>

      <!-- 表格：双击行打开详情 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="tenderId"
        border
        style="width: 100%"
        @row-dblclick="(row: TenderVO) => goDetail(row)"
      >
        <el-table-column prop="tenderCode" label="招标编号" min-width="120" />
        <el-table-column prop="tenderName" label="招标项目名称" min-width="200" />
        <el-table-column prop="purchaserName" label="招标单位" min-width="180" />
        <el-table-column prop="orgName" label="归属部门" min-width="120" />
        <el-table-column prop="personIdName" label="负责人" min-width="120" />
        <el-table-column prop="salesmanName" label="业务员" min-width="170" />
        <el-table-column label="招标方式" min-width="100">
          <template #default="{ row }">
            {{ formatTenderMode(row.tenderMode) }}
          </template>
        </el-table-column>
        <el-table-column label="金额(万元)" min-width="130">
          <template #default="{ row }">
            {{ formatAmountWithTax(row.budgetAmount, row.isTaxIncluded) }}
          </template>
        </el-table-column>
        <el-table-column label="币种" min-width="80">
          <template #default="{ row }">
            {{ formatCurrency(row.currency) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="getTenderStatusTagType(row.status)">{{ getTenderStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开标地点" min-width="100">
          <template #default="{ row }">
            {{ parseOpenAddressCity(row.openAddress) }}
          </template>
        </el-table-column>
        <el-table-column prop="openTime" label="开标时间" min-width="170" />
        <el-table-column prop="bidEndTime" label="投标截止时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(true, row)" :disabled="!canManage">
              编辑
            </el-button>
            <el-button link size="small" @click="goDetail(row)">详情</el-button>
            <el-button link type="danger" size="small" @click="deleteById(row)">删除</el-button>

          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

      <!-- 新增/编辑弹窗（你选了 3.B：弹窗） -->
      <el-dialog
        v-model="editDialog.visible"
        :title="editDialog.isEdit ? '编辑项目' : '新增项目'"
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
            <el-input v-model="form.tenderCode" placeholder="请输入项目编码" :disabled="editDialog.isEdit" />
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
              <el-input-number v-model="form.budgetAmount" :min="0" :max="999999999"  />
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
              :disabled="editDialog.isEdit"
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
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注（可选）" @keydown.enter.stop />
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
import { onMounted, reactive, ref, computed, watch } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage,ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import { generateProjectCode } from '@/utils/codeGenerator';
import type { ExpPersonVO } from '@/api/system/person';
import PersonSelector from '@/components/Selector/PersonSelector.vue';
import CompanySelector from '@/components/Selector/CompanySelector.vue';
import ProjectSelector from '@/components/Selector/ProjectSelector.vue';
import OrgSelector from '@/components/Selector/OrgSelector.vue';
import type { ProjectVO } from '@/api/corpProject/project';
import type { OrgNode } from '@/api/system/post';
import {
  queryBiddingProjectList,
  createBiddingProject,
  updateBiddingProject,
  deleteBiddingProject,
  getBiddingProjectDetail,
  type TenderVO,
  type BiddingProjectStatus,
  type CreateTenderReq,
  type UpdateTenderReq,
} from '@/api/bidding/project';

import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';
import { type DictOption, listDictOptions } from '@/api/system/dict';
import { parseOpenAddressCity, parseOpenAddress, buildOpenAddress, findRegionCodesByLabels } from '@/utils/openAddress';
import { useTenderStatusDict } from '@/composables/useTenderStatusDict';
import { regionData, codeToText } from 'element-china-area-data';

const route = useRoute();
const router = useRouter();
const canManage = computed(() => hasPermission('bidding:project:manage'));
const {
  tenderStatusOptions,
  fetchTenderStatusOptions,
  getTenderStatusText,
  getTenderStatusTagType,
} = useTenderStatusDict();
/**
 * 生成项目编码并填充
 */
function autoGenerateProjectCode() {
  // 默认流水号可以从后端获取
  // 这里先用当前页数量 + 1 作为模拟流水号
  const lsh = tableData.value.length + 1;

  const tenderModeValue = form.tenderMode || 'OPEN';

  form.tenderCode = generateProjectCode(tenderModeValue, lsh);
}

/**
 * 统一用英文值 + 字典转中文
 * @param value
 */
function formatTenderMode(value?: string) {
  if (!value) return '';
  const found = tenderModeList.value.find(x => x.value === value);
  return found?.label ?? value;
}

/** 币种展示：字典转中文 */
function formatCurrency(value?: string) {
  if (!value) return '';
  const found = currencyOptions.value.find((x) => x.value === value);
  return found?.label ?? value;
}

/** 金额 + 含税标识展示 */
function formatAmountWithTax(amount?: number, isTaxIncluded?: boolean) {
  if (amount == null) return '';
  const num = Number(amount);
  if (!Number.isFinite(num)) return '';
  const value = num.toFixed(2);
  if (isTaxIncluded === true) return `${value}(含税)`;
  if (isTaxIncluded === false) return `${value}(不含税)`;
  return value;
}

const tenderModeList = ref<DictOption[]>([]);
const tenderTypeList = ref<DictOption[]>([]);
/** 币种字典，用于列表/表单展示 */
const currencyOptions = ref<DictOption[]>([]);
/** 采购性质字典 */
const purchaseNatureOptions = ref<DictOption[]>([]);
/** 常用税率选项（界面显示百分数，内部值为百分数数字字符串） */
const taxRateOptions = ref<DictOption[]>([
  { label: '3%', value: '3' },
  { label: '6%', value: '6' },
  { label: '9%', value: '9' },
  { label: '13%', value: '13' },
]);
const loading = ref(false);
const saving = ref(false);
const advancedSearchVisible = ref(false);
const currentYear = new Date().getFullYear();

const query = reactive({
  tenderCode: '',
  tenderName: '',
  purchaserName: '',
  tenderType: '',
  tenderMode: '',
  status: undefined as BiddingProjectStatus | undefined,
  year: currentYear as number | undefined,
  pageNum: 1,
  pageSize: 10,
  sort: undefined as string | undefined,
});
const YEAR_ALL = 'ALL';
const yearOptions = Array.from({ length: 20 }).map((_, idx) => currentYear - idx);
const yearSelectValue = computed({
  get: () => query.year ?? YEAR_ALL,
  set: (val) => {
    if (val === YEAR_ALL || val === undefined || val === null) {
      query.year = undefined;
      return;
    }
    query.year = Number(val);
  },
});

const activeFilterCount = computed(() => {
  let count = 0;
  if ((query.tenderCode || '').trim()) count += 1;
  if ((query.tenderName || '').trim()) count += 1;
  if ((query.purchaserName || '').trim()) count += 1;
  if (query.tenderType) count += 1;
  if (query.tenderMode) count += 1;
  if (query.status) count += 1;
  if (query.year != null && query.year !== currentYear) count += 1;
  return count;
});

const tableData = ref<TenderVO[]>([]);
const total = ref(0);


//
// function normalizeDateTime (value?: string) {
//   return (value || '').replace('T', ' ');
// }

//删除


async function deleteById(row: TenderVO) {
  if (!row?.tenderId) {
    ElMessage.warning('未获取到项目ID');
    return;
  }

  try {
    await ElMessageBox.confirm(
        `确认删除项目「${row.tenderName}」吗？`,
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
        }
    );

    loading.value = true;

    await deleteBiddingProject(Number(row.tenderId));

    ElMessage.success('删除成功');

    // 如果当前页只有一条数据，删除后回退一页
    if (tableData.value.length === 1 && query.pageNum > 1) {
      query.pageNum -= 1;
    }

    await fetchList();

  } catch (err: any) {
    // 用户点取消，不提示错误
    if (err !== 'cancel') {
      ElMessage.error(err?.message || '删除失败');
    }
  } finally {
    loading.value = false;
  }
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await queryBiddingProjectList({ ...query });
    tableData.value = res.list ?? [];
    total.value = Number((res as any)?.total ?? 0) || 0;
  } catch (e) {
    console.error('查询招标项目失败:', e);
    tableData.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await Promise.all([
    fetchPostDictOptions(),
    fetchTenderStatusOptions(),
  ]);
  await fetchList();
});

watch(
  () => route.query.edit,
  async (val) => {
    const id = typeof val === 'string' ? val : Array.isArray(val) ? val[0] : '';
    if (!id) return;
    await fetchList();
    openEditById(id);
  },
  { immediate: true },
);

function handleSearch() {
  query.tenderCode = (query.tenderCode || '').trim();
  query.tenderName = (query.tenderName || '').trim();
  query.purchaserName = (query.purchaserName || '').trim();
  query.pageNum = 1;
  fetchList();
}

function handleReset() {
  query.tenderCode = '';
  query.tenderName = '';
  query.purchaserName = '';
  query.tenderType = '';
  query.tenderMode = '';
  query.status = undefined;
  query.year = currentYear;
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
async function fetchPostDictOptions() {
  try {
    const [modeRes, typeRes, currencyRes, purchaseNatureRes] = await Promise.all([
      listDictOptions('tender_mode'),
      listDictOptions('tender_type'),
      listDictOptions('currency'),
      listDictOptions('purchase_nature'),
    ]);

    tenderModeList.value = normalizeDictOptions(modeRes);
    tenderTypeList.value = normalizeDictOptions(typeRes);
    currencyOptions.value = normalizeDictOptions(currencyRes);
    purchaseNatureOptions.value = normalizeDictOptions(purchaseNatureRes);
  } catch (e) {
    tenderModeList.value = [];
    tenderTypeList.value = [];
    currencyOptions.value = [];
    purchaseNatureOptions.value = [];
  }
}
function normalizeDictOptions(res: DictOption[] | { data?: DictOption[] }) {
  // 如果输入已经是数组格式，直接返回
  if (Array.isArray(res)) return res;
  // 如果输入是对象格式且包含data数组，则返回data数组
  return Array.isArray(res?.data) ? res.data : [];
}
// 弹窗表单
const editDialog = reactive({
  visible: false,
  isEdit: false,
});
const formRef = ref<FormInstance>();
const selectedOrg = ref<OrgNode>();
interface CompanyVO {
  companyId: string;
  companyCode: string;
  companyName: string;
}

const form = reactive({
  // 招标项目id
  tenderId: '',
  // 项目编码
  tenderCode: '',
  // 项目名称
  tenderName: '',
  // 招标类型
  tenderType: '',
  // 招标方式
  tenderMode: '',
  // 负责人
  owner: undefined as ExpPersonVO | undefined,
  // 招标人
  company: undefined as CompanyVO | undefined,
  // 归属组织
  orgId: undefined as number | undefined,
  // 预算金额
  budgetAmount: 0,
  // 是否含税
  isTaxIncluded: false,
  // 税率（界面用百分数存储，如 13 表示 13%）
  taxRatePercent: '' as string | number,
  // 采购性质（1 政府采购 2 企业采购 3 其他）
  purchaseNature: '',
  status: '未开始' as BiddingProjectStatus,

  currency: 'CNY',
  // 招标项目概要/公告摘要
  tenderBrief:'',
  // 招标发布时间
  publishTime: '',
  // 招标开始时间
  bidStartTime:'',
  // 招标截止时间
  bidEndTime: '',
  // 开标时间
  openTime: '',
  // 开标地点：级联选择器值 [省code, 市code, 区code]
  openAddressCascader: [] as string[],
  // 开标地点：详细地址
  openAddressDetail: '',
  // 关联项目id
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
  tenderCode: [
    { required: true, message: '请输入项目编码', trigger: 'blur' }
  ],
  tenderName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  tenderType: [
    { required: true, message: '请选择招标类型', trigger: 'change' }
  ],
  tenderMode: [
    { required: true, message: '请选择招标方式', trigger: 'change' }
  ],
  bidStartTime: [
    { required: true, message: '请选择投标开始时间', trigger: 'change' }
  ],
  bidEndTime: [
    { required: true, message: '请选择投标截止时间', trigger: 'change' }
  ],
  openTime: [
    { required: true, message: '请选择开标时间', trigger: 'change' }
  ],
  // 开标地点留空表示线上，不校验必填
  publishTime: [
    { required: true, message: '请选择发布时间', trigger: 'change' }
  ],
  company: [
    { required: true, message: '请选择招标人', trigger: 'change' }
  ],
  orgId: [
    { required: true, message: '请选择归属组织', trigger: 'change' }
  ],
  relatedProject: [
    { required: false, message: '请选择关联项目', trigger: 'change' }
  ],
  budgetAmount: [
    { required: true, message: '请输入预算金额', trigger: 'blur' }
  ],
  taxRatePercent: [
    { required: true, message: '请输入税率', trigger: 'change' },
    { validator: validateTaxRatePercent, trigger: 'change' },
  ],
  purchaseNature: [
    { required: true, message: '请选择采购性质', trigger: 'change' }
  ],
};

function handleOrgChange(org: OrgNode | undefined) {
  form.orgId = org?.orgId as number | undefined;
}

function openEdit(isEdit: boolean, row?: TenderVO) {
  editDialog.isEdit = isEdit;

  // 1. 重置表单验证状态
  if (formRef.value) {
    formRef.value.resetFields();
  }

  if (isEdit && row) {
    // 2. 编辑模式：将行数据（row）映射到表单（form）
    form.tenderId = row.tenderId;
    form.tenderCode = row.tenderCode;
    form.tenderName = row.tenderName;
    form.tenderType = row.tenderType || '';
    form.tenderMode = row.tenderMode || '';
    form.status = row.status;
  form.orgId = row.orgId != null ? Number(row.orgId) : undefined;
    form.budgetAmount = row.budgetAmount || 0;
    form.isTaxIncluded = row.isTaxIncluded ?? false;
    form.taxRatePercent =
      row.taxRate != null ? String(Number(row.taxRate) * 100) : '';
    applyTaxRateDisplay(form.taxRatePercent);
    form.purchaseNature = row.purchaseNature || '';
    form.bidStartTime = row.bidStartTime || '';
    form.bidEndTime = row.bidEndTime || '';
    form.openTime = row.openTime || '';
    form.remark = row.remark || '';
    form.tenderBrief = row.tenderBrief ?? '';
    form.currency = row.currency ?? 'CNY';
  form.publishTime = (row as any).publishTime || '';
    // 开标地点回显：解析为级联值 + 详细地址
    const parsed = parseOpenAddress(row.openAddress);
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

    // 3. 处理选择器组件的回显 (关键：构造对象)
    // 归属组织回显
    selectedOrg.value = row.orgId
      ? ({
          orgId: Number(row.orgId),
          orgName: row.orgName || '',
          orgCode: '',
          children: [],
        } as OrgNode)
      : undefined;
    // 负责人回显 - 强制断言
    form.owner = row.personId ? ({
      personId: row.personId,
      personName: row.personIdName
    } as any) : undefined;

    // 招标单位回显
    form.company = row.purchaserId ? {
      companyId: row.purchaserId,
      companyName: row.purchaserName
    } as any : undefined;

    // 关联项目回显 - 强制断言
    form.relatedProject = row.projectId ? ({
      projectId: row.projectId,
      projectName: row.projectName
    } as any) : undefined;

  } else {
    // 4. 新增模式：重置所有字段
    Object.assign(form, {
      tenderId: '',
      tenderCode: '',
      tenderName: '',
      tenderType: '',
      tenderMode: '',
      status: '未开始',
      budgetAmount: 0,
      isTaxIncluded: false,
      taxRatePercent: '',
      purchaseNature: '',
      owner: undefined,
      company: undefined,
      orgId: undefined,
      relatedProject: undefined,
      bidStartTime: '',
      bidEndTime: '',
      openTime: '',
      openAddressCascader: [],
      openAddressDetail: '',
      tenderBrief: '',
      currency: 'CNY',
      publishTime: '',
      remark: '',
    });
    // 自动生成项目编码
    autoGenerateProjectCode();
  }

  editDialog.visible = true;
}

function goDetail(row: TenderVO) {
  router.push(`/bidding/project/${row.tenderId}`);
}

async function openEditById(projectId: string) {
  const list = tableData.value;
  let row = list.find((x) => String((x as any).tenderId) === String(projectId));
  if (row) {
    openEdit(true, row);
    return;
  }
  // 从详情页带 edit 跳转时，当前页可能没有该项目，用详情接口拉取后打开编辑
  try {
    const detail = await getBiddingProjectDetail(projectId);
    openEdit(true, { ...detail, tenderId: detail.tenderId ?? projectId } as TenderVO);
  } catch {
    ElMessage.warning('未找到该项目');
  }
}

async function submitForm() {
  if (!formRef.value) return;

  applyTaxRateDisplay(form.taxRatePercent);
  const valid = await formRef.value.validate();
  if (!valid) return;

  const companyId = form.company?.companyId != null ? Number(form.company.companyId) : null;
  if (companyId == null || companyId === 0) {
    ElMessage.warning('请选择招标单位');
    return;
  }

  if (form.orgId == null) {
    ElMessage.warning('请选择归属组织');
    return;
  }

  // 开标地点：级联+详细地址拼接为 "省, 市, 区, 详细地址"，未选则传空字符串
  let openAddressValue: string | undefined;
  const cascader = form.openAddressCascader;
  const c0 = cascader?.[0];
  const c1 = cascader?.[1];
  const c2 = cascader?.[2];
  if (c0 != null && c1 != null && c2 != null && codeToText[c0] && codeToText[c1] && codeToText[c2]) {
    openAddressValue = buildOpenAddress(
      codeToText[c0],
      codeToText[c1],
      codeToText[c2],
      form.openAddressDetail || ''
    );
    if (openAddressValue.endsWith(', ')) openAddressValue = openAddressValue.slice(0, -2);
  } else {
    openAddressValue = '';
  }

  saving.value = true;
  try {
    const taxRatePercentNum = Number(form.taxRatePercent);
    const taxRateDecimal = taxRatePercentNum / 100;

    const basePayload: CreateTenderReq = {
      tenderCode: form.tenderCode || '',
      tenderName: form.tenderName || '',
      tenderType: form.tenderType || '',
      tenderMode: form.tenderMode || '',
      companyId,
      budgetAmount: Number(form.budgetAmount) || 0,
      currency: form.currency || 'CNY',
      taxRate: taxRateDecimal,
      isTaxIncluded: !!form.isTaxIncluded,
      purchaseNature: form.purchaseNature || '',
      tenderBrief: form.tenderBrief || undefined,
      publishTime: form.publishTime || '',
      bidStartTime: form.bidStartTime || '',
      bidEndTime: form.bidEndTime || '',
      openTime: form.openTime || undefined,
      openAddress: openAddressValue ?? '',
      projectId: form.relatedProject?.projectId != null ? Number(form.relatedProject.projectId) : undefined,
      personId: form.owner?.personId != null ? Number(form.owner.personId) : undefined,
      orgId: form.orgId != null ? Number(form.orgId) : undefined,
      remark: form.remark || undefined,
    };

    if (editDialog.isEdit && form.tenderId) {
      const { currency, ...rest } = basePayload;
      const updatePayload: UpdateTenderReq = { ...rest, tenderId: Number(form.tenderId) };
      await updateBiddingProject(updatePayload);
      ElMessage.success('更新成功');
    } else {
      await createBiddingProject(basePayload);
      ElMessage.success('创建成功');
    }
    editDialog.visible = false;
    await fetchList();
  } catch (e: any) {
    ElMessage.error(e?.message || (editDialog.isEdit ? '更新失败' : '创建失败'));
  } finally {
    saving.value = false;
  }
}
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
  justify-content: flex-end;
  margin-top: 20px;
  margin-right: 30px;
  gap: 8px;
}

.search-bar {
  margin-bottom: 12px;
  padding: 16px 16px 4px;
  background: #f7f9fc;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
}

.search-row + .search-row {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #dcdfe6;
}

.search-row :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-item {
  margin-right: 0;
}

.search-item :deep(.el-input),
.search-item :deep(.el-select) {
  width: 100%;
}

.search-item-keyword {
  width: 220px;
}

.search-item-select,
.search-item-status {
  width: 160px;
}

.search-item-year {
  width: 120px;
}

.search-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-summary {
  padding: 0 10px;
  line-height: 28px;
  border-radius: 999px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
}

@media (max-width: 1400px) {
  .search-actions {
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 768px) {
  .search-item-keyword,
  .search-item-select,
  .search-item-status,
  .search-item-year {
    width: 100%;
  }

  .search-actions {
    justify-content: flex-start;
  }
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

.open-address-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
/* 在 .open-address-row 样式后面添加 */
.budget-row {
  display: flex;         /* 强制横向排列 */
  align-items: center;   /* 垂直居中 */
  width: 100%;

  :deep(.el-input-number) {
    flex: 1;             /* 关键：让数字输入框填满剩余空间 */
  }

  :deep(.el-checkbox) {
    margin-left: 12px;   /* 增加间距 */
    flex-shrink: 0;      /* 防止含税两个字被挤扁 */
  }
}
</style>
