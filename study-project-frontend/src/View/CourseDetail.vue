<script setup>
import {ref, onMounted, onUnmounted} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {get} from '@/net'

const router = useRouter()
const route = useRoute()
const courseId = route.params.id

const course = ref(null)
const detail = ref(null)
const loading = ref(true)

const bgRef = ref(null)
const scrollOverlay = ref(0)

const handleScroll = () => {
  const progress = Math.min(window.scrollY / 380, 1)
  if (bgRef.value) {
    bgRef.value.style.filter = `blur(${progress * 48}px) saturate(140%)`
  }
  scrollOverlay.value = progress * 0.52
}

const loadCourse = () => {
  get('/api/course/' + courseId, data => {
    course.value = data
  }, () => {
    course.value = null
  })
}

const loadDetail = () => {
  get('/api/course/' + courseId + '/detail', data => {
    detail.value = data
  }, () => {
    detail.value = null
  })
}

onMounted(() => {
  loadCourse()
  loadDetail()
  loading.value = false
  window.addEventListener('scroll', handleScroll, {passive: true})
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const isEmbedUrl = (url) => {
  if (!url) return false
  return url.includes('youtube.com/embed') || url.includes('player.bilibili.com') || url.includes('iframe')
}

const sections = [
  {key: 'objectives', title: '课程目标', icon: 'M9 5l7 7-7 7'},
  {key: 'outline', title: '课程大纲', icon: 'M4 6h16M4 12h16M4 18h10'},
  {key: 'requirements', title: '先修要求', icon: 'M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.56 5.82 22 7 14.14l-5-4.87 6.91-1.01z'},
  {key: 'audience', title: '适合人群', icon: 'M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2M9 11a4 4 0 100-8 4 4 0 000 8zM23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75'},
  {key: 'materials', title: '参考资料', icon: 'M4 19.5A2.5 2.5 0 016.5 17H20M4 19.5A2.5 2.5 0 016.5 17H20M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z'}
]
</script>

<template>
  <div class="page">
    <div class="bg" ref="bgRef"></div>
    <div class="bg-dim" :style="{ background: `rgba(240,246,252,${scrollOverlay})` }"></div>

    <header class="navbar">
      <button class="nav-btn" @click="router.back()">
        <svg width="9" height="16" viewBox="0 0 9 16" fill="none">
          <path d="M8 1L1 8L8 15" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回
      </button>
      <span class="navbar-title">课程详情</span>
      <span style="width:52px"></span>
    </header>

    <main class="main" v-if="course">


      <!-- 课程基本信息 -->
      <section class="glass-card hero-card">
        <div class="hero-top">
          <img v-if="course.coverImage" :src="course.coverImage" alt="课程封面" class="card-cover" @error="$event.target.style.display='none'" />
          <h1 class="course-title">{{ course.name }}</h1>
        </div>
        <p class="course-desc">{{ course.description }}</p>
        <div class="meta-row">
          <span class="meta-item">
            <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="14" height="14">
              <path d="M9 9a3.5 3.5 0 100-7 3.5 3.5 0 000 7zM2.5 16.5s-.5-5 6.5-5 6.5 5 6.5 5" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
            {{ course.teacherName }}
          </span>
          <span class="meta-item">
            <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="14" height="14">
              <circle cx="9" cy="9" r="7.5" stroke-width="1.6"/>
              <path d="M9 5v4l2.5 2" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
            {{ course.duration }}h
          </span>
          <span class="meta-item">
            <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="14" height="14">
              <path d="M12.5 5C12.5 3.34 11.16 2 9.5 2S6.5 3.34 6.5 5H2v1.5l3 7V16h9v-2.5l3-7V5h-4.5z" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            {{ course.studentsCount }} 学员
          </span>
        </div>
        <div class="stats-inset">
          <div class="stat-item">
            <span class="stat-n">{{ course.rating || '—' }}</span>
            <span class="stat-l">评分</span>
          </div>
          <div class="stat-sep"></div>
          <div class="stat-item">
            <span class="stat-n">{{ course.maxCheckInCount || 12 }}</span>
            <span class="stat-l">签到次数</span>
          </div>
        </div>
      </section>

      <!-- 视频 -->
      <div v-if="course.videoUrl" class="video-card">
        <div class="video-header">
          <div class="video-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="18" height="18">
              <polygon points="5 3 19 12 5 21 5 3" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            课程视频
          </div>
        </div>
        <div class="video-wrap">
          <iframe v-if="isEmbedUrl(course.videoUrl)" :src="course.videoUrl" class="video-iframe"
                  frameborder="0" allowfullscreen allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"></iframe>
          <video v-else :src="course.videoUrl" class="video-player" controls></video>
        </div>
      </div>

      <!-- 课程详情板块 -->
      <template v-if="detail">
        <section v-for="sec in sections" :key="sec.key" class="glass-card detail-section" v-show="detail[sec.key]">
          <div class="section-header">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="18" height="18">
              <path :d="sec.icon" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h2 class="section-title">{{ sec.title }}</h2>
          </div>
          <div class="section-body">
            <p v-for="(line, i) in detail[sec.key].split('\n')" :key="i">{{ line }}</p>
          </div>
        </section>
      </template>

      <!-- 无详情 -->
      <section v-if="!detail || sections.every(s => !detail[s.key])" class="glass-card empty-section">
        <div class="empty-inner">
          <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
            <path d="M6 10h36v28H6z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M6 18h36M16 10v28M6 26h10" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <p class="empty-text">暂无详细内容</p>
        </div>
      </section>
    </main>

    <!-- 加载中 -->
    <main class="main" v-if="loading">
      <div class="glass-card loading-card">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
    </main>

    <!-- 课程不存在 -->
    <main class="main" v-if="!loading && !course">
      <div class="glass-card loading-card">
        <p>课程不存在或已被删除</p>
        <button class="back-btn" @click="router.push('/courses')">返回课程中心</button>
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
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 20px;
  background: rgba(255,255,255,0.65);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border-bottom: 0.5px solid rgba(255,255,255,0.65);
  box-shadow: 0 1px 0 rgba(0,0,0,0.05);
}

.nav-btn {
  display: flex; align-items: center; gap: 5px;
  background: none; border: none; cursor: pointer;
  font-family: inherit; font-size: 15px;
  color: #0071e3; padding: 0;
  transition: opacity 0.1s;
}
.nav-btn:hover { opacity: 0.7; }
.navbar-title { font-size: 16px; font-weight: 600; letter-spacing: -0.02em; }

.main {
  position: relative; z-index: 2;
  max-width: 720px;
  margin: 0 auto;
  padding: 28px 18px 72px;
  display: flex; flex-direction: column; gap: 14px;
}

.glass-card {
  border-radius: 20px;
  padding: 22px;
  background: rgba(255,255,255,0.62);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.08), 0 0.5px 0 rgba(255,255,255,0.95) inset;
}

/* Cover image + title side by side */
.hero-top {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  margin-bottom: 14px;
}
.hero-top .course-title {
  flex: 1;
}
.card-cover {
  width: 80px; height: 80px;
  border-radius: 16px;
  object-fit: cover;
  flex-shrink: 0;
  order: 2;
}

/* Video */
.video-card {
  background: rgba(255,255,255,0.62);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border-radius: 20px;
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.08), 0 0.5px 0 rgba(255,255,255,0.95) inset;
  overflow: hidden;
}
.video-header {
  padding: 18px 22px 14px;
  border-bottom: 0.5px solid rgba(0,0,0,0.07);
}
.video-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}
.video-wrap {
  background: #000;
}
.video-iframe, .video-player {
  width: 100%; aspect-ratio: 16/9; display: block;
  border: none;
}

/* Hero card */
.hero-card { padding: 28px 24px; }
.course-title {
  font-size: 26px; font-weight: 700;
  letter-spacing: -0.03em; color: #1d1d1f;
  margin-bottom: 12px; line-height: 1.3;
}
.course-desc {
  font-size: 15px; color: #6e6e73;
  line-height: 1.65; margin-bottom: 18px;
}
.meta-row { display: flex; gap: 20px; margin-bottom: 18px; }
.meta-item {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 13px; color: #86868b; font-weight: 500;
}
.stats-inset {
  display: flex; align-items: center;
  border-radius: 12px; padding: 14px 0;
  background: rgba(0,0,0,0.04);
  box-shadow: 0 2px 4px rgba(0,0,0,0.07) inset, 0 1px 0 rgba(255,255,255,0.88);
  border: 0.5px solid rgba(0,0,0,0.06);
}
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 3px; }
.stat-n { font-size: 22px; font-weight: 700; letter-spacing: -0.04em; color: #1d1d1f; line-height: 1; }
.stat-l { font-size: 11px; color: #86868b; }
.stat-sep { width: 0.5px; height: 30px; background: rgba(0,0,0,0.08); }

/* Detail sections */
.detail-section { padding: 22px 24px; }
.section-header {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 16px; color: #1d1d1f;
}
.section-title {
  font-size: 17px; font-weight: 700;
  letter-spacing: -0.02em;
}
.section-body {
  font-size: 14px; color: #3a3a3c;
  line-height: 1.8; white-space: pre-line;
}
.section-body p { margin-bottom: 4px; }

/* Empty / Loading */
.empty-section { padding: 40px; }
.empty-inner {
  display: flex; flex-direction: column; align-items: center; gap: 12px; color: #c7c7cc;
}
.empty-text { font-size: 15px; color: #86868b; }
.loading-card {
  display: flex; flex-direction: column; align-items: center; gap: 14px;
  padding: 60px; color: #86868b; font-size: 15px;
}
.spinner {
  width: 24px; height: 24px;
  border: 2.5px solid rgba(0,0,0,0.1);
  border-top-color: #1d1d1f; border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.back-btn {
  margin-top: 10px; padding: 10px 22px;
  border-radius: 980px; font-size: 14px; font-weight: 600;
  cursor: pointer; border: none; font-family: inherit;
  background: rgba(29,29,31,0.88); color: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.2);
  transition: all 0.15s;
}
.back-btn:hover { background: rgba(0,0,0,0.95); }

@media (max-width: 768px) {
  .main { padding: 16px 14px 52px; }
  .navbar { padding: 0 16px; }
  .hero-card { padding: 20px 18px; }
  .course-title { font-size: 22px; }
  .meta-row { flex-wrap: wrap; gap: 12px; }
}
</style>
