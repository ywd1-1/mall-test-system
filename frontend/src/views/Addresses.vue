<template>
  <section class="page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Delivery Addresses</div>
        <h1 class="page-title">收货地址</h1>
        <p class="page-desc">维护下单地址；历史订单使用地址快照，不受后续修改影响</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadAddresses">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增地址</el-button>
      </div>
    </div>

    <div v-loading="loading" class="address-grid">
      <article v-for="address in addresses" :key="address.id" class="address-card">
        <div class="address-card__head">
          <div>
            <strong>{{ address.recipientName }}</strong>
            <span>{{ address.phone }}</span>
          </div>
          <el-tag v-if="address.isDefault" type="success" effect="light">默认地址</el-tag>
        </div>
        <p>{{ fullAddress(address) }}</p>
        <div class="address-card__actions">
          <el-button v-if="!address.isDefault" :icon="Check" @click="makeDefault(address)">设为默认</el-button>
          <el-button :icon="Edit" @click="openDialog(address)">编辑</el-button>
          <el-button type="danger" plain :icon="Delete" @click="confirmDelete(address)">删除</el-button>
        </div>
      </article>

      <div v-if="!loading && !addresses.length" class="empty-state address-grid__empty">
        <div class="empty-state__body">
          <el-icon><Location /></el-icon>
          <strong>暂无收货地址</strong>
          <p>新增地址后才能创建订单。</p>
          <el-button type="primary" :icon="Plus" @click="openDialog()">新增地址</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑收货地址' : '新增收货地址'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-row">
          <el-form-item label="收货人" prop="recipientName">
            <el-input v-model="form.recipientName" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" maxlength="20" />
          </el-form-item>
        </div>
        <div class="form-row form-row--three">
          <el-form-item label="省份" prop="province"><el-input v-model="form.province" maxlength="50" /></el-form-item>
          <el-form-item label="城市" prop="city"><el-input v-model="form.city" maxlength="50" /></el-form-item>
          <el-form-item label="区县" prop="district"><el-input v-model="form.district" maxlength="50" /></el-form-item>
        </div>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="form.detailAddress" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" active-text="设为默认地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveAddress">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Edit, Location, Plus, Refresh } from '@element-plus/icons-vue'
import { createAddress, deleteAddress, listAddresses, setDefaultAddress, updateAddress } from '../api/addresses'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const addresses = ref([])
const form = reactive(defaultForm())

const rules = {
  recipientName: [
    { required: true, message: '请输入收货人', trigger: 'blur' },
    { min: 2, max: 50, message: '长度为 2 到 50 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { min: 6, max: 20, message: '长度为 6 到 20 个字符', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 200, message: '长度为 5 到 200 个字符', trigger: 'blur' }
  ]
}

function defaultForm() {
  return {
    id: null,
    recipientName: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    isDefault: false
  }
}

function fullAddress(address) {
  return `${address.province}${address.city}${address.district}${address.detailAddress}`
}

async function loadAddresses() {
  loading.value = true
  try {
    addresses.value = await listAddresses()
  } finally {
    loading.value = false
  }
}

function openDialog(address) {
  Object.assign(form, address ? { ...defaultForm(), ...address } : defaultForm())
  dialogVisible.value = true
}

async function saveAddress() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) await updateAddress(form.id, form)
    else await createAddress(form)
    ElMessage.success('地址已保存')
    dialogVisible.value = false
    await loadAddresses()
  } finally {
    saving.value = false
  }
}

async function makeDefault(address) {
  await setDefaultAddress(address.id)
  ElMessage.success('默认地址已更新')
  await loadAddresses()
}

async function confirmDelete(address) {
  await ElMessageBox.confirm(`确认删除 ${address.recipientName} 的收货地址？`, '删除地址', { type: 'warning' })
  await deleteAddress(address.id)
  ElMessage.success('地址已删除')
  await loadAddresses()
}

onMounted(loadAddresses)
</script>
