import request from '@/api/request';

export type DictStatus = 'ENABLED' | 'DISABLED';

export interface DictType {
  id: number;
  dictCode: string;
  dictName: string;
  description?: string;
  status?: DictStatus;
  createdBy?: number;
  createdTime?: string;
  updatedBy?: number;
  updatedTime?: string;
}

export interface DictItem {
  id: number;
  dictCode: string;
  itemCode?: string;
  itemValue: string;
  itemLabel: string;
  sortNo?: number;
  status?: DictStatus;
  remark?: string;
  createdTime?: string;
  updatedTime?: string;
}

export interface PageResult<T> {
  records?: T[];
  total?: number;
  page?: number;
  pageSize?: number;
  list?: T[];
  rows?: T[];
}

export interface DictTypeQuery {
  page: number;
  pageSize: number;
  dictCode?: string;
  dictName?: string;
  status?: DictStatus;
}

export interface DictItemQuery {
  dictCode: string;
  page: number;
  pageSize: number;
  keyword?: string;
  status?: DictStatus;
}

const BASE = '/exp/sys/dict';

export function listDictTypes(params: DictTypeQuery) {
  return request.post<PageResult<DictType>, PageResult<DictType>>(`${BASE}/type/list`, {
    pageNum: params.page,
    pageSize: params.pageSize,
    queryParam: {
      dictCode: params.dictCode,
      dictName: params.dictName,
      status: params.status,
    },
  });
}

export function getDictTypeDetail(params: { id?: number; dictCode?: string }) {
  return request.get<DictType, DictType>(`${BASE}/type/detail`, { params });
}

export function createDictType(data: Partial<DictType>) {
  return request.post<{ id?: number }, { id?: number }>(`${BASE}/type/create`, data);
}

export function updateDictType(data: Partial<DictType>) {
  return request.post<void, void>(`${BASE}/type/update`, data);
}

export function deleteDictType(idOrIds: number | number[]) {
  const ids = Array.isArray(idOrIds) ? idOrIds : [idOrIds];
  return request.post<void, void>(`${BASE}/type/delete`, {
    id: ids.length === 1 ? ids[0] : undefined,
    ids,
  });
}

export function setDictTypeStatus(id: number, status: DictStatus) {
  return request.post<void, void>(`${BASE}/type/status`, { id, status });
}

export function listDictItems(params: DictItemQuery) {
  return request.post<PageResult<DictItem>, PageResult<DictItem>>(`${BASE}/item/list`, {
    pageNum: params.page,
    pageSize: params.pageSize,
    queryParam: {
      dictCode: params.dictCode,
      keyword: params.keyword,
      status: params.status,
    },
  });
}

export function getDictItemDetail(params: { id: number }) {
  return request.get<DictItem, DictItem>(`${BASE}/item/detail`, { params });
}

export function createDictItem(data: Partial<DictItem>) {
  return request.post<{ id?: number }, { id?: number }>(`${BASE}/item/create`, data);
}

export function updateDictItem(data: Partial<DictItem>) {
  return request.post<void, void>(`${BASE}/item/update`, data);
}

export function deleteDictItem(idOrIds: number | number[]) {
  const ids = Array.isArray(idOrIds) ? idOrIds : [idOrIds];
  return request.post<void, void>(`${BASE}/item/delete`, {
    id: ids.length === 1 ? ids[0] : undefined,
    ids,
  });
}

export function setDictItemStatus(id: number, status: DictStatus) {
  return request.post<void, void>(`${BASE}/item/status`, { id, status });
}
