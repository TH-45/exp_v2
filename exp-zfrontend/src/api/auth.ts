import request from './request';

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResult {
  token: string;
  userId: string;
  username: string;
  roles: string[];
  permissions: string[];
}

export interface ProfileResult {
  userId: string;
  username: string;
  deptId: string;
  deptName: string;
  roles: string[];
  permissions: string[];
  menus: string[];
}

export function loginApi(data: LoginParams) {
  // 实际请求路径：/api/exp/auth/login
  return request.post<LoginResult, LoginResult>('/exp/auth/login', data);
}

export function getProfileApi() {
  // 实际请求路径：/api/exp/auth/profile
  return request.get<ProfileResult, ProfileResult>('/exp/auth/profile');
}



