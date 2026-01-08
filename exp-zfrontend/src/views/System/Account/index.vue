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
            <el-button
              type="danger"
              size="small"
              @click="handleBatchDelete"
              :disabled="!selectedRows.length || !canDelete"
            >
              删除
            </el-button>

            <el-button size="small" :disabled="true">导入</el-button>
            <el-button size="small" :disabled="true">导出</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="账号名称">
          <el-input
            v-model="query.accountName"
            placeholder="请输入账号名称"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="账号人姓名">
          <el-input
            v-model="query.personName"
            placeholder="请输入账号人姓名"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="query.mobile"
            placeholder="请输入手机号"
            clearable
            style="width: 160px"
          />
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
        row-key="accountId"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="accountName" label="账号名" min-width="120" />
        <el-table-column prop="personName" label="姓名" min-width="120" />
        <el-table-column prop="orgName" label="组织名称" min-width="140" />
        <el-table-column prop="postName" label="岗位名称" min-width="120" />
        <el-table-column prop="mobile" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)" :disabled="!canManage">
              编辑
            </el-button>
            <el-button link size="small" @click="toggleStatus(row)" :disabled="!canManage">
              {{ row.status === 'DISABLED' ? '启用' : '禁用' }}
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
          <el-form-item label="账号名" prop="accountName">
            <el-input
              v-model="form.accountName"
              placeholder="系统自动生成"
              readonly
              class="readonly-input"
            />
          </el-form-item>
          <el-form-item label="关联人员" prop="personId">
            <PersonSelector
              v-model="selectedPerson"
              placeholder="请选择关联人员"
              @change="handlePersonChange"
            />
          </el-form-item>
          <el-form-item label="姓名" prop="personName">
            <el-input v-model="form.personName" placeholder="选择人员后自动带出" readonly class="readonly-input" />
          </el-form-item>
          <el-form-item label="所属组织" prop="orgId">
            <OrgSelector
                v-model="selectedOrg"
                placeholder="请选择所属组织"
                @change="handleOrgChange"
            />
          </el-form-item>

          <el-form-item label="手机号" prop="mobile">
            <el-input v-model="form.mobile" placeholder="选择人员后自动带出" readonly class="readonly-input" />
          </el-form-item>
          <el-form-item label="岗位" prop="postId">
            <PostSelector
                v-model="selectedPost"
                :org-id="form.orgId"
                placeholder="请选择岗位"
                @change="handlePostChange"
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="选择人员后自动带出" readonly class="readonly-input" />
          </el-form-item>

          <el-form-item label="备注">
            <el-input v-model="form.remark" placeholder="请输入备注" />
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
import { generateAccountName } from '@/utils/account';
import PersonSelector from '@/components/Selector/PersonSelector.vue';
import OrgSelector from '@/components/Selector/OrgSelector.vue';
import PostSelector from '@/components/Selector/PostSelector.vue';
import {
  queryAccountList,
  createUser,
  updateUser,
  deleteUser,
  setUserStatus,
  resetUserPassword,
  getAccountDetail,
  type AccountVO,
} from '@/api/system/account';
import type { ExpPersonVO } from '@/api/system/person';
import type { OrgNode } from '@/api/system/post';
import type { PostVO } from '@/api/system/post';

const loading = ref(false);
const saving = ref(false);
const resetting = ref(false);
const tableRef = ref();

const query = reactive({
  accountName: '',
  personName: '',
  mobile: '',
  pageNum: 1,
  pageSize: 10,
  sort: '',
  queryParam: {} as any,
});

const tableData = ref<AccountVO[]>([]);
const total = ref(0);
const selectedRows = ref<AccountVO[]>([]);

const canManage = computed(() => hasPermission('system:user:manage'));
const canDelete = computed(() => hasPermission('system:user:delete'));
const canReset = computed(() => hasPermission('system:user:reset'));

function statusTagType(status?: string) {
  if (status === 'ENABLED') return 'success';
  if (status === 'DISABLED') return 'info';
  if (status === 'LOCKED') return 'danger';
  if (status === 'INIT') return 'warning';
  return '';
}

function statusText(status?: string) {
  if (status === 'ENABLED') return '启用';
  if (status === 'DISABLED') return '禁用';
  if (status === 'LOCKED') return '锁定';
  if (status === 'INIT') return '初始';
  return '-';
}

function formatDateTime(dateTime?: string) {
  if (!dateTime) return '-';

  try {
    const date = new Date(dateTime);
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}`;
  } catch (e) {
    return '-';
  }
}

async function fetchList() {
  loading.value = true;
  try {
    // 构造查询参数
    const searchParams = { ...query };

    // 构造queryParam，包含搜索条件
    searchParams.queryParam = {};
    if (query.accountName.trim()) {
      searchParams.queryParam.accountName = query.accountName.trim();
    }
    if (query.personName.trim()) {
      searchParams.queryParam.personName = query.personName.trim();
    }
    if (query.mobile.trim()) {
      searchParams.queryParam.mobile = query.mobile.trim();
    }

    // 从searchParams中移除单独的搜索字段，只保留queryParam中的
    delete (searchParams as any).accountName;
    delete (searchParams as any).personName;
    delete (searchParams as any).mobile;

    console.log('Fetching account list with params:', searchParams);
    const res = await queryAccountList(searchParams);
    console.log('Account list response:', res);
    // 注意：由于axios响应拦截器的处理，这里收到的res已经是apiResponse.data了
    const data = res as any;

    if (data) {
      tableData.value = Array.isArray(data.list) ? data.list : [];
      total.value = Number(data.total) || 0;
      console.log('Loaded accounts:', tableData.value.length, 'total:', total.value);
    } else {
      tableData.value = [];
      total.value = 0;
      console.log('No data returned from account list API');
    }
  } catch (e) {
    console.error('Failed to fetch account list:', e);
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
  query.accountName = (query.accountName || '').trim();
  query.personName = (query.personName || '').trim();
  query.mobile = (query.mobile || '').trim();
  query.pageNum = 1;
  fetchList();
}

function handleReset() {
  query.accountName = '';
  query.personName = '';
  query.mobile = '';
  query.pageNum = 1;
  query.queryParam = {};
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

function handleSelectionChange(rows: AccountVO[]) {
  selectedRows.value = rows;
}

function handleRowClick(row: AccountVO) {
  // 使用表格的toggleRowSelection方法切换选中状态
  tableRef.value?.toggleRowSelection(row);
}

// 新增/编辑
const editDialog = reactive({
  visible: false,
  isEdit: false,
});

type EditFormModel = {
  accountId?: number;
  accountName: string;
  personName: string;  // 姓名（只读，由人员选择器带出）
  personId?: number;   // 关联人员ID
  orgId?: number;      // 所属组织ID
  postId?: number;     // 主岗位ID
  mobile?: string;     // 手机号（只读，由人员选择器带出）
  email?: string;      // 邮箱（只读，由人员选择器带出）
  remark?: string;
};

const formRef = ref<FormInstance>();
const form = reactive<EditFormModel>({
  accountId: undefined,
  accountName: '',
  personName: '',
  personId: undefined,
  orgId: undefined,
  postId: undefined,
  mobile: '',
  email: '',
  remark: '',
});

// 选择器数据
const selectedPerson = ref<ExpPersonVO>();
const selectedOrg = ref<OrgNode>();
const selectedPost = ref<PostVO>();

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
  form.accountId = undefined;
  form.accountName = '';
  form.personName = '';
  form.personId = undefined;
  form.orgId = undefined;
  form.postId = undefined;
  form.mobile = '';
  form.email = '';
  form.remark = '';

  // 重置选择器数据
  selectedPerson.value = undefined;
  selectedOrg.value = undefined;
  selectedPost.value = undefined;
}

// 选择器事件处理
function handlePersonChange(person: ExpPersonVO | undefined) {
  form.personId = person?.personId;
  form.personName = person?.personName || '';
  form.mobile = person?.mobile || '';
  form.email = person?.email || '';
}

function handleOrgChange(org: OrgNode | undefined) {
  form.orgId = org?.orgId;
  // 组织改变时，清空岗位选择
  selectedPost.value = undefined;
  form.postId = undefined;
}

function handlePostChange(post: PostVO | undefined) {
  form.postId = post?.postId;
}

const rules: FormRules = {
  accountName: [{ required: true, message: '账号名不能为空', trigger: 'blur' }],
  personName: [{ required: true, message: '请选择关联人员', trigger: 'blur' }],
  personId: [{ required: true, message: '请选择关联人员', trigger: 'change' }],
  orgId: [{ required: true, message: '请选择所属组织', trigger: 'change' }],
  postId: [{ required: true, message: '请选择岗位', trigger: 'change' }],
};

function handleAdd() {
  editDialog.isEdit = false;
  resetFormModel();
  // 自动生成账号名
  form.accountName = generateAccountName();
  editDialog.visible = true;
}

async function handleEdit(row: AccountVO) {
  editDialog.isEdit = true;
  resetFormModel();

  try {

    // 获取账号详情数据
    const detailData = await getAccountDetail(row.accountId);
    console.log('detailData = ', detailData);
    // 设置表单基本数据
    form.accountId = detailData.accountId;
    form.accountName = detailData.accountName;
    form.personName = detailData.personName || '';
    form.personId = detailData.personId;
    form.mobile = detailData.mobile || '';
    form.email = detailData.email || '';
    form.orgId = detailData.orgId;
    form.postId = detailData.postId;
    form.remark = detailData.remark || '';

    // 设置选择器数据
    if (detailData.personId && detailData.personName) {
      selectedPerson.value = {
        personId: detailData.personId,
        personCode: detailData.personName, // 账号详情接口目前只返回personName，暂时用作personCode
        personName: detailData.personName,
        gender: 'OTHER' as const,
        mobile: detailData.mobile,
        email: detailData.email,
        status: 'ONJOB' as const,
      };
    }

    if (detailData.orgId && detailData.orgName) {
      selectedOrg.value = {
        orgId: detailData.orgId,
        orgName: detailData.orgName,
        orgCode: detailData.orgCode || '',
        parentOrgId: 0, // 默认根节点
        orgType: 'DEPT' as const,
        orgPath: '',
        orgLevel: 1,
        managerPersonId: undefined,
        contactPhone: '',
        address: '',
        status: 'ENABLED' as const,
        sortNo: 0,
        children: [], // 树形结构需要的字段
      };
    }

    if (detailData.postId && detailData.postName) {
      selectedPost.value = {
        postId: detailData.postId,
        postCode: detailData.postCode || detailData.postName, // 优先使用postCode，如果没有则使用postName
        postName: detailData.postName,
        postType: '',
        postLevel: '',
        postCategory: '',
        postDesc: '',
        status: 'ENABLED' as const,
        defaultRoleId: undefined,
        defaultDataScope: '',
        isSystem: 0,
        sortNo: 0,
        postStatus: 'ENABLED' as const, // 添加postStatus字段
      };
    }

    editDialog.visible = true;
  } catch (e) {
    ElMessage.error('获取账号详情失败');
    console.error('获取账号详情失败:', e);
  }
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
    if (editDialog.isEdit) {
      await updateUser({
        accountId: form.accountId,
        accountDisplay: form.personName, // 使用personName作为accountDisplay
        personId: form.personId,
        orgId: form.orgId,
        postId: form.postId,
        mobile: form.mobile,
        email: form.email,
        remark: form.remark,
      });
      ElMessage.success('编辑成功');
    } else {
      await createUser({
        accountName: form.accountName,
        accountDisplay: form.personName, // 使用personName作为accountDisplay
        mobile: form.mobile,
        email: form.email,
        personId: form.personId!,
        orgId: form.orgId!,
        postId: form.postId!,
        remark: form.remark,
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
function handleDelete(row: AccountVO) {
  if (!canDelete.value) return;
  ElMessageBox.confirm(`确认删除账号「${row.accountName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.accountId);
      ElMessage.success('删除成功');
      fetchList();
    })
    .catch(() => {});
}

// 批量删除
async function handleBatchDelete() {
  if (!canDelete.value || !selectedRows.value.length) return;

  const count = selectedRows.value.length;
  const accountNames = selectedRows.value.map(row => row.accountName).join('、');

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${count} 个账号吗？\n\n账号列表：${accountNames}`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
        customClass: 'batch-delete-dialog',
      }
    );

    const ids = selectedRows.value.map(row => row.accountId);
    await Promise.all(ids.map(id => deleteUser(id)));
    ElMessage.success(`成功删除 ${count} 个账号`);
    fetchList();
  } catch (e) {
    if ((e as any)?.response) {
      ElMessage.error((e as any)?.message || '删除失败');
    }
    // 用户取消操作，不显示错误信息
  }
}

// 启用/禁用
async function toggleStatus(row: AccountVO) {
  if (!canManage.value) return;
  const nextStatus = row.status === 'DISABLED' ? 'ENABLED' : 'DISABLED';
  await setUserStatus(row.accountId, nextStatus);
  ElMessage.success('状态已更新');
  fetchList();
}

async function batchToggleStatus() {
  if (!canManage.value || !selectedRows.value.length) return;
  const hasDisabled = selectedRows.value.some((r) => r.status === 'DISABLED');
  const targetStatus = hasDisabled ? 'ENABLED' : 'DISABLED';
  const statusText = targetStatus === 'ENABLED' ? '启用' : '禁用';
  const ids = selectedRows.value.map((r) => r.accountId);

  try {
    await ElMessageBox.confirm(
      `确定要${statusText}选中的 ${selectedRows.value.length} 个账号吗？`,
      '批量操作确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--primary',
      }
    );

    await Promise.all(ids.map((id) => setUserStatus(id, targetStatus)));
    ElMessage.success(`批量${statusText}成功`);
    fetchList();
  } catch (e) {
    if ((e as any)?.response) {
      ElMessage.error((e as any)?.message || `批量${statusText}失败`);
    }
    // 用户取消操作，不显示错误信息
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

function openResetPwdDialog(rows?: AccountVO[]) {
  const targets = rows && rows.length ? rows : selectedRows.value;
  if (!targets.length) return;
  resetDialog.targetIds = targets.map((r) => r.accountId);
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

.readonly-input :deep(.el-input__inner) {
  background-color: #f5f7fa;
  color: #606266;
  cursor: not-allowed;

}

</style>


