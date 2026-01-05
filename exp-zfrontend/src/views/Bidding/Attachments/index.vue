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
            <el-button size="small" :disabled="true">批量下载</el-button>
          </div>
        </div>
      </template>

      <!-- 查询栏 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="项目">
          <el-input
            v-model="query.projectKeyword"
            placeholder="项目编码/名称"
            clearable
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.bizType" clearable style="width: 180px">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="文件名/上传人" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="fileId"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="fileName" label="文件名" min-width="260" />
        <el-table-column label="类型" min-width="160">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.bizType)">{{ typeText(row.bizType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="projectCode" label="项目编码" min-width="140" />
        <el-table-column prop="projectName" label="项目名称" min-width="220" />
        <el-table-column prop="uploader" label="上传人" min-width="140" />
        <el-table-column prop="uploadTime" label="上传时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="{ row }">
            <el-button link size="small" @click="preview(row)" :disabled="true">预览</el-button>
            <el-button link size="small" @click="download(row)">下载</el-button>
            <el-button link type="danger" size="small" :disabled="true">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

      <!-- 上传弹窗（示例占位，后续接 /exp/files/upload） -->
      <el-dialog v-model="uploadDialog.visible" title="上传附件" width="620px" destroy-on-close>
        <el-form :model="uploadForm" label-width="110px">
          <el-form-item label="项目ID">
            <el-input v-model="uploadForm.projectId" placeholder="占位：后续替换为项目选择" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="uploadForm.bizType" style="width: 100%">
              <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="文件">
            <el-upload
              :auto-upload="false"
              :limit="1"
              drag
              :disabled="true"
            >
              <el-icon><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽文件到此处，或 <em>点击选择</em></div>
              <template #tip>
                <div class="upload-tip">示例模式：上传接口未接入（后续对齐 `/exp/files/upload`）</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="uploadDialog.visible = false">取消</el-button>
          <el-button type="primary" :disabled="true">上传</el-button>
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
import { hasPermission } from '@/utils/permission';
import {
  queryBiddingAttachmentList,
  downloadFile,
  type AttachmentVO,
  type AttachmentBizType,
} from '@/api/bidding/attachments';

const canManage = computed(() => hasPermission('bidding:attachments:manage'));

const typeOptions: Array<{ label: string; value: AttachmentBizType }> = [
  { label: '招标文件', value: 'TENDER_DOC' },
  { label: '澄清文件', value: 'CLARIFICATION' },
  { label: '投标文件', value: 'BID_DOC' },
  { label: '评标报告', value: 'EVALUATION_REPORT' },
  { label: '其他', value: 'OTHER' },
];

function typeText(t: AttachmentBizType) {
  return typeOptions.find((x) => x.value === t)?.label || t;
}

function typeTagType(t: AttachmentBizType) {
  if (t === 'TENDER_DOC') return 'success';
  if (t === 'CLARIFICATION') return 'warning';
  if (t === 'BID_DOC') return 'info';
  if (t === 'EVALUATION_REPORT') return 'danger';
  return '';
}

const loading = ref(false);
const total = ref(0);
const selectedRows = ref<AttachmentVO[]>([]);

const query = reactive({
  projectKeyword: '',
  bizType: undefined as AttachmentBizType | undefined,
  keyword: '',
  page: 1,
  pageSize: 10,
});

const tableData = ref<AttachmentVO[]>([]);

const mockList: AttachmentVO[] = Array.from({ length: 26 }).map((_, idx) => ({
  fileId: `f_${idx + 1}`,
  fileName: idx % 2 === 0 ? `招标文件_${idx + 1}.pdf` : `评标报告_${idx + 1}.docx`,
  bizType: typeOptions[idx % typeOptions.length].value,
  projectId: String((idx % 6) + 1),
  projectCode: `TB-2025-${String((idx % 6) + 1).padStart(3, '0')}`,
  projectName: `示例招标项目 ${(idx % 6) + 1}`,
  uploader: idx % 2 === 0 ? '张三' : '李四',
  uploadTime: '2025-01-10 09:00:00',
}));

async function fetchList() {
  loading.value = true;
  try {
    const res = await queryBiddingAttachmentList({ ...query });
    const records = (res as any)?.records ?? [];
    tableData.value = Array.isArray(records) && records.length ? records : mockList;
    total.value = Number((res as any)?.total ?? tableData.value.length) || 0;
  } catch (e) {
    tableData.value = mockList;
    total.value = mockList.length;
  } finally {
    loading.value = false;
    selectedRows.value = [];
  }
}

onMounted(() => {
  fetchList();
});

function handleSearch() {
  query.projectKeyword = (query.projectKeyword || '').trim();
  query.keyword = (query.keyword || '').trim();
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.projectKeyword = '';
  query.bizType = undefined;
  query.keyword = '';
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

function handleSelectionChange(rows: AttachmentVO[]) {
  selectedRows.value = rows;
}

function preview(_row: AttachmentVO) {
  ElMessage.info('预览待接入（示例模式）');
}

function download(row: AttachmentVO) {
  const url = downloadFile(row.fileId);
  window.open(url, '_blank');
}

const uploadDialog = reactive({
  visible: false,
});
const uploadForm = reactive({
  projectId: '',
  bizType: 'OTHER' as AttachmentBizType,
});

function openUpload() {
  uploadDialog.visible = true;
  uploadForm.projectId = '';
  uploadForm.bizType = 'OTHER';
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

.upload-tip {
  color: #666;
  margin-top: 6px;
}
</style>


