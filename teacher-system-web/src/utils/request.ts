import axios, { type AxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 后端统一响应格式
 */
export interface ApiResponse<T = any> {
    code: number
    msg: string
    data: T,
    timestamp?: string,
    traceId?: string
}

/**
 * 创建axios实例
 */
const request = axios.create({
    // 开发环境使用相对路径走代理，生产环境使用完整URL
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

/**
 * 请求拦截器
 */
request.interceptors.request.use(
    (config) => {
        // 从localStorage获取token
        const token = localStorage.getItem('token')
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error: AxiosError) => {
        console.error('[Request] Request error:', error)
        return Promise.reject(error)
    }
)

/**
 * 响应拦截器
 */
request.interceptors.response.use(
    (response: AxiosResponse<ApiResponse>) => {
        const res = response.data

        // 后端返回格式: { code: 200, msg: "success", data: ..., timestamp: ..., traceId: ... }
        if (res.code === 200) {
            // 请求成功，返回data
            return res.data
        } else {
            // 业务错误
            const errorMsg = res.msg || '请求失败'
            console.error('[Request] Business error:', errorMsg)
            
            // 显示错误提示
            ElMessage.error(errorMsg)
            
            return Promise.reject(new Error(errorMsg))
        }
    },
    (error: AxiosError<ApiResponse>) => {
        let errorMessage = '请求失败'

        if (error.response) {
            // 服务器返回了错误状态码
            const status = error.response.status
            const data = error.response.data

            switch (status) {
                case 400:
                    errorMessage = data?.msg || '请求参数错误'
                    break
                case 401:
                    errorMessage = '未授权，请重新登录'
                    // 清除token并跳转到登录页
                    localStorage.removeItem('token')
                    localStorage.removeItem('userInfo')
                    window.location.href = '/login'
                    break
                case 403:
                    errorMessage = '没有权限访问'
                    break
                case 404:
                    errorMessage = '请求的资源不存在'
                    break
                case 500:
                    errorMessage = data?.msg || '服务器内部错误'
                    break
                case 502:
                    errorMessage = '网关错误'
                    break
                case 503:
                    errorMessage = '服务不可用'
                    break
                default:
                    errorMessage = data?.msg || `请求失败 (${status})`
            }
        } else if (error.request) {
            // 请求已发出，但没有收到响应
            errorMessage = '网络连接失败，请检查网络'
        } else {
            // 请求配置出错
            errorMessage = error.message || '请求配置错误'
        }

        console.error('[Request] Error:', errorMessage, error)
        
        // 显示错误提示
        ElMessage.error(errorMessage)
        
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
export function get<T = any>(url: string, config?: RequestConfig): Promise<T> {
    return request.get<ApiResponse<T>, T>(url, config)
}

/**
 * POST请求
 */
export function post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return request.post<ApiResponse<T>, T>(url, data, config)
}

/**
 * PUT请求
 */
export function put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return request.put<ApiResponse<T>, T>(url, data, config)
}

/**
 * DELETE请求
 */
export function del<T = any>(url: string, config?: RequestConfig): Promise<T> {
    return request.delete<ApiResponse<T>, T>(url, config)
}

/**
 * PATCH请求
 */
export function patch<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return request.patch<ApiResponse<T>, T>(url, data, config)
}

// 默认导出request实例
export default request

