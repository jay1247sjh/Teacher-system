import { post } from '@/utils/request'

/**
 * 用户登录请求参数
 */
export interface LoginParams {
    id: string          // 工号
    password: string    // 密码
}

/**
 * 用户注册请求参数
 */
export interface RegisterParams {
    id: string          // 工号
    username: string    // 姓名
    password: string    // 密码
    email: string       // 邮箱
    code: string        // 验证码
}

/**
 * 发送验证码请求参数
 */
export interface SendCodeParams {
    username: string    // 姓名
    email: string       // 邮箱
}

/**
 * 用户信息响应
 */
export interface UserVO {
    token: string           // 访问令牌
    expireAt: string        // 令牌过期时间
    wordId: string          // 用户工号
    username: string        // 用户名
    avatar: string          // 用户头像
    permissions: string[]   // 用户权限列表
    route: RouteVO          // 动态路由树
}

/**
 * 路由信息
 */
export interface RouteVO {
    path: string
    name: string
    component?: string
    children?: RouteVO[]
    meta?: {
        title?: string
        icon?: string
        [key: string]: any
    }
}

/**
 * 用户登录
 */
export function login(params: LoginParams): Promise<UserVO> {
    return post<UserVO>('/user/login', params)
}

/**
 * 用户注册
 */
export function register(params: RegisterParams): Promise<string> {
    return post<string>('/user/register', params)
}

/**
 * 发送验证码
 */
export function sendCode(params: SendCodeParams): Promise<string> {
    return post<string>('/user/send-code', params)
}

