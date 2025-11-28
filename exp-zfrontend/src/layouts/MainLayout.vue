<template>
  <el-container class="main-layout">
    <el-aside width="200px" class="sidebar">
      <div class="logo">招投标协同管理系统</div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        router
        background-color="#001529"
        text-color="#ffffff"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/">
          <span>仪表盘</span>
        </el-menu-item>
        <el-sub-menu index="/system">
          <template #title>系统管理</template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
        </el-sub-menu>
        <!-- 后续可按模块扩展更多菜单 -->
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <!-- TODO: 根据路由动态生成 -->
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="el-dropdown-link">
              {{ userStore.username || '未登录' }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const activeMenu = computed(() => route.path || '/');

const handleLogout = () => {
  userStore.logout();
  router.replace('/login');
};
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  color: #fff;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: bold;
    color: #fff;
  }

  .el-menu {
    border-right: none;
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.content {
  padding: 16px;
  background-color: #f5f5f5;
}
</style>



