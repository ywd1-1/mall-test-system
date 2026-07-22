<template>
  <section class="page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Order Flow</div>
        <h1 class="page-title">我的订单</h1>
        <p class="page-desc">按状态分页查询订单，并完成支付、取消和确认收货</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadOrders">刷新</el-button>
    </div>

    <div class="metric-grid metric-grid--four">
      <div class="metric-card">
        <div class="metric-card__icon"><el-icon><Tickets /></el-icon></div>
        <span>筛选结果</span>
        <strong>{{ pageData.total }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon warning"><el-icon><Clock /></el-icon></div>
        <span>本页待支付</span>
        <strong>{{ countByStatus('CREATED') }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon success"><el-icon><Van /></el-icon></div>
        <span>本页已发货</span>
        <strong>{{ countByStatus('SHIPPED') }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon danger"><el-icon><Money /></el-icon></div>
        <span>本页金额</span>
        <strong>¥{{ pageAmount }}</strong>
      </div>
    </div>

    <div class="table-panel">
      <div class="toolbar filter-toolbar">
        <div class="toolbar-title">
          <strong>订单记录</strong>
          <span>五类状态均可筛选，详情页展示完整状态时间线</span>
        </div>
        <div class="filter-group">
          <el-select v-model="filters.status" clearable placeholder="全部状态" class="filter-select">
            <el-option v-for="item in ORDER_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="orders" row-key="id" empty-text="暂无订单">
        <template #empty>
          <div class="empty-state">
            <div class="empty-state__body">
              <el-icon><Tickets /></el-icon>
              <strong>暂无订单</strong>
              <p>创建订单后可在这里验证完整状态流转。</p>
              <el-button type="primary" @click="$router.push('/products')">去创建订单</el-button>
            </div>
          </div>
        </template>
        <el-table-column prop="orderNo" label="订单编号" min-width="210" />
        <el-table-column label="收货人" min-width="150">
          <template #default="{ row }">
            <strong>{{ row.recipientName }}</strong>
            <div class="muted">{{ row.recipientPhone }}</div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }"><span class="price">¥{{ row.totalAmount }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="orderStatusTagType(row.status)" effect="light">{{ orderStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button :icon="View" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
            <el-button v-if="row.status === 'CREATED'" type="primary" :icon="CircleCheck" @click="confirmPay(row)">支付</el-button>
            <el-button v-if="row.status === 'CREATED'" type="danger" :icon="Close" @click="confirmCancel(row)">取消</el-button>
            <el-button v-if="row.status === 'SHIPPED'" type="success" :icon="CircleCheck" @click="confirmReceive(row)">确认收货</el-button>
          </template>
        </el-table-column>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, Clock, Close, Money, Refresh, Search, Tickets, Van, View } from '@element-plus/icons-vue'
import { cancelOrder, confirmReceipt, listOrders, payOrder } from '../api/orders'
import { ORDER_STATUS_OPTIONS, orderStatusTagType, orderStatusText } from '../utils/order'

const loading = ref(false)
const orders = ref([])
const filters = reactive({ status: '', page: 1, size: 10 })
const pageData = reactive({ total: 0, pages: 0 })

const pageAmount = computed(() => orders.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0).toFixed(2))
const countByStatus = (status) => orders.value.filter((item) => item.status === status).length

async function loadOrders() {
  loading.value = true
  try {
    const data = await listOrders({
      status: filters.status || undefined,
      page: filters.page,
      size: filters.size
    })
    orders.value = data?.records || []
    pageData.total = Number(data?.total || 0)
    pageData.pages = Number(data?.pages || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  filters.page = 1
  loadOrders()
}

function resetFilters() {
  filters.status = ''
  filters.page = 1
  loadOrders()
}

function handleSizeChange(size) {
  filters.size = size
  filters.page = 1
  loadOrders()
}

async function confirmCancel(row) {
  await ElMessageBox.confirm(`确认取消订单 ${row.orderNo}？取消后库存只恢复一次。`, '取消订单', { type: 'warning' })
  await cancelOrder(row.id)
  ElMessage.success('订单已取消')
  await loadOrders()
}

async function confirmPay(row) {
  await ElMessageBox.confirm(`确认模拟支付订单 ${row.orderNo}？`, '模拟支付', { type: 'info' })
  await payOrder(row.id)
  ElMessage.success('支付成功')
  await loadOrders()
}

async function confirmReceive(row) {
  await ElMessageBox.confirm(`确认已收到订单 ${row.orderNo}？`, '确认收货', { type: 'success' })
  await confirmReceipt(row.id)
  ElMessage.success('订单已完成')
  await loadOrders()
}

onMounted(loadOrders)
</script>
