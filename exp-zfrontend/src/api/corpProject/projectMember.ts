import request from '@/api/request';

export interface ProjectMemberVO {
  id: number;
  projectId: number;
  userId: number;
  userName?: string;
  department?: string;
  post?: string;
  orgId?: number;
  postId?: number;
  projectRoleCode?: string;
  projectRoleName?: string;
  isManager: boolean;
  joinDate: string;
  leaveDate?: string;
  status: 'ACTIVE' | 'INACTIVE';
  responsibilities?: string;
}

export interface ProjectMemberCreateDTO {
  projectId: number;
  userId: number;
  orgId?: number;
  postId?: number;
  isManager?: boolean;
  joinDate: string;
  leaveDate?: string;
  status: 'ACTIVE' | 'INACTIVE';
  responsibilities?: string;
}

export interface ProjectMemberUpdateDTO {
  id: number;
  userId?: number;
  orgId?: number;
  postId?: number;
  isManager?: boolean;
  joinDate?: string;
  leaveDate?: string;
  status?: 'ACTIVE' | 'INACTIVE';
  responsibilities?: string;
}

const BASE = '/exp/project/projectMgmt/member';

export function getProjectMembers(projectId: number) {
  return request.get<ProjectMemberVO[], ProjectMemberVO[]>(`${BASE}/list`, {
    params: { projectId },
  });
}

export function addProjectMember(data: ProjectMemberCreateDTO) {
  return request.post<ProjectMemberVO, ProjectMemberVO>(`${BASE}/create`, data);
}

export function updateProjectMember(data: ProjectMemberUpdateDTO) {
  return request.post<ProjectMemberVO, ProjectMemberVO>(`${BASE}/update`, data);
}

export function removeProjectMember(id: number) {
  return request.post<void, void>(`${BASE}/delete`, { id });
}
