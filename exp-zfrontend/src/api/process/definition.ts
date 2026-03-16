import request from '@/api/request';
import { buildPageQuery, type PageQueryInput } from '@/api/common';

export interface ProcessNode {
  nodeId?: number;
  procDefId: number;
  nodeName: string;
  sortNo: number;
  approveType: string;
  assigneeType?: 'ROLE' | 'POST' | 'USER';
  assigneeId: string;
  /** 审批人显示名称（后端批量查人员后返回） */
  assigneeDisplayName?: string;
}

export interface ProcessDefinition {
  procDefId?: number;
  procCode: string;
  procName: string;
  busType: string;
  isActive?: number;
  version?: number;
  remark?: string;
  nodes?: ProcessNode[];
}

export interface ProcessDefinitionQuery {
  pageNum: number;
  pageSize: number;
  procCode?: string;
  procName?: string;
  busType?: string;
  isActive?: number;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}

const BASE = '/exp/process/definition';

export function listProcessDefinitions(params: PageQueryInput<ProcessDefinitionQuery>) {
  return request.post<PageResult<ProcessDefinition>, PageResult<ProcessDefinition>>(
    `${BASE}/list`,
    buildPageQuery(params),
  );
}

export function getProcessDefinitionDetail(procDefId: number) {
  return request.get<ProcessDefinition, ProcessDefinition>(`${BASE}/detail`, { params: { procDefId } });
}

export function saveProcessDefinition(data: ProcessDefinition) {
  return request.post<ProcessDefinition, ProcessDefinition>(`${BASE}/save`, data);
}

export function activateProcessDefinition(procDefId: number, isActive: number) {
  return request.post<void, void>(`${BASE}/activate?procDefId=${procDefId}&isActive=${isActive}`);
}

/** 删除流程定义（无实例时方可删除） */
export function deleteProcessDefinition(procDefId: number) {
  return request.post<void, void>(`${BASE}/delete?procDefId=${procDefId}`);
}

export function copyProcessDefinition(data: { sourceProcDefId: number; newProcCode: string; newProcName: string }) {
  return request.post<ProcessDefinition, ProcessDefinition>(`${BASE}/copy`, data);
}

export function saveProcessNode(data: ProcessNode) {
  return request.post<ProcessNode, ProcessNode>(`${BASE}/node/save`, data);
}

export function deleteProcessNode(nodeId: number) {
  return request.post<void, void>(`${BASE}/node/delete?nodeId=${nodeId}`);
}

export function sortProcessNode(data: { nodeId: number; targetSortNo: number }) {
  return request.post<ProcessNode[], ProcessNode[]>(`${BASE}/node/sort`, data);
}
