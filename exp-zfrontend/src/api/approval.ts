import request from '@/api/request';

export type ApprovalType = 'bidding' | 'tender' | 'contract' | 'project';
export type ApprovalStatus = 'pending' | 'approved' | 'rejected' | 'draft';
export type Priority = 'urgent' | 'important' | 'normal';

export interface ApprovalTask {
  id: string;
  type: ApprovalType;
  title: string;
  description: string;
  applicant: string;
  applicantId: string;
  submitTime: string;
  status: ApprovalStatus;
  priority: Priority;
  businessId: string; // 关联的业务ID
  businessType: string; // 业务类型
  attachments?: string[];
  comments?: string;
  approveTime?: string;
  approver?: string;
  approverId?: string;
}

export interface ApprovalStats {
  todayPending: number;
  urgentPending: number;
  completedToday: number;
  efficiency: number; // 审批效率百分比
}

export interface ApprovalListQuery {
  page: number;
  pageSize: number;
  keyword?: string;
  type?: ApprovalType;
  status?: ApprovalStatus;
  priority?: Priority;
  startDate?: string;
  endDate?: string;
  applicant?: string;
}

export interface ApprovalDetail extends ApprovalTask {
  businessData?: any; // 具体的业务数据
  approvalHistory: ApprovalHistory[];
  attachments: Attachment[];
}

export interface ApprovalHistory {
  id: string;
  taskId: string;
  action: 'submit' | 'approve' | 'reject' | 'delegate';
  operator: string;
  operatorId: string;
  operateTime: string;
  comments?: string;
  status: ApprovalStatus;
}

export interface Attachment {
  id: string;
  name: string;
  url: string;
  size: number;
  uploadTime: string;
}

export interface ApprovalAction {
  taskId: string;
  action: 'approve' | 'reject' | 'delegate';
  comments?: string;
  delegateTo?: string; // 委托给谁
}

export interface PageResult<T> {
  records?: T[];
  total?: number;
  page?: number;
  pageSize?: number;
  list?: T[];
  rows?: T[];
}

const BASE = '/exp/approval';

// 获取审批统计信息
export function getApprovalStats() {
  return request.get<ApprovalStats, ApprovalStats>(`${BASE}/stats`);
}

// 获取审批任务列表
export function listApprovalTasks(params: ApprovalListQuery) {
  return request.get<PageResult<ApprovalTask>, PageResult<ApprovalTask>>(`${BASE}/tasks`, { params });
}

// 获取审批任务详情
export function getApprovalDetail(taskId: string) {
  return request.get<ApprovalDetail, ApprovalDetail>(`${BASE}/detail`, { params: { taskId } });
}

// 审批操作
export function approveTask(data: ApprovalAction) {
  return request.post<void, void>(`${BASE}/approve`, data);
}

// 驳回审批
export function rejectTask(data: ApprovalAction) {
  return request.post<void, void>(`${BASE}/reject`, data);
}

// 委托审批
export function delegateTask(data: ApprovalAction) {
  return request.post<void, void>(`${BASE}/delegate`, data);
}

// 批量审批
export function batchApprove(data: { taskIds: string[]; comments?: string }) {
  return request.post<void, void>(`${BASE}/batch-approve`, data);
}

// 批量驳回
export function batchReject(data: { taskIds: string[]; comments?: string }) {
  return request.post<void, void>(`${BASE}/batch-reject`, data);
}

// 获取审批历史
export function getApprovalHistory(taskId: string) {
  return request.get<ApprovalHistory[], ApprovalHistory[]>(`${BASE}/history`, { params: { taskId } });
}
