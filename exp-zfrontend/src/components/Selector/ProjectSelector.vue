<template>
  <el-select
      v-model="innerValue"
      filterable
      remote
      clearable
      placeholder="请选择项目"
      :remote-method="handleSearch"
      :loading="loading"
      value-key="projectId"
      style="width: 100%"
  >
    <el-option
        v-for="item in projectList"
        :key="item.projectId"
        :label="item.projectName"
        :value="item"
    >
      <div class="option-container">
        <div class="main-line">
          <span class="name">{{ item.projectName }}</span>
          <el-tag size="small" type="info">{{ item.projectCode }}</el-tag>
        </div>
        <div class="sub-line">
          负责人：{{ item.ownerName || '-' }}
        </div>
      </div>
    </el-option>
  </el-select>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

export interface ProjectVO {
  projectId: number
  projectCode: string
  projectName: string
  ownerName?: string
  status?: number
}

const props = defineProps<{
  modelValue?: ProjectVO | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: ProjectVO | null): void
}>()

const innerValue = ref<ProjectVO | null>(props.modelValue || null)
const projectList = ref<ProjectVO[]>([])
const loading = ref(false)

// 同步外部值
watch(
    () => props.modelValue,
    val => {
      innerValue.value = val || null
    }
)

// 同步内部值
watch(innerValue, val => {
  emit('update:modelValue', val)
})

// 模拟远程搜索（这里替换成你的接口）
async function handleSearch(keyword: string) {
  loading.value = true

  try {
    // TODO: 替换成真实接口
    projectList.value = [
      {
        projectId: 1,
        projectCode: 'PRJ001',
        projectName: '智慧园区建设',
        ownerName: '张三'
      },
      {
        projectId: 2,
        projectCode: 'PRJ002',
        projectName: '城市更新改造',
        ownerName: '李四'
      }
    ].filter(p => p.projectName.includes(keyword))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.option-container {
  display: flex;
  flex-direction: column;
}

.main-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-weight: 500;
}

.sub-line {
  font-size: 12px;
  color: #999;
}
</style>