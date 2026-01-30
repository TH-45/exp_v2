<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">合同变更</div>
          <div class="actions">
            <el-button type="primary" size="small" :disabled="!canManage" @click="openEdit(false)">
              新增变更
            </el-button>
            <el-button size="small" :disabled="true">导出</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="变更单号/合同/原因" clearable style="width: 260px" />
        </el-form-item>
        <el-form-item label="合同编码">
          <el-input v-model="query.contractCode" placeholder="合同编码" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 180px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已生效" value="EFFECTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" row-key="changeId" border style="width: 100%">
        <el-table-column prop="changeCode" label="变更单号" min-width="160" />
        <el-table-column prop="contractCode" label="合同编码" min-width="160" />
        <el-table-column prop="contractName" label="合同名称" min-width="220" />
        <el-table-column prop="reason" label="变更原因" min-width="240" />
        <el-table-column prop="deltaAmount" label="金额变动(万)" min-width="140" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'EFFECTIVE' ? 'success' : 'info'">
              {{ row.status === 'EFFECTIVE' ? '已生效' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :disabled="!canManage" @click="openEdit(true, row)">
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

      <el-dialog v-model="editDialog.visible" :title="editDialog.isEdit ? '编辑变更' : '新增变更'" width="860px" destroy-on-close>
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="110px"
          class="dialog-form two-col"
          @submit.prevent="submitForm"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="变更单号" prop="changeCode">
            <el-input v-model="form.changeCode" placeholder="请输入变更单号" />
          </el-form-item>
          <el-form-item label="合同编码" prop="contractCode">
            <el-input v-model="form.contractCode" placeholder="请输入合同编码" />
          </el-form-item>
          <el-form-item label="合同名称" prop="contractName">
            <el-input v-model="form.contractName" placeholder="请输入合同名称（占位）" />
          </el-form-item>
          <el-form-item label="金额变动(万)" prop="deltaAmount">
            <el-input-number v-model="form.deltaAmount" :min="-999999999" :max="999999999" style="width: 100%" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已生效" value="EFFECTIVE" />
            </el-select>
          </el-form-item>
          <el-form-item label="变更原因" class="full-row" prop="reason">
            <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入变更原因" @keydown.enter.stop />
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
import { hasPermission } from '@/utils/permission';
import { useRouter } from 'vue-router';

const router = useRouter();

const canManage = computed(() => hasPermission('contracts:change:manage'));

type ChangeStatus = 'DRAFT' | 'EFFECTIVE';
type ChangeRow = {
  changeId: string;
  changeCode: string;
  contractCode: string;
  contractName: string;
  reason: string;
  deltaAmount: number;
  status: ChangeStatus;
  time: string;
};

const loading = ref(false);
const saving = ref(false);
const total = ref(0);

const query = reactive({
  keyword: '',
  contractCode: '',
  status: undefined as ChangeStatus | undefined,
  page: 1,
  pageSize: 10,
});

const mockList: ChangeRow[] = Array.from({ length: 15 }).map((_, idx) => ({
  changeId: String(idx + 1),
  changeCode: `BG-2025-${String(idx + 1).padStart(4, '0')}`,
  contractCode: `HT-2025-${String((idx % 6) + 1).padStart(4, '0')}`,
  contractName: `示例合同 ${(idx % 6) + 1}`,
  reason: idx % 2 === 0 ? '范围调整' : '金额调整',
  deltaAmount: idx % 2 === 0 ? 10 : -5,
  status: idx % 3 === 0 ? 'EFFECTIVE' : 'DRAFT',
  time: '2025-04-01 10:00:00',
}));

const tableData = ref<ChangeRow[]>([]);

async function fetchList() {
  loading.value = true;
  try {
    // 后续接接口：/exp/contracts/change/list
    tableData.value = mockList;
    total.value = mockList.length;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchList();
});

function goDetail(row: ChangeRow) {
  router.push(`/contracts/change/${row.changeId}`);
}

function handleSearch() {
  query.keyword = (query.keyword || '').trim();
  query.contractCode = (query.contractCode || '').trim();
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.keyword = '';
  query.contractCode = '';
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

const editDialog = reactive({ visible: false, isEdit: false });
const formRef = ref<FormInstance>();
const form = reactive({
  changeId: '',
  changeCode: '',
  contractCode: '',
  contractName: '',
  reason: '',
  deltaAmount: 0,
  status: 'DRAFT' as ChangeStatus,
});

const rules: FormRules = {
  changeCode: [{ required: true, message: '请输入变更单号', trigger: 'blur' }],
  contractCode: [{ required: true, message: '请输入合同编码', trigger: 'blur' }],
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入变更原因', trigger: 'blur' }],
  deltaAmount: [{ required: true, message: '请输入金额变动', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

function openEdit(isEdit: boolean, row?: ChangeRow) {
  editDialog.isEdit = isEdit;
  if (isEdit && row) {
    Object.assign(form, row);
  } else {
    form.changeId = '';
    form.changeCode = '';
    form.contractCode = '';
    form.contractName = '';
    form.reason = '';
    form.deltaAmount = 0;
    form.status = 'DRAFT';
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



