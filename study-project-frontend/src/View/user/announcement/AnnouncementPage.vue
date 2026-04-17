<template>
  <div class="page">
    <div class="bg"></div>
    <div class="bg-dim" :style="{ backdropFilter: `blur(${scrollBlur}px)`, background: `rgba(240,246,252,${scrollOverlay})` }"></div>
    <header class="navbar">
      <div class="navbar-center">
        <ModuleTab />
      </div>
    </header>

    <div class="all-courses">
      <div class="page-header">
        <div>
          <h2 class="page-title">通知公告</h2>
          <span class="page-sub">共 {{ total }} 条公告</span>
        </div>
      </div>

      <div class="announcement-list">
        <div v-for="a in list" :key="a.id" class="glass-card" @click="openDetail(a)">
          <div class="card-dot"></div>
          <div class="card-body">
            <h3 class="card-title">{{ a.title }}</h3>
            <p class="card-content">{{ a.content }}</p>
            <div class="meta-row">
              <span class="meta-item">
                <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="13" height="13"><circle cx="9" cy="9" r="7.5" stroke-width="1.6"/><path d="M9 5v4l2.5 2" stroke-width="1.6" stroke-linecap="round"/></svg>
                {{ formatDate(a.createdAt) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="list.length === 0" class="empty-state">
        <div class="empty-icon-wrap">
          <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
            <path d="M24 8v28M12 16v20M36 12v24" stroke-width="2" stroke-linecap="round"/>
            <rect x="6" y="38" width="36" height="4" rx="2" stroke-width="2"/>
          </svg>
        </div>
        <p class="empty-title">暂无公告</p>
      </div>

      <div class="pagination" v-if="totalPages > 1">
        <button class="pg-btn" :disabled="page <= 1" @click="goToPage(page - 1)">
          <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" width="12" height="12"><path d="M10 3L5 8l5 5" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
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
          <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" width="12" height="12"><path d="M6 3l5 5-5 5" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </button>
      </div>

      <!-- 详情弹窗 -->
      <div class="notice-overlay" v-if="showDetail">
        <div class="brutalist-card">
          <div class="brutalist-card__header">
            <div class="brutalist-card__icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
            </div>
            <div class="brutalist-card__alert">公告详情</div>
            <button class="close-btn" @click="showDetail = false">✕</button>
          </div>
          <div class="brutalist-card__message">
            <div style="font-size:1rem;font-weight:900;margin-bottom:8px;">{{ detailData.title }}</div>
            <div style="color:#444;line-height:1.7;">{{ detailData.content }}</div>
            <div style="font-size:0.75rem;color:#888;margin-top:12px;">{{ formatDateTime(detailData.createdAt) }}</div>
          </div>
          <div class="brutalist-card__actions">
            <button class="brutalist-card__button brutalist-card__button--read" @click="showDetail = false">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/net'
import { useStore } from '@/stores'
import ModuleTab from '@/components/ModuleTab.vue'

const router = useRouter()
const store = useStore()
const lastSeenKey = 'announcement_last_seen_' + (store.auth.user?.id || 'anon')

const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 9
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

const showDetail = ref(false)
const detailData = ref({ title: '', content: '', createdAt: '' })

const scrollBlur = ref(0)
const scrollOverlay = ref(0)
const handleScroll = () => {
  const p = Math.min(window.scrollY / 380, 1)
  scrollBlur.value = p * 48
  scrollOverlay.value = p * 0.52
}

const loadList = () => {
  get('/api/announcement/list?page=' + page.value + '&pageSize=' + pageSize, data => {
    list.value = data?.list || []
    total.value = data?.total || 0
  }, () => { list.value = []; total.value = 0 })
}

const formatDate = (s) => { if (!s) return ''; try { return new Date(s).toLocaleDateString('zh-CN') } catch { return s } }
const formatDateTime = (s) => { if (!s) return ''; try { return new Date(s).toLocaleString('zh-CN') } catch { return s } }
const goToPage = (p) => { if (p >= 1 && p <= totalPages.value) { page.value = p; loadList() } }
const pageNumbers = computed(() => { const t = totalPages.value, c = page.value, pages = []; for (let i = Math.max(1, c - 2); i <= Math.min(t, c + 2); i++) pages.push(i); return pages })

const openDetail = (a) => {
  detailData.value = { title: a.title, content: a.content, createdAt: a.createdAt }
  showDetail.value = true
  // 标记已读
  localStorage.setItem(lastSeenKey, new Date().toISOString())
}

onMounted(() => { loadList(); window.addEventListener('scroll', handleScroll, { passive: true }) })
onUnmounted(() => { window.removeEventListener('scroll', handleScroll) })
</script>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.page { min-height: 100vh; position: relative; font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif; -webkit-font-smoothing: antialiased; color: #1d1d1f; }
.bg { position: fixed; inset: 0; z-index: 0; background-image: url('@/assets/images/4.jpg'); background-size: cover; background-position: center; }
.bg-dim { position: fixed; inset: 0; z-index: 1; transition: backdrop-filter 0.1s linear, -webkit-backdrop-filter 0.1s linear, background 0.1s linear; }

.navbar { position: sticky; top: 0; z-index: 100; height: 52px; display: flex; align-items: center; justify-content: center; padding: 0 20px; background: rgba(255,255,255,0.65); backdrop-filter: saturate(200%) blur(40px); -webkit-backdrop-filter: saturate(200%) blur(40px); border-bottom: 0.5px solid rgba(255,255,255,0.65); box-shadow: 0 1px 0 rgba(0,0,0,0.05); }
.nav-btn { display: flex; align-items: center; gap: 5px; background: none; border: none; cursor: pointer; font-family: inherit; font-size: 15px; color: #0071e3; padding: 0; transition: opacity 0.1s; }
.nav-btn:hover { opacity: 0.7; }
.navbar-title { font-size: 16px; font-weight: 600; letter-spacing: -0.02em; }
.navbar-center { display: flex; align-items: center; justify-content: center; }

.all-courses { position: relative; z-index: 2; max-width: 800px; margin: 0 auto; padding: 28px 20px 72px; }

.page-header { margin-bottom: 28px; }
.page-title { font-size: 26px; font-weight: 700; letter-spacing: -0.03em; color: #1d1d1f; }
.page-sub { font-size: 13px; color: #86868b; }

.announcement-list { display: flex; flex-direction: column; gap: 14px; }

.glass-card {
  border-radius: 20px; padding: 22px 24px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: saturate(200%) blur(40px); -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.10), 0 0.5px 0 rgba(255,255,255,0.95) inset;
  display: flex; gap: 16px; cursor: pointer;
  transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.22s ease;
}
.glass-card:hover { transform: translateY(-3px); box-shadow: 0 6px 40px rgba(0,0,0,0.13), 0 0.5px 0 rgba(255,255,255,0.95) inset; }

.card-dot {
  flex-shrink: 0; width: 8px; height: 8px; border-radius: 50%;
  background: #0071e3; margin-top: 8px;
  box-shadow: 0 0 0 3px rgba(0,113,227,0.15);
}

.card-body { flex: 1; min-width: 0; }

.card-title { font-size: 17px; font-weight: 700; letter-spacing: -0.02em; color: #1d1d1f; margin: 0 0 8px; line-height: 1.4; }
.card-content {
  font-size: 14px; color: #6e6e73; line-height: 1.6; margin: 0 0 12px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

.meta-row { display: flex; gap: 18px; }
.meta-item { display: inline-flex; align-items: center; gap: 5px; font-size: 12px; color: #86868b; font-weight: 500; }

.empty-state { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 80px 20px; }
.empty-icon-wrap { width: 72px; height: 72px; border-radius: 20px; background: rgba(255,255,255,0.52); backdrop-filter: blur(20px); border: 0.5px solid rgba(255,255,255,0.85); box-shadow: 0 2px 20px rgba(0,0,0,0.08); display: flex; align-items: center; justify-content: center; color: #c7c7cc; margin-bottom: 6px; }
.empty-title { font-size: 17px; font-weight: 600; color: #1d1d1f; }

.pagination { display: flex; align-items: center; justify-content: center; gap: 6px; margin-top: 28px; padding: 16px 0; }
.pg-btn { min-width: 36px; height: 36px; border-radius: 10px; border: none; background: rgba(255,255,255,0.55); backdrop-filter: blur(12px); border: 0.5px solid rgba(255,255,255,0.8); box-shadow: 0 1px 0 rgba(255,255,255,0.95) inset, 0 2px 8px rgba(0,0,0,0.06); color: #1d1d1f; font-size: 14px; font-weight: 600; font-family: inherit; cursor: pointer; transition: all 0.15s; display: flex; align-items: center; justify-content: center; }
.pg-btn:hover:not(:disabled):not(.active) { background: rgba(255,255,255,0.78); transform: translateY(-1px); }
.pg-btn.active { background: rgba(29,29,31,0.88); color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.22); }
.pg-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.pg-dots { color: #86868b; font-size: 14px; padding: 0 2px; }

/* 详情弹窗 */
:deep(.glass-dialog .el-dialog) { border-radius: 20px; background: rgba(255,255,255,0.78); backdrop-filter: saturate(180%) blur(40px); -webkit-backdrop-filter: saturate(180%) blur(40px); border: 0.5px solid rgba(255,255,255,0.85); box-shadow: 0 24px 60px rgba(0,0,0,0.16); }
:deep(.glass-dialog .el-dialog__header) { padding: 22px 24px 16px; border-bottom: 0.5px solid rgba(0,0,0,0.07); }
:deep(.glass-dialog .el-dialog__title) { font-size: 17px; font-weight: 700; color: #1d1d1f; }
:deep(.glass-dialog .el-dialog__body) { padding: 20px 24px; }

.detail-content { }
.detail-text { font-size: 15px; color: #1d1d1f; line-height: 1.8; white-space: pre-wrap; margin: 0 0 16px; }
.detail-time { font-size: 13px; color: #86868b; }

@media (max-width: 768px) {
  .all-courses { padding: 16px 14px 52px; }
  .page-title { font-size: 22px; }
  .glass-card { padding: 18px 16px; }
}

/* Brutalist 弹窗样式 */
.notice-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
}

.brutalist-card {
  width: 420px;
  max-height: 80vh;
  overflow-y: auto;
  border: 4px solid #000;
  background-color: #fff;
  padding: 1.5rem;
  box-shadow: 10px 10px 0 #000;
  font-family: "Arial", sans-serif;
}

.brutalist-card__header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  border-bottom: 2px solid #000;
  padding-bottom: 1rem;
}

.brutalist-card__icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #000;
  padding: 0.5rem;
}

.brutalist-card__icon svg { height: 1.5rem; width: 1.5rem; fill: #fff; }

.brutalist-card__alert {
  flex: 1;
  font-weight: 900;
  color: #000;
  font-size: 1.2rem;
  text-transform: uppercase;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 4px 8px;
  color: #666;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #000;
}

.brutalist-card__message {
  margin-top: 1rem;
  color: #000;
  font-size: 0.9rem;
  line-height: 1.4;
  border-bottom: 2px solid #000;
  padding-bottom: 1rem;
  font-weight: 600;
}

.brutalist-card__actions {
  margin-top: 1rem;
}

.brutalist-card__button {
  display: block;
  width: 100%;
  padding: 0.75rem;
  text-align: center;
  font-size: 1rem;
  font-weight: 700;
  text-transform: uppercase;
  border: 3px solid #000;
  background-color: #fff;
  color: #000;
  position: relative;
  transition: all 0.2s ease;
  box-shadow: 5px 5px 0 #000;
  overflow: hidden;
  text-decoration: none;
  margin-bottom: 1rem;
  cursor: pointer;
  font-family: inherit;
}

.brutalist-card__button--read {
  background-color: #000;
  color: #fff;
}

.brutalist-card__button::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(120deg, transparent, rgba(255,255,255,0.3), transparent);
  transition: all 0.6s;
}

.brutalist-card__button:hover::before {
  left: 100%;
}

.brutalist-card__button:hover {
  transform: translate(-2px, -2px);
  box-shadow: 7px 7px 0 #000;
}

.brutalist-card__button--read:hover {
  background-color: #D4A574;
  border-color: #D4A574;
  color: #fff;
  box-shadow: 7px 7px 0 #8B6B4A;
}

.brutalist-card__button:active {
  transform: translate(5px, 5px);
  box-shadow: none;
}
</style>
