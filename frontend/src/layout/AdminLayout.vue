<template>
  <el-container class="app-shell">
    <el-aside class="sidebar sidebar--desktop" width="260px">
      <div class="brand">
        <div class="brand-mark">
          <el-icon><Monitor /></el-icon>
        </div>
        <div class="brand-copy">
          <strong>后台管理</strong>
          <span>mall admin workspace</span>
        </div>
      </div>

      <div class="nav-section">管理菜单</div>
      <el-menu :default-active="activePath" router class="side-menu">
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Box /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="workspace">
      <el-header class="topbar">
        <button class="admin-menu-trigger" type="button" aria-label="打开管理菜单" @click="mobileNavOpen = true">
          <el-icon><MenuIcon /></el-icon>
        </button>
        <div class="topbar-heading">
          <div class="eyebrow">Admin Console</div>
          <div class="topbar-title">{{ pageTitle }}</div>
        </div>
        <div class="user-area">
          <el-tag type="warning" effect="light">管理员</el-tag>
          <div class="avatar">{{ usernameInitial }}</div>
          <span class="username">{{ authState.user?.username }}</span>
          <el-button :icon="SwitchButton" plain aria-label="退出登录" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>

    <el-drawer
      v-model="mobileNavOpen"
      direction="ltr"
      size="280px"
      :with-header="false"
      class="admin-nav-drawer"
    >
      <div class="drawer-sidebar">
        <div class="brand">
          <div class="brand-mark">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="brand-copy">
            <strong>后台管理</strong>
            <span>mall admin workspace</span>
          </div>
        </div>
        <div class="nav-section">管理菜单</div>
        <el-menu :default-active="activePath" router class="side-menu" @select="mobileNavOpen = false">
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Box /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Box, DataBoard, List, Menu as MenuIcon, Monitor, SwitchButton, User } from '@element-plus/icons-vue'
import { authState, clearAuth } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const mobileNavOpen = ref(false)

const activePath = computed(() => {
  if (route.path.startsWith('/admin/orders/')) return '/admin/orders'
  return route.path
})

const usernameInitial = computed(() => (authState.user?.username || 'A').slice(0, 1).toUpperCase())
const pageTitle = computed(() => {
  if (route.path.startsWith('/admin/orders/')) return '订单详情'
  const map = {
    '/admin/dashboard': '工作台',
    '/admin/products': '商品管理',
    '/admin/users': '用户管理',
    '/admin/orders': '订单管理'
  }
  return map[route.path] || '后台管理'
})

function logout() {
  clearAuth()
  router.push('/login')
}
</script>
