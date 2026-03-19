<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="left">
            <el-button link type="primary" @click="goBack">返回</el-button>
            <div class="title">收付款详情</div>
            <el-tag :type="payment.status === 'DONE' ? 'success' : 'info'">
              {{ payment.status === 'DONE' ? '已完成' : '未完成' }}
            </el-tag>
          </div>
          <div class="actions">
            <el-button size="small" type="primary" :disabled="!canManage" @click="goEdit">
              编辑记录
            </el-button>
            <el-button size="small" :disabled="true">打印凭证</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="3" border class="summary">
        <el-descriptions-item label="合同编码">{{ payment.contractCode }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ payment.contractName }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ payment.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="payment.type === 'PAY' ? 'warning' : 'success'">
            {{ payment.type === 'PAY' ? '付款' : '收款' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划金额(万)">{{ payment.planAmount }}</el-descriptions-item>
        <el-descriptions-item label="实际金额(万)">{{ payment.actualAmount }}</el-descriptions-item>
        <el-descriptions-item label="计划日期">{{ payment.planDate }}</el-descriptions-item>
        <el-descriptions-item label="完成日期">{{ payment.doneDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ payment.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="对账信息" name="reconcile">
          <el-alert title="这里用于展示对账/开票/回款等信息摘要，后续对接接口后补齐。" type="info" show-icon />
          <el-table :data="invoiceList" border style="width: 100%; margin-top: 12px">
            <el-table-column prop="invoiceNo" label="发票号" min-width="180" />
            <el-table-column prop="amount" label="金额(万)" min-width="140" />
            <el-table-column prop="date" label="日期" min-width="140" />
            <el-table-column prop="status" label="状态" min-width="120" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="关联附件" name="attachments">
          <el-alert title="附件独立库：这里展示摘要并可跳转到合同附件库。" type="info" show-icon />
          <div class="attach-actions">
            <el-button size="small" type="primary" @click="goAttachmentLib">打开合同附件库</el-button>
            <el-button size="small" :disabled="true">上传凭证</el-button>
          </div>
          <el-table :data="files" border style="width: 100%">
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
          <el-table :data="logs" border style="width: 100%">
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
import { getMenuLevel } from '@/utils/permission';

type PayType = 'PAY' | 'RECEIVE';
type PayStatus = 'PENDING' | 'DONE';

const route = useRoute();
const router = useRouter();

const canManage = computed(() => getMenuLevel('contracts:payment') >= 3);
const activeTab = ref('reconcile');

const payment = reactive({
  paymentId: '',
  contractCode: '',
  contractName: '',
  supplierName: '',
  type: 'PAY' as PayType,
  planAmount: 0,
  actualAmount: 0,
  planDate: '',
  doneDate: '',
  status: 'PENDING' as PayStatus,
  remark: '',
});

const invoiceList = ref([
  { invoiceNo: 'FP-2025-0001', amount: 20, date: '2025-03-05', status: '已开票' },
]);

const files = ref([
  { name: '付款凭证.pdf', type: '凭证', time: '2025-03-06 10:00:00' },
]);

const logs = ref([
  { time: '2025-03-01 10:00:00', user: '李四', action: '创建记录', remark: '计划付款' },
  { time: '2025-03-06 10:00:00', user: '财务', action: '更新为已完成', remark: '上传凭证（示例）' },
]);

onMounted(() => {
  const id = route.params.paymentId as string;
  payment.paymentId = id;
  payment.contractCode = `HT-2025-${String((Number(id) % 6) + 1).padStart(4, '0')}`;
  payment.contractName = `示例合同 ${(Number(id) % 6) + 1}`;
  payment.supplierName = `供应商${String.fromCharCode(65 + (Number(id) % 5))}`;
  payment.type = Number(id) % 2 === 0 ? 'PAY' : 'RECEIVE';
  payment.planAmount = 50 + Number(id);
  payment.actualAmount = Number(id) % 3 === 0 ? 20 : 0;
  payment.planDate = '2025-03-01';
  payment.status = Number(id) % 3 === 0 ? 'DONE' : 'PENDING';
  payment.doneDate = payment.status === 'DONE' ? '2025-03-06' : '';
  payment.remark = '示例备注';
});

function goBack() {
  router.push('/contracts/payment');
}

function goAttachmentLib() {
  router.push('/contracts/attachments');
}

function goEdit() {
  // 弹窗编辑：跳回列表页由你后续扩展 query 打开弹窗
  router.push('/contracts/payment');
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
.attach-actions {
  margin: 10px 0 12px;
  display: flex;
  gap: 8px;
}
</style>


