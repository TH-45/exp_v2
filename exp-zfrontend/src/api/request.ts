import axios from 'axios';
import { messageError } from '@/utils/message';
import { useUserStore } from '@/store/modules/user';

export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse<unknown>;
    if (
      res &&
      typeof res === 'object' &&
      'success' in res &&
      'code' in res &&
      'data' in res
    ) {
      if (res.success) {
        return res.data;
      }
      messageError(res.message || '请求失败');
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    // 非统一响应结构，直接返回原始数据
    return response.data;
  },
  (error) => {
    if (error.response) {
      messageError('系统异常，请联系管理员');
    } else {
      messageError('网络异常，请检查网络连接');
    }
    return Promise.reject(error);
  },
);

export default request;



