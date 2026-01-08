import request from '@/api/request';

export interface OrgNode {
  orgId: number;
  orgName: string;
  orgCode?: string;
  children?: OrgNode[];
}

export type PostStatus = 'ENABLED' | 'DISABLED';
export type RelStatus = 'ENABLED' | 'DISABLED';

export interface PostVO {
  postId: number;
  postCode: string;
  postName: string;
  postType?: string;
  postLevel?: string;
  postCategory?: string;
  postStatus: PostStatus;
  defaultRoleId?: number;
  defaultRoleName?: string;
  defaultDataScope?: string;
  isSystem?: number;
  sortNo?: number;
  postDesc?: string;
  remark?: string;

  // 组织关联维度
  relStatus?: RelStatus;
  relSortNo?: number;
  isPrimary?: number;

  createdTime?: string;
}

export interface OrgPostQuery {
  orgId: number;
  postCode?: string;
  postName?: string;
  relStatus?: 'ALL' | RelStatus;
  postStatus?: PostStatus;
  pageNum: number;
  pageSize: number;
}

export interface PageResult<T> {
  list?: T[];
  rows?: T[];
  records?: T[];
  total?: number;
}

const BASE_POST = '/exp/post';
const BASE_ORG_POST = '/exp/orgPost';

// 组织树
export function fetchOrgTree(params?: { keyword?: string }) {
  return request.get<OrgNode[], OrgNode[]>(`${BASE_POST}/orgTree`, { params });
}

// 查询某组织的岗位列表（关联信息）
export function queryOrgPosts(params: OrgPostQuery) {
  return request.post<PageResult<PostVO>, PageResult<PostVO>>(
    `${BASE_ORG_POST}/query`,
    params,
  );
}

// 新增岗位字典
export function createPost(data: Partial<PostVO>) {
  return request.post<void, void>(`${BASE_POST}/create`, data);
}

// 编辑岗位字典
export function updatePost(data: Partial<PostVO>) {
  return request.post<void, void>(`${BASE_POST}/update`, data);
}

// 批量启用/停用岗位字典
export function changePostStatus(postIds: number[], status: PostStatus) {
  return request.post<void, void>(`${BASE_POST}/status`, { postIds, status });
}

// 关联岗位到组织
export function bindPostsToOrg(orgId: number, postIds: number[]) {
  return request.post<void, void>(`${BASE_ORG_POST}/bind`, { orgId, postIds });
}

// 组织维度启用/停用
export function changeOrgPostStatus(orgId: number, postIds: number[], relStatus: RelStatus) {
  return request.post<void, void>(`${BASE_ORG_POST}/status`, {
    orgId,
    postIds,
    relStatus,
  });
}

// 设置主岗位（组织内只能有 1 个）
export function setOrgPrimaryPost(orgId: number, postId: number) {
  return request.post<void, void>(`${BASE_ORG_POST}/primary`, { orgId, postId });
}

// 解除关联
export function unbindOrgPosts(orgId: number, postIds: number[]) {
  return request.post<void, void>(`${BASE_ORG_POST}/unbind`, { orgId, postIds });
}

// 获取岗位字典列表（供关联弹窗选择）
export function queryPostDict(params: { keyword?: string; pageNum: number; pageSize: number }) {
  return request.post<PageResult<PostVO>, PageResult<PostVO>>(
    `${BASE_POST}/list`,
    params,
  );
}

// 组织管理相关接口
export interface CreateOrgPayload {
  orgName: string;
  orgCode: string;
  parentOrgId?: number;
  orgType: string;
  managerPersonId?: number;
}

// 创建组织
export function createOrg(data: CreateOrgPayload) {
  return request.post<OrgNode, OrgNode>(`${BASE_POST}/createOrg`, data);
}

// 删除组织
export function deleteOrg(orgId: number) {
  return request.post<void, void>(`${BASE_POST}/deleteOrg`, { orgId });
}


