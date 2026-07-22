import request from './request'

export function login(data) {
  return request.post('/login', data)
}

export function register(data) {
  return request.post('/register', data)
}

export function getCurrentUser() {
  return request.get('/user/current')
}
