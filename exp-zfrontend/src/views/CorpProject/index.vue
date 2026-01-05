<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">企业信息与工程项目</div>
          <div class="actions">
            <el-button size="small" @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-section">
        <el-row :gutter="16">
          <el-col :span="4">
            <el-card class="stats-card project-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#409EFF"><OfficeBuilding /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ projectStats.totalProjects }}</div>
                  <div class="stats-label">总项目数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stats-card ongoing-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#67C23A"><VideoPlay /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ projectStats.ongoingProjects }}</div>
                  <div class="stats-label">进行中</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stats-card completed-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#909399"><Check /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ projectStats.completedProjects }}</div>
                  <div class="stats-label">已完成</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stats-card delayed-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#F56C6C"><Warning /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ projectStats.delayedProjects }}</div>
                  <div class="stats-label">延期项目</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stats-card qualification-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#E6A23C"><Medal /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ corpStats.validQualifications }}</div>
                  <div class="stats-label">有效资质</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stats-card account-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon">
                  <el-icon size="32" color="#909399"><User /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-number">{{ corpStats.activeAccounts }}</div>
                  <div class="stats-label">活跃账号</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 快速入口 -->
      <div class="quick-actions">
        <h3>快速入口</h3>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-card class="action-card" shadow="hover" @click="goToQualifications">
              <div class="action-content">
                <el-icon size="48" color="#E6A23C"><Medal /></el-icon>
                <div class="action-info">
                  <div class="action-title">企业资质管理</div>
                  <div class="action-desc">资质证书上传、过期提醒</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="action-card" shadow="hover" @click="goToProjectProgress">
              <div class="action-content">
                <el-icon size="48" color="#409EFF"><TrendCharts /></el-icon>
                <div class="action-info">
                  <div class="action-title">项目进度跟踪</div>
                  <div class="action-desc">里程碑管理、进度更新</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="action-card" shadow="hover" @click="goToProjectMembers">
              <div class="action-content">
                <el-icon size="48" color="#67C23A"><UserFilled /></el-icon>
                <div class="action-info">
                  <div class="action-title">人员配置管理</div>
                  <div class="action-desc">项目团队组建、职责分配</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 最新动态 -->
      <div class="recent-activities">
        <h3>最新动态</h3>
        <el-timeline>
          <!-- 资质提醒 -->
          <el-timeline-item
            v-if="corpStats.expiringQualifications > 0"
            timestamp="刚刚"
            type="warning"
            color="#E6A23C"
          >
            <div class="activity-content">
              <el-icon><Warning /></el-icon>
              <span>{{ corpStats.expiringQualifications }} 个资质即将过期</span>
              <el-button link type="primary" size="small" @click="goToQualifications">查看详情</el-button>
            </div>
          </el-timeline-item>

          <!-- 项目延期提醒 -->
          <el-timeline-item
            v-if="projectStats.delayedProjects > 0"
            timestamp="1小时前"
            type="danger"
            color="#F56C6C"
          >
            <div class="activity-content">
              <el-icon><Warning /></el-icon>
              <span>{{ projectStats.delayedProjects }} 个项目存在延期风险</span>
              <el-button link type="primary" size="small" @click="goToProjects">查看详情</el-button>
            </div>
          </el-timeline-item>

          <!-- 新项目启动 -->
          <el-timeline-item timestamp="昨天" type="success" color="#67C23A">
            <div class="activity-content">
              <el-icon><Plus /></el-icon>
              <span>新项目 "某某商业广场" 已启动</span>
              <el-button link type="primary" size="small" @click="goToProjects">查看详情</el-button>
            </div>
          </el-timeline-item>

          <!-- 资质更新 -->
          <el-timeline-item timestamp="3天前" type="info" color="#409EFF">
            <div class="activity-content">
              <el-icon><Upload /></el-icon>
              <span>建筑施工资质证书已更新</span>
              <el-button link type="primary" size="small" @click="goToQualifications">查看详情</el-button>
            </div>
          </el-timeline-item>

          <!-- 公告发布 -->
          <el-timeline-item timestamp="1周前" type="primary" color="#409EFF">
            <div class="activity-content">
              <el-icon><Document /></el-icon>
              <span>新发布了公司年度工作计划</span>
              <el-button link type="primary" size="small" @click="goToAnnouncements">查看详情</el-button>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import {
  Refresh,
  OfficeBuilding,
  VideoPlay,
  Check,
  Warning,
  Medal,
  User,
  TrendCharts,
  UserFilled,
  Plus,
  Upload,
  Document
} from '@element-plus/icons-vue';
import { getProjectStats, type ProjectStats } from '@/api/project';
import { getCorpInfoStats, type CorpInfoStats } from '@/api/corp';

const router = useRouter();
const loading = ref(false);

const projectStats = reactive<ProjectStats>({
  totalProjects: 0,
  ongoingProjects: 0,
  completedProjects: 0,
  delayedProjects: 0,
  totalBudget: 0,
  totalCost: 0
});

const corpStats = reactive<CorpInfoStats>({
  totalAccounts: 0,
  activeAccounts: 0,
  totalQualifications: 0,
  validQualifications: 0,
  expiringQualifications: 0,
  expiredQualifications: 0,
  totalAnnouncements: 0,
  publishedAnnouncements: 0
});

// 模拟数据（用于演示）
const mockData = {
  projectStats: {
    totalProjects: 25,
    ongoingProjects: 12,
    completedProjects: 8,
    delayedProjects: 3,
    totalBudget: 50000000,
    totalCost: 32000000
  },
  corpStats: {
    totalAccounts: 156,
    activeAccounts: 142,
    totalQualifications: 18,
    validQualifications: 15,
    expiringQualifications: 2,
    expiredQualifications: 1,
    totalAnnouncements: 45,
    publishedAnnouncements: 42
  }
};

async function loadStats() {
  loading.value = true;
  try {
    // 尝试获取真实数据
    const [projectRes, corpRes] = await Promise.allSettled([
      getProjectStats(),
      getCorpInfoStats()
    ]);

    if (projectRes.status === 'fulfilled') {
      Object.assign(projectStats, projectRes.value);
    } else {
      Object.assign(projectStats, mockData.projectStats);
    }

    if (corpRes.status === 'fulfilled') {
      Object.assign(corpStats, corpRes.value);
    } else {
      Object.assign(corpStats, mockData.corpStats);
    }
  } catch (e) {
    // 使用模拟数据
    Object.assign(projectStats, mockData.projectStats);
    Object.assign(corpStats, mockData.corpStats);
  } finally {
    loading.value = false;
  }
}

function refreshData() {
  loadStats();
}

function goToQualifications() {
  router.push('/corp-project/corp-info/qualifications');
}

function goToProjectProgress() {
  router.push('/corp-project/project-mgmt/progress');
}

function goToProjectMembers() {
  router.push('/corp-project/project-mgmt/members');
}

function goToProjects() {
  router.push('/corp-project/project-mgmt/projects');
}

function goToAnnouncements() {
  router.push('/corp-project/corp-info/announcements');
}

onMounted(() => {
  loadStats();
});
</script>

<style scoped lang="scss">
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-weight: 600;
  font-size: 18px;
}

.actions > * + * {
  margin-left: 8px;
}

// 统计卡片
.stats-section {
  margin-bottom: 32px;

  .stats-card {
    height: 100px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .stats-content {
      display: flex;
      align-items: center;
      height: 100%;
      padding: 16px;

      .stats-icon {
        margin-right: 16px;
      }

      .stats-info {
        .stats-number {
          font-size: 28px;
          font-weight: bold;
          line-height: 1;
          margin-bottom: 4px;
        }

        .stats-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .project-card .stats-content .stats-icon {
    color: #409EFF;
  }

  .ongoing-card .stats-content .stats-icon {
    color: #67C23A;
  }

  .completed-card .stats-content .stats-icon {
    color: #909399;
  }

  .delayed-card .stats-content .stats-icon {
    color: #F56C6C;
  }

  .qualification-card .stats-content .stats-icon {
    color: #E6A23C;
  }

  .account-card .stats-content .stats-icon {
    color: #909399;
  }
}

// 快速入口
.quick-actions {
  margin-bottom: 32px;

  h3 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .action-card {
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .action-content {
      display: flex;
      align-items: center;
      padding: 20px;

      .action-info {
        margin-left: 16px;

        .action-title {
          font-size: 16px;
          font-weight: 500;
          margin-bottom: 4px;
          color: #303133;
        }

        .action-desc {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }
}

// 最新动态
.recent-activities {
  h3 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  :deep(.el-timeline-item__content) {
    .activity-content {
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        font-size: 16px;
      }

      span {
        flex: 1;
      }
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .stats-section {
    .el-col {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .quick-actions {
    .el-col {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .action-card .action-content {
      flex-direction: column;
      text-align: center;

      .action-info {
        margin-left: 0;
        margin-top: 12px;
      }
    }
  }
}
</style>
