<script setup>
import { get } from "@/net";
import { useStore } from "@/stores/index.js";
import { onMounted } from "vue";
import router from "@/router/index.js";
import { useRoute } from "vue-router";

const store = useStore()
const route = useRoute()

// Tab 顺序，用于判断切换方向
const tabOrder = ['/profile', '/courses', '/announcements', '/study-plan']

// 路由切换时判断方向并设置 transition
router.beforeEach((to, from) => {
  const toIdx = tabOrder.findIndex(r => to.path.startsWith(r))
  const fromIdx = tabOrder.findIndex(r => from.path.startsWith(r))
  if (toIdx >= 0 && fromIdx >= 0) {
    to.meta.transition = toIdx >= fromIdx ? 'slide-left' : 'slide-right'
  } else {
    to.meta.transition = 'fade'
  }
})

// 在应用启动时恢复登录状态
onMounted(() => {
  // 尝试从 localStorage 恢复管理员登录状态
  try {
    const adminAuth = localStorage.getItem('admin_auth')
    if (adminAuth) {
      store.auth.admin = JSON.parse(adminAuth)
    }
  } catch (e) {
    localStorage.removeItem('admin_auth')
  }

  // 只在非管理员页面检查普通用户登录状态
  if (!route.path.startsWith('/admin') && store.auth.user === null) {
    get('/api/user/me', (message) => {
      store.auth.user = message
      store.initialized = true
    }, () => {
      store.auth.user = null
      store.initialized = true
    })
  } else {
    store.initialized = true
  }
})
</script>

<template>
  <router-view v-slot="{ Component, route }">
    <transition :name="route.meta.transition || 'fade'" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<style scoped>
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-left-enter-from {
  opacity: 0;
  transform: translateX(28px);
}
.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-28px);
}
.slide-right-enter-from {
  opacity: 0;
  transform: translateX(-28px);
}
.slide-right-leave-to {
  opacity: 0;
  transform: translateX(-28px);
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
