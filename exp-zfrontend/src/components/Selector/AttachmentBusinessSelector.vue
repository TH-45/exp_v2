<template>
  <div>
    <el-input
      v-model="displayText"
      :placeholder="placeholder"
      readonly
      class="selector-input"
      @click="openDialog"
    >
      <template #suffix>
        <el-icon class="cursor-pointer" @click="openDialog">
          <Search />
        </el-icon>
      </template>
    </el-input>

    <el-dialog
      v-model="dialogVisible"
      title="选择绑定业务"
      width="920px"
      destroy-on-close
      draggable
    >
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" @submit.prevent>
          <el-form-item v-if="showBusinessTypeFilter" label="业务类型">
            <el-select v-model="query.businessType" style="width: 160px" @change="handleBusinessTypeChange">
              <el-option
                v-for="item in normalizedBusinessTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="业务编码">
            <el-input
              v-model="searchForm.businessCode"
              placeholder="请输入业务编码"
              clearable
              style="width: 220px"
            />
          </el-form-item>
          <el-form-item label="业务名称">
            <el-input
              v-model="searchForm.businessName"
              placeholder="请输入业务名称"
              clearable
              style="width: 220px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        height="420px"
        row-key="rowKey"
        highlight-current-row
        @row-click="handleRowClick"
        @row-dblclick="handleRowDoubleClick"
      >
        <el-table-column label="业务类型" width="120">
          <template #default="{ row }">
            {{ businessTypeText(row.businessType) }}
          </template>
        </el-table-column>
        <el-table-column prop="businessCode" label="业务编码" min-width="180" />
        <el-table-column prop="businessName" label="业务名称" min-width="280" />
        <el-table-column prop="statusText" label="状态" min-width="140" />
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedRow" @click="handleConfirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { queryBiddingProjectList } from '@/api/bidding/project';
import { queryBidList } from '@/api/bidding/bid';
import { queryContractList } from '@/api/contracts/contract';

type BusinessType = 'TENDER' | 'BID' | 'CONTRACT';

interface BusinessOption {
  label: string;
  value: BusinessType;
}

export interface AttachmentBusinessValue {
  businessType: BusinessType | string;
  businessId: number;
  businessName: string;
  businessCode?: string;
}

interface BusinessRow extends AttachmentBusinessValue {
  rowKey: string;
  statusText?: string;
}

interface Props {
  modelValue?: AttachmentBusinessValue;
  placeholder?: string;
  allowedBusinessTypes?: Array<BusinessType>;
}

interface Emits {
  (e: 'update:modelValue', value: AttachmentBusinessValue | undefined): void;
  (e: 'change', value: AttachmentBusinessValue | undefined): void;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择绑定业务',
  allowedBusinessTypes: () => ['TENDER', 'BID', 'CONTRACT'],
});
const emit = defineEmits<Emits>();

const baseBusinessTypeOptions: BusinessOption[] = [
  { label: '招标', value: 'TENDER' },
  { label: '投标', value: 'BID' },
  { label: '合同', value: 'CONTRACT' },
];

const normalizedBusinessTypeOptions = computed(() =>
  baseBusinessTypeOptions.filter((x) => props.allowedBusinessTypes.includes(x.value)),
);
const showBusinessTypeFilter = computed(() => normalizedBusinessTypeOptions.value.length > 1);

const dialogVisible = ref(false);
const loading = ref(false);
const tableRef = ref();
const tableData = ref<BusinessRow[]>([]);
const total = ref(0);
const selectedRow = ref<BusinessRow>();

const query = reactive({
  businessType: (props.allowedBusinessTypes[0] || 'TENDER') as BusinessType,
  pageNum: 1,
  pageSize: 10,
});
const searchForm = reactive({
  businessCode: '',
  businessName: '',
});

const displayText = computed(() => {
  const v = props.modelValue;
  if (!v) return '';
  return v.businessCode ? `${v.businessName} (${v.businessCode})` : v.businessName || '';
});

function businessTypeText(type?: string) {
  return baseBusinessTypeOptions.find((x) => x.value === type)?.label || type || '-';
}

function openDialog() {
  query.businessType = (props.modelValue?.businessType as BusinessType) || normalizedBusinessTypeOptions.value[0]?.value || 'TENDER';
  query.pageNum = 1;
  selectedRow.value = undefined;
  dialogVisible.value = true;
  fetchList();
}

async function fetchList() {
  loading.value = true;
  try {
    if (query.businessType === 'TENDER') {
      const res = await queryBiddingProjectList({
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        tenderCode: searchForm.businessCode.trim() || undefined,
        tenderName: searchForm.businessName.trim() || undefined,
      });
      const list = Array.isArray(res?.list) ? res.list : [];
      tableData.value = list.map((x) => ({
        rowKey: `TENDER_${x.tenderId}`,
        businessType: 'TENDER',
        businessId: Number(x.tenderId),
        businessCode: x.tenderCode || '',
        businessName: x.tenderName || '',
        statusText: x.status || '',
      }));
      total.value = Number(res?.total ?? 0) || 0;
    } else if (query.businessType === 'BID') {
      const res = await queryBidList({
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        bidCode: searchForm.businessCode.trim() || undefined,
        bidName: searchForm.businessName.trim() || undefined,
      });
      const list = Array.isArray(res?.list) ? res.list : [];
      tableData.value = list.map((x) => ({
        rowKey: `BID_${x.bidId}`,
        businessType: 'BID',
        businessId: Number(x.bidId),
        businessCode: x.bidCode || '',
        businessName: x.bidName || '',
        statusText: x.bidStatus || '',
      }));
      total.value = Number(res?.total ?? 0) || 0;
    } else {
      const res = await queryContractList({
        page: query.pageNum,
        pageSize: query.pageSize,
        keyword: [searchForm.businessCode.trim(), searchForm.businessName.trim()].filter(Boolean).join(' ') || undefined,
      });
      const records = Array.isArray((res as any)?.records) ? (res as any).records : [];
      tableData.value = records.map((x: any) => ({
        rowKey: `CONTRACT_${x.contractId}`,
        businessType: 'CONTRACT',
        businessId: Number(x.contractId),
        businessCode: x.contractCode || '',
        businessName: x.contractName || '',
        statusText: x.status || '',
      }));
      total.value = Number((res as any)?.total ?? 0) || 0;
    }
    nextTick(() => {
      if (!props.modelValue?.businessId) return;
      const row = tableData.value.find(
        (x) => x.businessType === props.modelValue?.businessType && Number(x.businessId) === Number(props.modelValue?.businessId),
      );
      if (row) {
        selectedRow.value = row;
        tableRef.value?.setCurrentRow(row);
      }
    });
  } catch (_e) {
    tableData.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  fetchList();
}

function handleReset() {
  searchForm.businessCode = '';
  searchForm.businessName = '';
  query.pageNum = 1;
  fetchList();
}

function handleBusinessTypeChange() {
  query.pageNum = 1;
  selectedRow.value = undefined;
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

function handleRowClick(row: BusinessRow) {
  selectedRow.value = row;
  tableRef.value?.setCurrentRow(row);
}

function handleRowDoubleClick(row: BusinessRow) {
  selectedRow.value = row;
  handleConfirm();
}

function handleConfirm() {
  if (!selectedRow.value) return;
  const value: AttachmentBusinessValue = {
    businessType: selectedRow.value.businessType,
    businessId: Number(selectedRow.value.businessId),
    businessName: selectedRow.value.businessName,
    businessCode: selectedRow.value.businessCode,
  };
  emit('update:modelValue', value);
  emit('change', value);
  dialogVisible.value = false;
}
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
