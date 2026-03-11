<template>
  <div class="page">
    <div class="bg"></div>
    <div class="bg-dim"
         :style="{
         backdropFilter: `blur(${scrollBlur}px) saturate(140%)`,
         WebkitBackdropFilter: `blur(${scrollBlur}px) saturate(140%)`,
         background: `rgba(240,246,252,${scrollOverlay})`
       }">
    </div>
    <header class="navbar">
      <button class="nav-btn" @click="goToHome">
        <svg width="9" height="16" viewBox="0 0 9 16" fill="none">
          <path d="M8 1L1 8L8 15" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回
      </button>
      <span class="navbar-title" >管理员管理</span>
      <span style="width:52px"></span>
    </header>
    <div class="all-courses">
      <div class="page-header">
        <div>
          <h2 class="page-title">管理员管理</h2>
          <span class="page-sub">{{ adminList.length }} 个管理员</span>
        </div>
        <button class="btn-add" @click="showAddDialog = true">
          <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" width="14" height="14">
            <path d="M8 3v10M3 8h10" stroke-width="2" stroke-linecap="round"/>
          </svg>
          添加管理员
        </button>
      </div>

      <!-- 管理员列表 -->
      <div class="course-grid">
        <div v-for="admin in adminList" :key="admin.id" class="glass-card">

          <div class="admin-head">
            <div class="admin-avatar">
              {{ admin.username ? admin.username.charAt(0).toUpperCase() : 'A' }}
            </div>
            <div class="admin-info">
              <h3 class="course-name">{{ admin.username || '未命名管理员' }}</h3>
              <span v-if="admin.id === currentAdminId" class="self-badge">当前账号</span>
            </div>
          </div>

          <div class="stats-inset">
            <div class="stat-item">
              <span class="stat-n">{{ admin.id }}</span>
              <span class="stat-l">ID</span>
            </div>
            <div class="stat-sep"></div>
            <div class="stat-item">
              <span class="stat-n">{{ admin.username ? admin.username.substring(0, 2) + '…' : 'N/A' }}</span>
              <span class="stat-l">用户名</span>
            </div>
            <div class="stat-sep"></div>

          </div>

          <div class="card-footer">
            <button v-if="admin.id !== currentAdminId" class="btn-danger-full" @click="handleDelete(admin)">
              <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" width="13" height="13">
                <path d="M14 6L6 14M6 6l8 8" stroke-width="2" stroke-linecap="round"/>
              </svg>
              删除管理员
            </button>
            <div v-else class="self-indicator">
              <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="14" height="14">
                <path d="M3.5 9.5l4 4 7-8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              当前登录账号
            </div>
          </div>
        </div>
      </div>

      <div v-if="adminList.length === 0" class="empty-state">
        <div class="empty-icon-wrap">
          <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
            <path d="M24 24a8 8 0 100-16 8 8 0 000 16zM8 42s-1-10 16-10 16 10 16 10" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="empty-title">暂无管理员</p>
      </div>

      <!-- 添加管理员弹窗 -->
      <el-dialog v-model="showAddDialog" title="添加管理员" width="500px"
                 :before-close="handleCloseDialog" class="glass-dialog">
        <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="addForm.username" placeholder="请输入用户名" maxlength="20" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="addForm.password" type="password" placeholder="请输入密码" show-password maxlength="20" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dlg-footer">
            <button class="dlg-btn dlg-cancel" @click="handleCloseDialog">取消</button>
            <button class="dlg-btn dlg-confirm" @click="handleAddAdmin" :disabled="adding">
              <span v-if="adding" class="spinner"></span>
              {{ adding ? '添加中…' : '确认添加' }}
            </button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from '@/stores/index.js'
import { get, post } from '@/net/index.js'

const router = useRouter()
const store = useStore()

// 状态管理
const scrollBlur = ref(0)
const scrollOverlay = ref(0)
const handleScroll = () => {
  const progress = Math.min(window.scrollY / 380, 1)
  scrollBlur.value = progress * 48
  scrollOverlay.value = progress * 0.52
}

const loading = ref(false)
const adding = ref(false)
const showAddDialog = ref(false)
const adminList = ref([])
const currentAdminId = ref(null)

// 表单引用
const addFormRef = ref()

// 表单数据
const addForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const addRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (dateString) => {
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  } catch (e) {
    return 'N/A'
  }
}

// 返回首页
const goToHome = () => {
  console.log('🏠 返回首页')
  router.push('/admin')
}

// 检查登录状态
const checkAdminLogin = () => {
  return new Promise((resolve) => {
    if (store.auth.admin) {
      console.log('Store 中已有管理员登录状态')
      resolve(true)
    } else {
      console.log('Store 中无管理员登录状态')
      resolve(false)
    }
  })
}

// 加载管理员列表
const loadAdminList = () => {
  loading.value = true
  get('/api/admin/list',
      (message) => {
        console.log('获取管理员列表成功:', message)
        if (message && message.data) {
          adminList.value = message.data
        } else if (Array.isArray(message)) {
          adminList.value = message
        } else {
          adminList.value = []
        }
        loading.value = false
      },
      (message) => {
        ElMessage.error(message || '加载管理员列表失败')
        adminList.value = []
        loading.value = false
      }
  )
}

// 添加管理员
const handleAddAdmin = () => {
  if (!addFormRef.value) return

  addFormRef.value.validate((valid) => {
    if (valid) {
      adding.value = true

      console.log('🔄 开始添加管理员:', {
        username: addForm.username,
        password: addForm.password
      })

      post('/api/admin/add', {
            username: addForm.username,
            password: addForm.password
          },
          (message) => {
            console.log('✅ 添加管理员成功:', message)
            ElMessage.success(message)
            handleCloseDialog()
            loadAdminList()
            adding.value = false
          },
          (message) => {
            console.error('❌ 添加管理员失败:', message)
            ElMessage.error(message || '添加失败')
            adding.value = false
          }
      )
    } else {
      console.log('❌ 表单验证失败')
    }
  })
}

// 删除管理员
const handleDelete = (admin) => {
  ElMessageBox.confirm(
      `确定要删除管理员 "${admin.username}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    post('/api/admin/delete', { id: admin.id },
        (message) => {
          ElMessage.success(message || '管理员删除成功')
          loadAdminList()
        },
        (message) => {
          ElMessage.error(message || '删除失败')
        }
    )
  }).catch(() => {
    // 用户取消
  })
}

// 关闭弹窗
const handleCloseDialog = () => {
  showAddDialog.value = false
  if (addFormRef.value) {
    addFormRef.value.resetFields()
  } else {
    addForm.username = ''
    addForm.password = ''
  }
}

// 获取当前登录管理员信息
const getCurrentAdmin = () => {
  get('/api/admin/me',
      (message) => {
        if (message && (message.id || message.data?.id)) {
          currentAdminId.value = message.id || message.data.id
          console.log('当前登录管理员ID:', currentAdminId.value)
        }
      },
      (message) => {
        console.log('获取当前管理员信息失败:', message)
      }
  )
}

// 生命周期
onMounted(async () => {
  const isLoggedIn = await checkAdminLogin()
  if (!isLoggedIn) {
    ElMessage.warning('请先登录管理员账户')
    router.push('/admin-login')
    return
  }

  loadAdminList()
  getCurrentAdmin()
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.page {
  min-height: 100vh;
  position: relative;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  color: #1d1d1f;
}
.bg {
  position: fixed; inset: 0; z-index: 0;
  background-image: url('@/assets/images/2.jpeg');
  background-size: cover; background-position: center;
}
.bg-dim {
  position: fixed; inset: 0; z-index: 1;
  transition: backdrop-filter 0.1s linear, -webkit-backdrop-filter 0.1s linear, background 0.1s linear;
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
  color: #0071e3; padding: 0; transition: opacity 0.1s;
}
.nav-btn:hover { opacity: 0.7; }
.navbar-title { font-size: 16px; font-weight: 600; letter-spacing: -0.02em; }

.all-courses {
  position: relative; z-index: 2;
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 20px 72px;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  color: #1d1d1f;
}

/* ── Header ── */
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 28px; gap: 16px;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.nav-back {
  display: flex; align-items: center; gap: 5px;
  background: none; border: none; cursor: pointer;
  font-family: inherit; font-size: 15px;
  color: #0071e3; padding: 0; transition: opacity 0.1s;
}
.nav-back:hover { opacity: 0.7; }
.page-title { font-size: 26px; font-weight: 700; letter-spacing: -0.03em; color: #ffffff; line-height: 1.2; }
.page-sub { font-size: 13px; color: #86868b; }

.btn-add {
  display: inline-flex; align-items: center; gap: 7px;
  background: rgba(29,29,31,0.88);
  backdrop-filter: blur(8px);
  color: #fff; border: none;
  border-radius: 980px; padding: 10px 20px;
  font-size: 14px; font-family: inherit; font-weight: 600;
  cursor: pointer; flex-shrink: 0;
  box-shadow: 0 2px 10px rgba(0,0,0,0.22), 0 0.5px 0 rgba(255,255,255,0.12) inset;
  transition: all 0.15s;
}
.btn-add:hover { background: rgba(0,0,0,0.95); transform: translateY(-1px); }

/* ── Grid ── */
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

/* ── Glass card ── */
.glass-card {
  border-radius: 20px;
  padding: 22px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.10), 0 0.5px 0 rgba(255,255,255,0.95) inset;
  display: flex; flex-direction: column; gap: 14px;
  transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.22s ease;
}
.glass-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 40px rgba(0,0,0,0.13), 0 0.5px 0 rgba(255,255,255,0.95) inset;
}

/* ── Admin head ── */
.admin-head { display: flex; align-items: center; gap: 14px; }
.admin-avatar {
  width: 46px; height: 46px; border-radius: 14px; flex-shrink: 0;
  background: rgba(29,29,31,0.82);
  backdrop-filter: blur(8px);
  display: flex; align-items: center; justify-content: center;
  color: rgba(255,255,255,0.9);
  font-size: 18px; font-weight: 700;
  box-shadow: 0 4px 16px rgba(0,0,0,0.18), 0 0.5px 0 rgba(255,255,255,0.1) inset;
}
.admin-info { display: flex; flex-direction: column; gap: 4px; }
.course-name { font-size: 17px; font-weight: 700; letter-spacing: -0.02em; color: #1d1d1f; margin: 0; }
.self-badge {
  display: inline-block; padding: 2px 8px;
  background: rgba(0,113,227,0.1); color: #0071e3;
  border-radius: 5px; font-size: 11px; font-weight: 600;
}

/* ── 凹陷统计块 ── */
.stats-inset {
  display: flex; align-items: center;
  border-radius: 12px; padding: 14px 0;
  background: rgba(0,0,0,0.04);
  box-shadow: 0 2px 4px rgba(0,0,0,0.07) inset, 0 1px 0 rgba(255,255,255,0.88);
  border: 0.5px solid rgba(0,0,0,0.06);
}
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 3px; }
.stat-n { font-size: 16px; font-weight: 700; letter-spacing: -0.03em; color: #0071e3; line-height: 1; }
.stat-l { font-size: 11px; color: #86868b; }
.stat-sep { width: 0.5px; height: 28px; background: rgba(0,0,0,0.08); }

/* ── Footer buttons ── */
.card-footer { }
.btn-danger-full {
  display: flex; align-items: center; justify-content: center; gap: 7px;
  width: 100%; padding: 11px;
  background: rgba(255,59,48,0.08);
  backdrop-filter: blur(8px); color: #ff3b30;
  border: 0.5px solid rgba(255,59,48,0.2);
  border-radius: 10px;
  font-size: 13px; font-weight: 600; cursor: pointer;
  font-family: inherit; transition: all 0.15s;
  box-shadow: 0 1px 0 rgba(255,255,255,0.9) inset;
}
.btn-danger-full:hover { background: rgba(255,59,48,0.15); transform: translateY(-1px); }

.self-indicator {
  display: flex; align-items: center; justify-content: center; gap: 7px;
  padding: 11px; border-radius: 10px;
  font-size: 13px; font-weight: 500; color: #aeaeb2;
  background: rgba(0,0,0,0.04);
  border: 0.5px solid rgba(0,0,0,0.06);
}

/* ── Empty ── */
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 80px 20px; }
.empty-icon-wrap {
  width: 72px; height: 72px; border-radius: 20px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 20px rgba(0,0,0,0.08);
  display: flex; align-items: center; justify-content: center;
  color: #c7c7cc; margin-bottom: 6px;
}
.empty-title { font-size: 17px; font-weight: 600; color: #1d1d1f; }

/* ── Dialog ── */
:deep(.glass-dialog .el-dialog) {
  border-radius: 20px;
  background: rgba(255,255,255,0.78);
  backdrop-filter: saturate(180%) blur(40px);
  -webkit-backdrop-filter: saturate(180%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 24px 60px rgba(0,0,0,0.16);
}
:deep(.glass-dialog .el-dialog__header) { padding: 22px 24px 16px; border-bottom: 0.5px solid rgba(0,0,0,0.07); }
:deep(.glass-dialog .el-dialog__title) { font-size: 17px; font-weight: 700; color: #1d1d1f; letter-spacing: -0.02em; }
:deep(.glass-dialog .el-dialog__body) { padding: 20px 24px; }
:deep(.glass-dialog .el-dialog__footer) { padding: 0 24px 22px; border-top: none; }
:deep(.glass-dialog .el-form-item__label) { font-size: 13px; font-weight: 500; color: #3a3a3c; }

:deep(.glass-dialog .el-input__wrapper) {
  background: rgba(255,255,255,0.55) !important;
  backdrop-filter: blur(10px);
  border: 0.5px solid rgba(255,255,255,0.8) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
}
:deep(.glass-dialog .el-input__wrapper.is-focus) {
  background: rgba(255,255,255,0.75) !important;
  border-color: rgba(0,113,227,0.4) !important;
  box-shadow: 0 0 0 3px rgba(0,113,227,0.12) !important;
}

.dlg-footer { display: flex; gap: 10px; justify-content: flex-end; }
.dlg-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 10px 22px; border-radius: 980px;
  font-size: 14px; font-weight: 600; cursor: pointer;
  border: none; font-family: inherit; transition: all 0.15s;
}
.dlg-cancel { background: rgba(0,0,0,0.06); color: #1d1d1f; }
.dlg-cancel:hover { background: rgba(0,0,0,0.10); }
.dlg-confirm { background: rgba(29,29,31,0.88); backdrop-filter: blur(8px); color: #fff; box-shadow: 0 2px 10px rgba(0,0,0,0.2); }
.dlg-confirm:hover:not(:disabled) { background: rgba(0,0,0,0.95); }
.dlg-confirm:disabled { opacity: 0.4; cursor: not-allowed; }

.spinner {
  width: 12px; height: 12px; flex-shrink: 0;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: #fff; border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .course-grid { grid-template-columns: 1fr; }
  .page-title { font-size: 22px; }
  .glass-card { padding: 18px 16px; }
  .page-header { flex-wrap: wrap; }
}
</style>