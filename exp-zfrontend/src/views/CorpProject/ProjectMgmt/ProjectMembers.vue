<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">项目人员配置 - {{ projectName }}</div>
          <div class="actions">
            <el-button
              type="primary"
              size="small"
              @click="openAddDialog"
              :disabled="!canUpdate"
            >
              <el-icon><Plus /></el-icon>
              添加成员
            </el-button>
            <el-button size="small" @click="exportMembers">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 项目概览 -->
      <div class="project-overview">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-statistic title="总人数" :value="memberStats.total" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="项目经理" :value="memberStats.managers" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="技术人员" :value="memberStats.technicians" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="在职人数" :value="memberStats.active" />
          </el-col>
        </el-row>
      </div>

      <!-- 组织架构图 -->
      <div class="org-structure">
        <h3>组织架构</h3>
        <div class="org-chart">
          <div class="org-node manager-node" v-if="orgStructure.manager">
            <div class="node-content">
              <div class="node-avatar">
                <el-icon size="32"><User /></el-icon>
              </div>
              <div class="node-info">
                <div class="node-name">{{ orgStructure.manager.userName }}</div>
                <div class="node-post">{{ orgStructure.manager.post }}</div>
              </div>
            </div>
          </div>

          <div class="org-level" v-if="orgStructure.departments.length > 0">
            <div
              class="org-node dept-node"
              v-for="dept in orgStructure.departments"
              :key="dept.id"
            >
              <div class="node-content">
                <div class="node-avatar">
                  <el-icon size="24"><UserFilled /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">{{ dept.userName }}</div>
                  <div class="node-post">{{ dept.post }}</div>
                </div>
              </div>
              <div class="node-members" v-if="dept.members?.length > 0">
                <div
                  class="member-item"
                  v-for="member in dept.members"
                  :key="member.id"
                >
                  {{ member.userName }} ({{ member.post }})
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 成员列表 -->
      <el-table
        v-loading="loading"
        :data="members"
        row-key="id"
        border
        style="width: 100%"
        :default-sort="{prop: 'joinDate', order: 'descending'}"
        @row-dblclick="handleRowDblClick"
      >
        <el-table-column prop="userName" label="姓名" min-width="120" />
        <el-table-column prop="department" label="部门" min-width="120" />
        <el-table-column prop="post" label="岗位" min-width="120" />
        <el-table-column prop="joinDate" label="入项日期" min-width="120" sortable />
        <el-table-column prop="leaveDate" label="离项日期" min-width="120">
          <template #default="{ row }">
            {{ row.leaveDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responsibilities" label="职责描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-space size="small">
              <el-button link type="primary" size="small" @click="editMember(row)" :disabled="!canUpdate">
                编辑
              </el-button>
              <el-button
                link
                :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
                size="small"
                @click="toggleMemberStatus(row)"
                :disabled="!canUpdate"
              >
                {{ row.status === 'ACTIVE' ? '离职' : '复职' }}
              </el-button>
              <el-button link type="danger" size="small" @click="removeMember(row)" :disabled="!canDelete">
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加/编辑成员弹窗 -->
      <el-dialog
        v-model="memberDialog.visible"
        :title="memberDialog.isEdit ? '编辑成员' : '添加成员'"
        width="600px"
        draggable
        destroy-on-close
      >
        <el-form
          ref="memberFormRef"
          :model="memberForm"
          :rules="memberRules"
          label-width="100px"
          class="member-form"
          @submit.prevent="saveMember"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="选择人员" prop="userId" v-if="!memberDialog.isEdit">
            <el-select
              v-model="memberForm.userId"
              placeholder="选择要添加的人员"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="user in availableUsers"
                :key="user.personId"
                :label="`${user.personName} (${user.orgName || '未分配'} - ${user.postName || '未分配'})`"
                :value="user.personId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="职责描述" prop="responsibilities">
            <el-input
              v-model="memberForm.responsibilities"
              type="textarea"
              :rows="3"
              placeholder="请输入该成员在项目中的职责描述"
              @keydown.enter.stop
            />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="入项日期" prop="joinDate">
                <el-date-picker
                  v-model="memberForm.joinDate"
                  type="date"
                  placeholder="选择入项日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="离项日期">
                <el-date-picker
                  v-model="memberForm.leaveDate"
                  type="date"
                  placeholder="选择离项日期（可选）"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="状态" prop="status">
            <el-select v-model="memberForm.status" placeholder="选择状态" style="width: 100%">
              <el-option label="在职" value="ACTIVE" />
              <el-option label="离职" value="INACTIVE" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="memberDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveMember">
            {{ memberDialog.isEdit ? '保存' : '添加' }}
          </el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, Download, User, UserFilled } from '@element-plus/icons-vue';
import { getMenuLevel } from '@/utils/permission';
import { queryPersonList, type ExpPersonVO } from '@/api/system/person';
import {
  addProjectMember,
  getProjectMembers,
  removeProjectMember,
  updateProjectMember,
  type ProjectMemberVO,
} from '@/api/corpProject/projectMember';

const route = useRoute();
const loading = ref(false);
const saving = ref(false);

function parseProjectId(raw: unknown): number | null {
  const text = String(raw || '').trim();
  if (!text || !/^\d+$/.test(text)) {
    return null;
  }
  return Number(text);
}

const projectId = ref<number | null>(parseProjectId(route.params.projectId));
const projectName = ref('工程项目');

const members = ref<ProjectMemberVO[]>([]);
const availableUsers = ref<ExpPersonVO[]>([]);

const orgStructure = reactive({
  manager: null as ProjectMemberVO | null,
  departments: [] as Array<{ id: string; userName: string; post: string; members: ProjectMemberVO[] }>,
});

const memberStats = reactive({
  total: 0,
  managers: 0,
  technicians: 0,
  active: 0,
});

const memberDialog = reactive({
  visible: false,
  isEdit: false,
  editId: null as number | null,
});

const memberFormRef = ref<FormInstance>();
const memberForm = reactive({
  userId: undefined as number | undefined,
  responsibilities: '',
  joinDate: '',
  leaveDate: '',
  status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
});

const memberRules: FormRules = {
  userId: [{ required: true, message: '请选择人员', trigger: 'change' }],
  responsibilities: [{ required: true, message: '请输入职责描述', trigger: 'blur' }],
  joinDate: [{ required: true, message: '请选择入项日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

const canUpdate = computed(() => getMenuLevel('project:members') >= 2);
const canDelete = computed(() => getMenuLevel('project:members') >= 3);

async function loadAvailableUsers() {
  try {
    const res = await queryPersonList({ pageNum: 1, pageSize: 500 });
    availableUsers.value = res.list || [];
  } catch (e) {
    availableUsers.value = [];
  }
}

async function loadMembers() {
  if (!projectId.value) {
    members.value = [];
    calculateStats();
    buildOrgStructure();
    return;
  }
  loading.value = true;
  try {
    const res = await getProjectMembers(projectId.value);
    members.value = res || [];
    calculateStats();
    buildOrgStructure();
  } finally {
    loading.value = false;
  }
}

function calculateStats() {
  memberStats.total = members.value.length;
  memberStats.active = members.value.filter((m) => m.status === 'ACTIVE').length;
  memberStats.managers = members.value.filter((m) => m.isManager).length;
  memberStats.technicians = members.value.filter((m) =>
    (m.post || '').includes('工程师') || (m.post || '').includes('技术员'),
  ).length;
}

function buildOrgStructure() {
  const manager = members.value.find((m) => m.isManager);
  orgStructure.manager = manager || null;

  const deptMap = new Map<string, { id: string; userName: string; post: string; members: ProjectMemberVO[] }>();
  members.value
    .filter((m) => !m.isManager)
    .forEach((member) => {
      const deptName = member.department || '未分配部门';
      if (!deptMap.has(deptName)) {
        deptMap.set(deptName, {
          id: deptName,
          userName: `${deptName}主管`,
          post: '部门主管',
          members: [],
        });
      }
      deptMap.get(deptName)!.members.push(member);
    });

  orgStructure.departments = Array.from(deptMap.values());
}

function openAddDialog() {
  if (!projectId.value) {
    ElMessage.warning('请先选择项目');
    return;
  }
  memberDialog.isEdit = false;
  memberDialog.visible = true;
  memberDialog.editId = null;
  resetMemberForm();
}

function editMember(member: ProjectMemberVO) {
  memberDialog.isEdit = true;
  memberDialog.visible = true;
  memberDialog.editId = member.id;
  memberForm.userId = member.userId;
  memberForm.responsibilities = member.responsibilities || '';
  memberForm.joinDate = member.joinDate;
  memberForm.leaveDate = member.leaveDate || '';
  memberForm.status = member.status;
}

function resetMemberForm() {
  memberForm.userId = undefined;
  memberForm.responsibilities = '';
  memberForm.joinDate = '';
  memberForm.leaveDate = '';
  memberForm.status = 'ACTIVE';
}

async function saveMember() {
  if (!memberFormRef.value || !projectId.value) return;
  const valid = await memberFormRef.value.validate();
  if (!valid) return;

  saving.value = true;
  try {
    if (memberDialog.isEdit && memberDialog.editId) {
      await updateProjectMember({
        id: memberDialog.editId,
        userId: memberForm.userId,
        joinDate: memberForm.joinDate,
        leaveDate: memberForm.leaveDate || undefined,
        status: memberForm.status,
        responsibilities: memberForm.responsibilities,
      });
      ElMessage.success('编辑成功');
    } else {
      const selectedUser = availableUsers.value.find((u) => u.personId === memberForm.userId);
      await addProjectMember({
        projectId: projectId.value,
        userId: Number(memberForm.userId),
        orgId: selectedUser?.orgId,
        postId: selectedUser?.postId,
        joinDate: memberForm.joinDate,
        leaveDate: memberForm.leaveDate || undefined,
        status: memberForm.status,
        responsibilities: memberForm.responsibilities,
      });
      ElMessage.success('添加成功');
    }
    memberDialog.visible = false;
    await loadMembers();
  } finally {
    saving.value = false;
  }
}

async function toggleMemberStatus(member: ProjectMemberVO) {
  const action = member.status === 'ACTIVE' ? '离职' : '复职';
  const newStatus = member.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  const leaveDate = newStatus === 'INACTIVE' ? new Date().toISOString().split('T')[0] : '';
  await updateProjectMember({
    id: member.id,
    status: newStatus,
    leaveDate: leaveDate || undefined,
  });
  await loadMembers();
  ElMessage.success(`${action}成功`);
}

function removeMember(member: ProjectMemberVO) {
  ElMessageBox.confirm(`确认移除项目成员「${member.userName || ''}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await removeProjectMember(member.id);
      await loadMembers();
      ElMessage.success('移除成功');
    })
    .catch(() => {});
}

function handleRowDblClick(row: ProjectMemberVO) {
  if (canUpdate.value) {
    editMember(row);
  }
}

function exportMembers() {
  ElMessage.info('导出功能开发中...');
}

watch(
  () => route.params.projectId,
  (newId) => {
    projectId.value = parseProjectId(newId);
    loadMembers();
  },
);

onMounted(async () => {
  if (!projectId.value) {
    ElMessage.warning('当前未选择有效项目，请从项目管理进入');
  }
  await loadAvailableUsers();
  await loadMembers();
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

.actions > * + * {
  margin-left: 8px;
}

.project-overview {
  margin-bottom: 24px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.org-structure {
  margin-bottom: 24px;

  h3 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .org-chart {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 8px;
    min-height: 200px;
  }

  .org-node {
    padding: 16px 20px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    min-width: 200px;

    .node-content {
      display: flex;
      align-items: center;
      gap: 12px;

      .node-avatar {
        flex-shrink: 0;

        .el-icon {
          color: #409EFF;
        }
      }

      .node-info {
        .node-name {
          font-weight: 500;
          margin-bottom: 4px;
        }

        .node-post {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .node-members {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #ebeef5;

      .member-item {
        padding: 4px 0;
        font-size: 13px;
        color: #606266;
      }
    }
  }

  .manager-node {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;

    .node-info .node-name,
    .node-info .node-post {
      color: white;
    }

    .node-members .member-item {
      color: rgba(255, 255, 255, 0.8);
    }
  }

  .dept-node {
    background: white;
    border: 2px solid #409EFF;
  }

  .org-level {
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
    justify-content: center;
  }
}

.member-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}

// 响应式
@media (max-width: 768px) {
  .org-structure {
    .org-level {
      flex-direction: column;
      align-items: center;
    }

    .org-node {
      min-width: 150px;
    }
  }

  .project-overview {
    .el-col {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}
</style>
