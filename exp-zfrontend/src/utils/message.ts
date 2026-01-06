import { ElMessage, ElMessageBox } from 'element-plus';

// 自定义消息配置
const DEFAULT_DURATION = 2000; // 默认显示2秒
const EXTENDED_DURATION = 6000; // 鼠标悬停时延长到6秒

// 定义消息参数类型
interface CustomMessageParams {
  message: string;
  type?: 'success' | 'warning' | 'info' | 'error';
  duration?: number;
  showClose?: boolean;
  center?: boolean;
  onClose?: () => void;
  offset?: number;
}

// 存储当前消息实例的引用
let currentMessageInstance: any = null;
let messageTimer: number | null = null;

/**
 * 自定义消息函数，支持鼠标悬停延长显示时间
 */
const createHoverableMessage = (options: CustomMessageParams) => {
  // 如果有正在显示的消息，先清除
  if (currentMessageInstance) {
    currentMessageInstance.close();
    if (messageTimer) {
      clearTimeout(messageTimer);
    }
  }

  // 设置默认显示时间
  const messageOptions: CustomMessageParams = {
    ...options,
    duration: options.duration ?? DEFAULT_DURATION,
  };

  // 显示消息
  currentMessageInstance = ElMessage(messageOptions);

  // 设置定时器，在指定时间后自动关闭
  messageTimer = setTimeout(() => {
    if (currentMessageInstance) {
      currentMessageInstance.close();
      currentMessageInstance = null;
    }
  }, messageOptions.duration);

  // 如果消息实例支持事件监听，添加鼠标悬停事件
  if (currentMessageInstance && currentMessageInstance.$el) {
    const messageEl = currentMessageInstance.$el;

    // 鼠标进入时延长显示时间
    messageEl.addEventListener('mouseenter', () => {
      if (messageTimer) {
        clearTimeout(messageTimer);
      }
      // 延长显示时间
      messageTimer = setTimeout(() => {
        if (currentMessageInstance) {
          currentMessageInstance.close();
          currentMessageInstance = null;
        }
      }, EXTENDED_DURATION);
    });

    // 鼠标离开时恢复正常计时
    messageEl.addEventListener('mouseleave', () => {
      if (messageTimer) {
        clearTimeout(messageTimer);
      }
      // 重新开始计时，默认时间
      messageTimer = setTimeout(() => {
        if (currentMessageInstance) {
          currentMessageInstance.close();
          currentMessageInstance = null;
        }
      }, DEFAULT_DURATION);
    });
  }

  return currentMessageInstance;
};

/**
 * 成功消息
 */
export const messageSuccess = (message: string, duration?: number) => {
  return createHoverableMessage({
    message,
    type: 'success',
    duration: duration || DEFAULT_DURATION,
  });
};

/**
 * 错误消息
 */
export const messageError = (message: string, duration?: number) => {
  return createHoverableMessage({
    message,
    type: 'error',
    duration: duration || DEFAULT_DURATION,
  });
};

/**
 * 警告消息
 */
export const messageWarning = (message: string, duration?: number) => {
  return createHoverableMessage({
    message,
    type: 'warning',
    duration: duration || DEFAULT_DURATION,
  });
};

/**
 * 信息消息
 */
export const messageInfo = (message: string, duration?: number) => {
  return createHoverableMessage({
    message,
    type: 'info',
    duration: duration || DEFAULT_DURATION,
  });
};

/**
 * 导出原始的ElMessage和ElMessageBox以保持兼容性
 */
export { ElMessage, ElMessageBox };
export default {
  success: messageSuccess,
  error: messageError,
  warning: messageWarning,
  info: messageInfo,
};
