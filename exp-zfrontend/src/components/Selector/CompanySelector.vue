<template>
  <div>
    <el-input
      v-model="displayText"
      :placeholder="placeholder"
      readonly
      @click="openDialog"
      class="selector-input"
    >
      <template #suffix>
        <el-icon @click="openDialog" class="cursor-pointer">
          <Search />
        </el-icon>
      </template>
    </el-input>

    <el-dialog
      v-model="dialogVisible"
      title="选择公司"
      width="800px"
      destroy-on-close
      draggable
    >
      <!-- 搜索区 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="公司名称">
            <el-input
              v-model="searchForm.companyName"
              placeholder="请输入公司名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="公司编码">
            <el-input
              v-model="searchForm.companyCode"
              placeholder="请输入公司编码"
              clearable
              style="width: 160px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 公司列表（列表数据来自接口，选择后仅回传 companyId / companyName / contactPhone，不含编号） -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        height="300px"
        row-key="companyId"
        @row-click="handleRowClick"
        highlight-current-row
        @current-change="handleCurrentChangeRow"
      >
        <el-table-column prop="companyName" label="公司名称" min-width="200" />
        <el-table-column prop="companyCode" label="公司编码" min-width="130" />
        <el-table-column prop="legalPerson" label="法定代表人" min-width="120" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="140" />
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          :total="total"
          @current-change="handleCurrentChange"
        />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedRow">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { parsePageResult } from '@/api/common';
import {
  listCompany,
  type CompanyListVO,
  type CompanySelectorValue,
} from '@/api/enterprise/company';

/* ===============================
   对外值类型（不含编号，见 api/enterprise/company CompanySelectorValue）
================================ */

/** 选择器接受的 modelValue 类型：支持仅 companyId + companyName 回显，兼容历史带 companyCode 的传值 */
export type CompanySelectorModelValue = CompanySelectorValue | {
  companyId?: number | string;
  companyName?: string;
  companyCode?: string;
  contactPhone?: string;
};

/* ===============================
   Props / Emits
================================ */

interface Props {
  modelValue?: CompanySelectorModelValue | null;
  placeholder?: string;
}

interface Emits {
  (e: 'update:modelValue', value: CompanySelectorValue | undefined): void;
  (e: 'change', value: CompanySelectorValue | undefined): void;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择公司',
});

const emit = defineEmits<Emits>();

/* ===============================
   状态
================================ */

const dialogVisible = ref(false);
const loading = ref(false);
const tableRef = ref();
const tableData = ref<CompanyListVO[]>([]);
const total = ref(0);
/** 当前选中的列表行（用于高亮与确认时转为 CompanySelectorValue 传出） */
const selectedRow = ref<CompanyListVO | null>(null);

const query = reactive({
  pageNum: 1,
  pageSize: 10,
});

const searchForm = reactive({
  companyName: '',
  companyCode: '',
});

/* ===============================
   回显：仅用名称，不依赖编号
================================ */

const displayText = computed(() => {
  const v = props.modelValue;
  if (!v || typeof v !== 'object') return '';
  return (v as { companyName?: string }).companyName ?? '';
});

/* ===============================
   打开弹窗
================================ */

function openDialog() {
  dialogVisible.value = true;
  selectedRow.value = null;
  query.pageNum = 1;
  fetchCompanyList();
}

/* ===============================
   接入公司列表接口
================================ */

async function fetchCompanyList() {
  loading.value = true;
  try {
    const res = await listCompany({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      companyName: searchForm.companyName.trim() || undefined,
      companyCode: searchForm.companyCode.trim() || undefined,
    });
    const parsed = parsePageResult<CompanyListVO>(res);
    tableData.value = parsed.list;
    total.value = parsed.total;

    // 回显：若已有 modelValue，在列表里选中对应行
    await nextTick();
    const current = props.modelValue as CompanySelectorModelValue | undefined;
    const wantId = current?.companyId != null ? Number(current.companyId) : null;
    if (wantId != null && tableData.value.length > 0) {
      const row = tableData.value.find((r) => r.companyId === wantId);
      if (row) {
        selectedRow.value = row;
        tableRef.value?.setCurrentRow(row);
      }
    }
  } catch (_e) {
    tableData.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  fetchCompanyList();
}

function handleReset() {
  searchForm.companyName = '';
  searchForm.companyCode = '';
  query.pageNum = 1;
  fetchCompanyList();
}

// function handleRowClick(row: CompanyListVO) {
//   selectedRow.value = row;
//   tableRef.value?.setCurrentRow(row);
// }
function handleCurrentChangeRow(row: CompanyListVO | undefined) {
  selectedRow.value = row ?? null;
}

/** 确认时只传出 companyId、companyName、contactPhone，不含编号 */
function handleConfirm() {
  const row = selectedRow.value;
  if (!row || row.companyId == null) return;

  const value: CompanySelectorValue = {
    companyId: row.companyId,
    companyName: row.companyName ?? '',
    contactPhone: row.contactPhone,
  };
  emit('update:modelValue', value);
  emit('change', value);
  dialogVisible.value = false;
}

function handleCurrentChange(page: number) {
  query.pageNum = page;
  fetchCompanyList();
}

watch(
  () => props.modelValue,
  (val) => {
    if (!dialogVisible.value) return;
    const wantId = val && typeof val === 'object' && val.companyId != null
      ? Number((val as CompanySelectorModelValue).companyId)
      : null;
    if (wantId == null) {
      selectedRow.value = null;
      tableRef.value?.setCurrentRow();
      return;
    }
    const row = tableData.value.find((r) => r.companyId === wantId);
    selectedRow.value = row ?? null;
    tableRef.value?.setCurrentRow(row ?? undefined);
  },
  { deep: true },
);
</script>

<style scoped lang="scss">
.selector-input {
  cursor: pointer;
  :deep(.el-input__inner) {
    cursor: pointer;
  }
}
.cursor-pointer {
  cursor: pointer;
}
.search-bar {
  margin-bottom: 12px;
}
.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
