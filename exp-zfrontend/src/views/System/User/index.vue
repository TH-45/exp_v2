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
                    批量设为禁用
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
        min-width="60"
        :formatter="formatGender"
      />
      <el-table-column prop="mobile" label="手机号" min-width="110" />
      <el-table-column prop="email" label="邮箱" min-width="160" />
      <el-table-column prop="orgName" label="归属组织" min-width="140" />
      <el-table-column prop="postName" label="归属岗位" min-width="120" />
      <el-table-column label="角色名称" min-width="110">
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
        min-width="70"
        #default="{ row }"
      >
        <el-tag :type="statusTagType(row.status)">
          {{ statusText(row.status) }}
        </el-tag>
      </el-table-column>
      <el-table-column
        prop="createdTime"
        label="创建时间"
        min-width="140"
        :formatter="formatDateTime"
      />

      <el-table-column label="操作" fixed="right" width="110">
        <template #default="{ row }">
          <el-button-group>
            <el-button
                link
                type="primary"
                size="small"
                @click="handleDetail(row)"
                :disabled="!canView"
            >
              详细
            </el-button>

            <el-dropdown
                trigger="click"
                :disabled="!canManage"
                @command="(status: PersonStatus) => changeStatus(row, status)"
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
                  <el-dropdown-item command="DISABLED">禁用</el-dropdown-item>
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

    <!-- 人员详细侧边栏 -->
    <el-drawer
      v-model="detailDrawer.visible"
      title="人员详细信息"
      direction="rtl"
      size="420px"
      :close-on-click-modal="false"
      destroy-on-close
      custom-class="person-detail-drawer"
    >
      <div v-if="detailDrawer.person" class="person-detail">
        <!-- 基础信息 -->
        <el-card class="info-card" shadow="never" style="flex-shrink: 0;">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              基础信息
            </div>
          </template>

          <div class="basic-info">
            <!-- 个人信息头部 -->
            <div class="person-header">
              <div class="person-name">{{ detailDrawer.person.personName }}</div>
              <div class="person-code">{{ detailDrawer.person.personCode }}</div>
            </div>

            <!-- 联系信息 -->
            <div class="contact-section">
              <div class="contact-item">
                <el-icon><Message /></el-icon>
                <span>{{ detailDrawer.person.email || '未设置' }}</span>
              </div>
              <div class="contact-item">
                <el-icon><Phone /></el-icon>
                <span>{{ detailDrawer.person.mobile || '未设置' }}</span>
              </div>
            </div>

            <!-- 标签信息 -->
            <div class="tag-section">
              <el-tag :type="getGenderTagType(detailDrawer.person.gender)">
                {{ getGenderText(detailDrawer.person.gender) }}
              </el-tag>
              <el-tag :type="getStatusTagType(detailDrawer.person.status)">
                {{ getStatusText(detailDrawer.person.status) }}
              </el-tag>
              <el-tag>{{ detailDrawer.person.isExternal === 0 ? '内部' : '外部' }}</el-tag>
              <el-tag type="info" v-if="detailDrawer.person.entryDate">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(detailDrawer.person.entryDate) }}
              </el-tag>
            </div>

            <!-- 组织岗位信息 -->
            <div class="org-section">
              <div class="org-item">
                <el-icon><OfficeBuilding /></el-icon>
                <div class="org-content">
                  <div class="org-label">主组织</div>
                  <div class="org-value">{{ detailDrawer.person.orgName || '未分配' }}</div>
                </div>
              </div>
              <div class="org-item">
                <el-icon><Management /></el-icon>
                <div class="org-content">
                  <div class="org-label">主岗位</div>
                  <div class="org-value">{{ detailDrawer.person.postName || '未分配' }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 兼职岗位 -->
        <el-card class="info-card" shadow="never" style="flex: 1;">
          <template #header>
            <div class="card-header">
              <el-icon><Briefcase /></el-icon>
              兼职岗位
            </div>
          </template>

          <div class="parttime-posts">
            <div class="post-row">
              <div class="selector-group">
                <label class="selector-label">组织：</label>
                <OrgSelector
                  v-model="selectedPartTimeOrg1"
                  placeholder="请选择组织"
                  @change="handlePartTimeOrgChange(1)"
                  style="flex: 1;"
                />
              </div>
              <div class="selector-group">
                <label class="selector-label">岗位：</label>
                <el-select
                  v-model="selectedPartTimePost1"
                  placeholder="请先选择组织"
                  :disabled="!selectedPartTimeOrg1"
                  style="flex: 1;"
                  @change="handlePartTimePostChange(1)"
                >
                  <el-option
                    v-for="post in partTimePosts1"
                    :key="post.postId"
                    :label="post.postName"
                    :value="post.postId"
                  />
                </el-select>
              </div>
            </div>

            <div class="post-row">
              <div class="selector-group">
                <label class="selector-label">组织：</label>
                <OrgSelector
                  v-model="selectedPartTimeOrg2"
                  placeholder="请选择组织"
                  @change="handlePartTimeOrgChange(2)"
                  style="flex: 1;"
                />
              </div>
              <div class="selector-group">
                <label class="selector-label">岗位：</label>
                <el-select
                  v-model="selectedPartTimePost2"
                  placeholder="请先选择组织"
                  :disabled="!selectedPartTimeOrg2"
                  style="flex: 1;"
                  @change="handlePartTimePostChange(2)"
                >
                  <el-option
                    v-for="post in partTimePosts2"
                    :key="post.postId"
                    :label="post.postName"
                    :value="post.postId"
                  />
                </el-select>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 操作按钮 -->
        <div class="drawer-footer" style="flex-shrink: 0;">
          <el-button @click="detailDrawer.visible = false">取消</el-button>
          <el-button type="primary" :loading="savingPartTime" @click="savePartTimePosts">
            保存修改
          </el-button>
        </div>
      </div>
    </el-drawer>

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
import {
  ArrowDown,
  User,
  Message,
  Phone,
  Calendar,
  OfficeBuilding,
  Management,
  Briefcase
} from '@element-plus/icons-vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import {
  queryPersonList,
  createPerson,
  updatePerson,
  deletePerson,
  changePersonStatus,
  batchChangePersonStatus,
  updatePersonPartTimePosts,
  type ExpPersonVO,
  type PersonStatus,
  type PartTimePost,
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

// 详细侧边栏
const detailDrawer = reactive({
  visible: false,
  person: null as ExpPersonVO | null,
});

// 兼职岗位相关
const savingPartTime = ref(false);
const selectedPartTimeOrg1 = ref<OrgNode>();
const selectedPartTimeOrg2 = ref<OrgNode>();
const selectedPartTimePost1 = ref<number>();
const selectedPartTimePost2 = ref<number>();
const partTimePosts1 = ref<PostVO[]>([]);
const partTimePosts2 = ref<PostVO[]>([]);

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const form = reactive<ExpPersonVO>({
  personId: 0,
  personCode: '',
  personName: '',
  gender: undefined,
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
const canView = computed(() => hasPermission('system:user:view'));

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
      includeChildren: false, // 不包括子组织
    });

    const posts = res || [];
    // 添加“待定”选项，但要去重
    const hasPending = posts.some(post => post.postName === '待定');
    const options = hasPending ? posts : [{ postId: -1, postName: '待定', postCode: 'PENDING', status: 'ENABLED' as const }, ...posts];

    postOptions.value = options;
  } catch (e) {
    console.error('获取岗位列表失败:', e);
    postOptions.value = [{ postId: -1, postName: '待定', postCode: 'PENDING', status: 'ENABLED' }];
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

// 详细功能
function handleDetail(row: ExpPersonVO) {
  detailDrawer.person = row;
  // 初始化兼职岗位数据
  selectedPartTimeOrg1.value = row.partTimeOrgId1 ? {
    orgId: row.partTimeOrgId1,
    orgName: row.partTimeOrgName1 || '',
    orgCode: '',
    children: []
  } : undefined;
  selectedPartTimePost1.value = row.partTimePostId1;

  selectedPartTimeOrg2.value = row.partTimeOrgId2 ? {
    orgId: row.partTimeOrgId2,
    orgName: row.partTimeOrgName2 || '',
    orgCode: '',
    children: []
  } : undefined;
  selectedPartTimePost2.value = row.partTimePostId2;

  // 如果已有组织，加载对应的岗位列表
  if (selectedPartTimeOrg1.value) {
    loadPartTimePosts(1, selectedPartTimeOrg1.value.orgId);
  }
  if (selectedPartTimeOrg2.value) {
    loadPartTimePosts(2, selectedPartTimeOrg2.value.orgId);
  }

  detailDrawer.visible = true;
}

// 兼职岗位组织选择处理
async function handlePartTimeOrgChange(index: 1 | 2, org?: OrgNode) {
  if (index === 1) {
    selectedPartTimePost1.value = undefined;
    if (org?.orgId) {
      await loadPartTimePosts(1, org.orgId);
    } else {
      partTimePosts1.value = [];
    }
  } else {
    selectedPartTimePost2.value = undefined;
    if (org?.orgId) {
      await loadPartTimePosts(2, org.orgId);
    } else {
      partTimePosts2.value = [];
    }
  }
}

// 兼职岗位选择处理
function handlePartTimePostChange(index: 1 | 2, postId?: number) {
  // 这里可以添加一些业务逻辑验证
  console.log(`兼职岗位${index}选择:`, postId);
}

// 加载兼职岗位列表
async function loadPartTimePosts(index: 1 | 2, orgId: number) {
  try {
    const res = await queryOrgPosts({
      orgId,
      includeChildren: false,
    });

    const posts = res || [];
    // 添加"待定"选项
    const hasPending = posts.some(post => post.postName === '待定');
    const options = hasPending ? posts : [{ postId: -1, postName: '待定', postCode: 'PENDING', status: 'ENABLED' as const }, ...posts];

    if (index === 1) {
      partTimePosts1.value = options;
    } else {
      partTimePosts2.value = options;
    }
  } catch (e) {
    console.error('获取兼职岗位列表失败:', e);
    if (index === 1) {
      partTimePosts1.value = [{ postId: -1, postName: '待定', postCode: 'PENDING', status: 'ENABLED' }];
    } else {
      partTimePosts2.value = [{ postId: -1, postName: '待定', postCode: 'PENDING', status: 'ENABLED' }];
    }
  }
}

// 保存兼职岗位
async function savePartTimePosts() {
  if (!detailDrawer.person) return;

  savingPartTime.value = true;
  try {
    // 构建兼职岗位数据
    const partTimePosts: any[] = [];

    if (selectedPartTimeOrg1.value && selectedPartTimePost1.value) {
      partTimePosts.push({
        orgId: selectedPartTimeOrg1.value.orgId,
        orgName: selectedPartTimeOrg1.value.orgName,
        postId: selectedPartTimePost1.value,
        postName: partTimePosts1.value.find(p => p.postId === selectedPartTimePost1.value)?.postName || '',
      });
    }

    if (selectedPartTimeOrg2.value && selectedPartTimePost2.value) {
      partTimePosts.push({
        orgId: selectedPartTimeOrg2.value.orgId,
        orgName: selectedPartTimeOrg2.value.orgName,
        postId: selectedPartTimePost2.value,
        postName: partTimePosts2.value.find(p => p.postId === selectedPartTimePost2.value)?.postName || '',
      });
    }

    // 调用API更新兼职岗位
    await updatePersonPartTimePosts(detailDrawer.person.personId, partTimePosts);

    ElMessage.success('兼职岗位保存成功');
    detailDrawer.visible = false;

    // 刷新列表数据
    fetchList();
  } catch (error) {
    ElMessage.error('保存失败');
  } finally {
    savingPartTime.value = false;
  }
}

// 工具函数
function getGenderTagType(gender?: string) {
  return gender === 'M' ? 'primary' : gender === 'F' ? 'danger' : '';
}

function getGenderText(gender?: string) {
  return gender === 'M' ? '男' : gender === 'F' ? '女' : '未知';
}

function getStatusTagType(status?: string) {
  if (status === 'ONJOB') return 'success';
  if (status === 'DISABLED') return 'info';
  if (status === 'LEAVE') return 'warning';
  return '';
}

function getStatusText(status?: string) {
  return status === 'ONJOB' ? '在职' : status === 'DISABLED' ? '禁用' : status === 'LEAVE' ? '离职' : '未知';
}

// --- 开发期测试填充：后期不用时，可注释调用处关闭 ---
function generateMockMobile() {
  const prefixes = ['130', '131', '132', '133', '135', '136', '137', '138', '139', '150', '151', '152', '157', '158', '159', '170', '178', '182', '183', '184', '187', '188', '189', '198', '199'];
  const prefix = prefixes[Math.floor(Math.random() * prefixes.length)];
  const tail = Math.floor(Math.random() * 10_000_000).toString().padStart(7, '0');
  return `${prefix}${tail}`;
}

function generateMockIdCard() {
  // 固定行政区代码 + 随机生日 + 顺序码 + 校验位
  const areaCode = '110101'; // 北京市东城区
  const start = new Date(1980, 0, 1).getTime();
  const end = new Date(2000, 11, 31).getTime();
  const birthTime = start + Math.random() * (end - start);
  const birth = new Date(birthTime);
  const y = birth.getFullYear();
  const m = String(birth.getMonth() + 1).padStart(2, '0');
  const d = String(birth.getDate()).padStart(2, '0');
  const birthStr = `${y}${m}${d}`;
  const seq = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
  const base17 = `${areaCode}${birthStr}${seq}`;

  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
  const checks = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];
  const sum = base17.split('').reduce((acc, cur, idx) => acc + Number(cur) * weights[idx], 0);
  const checkCode = checks[sum % 11];

  return `${base17}${checkCode}`;
}

function fillDevMockFields() {
  // 测试阶段自动填充，后期不需要时注释掉本函数调用
  form.mobile = generateMockMobile();
  form.idCardNo = generateMockIdCard();
}
// --- 开发期测试填充结束 ---

function formatDate(dateStr?: string) {
  if (!dateStr) return '';
  try {
    return new Date(dateStr).toLocaleDateString('zh-CN');
  } catch {
    return dateStr;
  }
}

function resetFormModel() {
  form.personId = 0;
  form.personCode = generatePersonCode();
  form.personName = '';
  form.gender = undefined;
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

  fillDevMockFields(); // 测试期自动填充手机号/身份证，后期可注释
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

// 人员详细侧边栏样式
:deep(.person-detail-drawer) {
  border-radius: 12px 0 0 12px;
  overflow: hidden;
  top: 12.5% !important;
  height: 75% !important;
}

.person-detail {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.info-card {
  margin-bottom: 16px;
  border-radius: 8px;

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: #303133;
  }

  :deep(.el-card__header) {
    padding: 12px 16px;
    border-bottom: 1px solid #ebeef5;
  }

  :deep(.el-card__body) {
    padding: 16px;
  }
}

.basic-info {
  .person-header {
    text-align: center;
    margin-bottom: 16px;

    .person-name {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }

    .person-code {
      color: #909399;
      font-size: 14px;
    }
  }

  .contact-section {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;

    .contact-item {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #606266;
      font-size: 14px;

      .el-icon {
        color: #c0c4cc;
        font-size: 16px;
      }
    }
  }

  .tag-section {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
    margin-bottom: 16px;
  }

  .org-section {
    .org-item {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .el-icon {
        color: #c0c4cc;
        font-size: 16px;
        margin-top: 2px;
      }

      .org-content {
        flex: 1;

        .org-label {
          font-size: 12px;
          color: #909399;
          margin-bottom: 2px;
        }

        .org-value {
          font-size: 14px;
          color: #606266;
          font-weight: 500;
        }
      }
    }
  }
}

.parttime-posts {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .post-row {
    display: flex;
    gap: 12px;
    align-items: center;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }

    .selector-group {
      display: flex;
      align-items: center;
      gap: 6px;
      flex: 0.5;

      .selector-label {
        min-width: 35px;
        color: #606266;
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
}

.drawer-footer {
  margin-top: auto;
  padding: 16px 0;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
