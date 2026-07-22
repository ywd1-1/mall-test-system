import request from './request'

export function addCart(data) {
  return request.post('/cart', data)
}

export function listCart() {
  return request.get('/cart')
}

export function updateCart(id, data) {
  return request.put(`/cart/${id}`, data)
}

export function deleteCart(id) {
  return request.delete(`/cart/${id}`)
}
