import axios, { type AxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

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
        console.log('[Request] 请求URL:', config.url)
        console.log('[Request] Token:', token)
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`
            console.log('[Request] Authorization头:', config.headers.Authorization)
        } else {
            console.log('[Request] 未设置Authorization头，原因:', !token ? 'token为空' : 'headers不存在')
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
            console.error('[Request] Business error, code:', res.code, 'msg:', errorMsg)
            
            // 只在特定的认证错误码时才清除token并跳转
            // 401: 未授权（token无效/过期）
            // 403: 禁止访问（可能是token相关）
            const isAuthError = res.code === 401 || res.code === 403
            
            if (isAuthError) {
                // 认证错误：清除token并跳转到登录页
                console.log('检测到认证错误，清除token并跳转登录页')
                console.log('当前 token:', localStorage.getItem('token'))
                console.log('调用栈:', new Error().stack)
                ElMessage.error('登录已过期，请重新登录')
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                console.log('Token已清除')
                
                // 获取当前路径（排除登录和注册页）
                const currentPath = window.location.pathname
                const currentRoute = router.currentRoute.value
                
                // 只在不是登录页时才跳转
                if (currentRoute.name !== 'Login' && currentRoute.name !== 'Register') {
                    // 使用 window.location.href 强制完整跳转，避免 keep-alive 缓存问题
                    const fullPath = currentPath + window.location.search
                    const redirectUrl = `/login?redirect=${encodeURIComponent(fullPath)}`
                    console.log('Token失效，跳转到登录页，保存路径:', fullPath)
                    window.location.href = redirectUrl
                }
            } else {
                // 其他业务错误：显示错误提示
                console.log('显示业务错误提示:', errorMsg)
                try {
                    ElMessage.error(errorMsg)
                    console.log('ElMessage.error 调用成功')
                } catch (e) {
                    console.error('ElMessage.error 调用失败:', e)
                    alert(errorMsg) // 备用方案
                }
            }
            
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
                    errorMessage = data?.msg || '登录已过期，请重新登录'
                    // 清除token并跳转到登录页，保存当前路径
                    localStorage.removeItem('token')
                    localStorage.removeItem('userInfo')
                    
                    // 获取当前路径
                    const currentPath = window.location.pathname
                    const currentRoute = router.currentRoute.value
                    
                    // 只在不是登录页时才跳转
                    if (currentRoute.name !== 'Login' && currentRoute.name !== 'Register') {
                        // 使用 window.location.href 强制完整跳转，避免 keep-alive 缓存问题
                        const fullPath = currentPath + window.location.search
                        const redirectUrl = `/login?redirect=${encodeURIComponent(fullPath)}`
                        console.log('Token失效(401)，跳转到登录页，保存路径:', fullPath)
                        window.location.href = redirectUrl
                    }
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

