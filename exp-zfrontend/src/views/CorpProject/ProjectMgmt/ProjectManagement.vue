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
        @row-dblclick="handleRowDblClick"
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
        draggable
        destroy-on-close
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          class="dialog-form"
          @submit.prevent="submitForm"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
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
                    :key="user.personId"
                    :label="user.personName"
                    :value="user.personId"
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
              @keydown.enter.stop
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
import { getMenuLevel } from '@/utils/permission';
import {
  listProject,
  getProjectDetail,
  createProject,
  updateProject,
  deleteProject,
  type ProjectVO,
  type ProjectCreateDTO,
  type ProjectUpdateDTO,
} from '@/api/corpProject/project';
import { queryPersonList, type ExpPersonVO } from '@/api/system/person';

const router = useRouter();
const loading = ref(false);
const saving = ref(false);

type ProjectTableRow = {
  id: number;
  name: string;
  code: string;
  description?: string;
  manager: string;
  managerId?: number;
  department: string;
  startDate: string;
  plannedEndDate: string;
  status: string;
  progress: number;
  budget: number;
  address: string;
  clientName?: string;
  contractAmount?: number;
  createTime: string;
  updateTime: string;
};

const query = reactive({
  keyword: '',
  status: undefined as string | undefined,
  manager: '',
  page: 1,
  pageSize: 10,
});

const dateRange = ref<string[]>([]);
const tableData = ref<ProjectTableRow[]>([]);
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
  managerId: undefined as number | undefined,
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
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  plannedEndDate: [{ required: true, message: '请选择计划完成日期', trigger: 'change' }],
  budget: [{ required: true, message: '请输入项目预算', trigger: 'blur' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }],
};

const detailDrawer = reactive({
  visible: false,
  data: null as ProjectTableRow | null,
});

const availableUsers = ref<ExpPersonVO[]>([]);

// 权限点
const canView = computed(() => getMenuLevel('project:management') >= 1);
const canCreate = computed(() => getMenuLevel('project:management') >= 2);
const canUpdate = computed(() => getMenuLevel('project:management') >= 2);
const canDelete = computed(() => getMenuLevel('project:management') >= 3);

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

function mapToTableRow(item: ProjectVO): ProjectTableRow {
  return {
    id: Number(item.projectId || 0),
    name: item.projectName || '',
    code: item.projectCode || '',
    description: item.remark || '',
    manager: item.managerName || '',
    managerId: item.managerPersonId,
    department: String(item.orgId || ''),
    startDate: item.startDate || '',
    plannedEndDate: item.planEndDate || '',
    status: item.projectStatus || 'PLANNING',
    progress: 0,
    budget: Number(item.budgetAmount || 0),
    address: '',
    clientName: '',
    contractAmount: 0,
    createTime: item.createdTime || '',
    updateTime: item.updatedTime || '',
  };
}

function mapToCreateReq(): ProjectCreateDTO {
  return {
    projectCode: form.code,
    projectName: form.name,
    projectStatus: form.status,
    managerPersonId: form.managerId,
    startDate: form.startDate,
    planEndDate: form.plannedEndDate,
    budgetAmount: form.budget,
    remark: form.description,
  };
}

function mapToUpdateReq(): ProjectUpdateDTO {
  return {
    projectId: Number(form.id),
    projectCode: form.code,
    projectName: form.name,
    projectStatus: form.status,
    managerPersonId: form.managerId,
    startDate: form.startDate,
    planEndDate: form.plannedEndDate,
    budgetAmount: form.budget,
    remark: form.description,
  };
}

async function loadAvailableUsers() {
  try {
    const res = await queryPersonList({ pageNum: 1, pageSize: 500 });
    availableUsers.value = res.list || [];
  } catch (e) {
    availableUsers.value = [];
  }
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await listProject({
      pageNum: query.page,
      pageSize: query.pageSize,
      projectName: query.keyword || undefined,
      projectStatus: query.status || undefined,
      managerName: query.manager || undefined,
      startDateFrom: dateRange.value?.[0] || undefined,
      startDateTo: dateRange.value?.[1] || undefined,
    });
    const list = (res.list || []) as ProjectVO[];
    tableData.value = list.map(mapToTableRow);
    total.value = Number(res.total ?? 0);
  } catch (e) {
    tableData.value = [];
    total.value = 0;
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

function openEditDialog(row: ProjectTableRow) {
  editDialog.isEdit = true;
  editDialog.visible = true;
  form.id = String(row.id);
  form.name = row.name;
  form.code = row.code;
  form.description = row.description || '';
  form.managerId = row.managerId;
  form.department = row.department || '';
  form.startDate = row.startDate;
  form.plannedEndDate = row.plannedEndDate;
  form.budget = row.budget || 0;
  form.address = row.address || '';
  form.clientName = row.clientName || '';
  form.contractAmount = Number(row.contractAmount || 0);
  form.status = row.status || 'PLANNING';
}

function resetForm() {
  form.id = '';
  form.name = '';
  form.code = '';
  form.description = '';
  form.managerId = undefined;
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
      await updateProject(mapToUpdateReq());
      ElMessage.success('编辑成功');
    } else {
      await createProject(mapToCreateReq());
      ElMessage.success('创建成功');
    }
    editDialog.visible = false;
    fetchList();
  } catch (e) {
    ElMessage.error(editDialog.isEdit ? '编辑失败' : '创建失败');
  } finally {
    saving.value = false;
  }
}

async function openDetail(row: ProjectTableRow) {
  detailDrawer.visible = true;
  try {
    const detail = await getProjectDetail(row.id);
    detailDrawer.data = mapToTableRow(detail);
  } catch (e) {
    detailDrawer.data = row;
  }
}

function goToProjectMembers(row: ProjectTableRow) {
  router.push(`/corp-project/project-mgmt/members/${row.id}`);
}

function goToProjectProgress(row: ProjectTableRow) {
  router.push(`/corp-project/project-mgmt/progress/${row.id}`);
}

function goToProjectMaterials(row: ProjectTableRow) {
  router.push(`/corp-project/project-mgmt/materials/${row.id}`);
}

function handleDelete(row: ProjectTableRow) {
  ElMessageBox.confirm(`确认删除项目「${row.name}」吗？此操作不可恢复。`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteProject(row.id);
        ElMessage.success('删除成功');
        fetchList();
      } catch (e) {
        ElMessage.error('删除失败');
      }
    })
    .catch(() => {});
}

function handleRowDblClick(row: ProjectTableRow) {
  if (canUpdate.value) {
    openEditDialog(row);
    return;
  }
  if (canView.value) {
    openDetail(row);
  }
}

function exportProjects() {
  ElMessage.info('导出功能开发中...');
}

onMounted(() => {
  loadAvailableUsers();
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
