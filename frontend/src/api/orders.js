import request from './request'

export function createOrder(data) {
  return request.post('/orders', data)
}

export function listOrders(params) {
  return request.get('/orders', { params })
}

export function getOrder(id) {
  return request.get(`/orders/${id}`)
}

export function cancelOrder(id) {
  return request.delete(`/orders/${id}`)
}

export function payOrder(id) {
  return request.post(`/orders/${id}/pay`)
}

export function confirmReceipt(id) {
  return request.post(`/orders/${id}/confirm-receipt`)
}

export function listAdminOrders(params) {
  return request.get('/admin/orders', { params })
}

export function getAdminOrder(id) {
  return request.get(`/admin/orders/${id}`)
}

export function shipOrder(id, data) {
  return request.post(`/admin/orders/${id}/ship`, data)
}
