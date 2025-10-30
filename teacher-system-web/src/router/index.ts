import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        redirect: '/login',
    },
    {
        path: '/home',
        component: () => import('@/layouts/TableLayout.vue'),
        children: [
            {
                path: '',
                name: 'HomeWelcome',
                component: () => import('@/views/main/home-welcome.vue')
            },
            {
                path: 'table-management',
                name: 'TableManagement',
                component: () => import('@/views/main/table-management.vue')
            },
            {
                path: 'account-management',
                name: 'AccountManagement',
                component: () => import('@/views/main/account-management.vue')
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

export default router