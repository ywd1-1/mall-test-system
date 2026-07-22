<template>
  <section class="page">
    <div class="page-head">
      <div>
        <div class="eyebrow">Admin Users</div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-desc">查询普通用户并启用或禁用账号；管理员账号不在此列表</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadUsers">刷新</el-button>
    </div>

    <div class="metric-grid">
      <div class="metric-card"><div class="metric-card__icon"><el-icon><User /></el-icon></div><span>筛选结果</span><strong>{{ pageData.total }}</strong></div>
      <div class="metric-card"><div class="metric-card__icon success"><el-icon><CircleCheck /></el-icon></div><span>本页启用</span><strong>{{ activeCount }}</strong></div>
      <div class="metric-card"><div class="metric-card__icon danger"><el-icon><CircleClose /></el-icon></div><span>本页禁用</span><strong>{{ disabledCount }}</strong></div>
    </div>

    <div class="table-panel">
      <div class="toolbar filter-toolbar">
        <div class="toolbar-title"><strong>普通用户列表</strong><span>禁用后现有 token 同步失效</span></div>
        <div class="filter-group">
          <el-input v-model="filters.username" clearable class="filter-input" placeholder="搜索用户名" :prefix-icon="Search" @keyup.enter="handleSearch" />
          <el-select v-model="filters.status" clearable class="filter-select" placeholder="全部状态">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="users" row-key="id" empty-text="暂无用户">
        <el-table-column prop="id" label="用户 ID" width="100" />
        <el-table-column prop="username" label="用户名" min-width="180" />
        <el-table-column label="角色" width="120"><template #default><el-tag effect="plain">普通用户</el-tag></template></el-table-column>
        <el-table-column label="状态" width="120"><template #default="{ row }"><el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" effect="light">{{ row.status === 'ACTIVE' ? '启用' : '禁用' }}</el-tag></template></el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="180" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'ACTIVE'" type="danger" plain :icon="Lock" @click="changeStatus(row, 'DISABLED')">禁用</el-button>
            <el-button v-else type="success" plain :icon="Unlock" @click="changeStatus(row, 'ACTIVE')">启用</el-button>
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
          @current-change="loadUsers"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, CircleClose, Lock, Refresh, Search, Unlock, User } from '@element-plus/icons-vue'
import { listAdminUsers, updateUserStatus } from '../../api/users'

const loading = ref(false)
const users = ref([])
const filters = reactive({ username: '', status: '', page: 1, size: 10 })
const pageData = reactive({ total: 0, pages: 0 })
const activeCount = computed(() => users.value.filter((item) => item.status === 'ACTIVE').length)
const disabledCount = computed(() => users.value.filter((item) => item.status === 'DISABLED').length)

async function loadUsers() {
  loading.value = true
  try {
    const data = await listAdminUsers({ username: filters.username || undefined, status: filters.status || undefined, page: filters.page, size: filters.size })
    users.value = data?.records || []
    pageData.total = Number(data?.total || 0)
    pageData.pages = Number(data?.pages || 0)
  } finally { loading.value = false }
}

function handleSearch() { filters.page = 1; loadUsers() }
function handleSizeChange(size) { filters.size = size; filters.page = 1; loadUsers() }
function resetFilters() { Object.assign(filters, { username: '', status: '', page: 1 }); loadUsers() }

async function changeStatus(row, status) {
  const action = status === 'ACTIVE' ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${action}用户 ${row.username}？`, `${action}用户`, { type: status === 'ACTIVE' ? 'success' : 'warning' })
  const updated = await updateUserStatus(row.id, status)
  Object.assign(row, updated)
  ElMessage.success(`用户已${action}`)
}

onMounted(loadUsers)
</script>
