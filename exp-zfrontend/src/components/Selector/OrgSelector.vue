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

    <!-- 组织选择弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="选择组织"
      width="600px"
      destroy-on-close
    >
      <!-- 搜索区 -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="请输入组织名称或编码"
          clearable
          @input="handleSearch"
          style="width: 300px"
        >
          <template #prefix>
            <el-icon>
              <Search />
            </el-icon>
          </template>
        </el-input>
      </div>

      <!-- 组织树 -->
      <div class="org-tree-container">
        <el-tree
          ref="treeRef"
          v-loading="loading"
          :data="treeData"
          :props="treeProps"
          :filter-node-method="filterNode"
          :highlight-current="true"
          :default-expand-all="false"
          :expand-on-click-node="false"
          @node-click="handleNodeClick"
          @current-change="handleCurrentChange"
        >
          <template #default="{ node, data }">
            <div class="org-node">
              <span class="org-name">{{ node.label }}</span>
              <span class="org-code" v-if="data.orgCode">({{ data.orgCode }})</span>
            </div>
          </template>
        </el-tree>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedOrg">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { fetchOrgTree, type OrgNode } from '@/api/system/post';

interface Props {
  modelValue?: OrgNode;
  placeholder?: string;
}

interface Emits {
  (e: 'update:modelValue', value: OrgNode | undefined): void;
  (e: 'change', value: OrgNode | undefined): void;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择组织',
});

const emit = defineEmits<Emits>();

const dialogVisible = ref(false);
const loading = ref(false);
const treeRef = ref();
const treeData = ref<OrgNode[]>([]);
const selectedOrg = ref<OrgNode>();
const keyword = ref('');

const treeProps = {
  label: 'orgName',
  children: 'children',
};

const displayText = computed(() => {
  return props.modelValue ? props.modelValue.orgName : '';
});

// 打开弹窗
function openDialog() {
  dialogVisible.value = true;
  selectedOrg.value = props.modelValue;
  fetchOrgTreeData();
}

// 获取组织树数据
async function fetchOrgTreeData() {
  loading.value = true;
  try {
    const params = keyword.value ? { keyword: keyword.value } : undefined;
    const res = await fetchOrgTree(params);
    treeData.value = res || [];

    // 如果有选中的组织，展开到该节点
    await nextTick();
    if (selectedOrg.value && treeRef.value) {
      treeRef.value.setCurrentKey(selectedOrg.value.orgId);
    }
  } catch (e) {
    treeData.value = [];
    ElMessage.error('获取组织树失败');
  } finally {
    loading.value = false;
  }
}

// 搜索
function handleSearch() {
  if (treeRef.value) {
    treeRef.value.filter(keyword.value);
  }
}

// 过滤节点
function filterNode(value: string, data: OrgNode) {
  if (!value) return true;
  return data.orgName.includes(value) || (data.orgCode && data.orgCode.includes(value));
}

// 节点点击
function handleNodeClick(data: OrgNode) {
  selectedOrg.value = data;
}

// 当前节点变化
function handleCurrentChange(data: OrgNode) {
  selectedOrg.value = data;
}

// 确认选择
function handleConfirm() {
  if (!selectedOrg.value) return;

  emit('update:modelValue', selectedOrg.value);
  emit('change', selectedOrg.value);
  dialogVisible.value = false;
}

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  selectedOrg.value = newVal;
}, { immediate: true });

// 监听关键词变化
watch(() => keyword.value, () => {
  handleSearch();
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
}

.cursor-pointer {
  cursor: pointer;
}

.search-bar {
  margin-bottom: 12px;
}

.org-tree-container {
  height: 400px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;

  :deep(.el-tree) {
    background: transparent;
  }

  :deep(.el-tree-node__content) {
    height: 32px;
    padding: 0;
  }

  :deep(.el-tree-node__label) {
    font-size: 14px;
  }
}

.org-node {
  display: flex;
  align-items: center;
  width: 100%;

  .org-name {
    flex: 1;
    font-weight: 500;
  }

  .org-code {
    color: #909399;
    font-size: 12px;
    margin-left: 8px;
  }
}
</style>
