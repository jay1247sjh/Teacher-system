import {defineStore} from 'pinia'
import {computed, ref} from 'vue'

/**
 * 用户信息接口
 * 基于RBAC权限模型，只存储权限标识，不存储角色
 */
export interface UserInfo {
  token: string
  expireAt: string
  wordId: string
  username: string
  avatar?: string
  permissions: string[]  // 权限标识列表，如 ['table:create', 'table:data:score']
}

/**
 * 用户状态管理 Store
 * 使用 Pinia 持久化存储用户信息和权限
 */
export const useUserStore = defineStore('user', () => {
  // ========== 状态定义 ==========
  
  /** 用户信息 */
  const userInfo = ref<UserInfo | null>(null)
  
  /** 是否已登录 */
  const isLoggedIn = computed(() => !!userInfo.value?.token)
  
  /** 用户工号 */
  const userId = computed(() => userInfo.value?.wordId || '')
  
  /** 用户名 */
  const username = computed(() => userInfo.value?.username || '')
  
  /** 权限列表 */
  const permissions = computed(() => userInfo.value?.permissions || [])
  
  // ========== 权限判断（基于权限标识） ==========
  
  /**
   * 判断是否拥有指定权限
   * @param permission 权限标识，如 'table:create'
   */
  const hasPermission = (permission: string): boolean => {
    return permissions.value.includes(permission)
  }
  
  /**
   * 判断是否拥有任意一个指定权限
   * @param permissionList 权限标识列表
   */
  const hasAnyPermission = (permissionList: string[]): boolean => {
    return permissionList.some(perm => permissions.value.includes(perm))
  }
  
  /**
   * 判断是否拥有所有指定权限
   * @param permissionList 权限标识列表
   */
  const hasAllPermissions = (permissionList: string[]): boolean => {
    return permissionList.every(perm => permissions.value.includes(perm))
  }
  
  // ========== 操作方法 ==========
  
  /**
   * 设置用户信息
   * @param info 用户信息
   */
  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    // 同时保存token到localStorage，供axios拦截器使用
    localStorage.setItem('token', info.token)
  }
  
  /**
   * 更新用户信息（部分更新）
   * @param info 部分用户信息
   */
  const updateUserInfo = (info: Partial<UserInfo>) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
    }
  }
  
  /**
   * 清除用户信息（登出）
   */
  const clearUserInfo = () => {
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo') // 清理旧的localStorage数据
  }
  
  /**
   * 从localStorage恢复用户信息
   * 用于页面刷新时恢复状态
   */
  const restoreUserInfo = () => {
    const token = localStorage.getItem('token')
    const userInfoStr = localStorage.getItem('userInfo')
    
    if (token && userInfoStr) {
      try {
        const info = JSON.parse(userInfoStr)
        // 确保permissions存在且是数组
        if (info && Array.isArray(info.permissions)) {
          userInfo.value = info
        } else {
          console.warn('用户信息格式不正确，清除缓存')
          clearUserInfo()
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
        clearUserInfo()
      }
    }
  }
  
  /**
   * 检查token是否过期
   */
  const isTokenExpired = computed(() => {
    if (!userInfo.value?.expireAt) return true
    const expireTime = new Date(userInfo.value.expireAt).getTime()
    return Date.now() >= expireTime
  })
  
  // ========== 返回 ==========
  
  return {
    // 状态
    userInfo,
    isLoggedIn,
    userId,
    username,
    permissions,
    
    // 权限判断（基于权限标识）
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    
    // Token状态
    isTokenExpired,
    
    // 操作方法
    setUserInfo,
    updateUserInfo,
    clearUserInfo,
    restoreUserInfo
  }
}, {
  // 启用持久化
  persist: {
    key: 'teacher-system-user',
    storage: localStorage,
    paths: ['userInfo'] // 只持久化userInfo
  }
})

