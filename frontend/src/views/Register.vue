<template>
  <div class="auth-page">
    <section class="auth-shell">
      <div class="auth-intro">
        <div class="auth-logo">MT</div>
        <div>
          <div class="auth-kicker">User Flow</div>
          <h1>创建测试账号</h1>
          <p>新注册账号默认为普通用户，可用于购物车、下单、查单和取消订单流程验证。</p>
          <div class="auth-system-list">
            <div>
              <strong>3-50</strong>
              <span>用户名字符</span>
            </div>
            <div>
              <strong>6+</strong>
              <span>密码位数</span>
            </div>
            <div>
              <strong>USER</strong>
              <span>默认角色</span>
            </div>
          </div>
        </div>
        <div class="auth-badges">
          <span>普通用户</span>
          <span>接口测试</span>
          <span>库存校验</span>
        </div>
      </div>

      <el-card class="auth-card" shadow="never">
        <div class="form-heading">
          <h2>注册账号</h2>
          <span>用户名 3-50 个字符，密码 6-100 位。</span>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" autocomplete="username" maxlength="50" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" autocomplete="new-password" maxlength="100" show-password size="large" />
          </el-form-item>
          <div class="auth-actions">
            <el-button type="primary" size="large" :loading="loading" @click="submit">注册并登录</el-button>
            <el-button size="large" @click="$router.push('/login')">返回登录</el-button>
          </div>
        </el-form>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'
import { setAuth } from '../stores/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
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
    const data = await register(form)
    setAuth(data.token, data.user)
    ElMessage.success('注册成功')
    router.push('/products')
  } finally {
    loading.value = false
  }
}
</script>
