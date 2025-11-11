import request from '@/utils/request'

/**
 * 附件信息VO
 */
export interface AttachmentVO {
  attachmentId: string
  fileName: string
  fileSize: number
  filePath: string
  fileUrl: string
  uploadTime: string
}

/**
 * 上传附件
 * @param file 文件对象
 * @param category 附件分类（如：table-data）
 * @param relatedId 关联ID（如：表格数据ID）
 */
export function uploadAttachment(file: File, category: string, relatedId?: string) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', category)
  if (relatedId) {
    formData.append('relatedId', relatedId)
  }
  
  return request<AttachmentVO>({
    url: '/table/attachment/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除附件
 * @param attachmentId 附件ID
 */
export function deleteAttachment(attachmentId: string) {
  return request<string>({
    url: `/table/attachment/${attachmentId}`,
    method: 'delete'
  })
}

/**
 * 获取附件信息
 * @param attachmentId 附件ID
 */
export function getAttachment(attachmentId: string) {
  return request<AttachmentVO>({
    url: `/table/attachment/${attachmentId}`,
    method: 'get'
  })
}

