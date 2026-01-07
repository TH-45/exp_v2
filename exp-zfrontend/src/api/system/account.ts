import request from '@/api/request';

/**
 * 账号管理（System/User）
 *
 * 接口参考 docs/public/接口约定.md：
 * - GET  /exp/system/user/list
 * - POST /exp/system/user/create
 * - POST /exp/system/user/update
 * - POST /exp/system/user/delete
 * - POST /exp/system/user/status
 * - POST /exp/system/user/resetPassword
 */

export type UserStatus = 0 | 1;

export interface SystemUserVO {
  userId: string;
  username: string;
  realName?: string;
  deptId?: string;
  deptName?: string;
  mobile?: string;
  email?: string;
  status?: UserStatus;
  roleIds?: string[];
  /** 便于表格展示（后端若直接返回 roleName/roleNames 也可用） */
  roleNames?: string[] | string;
  createTime?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface QueryUserParams {
  page: number;
  pageSize: number;
  keyword?: string;
  deptId?: string;
  status?: UserStatus;
}

// 新的账号查询参数接口
export interface QueryAccountParams {
  pageNum: number;
  pageSize: number;
  sort?: string;
  queryParam: {
    accountName?: string;
    accountDisplay?: string;
    mobile?: string;
  };
}

// 新的账号数据接口 - 对应实际返回数据
export interface AccountVO {
  accountId: number;        // 账号ID
  accountName: string;      // 登录账号名
  personName?: string;      // 姓名 (原accountDisplay)
  personId?: number;        // 关联人员ID
  mobile?: string;          // 手机号
  email?: string;           // 邮箱
  orgId?: number;           // 组织ID
  orgCode?: string;         // 组织代码
  orgName?: string;         // 组织名称
  postId?: number;          // 岗位ID
  postCode?: string;        // 岗位代码
  postName?: string;        // 岗位名称
  status: 'ENABLED' | 'DISABLED' | 'LOCKED' | 'INIT'; // 状态
  createdTime?: string;     // 创建时间
  remark?: string;          // 备注
}

// 新的账号分页结果接口
export interface AccountPageResult {
  total: number;
  page: number;
  size: number;
  list: AccountVO[];
}

export interface CreateUserPayload {
  accountName: string;
  accountDisplay: string;
  mobile?: string;
  email?: string;
  personId: number;
  orgId: number;
  postId: number;
  remark?: string;
}

export interface UpdateUserPayload {
  accountId: number;
  accountDisplay: string;
  mobile?: string;
  email?: string;
  personId: number;
  orgId: number;
  postId: number;
  remark?: string;
}

export function queryUserList(params: QueryUserParams) {
  return request.get<PageResult<SystemUserVO>, PageResult<SystemUserVO>>('/exp/system/user/list', {
    params,
  });
}

// 新的账号查询接口
export function queryAccountList(params: QueryAccountParams) {
  return request.post<AccountPageResult, AccountPageResult>('/exp/auth/account/list', params);
}

export function createUser(data: CreateUserPayload) {
  return request.post<void, void>('/exp/system/user/create', data);
}

export function updateUser(data: UpdateUserPayload) {
  return request.post<void, void>('/exp/system/user/update', data);
}

export function deleteUser(accountId: number, remark?: string) {
  return request.post<void, void>('/exp/auth/account/delete', { accountId, remark });
}

export function setUserStatus(accountId: number, status: string, remark?: string) {
  return request.post<AccountVO, AccountVO>('/exp/auth/account/status', { accountId, status, remark });
}

export function resetUserPassword(accountId: number, newPassword: string, remark?: string) {
  return request.post<void, void>('/exp/auth/account/resetPassword', { accountId, newPassword, remark });
}

export function getAccountDetail(accountId: number) {
  return request.get<AccountVO, AccountVO>('/exp/auth/account/detail', { params: { accountId } });
}


