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
      <span class="navbar-title">课程管理</span>
      <span style="width:52px"></span>
    </header>
    <div class="all-courses">
      <div class="page-header">
        <div>
          <h2 class="page-title">课程管理</h2>
          <span class="page-sub">共 {{ total }} 门课程</span>
        </div>
        <button class="btn-add" @click="showAddDialog = true">
          <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" width="14" height="14">
            <path d="M8 3v10M3 8h10" stroke-width="2" stroke-linecap="round"/>
          </svg>
          添加课程
        </button>
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
      </div>

      <!-- 课程列表 -->
      <div class="course-grid">
        <div v-for="course in courseList" :key="course.id" class="glass-card">

          <h3 class="course-name">{{ course.name || '未命名课程' }}</h3>
          <p class="course-desc">{{ course.description || '暂无课程描述' }}</p>

          <div class="meta-row">
          <span class="meta-item">
            <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="13" height="13">
              <path d="M9 9a3.5 3.5 0 100-7 3.5 3.5 0 000 7zM2.5 16.5s-.5-5 6.5-5 6.5 5 6.5 5" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
            {{ course.teacherName || '未知教师' }}
          </span>
            <span class="meta-item">
            <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="13" height="13">
              <circle cx="9" cy="9" r="7.5" stroke-width="1.6"/>
              <path d="M9 5v4l2.5 2" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
            {{ course.duration || '未知时长' }}
          </span>
          </div>

          <div class="stats-inset">
            <div class="stat-item">
              <span class="stat-n">{{ course.id }}</span>
              <span class="stat-l">ID</span>
            </div>
            <div class="stat-sep"></div>
            <div class="stat-item">
              <span class="stat-n">{{ course.studentsCount || 0 }}</span>
              <span class="stat-l">学生数</span>
            </div>
            <div class="stat-sep"></div>
            <div class="stat-item">
              <span class="stat-n">{{ course.maxCheckInCount || 0 }}</span>
              <span class="stat-l">最大签到</span>
            </div>
          </div>

          <div class="card-footer">
            <button class="btn btn-edit" @click="handleEdit(course)">编辑</button>
            <button class="btn btn-danger" @click="handleDelete(course)">删除</button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
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

      <div v-if="courseList.length === 0" class="empty-state">
        <div class="empty-icon-wrap">
          <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
            <path d="M24 6.253v36m0-36C22.332 5.477 20.246 5 18 5S13.668 5.477 12 6.253v36C13.668 41.477 15.754 41 18 41s4.332.477 6 1.253m0-36C25.668 5.477 27.754 5 30 5c2.747 0 4.832.477 6 1.253v36C34.832 41.477 32.747 41 30 41c-2.246 0-4.332.477-6 1.253" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <p class="empty-title">暂无课程</p>
      </div>

      <!-- 添加课程弹窗 -->
      <el-dialog v-model="showAddDialog" title="添加课程" width="500px"
                 :before-close="handleCloseDialog" class="glass-dialog">
        <el-form :model="addForm" :rules="formRules" ref="addFormRef" label-width="90px">
          <el-form-item label="课程名称" prop="name">
            <el-input v-model="addForm.name" placeholder="请输入课程名称" maxlength="50" />
          </el-form-item>
          <el-form-item label="教师姓名" prop="teacherName">
            <el-input v-model="addForm.teacherName" placeholder="请输入教师姓名" maxlength="20" />
          </el-form-item>
          <el-form-item label="课程描述" prop="description">
            <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="请输入课程描述" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="课程时长" prop="duration">
            <el-input v-model="addForm.duration" placeholder="例如：32课时、一学期等" maxlength="20" />
          </el-form-item>
          <el-form-item label="最大签到次数" prop="maxCheckInCount">
            <el-input-number v-model="addForm.maxCheckInCount" :min="1" :max="100" controls-position="right" class="w-100" />
          </el-form-item>
          <el-form-item label="封面图URL">
            <el-input v-model="addForm.coverImage" placeholder="输入封面图片链接" maxlength="500" />
          </el-form-item>
          <el-form-item label="视频URL">
            <el-input v-model="addForm.videoUrl" placeholder="输入视频链接（支持B站/YouTube嵌入链接）" maxlength="500" />
          </el-form-item>
          <div class="detail-divider">
            <span class="detail-divider-text">课程详情</span>
          </div>
          <el-form-item label="课程目标">
            <el-input v-model="addForm.objectives" type="textarea" :rows="3" placeholder="描述课程的学习目标" maxlength="1000" show-word-limit />
          </el-form-item>
          <el-form-item label="课程大纲">
            <el-input v-model="addForm.outline" type="textarea" :rows="3" placeholder="列出课程的主要章节内容" maxlength="2000" show-word-limit />
          </el-form-item>
          <el-form-item label="先修要求">
            <el-input v-model="addForm.requirements" type="textarea" :rows="2" placeholder="学习本课程需要的基础知识" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="适合人群">
            <el-input v-model="addForm.audience" type="textarea" :rows="2" placeholder="描述适合学习本课程的人群" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="参考资料">
            <el-input v-model="addForm.materials" type="textarea" :rows="2" placeholder="推荐的参考书籍或资源" maxlength="1000" show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dlg-footer">
            <button class="dlg-btn dlg-cancel" @click="handleCloseDialog">取消</button>
            <button class="dlg-btn dlg-confirm" @click="handleAddCourse" :disabled="adding">
              <span v-if="adding" class="spinner"></span>
              {{ adding ? '添加中…' : '确认添加' }}
            </button>
          </div>
        </template>
      </el-dialog>

      <!-- 编辑课程弹窗 -->
      <el-dialog v-model="showEditDialog" title="编辑课程" width="500px"
                 :before-close="handleCloseEditDialog" class="glass-dialog">
        <el-form :model="editForm" :rules="formRules" ref="editFormRef" label-width="90px">
          <el-form-item label="课程名称" prop="name">
            <el-input v-model="editForm.name" placeholder="请输入课程名称" maxlength="50" />
          </el-form-item>
          <el-form-item label="教师姓名" prop="teacherName">
            <el-input v-model="editForm.teacherName" placeholder="请输入教师姓名" maxlength="20" />
          </el-form-item>
          <el-form-item label="课程描述" prop="description">
            <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入课程描述" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="课程时长" prop="duration">
            <el-input v-model="editForm.duration" placeholder="例如：32课时、一学期等" maxlength="20" />
          </el-form-item>
          <el-form-item label="学生数" prop="studentsCount">
            <el-input-number v-model="editForm.studentsCount" :min="0" :max="10000" controls-position="right" class="w-100" />
          </el-form-item>
          <el-form-item label="最大签到次数" prop="maxCheckInCount">
            <el-input-number v-model="editForm.maxCheckInCount" :min="1" :max="100" controls-position="right" class="w-100" />
          </el-form-item>
          <el-form-item label="封面图URL">
            <el-input v-model="editForm.coverImage" placeholder="输入封面图片链接" maxlength="500" />
          </el-form-item>
          <el-form-item label="视频URL">
            <el-input v-model="editForm.videoUrl" placeholder="输入视频链接（支持B站/YouTube嵌入链接）" maxlength="500" />
          </el-form-item>
          <div class="detail-divider">
            <span class="detail-divider-text">课程详情</span>
          </div>
          <el-form-item label="课程目标">
            <el-input v-model="editForm.objectives" type="textarea" :rows="3" placeholder="描述课程的学习目标" maxlength="1000" show-word-limit />
          </el-form-item>
          <el-form-item label="课程大纲">
            <el-input v-model="editForm.outline" type="textarea" :rows="3" placeholder="列出课程的主要章节内容" maxlength="2000" show-word-limit />
          </el-form-item>
          <el-form-item label="先修要求">
            <el-input v-model="editForm.requirements" type="textarea" :rows="2" placeholder="学习本课程需要的基础知识" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="适合人群">
            <el-input v-model="editForm.audience" type="textarea" :rows="2" placeholder="描述适合学习本课程的人群" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="参考资料">
            <el-input v-model="editForm.materials" type="textarea" :rows="2" placeholder="推荐的参考书籍或资源" maxlength="1000" show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dlg-footer">
            <button class="dlg-btn dlg-cancel" @click="handleCloseEditDialog">取消</button>
            <button class="dlg-btn dlg-confirm" @click="handleUpdateCourse" :disabled="editing">
              <span v-if="editing" class="spinner"></span>
              {{ editing ? '保存中…' : '确认修改' }}
            </button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
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
const editing = ref(false)
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const courseList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 9
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))
const search = ref('')
let searchTimer = null

const onSearchInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { page.value = 1; loadCourseList() }, 300)
}

const goToPage = (p) => {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  loadCourseList()
}

const pageNumbers = computed(() => {
  const t = totalPages.value, c = page.value
  const pages = []
  const start = Math.max(1, c - 2), end = Math.min(t, c + 2)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

// 表单引用
const addFormRef = ref()
const editFormRef = ref()

// 表单数据
const addForm = reactive({
  name: '',
  teacherName: '',
  description: '',
  duration: '',
  maxCheckInCount: 12,
  objectives: '',
  outline: '',
  requirements: '',
  audience: '',
  materials: '',
  coverImage: '',
  videoUrl: ''
})

const editForm = reactive({
  id: null,
  name: '',
  teacherName: '',
  description: '',
  duration: '',
  studentsCount: 0,
  maxCheckInCount: 12,
  objectives: '',
  outline: '',
  requirements: '',
  audience: '',
  materials: '',
  coverImage: '',
  videoUrl: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '课程名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  teacherName: [
    { required: true, message: '请输入教师姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '教师姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' },
    { min: 10, max: 200, message: '课程描述长度在 10 到 200 个字符', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入课程时长', trigger: 'blur' }
  ],
  studentsCount: [
    { required: true, message: '请输入学生数', trigger: 'blur' }
  ]
}

// 返回首页
const goToHome = () => {
  console.log('🏠 返回首页')
  router.push('/admin')
}

// 检查管理员登录状态
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

// 加载课程列表
const loadCourseList = () => {
  loading.value = true
  const params = new URLSearchParams({
    page: page.value, pageSize,
    ...(search.value && { search: search.value })
  })
  get('/api/admin/course/list?' + params,
      (message) => {
        if (message && message.list) {
          courseList.value = message.list
          total.value = message.total || 0
        } else if (message && message.data) {
          courseList.value = message.data
          total.value = message.data.length
        } else if (Array.isArray(message)) {
          courseList.value = message
          total.value = message.length
        } else {
          courseList.value = []
          total.value = 0
        }
        loading.value = false
      },
      (message) => {
        ElMessage.error(message || '加载课程列表失败')
        courseList.value = []
        total.value = 0
        loading.value = false
      }
  )
}

// 添加课程
const handleAddCourse = () => {
  if (!addFormRef.value) return

  console.log('📝 开始验证添加课程表单...')

  addFormRef.value.validate((valid) => {
    if (valid) {
      console.log('✅ 表单验证通过，准备提交数据')

      // 打印完整的表单数据
      console.log('📦 提交的表单数据:', {
        name: addForm.name,
        teacherName: addForm.teacherName,
        description: addForm.description,
        duration: addForm.duration,
        maxCheckInCount: addForm.maxCheckInCount
      })

      adding.value = true

      // 检查 API 地址
      const apiUrl = '/api/admin/course/add'
      console.log('🔗 请求地址:', apiUrl)

      console.log('🔄 开始发送添加课程请求...')

      post(apiUrl, {
            name: addForm.name,
            teacherName: addForm.teacherName,
            description: addForm.description,
            duration: addForm.duration,
            maxCheckInCount: addForm.maxCheckInCount,
            objectives: addForm.objectives,
            outline: addForm.outline,
            requirements: addForm.requirements,
            audience: addForm.audience,
            materials: addForm.materials,
            coverImage: addForm.coverImage,
            videoUrl: addForm.videoUrl
          },
          // 成功回调
          (message) => {
            console.log('✅ 添加课程请求成功，服务器响应:', message)

            // 检查响应数据结构
            console.log('📨 响应数据结构:', {
              type: typeof message,
              isArray: Array.isArray(message),
              hasData: message && message.data !== undefined,
              hasSuccess: message && message.success !== undefined,
              fullResponse: message
            })

            if (typeof message === 'string') {
              ElMessage.success(message)
            } else if (message && message.message) {
              ElMessage.success(message.message)
            } else if (message && message.data) {
              ElMessage.success(message.data)
            } else {
              ElMessage.success('添加成功')
            }

            handleCloseDialog()
            loadCourseList()
            adding.value = false
          },
          // 失败回调
          (message) => {
            console.error('❌ 添加课程请求失败，错误信息:', message)

            // 详细记录错误信息
            console.error('❌ 错误详情:', {
              message: message,
              type: typeof message,
              timestamp: new Date().toISOString(),
              requestData: {
                name: addForm.name,
                teacherName: addForm.teacherName,
                description: addForm.description,
                duration: addForm.duration,
                maxCheckInCount: addForm.maxCheckInCount
              }
            })

            // 尝试解析错误信息
            let errorMsg = '添加失败'
            if (typeof message === 'string') {
              errorMsg = message
            } else if (message && message.message) {
              errorMsg = message.message
            } else if (message && message.error) {
              errorMsg = message.error
            } else if (message && message.msg) {
              errorMsg = message.msg
            }

            ElMessage.error(errorMsg)
            adding.value = false
          }
      )
    } else {
      console.log('❌ 表单验证失败，请检查输入')

      // 记录表单验证错误
      const errors = addFormRef.value?.fields?.reduce((acc, field) => {
        if (field.validateState === 'error') {
          acc[field.prop] = field.validateMessage
        }
        return acc
      }, {})

      console.log('❌ 表单验证错误详情:', errors)
      ElMessage.warning('请填写完整的课程信息')
    }
  })
}

// 编辑课程
const handleEdit = (course) => {
  editForm.id = course.id
  editForm.name = course.name
  editForm.teacherName = course.teacherName
  editForm.description = course.description
  editForm.duration = course.duration
  editForm.studentsCount = course.studentsCount || 0
  editForm.maxCheckInCount = course.maxCheckInCount
  editForm.coverImage = course.coverImage || ''
  editForm.videoUrl = course.videoUrl || ''
  // 加载课程详情
  get('/api/admin/course/detail?courseId=' + course.id, data => {
    editForm.objectives = data?.objectives || ''
    editForm.outline = data?.outline || ''
    editForm.requirements = data?.requirements || ''
    editForm.audience = data?.audience || ''
    editForm.materials = data?.materials || ''
  }, () => {
    editForm.objectives = ''
    editForm.outline = ''
    editForm.requirements = ''
    editForm.audience = ''
    editForm.materials = ''
  })
  showEditDialog.value = true
}

// 更新课程
const handleUpdateCourse = () => {
  if (!editFormRef.value) return
  editFormRef.value.validate((valid) => {
    if (valid) {
      editing.value = true

      console.log('🔄 开始更新课程:', {
        ...editForm
      })

      post('/api/admin/course/update', {
            id: editForm.id,
            name: editForm.name,
            teacherName: editForm.teacherName,
            description: editForm.description,
            duration: editForm.duration,
            studentsCount: editForm.studentsCount,
            maxCheckInCount: editForm.maxCheckInCount,
            objectives: editForm.objectives,
            outline: editForm.outline,
            requirements: editForm.requirements,
            audience: editForm.audience,
            materials: editForm.materials,
            coverImage: editForm.coverImage,
            videoUrl: editForm.videoUrl
          },
          (message) => {
            console.log('✅ 更新课程成功:', message)
            ElMessage.success(message)
            handleCloseEditDialog()
            loadCourseList()
            editing.value = false
          },
          (message) => {
            console.error('❌ 更新课程失败:', message)
            ElMessage.error(message || '更新失败')
            editing.value = false
          }
      )
    } else {
      console.log('❌ 表单验证失败')
    }
  })
}

// 删除课程
const handleDelete = (course) => {
  ElMessageBox.confirm(
      `确定要删除课程 "${course.name}" 吗？此操作将删除所有相关的报名记录，且不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    post('/api/admin/course/delete', {courseId: course.id},
        (message) => {
          ElMessage.success(message || '课程删除成功')
          loadCourseList()
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
    addForm.name = ''
    addForm.teacherName = ''
    addForm.description = ''
    addForm.duration = ''
    addForm.maxCheckInCount = 12
    addForm.objectives = ''
    addForm.outline = ''
    addForm.requirements = ''
    addForm.audience = ''
    addForm.materials = ''
    addForm.coverImage = ''
    addForm.videoUrl = ''
  }
}

const handleCloseEditDialog = () => {
  showEditDialog.value = false
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  } else {
    editForm.id = null
    editForm.name = ''
    editForm.teacherName = ''
    editForm.description = ''
    editForm.duration = ''
    editForm.studentsCount = 0
    editForm.maxCheckInCount = 12
    editForm.objectives = ''
    editForm.outline = ''
    editForm.requirements = ''
    editForm.audience = ''
    editForm.materials = ''
    editForm.coverImage = ''
    editForm.videoUrl = ''
  }
}

// 生命周期
onMounted(async () => {
  const isLoggedIn = await checkAdminLogin()
  if (!isLoggedIn) {
    ElMessage.warning('请先登录管理员账户')
    router.push('/admin-login')
    return
  }

  loadCourseList()
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
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
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

.course-name { font-size: 17px; font-weight: 700; letter-spacing: -0.02em; color: #1d1d1f; margin: 0; line-height: 1.4; }
.course-desc {
  font-size: 13px; color: rgba(255,255,255,0.75); line-height: 1.6; margin: 0;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  min-height: 42px;
}

.meta-row { display: flex; gap: 18px; }
.meta-item { display: inline-flex; align-items: center; gap: 5px; font-size: 12px; color: rgba(255,255,255,0.65); font-weight: 500; }

/* ── 凹陷统计块 ── */
.stats-inset {
  display: flex; align-items: center;
  border-radius: 12px; padding: 14px 0;
  background: rgba(0,0,0,0.04);
  box-shadow: 0 2px 4px rgba(0,0,0,0.07) inset, 0 1px 0 rgba(255,255,255,0.88);
  border: 0.5px solid rgba(0,0,0,0.06);
}
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 3px; }
.stat-n { font-size: 20px; font-weight: 700; letter-spacing: -0.04em; color: #1d1d1f; line-height: 1; }
.stat-l { font-size: 11px; color: #86868b; }
.stat-sep { width: 0.5px; height: 28px; background: rgba(0,0,0,0.08); }

/* ── Card footer ── */
.card-footer { display: flex; gap: 8px; }
.btn {
  flex: 1; padding: 10px 16px; border-radius: 10px;
  font-size: 13px; font-weight: 600; cursor: pointer;
  border: none; font-family: inherit;
  transition: all 0.15s cubic-bezier(0.4,0,0.2,1);
}
.btn-edit {
  background: rgba(29,29,31,0.88);
  backdrop-filter: blur(8px); color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2), 0 0.5px 0 rgba(255,255,255,0.12) inset;
}
.btn-edit:hover { background: rgba(0,0,0,0.95); transform: translateY(-1px); }
.btn-danger {
  background: rgba(255,59,48,0.08);
  backdrop-filter: blur(8px); color: #ff3b30;
  border: 0.5px solid rgba(255,59,48,0.2);
  box-shadow: 0 1px 0 rgba(255,255,255,0.9) inset;
}
.btn-danger:hover { background: rgba(255,59,48,0.15); transform: translateY(-1px); }

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

:deep(.glass-dialog .el-input__wrapper),
:deep(.glass-dialog .el-textarea__inner) {
  background: rgba(255,255,255,0.55) !important;
  backdrop-filter: blur(10px);
  border: 0.5px solid rgba(255,255,255,0.8) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  font-family: inherit;
}
:deep(.glass-dialog .el-input__wrapper.is-focus),
:deep(.glass-dialog .el-textarea__inner:focus) {
  background: rgba(255,255,255,0.75) !important;
  border-color: rgba(0,113,227,0.4) !important;
  box-shadow: 0 0 0 3px rgba(0,113,227,0.12) !important;
}
:deep(.glass-dialog .el-input-number.w-100) { width: 100%; }

.detail-divider {
  display: flex; align-items: center; gap: 12px;
  margin: 18px 0 6px; padding: 0;
}
.detail-divider::before, .detail-divider::after {
  content: ''; flex: 1; height: 0.5px;
  background: rgba(0,0,0,0.1);
}
.detail-divider-text {
  font-size: 11px; font-weight: 600;
  letter-spacing: 0.06em; text-transform: uppercase;
  color: #86868b; white-space: nowrap;
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
  .card-footer { flex-direction: column; }
  .page-header { flex-wrap: wrap; }
}

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
.search-input { flex: 1; border: none; outline: none; background: none; font-size: 14px; font-family: inherit; color: #1d1d1f; }
.search-input::placeholder { color: #aeaeb2; }

/* ── Pagination ── */
.pagination { display: flex; align-items: center; justify-content: center; gap: 6px; margin-top: 28px; padding: 16px 0; }
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
.pg-btn.active { background: rgba(29,29,31,0.88); color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.22); }
.pg-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.pg-dots { color: #86868b; font-size: 14px; padding: 0 2px; }
</style>