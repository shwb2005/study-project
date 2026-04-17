<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AllCourses from '@/View/user/course/AllCourses.vue'
import MyCourses from '@/View/user/course/MyCourses.vue'
import FavoriteCourses from '@/View/user/course/FavoriteCourses.vue'
import Community from '@/View/user/course/Community.vue'
import ModuleTab from '@/components/ModuleTab.vue'

const router = useRouter()
const activeTab = ref('all')

const bgRef = ref(null)
const scrollOverlay = ref(0)

const handleScroll = () => {
  const progress = Math.min(window.scrollY / 380, 1)
  if (bgRef.value) {
    bgRef.value.style.filter = `blur(${progress * 48}px) saturate(140%)`
  }
  scrollOverlay.value = progress * 0.52
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="page">
    <div class="bg" ref="bgRef"></div>
    <div class="bg-dim" :style="{ background: `rgba(240,246,252,${scrollOverlay})` }"></div>

    <header class="navbar">
      <div class="navbar-center">
        <ModuleTab />
      </div>
    </header>

    <main class="main">
      <div class="glass-tabs">
        <div class="tab-bar">
          <button class="tab-btn" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">
            所有课程
          </button>
          <button class="tab-btn" :class="{ active: activeTab === 'my' }" @click="activeTab = 'my'">
            我的课程
          </button>
          <button class="tab-btn" :class="{ active: activeTab === 'favorites' }" @click="activeTab = 'favorites'">
            我的收藏
          </button>
          <button class="tab-btn" :class="{ active: activeTab === 'community' }" @click="activeTab = 'community'">
            社区
          </button>
        </div>
        <div class="tab-content">
          <AllCourses v-if="activeTab === 'all'" />
          <MyCourses v-if="activeTab === 'my'" />
          <FavoriteCourses v-if="activeTab === 'favorites'" />
          <Community v-if="activeTab === 'community'" />
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.page {
  min-height: 100vh;
  position: relative;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  color: #1d1d1f;
  -webkit-font-smoothing: antialiased;
}

.bg {
  position: fixed; inset: 0; z-index: 0;
  background-image: url('@/assets/images/4.jpg');
  background-size: cover;
  background-position: center 40%;
  background-repeat: no-repeat;
  transition: filter 0.1s linear;
}

.bg-dim {
  position: fixed; inset: 0; z-index: 1;
  transition: background 0.1s linear;
  pointer-events: none;
}

.navbar {
  position: sticky; top: 0; z-index: 100;
  height: 52px;
  display: flex; align-items: center; justify-content: center;
  padding: 0 20px;
  background: rgba(255,255,255,0.65);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border-bottom: 0.5px solid rgba(255,255,255,0.65);
  box-shadow: 0 1px 0 rgba(0,0,0,0.05);
}
.navbar-center {
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-btn {
  display: flex; align-items: center; gap: 5px;
  background: none; border: none; cursor: pointer;
  font-family: inherit; font-size: 15px;
  color: #0071e3; padding: 0;
  transition: opacity 0.1s;
}
.nav-btn:hover { opacity: 0.7; }
.navbar-title { font-size: 16px; font-weight: 600; letter-spacing: -0.02em; color: #1d1d1f; }

.main {
  position: relative; z-index: 2;
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 20px 72px;
}

.glass-tabs {
  border-radius: 20px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow:
      0 2px 32px rgba(0,0,0,0.10),
      0 0.5px 0 rgba(255,255,255,0.95) inset;
}

.tab-bar {
  display: flex;
  gap: 4px;
  padding: 12px 16px 0;
  border-bottom: 0.5px solid rgba(0,0,0,0.07);
}

.tab-btn {
  position: relative;
  padding: 10px 20px;
  background: none; border: none; cursor: pointer;
  font-family: inherit; font-size: 14px; font-weight: 500;
  color: #86868b; border-radius: 10px 10px 0 0;
  transition: color 0.15s, background 0.15s;
  letter-spacing: -0.01em;
}
.tab-btn:hover { color: #1d1d1f; background: rgba(0,0,0,0.03); }
.tab-btn.active { color: #1d1d1f; font-weight: 700; }
.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -0.5px; left: 16px; right: 16px;
  height: 2px;
  background: #1d1d1f;
  border-radius: 2px 2px 0 0;
}

.tab-content { padding: 24px 20px; }

@media (max-width: 768px) {
  .main { padding: 16px 14px 52px; }
  .navbar { padding: 0 16px; }
  .tab-content { padding: 18px 14px; }
  .tab-btn { padding: 9px 14px; font-size: 13px; }
}
</style>