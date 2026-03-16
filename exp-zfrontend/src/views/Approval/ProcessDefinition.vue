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
      <el-table-column label="业务类型" min-width="140">
        <template #default="{ row }">{{ busTypeLabel(row.busType) }}</template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'">{{ row.isActive ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="380" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="openDefDialog(row)">编辑</el-button>
          <el-button link type="primary" @click.stop="openNodeConfig(row)">配置节点</el-button>
          <el-button link type="success" @click.stop="toggleActive(row)">
            {{ row.isActive ? '停用' : '启用' }}
          </el-button>
          <el-button link type="warning" @click.stop="openCopyDialog(row)">复制</el-button>
          <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="defDialog.visible" :title="defDialog.isEdit ? '编辑流程' : '新增流程'" width="620px" draggable destroy-on-close>
      <el-form ref="defFormRef" :model="defForm" :rules="defRules" label-width="100px">
        <el-form-item label="流程编码" prop="procCode" required>
          <el-input v-model="defForm.procCode" placeholder="请输入流程编码" />
        </el-form-item>
        <el-form-item label="流程名称" prop="procName" required>
          <el-input v-model="defForm.procName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="业务类型" prop="busType" required>
          <el-select v-model="defForm.busType" placeholder="请选择业务类型" clearable style="width: 100%">
            <el-option
              v-for="opt in busTypeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import {
  activateProcessDefinition,
  copyProcessDefinition,
  deleteProcessDefinition,
  listProcessDefinitions,
  saveProcessDefinition,
  type ProcessDefinition,
} from '@/api/process/definition';
import { listDictOptions, type DictOption } from '@/api/system/dict';

const router = useRouter();
const loading = ref(false);
const defList = ref<ProcessDefinition[]>([]);
const defFormRef = ref<FormInstance>();
const busTypeOptions = ref<DictOption[]>([]);

const defDialog = reactive({ visible: false, isEdit: false });
const copyDialog = reactive({ visible: false, sourceProcDefId: 0 });

const defForm = reactive<ProcessDefinition>({
  procCode: '',
  procName: '',
  busType: '',
  remark: '',
});
const copyForm = reactive({ newProcCode: '', newProcName: '' });

const defRules: FormRules = {
  procCode: [{ required: true, message: '请输入流程编码', trigger: 'blur' }],
  procName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
  busType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
};

/** 业务类型 value 转中文显示 */
function busTypeLabel(value?: string) {
  if (!value) return '-';
  const opt = busTypeOptions.value.find((o) => o.value === value);
  return opt?.label ?? value;
}

async function loadBusTypeOptions() {
  try {
    const res = await listDictOptions('ProcessType');
    busTypeOptions.value = Array.isArray(res) ? res : (res as { data?: DictOption[] })?.data ?? [];
  } catch {
    busTypeOptions.value = [];
  }
}

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
  await defFormRef.value?.validate();
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

async function handleDelete(row: ProcessDefinition) {
  if (!row.procDefId) return;
  await ElMessageBox.confirm(`确定要删除流程「${row.procName}」吗？删除后不可恢复。`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  });
  try {
    await deleteProcessDefinition(row.procDefId);
    ElMessage.success('删除成功');
    await loadDefs();
  } catch (e: unknown) {
    const msg = (e as { message?: string })?.message ?? '删除失败';
    ElMessage.error(msg);
  }
}

onMounted(() => {
  loadBusTypeOptions();
  loadDefs();
});
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
