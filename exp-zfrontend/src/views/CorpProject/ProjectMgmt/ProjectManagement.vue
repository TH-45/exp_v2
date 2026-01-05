<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">工程项目管理</div>
          <div class="actions">
            <el-button
              type="primary"
              size="small"
              @click="openCreateDialog"
              :disabled="!canCreate"
            >
              <el-icon><Plus /></el-icon>
              新建项目
            </el-button>
            <el-button size="small" @click="exportProjects">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 查询区 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="项目名称">
          <el-input
            v-model="query.keyword"
            placeholder="项目名称关键词"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="规划中" value="PLANNING" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已暂停" value="SUSPENDED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="query.manager" placeholder="负责人姓名" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="项目周期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
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
        row-key="id"
        border
        style="width: 100%"
        :default-sort="{prop: 'createTime', order: 'descending'}"
      >
        <el-table-column prop="name" label="项目名称" min-width="200">
          <template #default="{ row }">
            <div class="project-name">
              <div class="name-text">{{ row.name }}</div>
              <div class="code-text">编码：{{ row.code }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="manager" label="项目负责人" min-width="120" />
        <el-table-column label="项目周期" min-width="180">
          <template #default="{ row }">
            <div class="project-period">
              <div>{{ row.startDate }} 至</div>
              <div>{{ row.plannedEndDate }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="进度" min-width="120">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress
                :percentage="row.progress"
                :status="getProgressStatus(row.status, row.progress)"
                :stroke-width="8"
                :show-text="false"
              />
              <span class="progress-text">{{ row.progress }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="budget" label="项目预算" min-width="120">
          <template #default="{ row }">
            <span class="budget-amount">¥{{ formatAmount(row.budget) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-space size="small">
              <el-button link type="primary" size="small" @click="openDetail(row)" :disabled="!canView">
                详情
              </el-button>
              <el-button link type="primary" size="small" @click="openEditDialog(row)" :disabled="!canUpdate">
                编辑
              </el-button>
              <el-button
                link
                type="success"
                size="small"
                @click="goToProjectMembers(row)"
                :disabled="!canView"
              >
                成员
              </el-button>
              <el-button
                link
                type="info"
                size="small"
                @click="goToProjectProgress(row)"
                :disabled="!canView"
              >
                进度
              </el-button>
              <el-button
                link
                type="warning"
                size="small"
                @click="goToProjectMaterials(row)"
                :disabled="!canView"
              >
                物料
              </el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)" :disabled="!canDelete">
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

      <!-- 新增/编辑项目弹窗 -->
      <el-dialog
        v-model="editDialog.visible"
        :title="editDialog.isEdit ? '编辑项目' : '新建项目'"
        width="900px"
        destroy-on-close
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          class="dialog-form"
        >
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="项目名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入项目名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="项目编码" prop="code">
                <el-input v-model="form.code" placeholder="请输入项目编码" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="项目负责人" prop="managerId">
                <el-select v-model="form.managerId" placeholder="选择项目负责人" style="width: 100%">
                  <el-option
                    v-for="user in availableUsers"
                    :key="user.id"
                    :label="user.name"
                    :value="user.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="负责部门" prop="department">
                <el-input v-model="form.department" placeholder="请输入负责部门" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="开始日期" prop="startDate">
                <el-date-picker
                  v-model="form.startDate"
                  type="date"
                  placeholder="选择开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划完成日期" prop="plannedEndDate">
                <el-date-picker
                  v-model="form.plannedEndDate"
                  type="date"
                  placeholder="选择计划完成日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="项目预算(万元)" prop="budget">
                <el-input-number
                  v-model="form.budget"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="合同金额(万元)">
                <el-input-number
                  v-model="form.contractAmount"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="客户名称">
                <el-input v-model="form.clientName" placeholder="请输入客户名称" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="项目地址" prop="address">
                <el-input v-model="form.address" placeholder="请输入项目地址" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="项目状态" prop="status">
                <el-select v-model="form.status" placeholder="选择项目状态" style="width: 100%">
                  <el-option label="规划中" value="PLANNING" />
                  <el-option label="进行中" value="ONGOING" />
                  <el-option label="已完成" value="COMPLETED" />
                  <el-option label="已暂停" value="SUSPENDED" />
                  <el-option label="已取消" value="CANCELLED" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="项目描述">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请输入项目描述（可选）"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">
            {{ editDialog.isEdit ? '保存' : '创建' }}
          </el-button>
        </template>
      </el-dialog>

      <!-- 项目详情抽屉 -->
      <el-drawer v-model="detailDrawer.visible" title="项目详情" size="600px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目ID" :span="2">{{ detailDrawer.data?.id }}</el-descriptions-item>
          <el-descriptions-item label="项目名称" :span="2">{{ detailDrawer.data?.name }}</el-descriptions-item>
          <el-descriptions-item label="项目编码">{{ detailDrawer.data?.code }}</el-descriptions-item>
          <el-descriptions-item label="项目状态">
            <el-tag :type="getStatusTagType(detailDrawer.data?.status)">
              {{ getStatusLabel(detailDrawer.data?.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="项目负责人">{{ detailDrawer.data?.manager }}</el-descriptions-item>
          <el-descriptions-item label="负责部门">{{ detailDrawer.data?.department }}</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ detailDrawer.data?.startDate }}</el-descriptions-item>
          <el-descriptions-item label="计划完成日期">{{ detailDrawer.data?.plannedEndDate }}</el-descriptions-item>
          <el-descriptions-item label="项目进度">{{ detailDrawer.data?.progress }}%</el-descriptions-item>
          <el-descriptions-item label="项目预算">¥{{ formatAmount(detailDrawer.data?.budget || 0) }}</el-descriptions-item>
          <el-descriptions-item label="合同金额">¥{{ formatAmount(detailDrawer.data?.contractAmount || 0) }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ detailDrawer.data?.clientName || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="项目地址" :span="2">{{ detailDrawer.data?.address }}</el-descriptions-item>
          <el-descriptions-item label="项目描述" :span="2">{{ detailDrawer.data?.description || '暂无描述' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ detailDrawer.data?.createTime }}</el-descriptions-item>
        </el-descriptions>
      </el-drawer>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, Download } from '@element-plus/icons-vue';
import { hasPermission } from '@/utils/permission';
import {
  listProjects,
  getProjectDetail,
  createProject,
  updateProject,
  deleteProject,
  type ProjectVO
} from '@/api/project';

const router = useRouter();
const loading = ref(false);
const saving = ref(false);

const query = reactive({
  keyword: '',
  status: undefined as string | undefined,
  manager: '',
  page: 1,
  pageSize: 10,
});

const dateRange = ref<string[]>([]);
const tableData = ref<ProjectVO[]>([]);
const total = ref(0);

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const form = reactive({
  id: '',
  name: '',
  code: '',
  description: '',
  managerId: '',
  department: '',
  startDate: '',
  plannedEndDate: '',
  budget: 0,
  address: '',
  clientName: '',
  contractAmount: 0,
  status: 'PLANNING' as string,
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入项目编码', trigger: 'blur' }],
  managerId: [{ required: true, message: '请选择项目负责人', trigger: 'change' }],
  department: [{ required: true, message: '请输入负责部门', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  plannedEndDate: [{ required: true, message: '请选择计划完成日期', trigger: 'change' }],
  budget: [{ required: true, message: '请输入项目预算', trigger: 'blur' }],
  address: [{ required: true, message: '请输入项目地址', trigger: 'blur' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }],
};

const detailDrawer = reactive({
  visible: false,
  data: null as ProjectVO | null,
});

// 模拟用户数据（实际应该从API获取）
const availableUsers = ref([
  { id: 'user001', name: '张三' },
  { id: 'user002', name: '李四' },
  { id: 'user003', name: '王五' },
  { id: 'user004', name: '赵六' },
]);

// 权限点
const canView = computed(() => hasPermission('project:project:view'));
const canCreate = computed(() => hasPermission('project:project:create'));
const canUpdate = computed(() => hasPermission('project:project:update'));
const canDelete = computed(() => hasPermission('project:project:delete'));

// 模拟数据
const mockProjects: ProjectVO[] = [
  {
    id: 'p001',
    name: '某某大厦项目',
    code: 'XMDS2024001',
    description: '某某市中心商务大厦建设项目',
    manager: '张三',
    managerId: 'user001',
    department: '工程部',
    startDate: '2024-12-01',
    plannedEndDate: '2025-11-30',
    status: 'ONGOING',
    progress: 75,
    budget: 5000,
    actualCost: 3200,
    address: '某某市中心区',
    clientName: '某某地产有限公司',
    contractAmount: 4800,
    createTime: '2024-11-01 10:00:00',
    updateTime: '2025-01-05 14:30:00',
  },
  {
    id: 'p002',
    name: '商业广场项目',
    code: 'SYGC2024002',
    description: '某某商业广场扩建工程',
    manager: '李四',
    managerId: 'user002',
    department: '工程部',
    startDate: '2024-10-15',
    plannedEndDate: '2025-09-30',
    status: 'ONGOING',
    progress: 45,
    budget: 8000,
    actualCost: 3600,
    address: '某某市商业区',
    clientName: '某某商业集团',
    contractAmount: 7500,
    createTime: '2024-09-15 09:00:00',
    updateTime: '2025-01-03 16:20:00',
  },
  {
    id: 'p003',
    name: '住宅小区项目',
    code: 'ZZXQ2024003',
    description: '某某花园住宅小区建设',
    manager: '王五',
    managerId: 'user003',
    department: '工程部',
    startDate: '2024-08-01',
    plannedEndDate: '2025-07-31',
    status: 'COMPLETED',
    progress: 100,
    budget: 3000,
    actualCost: 2950,
    address: '某某市新区',
    clientName: '某某房地产开发有限公司',
    contractAmount: 2900,
    createTime: '2024-07-01 08:30:00',
    updateTime: '2025-01-01 17:45:00',
  },
];

function getStatusLabel(status?: string) {
  const labels = {
    PLANNING: '规划中',
    ONGOING: '进行中',
    COMPLETED: '已完成',
    SUSPENDED: '已暂停',
    CANCELLED: '已取消',
  };
  return labels[status as keyof typeof labels] || status;
}

function getStatusTagType(status?: string) {
  const types = {
    PLANNING: 'info',
    ONGOING: 'primary',
    COMPLETED: 'success',
    SUSPENDED: 'warning',
    CANCELLED: 'danger',
  };
  return types[status as keyof typeof types] || 'info';
}

function getProgressStatus(projectStatus: string, progress: number) {
  if (projectStatus === 'COMPLETED') return 'success';
  if (projectStatus === 'SUSPENDED') return 'warning';
  if (projectStatus === 'CANCELLED') return 'exception';
  if (progress >= 100) return 'success';
  return undefined;
}

function formatAmount(amount: number) {
  return new Intl.NumberFormat('zh-CN').format(amount);
}

async function fetchList() {
  // 更新日期查询条件
  if (dateRange.value && dateRange.value.length === 2) {
    query.startDate = dateRange.value[0];
    query.endDate = dateRange.value[1];
  }

  loading.value = true;
  try {
    const res = await listProjects(query);
    const list = (res.records || res.list || res.rows || []) as ProjectVO[];
    tableData.value = list.length ? list : mockProjects;
    total.value = Number(res.total ?? tableData.value.length) || 0;
  } catch (e) {
    tableData.value = mockProjects;
    total.value = mockProjects.length;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.keyword = '';
  query.status = undefined;
  query.manager = '';
  dateRange.value = [];
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

function openCreateDialog() {
  editDialog.isEdit = false;
  editDialog.visible = true;
  resetForm();
}

function openEditDialog(row: ProjectVO) {
  editDialog.isEdit = true;
  editDialog.visible = true;
  Object.assign(form, row);
  form.managerId = row.managerId;
}

function resetForm() {
  form.id = '';
  form.name = '';
  form.code = '';
  form.description = '';
  form.managerId = '';
  form.department = '';
  form.startDate = '';
  form.plannedEndDate = '';
  form.budget = 0;
  form.address = '';
  form.clientName = '';
  form.contractAmount = 0;
  form.status = 'PLANNING';
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;

  saving.value = true;
  try {
    if (editDialog.isEdit) {
      await updateProject(form.id, form);
      ElMessage.success('编辑成功');
    } else {
      await createProject(form);
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

function openDetail(row: ProjectVO) {
  detailDrawer.visible = true;
  detailDrawer.data = row;
}

function goToProjectMembers(row: ProjectVO) {
  router.push(`/corp-project/project-mgmt/members/${row.id}`);
}

function goToProjectProgress(row: ProjectVO) {
  router.push(`/corp-project/project-mgmt/progress/${row.id}`);
}

function goToProjectMaterials(row: ProjectVO) {
  router.push(`/corp-project/project-mgmt/materials/${row.id}`);
}

function handleDelete(row: ProjectVO) {
  ElMessageBox.confirm(`确认删除项目「${row.name}」吗？此操作不可恢复。`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteProject(row.id);
        ElMessage.success('删除成功');
        fetchList();
      } catch (e) {
        tableData.value = tableData.value.filter((r) => r.id !== row.id);
        total.value = tableData.value.length;
        ElMessage.success('删除成功（演示模式）');
      }
    })
    .catch(() => {});
}

function exportProjects() {
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

.project-name {
  .name-text {
    font-weight: 500;
    margin-bottom: 4px;
  }

  .code-text {
    font-size: 12px;
    color: #909399;
  }
}

.project-period {
  font-size: 12px;
  line-height: 1.4;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 8px;

  .progress-text {
    font-size: 12px;
    font-weight: 500;
    min-width: 35px;
  }
}

.budget-amount {
  font-weight: 500;
  color: #E6A23C;
}

.dialog-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}
</style>
