<template>
  <el-card>
    <template #header>
      <div class="header">
        <div class="title">我的待办中心</div>
      </div>
    </template>

    <el-row :gutter="12" class="stats">
      <el-col :span="6"><el-statistic title="待我处理" :value="stats.todoCount || 0" /></el-col>
      <el-col :span="6"><el-statistic title="我已处理" :value="stats.doneCount || 0" /></el-col>
      <el-col :span="6"><el-statistic title="我发起的" :value="stats.startedCount || 0" /></el-col>
      <el-col :span="6"><el-statistic title="已关闭" :value="stats.closedCount || 0" /></el-col>
    </el-row>

    <el-tabs v-model="query.tab" @tab-change="handleTabChange">
      <el-tab-pane label="待我处理" name="todo" />
      <el-tab-pane label="我已处理" name="done" />
      <el-tab-pane label="我发起的" name="started" />
      <el-tab-pane label="已关闭" name="closed" />
    </el-tabs>

    <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
      <el-form-item label="流程实例ID">
        <el-input v-model="query.instanceId" clearable placeholder="请输入流程实例ID" style="width: 180px" />
      </el-form-item>
      <el-form-item label="流程标题">
        <el-input v-model="query.instanceTitle" clearable placeholder="请输入流程标题" style="width: 220px" />
      </el-form-item>
      <el-form-item label="业务类型">
        <el-select v-model="query.busType" clearable placeholder="请选择业务类型" style="width: 180px">
          <el-option v-for="opt in busTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="流程状态">
        <el-select v-model="query.status" clearable placeholder="请选择流程状态" style="width: 150px">
          <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button></el-form-item>
    </el-form>

    <el-table :data="tableData" v-loading="loading" border @row-dblclick="openDetail">
      <el-table-column prop="busType" label="业务类型" min-width="120" />
      <el-table-column prop="title" label="标题/摘要" min-width="180" />
      <el-table-column prop="starterId" label="发起人" min-width="100" />
      <el-table-column prop="startTime" label="发起时间" min-width="170" />
      <el-table-column prop="currentNode" label="当前环节" min-width="150" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">
            {{ query.tab === 'todo' ? '办理' : '查看' }}
          </el-button>
          <el-button v-if="query.tab === 'started' && row.status === 'RUNNING'" link type="warning" @click="openCloseDialog(row)">
            强制关闭
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        @current-change="(p:number)=>{query.pageNum=p; fetchList();}"
        @size-change="(s:number)=>{query.pageSize=s; query.pageNum=1; fetchList();}"
      />
    </div>

    <el-dialog v-model="detailDialog.visible" title="审批详情" width="860px" draggable destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="业务类型">{{ detailDialog.data?.busType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务ID">{{ detailDialog.data?.busId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailDialog.data?.status || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ detailDialog.data?.currentNode || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="timeline-wrap">
        <el-timeline>
          <el-timeline-item
            v-for="item in detailDialog.data?.approvalHistory || []"
            :key="item.taskId"
            :timestamp="item.finishTime || item.createTime"
            :type="item.action === 'AGREE' ? 'success' : item.action === 'REJECT' ? 'danger' : 'primary'"
          >
            <div>{{ item.nodeName }} - {{ item.action }}</div>
            <div class="opinion">{{ item.opinion || '-' }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <el-form v-if="query.tab === 'todo'" :model="actionForm" label-width="90px">
        <el-form-item label="审批意见">
          <el-input v-model="actionForm.comments" type="textarea" :rows="3" placeholder="驳回时必填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
        <el-button v-if="query.tab === 'todo'" type="danger" @click="handleReject">驳回</el-button>
        <el-button v-if="query.tab === 'todo'" type="success" @click="handleApprove">同意</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="closeDialog.visible" title="强制关闭流程" width="520px" draggable destroy-on-close>
      <el-form :model="closeDialog">
        <el-form-item label="关闭原因" label-width="90px">
          <el-input v-model="closeDialog.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog.visible = false">取消</el-button>
        <el-button type="warning" @click="submitForceClose">确认关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { listDictOptions, type DictOption } from '@/api/system/dict';
import {
  approveTask,
  forceCloseInstance,
  getApprovalStats,
  listApprovalTasks,
  returnTask,
  type ApprovalDetail,
  type ApprovalStats,
  type ApprovalTask,
  type WorkbenchTab,
} from '@/api/approval';

const router = useRouter();
const loading = ref(false);
const tableData = ref<ApprovalTask[]>([]);
const total = ref(0);
const stats = reactive<ApprovalStats>({ todoCount: 0, doneCount: 0, startedCount: 0, closedCount: 0 });
const busTypeOptions = ref<DictOption[]>([]);
const statusOptions: Array<{ label: string; value: string }> = [
  { label: '审批中', value: 'APPROVING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已关闭', value: 'CLOSED' },
];
const query = reactive({
  tab: 'todo' as WorkbenchTab,
  instanceId: '',
  instanceTitle: '',
  busType: '',
  status: '',
  pageNum: 1,
  pageSize: 10,
});

const detailDialog = reactive({
  visible: false,
  data: null as ApprovalDetail | null,
});
const actionForm = reactive({ comments: '' });
const closeDialog = reactive({ visible: false, instanceId: 0, reason: '' });

async function fetchStats() {
  Object.assign(stats, await getApprovalStats());
}

async function loadBusTypeOptions() {
  try {
    const res = await listDictOptions('Business_Type');
    busTypeOptions.value = Array.isArray(res) ? res : (res as { data?: DictOption[] })?.data ?? [];
  } catch {
    busTypeOptions.value = [];
  }
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await listApprovalTasks(query);
    tableData.value = res.list || [];
    total.value = Number(res.total || 0);
  } finally {
    loading.value = false;
  }
}

async function handleTabChange() {
  query.pageNum = 1;
  await fetchList();
}

async function handleSearch() {
  query.pageNum = 1;
  await fetchList();
}

async function openDetail(row: ApprovalTask) {
  const instanceId = Number(row.instanceId || 0);
  if (!instanceId) {
    ElMessage.warning('当前记录缺少流程实例ID');
    return;
  }
  await router.push({
    path: `/approval/instance/${instanceId}`,
    query: {
      tab: query.tab,
      taskId: row.taskId ? String(row.taskId) : '',
      busType: String(row.busType || ''),
      busId: String(row.busId || ''),
    },
  });
}

async function handleApprove() {
  if (!detailDialog.data?.taskId) return;
  await approveTask({ taskId: detailDialog.data.taskId, comments: actionForm.comments });
  ElMessage.success('审批成功');
  detailDialog.visible = false;
  actionForm.comments = '';
  await refreshAll();
}

async function handleReject() {
  if (!detailDialog.data?.taskId) return;
  if (!actionForm.comments.trim()) {
    ElMessage.warning('驳回意见不能为空');
    return;
  }
  await returnTask({ taskId: detailDialog.data.taskId, comments: actionForm.comments });
  ElMessage.success('驳回成功');
  detailDialog.visible = false;
  actionForm.comments = '';
  await refreshAll();
}

function openCloseDialog(row: ApprovalTask) {
  closeDialog.instanceId = row.instanceId;
  closeDialog.reason = '';
  closeDialog.visible = true;
}

async function submitForceClose() {
  await forceCloseInstance({ instanceId: closeDialog.instanceId, reason: closeDialog.reason });
  ElMessage.success('流程已关闭');
  closeDialog.visible = false;
  await refreshAll();
}

async function refreshAll() {
  await Promise.all([fetchStats(), fetchList()]);
}

onMounted(async () => {
  await loadBusTypeOptions();
  await refreshAll();
});
</script>

<style scoped lang="scss">
.header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: 600; }
.stats { margin-bottom: 12px; }
.search-bar { margin: 12px 0; }
.pagination { margin-top: 12px; display: flex; justify-content: flex-end; }
.timeline-wrap { margin: 16px 0; max-height: 260px; overflow: auto; }
.opinion { color: #666; margin-top: 4px; }
</style>
