<template>
  <section class="page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Cart Checkout</div>
        <h1 class="page-title">购物车</h1>
        <p class="page-desc">修改数量后可按选中商品创建订单</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadCart">刷新</el-button>
    </div>

    <div class="metric-grid">
      <div class="metric-card">
        <div class="metric-card__icon"><el-icon><ShoppingCartFull /></el-icon></div>
        <span>购物车商品</span>
        <strong>{{ cartItems.length }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon accent"><el-icon><Check /></el-icon></div>
        <span>当前结算项</span>
        <strong>{{ selected.length }}</strong>
      </div>
      <div class="metric-card">
        <div class="metric-card__icon danger"><el-icon><Money /></el-icon></div>
        <span>结算金额</span>
        <strong>¥{{ selectedAmount }}</strong>
      </div>
    </div>

    <div class="summary-strip">
      <div class="summary-strip__copy">
        <strong>{{ selected.length ? '将按选中商品结算' : '请先勾选要结算的商品' }}</strong>
        <div class="checkout-address">
          <el-select v-model="addressId" placeholder="请选择收货地址" class="checkout-address__select">
            <el-option
              v-for="address in addresses"
              :key="address.id"
              :label="addressLabel(address)"
              :value="address.id"
            />
          </el-select>
          <el-button :icon="Location" @click="$router.push('/addresses')">管理地址</el-button>
        </div>
      </div>
      <div class="checkout-actions">
        <span class="summary-strip__amount">¥{{ selectedAmount }}</span>
        <el-button type="primary" :icon="Check" :disabled="cartItems.length === 0 || !addressId" @click="confirmCreateOrder">
          结算
        </el-button>
      </div>
    </div>

    <div class="table-panel">
      <div class="toolbar">
        <div class="toolbar-title">
          <strong>购物车明细</strong>
          <span>可直接在表格中调整数量</span>
        </div>
        <span class="muted">已选 {{ selected.length }} 项</span>
      </div>

      <el-table
        v-loading="loading"
        :data="cartItems"
        row-key="id"
        empty-text="购物车为空"
        @selection-change="selected = $event"
      >
        <template #empty>
          <div class="empty-state">
            <div class="empty-state__body">
              <el-icon><ShoppingCartFull /></el-icon>
              <strong>购物车为空</strong>
              <p>先去商品列表添加商品，再验证下单流程。</p>
              <el-button type="primary" @click="$router.push('/products')">去选商品</el-button>
            </div>
          </div>
        </template>
        <el-table-column type="selection" width="48" />
        <el-table-column label="商品" min-width="260">
          <template #default="{ row }">
            <div class="line-item">
              <img class="product-image" :src="row.imageUrl" :alt="row.productName" loading="lazy" />
              <div>
                <strong>{{ row.productName }}</strong>
                <span>库存 {{ row.stock }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="160">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="row.stock"
              size="small"
              @change="changeQuantity(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.subtotal }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" :icon="Delete" @click="confirmDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Location, Money, Refresh, ShoppingCartFull } from '@element-plus/icons-vue'
import { deleteCart, listCart, updateCart } from '../api/cart'
import { createOrder } from '../api/orders'
import { listAddresses } from '../api/addresses'

const router = useRouter()
const loading = ref(false)
const cartItems = ref([])
const selected = ref([])
const addresses = ref([])
const addressId = ref(null)

const selectedAmount = computed(() => {
  return selected.value.reduce((sum, item) => sum + Number(item.price) * Number(item.quantity), 0).toFixed(2)
})

async function loadCart() {
  loading.value = true
  try {
    const [cartData, addressData] = await Promise.all([listCart(), listAddresses()])
    cartItems.value = cartData
    selected.value = []
    addresses.value = addressData
    const selectedAddressExists = addressData.some((item) => item.id === addressId.value)
    if (!selectedAddressExists) {
      addressId.value = addressData.find((item) => item.isDefault)?.id || addressData[0]?.id || null
    }
  } finally {
    loading.value = false
  }
}

async function changeQuantity(row) {
  const updated = await updateCart(row.id, { quantity: row.quantity })
  Object.assign(row, updated)
  ElMessage.success('数量已更新')
}

async function confirmDelete(row) {
  await ElMessageBox.confirm(`确认删除「${row.productName}」？`, '删除购物车商品', { type: 'warning' })
  await deleteCart(row.id)
  ElMessage.success('删除成功')
  await loadCart()
}

async function confirmCreateOrder() {
  if (selected.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  if (!addressId.value) {
    ElMessage.warning('请先新增并选择收货地址')
    return
  }
  const rows = selected.value
  await ElMessageBox.confirm(`确认结算 ${rows.length} 件商品并创建订单？`, '结算', { type: 'info' })
  const order = await createOrder({ cartIds: rows.map((item) => item.id), addressId: addressId.value })
  ElMessage.success('订单创建成功')
  router.push(`/orders/${order.id}`)
}

function addressLabel(address) {
  return `${address.isDefault ? '默认 · ' : ''}${address.recipientName} ${address.phone} ${address.province}${address.city}${address.district}${address.detailAddress}`
}

onMounted(loadCart)
</script>
