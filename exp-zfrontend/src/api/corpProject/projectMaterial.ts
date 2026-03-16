import request from '@/api/request';

export interface ProjectMaterialVO {
  id: number;
  projectId: number;
  materialCode: string;
  materialName: string;
  spec: string;
  unit: string;
  requiredQuantity: number;
  receivedQuantity: number;
  usedQuantity: number;
  stockQuantity: number;
  unitPrice: number;
  totalAmount: number;
  supplierName?: string;
  status: 'NORMAL' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  lastUpdateTime?: string;
}

export interface ProjectMaterialDetailVO {
  materials: ProjectMaterialVO[];
  total: number;
  lowStock: number;
  outOfStock: number;
}

export interface ProjectMaterialCreateDTO {
  projectId: number;
  materialCode: string;
  materialName: string;
  spec: string;
  unit: string;
  requiredQuantity: number;
  unitPrice: number;
  supplierName?: string;
}

export interface ProjectMaterialUpdateDTO {
  id: number;
  materialName: string;
  spec: string;
  unit: string;
  requiredQuantity: number;
  unitPrice: number;
  supplierName?: string;
}

export interface ProjectMaterialInboundDTO {
  id: number;
  quantity: number;
  remarks?: string;
}

export interface ProjectMaterialOutboundDTO {
  id: number;
  quantity: number;
  useDate?: string;
  remarks?: string;
}

const BASE = '/exp/project/projectMgmt/material';

export function getProjectMaterialDetail(projectId: number) {
  return request.get<ProjectMaterialDetailVO, ProjectMaterialDetailVO>(`${BASE}/detail`, {
    params: { projectId },
  });
}

export function createProjectMaterial(data: ProjectMaterialCreateDTO) {
  return request.post<ProjectMaterialVO, ProjectMaterialVO>(`${BASE}/create`, data);
}

export function updateProjectMaterial(data: ProjectMaterialUpdateDTO) {
  return request.post<ProjectMaterialVO, ProjectMaterialVO>(`${BASE}/update`, data);
}

export function deleteProjectMaterial(id: number) {
  return request.post<void, void>(`${BASE}/delete`, { id });
}

export function inboundProjectMaterial(data: ProjectMaterialInboundDTO) {
  return request.post<ProjectMaterialVO, ProjectMaterialVO>(`${BASE}/inbound`, data);
}

export function outboundProjectMaterial(data: ProjectMaterialOutboundDTO) {
  return request.post<ProjectMaterialVO, ProjectMaterialVO>(`${BASE}/outbound`, data);
}
