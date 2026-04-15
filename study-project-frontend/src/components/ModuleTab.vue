<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const tabs = [
  {
    label: '首页',
    route: '/index',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
      <polyline points="9 22 9 12 15 12 15 22"/>
    </svg>`
  },
  {
    label: '个人中心',
    route: '/profile',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
      <circle cx="12" cy="7" r="4"/>
    </svg>`
  },
  {
    label: '课程中心',
    route: '/courses',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
    </svg>`
  },
  {
    label: '学习计划',
    route: '/study-plan',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
      <line x1="16" y1="2" x2="16" y2="6"/>
      <line x1="8" y1="2" x2="8" y2="6"/>
      <line x1="3" y1="10" x2="21" y2="10"/>
      <path d="M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01M16 18h.01"/>
    </svg>`
  },
  {
    label: '通知公告',
    route: '/announcements',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
      <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
    </svg>`
  },
]

const activeIndex = computed(() => {
  const idx = tabs.findIndex(t => route.path.startsWith(t.route))
  // /index 单独处理，不高亮任何 tab
  return idx
})

const navigate = (path) => {
  router.push(path)
}
</script>

<template>
  <nav class="module-menu">
    <a
      v-for="(tab, i) in tabs"
      :key="tab.route"
      class="menu-link"
      :class="{ active: activeIndex === i }"
      href="javascript:void(0)"
      @click="navigate(tab.route)"
    >
      <span class="link-icon" v-html="tab.icon"></span>
      <span class="link-title">{{ tab.label }}</span>
    </a>
  </nav>
</template>

<style scoped>
.module-menu {
  padding: 5px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 12px;
  border: 0.5px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.menu-link {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 44px;
  height: 42px;
  border-radius: 8px;
  position: relative;
  z-index: 1;
  overflow: hidden;
  transform-origin: center left;
  transition: width 0.2s ease-in, color 0.2s ease-in;
  text-decoration: none;
  color: #6e6e73;
  background: none;
  border: none;
  cursor: pointer;
}

.menu-link::before {
  position: absolute;
  z-index: -1;
  content: "";
  display: block;
  border-radius: 8px;
  width: 100%;
  height: 100%;
  top: 0;
  transform: translateX(100%);
  transition: transform 0.2s ease-in;
  transform-origin: center right;
  background: rgba(0, 0, 0, 0.06);
}

.menu-link.active {
  color: #185ee0;
  width: 108px;
}

.menu-link.active::before {
  transform: translateX(0);
  background: rgba(24, 94, 224, 0.1);
}

.menu-link:hover,
.menu-link:focus {
  outline: 0;
  width: 108px;
  color: #185ee0;
}

.menu-link:hover::before,
.menu-link:focus::before {
  transform: translateX(0);
}

.link-icon {
  width: 26px;
  height: 26px;
  display: block;
  flex-shrink: 0;
  position: absolute;
  left: 9px;
}

.link-icon :deep(svg) {
  width: 26px;
  height: 26px;
}

.link-title {
  transform: translateX(100%);
  transition: transform 0.2s ease-in, opacity 0.15s ease-in;
  transform-origin: center right;
  display: block;
  text-align: center;
  text-indent: 26px;
  width: 100%;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  opacity: 0;
}

.menu-link:hover .link-title,
.menu-link:focus .link-title,
.menu-link.active .link-title {
  transform: translateX(0);
  opacity: 1;
}

@media (max-width: 430px) {
  .module-menu {
    padding: 3px;
  }

  .menu-link {
    width: 36px;
    height: 36px;
  }

  .menu-link:hover,
  .menu-link:focus,
  .menu-link.active {
    width: 88px;
  }

  .link-icon {
    width: 20px;
    height: 20px;
    left: 8px;
  }

  .link-icon :deep(svg) {
    width: 20px;
    height: 20px;
  }

  .link-title {
    font-size: 11px;
    text-indent: 20px;
  }
}
</style>
