<template>
  <div class="instance-page" v-loading="loading">
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-title">流程基础信息</div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="流程类型">{{ detail?.busType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流程实例ID">{{ detail?.instanceId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流程标题">{{ detail?.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ detail?.starterName || detail?.starterId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ detail?.currentNode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流程状态">{{ detail?.status || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务ID">{{ detail?.busId || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="section-card business-section">
      <template #header>
        <div class="card-title">业务页面</div>
      </template>
      <component
        v-if="businessPanel"
        :is="businessPanel"
        :bus-id="detail?.busId || ''"
        :bus-type="detail?.busType || ''"
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
            :timestamp="item.finishTime || item.createTime"
            :type="timelineType(item.action)"
          >
            <div class="node-line">{{ item.nodeName || '-' }}（{{ item.actionLabel || actionText(item.action) }}）</div>
            <div class="opinion-line">处理人：{{ item.handlerName || item.handlerId || '-' }}</div>
            <div class="opinion-line">意见：{{ item.opinion || '-' }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  getApprovalInstanceDetail,
  getApprovalDetail,
  type ApprovalDetail,
  type ApprovalHistory,
} from '@/api/approval';
import { resolveBusinessPanel } from '../instance-center/business-registry';

const route = useRoute();
const loading = ref(false);
const detail = ref<ApprovalDetail | null>(null);
const history = ref<ApprovalHistory[]>([]);

const businessPanel = computed(() => resolveBusinessPanel(detail.value?.busType || ''));

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

.business-section {
  min-height: 240px;
  max-height: 380px;
  overflow: auto;
}

.timeline-wrap {
  max-height: 260px;
  overflow: auto;
}

.node-line {
  font-weight: 600;
}

.opinion-line {
  margin-top: 4px;
  color: #606266;
}
</style>
