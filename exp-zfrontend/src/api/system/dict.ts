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

export interface DictOption {
  value: string;
  label: string;
}

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

export function listDictOptions(dictCode: string) {
  return request.get<DictOption[], DictOption[] | { code?: number; message?: string; data?: DictOption[] }>(
    `${BASE}/item/options`,
    {
      params: { dictCode },
    },
  );
}

/** 获取指定字典类型的全部字典项（用于导出等） */
export function listAllDictItems(dictCode: string) {
  return request.get<DictItem[], DictItem[]>(`${BASE}/item/all`, { params: { dictCode } });
}

/** 获取全部字典项（用于导出） */
export function listAllDictItemsForExport() {
  return request.get<DictItem[], DictItem[]>(`${BASE}/item/export/all`);
}

/** 字典项导入行（JSON/Excel 通用） */
export interface DictItemImportRow {
  dictCode: string;
  itemCode?: string;
  itemValue: string;
  itemLabel: string;
  sortNo?: number;
  status?: string;
  remark?: string;
}

/** 导出结果 */
export interface DictItemImportRes {
  successCount: number;
  failCount: number;
  errors: string[];
}

/** 导出 JSON 格式 */
export function exportDictItemsJson() {
  return listAllDictItemsForExport();
}

/** 导出 Excel：父子结构，支持合并行 */
export interface DictExportHierarchy {
  dictType: DictType;
  items: DictItem[];
}

export function downloadDictExcelBlob(items?: DictItem[]) {
  if (items && items.length > 0) {
    return request.post<Blob, Blob>(`${BASE}/item/export/excel`, items, {
      responseType: 'blob',
    });
  }
  return request.get<Blob, Blob>(`${BASE}/item/export/excel`, {
    responseType: 'blob',
  });
}

/** 导出 Excel（父子结构，字典类型合并行） */
export function downloadDictExcelHierarchyBlob(hierarchy: DictExportHierarchy[]) {
  return request.post<Blob, Blob>(`${BASE}/item/export/excel/hierarchy`, hierarchy, {
    responseType: 'blob',
  });
}

/** 导入 JSON */
export function importDictItemsJson(rows: DictItemImportRow[]) {
  return request.post<DictItemImportRes, DictItemImportRes>(`${BASE}/item/import/json`, rows);
}

/** 导入 Excel */
export function importDictItemsExcel(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<DictItemImportRes, DictItemImportRes>(`${BASE}/item/import/excel`, formData);
}
