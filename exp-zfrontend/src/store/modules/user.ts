import { defineStore } from 'pinia';
import { computed,ref } from 'vue';
import { loginApi, getProfileApi, type ProfileResult } from '@/api/auth';

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('TOKEN'));
  const username = ref<string | null>(localStorage.getItem('USERNAME'));
  const userId = ref<string | null>(localStorage.getItem('USER_ID'));
  const roles = ref<string[]>([]);
  const permissions = ref<string[]>([]);
  const menus = ref<string[]>([]);
  const profileLoaded = ref(false);
  const isAdmin = computed(() => roles.value.some(role => role.toLowerCase() === 'admin'));

  const setToken = (val: string) => {
    token.value = val;
    localStorage.setItem('TOKEN', val);
  };

  const setProfile = (profile: ProfileResult) => {
    userId.value = profile.userId;
    username.value = profile.username;
    roles.value = profile.roles || [];
    permissions.value = profile.permissions || [];
    menus.value = profile.menus || [];

    localStorage.setItem('USER_ID', profile.userId);
    localStorage.setItem('USERNAME', profile.username);
    profileLoaded.value = true;
  };

  const login = async (user: string, pass: string) => {
    const res = await loginApi({ username: user, password: pass });
    setToken(res.token);
    // 登录成功后再拉取用户信息，包含权限与菜单
    const profile = await getProfileApi();
    setProfile(profile);
  };

  const fetchProfile = async () => {
    if (!token.value) return;
    const profile = await getProfileApi();
    setProfile(profile);
  };

  const logout = () => {
    token.value = null;
    username.value = null;
    userId.value = null;
    roles.value = [];
    permissions.value = [];
    menus.value = [];
    profileLoaded.value = false;

    localStorage.removeItem('TOKEN');
    localStorage.removeItem('USER_ID');
    localStorage.removeItem('USERNAME');
  };

  return {
    token,
    username,
    userId,
    roles,
    isAdmin,
    permissions,
    menus,
    profileLoaded,
    login,
    fetchProfile,
    logout,
    setToken,
  };
});

