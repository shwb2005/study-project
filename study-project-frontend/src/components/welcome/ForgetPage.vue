<script setup>
import {reactive, ref} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {post} from "@/net/index.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const form = reactive({
  email: '',
  code: '',
  password: '',
  password_repeat: ''
})

const active = ref(0)
const errors = reactive({
  email: '',
  code: '',
  password: '',
  password_repeat: ''
})

const isEmailValid = ref(true);
const coldTime = ref(0);

const validateEmail = () => {
  if (!form.email) {
    errors.email = '请输入邮箱'
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '请输入合法的邮箱'
    return
  }
  post('api/auth/valid-reset-email', {
    email: form.email
  }, (message) => {
    ElMessage.success(message)
    coldTime.value = 60
    setInterval(() => coldTime.value--, 1000)
  })
}

const startReset = () => {
  let valid = true
  if (!form.email) {
    errors.email = '请输入邮箱'
    valid = false
  }
  if (!form.code) {
    errors.code = '请输入验证码'
    valid = false
  }
  if (!valid) return

  post('/api/auth/start-reset', {
    email: form.email,
    code: form.code
  }, () => {
    active.value++
  }, (error) => {
    ElMessage.warning(error || "验证失败")
  })
}

const doReset = () => {
  let valid = true
  if (!form.password) {
    errors.password = '请输入密码'
    valid = false
  } else if (form.password.length < 6 || form.password.length > 16) {
    errors.password = '密码长度必须在6-16个字符之间'
    valid = false
  }
  if (!form.password_repeat) {
    errors.password_repeat = '请再次输入密码'
    valid = false
  } else if (form.password !== form.password_repeat) {
    errors.password_repeat = '两次输入的密码不一致'
    valid = false
  }
  if (!valid) return

  post('/api/auth/do-reset', {
    password: form.password
  }, (message) => {
    ElMessage.success(message)
    window.location.href = '/?from=reset';
  })
}

const clearError = (field) => {
  errors[field] = ''
}

</script>

<template>
  <div class="reset-container">
    <div class="reset-card">
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
        <p class="brand-subtitle">重置您的密码</p>
      </div>

      <!-- Steps Indicator -->
      <div class="steps-indicator">
        <div class="step" :class="{ active: active >= 0, done: active > 0 }">
          <div class="step-dot"></div>
          <span class="step-label">验证邮箱</span>
        </div>
        <div class="step-line" :class="{ active: active > 0 }"></div>
        <div class="step" :class="{ active: active >= 1 }">
          <div class="step-dot"></div>
          <span class="step-label">重置密码</span>
        </div>
      </div>

      <!-- Step 0: Email Verification -->
      <transition name="fade" mode="out-in">
        <form v-if="active === 0" class="reset-form" @submit.prevent="startReset">
          <div class="input-group">
            <div class="input-wrapper" :class="{ error: errors.email }">
              <Message class="input-icon"/>
              <input
                v-model="form.email"
                type="email"
                placeholder="电子邮件地址"
                autocomplete="email"
                @input="clearError('email')"
              />
            </div>
            <transition name="fade">
              <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
            </transition>
          </div>

          <div class="input-group">
            <div class="code-row">
              <div class="input-wrapper code-input" :class="{ error: errors.code }">
                <EditPen class="input-icon"/>
                <input
                  v-model="form.code"
                  type="text"
                  placeholder="验证码"
                  maxlength="6"
                  autocomplete="one-time-code"
                  @input="clearError('code')"
                />
              </div>
              <button
                type="button"
                class="btn-code"
                :disabled="isEmailValid || coldTime > 0"
                @click="validateEmail"
              >
                {{ coldTime > 0 ? `${coldTime}s` : '获取验证码' }}
              </button>
            </div>
            <transition name="fade">
              <span v-if="errors.code" class="error-text">{{ errors.code }}</span>
            </transition>
          </div>

          <button type="submit" class="btn-primary">
            验证并继续
          </button>
        </form>
      </transition>

      <!-- Step 1: Reset Password -->
      <transition name="fade" mode="out-in">
        <form v-if="active === 1" class="reset-form" @submit.prevent="doReset">
          <div class="input-group">
            <div class="input-wrapper" :class="{ error: errors.password }">
              <Lock class="input-icon"/>
              <input
                v-model="form.password"
                type="password"
                placeholder="新密码"
                maxlength="16"
                autocomplete="new-password"
                @input="clearError('password')"
              />
            </div>
            <transition name="fade">
              <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
            </transition>
          </div>

          <div class="input-group">
            <div class="input-wrapper" :class="{ error: errors.password_repeat }">
              <Lock class="input-icon"/>
              <input
                v-model="form.password_repeat"
                type="password"
                placeholder="重复新密码"
                maxlength="16"
                autocomplete="new-password"
                @input="clearError('password_repeat')"
              />
            </div>
            <transition name="fade">
              <span v-if="errors.password_repeat" class="error-text">{{ errors.password_repeat }}</span>
            </transition>
          </div>

          <button type="submit" class="btn-primary">
            立即重置
          </button>
        </form>
      </transition>

      <!-- Divider -->
      <div class="divider">
        <span>想起密码了</span>
      </div>

      <!-- Back to Login -->
      <button class="btn-secondary" @click="router.push('/')">
        返回登录
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

.reset-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f7;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', sans-serif;
  position: relative;
  overflow: hidden;
}

.reset-card {
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
  margin-bottom: 28px;
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

/* Steps Indicator */
.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  gap: 0;
}

.step {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d2d2d7;
  transition: all 0.3s ease;
}

.step.active .step-dot {
  background: #0071e3;
}

.step.done .step-dot {
  background: #30d158;
}

.step-label {
  font-size: 13px;
  color: #86868b;
  font-weight: 400;
  transition: color 0.3s ease;
}

.step.active .step-label {
  color: #1d1d1f;
  font-weight: 500;
}

.step-line {
  width: 40px;
  height: 2px;
  background: #d2d2d7;
  margin: 0 12px;
  border-radius: 1px;
  transition: background 0.3s ease;
}

.step-line.active {
  background: #30d158;
}

/* Form */
.reset-form {
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

/* Code Row */
.code-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.code-input {
  flex: 1;
  min-width: 0;
}

.btn-code {
  flex-shrink: 0;
  height: 52px;
  padding: 0 12px;
  background: #f5f5f7;
  color: #0071e3;
  border: 1px solid #d2d2d7;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  min-width: 88px;
  text-align: center;
}

.btn-code:hover:not(:disabled) {
  background: #e8e8ed;
}

.btn-code:active:not(:disabled) {
  background: #dcdce1;
  transform: scale(0.98);
}

.btn-code:disabled {
  color: #86868b;
  cursor: not-allowed;
}

/* Buttons */
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
  .reset-card {
    margin: 20px;
    padding: 36px 28px;
  }

  .brand-title {
    font-size: 24px;
  }

  .btn-code {
    padding: 0 12px;
    font-size: 12px;
  }
}
</style>
