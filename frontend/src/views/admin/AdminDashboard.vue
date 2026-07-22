<template>
  <section class="page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Dashboard</div>
        <h1 class="page-title">工作台</h1>
        <p class="page-desc">查看商品、库存和订单状态概览，快速进入后台管理流程</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadStatistics">刷新</el-button>
    </div>

    <div class="metric-grid metric-grid--four" v-loading="loading">
      <div class="metric-card">
        <div class="metric-card__icon"><el-icon><Goods /></el-icon></div>
        <span>商品总数</span>
        <strong>{{ stats.productTotal }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon warning"><el-icon><Warning /></el-icon></div>
        <span>低库存商品数</span>
        <strong>{{ stats.lowStockProductCount }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon"><el-icon><Tickets /></el-icon></div>
        <span>订单数量</span>
        <strong>{{ stats.orderCount }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon accent"><el-icon><Clock /></el-icon></div>
        <span>待支付订单数</span>
        <strong>{{ stats.pendingOrderCount }}</strong>
      </div>
    </div>

    <div class="admin-shortcuts">
      <button type="button" class="admin-shortcut" @click="$router.push('/admin/products')">
        <el-icon><Box /></el-icon>
        <strong>商品管理</strong>
        <span>维护商品、库存、上下架状态</span>
      </button>
      <button type="button" class="admin-shortcut" @click="$router.push('/admin/orders')">
        <el-icon><List /></el-icon>
        <strong>订单管理</strong>
        <span>筛选用户订单并查看明细</span>
      </button>
      <button type="button" class="admin-shortcut" @click="$router.push('/admin/users')">
        <el-icon><User /></el-icon>
        <strong>用户管理</strong>
        <span>查询普通用户并维护启禁用状态</span>
      </button>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Box, Clock, Goods, List, Refresh, Tickets, User, Warning } from '@element-plus/icons-vue'
import { getStatistics } from '../../api/statistics'

const loading = ref(false)
const stats = reactive({
  productTotal: 0,
  lowStockProductCount: 0,
  orderCount: 0,
  pendingOrderCount: 0
})

async function loadStatistics() {
  loading.value = true
  try {
    const data = await getStatistics()
    Object.assign(stats, data || {})
  } finally {
    loading.value = false
  }
}

onMounted(loadStatistics)
</script>
