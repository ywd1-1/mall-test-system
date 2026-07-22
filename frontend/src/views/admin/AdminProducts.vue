<template>
  <section class="page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Admin Products</div>
        <h1 class="page-title">商品管理</h1>
        <p class="page-desc">维护商品分类、库存和上架状态，支持搜索与分页校验</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadProducts">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增商品</el-button>
      </div>
    </div>

    <div class="metric-grid metric-grid--four">
      <div class="metric-card">
        <div class="metric-card__icon"><el-icon><Goods /></el-icon></div>
        <span>当前商品</span>
        <strong>{{ products.length }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon success"><el-icon><CircleCheck /></el-icon></div>
        <span>已上架</span>
        <strong>{{ onSaleCount }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon"><el-icon><Box /></el-icon></div>
        <span>已下架</span>
        <strong>{{ offShelfCount }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon warning"><el-icon><Warning /></el-icon></div>
        <span>低库存</span>
        <strong>{{ lowStockCount }}</strong>
      </div>
    </div>

    <div class="table-panel">
      <div class="toolbar filter-toolbar">
        <div class="toolbar-title">
          <strong>商品维护列表</strong>
          <span>库存可内联修改，状态变更会立即同步</span>
        </div>
        <div class="filter-group">
          <el-input
            v-model="filters.keyword"
            clearable
            class="filter-input"
            placeholder="搜索商品名称或描述"
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
          <el-select v-model="filters.category" clearable placeholder="全部分类" class="filter-select">
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filters.status" clearable placeholder="全部状态" class="filter-select">
            <el-option label="上架" value="ON_SALE" />
            <el-option label="下架" value="OFF_SHELF" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="products" row-key="id" empty-text="暂无商品">
        <template #empty>
          <div class="empty-state">
            <div class="empty-state__body">
              <el-icon><Goods /></el-icon>
              <strong>暂无商品</strong>
              <p>新增商品后可验证前台浏览、购物车和下单流程。</p>
              <el-button type="primary" :icon="Plus" @click="openDialog()">新增商品</el-button>
            </div>
          </div>
        </template>
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="product-cell">
              <img class="product-image" :src="row.imageUrl" :alt="row.name" loading="lazy" />
              <div>
                <strong>{{ row.name }}</strong>
                <span>{{ row.description }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.category || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="150">
          <template #default="{ row }">
            <el-input-number
              v-model="row.stock"
              :min="0"
              :step="1"
              size="small"
              controls-position="right"
              @change="changeStock(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ON_SALE' ? 'success' : 'info'" effect="plain">
              {{ row.status === 'ON_SALE' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <el-button :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button
              :type="row.status === 'ON_SALE' ? 'warning' : 'success'"
              plain
              @click="toggleStatus(row)"
            >
              {{ row.status === 'ON_SALE' ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" :icon="Delete" @click="confirmDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="filters.page"
          v-model:page-size="filters.size"
          background
          :page-sizes="[8, 12, 20]"
          layout="total, sizes, prev, pager, next"
          :total="pageData.total"
          @size-change="handleSizeChange"
          @current-change="loadProducts"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" maxlength="100" show-word-limit />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="分类" prop="category">
            <el-select v-model="form.category" class="full-field">
              <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" class="full-field">
              <el-option label="上架" value="ON_SALE" />
              <el-option label="下架" value="OFF_SHELF" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="价格" prop="price">
            <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="10" class="full-field" />
          </el-form-item>
          <el-form-item label="库存" prop="stock">
            <el-input-number v-model="form.stock" :min="0" :step="1" class="full-field" />
          </el-form-item>
        </div>
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input v-model="form.imageUrl" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveProduct">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box, CircleCheck, Delete, Edit, Goods, Plus, Refresh, Search, Warning } from '@element-plus/icons-vue'
import {
  createProduct,
  deleteProduct,
  listAdminProducts,
  updateProduct,
  updateProductStatus,
  updateProductStock
} from '../../api/products'

const categories = ['手机', '电脑', '配件', '生活用品']
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const products = ref([])
const filters = reactive({
  keyword: '',
  category: '',
  status: '',
  page: 1,
  size: 8
})
const pageData = reactive({
  total: 0,
  pages: 0
})
const form = reactive(defaultForm())

const onSaleCount = computed(() => products.value.filter((item) => item.status === 'ON_SALE').length)
const offShelfCount = computed(() => products.value.filter((item) => item.status === 'OFF_SHELF').length)
const lowStockCount = computed(() => products.value.filter((item) => Number(item.stock) <= 10).length)

function validateStock(_rule, value, callback) {
  if (value === null || value === undefined || value === '') {
    callback(new Error('请输入库存'))
    return
  }
  if (!Number.isInteger(Number(value)) || Number(value) < 0) {
    callback(new Error('库存必须为非负整数'))
    return
  }
  callback()
}

const rules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { max: 100, message: '商品名称不能超过 100 个字符', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ validator: validateStock, trigger: ['blur', 'change'] }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function defaultForm() {
  return {
    id: null,
    name: '',
    category: '配件',
    price: 1,
    stock: 0,
    status: 'ON_SALE',
    imageUrl: 'https://picsum.photos/seed/new-product/480/320',
    description: ''
  }
}

function buildParams() {
  return {
    keyword: filters.keyword || undefined,
    category: filters.category || undefined,
    status: filters.status || undefined,
    page: filters.page,
    size: filters.size
  }
}

function openDialog(row) {
  Object.assign(form, row ? { ...defaultForm(), ...row } : defaultForm())
  dialogVisible.value = true
}

async function loadProducts() {
  loading.value = true
  try {
    const data = await listAdminProducts(buildParams())
    products.value = data?.records || []
    pageData.total = Number(data?.total || 0)
    pageData.pages = Number(data?.pages || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  filters.page = 1
  loadProducts()
}

function resetFilters() {
  filters.keyword = ''
  filters.category = ''
  filters.status = ''
  filters.page = 1
  loadProducts()
}

function handleSizeChange(size) {
  filters.size = size
  filters.page = 1
  loadProducts()
}

async function saveProduct() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateProduct(form.id, form)
    } else {
      await createProduct(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadProducts()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const nextStatus = row.status === 'ON_SALE' ? 'OFF_SHELF' : 'ON_SALE'
  if (nextStatus === 'OFF_SHELF') {
    await ElMessageBox.confirm(`确认下架「${row.name}」？下架后普通用户不可见。`, '下架商品', {
      type: 'warning'
    })
  }
  const updated = await updateProductStatus(row.id, nextStatus)
  Object.assign(row, updated)
  ElMessage.success(nextStatus === 'ON_SALE' ? '商品已上架' : '商品已下架')
}

async function changeStock(row) {
  if (!Number.isInteger(Number(row.stock)) || Number(row.stock) < 0) {
    ElMessage.warning('库存必须为非负整数')
    await loadProducts()
    return
  }
  const updated = await updateProductStock(row.id, row.stock)
  Object.assign(row, updated)
  ElMessage.success('库存已更新')
}

async function confirmDelete(row) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？删除后商品会下架并从购物车移除。`, '删除商品', {
    type: 'warning'
  })
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  await loadProducts()
}

onMounted(loadProducts)
</script>
