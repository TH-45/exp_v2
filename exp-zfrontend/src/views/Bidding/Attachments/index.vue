<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">招投标附件库</div>
          <div class="actions">
            <el-button type="primary" size="small" :disabled="!canManage" @click="openUpload">
              上传附件
            </el-button>
            <el-button
              type="danger"
              size="small"
              :disabled="!canManage || selectedRows.length === 0"
              :loading="deleting"
              @click="handleBatchDelete"
            >
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <!-- 查询栏 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="业务类型">
          <el-select v-model="query.businessType" clearable style="width: 220px">
            <el-option
              v-for="t in businessTypeOptions"
              :key="t.value"
              :label="t.label"
              :value="t.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="query.fileType" clearable style="width: 180px">
            <el-option v-for="t in fileTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件名">
          <el-input v-model="query.fileName" placeholder="请输入文件名" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="attachmentId"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="fileName" label="文件名" min-width="260" />
        <el-table-column label="业务类型" min-width="120">
          <template #default="{ row }">
            {{ businessTypeText(row.businessType) }}
          </template>
        </el-table-column>
        <el-table-column label="文件类型" min-width="180">
          <template #default="{ row }">
            <el-tag>{{ typeText(row.fileType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessName" label="业务名称" min-width="220" />
        <el-table-column prop="uploadUserName" label="上传人" min-width="140" />
        <el-table-column prop="uploadTime" label="上传时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="{ row }">
            <el-button link size="small" @click="preview(row)" :disabled="true">预览</el-button>
            <el-button link size="small" @click="download(row)">下载</el-button>
            <el-button link type="danger" size="small" :disabled="!canManage || deleting" @click="handleDelete(row)">
              删除
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
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>

      <el-dialog v-model="uploadDialog.visible" title="上传附件" width="620px" destroy-on-close draggable>
        <el-form :model="uploadForm" label-width="110px" @submit.prevent>
          <el-form-item label="业务类型" required>
            <el-select v-model="uploadForm.businessType" style="width: 100%" @change="handleUploadBusinessTypeChange">
              <el-option
                v-for="t in businessTypeOptions"
                :key="t.value"
                :label="t.label"
                :value="t.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="绑定业务" required>
            <AttachmentBusinessSelector
              v-model="uploadForm.boundBusiness"
              :allowed-business-types="[uploadForm.businessType]"
              placeholder="请选择绑定业务"
            />
          </el-form-item>
          <el-form-item label="文件类型" required>
            <el-select v-model="uploadForm.fileType" style="width: 100%">
              <el-option v-for="t in fileTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="文件" required>
            <el-upload
              :auto-upload="false"
              :limit="20"
              multiple
              drag
              :file-list="uploadFileList"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
            >
              <el-icon><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽文件到此处，或 <em>点击选择</em>（可多选，至少一个）</div>
              <template #file="{ file }">
                <span class="el-upload-list__item-name">{{ file.name }}（{{ formatFileSize(file.size ?? (file.raw as File)?.size) }}）</span>
                <el-icon class="el-upload-list__item-delete" @click="removeFile(file)"><Delete /></el-icon>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="uploadDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="uploading" @click="submitUpload">上传</el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox } from 'element-plus';
import { UploadFilled, Delete } from '@element-plus/icons-vue';
import type { UploadFile, UploadFiles } from 'element-plus';
import { getMenuLevel } from '@/utils/permission';
import { listDictOptions, type DictOption } from '@/api/system/dict';
import AttachmentBusinessSelector, {
  type AttachmentBusinessValue,
} from '@/components/Selector/AttachmentBusinessSelector.vue';
import {
  queryBiddingAttachmentList,
  uploadBiddingAttachments,
  deleteBiddingAttachment,
  batchDeleteBiddingAttachment,
  downloadFile,
  type AttachmentVO,
  type AttachmentBusinessType,
  type CreateAttachmentBizReq,
} from '@/api/bidding/attachments';

const canManage = computed(() => getMenuLevel('bidding:attachments') >= 3);
type UploadBusinessType = 'TENDER' | 'BID';

const businessTypeOptions: Array<{ label: string; value: AttachmentBusinessType }> = [
  { label: '招标', value: 'TENDER' },
  { label: '投标', value: 'BID' },
];

function businessTypeText(t?: string) {
  return businessTypeOptions.find((x) => x.value === t)?.label || t || '-';
}

function typeText(t?: string) {
  return fileTypeOptions.value.find((x) => x.value === t)?.label || t || '-';
}

/** 格式化文件大小显示 */
function formatFileSize(bytes?: number): string {
  if (bytes == null || bytes === 0) return '0 B';
  const k = 1024;
  const units = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${units[i]}`;
}

const loading = ref(false);
const total = ref(0);
const selectedRows = ref<AttachmentVO[]>([]);

const query = reactive({
  businessType: undefined as AttachmentBusinessType | undefined,
  fileType: undefined as string | undefined,
  fileName: '',
  pageNum: 1,
  pageSize: 10,
  sort: undefined as string | undefined,
});

const tableData = ref<AttachmentVO[]>([]);

async function fetchList() {
  loading.value = true;
  try {
    const res = await queryBiddingAttachmentList({ ...query });
    tableData.value = Array.isArray((res as any)?.list) ? (res as any).list : [];
    total.value = Number((res as any)?.total ?? 0) || 0;
  } catch (_e) {
    tableData.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
    selectedRows.value = [];
  }
}

onMounted(() => {
  fetchFileTypeOptions();
  fetchList();
});

function handleSearch() {
  query.fileName = (query.fileName || '').trim();
  query.pageNum = 1;
  fetchList();
}

function handleReset() {
  query.businessType = undefined;
  query.fileType = undefined;
  query.fileName = '';
  query.pageNum = 1;
  fetchList();
}

function handleCurrentChange(page: number) {
  query.pageNum = page;
  fetchList();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.pageNum = 1;
  fetchList();
}

function handleSelectionChange(rows: AttachmentVO[]) {
  selectedRows.value = rows;
}

function preview(_row: AttachmentVO) {
  ElMessage.info('预览功能待接入');
}

function download(row: AttachmentVO) {
  const url = downloadFile(row.attachmentId);
  window.open(url, '_blank');
}

async function handleDelete(row: AttachmentVO) {
  try {
    await ElMessageBox.confirm(`确定删除附件「${row.fileName || row.attachmentId}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    });
  } catch (_e) {
    return;
  }
  deleting.value = true;
  try {
    await deleteBiddingAttachment(Number(row.attachmentId));
    ElMessage.success('删除成功');
    fetchList();
  } finally {
    deleting.value = false;
  }
}

async function handleBatchDelete() {
  const ids = selectedRows.value.map((x) => Number(x.attachmentId)).filter((x) => Number.isFinite(x));
  if (ids.length === 0) {
    ElMessage.warning('请先选择要删除的附件');
    return;
  }
  try {
    await ElMessageBox.confirm(`确定批量删除选中的 ${ids.length} 个附件吗？`, '批量删除确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    });
  } catch (_e) {
    return;
  }
  deleting.value = true;
  try {
    await batchDeleteBiddingAttachment(ids);
    ElMessage.success('批量删除成功');
    fetchList();
  } finally {
    deleting.value = false;
  }
}

const uploadDialog = reactive({
  visible: false,
});
const uploadFileList = ref<UploadFile[]>([]);
const selectedFiles = ref<File[]>([]);
const uploading = ref(false);
const deleting = ref(false);
const fileTypeOptions = ref<DictOption[]>([]);
const uploadForm = reactive({
  businessType: 'TENDER' as UploadBusinessType,
  boundBusiness: undefined as AttachmentBusinessValue | undefined,
  fileType: '',
});

function openUpload() {
  uploadDialog.visible = true;
  uploadForm.businessType = 'TENDER';
  uploadForm.boundBusiness = undefined;
  uploadForm.fileType = fileTypeOptions.value[0]?.value || '';
  uploadFileList.value = [];
  selectedFiles.value = [];
}

function handleUploadBusinessTypeChange() {
  uploadForm.boundBusiness = undefined;
}

async function fetchFileTypeOptions() {
  try {
    const res = await listDictOptions('Bid_File_Type');
    const options = Array.isArray(res) ? res : Array.isArray((res as any)?.data) ? (res as any).data : [];
    fileTypeOptions.value = options;
    if (options.length > 0) {
      uploadForm.fileType = options[0].value;
    }
  } catch (_e) {
    fileTypeOptions.value = [];
  }
}

function syncSelectedFiles(files: UploadFiles) {
  selectedFiles.value = files.map((f) => f.raw).filter(Boolean) as File[];
}

function handleFileChange(_file: UploadFile, files: UploadFiles) {
  uploadFileList.value = [...files];
  syncSelectedFiles(files);
}

function handleFileRemove(_file: UploadFile, files: UploadFiles) {
  uploadFileList.value = [...files];
  syncSelectedFiles(files);
}

function removeFile(file: UploadFile) {
  const next = uploadFileList.value.filter((f) => f.uid !== file.uid);
  uploadFileList.value = next;
  syncSelectedFiles(next);
}

async function submitUpload() {
  const businessId = Number(uploadForm.boundBusiness?.businessId);
  if (!uploadForm.businessType || !uploadForm.fileType || !businessId) {
    ElMessage.warning('请完整填写业务类型、绑定业务、文件类型');
    return;
  }
  if (!selectedFiles.value.length) {
    ElMessage.warning('请至少选择一个文件');
    return;
  }
  const biz: CreateAttachmentBizReq = {
    businessType: uploadForm.businessType,
    businessId,
    fileType: uploadForm.fileType,
    fileCategory: uploadForm.fileType,
  };
  const bizList: CreateAttachmentBizReq[] = selectedFiles.value.map(() => ({ ...biz }));
  uploading.value = true;
  try {
    await uploadBiddingAttachments(selectedFiles.value, bizList);
    ElMessage.success('上传成功');
    uploadDialog.visible = false;
    fetchList();
  } finally {
    uploading.value = false;
  }
}
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

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  margin-right: 30px;
  gap: 8px;
}

.search-bar {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}


</style>


