<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, post } from "@/net"
import { ElMessage } from "element-plus"

const courses = ref([])
const myCourses = ref([])
const favoriteIds = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 9
const search = ref('')
const teacherName = ref('')
let searchTimer = null

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

const loadCourses = () => {
  const params = new URLSearchParams({
    page: page.value, pageSize,
    ...(search.value && { search: search.value }),
    ...(teacherName.value && { teacherName: teacherName.value })
  })
  get('/api/course/list?' + params, data => {
    courses.value = data?.list || []
    total.value = data?.total || 0
    loadMyCourses()
    loadFavoriteIds()
  }, () => { courses.value = []; total.value = 0 })
}

const loadMyCourses = () => {
  get('/api/course/my-courses?page=1&pageSize=999', data => {
    const list = data?.list || data || []
    myCourses.value = list
  }, () => { myCourses.value = [] })
}

const loadFavoriteIds = () => {
  get('/api/course/favorite-ids?page=1&pageSize=999', data => {
    favoriteIds.value = data || []
  }, () => { favoriteIds.value = [] })
}

const isEnrolled = (courseId) => myCourses.value.some(c => (c.courseId || c.id) === courseId)
const isFavorited = (courseId) => favoriteIds.value.includes(courseId)

const toggleFavorite = (courseId) => {
  const wasFavorited = favoriteIds.value.includes(courseId)
  // 乐观更新
  if (wasFavorited) {
    favoriteIds.value = favoriteIds.value.filter(id => id !== courseId)
  } else {
    favoriteIds.value.push(courseId)
  }
  post('/api/course/favorite', { courseId }, () => {
    ElMessage.success(wasFavorited ? '已取消收藏' : '已收藏')
  }, () => {
    // 失败回滚
    if (wasFavorited) {
      favoriteIds.value.push(courseId)
    } else {
      favoriteIds.value = favoriteIds.value.filter(id => id !== courseId)
    }
    ElMessage.error('操作失败')
  })
}

const enrollCourse = (courseId) => {
  console.log('[enrollCourse] enrolling courseId:', courseId, 'myCourses before:', myCourses.value.length)
  post('/api/course/enroll', { courseId }, () => {
    console.log('[enrollCourse] success, optimistic update...')
    ElMessage.success('报名成功')
    // 乐观更新：直接加入本地 myCourses，不等重新请求
    if (!myCourses.value.some(m => (m.courseId || m.id) === courseId)) {
      myCourses.value.push({ courseId, id: courseId })
    }
  }, (err) => {
    console.error('[enrollCourse] failed:', err)
    ElMessage.error('报名失败')
  })
}

const onSearchInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { page.value = 1; loadCourses() }, 300)
}

const goToPage = (p) => {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  loadCourses()
}

const pageNumbers = computed(() => {
  const t = totalPages.value, c = page.value
  const pages = []
  const start = Math.max(1, c - 2), end = Math.min(t, c + 2)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

onMounted(() => {
  loadCourses()
  loadMyCourses()
  loadFavoriteIds()
})

</script>

<template>
  <div class="all-courses">
    <div class="tab-header">
      <h2 class="header-title">探索课程</h2>
      <span class="course-count">共 {{ total }} 门课程</span>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-input-wrap">
        <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" width="15" height="15">
          <circle cx="9" cy="9" r="6.5" stroke-width="1.6"/>
          <path d="M14 14l4 4" stroke-width="1.6" stroke-linecap="round"/>
        </svg>
        <input class="search-input" v-model="search" @input="onSearchInput" placeholder="搜索课程名称…" />
      </div>
      <div class="search-input-wrap">
        <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="15" height="15">
          <path d="M9 9a3.5 3.5 0 100-7 3.5 3.5 0 000 7zM2.5 16.5s-.5-5 6.5-5 6.5 5 6.5 5" stroke-width="1.6" stroke-linecap="round"/>
        </svg>
        <input class="search-input" v-model="teacherName" @input="onSearchInput" placeholder="按讲师筛选…" />
      </div>
    </div>

    <div class="course-grid">
      <div v-for="course in courses" :key="course.id" class="glass-card">

        <!-- 标题 + 收藏按钮 -->
        <div class="card-head">
          <h3 class="course-name">{{ course.name || '未命名课程' }}</h3>
          <button class="fav-btn" :class="{ active: isFavorited(course.id) }" @click="toggleFavorite(course.id)" title="收藏">
            <svg viewBox="0 0 20 20" width="16" height="16">
              <path v-if="isFavorited(course.id)"
                    d="M10 17.5l-1.4-1.27C3.92 11.98 1 9.36 1 6.14 1 3.52 3.08 1.5 5.75 1.5c1.51 0 2.96.7 3.9 1.8h.7c.94-1.1 2.39-1.8 3.9-1.8C16.92 1.5 19 3.52 19 6.14c0 3.22-2.92 5.84-7.6 10.1L10 17.5z"
                    fill="#ff3b30" stroke="none"/>
              <path v-else
                    d="M10 17.5l-1.4-1.27C3.92 11.98 1 9.36 1 6.14 1 3.52 3.08 1.5 5.75 1.5c1.51 0 2.96.7 3.9 1.8h.7c.94-1.1 2.39-1.8 3.9-1.8C16.92 1.5 19 3.52 19 6.14c0 3.22-2.92 5.84-7.6 10.1L10 17.5z"
                    fill="none" stroke="currentColor" stroke-width="1.6"/>
            </svg>
          </button>
        </div>

        <p class="course-desc">{{ course.description || '暂无课程描述' }}</p>

        <div class="meta-row">
          <span class="meta-item">
            <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="13" height="13">
              <path d="M9 9a3.5 3.5 0 100-7 3.5 3.5 0 000 7zM2.5 16.5s-.5-5 6.5-5 6.5 5 6.5 5" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
            {{ course.teacherName || '未知讲师' }}
          </span>
          <span class="meta-item">
            <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="13" height="13">
              <circle cx="9" cy="9" r="7.5" stroke-width="1.6"/>
              <path d="M9 5v4l2.5 2" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
            {{ course.duration || 0 }}h
          </span>
        </div>

        <div class="stats-inset">
          <div class="stat-item">
            <span class="stat-n">{{ course.studentsCount || 0 }}</span>
            <span class="stat-l">学员</span>
          </div>
          <div class="stat-sep"></div>
          <div class="stat-item">
            <span class="stat-n">{{ course.rating || '—' }}</span>
            <span class="stat-l">评分</span>
          </div>
          <div class="stat-sep"></div>
          <div class="stat-item">
            <span class="stat-n">{{ course.maxCheckInCount || 12 }}</span>
            <span class="stat-l">签到</span>
          </div>
        </div>

        <button v-if="!isEnrolled(course.id)" class="enroll-btn" @click="enrollCourse(course.id)">立即报名</button>
        <button v-else class="enrolled-btn" disabled>
          <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="14" height="14">
            <path d="M3.5 9.5l4 4 7-8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          已报名
        </button>
      </div>
    </div>

    <div v-if="courses.length === 0 && total === 0" class="empty-state">
      <div class="empty-icon-wrap">
        <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
          <rect x="6" y="6" width="36" height="36" rx="6" stroke-width="2"/>
          <path d="M16 18h16M16 28h10" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <p class="empty-title">{{ search || teacherName ? '没有找到匹配的课程' : '暂无课程' }}</p>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button class="pg-btn" :disabled="page <= 1" @click="goToPage(page - 1)">
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" width="12" height="12">
          <path d="M10 3L5 8l5 5" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <template v-if="pageNumbers[0] > 1">
        <button class="pg-btn" @click="goToPage(1)">1</button>
        <span v-if="pageNumbers[0] > 2" class="pg-dots">…</span>
      </template>
      <button v-for="p in pageNumbers" :key="p" class="pg-btn" :class="{ active: p === page }" @click="goToPage(p)">{{ p }}</button>
      <template v-if="pageNumbers[pageNumbers.length - 1] < totalPages">
        <span v-if="pageNumbers[pageNumbers.length - 1] < totalPages - 1" class="pg-dots">…</span>
        <button class="pg-btn" @click="goToPage(totalPages)">{{ totalPages }}</button>
      </template>
      <button class="pg-btn" :disabled="page >= totalPages" @click="goToPage(page + 1)">
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" width="12" height="12">
          <path d="M6 3l5 5-5 5" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.all-courses {
  width: 100%;
  padding: 8px;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* ── Header ── */
.tab-header { display: flex; align-items: baseline; gap: 14px; margin-bottom: 20px; }
.header-title { font-size: 26px; font-weight: 700; letter-spacing: -0.03em; color: #1d1d1f; }
.course-count { font-size: 14px; color: #86868b; }

/* ── Search ── */
.search-bar { display: flex; gap: 10px; margin-bottom: 20px; }
.search-input-wrap {
  flex: 1; display: flex; align-items: center; gap: 8px;
  padding: 10px 14px; border-radius: 12px;
  background: rgba(255,255,255,0.52); backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 1px 0 rgba(255,255,255,0.95) inset, 0 2px 8px rgba(0,0,0,0.06);
  color: #86868b; transition: box-shadow 0.15s;
}
.search-input-wrap:focus-within { box-shadow: 0 0 0 3px rgba(0,113,227,0.18), 0 1px 0 rgba(255,255,255,0.95) inset; }
.search-input {
  flex: 1; border: none; outline: none; background: none;
  font-size: 14px; font-family: inherit; color: #1d1d1f;
}
.search-input::placeholder { color: #aeaeb2; }

/* ── Grid ── */
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.glass-card {
  border-radius: 20px; padding: 22px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.10), 0 0.5px 0 rgba(255,255,255,0.95) inset;
  display: flex; flex-direction: column; gap: 14px;
  transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.22s ease;
}
.glass-card:hover { transform: translateY(-4px); box-shadow: 0 6px 40px rgba(0,0,0,0.13), 0 0.5px 0 rgba(255,255,255,0.95) inset; }

.card-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; }
.course-name { font-size: 17px; font-weight: 700; letter-spacing: -0.02em; color: #1d1d1f; line-height: 1.4; flex: 1; }

.fav-btn {
  flex-shrink: 0; width: 32px; height: 32px; border-radius: 50%; border: none;
  background: rgba(0,0,0,0.04); color: #86868b; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.15s; padding: 0;
}
.fav-btn:hover { background: rgba(255,59,48,0.1); color: #ff3b30; transform: scale(1.15); }
.fav-btn.active { color: #ff3b30; }

.course-desc {
  font-size: 13px; color: #6e6e73; line-height: 1.6;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; min-height: 42px;
}
.meta-row { display: flex; gap: 18px; }
.meta-item { display: inline-flex; align-items: center; gap: 5px; font-size: 12px; color: #86868b; font-weight: 500; }

.stats-inset {
  display: flex; align-items: center; border-radius: 12px; padding: 14px 0;
  background: rgba(0,0,0,0.04);
  box-shadow: 0 2px 4px rgba(0,0,0,0.07) inset, 0 1px 0 rgba(255,255,255,0.88);
  border: 0.5px solid rgba(0,0,0,0.06);
}
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 3px; }
.stat-n { font-size: 22px; font-weight: 700; letter-spacing: -0.04em; color: #1d1d1f; line-height: 1; }
.stat-l { font-size: 11px; color: #86868b; }
.stat-sep { width: 0.5px; height: 30px; background: rgba(0,0,0,0.08); }

.enroll-btn, .enrolled-btn {
  width: 100%; padding: 11px 20px; border-radius: 10px; font-size: 14px; font-weight: 600;
  cursor: pointer; border: none; outline: none; font-family: inherit;
  transition: all 0.15s; letter-spacing: -0.01em;
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.enroll-btn {
  background: rgba(29,29,31,0.88); backdrop-filter: blur(8px); color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.22), 0 0.5px 0 rgba(255,255,255,0.12) inset;
}
.enroll-btn:hover { background: rgba(0,0,0,0.95); transform: translateY(-1px); }
.enrolled-btn { background: rgba(0,0,0,0.05); color: #aeaeb2; cursor: not-allowed; border: 0.5px solid rgba(0,0,0,0.06); }

.empty-state { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 80px 20px; text-align: center; }
.empty-icon-wrap {
  width: 72px; height: 72px; border-radius: 20px;
  background: rgba(255,255,255,0.52); backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255,255,255,0.85); box-shadow: 0 2px 20px rgba(0,0,0,0.08);
  display: flex; align-items: center; justify-content: center; color: #c7c7cc; margin-bottom: 6px;
}
.empty-title { font-size: 17px; font-weight: 600; color: #1d1d1f; }

/* ── Pagination ── */
.pagination {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  margin-top: 28px; padding: 16px 0;
}
.pg-btn {
  min-width: 36px; height: 36px; border-radius: 10px; border: none;
  background: rgba(255,255,255,0.55); backdrop-filter: blur(12px);
  border: 0.5px solid rgba(255,255,255,0.8);
  box-shadow: 0 1px 0 rgba(255,255,255,0.95) inset, 0 2px 8px rgba(0,0,0,0.06);
  color: #1d1d1f; font-size: 14px; font-weight: 600; font-family: inherit;
  cursor: pointer; transition: all 0.15s;
  display: flex; align-items: center; justify-content: center;
}
.pg-btn:hover:not(:disabled):not(.active) { background: rgba(255,255,255,0.78); transform: translateY(-1px); }
.pg-btn.active {
  background: rgba(29,29,31,0.88); color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.22);
}
.pg-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.pg-dots { color: #86868b; font-size: 14px; padding: 0 2px; }

@media (max-width: 768px) {
  .course-grid { grid-template-columns: 1fr; gap: 12px; }
  .header-title { font-size: 22px; }
  .glass-card { padding: 18px 16px; }
  .search-bar { flex-direction: column; }
}
</style>
