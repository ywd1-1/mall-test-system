<template>
  <section class="page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Product Catalog</div>
        <h1 class="page-title">商品</h1>
        <p class="page-desc">浏览可售商品，查看详情后加入购物车并完成下单流程</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="refreshAll">刷新数据</el-button>
    </div>

    <div class="shop-panel">
      <div class="toolbar filter-toolbar">
        <div class="toolbar-title">
          <strong>可售商品</strong>
          <span>按关键词或分类筛选商品</span>
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
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <div v-loading="loading" class="product-grid">
        <article v-for="item in products" :key="item.id" class="product-card">
          <img class="product-card__image" :src="item.imageUrl" :alt="item.name" loading="lazy" />
          <div class="product-card__body">
            <div class="product-card__meta">
              <el-tag effect="plain">{{ item.category || '未分类' }}</el-tag>
              <span>库存 {{ item.stock }}</span>
            </div>
            <h2>{{ item.name }}</h2>
            <p>{{ item.description || '暂无商品描述' }}</p>
            <div class="product-card__footer">
              <span class="product-card__price">¥{{ item.price }}</span>
              <div class="product-card__actions">
                <el-button :icon="View" @click="$router.push(`/products/${item.id}`)">详情</el-button>
                <el-button type="primary" :icon="ShoppingCart" :disabled="item.stock <= 0" @click="add(item)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>
        </article>
        <template v-if="!loading && products.length === 0">
          <div class="empty-state">
            <div class="empty-state__body">
              <el-icon><Goods /></el-icon>
              <strong>暂无匹配商品</strong>
              <p>调整关键词或分类后重新查询。</p>
            </div>
          </div>
        </template>
      </div>

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
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Goods, Refresh, Search, ShoppingCart, View } from '@element-plus/icons-vue'
import { listProducts } from '../api/products'
import { addCart } from '../api/cart'

const categories = ['手机', '电脑', '配件', '生活用品']
const products = ref([])
const loading = ref(false)
const filters = reactive({
  keyword: '',
  category: '',
  page: 1,
  size: 8
})
const pageData = reactive({
  total: 0,
  pages: 0
})

function buildProductParams() {
  return {
    keyword: filters.keyword || undefined,
    category: filters.category || undefined,
    page: filters.page,
    size: filters.size
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const data = await listProducts(buildProductParams())
    products.value = data?.records || []
    pageData.total = Number(data?.total || 0)
    pageData.pages = Number(data?.pages || 0)
  } finally {
    loading.value = false
  }
}

async function refreshAll() {
  await loadProducts()
}

function handleSearch() {
  filters.page = 1
  loadProducts()
}

function resetFilters() {
  filters.keyword = ''
  filters.category = ''
  filters.page = 1
  loadProducts()
}

function handleSizeChange(size) {
  filters.size = size
  filters.page = 1
  loadProducts()
}

async function add(row) {
  await addCart({ productId: row.id, quantity: 1 })
  ElMessage.success('已加入购物车')
}

onMounted(refreshAll)
</script>
