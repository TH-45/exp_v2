import request from '@/api/request';

export type ContractStatus = 'DRAFT' | 'PENDING' | 'EFFECTIVE' | 'CHANGED' | 'TERMINATED' | 'ARCHIVED';

export interface ContractVO {
  contractId: string;
  contractCode: string;
  contractName: string;
  projectId?: string;
  projectName?: string;
  supplierName?: string;
  amount?: number;
  status: ContractStatus;
  signDate?: string;
  startDate?: string;
  endDate?: string;
  createdTime?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface QueryContractParams {
  page: number;
  pageSize: number;
  keyword?: string; // 合同编码/名称/供应商/项目
  status?: ContractStatus;
  projectId?: string;
  supplierName?: string;
}

// docs/public/接口约定.md：GET /exp/contracts/contract/list
export function queryContractList(params: QueryContractParams) {
  return request.get<PageResult<ContractVO>, PageResult<ContractVO>>('/exp/contracts/contract/list', { params });
}


