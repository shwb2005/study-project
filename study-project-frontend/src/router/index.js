import { createRouter, createWebHistory } from 'vue-router'
import { useStore } from "@/stores/index.js";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'welcome',
      component: () => import('@/view/WelcomeView.vue'),
      children: [
        {
          path: '',
          name: 'welcome-login',
          component: () => import('@/components/welcome/LoginPage.vue')
        }, {
          path: 'register',
          name: 'welcome-register',
          component: () => import('@/components/welcome/RegisterPage.vue')
        },
        {
          path: 'forget',
          name: 'welcome-forget',
          component: () => import('@/components/welcome/ForgetPage.vue')
        },
        // 添加管理员登录路由
        {
          path: 'admin-login',
          name: 'welcome-admin-login',
          component: () => import('@/components/welcome/AdminLoginPage.vue')
        }
      ]
    },
    {
      path: '/index',
      name: 'index',
      component: () => import('@/view/IndexView.vue')
    },
    {
      path: '/courses',
      name: 'courses',
      component: () => import('@/view/CoursePage.vue')
    },
    {
      path: '/course/:id',
      name: 'course-detail',
      component: () => import('@/view/CourseDetail.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/view/ProfilePage.vue')
    },
    {
      path: '/announcements',
      name: 'announcements',
      component: () => import('@/view/AnnouncementPage.vue')
    },
    // 添加管理员路由
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/view/AdminView.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/add-admin',
      name: 'AddAdmin',
      component: () => import('@/view/AddAdmin.vue'),
      meta: { requiresAdmin: true }  // 添加管理员权限要求
    },
    {
      path: '/add-course',
      name: 'AddCourse',
      component: () => import('@/view/AddCourse.vue'),
      meta: { requiresAdmin: true }  // 添加管理员权限要求
    },
    {
      path: '/admin-community',
      name: 'AdminCommunity',
      component: () => import('@/view/AdminCommunity.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin-announcement',
      name: 'AdminAnnouncement',
      component: () => import('@/view/AdminAnnouncement.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/index'
    }
  ],
})

router.beforeEach((to, from, next) => {
  const store = useStore()

  // 检查用户是否已登录
  const isLoggedIn = store.auth.user != null
  // 检查管理员是否已登录
  const isAdminLoggedIn = store.auth.admin != null

  // 检查是否访问欢迎页（登录页）
  const isWelcomePage = to.name && to.name.startsWith('welcome')

  // 检查是否访问需要认证的页面
  const isProtectedPage = to.name === 'index' || to.name === 'courses' || to.name === 'profile' || to.name === 'course-detail' || to.name === 'announcements'

  // 检查是否访问需要管理员权限的页面
  const isAdminPage = to.meta.requiresAdmin

  if ((isLoggedIn || isAdminLoggedIn) && isWelcomePage) {
    // 已登录但访问登录页，根据用户类型跳转到对应页面
    if (isLoggedIn) {
      next('/index')
    } else {
      next('/admin')
    }
  } else if (!isLoggedIn && isProtectedPage) {
    // 未登录但访问需要认证的页面，跳转到登录页
    next('/')
  } else if (isAdminPage && !isAdminLoggedIn) {
    // 访问管理员页面但未登录，跳转到管理员登录页
    next('/admin-login')
  } else {
    // 其他情况正常放行
    next()
  }
})

export default router