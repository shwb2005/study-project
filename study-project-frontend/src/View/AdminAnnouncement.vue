<template>
  <div class="page">
    <div class="bg"></div>
    <div class="bg-dim" :style="{ backdropFilter: `blur(${scrollBlur}px)`, background: `rgba(240,246,252,${scrollOverlay})` }"></div>
    <header class="navbar">
      <button class="nav-btn" @click="router.push('/admin')">
        <svg width="9" height="16" viewBox="0 0 9 16" fill="none"><path d="M8 1L1 8L8 15" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>
        返回
      </button>
      <span class="navbar-title">公告管理</span>
      <span style="width:52px"></span>
    </header>
    <div class="all-courses">
      <div class="page-header">
        <div>
          <h2 class="page-title">公告管理</h2>
          <span class="page-sub">共 {{ total }} 条公告</span>
        </div>
        <button class="btn-add" @click="openAdd">
          <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" width="14" height="14"><path d="M8 3v10M3 8h10" stroke-width="2" stroke-linecap="round"/></svg>
          发布公告
        </button>
      </div>

      <div class="course-grid">
        <div v-for="a in list" :key="a.id" class="glass-card">
          <h3 class="course-name">{{ a.title }}</h3>
          <p class="course-desc">{{ a.content }}</p>
          <div class="meta-row">
            <span class="meta-item">
              <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="13" height="13"><circle cx="9" cy="9" r="7.5" stroke-width="1.6"/><path d="M9 5v4l2.5 2" stroke-width="1.6" stroke-linecap="round"/></svg>
              {{ formatDate(a.createdAt) }}
            </span>
          </div>
          <div class="card-footer">
            <button class="btn btn-edit" @click="openEdit(a)">编辑</button>
            <button class="btn btn-danger" @click="handleDelete(a.id)">删除</button>
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

      <el-dialog v-model="showDialog" :title="isEdit ? '编辑公告' : '发布公告'" width="500px" class="glass-dialog">
        <el-form label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" maxlength="2000" show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dlg-footer">
            <button class="dlg-btn dlg-cancel" @click="showDialog = false">取消</button>
            <button class="dlg-btn dlg-confirm" @click="handleSubmit" :disabled="!form.title || !form.content">确认</button>
          </div>
        </template>
      </el-dialog>
    </div>

    <!-- 删除确认弹窗 -->
    <ConfirmDeleteModal v-model="showDeleteModal" message="确定删除该公告？" @confirm="confirmDelete" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, post } from '@/net'
import { ElMessage } from 'element-plus'
import ConfirmDeleteModal from '@/components/common/ConfirmDeleteModal.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 9
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

const showDialog = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, title: '', content: '' })
const showDeleteModal = ref(false)
const deleteTargetId = ref(null)
const confirmDelete = () => {
  if (deleteTargetId.value !== null) {
    post('/api/announcement/admin/delete', { id: deleteTargetId.value }, () => {
      ElMessage.success('删除成功')
      loadList()
    }, () => { ElMessage.error('删除失败') })
    deleteTargetId.value = null
  }
}

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
const goToPage = (p) => { if (p >= 1 && p <= totalPages.value) { page.value = p; loadList() } }
const pageNumbers = computed(() => { const t = totalPages.value, c = page.value, pages = []; for (let i = Math.max(1, c - 2); i <= Math.min(t, c + 2); i++) pages.push(i); return pages })

const openAdd = () => { isEdit.value = false; form.value = { id: null, title: '', content: '' }; showDialog.value = true }
const openEdit = (a) => { isEdit.value = true; form.value = { id: a.id, title: a.title, content: a.content }; showDialog.value = true }

const handleSubmit = () => {
  const url = isEdit.value ? '/api/announcement/admin/update' : '/api/announcement/admin/add'
  post(url, { id: form.value.id, title: form.value.title, content: form.value.content }, () => {
    ElMessage.success(isEdit.value ? '修改成功' : '发布成功'); showDialog.value = false; loadList()
  }, () => { ElMessage.error('操作失败') })
}

const handleDelete = (id) => {
  deleteTargetId.value = id
  showDeleteModal.value = true
}

onMounted(() => { loadList(); window.addEventListener('scroll', handleScroll, { passive: true }) })
onUnmounted(() => { window.removeEventListener('scroll', handleScroll) })
</script>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.page { min-height: 100vh; position: relative; font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif; -webkit-font-smoothing: antialiased; color: #1d1d1f; }
.bg { position: fixed; inset: 0; z-index: 0; background-image: url('@/assets/images/4.jpg'); background-size: cover; background-position: center; }
.bg-dim { position: fixed; inset: 0; z-index: 1; transition: backdrop-filter 0.1s linear, -webkit-backdrop-filter 0.1s linear, background 0.1s linear; }

.navbar { position: sticky; top: 0; z-index: 100; height: 52px; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; background: rgba(255,255,255,0.65); backdrop-filter: saturate(200%) blur(40px); -webkit-backdrop-filter: saturate(200%) blur(40px); border-bottom: 0.5px solid rgba(255,255,255,0.65); box-shadow: 0 1px 0 rgba(0,0,0,0.05); }
.nav-btn { display: flex; align-items: center; gap: 5px; background: none; border: none; cursor: pointer; font-family: inherit; font-size: 15px; color: #0071e3; padding: 0; transition: opacity 0.1s; }
.nav-btn:hover { opacity: 0.7; }
.navbar-title { font-size: 16px; font-weight: 600; letter-spacing: -0.02em; }

.all-courses { position: relative; z-index: 2; max-width: 1200px; margin: 0 auto; padding: 28px 20px 72px; }

.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 28px; gap: 16px; }
.page-title { font-size: 26px; font-weight: 700; letter-spacing: -0.03em; color: #fff; }
.page-sub { font-size: 13px; color: #86868b; }

.btn-add { display: inline-flex; align-items: center; gap: 7px; background: rgba(29,29,31,0.88); backdrop-filter: blur(8px); color: #fff; border: none; border-radius: 980px; padding: 10px 20px; font-size: 14px; font-family: inherit; font-weight: 600; cursor: pointer; flex-shrink: 0; box-shadow: 0 2px 10px rgba(0,0,0,0.22), 0 0.5px 0 rgba(255,255,255,0.12) inset; transition: all 0.15s; }
.btn-add:hover { background: rgba(0,0,0,0.95); transform: translateY(-1px); }

.course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }

.glass-card { border-radius: 20px; padding: 22px; background: rgba(255,255,255,0.52); backdrop-filter: saturate(200%) blur(40px); -webkit-backdrop-filter: saturate(200%) blur(40px); border: 0.5px solid rgba(255,255,255,0.85); box-shadow: 0 2px 32px rgba(0,0,0,0.10), 0 0.5px 0 rgba(255,255,255,0.95) inset; display: flex; flex-direction: column; gap: 14px; transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.22s ease; }
.glass-card:hover { transform: translateY(-4px); box-shadow: 0 6px 40px rgba(0,0,0,0.13), 0 0.5px 0 rgba(255,255,255,0.95) inset; }

.course-name { font-size: 17px; font-weight: 700; letter-spacing: -0.02em; color: #1d1d1f; margin: 0; line-height: 1.4; }
.course-desc { font-size: 13px; color: #6e6e73; line-height: 1.6; margin: 0; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; min-height: 42px; }

.meta-row { display: flex; gap: 18px; }
.meta-item { display: inline-flex; align-items: center; gap: 5px; font-size: 12px; color: #86868b; font-weight: 500; }

.card-footer { display: flex; gap: 8px; }
.btn { flex: 1; padding: 10px 16px; border-radius: 10px; font-size: 13px; font-weight: 600; cursor: pointer; border: none; font-family: inherit; transition: all 0.15s cubic-bezier(0.4,0,0.2,1); }
.btn-edit { background: rgba(29,29,31,0.88); backdrop-filter: blur(8px); color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.2), 0 0.5px 0 rgba(255,255,255,0.12) inset; }
.btn-edit:hover { background: rgba(0,0,0,0.95); transform: translateY(-1px); }
.btn-danger { background: rgba(255,59,48,0.08); backdrop-filter: blur(8px); color: #ff3b30; border: 0.5px solid rgba(255,59,48,0.2); box-shadow: 0 1px 0 rgba(255,255,255,0.9) inset; }
.btn-danger:hover { background: rgba(255,59,48,0.15); transform: translateY(-1px); }

.empty-state { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 80px 20px; }
.empty-icon-wrap { width: 72px; height: 72px; border-radius: 20px; background: rgba(255,255,255,0.52); backdrop-filter: blur(20px); border: 0.5px solid rgba(255,255,255,0.85); box-shadow: 0 2px 20px rgba(0,0,0,0.08); display: flex; align-items: center; justify-content: center; color: #c7c7cc; margin-bottom: 6px; }
.empty-title { font-size: 17px; font-weight: 600; color: #1d1d1f; }

.pagination { display: flex; align-items: center; justify-content: center; gap: 6px; margin-top: 28px; padding: 16px 0; }
.pg-btn { min-width: 36px; height: 36px; border-radius: 10px; border: none; background: rgba(255,255,255,0.55); backdrop-filter: blur(12px); border: 0.5px solid rgba(255,255,255,0.8); box-shadow: 0 1px 0 rgba(255,255,255,0.95) inset, 0 2px 8px rgba(0,0,0,0.06); color: #1d1d1f; font-size: 14px; font-weight: 600; font-family: inherit; cursor: pointer; transition: all 0.15s; display: flex; align-items: center; justify-content: center; }
.pg-btn:hover:not(:disabled):not(.active) { background: rgba(255,255,255,0.78); transform: translateY(-1px); }
.pg-btn.active { background: rgba(29,29,31,0.88); color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.22); }
.pg-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.pg-dots { color: #86868b; font-size: 14px; padding: 0 2px; }

:deep(.glass-dialog .el-dialog) { border-radius: 20px; background: rgba(255,255,255,0.78); backdrop-filter: saturate(180%) blur(40px); -webkit-backdrop-filter: saturate(180%) blur(40px); border: 0.5px solid rgba(255,255,255,0.85); box-shadow: 0 24px 60px rgba(0,0,0,0.16); }
:deep(.glass-dialog .el-dialog__header) { padding: 22px 24px 16px; border-bottom: 0.5px solid rgba(0,0,0,0.07); }
:deep(.glass-dialog .el-dialog__title) { font-size: 17px; font-weight: 700; color: #1d1d1f; }
:deep(.glass-dialog .el-dialog__body) { padding: 20px 24px; }
:deep(.glass-dialog .el-form-item__label) { font-size: 13px; font-weight: 500; color: #3a3a3c; }
:deep(.glass-dialog .el-input__wrapper), :deep(.glass-dialog .el-textarea__inner) { background: rgba(255,255,255,0.55) !important; backdrop-filter: blur(10px); border: 0.5px solid rgba(255,255,255,0.8) !important; border-radius: 10px !important; box-shadow: none !important; font-family: inherit; }
:deep(.glass-dialog .el-input__wrapper.is-focus), :deep(.glass-dialog .el-textarea__inner:focus) { background: rgba(255,255,255,0.75) !important; border-color: rgba(0,113,227,0.4) !important; box-shadow: 0 0 0 3px rgba(0,113,227,0.12) !important; }

.dlg-footer { display: flex; gap: 10px; justify-content: flex-end; }
.dlg-btn { display: inline-flex; align-items: center; gap: 7px; padding: 10px 22px; border-radius: 980px; font-size: 14px; font-weight: 600; cursor: pointer; border: none; font-family: inherit; transition: all 0.15s; }
.dlg-cancel { background: rgba(0,0,0,0.06); color: #1d1d1f; }
.dlg-cancel:hover { background: rgba(0,0,0,0.10); }
.dlg-confirm { background: rgba(29,29,31,0.88); backdrop-filter: blur(8px); color: #fff; box-shadow: 0 2px 10px rgba(0,0,0,0.2); }
.dlg-confirm:hover:not(:disabled) { background: rgba(0,0,0,0.95); }
.dlg-confirm:disabled { opacity: 0.4; cursor: not-allowed; }

@media (max-width: 768px) { .course-grid { grid-template-columns: 1fr; } .page-title { font-size: 22px; } .glass-card { padding: 18px 16px; } .card-footer { flex-direction: column; } .page-header { flex-wrap: wrap; } }
</style>
