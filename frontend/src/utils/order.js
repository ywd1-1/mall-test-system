export const ORDER_STATUS_OPTIONS = [
  { value: 'CREATED', label: '待支付' },
  { value: 'PAID', label: '已支付' },
  { value: 'SHIPPED', label: '已发货' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
]

export function orderStatusText(status) {
  return ORDER_STATUS_OPTIONS.find((item) => item.value === status)?.label || status
}

export function orderStatusTagType(status) {
  const map = {
    CREATED: 'warning',
    PAID: 'primary',
    SHIPPED: 'success',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return map[status] || ''
}
