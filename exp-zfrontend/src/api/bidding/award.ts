import request from '@/api/request';

export interface AwardResultVO {
  awardId: number;
  tenderId: number;
  winningBidId: number;
  awardStatus?: string;
  awardOpinion?: string;
  needRetender?: number;
  retenderReason?: string;
}

export interface CreateAwardResultReq {
  tenderId: number;
  winningBidId: number;
  awardStatus?: string;
  awardOpinion?: string;
  needRetender?: number;
  retenderReason?: string;
}

export interface AwardProcessDecisionReq {
  awardId: number;
  action: 'APPROVE' | 'REJECT';
  rejectReasonCode?: string;
  opinion?: string;
}

const baseUrl = '/exp/bid/award-result';

export function getAwardResultByTender(tenderId: number | string) {
  return request.get<AwardResultVO, AwardResultVO>(baseUrl + '/byTender', {
    params: { tenderId: Number(tenderId) },
  });
}

export function createAwardResult(data: CreateAwardResultReq) {
  return request.post<AwardResultVO, AwardResultVO>(baseUrl + '/create', data);
}

export function processAwardDecision(data: AwardProcessDecisionReq) {
  return request.post<AwardResultVO, AwardResultVO>(baseUrl + '/processDecision', data);
}

