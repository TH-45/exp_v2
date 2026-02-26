import request from '@/api/request';
import {buildPageQuery, type PageQueryInput} from '@/api/common';

export type BidStatus = 'DRAFT' | 'SUBMITTED' | 'WITHDRAWN' | 'REJECTED' | 'VALID';

export interface BidVO {
  bidId: string;
  projectId: string;
  projectCode?: string;
  projectName?: string;
  bidderName: string;
  amount?: number; // 万
  status: BidStatus;
  createdTime?: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  size: number;
}

export interface QueryBidParams {
  // pageNum: number;
  // size: number;
  projectId?: string;
  keyword?: string; // 项目/投标人
  bidderName?: string;
  status?: BidStatus;
  sort?: string;
}

export function queryBidList(data: PageQueryInput<QueryBidParams>) {
  return request.post<PageResult<BidVO>, PageResult<BidVO>>('/exp/bid/bidding/list',
     buildPageQuery<QueryBidParams>(data),
  );
}


