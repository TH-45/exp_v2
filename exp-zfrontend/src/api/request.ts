import axios from 'axios';
import { messageError } from '@/utils/message';
import { useUserStore } from '@/store/modules/user';

export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
}


/**
 * GET 请求示例：
 * request.get('/users', { params: { id: 123 } })
 *
 * POST 请求示例：
 * request.post('/users', { name: 'John', age: 30 })
 *
 * 入参说明：
 * - GET 请求：第一个参数为 URL，第二个参数为配置对象，可通过 params 传递查询参数
 * - POST 请求：第一个参数为 URL，第二个参数为请求体数据（通常为对象）
 */
const request = axios.create({
    timeout: 10000,
    baseURL: '/api',
});


// 请求拦截器：添加 token
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

// 响应拦截器：处理 API 返回的数据结构
request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse<unknown>;
    if (
      res &&
      typeof res === 'object' &&
      'success' in res &&
      'code' in res
    ) {
      if (res.success) {
        return (res as ApiResponse<unknown>).data;
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



