// utils/permission.ts
import { useUserStore } from '@/store/modules/user';

export function hasPermission(permission: string) {
    const userStore = useUserStore();

    // 超级管理员直接放行
    if (userStore.isAdmin) {
        return true;
    }

    return userStore.permissions.includes(permission);
}
