<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">制度与公告管理</div>
          <div class="actions">
            <el-button
              type="primary"
              size="small"
              @click="openCreateDialog"
              :disabled="!canCreate"
            >
              <el-icon><Plus /></el-icon>
              发布公告
            </el-button>
            <el-button size="small" @click="exportAnnouncements">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 查询区 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="公告标题">
          <el-input v-model="query.title" placeholder="公告标题关键词" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" clearable placeholder="全部" style="width: 120px">
            <el-option label="公告" value="NOTICE" />
            <el-option label="制度" value="POLICY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已撤回" value="WITHDRAWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布日期">
          <el-date-picker
            v-model="publishDateRange"
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
        :default-sort="{prop: 'publishTime', order: 'descending'}"
      >
        <el-table-column prop="title" label="标题" min-width="250">
          <template #default="{ row }">
            <div class="title-cell">
              <div class="title-text">{{ row.title }}</div>
              <div class="type-tag">
                <el-tag size="small" :type="getTypeTagType(row.type)">
                  {{ getTypeLabel(row.type) }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布人" min-width="120" />
        <el-table-column prop="publishTime" label="发布时间" min-width="160" sortable />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="readCount" label="阅读量" min-width="100" sortable />
        <el-table-column label="附件" min-width="100">
          <template #default="{ row }">
            <span v-if="row.attachments?.length">{{ row.attachments.length }} 个</span>
            <span v-else class="no-attachments">无</span>
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
                v-if="row.status === 'DRAFT'"
                link
                type="success"
                size="small"
                @click="handlePublish(row)"
                :disabled="!canUpdate"
              >
                发布
              </el-button>
              <el-button
                v-if="row.status === 'PUBLISHED'"
                link
                type="warning"
                size="small"
                @click="handleWithdraw(row)"
                :disabled="!canUpdate"
              >
                撤回
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

      <!-- 发布/编辑弹窗 -->
      <el-dialog
        v-model="editDialog.visible"
        :title="editDialog.isEdit ? '编辑公告' : '发布公告'"
        width="900px"
        destroy-on-close
        draggable
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="dialog-form"
          @submit.prevent="submitForm"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="公告标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入公告标题" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="公告类型" prop="type">
                <el-select v-model="form.type" placeholder="选择公告类型" style="width: 100%">
                  <el-option label="公告" value="NOTICE" />
                  <el-option label="制度" value="POLICY" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="公告内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="8"
              placeholder="请输入公告内容"
              @keydown.enter.stop
            />
          </el-form-item>

          <el-form-item label="附件">
            <el-upload
              ref="uploadRef"
              :file-list="fileList"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :auto-upload="false"
              multiple
              accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
            >
              <el-button size="small" type="primary">选择文件</el-button>
              <template #tip>
                <div class="upload-tip">
                  支持PDF、Word、图片格式，单个文件不超过10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">
            {{ editDialog.isEdit ? '保存' : '发布' }}
          </el-button>
        </template>
      </el-dialog>

      <!-- 详情抽屉 -->
      <el-drawer v-model="detailDrawer.visible" title="公告详情" size="600px">
        <div class="announcement-detail">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="标题">{{ detailDrawer.data?.title }}</el-descriptions-item>
            <el-descriptions-item label="类型">
              <el-tag :type="getTypeTagType(detailDrawer.data?.type)">
                {{ getTypeLabel(detailDrawer.data?.type) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发布人">{{ detailDrawer.data?.publisher }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ detailDrawer.data?.publishTime }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(detailDrawer.data?.status)">
                {{ getStatusLabel(detailDrawer.data?.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="阅读量">{{ detailDrawer.data?.readCount || 0 }}</el-descriptions-item>
          </el-descriptions>

          <div class="content-section">
            <h4>公告内容</h4>
            <div class="content">{{ detailDrawer.data?.content }}</div>
          </div>

          <div v-if="detailDrawer.data?.attachments?.length" class="attachments-section">
            <h4>附件</h4>
            <div class="attachments-list">
              <div
                v-for="attachment in detailDrawer.data.attachments"
                :key="attachment.attachmentId"
                class="attachment-item"
              >
                <el-link type="primary" @click="downloadAttachment(attachment)">
                  {{ attachment.fileName }}
                </el-link>
              </div>
            </div>
          </div>
        </div>
      </el-drawer>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, Download } from '@element-plus/icons-vue';
import { getMenuLevel } from '@/utils/permission';
import {
  listAnnouncements,
  getAnnouncementDetail,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  publishAnnouncement as publishAnnouncementApi,
  withdrawAnnouncement as withdrawAnnouncementApi,
  uploadAnnouncementAttachment,
  downloadAnnouncementAttachment,
  type AnnouncementVO,
  type NoticeAttachmentVO,
} from '@/api/corp.ts';

const loading = ref(false);
const saving = ref(false);

const query = reactive({
  title: '',
  type: undefined as string | undefined,
  status: undefined as string | undefined,
  page: 1,
  pageSize: 10,
});

const publishDateRange = ref<string[]>([]);
const tableData = ref<AnnouncementVO[]>([]);
const total = ref(0);

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const uploadRef = ref();
const fileList = ref<any[]>([]);
const newFiles = ref<File[]>([]);

const form = reactive({
  id: '',
  title: '',
  type: 'NOTICE' as string,
  content: '',
});

const rules: FormRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
};

const detailDrawer = reactive({
  visible: false,
  data: null as AnnouncementVO | null,
});

const canView = computed(() => getMenuLevel('enterprise:announcements') >= 1);
const canCreate = computed(() => getMenuLevel('enterprise:announcements') >= 2);
const canUpdate = computed(() => getMenuLevel('enterprise:announcements') >= 2);
const canDelete = computed(() => getMenuLevel('enterprise:announcements') >= 3);

function getTypeLabel(type?: string) {
  const labels = { NOTICE: '公告', POLICY: '制度' };
  return labels[type as keyof typeof labels] || type;
}

function getTypeTagType(type?: string) {
  const types = { NOTICE: 'primary', POLICY: 'success' };
  return types[type as keyof typeof types] || 'info';
}

function getStatusLabel(status?: string) {
  const labels = { DRAFT: '草稿', PUBLISHED: '已发布', WITHDRAWN: '已撤回' };
  return labels[status as keyof typeof labels] || status;
}

function getStatusTagType(status?: string) {
  const types = { DRAFT: 'info', PUBLISHED: 'success', WITHDRAWN: 'warning' };
  return types[status as keyof typeof types] || 'info';
}

async function fetchList() {
  if (publishDateRange.value && publishDateRange.value.length === 2) {
    (query as any).publishStartDate = publishDateRange.value[0];
    (query as any).publishEndDate = publishDateRange.value[1];
  }
  loading.value = true;
  try {
    const res = await listAnnouncements(query);
    tableData.value = (res.records || []) as AnnouncementVO[];
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
  query.title = '';
  query.type = undefined;
  query.status = undefined;
  publishDateRange.value = [];
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

async function openEditDialog(row: AnnouncementVO) {
  editDialog.isEdit = true;
  editDialog.visible = true;
  const detail = await getAnnouncementDetail(row.id);
  form.id = detail.id;
  form.title = detail.title;
  form.type = detail.type;
  form.content = detail.content;
  fileList.value = (detail.attachments || []).map((a) => ({ name: a.fileName, url: a.filePath }));
  newFiles.value = [];
}

function resetForm() {
  form.id = '';
  form.title = '';
  form.type = 'NOTICE';
  form.content = '';
  fileList.value = [];
  newFiles.value = [];
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
}

function handleFileChange(file: any) {
  if (file?.raw) {
    newFiles.value.push(file.raw as File);
  }
}

function handleFileRemove(file: any) {
  newFiles.value = newFiles.value.filter((f) => f.name !== file.name);
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;

  saving.value = true;
  try {
    let noticeId = form.id;
    if (editDialog.isEdit) {
      await updateAnnouncement(form.id, form);
      ElMessage.success('编辑成功');
    } else {
      const created = await createAnnouncement(form);
      noticeId = created.id;
      ElMessage.success('发布成功');
    }

    if (noticeId && newFiles.value.length > 0) {
      await Promise.all(newFiles.value.map((file) => uploadAnnouncementAttachment(noticeId, file)));
    }
    editDialog.visible = false;
    await fetchList();
  } finally {
    saving.value = false;
  }
}

async function openDetail(row: AnnouncementVO) {
  detailDrawer.visible = true;
  detailDrawer.data = await getAnnouncementDetail(row.id);
}

async function handlePublish(row: AnnouncementVO) {
  await publishAnnouncementApi(row.id);
  ElMessage.success('发布成功');
  fetchList();
}

async function handleWithdraw(row: AnnouncementVO) {
  await withdrawAnnouncementApi(row.id);
  ElMessage.success('撤回成功');
  fetchList();
}

function handleDelete(row: AnnouncementVO) {
  ElMessageBox.confirm(`确认删除${getTypeLabel(row.type)}「${row.title}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteAnnouncement(row.id);
      ElMessage.success('删除成功');
      fetchList();
    })
    .catch(() => {});
}

function downloadAttachment(attachment: NoticeAttachmentVO) {
  const link = document.createElement('a');
  link.href = downloadAnnouncementAttachment(attachment.attachmentId, attachment.fileName);
  link.download = attachment.fileName;
  link.click();
}

function exportAnnouncements() {
  ElMessage.info('导出功能开发中...');
}

onMounted(() => {
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

.search-bar {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;

  .title-text {
    flex: 1;
    font-weight: 500;
  }
}

.no-attachments {
  color: #C0C4CC;
}

.announcement-detail {
  .content-section,
  .attachments-section {
    margin-top: 24px;

    h4 {
      margin: 0 0 12px 0;
      color: #303133;
      font-size: 16px;
      font-weight: 600;
    }

    .content {
      padding: 16px;
      background-color: #f5f7fa;
      border-radius: 4px;
      line-height: 1.6;
      white-space: pre-line;
    }

    .attachments-list {
      .attachment-item {
        padding: 8px 0;
        border-bottom: 1px solid #ebeef5;

        &:last-child {
          border-bottom: none;
        }
      }
    }
  }
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.dialog-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}
</style>
