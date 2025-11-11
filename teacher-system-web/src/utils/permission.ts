import {useUserStore} from '@/store/user'

/**
 * 权限工具函数
 * 基于RBAC权限模型，通过权限标识判断，不依赖角色
 */

/**
 * 获取当前用户ID
 */
export function getUserId(): string {
  const userStore = useUserStore()
  return userStore.userId
}

/**
 * 获取当前用户名
 */
export function getUsername(): string {
  const userStore = useUserStore()
  return userStore.username
}

/**
 * 判断是否拥有指定权限
 * @param permission 权限标识，如 'table:create'
 */
export function hasPermission(permission: string): boolean {
  const userStore = useUserStore()
  return userStore.hasPermission(permission)
}

/**
 * 判断是否拥有任意一个指定权限
 * @param permissions 权限标识列表
 */
export function hasAnyPermission(permissions: string[]): boolean {
  const userStore = useUserStore()
  return userStore.hasAnyPermission(permissions)
}

/**
 * 判断是否拥有所有指定权限
 * @param permissions 权限标识列表
 */
export function hasAllPermissions(permissions: string[]): boolean {
  const userStore = useUserStore()
  return userStore.hasAllPermissions(permissions)
}

/**
 * 获取当前用户的所有权限
 */
export function getPermissions(): string[] {
  const userStore = useUserStore()
  return userStore.permissions
}

