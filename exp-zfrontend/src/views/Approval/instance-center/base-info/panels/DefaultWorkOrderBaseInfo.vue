<template>
  <el-descriptions :column="3" border>
    <el-descriptions-item label="流程类型">{{ detail?.busType || '-' }}</el-descriptions-item>
    <el-descriptions-item label="流程编号">{{ detail?.procCode || '-' }}</el-descriptions-item>
    <el-descriptions-item label="流程实例ID">{{ detail?.instanceId || '-' }}</el-descriptions-item>
    <el-descriptions-item label="流程标题">{{ detail?.title || '-' }}</el-descriptions-item>
    <el-descriptions-item label="发起人">{{ detail?.starterName || detail?.starterId || '-' }}</el-descriptions-item>
    <el-descriptions-item label="当前节点">{{ detail?.currentNode || '-' }}</el-descriptions-item>
    <el-descriptions-item label="流程状态">{{ processStatusText(detail?.status) }}</el-descriptions-item>
    <el-descriptions-item label="业务ID">{{ detail?.busId || '-' }}</el-descriptions-item>
  </el-descriptions>
</template>

<script setup lang="ts">
import type { ApprovalDetail } from '@/api/approval';

interface Props {
  detail: ApprovalDetail | null;
}

defineProps<Props>();

function processStatusText(status?: string) {
  const s = String(status || '').trim().toUpperCase();
  if (!s) return '-';
  if (s === 'APPROVING') return '审批中';
  if (s === 'COMPLETED') return '已完成';
  if (s === 'CLOSED') return '已关闭';
  if (s === 'REJECTED') return '已拒绝';
  return status || '-';
}
</script>
