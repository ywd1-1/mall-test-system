import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authState, isAdmin } from '../stores/auth'
import UserLayout from '../layout/UserLayout.vue'
import AdminLayout from '../layout/AdminLayout.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Products from '../views/Products.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '../views/Cart.vue'
import Orders from '../views/Orders.vue'
import OrderDetail from '../views/OrderDetail.vue'
import Addresses from '../views/Addresses.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import AdminProducts from '../views/admin/AdminProducts.vue'
import AdminOrders from '../views/admin/AdminOrders.vue'
import AdminOrderDetail from '../views/admin/AdminOrderDetail.vue'
import AdminUsers from '../views/admin/AdminUsers.vue'

function homePath() {
  return isAdmin() ? '/admin/dashboard' : '/products'
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: () => homePath() },
    { path: '/login', component: Login, meta: { public: true } },
    { path: '/register', component: Register, meta: { public: true } },
    {
      path: '/',
      component: UserLayout,
      children: [
        { path: 'user/home', redirect: '/products' },
        { path: 'products', component: Products },
        { path: 'products/:id', component: ProductDetail },
        { path: 'cart', component: Cart },
        { path: 'addresses', component: Addresses },
        { path: 'orders', component: Orders },
        { path: 'orders/:id', component: OrderDetail }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { admin: true },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', component: AdminDashboard },
        { path: 'products', component: AdminProducts },
        { path: 'users', component: AdminUsers },
        { path: 'orders', component: AdminOrders },
        { path: 'orders/:id', component: AdminOrderDetail }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.meta.public) {
    return authState.token ? homePath() : true
  }
  if (!authState.token) {
    return '/login'
  }
  if (to.matched.some((record) => record.meta.admin) && !isAdmin()) {
    ElMessage.warning('无权限访问管理员页面')
    return '/products'
  }
  return true
})

export default router
