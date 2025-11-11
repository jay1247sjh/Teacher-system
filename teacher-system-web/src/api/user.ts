import {get, post} from '@/utils/request'

/**
 * 登录请求参数
 */
export interface LoginRequest {
  id: string
  password: string
}

/**
 * 注册请求参数
 */
export interface RegisterRequest {
  id: string
  username: string
  password: string
  email: string
  code: string
}

/**
 * 发送验证码请求参数
 */
export interface SendCodeRequest {
  username: string
  email: string
}

/**
 * 用户信息
 */
export interface UserInfo {
  token: string
  expireAt: string
  wordId: string
  username: string
  avatar?: string
  permissions: string[]
  route: any
}

/**
 * 简单用户信息（用于下拉列表）
 */
export interface SimpleUser {
  id: string
  username: string
}

/**
 * 用户登录
 */
export function login(data: LoginRequest) {
  return post<UserInfo>('/user/login', data)
}

/**
 * 用户注册
 */
export function register(data: RegisterRequest) {
  return post<UserInfo>('/user/register', data)
}

/**
 * 发送邮箱验证码
 */
export function sendCode(data: SendCodeRequest) {
  return post<string>('/user/send-code', data)
}

/**
 * 获取所有普通用户列表
 */
export function getNormalUsers() {
  return get<SimpleUser[]>('/user/normal-users')
}
