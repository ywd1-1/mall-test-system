import { reactive } from 'vue'

const rawUser = localStorage.getItem('mall_user')

export const authState = reactive({
  token: localStorage.getItem('mall_token') || '',
  user: rawUser ? JSON.parse(rawUser) : null
})

export function setAuth(token, user) {
  authState.token = token
  authState.user = user
  localStorage.setItem('mall_token', token)
  localStorage.setItem('mall_user', JSON.stringify(user))
}

export function clearAuth() {
  authState.token = ''
  authState.user = null
  localStorage.removeItem('mall_token')
  localStorage.removeItem('mall_user')
}

export function isAdmin() {
  return authState.user && authState.user.role === 'ADMIN'
}
