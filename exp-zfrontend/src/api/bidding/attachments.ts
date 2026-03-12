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

/** 单文件上传（保留兼容） */
export function uploadBiddingAttachment(file: File, biz: CreateAttachmentBizReq) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('biz', new Blob([JSON.stringify(biz)], { type: 'application/json' }));
  return request.post('/exp/bid/attachment/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

/**
 * 多文件上传：文件必填，每个文件对应一份 biz，全成全败。
 * @param files 文件列表（至少一个）
 * @param bizList 与 files 一一对应的业务参数，长度需等于 files.length
 */
export function uploadBiddingAttachments(
  files: File[],
  bizList: CreateAttachmentBizReq[],
) {
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  formData.append(
    'bizList',
    new Blob([JSON.stringify(bizList)], { type: 'application/json' }),
  );
  return request.post<{ list?: unknown[] }>('/exp/bid/attachment/uploadBatch', formData, {
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