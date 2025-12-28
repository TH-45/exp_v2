<template>
  <el-config-provider :locale="zhCn">
    <div class="post-page">
      <div class="split-area" ref="splitRef">
        <!-- 左侧组织树 -->
        <div
          class="left-pane"
          :class="{ collapsed: isCollapsed, expand: isRightCollapsed }"
          :style="{
            width: isCollapsed ? '0px' : isRightCollapsed ? '100%' : leftWidth + 'px',
          }"
        >
          <el-card class="tree-card" body-class="tree-card-body">
            <div class="tree-header">
              <el-input
                v-model="treeFilter"
                size="small"
                placeholder="搜索组织名称/编码"
                clearable
                @input="filterTree"
              />
            </div>
            <el-tree
              ref="treeRef"
              class="org-tree"
              node-key="orgId"
              :data="orgTree"
              :props="treeProps"
              highlight-current
              :filter-node-method="treeFilterMethod"
              @node-click="handleTreeClick"
              v-loading="treeLoading"
            />
          </el-card>
        </div>

        <!-- 拖拽分割条 -->
        <div class="drag-handle" @mousedown="startDrag" @dblclick.stop="toggleCollapse">
          <span class="drag-line" />
          <button class="drag-toggle left" type="button" @click.stop="toggleCollapse">
            <el-icon :size="14">
              <ArrowRight v-if="isCollapsed" />
              <ArrowLeft v-else />
            </el-icon>
          </button>
          <button class="drag-toggle right" type="button" @click.stop="toggleRightCollapse">
            <el-icon :size="14">
              <ArrowLeft v-if="isRightCollapsed" />
              <ArrowRight v-else />
            </el-icon>
          </button>
        </div>

        <!-- 右侧岗位配置 -->
        <div class="right-pane" :class="{ collapsed: isRightCollapsed }">
          <el-card>
            <div class="right-header">
              <div class="title">岗位管理</div>
              <div class="org-label" v-if="currentOrg">
                当前组织：{{ currentOrg.orgName }}
              </div>
            </div>

            <!-- 顶部按钮栏 -->
            <div class="actions">
              <el-button
                type="primary"
                size="small"
                @click="openPostForm(false)"
                :disabled="!canCreate"
              >
                新增岗位
              </el-button>
              <el-button
                size="small"
                @click="openBindDialog"
                :disabled="!currentOrg || !canBind"
              >
                关联岗位
              </el-button>
              <el-button
                size="small"
                @click="batchToggleRelStatus"
                :disabled="!currentOrg || !selectedRows.length || !canOrgStatus"
              >
                启用/停用
              </el-button>
              <el-button
                size="small"
                @click="setPrimary"
                :disabled="!currentOrg || selectedRows.length !== 1 || !canSetPrimary"
              >
                设为主岗位
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="unbindSelected"
                :disabled="!currentOrg || !selectedRows.length || !canUnbind"
              >
                解除关联
              </el-button>
              <el-button size="small" :disabled="true">导入</el-button>
              <el-button size="small" :disabled="true">导出</el-button>
            </div>

            <!-- 查询栏 -->
            <el-form
              :inline="true"
              :model="query"
              class="search-bar"
              @submit.prevent
            >
              <el-form-item label="岗位编码">
                <el-input
                  v-model="query.postCode"
                  placeholder="请输入岗位编码"
                  clearable
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="岗位名称">
                <el-input
                  v-model="query.postName"
                  placeholder="请输入岗位名称"
                  clearable
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="组织可用状态">
                <el-select v-model="query.relStatus" style="width: 160px">
                  <el-option label="全部" value="ALL" />
                  <el-option label="启用" value="ENABLED" />
                  <el-option label="停用" value="DISABLED" />
                </el-select>
              </el-form-item>
              <el-form-item label="岗位字典状态">
                <el-select v-model="query.postStatus" clearable style="width: 160px">
                  <el-option label="启用" value="ENABLED" />
                  <el-option label="停用" value="DISABLED" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- 未选组织提示 -->
            <el-empty
              v-if="!currentOrg"
              description="请选择组织后再操作岗位"
              :image-size="120"
            />

            <!-- 表格 -->
            <el-table
              v-else
              v-loading="tableLoading"
              :data="tableData"
              row-key="postId"
              border
              style="width: 100%"
              @selection-change="handleSelectionChange"
            >
              <el-table-column type="selection" width="50" />
              <el-table-column prop="postCode" label="岗位编码" min-width="140" />
              <el-table-column prop="postName" label="岗位名称" min-width="140" />
              <el-table-column prop="postType" label="岗位类型" min-width="120" />
              <el-table-column prop="postLevel" label="岗位级别" min-width="120" />
              <el-table-column
                prop="defaultRoleName"
                label="默认角色"
                min-width="140"
              />
              <el-table-column label="岗位字典状态" min-width="140">
                <template #default="{ row }">
                  <el-tag :type="row.postStatus === 'ENABLED' ? 'success' : 'info'">
                    {{ row.postStatus === 'ENABLED' ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="组织可用状态" min-width="140">
                <template #default="{ row }">
                  <el-tag :type="row.relStatus === 'ENABLED' ? 'success' : 'info'">
                    {{ row.relStatus === 'ENABLED' ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="主岗位" min-width="100">
                <template #default="{ row }">
                  <el-tag :type="row.isPrimary ? 'warning' : 'info'">
                    {{ row.isPrimary ? '主' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="relSortNo" label="组织内排序" min-width="120" />
              <el-table-column prop="createdTime" label="创建时间" min-width="170" />
              <el-table-column label="操作" fixed="right" width="250">
                <template #default="{ row }">
                  <el-button
                    link
                    type="primary"
                    size="small"
                    @click="openPostForm(true, row)"
                    :disabled="!canUpdate"
                  >
                    编辑岗位
                  </el-button>
                  <el-button
                    link
                    size="small"
                    @click="rowToggleStatus(row)"
                    :disabled="!canOrgStatus"
                  >
                    {{ row.relStatus === 'ENABLED' ? '停用' : '启用' }}
                  </el-button>
                  <el-button
                    link
                    size="small"
                    @click="setPrimary(row)"
                    :disabled="!canSetPrimary"
                  >
                    设为主岗位
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    size="small"
                    @click="unbindSingle(row)"
                    :disabled="!canUnbind"
                  >
                    解除关联
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div v-if="currentOrg" class="pagination">
              <el-pagination
                background
                layout="total, prev, pager, next, sizes"
                :current-page="query.pageNum"
                :page-size="query.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="total"
                @current-change="handlePageChange"
                @size-change="handleSizeChange"
              />
            </div>
          </el-card>
        </div>
      </div>

      <!-- 岗位表单弹窗 -->
      <el-dialog
        v-model="postDialog.visible"
        :title="postDialog.isEdit ? '编辑岗位' : '新增岗位'"
        width="760px"
        destroy-on-close
      >
        <el-form
          ref="postFormRef"
          :model="postForm"
          :rules="postRules"
          label-width="120px"
          class="dialog-form two-col"
        >
          <el-form-item label="岗位编码" prop="postCode">
            <el-input v-model="postForm.postCode" />
          </el-form-item>
          <el-form-item label="岗位名称" prop="postName">
            <el-input v-model="postForm.postName" />
          </el-form-item>
          <el-form-item label="状态" prop="postStatus">
            <el-select v-model="postForm.postStatus" style="width: 200px">
              <el-option label="启用" value="ENABLED" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="默认角色">
            <el-input v-model="postForm.defaultRoleName" placeholder="角色名称（占位）" />
          </el-form-item>
          <el-form-item label="数据范围">
            <el-select v-model="postForm.defaultDataScope" clearable placeholder="预留枚举">
              <el-option label="本部门" value="DEPT" />
              <el-option label="本部门及下级" value="DEPT_AND_SUB" />
              <el-option label="全部" value="ALL" />
            </el-select>
          </el-form-item>
          <el-form-item label="岗位类型">
            <el-input v-model="postForm.postType" />
          </el-form-item>
          <el-form-item label="岗位级别">
            <el-input v-model="postForm.postLevel" />
          </el-form-item>
          <el-form-item label="岗位分类">
            <el-input v-model="postForm.postCategory" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="postForm.sortNo" :min="0" :max="9999" />
          </el-form-item>
          <el-form-item label="职责说明" class="full-row">
            <el-input v-model="postForm.postDesc" type="textarea" />
          </el-form-item>
          <el-form-item label="备注" class="full-row">
            <el-input v-model="postForm.remark" type="textarea" />
          </el-form-item>
          <el-form-item label="系统内置">
            <el-input v-model="postForm.isSystem" disabled placeholder="预留字段" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="postDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="postSaving" @click="submitPostForm">
            保存
          </el-button>
        </template>
      </el-dialog>

      <!-- 关联岗位弹窗 -->
      <el-dialog
        v-model="bindDialog.visible"
        title="关联岗位"
        width="720px"
        destroy-on-close
      >
        <div class="bind-search">
          <el-input
            v-model="bindDialog.keyword"
            placeholder="搜索岗位编码/名称"
            clearable
            style="width: 240px"
            @change="fetchBindList"
          />
        </div>
        <el-table
          v-loading="bindDialog.loading"
          :data="bindDialog.list"
          row-key="postId"
          height="360"
          @selection-change="handleBindSelection"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="postCode" label="岗位编码" min-width="140" />
          <el-table-column prop="postName" label="岗位名称" min-width="140" />
          <el-table-column label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="row.postStatus === 'ENABLED' ? 'success' : 'info'">
                {{ row.postStatus === 'ENABLED' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            background
            layout="total, prev, pager, next, sizes"
            :current-page="bindDialog.pageNum"
            :page-size="bindDialog.pageSize"
            :page-sizes="[5, 10, 20]"
            :total="bindDialog.total"
            @current-change="(p:number)=>changeBindPage(p, bindDialog.pageSize)"
            @size-change="(s:number)=>changeBindPage(1, s)"
          />
        </div>
        <template #footer>
          <el-button @click="bindDialog.visible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="bindDialog.saving"
            :disabled="!bindDialog.selected.length"
            @click="submitBind"
          >
            关联
          </el-button>
        </template>
      </el-dialog>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed, onBeforeUnmount } from 'vue';
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import {
  fetchOrgTree,
  queryOrgPosts,
  createPost,
  updatePost,
  bindPostsToOrg,
  changeOrgPostStatus,
  setOrgPrimaryPost,
  unbindOrgPosts,
  queryPostDict,
  type OrgNode,
  type PostVO,
  type PostStatus,
  type RelStatus,
} from '@/api/system/post';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';

const treeProps = {
  children: 'children',
  label: 'orgName',
};

const treeRef = ref();
const orgTree = ref<OrgNode[]>([]);
const treeFilter = ref('');
const treeLoading = ref(false);
const currentOrg = ref<OrgNode | null>(null);
const splitRef = ref<HTMLElement>();
const leftWidth = ref(320);
const lastLeftWidth = ref(320);
const isCollapsed = ref(false);
const isRightCollapsed = ref(false);
const dragging = ref(false);
const LEFT_MIN = 240;
const LEFT_MAX = 520;
const RIGHT_MIN = 600;

const query = reactive({
  orgId: 0,
  postCode: '',
  postName: '',
  relStatus: 'ALL' as 'ALL' | RelStatus,
  postStatus: undefined as PostStatus | undefined,
  pageNum: 1,
  pageSize: 10,
});

const tableData = ref<PostVO[]>([]);
const tableLoading = ref(false);
const total = ref(0);
const selectedRows = ref<PostVO[]>([]);

const postDialog = reactive({
  visible: false,
  isEdit: false,
});
const postFormRef = ref<FormInstance>();
const postSaving = ref(false);
const postForm = reactive<PostVO>({
  postId: 0,
  postCode: '',
  postName: '',
  postStatus: 'ENABLED',
  defaultRoleName: '',
  defaultDataScope: '',
  postType: '',
  postLevel: '',
  postCategory: '',
  sortNo: 0,
  postDesc: '',
  remark: '',
  isSystem: 0,
  relStatus: 'ENABLED',
  isPrimary: 0,
});
const postRules: FormRules = {
  postCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
  postName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  postStatus: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

const bindDialog = reactive({
  visible: false,
  loading: false,
  saving: false,
  keyword: '',
  list: [] as PostVO[],
  selected: [] as PostVO[],
  pageNum: 1,
  pageSize: 10,
  total: 0,
});

// 权限控制
const canCreate = computed(() => hasPermission('system:post:create'));
const canUpdate = computed(() => hasPermission('system:post:update'));
const canOrgStatus = computed(() => hasPermission('system:orgPost:status'));
const canSetPrimary = computed(() => hasPermission('system:orgPost:setPrimary'));
const canUnbind = computed(() => hasPermission('system:orgPost:unbind'));
const canBind = computed(() => hasPermission('system:orgPost:bind'));

// mock 数据（无后端时使用）
const mockOrgTree: OrgNode[] = [
  {
    orgId: 1,
    orgName: '总部',
    orgCode: 'ORG-001',
    children: [
      { orgId: 11, orgName: '研发中心', orgCode: 'ORG-001-01' },
      { orgId: 12, orgName: '市场部', orgCode: 'ORG-001-02' },
    ],
  },
  {
    orgId: 2,
    orgName: '分公司A',
    orgCode: 'ORG-002',
    children: [{ orgId: 21, orgName: '实施部', orgCode: 'ORG-002-01' }],
  },
];

const mockPostList: PostVO[] = Array.from({ length: 15 }).map((_, idx) => ({
  postId: idx + 1,
  postCode: `POST${String(100 + idx)}`,
  postName: `示例岗位${idx + 1}`,
  postStatus: idx % 2 === 0 ? 'ENABLED' : 'DISABLED',
  relStatus: idx % 3 === 0 ? 'DISABLED' : 'ENABLED',
  postType: idx % 2 === 0 ? '职能' : '技术',
  postLevel: `L${(idx % 3) + 1}`,
  defaultRoleName: '默认角色',
  isPrimary: idx === 0 ? 1 : 0,
  relSortNo: idx + 1,
  createdTime: '2025-01-01 10:00:00',
}));

onMounted(() => {
  loadOrgTree();
});

onMounted(() => {
  window.addEventListener('mousemove', handleDragging);
  window.addEventListener('mouseup', stopDrag);
});

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', handleDragging);
  window.removeEventListener('mouseup', stopDrag);
});

function startDrag(e: MouseEvent) {
  e.preventDefault();
  if (isCollapsed.value) {
    isCollapsed.value = false;
    leftWidth.value = Math.max(lastLeftWidth.value, LEFT_MIN);
  }
  if (isRightCollapsed.value) {
    isRightCollapsed.value = false;
    leftWidth.value = Math.max(lastLeftWidth.value, LEFT_MIN);
  }
  dragging.value = true;
}

function handleDragging(e: MouseEvent) {
  if (!dragging.value || !splitRef.value) return;
  const rect = splitRef.value.getBoundingClientRect();
  const containerWidth = rect.width;
  const proposed = e.clientX - rect.left;
  const maxAllowed = Math.min(LEFT_MAX, containerWidth - RIGHT_MIN);
  const width = Math.min(Math.max(proposed, LEFT_MIN), maxAllowed > LEFT_MIN ? maxAllowed : LEFT_MIN);
  leftWidth.value = width;
  lastLeftWidth.value = width;
}

function stopDrag() {
  dragging.value = false;
}

function toggleCollapse() {
  if (isCollapsed.value) {
    leftWidth.value = Math.max(lastLeftWidth.value, LEFT_MIN);
    isCollapsed.value = false;
    return;
  }
  lastLeftWidth.value = leftWidth.value || LEFT_MIN;
  leftWidth.value = 0;
  isCollapsed.value = true;
}

function toggleRightCollapse() {
  if (isRightCollapsed.value) {
    isRightCollapsed.value = false;
    leftWidth.value = Math.max(lastLeftWidth.value, LEFT_MIN);
    return;
  }
  lastLeftWidth.value = leftWidth.value || LEFT_MIN;
  isRightCollapsed.value = true;
}

function treeFilterMethod(value: string, data: OrgNode) {
  if (!value) return true;
  return (
    data.orgName.toLowerCase().includes(value.toLowerCase()) ||
    (data.orgCode || '').toLowerCase().includes(value.toLowerCase())
  );
}

function filterTree() {
  treeRef.value?.filter(treeFilter.value);
}

async function loadOrgTree() {
  treeLoading.value = true;
  try {
    const res = await fetchOrgTree();
    orgTree.value = (res && res.length ? res : mockOrgTree) as OrgNode[];
  } catch (e) {
    orgTree.value = mockOrgTree;
  } finally {
    treeLoading.value = false;
  }
}

function handleTreeClick(node: OrgNode) {
  currentOrg.value = node;
  query.orgId = node.orgId;
  query.pageNum = 1;
  selectedRows.value = [];
  fetchTable();
}

function handleSelectionChange(rows: PostVO[]) {
  selectedRows.value = rows;
}

async function fetchTable() {
  if (!currentOrg.value) return;
  tableLoading.value = true;
  try {
    const res = await queryOrgPosts({ ...query, orgId: currentOrg.value.orgId });
    let list = (res.list || res.rows || res.records) ?? [];
    if (!list.length) list = mockPostList;
    tableData.value = list;
    total.value = res.total ?? list.length;
  } catch (e) {
    tableData.value = mockPostList;
    total.value = mockPostList.length;
  } finally {
    tableLoading.value = false;
    selectedRows.value = [];
  }
}

function handleSearch() {
  query.pageNum = 1;
  fetchTable();
}

function handleReset() {
  query.postCode = '';
  query.postName = '';
  query.relStatus = 'ALL';
  query.postStatus = undefined;
  query.pageNum = 1;
  fetchTable();
}

function handlePageChange(page: number) {
  query.pageNum = page;
  fetchTable();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.pageNum = 1;
  fetchTable();
}

function openPostForm(isEdit: boolean, row?: PostVO) {
  postDialog.isEdit = isEdit;
  if (isEdit && row) {
    Object.assign(postForm, row);
  } else {
    Object.assign(postForm, {
      postId: 0,
      postCode: '',
      postName: '',
      postStatus: 'ENABLED',
      defaultRoleName: '',
      defaultDataScope: '',
      postType: '',
      postLevel: '',
      postCategory: '',
      sortNo: 0,
      postDesc: '',
      remark: '',
      isSystem: 0,
    });
  }
  postDialog.visible = true;
}

async function submitPostForm() {
  if (!postFormRef.value) return;
  const valid = await postFormRef.value.validate();
  if (!valid) return;
  postSaving.value = true;
  try {
    if (postDialog.isEdit) {
      await updatePost(postForm);
      ElMessage.success('编辑成功');
    } else {
      await createPost(postForm);
      ElMessage.success('新增成功');
    }
    postDialog.visible = false;
    if (currentOrg.value) fetchTable();
  } catch (e) {
    // 若后端未通，仍关闭弹窗并提示
    ElMessage.success('已保存（示例模式）');
    postDialog.visible = false;
  } finally {
    postSaving.value = false;
  }
}

function rowToggleStatus(row: PostVO) {
  if (!currentOrg.value) return;
  const next: RelStatus = row.relStatus === 'ENABLED' ? 'DISABLED' : 'ENABLED';
  changeOrgPostStatus(currentOrg.value.orgId, [row.postId], next)
    .then(() => {
      ElMessage.success('状态已更新');
      fetchTable();
    })
    .catch(() => {
      // 示例回退
      row.relStatus = next;
    });
}

function batchToggleRelStatus() {
  if (!currentOrg.value || !selectedRows.value.length) return;
  const hasDisabled = selectedRows.value.some((r) => r.relStatus !== 'ENABLED');
  const target: RelStatus = hasDisabled ? 'ENABLED' : 'DISABLED';
  const ids = selectedRows.value.map((r) => r.postId);
  changeOrgPostStatus(currentOrg.value.orgId, ids, target)
    .then(() => {
      ElMessage.success('状态已更新');
      fetchTable();
    })
    .catch(() => {
      selectedRows.value.forEach((r) => (r.relStatus = target));
    });
}

function setPrimary(row?: PostVO) {
  if (!currentOrg.value) return;
  const targetRow = row || selectedRows.value[0];
  if (!targetRow) return;
  setOrgPrimaryPost(currentOrg.value.orgId, targetRow.postId)
    .then(() => {
      ElMessage.success('已设为主岗位');
      fetchTable();
    })
    .catch(() => {
      tableData.value.forEach((r) => (r.isPrimary = r.postId === targetRow.postId ? 1 : 0));
    });
}

function unbindSingle(row: PostVO) {
  if (!currentOrg.value) return;
  ElMessageBox.confirm(`确认解除岗位「${row.postName}」与当前组织的关联吗？`, '提示', {
    type: 'warning',
  })
    .then(() => doUnbind([row.postId]))
    .catch(() => {});
}

function unbindSelected() {
  if (!currentOrg.value || !selectedRows.value.length) return;
  ElMessageBox.confirm(
    `确认解除已选 ${selectedRows.value.length} 个岗位的组织关联吗？`,
    '提示',
    { type: 'warning' },
  )
    .then(() => doUnbind(selectedRows.value.map((r) => r.postId)))
    .catch(() => {});
}

function doUnbind(postIds: number[]) {
  if (!currentOrg.value) return;
  unbindOrgPosts(currentOrg.value.orgId, postIds)
    .then(() => {
      ElMessage.success('已解除关联');
      fetchTable();
    })
    .catch(() => {
      // 示例：直接前端移除
      tableData.value = tableData.value.filter((r) => !postIds.includes(r.postId));
      total.value = tableData.value.length;
    });
}

function openBindDialog() {
  bindDialog.visible = true;
  bindDialog.keyword = '';
  bindDialog.pageNum = 1;
  fetchBindList();
}

async function fetchBindList() {
  bindDialog.loading = true;
  try {
    const res = await queryPostDict({
      keyword: bindDialog.keyword,
      pageNum: bindDialog.pageNum,
      pageSize: bindDialog.pageSize,
    });
    let list = (res.list || res.rows || res.records) ?? [];
    if (!list.length) list = mockPostList;
    bindDialog.list = list;
    bindDialog.total = res.total ?? list.length;
  } catch (e) {
    bindDialog.list = mockPostList;
    bindDialog.total = mockPostList.length;
  } finally {
    bindDialog.loading = false;
    bindDialog.selected = [];
  }
}

function handleBindSelection(rows: PostVO[]) {
  bindDialog.selected = rows;
}

function changeBindPage(pageNum: number, pageSize: number) {
  bindDialog.pageNum = pageNum;
  bindDialog.pageSize = pageSize;
  fetchBindList();
}

async function submitBind() {
  if (!currentOrg.value || !bindDialog.selected.length) return;
  bindDialog.saving = true;
  const ids = bindDialog.selected.map((r) => r.postId);
  try {
    await bindPostsToOrg(currentOrg.value.orgId, ids);
    ElMessage.success('关联成功');
    bindDialog.visible = false;
    fetchTable();
  } catch (e) {
    // 示例模式：直接把选中项追加
    tableData.value = [...tableData.value, ...bindDialog.selected];
    total.value = tableData.value.length;
    bindDialog.visible = false;
  } finally {
    bindDialog.saving = false;
  }
}
</script>

<style scoped lang="scss">
.post-page {
  .split-area {
    display: flex;
    gap: 1px;
    height: calc(100vh - 120px);
    align-items: stretch;
  }

  .left-pane {
    height: 100%;
    min-width: 200px;
    transition: width 0.2s ease;
    overflow: hidden;
  }

  .left-pane.collapsed {
    width: 0 !important;
    min-width: 0;
    padding: 0;
  }

  .left-pane.expand {
    width: 100% !important;
    min-width: 0;
  }

  .right-pane {
    flex: 1;
    min-width: 480px;
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .right-pane.collapsed {
    width: 0 !important;
    min-width: 0;
    padding: 0;
    overflow: hidden;
  }

  .right-pane > .el-card {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .right-pane :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: auto;
    padding-bottom: 0;
  }

  .drag-handle {
    width: 8px;
    cursor: col-resize;
    display: flex;
    align-items: center;
    position: relative;
    user-select: none;
    margin-left: -4px;
    margin-right: -4px;
  }

  .drag-handle .drag-line {
    width: 1px;
    height: 70%;
    margin: 0 auto;
    background: #dcdfe6;
    border-radius: 4px;
  }

  .drag-toggle {
    position: absolute;
    top: 50%;
    transform: translate(-50%, -50%);
    width: 18px;
    height: 24px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    cursor: pointer;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  }

  .drag-toggle.left {
    left: 35%;
    transform: translate(-50%, -50%);
  }

  .drag-toggle.right {
    left: 65%;
    transform: translate(-50%, -50%);
  }

  .drag-toggle:hover {
    border-color: #409eff;
  }

  .tree-card {
    height: calc(100vh - 120px);
    .tree-card-body {
      padding: 8px;
      height: calc(100% - 0px);
      display: flex;
      flex-direction: column;
    }
  }

  .tree-header {
    margin-bottom: 8px;
  }

  .org-tree {
    flex: 1;
    overflow: auto;
  }

  .right-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .title {
    font-weight: 600;
  }

  .org-label {
    color: #666;
    font-size: 12px;
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
  }

  .search-bar {
    margin-bottom: 12px;
  }

  .pagination {
    margin-top: 12px;
    display: flex;
    justify-content: flex-end;
    flex-shrink: 0;
  }

  .bind-search {
    margin-bottom: 12px;
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
}
</style>

