import {del, get, post} from '@/utils/request'

/**
 * 用户管理信息
 */
export interface UserManagement {
    id: string
    username: string
    email: string | null
    roleIds: number[]
    roleNames: string[]
    createTime: string
    updateTime: string
}

/**
 * 创建用户请求
 */
export interface CreateUserRequest {
    id: string
    username: string
    password: string
    email?: string
    code?: string
    roleIds: number[]
}

/**
 * 更新用户请求
 */
export interface UpdateUserRequest {
    id: string
    username: string
    email?: string
    roleIds: number[]
    newPassword?: string
}

/**
 * 获取所有用户列表（管理功能）
 */
export function getAllUsersForManagement(): Promise<UserManagement[]> {
    return get('/user/management/list')
}

/**
 * 创建用户
 */
export function createUser(data: CreateUserRequest): Promise<string> {
    return post('/user/management/create', data)
}

/**
 * 更新用户
 */
export function updateUser(data: UpdateUserRequest): Promise<string> {
    return post('/user/management/update', data)
}

/**
 * 删除用户
 */
export function deleteUser(id: string): Promise<string> {
    return del(`/user/management/delete/${id}`)
}

