<template>
  <el-config-provider :locale="zhCn">
    <div class="menu-page">
      <div class="split-area">
        <!-- 左侧菜单树 -->
        <div class="left-pane">
          <el-card class="tree-card" body-class="tree-card-body">
            <div class="tree-header">
              <el-input
                v-model="treeFilter"
                size="small"
                placeholder="搜索菜单名称/菜单编码"
                clearable
                @input="filterTree"
              />
            </div>
            <el-tree
              ref="treeRef"
              class="menu-tree"
              node-key="menuId"
              :data="menuTree"
              :props="treeProps"
              highlight-current
              :filter-node-method="treeFilterMethod"
              :default-expanded-keys="expandedKeys"
              @node-click="handleTreeClick"
              v-loading="treeLoading"
            >
              <template #default="{ data }">
                <span class="tree-node">
                  <span class="node-name">{{ data.menuName }}</span>
                  <el-tag size="small" class="node-type" :type="typeTagType(data.menuType)">
                    {{ typeText(data.menuType) }}
                  </el-tag>
                </span>
              </template>
            </el-tree>
          </el-card>
        </div>

        <!-- 右侧列表 -->
        <div class="right-pane">
          <el-card class="list-card">
            <template #header>
              <div class="right-header">
                <div class="title">菜单管理</div>
                <div class="actions">
                  <el-button type="primary" size="small" @click="openCreateRoot" :disabled="!canCreate">
                    新增根节点
                  </el-button>
                  <el-button size="small" @click="openCreateChild" :disabled="!currentNode || !canCreate">
                    新增子节点
                  </el-button>
                  <el-button size="small" :disabled="true">导入</el-button>
                  <el-button size="small" :disabled="true">导出</el-button>
                </div>
              </div>
              <div class="org-label" v-if="currentNode">
                当前位置：{{ currentNode.menuName }}（{{ typeText(currentNode.menuType) }}）
              </div>
              <div class="org-label" v-else>当前位置：根节点</div>
              <div class="header-divider"></div>
            </template>

            <!-- 查询区 -->
            <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
              <el-form-item label="编码">
                <el-input
                  v-model="query.menuCode"
                  placeholder="请输入菜单编码"
                  clearable
                  style="width: 150px"
                />
              </el-form-item>
              <el-form-item label="名称">
                <el-input
                  v-model="query.menuName"
                  placeholder="请输入菜单名称"
                  clearable
                  style="width: 150px"
                />
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="query.menuType" clearable placeholder="全部" style="width: 80px">
                  <el-option label="目录" value="CATALOG" />
                  <el-option label="菜单" value="MENU" />
                  <el-option label="按钮" value="BUTTON" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="query.status" clearable placeholder="全部" style="width: 80px">
                  <el-option label="启用" value="ENABLED" />
                  <el-option label="停用" value="DISABLED" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- 列表区 -->
            <el-table
              v-loading="tableLoading"
              :data="tableData"
              row-key="menuId"
              border
              style="width: 100%"
            >
              <el-table-column prop="menuName" label="名称" min-width="160" />
              <el-table-column label="类型" min-width="80">
                <template #default="{ row }">
                  <el-tag size="small" :type="typeTagType(row.menuType)">
                    {{ typeText(row.menuType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="routePath" label="路由/路径" min-width="160" />
              <el-table-column prop="component" label="组件" min-width="200" />
              <el-table-column prop="menuCode" label="菜单编码" min-width="180" />
<!--              <el-table-column prop="icon" label="图标" min-width="140" />-->
<!--              <el-table-column prop="sortNo" label="排序" min-width="60" />-->
              <el-table-column label="可见" min-width="90">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.visible === 1 ? 'success' : 'info'">
                    {{ row.visible === 1 ? '显示' : '隐藏' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" min-width="90">
                <template #default="{ row }">
                  <el-tag size="small" :type="statusTagType(row.status)">
                    {{ statusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" fixed="right" width="150">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="openEdit(row)" :disabled="!canUpdate">
                    编辑
                  </el-button>
                  <el-button
                    link
                    size="small"
                    @click="toggleStatus(row)"
                    :disabled="!canStatus"
                  >
                    {{ isEnabledStatus(row.status) ? '停用' : '启用' }}
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    size="small"
                    @click="handleDelete(row)"
                    :disabled="!canDelete"
                  >
                    删除
                  </el-button>
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
                :page-sizes="[10, 20, 50, 100]"
                :total="total"
                @current-change="handleCurrentChange"
                @size-change="handleSizeChange"
              />
            </div>
          </el-card>
        </div>
      </div>

      <!-- 新增/编辑弹窗 -->
      <el-dialog
        v-model="editDialog.visible"
        :title="editDialog.isEdit ? '编辑菜单' : '新增菜单'"
        width="820px"
        destroy-on-close
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="110px"
          class="dialog-form two-col"
        >
          <el-form-item label="父节点">
            <el-input :model-value="parentName" disabled />
          </el-form-item>
          <el-form-item label="类型" prop="menuType">
            <el-select v-model="form.menuType" placeholder="请选择">
              <el-option label="目录" value="CATALOG" />
              <el-option label="菜单" value="MENU" />
              <el-option label="按钮" value="BUTTON" />
            </el-select>
          </el-form-item>

          <el-form-item label="菜单名称" prop="menuName">
            <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortNo" :min="0" :max="9999" />
          </el-form-item>

          <el-form-item label="路由/路径" prop="routePath">
            <el-input v-model="form.routePath" placeholder="如：/system/user" />
          </el-form-item>
          <el-form-item label="组件" prop="component">
            <el-input v-model="form.component" placeholder="如：views/System/User/index.vue" />
          </el-form-item>

          <el-form-item label="菜单编码" prop="menuCode">
            <el-input v-model="form.menuCode" readonly class="readonly-input" />
          </el-form-item>
          <el-form-item label="图标">
            <el-input v-model="form.icon" placeholder="如：UserFilled" />
          </el-form-item>

          <el-form-item label="可见">
            <el-select v-model="form.visible" placeholder="请选择">
              <el-option label="显示" :value="1" />
              <el-option label="隐藏" :value="0" />
            </el-select>
          </el-form-item>
          <el-alert
            class="full-row"
            type="info"
            show-icon
            :closable="false"
            title="提示：按钮类型（BUTTON）通常只配置菜单名称 + 菜单编码；目录/菜单可按需配置路由、组件和图标。"
          />
        </el-form>

        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import { parsePageResult } from '@/api/common';
import {
  queryMenuTree,
  queryMenuList,
  createMenu,
  updateMenu,
  deleteMenu,
  setMenuStatus,
  type MenuItem,
  type MenuStatus,
  type MenuType,
  type VisibleStatus,
  type SaveMenuPayload,
} from '@/api/system/menu';

const treeProps = {
  children: 'children',
  label: 'menuName',
};

const treeRef = ref();
const treeFilter = ref('');
const treeLoading = ref(false);
const menuTree = ref<MenuItem[]>([]);
const currentNode = ref<MenuItem | null>(null);
const expandedKeys = ref<string[]>([]); // 记录需要展开的节点 ID

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  parentMenuId: undefined as string | undefined,
  menuCode: '',
  menuName: '',
  menuType: undefined as MenuType | undefined,
  status: undefined as MenuStatus | undefined,
});

const tableLoading = ref(false);
const tableData = ref<MenuItem[]>([]);
const total = ref(0);

const editDialog = reactive({
  visible: false,
  isEdit: false,
});
const formRef = ref<FormInstance>();
const saving = ref(false);
const form = reactive<SaveMenuPayload>({
  menuId: undefined,
  parentMenuId: undefined,
  menuCode: '',
  menuName: '',
  menuType: 'MENU',
  routePath: '',
  component: '',
  icon: '',
  sortNo: 0,
  visible: 1,
  remark: '',
});

const rules: FormRules = {
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  routePath: [
    {
      validator: (_rule, value, callback) => {
        if (form.menuType === 'BUTTON') return callback();
        if (!String(value || '').trim()) return callback(new Error('请输入路由/路径'));
        callback();
      },
      trigger: 'blur',
    },
  ],
  component: [
    {
      validator: (_rule, value, callback) => {
        if (form.menuType !== 'MENU') return callback();
        if (!String(value || '').trim()) return callback(new Error('请输入组件路径'));
        callback();
      },
      trigger: 'blur',
    },
  ],
};

// 权限点（当前项目约定风格）
const canCreate = computed(() => hasPermission('system:menu:create'));
const canUpdate = computed(() => hasPermission('system:menu:update'));
const canDelete = computed(() => hasPermission('system:menu:delete'));
const canStatus = computed(() => hasPermission('system:menu:status'));

const parentName = computed(() => {
  if (!form.parentMenuId) return '根节点';
  const node = findNode(menuTree.value, form.parentMenuId);
  return node ? `${node.menuName}（${typeText(node.menuType)}）` : form.parentMenuId;
});

onMounted(() => {
  loadTree();
});

function typeText(t: MenuType) {
  return t === 'CATALOG' || t === 'DIR' ? '目录' : t === 'MENU' ? '菜单' : '按钮';
}

function typeTagType(t: MenuType) {
  return t === 'CATALOG' || t === 'DIR' ? 'warning' : t === 'MENU' ? 'success' : 'info';
}

function isEnabledStatus(status?: MenuStatus) {
  return status === 'ENABLED' || status === 1;
}

function statusText(status?: MenuStatus) {
  return isEnabledStatus(status) ? '启用' : '停用';
}

function statusTagType(status?: MenuStatus) {
  return isEnabledStatus(status) ? 'success' : 'info';
}

function treeFilterMethod(value: string, data: MenuItem) {
  if (!value) return true;
  const v = value.toLowerCase();
  return (
    (data.menuName || '').toLowerCase().includes(v) ||
    String(data.menuCode || '').toLowerCase().includes(v) ||
    String(data.routePath || '').toLowerCase().includes(v)
  );
}

function filterTree() {
  treeRef.value?.filter(treeFilter.value);
}

async function loadTree() {
  treeLoading.value = true;
  try {
    const res = await queryMenuTree();
    const data = (Array.isArray(res) && res.length ? res : []) as MenuItem[];
    menuTree.value = data;
    // --- 新增：自动选中逻辑 ---
    if (data.length > 0) {
      const firstNode = data[0]!;
      const firstId = firstNode.menuId;

      // 1. 设置当前选中高亮状态 (nextTick 确保 DOM 已渲染)
      setTimeout(() => {
        treeRef.value?.setCurrentKey(firstId);
      }, 0);

      // 2. 展开该节点
      expandedKeys.value = [firstId];

      // 3. 模拟点击，触发右侧列表加载
      handleTreeClick(firstNode);
    } else {
      // 如果没数据，默认走原有逻辑
      currentNode.value = null;
      query.parentMenuId = undefined;
      fetchList();
    }

  } catch {
    menuTree.value = [];
  } finally {
    treeLoading.value = false;
  }
  // 默认选择根
  currentNode.value = null;
  query.parentMenuId = undefined;
  fetchList();
}

function handleTreeClick(node: MenuItem) {
  currentNode.value = node;
  query.parentMenuId = node.menuId;
  query.pageNum = 1;
  fetchList();
  // 如果你希望点击时也确保它在展开列表中
  if (!expandedKeys.value.includes(node.menuId)) {
    expandedKeys.value.push(node.menuId);
  }
}

function handleSearch() {
  query.menuCode = (query.menuCode || '').trim();
  query.menuName = (query.menuName || '').trim();
  query.pageNum = 1;
  fetchList();
}

function handleReset() {
  query.menuCode = '';
  query.menuName = '';
  query.menuType = undefined;
  query.status = undefined;
  query.pageNum = 1;
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

async function fetchList() {
  tableLoading.value = true;
  try {
    const res = await queryMenuList({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      menuCode: query.menuCode || undefined,
      menuName: query.menuName || undefined,
      menuType: query.menuType,
      status: query.status,
    });
    const { list, total: totalCount } = parsePageResult<MenuItem>(res);
    const filtered = query.parentMenuId
      ? list.filter((item) => item.parentMenuId === query.parentMenuId)
      : list;
    tableData.value = filtered;
    total.value = query.parentMenuId ? filtered.length : totalCount || filtered.length;
  } catch {
    // mock fallback：取当前父节点 children，并按条件过滤 + 简单分页
    const list = getChildrenByParent(menuTree.value, query.parentMenuId);
    const filtered = list
      .filter((x) => {
        if (query.menuType && x.menuType !== query.menuType) return false;
        if (query.status && x.status !== query.status) return false;
        const code = (query.menuCode || '').trim().toLowerCase();
        const name = (query.menuName || '').trim().toLowerCase();
        if (!code && !name) return true;
        return (
          (code && (x.menuCode || '').toLowerCase().includes(code)) ||
          (name && (x.menuName || '').toLowerCase().includes(name))
        );
      })
      .sort((a, b) => Number(a.sortNo ?? 0) - Number(b.sortNo ?? 0));

    total.value = filtered.length;
    const start = (query.pageNum - 1) * query.pageSize;
    tableData.value = filtered.slice(start, start + query.pageSize);
  } finally {
    tableLoading.value = false;
  }
}

function resetFormModel() {
  form.menuId = undefined;
  form.menuCode = '';
  form.menuName = '';
  form.menuType = 'MENU';
  form.routePath = '';
  form.component = '';
  form.icon = '';
  form.sortNo = 0;
  form.visible = 1;
  form.remark = '';
}

function openCreateRoot() {
  editDialog.isEdit = false;
  resetFormModel();
  form.parentMenuId = undefined;
  form.menuCode = generateMenuCode();
  editDialog.visible = true;
}

function openCreateChild() {
  editDialog.isEdit = false;
  resetFormModel();
  form.parentMenuId = currentNode.value?.menuId;
  form.menuCode = generateMenuCode();
  editDialog.visible = true;
}

function openEdit(row: MenuItem) {
  editDialog.isEdit = true;
  resetFormModel();
  form.menuId = row.menuId;
  form.parentMenuId = row.parentMenuId;
  form.menuCode = row.menuCode;
  form.menuName = row.menuName;
  form.menuType = row.menuType;
  form.routePath = row.routePath || '';
  form.component = row.component || '';
  form.icon = row.icon || '';
  form.sortNo = row.sortNo ?? 0;
  form.visible = (row.visible ?? 1) as VisibleStatus;
  form.remark = row.remark || '';
  editDialog.visible = true;
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
    const payload: SaveMenuPayload = { ...form };
    if (!editDialog.isEdit && !String(payload.menuCode || '').trim()) {
      payload.menuCode = generateMenuCode();
    }
    if (editDialog.isEdit) {
      await updateMenu(payload);
      ElMessage.success('编辑成功');
    } else {
      await createMenu(payload);
      ElMessage.success('新增成功');
    }
    editDialog.visible = false;
    await loadTree();
  } catch {
    // mock fallback：直接写入树结构并刷新列表
    upsertMock(menuTree.value, { ...form, menuId: form.menuId || genId() });
    ElMessage.success('已保存（示例模式）');
    editDialog.visible = false;
    fetchList();
  } finally {
    saving.value = false;
  }
}

function handleDelete(row: MenuItem) {
  if (!canDelete.value) return;
  ElMessageBox.confirm(`确认删除「${row.menuName}」吗？（包含其子节点）`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteMenu(row.menuId);
        ElMessage.success('删除成功');
        await loadTree();
      } catch {
        // mock fallback
        removeMock(menuTree.value, row.menuId);
        ElMessage.success('已删除（示例模式）');
        fetchList();
      }
    })
    .catch(() => {});
}

function toggleStatus(row: MenuItem) {
  if (!canStatus.value) return;
  const next: MenuStatus = isEnabledStatus(row.status) ? 'DISABLED' : 'ENABLED';
  setMenuStatus(row.menuId, next)
    .then(async () => {
      ElMessage.success('状态已更新');
      await loadTree();
    })
    .catch(() => {
      // mock fallback
      const node = findNode(menuTree.value, row.menuId);
      if (node) node.status = next;
      row.status = next;
    });
}

function findNode(list: MenuItem[], id: string): MenuItem | null {
  for (const item of list) {
    if (item.menuId === id) return item;
    if (item.children?.length) {
      const found = findNode(item.children, id);
      if (found) return found;
    }
  }
  return null;
}

function getChildrenByParent(list: MenuItem[], parentId?: string) {
  if (!parentId) return list;
  const node = findNode(list, parentId);
  return node?.children || [];
}

function upsertMock(list: MenuItem[], payload: SaveMenuPayload & { menuId: string }) {
  // edit
  if (payload.menuId && findNode(list, payload.menuId)) {
    const node = findNode(list, payload.menuId);
    if (!node) return;
    node.parentMenuId = payload.parentMenuId;
    node.menuCode = payload.menuCode;
    node.menuName = payload.menuName;
    node.menuType = payload.menuType;
    node.routePath = payload.routePath;
    node.component = payload.component;
    node.icon = payload.icon;
    node.sortNo = payload.sortNo;
    node.visible = payload.visible;
    node.remark = payload.remark;
    return;
  }

  // create
  const newNode: MenuItem = {
    menuId: payload.menuId,
    parentMenuId: payload.parentMenuId,
    menuCode: payload.menuCode,
    menuName: payload.menuName,
    menuType: payload.menuType,
    routePath: payload.routePath,
    component: payload.component,
    icon: payload.icon,
    sortNo: payload.sortNo,
    visible: payload.visible,
    status: 'ENABLED',
    remark: payload.remark,
    children: payload.menuType === 'BUTTON' ? undefined : [],
  };

  if (!payload.parentMenuId) {
    list.push(newNode);
    return;
  }
  const parent = findNode(list, payload.parentMenuId);
  if (!parent) {
    list.push(newNode);
    return;
  }
  parent.children = parent.children || [];
  parent.children.push(newNode);
}

function removeMock(list: MenuItem[], id: string) {
  const idx = list.findIndex((x) => x.menuId === id);
  if (idx >= 0) {
    list.splice(idx, 1);
    return true;
  }
  for (const item of list) {
    if (item.children?.length) {
      const ok = removeMock(item.children, id);
      if (ok) return true;
    }
  }
  return false;
}

function genId() {
  return `m_${Date.now()}_${Math.floor(Math.random() * 1000)}`;
}

// 生成菜单编码：MU + 年月日(YYMMDD) + 4位随机数
function generateMenuCode() {
  const now = new Date();
  const year = String(now.getFullYear()).slice(-2);
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const rand = String(Math.floor(Math.random() * 10000)).padStart(4, '0');
  return `MU${year}${month}${day}${rand}`;
}
</script>

<style scoped lang="scss">
.menu-page {
  .split-area {
    display: flex;
    gap: 5px;
    height: calc(100vh - 120px);
    align-items: stretch;
  }

  .left-pane {
    width: 150px;
    min-width: 260px;
    height: 100%;
  }

  .right-pane {
    flex: 1;
    height: 100%;
    min-width: 720px;
  }

  .tree-card {
    height: 100%;
    .tree-card-body {
      padding: 8px;
      height: 100%;
      display: flex;
      flex-direction: column;
    }
  }

  .tree-header {
    margin-bottom: 8px;
  }

  .menu-tree {
    flex: 1;
    overflow: auto;
  }

  .tree-node {
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }

  .node-name {
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .node-type {
    transform: translateY(-1px);
  }

  .list-card {
    height: 100%;
    display: flex;
    flex-direction: column;

  }

  .list-card :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: auto;
    padding-top: 8px;
  }


  .list-card :deep(.el-card__header) {
    border-bottom: none; /* 隐藏原生边框 */
    padding-bottom: 0;
  }

  .right-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    flex-wrap: nowrap;
  }

  .title {
    font-weight: 600;
    flex: 0 0 auto;
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: nowrap;
    white-space: nowrap;
  }

  .org-label {
    margin-top: 6px;
    color: #666;
    font-size: 12px;
  }

  .header-divider {
    margin-top: 6px;
    height: 1px;
    background: #ebeef5;
  }

  .search-bar {
    margin-top: 6px;   // 新增或调小
    //margin-bottom: 12px; // 恢复为正值更稳
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

  .readonly-input :deep(.el-input__inner) {
    background-color: #f5f7fa;
    color: #606266;
    cursor: not-allowed;
  }
}
</style>
