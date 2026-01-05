<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">合同台账</div>
          <div class="actions">
            <el-button type="primary" size="small" @click="openEdit(false)" :disabled="!canManage">
              新增合同
            </el-button>
            <el-button size="small" :disabled="true">导入</el-button>
            <el-button size="small" :disabled="true">导出</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="合同编码/名称/供应商/项目" clearable style="width: 260px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 180px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="query.supplierName" placeholder="供应商名称" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" row-key="contractId" border style="width: 100%">
        <el-table-column prop="contractCode" label="合同编码" min-width="160" />
        <el-table-column prop="contractName" label="合同名称" min-width="220" />
        <el-table-column prop="projectName" label="关联项目" min-width="200" />
        <el-table-column prop="supplierName" label="供应商" min-width="200" />
        <el-table-column prop="amount" label="合同金额(万)" min-width="140" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signDate" label="签订日期" min-width="140" />
        <el-table-column prop="createdTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(true, row)" :disabled="!canManage">
              编辑
            </el-button>
            <el-button link size="small" @click="goDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

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

      <!-- 新增/编辑弹窗（你统一选了弹窗模式） -->
      <el-dialog v-model="editDialog.visible" :title="editDialog.isEdit ? '编辑合同' : '新增合同'" width="900px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="dialog-form two-col">
          <el-form-item label="合同编码" prop="contractCode">
            <el-input v-model="form.contractCode" placeholder="请输入合同编码" />
          </el-form-item>
          <el-form-item label="合同名称" prop="contractName">
            <el-input v-model="form.contractName" placeholder="请输入合同名称" />
          </el-form-item>
          <el-form-item label="关联项目">
            <el-input v-model="form.projectName" placeholder="占位：后续替换为项目选择" />
          </el-form-item>
          <el-form-item label="供应商" prop="supplierName">
            <el-input v-model="form.supplierName" placeholder="请输入供应商" />
          </el-form-item>
          <el-form-item label="合同金额(万)" prop="amount">
            <el-input-number v-model="form.amount" :min="0" :max="999999999" style="width: 100%" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" style="width: 100%">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="签订日期">
            <el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
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
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRouter } from 'vue-router';
import { hasPermission } from '@/utils/permission';
import { queryContractList, type ContractVO, type ContractStatus } from '@/api/contracts/contract';

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

const loading = ref(false);
const saving = ref(false);
const total = ref(0);

const query = reactive({
  keyword: '',
  status: undefined as ContractStatus | undefined,
  supplierName: '',
  page: 1,
  pageSize: 10,
});

const tableData = ref<ContractVO[]>([]);

const mockList: ContractVO[] = Array.from({ length: 22 }).map((_, idx) => ({
  contractId: String(idx + 1),
  contractCode: `HT-2025-${String(idx + 1).padStart(4, '0')}`,
  contractName: `示例合同 ${idx + 1}`,
  projectId: String((idx % 6) + 1),
  projectName: `示例招标项目 ${(idx % 6) + 1}`,
  supplierName: `供应商${String.fromCharCode(65 + (idx % 5))}`,
  amount: 200 + idx,
  status: statusOptions[idx % statusOptions.length].value,
  signDate: '2025-02-01',
  startDate: '2025-02-01',
  endDate: '2026-02-01',
  createdTime: '2025-02-01 10:00:00',
}));

async function fetchList() {
  loading.value = true;
  try {
    const res = await queryContractList({ ...query });
    const records = (res as any)?.records ?? [];
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

function handleSearch() {
  query.keyword = (query.keyword || '').trim();
  query.supplierName = (query.supplierName || '').trim();
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.keyword = '';
  query.status = undefined;
  query.supplierName = '';
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

function goDetail(row: ContractVO) {
  router.push(`/contracts/contract/${row.contractId}`);
}

// 弹窗表单
const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const form = reactive({
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

const rules: FormRules = {
  contractCode: [{ required: true, message: '请输入合同编码', trigger: 'blur' }],
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  supplierName: [{ required: true, message: '请输入供应商', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入合同金额', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

function openEdit(isEdit: boolean, row?: ContractVO) {
  editDialog.isEdit = isEdit;
  if (isEdit && row) {
    form.contractId = row.contractId;
    form.contractCode = row.contractCode;
    form.contractName = row.contractName;
    form.projectName = row.projectName || '';
    form.supplierName = row.supplierName || '';
    form.amount = Number(row.amount ?? 0) || 0;
    form.status = row.status;
    form.signDate = row.signDate || '';
    form.startDate = row.startDate || '';
    form.endDate = row.endDate || '';
    form.remark = '';
  } else {
    form.contractId = '';
    form.contractCode = '';
    form.contractName = '';
    form.projectName = '';
    form.supplierName = '';
    form.amount = 0;
    form.status = 'DRAFT';
    form.signDate = '';
    form.startDate = '';
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


