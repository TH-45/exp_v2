import axios from 'axios';
import { messageError } from '@/utils/message';
import { useUserStore } from '@/store/modules/user';

declare module 'axios' {
  interface AxiosRequestConfig {
    skipErrorToast?: boolean;
  }
}

export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
}

const PRESET_ERROR_MESSAGE_BY_STATUS: Record<number, string> = {
  400: '请求参数有误，请检查后重试',
  401: '用户名或密码错误，或登录状态已失效',
  403: '当前账号无权限访问',
  404: '请求的服务不存在',
  408: '请求超时，请稍后重试',
  429: '请求过于频繁，请稍后再试',
  500: '系统繁忙，请稍后重试',
  502: '系统繁忙，请稍后重试',
  503: '系统繁忙，请稍后重试',
  504: '系统繁忙，请稍后重试',
};

const PRESET_ERROR_MESSAGE_BY_CODE: Record<string, string> = {
  AUTH_UNAUTHORIZED: '用户名或密码错误，或登录状态已失效',
  AUTH_INVALID_TOKEN: '登录状态已失效，请重新登录',
  AUTH_LOGIN_FAILED: '登录失败，请稍后重试',
};

const FALLBACK_ERROR_MESSAGE = '请求失败，请稍后重试';

const SENSITIVE_ERROR_PATTERN = /(exception|stack|trace|java\.|org\.|sql|syntax error|nullpointer)/i;

function isRecord(val: unknown): val is Record<string, unknown> {
  return !!val && typeof val === 'object';
}

function safeBackendMessage(msg: unknown): string {
  if (typeof msg !== 'string') return '';
  const trimmed = msg.trim();
  if (!trimmed) return '';
  if (trimmed.length > 120) return '';
  if (SENSITIVE_ERROR_PATTERN.test(trimmed)) return '';
  return trimmed;
}

function parseBackendError(data: unknown): { code?: string; message?: string } {
  if (typeof data === 'string') {
    return { message: safeBackendMessage(data) };
  }
  if (!isRecord(data)) {
    return {};
  }
  const code = typeof data.code === 'string' ? data.code : undefined;
  const message =
    safeBackendMessage(data.message) ||
    safeBackendMessage(data.msg) ||
    safeBackendMessage(data.error) ||
    safeBackendMessage(data.detail);
  return { code, message: message || undefined };
}

function inferStatusFromMessage(message?: string): number | undefined {
  if (!message) return undefined;
  const match = message.match(/\b([45]\d{2})\b/);
  if (!match) return undefined;
  const parsed = Number(match[1]);
  return Number.isNaN(parsed) ? undefined : parsed;
}

function resolveErrorMessage(params: { status?: number; code?: string; backendMessage?: string }): string {
  const { status, code, backendMessage } = params;
  const effectiveStatus = status ?? inferStatusFromMessage(backendMessage);
  if (typeof effectiveStatus === 'number' && PRESET_ERROR_MESSAGE_BY_STATUS[effectiveStatus]) {
    return PRESET_ERROR_MESSAGE_BY_STATUS[effectiveStatus];
  }
  if (code && PRESET_ERROR_MESSAGE_BY_CODE[code]) {
    return PRESET_ERROR_MESSAGE_BY_CODE[code];
  }
  if (backendMessage) {
    return backendMessage;
  }
  return FALLBACK_ERROR_MESSAGE;
}

function shouldShowErrorToast(config?: { skipErrorToast?: boolean }): boolean {
  return !config?.skipErrorToast;
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
      const msg = resolveErrorMessage({
        code: res.code,
        backendMessage: safeBackendMessage(res.message),
      });
      if (shouldShowErrorToast(response.config)) {
        messageError(msg);
      }
      return Promise.reject(new Error(msg));
    }
    // 非统一响应结构，直接返回原始数据
    return response.data;
  },
  (error) => {
    if (!shouldShowErrorToast(error?.config)) {
      return Promise.reject(error);
    }
    if (error.response) {
      const status = error.response.status as number | undefined;
      const parsed = parseBackendError(error.response.data);
      const msg = resolveErrorMessage({
        status,
        code: parsed.code,
        backendMessage: parsed.message,
      });
      messageError(msg);
    } else {
      messageError('网络异常，请检查网络连接');
    }
    return Promise.reject(error);
  },
);

export default request;



