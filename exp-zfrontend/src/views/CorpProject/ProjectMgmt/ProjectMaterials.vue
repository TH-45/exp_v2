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
            <el-button size="small" @click="createProcurement" :disabled="!canUpdate">
              <el-icon><ShoppingCart /></el-icon>
              批量采购
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
      >
        <el-table-column prop="name" label="物料名称" min-width="150" />
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
            <el-col :span="12">
              <el-form-item label="物料名称" prop="name">
                <el-input v-model="materialForm.name" placeholder="请输入物料名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
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
            <el-col :span="12">
              <el-form-item label="状态" prop="status">
                <el-select v-model="materialForm.status" placeholder="选择状态" style="width: 100%">
                  <el-option label="正常" value="NORMAL" />
                  <el-option label="库存不足" value="LOW_STOCK" />
                  <el-option label="缺货" value="OUT_OF_STOCK" />
                </el-select>
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
      <el-dialog v-model="stockDialog.visible" title="物料入库" width="400px">
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
      <el-dialog v-model="usageDialog.visible" title="物料出库" width="400px">
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
import { Plus, Download, ShoppingCart, Box, Check, Warning, Close } from '@element-plus/icons-vue';
import { hasPermission } from '@/utils/permission';
import {
  getProjectMaterials,
  addProjectMaterial,
  updateProjectMaterial,
  deleteProjectMaterial,
  updateMaterialStock,
  createMaterialProcurement,
  getMaterialStats,
  type ProjectMaterialVO
} from '@/api/project';

const route = useRoute();
const loading = ref(false);
const saving = ref(false);

const projectId = ref(route.params.projectId as string || '');
const projectName = ref('某某大厦项目');

// 物料列表
const materials = ref<ProjectMaterialVO[]>([]);
const total = ref(0);

// 统计信息
const materialStats = reactive({
  total: 0,
  lowStock: 0,
  outOfStock: 0,
});

// 查询条件
const query = reactive({
  name: '',
  specification: '',
  supplier: '',
  status: undefined as string | undefined,
  page: 1,
  pageSize: 10,
});

// 弹窗状态
const materialDialog = reactive({
  visible: false,
  isEdit: false,
  editId: '',
});

const materialFormRef = ref<FormInstance>();
const materialForm = reactive({
  name: '',
  specification: '',
  unit: '吨',
  requiredQuantity: 0,
  unitPrice: 0,
  supplier: '',
  status: 'NORMAL' as string,
});

const materialRules: FormRules = {
  name: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
  specification: [{ required: true, message: '请输入规格型号', trigger: 'blur' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  requiredQuantity: [{ required: true, message: '请输入需求总量', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

// 入库弹窗
const stockDialog = reactive({
  visible: false,
  materialId: '',
});

const stockForm = reactive({
  quantity: 0,
  remarks: '',
});

// 出库弹窗
const usageDialog = reactive({
  visible: false,
  materialId: '',
});

const usageForm = reactive({
  quantity: 0,
  remarks: '',
});

// 权限点
const canView = computed(() => hasPermission('project:material:view'));
const canUpdate = computed(() => hasPermission('project:material:update'));
const canDelete = computed(() => hasPermission('project:material:delete'));

// 模拟数据
const mockMaterials: ProjectMaterialVO[] = [
  {
    id: 'm001',
    projectId: projectId.value,
    name: '钢筋',
    specification: 'Φ12mm HRB400',
    unit: '吨',
    requiredQuantity: 100,
    receivedQuantity: 80,
    usedQuantity: 65,
    stockQuantity: 15,
    unitPrice: 4500,
    totalAmount: 450000,
    supplier: '某某钢材有限公司',
    status: 'NORMAL',
    lastUpdateTime: '2025-01-05 14:30:00',
  },
  {
    id: 'm002',
    projectId: projectId.value,
    name: '水泥',
    specification: '425号普通硅酸盐水泥',
    unit: '吨',
    requiredQuantity: 200,
    receivedQuantity: 150,
    usedQuantity: 148,
    stockQuantity: 2,
    unitPrice: 380,
    totalAmount: 76000,
    supplier: '某某水泥厂',
    status: 'LOW_STOCK',
    lastUpdateTime: '2025-01-04 16:20:00',
  },
  {
    id: 'm003',
    projectId: projectId.value,
    name: '砂石料',
    specification: '混合砂石料 0-20mm',
    unit: '立方米',
    requiredQuantity: 500,
    receivedQuantity: 350,
    usedQuantity: 320,
    stockQuantity: 30,
    unitPrice: 120,
    totalAmount: 60000,
    supplier: '某某建材市场',
    status: 'NORMAL',
    lastUpdateTime: '2025-01-03 10:15:00',
  },
  {
    id: 'm004',
    projectId: projectId.value,
    name: '模板',
    specification: '钢模板 标准规格',
    unit: '平方米',
    requiredQuantity: 2000,
    receivedQuantity: 1200,
    usedQuantity: 1100,
    stockQuantity: 100,
    unitPrice: 25,
    totalAmount: 50000,
    supplier: '某某模板租赁公司',
    status: 'NORMAL',
    lastUpdateTime: '2025-01-02 09:45:00',
  },
  {
    id: 'm005',
    projectId: projectId.value,
    name: '混凝土',
    specification: 'C30商品混凝土',
    unit: '立方米',
    requiredQuantity: 300,
    receivedQuantity: 0,
    usedQuantity: 0,
    stockQuantity: 0,
    unitPrice: 450,
    totalAmount: 135000,
    supplier: '某某混凝土搅拌站',
    status: 'OUT_OF_STOCK',
    lastUpdateTime: '2025-01-01 08:30:00',
  },
];

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
  if (!projectId.value) return;

  loading.value = true;
  try {
    const [materialsRes, statsRes] = await Promise.allSettled([
      getProjectMaterials(projectId.value),
      getMaterialStats(projectId.value)
    ]);

    if (materialsRes.status === 'fulfilled') {
      materials.value = materialsRes.value.length ? materialsRes.value : mockMaterials;
      total.value = materials.value.length;
    } else {
      materials.value = mockMaterials;
      total.value = mockMaterials.length;
    }

    if (statsRes.status === 'fulfilled') {
      Object.assign(materialStats, statsRes.value);
    } else {
      calculateMaterialStats();
    }
  } catch (e) {
    materials.value = mockMaterials;
    total.value = mockMaterials.length;
    calculateMaterialStats();
  } finally {
    loading.value = false;
  }
}

function calculateMaterialStats() {
  materialStats.total = materials.value.length;
  materialStats.lowStock = materials.value.filter(m =>
    m.stockQuantity > 0 && m.stockQuantity < m.requiredQuantity * 0.1
  ).length;
  materialStats.outOfStock = materials.value.filter(m => m.stockQuantity === 0).length;
}

function handleSearch() {
  query.page = 1;
  // 实际应用中应该调用API重新查询
  loadMaterials();
}

function handleReset() {
  query.name = '';
  query.specification = '';
  query.supplier = '';
  query.status = undefined;
  query.page = 1;
  loadMaterials();
}

function handleCurrentChange(page: number) {
  query.page = page;
  loadMaterials();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.page = 1;
  loadMaterials();
}

function openAddMaterialDialog() {
  materialDialog.isEdit = false;
  materialDialog.visible = true;
  materialDialog.editId = '';
  resetMaterialForm();
}

function editMaterial(material: ProjectMaterialVO) {
  materialDialog.isEdit = true;
  materialDialog.visible = true;
  materialDialog.editId = material.id;
  Object.assign(materialForm, material);
}

function resetMaterialForm() {
  materialForm.name = '';
  materialForm.specification = '';
  materialForm.unit = '吨';
  materialForm.requiredQuantity = 0;
  materialForm.unitPrice = 0;
  materialForm.supplier = '';
  materialForm.status = 'NORMAL';
}

async function saveMaterial() {
  if (!materialFormRef.value) return;
  const valid = await materialFormRef.value.validate();
  if (!valid) return;

  saving.value = true;
  try {
    const formData = {
      projectId: projectId.value,
      ...materialForm,
    };

    if (materialDialog.isEdit) {
      await updateProjectMaterial(materialDialog.editId, formData);
      const index = materials.value.findIndex(m => m.id === materialDialog.editId);
      if (index > -1) {
        materials.value[index] = {
          ...formData,
          id: materialDialog.editId,
          receivedQuantity: materials.value[index].receivedQuantity,
          usedQuantity: materials.value[index].usedQuantity,
          stockQuantity: materials.value[index].stockQuantity,
          totalAmount: formData.requiredQuantity * formData.unitPrice,
          lastUpdateTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        };
      }
      ElMessage.success('编辑成功');
    } else {
      const newMaterial = await addProjectMaterial(formData);
      materials.value.push({
        ...formData,
        id: newMaterial.id || `m${Date.now()}`,
        receivedQuantity: 0,
        usedQuantity: 0,
        stockQuantity: 0,
        totalAmount: formData.requiredQuantity * formData.unitPrice,
        lastUpdateTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
      });
      ElMessage.success('添加成功');
    }

    materialDialog.visible = false;
    calculateMaterialStats();
  } catch (e) {
    // 模拟前端操作
    if (materialDialog.isEdit) {
      const index = materials.value.findIndex(m => m.id === materialDialog.editId);
      if (index > -1) {
        materials.value[index] = {
          ...materialForm,
          id: materialDialog.editId,
          receivedQuantity: materials.value[index].receivedQuantity,
          usedQuantity: materials.value[index].usedQuantity,
          stockQuantity: materials.value[index].stockQuantity,
          totalAmount: materialForm.requiredQuantity * materialForm.unitPrice,
          projectId: projectId.value,
          lastUpdateTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        };
      }
      ElMessage.success('编辑成功（演示模式）');
    } else {
      materials.value.push({
        ...materialForm,
        id: `m${Date.now()}`,
        receivedQuantity: 0,
        usedQuantity: 0,
        stockQuantity: 0,
        totalAmount: materialForm.requiredQuantity * materialForm.unitPrice,
        projectId: projectId.value,
        lastUpdateTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
      });
      ElMessage.success('添加成功（演示模式）');
    }

    materialDialog.visible = false;
    calculateMaterialStats();
  } finally {
    saving.value = false;
  }
}

function updateStock(material: ProjectMaterialVO) {
  stockDialog.visible = true;
  stockDialog.materialId = material.id;
  stockForm.quantity = 0;
  stockForm.remarks = '';
}

async function confirmStockUpdate() {
  if (stockForm.quantity <= 0) {
    ElMessage.error('入库数量必须大于0');
    return;
  }

  try {
    await updateMaterialStock(stockDialog.materialId, stockForm.quantity);
    const material = materials.value.find(m => m.id === stockDialog.materialId);
    if (material) {
      material.receivedQuantity += stockForm.quantity;
      material.stockQuantity += stockForm.quantity;
      material.lastUpdateTime = new Date().toISOString().slice(0, 19).replace('T', ' ');
    }
    stockDialog.visible = false;
    calculateMaterialStats();
    ElMessage.success('入库成功');
  } catch (e) {
    // 模拟前端更新
    const material = materials.value.find(m => m.id === stockDialog.materialId);
    if (material) {
      material.receivedQuantity += stockForm.quantity;
      material.stockQuantity += stockForm.quantity;
      material.lastUpdateTime = new Date().toISOString().slice(0, 19).replace('T', ' ');
    }
    stockDialog.visible = false;
    calculateMaterialStats();
    ElMessage.success('入库成功（演示模式）');
  }
}

function useMaterial(material: ProjectMaterialVO) {
  usageDialog.visible = true;
  usageDialog.materialId = material.id;
  usageForm.quantity = 0;
  usageForm.remarks = '';
}

async function confirmMaterialUsage() {
  if (usageForm.quantity <= 0) {
    ElMessage.error('出库数量必须大于0');
    return;
  }

  const material = materials.value.find(m => m.id === usageDialog.materialId);
  if (material && usageForm.quantity > material.stockQuantity) {
    ElMessage.error('出库数量不能超过库存量');
    return;
  }

  try {
    // 这里应该调用出库API，但API中没有定义，暂时使用入库API的相反操作
    await updateMaterialStock(usageDialog.materialId, -usageForm.quantity);
    if (material) {
      material.usedQuantity += usageForm.quantity;
      material.stockQuantity -= usageForm.quantity;
      material.lastUpdateTime = new Date().toISOString().slice(0, 19).replace('T', ' ');
    }
    usageDialog.visible = false;
    calculateMaterialStats();
    ElMessage.success('出库成功');
  } catch (e) {
    // 模拟前端更新
    if (material) {
      material.usedQuantity += usageForm.quantity;
      material.stockQuantity -= usageForm.quantity;
      material.lastUpdateTime = new Date().toISOString().slice(0, 19).replace('T', ' ');
    }
    usageDialog.visible = false;
    calculateMaterialStats();
    ElMessage.success('出库成功（演示模式）');
  }
}

function deleteMaterial(material: ProjectMaterialVO) {
  ElMessageBox.confirm(`确认删除物料「${material.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteProjectMaterial(material.id);
        materials.value = materials.value.filter(m => m.id !== material.id);
        calculateMaterialStats();
        ElMessage.success('删除成功');
      } catch (e) {
        materials.value = materials.value.filter(m => m.id !== material.id);
        calculateMaterialStats();
        ElMessage.success('删除成功（演示模式）');
      }
    })
    .catch(() => {});
}

function createProcurement() {
  const lowStockMaterials = materials.value.filter(m =>
    m.status === 'LOW_STOCK' || m.status === 'OUT_OF_STOCK'
  );

  if (lowStockMaterials.length === 0) {
    ElMessage.info('当前没有需要采购的物料');
    return;
  }

  ElMessageBox.confirm(
    `发现 ${lowStockMaterials.length} 种物料需要采购，是否批量创建采购申请？`,
    '批量采购',
    { type: 'info' }
  ).then(async () => {
    let successCount = 0;
    for (const material of lowStockMaterials) {
      try {
        await createMaterialProcurement({
          materialId: material.id,
          quantity: material.requiredQuantity - material.receivedQuantity,
          supplier: material.supplier || '待定',
          expectedDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
          remarks: '系统自动生成采购申请',
        });
        successCount++;
      } catch (e) {
        // 忽略单个失败
      }
    }

    if (successCount > 0) {
      ElMessage.success(`成功创建 ${successCount} 个采购申请`);
    } else {
      ElMessage.error('采购申请创建失败');
    }
  });
}

function exportMaterials() {
  ElMessage.info('导出功能开发中...');
}

watch(() => route.params.projectId, (newId) => {
  projectId.value = newId as string || '';
  if (projectId.value) {
    loadMaterials();
  }
});

onMounted(() => {
  if (projectId.value) {
    loadMaterials();
  }
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
