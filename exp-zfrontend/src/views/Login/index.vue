<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">招投标协同管理系统</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { useUserStore } from '@/store/modules/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  username: 'admin',
  password: '123456',
});

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const handleLogin = async () => {
  if (!formRef.value) return;
    await formRef.value.validate(async (valid) => {
      if (!valid) return;
    loading.value = true;
    try {

      await userStore.login(form.username, form.password);

      const redirect = (route.query.redirect as string) || '/';
      router.replace(redirect);
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped lang="scss">
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 400px;
}

.title {
  text-align: center;
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
}
</style>



