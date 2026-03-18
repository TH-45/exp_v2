/**
 * 企业/公司（company）相关接口
 * 基于 openapi 规范，与 exp-corp 服务 /company 保持一致
 */
import request from '@/api/request';
import {
  buildPageQuery,
  type PageQueryInput,
  type PageResult,
} from '@/api/common';

interface ApiCallOptions {
  skipErrorToast?: boolean;
}

// ============ 类型定义（与 openapi / 后端实体对应） ============

/** 企业列表项（CompanyListRes） */
export interface CompanyListVO {
  companyId?: number;
  companyCode?: string;
  companyName?: string;
  companyShortName?: string;
  companyType?: string;
  unifiedSocialCreditCode?: string;
  legalPerson?: string;
  contactPhone?: string;
  status?: string;
  updatedTime?: string;
}

/** 企业详情（CompanyDetailRes） */
export interface CompanyDetailVO {
  companyId?: number;
  companyCode?: string;
  companyName?: string;
  companyShortName?: string;
  companyType?: string;
  unifiedSocialCreditCode?: string;
  taxNo?: string;
  legalPerson?: string;
  regAddress?: string;
  officeAddress?: string;
  contactPhone?: string;
  contactEmail?: string;
  website?: string;
  status?: string;
  createdBy?: number;
  createdTime?: string;
  updatedTime?: string;
  remark?: string;
}

/** 列表查询条件（QueryCompanyReq） */
export interface QueryCompanyParam {
  companyCode?: string;
  companyName?: string;
  companyType?: string;
  status?: string;
}

/** 创建企业请求：companyName 必填 */
export interface CreateCompanyReq {
  companyName: string;
  companyCode?: string;
  companyShortName?: string;
  companyType?: string;
  unifiedSocialCreditCode?: string;
  taxNo?: string;
  legalPerson?: string;
  regAddress?: string;
  officeAddress?: string;
  contactPhone?: string;
  contactEmail?: string;
  website?: string;
  status?: string;
  createdBy?: number;
  remark?: string;
}

/** 更新企业请求：companyId 必填 */
export interface UpdateCompanyReq {
  companyId: number;
  companyCode?: string;
  companyName?: string;
  companyShortName?: string;
  companyType?: string;
  unifiedSocialCreditCode?: string;
  taxNo?: string;
  legalPerson?: string;
  regAddress?: string;
  officeAddress?: string;
  contactPhone?: string;
  contactEmail?: string;
  website?: string;
  status?: string;
  remark?: string;
}

/**
 * 公司选择器对外值类型（尽量不含编号，便于其他页面回显与提交）
 * 其他页面 v-model 绑定此类型即可，回显仅需 companyId + companyName
 */
export interface CompanySelectorValue {
  companyId: number;
  companyName: string;
  contactPhone?: string;
}

// ============ 接口路径 ============
/** 网关将 /exp/corp/** 路由至 exp-corp 服务 */
const BASE = '/exp/corp/company';

// ============ API 方法 ============

/**
 * 分页查询企业列表
 * @param input 分页及筛选条件，可选 sort 排序字段
 * @returns 分页结果 total/page/size/list
 */
export function listCompany(
  input: PageQueryInput<QueryCompanyParam> & { sort?: string },
  options?: ApiCallOptions,
) {
  const { sort, ...pageInput } = input;
  const body = {
    ...buildPageQuery(pageInput as PageQueryInput<QueryCompanyParam>, {
      pageNum: 1,
      pageSize: 10,
    }),
    ...(sort != null && { sort }),
  };
  return request.post<PageResult<CompanyListVO>, PageResult<CompanyListVO>>(
    `${BASE}/list`,
    body,
    options,
  );
}

/**
 * 根据 companyId 获取企业详情
 * @param companyId 企业主键
 */
export function getCompanyDetail(companyId: number, options?: ApiCallOptions) {
  return request.get<CompanyDetailVO, CompanyDetailVO>(`${BASE}/detail`, {
    params: { companyId },
    ...options,
  });
}

/**
 * 批量获取企业详情
 * @param companyIds 企业主键数组
 * @returns 以 companyId 为 key 的详情映射，便于按 id 直接取值 result[companyId]
 */
export function batchGetCompanyDetail(companyIds: number[]) {
  return request.post<
    Record<string, CompanyDetailVO>,
    Record<string, CompanyDetailVO>
  >(`${BASE}/batchDetail`, companyIds);
}

/**
 * 创建企业
 * @param data 创建数据，需包含 companyName
 */
export function createCompany(data: CreateCompanyReq, options?: ApiCallOptions) {
  return request.post<CompanyDetailVO, CompanyDetailVO>(`${BASE}/create`, data, options);
}

/**
 * 更新企业
 * @param data 更新数据，需包含 companyId
 */
export function updateCompany(data: UpdateCompanyReq, options?: ApiCallOptions) {
  return request.post<CompanyDetailVO, CompanyDetailVO>(`${BASE}/update`, data, options);
}

/**
 * 删除单个企业
 * @param companyId 企业主键
 */
export function deleteCompany(companyId: number, options?: ApiCallOptions) {
  return request.post<void, void>(`${BASE}/delete`, { companyId }, options);
}

/**
 * 批量删除企业
 * @param companyIds 企业主键数组
 */
export function batchDeleteCompany(companyIds: number[]) {
  return request.post<void, void>(`${BASE}/batchDelete`, { companyIds });
}
