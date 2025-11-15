import type {BackendRouteConfig, MenuItem} from './types'

/**
 * 将后端路由配置转换为菜单数据
 * @param routes 后端路由配置数组
 * @returns 菜单数据数组
 */
export function transformRoutesToMenu(routes: BackendRouteConfig[]): MenuItem[] {
  return routes.map(route => {
    const menuItem: MenuItem = {
      path: route.path,
      name: route.name,
      title: route.meta?.title || route.name,
      icon: route.meta?.icon
    }
    
    if (route.children && route.children.length > 0) {
      menuItem.children = transformRoutesToMenu(route.children)
    }
    
    return menuItem
  })
}
