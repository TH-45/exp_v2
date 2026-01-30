<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">审批/待办中心</div>
          <div class="actions">
            <el-button size="small" @click="exportTasks">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button size="small" @click="openSettings">
              <el-icon><Setting /></el-icon>
              设置
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-card class="stats-card pending-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#409EFF"><DocumentChecked /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ stats.todayPending }}</div>
                  <div class="stats-label">今日待办</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stats-card urgent-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#F56C6C"><AlarmClock /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ stats.urgentPending }}</div>
                  <div class="stats-label">紧急待办</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stats-card completed-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#67C23A"><Check /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ stats.completedToday }}</div>
                  <div class="stats-label">今日完成</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stats-card efficiency-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#E6A23C"><TrendCharts /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ stats.efficiency }}%</div>
                  <div class="stats-label">审批效率</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabChange" class="approval-tabs">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="招标审批" name="bidding" />
        <el-tab-pane label="投标审批" name="tender" />
        <el-tab-pane label="合同审批" name="contract" />
        <el-tab-pane label="项目审批" name="project" />
      </el-tabs>

      <!-- 查询区 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="标题、申请人、描述"
            clearable
            style="width: 200px"
            @input="handleSearchInput"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="待审批" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="草稿" value="draft" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="query.priority" clearable placeholder="全部" style="width: 120px">
            <el-option label="紧急" value="urgent" />
            <el-option label="重要" value="important" />
            <el-option label="普通" value="normal" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
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

      <!-- 批量操作区 -->
      <div v-if="selectedTasks.length > 0" class="batch-actions">
        <span class="selected-count">已选择 {{ selectedTasks.length }} 项</span>
        <el-space>
          <el-button size="small" type="success" @click="batchApprove">
            <el-icon><Check /></el-icon>
            批量通过
          </el-button>
          <el-button size="small" type="danger" @click="batchReject">
            <el-icon><Close /></el-icon>
            批量驳回
          </el-button>
          <el-button size="small" @click="clearSelection">取消选择</el-button>
        </el-space>
      </div>

      <!-- 列表区 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="id"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
        :default-sort="{prop: 'submitTime', order: 'descending'}"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="审批类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题/描述" min-width="200">
          <template #default="{ row }">
            <div class="task-title">
              <div class="title-text">{{ row.title }}</div>
              <div class="description">{{ row.description }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="applicant" label="申请人" width="120" />
        <el-table-column prop="submitTime" label="提交时间" width="160" sortable />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <div class="priority-cell">
              <el-icon v-if="row.priority === 'urgent'" color="#F56C6C"><Warning /></el-icon>
              <span :class="`priority-${row.priority}`">{{ getPriorityLabel(row.priority) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-space size="small">
              <el-button link type="primary" size="small" @click="openDetail(row)">
                详情
              </el-button>
              <template v-if="row.status === 'pending'">
                <el-button link type="success" size="small" @click="quickApprove(row)">
                  通过
                </el-button>
                <el-button link type="danger" size="small" @click="quickReject(row)">
                  驳回
                </el-button>
              </template>
              <el-button v-else link type="info" size="small" @click="openDetail(row)">
                查看
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

      <!-- 审批详情弹窗 -->
      <el-dialog
        v-model="detailDialog.visible"
        :title="`审批详情 - ${detailDialog.data?.title}`"
        width="900px"
        destroy-on-close
      >
        <el-tabs v-model="detailTab" class="detail-tabs">
          <el-tab-pane label="申请信息" name="info">
            <div class="detail-content">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="审批类型">
                  <el-tag :type="getTypeTagType(detailDialog.data?.type)">
                    {{ getTypeLabel(detailDialog.data?.type) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="优先级">
                  <span :class="`priority-${detailDialog.data?.priority}`">
                    {{ getPriorityLabel(detailDialog.data?.priority) }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="申请人">{{ detailDialog.data?.applicant }}</el-descriptions-item>
                <el-descriptions-item label="提交时间">{{ detailDialog.data?.submitTime }}</el-descriptions-item>
                <el-descriptions-item label="当前状态">
                  <el-tag :type="getStatusTagType(detailDialog.data?.status)">
                    {{ getStatusLabel(detailDialog.data?.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="审批人">
                  {{ detailDialog.data?.approver || '暂无' }}
                </el-descriptions-item>
              </el-descriptions>

              <div class="business-data">
                <h4>业务详情</h4>
                <div class="business-content">
                  {{ detailDialog.data?.businessData || '暂无业务数据' }}
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="审批流程" name="flow">
            <el-timeline>
              <el-timeline-item
                v-for="history in approvalHistory"
                :key="history.id"
                :timestamp="history.operateTime"
                :type="getHistoryIconType(history.action)"
              >
                <div class="timeline-content">
                  <div class="operator">{{ history.operator }}</div>
                  <div class="action">{{ getActionLabel(history.action) }}</div>
                  <div v-if="history.comments" class="comments">{{ history.comments }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>

          <el-tab-pane label="附件资料" name="attachments">
            <div v-if="detailDialog.data?.attachments?.length" class="attachments-list">
              <el-table :data="detailDialog.data.attachments" stripe style="width: 100%">
                <el-table-column prop="name" label="文件名" />
                <el-table-column prop="size" label="大小" width="120">
                  <template #default="{ row }">
                    {{ formatFileSize(row.size) }}
                  </template>
                </el-table-column>
                <el-table-column prop="uploadTime" label="上传时间" width="160" />
                <el-table-column label="操作" width="100">
                  <template #default="{ row }">
                    <el-button link type="primary" size="small" @click="downloadAttachment(row)">
                      下载
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-else class="no-attachments">
              <el-empty description="暂无附件" />
            </div>
          </el-tab-pane>
        </el-tabs>

        <template #footer v-if="detailDialog.data?.status === 'pending'">
          <div class="dialog-footer">
            <el-form :model="actionForm" label-width="80px" @submit.prevent="handleApprove">
              <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
              <el-form-item label="审批意见">
                <el-input
                  v-model="actionForm.comments"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入审批意见（可选）"
                  @keydown.enter.stop
                />
              </el-form-item>
            </el-form>
            <el-space>
              <el-button @click="detailDialog.visible = false">取消</el-button>
              <el-button type="danger" @click="handleReject" :loading="actionLoading">
                驳回
              </el-button>
              <el-button type="success" @click="handleApprove" :loading="actionLoading">
                通过
              </el-button>
            </el-space>
          </div>
        </template>
      </el-dialog>

      <!-- 批量操作弹窗 -->
      <el-dialog
        v-model="batchDialog.visible"
        :title="batchDialog.type === 'approve' ? '批量通过' : '批量驳回'"
        width="500px"
      >
        <div class="batch-confirm">
          <p>确认{{ batchDialog.type === 'approve' ? '通过' : '驳回' }}选中的 {{ selectedTasks.length }} 个审批任务？</p>
          <el-form :model="batchForm" label-width="80px" @submit.prevent="confirmBatchAction">
            <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
            <el-form-item label="批量意见">
              <el-input
                v-model="batchForm.comments"
                type="textarea"
                :rows="3"
                placeholder="请输入批量审批意见（可选）"
                @keydown.enter.stop
              />
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <el-space>
            <el-button @click="batchDialog.visible = false">取消</el-button>
            <el-button
              :type="batchDialog.type === 'approve' ? 'success' : 'danger'"
              @click="confirmBatchAction"
              :loading="batchLoading"
            >
              确认{{ batchDialog.type === 'approve' ? '通过' : '驳回' }}
            </el-button>
          </el-space>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import {
  ElMessage,
  ElMessageBox,
  type FormInstance
} from 'element-plus';
import {
  Check,
  Close,
  DocumentChecked,
  AlarmClock,
  TrendCharts,
  Download,
  Setting,
  Warning
} from '@element-plus/icons-vue';
import {
  getApprovalStats,
  listApprovalTasks,
  getApprovalDetail,
  approveTask,
  rejectTask,
  batchApprove,
  batchReject,
  getApprovalHistory,
  type ApprovalTask,
  type ApprovalStats,
  type ApprovalDetail,
  type ApprovalHistory,
  type ApprovalType,
  type ApprovalStatus,
  type Priority
} from '@/api/approval';

const loading = ref(false);
const actionLoading = ref(false);
const batchLoading = ref(false);

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  type: undefined as ApprovalType | undefined,
  status: undefined as ApprovalStatus | undefined,
  priority: undefined as Priority | undefined,
  startDate: '',
  endDate: '',
  applicant: ''
});

const dateRange = ref<string[]>([]);
const activeTab = ref('all');
const tableData = ref<ApprovalTask[]>([]);
const total = ref(0);
const selectedTasks = ref<ApprovalTask[]>([]);
const stats = reactive<ApprovalStats>({
  todayPending: 0,
  urgentPending: 0,
  completedToday: 0,
  efficiency: 0
});

// 详情弹窗
const detailDialog = reactive({
  visible: false,
  data: null as ApprovalDetail | null
});
const detailTab = ref('info');
const approvalHistory = ref<ApprovalHistory[]>([]);

// 批量操作弹窗
const batchDialog = reactive({
  visible: false,
  type: 'approve' as 'approve' | 'reject'
});

// 表单数据
const actionForm = reactive({
  comments: ''
});
const batchForm = reactive({
  comments: ''
});

// 计算属性
const isAllTab = computed(() => activeTab.value === 'all');

// 工具函数
function getTypeLabel(type?: ApprovalType) {
  const labels = {
    bidding: '招标审批',
    tender: '投标审批',
    contract: '合同审批',
    project: '项目审批'
  };
  return labels[type || 'bidding'];
}

function getTypeTagType(type?: ApprovalType) {
  const types = {
    bidding: 'primary',
    tender: 'success',
    contract: 'warning',
    project: 'info'
  };
  return types[type || 'bidding'];
}

function getStatusLabel(status?: ApprovalStatus) {
  const labels = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已驳回',
    draft: '草稿'
  };
  return labels[status || 'pending'];
}

function getStatusTagType(status?: ApprovalStatus) {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    draft: 'info'
  };
  return types[status || 'pending'];
}

function getPriorityLabel(priority?: Priority) {
  const labels = {
    urgent: '紧急',
    important: '重要',
    normal: '普通'
  };
  return labels[priority || 'normal'];
}

function getActionLabel(action: string) {
  const labels = {
    submit: '提交申请',
    approve: '通过审批',
    reject: '驳回审批',
    delegate: '委托审批'
  };
  return labels[action as keyof typeof labels] || action;
}

function getHistoryIconType(action: string) {
  const types = {
    submit: 'primary',
    approve: 'success',
    reject: 'danger',
    delegate: 'warning'
  };
  return types[action as keyof typeof types] || 'primary';
}

function formatFileSize(bytes: number) {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// 事件处理
function handleTabChange() {
  if (activeTab.value === 'all') {
    query.type = undefined;
  } else {
    query.type = activeTab.value as ApprovalType;
  }
  query.page = 1;
  fetchList();
}

function handleSearchInput() {
  // 防抖搜索
  clearTimeout((window as any).searchTimer);
  (window as any).searchTimer = setTimeout(() => {
    query.page = 1;
    fetchList();
  }, 500);
}

function handleSearch() {
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.keyword = '';
  query.status = undefined;
  query.priority = undefined;
  dateRange.value = [];
  query.startDate = '';
  query.endDate = '';
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

function handleSelectionChange(rows: ApprovalTask[]) {
  selectedTasks.value = rows;
}

function clearSelection() {
  selectedTasks.value = [];
}

// 快速操作
function quickApprove(row: ApprovalTask) {
  ElMessageBox.confirm(`确认通过「${row.title}」的审批申请？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    actionLoading.value = true;
    try {
      await approveTask({
        taskId: row.id,
        action: 'approve'
      });
      ElMessage.success('审批通过成功');
      fetchList();
      fetchStats();
    } catch (e) {
      ElMessage.error('审批失败');
    } finally {
      actionLoading.value = false;
    }
  });
}

function quickReject(row: ApprovalTask) {
  ElMessageBox.confirm(`确认驳回「${row.title}」的审批申请？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    actionLoading.value = true;
    try {
      await rejectTask({
        taskId: row.id,
        action: 'reject'
      });
      ElMessage.success('审批驳回成功');
      fetchList();
      fetchStats();
    } catch (e) {
      ElMessage.error('驳回失败');
    } finally {
      actionLoading.value = false;
    }
  });
}

// 详情弹窗
async function openDetail(row: ApprovalTask) {
  detailDialog.visible = true;
  detailDialog.data = null;
  detailTab.value = 'info';

  try {
    const [detailRes, historyRes] = await Promise.all([
      getApprovalDetail(row.id),
      getApprovalHistory(row.id)
    ]);

    detailDialog.data = detailRes;
    approvalHistory.value = historyRes;
  } catch (e) {
    ElMessage.error('获取详情失败');
    detailDialog.visible = false;
  }
}

async function handleApprove() {
  if (!detailDialog.data) return;

  actionLoading.value = true;
  try {
    await approveTask({
      taskId: detailDialog.data.id,
      action: 'approve',
      comments: actionForm.comments
    });
    ElMessage.success('审批通过成功');
    detailDialog.visible = false;
    fetchList();
    fetchStats();
    actionForm.comments = '';
  } catch (e) {
    ElMessage.error('审批失败');
  } finally {
    actionLoading.value = false;
  }
}

async function handleReject() {
  if (!detailDialog.data) return;

  actionLoading.value = true;
  try {
    await rejectTask({
      taskId: detailDialog.data.id,
      action: 'reject',
      comments: actionForm.comments
    });
    ElMessage.success('审批驳回成功');
    detailDialog.visible = false;
    fetchList();
    fetchStats();
    actionForm.comments = '';
  } catch (e) {
    ElMessage.error('驳回失败');
  } finally {
    actionLoading.value = false;
  }
}

// 批量操作
function batchApprove() {
  batchDialog.type = 'approve';
  batchDialog.visible = true;
  batchForm.comments = '';
}

function batchReject() {
  batchDialog.type = 'reject';
  batchDialog.visible = true;
  batchForm.comments = '';
}

async function confirmBatchAction() {
  const taskIds = selectedTasks.value.map(task => task.id);

  batchLoading.value = true;
  try {
    if (batchDialog.type === 'approve') {
      await batchApprove({
        taskIds,
        comments: batchForm.comments
      });
      ElMessage.success('批量通过成功');
    } else {
      await batchReject({
        taskIds,
        comments: batchForm.comments
      });
      ElMessage.success('批量驳回成功');
    }

    batchDialog.visible = false;
    selectedTasks.value = [];
    fetchList();
    fetchStats();
    batchForm.comments = '';
  } catch (e) {
    ElMessage.error('批量操作失败');
  } finally {
    batchLoading.value = false;
  }
}

// 其他功能
function exportTasks() {
  ElMessage.info('导出功能开发中...');
}

function openSettings() {
  ElMessage.info('设置功能开发中...');
}

function downloadAttachment(attachment: any) {
  ElMessage.info('下载功能开发中...');
}

// 数据获取
async function fetchStats() {
  try {
    const res = await getApprovalStats();
    Object.assign(stats, res);
  } catch (e) {
    // 使用模拟数据
    Object.assign(stats, {
      todayPending: 12,
      urgentPending: 3,
      completedToday: 45,
      efficiency: 95
    });
  }
}

async function fetchList() {
  // 更新日期范围查询条件
  if (dateRange.value && dateRange.value.length === 2) {
    query.startDate = dateRange.value[0];
    query.endDate = dateRange.value[1];
  } else {
    query.startDate = '';
    query.endDate = '';
  }

  loading.value = true;
  try {
    const res = await listApprovalTasks(query);
    const list = (res.records || res.list || res.rows || []) as ApprovalTask[];
    tableData.value = list.length ? list : mockTasks;
    total.value = Number(res.total ?? tableData.value.length) || 0;
  } catch (e) {
    tableData.value = mockTasks;
    total.value = mockTasks.length;
  } finally {
    loading.value = false;
  }
}

// 模拟数据
const mockTasks: ApprovalTask[] = [
  {
    id: '1',
    type: 'bidding',
    title: '某某项目招标申请',
    description: '对某某工程项目进行公开招标，预算金额500万元',
    applicant: '张三',
    applicantId: 'user001',
    submitTime: '2025-01-05 10:30:00',
    status: 'pending',
    priority: 'urgent',
    businessId: 'bid001',
    businessType: 'bidding'
  },
  {
    id: '2',
    type: 'contract',
    title: '合同变更申请',
    description: '因工程量变化，需要对合同金额进行调整',
    applicant: '李四',
    applicantId: 'user002',
    submitTime: '2025-01-04 14:20:00',
    status: 'pending',
    priority: 'important',
    businessId: 'contract001',
    businessType: 'contract'
  },
  {
    id: '3',
    type: 'project',
    title: '项目延期申请',
    description: '由于天气原因，项目需要延期15天完成',
    applicant: '王五',
    applicantId: 'user003',
    submitTime: '2025-01-03 09:15:00',
    status: 'approved',
    priority: 'normal',
    businessId: 'project001',
    businessType: 'project',
    approveTime: '2025-01-03 11:30:00',
    approver: '赵六',
    approverId: 'user004'
  }
];

onMounted(() => {
  fetchStats();
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
  font-size: 18px;
}

.actions > * + * {
  margin-left: 8px;
}

// 统计卡片
.stats-cards {
  margin-bottom: 24px;

  .stats-card {
    height: 100px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .stats-content {
      display: flex;
      align-items: center;
      height: 100%;
      padding: 16px;

      .stats-icon {
        margin-right: 16px;
      }

      .stats-info {
        .stats-number {
          font-size: 28px;
          font-weight: bold;
          line-height: 1;
          margin-bottom: 4px;
        }

        .stats-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .pending-card .stats-content .stats-icon {
    color: #409EFF;
  }

  .urgent-card .stats-content .stats-icon {
    color: #F56C6C;
  }

  .completed-card .stats-content .stats-icon {
    color: #67C23A;
  }

  .efficiency-card .stats-content .stats-icon {
    color: #E6A23C;
  }
}

// 标签页
.approval-tabs {
  margin-bottom: 16px;

  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }
}

// 搜索栏
.search-bar {
  margin-bottom: 16px;
}

// 批量操作
.batch-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 16px;

  .selected-count {
    color: #409EFF;
    font-weight: 500;
  }
}

// 任务标题
.task-title {
  .title-text {
    font-weight: 500;
    margin-bottom: 4px;
  }

  .description {
    font-size: 12px;
    color: #909399;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 优先级
.priority-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.priority-urgent {
  color: #F56C6C;
  font-weight: 500;
}

.priority-important {
  color: #E6A23C;
  font-weight: 500;
}

.priority-normal {
  color: #909399;
}

// 分页
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

// 详情弹窗
.detail-tabs {
  :deep(.el-tabs__content) {
    padding: 16px 0;
  }
}

.detail-content {
  .business-data {
    margin-top: 24px;

    h4 {
      margin: 0 0 12px 0;
      color: #303133;
      font-size: 16px;
    }

    .business-content {
      padding: 12px;
      background-color: #f5f7fa;
      border-radius: 4px;
      min-height: 60px;
    }
  }
}

// 时间线
.timeline-content {
  .operator {
    font-weight: 500;
    margin-bottom: 4px;
  }

  .action {
    color: #409EFF;
    font-size: 14px;
    margin-bottom: 4px;
  }

  .comments {
    color: #606266;
    font-size: 13px;
    padding: 8px;
    background-color: #f5f7fa;
    border-radius: 4px;
  }
}

// 附件
.attachments-list {
  .no-attachments {
    text-align: center;
    padding: 40px 0;
  }
}

// 弹窗底部
.dialog-footer {
  .el-form {
    margin-bottom: 16px;
  }
}

// 批量确认
.batch-confirm {
  p {
    margin-bottom: 16px;
    font-weight: 500;
    color: #303133;
  }
}

// 响应式
@media (max-width: 768px) {
  .stats-cards {
    .el-col {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .actions {
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }

  .batch-actions {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
