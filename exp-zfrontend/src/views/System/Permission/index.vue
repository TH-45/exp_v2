<template>
  <el-config-provider :locale="zhCn">
    <div class="permission-page">
      <div class="split-area">
        <!-- 左侧角色选择 -->
        <div class="left-pane">
          <el-card class="role-card" body-class="role-card-body">
            <div class="card-header">
              <div class="title">角色列表</div>
              <el-button size="small" @click="refreshRoleList" :loading="roleListLoading">
                刷新
              </el-button>
            </div>
            <el-input
              v-model="roleFilter"
              size="small"
              placeholder="搜索角色名称/编码"
              clearable
              class="search-input"
              @input="filterRoles"
            />
            <el-scrollbar class="role-list-container" v-loading="roleListLoading">
              <el-tree
                ref="roleTreeRef"
                node-key="roleId"
                :data="filteredRoleList"
                :props="roleTreeProps"
                :expand-on-click-node="false"
                :highlight-current="true"
                @node-click="handleRoleSelect"
                class="role-tree"
              >
                <template #default="{ data }">
                  <div class="role-node">
                    <span class="role-name">{{ data.roleName }}</span>
                    <el-tag size="small" :type="roleStatusType(data.status)">
                      {{ roleStatusText(data.status) }}
                    </el-tag>
                  </div>
                </template>
              </el-tree>
            </el-scrollbar>
          </el-card>
        </div>

        <!-- 右侧权限分配 -->
        <div class="right-pane">
          <el-card class="permission-card" body-class="permission-card-body">
            <template #header>
              <div class="header">
                <div class="title">权限分配</div>
                <div class="actions">
                  <el-button 
                    type="primary" 
                    size="small" 
                    :disabled="!selectedRoleId || !canAssign" 
                    :loading="savingPermissions"
                    @click="savePermissions"
                  >
                    保存权限
                  </el-button>
                  <el-button 
                    size="small" 
                    :disabled="!selectedRoleId || !canAssign"
                    @click="resetPermissions"
                  >
                    重置
                  </el-button>
                </div>
              </div>
              <div class="current-role" v-if="selectedRoleName">
                当前角色：{{ selectedRoleName }}
              </div>
              <div class="current-role" v-else>
                请选择左侧角色
              </div>
            </template>

            <div class="permission-content" v-loading="permissionTreeLoading">
              <el-tabs v-model="activeTab" class="permission-tabs">
                <el-tab-pane label="菜单权限" name="menu">
                  <div class="permission-section">
                    <el-checkbox
                      v-model="checkAllMenus"
                      :indeterminate="isIndeterminate.menus"
                      @change="handleCheckAllMenusChange"
                      class="check-all"
                    >
                      全选/取消全选
                    </el-checkbox>
                    <el-tree
                      ref="menuTreeRef"
                      node-key="menuId"
                      :data="menuTreeData"
                      :props="menuTreeProps"
                      :default-expand-all="true"
                      show-checkbox
                      class="permission-tree"
                      @check="onMenuTreeCheck"
                    >
                      <template #default="{ data }">
                        <div class="tree-node">
                          <span class="node-label">{{ data.menuName }}</span>
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
                  </div>
                </el-tab-pane>
                
                <el-tab-pane label="功能权限" name="func">
                  <div class="permission-section">
                    <el-checkbox
                      v-model="checkAllFuncs"
                      :indeterminate="isIndeterminate.funcs"
                      @change="handleCheckAllFuncsChange"
                      class="check-all"
                    >
                      全选/取消全选
                    </el-checkbox>
                    <el-tree
                      ref="funcTreeRef"
                      node-key="permId"
                      :data="funcTreeData"
                      :props="funcTreeProps"
                      :default-expand-all="true"
                      show-checkbox
                      class="permission-tree"
                      @check="onFuncTreeCheck"
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
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import {
  listRoles,
  getRolePerm,
  saveRolePerm,
  type RoleVO,
  type RoleStatus,
  type RolePermDTO
} from '@/api/system/role';
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

// 角色选择相关
const roleTreeRef = ref();
const roleFilter = ref('');
const roleListLoading = ref(false);
const roleList = ref<RoleVO[]>([]);
const filteredRoleList = ref<RoleVO[]>([]);
const selectedRoleId = ref<string | null>(null);
const selectedRoleName = ref<string | null>(null);

// 权限分配相关
const menuTreeRef = ref();
const funcTreeRef = ref();
const permissionTreeLoading = ref(false);
const savingPermissions = ref(false);
const activeTab = ref('menu');

// 菜单权限相关
const menuTreeData = ref<MenuItem[]>([]);
const selectedMenuIds = ref<string[]>([]);
const checkAllMenus = ref(false);
const isIndeterminate = reactive({
  menus: false,
  funcs: false
});

// 功能权限相关
const funcTreeData = ref<PermissionItem[]>([]);
const selectedFuncIds = ref<string[]>([]);
const checkAllFuncs = ref(false);

// 权限点控制 - 控制用户是否有权限分配的权限
const canAssign = computed(() => hasPermission('system:permission:assign'));

// 【企业级应用特性】添加对菜单权限和功能权限的分别控制
// 可以根据需要添加更细粒度的权限控制
const canViewMenuPerm = computed(() => hasPermission('system:permission:menu:view'));
const canViewFuncPerm = computed(() => hasPermission('system:permission:func:view'));

// 角色树配置
const roleTreeProps = {
  children: 'children',
  label: 'roleName',
};

// 菜单树配置
const menuTreeProps = {
  children: 'children',
  label: 'menuName',
};

// 功能权限树配置
const funcTreeProps = {
  children: 'children',
  label: 'permName',
};

// 初始化数据
onMounted(() => {
  loadRoleList();
  loadMenuTree();
  loadFuncTree();
});

// 角色相关方法
function roleStatusText(status?: RoleStatus) {
  return status === 'ENABLED' ? '启用' : '停用';
}

function roleStatusType(status?: RoleStatus) {
  return status === 'ENABLED' ? 'success' : 'info';
}

async function loadRoleList() {
  roleListLoading.value = true;
  try {
    const res = await listRoles({
      pageNum: 1,
      pageSize: 9999, // 获取全部角色
      status: undefined
    });
    roleList.value = res.list || [];
    filteredRoleList.value = [...roleList.value];
  } catch (error) {
    console.error('加载角色列表失败:', error);
    ElMessage.error('加载角色列表失败');
  } finally {
    roleListLoading.value = false;
  }
}

function filterRoles() {
  if (!roleFilter.value.trim()) {
    filteredRoleList.value = [...roleList.value];
  } else {
    const filterValue = roleFilter.value.toLowerCase();
    filteredRoleList.value = roleList.value.filter(role =>
      role.roleName.toLowerCase().includes(filterValue) ||
      role.roleCode.toLowerCase().includes(filterValue)
    );
  }
}

function handleRoleSelect(role: RoleVO) {
  selectedRoleId.value = role.roleId;
  selectedRoleName.value = role.roleName;
  loadRolePermissions(role.roleId);
}

// 权限相关方法
async function loadRolePermissions(roleId: string) {
  if (!roleId) return;
  
  permissionTreeLoading.value = true;
  try {
    const res = await getRolePerm(roleId);
    
    // 解析菜单权限
    selectedMenuIds.value = res.menus || [];
    menuTreeRef.value?.setCheckedKeys(selectedMenuIds.value);
    
    // 解析功能权限
    // 根据后端返回的权限格式处理，这里假设有特定格式
    // 如果后端有专门的功能权限数据结构，需要根据实际情况调整
    if (res.menuPerms) {
      // 如果后端返回了特定的菜单权限结构，需要根据实际情况解析
      // 暂时将权限ID列表设置到功能权限树
      const permIdsFromBackend: string[] = [];
      // 这里需要根据后端实际返回格式进行处理
      selectedFuncIds.value = permIdsFromBackend;
    } else {
      // 如果没有特定的功能权限数据，可以尝试从其他途径获取
      selectedFuncIds.value = [];
    }
    funcTreeRef.value?.setCheckedKeys(selectedFuncIds.value);
    
    updateMenuCheckStatus();
    updateFuncCheckStatus();
  } catch (error) {
    console.error('加载角色权限失败:', error);
    ElMessage.error('加载角色权限失败');
  } finally {
    permissionTreeLoading.value = false;
  }
}

async function savePermissions() {
  if (!selectedRoleId.value) {
    ElMessage.warning('请先选择一个角色');
    return;
  }

  // 获取选中的菜单权限
  const checkedMenuIds = menuTreeRef.value?.getCheckedKeys() || [];
  const halfCheckedMenuIds = menuTreeRef.value?.getHalfCheckedKeys() || [];
  const allSelectedMenus = [...checkedMenuIds, ...halfCheckedMenuIds];

  // 获取选中的功能权限
  const checkedFuncIds = funcTreeRef.value?.getCheckedKeys() || [];
  const halfCheckedFuncIds = funcTreeRef.value?.getHalfCheckedKeys() || [];
  const allSelectedFuncs = [...checkedFuncIds, ...halfCheckedFuncIds];

  // 【企业级应用特性】在实际部署中，可能需要添加额外的数据验证
  // 确保权限分配符合业务规则，例如不能给普通用户分配管理员权限等

  try {
    savingPermissions.value = true;
    
    // 准备权限数据
    const permData: RolePermDTO = {
      menus: allSelectedMenus,
      menuPerms: {} // 这里根据实际需求设置菜单权限
    };

    // 如果需要保存功能权限，可以根据实际后端API格式进行调整
    // 例如，将选中的功能权限ID添加到permData中
    const checkedFuncIds = funcTreeRef.value?.getCheckedKeys() || [];
    if (checkedFuncIds.length > 0) {
      // 根据后端实际需要的格式处理功能权限
      // 可能需要添加到permData的其他字段中
    }

    await saveRolePerm(selectedRoleId.value, permData);
    ElMessage.success('权限保存成功');
  } catch (error) {
    console.error('保存权限失败:', error);
    ElMessage.error('保存权限保存失败');
  } finally {
    savingPermissions.value = false;
  }
}

function resetPermissions() {
  if (!selectedRoleId.value) {
    ElMessage.warning('请先选择一个角色');
    return;
  }
  
  ElMessageBox.confirm('确定要重置权限设置吗？', '提示', {
    type: 'warning'
  }).then(() => {
    if (selectedRoleId.value) {
      loadRolePermissions(selectedRoleId.value);
    }
  });
}

// 菜单权限相关方法
async function loadMenuTree() {
  try {
    const res = await queryMenuTree();
    menuTreeData.value = res || [];
  } catch (error) {
    console.error('加载菜单树失败:', error);
    ElMessage.error('加载菜单树失败');
  }
}

function menuTypeText(type: MenuType) {
  return type === 'CATALOG' || type === 'DIR' ? '目录' : type === 'MENU' ? '菜单' : '按钮';
}

function menuTypeTagType(type: MenuType) {
  return type === 'CATALOG' || type === 'DIR' ? 'warning' : type === 'MENU' ? 'success' : 'info';
}

function onMenuTreeCheck() {
  updateMenuCheckStatus();
}

function updateMenuCheckStatus() {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || [];
  const allKeys: string[] = [];
  
  // 收集所有菜单节点的keys
  const collectKeys = (nodes: MenuItem[]) => {
    for (const node of nodes) {
      allKeys.push(node.menuId);
      if (node.children) {
        collectKeys(node.children);
      }
    }
  };
  
  collectKeys(menuTreeData.value);
  
  checkAllMenus.value = checkedKeys.length === allKeys.length;
  isIndeterminate.menus = checkedKeys.length > 0 && checkedKeys.length < allKeys.length;
}

function handleCheckAllMenusChange(checked: boolean) {
  if (checked) {
    menuTreeRef.value?.setCheckedNodes(menuTreeData.value);
  } else {
    menuTreeRef.value?.setCheckedKeys([]);
  }
}

// 功能权限相关方法
async function loadFuncTree() {
  try {
    const res = await queryPermissionTree();
    funcTreeData.value = res || [];
  } catch (error) {
    console.error('加载功能权限树失败:', error);
    ElMessage.error('加载功能权限树失败');
  }
}

function onFuncTreeCheck() {
  updateFuncCheckStatus();
}

function updateFuncCheckStatus() {
  const checkedKeys = funcTreeRef.value?.getCheckedKeys() || [];
  const allKeys = funcTreeData.value.map((item: PermissionItem) => item.permId);
  
  checkAllFuncs.value = checkedKeys.length === allKeys.length;
  isIndeterminate.funcs = checkedKeys.length > 0 && checkedKeys.length < allKeys.length;
}

function handleCheckAllFuncsChange(checked: boolean) {
  if (checked) {
    funcTreeRef.value?.setCheckedNodes(funcTreeData.value);
  } else {
    funcTreeRef.value?.setCheckedKeys([]);
  }
}

// 刷新角色列表
function refreshRoleList() {
  loadRoleList();
}
</script>

<style scoped lang="scss">
.permission-page {
  height: 100%;
  
  .split-area {
    display: flex;
    gap: 5px;
    height: calc(100vh - 120px);
    align-items: stretch;
  }

  .left-pane {
    width: 300px;
    height: 100%;
  }

  .right-pane {
    flex: 1;
    height: 100%;
    min-width: 720px;
  }

  .role-card, .permission-card {
    height: 100%;
    
    :deep(.el-card__body) {
      height: 100%;
      display: flex;
      flex-direction: column;
    }
  }

  .role-card-body, .permission-card-body {
    padding: 8px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .title {
    font-weight: 600;
  }

  .search-input {
    margin-bottom: 8px;
  }

  .role-list-container {
    flex: 1;
    overflow: auto;
  }

  .role-tree {
    :deep(.el-tree-node__content) {
      height: 36px;
    }
  }

  .role-node {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    
    .role-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-right: 8px;
    }
  }

  .permission-content {
    flex: 1;
    overflow: auto;
    padding-top: 10px;
  }

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    flex-wrap: nowrap;
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: nowrap;
    white-space: nowrap;
  }

  .current-role {
    margin-top: 6px;
    color: #666;
    font-size: 12px;
  }

  .permission-tabs {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    :deep(.el-tabs__content) {
      flex: 1;
      overflow: auto;
    }
    
    :deep(.el-tab-pane) {
      height: 100%;
      display: flex;
      flex-direction: column;
    }
  }

  .permission-section {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .check-all {
    margin-bottom: 12px;
  }

  .permission-tree {
    flex: 1;
    overflow: auto;
    
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
}
</style>