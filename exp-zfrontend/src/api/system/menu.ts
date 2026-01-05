import request from '@/api/request';

/**
 * 菜单管理（System/Menu）
 * 说明：后端接口如有差异，可统一在这里调整，不影响页面层。
 */

export type MenuType = 'DIR' | 'MENU' | 'BUTTON';
export type MenuStatus = 0 | 1;
export type VisibleStatus = 0 | 1;

export interface MenuItem {
  menuId: string;
  parentId?: string;
  name: string;
  type: MenuType;
  path?: string;
  component?: string;
  perms?: string;
  icon?: string;
  sortNo?: number;
  visible?: VisibleStatus;
  status?: MenuStatus;
  updateTime?: string;
  children?: MenuItem[];
}

export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface QueryMenuParams {
  page: number;
  pageSize: number;
  parentId?: string;
  keyword?: string;
  type?: MenuType;
  status?: MenuStatus;
  visible?: VisibleStatus;
}

export interface SaveMenuPayload {
  menuId?: string;
  parentId?: string;
  name: string;
  type: MenuType;
  path?: string;
  component?: string;
  perms?: string;
  icon?: string;
  sortNo?: number;
  visible?: VisibleStatus;
  status?: MenuStatus;
}

// 约定路径（可按后端实际调整）
const BASE = '/exp/system/menu';

export function queryMenuTree() {
  return request.get<MenuItem[], MenuItem[]>(`${BASE}/tree`);
}

export function queryMenuList(params: QueryMenuParams) {
  return request.get<PageResult<MenuItem>, PageResult<MenuItem>>(`${BASE}/list`, { params });
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


