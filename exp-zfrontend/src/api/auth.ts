import request from './request';

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResult {
  token: string;
  userId: string;
  username: string;
  roles: string[];
  permissions: string[];
}

/** 用户基础信息（登录态），权限以 PermissionProfileResult 为准 */
export interface ProfileResult {
  userId: string;
  username: string;
  deptId?: string;
  deptName?: string;
  roles: string[];
}

/** 权限画像（full snapshot） */
export interface PermissionProfileResult {
  userId: number;
  username: string;
  roles: string[];
  permissionVersion: number;
  menuTree?: MenuNode[];
  menuLevelMap?: Record<string, number>;
  funcPermissionSet?: string[];
  dataScopeSummary?: DataScopeSummary;
}

export interface MenuNode {
  menuCode: string;
  menuName: string;
  icon?: string;
  sortNo?: number;
  permLevel?: number;
  nodeType: string;
  children?: MenuNode[];
}

export interface DataScopeSummary {
  scopeType?: string;
  orgIds?: number[];
  projectIds?: number[];
}

export interface ProfileDetailResult {
  userId: string;
  username: string;
  personInfo?: {
    personId?: number;
    personCode?: string;
    personName?: string;
    gender?: string;
    mobile?: string;
    email?: string;
    status?: string;
    entryDate?: string;
    jobTitle?: string;
  };
  accountInfo?: {
    accountId?: number;
    accountName?: string;
    accountDisplay?: string;
    status?: string;
    lastLoginTime?: string;
    needChangePwd?: boolean;
  };
  orgInfo?: {
    orgId?: number;
    orgCode?: string;
    orgName?: string;
    orgType?: string;
    managerName?: string;
    contactPhone?: string;
    parentOrgId?: number;
    parentOrgName?: string;
  };
}

export function loginApi(data: LoginParams) {
  // 实际请求路径：/api/exp/auth/login
  return request.post<LoginResult, LoginResult>('/exp/auth/login', data);
}

export function getProfileApi() {
  // 实际请求路径：/api/exp/auth/profile
  return request.get<ProfileResult, ProfileResult>('/exp/auth/profile');
}

export function getProfileDetailApi() {
  return request.get<ProfileDetailResult, ProfileDetailResult>('/exp/auth/profile/detail');
}

/** 获取权限画像（full snapshot） */
export function getPermissionProfileApi() {
  return request.get<PermissionProfileResult, PermissionProfileResult>('/exp/auth/permission/profile');
}



