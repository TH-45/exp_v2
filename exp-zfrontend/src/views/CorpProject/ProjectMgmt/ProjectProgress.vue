<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">项目进度管理 - {{ projectName }}</div>
          <div class="actions">
            <el-button
              type="primary"
              size="small"
              @click="openAddMilestoneDialog"
              :disabled="!canUpdate"
            >
              <el-icon><Plus /></el-icon>
              添加里程碑
            </el-button>
            <el-button size="small" @click="exportProgress">
              <el-icon><Download /></el-icon>
              导出进度
            </el-button>
          </div>
        </div>
      </template>

      <!-- 进度概览 -->
      <div class="progress-overview">
        <el-row :gutter="16">
          <el-col :span="6">
            <div class="progress-card">
              <el-progress
                type="circle"
                :percentage="progressData.overallProgress"
                :width="80"
                :stroke-width="8"
                color="#409EFF"
              />
              <div class="progress-info">
                <div class="progress-label">总体进度</div>
                <div class="progress-value">{{ progressData.overallProgress }}%</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ progressData.totalMilestones }}</div>
              <div class="stat-label">总里程碑</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ progressData.completedMilestones }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card warning">
              <div class="stat-number">{{ progressData.delayedMilestones }}</div>
              <div class="stat-label">延期里程碑</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 甘特图/进度图表 -->
      <div class="progress-chart">
        <h3>项目进度甘特图</h3>
        <div class="chart-container">
          <div class="gantt-chart">
            <div class="gantt-header">
              <div class="task-name">里程碑任务</div>
              <div class="timeline">
                <div
                  v-for="month in timelineMonths"
                  :key="month"
                  class="month-block"
                >
                  {{ month }}
                </div>
              </div>
            </div>
            <div class="gantt-body">
              <div
                v-for="milestone in progressData.milestones"
                :key="milestone.id"
                class="gantt-row"
              >
                <div class="task-info">
                  <div class="task-title">{{ milestone.name }}</div>
                  <div class="task-status">
                    <el-tag size="small" :type="getMilestoneStatusType(milestone.status)">
                      {{ getMilestoneStatusLabel(milestone.status) }}
                    </el-tag>
                  </div>
                </div>
                <div class="task-timeline">
                  <div
                    class="task-bar"
                    :class="getTaskBarClass(milestone)"
                    :style="getTaskBarStyle(milestone)"
                  >
                    <span class="task-progress">{{ milestone.progress }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 里程碑列表 -->
      <el-table
        v-loading="loading"
        :data="progressData.milestones"
        row-key="id"
        border
        style="width: 100%"
        :default-sort="{prop: 'plannedStartDate', order: 'ascending'}"
        @row-dblclick="handleRowDblClick"
      >
        <el-table-column prop="name" label="里程碑名称" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="计划时间" min-width="180">
          <template #default="{ row }">
            <div class="time-range">
              <div>{{ row.plannedStartDate }}</div>
              <div>至 {{ row.plannedEndDate }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="实际时间" min-width="180">
          <template #default="{ row }">
            <div class="time-range" v-if="row.actualStartDate">
              <div>{{ row.actualStartDate }}</div>
              <div v-if="row.actualEndDate">至 {{ row.actualEndDate }}</div>
            </div>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column label="进度" min-width="120">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress
                :percentage="row.progress"
                :status="getProgressStatus(row.status)"
                :stroke-width="8"
                :show-text="false"
              />
              <span class="progress-text">{{ row.progress }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getMilestoneStatusType(row.status)">
              {{ getMilestoneStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responsiblePerson" label="负责人" min-width="120" />
        <el-table-column label="延期天数" min-width="100">
          <template #default="{ row }">
            <span
              :class="getDelayClass(getDelayDays(row))"
              v-if="getDelayDays(row) !== 0"
            >
              {{ getDelayText(getDelayDays(row)) }}
            </span>
            <span v-else class="on-time">正常</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-space size="small">
              <el-button link type="primary" size="small" @click="editMilestone(row)" :disabled="!canUpdate">
                编辑
              </el-button>
              <el-button
                link
                type="success"
                size="small"
                @click="updateProgress(row)"
                :disabled="!canUpdate || row.status === 'COMPLETED'"
              >
                更新进度
              </el-button>
              <el-button link type="danger" size="small" @click="deleteMilestone(row)" :disabled="!canDelete">
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加/编辑里程碑弹窗 -->
      <el-dialog
        v-model="milestoneDialog.visible"
        :title="milestoneDialog.isEdit ? '编辑里程碑' : '添加里程碑'"
        width="700px"
        draggable
        destroy-on-close
      >
        <el-form
          ref="milestoneFormRef"
          :model="milestoneForm"
          :rules="milestoneRules"
          label-width="120px"
          class="milestone-form"
          @submit.prevent="saveMilestone"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="里程碑名称" prop="name">
                <el-input v-model="milestoneForm.name" placeholder="请输入里程碑名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="负责人" prop="responsiblePersonId">
                <el-select v-model="milestoneForm.responsiblePersonId" placeholder="选择负责人" style="width: 100%">
                  <el-option
                    v-for="member in projectMembers"
                    :key="member.id"
                    :label="member.userName"
                    :value="member.userId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="里程碑描述">
            <el-input
              v-model="milestoneForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入里程碑详细描述（可选）"
              @keydown.enter.stop
            />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="计划开始日期" prop="plannedStartDate">
                <el-date-picker
                  v-model="milestoneForm.plannedStartDate"
                  type="date"
                  placeholder="选择计划开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划结束日期" prop="plannedEndDate">
                <el-date-picker
                  v-model="milestoneForm.plannedEndDate"
                  type="date"
                  placeholder="选择计划结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="前置里程碑">
            <el-select v-model="milestoneForm.predecessorMilestoneId" placeholder="选择前置里程碑（可选）" clearable style="width: 100%">
              <el-option
                v-for="milestone in availableMilestones"
                :key="milestone.id"
                :label="milestone.name"
                :value="milestone.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="milestoneDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveMilestone">
            {{ milestoneDialog.isEdit ? '保存' : '添加' }}
          </el-button>
        </template>
      </el-dialog>

      <!-- 更新进度弹窗 -->
      <el-dialog v-model="progressDialog.visible" title="更新进度" width="500px" draggable>
        <el-form :model="progressForm" label-width="100px" @submit.prevent="confirmProgressUpdate">
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="当前进度" required>
            <el-slider
              v-model="progressForm.progress"
              :min="0"
              :max="100"
              :step="5"
              show-input
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="实际开始日期">
            <el-date-picker
              v-model="progressForm.actualStartDate"
              type="date"
              placeholder="选择实际开始日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="实际结束日期" v-if="progressForm.progress >= 100">
            <el-date-picker
              v-model="progressForm.actualEndDate"
              type="date"
              placeholder="选择实际结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="备注">
            <el-input
              v-model="progressForm.remarks"
              type="textarea"
              :rows="3"
              placeholder="更新说明（可选）"
              @keydown.enter.stop
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="progressDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="confirmProgressUpdate">
            更新进度
          </el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, Download } from '@element-plus/icons-vue';
import { hasPermission } from '@/utils/permission';
import {
  createProjectMilestone,
  deleteProjectMilestone,
  getProjectProgress,
  updateMilestoneProgress,
  updateProjectMilestone,
  type ProjectMilestoneVO,
  type ProjectProgressVO,
} from '@/api/corpProject/projectProgress';
import { getProjectMembers } from '@/api/corpProject/projectMember';

const route = useRoute();
const loading = ref(false);
const saving = ref(false);

function parseProjectId(raw: unknown): number | null {
  const text = String(raw || '').trim();
  if (!text || !/^\d+$/.test(text)) {
    return null;
  }
  return Number(text);
}

const projectId = ref<number | null>(parseProjectId(route.params.projectId));
const projectName = ref('工程项目');

const progressData = reactive<ProjectProgressVO>({
  projectId: projectId.value || 0,
  overallProgress: 0,
  milestones: [],
  delayedMilestones: 0,
  completedMilestones: 0,
  totalMilestones: 0,
});

const timelineMonths = [
  '2024-12', '2025-01', '2025-02', '2025-03', '2025-04', '2025-05', '2025-06',
];

const projectMembers = ref<Array<{ id: number; userId: number; userName?: string }>>([]);

const milestoneDialog = reactive({
  visible: false,
  isEdit: false,
  editId: null as number | null,
});

const milestoneFormRef = ref<FormInstance>();
const milestoneForm = reactive({
  name: '',
  description: '',
  plannedStartDate: '',
  plannedEndDate: '',
  responsiblePersonId: undefined as number | undefined,
  predecessorMilestoneId: undefined as number | undefined,
});

const milestoneRules: FormRules = {
  name: [{ required: true, message: '请输入里程碑名称', trigger: 'blur' }],
  plannedStartDate: [{ required: true, message: '请选择计划开始日期', trigger: 'change' }],
  plannedEndDate: [{ required: true, message: '请选择计划结束日期', trigger: 'change' }],
  responsiblePersonId: [{ required: true, message: '请选择负责人', trigger: 'change' }],
};

const progressDialog = reactive({
  visible: false,
  milestoneId: null as number | null,
});

const progressForm = reactive({
  progress: 0,
  actualStartDate: '',
  actualEndDate: '',
  remarks: '',
});

const availableMilestones = computed(() =>
  progressData.milestones.filter((m) => m.id !== milestoneDialog.editId),
);

const canUpdate = computed(() => hasPermission('project:progress:update'));
const canDelete = computed(() => hasPermission('project:progress:delete'));

function getMilestoneStatusLabel(status: string) {
  const labels = {
    NOT_STARTED: '未开始',
    ONGOING: '进行中',
    COMPLETED: '已完成',
    DELAYED: '延期',
  };
  return labels[status as keyof typeof labels] || status;
}

function getMilestoneStatusType(status: string) {
  const types = {
    NOT_STARTED: 'info',
    ONGOING: 'primary',
    COMPLETED: 'success',
    DELAYED: 'danger',
  };
  return types[status as keyof typeof types] || 'info';
}

function getProgressStatus(status: string) {
  if (status === 'COMPLETED') return 'success';
  if (status === 'DELAYED') return 'exception';
  return undefined;
}

function getDelayDays(milestone: ProjectMilestoneVO) {
  if (!milestone.actualEndDate && milestone.status === 'COMPLETED') return 0;
  if (milestone.status !== 'COMPLETED') return 0;

  const plannedEnd = new Date(milestone.plannedEndDate);
  const actualEnd = new Date(milestone.actualEndDate!);
  const diffTime = actualEnd.getTime() - plannedEnd.getTime();
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

function getDelayText(days: number) {
  if (days > 0) return `延期${days}天`;
  return '';
}

function getDelayClass(days: number) {
  return days > 0 ? 'delayed' : '';
}

function getTaskBarStyle(milestone: ProjectMilestoneVO) {
  // 简化的甘特图样式计算
  const startDate = new Date(milestone.plannedStartDate);
  const endDate = new Date(milestone.plannedEndDate);
  const totalDays = Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));

  // 假设每个月30天，计算位置
  const startMonth = startDate.getMonth();
  const left = (startMonth - 11) * 100; // 从12月开始
  const width = (totalDays / 30) * 100; // 每月100px

  return {
    left: `${Math.max(0, left)}px`,
    width: `${Math.max(50, width)}px`,
  };
}

function getTaskBarClass(milestone: ProjectMilestoneVO) {
  const classes = ['task-bar'];
  if (milestone.status === 'COMPLETED') classes.push('completed');
  else if (milestone.status === 'ONGOING') classes.push('ongoing');
  else if (milestone.status === 'DELAYED') classes.push('delayed');
  else classes.push('not-started');
  return classes;
}

async function loadProgress() {
  if (!projectId.value) {
    progressData.projectId = 0;
    progressData.overallProgress = 0;
    progressData.milestones = [];
    progressData.delayedMilestones = 0;
    progressData.completedMilestones = 0;
    progressData.totalMilestones = 0;
    return;
  }

  loading.value = true;
  try {
    const res = await getProjectProgress(projectId.value);
    progressData.projectId = res.projectId;
    progressData.overallProgress = Number(res.overallProgress || 0);
    progressData.milestones = res.milestones || [];
    progressData.delayedMilestones = Number(res.delayedMilestones || 0);
    progressData.completedMilestones = Number(res.completedMilestones || 0);
    progressData.totalMilestones = Number(res.totalMilestones || 0);
  } finally {
    loading.value = false;
  }
}

async function loadProjectMembers() {
  if (!projectId.value) {
    projectMembers.value = [];
    return;
  }
  const members = await getProjectMembers(projectId.value);
  projectMembers.value = members.map((m) => ({
    id: m.id,
    userId: m.userId,
    userName: m.userName,
  }));
}

function openAddMilestoneDialog() {
  if (!projectId.value) {
    ElMessage.warning('请先选择项目');
    return;
  }
  milestoneDialog.isEdit = false;
  milestoneDialog.visible = true;
  milestoneDialog.editId = null;
  resetMilestoneForm();
}

function editMilestone(milestone: ProjectMilestoneVO) {
  milestoneDialog.isEdit = true;
  milestoneDialog.visible = true;
  milestoneDialog.editId = milestone.id;
  milestoneForm.name = milestone.name;
  milestoneForm.description = milestone.description || '';
  milestoneForm.plannedStartDate = milestone.plannedStartDate;
  milestoneForm.plannedEndDate = milestone.plannedEndDate;
  milestoneForm.responsiblePersonId = milestone.responsiblePersonId;
  milestoneForm.predecessorMilestoneId = milestone.predecessorMilestoneId;
}

function resetMilestoneForm() {
  milestoneForm.name = '';
  milestoneForm.description = '';
  milestoneForm.plannedStartDate = '';
  milestoneForm.plannedEndDate = '';
  milestoneForm.responsiblePersonId = undefined;
  milestoneForm.predecessorMilestoneId = undefined;
}

async function saveMilestone() {
  if (!milestoneFormRef.value || !projectId.value) return;
  const valid = await milestoneFormRef.value.validate();
  if (!valid) return;

  saving.value = true;
  try {
    if (milestoneDialog.isEdit && milestoneDialog.editId) {
      await updateProjectMilestone({
        id: milestoneDialog.editId,
        name: milestoneForm.name,
        description: milestoneForm.description || undefined,
        plannedStartDate: milestoneForm.plannedStartDate,
        plannedEndDate: milestoneForm.plannedEndDate,
        predecessorMilestoneId: milestoneForm.predecessorMilestoneId,
        responsiblePersonId: Number(milestoneForm.responsiblePersonId),
      });
      ElMessage.success('编辑成功');
    } else {
      await createProjectMilestone({
        projectId: projectId.value,
        name: milestoneForm.name,
        description: milestoneForm.description || undefined,
        plannedStartDate: milestoneForm.plannedStartDate,
        plannedEndDate: milestoneForm.plannedEndDate,
        predecessorMilestoneId: milestoneForm.predecessorMilestoneId,
        responsiblePersonId: Number(milestoneForm.responsiblePersonId),
      });
      ElMessage.success('添加成功');
    }

    milestoneDialog.visible = false;
    await loadProgress();
  } finally {
    saving.value = false;
  }
}

function updateProgress(milestone: ProjectMilestoneVO) {
  progressDialog.visible = true;
  progressDialog.milestoneId = milestone.id;
  progressForm.progress = milestone.progress || 0;
  progressForm.actualStartDate = milestone.actualStartDate || '';
  progressForm.actualEndDate = milestone.actualEndDate || '';
  progressForm.remarks = '';
}

async function confirmProgressUpdate() {
  if (!progressDialog.milestoneId) {
    return;
  }
  await updateMilestoneProgress({
    milestoneId: progressDialog.milestoneId,
    progress: progressForm.progress,
    actualStartDate: progressForm.actualStartDate || undefined,
    actualEndDate: progressForm.actualEndDate || undefined,
    remarks: progressForm.remarks || undefined,
  });
  progressDialog.visible = false;
  await loadProgress();
  ElMessage.success('进度更新成功');
}

function deleteMilestone(milestone: ProjectMilestoneVO) {
  ElMessageBox.confirm(`确认删除里程碑「${milestone.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteProjectMilestone(milestone.id);
      await loadProgress();
      ElMessage.success('删除成功');
    })
    .catch(() => {});
}

function handleRowDblClick(row: ProjectMilestoneVO) {
  if (canUpdate.value) {
    editMilestone(row);
  }
}

function exportProgress() {
  ElMessage.info('导出功能开发中...');
}

watch(
  () => route.params.projectId,
  async (newId) => {
    projectId.value = parseProjectId(newId);
    await loadProjectMembers();
    await loadProgress();
  },
);

onMounted(async () => {
  if (!projectId.value) {
    ElMessage.warning('当前未选择有效项目，请从项目管理进入');
  }
  await loadProjectMembers();
  await loadProgress();
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

.progress-overview {
  margin-bottom: 24px;

  .progress-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .progress-info {
      .progress-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 4px;
      }

      .progress-value {
        font-size: 24px;
        font-weight: bold;
        color: #409EFF;
      }
    }
  }

  .stat-card {
    text-align: center;
    padding: 20px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    &.warning {
      border-left: 4px solid #F56C6C;
    }

    .stat-number {
      font-size: 32px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 8px;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }
}

.progress-chart {
  margin-bottom: 24px;

  h3 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .chart-container {
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .gantt-chart {
    .gantt-header {
      display: flex;
      border-bottom: 2px solid #ebeef5;
      margin-bottom: 16px;

      .task-name {
        width: 200px;
        padding: 12px;
        font-weight: 600;
        background: #f5f7fa;
      }

      .timeline {
        flex: 1;
        display: flex;

        .month-block {
          flex: 1;
          padding: 12px 8px;
          text-align: center;
          font-size: 12px;
          font-weight: 500;
          background: #f5f7fa;
          border-right: 1px solid #ebeef5;

          &:last-child {
            border-right: none;
          }
        }
      }
    }

    .gantt-body {
      .gantt-row {
        display: flex;
        border-bottom: 1px solid #ebeef5;
        min-height: 60px;

        &:last-child {
          border-bottom: none;
        }

        .task-info {
          width: 200px;
          padding: 12px;
          display: flex;
          flex-direction: column;
          justify-content: center;

          .task-title {
            font-weight: 500;
            margin-bottom: 8px;
          }

          .task-status {
            font-size: 12px;
          }
        }

        .task-timeline {
          flex: 1;
          position: relative;
          padding: 12px 0;

          .task-bar {
            position: absolute;
            height: 24px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 12px;
            font-weight: 500;

            &.completed {
              background: #67C23A;
            }

            &.ongoing {
              background: #409EFF;
            }

            &.delayed {
              background: #F56C6C;
            }

            &.not-started {
              background: #C0C4CC;
            }

            .task-progress {
              color: white;
            }
          }
        }
      }
    }
  }
}

.time-range {
  font-size: 12px;
  line-height: 1.4;
}

.no-data {
  color: #C0C4CC;
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

.delayed {
  color: #F56C6C;
  font-weight: 500;
}

.on-time {
  color: #67C23A;
}

.milestone-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}

// 响应式
@media (max-width: 768px) {
  .progress-overview {
    .el-col {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .progress-card {
      flex-direction: column;
      text-align: center;
    }
  }

  .progress-chart {
    .gantt-chart {
      .gantt-header {
        .task-name {
          width: 120px;
        }
      }

      .gantt-row {
        .task-info {
          width: 120px;
        }
      }
    }
  }
}
</style>
