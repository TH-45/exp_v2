<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">投标登记</div>
          <div class="actions">
            <el-button type="primary" size="small" @click="openEdit(false)" :disabled="!canManage">
              新增投标
            </el-button>
          </div>
        </div>
      </template>
      <el-form :model="query" class="search-bar" @submit.prevent>
        <div class="search-row search-row-primary">
          <el-form-item label="投标编号" class="search-item search-item-keyword">
            <el-input v-model="query.bidCode" clearable placeholder="请输入投标编号" />
          </el-form-item>
          <el-form-item label="投标名称" class="search-item search-item-keyword">
            <el-input v-model="query.bidName" clearable placeholder="请输入投标名称" />
          </el-form-item>
          <el-form-item label="状态" class="search-item search-item-status">
            <el-select v-model="query.bidStatus" clearable placeholder="全部状态">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="中标标识" class="search-item search-item-flag">
            <el-select v-model="query.winFlag" clearable placeholder="全部">
              <el-option label="未中标" :value="0" />
              <el-option label="已中标" :value="1" />
            </el-select>
          </el-form-item>
          <div class="search-actions">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button link type="primary" @click="toggleAdvancedSearch">
              {{ advancedSearchVisible ? '收起高级筛选' : '展开高级筛选' }}
            </el-button>
            <span v-if="activeFilterCount > 0" class="filter-summary">已筛选 {{ activeFilterCount }} 项</span>
          </div>
        </div>
        <div v-show="advancedSearchVisible" class="search-row search-row-advanced">
          <el-form-item label="招标单位名称" class="search-item search-item-keyword">
            <el-input v-model="query.purchaserName" clearable placeholder="请输入招标单位名称" />
          </el-form-item>
          <el-form-item label="招标项目名称" class="search-item search-item-keyword">
            <el-input v-model="query.tenderName" clearable placeholder="请输入招标项目名称" />
          </el-form-item>
          <el-form-item label="关联项目名称" class="search-item search-item-keyword">
            <el-input v-model="query.projectName" clearable placeholder="请输入关联项目名称" />
          </el-form-item>
          <el-form-item label="提交时间" class="search-item search-item-range">
            <el-date-picker
              v-model="bidSubmitRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
            />
          </el-form-item>
        </div>
      </el-form>

      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="bidId"
        border
        style="width: 100%"
        @row-dblclick="goDetail"
      >
        <el-table-column prop="bidCode" label="投标编号" min-width="140" />
        <el-table-column prop="bidName" label="投标名称" min-width="200" />
        <el-table-column prop="tenderName" label="招标项目" min-width="180" />
        <el-table-column prop="supplierName" label="投标单位" min-width="180" />
        <el-table-column label="报价金额" min-width="130">
          <template #default="{ row }">
            {{ formatAmount(row.bidTotalAmount, row.currency) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.bidStatus)">
              {{ statusText(row.bidStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="中标" min-width="80">
          <template #default="{ row }">
            <el-tag :type="Number(row.winFlag) === 1 ? 'success' : 'info'">
              {{ Number(row.winFlag) === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="managerPersonName" label="负责人" min-width="110" />
        <el-table-column prop="salesmanName" label="业务员" min-width="110" />
        <el-table-column prop="createdByName" label="创建人" min-width="110" />
        <el-table-column prop="createdTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="230">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(true, row)" :disabled="!canManage">
              编辑
            </el-button>
            <el-button link size="small" @click="goDetail(row)">详情</el-button>
            <el-popconfirm title="确认删除该投标记录吗？" @confirm="removeById(row)">
              <template #reference>
                <el-button link type="danger" size="small" :disabled="!canManage">删除</el-button>
              </template>
            </el-popconfirm>
            <el-dropdown trigger="click" :disabled="!canManage || nextStatusOptions(row.bidStatus).length === 0">
              <el-button link size="small" type="warning">
                状态变更
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="s in nextStatusOptions(row.bidStatus)"
                    :key="`${row.bidId}-${s}`"
                    @click="changeStatus(row, s)"
                  >
                    变更为{{ statusText(s) }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>

      <el-dialog
        v-model="editDialog.visible"
        :title="editDialog.isEdit ? '编辑投标' : '新增投标'"
        width="920px"
        destroy-on-close
        draggable
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          class="dialog-form two-col"
          @submit.prevent="submitForm"
        >
          <button type="submit" style="display: none;" aria-hidden="true" tabindex="-1"></button>
          <el-form-item label="招标项目ID" prop="tenderId">
            <el-input-number v-model="form.tenderId" :min="1" :precision="0" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="投标单位" prop="supplier">
            <CompanySelector v-model="form.supplier" />
          </el-form-item>
          <el-form-item label="投标编号" prop="bidCode">
            <el-input v-model="form.bidCode" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="投标名称" prop="bidName">
            <el-input v-model="form.bidName" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="投标金额" prop="bidTotalAmount">
            <el-input-number v-model="form.bidTotalAmount" :min="0" :precision="2" :max="999999999999" style="width: 100%" />
          </el-form-item>
          <el-form-item label="币种" prop="currency">
            <el-select v-model="form.currency" style="width: 100%">
              <el-option label="人民币(CNY)" value="CNY" />
              <el-option label="美元(USD)" value="USD" />
              <el-option label="欧元(EUR)" value="EUR" />
            </el-select>
          </el-form-item>
          <el-form-item label="归属组织" prop="org">
            <OrgSelector v-model="form.org" @change="handleOrgChange" />
          </el-form-item>
          <el-form-item label="负责人" prop="principal">
            <PersonSelector v-model="form.principal" />
          </el-form-item>
          <el-form-item label="业务员">
            <PersonSelector v-model="form.salesman" />
          </el-form-item>
          <el-form-item label="工程项目">
            <ProjectSelector v-model="form.project" />
          </el-form-item>
          <el-form-item label="提交时间">
            <el-date-picker
              v-model="form.bidSubmitTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择提交时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="当前状态">
            <el-input :model-value="editDialog.isEdit ? statusText(form.bidStatus) : '准备'" disabled />
          </el-form-item>
          <el-form-item label="备注" class="full-row">
            <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit @keydown.enter.stop />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">确认</el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import type { ExpPersonVO } from '@/api/system/person';
import type { OrgNode } from '@/api/system/post';
import type { ProjectVO } from '@/api/corpProject/project';
import type { CompanySelectorValue } from '@/api/enterprise/company';
import CompanySelector from '@/components/Selector/CompanySelector.vue';
import OrgSelector from '@/components/Selector/OrgSelector.vue';
import PersonSelector from '@/components/Selector/PersonSelector.vue';
import ProjectSelector from '@/components/Selector/ProjectSelector.vue';
import {
  checkBidCode,
  checkSupplierBid,
  createBid,
  deleteBid,
  getBidDetail,
  queryBidList,
  updateBid,
  updateBidStatus,
  type BidListVO,
  type BidStatus,
} from '@/api/bidding/bid';

const route = useRoute();
const router = useRouter();
const canManage = computed(() => hasPermission('bidding:bid:manage'));

const statusOptions: Array<{ label: string; value: BidStatus }> = [
  { label: '准备', value: 'PREPARE' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '评审中', value: 'EVALUATING' },
  { label: '中标', value: 'WON' },
  { label: '未中标', value: 'LOST' },
  { label: '已放弃', value: 'ABANDONED' },
];

const statusFlow: Record<BidStatus, BidStatus[]> = {
  PREPARE: ['SUBMITTED', 'ABANDONED'],
  SUBMITTED: ['EVALUATING', 'ABANDONED'],
  EVALUATING: ['WON', 'LOST', 'ABANDONED'],
  WON: [],
  LOST: [],
  ABANDONED: [],
};

function statusText(status?: string) {
  if (!status) return '';
  return statusOptions.find((x) => x.value === status)?.label ?? status;
}

function statusTagType(status?: string) {
  if (status === 'PREPARE') return 'info';
  if (status === 'SUBMITTED') return 'warning';
  if (status === 'EVALUATING') return 'primary';
  if (status === 'WON') return 'success';
  if (status === 'LOST') return 'danger';
  if (status === 'ABANDONED') return 'info';
  return '';
}

function nextStatusOptions(status?: string): BidStatus[] {
  if (!status) return [];
  return statusFlow[status as BidStatus] ?? [];
}

function formatAmount(amount?: number, currency?: string) {
  if (amount == null) return '';
  const value = Number(amount);
  if (!Number.isFinite(value)) return '';
  return `${value.toFixed(2)} ${currency || 'CNY'}`;
}

const loading = ref(false);
const saving = ref(false);
const advancedSearchVisible = ref(false);

const bidSubmitRange = ref<string[]>([]);
const query = reactive({
  bidCode: '',
  bidName: '',
  purchaserName: '',
  tenderName: '',
  projectName: '',
  bidStatus: undefined as BidStatus | undefined,
  winFlag: undefined as number | undefined,
  bidSubmitTimeStart: '',
  bidSubmitTimeEnd: '',
  pageNum: 1,
  pageSize: 10,
  sort: undefined as string | undefined,
});

const activeFilterCount = computed(() => {
  let count = 0;
  if ((query.bidCode || '').trim()) count += 1;
  if ((query.bidName || '').trim()) count += 1;
  if ((query.purchaserName || '').trim()) count += 1;
  if ((query.tenderName || '').trim()) count += 1;
  if ((query.projectName || '').trim()) count += 1;
  if (query.bidStatus) count += 1;
  if (query.winFlag !== undefined) count += 1;
  if ((bidSubmitRange.value?.length || 0) > 0) count += 1;
  return count;
});

const tableData = ref<BidListVO[]>([]);
const total = ref(0);

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const form = reactive({
  bidId: undefined as number | undefined,
  tenderId: undefined as number | undefined,
  supplier: undefined as CompanySelectorValue | undefined,
  bidCode: '',
  bidName: '',
  bidTotalAmount: undefined as number | undefined,
  currency: 'CNY',
  org: undefined as OrgNode | undefined,
  orgId: undefined as number | undefined,
  principal: undefined as ExpPersonVO | undefined,
  salesman: undefined as ExpPersonVO | undefined,
  project: undefined as ProjectVO | undefined,
  bidSubmitTime: '',
  bidStatus: 'PREPARE' as BidStatus,
  remark: '',
});

const rules: FormRules = {
  tenderId: [{ required: true, message: '请输入招标项目ID', trigger: 'change' }],
  supplier: [{ required: true, message: '请选择投标单位', trigger: 'change' }],
  bidCode: [
    { required: true, message: '请输入投标编号', trigger: 'blur' },
  ],
  bidName: [{ required: true, message: '请输入投标名称', trigger: 'blur' }],
  bidTotalAmount: [{ required: true, message: '请输入投标金额', trigger: 'change' }],
  currency: [{ required: true, message: '请选择币种', trigger: 'change' }],
  org: [{ required: true, message: '请选择归属组织', trigger: 'change' }],
  principal: [{ required: true, message: '请选择负责人', trigger: 'change' }],
};

watch(
  () => route.query.edit,
  (val) => {
    const id = typeof val === 'string' ? Number(val) : Array.isArray(val) ? Number(val[0]) : 0;
    if (!id) return;
    const target = tableData.value.find((x) => Number(x.bidId) === id);
    if (target) {
      openEdit(true, target);
      return;
    }
    openEditById(id);
  },
  { immediate: true },
);

async function fetchList() {
  loading.value = true;
  try {
    query.bidSubmitTimeStart = bidSubmitRange.value?.[0] || '';
    query.bidSubmitTimeEnd = bidSubmitRange.value?.[1] || '';
    const res = await queryBidList({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      purchaserName: query.purchaserName || undefined,
      tenderName: query.tenderName || undefined,
      projectName: query.projectName || undefined,
      bidCode: query.bidCode || undefined,
      bidName: query.bidName || undefined,
      bidStatus: query.bidStatus,
      winFlag: query.winFlag,
      bidSubmitTimeStart: query.bidSubmitTimeStart || undefined,
      bidSubmitTimeEnd: query.bidSubmitTimeEnd || undefined,
      sort: query.sort,
    });
    tableData.value = Array.isArray(res?.list) ? res.list : [];
    total.value = Number(res?.total ?? 0) || 0;
  } catch (_e) {
    tableData.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchList();
});

function handleSearch() {
  query.purchaserName = (query.purchaserName || '').trim();
  query.tenderName = (query.tenderName || '').trim();
  query.projectName = (query.projectName || '').trim();
  query.bidCode = (query.bidCode || '').trim();
  query.bidName = (query.bidName || '').trim();
  query.pageNum = 1;
  fetchList();
}

function handleReset() {
  query.bidCode = '';
  query.bidName = '';
  query.purchaserName = '';
  query.tenderName = '';
  query.projectName = '';
  query.bidStatus = undefined;
  query.winFlag = undefined;
  bidSubmitRange.value = [];
  query.pageNum = 1;
  fetchList();
}

function toggleAdvancedSearch() {
  advancedSearchVisible.value = !advancedSearchVisible.value;
}

function handleCurrentChange(page: number) {
  query.pageNum = page;
  fetchList();
}

function handleSizeChange(size: number) {
  query.pageSize = size;
  query.pageNum = 1;
  fetchList();
}

function goDetail(row: BidListVO) {
  router.push(`/bidding/bid/${row.bidId}`);
}

function resetForm() {
  Object.assign(form, {
    bidId: undefined,
    tenderId: undefined,
    supplier: undefined,
    bidCode: '',
    bidName: '',
    bidTotalAmount: undefined,
    currency: 'CNY',
    org: undefined,
    orgId: undefined,
    principal: undefined,
    salesman: undefined,
    project: undefined,
    bidSubmitTime: '',
    bidStatus: 'PREPARE',
    remark: '',
  });
}

function mapRowToForm(row: BidListVO) {
  form.bidId = Number(row.bidId);
  form.tenderId = row.tenderId != null ? Number(row.tenderId) : undefined;
  form.supplier = row.supplierId != null
    ? {
      companyId: Number(row.supplierId),
      companyName: row.supplierName || '',
    }
    : undefined;
  form.bidCode = row.bidCode || '';
  form.bidName = row.bidName || '';
  form.bidTotalAmount = row.bidTotalAmount != null ? Number(row.bidTotalAmount) : undefined;
  form.currency = row.currency || 'CNY';
  form.org = row.orgId != null
    ? {
      orgId: Number(row.orgId),
      orgName: row.orgIdName || '',
      children: [],
    }
    : undefined;
  form.orgId = row.orgId != null ? Number(row.orgId) : undefined;
  form.principal = row.managerPersonId != null
    ? ({
      personId: Number(row.managerPersonId),
      personName: row.managerPersonName || '',
      personCode: '',
      gender: 'OTHER',
      status: 'ONJOB',
    } as ExpPersonVO)
    : undefined;
  form.salesman = row.salesmanId != null
    ? ({
      personId: Number(row.salesmanId),
      personName: row.salesmanName || '',
      personCode: '',
      gender: 'OTHER',
      status: 'ONJOB',
    } as ExpPersonVO)
    : undefined;
  form.project = row.projectId != null
    ? ({
      projectId: Number(row.projectId),
      projectName: row.projectName || '',
    } as ProjectVO)
    : undefined;
  form.bidSubmitTime = row.bidSubmitTime || '';
  form.bidStatus = (row.bidStatus as BidStatus) || 'PREPARE';
  form.remark = '';
}

async function openEditById(bidId: number) {
  try {
    const detail = await getBidDetail(bidId);
    mapRowToForm({
      ...detail,
      bidId: String(detail.bidId),
    });
    editDialog.isEdit = true;
    editDialog.visible = true;
  } catch (_e) {
    ElMessage.warning('未找到投标信息');
  }
}

function openEdit(isEdit: boolean, row?: BidListVO) {
  editDialog.isEdit = isEdit;
  if (formRef.value) {
    formRef.value.clearValidate();
  }
  if (isEdit && row) {
    mapRowToForm(row);
  } else {
    resetForm();
  }
  editDialog.visible = true;
}

function handleOrgChange(org: OrgNode | undefined) {
  form.orgId = org?.orgId;
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) return;

  const tenderId = Number(form.tenderId);
  const supplierId = Number(form.supplier?.companyId);
  const orgId = Number(form.orgId);
  const principalId = Number(form.principal?.personId);
  const salesmanId = form.salesman?.personId != null ? Number(form.salesman.personId) : undefined;
  const projectId = form.project?.projectId != null ? Number(form.project.projectId) : undefined;

  if (!Number.isFinite(tenderId) || tenderId <= 0) {
    ElMessage.warning('招标项目ID不合法');
    return;
  }
  if (!Number.isFinite(supplierId) || supplierId <= 0) {
    ElMessage.warning('请选择投标单位');
    return;
  }
  if (!Number.isFinite(orgId) || orgId <= 0) {
    ElMessage.warning('请选择归属组织');
    return;
  }
  if (!Number.isFinite(principalId) || principalId <= 0) {
    ElMessage.warning('请选择负责人');
    return;
  }

  const amount = Number(form.bidTotalAmount);
  if (!Number.isFinite(amount) || amount < 0) {
    ElMessage.warning('投标金额必须是大于等于 0 的数字');
    return;
  }
  try {
    const bidCodeExists = await checkBidCode({
      bidCode: form.bidCode.trim(),
      excludeBidId: editDialog.isEdit ? form.bidId : undefined,
    });
    if (bidCodeExists) {
      ElMessage.warning('投标编号已存在');
      return;
    }
  } catch (_e) {
    // 编号校验失败时不阻断保存，后端会做最终校验
  }

  try {
    const duplicate = await checkSupplierBid({
      tenderId,
      supplierId,
      excludeBidId: editDialog.isEdit ? form.bidId : undefined,
    });
    if (duplicate) {
      ElMessage.warning('该投标单位已对该招标项目投标，请勿重复录入');
      return;
    }
  } catch (_e) {
    // 检查接口失败时不阻断保存，交由后端兜底校验
  }

  saving.value = true;
  try {
    if (editDialog.isEdit && form.bidId) {
      await updateBid({
        bidId: Number(form.bidId),
        bidCode: form.bidCode.trim(),
        bidName: form.bidName.trim(),
        bidTotalAmount: amount,
        principalId,
        salesmanId,
        orgId,
        remark: form.remark?.trim() || undefined,
      });
      ElMessage.success('更新成功');
    } else {
      await createBid({
        tenderId,
        supplierId,
        bidCode: form.bidCode.trim(),
        bidName: form.bidName.trim(),
        bidTotalAmount: amount,
        currency: form.currency || 'CNY',
        principalId,
        salesmanId,
        orgId,
        bidSubmitTime: form.bidSubmitTime || undefined,
        projectId,
        remark: form.remark?.trim() || undefined,
      });
      ElMessage.success('创建成功');
    }
    editDialog.visible = false;
    await fetchList();
  } catch (e: any) {
    ElMessage.error(e?.message || (editDialog.isEdit ? '更新失败' : '创建失败'));
  } finally {
    saving.value = false;
  }
}

async function removeById(row: BidListVO) {
  const bidId = Number(row.bidId);
  if (!Number.isFinite(bidId) || bidId <= 0) {
    ElMessage.warning('无效的投标ID');
    return;
  }
  try {
    await deleteBid({ bidId });
    ElMessage.success('删除成功');
    if (tableData.value.length === 1 && query.pageNum > 1) {
      query.pageNum -= 1;
    }
    await fetchList();
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败');
  }
}

async function changeStatus(row: BidListVO, target: BidStatus) {
  const bidId = Number(row.bidId);
  if (!Number.isFinite(bidId) || bidId <= 0) {
    ElMessage.warning('无效的投标ID');
    return;
  }
  try {
    await updateBidStatus({
      bidId,
      bidStatus: target,
    });
    ElMessage.success(`状态已更新为${statusText(target)}`);
    await fetchList();
  } catch (e: any) {
    ElMessage.error(e?.message || '状态更新失败');
  }
}
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

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  margin-right: 30px;
  gap: 8px;
}

.search-bar {
  margin-bottom: 12px;
  padding: 16px 16px 4px;
  background: #f7f9fc;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
}

.search-row + .search-row {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #dcdfe6;
}

.search-row :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-item {
  margin-right: 0;
}

.search-item :deep(.el-input),
.search-item :deep(.el-select),
.search-item :deep(.el-date-editor) {
  width: 100%;
}

.search-item-keyword {
  width: 220px;
}

.search-item-status,
.search-item-flag {
  width: 160px;
}

.search-item-range {
  width: 360px;
}

.search-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-summary {
  padding: 0 10px;
  line-height: 28px;
  border-radius: 999px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
}

@media (max-width: 1400px) {
  .search-actions {
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 768px) {
  .search-item-keyword,
  .search-item-status,
  .search-item-flag,
  .search-item-range {
    width: 100%;
  }

  .search-actions {
    justify-content: flex-start;
  }
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
</style>


