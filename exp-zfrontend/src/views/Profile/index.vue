<template>
  <el-card class="profile-page">
    <div class="profile-header">
      <div class="avatar-wrap">
        <div class="avatar">{{ getInitial(profile.username) }}</div>
      </div>
      <div class="header-main">
        <div class="name-row">
          <span class="name">{{ profile.username || '-' }}</span>
          <el-tag :type="statusTagType(profile.accountInfo?.status)">
            {{ statusText(profile.accountInfo?.status) }}
          </el-tag>
        </div>
        <div class="meta-row">
          <span>用户ID：{{ profile.userId || '-' }}</span>
          <span>账号：{{ profile.accountInfo?.accountName || '-' }}</span>
        </div>
      </div>
    </div>

    <el-row :gutter="16" class="content-row" v-loading="loading">
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <template #header>人员信息</template>
          <div class="field"><span class="label">姓名</span><span class="value">{{ profile.personInfo?.personName || '-' }}</span></div>
          <div class="field"><span class="label">工号</span><span class="value">{{ profile.personInfo?.personCode || '-' }}</span></div>
          <div class="field"><span class="label">性别</span><span class="value">{{ genderText(profile.personInfo?.gender) }}</span></div>
          <div class="field"><span class="label">手机号</span><span class="value">{{ profile.personInfo?.mobile || '-' }}</span></div>
          <div class="field"><span class="label">邮箱</span><span class="value">{{ profile.personInfo?.email || '-' }}</span></div>
          <div class="field"><span class="label">人员状态</span><span class="value">{{ personStatusText(profile.personInfo?.status) }}</span></div>
          <div class="field"><span class="label">入职日期</span><span class="value">{{ profile.personInfo?.entryDate || '-' }}</span></div>
          <div class="field"><span class="label">职务</span><span class="value">{{ profile.personInfo?.jobTitle || '-' }}</span></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <template #header>账户信息</template>
          <div class="field"><span class="label">账号ID</span><span class="value">{{ profile.accountInfo?.accountId ?? '-' }}</span></div>
          <div class="field"><span class="label">登录账号</span><span class="value">{{ profile.accountInfo?.accountName || '-' }}</span></div>
          <div class="field"><span class="label">显示名称</span><span class="value">{{ profile.accountInfo?.accountDisplay || '-' }}</span></div>
          <div class="field"><span class="label">账号状态</span><span class="value">{{ statusText(profile.accountInfo?.status) }}</span></div>
          <div class="field"><span class="label">最近登录</span><span class="value">{{ formatDateTime(profile.accountInfo?.lastLoginTime) }}</span></div>
          <div class="field"><span class="label">强制改密</span><span class="value">{{ yesNo(profile.accountInfo?.needChangePwd) }}</span></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <template #header>组织信息</template>
          <div class="field"><span class="label">组织ID</span><span class="value">{{ profile.orgInfo?.orgId ?? '-' }}</span></div>
          <div class="field"><span class="label">组织编码</span><span class="value">{{ profile.orgInfo?.orgCode || '-' }}</span></div>
          <div class="field"><span class="label">组织名称</span><span class="value">{{ profile.orgInfo?.orgName || '-' }}</span></div>
          <div class="field"><span class="label">组织类型</span><span class="value">{{ profile.orgInfo?.orgType || '-' }}</span></div>
          <div class="field"><span class="label">上级组织</span><span class="value">{{ profile.orgInfo?.parentOrgName || '-' }}</span></div>
          <div class="field"><span class="label">负责人</span><span class="value">{{ profile.orgInfo?.managerName || '-' }}</span></div>
          <div class="field"><span class="label">联系电话</span><span class="value">{{ profile.orgInfo?.contactPhone || '-' }}</span></div>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getProfileDetailApi, type ProfileDetailResult } from '@/api/auth';

const loading = ref(false);
const profile = ref<ProfileDetailResult>({
  userId: '',
  username: '',
});

const fetchData = async () => {
  loading.value = true;
  try {
    profile.value = await getProfileDetailApi();
  } catch (e) {
    ElMessage.error((e as any)?.message || '获取个人信息失败');
  } finally {
    loading.value = false;
  }
};

const getInitial = (name?: string) => {
  if (!name) return '?';
  return name.slice(0, 1).toUpperCase();
};

const statusTagType = (status?: string) => {
  if (status === 'ENABLED') return 'success';
  if (status === 'INIT') return 'warning';
  if (status === 'DISABLED') return 'info';
  if (status === 'LOCKED') return 'danger';
  return '';
};

const statusText = (status?: string) => {
  if (status === 'ENABLED') return '启用';
  if (status === 'INIT') return '初始';
  if (status === 'DISABLED') return '禁用';
  if (status === 'LOCKED') return '锁定';
  return '-';
};

const personStatusText = (status?: string) => {
  if (status === 'ONJOB') return '在职';
  if (status === 'LEAVE') return '离职';
  if (status === 'DISABLED') return '禁用';
  return '-';
};

const genderText = (gender?: string) => {
  if (gender === 'M') return '男';
  if (gender === 'F') return '女';
  return gender || '-';
};

const yesNo = (val?: boolean) => {
  if (val === true) return '是';
  if (val === false) return '否';
  return '-';
};

const formatDateTime = (value?: string) => {
  if (!value) return '-';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const h = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${day} ${h}:${min}`;
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.profile-page {
  min-height: calc(100vh - 120px);
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 0;
}

.avatar-wrap {
  margin-right: 16px;
}

.avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.meta-row {
  color: #909399;
  font-size: 13px;
  display: flex;
  gap: 16px;
}

.section-card {
  height: 100%;
}

.content-row {
  min-height: 460px;
}

.field {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}

.field:last-child {
  border-bottom: none;
}

.label {
  color: #909399;
  margin-right: 12px;
}

.value {
  color: #303133;
  text-align: right;
  word-break: break-all;
}
</style>
