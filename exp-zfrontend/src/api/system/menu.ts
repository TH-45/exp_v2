import request from '@/api/request';
import { buildPageQuery, type PageQueryInput, type PageResult } from '@/api/common';

/**
 * 菜单管理（System/Menu）
 * 说明：后端接口如有差异，可统一在这里调整，不影响页面层。
 */

export type MenuType = 'MENU' | 'PAGE';
export type MenuStatus = 'ENABLED' | 'DISABLED' | 0 | 1;
export type VisibleStatus = 0 | 1;

export interface MenuItem {
  menuId: string | number;
  parentMenuId?: string | number;
  menuCode: string;
  menuName: string;
  menuType: MenuType;
  routePath?: string;
  component?: string;
  visible?: VisibleStatus;
  status?: MenuStatus;
  sortNo?: number;
  remark?: string;
  children?: MenuItem[];
  hasChildren?: boolean;
  /** 权限等级(0无权、1查看、2编辑、3管理) */
  permLevel?: string;
}

export interface QueryMenuParams {
  menuCode?: string;
  menuName?: string;
  menuType?: MenuType;
  status?: MenuStatus;
}

export interface SaveMenuPayload {
  menuId?: string;
  parentMenuId?: string;
  menuCode: string;
  menuName: string;
  menuType: MenuType;
  routePath?: string;
  component?: string;
  icon?: string;
  sortNo?: number;
  visible?: VisibleStatus;
  remark?: string;
}

// 约定路径（可按后端实际调整）
const BASE = '/exp/auth/menu';

export function queryMenuTree() {
  return request.get<MenuItem[], MenuItem[]>(`${BASE}/tree`);
}


/**
 * 查询菜单权限树
 * - 后端当前实现要求 roleId 必传
 * - permLevel：预留字段（OpenAPI 文档存在，但当前后端接口未实现该过滤参数）
 */
export function queryMenuPermissionTree(roleId?: string | number, permLevel?: string) {
  const params: { roleId?: string | number; permLevel?: string } = {};
  if (roleId != null) params.roleId = roleId;
  if (permLevel != null) params.permLevel = permLevel;
  return request.get<MenuItem[],MenuItem[]>(`${BASE}/permissionTree`, {
    params: Object.keys(params).length ? params : undefined,
  });
}

export interface UpdateMenuPermissionTreePayload {
  roleId: string | number;
  menuNodes: Array<{
    menuCode: string;
    /** 权限等级(0无权、1查看、2编辑、3管理) */
    permLevel: string;
  }>;
}

/** 修改菜单权限树（增量提交：仅提交变化的叶子节点，含 1->0） */
export function updateMenuPermissionTree(data: UpdateMenuPermissionTreePayload) {
  return request.post<void, void>(`${BASE}/updatePermissionTree`, data);
}

export function queryMenuList(params: PageQueryInput<QueryMenuParams>) {
  return request.post<PageResult<MenuItem>, PageResult<MenuItem>>(
    `${BASE}/list`,
    buildPageQuery<QueryMenuParams>(params),
  );
}

export function createMenu(data: SaveMenuPayload) {
  return request.post<void, void>(`${BASE}/create`, data);
}

export function updateMenu(data: SaveMenuPayload) {
  return request.post<void, void>(`${BASE}/update`, data);
}

export function deleteMenu(menuIds: string[] | string) {
  const ids = Array.isArray(menuIds) ? menuIds : [menuIds];
  return request.post<void, void>(`${BASE}/delete`, {
    menuId: ids.length === 1 ? ids[0] : undefined,
    menuIds: ids,
  });
}

export function setMenuStatus(menuId: string, status: MenuStatus) {
  return request.post<void, void>(`${BASE}/status`, { menuId, status });
}


