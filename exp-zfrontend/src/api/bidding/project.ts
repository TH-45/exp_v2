import request from '@/api/request';
import {buildPageQuery, type PageQueryInput} from '@/api/common';

export type BiddingProjectStatus =
  | '未开始'
  | '进行中'
  | '已结束'
  | string;

export interface TenderVO {
  tenderId: string;
  tenderCode: string;
  tenderName: string;
  purchaserName?: string;     // 招标单位（后端真实字段）
  createdByName?: string;     // 负责人
  tenderType?: string;
  tenderMode?: string;
  status: BiddingProjectStatus;
  budgetAmount?: number;
  bidStartTime?: string;
  bidEndTime?: string;
  openTime?: string;
  openAddress?: string;
  createdTime?: string;
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

export function queryBiddingProjectList(data: PageQueryInput<QueryTenderParams>) {
  return request.post<PageResult<TenderVO>, PageResult<TenderVO>>(
      '/exp/bid/tender/list',
      buildPageQuery<QueryTenderParams>(data),
  );
}




