<script setup>
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from "element-plus";
import { get, post } from "@/net/index.js";
import { reactive, onMounted, onUnmounted } from "vue";
import router from "@/router/index.js";
import { useStore } from "@/stores/index.js";

const store = useStore()

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const onPasswordInput = (e) => {
  store.loginPassword.isTyping = true
  store.loginPassword.passwordLength = e.target.value.length
}

const onPasswordBlur = () => {
  store.loginPassword.isTyping = false
}

const loading = reactive({
  login: false
})

onMounted(() => {
  const savedLoginInfo = localStorage.getItem('saved_login_info')
  if (savedLoginInfo) {
    try {
      const loginInfo = JSON.parse(savedLoginInfo)
      form.username = loginInfo.username || ''
      form.password = loginInfo.password || ''
      form.remember = true
    } catch (e) {
      localStorage.removeItem('saved_login_info')
    }
  }
})

const login = () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }

  loading.login = true

  post('api/auth/login', {
    username: form.username,
    password: form.password,
    remember: form.remember
  }, (message) => {
    ElMessage.success(message)

    if (form.remember) {
      const loginInfo = {
        username: form.username,
        password: form.password,
        timestamp: new Date().getTime()
      }
      localStorage.setItem('saved_login_info', JSON.stringify(loginInfo))
    } else {
      localStorage.removeItem('saved_login_info')
    }

    get('/api/user/me', (message) => {
      store.auth.user = message
      store.resetLoginPassword()
      loading.login = false
      router.push('/index')
    }, (errorMsg) => {
      loading.login = false
      store.auth.user = null
      ElMessage.error(errorMsg || '获取用户信息失败')
    })
  }, (error) => {
    loading.login = false
    store.resetLoginPassword()
    console.error('登录失败:', error)
    ElMessage.error(error || '登录失败，请检查用户名和密码')
  })
}

onUnmounted(() => {
  store.resetLoginPassword()
})
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <!-- Logo / Brand -->
      <div class="brand">
        <div class="brand-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <rect width="40" height="40" rx="10" fill="#1d1d1f"/>
            <path d="M20 8C13.5 8 8 13.5 8 20C8 26.5 13.5 32 20 32C26.5 32 32 26.5 32 20C32 13.5 26.5 8 20 8ZM20 28C15.6 28 12 24.4 12 20C12 15.6 15.6 12 20 12C24.4 12 28 15.6 28 20C28 24.4 24.4 28 20 28Z" fill="#fff"/>
            <circle cx="20" cy="20" r="4" fill="#fff"/>
          </svg>
        </div>
        <h1 class="brand-title">Study</h1>
        <p class="brand-subtitle">登录到您的账户</p>
      </div>

      <!-- Form -->
      <form class="login-form" @submit.prevent="login">
        <div class="input-group">
          <div class="input-wrapper">
            <User class="input-icon"/>
            <input
              v-model="form.username"
              type="text"
              placeholder="用户名或邮箱"
              autocomplete="username"
            />
          </div>
        </div>

        <div class="input-group">
          <div class="input-wrapper">
            <Lock class="input-icon"/>
            <input
              v-model="form.password"
              type="password"
              placeholder="密码"
              autocomplete="current-password"
              @input="onPasswordInput"
              @blur="onPasswordBlur"
            />
          </div>
        </div>

        <!-- Remember & Forgot -->
        <div class="form-options">
          <label class="checkbox-wrapper">
            <input type="checkbox" v-model="form.remember"/>
            <span class="checkmark"></span>
            <span class="checkbox-label">记住我</span>
          </label>
          <a href="#" class="forgot-link" @click.prevent="router.push('/forget')">忘记密码？</a>
        </div>

        <!-- Submit Button -->
        <button type="submit" class="btn-primary" :class="{ loading: loading.login }">
          <span v-if="!loading.login">登录</span>
          <span v-else class="loading-dots">
            <span></span>
            <span></span>
            <span></span>
          </span>
        </button>
      </form>

      <!-- Divider -->
      <div class="divider">
        <span>没有账户</span>
      </div>

      <!-- Register -->
      <button class="btn-secondary" @click="router.push('/register')">
        创建账户
      </button>

      <!-- Admin -->
      <button class="btn-ghost" @click="router.push('/admin-login')">
        管理员登录
      </button>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f7;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', sans-serif;
  position: relative;
  overflow: hidden;
}

.login-card {
  width: 100%;
  max-width: 380px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.01),
    0 12px 40px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.5);
  position: relative;
  z-index: 10;
  animation: cardIn 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.brand {
  text-align: center;
  margin-bottom: 36px;
}

.brand-icon {
  display: inline-flex;
  margin-bottom: 16px;
}

.brand-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.5px;
  margin-bottom: 8px;
}

.brand-subtitle {
  font-size: 14px;
  font-weight: 400;
  color: #86868b;
  letter-spacing: -0.1px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group {
  position: relative;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #ffffff;
  border: 1px solid #d2d2d7;
  border-radius: 12px;
  padding: 0 16px;
  height: 52px;
  transition: all 0.2s ease;
}

.input-wrapper:focus-within {
  border-color: #0071e3;
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.12);
}

.input-icon {
  width: 18px;
  height: 18px;
  color: #86868b;
  flex-shrink: 0;
}

.input-wrapper input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  font-family: inherit;
  color: #1d1d1f;
  margin-left: 12px;
  height: 100%;
}

.input-wrapper input::placeholder {
  color: #86868b;
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
  margin-top: 4px;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
}

.checkbox-wrapper input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.checkmark {
  width: 18px;
  height: 18px;
  background: #ffffff;
  border: 1.5px solid #d2d2d7;
  border-radius: 5px;
  transition: all 0.2s ease;
  position: relative;
}

.checkbox-wrapper:hover .checkmark {
  border-color: #86868b;
}

.checkbox-wrapper input:checked ~ .checkmark {
  background: #0071e3;
  border-color: #0071e3;
}

.checkbox-wrapper input:checked ~ .checkmark::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 5px;
  height: 9px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-label {
  font-size: 13px;
  color: #1d1d1f;
  margin-left: 10px;
  user-select: none;
}

.forgot-link {
  font-size: 13px;
  color: #0071e3;
  text-decoration: none;
  transition: color 0.2s ease;
}

.forgot-link:hover {
  color: #0077ed;
  text-decoration: underline;
}

.btn-primary {
  width: 100%;
  height: 52px;
  background: #0071e3;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 8px;
  letter-spacing: -0.2px;
}

.btn-primary:hover {
  background: #0077ed;
}

.btn-primary:active {
  background: #005bb5;
  transform: scale(0.98);
}

.btn-primary.loading {
  pointer-events: none;
  opacity: 0.8;
}

.loading-dots {
  display: inline-flex;
  gap: 4px;
}

.loading-dots span {
  width: 6px;
  height: 6px;
  background: #ffffff;
  border-radius: 50%;
  animation: loadingDot 1s ease-in-out infinite;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.15s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes loadingDot {
  0%, 80%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}

.divider {
  display: flex;
  align-items: center;
  margin: 24px 0;
  gap: 16px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #d2d2d7;
}

.divider span {
  font-size: 12px;
  color: #86868b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-secondary {
  width: 100%;
  height: 52px;
  background: #f5f5f7;
  color: #1d1d1f;
  border: 1px solid #d2d2d7;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: -0.2px;
}

.btn-secondary:hover {
  background: #e8e8ed;
}

.btn-secondary:active {
  background: #dcdce1;
  transform: scale(0.98);
}

.btn-ghost {
  width: 100%;
  height: 44px;
  background: transparent;
  color: #86868b;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 400;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 12px;
}

.btn-ghost:hover {
  color: #1d1d1f;
  background: rgba(0, 0, 0, 0.03);
}

/* Responsive */
@media (max-width: 480px) {
  .login-card {
    margin: 20px;
    padding: 36px 28px;
  }

  .brand-title {
    font-size: 24px;
  }
}
</style>
