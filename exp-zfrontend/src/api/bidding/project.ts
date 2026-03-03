import request from '@/api/request';
import {buildPageQuery, type PageQueryInput} from '@/api/common';

export type BiddingProjectStatus =
  | '未开始'
  | '进行中'
  | '已结束'
  | string;

/**
 * 招标列表响应对象
 * 对齐后端 TenderListRes 实体类
 */
export interface TenderVO {
  // 招标项目id (Long型建议用string接收，避免精度丢失)
  tenderId: string;

  // 招标项目编号
  tenderCode: string;

  // 招标项目名称
  tenderName: string;

  // 招标方id
  purchaserId?: string;

  // 招标方名称 (原 purchaserName)
  purchaserName?: string;

  // 招标负责人id
  personId?: string;

  // 招标负责人名称 (即负责人)
  personIdName?: string;

  // 业务员id
  salesmanId?: string;

  //业务员
  salesmanName?: String;

  // 组织id
  orgId?: string;

  // 组织名称
  orgName?: string;

  // 招标项目状态
  status: BiddingProjectStatus;

  // 招标方式
  tenderMode?: string;

  // 招标类型
  tenderType?: string;

  // 招标预算金额 (BigDecimal 对应 number)
  budgetAmount?: number;

  // 币种
  currency?: string;

  // 关联项目 id
  projectId?: string;

  // 关联项目名称
  projectName?: string;

  // 投标开始时间
  bidStartTime?: string;

  // 招标截止时间
  bidEndTime?: string;

  /** 开标时间 */
  openTime?: string;

  // 招标项目创建人id
  createdBy?: string;

  // 招标项目创建人名称
  createdByName?: string;

  // 招标项目创建时间
  createdTime?: string;

  // 开标地址
  openAddress?: string;

  /** 招标项目概要/公告摘要（详情接口返回） */
  tenderBrief?: string;

  /** 更新时间（详情接口返回） */
  updatedTime?: string;

  remark?: string;
}


export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}

export interface QueryTenderParams {
  tenderCode?: string;
  tenderName?: string;
  purchaserName?: string;
  tenderType?: string;
  tenderMode?: string;
  status?: BiddingProjectStatus;
  year?: number;
  sort?: string;
}

/**
 * 基地址
 */
const  baseUrl = '/exp/bid/tender';

/**
 * 查询招标列表
 * @param data
 */
export function queryBiddingProjectList(data: PageQueryInput<QueryTenderParams>) {
  return request.post<PageResult<TenderVO>, PageResult<TenderVO>>(
      baseUrl + '/list',
      buildPageQuery<QueryTenderParams>(data),
  );
}

/**
 * 获取招标详情（与 openapi /tender/detail 一致，GET query tenderId）
 */
export function getBiddingProjectDetail(tenderId: number | string) {
  return request.get<TenderVO, TenderVO>(baseUrl + '/detail', {
    params: { tenderId: Number(tenderId) },
  });
}

/** 创建招标请求体，与 openapi CreateTenderReq 一致 */
export interface CreateTenderReq {
  tenderCode: string;
  tenderName: string;
  tenderType: string;
  tenderMode: string;
  companyId: number;
  budgetAmount: number;
  currency: string;
  tenderBrief?: string;
  publishTime?: string;
  bidStartTime: string;
  bidEndTime: string;
  openTime?: string;
  openAddress?: string;
  projectId?: number;
  remark?: string;
}

/** 更新招标请求体，与 openapi UpdateTenderReq 一致 */
export interface UpdateTenderReq extends CreateTenderReq {
  tenderId: number;
}

/**
 * 创建招标（POST /tender/create）
 */
export function createBiddingProject(data: CreateTenderReq) {
  return request.post<TenderVO, TenderVO>(baseUrl + '/create', data);
}

/**
 * 更新招标（POST /tender/update）
 */
export function updateBiddingProject(data: UpdateTenderReq) {
  return request.post<TenderVO, TenderVO>(baseUrl + '/update', data);
}


export interface DeleteTenderReq {
  tenderId: number;
}
/**
 * 删除招标项目
 */
export function deleteBiddingProject(tenderId: number) {
  const deleteTenderReq: DeleteTenderReq = { tenderId };
  return request.post<void, void>(baseUrl+`/delete`, deleteTenderReq);
}


/**
 *更改招标项目状态
 */
export function updateBiddingProjectStatus(tenderId: string, status: BiddingProjectStatus) {
  return request.post<void, void>(baseUrl+`/update/status`, { tenderId, status });
}

