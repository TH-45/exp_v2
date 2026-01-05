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

export interface CreateUserPayload {
  username: string;
  realName: string;
  deptId?: string;
  mobile?: string;
  email?: string;
  roleIds?: string[];
  password?: string;
}

export interface UpdateUserPayload {
  userId: string;
  realName?: string;
  deptId?: string;
  mobile?: string;
  email?: string;
  roleIds?: string[];
}

export function queryUserList(params: QueryUserParams) {
  return request.get<PageResult<SystemUserVO>, PageResult<SystemUserVO>>('/exp/system/user/list', {
    params,
  });
}

export function createUser(data: CreateUserPayload) {
  return request.post<void, void>('/exp/system/user/create', data);
}

export function updateUser(data: UpdateUserPayload) {
  return request.post<void, void>('/exp/system/user/update', data);
}

export function deleteUser(userIds: string[] | string) {
  const ids = Array.isArray(userIds) ? userIds : [userIds];
  // 文档是 userId，批量场景扩展为 userIds；这里兼容两种入参
  return request.post<void, void>('/exp/system/user/delete', {
    userId: ids.length === 1 ? ids[0] : undefined,
    userIds: ids,
  });
}

export function setUserStatus(userId: string, status: UserStatus) {
  return request.post<void, void>('/exp/system/user/status', { userId, status });
}

export function resetUserPassword(userIds: string[] | string, password: string) {
  const ids = Array.isArray(userIds) ? userIds : [userIds];
  // 文档是 userId + password；这里兼容批量 userIds
  return request.post<void, void>('/exp/system/user/resetPassword', {
    userId: ids.length === 1 ? ids[0] : undefined,
    userIds: ids,
    password,
  });
}


