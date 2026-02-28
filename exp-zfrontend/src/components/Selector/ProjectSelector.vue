<template>
  <div>
    <!-- 输入框：回显已选项目的名称与编码 -->
    <el-input
        v-model="displayText"
        :placeholder="placeholder"
        readonly
        @click="openDialog"
        class="selector-input"
    >
      <template #suffix>
        <el-icon @click="openDialog" class="cursor-pointer">
          <Search />
        </el-icon>
      </template>
    </el-input>

    <!-- 弹窗（可拖动） -->
    <el-dialog
        v-model="dialogVisible"
        title="选择项目"
        width="1000px"
        destroy-on-close
        draggable
    >
      <!-- 搜索区域 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="项目名称">
            <el-input
                v-model="searchForm.projectName"
                placeholder="请输入项目名称"
                clearable
                style="width: 200px"
            />
          </el-form-item>

          <el-form-item label="项目编码">
            <el-input
                v-model="searchForm.projectCode"
                placeholder="请输入项目编码"
                clearable
                style="width: 160px"
            />
          </el-form-item>

          <el-form-item label="项目状态">
            <el-input
                v-model="searchForm.projectStatus"
                placeholder="请输入项目状态"
                clearable
                style="width: 160px"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              查询
            </el-button>
            <el-button @click="handleReset">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格：双击行可视为选中并确认（与现有交互一致） -->
      <el-table
          ref="tableRef"
          v-loading="loading"
          :data="tableData"
          border
          height="450px"
          row-key="projectId"
          highlight-current-row
          @row-click="handleRowClick"
      >
        <el-table-column prop="projectCode" label="项目编码" width="150" />
        <el-table-column prop="projectName" label="项目名称" min-width="240" />
        <el-table-column prop="projectStatus" label="项目状态" width="120" />
        <el-table-column prop="startDate" label="开始时间" width="160" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            background
            layout="total, prev, pager, next"
            :current-page="query.pageNum"
            :page-size="query.pageSize"
            :total="total"
            @current-change="handleCurrentChange"
        />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="handleConfirm"
            :disabled="!selectedProject"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { listProject } from '@/api/corpProject/project'
import type { ProjectVO } from '@/api/corpProject/project'
import { parsePageResult } from '@/api/common'

// 导出类型供父组件使用，保证 v-model 与回显数据结构一致
export type { ProjectVO }

/* ===============================
   Props / Emits
================================ */

interface Props {
  modelValue?: ProjectVO | null
  placeholder?: string
}

interface Emits {
  (e: 'update:modelValue', value: ProjectVO | undefined): void
  (e: 'change', value: ProjectVO | undefined): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择项目'
})

const emit = defineEmits<Emits>()

/* ===============================
   状态
================================ */

const dialogVisible = ref(false)
const loading = ref(false)
const tableRef = ref()
const tableData = ref<ProjectVO[]>([])
const total = ref(0)
const selectedProject = ref<ProjectVO | undefined>()

const query = reactive({
  pageNum: 1,
  pageSize: 10
})

const searchForm = reactive({
  projectName: '',
  projectCode: '',
  projectStatus: ''
})

/* ===============================
   显示文本（回显：有 projectId 时也支持仅部分字段回显）
================================ */

const displayText = computed(() => {
  const v = props.modelValue
  if (!v) return ''
  const name = v.projectName ?? ''
  const code = v.projectCode ?? ''
  return code ? `${name} (${code})` : name || '—'
})

/* ===============================
   打开弹窗
================================ */

function openDialog() {
  dialogVisible.value = true
  selectedProject.value = props.modelValue ?? undefined
  fetchProjectList()
}

/* ===============================
   列表请求（对接 corpProject 项目列表接口）
================================ */

async function fetchProjectList() {
  loading.value = true
  try {
    const res = await listProject({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      projectName: searchForm.projectName.trim() || undefined,
      projectCode: searchForm.projectCode.trim() || undefined,
      projectStatus: searchForm.projectStatus.trim() || undefined
    })
    const { list, total: t } = parsePageResult(res)
    tableData.value = list ?? []
    total.value = t ?? 0
    // 若有已选项目且在当前页，高亮该行
    nextTick(() => {
      if (selectedProject.value?.projectId != null) {
        const row = tableData.value.find(
          r => r.projectId === selectedProject.value!.projectId
        )
        if (row) tableRef.value?.setCurrentRow(row)
      }
    })
  } finally {
    loading.value = false
  }
}

/* ===============================
   搜索
================================ */

function handleSearch() {
  query.pageNum = 1
  fetchProjectList()
}

function handleReset() {
  searchForm.projectName = ''
  searchForm.projectCode = ''
  searchForm.projectStatus = ''
  query.pageNum = 1
  fetchProjectList()
}

/* ===============================
   行点击
================================ */

function handleRowClick(row: ProjectVO) {
  selectedProject.value = row
  tableRef.value?.setCurrentRow(row)
}

/* ===============================
   确认
================================ */

function handleConfirm() {
  if (!selectedProject.value) return
  emit('update:modelValue', selectedProject.value)
  emit('change', selectedProject.value)
  dialogVisible.value = false
}

/* ===============================
   分页
================================ */

function handleCurrentChange(page: number) {
  query.pageNum = page
  fetchProjectList()
}

watch(
  () => props.modelValue,
  val => {
    selectedProject.value = val ?? undefined
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.selector-input {
  cursor: pointer;
  :deep(.el-input__inner) {
    cursor: pointer;
  }
}
.cursor-pointer {
  cursor: pointer;
}
.search-bar {
  margin-bottom: 12px;
}
.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>