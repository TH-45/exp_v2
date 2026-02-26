import request from '@/api/request';
import {buildPageQuery, type PageQueryInput} from '@/api/common';

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
  // pageNum: number;
  // size: number;
  projectCode?: string;
  projectName?: string;
  tenderOrg?: string;
  status?: BiddingProjectStatus;
  year?: number;
  sort?: string;
}

export function queryBiddingProjectList(data: PageQueryInput<QueryBiddingProjectParams>) {
  return request.post<PageResult<BiddingProjectVO>, PageResult<BiddingProjectVO>>(
    '/exp/bid/tender/list',
      buildPageQuery<QueryBiddingProjectParams>(data),
  );
}


