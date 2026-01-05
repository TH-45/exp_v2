<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="left">
            <el-button link type="primary" @click="goBack">返回</el-button>
            <div class="title">项目详情</div>
            <el-tag :type="statusTagType(project.status)" class="status-tag">
              {{ statusText(project.status) }}
            </el-tag>
          </div>
          <div class="actions">
            <el-button size="small" type="primary" :disabled="!canManage" @click="openEditProject">
              编辑项目
            </el-button>
            <el-button size="small" :disabled="true">操作记录</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="3" border class="summary">
        <el-descriptions-item label="项目编码">{{ project.projectCode }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ project.projectName }}</el-descriptions-item>
        <el-descriptions-item label="招标单位">{{ project.tenderOrg || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ project.ownerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年度">{{ project.year || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ project.createdTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="概览" name="overview">
          <el-alert
            title="这里汇总项目关键指标（投标数、标段数、关键节点、当前状态）。后续接接口后会替换为真实数据。"
            type="info"
            show-icon
          />
          <div class="grid">
            <el-card class="mini" shadow="never">
              <div class="metric">
                <div class="metric-label">投标登记</div>
                <div class="metric-value">{{ metrics.bidCount }}</div>
              </div>
            </el-card>
            <el-card class="mini" shadow="never">
              <div class="metric">
                <div class="metric-label">标段数量</div>
                <div class="metric-value">{{ metrics.lotCount }}</div>
              </div>
            </el-card>
            <el-card class="mini" shadow="never">
              <div class="metric">
                <div class="metric-label">附件数量</div>
                <div class="metric-value">{{ metrics.fileCount }}</div>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <el-tab-pane label="参与方" name="party">
          <el-table :data="partyList" border style="width: 100%">
            <el-table-column prop="type" label="类型" min-width="120" />
            <el-table-column prop="name" label="名称" min-width="220" />
            <el-table-column prop="contact" label="联系人" min-width="140" />
            <el-table-column prop="mobile" label="电话" min-width="140" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="时间节点" name="milestone">
          <el-timeline>
            <el-timeline-item
              v-for="(m, idx) in milestones"
              :key="idx"
              :timestamp="m.time"
              :type="m.type"
            >
              {{ m.title }}
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>

        <el-tab-pane label="投标登记" name="bids">
          <el-table :data="bidList" border style="width: 100%">
            <el-table-column prop="bidder" label="投标人" min-width="220" />
            <el-table-column prop="amount" label="报价(万)" min-width="120" />
            <el-table-column prop="status" label="状态" min-width="120" />
            <el-table-column prop="time" label="登记时间" min-width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="评标/定标" name="evaluation">
          <el-alert
            title="无审批流阶段：支持录入评标结果与推荐中标人（示例）。后续接审批流时可在此页挂上提交/审核动作。"
            type="warning"
            show-icon
          />
          <el-form :model="evaluation" label-width="120px" class="eval-form">
            <el-form-item label="推荐中标人">
              <el-input v-model="evaluation.winner" placeholder="请输入推荐中标人" style="width: 420px" />
            </el-form-item>
            <el-form-item label="综合评分">
              <el-input-number v-model="evaluation.score" :min="0" :max="100" />
            </el-form-item>
            <el-form-item label="评审意见">
              <el-input v-model="evaluation.comment" type="textarea" :rows="4" placeholder="请输入评审意见" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" :disabled="true">保存（待接接口）</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="附件" name="attachments">
          <el-alert
            title="你选了“附件独立库”，这里仅展示与本项目关联的附件摘要，并提供跳转到招投标附件库。"
            type="info"
            show-icon
          />
          <div class="attach-actions">
            <el-button size="small" type="primary" @click="goAttachmentLib">打开招投标附件库</el-button>
            <el-button size="small" :disabled="true">上传附件</el-button>
          </div>
          <el-table :data="fileList" border style="width: 100%">
            <el-table-column prop="name" label="文件名" min-width="260" />
            <el-table-column prop="type" label="类型" min-width="140" />
            <el-table-column prop="time" label="上传时间" min-width="170" />
            <el-table-column label="操作" fixed="right" width="120">
              <template #default>
                <el-button link size="small" :disabled="true">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="日志" name="logs">
          <el-table :data="logList" border style="width: 100%">
            <el-table-column prop="time" label="时间" min-width="170" />
            <el-table-column prop="user" label="操作人" min-width="140" />
            <el-table-column prop="action" label="动作" min-width="180" />
            <el-table-column prop="remark" label="说明" min-width="240" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { hasPermission } from '@/utils/permission';

type BiddingProjectStatus =
  | 'DRAFT'
  | 'PUBLISHED'
  | 'BIDDING'
  | 'EVALUATING'
  | 'AWARDED'
  | 'ARCHIVED';

const route = useRoute();
const router = useRouter();

const canManage = computed(() => hasPermission('bidding:project:manage'));

const statusOptions: Array<{ label: string; value: BiddingProjectStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '投标中', value: 'BIDDING' },
  { label: '评标中', value: 'EVALUATING' },
  { label: '已定标', value: 'AWARDED' },
  { label: '已归档', value: 'ARCHIVED' },
];

function statusText(s: BiddingProjectStatus) {
  return statusOptions.find((x) => x.value === s)?.label || s;
}

function statusTagType(s: BiddingProjectStatus) {
  if (s === 'DRAFT') return 'info';
  if (s === 'PUBLISHED') return 'success';
  if (s === 'BIDDING') return 'warning';
  if (s === 'EVALUATING') return 'warning';
  if (s === 'AWARDED') return 'success';
  if (s === 'ARCHIVED') return 'info';
  return '';
}

const activeTab = ref('overview');

const project = reactive({
  projectId: '',
  projectCode: '',
  projectName: '',
  tenderOrg: '',
  ownerName: '',
  year: new Date().getFullYear(),
  status: 'DRAFT' as BiddingProjectStatus,
  createdTime: '',
});

const metrics = reactive({
  bidCount: 8,
  lotCount: 2,
  fileCount: 15,
});

const partyList = ref([
  { type: '招标单位', name: '总部', contact: '张三', mobile: '13800000000' },
  { type: '代理机构', name: '示例代理公司', contact: '李四', mobile: '13900000000' },
]);

const milestones = ref([
  { time: '2025-01-01', title: '项目创建', type: 'primary' },
  { time: '2025-01-10', title: '发布招标公告', type: 'success' },
  { time: '2025-01-20', title: '投标截止', type: 'warning' },
  { time: '2025-01-25', title: '评标完成', type: 'info' },
]);

const bidList = ref([
  { bidder: '供应商A', amount: 120, status: '已提交', time: '2025-01-15 10:00:00' },
  { bidder: '供应商B', amount: 118, status: '已提交', time: '2025-01-15 11:20:00' },
]);

const evaluation = reactive({
  winner: '供应商B',
  score: 92,
  comment: '综合评分最高，报价合理。',
});

const fileList = ref([
  { name: '招标文件.pdf', type: '招标文件', time: '2025-01-10 09:00:00' },
  { name: '评标报告.docx', type: '评标报告', time: '2025-01-25 18:00:00' },
]);

const logList = ref([
  { time: '2025-01-01 10:00:00', user: 'admin', action: '创建项目', remark: '初始化项目' },
  { time: '2025-01-10 09:10:00', user: '张三', action: '发布公告', remark: '发布成功' },
]);

onMounted(() => {
  const projectId = route.params.projectId as string;
  project.projectId = projectId;
  // mock：根据 id 填充展示
  project.projectCode = `TB-${project.year}-${String(projectId).padStart(3, '0')}`;
  project.projectName = `示例招标项目 ${projectId}`;
  project.tenderOrg = '总部';
  project.ownerName = '张三';
  project.status = statusOptions[Number(projectId) % statusOptions.length].value;
  project.createdTime = '2025-01-01 10:00:00';
});

function goBack() {
  router.push('/bidding/project');
}

function goAttachmentLib() {
  router.push('/bidding/attachments');
}

function openEditProject() {
  // 你选了 3.B：编辑从列表弹窗为主；详情页的编辑入口先占位，后面可联动同一弹窗或跳转编辑页
  router.push({ path: '/bidding/project', query: { edit: project.projectId } });
}
</script>

<style scoped lang="scss">
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-weight: 600;
}

.status-tag {
  margin-left: 4px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary {
  margin-bottom: 12px;
}

.tabs {
  margin-top: 8px;
}

.grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.mini :deep(.el-card__body) {
  padding: 14px;
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-label {
  color: #666;
  font-size: 12px;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
}

.eval-form {
  margin-top: 12px;
  max-width: 860px;
}

.attach-actions {
  margin: 10px 0 12px;
  display: flex;
  gap: 8px;
}
</style>


