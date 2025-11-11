import request from '@/utils/request'

/**
 * 用户头像VO
 */
export interface UserAvatarVO {
  userId: string
  avatarPath: string | null
  avatarUrl: string | null
}

/**
 * 上传头像URL请求
 */
export interface UploadAvatarUrlRequest {
  avatarUrl: string
}

/**
 * 获取当前用户头像
 */
export function getMyAvatar() {
  return request<UserAvatarVO>({
    url: '/user/avatar',
    method: 'get'
  })
}

/**
 * 获取指定用户头像
 */
export function getUserAvatar(userId: string) {
  return request<UserAvatarVO>({
    url: `/user/avatar/${userId}`,
    method: 'get'
  })
}

/**
 * 上传头像文件
 */
export function uploadAvatarFile(fileOrFormData: File | FormData) {
  let formData: FormData
  
  if (fileOrFormData instanceof FormData) {
    formData = fileOrFormData
  } else {
    formData = new FormData()
    formData.append('file', fileOrFormData)
  }
  
  return request<UserAvatarVO>({
    url: '/user/avatar/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 设置头像URL
 */
export function setAvatarUrl(avatarUrl: string) {
  return request<UserAvatarVO>({
    url: '/user/avatar/url',
    method: 'post',
    data: { avatarUrl }
  })
}

/**
 * 删除头像
 */
export function deleteAvatar() {
  return request<string>({
    url: '/user/avatar',
    method: 'delete'
  })
}

