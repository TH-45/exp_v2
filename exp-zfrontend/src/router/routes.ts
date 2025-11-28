import type { RouteRecordRaw } from 'vue-router';

import MainLayout from '@/layouts/MainLayout.vue';
import BlankLayout from '@/layouts/BlankLayout.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: BlankLayout,
    children: [
      {
        path: '',
        name: 'Login',
        component: () => import('@/views/Login/index.vue'),
        meta: { title: '登录' },
      },
    ],
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'House' },
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/System/User/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          perms: ['system:user:view'],
        },
      },
    ],
  },
  {
    path: '/403',
    component: BlankLayout,
    children: [
      {
        path: '',
        name: 'Forbidden',
        component: () => import('@/views/Error/Forbidden.vue'),
        meta: { title: '无访问权限' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
];

export default routes;

