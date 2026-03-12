<template>
  <el-dialog
    v-model="visible"
    title="审批"
    width="480px"
    destroy-on-close
    draggable
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="审批意见模板" prop="template">
        <el-select
          v-model="form.template"
          placeholder="请选择"
          style="width: 100%"
          @change="onTemplateChange"
        >
          <el-option
            v-for="t in APPROVAL_OPINION_TEMPLATES"
            :key="t.value"
            :label="t.label"
            :value="t.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="审批意见" prop="comments" required>
        <el-input
          v-model="form.comments"
          type="textarea"
          :rows="3"
          placeholder="选择模板后自动填充，可编辑"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleConfirm">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';

/** 审批意见模板（写死） */
const APPROVAL_OPINION_TEMPLATES = [
  { label: '同意', value: '同意' },
  { label: '不同意', value: '不同意' },
  { label: '不同意信息有误', value: '不同意信息有误' },
  { label: '需补充材料', value: '需补充材料' },
  { label: '转交他人处理', value: '转交他人处理' },
];

interface Props {
  modelValue: boolean;
  taskId?: number;
}

interface Emits {
  (e: 'update:modelValue', v: boolean): void;
  (e: 'confirm', payload: { taskId: number; comments: string }): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const visible = ref(false);
const loading = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  template: '',
  comments: '',
});

const rules: FormRules = {
  comments: [{ required: true, message: '请输入审批意见', trigger: 'blur' }],
};

watch(
  () => props.modelValue,
  (v) => {
    visible.value = v;
    if (v) {
      form.template = '';
      form.comments = '';
    }
  },
  { immediate: true }
);

watch(visible, (v) => emit('update:modelValue', v));

function onTemplateChange(val: string) {
  form.comments = val || '';
}

function handleClose() {
  emit('update:modelValue', false);
}

async function handleConfirm() {
  if (!formRef.value || !props.taskId) return;
  await formRef.value.validate();
  loading.value = true;
  try {
    emit('confirm', { taskId: props.taskId, comments: form.comments.trim() });
    visible.value = false;
  } finally {
    loading.value = false;
  }
}
</script>
