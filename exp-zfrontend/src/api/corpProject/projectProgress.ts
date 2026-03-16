import request from '@/api/request';

export interface ProjectMilestoneVO {
  id: number;
  projectId: number;
  name: string;
  description?: string;
  plannedStartDate: string;
  plannedEndDate: string;
  actualStartDate?: string;
  actualEndDate?: string;
  progress: number;
  status: 'NOT_STARTED' | 'ONGOING' | 'COMPLETED' | 'DELAYED';
  predecessorMilestoneId?: number;
  responsiblePerson?: string;
  responsiblePersonId: number;
}

export interface ProjectProgressVO {
  projectId: number;
  overallProgress: number;
  milestones: ProjectMilestoneVO[];
  delayedMilestones: number;
  completedMilestones: number;
  totalMilestones: number;
}

export interface ProjectMilestoneCreateDTO {
  projectId: number;
  name: string;
  description?: string;
  plannedStartDate: string;
  plannedEndDate: string;
  predecessorMilestoneId?: number;
  responsiblePersonId: number;
}

export interface ProjectMilestoneUpdateDTO extends Omit<ProjectMilestoneCreateDTO, 'projectId'> {
  id: number;
}

export interface ProjectMilestoneProgressUpdateDTO {
  milestoneId: number;
  progress: number;
  actualStartDate?: string;
  actualEndDate?: string;
  remarks?: string;
}

const BASE = '/exp/project/projectMgmt/progress';

export function getProjectProgress(projectId: number) {
  return request.get<ProjectProgressVO, ProjectProgressVO>(`${BASE}/detail`, {
    params: { projectId },
  });
}

export function createProjectMilestone(data: ProjectMilestoneCreateDTO) {
  return request.post<ProjectMilestoneVO, ProjectMilestoneVO>(`${BASE}/milestone/create`, data);
}

export function updateProjectMilestone(data: ProjectMilestoneUpdateDTO) {
  return request.post<ProjectMilestoneVO, ProjectMilestoneVO>(`${BASE}/milestone/update`, data);
}

export function deleteProjectMilestone(id: number) {
  return request.post<void, void>(`${BASE}/milestone/delete`, { id });
}

export function updateMilestoneProgress(data: ProjectMilestoneProgressUpdateDTO) {
  return request.post<ProjectMilestoneVO, ProjectMilestoneVO>(`${BASE}/milestone/updateProgress`, data);
}
