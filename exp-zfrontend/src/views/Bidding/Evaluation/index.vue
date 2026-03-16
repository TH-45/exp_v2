<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">评标 / 定标</div>
          <div class="actions">
            <el-button type="primary" size="small" :disabled="!canManage" :loading="saveLoading" @click="saveEvaluation">
              保存结果
            </el-button>
            <el-button size="small" :disabled="true">导出评标报告</el-button>
          </div>
        </div>
      </template>

      <!-- 选择项目 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="项目">
          <el-select
            v-model="query.tenderId"
            filterable
            clearable
            placeholder="请选择项目（仅展示可进入评标/定标流程的项目）"
            style="width: 360px"
          >
            <el-option
              v-for="item in eligibleTenderOptions"
              :key="item.tenderId"
              :label="`${item.tenderCode || '-'} / ${item.tenderName || '-'}`"
              :value="item.tenderId"
            />
          </el-select>
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

      <div class="split-area">
        <!-- 投标列表（主区域） -->
        <el-card class="left" shadow="never">
          <template #header>
            <div class="card-title">投标列表</div>
          </template>
          <el-table
            v-loading="loading"
            :data="bidList"
            row-key="bidId"
            border
            style="width: 100%"
            @row-click="selectBid"
            @row-dblclick="openOpinionDialog"
          >
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
            <el-table-column label="操作" min-width="120">
              <template #default="{ row }">
                <el-button link size="small" @click.stop="openOpinionDialog(row)">评审意见</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 推荐中标信息（保留在主页面） -->
      <el-card shadow="never" class="summary-card">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="推荐中标人">
            {{ winnerName }}
          </el-descriptions-item>
          <el-descriptions-item label="最高评分">
            {{ bestScore }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 审批动作区域（保留在主页面底部） -->
      <el-card shadow="never" class="approval-card">
        <el-form :model="processForm" label-width="120px">
          <el-form-item label="审批动作">
            <el-radio-group v-model="processForm.action">
              <el-radio label="APPROVE">通过</el-radio>
              <el-radio label="REJECT">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="processForm.action === 'REJECT'" label="驳回原因码">
            <el-select v-model="processForm.rejectReasonCode" style="width: 260px">
              <el-option label="文档补正（默认回退RESULT_CONFIRMED）" value="DOC_FIX" />
              <el-option label="需复评（回退SCORING）" value="SCORE_REWORK" />
            </el-select>
          </el-form-item>
          <el-form-item label="审批意见">
            <el-input
              v-model="processForm.opinion"
              type="textarea"
              :rows="3"
              placeholder="请输入审批意见"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="success"
              size="small"
              :disabled="!canManage"
              :loading="decisionLoading"
              @click="submitDecision('APPROVE')"
            >
              审批通过
            </el-button>
            <el-button
              type="warning"
              size="small"
              :disabled="!canManage"
              :loading="decisionLoading"
              @click="submitDecision('REJECT')"
            >
              审批驳回
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 评审意见弹窗 -->
      <el-dialog
        v-model="opinionDialogVisible"
        :title="`评审意见 - ${currentBid?.bidderName || '-'}`"
        width="640px"
        destroy-on-close
        draggable
      >
        <el-form :model="evaluation" label-width="120px" class="eval-form">
          <el-form-item label="当前投标人">
            <el-input :model-value="currentBid?.bidderName || '-'" disabled />
          </el-form-item>
          <el-form-item label="综合评分">
            <el-input-number v-model="evaluation.score" :min="0" :max="100" :disabled="!canManage" />
          </el-form-item>
          <el-form-item label="评审意见">
            <el-input
              v-model="evaluation.comment"
              type="textarea"
              :rows="6"
              :disabled="!canManage"
              placeholder="请输入评审意见"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="opinionDialogVisible = false">取 消</el-button>
            <el-button type="primary" :disabled="!canManage || !currentBid" @click="handleApplyOpinion">
              应用到当前投标
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import { queryEvaluationEligibleBiddingProjectList, type TenderVO } from '@/api/bidding/project';
import { getTenderBids } from '@/api/bidding/bid';
import {
  createAwardResult,
  getAwardResultByTender,
  processAwardDecision,
  type AwardResultVO,
} from '@/api/bidding/award';
import {
  generateEvaluationResult,
  getCommitteesByTender,
  getEvaluationResultByBid,
  updateEvaluationResult,
  type EvaluationResultVO,
} from '@/api/bidding/evaluation';

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
  tenderId: undefined as string | undefined,
  status: undefined as EvalStatus | undefined,
});

const loading = ref(false);
const eligibleTenderOptions = ref<TenderVO[]>([]);
const decisionLoading = ref(false);
const saveLoading = ref(false);
const committeeId = ref<number | null>(null);
const evaluationResultByBidId = ref<Record<string, EvaluationResultVO>>({});

const bidList = ref<EvalBidRow[]>([]);
const currentBid = ref<EvalBidRow | null>(null);

const evaluation = reactive({
  score: 0,
  comment: '',
});

const processForm = reactive({
  action: 'APPROVE' as 'APPROVE' | 'REJECT',
  rejectReasonCode: 'DOC_FIX',
  opinion: '',
});

const opinionDialogVisible = ref(false);

const winnerName = computed(() => bidList.value.find((b) => b.isWinner)?.bidderName || '-');
const bestScore = computed(() => {
  if (!bidList.value.length) return '-';
  const max = Math.max(...bidList.value.map((b) => b.score ?? 0));
  return Number.isFinite(max) ? String(max) : '-';
});

async function fetchEligibleTenders() {
  const res = await queryEvaluationEligibleBiddingProjectList({
    pageNum: 1,
    pageSize: 200,
  });
  eligibleTenderOptions.value = Array.isArray(res?.list) ? res.list : [];
  if (!eligibleTenderOptions.value.find((item) => item.tenderId === query.tenderId)) {
    query.tenderId = eligibleTenderOptions.value[0]?.tenderId;
  }
}

async function fetchList() {
  loading.value = true;
  try {
    await fetchEligibleTenders();
    if (!query.tenderId) {
      bidList.value = [];
      currentBid.value = null;
      evaluation.score = 0;
      evaluation.comment = '';
      committeeId.value = null;
      evaluationResultByBidId.value = {};
      return;
    }

    const committees = await getCommitteesByTender(Number(query.tenderId));
    committeeId.value = committees?.[0]?.committeeId ? Number(committees[0].committeeId) : null;

    const bids = await getTenderBids(Number(query.tenderId));
    const award = await getAwardResultByTender(Number(query.tenderId)).catch(() => null);
    const winnerBidId = award?.winningBidId ? String(award.winningBidId) : '';

    const rows = (Array.isArray(bids) ? bids : []).map((item) => ({
      bidId: String(item.bidId),
      bidderName: item.supplierName || item.bidName || `投标${item.bidId}`,
      amount: Number(item.bidTotalAmount ?? 0),
      score: 0,
      comment: '',
      isWinner: winnerBidId === String(item.bidId),
    }));

    const resultMap: Record<string, EvaluationResultVO> = {};
    await Promise.all(rows.map(async (row) => {
      try {
        const result = await getEvaluationResultByBid(Number(row.bidId));
        if (result?.resultId) {
          row.score = Number(result.finalScore ?? 0);
          row.comment = result.evaluationOpinion || '';
          resultMap[row.bidId] = result;
        }
      } catch (_e) {
        // 无评标结果时忽略
      }
    }));

    bidList.value = rows;
    evaluationResultByBidId.value = resultMap;
    currentBid.value = bidList.value[0] || null;
    evaluation.score = currentBid.value?.score ?? 0;
    evaluation.comment = currentBid.value?.comment || '';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchList();
});

function handleSearch() {
  fetchList();
}

function handleReset() {
  query.tenderId = undefined;
  query.status = undefined;
  fetchList();
}

function selectBid(row: EvalBidRow) {
  currentBid.value = row;
  evaluation.score = row.score;
  evaluation.comment = row.comment || '';
}

function openOpinionDialog(row: EvalBidRow) {
  selectBid(row);
  opinionDialogVisible.value = true;
}

function applyToBid() {
  if (!currentBid.value) return;
  currentBid.value.score = evaluation.score;
  currentBid.value.comment = evaluation.comment;
  ElMessage.success('已应用到当前投标');
}

function handleApplyOpinion() {
  applyToBid();
  opinionDialogVisible.value = false;
}

function setWinner(row: EvalBidRow) {
  bidList.value.forEach((b) => (b.isWinner = b.bidId === row.bidId));
  ElMessage.success(`已设为中标：${row.bidderName}`);
}

function computeRankingMap(rows: EvalBidRow[]) {
  const sorted = [...rows].sort((a, b) => (Number(b.score) || 0) - (Number(a.score) || 0));
  const rankingMap: Record<string, number> = {};
  sorted.forEach((item, index) => {
    rankingMap[item.bidId] = index + 1;
  });
  return rankingMap;
}

async function saveEvaluation() {
  if (!query.tenderId) {
    ElMessage.warning('请先选择项目');
    return;
  }
  if (!committeeId.value) {
    ElMessage.warning('该项目尚未组建评标委员会，无法保存评标结果');
    return;
  }
  if (!bidList.value.length) {
    ElMessage.warning('暂无投标数据可保存');
    return;
  }

  saveLoading.value = true;
  try {
    const rankingMap = computeRankingMap(bidList.value);
    const top3BidIds = new Set(
      Object.entries(rankingMap)
        .filter(([, rank]) => rank <= 3)
        .map(([bidId]) => bidId),
    );

    for (const row of bidList.value) {
      const payload = {
        committeeId: Number(committeeId.value),
        bidId: Number(row.bidId),
        comprehensiveScore: Number(row.score || 0),
        finalScore: Number(row.score || 0),
        ranking: rankingMap[row.bidId],
        isRecommended: top3BidIds.has(row.bidId) ? 1 : 0,
        evaluationConclusion: Number(row.score || 0) > 0 ? '通过' : '待定',
        evaluationOpinion: row.comment || undefined,
        resultStatus: '终评',
      };
      const existing = evaluationResultByBidId.value[row.bidId];
      if (existing?.resultId) {
        const updated = await updateEvaluationResult(Number(existing.resultId), payload);
        evaluationResultByBidId.value[row.bidId] = updated;
      } else {
        const created = await generateEvaluationResult(payload);
        evaluationResultByBidId.value[row.bidId] = created;
      }
    }
    ElMessage.success('评标结果保存成功');
  } catch (e: any) {
    ElMessage.error(e?.message || '评标结果保存失败');
  } finally {
    saveLoading.value = false;
  }
}

async function ensureAwardResult(tenderId: number, winningBidId: number): Promise<AwardResultVO> {
  try {
    const existing = await getAwardResultByTender(tenderId);
    if (existing?.awardId) {
      return existing;
    }
  } catch (_e) {
    // 无定标结果时创建
  }
  return createAwardResult({
    tenderId,
    winningBidId,
    awardOpinion: evaluation.comment || undefined,
  });
}

async function submitDecision(action: 'APPROVE' | 'REJECT') {
  if (!query.tenderId) {
    ElMessage.warning('请先选择项目');
    return;
  }
  const winner = bidList.value.find((item) => item.isWinner);
  if (!winner) {
    ElMessage.warning('请先设置推荐中标人');
    return;
  }

  if (action === 'REJECT' && !processForm.rejectReasonCode) {
    ElMessage.warning('请选择驳回原因码');
    return;
  }

  decisionLoading.value = true;
  try {
    const award = await ensureAwardResult(Number(query.tenderId), Number(winner.bidId));
    await processAwardDecision({
      awardId: award.awardId,
      action,
      rejectReasonCode: action === 'REJECT' ? processForm.rejectReasonCode : undefined,
      opinion: processForm.opinion || undefined,
    });
    ElMessage.success(action === 'APPROVE' ? '审批通过成功' : '审批驳回成功');
  } catch (e: any) {
    ElMessage.error(e?.message || (action === 'APPROVE' ? '审批通过失败' : '审批驳回失败'));
  } finally {
    decisionLoading.value = false;
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

.tip {
  margin-bottom: 12px;
}

.split-area {
  display: block;
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

.summary-card {
  margin-top: 12px;
}

.approval-card {
  margin-top: 12px;
}
</style>


