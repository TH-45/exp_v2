import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import routes from './routes';
import { useUserStore } from '@/store/modules/user';

const router = createRouter({
  history: createWebHistory(),
  routes: routes as RouteRecordRaw[],
});

// 路由守卫：登录校验 + 懒加载用户信息 + 简单权限控制
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore();
  const isLogin = !!userStore.token;

  // 未登录访问需要鉴权的页面，跳转登录页
  if (to.meta.requiresAuth && !isLogin && to.path !== '/login') {
    return next({ path: '/login', query: { redirect: to.fullPath } });
  }

  // 已登录访问登录页，直接跳转首页
  if (to.path === '/login' && isLogin) {
    return next({ path: '/' });
  }

  // 已登录但还未加载用户信息时，尝试加载
  if (isLogin && !userStore.profileLoaded) {
    try {
      await userStore.fetchProfile();
    } catch (e) {
      // token 失效或获取信息失败，清理后返回登录页
      userStore.logout();
      return next({ path: '/login', query: { redirect: to.fullPath } });
    }
  }

  // 权限控制：优先使用 menuCode + requiredLevel，兼容 meta.perms
  const menuCode = to.meta.menuCode as string | undefined;
  const requiredLevel = (to.meta.requiredLevel as number) ?? 1;
  const requiredPerms = to.meta.perms as string[] | undefined;

  if (menuCode) {
    if (!userStore.isAdmin && userStore.getMenuLevel(menuCode) < requiredLevel) {
      return next({ path: '/403' });
    }
  } else if (requiredPerms && requiredPerms.length > 0) {
    if (!userStore.isAdmin) {
      const hasPerm = requiredPerms.some((p) => userStore.hasFuncPermission(p));
      if (!hasPerm) {
        return next({ path: '/403' });
      }
    }
  }

  return next();
});

export default router;

