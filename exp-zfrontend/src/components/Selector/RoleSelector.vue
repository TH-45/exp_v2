<template>
  <!--  前端-角色选择器-->
  <div>
    <el-input
      :model-value="displayText"
      :placeholder="placeholder"
      readonly
      clearable
      @clear="handleClear"
      @click="openDialog"
      class="selector-input"
    >
      <template #suffix>
        <el-icon @click="openDialog" class="cursor-pointer">
          <Search />
        </el-icon>
      </template>
    </el-input>

    <!-- 角色选择弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="选择角色"
      width="720px"
      destroy-on-close
      draggable
    >
      <form @submit.prevent="handleConfirm">
        <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
        <!-- 搜索区 -->
        <div class="search-bar">
          <el-form :inline="true" :model="searchForm" @submit.prevent="handleConfirm">
            <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
            <el-form-item label="角色名称">
              <el-input
                v-model="searchForm.roleName"
                placeholder="请输入角色名称"
                clearable
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item label="角色编码">
              <el-input
                v-model="searchForm.roleCode"
                placeholder="请输入角色编码"
                clearable
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </form>

      <!-- 角色列表 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
        height="360px"
        @row-click="handleRowClick"
      >
        <el-table-column type="radio" width="50" />
        <el-table-column prop="roleCode" label="角色编码" min-width="140" />
        <el-table-column prop="roleName" label="角色名称" min-width="160" />
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
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
          :page-sizes="[10, 20, 50]"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedRole">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { listRoles, type RoleVO } from '@/api/system/role';
import { parsePageResult } from '@/api/common';

interface Props {
  modelValue?: RoleVO;
  placeholder?: string;
}

interface Emits {
  (e: 'update:modelValue', value: RoleVO | undefined): void;
  (e: 'change', value: RoleVO | undefined): void;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择角色',
});

const emit = defineEmits<Emits>();

const dialogVisible = ref(false);
const loading = ref(false);
const tableRef = ref();
const tableData = ref<RoleVO[]>([]);
const total = ref(0);
const selectedRole = ref<RoleVO>();

const query = reactive({
  pageNum: 1,
  pageSize: 10,
});

const searchForm = reactive({
  roleName: '',
  roleCode: '',
});

const displayText = computed(() => {
  if (!props.modelValue) return '';
  const codeText = props.modelValue.roleCode ? `(${props.modelValue.roleCode})` : '';
  return `${props.modelValue.roleName}${codeText}`;
});

function getStatusTagType(status?: string) {
  if (status === 'ENABLED') return 'success';
  if (status === 'DISABLED') return 'info';
  return '';
}

function getStatusText(status?: string) {
  if (status === 'ENABLED') return '启用';
  if (status === 'DISABLED') return '停用';
  return '-';
}

function openDialog() {
  dialogVisible.value = true;
  selectedRole.value = props.modelValue;
  fetchRoleList();
}

async function fetchRoleList() {
  loading.value = true;
  try {
    const res = await listRoles({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      roleName: searchForm.roleName || undefined,
      roleCode: searchForm.roleCode || undefined,
      status: 'ENABLED',
    });
    const { list, total: totalCount } = parsePageResult<RoleVO>(res);
    tableData.value = list;
    total.value = totalCount;
  } catch (e) {
    tableData.value = [];
    total.value = 0;
    ElMessage.error('获取角色列表失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  fetchRoleList();
}

function handleReset() {
  searchForm.roleName = '';
  searchForm.roleCode = '';
  query.pageNum = 1;
  fetchRoleList();
}

function handleRowClick(row: RoleVO) {
  selectedRole.value = row;
  tableRef.value?.setCurrentRow(row);
}

function handleCurrentChange(page: number) {
  query.pageNum = page;
  fetchRoleList();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.pageNum = 1;
  fetchRoleList();
}

function handleConfirm() {
  if (!selectedRole.value) return;
  emit('update:modelValue', selectedRole.value);
  emit('change', selectedRole.value);
  dialogVisible.value = false;
}

function handleClear() {
  emit('update:modelValue', undefined);
  emit('change', undefined);
}

watch(() => props.modelValue, (newVal) => {
  selectedRole.value = newVal;
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
</style>
