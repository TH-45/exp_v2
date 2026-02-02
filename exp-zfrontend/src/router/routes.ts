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
          title: '人员管理',
          icon: 'UserFilled',
          perms: ['system:user:view'],
        },
      },
      {
        path: 'system/account',
        name: 'SystemAccount',
        component: () => import('@/views/System/Account/index.vue'),
        meta: {
          title: '账号管理',
          icon: 'User',
          perms: ['system:user:view'],
        },
      },
      {
        path: 'system/post',
        name: 'SystemPost',
        component: () => import('@/views/System/Post/index.vue'),
        meta: {
          title: '岗位管理',
          icon: 'Suitcase',
          perms: ['system:post:view'],
        },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/System/Role/index.vue'),
        meta: {
          title: '角色管理',
          icon: 'Key',
          perms: ['system:role:view'],
        },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/System/Menu/index.vue'),
        meta: {
          title: '菜单管理',
          icon: 'Menu',
          perms: ['system:menu:view'],
        },
      },
      {
        path: 'system/dict',
        name: 'SystemDict',
        component: () => import('@/views/System/Dict/index.vue'),
        meta: {
          title: '字典管理',
          icon: 'Collection',
          perms: ['system:dict:view'],
        },
      },
      {
        path: 'system/permission',
        name: 'SystemPermission',
        component: () => import('@/views/System/Permission/index.vue'),
        meta: {
          title: '权限管理',
          icon: 'Monitor',
          perms: ['system:permission:view'],
        },
      },

      // 招投标管理
      {
        path: 'bidding/project',
        name: 'BiddingProject',
        component: () => import('@/views/Bidding/Project/index.vue'),
        meta: {
          title: '招标项目',
          icon: 'Document',
          perms: ['bidding:project:view'],
        },
      },
      {
        path: 'bidding/project/:projectId',
        name: 'BiddingProjectDetail',
        component: () => import('@/views/Bidding/ProjectDetail/index.vue'),
        meta: {
          title: '项目详情',
          icon: 'Document',
          perms: ['bidding:project:view'],
        },
      },
      {
        path: 'bidding/bid',
        name: 'BiddingBid',
        component: () => import('@/views/Bidding/Bid/index.vue'),
        meta: {
          title: '投标登记',
          icon: 'Tickets',
          perms: ['bidding:bid:view'],
        },
      },
      {
        path: 'bidding/evaluation',
        name: 'BiddingEvaluation',
        component: () => import('@/views/Bidding/Evaluation/index.vue'),
        meta: {
          title: '评标/定标',
          icon: 'Document',
          perms: ['bidding:evaluation:view'],
        },
      },
      {
        path: 'bidding/attachments',
        name: 'BiddingAttachments',
        component: () => import('@/views/Bidding/Attachments/index.vue'),
        meta: {
          title: '招投标附件库',
          icon: 'FolderOpened',
          perms: ['bidding:attachments:view'],
        },
      },

      // 合同管理
      {
        path: 'contracts/contract',
        name: 'ContractsContract',
        component: () => import('@/views/Contracts/Contract/index.vue'),
        meta: {
          title: '合同台账',
          icon: 'Document',
          perms: ['contracts:contract:view'],
        },
      },
      {
        path: 'contracts/contract/:contractId',
        name: 'ContractsContractDetail',
        component: () => import('@/views/Contracts/ContractDetail/index.vue'),
        meta: {
          title: '合同详情',
          icon: 'Document',
          perms: ['contracts:contract:view'],
        },
      },
      {
        path: 'contracts/change',
        name: 'ContractsChange',
        component: () => import('@/views/Contracts/Change/index.vue'),
        meta: {
          title: '合同变更',
          icon: 'Refresh',
          perms: ['contracts:change:view'],
        },
      },
      {
        path: 'contracts/change/:changeId',
        name: 'ContractsChangeDetail',
        component: () => import('@/views/Contracts/ChangeDetail/index.vue'),
        meta: {
          title: '变更详情',
          icon: 'Refresh',
          perms: ['contracts:change:view'],
        },
      },
      {
        path: 'contracts/payment',
        name: 'ContractsPayment',
        component: () => import('@/views/Contracts/Payment/index.vue'),
        meta: {
          title: '收付款台账',
          icon: 'Coin',
          perms: ['contracts:payment:view'],
        },
      },
      {
        path: 'contracts/payment/:paymentId',
        name: 'ContractsPaymentDetail',
        component: () => import('@/views/Contracts/PaymentDetail/index.vue'),
        meta: {
          title: '收付款详情',
          icon: 'Coin',
          perms: ['contracts:payment:view'],
        },
      },
      {
        path: 'contracts/attachments',
        name: 'ContractsAttachments',
        component: () => import('@/views/Contracts/Attachments/index.vue'),
        meta: {
          title: '合同附件库',
          icon: 'FolderOpened',
          perms: ['contracts:attachments:view'],
        },
      },

      // 审批/待办中心
      {
        path: 'approval',
        name: 'Approval',
        component: () => import('@/views/Approval/index.vue'),
        meta: {
          title: '审批/待办中心',
          icon: 'DocumentChecked',
          perms: ['approval:task:view'],
        },
      },

      // 企业信息与工程项目
      {
        path: 'corp-project',
        name: 'CorpProject',
        component: () => import('@/views/CorpProject/index.vue'),
        meta: {
          title: '企业信息与工程项目',
          icon: 'OfficeBuilding',
          perms: ['corp:info:view'],
        },
      },
      // 企业信息管理
      {
        path: 'corp-project/corp-info/accounts',
        name: 'CorpInfoAccounts',
        component: () => import('@/views/CorpProject/CorpInfo/AccountManagement.vue'),
        meta: {
          title: '账号信息管理',
          icon: 'User',
          perms: ['corp:account:view'],
        },
      },
      {
        path: 'corp-project/corp-info/announcements',
        name: 'CorpInfoAnnouncements',
        component: () => import('@/views/CorpProject/CorpInfo/AnnouncementManagement.vue'),
        meta: {
          title: '制度与公告',
          icon: 'Document',
          perms: ['corp:announcement:view'],
        },
      },
      {
        path: 'corp-project/corp-info/qualifications',
        name: 'CorpInfoQualifications',
        component: () => import('@/views/CorpProject/CorpInfo/QualificationManagement.vue'),
        meta: {
          title: '企业资质管理',
          icon: 'Medal',
          perms: ['corp:qualification:view'],
        },
      },
      {
        path: 'corp-project/corp-info/basic-info',
        name: 'CorpInfoBasicInfo',
        component: () => import('@/views/CorpProject/CorpInfo/BasicInfoManagement.vue'),
        meta: {
          title: '企业基础信息',
          icon: 'InfoFilled',
          perms: ['corp:basic:view'],
        },
      },
      // 工程项目管理
      {
        path: 'corp-project/project-mgmt/projects',
        name: 'ProjectMgmtProjects',
        component: () => import('@/views/CorpProject/ProjectMgmt/ProjectManagement.vue'),
        meta: {
          title: '项目管理',
          icon: 'Management',
          perms: ['project:project:view'],
        },
      },
      {
        path: 'corp-project/project-mgmt/members/:projectId?',
        name: 'ProjectMgmtMembers',
        component: () => import('@/views/CorpProject/ProjectMgmt/ProjectMembers.vue'),
        meta: {
          title: '项目人员配置',
          icon: 'UserFilled',
          perms: ['project:member:view'],
        },
      },
      {
        path: 'corp-project/project-mgmt/progress/:projectId?',
        name: 'ProjectMgmtProgress',
        component: () => import('@/views/CorpProject/ProjectMgmt/ProjectProgress.vue'),
        meta: {
          title: '项目进度管理',
          icon: 'TrendCharts',
          perms: ['project:progress:view'],
        },
      },
      {
        path: 'corp-project/project-mgmt/materials/:projectId?',
        name: 'ProjectMgmtMaterials',
        component: () => import('@/views/CorpProject/ProjectMgmt/ProjectMaterials.vue'),
        meta: {
          title: '项目物料管理',
          icon: 'Box',
          perms: ['project:material:view'],
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

