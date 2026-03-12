import request from '@/api/request';
import { buildPageQuery, type PageQueryInput } from '@/api/common';

/** 合同状态（与流程：起草→审核→拟签→归档） */
export type ContractStatus =
  | 'DRAFT'
  | 'UNDER_REVIEW'
  | 'PENDING_SIGN'
  | 'EFFECTIVE'
  | 'ARCHIVED'
  | 'ARCHIVED_ABNORMAL'
  | 'CHANGED'
  | 'TERMINATED'
  | 'REJECTED';

export interface ContractVO {
  contractId: string;
  contractCode: string;
  contractName: string;
  projectId?: string;
  projectName?: string;
  purchaserId?: string;
  purchaserName?: string;
  supplierId?: string;
  supplierName?: string;
  amountTotal?: number;
  amount?: number; // 兼容旧字段，合同金额(万)
  status: ContractStatus;
  signDate?: string;
  effectiveDate?: string;
  endDate?: string;
  startDate?: string; // 兼容
  createdTime?: string;
}

export interface PageResult<T> {
  list?: T[];
  records?: T[];
  total: number;
  page?: number;
  size?: number;
}

export interface QueryContractParams {
  pageNum?: number;
  pageSize?: number;
  contractCode?: string;
  contractName?: string;
  projectId?: number;
  projectName?: string;
  amountMin?: number;
  amountMax?: number;
  signDateStart?: string;
  signDateEnd?: string;
  effectiveDateStart?: string;
  effectiveDateEnd?: string;
  status?: ContractStatus;
  supplierId?: number;
  supplierName?: string;
  /** 合作方类型：1-甲方，2-供应商，字典 Partner_Type */
  partnerType?: string;
  /** 合作方名称（模糊） */
  partnerName?: string;
}

export interface CreateContractReq {
  contractCode: string;
  contractName: string;
  contractType?: string;
  contractCategory?: string;
  tenderId?: number;
  bidId?: number;
  projectId?: number;
  purchaserId?: number;
  supplierId: number;
  signDate?: string;
  effectiveDate?: string;
  endDate?: string;
  amountTotal: number;
  amountWithoutTax?: number;
  taxRateDefault?: number;
  currency?: string;
  payTerms?: string;
  settleMode?: string;
  remark?: string;
  /** 业务员人员ID，创建时通过人员选择器选择 */
  salesmanPersonId?: number;
}

export interface UpdateContractReq {
  contractId: number;
  contractCode?: string;
  contractName?: string;
  contractType?: string;
  contractCategory?: string;
  tenderId?: number;
  bidId?: number;
  projectId?: number;
  purchaserId?: number;
  supplierId?: number;
  signDate?: string;
  effectiveDate?: string;
  endDate?: string;
  amountTotal?: number;
  amountWithoutTax?: number;
  taxRateDefault?: number;
  currency?: string;
  payTerms?: string;
  settleMode?: string;
  remark?: string;
  salesmanPersonId?: number;
}

const BASE = '/exp/bid/contract';

/** 分页查询合同列表 */
export function queryContractList(params: PageQueryInput<QueryContractParams>) {
  const req = buildPageQuery<QueryContractParams>(params, { pageNum: 1, pageSize: 10 });
  return request.post<PageResult<ContractVO>, PageResult<ContractVO>>(`${BASE}/list`, req);
}

/** 查询合同详情 */
export function getContractDetail(contractId: number | string) {
  return request.get<ContractVO, ContractVO>(`${BASE}/detail`, { params: { contractId } });
}

/** 创建合同 */
export function createContract(data: CreateContractReq) {
  return request.post<ContractVO, ContractVO>(`${BASE}/create`, data);
}

/** 更新合同 */
export function updateContract(data: UpdateContractReq) {
  return request.post<ContractVO, ContractVO>(`${BASE}/update`, data);
}

/** 删除合同 */
export function deleteContract(contractId: number | string) {
  return request.post<void, void>(`${BASE}/delete`, null, { params: { contractId } });
}

/** 提交审批（内部调用流程创建，保留兼容） */
export function submitContractForApproval(data: { contractId: number; procDefId?: number; procCode?: string }) {
  return request.post<number, number>(`${BASE}/submitForApproval`, data);
}

/** 流程创建成功后，将合同状态更新为审核中 */
export function updateContractStatusAfterProcessStart(contractId: number) {
  return request.post<void, void>(`${BASE}/updateStatusAfterProcessStart`, null, { params: { contractId } });
}

/** 合同签订/不签订（统一接口） */
export interface SignContractReq {
  contractId: number;
  action: 'SIGN' | 'UNSIGN';
  opinion?: string;
  needChange?: boolean;
}

export function signContract(data: SignContractReq) {
  return request.post<void, void>(`${BASE}/sign`, data);
}
