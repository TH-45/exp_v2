<template>
  <el-card>
    <template #header>
      <div class="header">
        <div class="title">流程定义</div>
        <el-button type="primary" size="small" @click="openDefDialog()">新增流程</el-button>
      </div>
    </template>

    <el-table :data="defList" border v-loading="loading">
      <el-table-column prop="procCode" label="流程编码" min-width="160" />
      <el-table-column prop="procName" label="流程名称" min-width="180" />
      <el-table-column prop="busType" label="业务类型" min-width="140" />
      <el-table-column prop="version" label="版本" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'">{{ row.isActive ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="openDefDialog(row)">编辑</el-button>
          <el-button link type="primary" @click.stop="openNodeConfig(row)">配置节点</el-button>
          <el-button link type="success" @click.stop="toggleActive(row)">
            {{ row.isActive ? '停用' : '启用' }}
          </el-button>
          <el-button link type="warning" @click.stop="openCopyDialog(row)">复制</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="defDialog.visible" :title="defDialog.isEdit ? '编辑流程' : '新增流程'" width="620px" draggable destroy-on-close>
      <el-form :model="defForm" label-width="100px">
        <el-form-item label="流程编码"><el-input v-model="defForm.procCode" /></el-form-item>
        <el-form-item label="流程名称"><el-input v-model="defForm.procName" /></el-form-item>
        <el-form-item label="业务类型"><el-input v-model="defForm.busType" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="defForm.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="defDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitDef">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="copyDialog.visible" title="复制流程" width="520px" draggable destroy-on-close>
      <el-form :model="copyForm" label-width="100px">
        <el-form-item label="新流程编码"><el-input v-model="copyForm.newProcCode" /></el-form-item>
        <el-form-item label="新流程名称"><el-input v-model="copyForm.newProcName" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="copyDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitCopy">确认复制</el-button>
      </template>
    </el-dialog>

  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  activateProcessDefinition,
  copyProcessDefinition,
  listProcessDefinitions,
  saveProcessDefinition,
  type ProcessDefinition,
} from '@/api/process/definition';

const router = useRouter();
const loading = ref(false);
const defList = ref<ProcessDefinition[]>([]);

const defDialog = reactive({ visible: false, isEdit: false });
const copyDialog = reactive({ visible: false, sourceProcDefId: 0 });

const defForm = reactive<ProcessDefinition>({
  procCode: '',
  procName: '',
  busType: '',
  remark: '',
});
const copyForm = reactive({ newProcCode: '', newProcName: '' });

async function loadDefs() {
  loading.value = true;
  try {
    const res = await listProcessDefinitions({ pageNum: 1, pageSize: 200 });
    defList.value = res.list || [];
  } finally {
    loading.value = false;
  }
}

function openDefDialog(row?: ProcessDefinition) {
  defDialog.visible = true;
  defDialog.isEdit = !!row?.procDefId;
  defForm.procDefId = row?.procDefId;
  defForm.procCode = row?.procCode || '';
  defForm.procName = row?.procName || '';
  defForm.busType = row?.busType || '';
  defForm.remark = row?.remark || '';
  defForm.isActive = row?.isActive ?? 1;
  defForm.version = row?.version ?? 1;
}

async function submitDef() {
  await saveProcessDefinition(defForm);
  ElMessage.success('保存成功');
  defDialog.visible = false;
  await loadDefs();
}

async function toggleActive(row: ProcessDefinition) {
  if (!row.procDefId) return;
  await activateProcessDefinition(row.procDefId, row.isActive ? 0 : 1);
  ElMessage.success('状态更新成功');
  await loadDefs();
}

function openCopyDialog(row: ProcessDefinition) {
  if (!row.procDefId) return;
  copyDialog.sourceProcDefId = row.procDefId;
  copyForm.newProcCode = `${row.procCode}_COPY`;
  copyForm.newProcName = `${row.procName}-复制`;
  copyDialog.visible = true;
}

async function submitCopy() {
  await copyProcessDefinition({
    sourceProcDefId: copyDialog.sourceProcDefId,
    newProcCode: copyForm.newProcCode,
    newProcName: copyForm.newProcName,
  });
  ElMessage.success('复制成功');
  copyDialog.visible = false;
  await loadDefs();
}

function openNodeConfig(row: ProcessDefinition) {
  if (!row.procDefId) return;
  const href = router.resolve({
    name: 'ApprovalNodeConfig',
    query: {
      procDefId: String(row.procDefId),
      procCode: row.procCode || '',
      procName: row.procName || '',
    },
  }).href;
  window.open(href, '_blank');
}

onMounted(loadDefs);
</script>

<style scoped lang="scss">
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-weight: 600;
}
</style>
