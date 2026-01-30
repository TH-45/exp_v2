import request from '@/api/request';

export type AttachmentBizType =
  | 'TENDER_DOC'
  | 'CLARIFICATION'
  | 'BID_DOC'
  | 'EVALUATION_REPORT'
  | 'OTHER';

export interface AttachmentVO {
  fileId: string;
  fileName: string;
  bizType: AttachmentBizType;
  projectId?: string;
  projectCode?: string;
  projectName?: string;
  uploader?: string;
  uploadTime?: string;
  url?: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  size: number;
}

export interface QueryAttachmentParams {
  pageNum: number;
  size: number;
  keyword?: string;
  projectKeyword?: string;
  bizType?: AttachmentBizType;
  sort?: string;
}

// 说明：docs 只约定了 upload/download，这里 list 先占位，后续对齐后端即可
export function queryBiddingAttachmentList(params: QueryAttachmentParams) {
  return request.get<PageResult<AttachmentVO>, PageResult<AttachmentVO>>('/exp/files/list', { params });
}

export function downloadFile(fileId: string) {
  // 实际下载为文件流；这里返回 url 供前端打开
  return `/api/exp/files/download?fileId=${encodeURIComponent(fileId)}`;
}


