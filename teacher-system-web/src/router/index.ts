import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
    {
        path: '/',
        redirect: '/login',
    },
    {
        path: '/home',
        component: () => import('@/layouts/TableLayout.vue'),
        meta: { requiresAuth: true }, // 需要登录
        children: [
            {
                path: '',
                name: 'HomeWelcome',
                component: () => import('@/views/main/home-welcome.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: 'table-management',
                name: 'TableManagement',
                component: () => import('@/views/main/table-management.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: 'account-management',
                name: 'AccountManagement',
                component: () => import('@/views/main/account-management.vue'),
                meta: { requiresAuth: true }
            }
        ]
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/login/register.vue')
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/error/404.vue')
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

    console.log('[Router Guard] 路由跳转:', from.path, '->', to.path)
    console.log('[Router Guard] Token状态:', token ? `存在(${token.substring(0, 20)}...)` : '不存在')
    console.log('[Router Guard] 需要认证:', requiresAuth)

    if (requiresAuth && !token) {
        // 需要登录但没有token，保存目标路径并跳转到登录页
        console.log('[Router Guard] 缺少token，跳转登录页')
        ElMessage.warning('请先登录')
        next({
            name: 'Login',
            query: { redirect: to.fullPath } // 保存目标路径
        })
    } else if (to.name === 'Login' && token) {
        // 已登录用户访问登录页，重定向到首页
        console.log('[Router Guard] 已登录，重定向到首页')
        next({ path: '/home' })
    } else {
        // 正常访问
        console.log('[Router Guard] 允许访问')
        next()
    }
})

export default router