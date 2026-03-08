<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">投标详情</div>
          <div class="actions">
            <el-button @click="goBack">返回列表</el-button>
            <el-button type="primary" :disabled="!canManage" @click="goEdit">去编辑</el-button>
          </div>
        </div>
      </template>

      <el-skeleton :loading="loading" :rows="8" animated>
        <template #default>
          <el-descriptions title="基础信息" :column="3" border>
            <el-descriptions-item label="投标编号">{{ detail.bidCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="投标名称">{{ detail.bidName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTagType(detail.bidStatus)">{{ statusText(detail.bidStatus) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="招标项目">{{ detail.tenderName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="投标单位">{{ detail.supplierName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="报价金额">{{ formatAmount(detail.bidTotalAmount, detail.currency) || '-' }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ detail.bidSubmitTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="中标标识">
              <el-tag :type="Number(detail.winFlag) === 1 ? 'success' : 'info'">
                {{ Number(detail.winFlag) === 1 ? '已中标' : '未中标' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="中标通知书">{{ detail.winNoticeNo || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-descriptions title="业务信息" :column="3" border class="mt12">
            <el-descriptions-item label="工程项目">{{ detail.projectName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="归属组织">{{ detail.orgIdName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ detail.managerPersonName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="业务员">{{ detail.salesmanName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="合同ID">{{ detail.contractId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-descriptions title="审计信息" :column="3" border class="mt12">
            <el-descriptions-item label="创建人">{{ detail.createdByName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建部门">{{ detail.createdDeptName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建岗位">{{ detail.createdPostName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detail.createdTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ detail.updatedTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="投标ID">{{ detail.bidId || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-skeleton>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import { getBidDetail, type BidDetailVO } from '@/api/bidding/bid';

const route = useRoute();
const router = useRouter();
const canManage = computed(() => hasPermission('bidding:bid:manage'));
const loading = ref(false);
const detail = reactive<Partial<BidDetailVO>>({});

function statusText(status?: string) {
  if (status === 'PREPARE') return '准备';
  if (status === 'SUBMITTED') return '已提交';
  if (status === 'EVALUATING') return '评审中';
  if (status === 'WON') return '中标';
  if (status === 'LOST') return '未中标';
  if (status === 'ABANDONED') return '已放弃';
  return status || '-';
}

function statusTagType(status?: string) {
  if (status === 'PREPARE') return 'info';
  if (status === 'SUBMITTED') return 'warning';
  if (status === 'EVALUATING') return 'primary';
  if (status === 'WON') return 'success';
  if (status === 'LOST') return 'danger';
  if (status === 'ABANDONED') return 'info';
  return '';
}

function formatAmount(amount?: number, currency?: string) {
  if (amount == null) return '';
  const num = Number(amount);
  if (!Number.isFinite(num)) return '';
  return `${num.toFixed(2)} ${currency || 'CNY'}`;
}

function getBidId() {
  const raw = route.params.bidId;
  const id = Array.isArray(raw) ? Number(raw[0]) : Number(raw);
  return Number.isFinite(id) && id > 0 ? id : 0;
}

async function fetchDetail() {
  const bidId = getBidId();
  if (!bidId) {
    ElMessage.error('投标ID不合法');
    return;
  }
  loading.value = true;
  try {
    const res = await getBidDetail(bidId);
    Object.assign(detail, res);
  } catch (e: any) {
    ElMessage.error(e?.message || '加载投标详情失败');
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push('/bidding/bid');
}

function goEdit() {
  const bidId = getBidId();
  router.push({
    path: '/bidding/bid',
    query: {
      edit: String(bidId),
    },
  });
}

onMounted(() => {
  fetchDetail();
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

.actions {
  display: flex;
  gap: 8px;
}

.mt12 {
  margin-top: 12px;
}
</style>
