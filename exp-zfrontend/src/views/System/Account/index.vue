<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">账号管理</div>
          <div class="actions">
            <el-button type="primary" size="small" @click="handleAdd" :disabled="!canManage">
              新增账号
            </el-button>
            <el-button
              size="small"
              @click="batchToggleStatus"
              :disabled="!selectedRows.length || !canManage"
            >
              启用/禁用
            </el-button>
            <el-button
              size="small"
              @click="openResetPwdDialog"
              :disabled="!selectedRows.length || !canReset"
            >
              重置密码
            </el-button>
            <el-button size="small" :disabled="true">导入</el-button>
            <el-button size="small" :disabled="true">导出</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="账号/姓名/手机号/邮箱"
            clearable
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 160px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
        row-key="userId"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="username" label="账号" min-width="140" />
        <el-table-column prop="realName" label="姓名" min-width="140" />
        <el-table-column prop="deptName" label="部门" min-width="160" />
        <el-table-column prop="mobile" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="260">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)" :disabled="!canManage">
              编辑
            </el-button>
            <el-button link size="small" @click="toggleStatus(row)" :disabled="!canManage">
              {{ row.status === 0 ? '启用' : '禁用' }}
            </el-button>
            <el-button link size="small" @click="openResetPwdDialog([row])" :disabled="!canReset">
              重置密码
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" :disabled="!canDelete">
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

      <!-- 新增 / 编辑弹窗 -->
      <el-dialog
        v-model="editDialog.visible"
        :title="editDialog.isEdit ? '编辑账号' : '新增账号'"
        width="760px"
        destroy-on-close
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="dialog-form two-col"
        >
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入账号"
              :disabled="editDialog.isEdit"
            />
          </el-form-item>
          <el-form-item label="姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.mobile" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="部门ID">
            <el-input v-model="form.deptId" placeholder="占位：后续替换为部门选择" />
          </el-form-item>
          <el-form-item label="角色ID">
            <el-input v-model="roleIdsText" placeholder="占位：用逗号分隔，如 r_admin,r_normal" />
          </el-form-item>
          <el-form-item v-if="!editDialog.isEdit" label="初始密码" prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">确认</el-button>
        </template>
      </el-dialog>

      <!-- 重置密码弹窗 -->
      <el-dialog v-model="resetDialog.visible" title="重置密码" width="460px" destroy-on-close>
        <div class="reset-tip">将重置 {{ resetDialog.targetCount }} 个账号的密码，请确认。</div>
        <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="100px">
          <el-form-item label="新密码" prop="password">
            <el-input v-model="resetForm.password" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="resetForm.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="resetDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="resetting" @click="submitResetPwd">确认</el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed, watch } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import {
  queryUserList,
  createUser,
  updateUser,
  deleteUser,
  setUserStatus,
  resetUserPassword,
  type SystemUserVO,
  type UserStatus,
} from '@/api/system/account';

const loading = ref(false);
const saving = ref(false);
const resetting = ref(false);

const query = reactive({
  keyword: '',
  status: undefined as UserStatus | undefined,
  page: 1,
  pageSize: 10,
});

const tableData = ref<SystemUserVO[]>([]);
const total = ref(0);
const selectedRows = ref<SystemUserVO[]>([]);

const canManage = computed(() => hasPermission('system:user:manage'));
const canDelete = computed(() => hasPermission('system:user:delete'));
const canReset = computed(() => hasPermission('system:user:reset'));

function statusTagType(status?: UserStatus) {
  if (status === 1) return 'success';
  if (status === 0) return 'info';
  return '';
}

function statusText(status?: UserStatus) {
  if (status === 1) return '启用';
  if (status === 0) return '禁用';
  return '-';
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await queryUserList({ ...query });
    const records = (res as any)?.records ?? (res as any)?.list ?? (res as any)?.rows ?? [];
    tableData.value = Array.isArray(records) ? records : [];
    total.value = Number((res as any)?.total ?? tableData.value.length) || 0;
  } catch (e) {
    tableData.value = [];
    total.value = 0;
    ElMessage.error((e as any)?.message || '查询失败');
  } finally {
    loading.value = false;
    selectedRows.value = [];
  }
}

onMounted(() => {
  fetchList();
});

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

function handleSelectionChange(rows: SystemUserVO[]) {
  selectedRows.value = rows;
}

// 新增/编辑
const editDialog = reactive({
  visible: false,
  isEdit: false,
});

type EditFormModel = {
  userId: string;
  username: string;
  realName: string;
  deptId?: string;
  mobile?: string;
  email?: string;
  status: UserStatus;
  roleIds?: string[];
  password?: string;
};

const formRef = ref<FormInstance>();
const form = reactive<EditFormModel>({
  userId: '',
  username: '',
  realName: '',
  deptId: '',
  mobile: '',
  email: '',
  status: 1,
  roleIds: [],
  password: '123456',
});

const roleIdsText = ref('');
watch(
  () => roleIdsText.value,
  (val) => {
    const arr = (val || '')
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);
    form.roleIds = arr;
  },
);

function resetFormModel() {
  form.userId = '';
  form.username = '';
  form.realName = '';
  form.deptId = '';
  form.mobile = '';
  form.email = '';
  form.status = 1;
  form.roleIds = [];
  form.password = '123456';
  roleIdsText.value = '';
}

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [
    {
      validator: (_rule, value, callback) => {
        // 编辑账号时不修改初始密码，因此不校验
        if (editDialog.isEdit) return callback();
        if (!value) return callback(new Error('请输入初始密码'));
        return callback();
      },
      trigger: 'blur',
    },
  ],
};

function handleAdd() {
  editDialog.isEdit = false;
  resetFormModel();
  editDialog.visible = true;
}

function handleEdit(row: SystemUserVO) {
  editDialog.isEdit = true;
  resetFormModel();
  form.userId = row.userId;
  form.username = row.username;
  form.realName = row.realName || '';
  form.deptId = row.deptId || '';
  form.mobile = row.mobile || '';
  form.email = row.email || '';
  form.status = (row.status ?? 1) as UserStatus;
  const ids = Array.isArray(row.roleIds) ? row.roleIds : [];
  form.roleIds = ids;
  roleIdsText.value = ids.join(',');
  editDialog.visible = true;
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
    if (editDialog.isEdit) {
      await updateUser({
        userId: form.userId,
        realName: form.realName,
        deptId: form.deptId,
        mobile: form.mobile,
        email: form.email,
        roleIds: form.roleIds,
      });
      ElMessage.success('编辑成功');
    } else {
      await createUser({
        username: form.username,
        realName: form.realName,
        deptId: form.deptId,
        mobile: form.mobile,
        email: form.email,
        roleIds: form.roleIds,
        password: form.password,
      });
      ElMessage.success('新增成功');
    }
    editDialog.visible = false;
    fetchList();
  } finally {
    saving.value = false;
  }
}

// 删除
function handleDelete(row: SystemUserVO) {
  if (!canDelete.value) return;
  ElMessageBox.confirm(`确认删除账号「${row.username}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.userId);
      ElMessage.success('删除成功');
      fetchList();
    })
    .catch(() => {});
}

// 启用/禁用
async function toggleStatus(row: SystemUserVO) {
  if (!canManage.value) return;
  const next: UserStatus = row.status === 0 ? 1 : 0;
  await setUserStatus(row.userId, next);
  ElMessage.success('状态已更新');
  fetchList();
}

async function batchToggleStatus() {
  if (!canManage.value || !selectedRows.value.length) return;
  const hasDisabled = selectedRows.value.some((r) => r.status === 0);
  const target: UserStatus = hasDisabled ? 1 : 0;
  const ids = selectedRows.value.map((r) => r.userId);
  try {
    await Promise.all(ids.map((id) => setUserStatus(id, target)));
    ElMessage.success('状态已更新');
    fetchList();
  } catch (e) {
    ElMessage.error((e as any)?.message || '状态更新失败');
  }
}

// 重置密码
const resetDialog = reactive({
  visible: false,
  targetIds: [] as string[],
  targetCount: 0,
});

const resetFormRef = ref<FormInstance>();
const resetForm = reactive({
  password: '123456',
  confirmPassword: '123456',
});

const resetRules: FormRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== resetForm.password) callback(new Error('两次输入的密码不一致'));
        else callback();
      },
      trigger: 'blur',
    },
  ],
};

function openResetPwdDialog(rows?: SystemUserVO[]) {
  const targets = rows && rows.length ? rows : selectedRows.value;
  if (!targets.length) return;
  resetDialog.targetIds = targets.map((r) => r.userId);
  resetDialog.targetCount = targets.length;
  resetForm.password = '123456';
  resetForm.confirmPassword = '123456';
  resetDialog.visible = true;
}

async function submitResetPwd() {
  if (!resetFormRef.value) return;
  const valid = await resetFormRef.value.validate();
  if (!valid) return;
  resetting.value = true;
  try {
    await resetUserPassword(resetDialog.targetIds, resetForm.password);
    ElMessage.success('密码已重置');
    resetDialog.visible = false;
  } finally {
    resetting.value = false;
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

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  margin-right: 30px;
  gap: 8px;
}

.search-bar {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.reset-tip {
  margin-bottom: 12px;
  color: #666;
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
</style>


