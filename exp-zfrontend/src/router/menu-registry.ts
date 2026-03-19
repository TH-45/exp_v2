/**
 * 菜单注册表：menuCode -> 路由与组件映射。
 * 后端不下发 component，由前端本地维护。
 * 当后端返回的 menuCode 未注册时：不展示该菜单、不允许访问、记录告警。
 */
import type { Component } from 'vue';

export interface MenuRegistryItem {
  routePath: string;
  component: () => Promise<{ default: Component }>;
  title: string;
  icon?: string;
}

export const menuRegistry: Record<string, MenuRegistryItem> = {
  // 系统管理
  'system:account': {
    routePath: '/system/account',
    component: () => import('@/views/System/Account/index.vue'),
    title: '账号管理',
    icon: 'User',
  },
  'system:user': {
    routePath: '/system/user',
    component: () => import('@/views/System/User/index.vue'),
    title: '人员管理',
    icon: 'UserFilled',
  },
  /**
   * 组织与岗位管理：同一页面包含左侧组织树、右侧岗位配置，统一使用 system:organdpost。
   * 后端 OrgUnitController、PositionController 均使用此 menuCode 做权限校验。
   */
  'system:organdpost': {
    routePath: '/system/post',
    component: () => import('@/views/System/Post/index.vue'),
    title: '岗位管理',
    icon: 'Suitcase',
  },
  'system:role': {
    routePath: '/system/role',
    component: () => import('@/views/System/Role/index.vue'),
    title: '角色管理',
    icon: 'Key',
  },
  'system:menu': {
    routePath: '/system/menu',
    component: () => import('@/views/System/Menu/index.vue'),
    title: '菜单管理',
    icon: 'Menu',
  },
  'system:dict': {
    routePath: '/system/dict',
    component: () => import('@/views/System/Dict/index.vue'),
    title: '字典管理',
    icon: 'Collection',
  },
  // 招投标管理
  'bidding:project': {
    routePath: '/bidding/project',
    component: () => import('@/views/Bidding/Project/index.vue'),
    title: '招标项目',
    icon: 'Document',
  },
  'bidding:bid': {
    routePath: '/bidding/bid',
    component: () => import('@/views/Bidding/Bid/index.vue'),
    title: '投标登记',
    icon: 'Tickets',
  },
  'bidding:evaluation': {
    routePath: '/bidding/evaluation',
    component: () => import('@/views/Bidding/Evaluation/index.vue'),
    title: '评标/定标',
    icon: 'Document',
  },
  'bidding:attachments': {
    routePath: '/bidding/attachments',
    component: () => import('@/views/Bidding/Attachments/index.vue'),
    title: '招投标附件库',
    icon: 'FolderOpened',
  },
  // 合同管理
  'contracts:contract': {
    routePath: '/contracts/contract',
    component: () => import('@/views/Contracts/Contract/index.vue'),
    title: '合同台账',
    icon: 'Document',
  },
  'contracts:change': {
    routePath: '/contracts/change',
    component: () => import('@/views/Contracts/Change/index.vue'),
    title: '合同变更',
    icon: 'Refresh',
  },
  'contracts:payment': {
    routePath: '/contracts/payment',
    component: () => import('@/views/Contracts/Payment/index.vue'),
    title: '收付款台账',
    icon: 'Coin',
  },
  'contracts:attachments': {
    routePath: '/contracts/attachments',
    component: () => import('@/views/Contracts/Attachments/index.vue'),
    title: '合同附件库',
    icon: 'FolderOpened',
  },
  // 审批管理
  'approval:center': {
    routePath: '/approval',
    component: () => import('@/views/Approval/index.vue'),
    title: '审批/待办中心',
    icon: 'DocumentChecked',
  },
  'process:start': {
    routePath: '/approval/start-center',
    component: () => import('@/views/Approval/StartCenter.vue'),
    title: '流程发起中心',
    icon: 'Grid',
  },
  'process:definition': {
    routePath: '/approval/definition',
    component: () => import('@/views/Approval/ProcessDefinition.vue'),
    title: '流程定义',
    icon: 'Connection',
  },
  // 企业信息
  'enterprise:basic': {
    routePath: '/enterprise/basic-info',
    component: () => import('@/views/Enterprise/BasicInfoManagement.vue'),
    title: '基础信息',
    icon: 'InfoFilled',
  },
  'enterprise:qualifications': {
    routePath: '/enterprise/qualifications',
    component: () => import('@/views/Enterprise/QualificationManagement.vue'),
    title: '证件资质',
    icon: 'Medal',
  },
  'enterprise:announcements': {
    routePath: '/enterprise/announcements',
    component: () => import('@/views/Enterprise/AnnouncementManagement.vue'),
    title: '制度与公告',
    icon: 'Bell',
  },
  // 工程项目
  'project:management': {
    routePath: '/corp-project/project-mgmt/projects',
    component: () => import('@/views/CorpProject/ProjectMgmt/ProjectManagement.vue'),
    title: '项目管理',
    icon: 'Management',
  },
  'project:members': {
    routePath: '/corp-project/project-mgmt/members',
    component: () => import('@/views/CorpProject/ProjectMgmt/ProjectMembers.vue'),
    title: '项目人员配置',
    icon: 'UserFilled',
  },
  'project:progress': {
    routePath: '/corp-project/project-mgmt/progress',
    component: () => import('@/views/CorpProject/ProjectMgmt/ProjectProgress.vue'),
    title: '项目进度管理',
    icon: 'TrendCharts',
  },
  'project:materials': {
    routePath: '/corp-project/project-mgmt/materials',
    component: () => import('@/views/CorpProject/ProjectMgmt/ProjectMaterials.vue'),
    title: '项目物料管理',
    icon: 'Box',
  },
};

/**
 * 根据 menuCode 获取注册项，未注册时返回 null 并记录告警。
 */
export function getMenuRegistryItem(menuCode: string): MenuRegistryItem | null {
  const item = menuRegistry[menuCode];
  if (!item) {
    if (import.meta.env.DEV) {
      console.warn('[menuRegistry] 未注册的 menuCode:', menuCode);
    }
    return null;
  }
  return item;
}
