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
        <el-form-item label="合同">
          <el-input v-model="query.contractKeyword" placeholder="合同编码/名称" clearable style="width: 220px" />
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
        <el-table-column prop="contractCode" label="合同编码" min-width="160" />
        <el-table-column prop="contractName" label="合同名称" min-width="220" />
        <el-table-column prop="uploader" label="上传人" min-width="140" />
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
          :current-page="query.page"
          :page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>

      <el-dialog v-model="uploadDialog.visible" title="上传附件" width="620px" destroy-on-close>
        <el-form :model="uploadForm" label-width="110px" @submit.prevent>
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="合同ID">
            <el-input v-model="uploadForm.contractId" placeholder="占位：后续替换为合同选择" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="uploadForm.bizType" style="width: 100%">
              <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="文件">
            <el-upload :auto-upload="false" :limit="1" drag :disabled="true">
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
import { UploadFilled } from '@element-plus/icons-vue';
import { hasPermission } from '@/utils/permission';

type ContractAttachmentType = 'CONTRACT_MAIN' | 'SUPPLEMENT' | 'SCAN' | 'OTHER';
type Row = {
  fileId: string;
  fileName: string;
  bizType: ContractAttachmentType;
  contractId: string;
  contractCode: string;
  contractName: string;
  uploader: string;
  uploadTime: string;
};

const canManage = computed(() => hasPermission('contracts:attachments:manage'));

const typeOptions: Array<{ label: string; value: ContractAttachmentType }> = [
  { label: '合同正文', value: 'CONTRACT_MAIN' },
  { label: '补充协议', value: 'SUPPLEMENT' },
  { label: '扫描件', value: 'SCAN' },
  { label: '其他', value: 'OTHER' },
];

function typeText(t: ContractAttachmentType) {
  return typeOptions.find((x) => x.value === t)?.label || t;
}

function typeTagType(t: ContractAttachmentType) {
  if (t === 'CONTRACT_MAIN') return 'success';
  if (t === 'SUPPLEMENT') return 'warning';
  if (t === 'SCAN') return 'info';
  return '';
}

const loading = ref(false);
const total = ref(0);
const selectedRows = ref<Row[]>([]);

const query = reactive({
  contractKeyword: '',
  bizType: undefined as ContractAttachmentType | undefined,
  keyword: '',
  page: 1,
  pageSize: 10,
});

const mockList: Row[] = Array.from({ length: 20 }).map((_, idx) => ({
  fileId: `cf_${idx + 1}`,
  fileName: idx % 2 === 0 ? `合同正文_${idx + 1}.pdf` : `补充协议_${idx + 1}.docx`,
  bizType: typeOptions[idx % typeOptions.length].value,
  contractId: String((idx % 6) + 1),
  contractCode: `HT-2025-${String((idx % 6) + 1).padStart(4, '0')}`,
  contractName: `示例合同 ${(idx % 6) + 1}`,
  uploader: idx % 2 === 0 ? '张三' : '李四',
  uploadTime: '2025-02-01 10:00:00',
}));

const tableData = ref<Row[]>([]);

async function fetchList() {
  loading.value = true;
  try {
    // 后续接接口：/exp/files/list (按 contracts 维度筛选)
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
  query.contractKeyword = (query.contractKeyword || '').trim();
  query.keyword = (query.keyword || '').trim();
  query.page = 1;
  fetchList();
}

function handleReset() {
  query.contractKeyword = '';
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

function handleSelectionChange(rows: Row[]) {
  selectedRows.value = rows;
}

function download(row: Row) {
  window.open(`/api/exp/files/download?fileId=${encodeURIComponent(row.fileId)}`, '_blank');
}

const uploadDialog = reactive({ visible: false });
const uploadForm = reactive({
  contractId: '',
  bizType: 'OTHER' as ContractAttachmentType,
});

function openUpload() {
  uploadDialog.visible = true;
  uploadForm.contractId = '';
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



