/**
 * 路由元信息
 */
export interface RouteMeta {
  title: string
  icon?: string
  requireAuth?: boolean
  permissions?: string[]
}

/**
 * 后端路由配置
 */
export interface BackendRouteConfig {
  path: string
  name: string
  component: string
  meta?: RouteMeta
  children?: BackendRouteConfig[]
}

/**
 * 菜单项
 */
export interface MenuItem {
  path: string
  name: string
  title: string
  icon?: string
  children?: MenuItem[]
}
