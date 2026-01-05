import request from '@/api/request';

// 工程项目相关接口

// 项目基本信息
export interface ProjectVO {
  id: string;
  name: string;
  code: string;
  description?: string;
  manager: string;
  managerId: string;
  department: string;
  startDate: string;
  endDate: string;
  plannedEndDate: string;
  status: 'PLANNING' | 'ONGOING' | 'COMPLETED' | 'SUSPENDED' | 'CANCELLED';
  progress: number; // 0-100
  budget: number;
  actualCost?: number;
  address: string;
  clientName?: string;
  contractAmount?: number;
  createTime: string;
  updateTime: string;
}

export interface ProjectCreateDTO {
  name: string;
  code: string;
  description?: string;
  managerId: string;
  department: string;
  startDate: string;
  plannedEndDate: string;
  budget: number;
  address: string;
  clientName?: string;
  contractAmount?: number;
}

export interface ProjectListQuery {
  page: number;
  pageSize: number;
  keyword?: string;
  status?: string;
  manager?: string;
  department?: string;
  startDate?: string;
  endDate?: string;
}

// 项目人员配置
export interface ProjectMemberVO {
  id: string;
  projectId: string;
  userId: string;
  userName: string;
  department: string;
  post: string;
  joinDate: string;
  leaveDate?: string;
  responsibilities: string;
  status: 'ACTIVE' | 'INACTIVE';
  isManager: boolean;
}

export interface ProjectMemberCreateDTO {
  projectId: string;
  userId: string;
  responsibilities: string;
  joinDate: string;
}

export interface ProjectOrgNode {
  id: string;
  name: string;
  post: string;
  parentId?: string;
  children?: ProjectOrgNode[];
  member?: ProjectMemberVO;
}

// 项目物料管理
export interface ProjectMaterialVO {
  id: string;
  projectId: string;
  name: string;
  specification: string;
  unit: string;
  requiredQuantity: number;
  receivedQuantity: number;
  usedQuantity: number;
  stockQuantity: number;
  unitPrice: number;
  totalAmount: number;
  supplier?: string;
  status: 'NORMAL' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  lastUpdateTime: string;
}

export interface ProjectMaterialCreateDTO {
  projectId: string;
  name: string;
  specification: string;
  unit: string;
  requiredQuantity: number;
  unitPrice: number;
  supplier?: string;
}

export interface MaterialProcurementDTO {
  materialId: string;
  quantity: number;
  supplier: string;
  expectedDate: string;
  remarks?: string;
}

// 项目进度管理
export interface ProjectMilestoneVO {
  id: string;
  projectId: string;
  name: string;
  description?: string;
  plannedStartDate: string;
  plannedEndDate: string;
  actualStartDate?: string;
  actualEndDate?: string;
  progress: number; // 0-100
  status: 'NOT_STARTED' | 'ONGOING' | 'COMPLETED' | 'DELAYED';
  predecessorMilestoneId?: string;
  responsiblePerson: string;
  responsiblePersonId: string;
  createTime: string;
}

export interface ProjectMilestoneCreateDTO {
  projectId: string;
  name: string;
  description?: string;
  plannedStartDate: string;
  plannedEndDate: string;
  predecessorMilestoneId?: string;
  responsiblePersonId: string;
}

export interface ProjectProgressUpdateDTO {
  milestoneId: string;
  progress: number;
  actualStartDate?: string;
  actualEndDate?: string;
  remarks?: string;
}

export interface ProjectProgressVO {
  projectId: string;
  overallProgress: number;
  milestones: ProjectMilestoneVO[];
  delayedMilestones: number;
  completedMilestones: number;
  totalMilestones: number;
  nextMilestone?: ProjectMilestoneVO;
}

export interface ProjectStats {
  totalProjects: number;
  ongoingProjects: number;
  completedProjects: number;
  delayedProjects: number;
  totalBudget: number;
  totalCost: number;
}

const BASE = '/exp/project';

// 项目基本管理
export function listProjects(params: ProjectListQuery) {
  return request.get<{ records: ProjectVO[]; total: number }, { records: ProjectVO[]; total: number }>(`${BASE}/projects`, { params });
}

export function getProjectDetail(projectId: string) {
  return request.get<ProjectVO, ProjectVO>(`${BASE}/projects/${projectId}`);
}

export function createProject(data: ProjectCreateDTO) {
  return request.post<ProjectVO, ProjectVO>(`${BASE}/projects`, data);
}

export function updateProject(projectId: string, data: Partial<ProjectVO>) {
  return request.put<ProjectVO, ProjectVO>(`${BASE}/projects/${projectId}`, data);
}

export function deleteProject(projectId: string) {
  return request.delete<void, void>(`${BASE}/projects/${projectId}`);
}

export function getProjectStats() {
  return request.get<ProjectStats, ProjectStats>(`${BASE}/stats`);
}

// 项目人员管理
export function getProjectMembers(projectId: string) {
  return request.get<ProjectMemberVO[], ProjectMemberVO[]>(`${BASE}/projects/${projectId}/members`);
}

export function addProjectMember(data: ProjectMemberCreateDTO) {
  return request.post<ProjectMemberVO, ProjectMemberVO>(`${BASE}/members`, data);
}

export function updateProjectMember(memberId: string, data: Partial<ProjectMemberVO>) {
  return request.put<ProjectMemberVO, ProjectMemberVO>(`${BASE}/members/${memberId}`, data);
}

export function removeProjectMember(memberId: string) {
  return request.delete<void, void>(`${BASE}/members/${memberId}`);
}

export function getProjectOrgStructure(projectId: string) {
  return request.get<ProjectOrgNode[], ProjectOrgNode[]>(`${BASE}/projects/${projectId}/org-structure`);
}

// 项目物料管理
export function getProjectMaterials(projectId: string) {
  return request.get<ProjectMaterialVO[], ProjectMaterialVO[]>(`${BASE}/projects/${projectId}/materials`);
}

export function addProjectMaterial(data: ProjectMaterialCreateDTO) {
  return request.post<ProjectMaterialVO, ProjectMaterialVO>(`${BASE}/materials`, data);
}

export function updateProjectMaterial(materialId: string, data: Partial<ProjectMaterialVO>) {
  return request.put<ProjectMaterialVO, ProjectMaterialVO>(`${BASE}/materials/${materialId}`, data);
}

export function deleteProjectMaterial(materialId: string) {
  return request.delete<void, void>(`${BASE}/materials/${materialId}`);
}

export function updateMaterialStock(materialId: string, quantity: number) {
  return request.post<void, void>(`${BASE}/materials/${materialId}/stock`, { quantity });
}

export function createMaterialProcurement(data: MaterialProcurementDTO) {
  return request.post<void, void>(`${BASE}/materials/procurement`, data);
}

export function getMaterialStats(projectId: string) {
  return request.get<{ total: number; lowStock: number; outOfStock: number }, { total: number; lowStock: number; outOfStock: number }>(`${BASE}/projects/${projectId}/materials/stats`);
}

// 项目进度管理
export function getProjectMilestones(projectId: string) {
  return request.get<ProjectMilestoneVO[], ProjectMilestoneVO[]>(`${BASE}/projects/${projectId}/milestones`);
}

export function createProjectMilestone(data: ProjectMilestoneCreateDTO) {
  return request.post<ProjectMilestoneVO, ProjectMilestoneVO>(`${BASE}/milestones`, data);
}

export function updateProjectMilestone(milestoneId: string, data: Partial<ProjectMilestoneVO>) {
  return request.put<ProjectMilestoneVO, ProjectMilestoneVO>(`${BASE}/milestones/${milestoneId}`, data);
}

export function deleteProjectMilestone(milestoneId: string) {
  return request.delete<void, void>(`${BASE}/milestones/${milestoneId}`);
}

export function updateMilestoneProgress(data: ProjectProgressUpdateDTO) {
  return request.post<void, void>(`${BASE}/milestones/progress`, data);
}

export function getProjectProgress(projectId: string) {
  return request.get<ProjectProgressVO, ProjectProgressVO>(`${BASE}/projects/${projectId}/progress`);
}

// 延期提醒
export function getDelayedProjects() {
  return request.get<ProjectVO[], ProjectVO[]>(`${BASE}/delayed-projects`);
}

export function setProjectDelayReminder(projectId: string, reminderDays: number) {
  return request.post<void, void>(`${BASE}/projects/${projectId}/delay-reminder`, { reminderDays });
}
