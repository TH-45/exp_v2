import request from '@/api/request';

export type RoleStatus = 'ENABLED' | 'DISABLED';

export interface RoleVO {
  roleId: string;
  /** 前端生成且只读 */
  roleCode: string;
  roleName: string;
  status?: RoleStatus;
  remark?: string;
  createTime?: string;
  updateTime?: string;

  /** 后端可选字段：用于前端控制“禁止删除” */
  canDelete?: boolean;
}

export interface PageResult<T> {
  /** 文档约定字段 */
  records?: T[];
  total?: number;
  page?: number;
  pageSize?: number;
  /** 兼容后端常见字段 */
  list?: T[];
  rows?: T[];
}

export interface RoleListQuery {
  page: number;
  pageSize: number;
  keyword?: string;
  roleName?: string;
  roleCode?: string;
  status?: RoleStatus;
}

const BASE = '/exp/system/role';

export function listRoles(params: RoleListQuery) {
  return request.get<PageResult<RoleVO>, PageResult<RoleVO>>(`${BASE}/list`, { params });
}

export function getRoleDetail(roleId: string) {
  return request.get<RoleVO, RoleVO>(`${BASE}/detail`, { params: { roleId } });
}

export interface RolePermDTO {
  /** 菜单节点 key（如 system/user） */
  menus: string[];
  /**
   * 功能权限（跟随菜单节点）
   * key: menuKey, value: permissions[]
   */
  menuPerms: Record<string, string[]>;
}

// 权限配置（文档未写明，先按同模块扩展：/perm/detail + /perm/save）
export function getRolePerm(roleId: string) {
  return request.get<RolePermDTO, RolePermDTO>(`${BASE}/perm/detail`, { params: { roleId } });
}

export function saveRolePerm(roleId: string, data: RolePermDTO) {
  return request.post<void, void>(`${BASE}/perm/save`, { roleId, ...data });
}

export function createRole(data: Partial<RoleVO>) {
  // 后端如返回 roleId，可在页面中接住用于后续权限保存；不返回也不影响基础保存
  return request.post<{ roleId?: string }, { roleId?: string }>(`${BASE}/create`, data);
}

export function updateRole(data: Partial<RoleVO>) {
  return request.post<void, void>(`${BASE}/update`, data);
}

export function deleteRole(roleId: string) {
  return request.post<void, void>(`${BASE}/delete`, { roleId });
}


