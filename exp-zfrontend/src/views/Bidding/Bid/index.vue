<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">投标登记</div>
          <div class="actions">
            <el-button type="primary" size="small" @click="openEdit(false)" :disabled="!canManage">
              新增投标
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
            placeholder="项目编码/名称/投标人"
            clearable
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="投标人">
          <el-input v-model="query.bidderName" placeholder="请输入投标人" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 180px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="bidId"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="projectCode" label="项目编码" min-width="140" />
        <el-table-column prop="projectName" label="项目名称" min-width="200" />
        <el-table-column prop="bidderName" label="投标人" min-width="200" />
        <el-table-column prop="amount" label="报价(万)" min-width="120" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="登记时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(true, row)" :disabled="!canManage">
              编辑
            </el-button>
            <el-button link size="small" :disabled="true">详情</el-button>
            <el-button link type="danger" size="small" :disabled="true">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page="query.page"
          :page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>

      <!-- 新增/编辑弹窗 -->
      <el-dialog v-model="editDialog.visible" :title="editDialog.isEdit ? '编辑投标' : '新增投标'" width="860px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="dialog-form two-col">
          <el-form-item label="项目ID" prop="projectId">
            <el-input v-model="form.projectId" placeholder="占位：后续替换为项目选择" />
          </el-form-item>
          <el-form-item label="投标人" prop="bidderName">
            <el-input v-model="form.bidderName" placeholder="请输入投标人名称" />
          </el-form-item>
          <el-form-item label="报价(万)" prop="amount">
            <el-input-number v-model="form.amount" :min="0" :max="999999999" style="width: 100%" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" style="width: 100%">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" class="full-row">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选" />
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
import { onMounted, reactive, ref, computed } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import { queryBidList, type BidVO, type BidStatus } from '@/api/bidding/bid';

const canManage = computed(() => hasPermission('bidding:bid:manage'));

const statusOptions: Array<{ label: string; value: BidStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '有效', value: 'VALID' },
  { label: '已撤回', value: 'WITHDRAWN' },
  { label: '不通过', value: 'REJECTED' },
];

function statusText(s: BidStatus) {
  return statusOptions.find((x) => x.value === s)?.label || s;
}

function statusTagType(s: BidStatus) {
  if (s === 'DRAFT') return 'info';
  if (s === 'SUBMITTED') return 'warning';
  if (s === 'VALID') return 'success';
  if (s === 'WITHDRAWN') return 'info';
  if (s === 'REJECTED') return 'danger';
  return '';
}

const loading = ref(false);
const saving = ref(false);

const query = reactive({
  keyword: '',
  bidderName: '',
  status: undefined as BidStatus | undefined,
  page: 1,
  pageSize: 10,
});

const tableData = ref<BidVO[]>([]);
const total = ref(0);
const selectedRows = ref<BidVO[]>([]);

const mockList: BidVO[] = Array.from({ length: 18 }).map((_, idx) => ({
  bidId: String(idx + 1),
  projectId: String((idx % 6) + 1),
  projectCode: `TB-2025-${String((idx % 6) + 1).padStart(3, '0')}`,
  projectName: `示例招标项目 ${(idx % 6) + 1}`,
  bidderName: `供应商${String.fromCharCode(65 + (idx % 5))}`,
  amount: 100 + idx,
  status: statusOptions[idx % statusOptions.length].value,
  createdTime: '2025-01-15 10:00:00',
}));

async function fetchList() {
  loading.value = true;
  try {
    const res = await queryBidList({ ...query });
    const records = (res as any)?.records ?? [];
    tableData.value = Array.isArray(records) && records.length ? records : mockList;
    total.value = Number((res as any)?.total ?? tableData.value.length) || 0;
  } catch (e) {
    tableData.value = mockList;
    total.value = mockList.length;
  } finally {
    loading.value = false;
    selectedRows.value = [];
  }
}

onMounted(() => {
  fetchList();
});

function handleSearch() {
  query.keyword = (query.keyword || '').trim();
  query.bidderName = (query.bidderName || '').trim();
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.keyword = '';
  query.bidderName = '';
  query.status = undefined;
  query.page = 1;
  fetchList();
}

function handleCurrentChange(page: number) {
  query.page = page;
  fetchList();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.page = 1;
  fetchList();
}

function handleSelectionChange(rows: BidVO[]) {
  selectedRows.value = rows;
}

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const form = reactive({
  bidId: '',
  projectId: '',
  bidderName: '',
  amount: 0,
  status: 'DRAFT' as BidStatus,
  remark: '',
});

const rules: FormRules = {
  projectId: [{ required: true, message: '请输入项目ID（占位）', trigger: 'blur' }],
  bidderName: [{ required: true, message: '请输入投标人', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入报价', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

function openEdit(isEdit: boolean, row?: BidVO) {
  editDialog.isEdit = isEdit;
  if (isEdit && row) {
    form.bidId = row.bidId;
    form.projectId = row.projectId;
    form.bidderName = row.bidderName;
    form.amount = Number(row.amount ?? 0) || 0;
    form.status = row.status;
    form.remark = '';
  } else {
    form.bidId = '';
    form.projectId = '';
    form.bidderName = '';
    form.amount = 0;
    form.status = 'DRAFT';
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


