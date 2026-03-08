<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">合同附件库</div>
          <div class="actions">
            <el-button type="primary" size="small" :disabled="!canManage" @click="openUpload">
              上传附件
            </el-button>
            <el-button size="small" :disabled="true">批量下载</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="类型">
          <el-select v-model="query.fileType" clearable style="width: 180px">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
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
        <el-table-column label="类型" min-width="160">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.fileType)">{{ typeText(row.fileType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessName" label="合同名称" min-width="220" />
        <el-table-column prop="uploadUserName" label="上传人" min-width="140" />
        <el-table-column prop="uploadTime" label="上传时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="{ row }">
            <el-button link size="small" :disabled="true">预览</el-button>
            <el-button link size="small" @click="download(row)">下载</el-button>
            <el-button link type="danger" size="small" :disabled="true">删除</el-button>
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
          <el-form-item label="绑定业务" required>
            <AttachmentBusinessSelector
              v-model="uploadForm.boundBusiness"
              :allowed-business-types="['CONTRACT']"
              placeholder="请选择绑定合同"
            />
          </el-form-item>
          <el-form-item label="文件类型" required>
            <el-select v-model="uploadForm.fileType" style="width: 100%">
              <el-option v-for="t in uploadTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="文件">
            <el-upload
              :auto-upload="false"
              :limit="1"
              drag
              :file-list="uploadFileList"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
            >
              <el-icon><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽文件到此处，或 <em>点击选择</em></div>
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
import { ElMessage } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';
import type { UploadFile, UploadFiles } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import { listDictOptions, type DictOption } from '@/api/system/dict';
import AttachmentBusinessSelector, {
  type AttachmentBusinessValue,
} from '@/components/Selector/AttachmentBusinessSelector.vue';
import {
  queryBiddingAttachmentList,
  uploadBiddingAttachment,
  downloadFile,
  type AttachmentVO,
} from '@/api/bidding/attachments';

const canManage = computed(() => hasPermission('contracts:attachments:manage'));

const typeOptions: Array<{ label: string; value: string }> = [
  { label: '合同正文', value: 'CONTRACT_MAIN' },
  { label: '补充协议', value: 'SUPPLEMENT' },
  { label: '扫描件', value: 'SCAN' },
  { label: '其他', value: 'OTHER' },
];

function typeText(t?: string) {
  return typeOptions.find((x) => x.value === t)?.label || t || '-';
}

function typeTagType(t?: string) {
  if (t === 'CONTRACT_MAIN') return 'success';
  if (t === 'SUPPLEMENT') return 'warning';
  if (t === 'SCAN') return 'info';
  return '';
}

const loading = ref(false);
const total = ref(0);
const selectedRows = ref<AttachmentVO[]>([]);

const query = reactive({
  businessType: 'CONTRACT',
  fileType: undefined as string | undefined,
  fileName: '',
  pageNum: 1,
  pageSize: 10,
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
  fetchUploadTypeOptions();
  fetchList();
});

function handleSearch() {
  query.fileName = (query.fileName || '').trim();
  query.pageNum = 1;
  fetchList();
}

function handleReset() {
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

function download(row: AttachmentVO) {
  window.open(downloadFile(row.attachmentId), '_blank');
}

const uploadDialog = reactive({ visible: false });
const uploadFileList = ref<UploadFile[]>([]);
const selectedFile = ref<File | null>(null);
const uploading = ref(false);
const uploadTypeOptions = ref<DictOption[]>([]);
const uploadForm = reactive({
  boundBusiness: undefined as AttachmentBusinessValue | undefined,
  fileType: 'OTHER',
});

function openUpload() {
  uploadDialog.visible = true;
  uploadForm.boundBusiness = undefined;
  uploadForm.fileType = 'OTHER';
  uploadFileList.value = [];
  selectedFile.value = null;
}

function handleFileChange(file: UploadFile, files: UploadFiles) {
  uploadFileList.value = files.slice(-1);
  selectedFile.value = file.raw ?? null;
}

function handleFileRemove() {
  uploadFileList.value = [];
  selectedFile.value = null;
}

async function fetchUploadTypeOptions() {
  try {
    const res = await listDictOptions('Contract_File_Type');
    const options = Array.isArray(res) ? res : Array.isArray((res as any)?.data) ? (res as any).data : [];
    uploadTypeOptions.value = options;
    if (options.length > 0) {
      uploadForm.fileType = options[0].value;
    }
  } catch (_e) {
    uploadTypeOptions.value = [];
  }
}

async function submitUpload() {
  const businessId = Number(uploadForm.boundBusiness?.businessId);
  if (!uploadForm.fileType || !businessId || !selectedFile.value) {
    ElMessage.warning('请完整填写绑定业务、文件类型并选择文件');
    return;
  }
  uploading.value = true;
  try {
    await uploadBiddingAttachment(selectedFile.value, {
      businessType: 'CONTRACT',
      businessId,
      fileType: uploadForm.fileType,
      fileCategory: uploadForm.fileType,
    });
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



