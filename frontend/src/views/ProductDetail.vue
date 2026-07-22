<template>
  <section class="page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Product Detail</div>
        <h1 class="page-title">商品详情</h1>
        <p class="page-desc">确认库存和数量后加入购物车</p>
      </div>
      <el-button :icon="Back" @click="$router.back()">返回</el-button>
    </div>

    <div class="detail-panel" v-loading="loading">
      <div v-if="product" class="detail-grid">
        <img class="detail-image" :src="product.imageUrl" :alt="product.name" loading="lazy" />
        <div class="detail-meta">
          <div class="detail-title-row">
            <h2>{{ product.name }}</h2>
            <el-tag :type="statusTagType(product.status)" effect="light">{{ statusText(product.status) }}</el-tag>
          </div>
          <div class="price">¥{{ product.price }}</div>
          <p class="detail-description">{{ product.description }}</p>
          <div class="detail-attributes">
            <div class="detail-attribute">
              <span>商品分类</span>
              <strong>{{ product.category || '未分类' }}</strong>
            </div>
            <div class="detail-attribute">
              <span>可用库存</span>
              <strong>{{ product.stock }}</strong>
            </div>
          </div>
          <div class="detail-actions">
            <el-input-number
              v-model="quantity"
              class="quantity-control"
              :min="1"
              :max="Math.max(product.stock, 1)"
              :disabled="product.stock <= 0"
            />
            <el-button type="primary" :icon="ShoppingCart" :disabled="product.stock <= 0" @click="add">
              加入购物车
            </el-button>
          </div>
        </div>
      </div>
      <div v-else-if="!loading" class="empty-state">
        <div class="empty-state__body">
          <el-icon><Goods /></el-icon>
          <strong>未找到商品</strong>
          <p>返回商品列表后重新选择要验证的商品。</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Goods, ShoppingCart } from '@element-plus/icons-vue'
import { getProduct } from '../api/products'
import { addCart } from '../api/cart'

const route = useRoute()
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)

function statusText(status) {
  return status === 'ON_SALE' ? '上架' : '下架'
}

function statusTagType(status) {
  return status === 'ON_SALE' ? 'success' : 'info'
}

async function loadProduct() {
  loading.value = true
  try {
    product.value = await getProduct(route.params.id)
  } finally {
    loading.value = false
  }
}

async function add() {
  await addCart({ productId: product.value.id, quantity: quantity.value })
  ElMessage.success('已加入购物车')
}

onMounted(loadProduct)
</script>
