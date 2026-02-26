import request from '@/api/request';
import {buildPageQuery, type PageQueryInput, type PageResult} from '@/api/common';

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
  status: PostStatus;
  defaultRoleId?: number;
  defaultRoleName?: string;
  defaultDataScope?: string;
  isSystem?: number;
  isOutsourcing?: number;
  sortNo?: number;
  postDesc?: string;
  remark?: string;
  createdTime?: string;
  orgId?: number;
  orgName?: string;
  orgCode?: string;
}

export interface OrgPostQuery {
  orgId: number;
  postCode?: string;
  postName?: string;
  relStatus?: 'ALL' | RelStatus;
  status?: PostStatus;
  pageNum: number;
  pageSize: number;
}

export interface OutsourcePostReq {
  postId: number;
  currentOrgId: number;
  targetOrgId: number;
  status: PostStatus;
  isOutsourcing: number;
  remark?: string;
}



const BASE_POST = '/exp/auth/position';
const BASE_ORG_POST = '/exp/auth/orgunit';

// 组织树
export function fetchOrgTree(params?: { orgCode?: string; orgName?: string }) {
  return request.get<OrgNode[], OrgNode[]>(`${BASE_ORG_POST}/tree`, { params });
}

// 查询某组织的岗位列表（关联信息）
export function queryOrgPosts(
    input: PageQueryInput<{
      orgId: number;
      includeChildren?: boolean;
      status?: PostStatus;
      postCode: String;
      postName: String;

    }>
) {
  const params = buildPageQuery(input, {
    pageNum: 1,
    pageSize: 999, // 下拉框一般查全部
  });
  return request.post<PageResult<PostVO>, PageResult<PostVO>>(
      `${BASE_POST}/queryByOrg`,
      params,
  );
}


// 新增岗位字典
export function createPost(data: Partial<PostVO>) {
  return request.post<PostVO, PostVO>(`${BASE_POST}/create`, data);
}

// 编辑岗位字典
export function updatePost(data: Partial<PostVO>) {
  return request.post<PostVO, PostVO>(`${BASE_POST}/update`, data);
}

// 批量启用/停用岗位字典
export function changePostStatus(postIds: number[], status: PostStatus) {
  return request.post<null, null>(`${BASE_POST}/batchStatus`, { postIds, status });
}

// 外派岗位
export function outsourcePost(data: OutsourcePostReq) {
  return request.post<string, string>(`${BASE_POST}/outsource`, data);
}


// 获取岗位字典列表（供关联弹窗选择）
export function queryPostDict(params: { keyword?: string; pageNum: number; pageSize: number }) {
  const req = {
    pageNum: params.pageNum,
    pageSize: params.pageSize,
    queryParam: {
      postCode: params.keyword,
      postName: params.keyword,
      status: undefined as PostStatus | undefined
    }
  };
  return request.post<{ success: boolean; data: { total: number; page: number; size: number; list: PostVO[] } }, { success: boolean; data: { total: number; page: number; size: number; list: PostVO[] } }>(
    `${BASE_POST}/list`,
    req,
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
  return request.post<OrgNode, OrgNode>(`${BASE_ORG_POST}/create`, data);
}

// 删除组织
export function deleteOrg(orgId: number) {
  return request.post<null, null>(`${BASE_ORG_POST}/delete`, { orgId });
}


