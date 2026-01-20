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
              <div class="tree-controls">
                <el-input
                  v-model="treeFilter"
                  size="small"
                  placeholder="搜索组织名称/编码"
                  clearable
                  @input="filterTree"
                  style="width: 280px;"
                />
              </div>
              <div class="tree-buttons">
                <el-button
                  size="small"
                  type="text"
                  @click="toggleTreeExpansion"
                  :title="isTreeExpanded ? '收起组织树' : '展开组织树'"
                  class="tree-toggle-btn"
                >
                  <el-icon :size="14">
                    <ArrowDown v-if="isTreeExpanded" />
                    <ArrowUp v-else />
                  </el-icon>
                </el-button>
                <el-button
                  size="small"
                  type="primary"
                  @click="toggleEditMode"
                  :title="isEditMode ? '退出编辑' : '编辑组织'"
                >
                  {{ isEditMode ? '完成' : '编辑' }}
                </el-button>
              </div>
            </div>
            <el-tree
              ref="treeRef"
              :class="['org-tree', { 'edit-mode': isEditMode }]"
              node-key="orgId"
              :data="orgTree"
              :props="treeProps"
              highlight-current
              :filter-node-method="treeFilterMethod"
              @node-click="handleTreeClick"
              v-loading="treeLoading"
            >
              <template #default="{ node, data }">
                <div class="custom-tree-node">
                  <span class="node-label">{{ node.label }}</span>
                  <span v-if="isEditMode && currentTreeOrg?.orgId === data.orgId" class="node-actions">
                    <el-icon @click.stop="showAddOrgDialog(data)" title="新增子组织" class="action-icon add-icon">
                      <Plus />
                    </el-icon>
                    <el-icon @click.stop="showEditOrgDialog(data)" title="修改组织" class="action-icon edit-icon">
                      <Edit />
                    </el-icon>
                    <el-icon @click.stop="confirmDeleteOrg(data)" title="删除组织" class="action-icon delete-icon">
                      <Delete />
                    </el-icon>
                  </span>
                </div>
              </template>
            </el-tree>
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
                    :disabled="selectedRows.length !== 1"
                    @click="openBindDialog"
                >
                  关联人员
                </el-button>
                <el-button
                    size="small"
                    @click="batchToggleRelStatus"
                    :disabled="!currentOrg || !selectedRows.length || !canOrgStatus"
                >
                  启用/停用
                </el-button>
                <el-button size="small" :disabled="true">导入</el-button>
                <el-button size="small" :disabled="true">导出</el-button>
              </div>
<!--              <div class="org-label" v-if="currentOrg">-->
<!--                当前组织：{{ currentOrg.orgName }}-->
<!--              </div>-->
            </div>



            <!-- 查询栏 -->
            <el-form
              :model="query"
              class="search-bar"
              @submit.prevent
            >
              <div class="search-row">
                <el-form-item label="岗位编码">
                  <el-input
                    v-model="query.postCode"
                    placeholder="请输入岗位编码"
                    clearable
                    style="width: 160px"
                  />
                </el-form-item>
                <el-form-item label="岗位名称">
                  <el-input
                    v-model="query.postName"
                    placeholder="请输入岗位名称"
                    clearable
                    style="width: 160px"
                  />
                </el-form-item>
                <el-form-item label="类型">
                  <el-input
                    v-model="query.postType"
                    placeholder="请输入类型"
                    clearable
                    style="width: 140px"
                  />
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="query.postStatus" clearable style="width: 100px">
                    <el-option label="启用" value="ENABLED" />
                    <el-option label="停用" value="DISABLED" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </div>
            </el-form>

            <!-- 未选组织提示 -->
            <el-empty
              v-if="!currentOrg"
              description="请选择组织后再操作岗位"
              :image-size="120"
            />

            <!-- 表格 -->
            <el-table
              ref="tableRef"
              v-else
              v-loading="tableLoading"
              :data="tableData"
              row-key="postId"
              border
              style="width: 100%"
              @selection-change="handleSelectionChange"
              @row-click="handleRowClick"
            >
              <el-table-column type="selection" width="50" />
              <el-table-column prop="postCode" label="岗位编码" min-width="140" />
              <el-table-column prop="postName" label="岗位名称" min-width="140" />
              <el-table-column prop="postType" label="类型" min-width="70" />
              <el-table-column label="状态" min-width="70">
                <template #default="{ row }">
                  <el-tag :type="row.postStatus === 'ENABLED' ? 'success' : 'info'">
                    {{ row.postStatus === 'ENABLED' ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="createdTime"
                label="创建时间"
                min-width="170"
                :formatter="formatDateTime"
              />
              <el-table-column label="操作" fixed="right" width="120">
                <template #default="{ row }">
                  <el-button
                    link
                    type="primary"
                    size="small"
                    @click="openPostForm(true, row)"
                    :disabled="!canUpdate"
                  >
                    编辑
                  </el-button>
                  <el-button
                    link
                    size="small"
                    @click="rowToggleStatus(row)"
                    :disabled="!canOrgStatus"
                  >
                    停用
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

      <!-- 新增/编辑组织弹窗 -->
      <el-dialog
        v-model="orgDialog.visible"
        :title="orgDialog.isEdit ? '修改组织' : '新增组织'"
        width="720px"
        destroy-on-close
      >
        <el-form
          ref="orgFormRef"
          :model="orgForm"
          :rules="orgRules"
          label-width="100px"
          class="dialog-form two-col"
        >
          <el-form-item label="组织编码" prop="orgCode">
            <el-input v-model="orgForm.orgCode" readonly class="readonly-input" />
          </el-form-item>
          <el-form-item label="组织名称" prop="orgName">
            <el-input v-model="orgForm.orgName" placeholder="请输入组织名称" />
          </el-form-item>
          <el-form-item label="组织类型" prop="orgType">
            <el-select v-model="orgForm.orgType" placeholder="请选择组织类型" style="width: 100%">
              <el-option label="公司/法人主体" value="COMPANY" />
              <el-option label="部门" value="DEPT" />
              <el-option label="项目部/项目组织" value="PROJECT" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="上级组织" prop="parentOrgId">
            <el-input
              v-model="orgForm.parentOrgName"
              readonly
              placeholder="请选择上级组织"
              @click="openParentOrgSelector"
              style="cursor: pointer"
            >
              <template #suffix>
                <el-icon class="cursor-pointer">
                  <Search />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="负责人员" prop="managerPersonId">
            <PersonSelector
              v-model="selectedManager"
              placeholder="请选择负责人员"
              @change="handleManagerChange"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="orgForm.status" placeholder="请选择状态" style="width: 100%">
              <el-option label="启用" value="ENABLED" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" class="full-row">
            <el-input
              v-model="orgForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="orgDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="orgDialog.saving" @click="submitOrgForm">
            保存
          </el-button>
        </template>
      </el-dialog>

      <!-- 上级组织选择器弹窗 -->
      <el-dialog
        v-model="parentOrgDialog.visible"
        title="选择上级组织"
        width="900px"
        destroy-on-close
      >
        <div class="parent-org-search">
          <el-input
            v-model="parentOrgDialog.keyword"
            placeholder="搜索组织编号"
            clearable
            style="width: 240px"
            @change="fetchParentOrgList"
          />
        </div>
        <el-table
          v-loading="parentOrgDialog.loading"
          :data="parentOrgDialog.list"
          row-key="orgId"
          height="400px"
          highlight-current-row
          @current-change="handleParentOrgSelect"
        >
          <el-table-column prop="orgCode" label="组织编号" min-width="140" />
          <el-table-column prop="orgName" label="组织名称" min-width="140" />
          <el-table-column prop="parentOrgCode" label="上级组织编号" min-width="140" />
          <el-table-column prop="parentOrgName" label="上级组织名称" min-width="140" />
          <el-table-column prop="managerName" label="负责人" min-width="100" />
          <el-table-column label="状态" min-width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
                {{ row.status === 'ENABLED' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <template #footer>
          <el-button @click="parentOrgDialog.visible = false">取消</el-button>
          <el-button
            type="primary"
            :disabled="!parentOrgDialog.selected"
            @click="confirmParentOrgSelect"
          >
            确定
          </el-button>
        </template>
      </el-dialog>

    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed, onBeforeUnmount,nextTick } from 'vue';
import { ArrowLeft, ArrowRight, ArrowUp, ArrowDown, Plus, Edit, Delete, Search } from '@element-plus/icons-vue';
import {
  fetchOrgTree,
  queryOrgPosts,
  createPost,
  updatePost,
  queryPostDict,
  createOrg,
  deleteOrg,
  type OrgNode,
  type PostVO,
  type PostStatus, changePostStatus,
} from '@/api/system/post';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import PersonSelector from '@/components/Selector/PersonSelector.vue';
import type { ExpPersonVO } from '@/api/system/person';

const treeProps = {
  children: 'children',
  label: 'orgName',
};

const treeRef = ref();
const orgTree = ref<OrgNode[]>([]);
const treeFilter = ref('');
const treeLoading = ref(false);
const currentOrg = ref<OrgNode | null>(null);
const currentTreeOrg = ref<OrgNode | null>(null); // 左侧树选中的组织
const originalTableData = ref<PostVO[]>([]); // 原始表格数据，用于前端筛选
const isTreeExpanded = ref(false); // 组织树是否展开
const isEditMode = ref(false); // 编辑模式
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
  postStatus: undefined as PostStatus | undefined,
  postType: '',
  pageNum: 1,
  pageSize: 10,
});

const tableData = ref<PostVO[]>([]);
const tableLoading = ref(false);
const total = ref(0);
const selectedRows = ref<PostVO[]>([]);
const tableRef = ref();

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
  defaultDataScope: '',
  postType: '',
  postLevel: '',
  postCategory: '',
      sortNo: 0,
      postDesc: '',
      remark: '',
      isSystem: 0,
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

// 组织表单弹窗
const orgDialog = reactive({
  visible: false,
  isEdit: false,
  saving: false,
});

const orgFormRef = ref<FormInstance>();
const orgForm = reactive({
  orgId: undefined as number | undefined,
  orgCode: '',
  orgName: '',
  orgType: 'OTHER' as 'COMPANY' | 'DEPT' | 'PROJECT' | 'OTHER',
  parentOrgId: undefined as number | undefined,
  parentOrgName: '',
  managerPersonId: undefined as number | undefined,
  managerName: '',
  status: 'ENABLED' as 'ENABLED' | 'DISABLED',
  remark: '',
});

const orgRules: FormRules = {
  orgCode: [{ required: true, message: '组织编码不能为空', trigger: 'blur' }],
  orgName: [{ required: true, message: '请输入组织名称', trigger: 'blur' }],
  orgType: [{ required: true, message: '请选择组织类型', trigger: 'change' }],
  parentOrgId: [{ required: true, message: '请选择上级组织', trigger: 'change' }],
  managerPersonId: [{ required: true, message: '请选择负责人员', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

const selectedManager = ref<ExpPersonVO>();

// 上级组织选择器弹窗
const parentOrgDialog = reactive({
  visible: false,
  loading: false,
  keyword: '',
  list: [] as OrgNode[],
  selected: null as OrgNode | null,
});

// 权限控制
const canCreate = computed(() => hasPermission('system:post:create'));
const canUpdate = computed(() => hasPermission('system:post:update'));
const canOrgStatus = computed(() => hasPermission('system:orgPost:status'));

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

// 切换树形结构展开/收起
function toggleTreeExpansion() {
  isTreeExpanded.value = !isTreeExpanded.value;
  
  // 获取所有节点
  const allNodes = getAllTreeNodes(orgTree.value);
  
  if (isTreeExpanded.value) {
    // 展开所有节点
    allNodes.forEach(node => {
      treeRef.value?.store.nodesMap[node.orgId]?.expand();
    });
  } else {
    // 收起所有节点
    allNodes.forEach(node => {
      treeRef.value?.store.nodesMap[node.orgId]?.collapse();
    });
  }
}

// 递归获取所有节点
function getAllTreeNodes(nodes: OrgNode[]): OrgNode[] {
  const result: OrgNode[] = [];
  
  function traverse(nodeList: OrgNode[]) {
    nodeList.forEach(node => {
      result.push(node);
      if (node.children && node.children.length > 0) {
        traverse(node.children);
      }
    });
  }
  
  traverse(nodes);
  return result;
}

async function loadOrgTree() {
  treeLoading.value = true;
  try {
    const res = await fetchOrgTree();
    // 由于请求拦截器已经处理了响应，直接使用返回的数据
    orgTree.value = (res && Array.isArray(res) && res.length ? res : []) as OrgNode[];
    // ✅ 自动选中第一个根节点（如果存在）
    if (orgTree.value.length > 0) {
      const firstRoot = orgTree.value[0];
      if (firstRoot) {
        handleTreeClick(firstRoot);

        const orgId = firstRoot.orgId;
        const hasChildren = Array.isArray(firstRoot.children) && firstRoot.children.length > 0;

        nextTick(() => {
          treeRef.value?.setCurrentKey(orgId);
          if (hasChildren) {
            treeRef.value?.store.nodesMap[orgId]?.expand();
          }
        });
      }
    } else {
      // 如果没有组织，清空右侧
      currentOrg.value = null;
      tableData.value = [];
      total.value = 0;
    }
  } catch (e) {
    console.error('加载组织树失败:', e);
    orgTree.value = [];
  } finally {
    treeLoading.value = false;
  }
}

function handleTreeClick(node: OrgNode) {
  currentTreeOrg.value = node;
  currentOrg.value = node;
  query.orgId = node.orgId;
  query.pageNum = 1;
  selectedRows.value = [];
  fetchTable();
}

function handleSelectionChange(rows: PostVO[]) {
  selectedRows.value = rows;
}

function handleRowClick(row: PostVO) {
  // 使用表格的toggleRowSelection方法切换选中状态
  tableRef.value?.toggleRowSelection(row);
}

async function fetchTable() {
  if (!currentOrg.value) return;
  tableLoading.value = true;
  try {
    // 构造查询参数
    const searchQuery = {
      orgId: currentOrg.value.orgId,
      includeChildren: true,
      postCode: query.postCode,
      postName: query.postName,
      postStatus: query.postStatus,
      postType: query.postType,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    };

    const res = await queryOrgPosts(searchQuery);
    let list = (res && Array.isArray(res)) ? res : [];

    // 🔧 统一字段
    list = list.map(item => ({
      ...item,
      postStatus: item.postStatus ?? (item as any).status,
    }));

    // 应用前端筛选（根据岗位状态）
    tableData.value = list.filter(item => {
      if (!query.postStatus) return true;
      return item.postStatus === query.postStatus;
    });

    // 保存原始数据
    originalTableData.value = list;
    total.value = tableData.value.length;
  } catch (e) {
    console.error('加载岗位列表失败:', e);
    originalTableData.value = [];
    tableData.value = [];
    total.value = 0;
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
  query.postType = '';
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
    ElMessage.error((e as any)?.message || '操作失败');
  } finally {
    postSaving.value = false;
  }
}

function rowToggleStatus(row: PostVO) {
  const next: PostStatus = row.postStatus === 'ENABLED' ? 'DISABLED' : 'ENABLED';
  changePostStatus([row.postId], next)
    .then(() => {
      ElMessage.success('状态已更新');
      fetchTable();
    })
    .catch(() => {
      ElMessage.error('状态更新失败');
    });
}

function batchToggleRelStatus() {
  if (!selectedRows.value.length) return;
  const hasDisabled = selectedRows.value.some((r) => r.postStatus !== 'ENABLED');
  const target: PostStatus = hasDisabled ? 'ENABLED' : 'DISABLED';
  const ids = selectedRows.value.map((r) => r.postId);
  changePostStatus(ids, target)
    .then(() => {
      ElMessage.success('状态已更新');
      fetchTable();
    })
    .catch(() => {
      ElMessage.error('状态更新失败');
    });
}


function openBindDialog() {
  // 新版本API暂不支持此功能
  ElMessage.warning('新版本API暂不支持关联岗位功能');
}

async function fetchBindList() {
  bindDialog.loading = true;
  try {
    const res = await queryPostDict({
      keyword: bindDialog.keyword,
      pageNum: bindDialog.pageNum,
      pageSize: bindDialog.pageSize,
    });
    let list = (res?.success && res.data?.list) ? res.data.list : [];
    bindDialog.list = list;
    bindDialog.total = res?.data?.total ?? list.length;
  } catch (e) {
    console.error('加载岗位字典失败:', e);
    bindDialog.list = [];
    bindDialog.total = 0;
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
  // 新版本API暂不支持此功能
  ElMessage.warning('新版本API暂不支持关联岗位功能');
}

// 切换编辑模式
function toggleEditMode() {
  isEditMode.value = !isEditMode.value;
  if (!isEditMode.value) {
    // 退出编辑模式时，清除选中状态
    currentTreeOrg.value = null;
  }
}

// 显示添加组织对话框
function showAddOrgDialog(node?: OrgNode) {
  if (!currentTreeOrg.value && !node) return;
  
  orgDialog.isEdit = false;
  orgDialog.visible = true;
  
  // 重置表单
  orgForm.orgId = undefined;
  orgForm.orgCode = generateOrgCode();
  orgForm.orgName = '';
  orgForm.orgType = 'OTHER';
  orgForm.parentOrgId = currentTreeOrg.value?.orgId;
  orgForm.parentOrgName = currentTreeOrg.value?.orgName || '';
  orgForm.managerPersonId = undefined;
  orgForm.managerName = '';
  orgForm.status = 'ENABLED';
  orgForm.remark = '';
  selectedManager.value = undefined;
}

// 显示编辑组织对话框
function showEditOrgDialog(node: OrgNode) {
  orgDialog.isEdit = true;
  orgDialog.visible = true;
  
  // 回显数据 - TODO: 需要调用后端接口获取完整数据
  orgForm.orgId = node.orgId;
  orgForm.orgCode = node.orgCode || '';
  orgForm.orgName = node.orgName;
  orgForm.orgType = 'OTHER'; // TODO: 从后端获取
  orgForm.parentOrgId = undefined; // TODO: 从后端获取
  orgForm.parentOrgName = '';
  orgForm.managerPersonId = undefined;
  orgForm.managerName = '';
  orgForm.status = 'ENABLED';
  orgForm.remark = '';
  selectedManager.value = undefined;
}

// 打开上级组织选择器
function openParentOrgSelector() {
  parentOrgDialog.visible = true;
  parentOrgDialog.keyword = '';
  fetchParentOrgList();
}

// 获取上级组织列表
async function fetchParentOrgList() {
  parentOrgDialog.loading = true;
  try {
    const res = await fetchOrgTree({ keyword: parentOrgDialog.keyword });
    // 将树形结构展开为列表
    parentOrgDialog.list = flattenOrgTree(res || []);
  } catch (e) {
    parentOrgDialog.list = [];
  } finally {
    parentOrgDialog.loading = false;
  }
}

// 将组织树展开为列表
function flattenOrgTree(nodes: OrgNode[], parentInfo?: { code?: string; name?: string }): OrgNode[] {
  const result: OrgNode[] = [];
  nodes.forEach(node => {
    const flatNode = {
      ...node,
      parentOrgCode: parentInfo?.code || '',
      parentOrgName: parentInfo?.name || '',
      managerName: '', // TODO: 从后端获取
      status: 'ENABLED' as const, // TODO: 从后端获取
    };
    result.push(flatNode);
    if (node.children && node.children.length > 0) {
      result.push(...flattenOrgTree(node.children, { code: node.orgCode, name: node.orgName }));
    }
  });
  return result;
}

// 选择上级组织
function handleParentOrgSelect(row: OrgNode | null) {
  parentOrgDialog.selected = row;
}

// 确认选择上级组织
function confirmParentOrgSelect() {
  if (!parentOrgDialog.selected) return;
  orgForm.parentOrgId = parentOrgDialog.selected.orgId;
  orgForm.parentOrgName = parentOrgDialog.selected.orgName;
  parentOrgDialog.visible = false;
}

// 选择负责人
function handleManagerChange(person: ExpPersonVO | undefined) {
  orgForm.managerPersonId = person?.personId;
  orgForm.managerName = person?.personName || '';
}

// 提交组织表单
async function submitOrgForm() {
  if (!orgFormRef.value) return;
  
  const valid = await orgFormRef.value.validate();
  if (!valid) return;
  
  orgDialog.saving = true;
  try {
    const payload = {
      orgName: orgForm.orgName.trim(),
      orgCode: orgForm.orgCode,
      parentOrgId: orgForm.parentOrgId,
      orgType: orgForm.orgType,
      managerPersonId: orgForm.managerPersonId,
    };
    
    if (orgDialog.isEdit) {
      // TODO: 调用修改接口
      ElMessage.success('修改成功');
    } else {
      await createOrg(payload);
      ElMessage.success('新增成功');
    }
    
    orgDialog.visible = false;
    loadOrgTree(); // 重新加载组织树
  } catch (e) {
    ElMessage.error(orgDialog.isEdit ? '修改失败' : '新增失败');
  } finally {
    orgDialog.saving = false;
  }
}

// 生成组织编码
function generateOrgCode() {
  const now = new Date();
  const year = String(now.getFullYear()).slice(-2);
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const rand = String(Math.floor(Math.random() * 1000)).padStart(3, '0');
  return `org${year}${month}${day}${rand}`;
}

// 确认删除组织
function confirmDeleteOrg(node?: OrgNode) {
  const targetNode = node || currentTreeOrg.value;
  if (!targetNode) return;

  // 检查是否有子节点
  if (targetNode.children && targetNode.children.length > 0) {
    ElMessage.warning('该组织存在子组织，无法删除！');
    return;
  }

  ElMessageBox.confirm(
    `确定要删除组织「${targetNode.orgName}」吗？删除后不可恢复！`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
    }
  ).then(() => {
    deleteOrgAction(targetNode);
  }).catch(() => {});
}

// 执行删除组织
async function deleteOrgAction(node: OrgNode) {
  try {
    await deleteOrg(node.orgId);
    ElMessage.success('组织删除成功');
    currentTreeOrg.value = null;
    currentOrg.value = null;
    loadOrgTree(); // 重新加载组织树
  } catch (e) {
    ElMessage.error('组织删除失败');
  }
}

function formatDateTime(row: any, column: any, cellValue: string) {
  if (!cellValue) return '';
  try {
    const date = new Date(cellValue);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  } catch (e) {
    return cellValue;
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

  .tree-controls {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }

  .tree-buttons {
    display: flex;
    gap: 1px;
    justify-content: flex-end;
    align-items: center;
  }
  .tree-buttons .el-button {
    padding: 0 4px;  /* 左右内边距更小 */
    min-width: 20px; /* 最小宽度 */
    height: 20px;    /* 高度 */
    font-size: 12px; /* 字体更小 */
  }
  .tree-toggle-btn {
    background: transparent !important;
    border: none !important;
    &:hover {
      background: rgba(0, 0, 0, 0.04) !important;
    }
  }

  .add-org-content {
    .add-org-tip {
      margin-bottom: 12px;
      color: #666;
      font-size: 14px;
    }

    .el-input {
      width: 100%;
    }
  }

  .org-tree {
    flex: 1;
    overflow: auto;
  }

  // 编辑模式下的树节点样式
  .custom-tree-node {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding-right: 8px;

    .node-label {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .node-actions {
      display: flex;
      gap: 8px;
      align-items: center;
      margin-left: 8px;

      .action-icon {
        cursor: pointer;
        font-size: 16px;
        padding: 2px;
        border-radius: 4px;
        transition: all 0.2s;

        &:hover {
          background-color: rgba(0, 0, 0, 0.05);
        }
      }

      .add-icon {
        color: #67c23a;
        &:hover {
          color: #529b2e;
          background-color: rgba(103, 194, 58, 0.1);
        }
      }

      .edit-icon {
        color: #409eff;
        &:hover {
          color: #337ecc;
          background-color: rgba(64, 158, 255, 0.1);
        }
      }

      .delete-icon {
        color: #f56c6c;
        &:hover {
          color: #c45656;
          background-color: rgba(245, 108, 108, 0.1);
        }
      }
    }
  }

  // 编辑模式下的树样式调整
  .org-tree.edit-mode {
    :deep(.el-tree-node__content) {
      padding-right: 0;
    }
  }

  .right-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
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
    margin-bottom: 5px;
    margin-top: 10px;
    margin-right: 10px;
  }

  .search-bar {
    margin-bottom: 12px;

    .search-row {
      display: flex;
      align-items: flex-end;
      gap: 12px;
      flex-wrap: nowrap;

      :deep(.el-form-item) {
        margin-bottom: 0;
      }
    }
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

  .readonly-input :deep(.el-input__inner) {
    background-color: #f5f7fa;
    color: #606266;
    cursor: not-allowed;
  }

  .cursor-pointer {
    cursor: pointer;
  }

  .parent-org-search {
    margin-bottom: 12px;
  }
}
</style>

