import request from '@/api/request';

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
  publisher: string;
  publishTime: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  attachments?: string[];
  readCount?: number;
}

export interface AnnouncementCreateDTO {
  title: string;
  type: 'NOTICE' | 'POLICY';
  content: string;
  attachments?: File[];
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
  attachmentUrl?: string;
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
export function listAnnouncements(params: { page: number; pageSize: number; type?: string; status?: string }) {
  return request.get<{ records: AnnouncementVO[]; total: number }, { records: AnnouncementVO[]; total: number }>(`${BASE}/announcements`, { params });
}

export function getAnnouncementDetail(id: string) {
  return request.get<AnnouncementVO, AnnouncementVO>(`${BASE}/announcements/${id}`);
}

export function createAnnouncement(data: AnnouncementCreateDTO) {
  return request.post<AnnouncementVO, AnnouncementVO>(`${BASE}/announcements`, data);
}

export function updateAnnouncement(id: string, data: Partial<AnnouncementVO>) {
  return request.put<AnnouncementVO, AnnouncementVO>(`${BASE}/announcements/${id}`, data);
}

export function deleteAnnouncement(id: string) {
  return request.delete<void, void>(`${BASE}/announcements/${id}`);
}

export function publishAnnouncement(id: string) {
  return request.post<void, void>(`${BASE}/announcements/${id}/publish`);
}

export function archiveAnnouncement(id: string) {
  return request.post<void, void>(`${BASE}/announcements/${id}/archive`);
}

// 企业资质
export function listQualifications(params: { page: number; pageSize: number; status?: string; category?: string }) {
  return request.get<{ records: QualificationVO[]; total: number }, { records: QualificationVO[]; total: number }>(`${BASE}/qualifications`, { params });
}

export function getQualificationDetail(id: string) {
  return request.get<QualificationVO, QualificationVO>(`${BASE}/qualifications/${id}`);
}

export function createQualification(data: QualificationCreateDTO) {
  return request.post<QualificationVO, QualificationVO>(`${BASE}/qualifications`, data);
}

export function updateQualification(id: string, data: Partial<QualificationVO>) {
  return request.put<QualificationVO, QualificationVO>(`${BASE}/qualifications/${id}`, data);
}

export function deleteQualification(id: string) {
  return request.delete<void, void>(`${BASE}/qualifications/${id}`);
}

export function getQualificationsStats() {
  return request.get<{ valid: number; expiring: number; expired: number }, { valid: number; expiring: number; expired: number }>(`${BASE}/qualifications/stats`);
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
