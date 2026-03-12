<template>
  <el-dialog
    v-model="visible"
    title="签订合同"
    width="500px"
    destroy-on-close
    draggable
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" label-width="120px">
      <el-form-item label="签订意见">
        <el-input
          v-model="form.opinion"
          type="textarea"
          :rows="2"
          placeholder="选填"
        />
      </el-form-item>
      <el-form-item label="合同正文" required>
        <el-upload
          :auto-upload="false"
          :limit="1"
          :file-list="mainFileList"
          :on-change="handleMainFileChange"
          :on-remove="handleMainFileRemove"
        >
          <el-button type="primary" size="small">选择文件</el-button>
          <template #file="{ file }">
            <span class="file-item">
              {{ file.name }}（{{ formatFileSize(file.size ?? (file.raw as File)?.size) }}）
            </span>
            <el-icon class="el-icon--delete" @click="removeMainFile(file)"><Delete /></el-icon>
          </template>
        </el-upload>
        <div class="upload-tip">必填，1个文件）</div>
      </el-form-item>
      <el-form-item label="合同相关附件">
        <el-upload
          :auto-upload="false"
          :limit="10"
          multiple
          :file-list="attachFileList"
          :on-change="handleAttachFileChange"
          :on-remove="handleAttachFileRemove"
        >
          <el-button type="primary" size="small">选择文件</el-button>
          <template #file="{ file }">
            <span class="file-item">
              {{ file.name }}（{{ formatFileSize(file.size ?? (file.raw as File)?.size) }}）
            </span>
            <el-icon class="el-icon--delete" @click="removeAttachFile(file)"><Delete /></el-icon>
          </template>
        </el-upload>
        <div class="upload-tip">选填，支持多个）</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleConfirm">确认签订</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Delete } from '@element-plus/icons-vue';
import type { UploadFile, UploadFiles } from 'element-plus';

function formatFileSize(bytes?: number): string {
  if (bytes == null || bytes === 0) return '0 B';
  const k = 1024;
  const units = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${units[i]}`;
}

interface Props {
  modelValue: boolean;
  contractId?: number;
}

interface Emits {
  (e: 'update:modelValue', v: boolean): void;
  (e: 'confirm', payload: { opinion?: string; mainFiles: File[]; attachFiles: File[] }): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const visible = ref(false);
const loading = ref(false);
const formRef = ref();

const form = reactive({ opinion: '' });
const mainFileList = ref<UploadFile[]>([]);
const attachFileList = ref<UploadFile[]>([]);
const mainFiles = ref<File[]>([]);
const attachFiles = ref<File[]>([]);

watch(
  () => props.modelValue,
  (v) => {
    visible.value = v;
    if (v) {
      form.opinion = '';
      mainFileList.value = [];
      attachFileList.value = [];
      mainFiles.value = [];
      attachFiles.value = [];
    }
  },
  { immediate: true }
);

watch(visible, (v) => emit('update:modelValue', v));

function handleMainFileChange(_f: UploadFile, files: UploadFiles) {
  mainFileList.value = [...files];
  mainFiles.value = files.map((x) => x.raw).filter(Boolean) as File[];
}

function handleMainFileRemove(_f: UploadFile, files: UploadFiles) {
  mainFileList.value = [...files];
  mainFiles.value = files.map((x) => x.raw).filter(Boolean) as File[];
}

function removeMainFile(file: UploadFile) {
  mainFileList.value = mainFileList.value.filter((x) => x.uid !== file.uid);
  mainFiles.value = mainFileList.value.map((x) => x.raw).filter(Boolean) as File[];
}

function handleAttachFileChange(_f: UploadFile, files: UploadFiles) {
  attachFileList.value = [...files];
  attachFiles.value = files.map((x) => x.raw).filter(Boolean) as File[];
}

function handleAttachFileRemove(_f: UploadFile, files: UploadFiles) {
  attachFileList.value = [...files];
  attachFiles.value = files.map((x) => x.raw).filter(Boolean) as File[];
}

function removeAttachFile(file: UploadFile) {
  attachFileList.value = attachFileList.value.filter((x) => x.uid !== file.uid);
  attachFiles.value = attachFileList.value.map((x) => x.raw).filter(Boolean) as File[];
}

function handleClose() {
  emit('update:modelValue', false);
}

function handleConfirm() {
  if (mainFiles.value.length === 0) {
    ElMessage.warning('请上传合同正文（必填）');
    return;
  }
  loading.value = true;
  try {
    emit('confirm', {
      opinion: form.opinion?.trim() || undefined,
      mainFiles: mainFiles.value,
      attachFiles: attachFiles.value,
    });
    visible.value = false;
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped lang="scss">
.file-item {
  margin-right: 8px;
}
.el-icon--delete {
  cursor: pointer;
  color: var(--el-color-danger);
}
.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
</style>
