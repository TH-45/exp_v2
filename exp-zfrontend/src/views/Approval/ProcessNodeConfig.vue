<template>
  <el-card>
    <template #header>
      <div class="header">
        <div class="title">
          节点配置 - {{ procName || '-' }}
          <span class="sub">（{{ procCode || '-' }}）</span>
        </div>
        <div class="actions">
          <el-button type="primary" size="small" @click="openNodeDialog()">新增节点</el-button>
          <el-button size="small" @click="resetDraft">重置</el-button>
          <el-button type="success" size="small" :loading="saving" @click="saveAll">保存</el-button>
        </div>
      </div>
    </template>

    <el-alert
      title="本页面支持多标签并行配置。仅点击“保存”才会写入数据库；未保存直接关闭标签页将自动清理本页缓存。"
      type="info"
      show-icon
      :closable="false"
      class="tip"
    />

    <el-table :data="draftNodes" row-key="draftKey" border @row-dblclick="openNodeDialog">
      <el-table-column prop="sortNo" label="序号" width="80" />
      <el-table-column prop="nodeName" label="节点名称" min-width="180" />
      <el-table-column label="审批类型" width="120">
        <template #default="{ row }">{{ approveTypeLabel(row.approveType) }}</template>
      </el-table-column>
      <el-table-column label="审批人" min-width="180">
        <template #default="{ row }">{{ row.assigneeDisplayName || row.assigneeId || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openNodeDialog(row)">编辑</el-button>
          <el-button link @click="moveUp(row)">上移</el-button>
          <el-button link @click="moveDown(row)">下移</el-button>
          <el-button link type="danger" @click="removeNode(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="nodeDialog.visible" :title="nodeDialog.isEdit ? '编辑节点' : '新增节点'" width="620px" draggable destroy-on-close>
      <el-form ref="nodeFormRef" :model="nodeForm" :rules="nodeRules" label-width="100px">
        <el-form-item label="节点名称" prop="nodeName" required>
          <el-input v-model="nodeForm.nodeName" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="审批类型" prop="approveType" required>
          <el-select v-model="nodeForm.approveType" placeholder="请选择审批类型" style="width: 100%">
            <el-option label="单签" value="OR" />
            <el-option label="会签" value="AND" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批人" prop="assigneePerson" required>
          <PersonSelector v-model="nodeForm.assigneePerson" placeholder="请选择审批人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitNode">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { onBeforeRouteLeave, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { deleteProcessNode, getProcessDefinitionDetail, saveProcessNode, type ProcessNode } from '@/api/process/definition';
import type { ExpPersonVO } from '@/api/system/person';
import PersonSelector from '@/components/Selector/PersonSelector.vue';

const route = useRoute();
const procDefId = Number(route.query.procDefId || 0);
const procName = String(route.query.procName || '');
const procCode = String(route.query.procCode || '');
const saving = ref(false);
let draftSeed = 1;

type DraftNode = ProcessNode & { draftKey: string };

const nodeFormRef = ref<FormInstance>();
const nodeDialog = reactive({ visible: false, isEdit: false, editIndex: -1 });
const nodeForm = reactive<DraftNode & { assigneePerson?: ExpPersonVO }>({
  procDefId,
  draftKey: '',
  nodeName: '',
  sortNo: 1,
  approveType: 'OR',
  assigneeType: 'USER',
  assigneeId: '',
  assigneePerson: undefined,
});

const nodeRules: FormRules = {
  nodeName: [{ required: true, message: '请输入节点名称', trigger: 'blur' }],
  approveType: [{ required: true, message: '请选择审批类型', trigger: 'change' }],
  assigneePerson: [
    {
      validator: (_rule, _val, cb) => {
        if (!nodeForm.assigneePerson?.personId) cb(new Error('请选择审批人'));
        else cb();
      },
      trigger: 'change',
    },
  ],
};

/** 审批类型 value 转中文 */
function approveTypeLabel(value?: string) {
  if (value === 'OR') return '单签';
  if (value === 'AND') return '会签';
  return value ?? '-';
}

const originalNodes = ref<DraftNode[]>([]);
const draftNodes = ref<DraftNode[]>([]);
const deletedNodeIds = ref<number[]>([]);
const dirty = ref(false);
const draftKey = `process-node-draft:${procDefId}`;

function markDirty() {
  dirty.value = true;
  sessionStorage.setItem(
    draftKey,
    JSON.stringify({
      nodes: draftNodes.value,
      deletedNodeIds: deletedNodeIds.value,
      updatedAt: Date.now(),
    }),
  );
}

function clearDraft() {
  sessionStorage.removeItem(draftKey);
  deletedNodeIds.value = [];
}

function newDraftKey(nodeId?: number) {
  if (nodeId != null) {
    return `db-${nodeId}`;
  }
  draftSeed += 1;
  return `tmp-${Date.now()}-${draftSeed}`;
}

function toDraftNode(node: ProcessNode): DraftNode {
  return {
    ...node,
    draftKey: newDraftKey(node.nodeId),
  };
}

async function loadData() {
  if (!procDefId) {
    ElMessage.error('缺少 procDefId 参数');
    return;
  }
  const detail = await getProcessDefinitionDetail(procDefId);
  originalNodes.value = (detail.nodes || []).map((n) => toDraftNode(n));
  draftNodes.value = (detail.nodes || []).map((n) => toDraftNode(n));

  const cache = sessionStorage.getItem(draftKey);
  if (cache) {
    try {
      const parsed = JSON.parse(cache);
      draftNodes.value = Array.isArray(parsed.nodes)
        ? parsed.nodes.map((n: ProcessNode & { draftKey?: string }) => ({
            ...n,
            draftKey: n.draftKey || newDraftKey(n.nodeId),
          }))
        : draftNodes.value;
      deletedNodeIds.value = Array.isArray(parsed.deletedNodeIds) ? parsed.deletedNodeIds : [];
      dirty.value = true;
    } catch {
      clearDraft();
    }
  }
}

function openNodeDialog(row?: DraftNode) {
  nodeDialog.visible = true;
  if (!row) {
    nodeDialog.isEdit = false;
    nodeDialog.editIndex = -1;
    nodeForm.nodeId = undefined;
    nodeForm.procDefId = procDefId;
    nodeForm.draftKey = newDraftKey();
    nodeForm.nodeName = '';
    nodeForm.sortNo = draftNodes.value.length + 1;
    nodeForm.approveType = 'OR';
    nodeForm.assigneeType = 'USER';
    nodeForm.assigneeId = '';
    nodeForm.assigneePerson = undefined;
    nodeForm.assigneeDisplayName = undefined;
    return;
  }
  nodeDialog.isEdit = true;
  nodeDialog.editIndex = draftNodes.value.findIndex((x) => x.draftKey === row.draftKey);
  nodeForm.nodeId = row.nodeId;
  nodeForm.procDefId = procDefId;
  nodeForm.draftKey = row.draftKey;
  nodeForm.nodeName = row.nodeName;
  nodeForm.sortNo = row.sortNo;
  nodeForm.approveType = row.approveType;
  nodeForm.assigneeType = 'USER';
  nodeForm.assigneeId = row.assigneeId;
  // 从 assigneeId + assigneeDisplayName 构造 PersonSelector 所需对象
  nodeForm.assigneePerson =
    row.assigneeId && row.assigneeId.trim()
      ? {
          personId: Number(row.assigneeId),
          personCode: '',
          personName: row.assigneeDisplayName ?? '',
          gender: 'M',
          status: 'ONJOB',
        }
      : undefined;
}

async function submitNode() {
  await nodeFormRef.value?.validate();
  const person = nodeForm.assigneePerson;
  if (!person?.personId) {
    ElMessage.warning('请选择审批人');
    return;
  }
  const data: DraftNode = {
    nodeId: nodeForm.nodeId,
    procDefId,
    draftKey: nodeForm.draftKey || newDraftKey(nodeForm.nodeId),
    nodeName: nodeForm.nodeName.trim(),
    sortNo: nodeForm.sortNo,
    approveType: nodeForm.approveType,
    assigneeType: 'USER',
    assigneeId: String(person.personId),
    assigneeDisplayName: person.personName,
  };
  if (nodeDialog.isEdit && nodeDialog.editIndex > -1) {
    draftNodes.value.splice(nodeDialog.editIndex, 1, data);
  } else {
    draftNodes.value.push(data);
  }
  normalizeSortNo();
  nodeDialog.visible = false;
  markDirty();
}

function normalizeSortNo() {
  draftNodes.value.forEach((x, idx) => {
    x.sortNo = idx + 1;
  });
}

function removeNode(row: DraftNode) {
  const idx = draftNodes.value.findIndex((x) => x.draftKey === row.draftKey);
  if (idx < 0) return;
  const deleting = draftNodes.value[idx];
  if (deleting.nodeId) {
    deletedNodeIds.value.push(deleting.nodeId);
  }
  draftNodes.value.splice(idx, 1);
  normalizeSortNo();
  markDirty();
}

function moveUp(row: DraftNode) {
  const idx = draftNodes.value.findIndex((x) => x.draftKey === row.draftKey);
  if (idx <= 0) return;
  const cloned = [...draftNodes.value];
  [cloned[idx - 1], cloned[idx]] = [cloned[idx], cloned[idx - 1]];
  draftNodes.value = cloned;
  normalizeSortNo();
  markDirty();
}

function moveDown(row: DraftNode) {
  const idx = draftNodes.value.findIndex((x) => x.draftKey === row.draftKey);
  if (idx < 0 || idx >= draftNodes.value.length - 1) return;
  const cloned = [...draftNodes.value];
  [cloned[idx], cloned[idx + 1]] = [cloned[idx + 1], cloned[idx]];
  draftNodes.value = cloned;
  normalizeSortNo();
  markDirty();
}

async function saveAll() {
  saving.value = true;
  try {
    normalizeSortNo();
    for (const node of draftNodes.value) {
      await saveProcessNode({
        nodeId: node.nodeId,
        procDefId,
        nodeName: node.nodeName,
        sortNo: node.sortNo,
        approveType: node.approveType,
        assigneeType: 'USER',
        assigneeId: node.assigneeId,
      });
    }
    for (const nodeId of Array.from(new Set(deletedNodeIds.value))) {
      await deleteProcessNode(nodeId);
    }
    dirty.value = false;
    clearDraft();
    await loadData();
    ElMessage.success('保存成功');
  } finally {
    saving.value = false;
  }
}

async function resetDraft() {
  if (!dirty.value) return;
  await ElMessageBox.confirm('重置将丢弃当前未保存修改，是否继续？', '提示', { type: 'warning' });
  clearDraft();
  dirty.value = false;
  await loadData();
}

function handleBeforeUnload() {
  // 用户关闭标签页时，不保留草稿缓存
  clearDraft();
}

onBeforeRouteLeave(async (_to, _from, next) => {
  if (!dirty.value) {
    clearDraft();
    next();
    return;
  }
  try {
    await ElMessageBox.confirm('存在未保存修改，离开后将清理当前页缓存，是否继续？', '提示', { type: 'warning' });
    clearDraft();
    next();
  } catch {
    next(false);
  }
});

onMounted(async () => {
  window.addEventListener('beforeunload', handleBeforeUnload);
  await loadData();
});

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload);
  clearDraft();
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
  font-size: 18px;
}
.sub {
  font-size: 13px;
  color: #909399;
}
.actions {
  display: flex;
  gap: 8px;
}
.tip {
  margin-bottom: 12px;
}
</style>
