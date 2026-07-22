<template>
  <el-container class="user-shell">
    <el-header class="user-topbar">
      <router-link class="user-brand" to="/products" aria-label="电商订单系统首页">
        <el-icon><ShoppingBag /></el-icon>
        <span>电商订单系统</span>
      </router-link>

      <el-menu :default-active="activePath" mode="horizontal" router :ellipsis="false" class="user-nav">
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>商品</span>
        </el-menu-item>
        <el-menu-item index="/cart">
          <el-icon><ShoppingCart /></el-icon>
          <span>购物车</span>
        </el-menu-item>
        <el-menu-item index="/addresses">
          <el-icon><Location /></el-icon>
          <span>收货地址</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Tickets /></el-icon>
          <span>我的订单</span>
        </el-menu-item>
      </el-menu>

      <div class="user-account">
        <div class="avatar">{{ usernameInitial }}</div>
        <span class="username">{{ authState.user?.username }}</span>
        <el-button class="user-logout" :icon="SwitchButton" plain aria-label="退出登录" @click="logout">
          <span class="user-logout__text">退出登录</span>
        </el-button>
      </div>
    </el-header>

    <el-main class="user-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Goods, Location, ShoppingBag, ShoppingCart, SwitchButton, Tickets } from '@element-plus/icons-vue'
import { authState, clearAuth } from '../stores/auth'

const route = useRoute()
const router = useRouter()

const activePath = computed(() => {
  if (route.path.startsWith('/products/')) return '/products'
  if (route.path.startsWith('/orders/')) return '/orders'
  return route.path === '/user/home' ? '/products' : route.path
})

const usernameInitial = computed(() => (authState.user?.username || 'U').slice(0, 1).toUpperCase())

function logout() {
  clearAuth()
  router.push('/login')
}
</script>
