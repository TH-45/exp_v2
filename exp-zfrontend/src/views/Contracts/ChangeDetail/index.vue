<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="left">
            <el-button link type="primary" @click="goBack">返回</el-button>
            <div class="title">变更详情</div>
            <el-tag :type="change.status === 'EFFECTIVE' ? 'success' : 'info'">
              {{ change.status === 'EFFECTIVE' ? '已生效' : '草稿' }}
            </el-tag>
          </div>
          <div class="actions">
            <el-button size="small" type="primary" :disabled="!canManage" @click="goEdit">
              编辑变更
            </el-button>
            <el-button size="small" :disabled="true">生成变更说明</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="3" border class="summary">
        <el-descriptions-item label="变更单号">{{ change.changeCode }}</el-descriptions-item>
        <el-descriptions-item label="合同编码">{{ change.contractCode }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ change.contractName }}</el-descriptions-item>
        <el-descriptions-item label="金额变动(万)">{{ change.deltaAmount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ change.time }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ change.creator }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="变更内容" name="content">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="变更原因">{{ change.reason }}</el-descriptions-item>
            <el-descriptions-item label="影响范围">{{ change.scope }}</el-descriptions-item>
          </el-descriptions>
          <el-table :data="items" border style="width: 100%; margin-top: 12px">
            <el-table-column prop="field" label="字段" min-width="160" />
            <el-table-column prop="before" label="变更前" min-width="220" />
            <el-table-column prop="after" label="变更后" min-width="220" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="关联附件" name="attachments">
          <el-alert title="你选了“附件独立库”，这里展示摘要并可跳转到合同附件库。" type="info" show-icon />
          <div class="attach-actions">
            <el-button size="small" type="primary" @click="goAttachmentLib">打开合同附件库</el-button>
            <el-button size="small" :disabled="true">上传变更附件</el-button>
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

type ChangeStatus = 'DRAFT' | 'EFFECTIVE';

const route = useRoute();
const router = useRouter();

const canManage = computed(() => getMenuLevel('contracts:change') >= 3);
const activeTab = ref('content');

const change = reactive({
  changeId: '',
  changeCode: '',
  contractCode: '',
  contractName: '',
  reason: '',
  scope: '',
  deltaAmount: 0,
  status: 'DRAFT' as ChangeStatus,
  time: '',
  creator: '张三',
});

const items = ref([
  { field: '合同金额', before: '200万', after: '210万' },
  { field: '交付范围', before: '范围A', after: '范围A + 增补项' },
]);

const files = ref([
  { name: '变更说明.docx', type: '变更附件', time: '2025-04-01 18:00:00' },
]);

const logs = ref([
  { time: '2025-04-01 10:00:00', user: '张三', action: '创建变更', remark: '初始化' },
  { time: '2025-04-01 18:10:00', user: '李四', action: '补充附件', remark: '示例' },
]);

onMounted(() => {
  const id = route.params.changeId as string;
  change.changeId = id;
  change.changeCode = `BG-2025-${String(id).padStart(4, '0')}`;
  change.contractCode = `HT-2025-${String((Number(id) % 6) + 1).padStart(4, '0')}`;
  change.contractName = `示例合同 ${(Number(id) % 6) + 1}`;
  change.reason = Number(id) % 2 === 0 ? '范围调整' : '金额调整';
  change.scope = '交付范围/金额/条款要点';
  change.deltaAmount = Number(id) % 2 === 0 ? 10 : -5;
  change.status = Number(id) % 3 === 0 ? 'EFFECTIVE' : 'DRAFT';
  change.time = '2025-04-01 10:00:00';
});

function goBack() {
  router.push('/contracts/change');
}

function goAttachmentLib() {
  router.push('/contracts/attachments');
}

function goEdit() {
  // 你采用弹窗编辑：跳回列表页后可自己扩展 query 打开弹窗
  router.push('/contracts/change');
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


