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
        destroy-on-close
      >
        <el-form
          ref="memberFormRef"
          :model="memberForm"
          :rules="memberRules"
          label-width="100px"
          class="member-form"
        >
          <el-form-item label="选择人员" prop="userId" v-if="!memberDialog.isEdit">
            <el-select
              v-model="memberForm.userId"
              placeholder="选择要添加的人员"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="user in availableUsers"
                :key="user.id"
                :label="`${user.name} (${user.department} - ${user.post})`"
                :value="user.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="职责描述" prop="responsibilities">
            <el-input
              v-model="memberForm.responsibilities"
              type="textarea"
              :rows="3"
              placeholder="请输入该成员在项目中的职责描述"
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
import { hasPermission } from '@/utils/permission';
import {
  getProjectMembers,
  addProjectMember,
  updateProjectMember,
  removeProjectMember,
  getProjectOrgStructure,
  type ProjectMemberVO,
  type ProjectOrgNode
} from '@/api/project';

const route = useRoute();
const loading = ref(false);
const saving = ref(false);

const projectId = ref(route.params.projectId as string || '');
const projectName = ref('某某大厦项目');

// 成员列表
const members = ref<ProjectMemberVO[]>([]);

// 组织架构
const orgStructure = reactive({
  manager: null as ProjectMemberVO | null,
  departments: [] as any[],
});

// 统计信息
const memberStats = reactive({
  total: 0,
  managers: 0,
  technicians: 0,
  active: 0,
});

// 弹窗
const memberDialog = reactive({
  visible: false,
  isEdit: false,
  editId: '',
});

const memberFormRef = ref<FormInstance>();
const memberForm = reactive({
  userId: '',
  responsibilities: '',
  joinDate: '',
  leaveDate: '',
  status: 'ACTIVE' as string,
});

const memberRules: FormRules = {
  userId: [{ required: true, message: '请选择人员', trigger: 'change' }],
  responsibilities: [{ required: true, message: '请输入职责描述', trigger: 'blur' }],
  joinDate: [{ required: true, message: '请选择入项日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

// 模拟用户数据
const availableUsers = ref([
  { id: 'user001', name: '张三', department: '技术部', post: '高级工程师' },
  { id: 'user002', name: '李四', department: '技术部', post: '工程师' },
  { id: 'user003', name: '王五', department: '施工部', post: '项目经理' },
  { id: 'user004', name: '赵六', department: '施工部', post: '施工主管' },
  { id: 'user005', name: '孙七', department: '质量部', post: '质量工程师' },
  { id: 'user006', name: '周八', department: '安全部', post: '安全员' },
]);

// 权限点
const canView = computed(() => hasPermission('project:member:view'));
const canUpdate = computed(() => hasPermission('project:member:update'));
const canDelete = computed(() => hasPermission('project:member:delete'));

// 模拟数据
const mockMembers: ProjectMemberVO[] = [
  {
    id: 'm001',
    projectId: projectId.value,
    userId: 'user003',
    userName: '王五',
    department: '施工部',
    post: '项目经理',
    joinDate: '2024-12-01',
    responsibilities: '全面负责项目管理工作，包括进度控制、质量管理、安全管理、成本控制等',
    status: 'ACTIVE',
    isManager: true,
  },
  {
    id: 'm002',
    projectId: projectId.value,
    userId: 'user001',
    userName: '张三',
    department: '技术部',
    post: '高级工程师',
    joinDate: '2024-12-01',
    responsibilities: '负责项目技术方案设计、图纸审核、技术难题解决',
    status: 'ACTIVE',
    isManager: false,
  },
  {
    id: 'm003',
    projectId: projectId.value,
    userId: 'user004',
    userName: '赵六',
    department: '施工部',
    post: '施工主管',
    joinDate: '2024-12-01',
    responsibilities: '负责施工现场管理、施工进度控制、施工质量把关',
    status: 'ACTIVE',
    isManager: false,
  },
  {
    id: 'm004',
    projectId: projectId.value,
    userId: 'user005',
    userName: '孙七',
    department: '质量部',
    post: '质量工程师',
    joinDate: '2024-12-15',
    responsibilities: '负责工程质量检测、质量问题整改、质量档案管理',
    status: 'ACTIVE',
    isManager: false,
  },
];

async function loadMembers() {
  if (!projectId.value) return;

  loading.value = true;
  try {
    const res = await getProjectMembers(projectId.value);
    members.value = res.length ? res : mockMembers;
    calculateStats();
    buildOrgStructure();
  } catch (e) {
    members.value = mockMembers;
    calculateStats();
    buildOrgStructure();
  } finally {
    loading.value = false;
  }
}

function calculateStats() {
  memberStats.total = members.value.length;
  memberStats.active = members.value.filter(m => m.status === 'ACTIVE').length;
  memberStats.managers = members.value.filter(m => m.isManager).length;
  memberStats.technicians = members.value.filter(m =>
    m.post.includes('工程师') || m.post.includes('技术员')
  ).length;
}

function buildOrgStructure() {
  const manager = members.value.find(m => m.isManager);
  orgStructure.manager = manager || null;

  // 按部门分组
  const deptMap = new Map();
  members.value.filter(m => !m.isManager).forEach(member => {
    if (!deptMap.has(member.department)) {
      deptMap.set(member.department, {
        id: member.department,
        userName: `${member.department}主管`,
        post: '部门主管',
        members: [],
      });
    }
    deptMap.get(member.department).members.push(member);
  });

  orgStructure.departments = Array.from(deptMap.values());
}

function openAddDialog() {
  memberDialog.isEdit = false;
  memberDialog.visible = true;
  memberDialog.editId = '';
  resetMemberForm();
}

function editMember(member: ProjectMemberVO) {
  memberDialog.isEdit = true;
  memberDialog.visible = true;
  memberDialog.editId = member.id;
  Object.assign(memberForm, member);
  memberForm.userId = member.userId;
}

function resetMemberForm() {
  memberForm.userId = '';
  memberForm.responsibilities = '';
  memberForm.joinDate = '';
  memberForm.leaveDate = '';
  memberForm.status = 'ACTIVE';
}

async function saveMember() {
  if (!memberFormRef.value) return;
  const valid = await memberFormRef.value.validate();
  if (!valid) return;

  saving.value = true;
  try {
    const formData = {
      projectId: projectId.value,
      ...memberForm,
    };

    if (memberDialog.isEdit) {
      await updateProjectMember(memberDialog.editId, formData);
      const index = members.value.findIndex(m => m.id === memberDialog.editId);
      if (index > -1) {
        members.value[index] = { ...formData, id: memberDialog.editId };
      }
      ElMessage.success('编辑成功');
    } else {
      const newMember = await addProjectMember(formData);
      const selectedUser = availableUsers.value.find(u => u.id === memberForm.userId);
      members.value.push({
        ...formData,
        id: newMember.id || `m${Date.now()}`,
        userName: selectedUser?.name || '未知',
        department: selectedUser?.department || '',
        post: selectedUser?.post || '',
        isManager: false,
      });
      ElMessage.success('添加成功');
    }

    memberDialog.visible = false;
    calculateStats();
    buildOrgStructure();
  } catch (e) {
    // 模拟前端操作
    if (memberDialog.isEdit) {
      const index = members.value.findIndex(m => m.id === memberDialog.editId);
      if (index > -1) {
        const selectedUser = availableUsers.value.find(u => u.id === memberForm.userId);
        members.value[index] = {
          ...memberForm,
          id: memberDialog.editId,
          userName: selectedUser?.name || members.value[index].userName,
          department: selectedUser?.department || members.value[index].department,
          post: selectedUser?.post || members.value[index].post,
          projectId: projectId.value,
          isManager: members.value[index].isManager,
        };
      }
      ElMessage.success('编辑成功（演示模式）');
    } else {
      const selectedUser = availableUsers.value.find(u => u.id === memberForm.userId);
      members.value.push({
        ...memberForm,
        id: `m${Date.now()}`,
        userName: selectedUser?.name || '未知',
        department: selectedUser?.department || '',
        post: selectedUser?.post || '',
        projectId: projectId.value,
        isManager: false,
      });
      ElMessage.success('添加成功（演示模式）');
    }

    memberDialog.visible = false;
    calculateStats();
    buildOrgStructure();
  } finally {
    saving.value = false;
  }
}

async function toggleMemberStatus(member: ProjectMemberVO) {
  const action = member.status === 'ACTIVE' ? '离职' : '复职';
  const newStatus = member.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';

  try {
    await updateProjectMember(member.id, { status: newStatus });
    member.status = newStatus;
    if (newStatus === 'INACTIVE') {
      member.leaveDate = new Date().toISOString().split('T')[0];
    }
    calculateStats();
    ElMessage.success(`${action}成功`);
  } catch (e) {
    member.status = newStatus;
    if (newStatus === 'INACTIVE') {
      member.leaveDate = new Date().toISOString().split('T')[0];
    }
    calculateStats();
    ElMessage.success(`${action}成功（演示模式）`);
  }
}

function removeMember(member: ProjectMemberVO) {
  ElMessageBox.confirm(`确认移除项目成员「${member.userName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await removeProjectMember(member.id);
        members.value = members.value.filter(m => m.id !== member.id);
        calculateStats();
        buildOrgStructure();
        ElMessage.success('移除成功');
      } catch (e) {
        members.value = members.value.filter(m => m.id !== member.id);
        calculateStats();
        buildOrgStructure();
        ElMessage.success('移除成功（演示模式）');
      }
    })
    .catch(() => {});
}

function exportMembers() {
  ElMessage.info('导出功能开发中...');
}

watch(() => route.params.projectId, (newId) => {
  projectId.value = newId as string || '';
  if (projectId.value) {
    loadMembers();
  }
});

onMounted(() => {
  if (projectId.value) {
    loadMembers();
  }
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
