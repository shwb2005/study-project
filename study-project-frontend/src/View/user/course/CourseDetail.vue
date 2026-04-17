<script setup>
import {ref, computed, onMounted, onUnmounted} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {get} from '@/net'

const router = useRouter()
const route = useRoute()
const courseId = route.params.id

const course = ref(null)
const chapters = ref([])
const activeChapterIdx = ref(0)
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

const loadChapters = () => {
  get('/api/course/' + courseId + '/chapters', data => {
    chapters.value = data || []
    // 默认选中第一个有视频的章节
    if (chapters.value.length > 0) {
      const firstWithVideo = chapters.value.findIndex(c => c.videoUrl)
      activeChapterIdx.value = firstWithVideo >= 0 ? firstWithVideo : 0
    }
  }, () => {
    chapters.value = []
  })
}

const selectChapter = (idx) => {
  activeChapterIdx.value = idx
}

onMounted(() => {
  loadCourse()
  loadChapters()
  loading.value = false
  window.addEventListener('scroll', handleScroll, {passive: true})
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const isEmbedUrl = (url) => {
  if (!url) return false
  return url.includes('youtube.com/embed') ||
         url.includes('player.bilibili.com') ||
         url.includes('iframe') ||
         /www\.bilibili\.com\/video\//.test(url)
}

const getEmbedUrl = (url) => {
  if (!url) return url
  const bilibiliMatch = url.match(/www\.bilibili\.com\/video\/(BV[a-zA-Z0-9]+)/)
  if (bilibiliMatch) {
    return 'https://player.bilibili.com/player.html?bvid=' + bilibiliMatch[1] + '&autoplay=0'
  }
  return url
}

const activeChapter = computed(() => {
  return chapters.value[activeChapterIdx.value] || null
})
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
            <span class="stat-n">{{ chapters.length }}</span>
            <span class="stat-l">章节数</span>
          </div>
        </div>
      </section>

      <!-- 当前选中章节的视频 -->
      <div v-if="activeChapter && activeChapter.videoUrl" class="video-card">
        <div class="video-header">
          <div class="video-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="18" height="18">
              <polygon points="5 3 19 12 5 21 5 3" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            {{ activeChapter.title }}
          </div>
        </div>
        <div class="video-wrap">
          <iframe v-if="isEmbedUrl(activeChapter.videoUrl)" :src="getEmbedUrl(activeChapter.videoUrl)" class="video-iframe"
                  frameborder="0" allowfullscreen allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"></iframe>
          <video v-else :src="activeChapter.videoUrl" class="video-player" controls></video>
        </div>
      </div>

      <!-- 章节列表 -->
      <section class="glass-card chapters-card">
        <div class="chapters-header">
          <h2 class="chapters-title">课程章节</h2>
          <span class="chapters-count">{{ chapters.length }} 节</span>
        </div>
        <div v-if="chapters.length === 0" class="chapters-empty">
          <p>暂无章节内容</p>
        </div>
        <div class="chapter-list" v-else>
          <div
            v-for="(chapter, idx) in chapters"
            :key="chapter.id"
            class="chapter-item"
            :class="{ active: idx === activeChapterIdx }"
            @click="selectChapter(idx)"
          >
            <span class="chapter-num">{{ idx + 1 }}</span>
            <span class="chapter-title">{{ chapter.title }}</span>
            <span v-if="chapter.videoUrl" class="chapter-tag">含视频</span>
          </div>
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
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 32px 80px;
  display: flex; flex-direction: column; gap: 20px;
}

.glass-card {
  border-radius: 22px;
  padding: 32px;
  background: rgba(255,255,255,0.62);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.08), 0 0.5px 0 rgba(255,255,255,0.95) inset;
}

.hero-top {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 16px;
}
.hero-top .course-title {
  flex: 1;
}
.card-cover {
  width: 100px; height: 100px;
  border-radius: 18px;
  object-fit: cover;
  flex-shrink: 0;
  order: 2;
}

.video-card {
  background: rgba(255,255,255,0.62);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border-radius: 22px;
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.08), 0 0.5px 0 rgba(255,255,255,0.95) inset;
  overflow: hidden;
}
.video-header {
  padding: 22px 28px 18px;
  border-bottom: 0.5px solid rgba(0,0,0,0.07);
}
.video-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
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

.hero-card { padding: 36px 32px; }
.course-title {
  font-size: 30px; font-weight: 700;
  letter-spacing: -0.03em; color: #1d1d1f;
  margin-bottom: 14px; line-height: 1.3;
}
.course-desc {
  font-size: 16px; color: #6e6e73;
  line-height: 1.65; margin-bottom: 22px;
}
.meta-row { display: flex; gap: 24px; margin-bottom: 22px; }
.meta-item {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 14px; color: #86868b; font-weight: 500;
}
.stats-inset {
  display: flex; align-items: center;
  border-radius: 14px; padding: 18px 0;
  background: rgba(0,0,0,0.04);
  box-shadow: 0 2px 4px rgba(0,0,0,0.07) inset, 0 1px 0 rgba(255,255,255,0.88);
  border: 0.5px solid rgba(0,0,0,0.06);
}
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stat-n { font-size: 26px; font-weight: 700; letter-spacing: -0.04em; color: #1d1d1f; line-height: 1; }
.stat-l { font-size: 12px; color: #86868b; }
.stat-sep { width: 0.5px; height: 36px; background: rgba(0,0,0,0.08); }

.chapters-card { padding: 32px; }
.chapters-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 22px;
}
.chapters-title {
  font-size: 19px; font-weight: 700;
  letter-spacing: -0.02em; color: #1d1d1f;
}
.chapters-count {
  font-size: 14px; color: #86868b;
}
.chapters-empty {
  text-align: center; padding: 40px; color: #86868b; font-size: 15px;
}
.chapter-list {
  display: flex; flex-direction: column; gap: 10px;
}
.chapter-item {
  display: flex; align-items: center; gap: 14px;
  padding: 16px 18px;
  background: rgba(255,255,255,0.5);
  border-radius: 13px;
  border: 1px solid rgba(0,0,0,0.06);
  cursor: pointer;
  transition: all 0.15s;
}
.chapter-item:hover {
  background: rgba(255,255,255,0.8);
  border-color: rgba(0,0,0,0.1);
}
.chapter-item.active {
  background: rgba(0,113,227,0.08);
  border-color: rgba(0,113,227,0.3);
}
.chapter-num {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.06);
  border-radius: 9px;
  font-size: 14px; font-weight: 600;
  color: #6e6e73;
  flex-shrink: 0;
}
.chapter-item.active .chapter-num {
  background: rgba(0,113,227,0.15);
  color: #0071e3;
}
.chapter-title {
  flex: 1;
  font-size: 16px; font-weight: 500;
  color: #1d1d1f;
}
.chapter-tag {
  padding: 4px 10px;
  background: rgba(52,199,89,0.12);
  color: #34c759;
  font-size: 11px; font-weight: 600;
  border-radius: 6px;
  flex-shrink: 0;
}

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
  .main { padding: 20px 16px 60px; max-width: 100%; }
  .navbar { padding: 0 16px; }
  .glass-card { padding: 24px; }
  .hero-card { padding: 24px; }
  .course-title { font-size: 24px; }
  .meta-row { flex-wrap: wrap; gap: 14px; }
  .card-cover { width: 80px; height: 80px; }
  .chapters-card { padding: 24px; }
}
</style>
