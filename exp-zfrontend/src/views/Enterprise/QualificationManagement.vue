<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">资质管理</div>
          <div class="actions">
            <el-button
              type="primary"
              size="small"
              @click="openCreateDialog"
              :disabled="!canCreate"
            >
              <el-icon><Plus /></el-icon>
              上传资质
            </el-button>
            <el-button size="small" @click="exportQualifications">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 资质概览 -->
      <div class="stats-overview">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-card class="stats-card valid-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#67C23A"><Check /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ qualificationStats.valid }}</div>
                  <div class="stats-label">有效资质</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="stats-card expiring-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#E6A23C"><Warning /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ qualificationStats.expiring }}</div>
                  <div class="stats-label">即将过期</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="stats-card expired-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#F56C6C"><Close /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ qualificationStats.expired }}</div>
                  <div class="stats-label">已过期</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 查询区 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="资质名称">
          <el-input v-model="query.name" placeholder="资质名称关键词" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="资质类别">
          <el-input v-model="query.category" placeholder="如：施工、设计" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="有效" value="VALID" />
            <el-option label="即将过期" value="EXPIRING" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item label="到期时间">
          <el-date-picker
            v-model="expiryDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表区 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="id"
        border
        style="width: 100%"
        :default-sort="{prop: 'expiryDate', order: 'ascending'}"
      >
        <el-table-column prop="name" label="资质名称" min-width="180" />
        <el-table-column prop="category" label="资质类别" min-width="120" />
        <el-table-column prop="certificateNumber" label="证书编号" min-width="160" />
        <el-table-column prop="issuingAuthority" label="发证机关" min-width="140" />
        <el-table-column prop="issueDate" label="发证日期" min-width="120" />
        <el-table-column prop="expiryDate" label="到期日期" min-width="120" sortable />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到期天数" min-width="100">
          <template #default="{ row }">
            <span :class="getDaysClass(getDaysUntilExpiry(row.expiryDate))">
              {{ getDaysText(getDaysUntilExpiry(row.expiryDate)) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="关联项目" min-width="120">
          <template #default="{ row }">
            <span v-if="row.relatedProjects?.length">
              {{ row.relatedProjects.length }} 个项目
            </span>
            <span v-else class="no-projects">未关联</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-space size="small">
              <el-button link type="primary" size="small" @click="openDetail(row)" :disabled="!canView">
                详情
              </el-button>
              <el-button link type="primary" size="small" @click="openEditDialog(row)" :disabled="!canUpdate">
                编辑
              </el-button>
              <el-button
                link
                type="success"
                size="small"
                @click="downloadCertificate(row)"
                v-if="row.attachmentUrl"
              >
                下载
              </el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)" :disabled="!canDelete">
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page="query.page"
          :page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>

      <!-- 新增/编辑弹窗 -->
      <el-dialog
        v-model="editDialog.visible"
        :title="editDialog.isEdit ? '编辑资质' : '上传资质'"
        width="800px"
        destroy-on-close
        draggable
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          class="dialog-form"
          @submit.prevent="submitForm"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="资质名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入资质名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="资质类别" prop="category">
                <el-input v-model="form.category" placeholder="如：建筑施工、工程设计" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="证书编号" prop="certificateNumber">
                <el-input v-model="form.certificateNumber" placeholder="请输入证书编号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发证机关" prop="issuingAuthority">
                <el-input v-model="form.issuingAuthority" placeholder="请输入发证机关" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="发证日期" prop="issueDate">
                <el-date-picker
                  v-model="form.issueDate"
                  type="date"
                  placeholder="选择发证日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="到期日期" prop="expiryDate">
                <el-date-picker
                  v-model="form.expiryDate"
                  type="date"
                  placeholder="选择到期日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="资质证书" prop="attachment">
            <el-upload
              ref="uploadRef"
              :file-list="fileList"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :auto-upload="false"
              accept=".pdf,.jpg,.jpeg,.png"
              :limit="1"
            >
              <el-button size="small" type="primary">选择文件</el-button>
              <template #tip>
                <div class="upload-tip">
                  支持PDF、JPG、PNG格式，大小不超过10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">
            {{ editDialog.isEdit ? '保存' : '上传' }}
          </el-button>
        </template>
      </el-dialog>

      <!-- 详情抽屉 -->
      <el-drawer v-model="detailDrawer.visible" title="资质详情" size="500px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="资质ID">{{ detailDrawer.data?.id }}</el-descriptions-item>
          <el-descriptions-item label="资质名称">{{ detailDrawer.data?.name }}</el-descriptions-item>
          <el-descriptions-item label="资质类别">{{ detailDrawer.data?.category }}</el-descriptions-item>
          <el-descriptions-item label="证书编号">{{ detailDrawer.data?.certificateNumber }}</el-descriptions-item>
          <el-descriptions-item label="发证机关">{{ detailDrawer.data?.issuingAuthority }}</el-descriptions-item>
          <el-descriptions-item label="发证日期">{{ detailDrawer.data?.issueDate }}</el-descriptions-item>
          <el-descriptions-item label="到期日期">{{ detailDrawer.data?.expiryDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(detailDrawer.data?.status)">
              {{ getStatusLabel(detailDrawer.data?.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="关联项目">
            <div v-if="detailDrawer.data?.relatedProjects?.length">
              <el-tag
                v-for="project in detailDrawer.data.relatedProjects"
                :key="project"
                size="small"
                style="margin-right: 8px; margin-bottom: 4px;"
              >
                {{ project }}
              </el-tag>
            </div>
            <span v-else class="no-projects">暂无关联项目</span>
          </el-descriptions-item>
        </el-descriptions>

        <template #footer>
          <div class="drawer-footer">
            <el-space>
              <el-button @click="detailDrawer.visible = false">关闭</el-button>
              <el-button
                v-if="detailDrawer.data?.attachmentUrl"
                type="primary"
                @click="downloadCertificate(detailDrawer.data)"
              >
                下载证书
              </el-button>
            </el-space>
          </div>
        </template>
      </el-drawer>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, Download, Check, Warning, Close } from '@element-plus/icons-vue';
import { getMenuLevel } from '@/utils/permission';
import {
  listQualifications,
  getQualificationDetail,
  createQualification,
  updateQualification,
  deleteQualification,
  getQualificationsStats,
  uploadQualificationAttachment,
  type QualificationVO
} from '@/api/corp.ts';

const loading = ref(false);
const saving = ref(false);

const query = reactive({
  name: '',
  category: '',
  status: undefined as string | undefined,
  page: 1,
  pageSize: 10,
});

const expiryDateRange = ref<string[]>([]);
const tableData = ref<QualificationVO[]>([]);
const total = ref(0);
const qualificationStats = reactive({
  valid: 0,
  expiring: 0,
  expired: 0,
});

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const uploadRef = ref();
const fileList = ref<any[]>([]);

const form = reactive({
  id: '',
  name: '',
  category: '',
  certificateNumber: '',
  issuingAuthority: '',
  issueDate: '',
  expiryDate: '',
  attachment: null as File | null,
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入资质名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入资质类别', trigger: 'blur' }],
  certificateNumber: [{ required: true, message: '请输入证书编号', trigger: 'blur' }],
  issuingAuthority: [{ required: true, message: '请输入发证机关', trigger: 'blur' }],
  issueDate: [{ required: true, message: '请选择发证日期', trigger: 'change' }],
  expiryDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }],
};

const detailDrawer = reactive({
  visible: false,
  data: null as QualificationVO | null,
});

// 权限点
const canView = computed(() => getMenuLevel('enterprise:qualifications') >= 1);
const canCreate = computed(() => getMenuLevel('enterprise:qualifications') >= 2);
const canUpdate = computed(() => getMenuLevel('enterprise:qualifications') >= 2);
const canDelete = computed(() => getMenuLevel('enterprise:qualifications') >= 3);

function getStatusLabel(status?: string) {
  const labels = {
    VALID: '有效',
    EXPIRING: '即将过期',
    EXPIRED: '已过期',
  };
  return labels[status as keyof typeof labels] || status;
}

function getStatusTagType(status?: string) {
  const types = {
    VALID: 'success',
    EXPIRING: 'warning',
    EXPIRED: 'danger',
  };
  return types[status as keyof typeof types] || 'info';
}

function getDaysUntilExpiry(expiryDate: string) {
  const today = new Date();
  const expiry = new Date(expiryDate);
  const diffTime = expiry.getTime() - today.getTime();
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

function getDaysText(days: number) {
  if (days < 0) {
    return `已过期${Math.abs(days)}天`;
  } else if (days === 0) {
    return '今日到期';
  } else if (days <= 30) {
    return `${days}天后到期`;
  } else {
    return `${days}天`;
  }
}

function getDaysClass(days: number) {
  if (days < 0) {
    return 'expired-days';
  } else if (days <= 30) {
    return 'warning-days';
  } else {
    return 'normal-days';
  }
}

async function fetchStats() {
  try {
    const res = await getQualificationsStats();
    Object.assign(qualificationStats, res);
  } catch (e) {
    qualificationStats.valid = 0;
    qualificationStats.expiring = 0;
    qualificationStats.expired = 0;
  }
}

async function fetchList() {
  // 更新到期日期查询条件
  if (expiryDateRange.value && expiryDateRange.value.length === 2) {
    query.startDate = expiryDateRange.value[0];
    query.endDate = expiryDateRange.value[1];
  }

  loading.value = true;
  try {
    const res = await listQualifications(query);
    tableData.value = (res.records || []) as QualificationVO[];
    total.value = Number(res.total ?? 0) || 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.name = '';
  query.category = '';
  query.status = undefined;
  expiryDateRange.value = [];
  query.page = 1;
  fetchList();
}

function handleCurrentChange(page: number) {
  query.page = page;
  fetchList();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.page = 1;
  fetchList();
}

function openCreateDialog() {
  editDialog.isEdit = false;
  editDialog.visible = true;
  resetForm();
}

function openEditDialog(row: QualificationVO) {
  editDialog.isEdit = true;
  editDialog.visible = true;
  Object.assign(form, row);
  fileList.value = row.attachmentUrl ? [{ name: '资质证书', url: row.attachmentUrl }] : [];
}

function resetForm() {
  form.id = '';
  form.name = '';
  form.category = '';
  form.certificateNumber = '';
  form.issuingAuthority = '';
  form.issueDate = '';
  form.expiryDate = '';
  form.attachment = null;
  fileList.value = [];
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
}

function handleFileChange(file: any) {
  form.attachment = file.raw;
  fileList.value = [file];
}

function handleFileRemove() {
  form.attachment = null;
  fileList.value = [];
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;

  saving.value = true;
  try {
    let savedId = form.id;
    if (editDialog.isEdit) {
      await updateQualification(form.id, form);
      ElMessage.success('编辑成功');
    } else {
      const created = await createQualification(form);
      savedId = created.id;
      ElMessage.success('上传成功');
    }
    if (form.attachment && savedId) {
      await uploadQualificationAttachment(savedId, form.attachment);
    }
    editDialog.visible = false;
    fetchList();
    fetchStats();
  } finally {
    saving.value = false;
  }
}

async function openDetail(row: QualificationVO) {
  const detail = await getQualificationDetail(row.id);
  detailDrawer.visible = true;
  detailDrawer.data = detail;
}

function downloadCertificate(row: QualificationVO) {
  if (row.attachmentUrl) {
    // 创建下载链接
    const link = document.createElement('a');
    link.href = row.attachmentUrl;
    link.download = row.attachmentName || `${row.name}.pdf`;
    link.click();
    ElMessage.success('开始下载证书');
  } else {
    ElMessage.warning('证书文件不存在');
  }
}

function handleDelete(row: QualificationVO) {
  ElMessageBox.confirm(`确认删除资质「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteQualification(row.id);
      ElMessage.success('删除成功');
      fetchList();
      fetchStats();
    })
    .catch(() => {});
}

function exportQualifications() {
  ElMessage.info('导出功能开发中...');
}

onMounted(() => {
  fetchStats();
  fetchList();
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

// 统计卡片
.stats-overview {
  margin-bottom: 24px;

  .stats-card {
    height: 100px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .stats-content {
      display: flex;
      align-items: center;
      height: 100%;
      padding: 16px;

      .stats-icon {
        margin-right: 16px;
      }

      .stats-info {
        .stats-number {
          font-size: 28px;
          font-weight: bold;
          line-height: 1;
          margin-bottom: 4px;
        }

        .stats-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .valid-card .stats-content .stats-icon {
    color: #67C23A;
  }

  .expiring-card .stats-content .stats-icon {
    color: #E6A23C;
  }

  .expired-card .stats-content .stats-icon {
    color: #F56C6C;
  }
}

.search-bar {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.dialog-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.no-projects {
  color: #C0C4CC;
  font-style: italic;
}

.expired-days {
  color: #F56C6C;
  font-weight: 500;
}

.warning-days {
  color: #E6A23C;
  font-weight: 500;
}

.normal-days {
  color: #909399;
}

.drawer-footer {
  text-align: right;
}
</style>
