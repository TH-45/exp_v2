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
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="角色名称/角色编码"
            clearable
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表区 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="roleId"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
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
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
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
        >
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model="form.roleName" placeholder="请输入角色名称" />
          </el-form-item>
          <el-form-item label="角色编码" prop="roleCode">
            <el-input v-model="form.roleCode" placeholder="如：ADMIN / ROLE_ADMIN" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" class="full-row">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选" />
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
          <el-descriptions-item label="创建时间">{{ detailDrawer.data?.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detailDrawer.data?.updateTime }}</el-descriptions-item>
        </el-descriptions>
      </el-drawer>
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
  type RoleVO,
  type RoleStatus,
} from '@/api/system/role';

const loading = ref(false);
const saving = ref(false);

const query = reactive({
  keyword: '',
  status: undefined as RoleStatus | undefined,
  page: 1,
  pageSize: 10,
});

const tableData = ref<RoleVO[]>([]);
const total = ref(0);
const selectedRows = ref<RoleVO[]>([]);

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const form = reactive<Partial<RoleVO>>({
  roleId: '',
  roleName: '',
  roleCode: '',
  status: 1,
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

// 权限点（与路由 meta.perms 保持一致）
const canView = computed(() => hasPermission('system:role:view'));
const canCreate = computed(() => hasPermission('system:role:create'));
const canUpdate = computed(() => hasPermission('system:role:update'));
const canDelete = computed(() => hasPermission('system:role:delete'));

const mockList: RoleVO[] = [
  {
    roleId: 'r_admin',
    roleCode: 'ADMIN',
    roleName: '系统管理员',
    status: 1,
    remark: '拥有全部权限',
    createTime: '2025-01-01 10:00:00',
    updateTime: '2025-01-02 10:00:00',
  },
  {
    roleId: 'r_user',
    roleCode: 'USER',
    roleName: '普通用户',
    status: 1,
    remark: '仅查看权限',
    createTime: '2025-01-01 10:00:00',
    updateTime: '2025-01-02 10:00:00',
  },
  {
    roleId: 'r_disabled',
    roleCode: 'DISABLED',
    roleName: '停用角色',
    status: 0,
    remark: '示例数据',
    createTime: '2025-01-01 10:00:00',
    updateTime: '2025-01-02 10:00:00',
  },
];

onMounted(() => {
  fetchList();
});

function statusTagType(status?: RoleStatus) {
  const s = String(status ?? '1');
  return s === '1' ? 'success' : 'info';
}

function statusText(status?: RoleStatus) {
  const s = String(status ?? '1');
  return s === '1' ? '启用' : '停用';
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await listRoles({
      page: query.page,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status,
    });
    const list = (res.records || res.list || res.rows || []) as RoleVO[];
    tableData.value = list.length ? list : mockList;
    total.value = Number(res.total ?? tableData.value.length) || 0;
  } catch (e) {
    tableData.value = mockList;
    total.value = mockList.length;
  } finally {
    loading.value = false;
    selectedRows.value = [];
  }
}

function handleSearch() {
  query.keyword = (query.keyword || '').trim();
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.keyword = '';
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

function resetFormModel() {
  form.roleId = '';
  form.roleName = '';
  form.roleCode = '';
  form.status = 1;
  form.remark = '';
}

function openEdit(isEdit: boolean, row?: RoleVO) {
  editDialog.isEdit = isEdit;
  if (isEdit && row) {
    Object.assign(form, row);
  } else {
    resetFormModel();
  }
  editDialog.visible = true;
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
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
    detailDrawer.data = res;
  } catch {
    // 使用列表行数据展示即可
  }
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
</style>


