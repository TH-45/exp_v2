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
  menuId: string;
  parentMenuId?: string;
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
  perLevel?: string;
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

/** 菜单权限树响应：查权限并结构对应到树（tree + 该角色已选菜单 ID） */
export interface MenuPermissionTreeRes {
  /** 菜单树，与 /tree 结构一致 */
  tree: MenuItem[];
  /** 当前角色已拥有的菜单权限 ID 列表，与树对应便于 setCheckedKeys */
  selectedMenuIds: (string | number)[];
}

/** 查询菜单权限树（传 roleId 时查该角色权限并填充 selectedMenuIds；perLevel 表示权限等级） */
export function queryMenuPermissionTree(roleId?: string, perLevel?: string) {
  const params: { roleId?: string; perLevel?: string } = {};
  if (roleId != null) params.roleId = roleId;
  if (perLevel != null) params.perLevel = perLevel;
  return request.get<MenuPermissionTreeRes, MenuPermissionTreeRes>(`${BASE}/permissionTree`, {
    params: Object.keys(params).length ? params : undefined,
  });
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


