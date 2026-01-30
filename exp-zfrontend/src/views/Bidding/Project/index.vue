<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">招标项目</div>
          <div class="actions">
            <el-button type="primary" size="small" @click="openEdit(false)" :disabled="!canManage">
              新增项目
            </el-button>
            <el-button size="small" :disabled="true">导入</el-button>
            <el-button size="small" :disabled="true">导出</el-button>
          </div>
        </div>
      </template>

      <!-- 查询栏 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="项目编码/名称/招标单位"
            clearable
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 180px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="年度">
          <el-input-number v-model="query.year" :min="2000" :max="2100" :controls="false" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" row-key="projectId" border style="width: 100%">
        <el-table-column prop="projectCode" label="项目编码" min-width="140" />
        <el-table-column prop="projectName" label="项目名称" min-width="200" />
        <el-table-column prop="tenderOrg" label="招标单位" min-width="180" />
        <el-table-column prop="ownerName" label="负责人" min-width="120" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="180">
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
          :page-size="query.size"
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
          <el-form-item label="项目编码" prop="projectCode">
            <el-input v-model="form.projectCode" placeholder="请输入项目编码" />
          </el-form-item>
          <el-form-item label="项目名称" prop="projectName">
            <el-input v-model="form.projectName" placeholder="请输入项目名称" />
          </el-form-item>
          <el-form-item label="招标单位">
            <el-input v-model="form.tenderOrg" placeholder="请输入招标单位" />
          </el-form-item>
          <el-form-item label="负责人">
            <el-input v-model="form.ownerName" placeholder="请输入负责人" />
          </el-form-item>
          <el-form-item label="预算金额(万)">
            <el-input-number v-model="form.budgetAmount" :min="0" :max="999999999" style="width: 100%" />
          </el-form-item>
          <el-form-item label="招标方式">
            <el-select v-model="form.tenderMethod" clearable style="width: 100%">
              <el-option label="公开招标" value="OPEN" />
              <el-option label="邀请招标" value="INVITE" />
              <el-option label="竞争性谈判" value="NEGOTIATION" />
            </el-select>
          </el-form-item>
          <el-form-item label="投标截止时间">
            <el-date-picker
              v-model="form.bidDeadline"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择投标截止时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="开标时间">
            <el-date-picker
              v-model="form.openBidTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择开标时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" style="width: 100%">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
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
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import {
  queryBiddingProjectList,
  type BiddingProjectVO,
  type BiddingProjectStatus,
} from '@/api/bidding/project';

import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';

const route = useRoute();
const router = useRouter();
const canManage = computed(() => hasPermission('bidding:project:manage'));

const statusOptions: Array<{ label: string; value: BiddingProjectStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '投标中', value: 'BIDDING' },
  { label: '评标中', value: 'EVALUATING' },
  { label: '已定标', value: 'AWARDED' },
  { label: '已归档', value: 'ARCHIVED' },
];

function statusText(s: BiddingProjectStatus) {
  return statusOptions.find((x) => x.value === s)?.label || s;
}

function statusTagType(s: BiddingProjectStatus) {
  if (s === 'DRAFT') return 'info';
  if (s === 'PUBLISHED') return 'success';
  if (s === 'BIDDING') return 'warning';
  if (s === 'EVALUATING') return 'warning';
  if (s === 'AWARDED') return 'success';
  if (s === 'ARCHIVED') return 'info';
  return '';
}

const loading = ref(false);
const saving = ref(false);

const query = reactive({
  keyword: '',
  status: undefined as BiddingProjectStatus | undefined,
  year: new Date().getFullYear(),
  pageNum: 1,
  size: 10,
  sort: undefined as string | undefined,
});

const tableData = ref<BiddingProjectVO[]>([]);
const total = ref(0);

const mockList: BiddingProjectVO[] = Array.from({ length: 23 }).map((_, idx) => ({
  projectId: String(idx + 1),
  projectCode: `TB-${query.year}-${String(idx + 1).padStart(3, '0')}`,
  projectName: `示例招标项目 ${idx + 1}`,
  tenderOrg: idx % 2 === 0 ? '总部' : '分公司A',
  ownerName: idx % 3 === 0 ? '张三' : idx % 3 === 1 ? '李四' : '王五',
  status: statusOptions[idx % statusOptions.length].value,
  createdTime: '2025-01-01 10:00:00',
}));

async function fetchList() {
  loading.value = true;
  try {
    const res = await queryBiddingProjectList({ ...query });
    const records = (res as any)?.list ?? [];
    tableData.value = Array.isArray(records) && records.length ? records : mockList;
    total.value = Number((res as any)?.total ?? tableData.value.length) || 0;
  } catch (e) {
    tableData.value = mockList;
    total.value = mockList.length;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchList();
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
  query.keyword = (query.keyword || '').trim();
  query.pageNum = 1;
  fetchList();
}

function handleReset() {
  query.keyword = '';
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
  query.size = size;
  query.pageNum = 1;
  fetchList();
}

// 弹窗表单
const editDialog = reactive({
  visible: false,
  isEdit: false,
});
const formRef = ref<FormInstance>();
const form = reactive({
  projectId: '',
  projectCode: '',
  projectName: '',
  tenderOrg: '',
  ownerName: '',
  status: 'DRAFT' as BiddingProjectStatus,
  budgetAmount: 0,
  tenderMethod: '' as '' | 'OPEN' | 'INVITE' | 'NEGOTIATION',
  bidDeadline: '',
  openBidTime: '',
  remark: '',
});

const rules: FormRules = {
  projectCode: [{ required: true, message: '请输入项目编码', trigger: 'blur' }],
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

function openEdit(isEdit: boolean, row?: BiddingProjectVO) {
  editDialog.isEdit = isEdit;
  if (isEdit && row) {
    form.projectId = row.projectId;
    form.projectCode = row.projectCode;
    form.projectName = row.projectName;
    form.tenderOrg = row.tenderOrg || '';
    form.ownerName = row.ownerName || '';
    form.status = row.status;
  } else {
    form.projectId = '';
    form.projectCode = '';
    form.projectName = '';
    form.tenderOrg = '';
    form.ownerName = '';
    form.status = 'DRAFT';
    form.budgetAmount = 0;
    form.tenderMethod = 'OPEN';
    form.bidDeadline = '';
    form.openBidTime = '';
    form.remark = '';
  }
  editDialog.visible = true;
}

function goDetail(row: BiddingProjectVO) {
  router.push(`/bidding/project/${row.projectId}`);
}

function openEditById(projectId: string) {
  const list = tableData.value.length ? tableData.value : mockList;
  const row = list.find((x) => String((x as any).projectId) === String(projectId));
  if (row) {
    openEdit(true, row);
    return;
  }
  openEdit(true, {
    projectId,
    projectCode: `TB-${query.year}-${String(projectId).padStart(3, '0')}`,
    projectName: `示例招标项目 ${projectId}`,
    tenderOrg: '',
    ownerName: '',
    status: 'DRAFT',
    createdTime: '',
  });
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
    // 暂未接后端 create/update：先做交互闭环（你后续确认接口再补）
    ElMessage.success(editDialog.isEdit ? '已保存（示例模式）' : '已新增（示例模式）');
    editDialog.visible = false;
    fetchList();
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


