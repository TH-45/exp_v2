import request from '@/api/request';
import { buildPageQuery } from '@/api/common';

// 企业信息相关接口

// 账号信息
export interface AccountVO {
  accountId: string;
  username: string;
  realName: string;
  department: string;
  post: string;
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED';
  lastLoginTime?: string;
  createTime: string;
  email?: string;
  phone?: string;
}

export interface AccountListQuery {
  page: number;
  pageSize: number;
  keyword?: string;
  status?: string;
  department?: string;
  post?: string;
}

export interface AccountCreateDTO {
  username: string;
  realName: string;
  department: string;
  post: string;
  email?: string;
  phone?: string;
  initialPassword?: string;
}

// 制度与公告
export interface AnnouncementVO {
  id: string;
  title: string;
  type: 'NOTICE' | 'POLICY';
  content: string;
  publisher?: string;
  publishTime?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'WITHDRAWN';
  attachments?: NoticeAttachmentVO[];
  readCount?: number;
}

export interface AnnouncementCreateDTO {
  title: string;
  type: 'NOTICE' | 'POLICY';
  content: string;
  attachments?: File[]; // 附件通过 uploadAnnouncementAttachment 单独上传
}

export interface NoticeAttachmentVO {
  attachmentId: number;
  noticeId: number;
  fileName: string;
  filePath?: string;
  fileSize?: number;
}

// 企业资质
export interface QualificationVO {
  id: string;
  name: string;
  category: string;
  certificateNumber: string;
  issueDate: string;
  expiryDate: string;
  status: 'VALID' | 'EXPIRING' | 'EXPIRED';
  issuingAuthority: string;
  attachmentId?: number;
  attachmentUrl?: string;
  attachmentName?: string;
  relatedProjects?: string[];
}

export interface QualificationCreateDTO {
  name: string;
  category: string;
  certificateNumber: string;
  issueDate: string;
  expiryDate: string;
  issuingAuthority: string;
  attachment?: File;
}

// 企业基础信息
export interface CorpBasicInfoVO {
  corpId: string;
  corpName: string;
  creditCode: string;
  registeredCapital: string;
  establishDate: string;
  corpType: string;
  industry: string;
  legalPerson: string;
  address: string;
  phone: string;
  email: string;
  website?: string;
}

export interface PartnerVO {
  id: string;
  name: string;
  contact: string;
  phone: string;
  cooperationType: string;
  cooperationStartDate: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface CorpInfoStats {
  totalAccounts: number;
  activeAccounts: number;
  totalQualifications: number;
  validQualifications: number;
  expiringQualifications: number;
  expiredQualifications: number;
  totalAnnouncements: number;
  publishedAnnouncements: number;
}

const BASE = '/exp/corp';
const toApiPath = (path: string) => `${BASE}${path}`;

interface SimplePageRes<T> {
  list?: T[];
  total?: number;
  page?: number;
  size?: number;
}

// 账号管理
export function listAccounts(params: AccountListQuery) {
  return request.get<{ records: AccountVO[]; total: number }, { records: AccountVO[]; total: number }>(`${BASE}/accounts`, { params });
}

export function getAccountDetail(accountId: string) {
  return request.get<AccountVO, AccountVO>(`${BASE}/accounts/${accountId}`);
}

export function createAccount(data: AccountCreateDTO) {
  return request.post<AccountVO, AccountVO>(`${BASE}/accounts`, data);
}

export function updateAccount(accountId: string, data: Partial<AccountVO>) {
  return request.put<AccountVO, AccountVO>(`${BASE}/accounts/${accountId}`, data);
}

export function deleteAccount(accountId: string) {
  return request.delete<void, void>(`${BASE}/accounts/${accountId}`);
}

export function resetPassword(accountId: string, newPassword: string) {
  return request.post<void, void>(`${BASE}/accounts/${accountId}/reset-password`, { newPassword });
}

export function lockAccount(accountId: string) {
  return request.post<void, void>(`${BASE}/accounts/${accountId}/lock`);
}

export function unlockAccount(accountId: string) {
  return request.post<void, void>(`${BASE}/accounts/${accountId}/unlock`);
}

// 制度与公告
export function listAnnouncements(params: {
  page: number;
  pageSize: number;
  title?: string;
  type?: string;
  status?: string;
  publishStartDate?: string;
  publishEndDate?: string;
}) {
  const payload = buildPageQuery({
    pageNum: params.page,
    pageSize: params.pageSize,
    noticeType: params.type,
    publishStatus: params.status,
    title: params.title,
    publishStartDate: params.publishStartDate,
    publishEndDate: params.publishEndDate,
  });
  return request.post<SimplePageRes<any>, { records: AnnouncementVO[]; total: number }>(
    toApiPath('/notice/list'),
    payload,
  ).then((res) => {
    const list = (res?.list || []).map((item: any) => ({
      id: String(item.noticeId),
      title: item.title,
      type: item.noticeType,
      content: item.content || '',
      publisher: item.publisherUserId ? String(item.publisherUserId) : '',
      publishTime: item.publishTime,
      status: item.publishStatus,
      readCount: Number(item.readCount || 0),
      attachments: (item.attachments || []).map((a: any) => ({
        attachmentId: Number(a.attachmentId),
        noticeId: Number(a.noticeId),
        fileName: a.fileName,
        filePath: a.filePath,
        fileSize: a.fileSize,
      })),
    })) as AnnouncementVO[];
    return { records: list, total: Number(res?.total || 0) };
  });
}

export function getAnnouncementDetail(id: string) {
  return request.get<any, AnnouncementVO>(toApiPath('/notice/detail'), {
    params: { noticeId: Number(id) },
  }).then((res) => ({
    id: String(res.noticeId),
    title: res.title,
    type: res.noticeType,
    content: res.content || '',
    publisher: res.publisherUserId ? String(res.publisherUserId) : '',
    publishTime: res.publishTime,
    status: res.publishStatus,
    readCount: Number(res.readCount || 0),
    attachments: (res.attachments || []).map((a: any) => ({
      attachmentId: Number(a.attachmentId),
      noticeId: Number(a.noticeId),
      fileName: a.fileName,
      filePath: a.filePath,
      fileSize: a.fileSize,
    })),
  }));
}

export function createAnnouncement(data: AnnouncementCreateDTO) {
  return request.post<any, AnnouncementVO>(toApiPath('/notice/create'), {
    noticeType: data.type,
    title: data.title,
    content: data.content,
  }).then((res) => ({
    id: String(res.noticeId),
    title: res.title,
    type: res.noticeType,
    content: res.content || '',
    publisher: res.publisherUserId ? String(res.publisherUserId) : '',
    publishTime: res.publishTime,
    status: res.publishStatus,
    readCount: Number(res.readCount || 0),
    attachments: [],
  }));
}

export function updateAnnouncement(id: string, data: Partial<AnnouncementVO>) {
  return request.post<any, AnnouncementVO>(toApiPath('/notice/update'), {
    noticeId: Number(id),
    noticeType: data.type,
    title: data.title,
    content: data.content,
  }).then((res) => ({
    id: String(res.noticeId),
    title: res.title,
    type: res.noticeType,
    content: res.content || '',
    publisher: res.publisherUserId ? String(res.publisherUserId) : '',
    publishTime: res.publishTime,
    status: res.publishStatus,
    readCount: Number(res.readCount || 0),
    attachments: (res.attachments || []).map((a: any) => ({
      attachmentId: Number(a.attachmentId),
      noticeId: Number(a.noticeId),
      fileName: a.fileName,
      filePath: a.filePath,
      fileSize: a.fileSize,
    })),
  }));
}

export function deleteAnnouncement(id: string) {
  return request.post<void, void>(toApiPath('/notice/delete'), { noticeId: Number(id) });
}

export function publishAnnouncement(id: string) {
  return request.post<void, void>(toApiPath('/notice/publish'), { noticeId: Number(id) });
}

export function withdrawAnnouncement(id: string) {
  return request.post<void, void>(toApiPath('/notice/withdraw'), { noticeId: Number(id) });
}

/** 兼容旧命名 */
export const archiveAnnouncement = withdrawAnnouncement;

export function uploadAnnouncementAttachment(noticeId: string, file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<NoticeAttachmentVO, NoticeAttachmentVO>(toApiPath(`/notice-attachment/upload?noticeId=${encodeURIComponent(noticeId)}`), formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function deleteAnnouncementAttachment(attachmentId: number) {
  return request.post<void, void>(toApiPath('/notice-attachment/delete'), { attachmentId });
}

export function downloadAnnouncementAttachment(attachmentId: number, fileName?: string) {
  const base = `/api/exp/corp/notice-attachment/downloadStream?attachmentId=${encodeURIComponent(String(attachmentId))}`;
  return fileName ? `${base}&fileName=${encodeURIComponent(fileName)}` : base;
}

// 企业资质
export function listQualifications(params: { page: number; pageSize: number; status?: string; category?: string }) {
  const statusMap: Record<string, string> = {
    EXPIRING: 'WILL_EXPIRE',
  };
  const payload = buildPageQuery({
    pageNum: params.page,
    pageSize: params.pageSize,
    qualificationName: (params as any).name,
    qualificationType: params.category,
    status: params.status ? (statusMap[params.status] || params.status) : undefined,
  });
  return request.post<SimplePageRes<any>, { records: QualificationVO[]; total: number }>(
    toApiPath('/qualification/list'),
    payload,
  ).then((res) => {
    const rawList = (res?.list || []) as any[];
    const mapped = rawList.map((item) => ({
      id: String(item.qualificationId),
      name: item.qualificationName,
      category: item.qualificationType,
      certificateNumber: item.qualificationCode,
      issueDate: item.issueDate || '',
      expiryDate: item.validTo || '',
      status: item.status === 'WILL_EXPIRE' ? 'EXPIRING' : item.status,
      issuingAuthority: item.issueOrg || '',
      relatedProjects: [],
    })) as QualificationVO[];
    return { records: mapped, total: Number(res?.total || 0) };
  });
}

export function getQualificationDetail(id: string) {
  return request.get<any, QualificationVO>(toApiPath('/qualification/detail'), {
    params: { qualificationId: Number(id) },
  }).then(async (res) => {
    const attachmentList = await request.post<SimplePageRes<any>, SimplePageRes<any>>(toApiPath('/qualification-attachment/list'), {
      pageNum: 1,
      pageSize: 10,
      queryParam: { qualificationId: Number(id) },
    }).catch(() => ({ list: [] }));
    const firstAttachment = Array.isArray(attachmentList?.list) ? attachmentList.list[0] : undefined;
    return {
      id: String(res.qualificationId),
      name: res.qualificationName,
      category: res.qualificationType,
      certificateNumber: res.qualificationCode,
      issueDate: res.issueDate,
      expiryDate: res.validTo,
      status: res.status === 'WILL_EXPIRE' ? 'EXPIRING' : res.status,
      issuingAuthority: res.issueOrg,
      attachmentId: firstAttachment?.attachmentId ? Number(firstAttachment.attachmentId) : undefined,
      attachmentName: firstAttachment?.fileName,
      attachmentUrl: firstAttachment?.attachmentId
        ? `/api/exp/corp/qualification-attachment/downloadStream?attachmentId=${encodeURIComponent(String(firstAttachment.attachmentId))}&fileName=${encodeURIComponent(firstAttachment.fileName || 'qualification')}`
        : undefined,
      relatedProjects: [],
    };
  });
}

export function createQualification(data: QualificationCreateDTO) {
  return request.post<any, QualificationVO>(toApiPath('/qualification/create'), {
    qualificationCode: data.certificateNumber,
    qualificationName: data.name,
    qualificationType: data.category,
    issueOrg: data.issuingAuthority,
    issueDate: data.issueDate,
    validFrom: data.issueDate,
    validTo: data.expiryDate,
    status: undefined,
  }).then((res) => ({
    id: String(res.qualificationId),
    name: res.qualificationName,
    category: res.qualificationType,
    certificateNumber: res.qualificationCode,
    issueDate: res.issueDate,
    expiryDate: res.validTo,
    status: res.status === 'WILL_EXPIRE' ? 'EXPIRING' : res.status,
    issuingAuthority: res.issueOrg,
    relatedProjects: [],
  }));
}

export function updateQualification(id: string, data: Partial<QualificationVO>) {
  return request.post<any, QualificationVO>(toApiPath('/qualification/update'), {
    qualificationId: Number(id),
    qualificationCode: data.certificateNumber,
    qualificationName: data.name,
    qualificationType: data.category,
    issueOrg: data.issuingAuthority,
    issueDate: data.issueDate,
    validFrom: data.issueDate,
    validTo: data.expiryDate,
    status: data.status === 'EXPIRING' ? 'WILL_EXPIRE' : data.status,
  }).then((res) => ({
    id: String(res.qualificationId),
    name: res.qualificationName,
    category: res.qualificationType,
    certificateNumber: res.qualificationCode,
    issueDate: res.issueDate,
    expiryDate: res.validTo,
    status: res.status === 'WILL_EXPIRE' ? 'EXPIRING' : res.status,
    issuingAuthority: res.issueOrg,
    relatedProjects: [],
  }));
}

export function deleteQualification(id: string) {
  return request.post<void, void>(toApiPath('/qualification/delete'), { qualificationId: Number(id) });
}

export function getQualificationsStats() {
  return request.get<{ valid: number; expiring: number; expired: number }, { valid: number; expiring: number; expired: number }>(toApiPath('/qualification/stats'));
}

export function uploadQualificationAttachment(qualificationId: string, file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<any, any>(toApiPath(`/qualification-attachment/upload?qualificationId=${encodeURIComponent(qualificationId)}`), formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

// 企业基础信息
export function getCorpBasicInfo() {
  return request.get<CorpBasicInfoVO, CorpBasicInfoVO>(`${BASE}/basic-info`);
}

export function updateCorpBasicInfo(data: Partial<CorpBasicInfoVO>) {
  return request.put<CorpBasicInfoVO, CorpBasicInfoVO>(`${BASE}/basic-info`, data);
}

// 合作单位
export function listPartners(params: { page: number; pageSize: number; status?: string }) {
  return request.get<{ records: PartnerVO[]; total: number }, { records: PartnerVO[]; total: number }>(`${BASE}/partners`, { params });
}

export function createPartner(data: Omit<PartnerVO, 'id'>) {
  return request.post<PartnerVO, PartnerVO>(`${BASE}/partners`, data);
}

export function updatePartner(id: string, data: Partial<PartnerVO>) {
  return request.put<PartnerVO, PartnerVO>(`${BASE}/partners/${id}`, data);
}

export function deletePartner(id: string) {
  return request.delete<void, void>(`${BASE}/partners/${id}`);
}

// 统计信息
export function getCorpInfoStats() {
  return request.get<CorpInfoStats, CorpInfoStats>(`${BASE}/stats`);
}
