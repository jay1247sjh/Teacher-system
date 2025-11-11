import {createRouter, createWebHistory} from 'vue-router'
import {ElMessage} from 'element-plus'
import {useUserStore} from '@/store/user'

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
                meta: { 
                    requiresAuth: true,
                    requiredPermissions: ['table:create'] // 只有超级管理员拥有此权限
                }
            },
            {
                path: 'account-management',
                name: 'AccountManagement',
                component: () => import('@/views/main/account-management.vue'),
                meta: { 
                    requiresAuth: true,
                    requiredPermissions: ['table:create'] // 只有超级管理员拥有此权限
                }
            },
            {
                path: 'data-monitoring',
                name: 'DataMonitoring',
                component: () => import('@/views/main/data-monitoring.vue'),
                meta: { 
                    requiresAuth: true,
                    requiredPermissions: ['table:create'] // 只有超级管理员拥有此权限
                }
            },
            {
                path: 'system-config',
                name: 'SystemConfig',
                component: () => import('@/views/main/system-config.vue'),
                meta: { 
                    requiresAuth: true,
                    requiredPermissions: ['table:create'] // 只有超级管理员拥有此权限
                }
            },
            {
                path: 'my-data',
                name: 'MyData',
                component: () => import('@/views/main/my-data.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: 'table/:id',
                name: 'TableDetail',
                component: () => import('@/views/main/table-detail.vue'),
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

// 路由守卫：检查登录状态和权限
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

    console.log('[Router Guard] 路由跳转:', from.path, '->', to.path)
    console.log('[Router Guard] 登录状态:', userStore.isLoggedIn)
    console.log('[Router Guard] 用户权限:', userStore.permissions)
    console.log('[Router Guard] 需要认证:', requiresAuth)

    if (requiresAuth && !userStore.isLoggedIn) {
        // 需要登录但未登录，保存目标路径并跳转到登录页
        console.log('[Router Guard] 未登录，跳转登录页')
        ElMessage.warning('请先登录')
        next({
            name: 'Login',
            query: { redirect: to.fullPath } // 保存目标路径
        })
    } else if (to.name === 'Login' && userStore.isLoggedIn) {
        // 已登录用户访问登录页，重定向到首页
        console.log('[Router Guard] 已登录，重定向到首页')
        next({ path: '/home' })
    } else if (requiresAuth && userStore.isLoggedIn) {
        // 检查权限
        const requiredPermissions = to.meta.requiredPermissions as string[] | undefined
        
        if (requiredPermissions && requiredPermissions.length > 0) {
            // 需要特定权限
            const hasPermission = userStore.hasAnyPermission(requiredPermissions)
            
            if (!hasPermission) {
                console.log('[Router Guard] 权限不足，拒绝访问')
                ElMessage.error('您没有权限访问此页面')
                // 重定向到首页
                next({ path: '/home' })
                return
            }
        }
        
        // 有权限，允许访问
        console.log('[Router Guard] 允许访问')
        next()
    } else {
        // 正常访问
        console.log('[Router Guard] 允许访问')
        next()
    }
})

export default router