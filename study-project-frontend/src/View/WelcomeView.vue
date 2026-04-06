<template>
  <div class="welcome-container">
    <!-- Left: Animated Characters Panel -->
    <div class="left-panel">
      <div class="brand-logo">
        <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
          <rect width="32" height="32" rx="8" fill="rgba(255,255,255,0.2)"/>
          <path d="M16 6C10.5 6 6 10.5 6 16C6 21.5 10.5 26 16 26C21.5 26 26 21.5 26 16C26 10.5 21.5 6 16 6ZM16 22C12.7 22 10 19.3 10 16C10 12.7 12.7 10 16 10C19.3 10 22 12.7 22 16C22 19.3 19.3 22 16 22Z" fill="white"/>
          <circle cx="16" cy="16" r="3" fill="white"/>
        </svg>
        <span>Study</span>
      </div>
      <div class="characters-wrapper">
        <AnimatedCharacters
          :is-typing="store.loginPassword.isTyping"
          :show-password="store.loginPassword.showPassword"
          :password-length="store.loginPassword.passwordLength"
        />
      </div>
      <div class="footer-links">
        <a href="#">隐私政策</a>
        <a href="#">服务条款</a>
      </div>
    </div>

    <!-- Right: Form Panel -->
    <div class="right-panel">
      <router-view v-slot="{ Component}">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" style="height: 100%"/>
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import { useStore } from "@/stores/index.js";
import AnimatedCharacters from "@/components/AnimatedCharacters.vue";

const store = useStore()
</script>

<style scoped>
.welcome-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
}

.left-panel {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  overflow: hidden;
}

/* 光影效果 */
.left-panel::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(ellipse at 30% 20%, rgba(102, 126, 234, 0.08) 0%, transparent 50%),
              radial-gradient(ellipse at 70% 80%, rgba(118, 75, 162, 0.06) 0%, transparent 50%);
  pointer-events: none;
}

.left-panel::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(255,255,255,0.5) 0%, transparent 30%, transparent 70%, rgba(0,0,0,0.02) 100%);
  pointer-events: none;
}

.brand-logo {
  position: absolute;
  top: 40px;
  left: 40px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-size: 18px;
  font-weight: 600;
  z-index: 10;
}

.characters-wrapper {
  position: relative;
  z-index: 5;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  filter: drop-shadow(0 20px 40px rgba(102, 126, 234, 0.15));
}

.characters-wrapper::after {
  content: '';
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  width: 400px;
  height: 20px;
  background: radial-gradient(ellipse, rgba(102, 126, 234, 0.12) 0%, transparent 70%);
  border-radius: 50%;
}

.footer-links {
  position: absolute;
  bottom: 30px;
  display: flex;
  gap: 24px;
  z-index: 10;
}

.footer-links a {
  color: rgba(255,255,255,0.7);
  font-size: 13px;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: white;
}

.right-panel {
  width: 420px;
  background-color: white;
  z-index: 1;
  overflow-y: auto;
}

@media (max-width: 900px) {
  .welcome-container {
    flex-direction: column;
  }

  .left-panel {
    display: none;
  }

  .right-panel {
    width: 100%;
    min-height: 100vh;
  }
}
</style>
