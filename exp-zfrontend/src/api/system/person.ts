import request from '@/api/request';

/**
 * 账号管理（基于 ExpPerson）
 *
 * 注意：本文档说明里「接口 URL / DTO 由后端最终提供」。
 * 这里先按说明中的“方法名（示例）”做前端封装，URL 统一挂在 /exp/person 下，便于后续整体替换。
 */

export type Gender = 'M' | 'F' | 'OTHER';
export type PersonStatus = 'ONJOB' | 'LEAVE' | 'DISABLED';

export interface ExpPersonVO {
  personId: number;
  personCode: string;
  personName: string;
  gender: Gender;
  mobile?: string;
  email?: string;
  idCardNo?: string;
  jobTitle?: string;
  orgId?: number;
  postId?: number;
  accountId?: number;
  status: PersonStatus;
  entryDate?: string; // LocalDate: 'YYYY-MM-DD'
  isExternal?: number; // 0/1
  createdTime?: string; // LocalDateTime: ISO 字符串
  remark?: string;

  /** 非实体字段（后端 VO 返回） */
  deptName?: string;
  roleName?: string;
}

export interface PageResult<T> {
  /** 常见：list/rows/records */
  list?: T[];
  rows?: T[];
  records?: T[];
  /** 常见：total */
  total?: number;
}

export interface QueryPersonParams {
  personCode?: string;
  personName?: string;
  mobile?: string;
  pageNum: number;
  pageSize: number;
}

export interface SimplePageReq<T> {
  pageNum: number;
  pageSize: number;
  sort?: string;
  queryParam?: T;
}

export interface SimplePageRes<T> {
  total: number;
  page: number;
  size: number;
  list: T[];
}

export interface QueryPersonReq {
  personCode?: string;
  personName?: string;
  mobile?: string;
  status?: string;
}

export type SavePersonPayload = Partial<ExpPersonVO> & {
  /** 新增时必填，编辑时只读回显 */
  personCode: string;
  personName: string;
  gender: Gender;
  status: PersonStatus | 'ONJOB' | 'DISABLED';
};

// 后端实际路径：/auth/person/*
const BASE = '/auth/person';

export function queryPersonList(params: QueryPersonParams) {
  const req: SimplePageReq<QueryPersonReq> = {
    pageNum: params.pageNum,
    pageSize: params.pageSize,
    queryParam: {
      personCode: params.personCode,
      personName: params.personName, // 文档中使用 name 而不是 personName
      mobile: params.mobile,    // 文档中使用 phone 而不是 mobile
    },
  };
  return request.post<SimplePageRes<ExpPersonVO>, SimplePageRes<ExpPersonVO>>(
    `${BASE}/list`,
    req,
  );
}

export function getPersonDetail(personId: number) {
  return request.get<ExpPersonVO, ExpPersonVO>(`${BASE}/detail`, {
    params: { personId }
  });
}

export function createPerson(data: SavePersonPayload) {
  return request.post<ExpPersonVO, ExpPersonVO>(`${BASE}/create`, data);
}

export function updatePerson(data: SavePersonPayload) {
  return request.post<ExpPersonVO, ExpPersonVO>(`${BASE}/update`, data);
}

export function deletePerson(personIds: number[] | number) {
  const ids = Array.isArray(personIds) ? personIds : [personIds];
  if (ids.length === 1) {
    return request.post<void, void>(`${BASE}/delete`, { personId: ids[0] });
  } else {
    return request.post<void, void>(`${BASE}/batchDelete`, { personIds: ids });
  }
}

export function changePersonStatus(personId: number, status: PersonStatus) {
  return request.post<ExpPersonVO, ExpPersonVO>(`${BASE}/status`, {
    personId,
    status,
  });
}

export function batchChangePersonStatus(personIds: number[], status: PersonStatus) {
  return request.post<void, void>(`${BASE}/batchStatus`, {
    personIds,
    status,
  });
}

export function resetPassword(personIds: number[] | number, newPassword: string) {
  // 示例方法名：resetPassword（允许单条/批量）
  const ids = Array.isArray(personIds) ? personIds : [personIds];
  return request.post<void, void>(`${BASE}/resetPassword`, {
    personIds: ids,
    newPassword,
  });
}


