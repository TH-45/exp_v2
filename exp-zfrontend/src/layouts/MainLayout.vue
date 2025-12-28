<template>
  <el-container class="main-layout">
<el-aside width="200px" class="sidebar">
      <div class="logo">招投标协同管理系统</div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        router
        background-color="transparent"
        text-color="#ffffff"
        active-text-color="#409EFF"
      >
        <el-sub-menu index="/system">
          <template #title>系统管理</template>
          <el-menu-item index="/system/user">账号管理</el-menu-item>
          <el-menu-item index="/system/post">岗位管理</el-menu-item>
        </el-sub-menu>
        <!-- 后续可按模块扩展更多菜单 -->
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="tabs-bar">
          <el-icon class="home-icon" :class="{ inactive: route.path !== '/' }" @click="goHome">
            <House />
          </el-icon>
          <el-tabs
            v-model="activeTab"
            type="card"
            :stretch="false"
            class="tabs-with-home"
            @tab-remove="removeTab"
            @tab-click="handleTabClick"
          >
            <span class="tabs-divider"></span>
            <el-tab-pane
              v-for="tab in tabs"
              :key="tab.path"
              :label="renderLabel(tab)"
              :name="tab.path"
              :closable="tab.closable"
            />
          </el-tabs>
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
import { computed, h, reactive, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { House } from '@element-plus/icons-vue';
import * as Icons from '@element-plus/icons-vue';
import type { Component } from 'vue';

type TabItem = {
  title: string;
  path: string;
  icon?: Component;
  closable: boolean;
};

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const activeMenu = computed(() => route.path || '/');

const tabs = reactive<TabItem[]>([
  // {
  //   title: '首页',
  //   path: '/',
  //   icon: Icons.House,
  //   closable: false,
  // },
]);
const activeTab = computed({
  get: () => route.path,
  set: (val: string) => {
    if (val && val !== route.path) {
      router.push(val);
    }
  },
});

const iconMap = Icons as Record<string, Component>;

const addTab = () => {
  const path = route.path;
  // if (!path) return;
  if (!path || path === '/') return;
  const exists = tabs.find((t) => t.path === path);
  if (exists) return;
  const metaTitle = (route.meta?.title as string) || '';
  const iconName = (route.meta?.icon as string) || '';
  tabs.push({
    title: metaTitle || route.name?.toString() || path,
    path,
    icon: iconMap[iconName],
    closable: path !== '/',
  });
};

watch(
  () => route.fullPath,
  () => {
    addTab();
  },
  { immediate: true },
);

const handleTabClick = (pane: { props: { name: string } }) => {
  const target = pane.props.name;
  if (target && target !== route.path) {
    router.push(target);
  }
};

const removeTab = (name: string) => {
  const idx = tabs.findIndex((t) => t.path === name);
  if (idx === -1) return;
  const isActive = route.path === name;
  tabs.splice(idx, 1);
  // if (isActive) {
  //   const fallback = tabs[idx - 1] ?? tabs[0];
  //   router.push(fallback ? fallback.path : '/');
  // }
  // 如果关的是当前激活页
  if (!isActive) return;

  if (tabs.length === 0) {
    router.push('/');
    return;
  }

  const targetTab = tabs[idx - 1] ?? tabs[0];
  if (!targetTab) return;   // 👈 关键一行

  router.push(targetTab.path);
};

const renderLabel = (tab: TabItem) => {
  const isHome = tab.path === '/';
  return h(
    'span',
    { class: ['tab-label', isHome ? 'tab-home' : ''] },
    [
      tab.icon ? h(tab.icon, { class: 'tab-icon' }) : null,
      isHome ? null : h('span', { class: 'tab-text' }, tab.title),
    ].filter(Boolean),
  );
};

const goHome = () => {
  if (route.path !== '/') router.push('/');
};

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
  background: linear-gradient(180deg, #1f1f3f 0%, #48485a 100%);
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
    background-color: transparent;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    background-color: transparent !important;
  }

  :deep(.el-menu-item.is-active),
  :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
    background: rgba(255, 255, 255, 0.12) !important;
  }

  :deep(.el-menu-item:hover),
  :deep(.el-sub-menu__title:hover) {
    background: rgba(255, 255, 255, 0.08) !important;
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 8px 16px;
  min-height: 64px;

}

.content {
  padding: 16px;
  background-color: #f5f5f5;
}

.tabs-bar {

  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;

  .home-icon {
    cursor: pointer;
    color: #409eff;// ✅ 首页激活态（蓝色）
    font-size: 22px;
    transform: translateY(-6px);
  }

  .home-icon.inactive {
    color: #303133; // ❗非激活态（Element Plus 标准深灰）
  }

  :deep(.el-tabs) {
    flex: 1;
    --el-tabs-header-height: 40px;
  }

  :deep(.el-tabs__header) {
    border-bottom: none;
    margin-top: 25px; // 让标签页顶部留出间距
  }

  :deep(.el-tabs__nav) {
    margin-left: 0;
  }

  :deep(.el-tabs__item) {
    height: 40px;
    line-height: 40px;
    padding: 0 14px;
  }

  :deep(.el-tabs__item:first-child) {
    padding: 0 10px;
  }

  :deep(.el-tabs__item.is-active:first-child) {
    color: #409eff;
  }
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tab-icon {
  font-size: 14px;
}

.tab-home {
  padding: 0 4px;
}

.tabs-with-home :deep(.el-tabs__item:first-child) {
  border: none;
  background: transparent;
}

.tabs-with-home :deep(.el-tabs__item:first-child):hover {
  background: transparent;
}
.tabs-divider {
  width: 1px;
  height: 24px;
  background-color: #dcdfe6;
  margin: 0 8px;
}
</style>



