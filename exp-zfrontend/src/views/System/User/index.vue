<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">人员管理</div>
          <!-- 顶部操作区（放在搜索与列表之间） -->
          <div class="actions">
            <el-button
                type="primary"
                size="small"
                @click="handleAdd"
                :disabled="!canManage"
            >
              新增人员信息
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleBatchDelete"
              :disabled="!selectedRows.length || !canDelete"
            >
              删除
            </el-button>
            <el-dropdown
              @command="handleBatchStatusChange"
              :disabled="!selectedRows.length || !canManage"
              trigger="click"
            >
              <el-button size="small">
                批量状态变更
                <el-icon class="el-icon--right">
                  <ArrowDown />
                </el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="ONJOB">
                    批量设为在职
                  </el-dropdown-item>
                  <el-dropdown-item command="LEAVE">
                    批量设为离职
                  </el-dropdown-item>
                  <el-dropdown-item command="DISABLED">
                    批量设为停用
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button size="small" :disabled="true">导入</el-button>
            <el-button size="small" :disabled="true">导出</el-button>
          </div>
        </div>

      </template>

      <!-- 搜索区 -->
      <el-form
        :inline="true"
        :model="query"
        class="search-bar"
        @submit.prevent
      >
        <el-form-item label="人员编码">
          <el-input
            v-model="query.personCode"
            placeholder="请输入人员编码"
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



    <!-- 列表区 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="tableData"
      row-key="personId"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
      @row-click="handleRowClick"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="personCode" label="人员编码" min-width="140" />
      <el-table-column prop="personName" label="姓名" min-width="120" />
      <el-table-column
        prop="gender"
        label="性别"
        min-width="90"
        :formatter="formatGender"
      />
      <el-table-column prop="mobile" label="手机号" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="orgName" label="归属组织" min-width="140" />
      <el-table-column label="角色名称" min-width="140">
        <template #default="{ row }">
          <el-tooltip
            :content="row.roleNames || '无'"
            placement="top"
            :disabled="!row.roleNames || row.roleNames.length <= 15"
          >
            <span class="role-text" :class="{ 'ellipsis': row.roleNames && row.roleNames.length > 15 }">
              {{ row.roleNames || '无' }}
            </span>
          </el-tooltip>
        </template>
      </el-table-column>
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
      <el-table-column
        prop="createdTime"
        label="创建时间"
        min-width="170"
        :formatter="formatDateTime"
      />

      <el-table-column label="操作" fixed="right" width="110">
        <template #default="{ row }">
          <el-button-group>
            <el-button
                link
                type="primary"
                size="small"
                @click="handleEdit(row)"
                :disabled="!canManage"
            >
              编辑
            </el-button>

            <el-dropdown
                trigger="click"
                :disabled="!canManage"
                @command="(status) => changeStatus(row, status)"
            >
              <el-button link size="small">
                状态
                <el-icon class="el-icon--right">
                  <ArrowDown />
                </el-icon>
              </el-button>

              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="ONJOB">在职</el-dropdown-item>
                  <el-dropdown-item command="LEAVE">离职</el-dropdown-item>
                  <el-dropdown-item command="DISABLED">停用</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-button-group>
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
      :title="editDialog.isEdit ? '编辑人员' : '新增人员'"
      width="760px"
      destroy-on-close
      draggable
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="dialog-form two-col"
      >
        <el-form-item label="人员编码" prop="personCode">
          <el-input v-model="form.personCode" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="personName">
          <el-input v-model="form.personName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别" clearable>
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
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="组织" prop="orgId">
          <OrgSelector
            v-model="selectedOrg"
            placeholder="请选择组织"
            @change="handleOrgChange"
          />
        </el-form-item>
        <el-form-item label="岗位" prop="postId">
          <el-select
            v-model="form.postId"
            placeholder="请先选择组织"
            :disabled="!selectedOrg"
            @change="handlePostChange"
          >
            <el-option
              v-for="post in postOptions"
              :key="post.postId"
              :label="post.postName"
              :value="post.postId"
            />
          </el-select>
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

    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import {
  queryPersonList,
  createPerson,
  updatePerson,
  deletePerson,
  changePersonStatus,
  batchChangePersonStatus,
  type ExpPersonVO,
  type PersonStatus,
} from '@/api/system/person';
import { hasPermission } from '@/utils/permission';
import OrgSelector from '@/components/Selector/OrgSelector.vue';
import { queryOrgPosts, type OrgNode, type PostVO } from '@/api/system/post';



const genderMap: Record<string, string> = {
  M: '男',
  F: '女',
  OTHER: '未知',
};

const genderOptions = [
  { label: '男', value: 'M' },
  { label: '女', value: 'F' },
];

// 状态选项（新增时不显示离职）
const statusOptions = computed(() => {
  if (editDialog.isEdit) {
    // 编辑时显示所有状态
    return [
      { label: '在职', value: 'ONJOB' },
      { label: '禁用', value: 'DISABLED' },
      { label: '离职', value: 'LEAVE' },
    ];
  } else {
    // 新增时不显示离职
    return [
      { label: '在职', value: 'ONJOB' },
      { label: '禁用', value: 'DISABLED' },
    ];
  }
});

const loading = ref(false);
const saving = ref(false);
const tableRef = ref();

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
  orgName: '',
  roleName: '',
});

// 选择器数据
const selectedOrg = ref<OrgNode>();
const postOptions = ref<PostVO[]>([]);

const rules: FormRules = {
  personName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  personCode: [{ required: true, message: 'personCode 缺失', trigger: 'change' }],
  orgId: [{ required: true, message: '请选择组织', trigger: 'change' }],
  postId: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  idCardNo: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }
        // 18位身份证校验
        const idCardRegex = /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/;
        if (!idCardRegex.test(value)) {
          callback(new Error('请输入正确的18位身份证号'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }
        // 邮件格式校验
        const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
        if (!emailRegex.test(value)) {
          callback(new Error('请输入正确的邮箱格式'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
};



const canManage = computed(() => hasPermission('system:user:manage'));
const canDelete = computed(() => hasPermission('system:user:delete'));

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

function formatDateTime(row: ExpPersonVO, column: any, cellValue: string) {
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

async function fetchList() {
  loading.value = true;
  try {
    // 使用新的API接口
    const res = await queryPersonList({ ...query });
    tableData.value = Array.isArray(res?.list) ? res.list : [];
    total.value = Number(res?.total ?? 0) || 0;
  } catch (e) {
    tableData.value = [];
    total.value = 0;
    ElMessage.error((e as any)?.message || '查询失败');
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

function handleRowClick(row: ExpPersonVO) {
  // 使用表格的toggleRowSelection方法切换选中状态
  tableRef.value?.toggleRowSelection(row);
}

// 组织选择处理
async function handleOrgChange(org: OrgNode | undefined) {
  form.orgId = org?.orgId;
  // 清空岗位选择
  form.postId = undefined;
  // 获取岗位列表
  if (org?.orgId) {
    await fetchPostOptions(org.orgId);
  } else {
    postOptions.value = [];
  }
}

// 岗位选择处理
function handlePostChange(postId: number | undefined) {
  form.postId = postId;
}

// 获取岗位选项列表
async function fetchPostOptions(orgId: number) {
  try {
    const res = await queryOrgPosts({
      orgId,
      pageNum: 1,
      pageSize: 1000, // 获取所有岗位
      relStatus: 'ENABLED',
      postStatus: 'ENABLED'
    });

    const posts = res?.list || [];
    // 添加"待定"选项，但要去重
    const hasPending = posts.some(post => post.postName === '待定');
    const options = hasPending ? posts : [{ postId: -1, postName: '待定', postCode: 'PENDING', postStatus: 'ENABLED' as const }, ...posts];

    postOptions.value = options;
  } catch (e) {
    console.error('获取岗位列表失败:', e);
    postOptions.value = [{ postId: -1, postName: '待定', postCode: 'PENDING', postStatus: 'ENABLED' }];
  }
}

function generatePersonCode() {
  const now = new Date();
  const year = String(now.getFullYear()).slice(-2); // 年份后2位
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const rand = String(Math.floor(Math.random() * 1000)).padStart(3, '0');
  return `exp${year}${month}${day}${rand}`;
}

function resetFormModel() {
  form.personId = 0;
  form.personCode = generatePersonCode();
  form.personName = '';
  form.gender = undefined; // 性别默认空
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
  form.orgName = '';
  form.roleName = '';
  form.roleIds = '';
  form.roleNames = '';

  // 重置选择器数据
  selectedOrg.value = undefined;
  postOptions.value = [];
}

function handleAdd() {
  editDialog.isEdit = false;
  resetFormModel();
  editDialog.visible = true;
}

async function handleEdit(row: ExpPersonVO) {
  editDialog.isEdit = true;
  Object.assign(form, row);

  // 设置组织选择器数据
  if (row.orgId) {
    // 这里需要根据orgId获取组织信息，或者从row中获取
    // 暂时设置一个基础的组织对象，后续可以优化
    selectedOrg.value = {
      orgId: row.orgId,
      orgName: row.orgName || '',
      orgCode: '',
      children: []
    };

    // 获取岗位列表
    await fetchPostOptions(row.orgId);
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

// 批量删除
async function handleBatchDelete() {
  if (!canDelete.value || !selectedRows.value.length) return;

  const count = selectedRows.value.length;
  const personNames = selectedRows.value.map(row => row.personName).join('、');

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${count} 个人员吗？\n\n人员列表：${personNames}`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
        customClass: 'batch-delete-dialog',
      }
    );

    const ids = selectedRows.value.map(row => row.personId);
    await deletePerson(ids);
    ElMessage.success(`成功删除 ${count} 个人员`);
    fetchList();
  } catch (e) {
    if ((e as any)?.response) {
      ElMessage.error((e as any)?.message || '删除失败');
    }
    // 用户取消操作，不显示错误信息
  }
}

// 批量状态变更
async function handleBatchStatusChange(newStatus: PersonStatus) {
  if (!canManage.value || !selectedRows.value.length) return;

  const count = selectedRows.value.length;
  const personNames = selectedRows.value.map(row => row.personName).join('、');

  try {
    await ElMessageBox.confirm(
      `确定要将选中的 ${count} 个人员状态设为"${statusText(newStatus)}"吗？\n\n人员列表：${personNames}`,
      '批量状态变更确认',
      {
        confirmButtonText: '确定变更',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--primary',
      }
    );

    const ids = selectedRows.value.map(row => row.personId);
    await batchChangePersonStatus(ids, newStatus);
    ElMessage.success(`批量状态变更成功`);
    fetchList();
  } catch (e) {
    if ((e as any)?.response) {
      ElMessage.error((e as any)?.message || '状态变更失败');
    }
    // 用户取消操作，不显示错误信息
  }
}

async function changeStatus(row: ExpPersonVO, newStatus: PersonStatus) {
  if (!canManage.value || row.status === newStatus) return;

  try {
    // 检查状态变更规则
    if (row.status === 'LEAVE' && newStatus === 'ONJOB') {
      ElMessage.warning('离职人员不能直接设为在职状态');
      return;
    }

    await changePersonStatus(row.personId, newStatus);
    ElMessage.success('状态已更新');
    fetchList();
  } catch (e) {
    ElMessage.error((e as any)?.message || '状态更新失败');
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

.role-text {
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}

.role-text.ellipsis {
  cursor: pointer;
}
</style>
