<template>
  <el-container class="main-layout">
  <el-aside width="200px" class="sidebar">
      <!-- 固定标题区域 -->
      <div class="sidebar-header">
        <div class="logo">招投标协同管理系统</div>
      </div>

      <!-- 可滚动菜单区域 -->
      <div class="sidebar-content">
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
          <el-menu-item index="/system/account">账号管理</el-menu-item>
          <el-menu-item index="/system/user">人员管理</el-menu-item>
          <el-menu-item index="/system/post">岗位管理</el-menu-item>
          <el-menu-item v-if="canRoleView" index="/system/role">角色管理</el-menu-item>
          <el-menu-item v-if="canMenuView" index="/system/menu">菜单管理</el-menu-item>
          <el-menu-item v-if="canDictView" index="/system/dict">字典管理</el-menu-item>
<!--          <el-menu-item v-if="canPermView" index="/system/permission">权限管理</el-menu-item>-->
        </el-sub-menu>
        <el-sub-menu
          v-if="canBiddingProjectView || canBiddingBidView || canBiddingEvaluationView || canBiddingAttachmentsView"
          index="/bidding"
        >
          <template #title>招投标管理</template>
          <el-menu-item index="/bidding/project">招标项目</el-menu-item>
          <el-menu-item v-if="canBiddingBidView" index="/bidding/bid">投标登记</el-menu-item>
          <el-menu-item v-if="canBiddingEvaluationView" index="/bidding/evaluation">评标/定标</el-menu-item>
          <el-menu-item v-if="canBiddingAttachmentsView" index="/bidding/attachments">招投标附件库</el-menu-item>
        </el-sub-menu>
        <el-sub-menu
          v-if="canContractsContractView || canContractsChangeView || canContractsPaymentView || canContractsAttachmentsView"
          index="/contracts"
        >
          <template #title>合同管理</template>
          <el-menu-item v-if="canContractsContractView" index="/contracts/contract">合同台账</el-menu-item>
          <el-menu-item v-if="canContractsChangeView" index="/contracts/change">合同变更</el-menu-item>
          <el-menu-item v-if="canContractsPaymentView" index="/contracts/payment">收付款台账</el-menu-item>
          <el-menu-item v-if="canContractsAttachmentsView" index="/contracts/attachments">合同附件库</el-menu-item>
        </el-sub-menu>

        <!-- 审批管理 -->
        <el-sub-menu v-if="canApprovalView" index="/approval">
          <template #title>
            <span>审批管理</span>
          </template>

          <el-menu-item index="/approval">
            审批/待办中心
          </el-menu-item>
        </el-sub-menu>

        <!-- 企业信息管理 -->
        <el-sub-menu v-if="canCorpView" index="/corp-project/corp-info">
          <template #title>企业信息管理</template>
          <!-- 概览页面 -->
          <el-menu-item index="/corp-project">
            概览
          </el-menu-item>
          <el-menu-item v-if="canAccountView" index="/corp-project/corp-info/accounts">
            账号信息管理
          </el-menu-item>
          <el-menu-item v-if="canAnnouncementView" index="/corp-project/corp-info/announcements">
            制度与公告
          </el-menu-item>
          <el-menu-item v-if="canQualificationView" index="/corp-project/corp-info/qualifications">
            企业资质管理
          </el-menu-item>
          <el-menu-item v-if="canBasicInfoView" index="/corp-project/corp-info/basic-info">
            企业基础信息
          </el-menu-item>
        </el-sub-menu>

        <!-- 工程项目管理 -->
        <el-sub-menu v-if="canProjectView" index="/corp-project/project-mgmt">
          <template #title>工程项目管理</template>
          <el-menu-item v-if="canProjectView" index="/corp-project/project-mgmt/projects">
            项目管理
          </el-menu-item>
          <el-menu-item v-if="canProjectView" index="/corp-project/project-mgmt/members">
            项目人员配置
          </el-menu-item>
          <el-menu-item v-if="canProjectView" index="/corp-project/project-mgmt/progress">
            项目进度管理
          </el-menu-item>
          <el-menu-item v-if="canProjectView" index="/corp-project/project-mgmt/materials">
            项目物料管理
          </el-menu-item>
        </el-sub-menu>



        <!-- 后续可按模块扩展更多菜单 -->
        </el-menu>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="tabs-bar">
          <el-icon class="home-icon" :class="{ inactive: route.path !== '/' }" @click="goHome">
            <House />
          </el-icon>

          <div class="tabs-wrapper">
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
                  v-for="tab in visibleTabs"
                  :key="tab.path"
                  :label="renderLabel(tab)"
                  :name="tab.path"
                  :closable="tab.closable"
              />
            </el-tabs>
          </div>

          <!-- 标签页下拉菜单 -->
          <el-dropdown v-if="showDropdown" class="tabs-dropdown" @command="handleDropdownCommand">
            <el-icon class="dropdown-icon">
              <ArrowDown />
            </el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="tab in hiddenTabs"
                  :key="tab.path"
                  :command="{ action: 'switch', tab }"
                  class="dropdown-menu-item"
                >
                  <div class="tab-item-content">
                    <span class="tab-item-label">
                      {{ tab.icon ? '' : '' }}{{ tab.title }}
                    </span>
                    <el-icon class="close-icon" @click.stop="closeTabFromDropdown(tab.path)">
                      <Close />
                    </el-icon>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="closeAll" class="close-all-option">
                  <el-icon class="close-all-icon"><Close /></el-icon>
                  关闭全部
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="header-right">
          <el-dropdown>
            <div class="user-avatar">
              <img
                v-if="userStore.avatar"
                :src="userStore.avatar"
                alt="头像"
                class="avatar-image"
              />
              <div v-else class="avatar-placeholder">
                {{ getInitials(userStore.username || '未登录') }}
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="user-info">
                  <div class="user-avatar-dropdown">
                    <img
                      v-if="userStore.avatar"
                      :src="userStore.avatar"
                      alt="头像"
                      class="avatar-image"
                    />
                    <div v-else class="avatar-placeholder">
                      {{ getInitials(userStore.username || '未登录') }}
                    </div>
                  </div>
                  <div class="user-details">
                    <div class="username">{{ userStore.username || '未登录' }}</div>
                    <div class="user-role">{{ getUserRole() }}</div>
                  </div>
                </div>

                <el-dropdown-item>
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
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
import { House, ArrowDown, Close, User, SwitchButton } from '@element-plus/icons-vue';
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

// 标签页最大显示数量
const MAX_VISIBLE_TABS = 9;

const activeMenu = computed(() => route.path || '/');
const canRoleView = computed(() => userStore.isAdmin || userStore.permissions.includes('system:role:view'));
const canMenuView = computed(() => userStore.isAdmin || userStore.permissions.includes('system:menu:view'));
const canDictView = computed(() => userStore.isAdmin || userStore.permissions.includes('system:dic:view'));
// const canPermView = computed(() => userStore.isAdmin || userStore.permissions.includes('system:user:view'));

const canBiddingProjectView = computed(
  () => userStore.isAdmin || userStore.permissions.includes('bidding:project:view'),
);
const canBiddingBidView = computed(
  () => userStore.isAdmin || userStore.permissions.includes('bidding:bid:view'),
);
const canBiddingEvaluationView = computed(
  () => userStore.isAdmin || userStore.permissions.includes('bidding:evaluation:view'),
);
const canBiddingAttachmentsView = computed(
  () => userStore.isAdmin || userStore.permissions.includes('bidding:attachments:view'),
);
const canContractsContractView = computed(
  () => userStore.isAdmin || userStore.permissions.includes('contracts:contract:view'),
);
const canContractsChangeView = computed(
  () => userStore.isAdmin || userStore.permissions.includes('contracts:change:view'),
);
const canContractsPaymentView = computed(
  () => userStore.isAdmin || userStore.permissions.includes('contracts:payment:view'),
);
const canContractsAttachmentsView = computed(
  () => userStore.isAdmin || userStore.permissions.includes('contracts:attachments:view'),
);
const canApprovalView = computed(
  () => userStore.isAdmin || userStore.permissions.includes('approval:task:view'),
);
// 企业信息与工程项目权限
const canCorpView = computed(() => userStore.isAdmin || userStore.permissions.some(p =>
  p.startsWith('corp:') && p.endsWith(':view')
));
const canProjectView = computed(() => userStore.isAdmin || userStore.permissions.some(p =>
  p.startsWith('project:') && p.endsWith(':view')
));
const canAccountView = computed(() => userStore.isAdmin || userStore.permissions.includes('corp:account:view'));
const canAnnouncementView = computed(() => userStore.isAdmin || userStore.permissions.includes('corp:announcement:view'));
const canQualificationView = computed(() => userStore.isAdmin || userStore.permissions.includes('corp:qualification:view'));
const canBasicInfoView = computed(() => userStore.isAdmin || userStore.permissions.includes('corp:basic:view'));

// 标签页显示逻辑
const visibleTabs = computed(() => {
  if (tabs.length <= MAX_VISIBLE_TABS) {
    return tabs;
  }
  // 如果当前激活的标签页不在前 MAX_VISIBLE_TABS 个中，则调整显示范围
  const activeIndex = tabs.findIndex(tab => tab.path === activeTab.value);
  if (activeIndex >= MAX_VISIBLE_TABS) {
    const startIndex = Math.max(0, activeIndex - MAX_VISIBLE_TABS + 1);
    return tabs.slice(startIndex, startIndex + MAX_VISIBLE_TABS);
  }
  return tabs.slice(0, MAX_VISIBLE_TABS);
});

const hiddenTabs = computed(() => {
  if (tabs.length <= MAX_VISIBLE_TABS) {
    return [];
  }
  const activeIndex = tabs.findIndex(tab => tab.path === activeTab.value);
  if (activeIndex >= MAX_VISIBLE_TABS) {
    const startIndex = Math.max(0, activeIndex - MAX_VISIBLE_TABS + 1);
    return [...tabs.slice(0, startIndex), ...tabs.slice(startIndex + MAX_VISIBLE_TABS)];
  }
  return tabs.slice(MAX_VISIBLE_TABS);
});

const showDropdown = computed(() => tabs.length > MAX_VISIBLE_TABS);

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

// 获取用户名的首字符（支持中英文）
const getInitials = (name: string) => {
  if (!name || name === '未登录') return '?';
  // 获取第一个字符，如果是中文则直接返回，否则取大写
  const firstChar = name.charAt(0);
  // 检查是否为中文字符
  if (/[\u4e00-\u9fa5]/.test(firstChar)) {
    return firstChar;
  }
  return firstChar.toUpperCase();
};

// 获取用户角色显示
const getUserRole = () => {
  if (userStore.isAdmin) return '系统管理员';
  if (userStore.roles.length > 0) {
    return userStore.roles[0]; // 显示第一个角色
  }
  return '普通用户';
};

// 处理下拉菜单命令
const handleDropdownCommand = (command: { action: string; tab?: TabItem } | string) => {
  if (typeof command === 'string') {
    if (command === 'closeAll') {
      closeAllTabs();
    }
  } else if (command.action === 'switch' && command.tab) {
    router.push(command.tab.path);
  }
};

// 从下拉菜单关闭标签页
const closeTabFromDropdown = (tabPath: string) => {
  removeTab(tabPath);
};

// 关闭所有标签页
const closeAllTabs = () => {
  tabs.splice(0, tabs.length);
  router.push('/');
};
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #1f1f3f 0%, #48485a 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  height: 100vh; // 确保sidebar占满整个视口高度

  .sidebar-header {
    flex-shrink: 0; // 固定头部，不参与滚动
    background: linear-gradient(180deg, #2a2a50 0%, #3a3a65 100%); // 标题区域使用浅色调背景
    border-bottom: 1px solid rgba(255, 255, 255, 0.15); // 添加底部边框分隔
  }

  .sidebar-content {
    flex: 1; // 内容区域占据剩余空间
    overflow-y: auto; // 启用垂直滚动
    overflow-x: hidden; // 隐藏水平滚动

    // 自定义滚动条样式
    &::-webkit-scrollbar {
      width: 6px; // 滚动条宽度
    }

    &::-webkit-scrollbar-track {
      background: rgba(255, 255, 255, 0.1); // 轨道背景色
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.3); // 滑块颜色
      border-radius: 3px;

      &:hover {
        background: rgba(255, 255, 255, 0.5); // 悬停时颜色
      }
    }

    // Firefox 滚动条样式
    scrollbar-width: thin;
    scrollbar-color: rgba(255, 255, 255, 0.3) rgba(255, 255, 255, 0.1);
  }

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
    padding-bottom: 20vh; // 底部额外空间，约20%的视口高度
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

  .header-right {
    flex-shrink: 0;
    margin-left: 16px;

    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: 2px solid rgba(255, 255, 255, 0.2);
      transition: all 0.3s ease;

      &:hover {
        transform: scale(1.05);
        border-color: rgba(255, 255, 255, 0.4);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      .avatar-image {
        width: 100%;
        height: 100%;
        border-radius: 50%;
        object-fit: cover;
      }

      .avatar-placeholder {
        color: white;
        font-size: 16px;
        font-weight: bold;
        text-transform: uppercase;
      }
    }
  }
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
  padding-top: 10px;

  .home-icon {
    cursor: pointer;
    color: #409eff;// ✅ 首页激活态（蓝色）
    font-size: 25px;
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

/* 标签可滚动区域 */
.tabs-scroll-wrapper {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.tabs-with-home {
  flex: 1;
  min-width: 0;

  :deep(.el-tabs__nav-wrap) {
    overflow: hidden; // 禁用默认滚动
  }

  :deep(.el-tabs__nav-scroll) {
    overflow: hidden;
  }
}

/* 左右箭头 */
.scroll-btn {
  cursor: pointer;
  color: #606266;
  font-size: 18px;
  padding: 0 4px;

  &:hover {
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

// 标签页下拉菜单样式
.tabs-dropdown {
  //margin-left:0px;
  color: #606266;
  cursor: pointer;
  flex-shrink: 0;
  margin-left: 4px;

  .dropdown-icon {
    font-size: 16px;
    transition: color 0.2s;

    &:hover {
      color: #409eff;
    }
  }
}

:deep(.dropdown-menu-item) {
  padding: 8px 12px;

  .tab-item-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;

    .tab-item-label {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 6px;

      .tab-icon {
        font-size: 14px;
      }
    }

    .close-icon {
      color: #f56c6c;
      cursor: pointer;
      font-size: 14px;
      margin-left: 8px;
      transition: color 0.2s;

      &:hover {
        color: #f78989;
      }
    }
  }
}

:deep(.close-all-option) {
  color: #f56c6c;
  border-top: 1px solid #ebeef5;
  margin-top: 4px;
  padding-top: 8px;

  &:hover {
    background-color: #fef0f0;
  }

  .close-all-icon {
    margin-right: 6px;
  }
}

// 用户信息下拉菜单样式
:deep(.user-info) {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 8px;

  .user-avatar-dropdown {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: 2px solid rgba(255, 255, 255, 0.2);

    .avatar-image {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      object-fit: cover;
    }

    .avatar-placeholder {
      color: white;
      font-size: 18px;
      font-weight: bold;
      text-transform: uppercase;
    }
  }

  .user-details {
    flex: 1;

    .username {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }

    .user-role {
      font-size: 12px;
      color: #909399;
    }
  }
}
.tabs-wrapper {
  margin-left: 10px;
  width: 900px;       // 或固定像素，比如 800px
  max-width: 1000px; // 限制整体最大宽度
  overflow: hidden;  // 超出隐藏（你用下拉菜单显示）
}

</style>



