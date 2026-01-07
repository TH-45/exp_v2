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

    <!-- 人员选择弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="选择人员"
      width="800px"
      destroy-on-close
    >
      <!-- 搜索区 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" @submit.prevent>
          <el-form-item label="人员姓名">
            <el-input
              v-model="searchForm.personName"
              placeholder="请输入人员姓名"
              clearable
              style="width: 160px"
            />
          </el-form-item>
          <el-form-item label="工号">
            <el-input
              v-model="searchForm.personCode"
              placeholder="请输入工号"
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

      <!-- 人员列表 -->
      <div v-if="!hasSearchCondition" class="no-search-tip">
        <el-empty description="请先输入查询条件进行搜索" :image-size="80">
          <template #image>
            <el-icon size="80" class="no-search-icon">
              <Search />
            </el-icon>
          </template>
        </el-empty>
      </div>
      <el-table
        v-else
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
        height="400px"
        @row-click="handleRowClick"
      >

        <el-table-column prop="personCode" label="工号" min-width="120" />
        <el-table-column prop="personName" label="姓名" min-width="120" />
        <el-table-column prop="mobile" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="jobTitle" label="职务" min-width="120" />
      </el-table>

      <!-- 分页 -->
      <div v-if="hasSearchCondition" class="pagination">
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
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedPerson">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { queryPersonInfo, type ExpPersonVO } from '@/api/system/person';

interface Props {
  modelValue?: ExpPersonVO;
  placeholder?: string;
}

interface Emits {
  (e: 'update:modelValue', value: ExpPersonVO | undefined): void;
  (e: 'change', value: ExpPersonVO | undefined): void;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择人员',
});

const emit = defineEmits<Emits>();

const dialogVisible = ref(false);
const loading = ref(false);
const tableRef = ref();
const tableData = ref<ExpPersonVO[]>([]);
const total = ref(0);
const selectedPerson = ref<ExpPersonVO>();

const query = reactive({
  pageNum: 1,
  pageSize: 10,
});

const searchForm = reactive({
  personName: '',
  personCode: '',
});

const displayText = computed(() => {
  return props.modelValue ? `${props.modelValue.personName}(${props.modelValue.personCode})` : '';
});

const hasSearchCondition = computed(() => {
  return searchForm.personName.trim() || searchForm.personCode.trim();
});


// 打开弹窗
function openDialog() {
  dialogVisible.value = true;
  selectedPerson.value = props.modelValue;
  // fetchPersonList();
}

// 搜索
function handleSearch() {
  // 检查是否有查询条件
  if (!searchForm.personName.trim() && !searchForm.personCode.trim()) {
    ElMessage.warning('请至少输入一个查询条件（姓名或工号）');
    return;
  }
  query.pageNum = 1;
  fetchPersonList();
}

// 重置
function handleReset() {
  searchForm.personName = '';
  searchForm.personCode = '';
  query.pageNum = 1;
  // 清空表格数据
  tableData.value = [];
  total.value = 0;
  selectedPerson.value = undefined;
}

// 获取人员列表
async function fetchPersonList() {
  // 检查是否有查询条件
  if (!searchForm.personName.trim() && !searchForm.personCode.trim()) {
    return;
  }

  loading.value = true;
  try {
    const params = {
      ...query,
      personName: searchForm.personName || undefined,
      personCode: searchForm.personCode || undefined,
      status: 'ONJOB', // 只查询在职人员
    };

    const res = await queryPersonInfo(params);
    tableData.value = res.list || [];
    total.value = res.total || 0;
  } catch (e) {
    tableData.value = [];
    total.value = 0;
    ElMessage.error('获取人员列表失败');
  } finally {
    loading.value = false;
  }
}

// 行点击
function handleRowClick(row: ExpPersonVO) {
  selectedPerson.value = row;
  // 设置单选
  tableRef.value?.setCurrentRow(row);
}

// 分页
function handleCurrentChange(page: number) {
  query.pageNum = page;
  fetchPersonList();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.pageNum = 1;
  fetchPersonList();
}

// 确认选择
function handleConfirm() {
  if (!selectedPerson.value) return;

  emit('update:modelValue', selectedPerson.value);
  emit('change', selectedPerson.value);
  dialogVisible.value = false;
}

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  selectedPerson.value = newVal;
}, { immediate: true });
</script>

<style scoped lang="scss">
.selector-input {
  cursor: pointer;

  :deep(.el-input__inner) {
    cursor: pointer;
  }

  :deep(.el-input__suffix) {
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

.no-search-tip {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.no-search-icon {
  color: #c0c4cc;
}
</style>
