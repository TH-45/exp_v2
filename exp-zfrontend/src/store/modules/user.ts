import { defineStore } from 'pinia';
import { computed,ref } from 'vue';
import { loginApi, getProfileApi, getPermissionProfileApi, type ProfileResult, type PermissionProfileResult } from '@/api/auth';

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('TOKEN'));
  const username = ref<string | null>(localStorage.getItem('USERNAME'));
  const userId = ref<string | null>(localStorage.getItem('USER_ID'));
  const avatar = ref<string | null>(localStorage.getItem('AVATAR'));
  const roles = ref<string[]>([]);
  const profileLoaded = ref(false);
  const isAdmin = computed(() => roles.value.some(role => role.toLowerCase() === 'admin'));

  const permissionVersion = ref<number>(0);
  const menuTree = ref<PermissionProfileResult['menuTree']>([]);
  const menuLevelMap = ref<Record<string, number>>({});
  const funcPermissionSet = ref<Set<string>>(new Set());

  const setToken = (val: string) => {
    token.value = val;
    localStorage.setItem('TOKEN', val);
  };

  const setProfile = (profile: ProfileResult) => {
    userId.value = profile.userId;
    username.value = profile.username;
    avatar.value = (profile as any).avatar || null;
    roles.value = profile.roles || [];

    localStorage.setItem('USER_ID', profile.userId);
    localStorage.setItem('USERNAME', profile.username);
    if ((profile as any).avatar) {
      localStorage.setItem('AVATAR', (profile as any).avatar);
    }
    profileLoaded.value = true;
  };

  const setPermissionProfile = (profile: PermissionProfileResult) => {
    permissionVersion.value = profile.permissionVersion ?? 0;
    menuTree.value = profile.menuTree ?? [];
    menuLevelMap.value = profile.menuLevelMap ?? {};
    funcPermissionSet.value = new Set(profile.funcPermissionSet ?? []);
    roles.value = profile.roles ?? [];
  };

  const refreshPermissionProfile = async () => {
    if (!token.value) return;
    const profile = await getPermissionProfileApi();
    setPermissionProfile(profile);
  };

  const getMenuLevel = (menuCode: string): number => menuLevelMap.value[menuCode] ?? 0;
  const hasFuncPermission = (permCode: string): boolean => funcPermissionSet.value.has(permCode);

  const login = async (user: string, pass: string) => {
    const res = await loginApi({ username: user, password: pass });
    setToken(res.token);
    const permProfile = await getPermissionProfileApi();
    setPermissionProfile(permProfile);
    userId.value = String(permProfile.userId);
    username.value = permProfile.username;
    localStorage.setItem('USER_ID', String(permProfile.userId));
    localStorage.setItem('USERNAME', permProfile.username);
    profileLoaded.value = true;
  };

  const fetchProfile = async () => {
    if (!token.value) return;
    const profile = await getProfileApi();
    setProfile(profile);
    await refreshPermissionProfile();
  };

  const logout = () => {
    token.value = null;
    username.value = null;
    userId.value = null;
    avatar.value = null;
    roles.value = [];
    permissionVersion.value = 0;
    menuTree.value = [];
    menuLevelMap.value = {};
    funcPermissionSet.value = new Set();
    profileLoaded.value = false;

    localStorage.removeItem('TOKEN');
    localStorage.removeItem('USER_ID');
    localStorage.removeItem('USERNAME');
    localStorage.removeItem('AVATAR');
  };

  return {
    token,
    username,
    userId,
    avatar,
    roles,
    isAdmin,
    profileLoaded,
    permissionVersion,
    menuTree,
    getMenuLevel,
    hasFuncPermission,
    refreshPermissionProfile,
    setPermissionProfile,
    login,
    fetchProfile,
    logout,
    setToken,
  };
});

