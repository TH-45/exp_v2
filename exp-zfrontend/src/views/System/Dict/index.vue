<template>
  <el-config-provider :locale="zhCn">
    <el-card class="dict-card">
      <template #header>
        <div class="header">
          <div class="title">字典管理</div>
          <div class="actions">
            <el-button size="small" :loading="exportingJson" @click="handleExportJson">导出 JSON</el-button>
            <el-button size="small" :loading="exportingExcel" @click="handleExportExcel">导出 Excel</el-button>
            <el-button size="small" @click="openImportJsonDialog">导入 JSON</el-button>
            <el-button size="small" @click="openImportExcelDialog">导入 Excel</el-button>
            <el-button
              v-if="activeTab === 'type'"
              type="primary"
              size="small"
              @click="openTypeCreate"
              :disabled="!canTypeCreate"
            >
              新增字典类型
            </el-button>
            <el-button
              v-else
              type="primary"
              size="small"
              @click="openItemCreate"
              :disabled="!canItemCreate"
            >
              新增字典项
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="字典类型管理" name="type">
          <!-- 查询区 -->
          <el-form :inline="true" :model="typeQuery" class="search-bar" @submit.prevent>
            <el-form-item label="类型编码">
              <el-input
                v-model="typeQuery.dictCode"
                placeholder="请输入类型编码"
                clearable
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item label="类型名称">
              <el-input
                v-model="typeQuery.dictName"
                placeholder="请如输入类型名称"
                clearable
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="typeQuery.status" clearable placeholder="全部" style="width: 160px">
                <el-option
                  v-for="opt in statusOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleTypeSearch">查询</el-button>
              <el-button @click="handleTypeReset">重置</el-button>
            </el-form-item>
            <el-form-item v-if="selectedTypes.length > 0" class="selected-tip">
              <span class="selected-count">已选 {{ selectedTypes.length }} 个类型</span>
            </el-form-item>
          </el-form>

          <!-- 列表区 -->
            <el-table
              v-loading="typeLoading"
              :data="typeTableData"
              row-key="id"
              border
              style="width: 100%"
              :row-class-name="typeTableRowClassName"
              @row-click="handleTypeRowClick"
              @row-dblclick="enterItemByRow"
            >
            <el-table-column prop="dictCode" label="类型编码" min-width="160" />
            <el-table-column prop="dictName" label="字典名称" min-width="160" />
            <el-table-column prop="description" label="描述" min-width="220" />
            <el-table-column label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedTime" label="更新时间" min-width="170" :formatter="formatDateTime" />
            <el-table-column label="操作" fixed="right" width="240">
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  size="small"
                  @click="openTypeEdit(row)"
                  :disabled="!canTypeUpdate"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="primary"
                  size="small"
                  @click="enterItemByRow(row)"
                  :disabled="!canItemView"
                >
                  设置字典项
                </el-button>
                <el-button
                  link
                  size="small"
                  @click="toggleTypeStatus(row)"
                  :disabled="!canTypeStatus"
                >
                  {{ row.status === 'ENABLED' ? '停用' : '启用' }}
                </el-button>
                <el-button
                  link
                  type="danger"
                  size="small"
                  @click="handleTypeDelete(row)"
                  :disabled="!canTypeDelete"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              background
              layout="total, prev, pager, next, sizes"
              :current-page="typeQuery.page"
              :page-size="typeQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="typeTotal"
              @current-change="handleTypeCurrentChange"
              @size-change="handleTypeSizeChange"
            />
          </div>

          <!-- 新增/编辑字典类型 -->
          <el-dialog
            v-model="typeDialog.visible"
            :title="typeDialog.isEdit ? '编辑字典类型' : '新增字典类型'"
            width="720px"
            destroy-on-close
          >
            <el-form
              ref="typeFormRef"
              :model="typeForm"
              :rules="typeRules"
              label-width="110px"
              class="dialog-form two-col"
              @submit.prevent="submitTypeForm"
            >
              <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
              <el-form-item label="类型编码" prop="dictCode">
                <el-input v-model="typeForm.dictCode" placeholder="如：USER_STATUS" />
              </el-form-item>
              <el-form-item label="类型名称" prop="dictName">
                <el-input v-model="typeForm.dictName" placeholder="如：用户状态" />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="typeForm.status" placeholder="请选择">
                  <el-option
                    v-for="opt in statusOptions"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="描述" class="full-row">
                <el-input v-model="typeForm.description" type="textarea" :rows="3" placeholder="可选" />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="typeDialog.visible = false">取消</el-button>
              <el-button type="primary" :loading="saving" @click="submitTypeForm">保存</el-button>
            </template>
          </el-dialog>
        </el-tab-pane>

        <el-tab-pane label="字典项管理" name="item" disabled>
          <!-- 查询区 -->
          <el-form :inline="true" :model="itemQuery" class="search-bar" @submit.prevent>
            <el-form-item label="字典类型">
              <el-select
                v-model="itemQuery.dictCode"
                clearable
                filterable
                placeholder="请选择字典类型"
                style="width: 220px"
                @change="handleItemTypeChange"
              >
                <el-option
                  v-for="opt in typeOptions"
                  :key="opt.dictCode"
                  :label="`${opt.dictName}（${opt.dictCode}）`"
                  :value="opt.dictCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input
                v-model="itemQuery.keyword"
                placeholder="名称/值/编码"
                clearable
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="itemQuery.status" clearable placeholder="全部" style="width: 160px">
                <el-option
                  v-for="opt in statusOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleItemSearch">查询</el-button>
              <el-button @click="handleItemReset">重置</el-button>
            </el-form-item>
            <el-form-item v-if="selectedTypes.length > 0" class="selected-tip">
              <span class="selected-count">已选 {{ selectedTypes.length }} 个类型</span>
            </el-form-item>
            <el-form-item v-else-if="selectedItems.length > 0" class="selected-tip">
              <span class="selected-count">已选 {{ selectedItems.length }} 条字典项</span>
            </el-form-item>
          </el-form>

          <!-- 列表区 -->
          <el-table
            ref="itemTableRef"
            v-loading="itemLoading"
            :data="itemTableData"
            row-key="id"
            border
            style="width: 100%"
            :row-class-name="itemTableRowClassName"
            @row-click="handleItemRowClick"
            @row-dblclick="handleItemRowDblClick"
          >
            <el-table-column prop="itemCode" label="字典项编码" min-width="160" />
            <el-table-column prop="itemValue" label="字典项值" min-width="140" />
            <el-table-column prop="itemLabel" label="字典项名称" min-width="160" />
            <el-table-column prop="sortNo" label="排序" min-width="90" />
            <el-table-column label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="200" />
            <el-table-column prop="updatedTime" label="更新时间" min-width="170" :formatter="formatDateTime" />
            <el-table-column label="操作" fixed="right" width="240">
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  size="small"
                  @click="openItemEdit(row)"
                  :disabled="!canItemUpdate"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  size="small"
                  @click="toggleItemStatus(row)"
                  :disabled="!canItemStatus"
                >
                  {{ row.status === 'ENABLED' ? '停用' : '启用' }}
                </el-button>
                <el-button
                  link
                  type="danger"
                  size="small"
                  @click="handleItemDelete(row)"
                  :disabled="!canItemDelete"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              background
              layout="total, prev, pager, next, sizes"
              :current-page="itemQuery.page"
              :page-size="itemQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="itemTotal"
              @current-change="handleItemCurrentChange"
              @size-change="handleItemSizeChange"
            />
          </div>

          <!-- 新增/编辑字典项 -->
          <el-dialog
            v-model="itemDialog.visible"
            :title="itemDialog.isEdit ? '编辑字典项' : '新增字典项'"
            width="760px"
            destroy-on-close
            draggable
          >
            <el-form
              ref="itemFormRef"
              :model="itemForm"
              :rules="itemRules"
              label-width="110px"
              class="dialog-form two-col"
              @submit.prevent="submitItemForm"
            >
              <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
              <el-form-item label="字典类型">
                <el-input :model-value="selectedTypeLabel" disabled />
              </el-form-item>
              <el-form-item label="字典项编码" prop="itemCode">
                <el-input v-model="itemForm.itemCode" placeholder="可选" />
              </el-form-item>
              <el-form-item label="字典项值" prop="itemValue">
                <el-input v-model="itemForm.itemValue" placeholder="如：1" />
              </el-form-item>
              <el-form-item label="字典项名称" prop="itemLabel">
                <el-input v-model="itemForm.itemLabel" placeholder="如：启用" />
              </el-form-item>
              <el-form-item label="排序">
                <el-input-number v-model="itemForm.sortNo" :min="0" :max="9999" />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="itemForm.status" placeholder="请选择">
                  <el-option
                    v-for="opt in statusOptions"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="备注" class="full-row">
                <el-input v-model="itemForm.remark" type="textarea" :rows="3" placeholder="可选" @keydown.enter.stop />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="itemDialog.visible = false">取消</el-button>
              <el-button type="primary" :loading="saving" @click="submitItemForm">保存</el-button>
            </template>
          </el-dialog>

        </el-tab-pane>
      </el-tabs>

      <!-- 导入 JSON 弹窗 -->
      <el-dialog
        v-model="importJsonDialog.visible"
        title="导入 JSON"
        width="560px"
        destroy-on-close
        draggable
      >
        <el-input
          v-model="importJsonDialog.content"
          type="textarea"
          :rows="12"
          placeholder='请粘贴 JSON 数组，格式如：[{"dictCode":"USER_STATUS","itemCode":"ENABLED","itemValue":"1","itemLabel":"启用","sortNo":1,"status":"ENABLED","remark":""}]'
        />
        <template #footer>
          <el-button @click="importJsonDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="importing" @click="submitImportJson">确定导入</el-button>
        </template>
      </el-dialog>

      <!-- 导入 Excel 弹窗 -->
      <el-dialog
        v-model="importExcelDialog.visible"
        title="导入 Excel"
        width="480px"
        destroy-on-close
        draggable
      >
        <el-upload
          ref="importExcelRef"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx"
          :on-change="onImportExcelChange"
          :on-remove="onImportExcelRemove"
        >
          <el-button type="primary" size="small">选择 .xlsx 文件</el-button>
        </el-upload>
        <div class="import-tip">支持 .xlsx 格式，列：字典类型编码、字典项编码、字典项值、字典项名称、排序、状态、备注</div>
        <template #footer>
          <el-button @click="importExcelDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="importing" :disabled="!importExcelDialog.file" @click="submitImportExcel">确定导入</el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import {
  createDictItem,
  createDictType,
  deleteDictItem,
  deleteDictType,
  downloadDictExcelHierarchyBlob,
  getDictTypeDetail,
  importDictItemsExcel,
  importDictItemsJson,
  listAllDictItems,
  listAllDictItemsForExport,
  listDictItems,
  listDictTypes,
  setDictItemStatus,
  setDictTypeStatus,
  updateDictItem,
  updateDictType,
  type DictItem,
  type DictItemImportRow,
  type DictStatus,
  type DictType,
  type PageResult,
} from '@/api/system/dict';

const activeTab = ref<'type' | 'item'>('type');
const route = useRoute();
const router = useRouter();

const statusOptions = [
  { label: '启用', value: 'ENABLED' as DictStatus },
  { label: '停用', value: 'DISABLED' as DictStatus },
];

const typeLoading = ref(false);
const itemLoading = ref(false);
const saving = ref(false);
const exportingJson = ref(false);
const exportingExcel = ref(false);
const importing = ref(false);

const typeQuery = reactive({
  page: 1,
  pageSize: 10,
  dictCode: '',
  dictName: '',
  status: undefined as DictStatus | undefined,
});

const itemQuery = reactive({
  dictCode: '',
  page: 1,
  pageSize: 10,
  keyword: '',
  status: undefined as DictStatus | undefined,
});

const typeTableData = ref<DictType[]>([]);
const itemTableData = ref<DictItem[]>([]);
const typeTotal = ref(0);
const itemTotal = ref(0);
const typeOptions = ref<DictType[]>([]);

const typeDialog = reactive({
  visible: false,
  isEdit: false,
});

const itemDialog = reactive({
  visible: false,
  isEdit: false,
});

const importJsonDialog = reactive({
  visible: false,
  content: '',
});

const importExcelDialog = reactive({
  visible: false,
  file: null as File | null,
});

const importExcelRef = ref();
const itemTableRef = ref();
/** 字典类型表格选中的行（单击选中/取消，支持多选） */
const selectedTypes = ref<DictType[]>([]);
/** 字典项表格选中的行（单击选中/取消，支持多选） */
const selectedItems = ref<DictItem[]>([]);

const typeFormRef = ref<FormInstance>();
const itemFormRef = ref<FormInstance>();

const typeForm = reactive<Partial<DictType>>({
  id: undefined,
  dictCode: '',
  dictName: '',
  description: '',
  status: 'ENABLED',
});

const itemForm = reactive<Partial<DictItem>>({
  id: undefined,
  dictCode: '',
  itemCode: '',
  itemValue: '',
  itemLabel: '',
  sortNo: 0,
  status: 'ENABLED',
  remark: '',
});

const typeRules: FormRules = {
  dictCode: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

const itemRules: FormRules = {
  itemCode: [{ required: true, message: '请输入字典项编码', trigger: 'blur' }],
  itemValue: [{ required: true, message: '请输入字典项值', trigger: 'blur' }],
  itemLabel: [{ required: true, message: '请输入字典项名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

const canTypeCreate = computed(() => hasPermission('system:dict:type:create'));
const canTypeUpdate = computed(() => hasPermission('system:dict:type:update'));
const canTypeDelete = computed(() => hasPermission('system:dict:type:delete'));
const canTypeStatus = computed(() => hasPermission('system:dict:type:status'));
const canItemView = computed(() => hasPermission('system:dict:view'));
const canItemCreate = computed(() => hasPermission('system:dict:item:create'));
const canItemUpdate = computed(() => hasPermission('system:dict:item:update'));
const canItemDelete = computed(() => hasPermission('system:dict:item:delete'));
const canItemStatus = computed(() => hasPermission('system:dict:item:status'));

const selectedTypeLabel = computed(() => {
  if (!itemForm.dictCode) return '';
  const found = typeOptions.value.find((x) => x.dictCode === itemForm.dictCode);
  return found ? `${found.dictName}（${found.dictCode}）` : itemForm.dictCode;
});

const mockTypes: DictType[] = [
  {
    id: 1,
    dictCode: 'USER_STATUS',
    dictName: '用户状态',
    description: '用户启用/停用',
    status: 'ENABLED',
    updatedTime: '2025-01-02 10:00:00',
  },
  {
    id: 2,
    dictCode: 'ORG_LEVEL',
    dictName: '组织级别',
    description: '组织级别示例',
    status: 'ENABLED',
    updatedTime: '2025-01-02 10:00:00',
  },
];

const mockItems: Record<string, DictItem[]> = {
  USER_STATUS: [
    {
      id: 11,
      dictCode: 'USER_STATUS',
      itemCode: 'ENABLED',
      itemValue: '1',
      itemLabel: '启用',
      sortNo: 1,
      status: 'ENABLED',
      remark: '正常可用',
      updatedTime: '2025-01-02 10:00:00',
    },
    {
      id: 12,
      dictCode: 'USER_STATUS',
      itemCode: 'DISABLED',
      itemValue: '0',
      itemLabel: '停用',
      sortNo: 2,
      status: 'DISABLED',
      remark: '停用示例',
      updatedTime: '2025-01-02 10:00:00',
    },
  ],
};

const TAB_QUERY_KEY = 'tab';
const DICT_CODE_QUERY_KEY = 'dictCode';

onMounted(() => {
  fetchTypeList();
  fetchTypeOptions();
});

watch(
  () => [route.query[TAB_QUERY_KEY], route.query[DICT_CODE_QUERY_KEY]],
  () => {
    void syncTabByRouteQuery();
  },
  { immediate: true },
);

function statusText(status?: DictStatus) {
  return status === 'DISABLED' ? '停用' : '启用';
}

function statusTagType(status?: DictStatus) {
  return status === 'DISABLED' ? 'info' : 'success';
}

function formatDateTime(row: DictType | DictItem, column: any, cellValue: string) {
  if (!cellValue) return '';
  try {
    const date = new Date(cellValue);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  } catch (e) {
    return cellValue;
  }
}

function normalizePage<T>(res?: PageResult<T>) {
  const list = (res?.records || res?.list || res?.rows || []) as T[];
  const total = Number(res?.total ?? list.length) || 0;
  return { list, total };
}

async function fetchTypeOptions() {
  try {
    const res = await listDictTypes({ page: 1, pageSize: 200 });
    typeOptions.value = normalizePage(res).list;
  } catch {
    // 无后端时的兜底数据
    typeOptions.value = mockTypes;
  }
}

async function fetchTypeList() {
  typeLoading.value = true;
  try {
    const res = await listDictTypes({
      page: typeQuery.page,
      pageSize: typeQuery.pageSize,
      dictCode: typeQuery.dictCode || undefined,
      dictName: typeQuery.dictName || undefined,
      status: typeQuery.status,
    });
    const { list, total } = normalizePage(res);
    typeTableData.value = list;
    typeTotal.value = total;
  } catch {
    // 无后端时的兜底数据
    const code = typeQuery.dictCode.trim();
    const name = typeQuery.dictName.trim();
    const filtered = mockTypes.filter((x) => {
      if (typeQuery.status && x.status !== typeQuery.status) return false;
      if (code && !x.dictCode.includes(code)) return false;
      if (name && !x.dictName.includes(name)) return false;
      return true;
    });
    typeTableData.value = filtered;
    typeTotal.value = filtered.length;
  } finally {
    typeLoading.value = false;
  }
}

async function fetchItemList() {
  if (!itemQuery.dictCode) {
    itemTableData.value = [];
    itemTotal.value = 0;
    return;
  }
  itemLoading.value = true;
  try {
    const res = await listDictItems({
      dictCode: itemQuery.dictCode,
      page: itemQuery.page,
      pageSize: itemQuery.pageSize,
      keyword: itemQuery.keyword || undefined,
      status: itemQuery.status,
    });
    const { list, total } = normalizePage(res);
    itemTableData.value = list;
    itemTotal.value = total;
  } catch {
    // 无后端时的兜底数据
    const base = mockItems[itemQuery.dictCode] || [];
    const kw = itemQuery.keyword.trim();
    const filtered = base.filter((x) => {
      if (itemQuery.status && x.status !== itemQuery.status) return false;
      if (!kw) return true;
      return (
        (x.itemLabel || '').includes(kw) ||
        (x.itemValue || '').includes(kw) ||
        (x.itemCode || '').includes(kw)
      );
    });
    itemTableData.value = filtered;
    itemTotal.value = filtered.length;
  } finally {
    itemLoading.value = false;
  }
}

function handleTypeSearch() {
  typeQuery.dictCode = typeQuery.dictCode.trim();
  typeQuery.dictName = typeQuery.dictName.trim();
  typeQuery.page = 1;
  selectedTypes.value = [];
  fetchTypeList();
}

function handleTypeReset() {
  typeQuery.dictCode = '';
  typeQuery.dictName = '';
  typeQuery.status = undefined;
  typeQuery.page = 1;
  selectedTypes.value = [];
  fetchTypeList();
}

function handleTypeCurrentChange(page: number) {
  typeQuery.page = page;
  fetchTypeList();
}

function handleTypeSizeChange(size: number) {
  typeQuery.pageSize = size;
  typeQuery.page = 1;
  fetchTypeList();
}

function handleItemSearch() {
  itemQuery.keyword = itemQuery.keyword.trim();
  itemQuery.page = 1;
  selectedItems.value = [];
  if (!itemQuery.dictCode) {
    ElMessage.warning('请先选择字典类型');
    return;
  }
  fetchItemList();
}

function handleItemReset() {
  itemQuery.keyword = '';
  itemQuery.status = undefined;
  itemQuery.page = 1;
  selectedItems.value = [];
  if (itemQuery.dictCode) fetchItemList();
}

function handleItemCurrentChange(page: number) {
  itemQuery.page = page;
  fetchItemList();
}

function handleItemSizeChange(size: number) {
  itemQuery.pageSize = size;
  itemQuery.page = 1;
  fetchItemList();
}

function handleItemTypeChange() {
  itemQuery.page = 1;
  selectedItems.value = [];
  if (itemQuery.dictCode) {
    replaceItemRouteQuery(itemQuery.dictCode);
  } else {
    replaceItemRouteQuery();
    itemTableData.value = [];
    itemTotal.value = 0;
  }
}

function resetTypeForm() {
  typeForm.id = undefined;
  typeForm.dictCode = '';
  typeForm.dictName = '';
  typeForm.description = '';
  typeForm.status = 'ENABLED';
}

function resetItemForm() {
  itemForm.id = undefined;
  itemForm.dictCode = '';
  itemForm.itemCode = '';
  itemForm.itemValue = '';
  itemForm.itemLabel = '';
  itemForm.sortNo = 0;
  itemForm.status = 'ENABLED';
  itemForm.remark = '';
}

function openTypeCreate() {
  typeDialog.isEdit = false;
  resetTypeForm();
  typeDialog.visible = true;
}

function openTypeEdit(row: DictType) {
  typeDialog.isEdit = true;
  Object.assign(typeForm, row);
  typeDialog.visible = true;
}

function enterItemByRow(row?: DictType) {
  if (!row?.dictCode) return;
  pushItemRouteQuery(row.dictCode);
}

function buildItemRouteQuery(dictCode?: string) {
  const query = { ...route.query } as Record<string, unknown>;
  query[TAB_QUERY_KEY] = 'item';
  if (dictCode) {
    query[DICT_CODE_QUERY_KEY] = dictCode;
  } else {
    delete query[DICT_CODE_QUERY_KEY];
  }
  return query;
}

function pushItemRouteQuery(dictCode: string) {
  router.push({
    path: route.path,
    query: buildItemRouteQuery(dictCode),
  });
}

function replaceItemRouteQuery(dictCode?: string) {
  router.replace({
    path: route.path,
    query: buildItemRouteQuery(dictCode),
  });
}

async function syncTabByRouteQuery() {
  const tabQuery = route.query[TAB_QUERY_KEY];
  const dictCodeQuery = route.query[DICT_CODE_QUERY_KEY];
  const tab = tabQuery === 'item' ? 'item' : 'type';
  const dictCode = typeof dictCodeQuery === 'string' ? dictCodeQuery : '';

  if (tab === 'type') {
    activeTab.value = 'type';
    return;
  }

  activeTab.value = 'item';
  if (!typeOptions.value.length) {
    await fetchTypeOptions();
  }

  if (itemQuery.dictCode !== dictCode) {
    itemQuery.dictCode = dictCode;
    itemQuery.keyword = '';
    itemQuery.status = undefined;
    itemQuery.page = 1;
  }

  if (!dictCode) {
    itemTableData.value = [];
    itemTotal.value = 0;
    return;
  }
  fetchItemList();
}

function triggerDownload(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

async function copyToClipboard(text: string) {
  if (navigator?.clipboard?.writeText) {
    await navigator.clipboard.writeText(text);
    return;
  }
  const textarea = document.createElement('textarea');
  textarea.value = text;
  textarea.style.position = 'fixed';
  textarea.style.left = '-9999px';
  document.body.appendChild(textarea);
  textarea.select();
  document.execCommand('copy');
  document.body.removeChild(textarea);
}

/** 构建父子结构 [{ dictType, items }, ...] */
function buildExportHierarchy(
  types: DictType[],
  itemsByDictCode: (code: string) => Promise<DictItem[]>,
): Promise<Array<{ dictType: DictType; items: DictItem[] }>> {
  return Promise.all(
    types.map(async (t) => {
      const items = await itemsByDictCode(t.dictCode);
      const list = Array.isArray(items) ? items : (items as { data?: DictItem[] })?.data ?? [];
      return { dictType: t, items: list };
    }),
  );
}

/** JSON 字段说明（追加在导出内容下方） */
const JSON_FIELD_DESC = `

------------------------------------------
【字段说明】
dictType（字典类型）：
  - id：主键ID 数据表自动插入
  - dictCode：字典类型编码（全局唯一）
  - dictName：字典类型名称
  - description：描述
  - status：状态（ENABLED-启用，DISABLED-停用）

items（字典项）：
  - id：主键ID 数据表自动插入
  - dictCode：所属字典类型编码
  - itemCode：字典项编码
  - itemValue：字典项值（业务表存储值）
  - itemLabel：字典项名称
  - sortNo：排序号
  - status：状态（ENABLED-启用，DISABLED-停用）
  - remark：备注
`;

/** 导出 JSON：父子结构 + 底部字段说明 */
async function handleExportJson() {
  exportingJson.value = true;
  try {
    let hierarchy: Array<{ dictType: DictType; items: DictItem[] }>;
    if (selectedTypes.value.length > 0) {
      hierarchy = await buildExportHierarchy(selectedTypes.value, listAllDictItems);
      ElMessage.success(`已复制 ${selectedTypes.value.length} 个类型及其 ${hierarchy.reduce((s, h) => s + h.items.length, 0)} 条字典项`);
    } else if (selectedItems.value.length > 0) {
      const dictCodes = [...new Set(selectedItems.value.map((i) => i.dictCode))];
      const typeMap = new Map<string, DictType>();
      for (const t of typeOptions.value) {
        typeMap.set(t.dictCode, t);
      }
      const typesToFetch = dictCodes.filter((c) => !typeMap.has(c));
      for (const code of typesToFetch) {
        const detail = await getDictTypeDetail({ dictCode: code });
        if (detail && typeof detail === 'object' && 'dictCode' in detail) {
          typeMap.set(code, detail as DictType);
        }
      }
      const types = dictCodes.map((c) => typeMap.get(c)).filter(Boolean) as DictType[];
      hierarchy = types.map((t) => ({
        dictType: t,
        items: selectedItems.value.filter((i) => i.dictCode === t.dictCode),
      }));
      ElMessage.success(`已复制 ${hierarchy.length} 个类型及其 ${selectedItems.value.length} 条字典项`);
    } else {
      const list = await listAllDictItemsForExport();
      const allItems = Array.isArray(list) ? list : (list as { data?: DictItem[] })?.data ?? [];
      const typeRes = await listDictTypes({ page: 1, pageSize: 999 });
      const typeList = normalizePage(typeRes).list;
      const dictCodes = [...new Set(allItems.map((i) => i.dictCode))];
      hierarchy = dictCodes.map((code) => {
        const t = typeList.find((x) => x.dictCode === code) || { id: 0, dictCode: code, dictName: code, status: 'ENABLED' } as DictType;
        return { dictType: t, items: allItems.filter((i) => i.dictCode === code) };
      });
      ElMessage.success('已复制到剪贴板');
    }
    const jsonStr = JSON.stringify(hierarchy, null, 2) + JSON_FIELD_DESC;
    await copyToClipboard(jsonStr);
  } catch {
    ElMessage.error('复制失败');
  } finally {
    exportingJson.value = false;
  }
}

/** 导出 Excel：父子结构 + 类型合并行 + 字段说明 */
async function handleExportExcel() {
  exportingExcel.value = true;
  try {
    let hierarchy: Array<{ dictType: DictType; items: DictItem[] }>;
    if (selectedTypes.value.length > 0) {
      hierarchy = await buildExportHierarchy(selectedTypes.value, listAllDictItems);
      const blob = await downloadDictExcelHierarchyBlob(hierarchy);
      triggerDownload(blob, `字典项_${Date.now()}.xlsx`);
      ElMessage.success(`已导出 ${selectedTypes.value.length} 个类型及其 ${hierarchy.reduce((s, h) => s + h.items.length, 0)} 条字典项`);
    } else if (selectedItems.value.length > 0) {
      const dictCodes = [...new Set(selectedItems.value.map((i) => i.dictCode))];
      const typeMap = new Map<string, DictType>();
      for (const t of typeOptions.value) {
        typeMap.set(t.dictCode, t);
      }
      for (const code of dictCodes.filter((c) => !typeMap.has(c))) {
        try {
          const detail = await getDictTypeDetail({ dictCode: code });
          if (detail && typeof detail === 'object' && 'dictCode' in detail) {
            typeMap.set(code, detail as DictType);
          }
        } catch {
          typeMap.set(code, { id: 0, dictCode: code, dictName: code, status: 'ENABLED' } as DictType);
        }
      }
      hierarchy = dictCodes.map((c) => ({
        dictType: typeMap.get(c)!,
        items: selectedItems.value.filter((i) => i.dictCode === c),
      }));
      const blob = await downloadDictExcelHierarchyBlob(hierarchy);
      triggerDownload(blob, `字典项_${Date.now()}.xlsx`);
      ElMessage.success(`已导出 ${hierarchy.length} 个类型及其 ${selectedItems.value.length} 条字典项`);
    } else {
      const list = await listAllDictItemsForExport();
      const allItems = Array.isArray(list) ? list : (list as { data?: DictItem[] })?.data ?? [];
      const typeRes = await listDictTypes({ page: 1, pageSize: 999 });
      const typeList = normalizePage(typeRes).list;
      const dictCodes = [...new Set(allItems.map((i) => i.dictCode))];
      hierarchy = dictCodes.map((code) => {
        const t = typeList.find((x) => x.dictCode === code) || ({ id: 0, dictCode: code, dictName: code, status: 'ENABLED' } as DictType);
        return { dictType: t, items: allItems.filter((i) => i.dictCode === code) };
      });
      const blob = await downloadDictExcelHierarchyBlob(hierarchy);
      triggerDownload(blob, `字典项_${Date.now()}.xlsx`);
      ElMessage.success('导出成功');
    }
  } catch {
    ElMessage.error('导出失败');
  } finally {
    exportingExcel.value = false;
  }
}

/** 打开导入 JSON 弹窗：有选中项则预填选中项，便于编辑后导入 */
function openImportJsonDialog() {
  if (activeTab.value === 'item' && selectedItems.value.length > 0) {
    importJsonDialog.content = JSON.stringify(
      selectedItems.value.map((x) => ({
        dictCode: x.dictCode,
        itemCode: x.itemCode,
        itemValue: x.itemValue,
        itemLabel: x.itemLabel,
        sortNo: x.sortNo,
        status: x.status,
        remark: x.remark,
      })),
      null,
      2,
    );
  } else {
    importJsonDialog.content = '';
  }
  importJsonDialog.visible = true;
}

async function submitImportJson() {
  const content = importJsonDialog.content.trim();
  if (!content) {
    ElMessage.warning('请粘贴 JSON 内容');
    return;
  }
  let rows: DictItemImportRow[];
  try {
    rows = JSON.parse(content);
  } catch {
    ElMessage.error('JSON 格式错误');
    return;
  }
  if (!Array.isArray(rows)) {
    ElMessage.error('请提供 JSON 数组格式');
    return;
  }
  importing.value = true;
  try {
    const res = await importDictItemsJson(rows);
    const msg = `导入完成：成功 ${res.successCount} 条，失败 ${res.failCount} 条`;
    if (res.failCount > 0 && res.errors?.length) {
      ElMessage.warning(msg + '\n' + res.errors.slice(0, 5).join('\n') + (res.errors.length > 5 ? '\n...' : ''));
    } else {
      ElMessage.success(msg);
    }
    importJsonDialog.visible = false;
    fetchTypeOptions();
    if (itemQuery.dictCode) fetchItemList();
  } catch {
    ElMessage.error('导入失败');
  } finally {
    importing.value = false;
  }
}

function openImportExcelDialog() {
  importExcelDialog.file = null;
  importExcelDialog.visible = true;
}

function onImportExcelChange(file: { raw?: File }) {
  importExcelDialog.file = file?.raw ?? null;
}

function onImportExcelRemove() {
  importExcelDialog.file = null;
}

async function submitImportExcel() {
  const file = importExcelDialog.file;
  if (!file) {
    ElMessage.warning('请选择文件');
    return;
  }
  importing.value = true;
  try {
    const res = await importDictItemsExcel(file);
    const msg = `导入完成：成功 ${res.successCount} 条，失败 ${res.failCount} 条`;
    if (res.failCount > 0 && res.errors?.length) {
      ElMessage.warning(msg + '\n' + res.errors.slice(0, 5).join('\n') + (res.errors.length > 5 ? '\n...' : ''));
    } else {
      ElMessage.success(msg);
    }
    importExcelDialog.visible = false;
    importExcelDialog.file = null;
    fetchTypeOptions();
    if (itemQuery.dictCode) fetchItemList();
  } catch {
    ElMessage.error('导入失败');
  } finally {
    importing.value = false;
  }
}

function openItemCreate() {
  if (!itemQuery.dictCode) {
    ElMessage.warning('请先选择字典类型');
    return;
  }
  itemDialog.isEdit = false;
  resetItemForm();
  itemForm.dictCode = itemQuery.dictCode;
  const sortValues = itemTableData.value.map((item) => Number(item.sortNo ?? 0));
  itemForm.sortNo = sortValues.length ? Math.max(...sortValues) + 1 : 0;
  itemDialog.visible = true;
}

function openItemEdit(row: DictItem) {
  itemDialog.isEdit = true;
  Object.assign(itemForm, row);
  itemDialog.visible = true;
}

function handleItemRowDblClick(row: DictItem) {
  if (!canItemUpdate.value) return;
  openItemEdit(row);
}

/** 单击行：选中/取消，忽略按钮区域点击 */
function handleTypeRowClick(row: DictType, _column: unknown, event: Event) {
  if ((event.target as HTMLElement).closest('.el-button') || (event.target as HTMLElement).closest('.el-link')) return;
  const idx = selectedTypes.value.findIndex((s) => s.id === row.id);
  if (idx >= 0) {
    selectedTypes.value = selectedTypes.value.filter((s) => s.id !== row.id);
  } else {
    selectedTypes.value = [...selectedTypes.value, row];
  }
}

function handleItemRowClick(row: DictItem, _column: unknown, event: Event) {
  if ((event.target as HTMLElement).closest('.el-button') || (event.target as HTMLElement).closest('.el-link')) return;
  const idx = selectedItems.value.findIndex((s) => s.id === row.id);
  if (idx >= 0) {
    selectedItems.value = selectedItems.value.filter((s) => s.id !== row.id);
  } else {
    selectedItems.value = [...selectedItems.value, row];
  }
}

/** 字典类型选中行高亮 */
function typeTableRowClassName({ row }: { row: DictType }) {
  return selectedTypes.value.some((s) => s.id === row.id) ? 'dict-row-selected' : '';
}

/** 字典项选中行高亮 */
function itemTableRowClassName({ row }: { row: DictItem }) {
  return selectedItems.value.some((s) => s.id === row.id) ? 'dict-row-selected' : '';
}

async function submitTypeForm() {
  if (!typeFormRef.value) return;
  const valid = await typeFormRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
    if (typeDialog.isEdit) {
      await updateDictType(typeForm);
      ElMessage.success('编辑成功');
    } else {
      await createDictType(typeForm);
      ElMessage.success('新增成功');
    }
    typeDialog.visible = false;
    fetchTypeList();
    fetchTypeOptions();
  } catch {
    // 示例模式：无接口时也能体验交互
    ElMessage.success(typeDialog.isEdit ? '已保存（示例模式）' : '已新增（示例模式）');
    typeDialog.visible = false;
    fetchTypeList();
    fetchTypeOptions();
  } finally {
    saving.value = false;
  }
}

async function submitItemForm() {
  if (!itemFormRef.value) return;
  const valid = await itemFormRef.value.validate();
  if (!valid) return;
  saving.value = true;
  try {
    if (itemDialog.isEdit) {
      await updateDictItem(itemForm);
      ElMessage.success('编辑成功');
    } else {
      await createDictItem(itemForm);
      ElMessage.success('新增成功');
    }
    itemDialog.visible = false;
    fetchItemList();
  } catch {
    // 示例模式：无接口时也能体验交互
    ElMessage.success(itemDialog.isEdit ? '已保存（示例模式）' : '已新增（示例模式）');
    itemDialog.visible = false;
    fetchItemList();
  } finally {
    saving.value = false;
  }
}

function handleTypeDelete(row: DictType) {
  if (!canTypeDelete.value) return;
  ElMessageBox.confirm(`确认删除「${row.dictName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteDictType(row.id);
        ElMessage.success('删除成功');
        fetchTypeList();
        fetchTypeOptions();
      } catch {
        // 示例模式：前端移除
        typeTableData.value = typeTableData.value.filter((x) => x.id !== row.id);
        typeTotal.value = typeTableData.value.length;
        typeOptions.value = typeOptions.value.filter((x) => x.id !== row.id);
        ElMessage.success('已删除（示例模式）');
      }
    })
    .catch(() => {});
}

function handleItemDelete(row: DictItem) {
  if (!canItemDelete.value) return;
  ElMessageBox.confirm(`确认删除「${row.itemLabel}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteDictItem(row.id);
        ElMessage.success('删除成功');
        fetchItemList();
      } catch {
        // 示例模式：前端移除
        itemTableData.value = itemTableData.value.filter((x) => x.id !== row.id);
        itemTotal.value = itemTableData.value.length;
        ElMessage.success('已删除（示例模式）');
      }
    })
    .catch(() => {});
}

function toggleTypeStatus(row: DictType) {
  if (!canTypeStatus.value) return;
  const next: DictStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
  setDictTypeStatus(row.id, next)
    .then(() => {
      ElMessage.success('状态已更新');
      fetchTypeList();
      fetchTypeOptions();
    })
    .catch(() => {
      row.status = next;
    });
}

function toggleItemStatus(row: DictItem) {
  if (!canItemStatus.value) return;
  const next: DictStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
  setDictItemStatus(row.id, next)
    .then(() => {
      ElMessage.success('状态已更新');
      fetchItemList();
    })
    .catch(() => {
      row.status = next;
    });
}
</script>

<style scoped lang="scss">
.dict-card {
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .title {
    font-weight: 600;
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .search-bar {
    margin-bottom: 12px;
  }

  .pagination {
    margin-top: 12px;
    display: flex;
    justify-content: flex-end;
  }

  .dialog-form.two-col {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    column-gap: 16px;
    row-gap: 12px;
  }

  .dialog-form.two-col :deep(.el-form-item) {
    margin-bottom: 0;
  }

  .dialog-form.two-col .full-row {
    grid-column: 1 / span 2;
  }

  .import-tip {
    margin-top: 12px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .selected-tip .selected-count {
    color: var(--el-color-primary);
    font-weight: 500;
  }

  /* 字典类型/字典项选中行高亮（单击选中，无多选框） */
  :deep(.dict-row-selected) {
    background-color: var(--el-color-primary-light-9) !important;
  }
  :deep(.dict-row-selected:hover) {
    background-color: var(--el-color-primary-light-8) !important;
  }
}
</style>
