import { createRouter, createWebHistory } from 'vue-router'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path:'/',
      name:'welcome',
      component:()=>import('@/View/WelcomeView.vue'),
      children:[
        {
          path:'',
          name:'welcome-login',
          component:()=>import('@/components/welcome/LoginPage.vue')
        },{
          path:'register',
          name:'welcome-register',
          component:()=>import('@/components/welcome/RegisterPage.vue')
        }

      ]
    },{
        path:'/index',
        name:'index',
        component:()=>import('@/View/IndexView.vue')
    }
  ],
})

export default router
