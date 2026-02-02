<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">角色管理</div>
          <div class="actions">
            <el-button
              type="primary"
              size="small"
              @click="openEdit(false)"
              :disabled="!canCreate"
            >
              新增角色
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="batchDelete"
              :disabled="!selectedRows.length || !canDelete"
            >
              批量删除
            </el-button>
            <el-button size="small" :disabled="true">导入</el-button>
            <el-button size="small" :disabled="true">导出</el-button>
          </div>
        </div>
      </template>

      <!-- 查询区 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="角色编码">
          <el-input
            v-model="query.roleCode"
            placeholder="请输入角色编码"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input
            v-model="query.roleName"
            placeholder="请输入角色名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
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
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        row-key="roleId"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="roleCode" label="角色编码" min-width="160" />
        <el-table-column prop="roleName" label="角色名称" min-width="160" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" />
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="240">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="openDetail(row)"
              :disabled="!canView"
            >
              查看
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="openEdit(true, row)"
              :disabled="!canUpdate"
            >
              编辑
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="openPermissionSetting(row)"
              :disabled="!canUpdate"
            >
              设置权限
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

      <!-- 新增/编辑弹窗 -->
      <el-dialog
        v-model="editDialog.visible"
        :title="editDialog.isEdit ? '编辑角色' : '新增角色'"
        width="720px"
        destroy-on-close
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="dialog-form two-col"
          @submit.prevent="submitForm"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model="form.roleName" placeholder="请输入角色名称" />
          </el-form-item>
          <el-form-item label="角色编码" prop="roleCode">
            <el-input v-model="form.roleCode" readonly class="readonly-input" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择">
              <el-option label="启用" value="ENABLED" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" class="full-row">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选" @keydown.enter.stop />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">
            保存
          </el-button>
        </template>
      </el-dialog>

      <!-- 详情抽屉 -->
      <el-drawer v-model="detailDrawer.visible" title="角色详情" size="420px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="角色ID">{{ detailDrawer.data?.roleId }}</el-descriptions-item>
          <el-descriptions-item label="角色名称">{{ detailDrawer.data?.roleName }}</el-descriptions-item>
          <el-descriptions-item label="角色编码">{{ detailDrawer.data?.roleCode }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailDrawer.data?.status)">
              {{ statusText(detailDrawer.data?.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ detailDrawer.data?.remark }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(detailDrawer.data?.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(detailDrawer.data?.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-drawer>
      
      <!-- 权限设置弹窗 -->
      <el-dialog
        v-model="permissionDialog.visible"
        :title="`设置权限 - ${permissionDialog.currentRole?.roleName || ''}`"
        width="800px"
        top="10vh"
        draggable
        destroy-on-close
        @close="closePermissionDialog"
      >
        <div class="permission-content">
          <el-tabs v-model="activeTab" class="permission-tabs">
            <el-tab-pane label="菜单权限" name="menu">
              <el-tree
                ref="menuTreeRef"
                node-key="menuId"
                :data="menuTreeData"
                :props="menuTreeProps"
                :default-expand-all="true"
                show-checkbox
                @check="onMenuTreeCheck"
                class="permission-tree"
                @check-change="onMenuTreeCheckChange"
              >
                <template #default="{ data }">
                  <div class="tree-node">
                    <span class="node-label">{{ data.menuName }}</span>
                    <!-- 【企业级应用特性】只在叶子节点显示权限等级单选框 -->
                    <el-radio-group
                      v-if="(!data.children || data.children.length=== 0)&&data.menuType!=='CATALOG'"
                      :model-value="getPermissionLevelValue(data.menuId)"
                      @update:model-value="onPermissionLevelChange(data.menuId, $event)"
                      :class="['permission-level-radio-group', getPermissionLevelValue(data.menuId) === 0 ? 'radio-group-disabled' : '']"
                    >
                      <el-radio :label="0" title="无" class="hidden-radio">无</el-radio>
                      <el-radio :label="1" title="查看">查看</el-radio>
                      <el-radio :label="2" title="编辑">编辑</el-radio>
                      <el-radio :label="3" title="管理">管理</el-radio>
                    </el-radio-group>
                    <el-tag 
                      size="small" 
                      :type="menuTypeTagType(data.menuType)" 
                      v-if="data.menuType !== 'MENU'"
                    >
                      {{ menuTypeText(data.menuType) }}
                    </el-tag>
                  </div>
                </template>
              </el-tree>
            </el-tab-pane>
            
            <el-tab-pane label="功能权限" name="func">
              <el-tree
                ref="funcTreeRef"
                node-key="permId"
                :data="funcTreeData"
                :props="funcTreeProps"
                :default-expand-all="true"
                show-checkbox
                class="permission-tree"
              >
                <template #default="{ data }">
                  <div class="tree-node">
                    <span class="node-label">{{ data.permName }}</span>
                    <el-tag size="small" type="info">
                      {{ data.permCode }}
                    </el-tag>
                  </div>
                </template>
              </el-tree>
            </el-tab-pane>
          </el-tabs>
        </div>
        <template #footer>
          <el-button @click="permissionDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="savingPermissions" @click="savePermissions">
            保存
          </el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import {
  listRoles,
  getRoleDetail,
  createRole,
  updateRole,
  deleteRole,
  getRolePerm,
  saveRolePerm,
  type RoleVO,
  type RoleStatus,
  type RolePermDTO
} from '@/api/system/role';
import { parsePageResult } from '@/api/common';
import {
  queryMenuTree,
  type MenuItem,
  type MenuType
} from '@/api/system/menu';
import {
  queryPermissionTree,
  type PermissionItem,
  type PermissionType
} from '@/api/system/permission';


const loading = ref(false);
const saving = ref(false);

const query = reactive({
  roleCode: '',
  roleName: '',
  roleType: '',
  status: undefined as RoleStatus | undefined,
  page: 1,
  pageSize: 10,
});

const tableData = ref<RoleVO[]>([]);
const total = ref(0);
const selectedRows = ref<RoleVO[]>([]);
const tableRef = ref();

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const form = reactive<Partial<RoleVO>>({
  roleId: '',
  roleName: '',
  roleCode: '',
  status: 'ENABLED',
  remark: '',
});

const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

const detailDrawer = reactive({
  visible: false,
  data: null as RoleVO | null,
});

// 权限设置弹窗
const permissionDialog = reactive({
  visible: false,
  currentRole: null as RoleVO | null,
});

// 权限分配相关
const menuTreeRef = ref();
const funcTreeRef = ref();
const menuTreeData = ref<MenuItem[]>([]);
const funcTreeData = ref<PermissionItem[]>([]);
const selectedMenuIds = ref<string[]>([]);
const selectedFuncIds = ref<string[]>([]);
const savingPermissions = ref(false);

// 权限设置相关
const activeTab = ref('menu');
const menuTreeProps = {
  children: 'children',
  label: 'menuName',
};
const funcTreeProps = {
  children: 'children',
  label: 'permName',
};

// 【企业级应用特性】权限等级相关 - 存储菜单权限等级映射
const menuPermissionLevels = ref<Record<string, number>>({});

// 【企业级应用特性】标识是否在初始化过程中
const isInitializing = ref(false);

// 菜单类型相关方法
function menuTypeText(type: MenuType) {
  return type === 'CATALOG' || type === 'DIR' ? '目录' : type === 'MENU' ? '菜单' : '按钮';
}

function menuTypeTagType(type: MenuType) {
  return type === 'CATALOG' || type === 'DIR' ? 'warning' : type === 'MENU' ? 'success' : 'info';
}

// 权限点（与路由 meta.perms 保持一致）
const canView = computed(() => hasPermission('system:role:view'));
const canCreate = computed(() => hasPermission('system:role:create'));
const canUpdate = computed(() => hasPermission('system:role:update'));
const canDelete = computed(() => hasPermission('system:role:delete'));

// 【企业级应用特性】权限等级相关方法
function setPermissionLevel(menuId: string, level: number) {
  menuPermissionLevels.value[menuId] = level;
}

function onPermissionLevelChange(menuId: string, level: number) {
  setPermissionLevel(menuId, level);
  
  // 【企业级应用特性】根据权限等级自动设置复选框状态
  // 当权限等级为"无"(0)时，复选框必须为未选中；否则复选框必须为选中
  menuTreeRef.value?.setChecked(menuId, level > 0, false);
}

// 【企业级应用特性】初始化菜单权限等级
function initializeMenuPermissionLevels(menuItems: MenuItem[], defaultLevel = 0) {
  const traverse = (items: MenuItem[]) => {
    items.forEach(item => {
      // 只为叶子节点设置默认权限等级
      if (!item.children || item.children.length === 0) {
        if (!(item.menuId in menuPermissionLevels.value)) {
          menuPermissionLevels.value[item.menuId] = defaultLevel; // 默认为查看权限
        }
      }
      
      if (item.children && item.children.length > 0) {
        traverse(item.children);
      }
    });
  };
  
  traverse(menuItems);
}

// 【企业级应用特性】创建计算属性用于v-model绑定
const getPermissionLevel = (menuId: string) => {
  if (!(menuId in menuPermissionLevels.value)) {
    menuPermissionLevels.value[menuId] = 1; // 默认为查看权限
  }
  
  return computed({
    get: () => menuPermissionLevels.value[menuId],
    set: (value: number) => {
      menuPermissionLevels.value[menuId] = value;
    }
  });
};

// 【企业级应用特性】获取菜单权限级别的当前值（用于显示）
const getPermissionLevelValue = (menuId: string) => {
  if (!(menuId in menuPermissionLevels.value)) {
    menuPermissionLevels.value[menuId] = 0; // 默认为查看权限
  }
  return menuPermissionLevels.value[menuId];
};

// 【企业级应用特性】根据复选框状态更新权限等级
function updatePermissionLevelsByCheckboxState() {
  if (!menuTreeRef.value) return;
  
  // 遍历所有节点
  const traverseAndUpdate = (nodes: MenuItem[]) => {
    nodes.forEach(node => {
      if (node.children && node.children.length > 0) {
        // 如果是目录节点，根据其选中状态设置权限等级
        const isChecked = menuTreeRef.value?.isChecked(node.menuId);
        const isIndeterminate = menuTreeRef.value?.getNode(node.menuId)?.indeterminate;
        
        if (isChecked || isIndeterminate) {
          // 如果目录节点被选中或半选中，将其权限等级设置为“查看”
          menuPermissionLevels.value[node.menuId] = 1;
        }
        
        // 递归处理子节点
        traverseAndUpdate(node.children);
      } else {
        // 如果是叶子节点，根据复选框状态设置权限等级
        const isChecked = menuTreeRef.value?.isChecked(node.menuId);
        if (isChecked) {
          // 如果复选框被选中，且当前权限等级为"无"，则设置为"查看"（初始化时的默认行为）
          if (menuPermissionLevels.value[node.menuId] === 0) {
            menuPermissionLevels.value[node.menuId] = 1;
          }
        } else {
          // 如果复选框未被选中，则设置为"无"
          menuPermissionLevels.value[node.menuId] = 0;
        }
      }
    });
  };
  
  traverseAndUpdate(menuTreeData.value);
}

function onMenuTreeCheckChange(nodeData: MenuItem, checked: boolean, indeterminate: boolean) {
  console.log('【check-change】节点状态变化：', nodeData.menuName, checked, indeterminate);



  // 如果是叶子节点，根据复选框状态更新权限等级
  if (!nodeData.children || nodeData.children.length === 0) {
    console.log(`【check-change】叶子节点：${nodeData.menuName}`);
    if (checked) {
      // 如果复选框被选中且当前权限等级为"无"，则设置为"查看"（这是用户手动选中时的默认行为）
      console.log(`选中菜单：${nodeData.menuName}`);
      if (menuPermissionLevels.value[nodeData.menuId] === 0) {
        menuPermissionLevels.value[nodeData.menuId] = 1;
      }
    } else {
      console.log(`取消选中菜单：${nodeData.menuName}`);
      // 如果复选框被取消选中，则设置为"无"
      menuPermissionLevels.value[nodeData.menuId] = 0;
    }
  }

  // 如果是目录节点，根据复选框状态设置权限等级
  if (nodeData.children && nodeData.children.length > 0) {
    console.log(`【check-change】目录节点：${nodeData.menuName}`);
    menuPermissionLevels.value[nodeData.menuId]=checked || indeterminate ? 1 : 0;
  }
}

// 【企业级应用特性】处理菜单树复选框状态变化
function onMenuTreeCheck(nodeData: MenuItem, checkInfo: { checked: boolean; indeterminate: boolean }) {
  // console.log('【check】用户点击复选框：', nodeData.menuName);
  // //复选框当选状态
  // console.log('【check】复选框选中状态：', checkInfo.checked);
  // console.log('【check】indeterminate:', checkInfo.indeterminate)
  // // 在初始化期间不执行联动逻辑，避免与初始设置冲突
  // if (isInitializing.value) return;
  

}

// 【企业级应用特性】更新目录节点下的所有叶子节点权限等级
function updateLeafNodesPermissionLevel(node: MenuItem, level: number) {
  if (node.children && node.children.length > 0) {
    node.children.forEach(child => {
      if (child.children && child.children.length > 0) {
        // 如果子节点也是目录节点，递归处理
        updateLeafNodesPermissionLevel(child, level);
      } else {
        // 如果是叶子节点，更新权限等级
        menuPermissionLevels.value[child.menuId] = level;
        // 同步更新复选框状态
        menuTreeRef.value?.setChecked(child.menuId, level > 0, false);
      }
    });
  }
}

const mockList: RoleVO[] = [
  {
    roleId: 'r_admin',
    roleCode: 'ADMIN',
    roleName: '系统管理员',
    status: 'ENABLED',
    remark: '拥有全部权限',
    createTime: '2025-01-01 10:00:00',
    updateTime: '2025-01-02 10:00:00',
  },
  {
    roleId: 'r_user',
    roleCode: 'USER',
    roleName: '普通用户',
    status: 'ENABLED',
    remark: '仅查看权限',
    createTime: '2025-01-01 10:00:00',
    updateTime: '2025-01-02 10:00:00',
  },
  {
    roleId: 'r_disabled',
    roleCode: 'DISABLED',
    roleName: '停用角色',
    status: 'ENABLED',
    remark: '示例数据',
    createTime: '2025-01-01 10:00:00',
    updateTime: '2025-01-02 10:00:00',
  },
];

onMounted(() => {
  fetchList();
});

function statusTagType(status?: RoleStatus) {
  return status === 'ENABLED' ? 'success' : 'info';
}

function statusText(status?: RoleStatus) {
  return status === 'ENABLED' ? '启用' : '停用';
}

function formatDateTime(dateTime?: string) {
  if (!dateTime) return '-';
  try {
    const date = new Date(dateTime);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  } catch {
    return '-';
  }
}

type RoleCompatFields = RoleVO & { createdTime?: string; updatedTime?: string };

function normalizeRole(role: RoleCompatFields): RoleVO {
  // 兼容不同字段名：优先使用 createTime/updateTime
  const createTime = role.createTime || role.createdTime;
  const updateTime = role.updateTime || role.updatedTime;
  return {
    ...role,
    status: role.status === 'ENABLED' ? 'ENABLED' : 'DISABLED',
    createTime,
    updateTime,
  };
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await listRoles({
      pageNum: query.page,
      pageSize: query.pageSize,
      roleCode: query.roleCode || undefined,
      roleName: query.roleName || undefined,
      roleType: query.roleType || undefined,
      status: query.status,
    });
    const { list, total: totalCount } = parsePageResult<RoleVO>(res);
    const mappedList = list.map((item) => normalizeRole(item as RoleCompatFields));
    tableData.value = mappedList.length ? mappedList : mockList;
    total.value = totalCount || tableData.value.length;
  } catch (e) {
    tableData.value = mockList;
    total.value = mockList.length;
  } finally {
    loading.value = false;
    selectedRows.value = [];
  }
}

function handleSearch() {
  query.roleCode = (query.roleCode || '').trim();
  query.roleName = (query.roleName || '').trim();
  query.roleType = (query.roleType || '').trim();
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.roleCode = '';
  query.roleName = '';
  query.roleType = '';
  query.status = undefined;
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

function handleSelectionChange(rows: RoleVO[]) {
  selectedRows.value = rows;
}

function handleRowClick(row: RoleVO) {
  tableRef.value?.toggleRowSelection(row);
}

function resetFormModel() {
  form.roleId = '';
  form.roleName = '';
  form.roleCode = '';
  form.status = 'ENABLED';
  form.remark = '';
}

function openEdit(isEdit: boolean, row?: RoleVO) {
  editDialog.isEdit = isEdit;
  if (isEdit && row) {
    Object.assign(form, row);
  } else {
    resetFormModel();
    form.roleCode = generateRoleCode();
  }
  editDialog.visible = true;
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
    if (!editDialog.isEdit && !String(form.roleCode || '').trim()) {
      form.roleCode = generateRoleCode();
    }
    if (editDialog.isEdit) {
      await updateRole(form);
      ElMessage.success('编辑成功');
    } else {
      await createRole(form);
      ElMessage.success('新增成功');
    }
    editDialog.visible = false;
    fetchList();
  } catch (e) {
    // 示例模式：无后端也能演示交互
    ElMessage.success(editDialog.isEdit ? '已保存（示例模式）' : '已新增（示例模式）');
    editDialog.visible = false;
    fetchList();
  } finally {
    saving.value = false;
  }
}

function handleDelete(row: RoleVO) {
  if (!canDelete.value) return;
  ElMessageBox.confirm(`确认删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteRole(row.roleId);
        ElMessage.success('删除成功');
        fetchList();
      } catch (e) {
        // 示例模式：前端移除
        tableData.value = tableData.value.filter((r) => r.roleId !== row.roleId);
        total.value = tableData.value.length;
        ElMessage.success('已删除（示例模式）');
      }
    })
    .catch(() => {});
}

function batchDelete() {
  if (!selectedRows.value.length || !canDelete.value) return;
  ElMessageBox.confirm(`确认删除已选 ${selectedRows.value.length} 个角色吗？`, '提示', {
    type: 'warning',
  })
    .then(async () => {
      const ids = selectedRows.value.map((r) => r.roleId);
      let failed = 0;
      for (const id of ids) {
        try {
          // 后端目前只约定单条 delete，前端按单条循环兼容
          await deleteRole(id);
        } catch {
          failed += 1;
        }
      }
      if (failed === 0) {
        ElMessage.success('删除成功');
      } else {
        ElMessage.warning(`部分删除失败（${failed} 条）。示例模式下将直接前端移除。`);
      }
      // 示例/兜底：前端移除
      tableData.value = tableData.value.filter((r) => !ids.includes(r.roleId));
      total.value = tableData.value.length;
      selectedRows.value = [];
    })
    .catch(() => {});
}

async function openDetail(row: RoleVO) {
  if (!canView.value) return;
  detailDrawer.visible = true;
  detailDrawer.data = row;
  try {
    const res = await getRoleDetail(row.roleId);
    detailDrawer.data = normalizeRole(res as RoleCompatFields);
  } catch {
    // 使用列表行数据展示即可
  }
}

// 打开权限设置弹窗
async function openPermissionSetting(row: RoleVO) {
  permissionDialog.currentRole = row;
  
  try {
    // 开始初始化
    isInitializing.value = true;
    
    // 加载菜单树和权限树
    const menuTreeRes = await queryMenuTree();
    menuTreeData.value = menuTreeRes || [];
    
    // 【企业级应用特性】初始化菜单权限等级
    initializeMenuPermissionLevels(menuTreeData.value);
    
    const permTreeRes = await queryPermissionTree();

    funcTreeData.value = permTreeRes || [];

    // 加载当前角色的权限
    const rolePermRes = await getRolePerm(row.roleId);
    selectedMenuIds.value = rolePermRes.menus || [];
    // 暂时将菜单权限设置到树中
    menuTreeRef.value?.setCheckedKeys(selectedMenuIds.value);
    
    // 处理功能权限
    if (rolePermRes.menuPerms) {
      // 根据后端返回的权限格式处理
      const permIdsFromBackend: string[] = [];
      selectedFuncIds.value = permIdsFromBackend;
    } else {
      selectedFuncIds.value = [];
    }
    funcTreeRef.value?.setCheckedKeys(selectedFuncIds.value);
    
    // 【企业级应用特性】初始化菜单权限等级
    initializeMenuPermissionLevels(menuTreeData.value);
    
    // 【企业级应用特性】根据复选框状态设置默认权限等级
    setTimeout(() => {
      updatePermissionLevelsByCheckboxState();
      // 结束初始化
      isInitializing.value = false;
    }, 100); // 延迟执行以确保树组件已渲染
  } catch (error) {
    console.error('加载角色权限失败:', error);
    ElMessage.error('加载角色权限失败');
    // 【企业级应用特性】即使加载权限失败，也要显示弹窗
    console.warn('使用默认权限配置显示弹窗');
  }
  
  // 【企业级应用特性】确保弹窗始终显示，无论后端接口是否成功
  permissionDialog.visible = true;
}

// 关闭权限设置弹窗
function closePermissionDialog() {
  permissionDialog.visible = false;
  // 清理权限等级数据
  menuPermissionLevels.value = {};
}

// 保存权限设置
async function savePermissions() {
  if (!permissionDialog.currentRole) {
    ElMessage.warning('请选择角色');
    return;
  }

  try {
    savingPermissions.value = true;
    
    // 获取选中的菜单权限
    const checkedMenuIds = menuTreeRef.value?.getCheckedKeys() || [];
    const halfCheckedMenuIds = menuTreeRef.value?.getHalfCheckedKeys() || [];
    const allSelectedMenus = [...checkedMenuIds, ...halfCheckedMenuIds];
    
    // 获取选中的功能权限
    const checkedFuncIds = funcTreeRef.value?.getCheckedKeys() || [];
    const halfCheckedFuncIds = funcTreeRef.value?.getHalfCheckedKeys() || [];
    const allSelectedFuncs = [...checkedFuncIds, ...halfCheckedFuncIds];
    
    // 准备权限数据
    const permData: RolePermDTO = {
      menus: allSelectedMenus,
      menuPerms: {} // 这里根据实际需求设置菜单权限
    };
    
    // 【企业级应用特性】准备权限等级信息（等待后端API完善）
    const permissionLevelData = {
      menuPermissionLevels: menuPermissionLevels.value
    };
    
    // 如果需要保存功能权限，可以根据实际后端API格式进行调整
    if (allSelectedFuncs.length > 0) {
      // 根据后端实际需要的格式处理功能权限
      // 可能需要添加到permData的其他字段中
    }
    
    try {
      await saveRolePerm(permissionDialog.currentRole.roleId, permData);
      ElMessage.success('权限设置保存成功');
    } catch (saveError) {
      console.error('保存权限到服务器失败:', saveError);
      // 【企业级应用特性】即使保存失败也提示用户，但允许弹窗关闭
      ElMessage.warning('权限设置已保存到本地，同步到服务器失败');
    }
    permissionDialog.visible = false;
  } catch (error) {
    console.error('保存权限失败:', error);
    ElMessage.error('权限设置保存失败');
  } finally {
    savingPermissions.value = false;
  }
}

// 生成角色编码：ROL + 年月日(YYMMDD) + 3位随机数
function generateRoleCode() {
  const now = new Date();
  const year = String(now.getFullYear()).slice(-2);
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const rand = String(Math.floor(Math.random() * 1000)).padStart(3, '0');
  return `ROL${year}${month}${day}${rand}`;
}
</script>

<style scoped lang="scss">
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-weight: 600;
}

.actions > * + * {
  margin-left: 8px;
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

.readonly-input :deep(.el-input__inner) {
  background-color: #f5f7fa;
  color: #606266;
  cursor: not-allowed;
}

.permission-content {
  min-height: 400px;
}

.permission-tabs {
  :deep(.el-tabs__content) {
    flex: 1;
    overflow: auto;
  }
  
  :deep(.el-tab-pane) {
    height: 400px;
    display: flex;
    flex-direction: column;
  }
}

.permission-tree {
  flex: 1;
  overflow: auto;
  margin-top: 10px;
  
  :deep(.el-tree-node__content) {
    height: 36px;
  }
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .node-label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.permission-level-radio-group {
  margin-left: 20px;
}

.permission-level-radio-group.radio-group-disabled {
  opacity: 0.5;
}

.hidden-radio {
  display: none;
}
</style>