import request from '@/api/request';
import { buildPageQuery, type PageQueryInput } from '@/api/common';

export type WorkbenchTab = 'todo' | 'done' | 'started' | 'closed';
export type InstanceStatus = 'RUNNING' | 'COMPLETED' | 'REJECTED' | 'CLOSED' | string;

export interface ApprovalStats {
  todoCount: number;
  doneCount: number;
  startedCount: number;
  closedCount: number;
}

export interface ApprovalTaskQuery {
  tab: WorkbenchTab;
  pageNum: number;
  pageSize: number;
  keyword?: string;
  busType?: string;
  status?: InstanceStatus;
}

export interface ApprovalTask {
  taskId?: number;
  instanceId: number;
  title: string;
  busType: string;
  busId: number | string;
  starterId: number;
  startTime: string;
  currentNode: string;
  status: InstanceStatus;
  isDone: number;
}

export interface ApprovalDetail {
  taskId: number;
  instanceId: number;
  busType: string;
  busId: string;
  status: InstanceStatus;
  currentNode: string;
  starterId: number;
  businessData?: unknown;
  approvalHistory: ApprovalHistory[];
  attachments: Attachment[];
}

export interface ApprovalHistory {
  taskId: number;
  nodeId: number;
  nodeName: string;
  action: string;
  handlerId: number;
  opinion?: string;
  isDone: number;
  createTime: string;
  finishTime?: string;
}

export interface Attachment {
  id: number;
  name: string;
  url: string;
  size: number;
  uploadTime?: string;
}

export interface ApprovalAction {
  taskId: number;
  /** 审批动作：AGREE-同意，REJECT-拒绝/驳回 */
  action?: string;
  comments?: string;
  attachments?: Array<{
    name: string;
    url: string;
    size?: number;
  }>;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}

const BASE = '/exp/process/approval';

/** 流程创建请求（统一流程创建接口，与后端 StartProcessReq 对齐） */
export interface ProcessStartReq {
  procCode: string;
  busId: number;
  busType?: string;
  title?: string;
}

/** 发起流程（提交审批时调用，后端路径 /approval/create） */
export function startProcess(data: ProcessStartReq) {
  return request.post<number, number>(`${BASE}/create`, data);
}

/** 获取审批统计信息（待办/已办/我发起/已关闭数量） */
export function getApprovalStats() {
  return request.get<ApprovalStats, ApprovalStats>(`${BASE}/stats`);
}

// 获取审批任务列表
export function listApprovalTasks(params: PageQueryInput<ApprovalTaskQuery>) {
  return request.post<PageResult<ApprovalTask>, PageResult<ApprovalTask>>(`${BASE}/tasks`, buildPageQuery(params));
}

// 获取审批任务详情
export function getApprovalDetail(taskId: number) {
  return request.get<ApprovalDetail, ApprovalDetail>(`${BASE}/detail`, { params: { taskId } });
}

/** 审批操作（同意：action=AGREE，驳回：action=REJECT，统一走 /approve） */
export function approveTask(data: ApprovalAction) {
  return request.post<void, void>(`${BASE}/approve`, {
    ...data,
    action: data.action || 'AGREE',
  });
}

/** 驳回审批（统一走 /approve，action=REJECT） */
export function rejectTask(data: ApprovalAction) {
  return request.post<void, void>(`${BASE}/approve`, {
    ...data,
    action: 'REJECT',
  });
}

// 批量审批
export function batchApprove(data: { taskIds: number[]; comments?: string }) {
  return request.post<void, void>(`${BASE}/batch-approve`, data);
}

// 批量驳回
export function batchReject(data: { taskIds: number[]; comments?: string }) {
  return request.post<void, void>(`${BASE}/batch-reject`, data);
}

// 获取审批历史
export function getApprovalHistory(taskId: number) {
  return request.get<ApprovalHistory[], ApprovalHistory[]>(`${BASE}/history`, { params: { taskId } });
}

/** 强制关闭流程（仅发起人） */
export function forceCloseInstance(data: { instanceId: number; reason?: string }) {
  return request.post<void, void>(`${BASE}/force-close`, data);
}
