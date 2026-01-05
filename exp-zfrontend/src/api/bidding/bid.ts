import request from '@/api/request';

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
  records: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface QueryBidParams {
  page: number;
  pageSize: number;
  projectId?: string;
  keyword?: string; // 项目/投标人
  bidderName?: string;
  status?: BidStatus;
}

// 约定路径（与 docs/public/接口约定.md 的示例保持一致）
export function queryBidList(params: QueryBidParams) {
  return request.get<PageResult<BidVO>, PageResult<BidVO>>('/exp/bidding/bid/list', { params });
}


