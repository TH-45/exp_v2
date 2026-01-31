import request from '@/api/request';

export type BiddingProjectStatus =
  | 'DRAFT'
  | 'PUBLISHED'
  | 'BIDDING'
  | 'EVALUATING'
  | 'AWARDED'
  | 'ARCHIVED';

export interface BiddingProjectVO {
  projectId: string;
  projectCode: string;
  projectName: string;
  tenderOrg?: string;
  ownerName?: string;
  status: BiddingProjectStatus;
  createdTime?: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  size: number;
}

export interface QueryBiddingProjectParams {
  pageNum: number;
  size: number;
  projectCode?: string;
  projectName?: string;
  tenderOrg?: string;
  status?: BiddingProjectStatus;
  year?: number;
  sort?: string;
}

// 约定路径（与 docs/public/接口约定.md 的示例保持一致）
export function queryBiddingProjectList(params: QueryBiddingProjectParams) {
  return request.get<PageResult<BiddingProjectVO>, PageResult<BiddingProjectVO>>(
    '/exp/bidding/project/list',
    { params },
  );
}


