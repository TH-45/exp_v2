/**
 * 账号生成工具函数
 */

/**
 * 生成账号名
 * 规则：4A + 四位时间戳 + 三位随机数
 * @returns 生成的账号名
 */
export function generateAccountName(): string {
  // 获取当前时间戳的后4位
  const timestamp = Date.now().toString().slice(-4);

  // 生成3位随机数
  const randomNum = Math.floor(Math.random() * 1000).toString().padStart(3, '0');

  // 组合账号
  return `4A${timestamp}${randomNum}`;
}

/**
 * 检查账号名是否符合规则
 * @param accountName 账号名
 * @returns 是否符合规则
 */
export function isValidAccountName(accountName: string): boolean {
  const pattern = /^4A\d{7}$/;
  return pattern.test(accountName);
}

/**
 * 生成密码
 * 默认生成8位随机密码
 * @param length 密码长度，默认8位
 * @returns 生成的密码
 */
export function generatePassword(length: number = 8): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}
