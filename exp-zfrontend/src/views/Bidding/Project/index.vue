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
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="项目编码">
          <el-input v-model="query.tenderCode" placeholder="请输入项目编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model="query.tenderName" placeholder="请输入项目名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="招标单位">
          <el-input v-model="query.purchaserName" placeholder="请输入招标单位" clearable style="width: 200px" />
        </el-form-item>
        <!-- 强制换行 -->
        <div style="flex-basis: 100%; height: 0;"></div>
        <el-form-item label="招标方式">
          <el-select v-model="query.tenderMode" clearable style="width: 140px">
            <el-option v-for="t in tenderModeList" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="招标类型">
          <el-select v-model="query.tenderType" clearable style="width: 140px">
            <el-option v-for="t in tenderTypeList" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 100px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="年度">
          <el-select v-model="yearSelectValue" clearable style="width: 100px">
            <el-option label="全部" :value="YEAR_ALL" />
            <el-option v-for="y in yearOptions" :key="y" :label="String(y)" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" row-key="tenderId" border style="width: 100%">
        <el-table-column prop="tenderCode" label="项目编码" min-width="120" />
        <el-table-column prop="tenderName" label="项目名称" min-width="200" />
        <el-table-column prop="purchaserName" label="招标单位" min-width="180" />
        <el-table-column prop="orgName" label="归属部门" min-width="120" />
        <el-table-column prop="personIdName" label="负责人" min-width="120" />
        <el-table-column prop="salesmanName" label="业务员" min-width="170" />
        <el-table-column label="招标方式" min-width="100">
          <template #default="{ row }">
            {{ formatTenderMode(row.tenderMode) }}
          </template>
        </el-table-column>

        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bidStartTime" label="开标时间" min-width="170" />
        <el-table-column prop="bidEndTime" label="截止时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(true, row)" :disabled="!canManage">
              编辑
            </el-button>
            <el-button link size="small" @click="goDetail(row)">详情</el-button>
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
            <el-input v-model="form.tenderCode" placeholder="请输入项目编码" />
          </el-form-item>
          <el-form-item label="项目名称" prop="tenderName">
            <el-input v-model="form.tenderName" placeholder="请输入项目名称" />
          </el-form-item>
          <el-form-item label="招标单位" prop="company">
            <CompanySelector v-model="form.company" />
          </el-form-item>
          <el-form-item label="负责人" prop="owner">
            <PersonSelector v-model="form.owner" />
          </el-form-item>
          <el-form-item label="关联项目" prop="relatedProject">
            <ProjectSelector v-model="form.relatedProject" />
          </el-form-item>
          <el-form-item label="预算金额(万)" prop="budgetAmount">
            <el-input-number v-model="form.budgetAmount" :min="0" :max="999999999" style="width: 100%" />
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
          <el-form-item label="投标截止时间" prop="bidEndTime">
            <el-date-picker
              v-model="form.bidEndTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择投标截止时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="开标时间" prop="bidStartTime">
            <el-date-picker
              v-model="form.bidStartTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择开标时间"
              style="width: 100%"
            />
          </el-form-item>
<!--          <el-form-item label="状态" prop="status">-->
<!--            <el-select v-model="form.status" style="width: 100%">-->
<!--              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />-->
<!--            </el-select>-->
<!--          </el-form-item>-->
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
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import { generateProjectCode } from '@/utils/codeGenerator';
import type { ExpPersonVO } from '@/api/system/person';
import PersonSelector from '@/components/Selector/PersonSelector.vue';
import CompanySelector from '@/components/Selector/CompanySelector.vue';
import ProjectSelector from '@/components/Selector/ProjectSelector.vue'
import type { ProjectVO } from '@/api/corpProject/project'
import {
  queryBiddingProjectList,
  createBiddingProject,
  updateBiddingProject,
  type TenderVO,
  type BiddingProjectStatus,
  type CreateTenderReq,
  type UpdateTenderReq,
} from '@/api/bidding/project';

import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';
import {type DictOption, listDictOptions} from "@/api/system/dict.ts";

const route = useRoute();
const router = useRouter();
const canManage = computed(() => hasPermission('bidding:project:manage'));

const statusOptions: Array<{ label: string; value: BiddingProjectStatus }> = [
  { label: '未开始', value: '未开始' },
  { label: '进行中', value: '进行中' },
  { label: '已结束', value: '已结束' },
];

function statusText(s: BiddingProjectStatus) {
  return statusOptions.find((x) => x.value === s)?.label || s;
}

function statusTagType(s: BiddingProjectStatus) {
  if (s === '未开始') return 'info';
  if (s === '进行中') return 'warning';
  if (s === '已结束') return 'success';
  return '';
}
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

const tenderModeList = ref<DictOption[]>([]);
const tenderTypeList = ref<DictOption[]>([]);
const loading = ref(false);
const saving = ref(false);

const query = reactive({
  tenderCode: '',
  tenderName: '',
  purchaserName: '',
  tenderType: '',
  tenderMode: '',
  status: undefined as BiddingProjectStatus | undefined,
  year: new Date().getFullYear() as number | undefined,
  pageNum: 1,
  pageSize: 10,
  sort: undefined as string | undefined,
});
const YEAR_ALL = 'ALL';
const yearOptions = Array.from({ length: 20 }).map((_, idx) => new Date().getFullYear() - idx);
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

const tableData = ref<TenderVO[]>([]);
const total = ref(0);


//
// function normalizeDateTime (value?: string) {
//   return (value || '').replace('T', ' ');
// }



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
  query.status = undefined;
  query.year = new Date().getFullYear();
  query.pageNum = 1;
  fetchList();
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
    const [modeRes, typeRes] = await Promise.all([
      listDictOptions('tender_mode'),
      listDictOptions('tender_type'),
    ]);

    tenderModeList.value = normalizeDictOptions(modeRes);
    tenderTypeList.value = normalizeDictOptions(typeRes);
  } catch (e) {
    tenderModeList.value = [];
    tenderTypeList.value = [];
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
  // 预算金额
  budgetAmount: 0,
  status: '未开始' as BiddingProjectStatus,

  currency: 'CNY',
  // 招标项目概要/公告摘要
  tenderBrief:'',
  // 招标开始时间
  bidStartTime:'',
  // 招标截止时间
  bidEndTime: '',
  // 开标时间
  openTime: '',
  // 开标地点
  openAddress: '',
  // 关联项目id
  relatedProject: undefined as ProjectVO | undefined,
  remark: '',
});

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
  bidEndTime: [
    { required: true, message: '请选择投标截止时间', trigger: 'change' }
  ],
  openTime: [
    { required: true, message: '请选择开标时间', trigger: 'change' }
  ],
  openAddress: [
    { required: true, message: '请输入开标地点', trigger: 'blur' }
  ],
   owner: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ],
  company: [
    { required: true, message: '请选择招标人', trigger: 'change' }
  ],
  relatedProject: [
    { required: false, message: '请选择关联项目', trigger: 'change' }
  ],
  budgetAmount: [
    { required: true, message: '请输入预算金额', trigger: 'blur' }
  ],


};

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
    form.budgetAmount = row.budgetAmount || 0;
    form.bidStartTime = row.bidStartTime || '';
    form.bidEndTime = row.bidEndTime || '';
    form.openTime = ''; // 注意：后端返回可能是 openTime 或其他，需根据后端字段名调整
    form.remark = row.remark || '';
    form.openAddress = row.openAddress || '';

    // 3. 处理选择器组件的回显 (关键：构造对象)
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
      owner: undefined,
      company: undefined,
      relatedProject: undefined,
      bidStartTime: '',
      bidEndTime: '',
      openTime: '',
      openAddress: '',
      remark: '',
    });
    // 自动生成项目编码
    autoGenerateProjectCode();
  }

  editDialog.visible = true;
}

function goDetail(row: TenderVO) {
  router.push(`/bidding/tender/${row.tenderId}`);
}

function openEditById(projectId: string) {
  const list = tableData.value;
  const row = list.find((x) => String((x as any).tenderId) === String(projectId));
  if (row) {
    openEdit(true, row);
    return;
  }
  if (!row) {
    ElMessage.warning('未找到该项目');
    return;
  }
}

async function submitForm() {
  if (!formRef.value) return;

  const valid = await formRef.value.validate();
  if (!valid) return;

  const companyId = form.company?.companyId != null ? Number(form.company.companyId) : null;
  if (companyId == null || companyId === 0) {
    ElMessage.warning('请选择招标单位');
    return;
  }

  saving.value = true;
  try {
    const basePayload: CreateTenderReq = {
      tenderCode: form.tenderCode || '',
      tenderName: form.tenderName || '',
      tenderType: form.tenderType || '',
      tenderMode: form.tenderMode || '',
      companyId,
      budgetAmount: Number(form.budgetAmount) || 0,
      currency: form.currency || 'CNY',
      tenderBrief: form.tenderBrief || undefined,
      bidStartTime: form.bidStartTime || '',
      bidEndTime: form.bidEndTime || '',
      openTime: form.openTime || undefined,
      openAddress: form.openAddress || undefined,
      projectId: form.relatedProject?.projectId != null ? Number(form.relatedProject.projectId) : undefined,
      remark: form.remark || undefined,
    };

    if (editDialog.isEdit && form.tenderId) {
      const updatePayload: UpdateTenderReq = { ...basePayload, tenderId: Number(form.tenderId) };
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
</style>


