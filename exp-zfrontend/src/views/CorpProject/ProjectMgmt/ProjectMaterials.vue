<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">项目物料管理 - {{ projectName }}</div>
          <div class="actions">
            <el-button
              type="primary"
              size="small"
              @click="openAddMaterialDialog"
              :disabled="!canUpdate"
            >
              <el-icon><Plus /></el-icon>
              添加物料
            </el-button>
            <el-button size="small" @click="exportMaterials">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 物料统计 -->
      <div class="material-stats">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-card class="stat-card total-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon size="32" color="#409EFF"><Box /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ materialStats.total }}</div>
                  <div class="stat-label">总物料种类</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card normal-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon size="32" color="#67C23A"><Check /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ materialStats.total - materialStats.lowStock - materialStats.outOfStock }}</div>
                  <div class="stat-label">库存正常</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card warning-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon size="32" color="#E6A23C"><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ materialStats.lowStock }}</div>
                  <div class="stat-label">库存预警</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card danger-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon size="32" color="#F56C6C"><Close /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ materialStats.outOfStock }}</div>
                  <div class="stat-label">缺货物料</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 查询区 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="物料名称">
          <el-input
            v-model="query.name"
            placeholder="物料名称关键词"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input
            v-model="query.specification"
            placeholder="规格型号"
            clearable
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input
            v-model="query.supplier"
            placeholder="供应商名称"
            clearable
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="库存不足" value="LOW_STOCK" />
            <el-option label="缺货" value="OUT_OF_STOCK" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 物料列表 -->
      <el-table
        v-loading="loading"
        :data="materials"
        row-key="id"
        border
        style="width: 100%"
        :default-sort="{prop: 'lastUpdateTime', order: 'descending'}"
        @row-dblclick="handleRowDblClick"
      >
        <el-table-column prop="name" label="物料名称" min-width="150" />
        <el-table-column prop="materialCode" label="物料编码" min-width="120" />
        <el-table-column prop="specification" label="规格型号" min-width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="需求量" min-width="120">
          <template #default="{ row }">
            <span class="quantity required">{{ row.requiredQuantity }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已到货" min-width="120">
          <template #default="{ row }">
            <span class="quantity received">{{ row.receivedQuantity }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存量" min-width="120">
          <template #default="{ row }">
            <span :class="getStockClass(row)">{{ row.stockQuantity }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已使用" min-width="120">
          <template #default="{ row }">
            <span class="quantity used">{{ row.usedQuantity }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价(元)" min-width="100">
          <template #default="{ row }">
            <span class="price">{{ formatPrice(row.unitPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总金额(元)" min-width="120">
          <template #default="{ row }">
            <span class="total-amount">{{ formatPrice(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" min-width="120" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="最后更新" min-width="160" sortable />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-space size="small">
              <el-button link type="primary" size="small" @click="editMaterial(row)" :disabled="!canUpdate">
                编辑
              </el-button>
              <el-button
                link
                type="success"
                size="small"
                @click="updateStock(row)"
                :disabled="!canUpdate"
              >
                入库
              </el-button>
              <el-button
                link
                type="warning"
                size="small"
                @click="useMaterial(row)"
                :disabled="!canUpdate"
              >
                出库
              </el-button>
              <el-button link type="danger" size="small" @click="deleteMaterial(row)" :disabled="!canDelete">
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page="query.page"
          :page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>

      <!-- 添加/编辑物料弹窗 -->
      <el-dialog
        v-model="materialDialog.visible"
        :title="materialDialog.isEdit ? '编辑物料' : '添加物料'"
        width="700px"
        draggable
        destroy-on-close
      >
        <el-form
          ref="materialFormRef"
          :model="materialForm"
          :rules="materialRules"
          label-width="120px"
          class="material-form"
          @submit.prevent="saveMaterial"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="物料编码" prop="materialCode">
                <el-input v-model="materialForm.materialCode" placeholder="请输入物料编码" :disabled="materialDialog.isEdit" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="物料名称" prop="name">
                <el-input v-model="materialForm.name" placeholder="请输入物料名称" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="规格型号" prop="specification">
                <el-input v-model="materialForm.specification" placeholder="请输入规格型号" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="单位" prop="unit">
                <el-select v-model="materialForm.unit" placeholder="选择单位" style="width: 100%">
                  <el-option label="吨" value="吨" />
                  <el-option label="立方米" value="立方米" />
                  <el-option label="平方米" value="平方米" />
                  <el-option label="米" value="米" />
                  <el-option label="个" value="个" />
                  <el-option label="套" value="套" />
                  <el-option label="包" value="包" />
                  <el-option label="箱" value="箱" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="需求总量" prop="requiredQuantity">
                <el-input-number
                  v-model="materialForm.requiredQuantity"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="单价(元)" prop="unitPrice">
                <el-input-number
                  v-model="materialForm.unitPrice"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="供应商">
                <el-input v-model="materialForm.supplier" placeholder="请输入供应商名称" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <el-button @click="materialDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveMaterial">
            {{ materialDialog.isEdit ? '保存' : '添加' }}
          </el-button>
        </template>
      </el-dialog>

      <!-- 入库弹窗 -->
      <el-dialog v-model="stockDialog.visible" title="物料入库" width="400px" draggable>
        <el-form :model="stockForm" label-width="100px" @submit.prevent="confirmStockUpdate">
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="入库数量" required>
            <el-input-number
              v-model="stockForm.quantity"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="stockForm.remarks"
              type="textarea"
              :rows="2"
              placeholder="入库备注（可选）"
              @keydown.enter.stop
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="stockDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="confirmStockUpdate">
            确认入库
          </el-button>
        </template>
      </el-dialog>

      <!-- 出库弹窗 -->
      <el-dialog v-model="usageDialog.visible" title="物料出库" width="400px" draggable>
        <el-form :model="usageForm" label-width="100px" @submit.prevent="confirmMaterialUsage">
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="出库数量" required>
            <el-input-number
              v-model="usageForm.quantity"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="usageForm.remarks"
              type="textarea"
              :rows="2"
              placeholder="出库备注（可选）"
              @keydown.enter.stop
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="usageDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="confirmMaterialUsage">
            确认出库
          </el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, Download, Box, Check, Warning, Close } from '@element-plus/icons-vue';
import { hasPermission } from '@/utils/permission';
import {
  createProjectMaterial,
  deleteProjectMaterial,
  getProjectMaterialDetail,
  inboundProjectMaterial,
  outboundProjectMaterial,
  updateProjectMaterial,
  type ProjectMaterialVO,
} from '@/api/corpProject/projectMaterial';

const route = useRoute();
const loading = ref(false);
const saving = ref(false);

function parseProjectId(raw: unknown): number | null {
  const text = String(raw || '').trim();
  if (!text || !/^\d+$/.test(text)) {
    return null;
  }
  return Number(text);
}

const projectId = ref<number | null>(parseProjectId(route.params.projectId));
const projectName = ref('工程项目');

type MaterialRow = {
  id: number;
  projectId: number;
  materialCode: string;
  name: string;
  specification: string;
  unit: string;
  requiredQuantity: number;
  receivedQuantity: number;
  usedQuantity: number;
  stockQuantity: number;
  unitPrice: number;
  totalAmount: number;
  supplier?: string;
  status: 'NORMAL' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  lastUpdateTime: string;
};

const allMaterials = ref<MaterialRow[]>([]);
const materials = ref<MaterialRow[]>([]);
const total = ref(0);

const materialStats = reactive({
  total: 0,
  lowStock: 0,
  outOfStock: 0,
});

const query = reactive({
  name: '',
  specification: '',
  supplier: '',
  status: undefined as string | undefined,
  page: 1,
  pageSize: 10,
});

const materialDialog = reactive({
  visible: false,
  isEdit: false,
  editId: null as number | null,
});

const materialFormRef = ref<FormInstance>();
const materialForm = reactive({
  materialCode: '',
  name: '',
  specification: '',
  unit: '吨',
  requiredQuantity: 0,
  unitPrice: 0,
  supplier: '',
});

const materialRules: FormRules = {
  materialCode: [{ required: true, message: '请输入物料编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
  specification: [{ required: true, message: '请输入规格型号', trigger: 'blur' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  requiredQuantity: [{ required: true, message: '请输入需求总量', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
};

const stockDialog = reactive({
  visible: false,
  materialId: null as number | null,
});

const stockForm = reactive({
  quantity: 0,
  remarks: '',
});

const usageDialog = reactive({
  visible: false,
  materialId: null as number | null,
});

const usageForm = reactive({
  quantity: 0,
  remarks: '',
});

const canUpdate = computed(() => hasPermission('project:material:update'));
const canDelete = computed(() => hasPermission('project:material:delete'));

function getStatusLabel(status?: string) {
  const labels = {
    NORMAL: '正常',
    LOW_STOCK: '库存不足',
    OUT_OF_STOCK: '缺货',
  };
  return labels[status as keyof typeof labels] || status;
}

function getStatusTagType(status?: string) {
  const types = {
    NORMAL: 'success',
    LOW_STOCK: 'warning',
    OUT_OF_STOCK: 'danger',
  };
  return types[status as keyof typeof types] || 'info';
}

function getStockClass(material: ProjectMaterialVO) {
  if (material.stockQuantity === 0) return 'out-of-stock';
  if (material.stockQuantity < material.requiredQuantity * 0.1) return 'low-stock';
  return 'normal-stock';
}

function formatPrice(price: number) {
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(price);
}

async function loadMaterials() {
  if (!projectId.value) {
    materials.value = [];
    allMaterials.value = [];
    total.value = 0;
    materialStats.total = 0;
    materialStats.lowStock = 0;
    materialStats.outOfStock = 0;
    return;
  }

  loading.value = true;
  try {
    const res = await getProjectMaterialDetail(projectId.value);
    allMaterials.value = (res.materials || []).map((item: ProjectMaterialVO) => ({
      id: item.id,
      projectId: item.projectId,
      materialCode: item.materialCode,
      name: item.materialName,
      specification: item.spec,
      unit: item.unit,
      requiredQuantity: Number(item.requiredQuantity || 0),
      receivedQuantity: Number(item.receivedQuantity || 0),
      usedQuantity: Number(item.usedQuantity || 0),
      stockQuantity: Number(item.stockQuantity || 0),
      unitPrice: Number(item.unitPrice || 0),
      totalAmount: Number(item.totalAmount || 0),
      supplier: item.supplierName,
      status: item.status,
      lastUpdateTime: item.lastUpdateTime || '',
    }));
    materialStats.total = Number(res.total || allMaterials.value.length);
    materialStats.lowStock = Number(res.lowStock || 0);
    materialStats.outOfStock = Number(res.outOfStock || 0);
    applyFilter();
  } finally {
    loading.value = false;
  }
}

function applyFilter() {
  const filtered = allMaterials.value.filter((m) => {
    const okName = !query.name || m.name.includes(query.name);
    const okSpec = !query.specification || m.specification.includes(query.specification);
    const okSupplier = !query.supplier || (m.supplier || '').includes(query.supplier);
    const okStatus = !query.status || m.status === query.status;
    return okName && okSpec && okSupplier && okStatus;
  });
  total.value = filtered.length;
  const start = (query.page - 1) * query.pageSize;
  const end = start + query.pageSize;
  materials.value = filtered.slice(start, end);
}

function handleSearch() {
  query.page = 1;
  applyFilter();
}

function handleReset() {
  query.name = '';
  query.specification = '';
  query.supplier = '';
  query.status = undefined;
  query.page = 1;
  applyFilter();
}

function handleCurrentChange(page: number) {
  query.page = page;
  applyFilter();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.page = 1;
  applyFilter();
}

function openAddMaterialDialog() {
  materialDialog.isEdit = false;
  materialDialog.visible = true;
  materialDialog.editId = null;
  resetMaterialForm();
}

function editMaterial(material: MaterialRow) {
  materialDialog.isEdit = true;
  materialDialog.visible = true;
  materialDialog.editId = material.id;
  materialForm.materialCode = material.materialCode;
  materialForm.name = material.name;
  materialForm.specification = material.specification;
  materialForm.unit = material.unit;
  materialForm.requiredQuantity = material.requiredQuantity;
  materialForm.unitPrice = material.unitPrice;
  materialForm.supplier = material.supplier || '';
}

function resetMaterialForm() {
  materialForm.materialCode = '';
  materialForm.name = '';
  materialForm.specification = '';
  materialForm.unit = '吨';
  materialForm.requiredQuantity = 0;
  materialForm.unitPrice = 0;
  materialForm.supplier = '';
}

async function saveMaterial() {
  if (!materialFormRef.value || !projectId.value) return;
  const valid = await materialFormRef.value.validate();
  if (!valid) return;

  saving.value = true;
  try {
    if (materialDialog.isEdit && materialDialog.editId) {
      await updateProjectMaterial({
        id: materialDialog.editId,
        materialName: materialForm.name,
        spec: materialForm.specification,
        unit: materialForm.unit,
        requiredQuantity: materialForm.requiredQuantity,
        unitPrice: materialForm.unitPrice,
        supplierName: materialForm.supplier || undefined,
      });
      ElMessage.success('编辑成功');
    } else {
      await createProjectMaterial({
        projectId: projectId.value,
        materialCode: materialForm.materialCode,
        materialName: materialForm.name,
        spec: materialForm.specification,
        unit: materialForm.unit,
        requiredQuantity: materialForm.requiredQuantity,
        unitPrice: materialForm.unitPrice,
        supplierName: materialForm.supplier || undefined,
      });
      ElMessage.success('添加成功');
    }

    materialDialog.visible = false;
    await loadMaterials();
  } finally {
    saving.value = false;
  }
}

function updateStock(material: MaterialRow) {
  stockDialog.visible = true;
  stockDialog.materialId = material.id;
  stockForm.quantity = 0;
  stockForm.remarks = '';
}

async function confirmStockUpdate() {
  if (!stockDialog.materialId) return;
  if (stockForm.quantity <= 0) {
    ElMessage.error('入库数量必须大于0');
    return;
  }

  await inboundProjectMaterial({ id: stockDialog.materialId, quantity: stockForm.quantity, remarks: stockForm.remarks || undefined });
  stockDialog.visible = false;
  await loadMaterials();
  ElMessage.success('入库成功');
}

function useMaterial(material: MaterialRow) {
  usageDialog.visible = true;
  usageDialog.materialId = material.id;
  usageForm.quantity = 0;
  usageForm.remarks = '';
}

async function confirmMaterialUsage() {
  if (!usageDialog.materialId) return;
  if (usageForm.quantity <= 0) {
    ElMessage.error('出库数量必须大于0');
    return;
  }

  const material = allMaterials.value.find((m) => m.id === usageDialog.materialId);
  if (material && usageForm.quantity > material.stockQuantity) {
    ElMessage.error('出库数量不能超过库存量');
    return;
  }

  await outboundProjectMaterial({
    id: usageDialog.materialId,
    quantity: usageForm.quantity,
    useDate: new Date().toISOString().split('T')[0],
    remarks: usageForm.remarks || undefined,
  });
  usageDialog.visible = false;
  await loadMaterials();
  ElMessage.success('出库成功');
}

function deleteMaterial(material: MaterialRow) {
  ElMessageBox.confirm(`确认删除物料「${material.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteProjectMaterial(material.id);
      await loadMaterials();
      ElMessage.success('删除成功');
    })
    .catch(() => {});
}

function handleRowDblClick(row: MaterialRow) {
  if (canUpdate.value) {
    editMaterial(row);
  }
}

function exportMaterials() {
  ElMessage.info('导出功能开发中...');
}

watch(() => route.params.projectId, (newId) => {
  projectId.value = parseProjectId(newId);
  loadMaterials();
});

onMounted(() => {
  if (!projectId.value) {
    ElMessage.warning('当前未选择有效项目，请从项目管理进入');
  }
  loadMaterials();
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
}

.actions > * + * {
  margin-left: 8px;
}

.material-stats {
  margin-bottom: 24px;

  .stat-card {
    height: 100px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .stat-content {
      display: flex;
      align-items: center;
      height: 100%;
      padding: 16px;

      .stat-icon {
        margin-right: 16px;
      }

      .stat-info {
        .stat-number {
          font-size: 28px;
          font-weight: bold;
          line-height: 1;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .total-card .stat-content .stat-icon {
    color: #409EFF;
  }

  .normal-card .stat-content .stat-icon {
    color: #67C23A;
  }

  .warning-card .stat-content .stat-icon {
    color: #E6A23C;
  }

  .danger-card .stat-content .stat-icon {
    color: #F56C6C;
  }
}

.search-bar {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.quantity {
  font-weight: 500;

  &.required {
    color: #409EFF;
  }

  &.received {
    color: #67C23A;
  }

  &.used {
    color: #F56C6C;
  }
}

.normal-stock {
  color: #67C23A;
  font-weight: 500;
}

.low-stock {
  color: #E6A23C;
  font-weight: 500;
}

.out-of-stock {
  color: #F56C6C;
  font-weight: 500;
}

.price {
  color: #E6A23C;
  font-weight: 500;
}

.total-amount {
  color: #409EFF;
  font-weight: 500;
}

.material-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}
</style>
