import axios, { type AxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios'

/**
 * 创建axios实例
 */
const instance = axios.create({
    baseURL: import.meta.env.VITE_API_TARGET || '/api/v1',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

/**
 * 请求拦截器
 */
instance.interceptors.request.use(
    (config) => {
        // 从localStorage或sessionStorage获取token
        const token = localStorage.getItem('token') || sessionStorage.getItem('token')
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error: AxiosError) => {
        return Promise.reject(error)
    }
)

/**
 * 响应拦截器
 */
instance.interceptors.response.use(
    (response: AxiosResponse) => {
        // 根据后端返回的数据结构调整
        // 假设后端返回格式为 { code: 200, data: ..., message: ... }
        if (response.data && typeof response.data === 'object' && 'code' in response.data) {
            if (response.data.code === 200 || response.data.code === 0) {
                return response.data.data
            } else {
                // 处理业务错误
                console.error('[Request] Business error:', response.data.message)
                return Promise.reject(new Error(response.data.message || '请求失败'))
            }
        }
        // 如果后端直接返回数据，则直接返回
        return response.data
    },
    (error: AxiosError) => {
        // 处理HTTP错误
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    // 未授权，清除token并跳转到登录页
                    localStorage.removeItem('token')
                    sessionStorage.removeItem('token')
                    window.location.href = '/login'
                    break
                case 403:
                    console.error('[Request] Forbidden: 没有权限')
                    break
                case 404:
                    console.error('[Request] Not Found: 请求的资源不存在')
                    break
                case 500:
                    console.error('[Request] Server Error: 服务器内部错误')
                    break
                default:
                    console.error('[Request] Error:', error.message)
            }
        } else if (error.request) {
            console.error('[Request] No response:', error.request)
        } else {
            console.error('[Request] Error:', error.message)
        }
        return Promise.reject(error)
    }
)

/**
 * 请求配置接口
 */
export interface RequestConfig extends AxiosRequestConfig {
    [key: string]: unknown
}

/**
 * GET请求
 */
export function get<T = unknown>(url: string, config?: RequestConfig): Promise<T> {
    return instance.get<T>(url, config) as Promise<T>
}

/**
 * POST请求
 */
export function post<T = unknown>(url: string, data?: unknown, config?: RequestConfig): Promise<T> {
    return instance.post<T>(url, data, config) as Promise<T>
}

/**
 * PUT请求
 */
export function put<T = unknown>(url: string, data?: unknown, config?: RequestConfig): Promise<T> {
    return instance.put<T>(url, data, config) as Promise<T>
}

/**
 * DELETE请求
 */
export function del<T = unknown>(url: string, config?: RequestConfig): Promise<T> {
    return instance.delete<T>(url, config) as Promise<T>
}

/**
 * PATCH请求
 */
export function patch<T = unknown>(url: string, data?: unknown, config?: RequestConfig): Promise<T> {
    return instance.patch<T>(url, data, config) as Promise<T>
}

export default instance

