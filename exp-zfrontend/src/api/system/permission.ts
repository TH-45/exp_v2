import request from '@/api/request';
import { buildPageQuery, type PageQueryInput, type PageResult } from '@/api/common';

/**
 * 权限管理（System/Permission）
 * 说明：用于管理系统权限点，配合角色进行权限分配。
 */

export type PermissionType = 'MENU' | 'FUNC';
export type PermissionStatus = 'ENABLED' | 'DISABLED';

export interface PermissionItem {
  permId: string;
  /** 权限编码（唯一），如 TENDER:VIEW、BID:ADD 等 */
  permCode: string;
  /** 权限名称，如"招标查看"、"投标新增"等 */
  permName: string;
  /** 权限类型：MENU-菜单权限，FUNC-功能操作权限 */
  permType: PermissionType;
  /** 所属功能模块编码，如 TENDER、BID、CONTRACT 等 */
  moduleCode: string;
  /** 所属菜单分组/路径（用于前端菜单分组展示，可选） */
  menuGroup?: string;
  /** 操作编码（VIEW、ADD、EDIT、DELETE、APPROVE 等，仅 FUNC 类型使用） */
  actionCode?: string;
  /** 前端路由地址或接口路径（主要用于 MENU 类型，可选） */
  urlPath?: string;
  /** 状态 */
  status: PermissionStatus;
  /** 排序号 */
  sortNo?: number;
  /** 备注 */
  remark?: string;
  /** 子权限 */
  children?: PermissionItem[];
}

export interface QueryPermissionParams {
  permCode?: string;
  permName?: string;
  permType?: PermissionType;
  moduleCode?: string;
  status?: PermissionStatus;
}

export interface SavePermissionPayload {
  permId?: string;
  permCode: string;
  permName: string;
  permType: PermissionType;
  moduleCode: string;
  menuGroup?: string;
  actionCode?: string;
  urlPath?: string;
  status: PermissionStatus;
  sortNo?: number;
  remark?: string;
}

// 约定路径（可按后端实际调整）
const BASE = '/exp/auth/permission';

export function queryPermissionTree() {
  return request.get<PermissionItem[], PermissionItem[]>(`${BASE}/tree`);
}

export function queryPermissionList(params: PageQueryInput<QueryPermissionParams>) {
  return request.post<PageResult<PermissionItem>, PageResult<PermissionItem>>(
    `${BASE}/list`,
    buildPageQuery<QueryPermissionParams>(params),
  );
}

export function createPermission(data: SavePermissionPayload) {
  return request.post<void, void>(`${BASE}/create`, data);
}

export function updatePermission(data: SavePermissionPayload) {
  return request.post<void, void>(`${BASE}/update`, data);
}

export function deletePermission(permIds: string[] | string) {
  const ids = Array.isArray(permIds) ? permIds : [permIds];
  return request.post<void, void>(`${BASE}/delete`, {
    permId: ids.length === 1 ? ids[0] : undefined,
    permIds: ids,
  });
}

export function setPermissionStatus(permId: string, status: PermissionStatus) {
  return request.post<void, void>(`${BASE}/status`, { permId, status });
}

export function getPermissionDetail(permId: string) {
  return request.get<PermissionItem, PermissionItem>(`${BASE}/detail`, { params: { permId } });
}

/** 更新菜单树权限请求（与后端 UpdateMenuTreePermissionReq 一致） */
export interface UpdateMenuTreePermissionPayload {
  roleId: string | number;
  /** 更改的权限的等级（如 0-无权、1-查看、2-编辑、3-管理） */
  perLevel?: string;
  /** 选中的菜单 ID 列表 */
  menuIds?: (string | number)[];
}

/** 更新菜单树权限（角色勾选的菜单 ID 及权限等级） */
export function updateMenuTreePermission(data: UpdateMenuTreePermissionPayload) {
  return request.post<void, void>(`${BASE}/update/menu/treePermission`, data);
}