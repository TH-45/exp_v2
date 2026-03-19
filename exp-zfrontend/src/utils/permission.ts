/**
 * 权限工具：统一对外暴露 getMenuLevel / hasFuncPermission，内部委托 userStore 权限池。
 */
import { useUserStore } from '@/store/modules/user';

/** 获取菜单权限等级：0=无权限，1=查看，2=编辑，3=管理 */
export function getMenuLevel(menuCode: string): number {
  const userStore = useUserStore();
  if (userStore.isAdmin) return 3;
  return userStore.getMenuLevel(menuCode);
}

/** 是否具备功能点权限（perm_code） */
export function hasFuncPermission(permCode: string): boolean {
  const userStore = useUserStore();
  if (userStore.isAdmin) return true;
  return userStore.hasFuncPermission(permCode);
}
