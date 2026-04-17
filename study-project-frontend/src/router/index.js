import { createRouter, createWebHistory } from 'vue-router'
import { useStore } from "@/stores/index.js";
import { ElMessage } from "element-plus";

const roleAllowedRoutes = {
  super_admin: ['AddAdmin', 'AddCourse', 'AdminCommunity', 'AdminAnnouncement'],
  course_admin: ['AddCourse'],
  community_admin: ['AdminCommunity'],
  announcement_admin: ['AdminAnnouncement']
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'welcome',
      component: () => import('@/View/user/welcome/WelcomeView.vue'),
      children: [
        {
          path: '',
          name: 'welcome-login',
          component: () => import('@/View/user/welcome/LoginPage.vue')
        }, {
          path: 'register',
          name: 'welcome-register',
          component: () => import('@/View/user/welcome/RegisterPage.vue')
        },
        {
          path: 'forget',
          name: 'welcome-forget',
          component: () => import('@/View/user/welcome/ForgetPage.vue')
        },
        // 添加管理员登录路由
        {
          path: 'admin-login',
          name: 'welcome-admin-login',
          component: () => import('@/View/admin/AdminLoginPage.vue')
        }
      ]
    },
    {
      path: '/index',
      name: 'index',
      component: () => import('@/View/user/home/IndexView.vue')
    },
    {
      path: '/courses',
      name: 'courses',
      component: () => import('@/View/user/course/CoursePage.vue')
    },
    {
      path: '/course/:id',
      name: 'course-detail',
      component: () => import('@/View/user/course/CourseDetail.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/View/user/profile/ProfilePage.vue')
    },
    {
      path: '/announcements',
      name: 'announcements',
      component: () => import('@/View/user/announcement/AnnouncementPage.vue')
    },
    {
      path: '/study-plan',
      name: 'study-plan',
      component: () => import('@/View/user/study/StudyPlanPage.vue')
    },
    // 添加管理员路由
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/View/admin/AdminView.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/add-admin',
      name: 'AddAdmin',
      component: () => import('@/View/admin/AddAdmin.vue'),
      meta: { requiresAdmin: true }  // 添加管理员权限要求
    },
    {
      path: '/add-course',
      name: 'AddCourse',
      component: () => import('@/View/admin/AddCourse.vue'),
      meta: { requiresAdmin: true }  // 添加管理员权限要求
    },
    {
      path: '/admin-community',
      name: 'AdminCommunity',
      component: () => import('@/View/admin/AdminCommunity.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin-announcement',
      name: 'AdminAnnouncement',
      component: () => import('@/View/admin/AdminAnnouncement.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/index'
    }
  ],
})

router.beforeEach((to, _from, next) => {
  const store = useStore()

  const isLoggedIn = store.auth.user != null
  const isAdminLoggedIn = store.auth.admin != null
  const isWelcomePage = to.name && to.name.startsWith('welcome')
  const isProtectedPage = to.name === 'index' || to.name === 'courses' || to.name === 'profile' || to.name === 'course-detail' || to.name === 'announcements' || to.name === 'study-plan'
  const isAdminPage = to.meta.requiresAdmin

  // 管理员页面
  if (isAdminPage) {
    if (!isAdminLoggedIn) {
      next('/admin-login')
    } else {
      const adminRole = store.auth.admin?.role
      const allowedRoutes = roleAllowedRoutes[adminRole] || []
      if (to.name !== 'admin' && !allowedRoutes.includes(to.name)) {
        ElMessage.warning('权限不足')
        next('/admin')
        return
      }
      next()
    }
    return
  }

  // 用户页面：等待初始化完成再检查登录状态
  if (!store.initialized && isProtectedPage) {
    next()
    return
  }

  if ((isLoggedIn || isAdminLoggedIn) && isWelcomePage) {
    if (isLoggedIn) {
      next('/index')
    } else {
      next('/admin')
    }
  } else if (!isLoggedIn && !isAdminLoggedIn && isProtectedPage && store.initialized) {
    next('/')
  } else {
    next()
  }
})

export default router