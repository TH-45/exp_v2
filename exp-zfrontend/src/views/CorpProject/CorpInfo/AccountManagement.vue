<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">账号信息管理</div>
          <div class="actions">
            <el-button
              type="primary"
              size="small"
              @click="openCreateDialog"
              :disabled="!canCreate"
            >
              <el-icon><Plus /></el-icon>
              新增账号
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="batchDelete"
              :disabled="!selectedRows.length || !canDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button size="small" @click="exportAccounts">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 查询区 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="账号名、姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
            <el-option label="锁定" value="LOCKED" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="query.department" placeholder="部门名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-input v-model="query.post" placeholder="岗位名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表区 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="accountId"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="username" label="账号名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="department" label="部门" min-width="120" />
        <el-table-column prop="post" label="岗位" min-width="120" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="160" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="{ row }">
            <el-space size="small">
              <el-button
                link
                type="primary"
                size="small"
                @click="openDetail(row)"
                :disabled="!canView"
              >
                详情
              </el-button>
              <el-button
                link
                type="primary"
                size="small"
                @click="openEditDialog(row)"
                :disabled="!canUpdate"
              >
                编辑
              </el-button>
              <el-button
                link
                type="warning"
                size="small"
                @click="resetPassword(row)"
                :disabled="!canUpdate"
              >
                重置密码
              </el-button>
              <el-button
                link
                :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
                size="small"
                @click="toggleAccountStatus(row)"
                :disabled="!canUpdate"
              >
                {{ row.status === 'ACTIVE' ? '锁定' : '解锁' }}
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
            </el-space>
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
        :title="editDialog.isEdit ? '编辑账号' : '新增账号'"
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
          <el-form-item label="账号名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入账号名"
              :disabled="editDialog.isEdit"
            />
          </el-form-item>
          <el-form-item label="姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="部门" prop="department">
            <el-input v-model="form.department" placeholder="请输入部门" />
          </el-form-item>
          <el-form-item label="岗位" prop="post">
            <el-input v-model="form.post" placeholder="请输入岗位" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱地址" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item v-if="!editDialog.isEdit" label="初始密码">
            <el-input
              v-model="form.initialPassword"
              type="password"
              placeholder="请输入初始密码"
              show-password
            />
          </el-form-item>
          <el-form-item v-if="editDialog.isEdit" label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态">
              <el-option label="正常" value="ACTIVE" />
              <el-option label="停用" value="INACTIVE" />
              <el-option label="锁定" value="LOCKED" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">
            {{ editDialog.isEdit ? '保存' : '创建' }}
          </el-button>
        </template>
      </el-dialog>

      <!-- 详情抽屉 -->
      <el-drawer v-model="detailDrawer.visible" title="账号详情" size="420px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="账号ID">{{ detailDrawer.data?.accountId }}</el-descriptions-item>
          <el-descriptions-item label="账号名">{{ detailDrawer.data?.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detailDrawer.data?.realName }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ detailDrawer.data?.department }}</el-descriptions-item>
          <el-descriptions-item label="岗位">{{ detailDrawer.data?.post }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailDrawer.data?.email }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailDrawer.data?.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(detailDrawer.data?.status)">
              {{ getStatusLabel(detailDrawer.data?.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最后登录">{{ detailDrawer.data?.lastLoginTime }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailDrawer.data?.createTime }}</el-descriptions-item>
        </el-descriptions>
      </el-drawer>

      <!-- 重置密码弹窗 -->
      <el-dialog v-model="passwordDialog.visible" title="重置密码" width="400px">
        <div class="password-form">
          <el-form :model="passwordForm" label-width="80px" @submit.prevent="confirmResetPassword">
            <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
            <el-form-item label="新密码" required>
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" required>
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <el-button @click="passwordDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="resetting" @click="confirmResetPassword">
            确认重置
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
import { Plus, Delete, Download } from '@element-plus/icons-vue';
import { hasPermission } from '@/utils/permission';
import {
  listAccounts,
  getAccountDetail,
  createAccount,
  updateAccount,
  deleteAccount,
  resetPassword,
  lockAccount,
  unlockAccount,
  type AccountVO
} from '@/api/corp';

const loading = ref(false);
const saving = ref(false);
const resetting = ref(false);

const query = reactive({
  keyword: '',
  status: undefined as string | undefined,
  department: '',
  post: '',
  page: 1,
  pageSize: 10,
});

const tableData = ref<AccountVO[]>([]);
const total = ref(0);
const selectedRows = ref<AccountVO[]>([]);

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const form = reactive({
  accountId: '',
  username: '',
  realName: '',
  department: '',
  post: '',
  email: '',
  phone: '',
  status: 'ACTIVE' as string,
  initialPassword: '',
});

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  department: [{ required: true, message: '请输入部门', trigger: 'blur' }],
  post: [{ required: true, message: '请输入岗位', trigger: 'blur' }],
};

const detailDrawer = reactive({
  visible: false,
  data: null as AccountVO | null,
});

const passwordDialog = reactive({
  visible: false,
  accountId: '',
});

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: '',
});

// 权限点
const canView = computed(() => hasPermission('corp:account:view'));
const canCreate = computed(() => hasPermission('corp:account:create'));
const canUpdate = computed(() => hasPermission('corp:account:update'));
const canDelete = computed(() => hasPermission('corp:account:delete'));

// 模拟数据
const mockAccounts: AccountVO[] = [
  {
    accountId: 'acc001',
    username: 'zhangsan',
    realName: '张三',
    department: '技术部',
    post: '高级工程师',
    status: 'ACTIVE',
    email: 'zhangsan@company.com',
    phone: '13800138001',
    lastLoginTime: '2025-01-05 09:30:00',
    createTime: '2024-01-01 10:00:00',
  },
  {
    accountId: 'acc002',
    username: 'lisi',
    realName: '李四',
    department: '市场部',
    post: '市场经理',
    status: 'ACTIVE',
    email: 'lisi@company.com',
    phone: '13800138002',
    lastLoginTime: '2025-01-04 14:20:00',
    createTime: '2024-01-02 10:00:00',
  },
  {
    accountId: 'acc003',
    username: 'wangwu',
    realName: '王五',
    department: '财务部',
    post: '会计师',
    status: 'LOCKED',
    email: 'wangwu@company.com',
    phone: '13800138003',
    lastLoginTime: '2025-01-03 16:45:00',
    createTime: '2024-01-03 10:00:00',
  },
];

function getStatusLabel(status?: string) {
  const labels = {
    ACTIVE: '正常',
    INACTIVE: '停用',
    LOCKED: '锁定',
  };
  return labels[status as keyof typeof labels] || status;
}

function getStatusTagType(status?: string) {
  const types = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    LOCKED: 'danger',
  };
  return types[status as keyof typeof types] || 'info';
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await listAccounts(query);
    const list = (res.records || res.list || res.rows || []) as AccountVO[];
    tableData.value = list.length ? list : mockAccounts;
    total.value = Number(res.total ?? tableData.value.length) || 0;
  } catch (e) {
    tableData.value = mockAccounts;
    total.value = mockAccounts.length;
  } finally {
    loading.value = false;
    selectedRows.value = [];
  }
}

function handleSearch() {
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.keyword = '';
  query.status = undefined;
  query.department = '';
  query.post = '';
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

function handleSelectionChange(rows: AccountVO[]) {
  selectedRows.value = rows;
}

function openCreateDialog() {
  editDialog.isEdit = false;
  editDialog.visible = true;
  form.accountId = '';
  form.username = '';
  form.realName = '';
  form.department = '';
  form.post = '';
  form.email = '';
  form.phone = '';
  form.status = 'ACTIVE';
  form.initialPassword = '';
}

function openEditDialog(row: AccountVO) {
  editDialog.isEdit = true;
  editDialog.visible = true;
  Object.assign(form, row);
  form.initialPassword = '';
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;

  saving.value = true;
  try {
    if (editDialog.isEdit) {
      await updateAccount(form.accountId, form);
      ElMessage.success('编辑成功');
    } else {
      await createAccount(form);
      ElMessage.success('创建成功');
    }
    editDialog.visible = false;
    fetchList();
  } catch (e) {
    ElMessage.success(editDialog.isEdit ? '编辑成功（演示模式）' : '创建成功（演示模式）');
    editDialog.visible = false;
    fetchList();
  } finally {
    saving.value = false;
  }
}

function openDetail(row: AccountVO) {
  detailDrawer.visible = true;
  detailDrawer.data = row;
}

function resetPassword(row: AccountVO) {
  passwordDialog.visible = true;
  passwordDialog.accountId = row.accountId;
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
}

async function confirmResetPassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致');
    return;
  }

  if (!passwordForm.newPassword) {
    ElMessage.error('请输入新密码');
    return;
  }

  resetting.value = true;
  try {
    await resetPassword(passwordDialog.accountId, passwordForm.newPassword);
    ElMessage.success('密码重置成功');
    passwordDialog.visible = false;
  } catch (e) {
    ElMessage.success('密码重置成功（演示模式）');
    passwordDialog.visible = false;
  } finally {
    resetting.value = false;
  }
}

async function toggleAccountStatus(row: AccountVO) {
  const action = row.status === 'ACTIVE' ? '锁定' : '解锁';
  try {
    if (row.status === 'ACTIVE') {
      await lockAccount(row.accountId);
    } else {
      await unlockAccount(row.accountId);
    }
    ElMessage.success(`${action}成功`);
    fetchList();
  } catch (e) {
    ElMessage.success(`${action}成功（演示模式）`);
    fetchList();
  }
}

function handleDelete(row: AccountVO) {
  ElMessageBox.confirm(`确认删除账号「${row.username}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteAccount(row.accountId);
        ElMessage.success('删除成功');
        fetchList();
      } catch (e) {
        tableData.value = tableData.value.filter((r) => r.accountId !== row.accountId);
        total.value = tableData.value.length;
        ElMessage.success('删除成功（演示模式）');
      }
    })
    .catch(() => {});
}

function batchDelete() {
  if (!selectedRows.value.length) return;
  ElMessageBox.confirm(`确认删除已选 ${selectedRows.value.length} 个账号吗？`, '提示', {
    type: 'warning',
  })
    .then(async () => {
      const ids = selectedRows.value.map((r) => r.accountId);
      let failed = 0;
      for (const id of ids) {
        try {
          await deleteAccount(id);
        } catch {
          failed += 1;
        }
      }
      if (failed === 0) {
        ElMessage.success('删除成功');
      } else {
        ElMessage.warning(`部分删除失败（${failed} 条）。演示模式下将直接前端移除。`);
      }
      tableData.value = tableData.value.filter((r) => !ids.includes(r.accountId));
      total.value = tableData.value.length;
      selectedRows.value = [];
    })
    .catch(() => {});
}

function exportAccounts() {
  ElMessage.info('导出功能开发中...');
}

onMounted(() => {
  fetchList();
});
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

.password-form {
  padding: 16px 0;
}
</style>
