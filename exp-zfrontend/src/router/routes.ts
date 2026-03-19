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
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile/index.vue'),
        meta: { title: '个人信息' },
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/System/User/index.vue'),
        meta: {
          title: '人员管理',
          icon: 'UserFilled',
          menuCode: 'system:user',
          requiredLevel: 1,
        },
      },
      {
        path: 'system/account',
        name: 'SystemAccount',
        component: () => import('@/views/System/Account/index.vue'),
        meta: {
          title: '账号管理',
          icon: 'User',
          menuCode: 'system:account',
          requiredLevel: 1,
        },
      },
      {
        path: 'system/post',
        name: 'SystemPost',
        component: () => import('@/views/System/Post/index.vue'),
        meta: {
          title: '岗位管理',
          icon: 'Suitcase',
          menuCode: 'system:organdpost',
          requiredLevel: 1,
        },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/System/Role/index.vue'),
        meta: {
          title: '角色管理',
          icon: 'Key',
          menuCode: 'system:role',
          requiredLevel: 1,
        },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/System/Menu/index.vue'),
        meta: {
          title: '菜单管理',
          icon: 'Menu',
          menuCode: 'system:menu',
          requiredLevel: 1,
        },
      },
      {
        path: 'system/dict',
        name: 'SystemDict',
        component: () => import('@/views/System/Dict/index.vue'),
        meta: {
          title: '字典管理',
          icon: 'Collection',
          menuCode: 'system:dict',
          requiredLevel: 1,
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
          menuCode: 'bidding:project',
          requiredLevel: 1,
        },
      },
      {
        path: 'bidding/project/:projectId',
        name: 'BiddingProjectDetail',
        component: () => import('@/views/Bidding/ProjectDetail/index.vue'),
        meta: {
          title: '项目详情',
          icon: 'Document',
          menuCode: 'bidding:project',
          requiredLevel: 1,
        },
      },
      {
        path: 'bidding/bid',
        name: 'BiddingBid',
        component: () => import('@/views/Bidding/Bid/index.vue'),
        meta: {
          title: '投标登记',
          icon: 'Tickets',
          menuCode: 'bidding:bid',
          requiredLevel: 1,
        },
      },
      {
        path: 'bidding/bid/:bidId',
        name: 'BiddingBidDetail',
        component: () => import('@/views/Bidding/BidDetail/index.vue'),
        meta: {
          title: '投标详情',
          icon: 'Tickets',
          menuCode: 'bidding:bid',
          requiredLevel: 1,
        },
      },
      {
        path: 'bidding/evaluation',
        name: 'BiddingEvaluation',
        component: () => import('@/views/Bidding/Evaluation/index.vue'),
        meta: {
          title: '评标/定标',
          icon: 'Document',
          menuCode: 'bidding:evaluation',
          requiredLevel: 1,
        },
      },
      {
        path: 'bidding/attachments',
        name: 'BiddingAttachments',
        component: () => import('@/views/Bidding/Attachments/index.vue'),
        meta: {
          title: '招投标附件库',
          icon: 'FolderOpened',
          menuCode: 'bidding:attachments',
          requiredLevel: 1,
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
          menuCode: 'contracts:contract',
          requiredLevel: 1,
        },
      },
      {
        path: 'contracts/contract/create',
        name: 'ContractsContractCreate',
        component: () => import('@/views/Contracts/ContractDraft/index.vue'),
        meta: {
          title: '新增合同',
          icon: 'Document',
          menuCode: 'contracts:contract',
          requiredLevel: 2,
        },
      },
      {
        path: 'contracts/contract/:contractId',
        name: 'ContractsContractDetail',
        component: () => import('@/views/Contracts/ContractDraft/index.vue'),
        meta: {
          title: '合同起草/审批/拟签',
          icon: 'Document',
          menuCode: 'contracts:contract',
          requiredLevel: 1,
        },
      },
      {
        path: 'contracts/change',
        name: 'ContractsChange',
        component: () => import('@/views/Contracts/Change/index.vue'),
        meta: {
          title: '合同变更',
          icon: 'Refresh',
          menuCode: 'contracts:change',
          requiredLevel: 1,
        },
      },
      {
        path: 'contracts/change/:changeId',
        name: 'ContractsChangeDetail',
        component: () => import('@/views/Contracts/ChangeDetail/index.vue'),
        meta: {
          title: '变更详情',
          icon: 'Refresh',
          menuCode: 'contracts:change',
          requiredLevel: 1,
        },
      },
      {
        path: 'contracts/payment',
        name: 'ContractsPayment',
        component: () => import('@/views/Contracts/Payment/index.vue'),
        meta: {
          title: '收付款台账',
          icon: 'Coin',
          menuCode: 'contracts:payment',
          requiredLevel: 1,
        },
      },
      {
        path: 'contracts/payment/:paymentId',
        name: 'ContractsPaymentDetail',
        component: () => import('@/views/Contracts/PaymentDetail/index.vue'),
        meta: {
          title: '收付款详情',
          icon: 'Coin',
          menuCode: 'contracts:payment',
          requiredLevel: 1,
        },
      },
      {
        path: 'contracts/attachments',
        name: 'ContractsAttachments',
        component: () => import('@/views/Contracts/Attachments/index.vue'),
        meta: {
          title: '合同附件库',
          icon: 'FolderOpened',
          menuCode: 'contracts:attachments',
          requiredLevel: 1,
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
          menuCode: 'approval:center',
          requiredLevel: 1,
        },
      },
      {
        path: 'approval/instance/:instanceId',
        name: 'ApprovalInstanceDetail',
        component: () => import('@/views/Approval/InstanceDetail/index.vue'),
        meta: {
          title: '工单详细',
          menuCode: 'approval:center',
          requiredLevel: 1,
        },
      },
      {
        path: 'approval/start-center',
        name: 'ApprovalStartCenter',
        component: () => import('@/views/Approval/StartCenter.vue'),
        meta: {
          title: '流程发起中心',
          icon: 'Grid',
          menuCode: 'process:start',
          requiredLevel: 1,
        },
      },
      {
        path: 'approval/start-dispatch',
        name: 'ApprovalStartDispatch',
        component: () => import('@/views/Approval/StartDispatch.vue'),
        meta: {
          title: '流程路由分发',
          menuCode: 'process:start',
          requiredLevel: 1,
        },
      },
      {
        path: 'approval/definition',
        name: 'ApprovalDefinition',
        component: () => import('@/views/Approval/ProcessDefinition.vue'),
        meta: {
          title: '流程定义',
          icon: 'Connection',
          menuCode: 'process:definition',
          requiredLevel: 1,
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
          menuCode: 'enterprise:basic',
          requiredLevel: 1,
        },
      },
      {
        path: 'enterprise/basic-info',
        name: 'CorpInfoBasicInfo',
        component: () => import('@/views/Enterprise/BasicInfoManagement.vue'),
        meta: {
          title: '基础信息管理',
          icon: 'InfoFilled',
          menuCode: 'enterprise:basic',
          requiredLevel: 1,
        },
      },
      {
        path: 'enterprise/qualifications',
        name: 'EnterpriseQualifications',
        component: () => import('@/views/Enterprise/QualificationManagement.vue'),
        meta: {
          title: '证件资质',
          icon: 'Medal',
          menuCode: 'enterprise:qualifications',
          requiredLevel: 1,
        },
      },
      {
        path: 'enterprise/announcements',
        name: 'EnterpriseAnnouncements',
        component: () => import('@/views/Enterprise/AnnouncementManagement.vue'),
        meta: {
          title: '制度与公告',
          icon: 'Bell',
          menuCode: 'enterprise:announcements',
          requiredLevel: 1,
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
          menuCode: 'project:management',
          requiredLevel: 1,
        },
      },
      {
        path: 'corp-project/project-mgmt/members/:projectId?',
        name: 'ProjectMgmtMembers',
        component: () => import('@/views/CorpProject/ProjectMgmt/ProjectMembers.vue'),
        meta: {
          title: '项目人员配置',
          icon: 'UserFilled',
          menuCode: 'project:members',
          requiredLevel: 1,
        },
      },
      {
        path: 'corp-project/project-mgmt/progress/:projectId?',
        name: 'ProjectMgmtProgress',
        component: () => import('@/views/CorpProject/ProjectMgmt/ProjectProgress.vue'),
        meta: {
          title: '项目进度管理',
          icon: 'TrendCharts',
          menuCode: 'project:progress',
          requiredLevel: 1,
        },
      },
      {
        path: 'corp-project/project-mgmt/materials/:projectId?',
        name: 'ProjectMgmtMaterials',
        component: () => import('@/views/CorpProject/ProjectMgmt/ProjectMaterials.vue'),
        meta: {
          title: '项目物料管理',
          icon: 'Box',
          menuCode: 'project:materials',
          requiredLevel: 1,
        },
      },
    ],
  },
  {
    path: '/approval/definition/node-config',
    name: 'ApprovalNodeConfig',
    component: () => import('@/views/Approval/ProcessNodeConfig.vue'),
    meta: {
      requiresAuth: true,
      title: '节点配置',
      menuCode: 'process:definition',
      requiredLevel: 1,
    },
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

