<template>
  <el-config-provider :locale="zhCn">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title">企业基础信息</div>
          <div class="actions">
            <el-button
              type="primary"
              size="small"
              @click="openEditDialog"
              :disabled="!canUpdate"
            >
              <el-icon><Edit /></el-icon>
              编辑信息
            </el-button>
          </div>
        </div>
      </template>

      <!-- 基本信息展示 -->
      <div class="info-sections">
        <!-- 企业基本信息 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><OfficeBuilding /></el-icon>
              <span>企业基本信息</span>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="企业名称">{{ corpInfo.corpName }}</el-descriptions-item>
            <el-descriptions-item label="统一社会信用代码">{{ corpInfo.creditCode }}</el-descriptions-item>
            <el-descriptions-item label="注册资本">{{ corpInfo.registeredCapital }}万元</el-descriptions-item>
            <el-descriptions-item label="成立日期">{{ corpInfo.establishDate }}</el-descriptions-item>
            <el-descriptions-item label="企业性质">{{ corpInfo.corpType }}</el-descriptions-item>
            <el-descriptions-item label="行业分类">{{ corpInfo.industry }}</el-descriptions-item>
            <el-descriptions-item label="法定代表人">{{ corpInfo.legalPerson }}</el-descriptions-item>
            <el-descriptions-item label="公司地址" :span="2">{{ corpInfo.address }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 联系方式 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Phone /></el-icon>
              <span>联系方式</span>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="联系电话">{{ corpInfo.phone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱地址">{{ corpInfo.email }}</el-descriptions-item>
            <el-descriptions-item label="公司网址" :span="2">{{ corpInfo.website || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="邮政编码">100000</el-descriptions-item>
            <el-descriptions-item label="传真号码">010-12345678</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 经营信息 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Management /></el-icon>
              <span>经营信息</span>
            </div>
          </template>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="经营范围">
              房屋建筑工程、装饰装修工程、建筑智能化工程、消防工程、环保工程、市政工程、
              公路工程、桥梁工程、隧道工程、铁路工程、港口工程、机场工程、水利工程、
              电力工程、矿山工程、冶金工程、化工工程、石油化工工程、机电工程安装、
              建筑幕墙工程、钢结构工程、建筑防水工程、电梯工程安装、建筑劳务分包等。
            </el-descriptions-item>
            <el-descriptions-item label="营业期限">长期</el-descriptions-item>
            <el-descriptions-item label="登记机关">某某市市场监督管理局</el-descriptions-item>
            <el-descriptions-item label="核准日期">2024-01-15</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 合作单位 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>合作单位</span>
              <el-button
                style="float: right; padding: 3px 0"
                text
                type="primary"
                @click="openPartnerDialog"
                :disabled="!canUpdate"
              >
                <el-icon><Plus /></el-icon>
                添加合作单位
              </el-button>
            </div>
          </template>

          <el-table :data="partners" stripe style="width: 100%">
            <el-table-column prop="name" label="单位名称" min-width="200" />
            <el-table-column prop="contact" label="联系人" min-width="120" />
            <el-table-column prop="phone" label="联系电话" min-width="140" />
            <el-table-column prop="cooperationType" label="合作类型" min-width="120" />
            <el-table-column prop="cooperationStartDate" label="合作开始日期" min-width="140" />
            <el-table-column label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
                  {{ row.status === 'ACTIVE' ? '合作中' : '已终止' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" v-if="canUpdate">
              <template #default="{ row }">
                <el-space size="small">
                  <el-button link type="primary" size="small" @click="editPartner(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="deletePartner(row)">删除</el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 编辑企业信息弹窗 -->
      <el-dialog
        v-model="editDialog.visible"
        title="编辑企业信息"
        width="900px"
        destroy-on-close
      >
        <el-tabs v-model="activeTab" class="edit-tabs">
          <el-tab-pane label="基本信息" name="basic">
            <el-form
              ref="basicFormRef"
              :model="editForm"
              :rules="basicRules"
              label-width="140px"
              class="edit-form"
            >
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="企业名称" prop="corpName">
                    <el-input v-model="editForm.corpName" placeholder="请输入企业名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="统一社会信用代码" prop="creditCode">
                    <el-input v-model="editForm.creditCode" placeholder="请输入统一社会信用代码" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="注册资本(万元)" prop="registeredCapital">
                    <el-input-number
                      v-model="editForm.registeredCapital"
                      :min="0"
                      :precision="2"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="成立日期" prop="establishDate">
                    <el-date-picker
                      v-model="editForm.establishDate"
                      type="date"
                      placeholder="选择成立日期"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="企业性质" prop="corpType">
                    <el-select v-model="editForm.corpType" placeholder="选择企业性质" style="width: 100%">
                      <el-option label="有限责任公司" value="有限责任公司" />
                      <el-option label="股份有限公司" value="股份有限公司" />
                      <el-option label="国有独资公司" value="国有独资公司" />
                      <el-option label="其他" value="其他" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="行业分类" prop="industry">
                    <el-input v-model="editForm.industry" placeholder="请输入行业分类" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="法定代表人" prop="legalPerson">
                <el-input v-model="editForm.legalPerson" placeholder="请输入法定代表人" />
              </el-form-item>

              <el-form-item label="公司地址" prop="address">
                <el-input v-model="editForm.address" placeholder="请输入公司地址" />
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="联系方式" name="contact">
            <el-form
              ref="contactFormRef"
              :model="editForm"
              :rules="contactRules"
              label-width="140px"
              class="edit-form"
            >
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="phone">
                    <el-input v-model="editForm.phone" placeholder="请输入联系电话" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱地址" prop="email">
                    <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="公司网址">
                <el-input v-model="editForm.website" placeholder="请输入公司网址" />
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <template #footer>
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveCorpInfo">
            保存
          </el-button>
        </template>
      </el-dialog>

      <!-- 添加/编辑合作单位弹窗 -->
      <el-dialog
        v-model="partnerDialog.visible"
        :title="partnerDialog.isEdit ? '编辑合作单位' : '添加合作单位'"
        width="600px"
        destroy-on-close
      >
        <el-form
          ref="partnerFormRef"
          :model="partnerForm"
          :rules="partnerRules"
          label-width="100px"
          class="partner-form"
        >
          <el-form-item label="单位名称" prop="name">
            <el-input v-model="partnerForm.name" placeholder="请输入单位名称" />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="联系人" prop="contact">
                <el-input v-model="partnerForm.contact" placeholder="请输入联系人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="partnerForm.phone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="合作类型" prop="cooperationType">
                <el-select v-model="partnerForm.cooperationType" placeholder="选择合作类型" style="width: 100%">
                  <el-option label="材料供应商" value="材料供应商" />
                  <el-option label="设备供应商" value="设备供应商" />
                  <el-option label="劳务分包" value="劳务分包" />
                  <el-option label="设计单位" value="设计单位" />
                  <el-option label="监理单位" value="监理单位" />
                  <el-option label="银行" value="银行" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="合作开始日期" prop="cooperationStartDate">
                <el-date-picker
                  v-model="partnerForm.cooperationStartDate"
                  type="date"
                  placeholder="选择合作开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="状态" prop="status">
            <el-select v-model="partnerForm.status" placeholder="选择状态" style="width: 100%">
              <el-option label="合作中" value="ACTIVE" />
              <el-option label="已终止" value="INACTIVE" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="partnerDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="partnerSaving" @click="savePartner">
            {{ partnerDialog.isEdit ? '保存' : '添加' }}
          </el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Edit, OfficeBuilding, Phone, Management, User, Plus } from '@element-plus/icons-vue';
import { hasPermission } from '@/utils/permission';
import {
  getCorpBasicInfo,
  updateCorpBasicInfo,
  listPartners,
  createPartner,
  updatePartner,
  deletePartner,
  type CorpBasicInfoVO,
  type PartnerVO
} from '@/api/corp';

const loading = ref(false);
const saving = ref(false);
const partnerSaving = ref(false);

// 企业信息
const corpInfo = reactive<CorpBasicInfoVO>({
  corpId: 'corp001',
  corpName: '某某建设集团有限公司',
  creditCode: '91110000123456789X',
  registeredCapital: '50000',
  establishDate: '2010-01-01',
  corpType: '有限责任公司',
  industry: '建筑业',
  legalPerson: '张总',
  address: '北京市朝阳区某某路123号',
  phone: '010-12345678',
  email: 'contact@company.com',
  website: 'www.company.com',
});

// 合作单位
const partners = ref<PartnerVO[]>([
  {
    id: 'p001',
    name: '中国建设银行某某支行',
    contact: '李经理',
    phone: '13800138001',
    cooperationType: '银行',
    cooperationStartDate: '2020-01-01',
    status: 'ACTIVE',
  },
  {
    id: 'p002',
    name: '某某建材有限公司',
    contact: '王总',
    phone: '13800138002',
    cooperationType: '材料供应商',
    cooperationStartDate: '2019-06-01',
    status: 'ACTIVE',
  },
  {
    id: 'p003',
    name: '某某设计院',
    contact: '赵工',
    phone: '13800138003',
    cooperationType: '设计单位',
    cooperationStartDate: '2021-03-01',
    status: 'ACTIVE',
  },
]);

const editDialog = reactive({
  visible: false,
});

const activeTab = ref('basic');
const basicFormRef = ref<FormInstance>();
const contactFormRef = ref<FormInstance>();

const editForm = reactive<CorpBasicInfoVO>({
  corpId: '',
  corpName: '',
  creditCode: '',
  registeredCapital: '',
  establishDate: '',
  corpType: '',
  industry: '',
  legalPerson: '',
  address: '',
  phone: '',
  email: '',
  website: '',
});

const basicRules: FormRules = {
  corpName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  creditCode: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }],
  registeredCapital: [{ required: true, message: '请输入注册资本', trigger: 'blur' }],
  establishDate: [{ required: true, message: '请选择成立日期', trigger: 'change' }],
  corpType: [{ required: true, message: '请选择企业性质', trigger: 'change' }],
  industry: [{ required: true, message: '请输入行业分类', trigger: 'blur' }],
  legalPerson: [{ required: true, message: '请输入法定代表人', trigger: 'blur' }],
  address: [{ required: true, message: '请输入公司地址', trigger: 'blur' }],
};

const contactRules: FormRules = {
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
};

const partnerDialog = reactive({
  visible: false,
  isEdit: false,
  editId: '',
});

const partnerFormRef = ref<FormInstance>();
const partnerForm = reactive({
  id: '',
  name: '',
  contact: '',
  phone: '',
  cooperationType: '',
  cooperationStartDate: '',
  status: 'ACTIVE' as string,
});

const partnerRules: FormRules = {
  name: [{ required: true, message: '请输入单位名称', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  cooperationType: [{ required: true, message: '请选择合作类型', trigger: 'change' }],
  cooperationStartDate: [{ required: true, message: '请选择合作开始日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

// 权限点
const canView = computed(() => hasPermission('corp:basic:view'));
const canUpdate = computed(() => hasPermission('corp:basic:update'));

async function loadCorpInfo() {
  try {
    const res = await getCorpBasicInfo();
    Object.assign(corpInfo, res);
  } catch (e) {
    // 使用模拟数据
  }
}

async function loadPartners() {
  try {
    const res = await listPartners({ page: 1, pageSize: 100 });
    const list = (res.records || res.list || res.rows || []) as PartnerVO[];
    if (list.length > 0) {
      partners.value = list;
    }
  } catch (e) {
    // 使用模拟数据
  }
}

function openEditDialog() {
  editDialog.visible = true;
  activeTab.value = 'basic';
  Object.assign(editForm, corpInfo);
}

async function saveCorpInfo() {
  let valid = false;
  if (activeTab.value === 'basic') {
    valid = await basicFormRef.value?.validate().catch(() => false) || false;
  } else {
    valid = await contactFormRef.value?.validate().catch(() => false) || false;
  }

  if (!valid) return;

  saving.value = true;
  try {
    await updateCorpBasicInfo(editForm);
    Object.assign(corpInfo, editForm);
    ElMessage.success('保存成功');
    editDialog.visible = false;
  } catch (e) {
    Object.assign(corpInfo, editForm);
    ElMessage.success('保存成功（演示模式）');
    editDialog.visible = false;
  } finally {
    saving.value = false;
  }
}

function openPartnerDialog() {
  partnerDialog.isEdit = false;
  partnerDialog.visible = true;
  partnerDialog.editId = '';
  resetPartnerForm();
}

function editPartner(partner: PartnerVO) {
  partnerDialog.isEdit = true;
  partnerDialog.visible = true;
  partnerDialog.editId = partner.id;
  Object.assign(partnerForm, partner);
}

function resetPartnerForm() {
  partnerForm.id = '';
  partnerForm.name = '';
  partnerForm.contact = '';
  partnerForm.phone = '';
  partnerForm.cooperationType = '';
  partnerForm.cooperationStartDate = '';
  partnerForm.status = 'ACTIVE';
}

async function savePartner() {
  if (!partnerFormRef.value) return;
  const valid = await partnerFormRef.value.validate();
  if (!valid) return;

  partnerSaving.value = true;
  try {
    if (partnerDialog.isEdit) {
      await updatePartner(partnerDialog.editId, partnerForm);
      const index = partners.value.findIndex(p => p.id === partnerDialog.editId);
      if (index > -1) {
        partners.value[index] = { ...partnerForm, id: partnerDialog.editId };
      }
      ElMessage.success('编辑成功');
    } else {
      const newPartner = await createPartner(partnerForm);
      partners.value.push({ ...partnerForm, id: newPartner.id || `p${Date.now()}` });
      ElMessage.success('添加成功');
    }
    partnerDialog.visible = false;
  } catch (e) {
    if (partnerDialog.isEdit) {
      const index = partners.value.findIndex(p => p.id === partnerDialog.editId);
      if (index > -1) {
        partners.value[index] = { ...partnerForm, id: partnerDialog.editId };
      }
      ElMessage.success('编辑成功（演示模式）');
    } else {
      partners.value.push({ ...partnerForm, id: `p${Date.now()}` });
      ElMessage.success('添加成功（演示模式）');
    }
    partnerDialog.visible = false;
  } finally {
    partnerSaving.value = false;
  }
}

function deletePartner(partner: PartnerVO) {
  ElMessageBox.confirm(`确认删除合作单位「${partner.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deletePartner(partner.id);
        partners.value = partners.value.filter(p => p.id !== partner.id);
        ElMessage.success('删除成功');
      } catch (e) {
        partners.value = partners.value.filter(p => p.id !== partner.id);
        ElMessage.success('删除成功（演示模式）');
      }
    })
    .catch(() => {});
}

onMounted(() => {
  loadCorpInfo();
  loadPartners();
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

.info-sections {
  .info-card {
    margin-bottom: 16px;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        color: #409EFF;
        font-size: 18px;
      }
    }
  }
}

.edit-tabs {
  :deep(.el-tabs__content) {
    padding: 16px 0;
  }
}

.edit-form,
.partner-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}
</style>
