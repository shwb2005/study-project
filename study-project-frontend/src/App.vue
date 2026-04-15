<script setup>
import { get } from "@/net";
import { useStore } from "@/stores/index.js";
import { reactive } from "vue";
import router from "@/router/index.js";
import { useRoute } from "vue-router";

const store = useStore()
const route = useRoute()

// Tab 顺序，用于判断切换方向
const tabOrder = ['/profile', '/courses', '/announcements', '/study-plan']

// 上一次访问的 tab 索引
let lastTabIndex = -1

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

if (store.auth.user == null) {
  get('/api/user/me', (message) => {
    store.auth.user = message
    router.push('/index')
  }, () => {
    store.auth.user = null
  })
}
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
  transform: translateX(28px);
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
