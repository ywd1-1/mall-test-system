<template>
  <section class="page admin-orders-page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Admin Orders</div>
        <h1 class="page-title">订单管理</h1>
        <p class="page-desc">按订单号、用户名、状态和下单时间组合查询</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadOrders">刷新</el-button>
    </div>

    <div class="metric-grid metric-grid--four">
      <div class="metric-card"><div class="metric-card__icon"><el-icon><Tickets /></el-icon></div><span>筛选结果</span><strong>{{ pageData.total }}</strong></div>
      <div class="metric-card"><div class="metric-card__icon warning"><el-icon><Clock /></el-icon></div><span>本页待支付</span><strong>{{ countByStatus('CREATED') }}</strong></div>
      <div class="metric-card"><div class="metric-card__icon success"><el-icon><Van /></el-icon></div><span>本页待发货</span><strong>{{ countByStatus('PAID') }}</strong></div>
      <div class="metric-card"><div class="metric-card__icon danger"><el-icon><Money /></el-icon></div><span>本页金额</span><strong>¥{{ pageAmount }}</strong></div>
    </div>

    <div class="table-panel">
      <div class="toolbar filter-toolbar">
        <div class="toolbar-title"><strong>全部用户订单</strong><span>支持多条件叠加筛选</span></div>
        <div class="filter-group filter-group--admin-orders">
          <el-input v-model="filters.orderNo" clearable class="filter-input filter-input--short" placeholder="订单号" :prefix-icon="Search" @keyup.enter="handleSearch" />
          <el-input v-model="filters.username" clearable class="filter-input filter-input--short" placeholder="用户名" :prefix-icon="User" @keyup.enter="handleSearch" />
          <el-select v-model="filters.status" clearable placeholder="全部状态" class="filter-select">
            <el-option v-for="item in ORDER_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker
            v-model="filters.dateRange"
            type="datetimerange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            range-separator="至"
            class="filter-date-range"
          />
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="orders" row-key="id" empty-text="暂无订单">
        <el-table-column type="expand">
          <template #default="{ row }">
            <el-table :data="row.items" row-key="id" class="inner-table">
              <el-table-column prop="productName" label="商品名称" min-width="180" />
              <el-table-column label="单价" width="120"><template #default="{ row: item }">¥{{ item.price }}</template></el-table-column>
              <el-table-column prop="quantity" label="数量" width="100" />
              <el-table-column label="小计" width="120"><template #default="{ row: item }"><span class="price">¥{{ item.subtotal }}</span></template></el-table-column>
            </el-table>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" min-width="210" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column label="金额" width="120"><template #default="{ row }"><span class="price">¥{{ row.totalAmount }}</span></template></el-table-column>
        <el-table-column label="状态" width="120"><template #default="{ row }"><el-tag :type="orderStatusTagType(row.status)" effect="light">{{ orderStatusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="120" fixed="right"><template #default="{ row }"><el-button :icon="View" @click="$router.push(`/admin/orders/${row.id}`)">详情</el-button></template></el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="filters.page"
          v-model:page-size="filters.size"
          background
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="pageData.total"
          @size-change="handleSizeChange"
          @current-change="loadOrders"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Clock, Money, Refresh, Search, Tickets, User, Van, View } from '@element-plus/icons-vue'
import { listAdminOrders } from '../../api/orders'
import { ORDER_STATUS_OPTIONS, orderStatusTagType, orderStatusText } from '../../utils/order'

const loading = ref(false)
const orders = ref([])
const filters = reactive({ orderNo: '', username: '', status: '', dateRange: [], page: 1, size: 10 })
const pageData = reactive({ total: 0, pages: 0 })

const pageAmount = computed(() => orders.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0).toFixed(2))
const countByStatus = (status) => orders.value.filter((item) => item.status === status).length

function buildParams() {
  return {
    orderNo: filters.orderNo || undefined,
    username: filters.username || undefined,
    status: filters.status || undefined,
    startTime: filters.dateRange?.[0] || undefined,
    endTime: filters.dateRange?.[1] || undefined,
    page: filters.page,
    size: filters.size
  }
}

async function loadOrders() {
  loading.value = true
  try {
    const data = await listAdminOrders(buildParams())
    orders.value = data?.records || []
    pageData.total = Number(data?.total || 0)
    pageData.pages = Number(data?.pages || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() { filters.page = 1; loadOrders() }
function handleSizeChange(size) { filters.size = size; filters.page = 1; loadOrders() }
function resetFilters() {
  Object.assign(filters, { orderNo: '', username: '', status: '', dateRange: [], page: 1 })
  loadOrders()
}

onMounted(loadOrders)
</script>
