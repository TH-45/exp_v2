<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">评标 / 定标</div>
          <div class="actions">
            <el-button type="primary" size="small" :disabled="!canManage" @click="saveEvaluation">
              保存结果
            </el-button>
            <el-button size="small" :disabled="true">导出评标报告</el-button>
          </div>
        </div>
      </template>

      <!-- 选择项目 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="项目">
          <el-input v-model="query.projectKeyword" placeholder="项目编码/名称（占位）" clearable style="width: 260px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 180px">
            <el-option label="评标中" value="EVALUATING" />
            <el-option label="已定标" value="AWARDED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        title="当前阶段无审批流：本页用于录入评分、评审意见、推荐中标人。后续增加审批流时，可在此页增加“提交/审核”动作。"
        type="info"
        show-icon
        class="tip"
      />

      <div class="split-area">
        <!-- 左侧：投标列表与评分 -->
        <el-card class="left" shadow="never">
          <template #header>
            <div class="card-title">投标列表</div>
          </template>
          <el-table v-loading="loading" :data="bidList" row-key="bidId" border style="width: 100%" @row-click="selectBid">
            <el-table-column prop="bidderName" label="投标人" min-width="200" />
            <el-table-column prop="amount" label="报价(万)" min-width="120" />
            <el-table-column label="评分" min-width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.score" :min="0" :max="100" :disabled="!canManage" />
              </template>
            </el-table-column>
            <el-table-column label="推荐" min-width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isWinner" type="success">中标</el-tag>
                <el-button v-else link size="small" :disabled="!canManage" @click.stop="setWinner(row)">
                  设为中标
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 右侧：评审意见 -->
        <el-card class="right" shadow="never">
          <template #header>
            <div class="card-title">评审意见</div>
          </template>

          <el-form :model="evaluation" label-width="120px" class="eval-form">
            <el-form-item label="当前投标人">
              <el-input :model-value="currentBid?.bidderName || '-'" disabled />
            </el-form-item>
            <el-form-item label="综合评分">
              <el-input-number v-model="evaluation.score" :min="0" :max="100" :disabled="!canManage" />
            </el-form-item>
            <el-form-item label="评审意见">
              <el-input v-model="evaluation.comment" type="textarea" :rows="6" :disabled="!canManage" placeholder="请输入评审意见" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" :disabled="!canManage || !currentBid" @click="applyToBid">
                应用到当前投标
              </el-button>
              <el-button size="small" :disabled="true">上传评标附件</el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <el-descriptions :column="1" border>
            <el-descriptions-item label="推荐中标人">
              {{ winnerName }}
            </el-descriptions-item>
            <el-descriptions-item label="最高评分">
              {{ bestScore }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage } from 'element-plus';
import { hasPermission } from '@/utils/permission';

type EvalStatus = 'EVALUATING' | 'AWARDED';

type EvalBidRow = {
  bidId: string;
  bidderName: string;
  amount: number;
  score: number;
  comment?: string;
  isWinner: boolean;
};

const canManage = computed(() => hasPermission('bidding:evaluation:manage'));

const query = reactive({
  projectKeyword: '',
  status: undefined as EvalStatus | undefined,
});

const loading = ref(false);

const bidList = ref<EvalBidRow[]>([]);
const currentBid = ref<EvalBidRow | null>(null);

const evaluation = reactive({
  score: 0,
  comment: '',
});

const winnerName = computed(() => bidList.value.find((b) => b.isWinner)?.bidderName || '-');
const bestScore = computed(() => {
  if (!bidList.value.length) return '-';
  const max = Math.max(...bidList.value.map((b) => b.score ?? 0));
  return Number.isFinite(max) ? String(max) : '-';
});

function mockFetch() {
  bidList.value = [
    { bidId: '1', bidderName: '供应商A', amount: 120, score: 86, comment: '报价较高', isWinner: false },
    { bidId: '2', bidderName: '供应商B', amount: 118, score: 92, comment: '综合最优', isWinner: true },
    { bidId: '3', bidderName: '供应商C', amount: 119, score: 88, comment: '方案可行', isWinner: false },
  ];
  currentBid.value = bidList.value[0];
  evaluation.score = currentBid.value.score;
  evaluation.comment = currentBid.value.comment || '';
}

async function fetchList() {
  loading.value = true;
  try {
    // 后续接接口：按 query.projectKeyword / query.status 获取项目的投标与评分
    mockFetch();
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchList();
});

function handleSearch() {
  query.projectKeyword = (query.projectKeyword || '').trim();
  fetchList();
}

function handleReset() {
  query.projectKeyword = '';
  query.status = undefined;
  fetchList();
}

function selectBid(row: EvalBidRow) {
  currentBid.value = row;
  evaluation.score = row.score;
  evaluation.comment = row.comment || '';
}

function applyToBid() {
  if (!currentBid.value) return;
  currentBid.value.score = evaluation.score;
  currentBid.value.comment = evaluation.comment;
  ElMessage.success('已应用到当前投标（示例模式）');
}

function setWinner(row: EvalBidRow) {
  bidList.value.forEach((b) => (b.isWinner = b.bidId === row.bidId));
  ElMessage.success(`已设为中标：${row.bidderName}（示例模式）`);
}

function saveEvaluation() {
  ElMessage.success('评标结果已保存（示例模式）');
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

.tip {
  margin-bottom: 12px;
}

.split-area {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 12px;
}

.card-title {
  font-weight: 600;
}

.left :deep(.el-card__body),
.right :deep(.el-card__body) {
  padding: 12px;
}

.eval-form {
  max-width: 680px;
}
</style>


