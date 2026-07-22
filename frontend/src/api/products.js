import request from './request'

export function listProducts(params) {
  return request.get('/products', { params })
}

export function getProduct(id) {
  return request.get(`/products/${id}`)
}

export function listAdminProducts(params) {
  return request.get('/admin/products', { params })
}

export function createProduct(data) {
  return request.post('/admin/products', data)
}

export function updateProduct(id, data) {
  return request.put(`/admin/products/${id}`, data)
}

export function deleteProduct(id) {
  return request.delete(`/admin/products/${id}`)
}

export function updateProductStatus(id, status) {
  return request.put(`/admin/products/${id}/status`, { status })
}

export function updateProductStock(id, stock) {
  return request.put(`/admin/products/${id}/stock`, { stock })
}
