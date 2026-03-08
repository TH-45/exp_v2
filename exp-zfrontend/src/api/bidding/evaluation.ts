import request from '@/api/request';

export interface EvaluationCommitteeVO {
  committeeId: number;
  tenderId?: number;
  committeeCode?: string;
  committeeName?: string;
  status?: string;
}

export interface CreateEvaluationResultReq {
  committeeId: number;
  bidId: number;
  technicalScore?: number;
  businessScore?: number;
  comprehensiveScore?: number;
  finalScore?: number;
  ranking?: number;
  isRecommended?: number;
  evaluationConclusion?: string;
  evaluationOpinion?: string;
  resultStatus?: string;
  remark?: string;
}

export interface EvaluationResultVO {
  resultId: number;
  committeeId: number;
  bidId: number;
  finalScore?: number;
  ranking?: number;
  isRecommended?: number;
  evaluationOpinion?: string;
  resultStatus?: string;
}

export function getCommitteesByTender(tenderId: number | string) {
  return request.get<EvaluationCommitteeVO[], EvaluationCommitteeVO[]>('/exp/bid/evaluation-committee/byTender', {
    params: { tenderId: Number(tenderId) },
  });
}

export function getEvaluationResultByBid(bidId: number | string) {
  return request.get<EvaluationResultVO, EvaluationResultVO>('/exp/bid/evaluation-result/byBid', {
    params: { bidId: Number(bidId) },
  });
}

export function generateEvaluationResult(data: CreateEvaluationResultReq) {
  return request.post<EvaluationResultVO, EvaluationResultVO>('/exp/bid/evaluation-result/generate', data);
}

export function updateEvaluationResult(resultId: number, data: CreateEvaluationResultReq) {
  return request.post<EvaluationResultVO, EvaluationResultVO>('/exp/bid/evaluation-result/update', data, {
    params: { resultId },
  });
}

