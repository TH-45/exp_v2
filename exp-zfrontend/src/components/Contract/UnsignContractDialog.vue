<template>
  <el-dialog
    v-model="visible"
    title="不签订"
    width="480px"
    destroy-on-close
    draggable
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" label-width="120px">
      <el-form-item label="不签订意见">
        <el-input
          v-model="form.opinion"
          type="textarea"
          :rows="2"
          placeholder="选填"
        />
      </el-form-item>
      <el-form-item label="是否变更" required>
        <el-radio-group v-model="form.needChange">
          <el-radio :label="true">是，返回合同起草进行变更</el-radio>
          <el-radio :label="false">否，异常归档</el-radio>
        </el-radio-group>
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

interface Props {
  modelValue: boolean;
}

interface Emits {
  (e: 'update:modelValue', v: boolean): void;
  (e: 'confirm', payload: { opinion?: string; needChange: boolean }): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const visible = ref(false);
const loading = ref(false);
const formRef = ref();

const form = reactive({
  opinion: '',
  needChange: false,
});

watch(
  () => props.modelValue,
  (v) => {
    visible.value = v;
    if (v) {
      form.opinion = '';
      form.needChange = false;
    }
  },
  { immediate: true }
);

watch(visible, (v) => emit('update:modelValue', v));

function handleClose() {
  emit('update:modelValue', false);
}

function handleConfirm() {
  loading.value = true;
  try {
    emit('confirm', {
      opinion: form.opinion?.trim() || undefined,
      needChange: form.needChange,
    });
    visible.value = false;
  } finally {
    loading.value = false;
  }
}
</script>
