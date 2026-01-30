<template>
  <div>
    <el-input
      v-model="displayText"
      :placeholder="placeholder"
      :disabled="!orgId"
      readonly
      @click="openDialog"
      class="selector-input"
    >
      <template #suffix>
        <el-icon v-if="orgId" @click="openDialog" class="cursor-pointer">
          <Search />
        </el-icon>
        <el-icon v-else class="disabled-icon">
          <InfoFilled />
        </el-icon>
      </template>
    </el-input>

    <!-- 岗位选择弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="选择岗位"
      width="700px"
      destroy-on-close
    >
      <!-- 提示信息 -->
      <div v-if="!orgId" class="no-org-tip">
        <el-alert
          title="请先选择所属组织"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>

      <!-- 搜索区 -->
      <div v-else class="search-bar">
        <el-form :inline="true" :model="searchForm" @submit.prevent="handleConfirm">
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="岗位名称">
            <el-input
              v-model="searchForm.postName"
              placeholder="请输入岗位名称"
              clearable
              style="width: 160px"
            />
          </el-form-item>
          <el-form-item label="岗位编码">
            <el-input
              v-model="searchForm.postCode"
              placeholder="请输入岗位编码"
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

      <!-- 岗位列表 -->
      <el-table
        v-if="orgId"
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
        height="350px"
        @row-click="handleRowClick"
      >
        <el-table-column type="radio" width="50" />
        <el-table-column prop="postCode" label="岗位编码" min-width="120" />
        <el-table-column prop="postName" label="岗位名称" min-width="140" />
        <el-table-column prop="postType" label="岗位类型" min-width="100" />
        <el-table-column prop="postLevel" label="岗位级别" min-width="100" />
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="postDesc" label="岗位职责" min-width="200" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <div v-if="orgId" class="pagination">
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
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedPost || !orgId">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, InfoFilled } from '@element-plus/icons-vue';
import { queryOrgPosts, type PostVO, type OrgNode } from '@/api/system/post';

interface Props {
  modelValue?: PostVO;
  orgId?: number;
  placeholder?: string;
}

interface Emits {
  (e: 'update:modelValue', value: PostVO | undefined): void;
  (e: 'change', value: PostVO | undefined): void;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择岗位',
});

const emit = defineEmits<Emits>();

const dialogVisible = ref(false);
const loading = ref(false);
const tableRef = ref();
const tableData = ref<PostVO[]>([]);
const total = ref(0);
const selectedPost = ref<PostVO>();

const query = reactive({
  pageNum: 1,
  pageSize: 10,
});

const searchForm = reactive({
  postName: '',
  postCode: '',
});

const displayText = computed(() => {
  return props.modelValue ? `${props.modelValue.postName}(${props.modelValue.postCode})` : '';
});

// 状态标签类型
function getStatusTagType(status?: string) {
  if (status === 'ENABLED') return 'success';
  if (status === 'DISABLED') return 'info';
  return '';
}

// 状态文本
function getStatusText(status?: string) {
  if (status === 'ENABLED') return '启用';
  if (status === 'DISABLED') return '停用';
  return '-';
}

// 打开弹窗
function openDialog() {
  if (!props.orgId) {
    ElMessage.warning('请先选择所属组织');
    return;
  }

  dialogVisible.value = true;
  selectedPost.value = props.modelValue;
  fetchPostList();
}

// 获取岗位列表
async function fetchPostList() {
  if (!props.orgId) return;

  loading.value = true;
  try {
    const params = {
      orgId: props.orgId,
      postCode: searchForm.postCode || undefined,
      postName: searchForm.postName || undefined,
      relStatus: 'ENABLED' as const,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    };

    const res = await queryOrgPosts(params);
    tableData.value = res.list || [];
    total.value = res.total || 0;
  } catch (e) {
    tableData.value = [];
    total.value = 0;
    ElMessage.error('获取岗位列表失败');
  } finally {
    loading.value = false;
  }
}

// 搜索
function handleSearch() {
  query.pageNum = 1;
  fetchPostList();
}

// 重置
function handleReset() {
  searchForm.postName = '';
  searchForm.postCode = '';
  query.pageNum = 1;
  fetchPostList();
}

// 行点击
function handleRowClick(row: PostVO) {
  selectedPost.value = row;
  // 设置单选
  tableRef.value?.setCurrentRow(row);
}

// 分页
function handleCurrentChange(page: number) {
  query.pageNum = page;
  fetchPostList();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.pageNum = 1;
  fetchPostList();
}

// 确认选择
function handleConfirm() {
  if (!selectedPost.value || !props.orgId) return;

  emit('update:modelValue', selectedPost.value);
  emit('change', selectedPost.value);
  dialogVisible.value = false;
}

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  selectedPost.value = newVal;
}, { immediate: true });

// 监听组织变化，清空岗位选择
watch(() => props.orgId, (newVal) => {
  if (!newVal) {
    emit('update:modelValue', undefined);
    emit('change', undefined);
  }
});
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

  &.is-disabled {
    cursor: not-allowed;

    :deep(.el-input__inner) {
      cursor: not-allowed;
    }
  }
}

.cursor-pointer {
  cursor: pointer;
}

.disabled-icon {
  color: #c0c4cc;
}

.no-org-tip {
  margin-bottom: 12px;
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
