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
                placeholder="搜索菜单名称/权限标识"
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
              @node-click="handleTreeClick"
              v-loading="treeLoading"
            >
              <template #default="{ data }">
                <span class="tree-node">
                  <span class="node-name">{{ data.name }}</span>
                  <el-tag size="small" class="node-type" :type="typeTagType(data.type)">
                    {{ typeText(data.type) }}
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
              <div class="header">
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
              <div class="sub-title" v-if="currentNode">
                当前父节点：{{ currentNode.name }}（{{ typeText(currentNode.type) }}）
              </div>
              <div class="sub-title" v-else>当前父节点：根节点</div>
            </template>

            <!-- 查询区 -->
            <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
              <el-form-item label="关键词">
                <el-input
                  v-model="query.keyword"
                  placeholder="菜单名称/权限标识"
                  clearable
                  style="width: 220px"
                />
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="query.type" clearable placeholder="全部" style="width: 140px">
                  <el-option label="目录" value="DIR" />
                  <el-option label="菜单" value="MENU" />
                  <el-option label="按钮" value="BUTTON" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
                  <el-option label="启用" :value="1" />
                  <el-option label="停用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item label="可见">
                <el-select v-model="query.visible" clearable placeholder="全部" style="width: 140px">
                  <el-option label="显示" :value="1" />
                  <el-option label="隐藏" :value="0" />
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
              <el-table-column prop="name" label="名称" min-width="180" />
              <el-table-column label="类型" min-width="110">
                <template #default="{ row }">
                  <el-tag size="small" :type="typeTagType(row.type)">{{ typeText(row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="path" label="路由/路径" min-width="160" />
              <el-table-column prop="component" label="组件" min-width="200" />
              <el-table-column prop="perms" label="权限标识" min-width="200" />
              <el-table-column prop="icon" label="图标" min-width="140" />
              <el-table-column prop="sortNo" label="排序" min-width="90" />
              <el-table-column label="可见" min-width="90">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.visible === 1 ? 'success' : 'info'">
                    {{ row.visible === 1 ? '显示' : '隐藏' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" min-width="90">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
                    {{ row.status === 1 ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="updateTime" label="更新时间" min-width="170" />
              <el-table-column label="操作" fixed="right" width="240">
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
                    {{ row.status === 1 ? '停用' : '启用' }}
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
                :current-page="query.page"
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
          <el-form-item label="类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择">
              <el-option label="目录" value="DIR" />
              <el-option label="菜单" value="MENU" />
              <el-option label="按钮" value="BUTTON" />
            </el-select>
          </el-form-item>

          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入名称" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortNo" :min="0" :max="9999" />
          </el-form-item>

          <el-form-item label="路由/路径" prop="path">
            <el-input v-model="form.path" placeholder="如：/system/user 或 system:user" />
          </el-form-item>
          <el-form-item label="组件" prop="component">
            <el-input v-model="form.component" placeholder="如：views/System/User/index.vue" />
          </el-form-item>

          <el-form-item label="权限标识" prop="perms">
            <el-input v-model="form.perms" placeholder="如：system:user:view" />
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
          <el-form-item label="状态">
            <el-select v-model="form.status" placeholder="请选择">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>

          <el-alert
            class="full-row"
            type="info"
            show-icon
            :closable="false"
            title="提示：按钮类型（BUTTON）通常只配置名称 + 权限标识；目录/菜单可按需配置 path/component/icon。"
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
  label: 'name',
};

const treeRef = ref();
const treeFilter = ref('');
const treeLoading = ref(false);
const menuTree = ref<MenuItem[]>([]);
const currentNode = ref<MenuItem | null>(null);

const query = reactive({
  page: 1,
  pageSize: 10,
  parentId: undefined as string | undefined,
  keyword: '',
  type: undefined as MenuType | undefined,
  status: undefined as MenuStatus | undefined,
  visible: undefined as VisibleStatus | undefined,
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
  parentId: undefined,
  name: '',
  type: 'MENU',
  path: '',
  component: '',
  perms: '',
  icon: '',
  sortNo: 0,
  visible: 1,
  status: 1,
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  path: [
    {
      validator: (_rule, value, callback) => {
        if (form.type === 'BUTTON') return callback();
        if (!String(value || '').trim()) return callback(new Error('请输入路由/路径'));
        callback();
      },
      trigger: 'blur',
    },
  ],
  component: [
    {
      validator: (_rule, value, callback) => {
        if (form.type !== 'MENU') return callback();
        if (!String(value || '').trim()) return callback(new Error('请输入组件路径'));
        callback();
      },
      trigger: 'blur',
    },
  ],
  perms: [
    {
      validator: (_rule, value, callback) => {
        if (form.type !== 'BUTTON') return callback();
        if (!String(value || '').trim()) return callback(new Error('按钮类型需填写权限标识'));
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
  if (!form.parentId) return '根节点';
  const node = findNode(menuTree.value, form.parentId);
  return node ? `${node.name}（${typeText(node.type)}）` : form.parentId;
});

const mockTree: MenuItem[] = [
  {
    menuId: 'system',
    name: '系统管理',
    type: 'DIR',
    path: '/system',
    icon: 'Setting',
    sortNo: 10,
    visible: 1,
    status: 1,
    updateTime: '2025-01-02 10:00:00',
    children: [
      {
        menuId: 'system:user',
        parentId: 'system',
        name: '人员管理',
        type: 'MENU',
        path: '/system/user',
        component: 'views/System/User/index.vue',
        perms: 'system:user:view',
        icon: 'UserFilled',
        sortNo: 10,
        visible: 1,
        status: 1,
        updateTime: '2025-01-02 10:00:00',
        children: [
          {
            menuId: 'system:user:create',
            parentId: 'system:user',
            name: '新增',
            type: 'BUTTON',
            perms: 'system:user:manage',
            sortNo: 10,
            visible: 1,
            status: 1,
            updateTime: '2025-01-02 10:00:00',
          },
          {
            menuId: 'system:user:delete',
            parentId: 'system:user',
            name: '删除',
            type: 'BUTTON',
            perms: 'system:user:delete',
            sortNo: 20,
            visible: 1,
            status: 1,
            updateTime: '2025-01-02 10:00:00',
          },
        ],
      },
      {
        menuId: 'system:menu',
        parentId: 'system',
        name: '菜单管理',
        type: 'MENU',
        path: '/system/menu',
        component: 'views/System/Menu/index.vue',
        perms: 'system:menu:view',
        icon: 'Menu',
        sortNo: 20,
        visible: 1,
        status: 1,
        updateTime: '2025-01-02 10:00:00',
      },
    ],
  },
];

onMounted(() => {
  loadTree();
});

function typeText(t: MenuType) {
  return t === 'DIR' ? '目录' : t === 'MENU' ? '菜单' : '按钮';
}

function typeTagType(t: MenuType) {
  return t === 'DIR' ? 'warning' : t === 'MENU' ? 'success' : 'info';
}

function treeFilterMethod(value: string, data: MenuItem) {
  if (!value) return true;
  const v = value.toLowerCase();
  return (
    (data.name || '').toLowerCase().includes(v) ||
    String(data.perms || '').toLowerCase().includes(v) ||
    String(data.path || '').toLowerCase().includes(v)
  );
}

function filterTree() {
  treeRef.value?.filter(treeFilter.value);
}

async function loadTree() {
  treeLoading.value = true;
  try {
    const res = await queryMenuTree();
    menuTree.value = (Array.isArray(res) && res.length ? res : mockTree) as MenuItem[];
  } catch {
    menuTree.value = mockTree;
  } finally {
    treeLoading.value = false;
  }
  // 默认选择根
  currentNode.value = null;
  query.parentId = undefined;
  fetchList();
}

function handleTreeClick(node: MenuItem) {
  currentNode.value = node;
  query.parentId = node.menuId;
  query.page = 1;
  fetchList();
}

function handleSearch() {
  query.keyword = (query.keyword || '').trim();
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.keyword = '';
  query.type = undefined;
  query.status = undefined;
  query.visible = undefined;
  query.page = 1;
  fetchList();
}

function handleCurrentChange(page: number) {
  query.page = page;
  fetchList();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.page = 1;
  fetchList();
}

async function fetchList() {
  tableLoading.value = true;
  try {
    const res = await queryMenuList({ ...query });
    tableData.value = Array.isArray(res?.records) ? res.records : [];
    total.value = Number(res?.total ?? 0) || 0;
  } catch {
    // mock fallback：取当前父节点 children，并按条件过滤 + 简单分页
    const list = getChildrenByParent(menuTree.value, query.parentId);
    const filtered = list
      .filter((x) => {
        if (query.type && x.type !== query.type) return false;
        if (typeof query.status === 'number' && x.status !== query.status) return false;
        if (typeof query.visible === 'number' && x.visible !== query.visible) return false;
        const kw = (query.keyword || '').trim().toLowerCase();
        if (!kw) return true;
        return (
          (x.name || '').toLowerCase().includes(kw) ||
          String(x.perms || '').toLowerCase().includes(kw) ||
          String(x.path || '').toLowerCase().includes(kw)
        );
      })
      .sort((a, b) => Number(a.sortNo ?? 0) - Number(b.sortNo ?? 0));

    total.value = filtered.length;
    const start = (query.page - 1) * query.pageSize;
    tableData.value = filtered.slice(start, start + query.pageSize);
  } finally {
    tableLoading.value = false;
  }
}

function resetFormModel() {
  form.menuId = undefined;
  form.name = '';
  form.type = 'MENU';
  form.path = '';
  form.component = '';
  form.perms = '';
  form.icon = '';
  form.sortNo = 0;
  form.visible = 1;
  form.status = 1;
}

function openCreateRoot() {
  editDialog.isEdit = false;
  resetFormModel();
  form.parentId = undefined;
  editDialog.visible = true;
}

function openCreateChild() {
  editDialog.isEdit = false;
  resetFormModel();
  form.parentId = currentNode.value?.menuId;
  editDialog.visible = true;
}

function openEdit(row: MenuItem) {
  editDialog.isEdit = true;
  resetFormModel();
  form.menuId = row.menuId;
  form.parentId = row.parentId;
  form.name = row.name;
  form.type = row.type;
  form.path = row.path || '';
  form.component = row.component || '';
  form.perms = row.perms || '';
  form.icon = row.icon || '';
  form.sortNo = row.sortNo ?? 0;
  form.visible = (row.visible ?? 1) as VisibleStatus;
  form.status = (row.status ?? 1) as MenuStatus;
  editDialog.visible = true;
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
    const payload: SaveMenuPayload = { ...form };
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
  ElMessageBox.confirm(`确认删除「${row.name}」吗？（包含其子节点）`, '提示', { type: 'warning' })
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
  const next: MenuStatus = row.status === 1 ? 0 : 1;
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
    node.parentId = payload.parentId;
    node.name = payload.name;
    node.type = payload.type;
    node.path = payload.path;
    node.component = payload.component;
    node.perms = payload.perms;
    node.icon = payload.icon;
    node.sortNo = payload.sortNo;
    node.visible = payload.visible;
    node.status = payload.status;
    node.updateTime = new Date().toISOString();
    return;
  }

  // create
  const newNode: MenuItem = {
    menuId: payload.menuId,
    parentId: payload.parentId,
    name: payload.name,
    type: payload.type,
    path: payload.path,
    component: payload.component,
    perms: payload.perms,
    icon: payload.icon,
    sortNo: payload.sortNo,
    visible: payload.visible,
    status: payload.status,
    updateTime: new Date().toISOString(),
    children: payload.type === 'BUTTON' ? undefined : [],
  };

  if (!payload.parentId) {
    list.push(newNode);
    return;
  }
  const parent = findNode(list, payload.parentId);
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
</script>

<style scoped lang="scss">
.menu-page {
  .split-area {
    display: flex;
    gap: 12px;
    height: calc(100vh - 120px);
    align-items: stretch;
  }

  .left-pane {
    width: 320px;
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
  }

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .title {
    font-weight: 600;
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .sub-title {
    margin-top: 6px;
    color: #666;
    font-size: 12px;
  }

  .search-bar {
    margin-bottom: 12px;
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
}
</style>
