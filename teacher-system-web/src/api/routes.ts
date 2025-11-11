import type {BackendRouteConfig} from '@/router/types'

/**
 * 路由API接口
 */

/**
 * 从后端获取动态路由配置
 * TODO: 替换为实际的API调用
 * @returns 后端返回的路由配置数组
 */
export async function fetchRoutesFromBackend(): Promise<BackendRouteConfig[]> {
    // 这里应该调用后端API获取路由配置
    // 示例：
    // const response = await request.get('/api/v1/system/routes')
    // return response.data
    
    // 临时模拟数据，实际使用时替换为API调用
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve([
                {
                    path: '/home',
                    name: 'Home',
                    component: 'main/home',
                    meta: {
                        title: '首页',
                        icon: '🏠',
                        requireAuth: true
                    },
                    children: [
                        {
                            path: 'account-management',
                            name: 'AccountManagement',
                            component: 'main/account-management',
                            meta: {
                                title: '账号管理',
                                icon: '👤',
                                requireAuth: true
                            }
                        },
                        {
                            path: 'form-management',
                            name: 'FormManagement',
                            component: 'main/form-management',
                            meta: {
                                title: '登记表管理',
                                icon: '📝',
                                requireAuth: true
                            }
                        }
                    ]
                }
            ] as BackendRouteConfig[])
        }, 100)
    })
}

/**
 * 获取用户菜单数据
 * @returns 菜单数据
 */
export async function getUserMenus(): Promise<import('@/router/types').MenuItem[]> {
    // 这里可以调用后端API获取用户菜单
    // const response = await request.get('/api/v1/system/menus')
    // return response.data
    
    // 临时返回路由转换后的菜单数据
    const routes = await fetchRoutesFromBackend()
    const { transformRoutesToMenu } = await import('@/router/utils')
    return transformRoutesToMenu(routes)
}

