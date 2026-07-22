<template>
  <div class="login-page">
    <main class="login-panel" aria-labelledby="login-title">
      <div class="login-heading">
        <div class="login-mark" aria-hidden="true">MT</div>
        <h1 id="login-title">电商订单管理系统</h1>
        <p>软件测试与接口测试练习系统</p>
      </div>

      <section class="login-form" aria-label="登录表单">
        <div class="login-form__head">
          <strong>账号登录</strong>
          <span>默认测试账号</span>
        </div>
        <div class="login-accounts" aria-label="默认测试账号">
          <span>user / 123456</span>
          <span>admin / 123456</span>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" autocomplete="username" maxlength="50" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" autocomplete="current-password" maxlength="100" show-password size="large" />
          </el-form-item>
          <div class="auth-actions">
            <el-button type="primary" size="large" :loading="loading" @click="submit">登录</el-button>
            <el-button size="large" @click="$router.push('/register')">注册账号</el-button>
          </div>
        </el-form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { setAuth } from '../stores/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: 'user',
  password: '123456'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为 3-50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度为 6-100 个字符', trigger: 'blur' }
  ]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await login(form)
    setAuth(data.token, data.user)
    ElMessage.success('登录成功')
    router.push(data.user?.role === 'ADMIN' ? '/admin/dashboard' : '/products')
  } finally {
    loading.value = false
  }
}
</script>
