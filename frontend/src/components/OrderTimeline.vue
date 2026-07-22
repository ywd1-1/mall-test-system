<template>
  <div class="order-timeline">
    <div class="toolbar-title timeline-heading">
      <strong>状态时间线</strong>
      <span>每次状态变更均由后端事务写入</span>
    </div>
    <el-timeline v-if="logs.length">
      <el-timeline-item
        v-for="log in logs"
        :key="log.id"
        :timestamp="log.operatedAt"
        placement="top"
        :type="timelineType(log.newStatus)"
      >
        <div class="timeline-entry">
          <div class="timeline-entry__title">
            <strong>{{ transitionText(log) }}</strong>
            <el-tag size="small" effect="plain">{{ log.operatorName }} · {{ roleText(log.operatorRole) }}</el-tag>
          </div>
          <p>{{ log.remark || '状态已更新' }}</p>
        </div>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="暂无状态记录" :image-size="72" />
  </div>
</template>

<script setup>
import { orderStatusText } from '../utils/order'

defineProps({
  logs: {
    type: Array,
    default: () => []
  }
})

function transitionText(log) {
  if (!log.oldStatus) return orderStatusText(log.newStatus)
  return `${orderStatusText(log.oldStatus)} → ${orderStatusText(log.newStatus)}`
}

function timelineType(status) {
  if (status === 'CANCELLED') return 'info'
  if (status === 'CREATED') return 'primary'
  return 'success'
}

function roleText(role) {
  return role === 'ADMIN' ? '管理员' : '用户'
}
</script>
