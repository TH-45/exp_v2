import request from '@/api/request';
import {buildPageQuery, type PageQueryInput} from '@/api/common';

export type BidStatus =
  | 'PREPARE'
  | 'SUBMITTED'
  | 'EVALUATING'
  | 'WON'
  | 'LOST'
  | 'ABANDONED';

export interface BidListVO {
  bidId: string;
  tenderId?: string;
  tenderName?: string;
  supplierId?: string;
  supplierName?: string;
  bidCode?: string;
  bidName?: string;
  bidTotalAmount?: number;
  currency?: string;
  bidSubmitTime?: string;
  bidStatus: BidStatus | string;
  winFlag?: number;
  winNoticeNo?: string;
  projectId?: string;
  projectName?: string;
  createdBy?: string;
  createdByName?: string;
  managerPersonId?: string;
  managerPersonName?: string;
  orgId?: string;
  orgIdName?: string;
  salesmanId?: string;
  salesmanName?: string;
  createdTime?: string;
}

export interface BidDetailVO extends BidListVO {
  contractId?: string;
  createdDeptId?: string;
  createdDeptName?: string;
  createdPostId?: string;
  createdPostName?: string;
  updatedTime?: string;
  remark?: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}

export interface QueryBidParams {
  purchaserName?: string;
  tenderName?: string;
  projectName?: string;
  bidCode?: string;
  bidName?: string;
  bidStatus?: BidStatus | string;
  winFlag?: number;
  bidSubmitTimeStart?: string;
  bidSubmitTimeEnd?: string;
  createdBy?: number;
  createdTimeStart?: string;
  createdTimeEnd?: string;
  sort?: string;
}

export interface CreateBidReq {
  tenderId: number;
  supplierId: number;
  bidCode: string;
  bidName: string;
  bidTotalAmount: number;
  currency: string;
  principalId?: number;
  salesmanId?: number;
  orgId: number;
  bidSubmitTime?: string;
  projectId?: number;
  remark?: string;
}

export interface UpdateBidReq {
  bidId: number;
  bidCode: string;
  bidName: string;
  bidTotalAmount: number;
  principalId?: number;
  salesmanId?: number;
  orgId: number;
  remark?: string;
}

export interface DeleteBidReq {
  bidId: number;
}

export interface BidStatusReq {
  bidId: number;
  bidStatus: BidStatus | string;
}

export interface CheckBidCodeParams {
  bidCode: string;
  excludeBidId?: number;
}

export interface CheckSupplierBidParams {
  tenderId: number;
  supplierId: number;
  excludeBidId?: number;
}

export function queryBidList(data: PageQueryInput<QueryBidParams>) {
  return request.post<PageResult<BidListVO>, PageResult<BidListVO>>('/exp/bid/bidding/list',
     buildPageQuery<QueryBidParams>(data),
  );
}

export function getBidDetail(bidId: number | string) {
  return request.get<BidDetailVO, BidDetailVO>('/exp/bid/bidding/detail', {
    params: { bidId: Number(bidId) },
  });
}

export function createBid(data: CreateBidReq) {
  return request.post<BidDetailVO, BidDetailVO>('/exp/bid/bidding/create', data);
}

export function updateBid(data: UpdateBidReq) {
  return request.post<BidDetailVO, BidDetailVO>('/exp/bid/bidding/update', data);
}

export function deleteBid(data: DeleteBidReq) {
  return request.post<void, void>('/exp/bid/bidding/delete', data);
}

export function updateBidStatus(data: BidStatusReq) {
  return request.post<BidDetailVO, BidDetailVO>('/exp/bid/bidding/status', data);
}

export function checkBidCode(params: CheckBidCodeParams) {
  return request.get<boolean, boolean>('/exp/bid/bidding/checkBidCode', { params });
}

export function checkSupplierBid(params: CheckSupplierBidParams) {
  return request.get<boolean, boolean>('/exp/bid/bidding/checkSupplierBid', { params });
}

