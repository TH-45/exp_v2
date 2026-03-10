import request from '@/api/request';
import { buildPageQuery, type PageQueryInput, type PageResult } from '@/api/common';

export type AttachmentBusinessType = 'TENDER' | 'BID' | 'CONTRACT' | string;
export type AttachmentFileType = string;

export interface AttachmentVO {
  attachmentId: number;
  businessType: AttachmentBusinessType;
  businessId: number;
  businessName?: string;
  fileName: string;
  fileType?: AttachmentFileType;
  fileCategory?: string;
  uploadUserName?: string;
  uploadTime?: string;
}

export interface QueryAttachmentParams {
  businessType?: AttachmentBusinessType;
  fileType?: AttachmentFileType;
  fileName?: string;
  businessId?: number;
  sort?: string;
}

export interface CreateAttachmentBizReq {
  businessType: AttachmentBusinessType;
  businessId: number;
  fileType: AttachmentFileType;
  fileCategory?: string;
  versionNo?: string;
  securityLevel?: string;
  remark?: string;
}

export function queryBiddingAttachmentList(data: PageQueryInput<QueryAttachmentParams>) {
  return request.post<PageResult<AttachmentVO>, PageResult<AttachmentVO>>(
    '/exp/bid/attachment/list',
    buildPageQuery<QueryAttachmentParams>(data),
  );
}

export function uploadBiddingAttachment(file: File, biz: CreateAttachmentBizReq) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('biz', new Blob([JSON.stringify(biz)], { type: 'application/json' }));
  return request.post('/exp/bid/attachment/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function deleteBiddingAttachment(attachmentId: number) {
  return request.post<void, void>(`/exp/bid/attachment/delete?attachmentId=${attachmentId}`);
}

export function batchDeleteBiddingAttachment(attachmentIds: number[]) {
  return request.post<void, void>('/exp/bid/attachment/batchDelete', attachmentIds);
}

export function downloadFile(attachmentId: number | string) {
  return `/api/exp/bid/attachment/downloadStream?attachmentId=${encodeURIComponent(String(attachmentId))}`;
}