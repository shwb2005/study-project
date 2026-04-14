<script setup>
import {User, Lock} from '@element-plus/icons-vue'
import {ElMessage} from "element-plus";
import {reactive, ref, onMounted} from "vue";
import router from "@/router/index.js";
import {useStore} from "@/stores/index.js";
import {post, get} from "@/net/index.js";

const store = useStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const errors = reactive({
  username: '',
  password: ''
})

onMounted(() => {
  const rememberedAdmin = localStorage.getItem('remembered_admin')
  if (rememberedAdmin) {
    try {
      const adminInfo = JSON.parse(rememberedAdmin)
      form.username = adminInfo.username || ''
      form.password = adminInfo.password || ''
      form.remember = true
    } catch (e) {
      console.error('解析记住的管理员信息失败:', e)
    }
  }
})

const clearError = (field) => {
  errors[field] = ''
}

const login = () => {
  let valid = true
  if (!form.username) {
    errors.username = '请输入管理员用户名'
    valid = false
  }
  if (!form.password) {
    errors.password = '请输入密码'
    valid = false
  }
  if (!valid) return

  loading.value = true

  post('/api/admin/login', {
    username: form.username,
    password: form.password
  }, () => {
    get('/api/admin/me', (adminInfo) => {
      const adminAuth = {
        username: adminInfo.username,
        role: adminInfo.role,
        isAdmin: true,
        loginTime: new Date().getTime()
      }
      store.auth.admin = adminAuth

      if (form.remember) {
        try {
          const rememberedInfo = {
            username: form.username,
            password: form.password
          }
          localStorage.setItem('remembered_admin', JSON.stringify(rememberedInfo))
        } catch (error) {
          console.error('保存管理员凭据失败:', error)
        }
      } else {
        localStorage.removeItem('remembered_admin')
      }

      try {
        localStorage.setItem('admin_auth', JSON.stringify(adminAuth))
      } catch (error) {
        console.error('保存到 localStorage 失败:', error)
      }

      ElMessage.success('登录成功')
      router.push('/admin')
    }, () => {
      ElMessage.error('获取管理员信息失败')
      loading.value = false
    })
  }, (message) => {
    ElMessage.error('登录失败: ' + message)
    loading.value = false
  }, (error) => {
    console.error('请求失败:', error)
    ElMessage.error('请求失败，请检查网络连接')
    loading.value = false
  })
}
</script>

<template>
  <div class="admin-login-container">
    <div class="admin-login-card">
      <!-- Logo / Brand -->
      <div class="brand">
        <div class="brand-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <rect width="40" height="40" rx="10" fill="#1d1d1f"/>
            <path d="M12 14h16v2H12v-2zm0 5h16v2H12v-2zm0 5h10v2H12v-2z" fill="#fff"/>
            <circle cx="30" cy="28" r="6" fill="#0071e3"/>
            <path d="M30 25a1 1 0 011 1v2h2a1 1 0 110 2h-2v2a1 1 0 11-2 0v-2h-2a1 1 0 110-2h2v-2a1 1 0 011-1z" fill="#fff"/>
          </svg>
        </div>
        <h1 class="brand-title">Admin</h1>
        <p class="brand-subtitle">管理员登录</p>
      </div>

      <!-- Form -->
      <form class="login-form" @submit.prevent="login">
        <div class="input-group">
          <div class="input-wrapper" :class="{ error: errors.username }">
            <User class="input-icon"/>
            <input
              v-model="form.username"
              type="text"
              placeholder="管理员用户名"
              autocomplete="username"
              @input="clearError('username')"
            />
          </div>
          <transition name="fade">
            <span v-if="errors.username" class="error-text">{{ errors.username }}</span>
          </transition>
        </div>

        <div class="input-group">
          <div class="input-wrapper" :class="{ error: errors.password }">
            <Lock class="input-icon"/>
            <input
              v-model="form.password"
              type="password"
              placeholder="密码"
              autocomplete="current-password"
              @input="clearError('password')"
            />
          </div>
          <transition name="fade">
            <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
          </transition>
        </div>

        <!-- Remember -->
        <div class="form-options">
          <label class="checkbox-wrapper">
            <input type="checkbox" v-model="form.remember"/>
            <span class="checkmark"></span>
            <span class="checkbox-label">记住我</span>
          </label>
        </div>

        <!-- Submit -->
        <button type="submit" class="btn-primary" :class="{ loading }">
          <span v-if="!loading">登录</span>
          <span v-else class="loading-dots">
            <span></span>
            <span></span>
            <span></span>
          </span>
        </button>
      </form>

      <!-- Divider -->
      <div class="divider">
        <span>普通用户</span>
      </div>

      <!-- Back to User Login -->
      <button class="btn-secondary" @click="router.push('/')">
        返回用户登录
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

.admin-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f7;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', sans-serif;
  position: relative;
  overflow: hidden;
}

.admin-login-card {
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

/* Form */
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

.input-wrapper.error {
  border-color: #ff3b30;
}

.input-wrapper.error:focus-within {
  box-shadow: 0 0 0 3px rgba(255, 59, 48, 0.12);
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

.error-text {
  display: block;
  font-size: 12px;
  color: #ff3b30;
  margin-top: 6px;
  padding-left: 4px;
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

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 480px) {
  .admin-login-card {
    margin: 20px;
    padding: 36px 28px;
  }

  .brand-title {
    font-size: 24px;
  }
}
</style>
