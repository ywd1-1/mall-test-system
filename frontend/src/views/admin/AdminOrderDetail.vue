<template>
  <section class="page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Admin Order Detail</div>
        <h1 class="page-title">订单详情</h1>
        <p class="page-desc">核对地址快照、物流、商品与状态记录；已支付订单可发货</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Back" @click="$router.back()">返回</el-button>
        <el-button v-if="order?.status === 'PAID'" type="primary" :icon="Van" @click="openShipDialog">订单发货</el-button>
      </div>
    </div>

    <div class="detail-panel" v-loading="loading">
      <template v-if="order">
        <div class="order-facts">
          <div class="order-fact"><span>订单编号</span><strong>{{ order.orderNo }}</strong></div>
          <div class="order-fact"><span>下单用户</span><strong>{{ order.username }}</strong></div>
          <div class="order-fact"><span>订单状态</span><strong><el-tag :type="orderStatusTagType(order.status)" effect="light">{{ orderStatusText(order.status) }}</el-tag></strong></div>
          <div class="order-fact"><span>订单金额</span><strong class="price">¥{{ order.totalAmount }}</strong></div>
          <div class="order-fact"><span>创建时间</span><strong>{{ order.createdAt }}</strong></div>
        </div>

        <div class="detail-sections">
          <section class="detail-section">
            <div class="toolbar-title"><strong>收货地址快照</strong><span>与用户当前地址数据相互独立</span></div>
            <div class="address-snapshot"><strong>{{ order.recipientName }} · {{ order.recipientPhone }}</strong><span>{{ fullAddress }}</span></div>
          </section>
          <section class="detail-section">
            <div class="toolbar-title"><strong>物流信息</strong><span>发货后不可重复修改</span></div>
            <div v-if="order.shippingCompany" class="address-snapshot"><strong>{{ order.shippingCompany }}</strong><span>{{ order.trackingNumber }}</span></div>
            <div v-else class="muted">暂未发货</div>
          </section>
        </div>

        <div class="detail-section">
          <div class="toolbar-title"><strong>商品明细</strong><span>后台核对商品、数量和金额</span></div>
          <el-table :data="order.items" row-key="id">
            <el-table-column label="商品" min-width="260">
              <template #default="{ row }"><div class="line-item"><img class="product-image" :src="row.productImageUrl" :alt="row.productName" loading="lazy" /><div><strong>{{ row.productName }}</strong><span>数量 {{ row.quantity }}</span></div></div></template>
            </el-table-column>
            <el-table-column label="单价" width="120"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
            <el-table-column prop="quantity" label="数量" width="90" />
            <el-table-column label="小计" width="120"><template #default="{ row }"><span class="price">¥{{ row.subtotal }}</span></template></el-table-column>
          </el-table>
        </div>

        <OrderTimeline :logs="order.statusLogs" />
      </template>
      <div v-else-if="!loading" class="empty-state"><div class="empty-state__body"><el-icon><Tickets /></el-icon><strong>未找到订单</strong><p>返回订单管理列表后重新选择订单。</p></div></div>
    </div>

    <el-dialog v-model="shipDialogVisible" title="订单发货" width="520px">
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-position="top">
        <el-form-item label="物流公司" prop="shippingCompany"><el-input v-model="shipForm.shippingCompany" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item label="物流单号" prop="trackingNumber"><el-input v-model="shipForm.trackingNumber" maxlength="100" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button @click="shipDialogVisible = false">取消</el-button><el-button type="primary" :loading="shipping" @click="submitShip">确认发货</el-button></div></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Tickets, Van } from '@element-plus/icons-vue'
import OrderTimeline from '../../components/OrderTimeline.vue'
import { getAdminOrder, shipOrder } from '../../api/orders'
import { orderStatusTagType, orderStatusText } from '../../utils/order'

const route = useRoute()
const loading = ref(false)
const shipping = ref(false)
const shipDialogVisible = ref(false)
const shipFormRef = ref()
const order = ref(null)
const shipForm = reactive({ shippingCompany: '', trackingNumber: '' })
const shipRules = {
  shippingCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  trackingNumber: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}
const fullAddress = computed(() => order.value ? `${order.value.province}${order.value.city}${order.value.district}${order.value.detailAddress}` : '')

async function loadOrder() {
  loading.value = true
  try { order.value = await getAdminOrder(route.params.id) } finally { loading.value = false }
}

function openShipDialog() {
  Object.assign(shipForm, { shippingCompany: '', trackingNumber: '' })
  shipDialogVisible.value = true
}

async function submitShip() {
  await shipFormRef.value.validate()
  await ElMessageBox.confirm(`确认使用 ${shipForm.shippingCompany} 发货？`, '确认发货', { type: 'info' })
  shipping.value = true
  try {
    order.value = await shipOrder(order.value.id, shipForm)
    shipDialogVisible.value = false
    ElMessage.success('订单已发货')
  } finally {
    shipping.value = false
  }
}

onMounted(loadOrder)
</script>
