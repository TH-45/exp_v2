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
            <el-button type="primary" @click="handleSearch">
              查询
            </el-button>
            <el-button @click="handleReset">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 公司列表 -->
      <el-table
          ref="tableRef"
          v-loading="loading"
          :data="tableData"
          border
          height="400px"
          @row-click="handleRowClick"
      >
        <el-table-column prop="companyCode" label="公司编码" min-width="140" />
        <el-table-column prop="companyName" label="公司名称" min-width="220" />
        <el-table-column prop="contactPerson" label="联系人" min-width="140" />
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
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedCompany">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { Search } from '@element-plus/icons-vue';

/* ===============================
   DTO 定义
================================ */

export interface CompanyVO {
  companyId: string;
  companyCode: string;
  companyName: string;
  contactPerson?: string;
  contactPhone?: string;
}

/* ===============================
   Props / Emits
================================ */

interface Props {
  modelValue?: CompanyVO;
  placeholder?: string;
}

interface Emits {
  (e: 'update:modelValue', value: CompanyVO | undefined): void;
  (e: 'change', value: CompanyVO | undefined): void;
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
const tableData = ref<CompanyVO[]>([]);
const total = ref(0);
const selectedCompany = ref<CompanyVO>();

const query = reactive({
  pageNum: 1,
  pageSize: 10,
});

const searchForm = reactive({
  companyName: '',
  companyCode: '',
});

/* ===============================
   显示文本
================================ */

const displayText = computed(() => {
  return props.modelValue
      ? `${props.modelValue.companyName}(${props.modelValue.companyCode})`
      : '';
});

/* ===============================
   打开弹窗
================================ */

function openDialog() {
  dialogVisible.value = true;
  selectedCompany.value = props.modelValue;
  tableData.value = [];
  total.value = 0;
}

/* ===============================
   TODO：接入公司查询接口
================================ */

/**
 * TODO:
 * 替换为真实接口
 *
 * import { queryCompanyList } from '@/api/system/company'
 */

async function fetchCompanyList() {
  loading.value = true;

  try {
    // TODO: 替换为真实接口
    /*
    const res = await queryCompanyList({
      ...query,
      companyName: searchForm.companyName.trim() || undefined,
      companyCode: searchForm.companyCode.trim() || undefined,
    });

    tableData.value = res.list || [];
    total.value = res.total || 0;
    */

    // 临时模拟数据
    tableData.value = [
      {
        companyId: '1',
        companyCode: 'CMP001',
        companyName: '测试招标有限公司',
        contactPerson: '张三',
        contactPhone: '13800000000',
      },
    ];
    total.value = 1;

  } finally {
    loading.value = false;
  }
}

/* ===============================
   搜索
================================ */

function handleSearch() {
  query.pageNum = 1;
  fetchCompanyList();
}

function handleReset() {
  searchForm.companyName = '';
  searchForm.companyCode = '';
  query.pageNum = 1;
}

/* ===============================
   行点击
================================ */

function handleRowClick(row: CompanyVO) {
  selectedCompany.value = row;
  tableRef.value?.setCurrentRow(row);
}

/* ===============================
   确认
================================ */

function handleConfirm() {
  if (!selectedCompany.value) return;

  emit('update:modelValue', selectedCompany.value);
  emit('change', selectedCompany.value);
  dialogVisible.value = false;
}

/* ===============================
   分页
================================ */

function handleCurrentChange(page: number) {
  query.pageNum = page;
  fetchCompanyList();
}

watch(() => props.modelValue, (val) => {
  selectedCompany.value = val;
}, { immediate: true });
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