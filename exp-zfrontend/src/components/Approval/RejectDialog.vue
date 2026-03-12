<template>
  <el-dialog
    v-model="visible"
    title="驳回上一个审批人"
    width="480px"
    destroy-on-close
    draggable
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="驳回意见" prop="comments" required>
        <el-input
          v-model="form.comments"
          type="textarea"
          :rows="3"
          placeholder="请输入驳回意见"
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
import { type FormInstance, type FormRules } from 'element-plus';

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
  comments: '',
});

const rules: FormRules = {
  comments: [{ required: true, message: '请输入驳回意见', trigger: 'blur' }],
};

watch(
  () => props.modelValue,
  (v) => {
    visible.value = v;
    if (v) form.comments = '';
  },
  { immediate: true }
);

watch(visible, (v) => emit('update:modelValue', v));

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
