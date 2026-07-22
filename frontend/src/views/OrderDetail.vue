<template>
  <section class="page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Order Detail</div>
        <h1 class="page-title">订单详情</h1>
        <p class="page-desc">查看收货快照、物流信息、商品明细和完整状态时间线</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Back" @click="$router.back()">返回</el-button>
        <el-button v-if="canPay" type="primary" :icon="CircleCheck" :loading="paying" @click="confirmPay">模拟支付</el-button>
        <el-button v-if="canCancel" type="danger" :icon="Close" :loading="cancelling" @click="confirmCancel">取消订单</el-button>
        <el-button v-if="canConfirm" type="success" :icon="CircleCheck" :loading="confirming" @click="confirmReceive">确认收货</el-button>
      </div>
    </div>

    <div class="detail-panel" v-loading="loading">
      <template v-if="order">
        <div class="order-facts">
          <div class="order-fact"><span>订单编号</span><strong>{{ order.orderNo }}</strong></div>
          <div class="order-fact">
            <span>订单状态</span>
            <strong><el-tag :type="orderStatusTagType(order.status)" effect="light">{{ orderStatusText(order.status) }}</el-tag></strong>
          </div>
          <div class="order-fact"><span>订单金额</span><strong class="price">¥{{ order.totalAmount }}</strong></div>
          <div class="order-fact"><span>创建时间</span><strong>{{ order.createdAt }}</strong></div>
        </div>

        <div class="detail-sections">
          <section class="detail-section">
            <div class="toolbar-title"><strong>收货地址快照</strong><span>地址修改或删除不会改变此处内容</span></div>
            <div class="address-snapshot">
              <strong>{{ order.recipientName }} · {{ order.recipientPhone }}</strong>
              <span>{{ fullAddress }}</span>
            </div>
          </section>
          <section class="detail-section">
            <div class="toolbar-title"><strong>物流信息</strong><span>管理员发货后生成</span></div>
            <div v-if="order.shippingCompany" class="address-snapshot">
              <strong>{{ order.shippingCompany }}</strong>
              <span>{{ order.trackingNumber }}</span>
            </div>
            <div v-else class="muted">暂未发货</div>
          </section>
        </div>

        <div class="detail-section">
          <div class="toolbar-title"><strong>商品明细</strong><span>核对订单商品、数量和小计</span></div>
          <el-table :data="order.items" row-key="id">
            <el-table-column label="商品" min-width="260">
              <template #default="{ row }">
                <div class="line-item">
                  <img class="product-image" :src="row.productImageUrl" :alt="row.productName" loading="lazy" />
                  <div><strong>{{ row.productName }}</strong><span>数量 {{ row.quantity }}</span></div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
            <el-table-column prop="quantity" label="数量" width="90" />
            <el-table-column label="小计" width="120"><template #default="{ row }"><span class="price">¥{{ row.subtotal }}</span></template></el-table-column>
          </el-table>
        </div>

        <OrderTimeline :logs="order.statusLogs" />
      </template>
      <div v-else-if="!loading" class="empty-state">
        <div class="empty-state__body"><el-icon><Tickets /></el-icon><strong>未找到订单</strong><p>返回订单列表后重新选择订单。</p></div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, CircleCheck, Close, Tickets } from '@element-plus/icons-vue'
import OrderTimeline from '../components/OrderTimeline.vue'
import { cancelOrder, confirmReceipt, getOrder, payOrder } from '../api/orders'
import { orderStatusTagType, orderStatusText } from '../utils/order'

const route = useRoute()
const loading = ref(false)
const paying = ref(false)
const cancelling = ref(false)
const confirming = ref(false)
const order = ref(null)

const canPay = computed(() => order.value?.status === 'CREATED')
const canCancel = computed(() => order.value?.status === 'CREATED')
const canConfirm = computed(() => order.value?.status === 'SHIPPED')
const fullAddress = computed(() => {
  if (!order.value) return ''
  return `${order.value.province}${order.value.city}${order.value.district}${order.value.detailAddress}`
})

async function loadOrder() {
  loading.value = true
  try {
    order.value = await getOrder(route.params.id)
  } finally {
    loading.value = false
  }
}

async function confirmPay() {
  await ElMessageBox.confirm(`确认模拟支付订单 ${order.value.orderNo}？`, '模拟支付', { type: 'info' })
  paying.value = true
  try {
    order.value = await payOrder(order.value.id)
    ElMessage.success('支付成功')
  } finally {
    paying.value = false
  }
}

async function confirmCancel() {
  await ElMessageBox.confirm(`确认取消订单 ${order.value.orderNo}？`, '取消订单', { type: 'warning' })
  cancelling.value = true
  try {
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    await loadOrder()
  } finally {
    cancelling.value = false
  }
}

async function confirmReceive() {
  await ElMessageBox.confirm(`确认已收到订单 ${order.value.orderNo}？`, '确认收货', { type: 'success' })
  confirming.value = true
  try {
    order.value = await confirmReceipt(order.value.id)
    ElMessage.success('订单已完成')
  } finally {
    confirming.value = false
  }
}

onMounted(loadOrder)
</script>
