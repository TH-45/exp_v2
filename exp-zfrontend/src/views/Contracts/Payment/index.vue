<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">收付款台账</div>
          <div class="actions">
            <el-button type="primary" size="small" :disabled="!canManage" @click="openEdit(false)">
              新增记录
            </el-button>
            <el-button size="small" :disabled="true">导出</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="合同/项目/供应商" clearable style="width: 260px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" clearable style="width: 160px">
            <el-option label="付款" value="PAY" />
            <el-option label="收款" value="RECEIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 160px">
            <el-option label="未完成" value="PENDING" />
            <el-option label="已完成" value="DONE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" row-key="paymentId" border style="width: 100%">
        <el-table-column prop="contractCode" label="合同编码" min-width="160" />
        <el-table-column prop="contractName" label="合同名称" min-width="220" />
        <el-table-column prop="supplierName" label="供应商" min-width="200" />
        <el-table-column label="类型" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'PAY' ? 'warning' : 'success'">
              {{ row.type === 'PAY' ? '付款' : '收款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="planAmount" label="计划金额(万)" min-width="140" />
        <el-table-column prop="actualAmount" label="实际金额(万)" min-width="140" />
        <el-table-column prop="planDate" label="计划日期" min-width="140" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'DONE' ? 'success' : 'info'">
              {{ row.status === 'DONE' ? '已完成' : '未完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="160">
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

      <el-dialog v-model="editDialog.visible" :title="editDialog.isEdit ? '编辑记录' : '新增记录'" width="860px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="dialog-form two-col">
          <el-form-item label="合同编码" prop="contractCode">
            <el-input v-model="form.contractCode" placeholder="请输入合同编码" />
          </el-form-item>
          <el-form-item label="合同名称" prop="contractName">
            <el-input v-model="form.contractName" placeholder="请输入合同名称（占位）" />
          </el-form-item>
          <el-form-item label="供应商" prop="supplierName">
            <el-input v-model="form.supplierName" placeholder="请输入供应商" />
          </el-form-item>
          <el-form-item label="类型" prop="type">
            <el-select v-model="form.type" style="width: 100%">
              <el-option label="付款" value="PAY" />
              <el-option label="收款" value="RECEIVE" />
            </el-select>
          </el-form-item>
          <el-form-item label="计划金额(万)" prop="planAmount">
            <el-input-number v-model="form.planAmount" :min="0" :max="999999999" style="width: 100%" />
          </el-form-item>
          <el-form-item label="实际金额(万)">
            <el-input-number v-model="form.actualAmount" :min="0" :max="999999999" style="width: 100%" />
          </el-form-item>
          <el-form-item label="计划日期">
            <el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="未完成" value="PENDING" />
              <el-option label="已完成" value="DONE" />
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
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import { useRouter } from 'vue-router';

const router = useRouter();

const canManage = computed(() => hasPermission('contracts:payment:manage'));

type PayType = 'PAY' | 'RECEIVE';
type PayStatus = 'PENDING' | 'DONE';
type PayRow = {
  paymentId: string;
  contractCode: string;
  contractName: string;
  supplierName: string;
  type: PayType;
  planAmount: number;
  actualAmount: number;
  planDate: string;
  status: PayStatus;
  remark?: string;
};

const loading = ref(false);
const saving = ref(false);
const total = ref(0);

const query = reactive({
  keyword: '',
  type: undefined as PayType | undefined,
  status: undefined as PayStatus | undefined,
  page: 1,
  pageSize: 10,
});

const mockList: PayRow[] = Array.from({ length: 18 }).map((_, idx) => ({
  paymentId: String(idx + 1),
  contractCode: `HT-2025-${String((idx % 6) + 1).padStart(4, '0')}`,
  contractName: `示例合同 ${(idx % 6) + 1}`,
  supplierName: `供应商${String.fromCharCode(65 + (idx % 5))}`,
  type: idx % 2 === 0 ? 'PAY' : 'RECEIVE',
  planAmount: 50 + idx,
  actualAmount: idx % 3 === 0 ? 20 : 0,
  planDate: '2025-03-01',
  status: idx % 3 === 0 ? 'DONE' : 'PENDING',
  remark: '',
}));

const tableData = ref<PayRow[]>([]);

async function fetchList() {
  loading.value = true;
  try {
    // 后续接接口：/exp/contracts/payment/list
    tableData.value = mockList;
    total.value = mockList.length;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchList();
});

function goDetail(row: PayRow) {
  router.push(`/contracts/payment/${row.paymentId}`);
}

function handleSearch() {
  query.keyword = (query.keyword || '').trim();
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.keyword = '';
  query.type = undefined;
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
  paymentId: '',
  contractCode: '',
  contractName: '',
  supplierName: '',
  type: 'PAY' as PayType,
  planAmount: 0,
  actualAmount: 0,
  planDate: '',
  status: 'PENDING' as PayStatus,
  remark: '',
});

const rules: FormRules = {
  contractCode: [{ required: true, message: '请输入合同编码', trigger: 'blur' }],
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  supplierName: [{ required: true, message: '请输入供应商', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  planAmount: [{ required: true, message: '请输入计划金额', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

function openEdit(isEdit: boolean, row?: PayRow) {
  editDialog.isEdit = isEdit;
  if (isEdit && row) {
    Object.assign(form, row);
  } else {
    form.paymentId = '';
    form.contractCode = '';
    form.contractName = '';
    form.supplierName = '';
    form.type = 'PAY';
    form.planAmount = 0;
    form.actualAmount = 0;
    form.planDate = '';
    form.status = 'PENDING';
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



