<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">基础信息管理</div>
          <div class="actions">
            <el-button
              type="primary"
              size="small"
              @click="openCreateDialog"
              :disabled="!canCreate"
            >
              <el-icon><Plus /></el-icon>
              新建企业
            </el-button>
            <el-button size="small" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 查询区 -->
      <el-form :inline="true" :model="query" class="search-bar" @submit.prevent>
        <el-form-item label="企业编码">
          <el-input
            v-model="query.companyCode"
            placeholder="企业编码"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="企业名称">
          <el-input
            v-model="query.companyName"
            placeholder="企业名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="企业类型">
          <el-input
            v-model="query.companyType"
            placeholder="企业类型"
            clearable
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表区：双击行打开详情 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="companyId"
        border
        style="width: 100%"
        @row-dblclick="(row: CompanyListVO) => openDetail(row)"
      >
        <el-table-column prop="companyCode" label="企业编码" min-width="120" />
        <el-table-column prop="companyName" label="企业名称" min-width="180" />
        <el-table-column prop="companyShortName" label="简称" min-width="100" />
        <el-table-column prop="companyType" label="企业类型" min-width="100" />
        <el-table-column prop="unifiedSocialCreditCode" label="统一社会信用代码" min-width="180" />
        <el-table-column prop="legalPerson" label="法定代表人" min-width="100" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="130" />
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedTime" label="更新时间" min-width="160" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-space size="small">
              <el-button link type="primary" size="small" @click="openDetail(row)" :disabled="!canView">
                详情
              </el-button>
              <el-button link type="primary" size="small" @click="openEditDialog(row)" :disabled="!canUpdate">
                编辑
              </el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)" :disabled="!canDelete">
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
          :current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>

      <!-- 新增/编辑弹窗（可拖拽） -->
      <el-dialog
        v-model="editDialog.visible"
        :title="editDialog.isEdit ? '编辑企业' : '新建企业'"
        width="720px"
        destroy-on-close
        draggable
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          class="dialog-form"
          @submit.prevent="submitForm"
        >
          <button type="submit" style="display: none" aria-hidden="true" tabindex="-1" />
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="企业名称" prop="companyName">
                <el-input v-model="form.companyName" placeholder="请输入企业名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="企业编码" prop="companyCode">
                <el-input v-model="form.companyCode" placeholder="请输入企业编码" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="简称" prop="companyShortName">
                <el-input v-model="form.companyShortName" placeholder="请输入简称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="企业类型" prop="companyType">
                <el-input v-model="form.companyType" placeholder="如：有限责任公司" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="统一社会信用代码" prop="unifiedSocialCreditCode">
                <el-input v-model="form.unifiedSocialCreditCode" placeholder="请输入统一社会信用代码" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="纳税人识别号" prop="taxNo">
                <el-input v-model="form.taxNo" placeholder="选填" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="法定代表人" prop="legalPerson">
                <el-input v-model="form.legalPerson" placeholder="请输入法定代表人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="注册地址" prop="regAddress">
            <el-input v-model="form.regAddress" placeholder="请输入注册地址" />
          </el-form-item>
          <el-form-item label="办公地址" prop="officeAddress">
            <el-input v-model="form.officeAddress" placeholder="请输入办公地址" />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="联系邮箱" prop="contactEmail">
                <el-input v-model="form.contactEmail" placeholder="选填" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="官网" prop="website">
                <el-input v-model="form.website" placeholder="选填" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="状态" prop="status" v-if="editDialog.isEdit">
            <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
              <el-option label="启用" value="ENABLED" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">
            {{ editDialog.isEdit ? '保存' : '创建' }}
          </el-button>
        </template>
      </el-dialog>

      <!-- 详情抽屉 -->
      <el-drawer v-model="detailDrawer.visible" title="企业详情" size="560px">
        <el-descriptions v-if="detailDrawer.data" :column="1" border>
          <el-descriptions-item label="企业ID">{{ detailDrawer.data.companyId }}</el-descriptions-item>
          <el-descriptions-item label="企业编码">{{ detailDrawer.data.companyCode }}</el-descriptions-item>
          <el-descriptions-item label="企业名称">{{ detailDrawer.data.companyName }}</el-descriptions-item>
          <el-descriptions-item label="简称">{{ detailDrawer.data.companyShortName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="企业类型">{{ detailDrawer.data.companyType || '—' }}</el-descriptions-item>
          <el-descriptions-item label="统一社会信用代码">{{ detailDrawer.data.unifiedSocialCreditCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="纳税人识别号">{{ detailDrawer.data.taxNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="法定代表人">{{ detailDrawer.data.legalPerson || '—' }}</el-descriptions-item>
          <el-descriptions-item label="注册地址">{{ detailDrawer.data.regAddress || '—' }}</el-descriptions-item>
          <el-descriptions-item label="办公地址">{{ detailDrawer.data.officeAddress || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailDrawer.data.contactPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ detailDrawer.data.contactEmail || '—' }}</el-descriptions-item>
          <el-descriptions-item label="官网">{{ detailDrawer.data.website || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(detailDrawer.data.status)">
              {{ getStatusLabel(detailDrawer.data.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailDrawer.data.createdTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detailDrawer.data.updatedTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detailDrawer.data.remark || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-drawer>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, Download } from '@element-plus/icons-vue';
import { hasPermission } from '@/utils/permission';
import { parsePageResult } from '@/api/common';
import {
  listCompany,
  getCompanyDetail,
  createCompany,
  updateCompany,
  deleteCompany,
  type CompanyListVO,
  type CompanyDetailVO,
  type CreateCompanyReq,
  type UpdateCompanyReq,
} from '@/api/enterprise/company';

const loading = ref(false);
const saving = ref(false);

// 查询条件（与 QueryCompanyParam 对齐）
const query = reactive({
  companyCode: '',
  companyName: '',
  companyType: '',
  status: '' as string,
});

const pageNum = ref(1);
const pageSize = ref(10);
const tableData = ref<CompanyListVO[]>([]);
const total = ref(0);

const editDialog = reactive({
  visible: false,
  isEdit: false,
});

const formRef = ref<FormInstance>();
const form = reactive<CreateCompanyReq & UpdateCompanyReq & { taxNo?: string }>({
  companyId: undefined as unknown as number,
  companyName: '',
  companyCode: '',
  companyShortName: '',
  companyType: '',
  unifiedSocialCreditCode: '',
  taxNo: '',
  legalPerson: '',
  regAddress: '',
  officeAddress: '',
  contactPhone: '',
  contactEmail: '',
  website: '',
  status: 'ENABLED',
  remark: '',
});

const rules: FormRules = {
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
};

const detailDrawer = reactive<{
  visible: boolean;
  data: CompanyDetailVO | null;
}>({
  visible: false,
  data: null,
});

// 权限占位：后续接入权限体系时使用 corp:basic:view/create/update/delete，当前未接入故用 ?? true 放行
const canView = computed(() => hasPermission('corp:basic:view') ?? true);
const canCreate = computed(() => hasPermission('corp:basic:create') ?? true);
const canUpdate = computed(() => hasPermission('corp:basic:update') ?? true);
const canDelete = computed(() => hasPermission('corp:basic:delete') ?? true);

function getStatusLabel(status?: string) {
  const labels: Record<string, string> = {
    ENABLED: '启用',
    DISABLED: '禁用',
  };
  return labels[status ?? ''] ?? status ?? '—';
}

function getStatusTagType(status?: string) {
  const types: Record<string, string> = {
    ENABLED: 'success',
    DISABLED: 'info',
  };
  return (types[status ?? ''] ?? 'info') as 'success' | 'info' | 'warning' | 'danger';
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await listCompany({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      companyCode: query.companyCode || undefined,
      companyName: query.companyName || undefined,
      companyType: query.companyType || undefined,
      status: query.status || undefined,
    });
    const parsed = parsePageResult(res);
    tableData.value = parsed.list;
    total.value = parsed.total;
  } catch (e) {
    tableData.value = [];
    total.value = 0;
    ElMessage.error('加载列表失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  fetchList();
}

function handleReset() {
  query.companyCode = '';
  query.companyName = '';
  query.companyType = '';
  query.status = '';
  pageNum.value = 1;
  fetchList();
}

function handleCurrentChange(page: number) {
  pageNum.value = page;
  fetchList();
}

function handleSizeChange(size: number) {
  pageSize.value = size;
  pageNum.value = 1;
  fetchList();
}

function openCreateDialog() {
  editDialog.isEdit = false;
  editDialog.visible = true;
  resetForm();
}

function openEditDialog(row: CompanyListVO) {
  const id = row.companyId;
  if (id == null) return;
  editDialog.isEdit = true;
  editDialog.visible = true;
  loading.value = true;
  getCompanyDetail(id)
    .then((detail) => {
      Object.assign(form, detail);
      form.companyId = id;
      form.taxNo = detail.taxNo;
    })
    .catch(() => ElMessage.error('获取详情失败'))
    .finally(() => { loading.value = false; });
}

function resetForm() {
  form.companyId = undefined as unknown as number;
  form.companyName = '';
  form.companyCode = '';
  form.companyShortName = '';
  form.companyType = '';
  form.unifiedSocialCreditCode = '';
  form.taxNo = '';
  form.legalPerson = '';
  form.regAddress = '';
  form.officeAddress = '';
  form.contactPhone = '';
  form.contactEmail = '';
  form.website = '';
  form.status = 'ENABLED';
  form.remark = '';
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid || !form.companyName?.trim()) return;

  saving.value = true;
  try {
    if (editDialog.isEdit && form.companyId != null) {
      await updateCompany({
        companyId: form.companyId,
        companyCode: form.companyCode,
        companyName: form.companyName,
        companyShortName: form.companyShortName,
        companyType: form.companyType,
        unifiedSocialCreditCode: form.unifiedSocialCreditCode,
        taxNo: form.taxNo,
        legalPerson: form.legalPerson,
        regAddress: form.regAddress,
        officeAddress: form.officeAddress,
        contactPhone: form.contactPhone,
        contactEmail: form.contactEmail,
        website: form.website,
        status: form.status,
        remark: form.remark,
      });
      ElMessage.success('保存成功');
    } else {
      await createCompany({
        companyName: form.companyName,
        companyCode: form.companyCode,
        companyShortName: form.companyShortName,
        companyType: form.companyType,
        unifiedSocialCreditCode: form.unifiedSocialCreditCode,
        taxNo: form.taxNo,
        legalPerson: form.legalPerson,
        regAddress: form.regAddress,
        officeAddress: form.officeAddress,
        contactPhone: form.contactPhone,
        contactEmail: form.contactEmail,
        website: form.website,
        status: form.status,
        remark: form.remark,
      });
      ElMessage.success('创建成功');
    }
    editDialog.visible = false;
    fetchList();
  } catch (e) {
    ElMessage.error(editDialog.isEdit ? '保存失败' : '创建失败');
  } finally {
    saving.value = false;
  }
}

function openDetail(row: CompanyListVO) {
  const id = row.companyId;
  if (id == null) return;
  detailDrawer.visible = true;
  detailDrawer.data = null;
  getCompanyDetail(id)
    .then((detail) => { detailDrawer.data = detail; })
    .catch(() => ElMessage.error('获取详情失败'));
}

function handleDelete(row: CompanyListVO) {
  const id = row.companyId;
  const name = row.companyName ?? '该企业';
  if (id == null) return;
  ElMessageBox.confirm(`确认删除企业「${name}」吗？此操作不可恢复。`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteCompany(id);
        ElMessage.success('删除成功');
        fetchList();
      } catch (e) {
        ElMessage.error('删除失败');
      }
    })
    .catch(() => {});
}

function handleExport() {
  ElMessage.info('导出功能开发中...');
}

onMounted(() => {
  fetchList();
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

.search-bar {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.dialog-form {
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style>
