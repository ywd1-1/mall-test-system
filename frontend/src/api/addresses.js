import request from './request'

export function listAddresses() {
  return request.get('/addresses')
}

export function createAddress(data) {
  return request.post('/addresses', data)
}

export function updateAddress(id, data) {
  return request.put(`/addresses/${id}`, data)
}

export function deleteAddress(id) {
  return request.delete(`/addresses/${id}`)
}

export function setDefaultAddress(id) {
  return request.put(`/addresses/${id}/default`)
}
