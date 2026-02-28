import request from '@/api/request';
import {
  buildPageQuery,
  type PageQueryInput,
  type PageResult,
} from '@/api/common';

/** 工程项目主表 VO，对应 exp_project */
export interface ProjectVO {
  /** 主键ID */
  projectId?: number;
  /** 项目编码（内部唯一） */
  projectCode?: string;
  /** 项目名称 */
  projectName?: string;
  /** 项目类型（工程/服务/采购等） */
  projectType?: string;
  /** 项目状态 */
  projectStatus?: string;
  /** 项目所属单位（本公司） */
  ownerCompanyId?: number;
  /** 业主/甲方单位ID */
  customerCompanyId?: number;
  /** 主合同ID（可为空） */
  contractId?: number;
  /** 招标ID（可为空） */
  tenderId?: number;
  /** 中标投标ID（可为空） */
  bidId?: number;
  /** 项目开始日期 */
  startDate?: string;
  /** 计划完成日期 */
  planEndDate?: string;
  /** 实际完成日期 */
  actualEndDate?: string;
  /** 项目负责人（人员ID） */
  managerPersonId?: number;
  /** 项目负责人账号（冗余） */
  managerAccountId?: number;
  /** 项目归属组织/项目部 */
  orgId?: number;
  /** 项目预算金额 */
  budgetAmount?: number;
  /** 币种 */
  currency?: string;
  /** 创建人 */
  createdBy?: number;
  /** 创建时间 */
  createdTime?: string;
  /** 更新时间 */
  updatedTime?: string;
  /** 备注 */
  remark?: string;
}

/** 列表查询条件（常规筛选） */
export interface ProjectListQueryParam {
  /** 项目编码（模糊） */
  projectCode?: string;
  /** 项目名称（模糊） */
  projectName?: string;
  /** 项目类型 */
  projectType?: string;
  /** 项目状态 */
  projectStatus?: string;
  /** 所属单位ID */
  ownerCompanyId?: number;
  /** 业主/甲方单位ID */
  customerCompanyId?: number;
  /** 项目负责人人员ID */
  managerPersonId?: number;
  /** 归属组织/项目部ID */
  orgId?: number;
  /** 开始日期-起（用于区间） */
  startDateFrom?: string;
  /** 开始日期-止 */
  startDateTo?: string;
  /** 计划完成日期-起 */
  planEndDateFrom?: string;
  /** 计划完成日期-止 */
  planEndDateTo?: string;
}

/** 创建项目 DTO（不含主键与审计字段） */
export type ProjectCreateDTO = Omit<
  ProjectVO,
  'projectId' | 'createdBy' | 'createdTime' | 'updatedTime'
>;

/** 更新项目 DTO（至少含 projectId） */
export interface ProjectUpdateDTO extends ProjectVO {
  /** 主键ID，更新时必填 */
  projectId: number;
}

const BASE = '/exp/project/project';

/**
 * 分页查询项目列表
 * @param input 分页及筛选条件，含 sort 时传入排序字段
 * @returns 分页结果 total/page/size/list
 */
export function listProject(
  input: PageQueryInput<ProjectListQueryParam> & { sort?: string },
) {
  const { sort, ...pageInput } = input;
  const body = {
    ...buildPageQuery(pageInput as PageQueryInput<ProjectListQueryParam>, {
      pageNum: 1,
      pageSize: 10,
    }),
    sort,
  };
  return request.post<PageResult<ProjectVO>, PageResult<ProjectVO>>(
    `${BASE}/list`,
    body,
  );
}

/**
 * 根据项目ID获取详情
 * @param projectId 项目主键
 */
export function getProjectDetail(projectId: number) {
  return request.get<ProjectVO, ProjectVO>(`${BASE}/detail`, {
    params: { projectId },
  });
}

/**
 * 创建项目
 * @param data 创建数据，不含 projectId/createdBy/createdTime/updatedTime
 */
export function createProject(data: ProjectCreateDTO) {
  return request.post<ProjectVO, ProjectVO>(`${BASE}/create`, data);
}

/**
 * 更新项目
 * @param data 更新数据，需包含 projectId
 */
export function updateProject(data: ProjectUpdateDTO) {
  return request.post<ProjectVO, ProjectVO>(`${BASE}/update`, data);
}

/**
 * 删除单个项目
 * @param id 项目主键ID
 */
export function deleteProject(id: number) {
  return request.post<unknown, unknown>(`${BASE}/delete`, { id });
}

/**
 * 批量删除项目
 * @param ids 项目主键ID数组
 */
export function batchDeleteProject(ids: number[]) {
  return request.post<unknown, unknown>(`${BASE}/batchDelete`, { ids });
}
