<template>
  <div class="instance-page" v-loading="loading">
    <el-card v-if="canOperateApproval || canForceClose" shadow="never" class="section-card action-card">
      <div class="action-wrap">
        <ApprovalActionBar v-if="canOperateApproval" @approve="openApprovalDialog" @reject="openRejectDialog" />
        <el-button v-if="canForceClose" type="warning" @click="openCloseDialog">强制关闭</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-title">流程基础信息</div>
      </template>
      <component :is="baseInfoPanel" :detail="detail" />
    </el-card>

    <el-card shadow="never" class="section-card business-section">
      <component
        v-if="businessPanel"
        :is="businessPanel"
        :bus-id="detail?.busId || ''"
        :bus-type="detail?.busType || ''"
        :proc-code="detail?.procCode || ''"
        :detail="detail"
      />
      <el-empty v-else description="该业务类型暂未接入流程基础信息页" />
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-title">提交历史</div>
      </template>
      <el-empty v-if="history.length === 0" description="暂无提交历史" />
      <div v-else class="timeline-wrap">
        <el-timeline>
          <el-timeline-item
            v-for="item in history"
            :key="String(item.taskId) + String(item.createTime)"
            :timestamp="formatTimelineTime(item)"
            :type="timelineType(item.action)"
          >
            <div class="node-line">{{ item.nodeName || '-' }}（{{ item.actionLabel || actionText(item.action) }}）</div>
            <div class="opinion-line">处理人：{{ formatProcessor(item) }}</div>
            <div class="opinion-line">意见：{{ item.opinion || '-' }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>

    <ApprovalDialog
      v-model="approvalDialogVisible"
      :task-id="currentTaskId"
      @confirm="handleApprovalConfirm"
    />
    <RejectDialog
      v-model="rejectDialogVisible"
      :task-id="currentTaskId"
      @confirm="handleRejectConfirm"
    />
    <el-dialog v-model="closeDialog.visible" title="强制关闭流程" width="520px" draggable destroy-on-close>
      <el-form :model="closeDialog">
        <el-form-item label="关闭原因" label-width="90px">
          <el-input v-model="closeDialog.reason" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog.visible = false">取消</el-button>
        <el-button type="warning" @click="submitForceClose">确认关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  approveTask,
  forceCloseInstance,
  getApprovalInstanceDetail,
  getApprovalDetail,
  returnTask,
  type ApprovalDetail,
  type ApprovalHistory,
} from '@/api/approval';
import ApprovalActionBar from '@/components/Approval/ApprovalActionBar.vue';
import ApprovalDialog from '@/components/Approval/ApprovalDialog.vue';
import RejectDialog from '@/components/Approval/RejectDialog.vue';
import { resolveBaseInfoPanel } from '../instance-center/base-info-registry';
import { resolveBusinessPanel } from '../instance-center/business-registry';

const route = useRoute();
const loading = ref(false);
const detail = ref<ApprovalDetail | null>(null);
const history = ref<ApprovalHistory[]>([]);
const approvalDialogVisible = ref(false);
const rejectDialogVisible = ref(false);
const closeDialog = ref<{ visible: boolean; reason: string }>({ visible: false, reason: '' });

const baseInfoPanel = computed(() => resolveBaseInfoPanel(detail.value?.busType || '', detail.value?.procCode || ''));
const businessPanel = computed(() => resolveBusinessPanel(detail.value?.busType || '', detail.value?.procCode || ''));
const currentTaskId = computed(() => {
  const fromQuery = Number(route.query.taskId || 0);
  if (fromQuery > 0) {
    return fromQuery;
  }
  return Number(detail.value?.taskId || 0);
});
const canOperateApproval = computed(() => {
  const tab = String(route.query.tab || '').toLowerCase();
  return tab === 'todo' && currentTaskId.value > 0 && String(detail.value?.status || '').toUpperCase() === 'APPROVING';
});
const canForceClose = computed(() => {
  const tab = String(route.query.tab || '').toLowerCase();
  return tab === 'started' && Number(detail.value?.instanceId || 0) > 0 && String(detail.value?.status || '').toUpperCase() === 'APPROVING';
});

function actionText(action: string) {
  const act = String(action || '').toUpperCase();
  if (act === 'AGREE') return '通过';
  if (act === 'REJECT') return '不同意';
  if (act === 'RETURN') return '驳回';
  if (act === 'CLOSE') return '关闭';
  if (act === 'APPROVE') return '已提交';
  return act || '-';
}

function timelineType(action: string) {
  const act = String(action || '').toUpperCase();
  if (act === 'AGREE') return 'success';
  if (act === 'REJECT') return 'danger';
  if (act === 'RETURN') return 'warning';
  return 'primary';
}

function formatDateYmd(value?: string) {
  if (!value) return '';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '';
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function formatTimelineTime(item: ApprovalHistory) {
  const action = String(item.action || '').toUpperCase();
  // 创建节点：显示实例/创建时间
  if (action === 'CREATE') {
    return formatDateYmd(item.createTime || item.finishTime);
  }
  // 未完成审批节点：不显示时间
  if (item.isDone !== 1) {
    return '';
  }
  // 已完成审批节点：显示审批完成时间
  return formatDateYmd(item.finishTime || item.createTime);
}

function formatProcessor(item: ApprovalHistory) {
  // 成对展示：handlerCode+handlerName；若未处理则展示 candidateCode+candidateName
  if (item.handlerName) {
    const handlerCode = String(item.handlerCode || '').trim();
    return handlerCode ? `${item.handlerName}（${handlerCode}）` : item.handlerName;
  }
  const candidateName = item.candidateName || '-';
  const candidateCode = String(item.candidateCode || '').trim();
  return candidateCode && candidateName !== '-' ? `${candidateName}（${candidateCode}）` : candidateName;
}

function openApprovalDialog() {
  if (!currentTaskId.value) {
    ElMessage.warning('当前记录缺少任务ID，无法审批');
    return;
  }
  approvalDialogVisible.value = true;
}

function openRejectDialog() {
  if (!currentTaskId.value) {
    ElMessage.warning('当前记录缺少任务ID，无法驳回');
    return;
  }
  rejectDialogVisible.value = true;
}

async function handleApprovalConfirm(payload: { taskId: number; action: 'AGREE' | 'REJECT'; comments: string }) {
  await approveTask(payload);
  ElMessage.success('审批操作成功');
  await fetchDetail();
}

async function handleRejectConfirm(payload: { taskId: number; comments: string }) {
  await returnTask({ taskId: payload.taskId, comments: payload.comments });
  ElMessage.success('驳回成功');
  await fetchDetail();
}

function openCloseDialog() {
  if (!detail.value?.instanceId) {
    ElMessage.warning('当前记录缺少流程实例ID，无法强制关闭');
    return;
  }
  closeDialog.value.reason = '';
  closeDialog.value.visible = true;
}

async function submitForceClose() {
  if (!detail.value?.instanceId) {
    ElMessage.warning('当前记录缺少流程实例ID，无法强制关闭');
    return;
  }
  await forceCloseInstance({
    instanceId: detail.value.instanceId,
    reason: closeDialog.value.reason,
  });
  ElMessage.success('流程已关闭');
  closeDialog.value.visible = false;
  await fetchDetail();
}

async function fetchDetail() {
  const instanceId = Number(route.params.instanceId || 0);
  if (!instanceId) {
    ElMessage.warning('缺少流程实例ID');
    return;
  }
  loading.value = true;
  try {
    detail.value = await getApprovalInstanceDetail(instanceId);
    history.value = detail.value?.approvalHistory || [];
  } catch (e) {
    // 兼容旧后端：若尚未发布 instance-detail，回退到 taskId 详情
    const taskId = Number(route.query.taskId || 0);
    if (!taskId) {
      ElMessage.error((e as Error)?.message || '查询流程详情失败');
      detail.value = null;
      history.value = [];
      return;
    }
    try {
      detail.value = await getApprovalDetail(taskId);
      history.value = detail.value?.approvalHistory || [];
    } catch (fallbackError) {
      ElMessage.error((fallbackError as Error)?.message || '查询流程详情失败');
      detail.value = null;
      history.value = [];
    }
  } finally {
    loading.value = false;
  }
}

onMounted(fetchDetail);
watch(
  () => [route.params.instanceId, route.query.taskId],
  () => {
    fetchDetail();
  }
);
</script>

<style scoped lang="scss">
.instance-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-card {
  width: 100%;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
}

.action-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-card :deep(.el-card__body) {
  padding-top: 12px;
  padding-bottom: 12px;
}

.business-section {
  min-height: 420px;
  max-height: none;
  overflow: visible;
}

.timeline-wrap {
  max-height: none;
  overflow: visible;
}

.node-line {
  font-weight: 600;
}

.opinion-line {
  margin-top: 4px;
  color: #606266;
}
</style>
