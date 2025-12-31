<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">账号管理</div>
        </div>
      </template>

      <!-- 搜索区 -->
      <el-form
        :inline="true"
        :model="query"
        class="search-bar"
        @submit.prevent
      >
        <el-form-item label="账号编码">
          <el-input
            v-model="query.personCode"
            placeholder="请输入账号编码"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="query.personName"
            placeholder="请输入姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="query.mobile"
            placeholder="请输入手机号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 顶部操作区（放在搜索与列表之间） -->
      <div class="actions">
        <el-button
          type="primary"
          size="small"
          @click="handleAdd"
          :disabled="!canManage"
        >
          新增账号
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

    <!-- 列表区 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      row-key="personId"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="personCode" label="账号编码" min-width="140" />
      <el-table-column prop="personName" label="姓名" min-width="120" />
      <el-table-column
        prop="gender"
        label="性别"
        min-width="90"
        :formatter="formatGender"
      />
      <el-table-column prop="mobile" label="手机号" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="deptName" label="部门名称" min-width="140" />
      <el-table-column prop="roleName" label="角色名称" min-width="140" />
      <el-table-column
        prop="status"
        label="状态"
        min-width="100"
        #default="{ row }"
      >
        <el-tag :type="statusTagType(row.status)">
          {{ statusText(row.status) }}
        </el-tag>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" fixed="right" width="220">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            size="small"
            @click="handleEdit(row)"
            :disabled="!canManage"
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
          <el-button
            link
            size="small"
            @click="toggleStatus(row)"
            :disabled="!canManage"
          >
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
        <el-form-item label="账号编码" prop="personCode">
          <el-input v-model="form.personCode" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="personName">
          <el-input v-model="form.personName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option
              v-for="item in genderOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input
            v-model="form.idCardNo"
            placeholder="预留字段，可按需求开启校验"
          />
        </el-form-item>
        <el-form-item label="职务">
          <el-input v-model="form.jobTitle" placeholder="预留字段" />
        </el-form-item>
        <el-form-item label="入职日期">
          <el-date-picker
            v-model="form.entryDate"
            type="date"
            placeholder="预留字段"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="人员性质" prop="isExternal">
          <el-select v-model="form.isExternal" placeholder="请选择">
            <el-option :value="0" label="内部" />
            <el-option :value="1" label="外部" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="在职" value="ONJOB" />
            <el-option label="禁用" value="DISABLED" />
            <el-option label="离职" value="LEAVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-input
            v-model="form.deptName"
            placeholder="后端 VO 返回，前端不拼装"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-input
            v-model="form.roleName"
            placeholder="后端 VO 返回，前端不拼装"
          />
        </el-form-item>
        <el-form-item label="备注" class="full-row">
          <el-input
            v-model="form.remark"
            type="textarea"
            placeholder="预留字段"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog
      v-model="resetDialog.visible"
      title="重置密码"
      width="460px"
      destroy-on-close
    >
      <div class="reset-tip">
        将重置 {{ selectedRows.length }} 个账号的密码，请确认。
      </div>
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
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
        <el-button type="primary" :loading="resetting" @click="submitResetPwd">
          确认
        </el-button>
      </template>
    </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import {
  queryPersonInfo,
  createPerson,
  updatePerson,
  deletePerson,
  changePersonStatus,
  resetPassword,
  type ExpPersonVO,
  type PersonStatus,
} from '@/api/system/person';
import { hasPermission } from '@/utils/permission';



const genderMap: Record<string, string> = {
  M: '男',
  F: '女',
  OTHER: '未知',
};

// 人员查询接口（按你提供的地址直连）
const PERSON_QUERY_URL = 'http://192.168.101.128/api/exp/auth/person/queryPersonInfo';
let lastListAbortController: AbortController | null = null;

function normalizeListResponse(raw: any): { list: ExpPersonVO[]; total: number } {
  // 兼容常见返回结构：{list,total} / {rows,total} / {records,total} / {data:{list,total}} / {data:{rows,total}} ...
  const body = raw?.data ?? raw ?? {};
  const list: ExpPersonVO[] =
    body?.list ?? body?.rows ?? body?.records ?? body?.data?.list ?? body?.data?.rows ?? body?.data?.records ?? [];
  // 注意：total 可能合法为 0，不能用 `||` 回退
  const total: number =
    body?.total ?? body?.data?.total ?? raw?.total ?? raw?.data?.total ?? (Array.isArray(list) ? list.length : 0);
  return { list: Array.isArray(list) ? list : [], total: Number(total) || 0 };
}

async function requestPersonList(params: Record<string, any>) {
  // 取消上一次未完成的列表请求，避免快速点击/翻页导致的结果覆盖
  if (lastListAbortController) {
    lastListAbortController.abort();
  }
  lastListAbortController = new AbortController();

  // 尽量带上常见 token（如果项目用 cookie 会走 credentials: include）
  const rawToken =
    localStorage.getItem('token') ||
    localStorage.getItem('access_token') ||
    localStorage.getItem('Authorization') ||
    sessionStorage.getItem('token') ||
    sessionStorage.getItem('access_token') ||
    sessionStorage.getItem('Authorization') ||
    '';
  const token = rawToken && rawToken.startsWith('Bearer ') ? rawToken : rawToken ? `Bearer ${rawToken}` : '';

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (token) headers.Authorization = token;

  const resp = await fetch(PERSON_QUERY_URL, {
    method: 'POST',
    headers,
    body: JSON.stringify(params),
    credentials: 'include',
    signal: lastListAbortController.signal,
  });

  if (!resp.ok) {
    throw new Error(`queryPersonInfo http error: ${resp.status}`);
  }

  // 有些后端会返回空 body
  const json = await resp.json().catch(() => ({}));
  return json;
}

const genderOptions = [
  { label: '男', value: 'M' },
  { label: '女', value: 'F' },
  { label: '未知', value: 'OTHER' },
];

const loading = ref(false);
const saving = ref(false);
const resetting = ref(false);

const query = reactive({
  personCode: '',
  personName: '',
  mobile: '',
  pageNum: 1,
  pageSize: 10,
});

const tableData = ref<ExpPersonVO[]>([]);
const total = ref(0);
const selectedRows = ref<ExpPersonVO[]>([]);

// 本地示例数据，便于无后端时测试
const mockData: ExpPersonVO[] = Array.from({ length: 21 }).map((_, idx) => ({
  personId: idx + 1,
  personCode: `Exp2025010${String(idx + 1).padStart(2, '0')}`,
  personName: `示例用户${idx + 1}`,
  gender: idx % 3 === 0 ? 'M' : idx % 3 === 1 ? 'F' : 'OTHER',
  mobile: `1380000${String(1000 + idx)}`,
  email: `user${idx + 1}@demo.com`,
  status: idx % 4 === 0 ? 'DISABLED' : 'ONJOB',
  createdTime: '2025-01-01 10:00:00',
  deptName: '示例部门',
  roleName: '示例角色',
  isExternal: idx % 2,
}));

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const form = reactive<ExpPersonVO>({
  personId: 0,
  personCode: '',
  personName: '',
  gender: 'OTHER',
  mobile: '',
  email: '',
  idCardNo: '',
  jobTitle: '',
  orgId: undefined,
  postId: undefined,
  accountId: undefined,
  status: 'ONJOB',
  entryDate: '',
  isExternal: 0,
  createdTime: '',
  remark: '',
  deptName: '',
  roleName: '',
});

const rules: FormRules = {
  personName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  personCode: [{ required: true, message: 'personCode 缺失', trigger: 'change' }],
};

const resetDialog = reactive({
  visible: false,
});
const resetFormRef = ref<FormInstance>();
const resetForm = reactive({
  newPassword: '123456',
  confirmPassword: '123456',
});
const resetRules: FormRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== resetForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
};


const canManage = computed(() => hasPermission('system:user:manage'));
const canDelete = computed(() => hasPermission('system:user:delete'));
const canReset  = computed(() => hasPermission('system:user:reset'));

onMounted(() => {
  fetchList();
});

function statusTagType(status: string) {
  if (status === 'ONJOB') return 'success';
  if (status === 'DISABLED') return 'info';
  if (status === 'LEAVE') return 'warning';
  return '';
}

function statusText(status: string) {
  return status === 'ONJOB'
    ? '在职'
    : status === 'DISABLED'
    ? '禁用'
    : status === 'LEAVE'
    ? '离职'
    : status;
}

function formatGender(row: ExpPersonVO) {
  return genderMap[row.gender] || row.gender;
}

async function fetchList() {
  loading.value = true;
  try {
    // 优先按指定地址直连请求；如联调环境受 CORS/鉴权影响失败，则兜底用原 api 封装
    let res: any;
    try {
      // 你的接口返回字段使用 page/size，这里请求参数也同时带上 page/size，兼容后端入参
      res = await requestPersonList({
        ...query,
        page: query.pageNum,
        size: query.pageSize,
      });
    } catch (e) {
      // Abort 不算失败（用户快速操作时很常见）
      if ((e as any)?.name === 'AbortError') return;
      res = await queryPersonInfo({ ...query });
    }

    const normalized = normalizeListResponse(res);
    let list = normalized.list ?? [];
    if (!list.length) list = mockData;
    tableData.value = list;
    // total=0 也是合法值，不能用 `||` 回退
    total.value = normalized.total ?? list.length;
  } catch (e) {
    // 接口异常时也用本地示例数据，方便无后端联调
    tableData.value = mockData;
    total.value = mockData.length;
  } finally {
    loading.value = false;
    // 翻页后清空多选
    selectedRows.value = [];
  }
}

function handleSearch() {
  // 清洗输入，避免前后空格导致的“查不到”
  query.personCode = (query.personCode || '').trim();
  query.personName = (query.personName || '').trim();
  query.mobile = (query.mobile || '').trim();

  query.pageNum = 1;
  fetchList();
}

function handleReset() {
  query.personCode = '';
  query.personName = '';
  query.mobile = '';
  query.pageNum = 1;
  fetchList();
}

function changePage(pageNum: number, pageSize: number) {
  query.pageNum = pageNum;
  query.pageSize = pageSize;
  fetchList();
}

function handleCurrentChange(page: number) {
  changePage(page, query.pageSize);
}

function handleSizeChange(size: number) {
  changePage(1, size);
}

function handleSelectionChange(rows: ExpPersonVO[]) {
  selectedRows.value = rows;
}

function generatePersonCode() {
  const now = new Date();
  const yyyyMMdd = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(
    now.getDate(),
  ).padStart(2, '0')}`;
  const rand = String(Math.floor(Math.random() * 1000)).padStart(3, '0');
  return `Exp${yyyyMMdd}${rand}`;
}

function resetFormModel() {
  form.personId = 0;
  form.personCode = generatePersonCode();
  form.personName = '';
  form.gender = 'OTHER';
  form.mobile = '';
  form.email = '';
  form.idCardNo = '';
  form.jobTitle = '';
  form.orgId = undefined;
  form.postId = undefined;
  form.accountId = undefined;
  form.status = 'ONJOB';
  form.entryDate = '';
  form.isExternal = 0;
  form.remark = '';
  form.deptName = '';
  form.roleName = '';
}

function handleAdd() {
  editDialog.isEdit = false;
  resetFormModel();
  editDialog.visible = true;
}

function handleEdit(row: ExpPersonVO) {
  editDialog.isEdit = true;
  Object.assign(form, row);
  editDialog.visible = true;
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
    if (editDialog.isEdit) {
      await updatePerson(form);
      ElMessage.success('编辑成功');
    } else {
      await createPerson(form);
      ElMessage.success('新增成功');
    }
    editDialog.visible = false;
    fetchList();
  } finally {
    saving.value = false;
  }
}

function handleDelete(row: ExpPersonVO) {
  if (!canDelete.value) return;
  ElMessageBox.confirm(`确认删除账号「${row.personName}」吗？`, '提示', {
    type: 'warning',
  })
    .then(async () => {
      await deletePerson(row.personId);
      ElMessage.success('删除成功');
      fetchList();
    })
    .catch(() => {});
}

async function toggleStatus(row: ExpPersonVO) {
  if (!canManage.value) return;
  const nextStatus: PersonStatus = row.status === 'DISABLED' ? 'ONJOB' : 'DISABLED';
  await changePersonStatus(row.personId, nextStatus);
  ElMessage.success('状态已更新');
  fetchList();
}

function openResetPwdDialog() {
  if (!selectedRows.value.length) return;
  resetDialog.visible = true;
  resetForm.newPassword = '123456';
  resetForm.confirmPassword = '123456';
}

async function submitResetPwd() {
  if (!resetFormRef.value) return;
  const valid = await resetFormRef.value.validate();
  if (!valid) return;
  resetting.value = true;
  try {
    const ids = selectedRows.value.map((r) => r.personId);
    await resetPassword(ids, resetForm.newPassword);
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

.actions > * + * {
  margin-left: 8px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
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

.dialog-form.two-col .full-row {
  grid-column: 1 / span 2;
}
</style>
