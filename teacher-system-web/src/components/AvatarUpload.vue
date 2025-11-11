<template>
  <div class="avatar-upload-container">
    <!-- 头像显示区域 -->
    <div class="avatar-display" @click="showUploadDialog = true">
      <!-- 使用懒加载组件优化头像加载 -->
      <LazyImage
        v-if="displayAvatar"
        :src="displayAvatar"
        :alt="`${username}的头像`"
        :aspect-ratio="1"
        class="avatar-img-wrapper"
        @error="handleAvatarError"
      />
      <div v-else class="avatar-placeholder">
        {{ userInitial }}
      </div>
      <div class="avatar-overlay">
        <span class="upload-icon">📷</span>
        <span class="upload-text">更换头像</span>
      </div>
    </div>

    <!-- 上传对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="更换头像"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-tabs v-model="activeTab" class="upload-tabs">
        <!-- 上传文件 -->
        <el-tab-pane label="上传文件" name="file">
          <div class="upload-section">
            <!-- 未选择图片时显示上传区 -->
            <div v-if="!selectedFile" class="upload-area-wrapper">
              <input
                ref="fileInputRef"
                type="file"
              accept="image/*"
                style="display: none"
                @change="handleFileSelect"
              />
              <div class="upload-area" @click="triggerFileSelect">
                <el-icon class="upload-icon-large"><Upload /></el-icon>
                <div class="upload-hint">
                  <p>点击选择图片</p>
                  <p class="upload-limit">支持 JPG、PNG、GIF、WEBP 格式，文件大小不超过 10MB</p>
                </div>
              </div>
            </div>
            
            <!-- 裁剪区域 -->
            <div v-else class="cropper-section">
              <div class="cropper-container">
                <img ref="cropperImageRef" :src="previewUrl" style="max-width: 100%; display: block;" />
              </div>
              
              <div class="cropper-controls">
                <el-button-group>
                  <el-button size="small" @click="rotateCropper(-45)">
                    <el-icon><RefreshLeft /></el-icon> 左旋转
                  </el-button>
                  <el-button size="small" @click="rotateCropper(45)">
                    <el-icon><RefreshRight /></el-icon> 右旋转
                  </el-button>
                  <el-button size="small" @click="zoomCropper(0.1)">
                    <el-icon><ZoomIn /></el-icon> 放大
                  </el-button>
                  <el-button size="small" @click="zoomCropper(-0.1)">
                    <el-icon><ZoomOut /></el-icon> 缩小
                  </el-button>
                  <el-button size="small" @click="resetCropper">
                    <el-icon><Refresh /></el-icon> 重置
                  </el-button>
                </el-button-group>
              </div>
              
              <div class="cropper-actions">
                <el-button @click="cancelCrop">重新选择</el-button>
                <el-button type="primary" :loading="uploadLoading" @click="confirmCrop">
                  确认上传
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 设置URL -->
        <el-tab-pane label="使用URL" name="url">
          <div class="url-section">
            <el-form :model="urlForm" :rules="urlRules" ref="urlFormRef" label-width="80px">
              <el-form-item label="图片URL" prop="avatarUrl">
                <el-input
                  v-model="urlForm.avatarUrl"
                  placeholder="请输入图片URL（http:// 或 https://）"
                  clearable
                />
              </el-form-item>
              
              <!-- URL预览 -->
              <el-form-item v-if="urlForm.avatarUrl && isValidUrl(urlForm.avatarUrl)" label="预览">
                <LazyImage 
                  :src="urlForm.avatarUrl" 
                  :aspect-ratio="1"
                  alt="URL预览"
                  class="url-preview-img-wrapper"
                  @error="handleUrlPreviewError"
                />
                <p v-if="urlPreviewError" class="url-error">图片加载失败，请检查URL是否正确</p>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="handleSetUrl" :loading="urlLoading">
                  保存
                </el-button>
                <el-button @click="urlForm.avatarUrl = ''">清空</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="showUploadDialog = false">关闭</el-button>
        <el-button type="danger" @click="handleDeleteAvatar" :loading="deleteLoading">
          删除头像
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import {computed, defineComponent, nextTick, onBeforeUnmount, ref, watch} from 'vue'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadProps} from 'element-plus'
import {Refresh, RefreshLeft, RefreshRight, Upload, ZoomIn, ZoomOut} from '@element-plus/icons-vue'
import {deleteAvatar, setAvatarUrl, uploadAvatarFile} from '@/api/avatar'
import {useUserStore} from '@/store/user'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'
import {useRouter} from 'vue-router'
import LazyImage from './LazyImage.vue'

export default defineComponent({
  name: 'AvatarUpload',
  components: {
    Upload,
    LazyImage,
    RefreshLeft,
    RefreshRight,
    ZoomIn,
    ZoomOut,
    Refresh
  },
  props: {
    avatar: {
      type: [String, null] as any,
      default: null
    },
    username: {
      type: String,
      required: true
    }
  },
  emits: ['update:avatar', 'avatar-changed'],
  setup(props, { emit }) {
    const userStore = useUserStore()
    const router = useRouter()
    const showUploadDialog = ref(false)
    const activeTab = ref('file')
    const previewUrl = ref('')
    const urlLoading = ref(false)
    const deleteLoading = ref(false)
    const uploadLoading = ref(false)
    const avatarError = ref(false)
    const urlPreviewError = ref(false)
    
    // 头像更新时间戳（用于强制刷新缓存）
    const avatarTimestamp = ref(Date.now())
    
    // 裁剪相关
    const cropperRef = ref<Cropper | null>(null)
    const cropperImageRef = ref<HTMLImageElement | null>(null)
    const fileInputRef = ref<HTMLInputElement | null>(null)
    const selectedFile = ref<File | null>(null)
    
    // URL表单
    const urlFormRef = ref<FormInstance>()
    const urlForm = ref({
      avatarUrl: ''
    })
    
    // 辅助函数，确保路径以斜杠开头
    const ensureLeadingSlash = (path: string) => path.startsWith('/') ? path : `/${path}`

    const urlRules: FormRules = {
      avatarUrl: [
        { required: true, message: '请输入图片URL', trigger: 'blur' },
        {
          pattern: /^https?:\/\/.+/,
          message: 'URL必须以http://或https://开头',
          trigger: 'blur'
        }
      ]
    }

    watch(() => props.avatar, () => {
      avatarError.value = false
    })
    
    // 上传配置
    const uploadAction = computed(() => {
      const baseApi = (import.meta as any).env?.VITE_BASE_API
      return `${baseApi}/user/avatar/upload`
    })
    
    const uploadHeaders = computed(() => {
      return {
        Authorization: `Bearer ${userStore.userInfo?.token || ''}`
      }
    })
    
    // 显示的头像
    const displayAvatar = computed(() => {
      if (avatarError.value) return null
      
      const avatar = props.avatar
      if (!avatar) return null
      
      // 使用固定的时间戳，只在上传成功时更新
      const timestamp = avatarTimestamp.value
      
      // 如果是完整URL，添加时间戳防止缓存
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
        return `${avatar}?t=${timestamp}`
      }

      // 如果路径已经包含了 /api/v1/attachments，说明是旧数据或已拼接过的路径
      // 直接返回，不再重复拼接
      if (avatar.includes('/api/v1/attachments/')) {
        // 提取出真正的相对路径（去掉 /api/v1/attachments 前缀）
        const actualPath = avatar.substring(avatar.indexOf('/api/v1/attachments/') + '/api/v1/attachments'.length)
        console.log('[AvatarUpload] 检测到已拼接的路径，提取相对路径:', actualPath)
        
        const apiTarget = (import.meta as any).env?.VITE_API_TARGET || 'http://localhost:10001';
        const baseApiEnv = (import.meta as any).env?.VITE_BASE_API || '/api/v1';
        const attachmentPath = (import.meta as any).env?.VITE_ATTACHMENT_BASE_URL || 'attachments/';

        const baseApiNormalized = baseApiEnv.replace(/^\/|\/$/g, '');
        const attachmentPathNormalized = attachmentPath.replace(/^\/|\/$/g, '');

        const resolvedBase = `${apiTarget}${baseApiNormalized}/${attachmentPathNormalized}`;
        
        return `${resolvedBase}${ensureLeadingSlash(actualPath)}?t=${timestamp}`
      }

      const apiTarget = (import.meta as any).env?.VITE_API_TARGET || 'http://localhost:10001';
      const baseApiEnv = (import.meta as any).env?.VITE_BASE_API || '/api/v1';
      const attachmentPath = (import.meta as any).env?.VITE_ATTACHMENT_BASE_URL || 'attachments/';

      const baseApiNormalized = baseApiEnv.replace(/^\/|\/$/g, '');
      const attachmentPathNormalized = attachmentPath.replace(/^\/|\/$/g, '');

      const resolvedBase = `${apiTarget}${baseApiNormalized}/${attachmentPathNormalized}`;
      
      return `${resolvedBase}${ensureLeadingSlash(avatar)}?t=${timestamp}`
    })
    
    // 用户名首字母
    const userInitial = computed(() => {
      return props.username ? props.username.charAt(0).toUpperCase() : '?'
    })
    
    // 验证URL格式
    const isValidUrl = (url: string): boolean => {
      return /^https?:\/\/.+/.test(url)
    }
    
    // 头像加载失败
    const handleAvatarError = () => {
      avatarError.value = true
    }
    
    // URL预览加载失败
    const handleUrlPreviewError = () => {
      urlPreviewError.value = true
    }
    
    // 触发文件选择
    const triggerFileSelect = () => {
      fileInputRef.value?.click()
    }
    
    // 文件选择处理
    const handleFileSelect = async (event: Event) => {
      const target = event.target as HTMLInputElement
      const file = target.files?.[0]
      
      if (!file) return
      
      // 验证文件
      const isImage = file.type.startsWith('image/')
      const isLt10M = file.size / 1024 / 1024 < 10
      
      if (!isImage) {
        ElMessage.error('只能上传图片文件！')
        return
      }
      if (!isLt10M) {
        ElMessage.error('图片大小不能超过 10MB！')
        return
      }
      
      selectedFile.value = file
      
      // 生成预览
      const reader = new FileReader()
      reader.onload = async (e) => {
        previewUrl.value = e.target?.result as string
        
        // 等待 DOM 更新
        await nextTick()
        
        // 初始化裁剪器
        if (cropperImageRef.value) {
          // 销毁旧的裁剪器
          if (cropperRef.value) {
            cropperRef.value.destroy()
          }
          
          // 创建新的裁剪器
          cropperRef.value = new Cropper(cropperImageRef.value, {
            aspectRatio: 1,
            viewMode: 1,
            autoCropArea: 0.8,
            background: true,
            rotatable: true,
            scalable: true,
            zoomable: true,
            guides: true,
            center: true,
            highlight: true,
            cropBoxMovable: true,
            cropBoxResizable: true
          })
        }
      }
      reader.readAsDataURL(file)
    }
    
    // 旋转裁剪器 (cropperjs 1.x)
    const rotateCropper = (degree: number) => {
      cropperRef.value?.rotate(degree)
    }
    
    // 缩放裁剪器 (cropperjs 1.x)
    const zoomCropper = (ratio: number) => {
      cropperRef.value?.zoom(ratio)
    }
    
    // 重置裁剪器 (cropperjs 1.x)
    const resetCropper = () => {
      cropperRef.value?.reset()
    }
    
    // 取消裁剪
    const cancelCrop = () => {
      // 销毁裁剪器
      if (cropperRef.value) {
        cropperRef.value.destroy()
        cropperRef.value = null
      }
      
      selectedFile.value = null
      previewUrl.value = ''
      if (fileInputRef.value) {
        fileInputRef.value.value = ''
      }
    }
    
    // 确认裁剪并上传
    const confirmCrop = async () => {
      if (!cropperRef.value || !selectedFile.value) return
      
      uploadLoading.value = true
      
      try {
        // 使用 cropperjs 1.x 的 getCroppedCanvas 方法获取裁剪后的 canvas
        const canvas = cropperRef.value.getCroppedCanvas({
          width: 400,
          height: 400
        })
        
        if (!canvas) {
          throw new Error('无法获取裁剪后的图片')
        }
        
        // 将canvas转为blob
        const blob: Blob | null = await new Promise((resolve) => {
          canvas.toBlob(resolve, 'image/jpeg', 0.9)
        })
        
        if (!blob) {
          throw new Error('图片处理失败')
        }
        
        // 创建FormData
        const formData = new FormData()
        const fileName = selectedFile.value.name.replace(/\.[^/.]+$/, '') + '_cropped.jpg'
        formData.append('file', blob, fileName)
        
        // 上传
        const response = await uploadAvatarFile(formData)
        
        // 处理上传成功
        if (response) {
          ElMessage.success('头像上传成功！')
          
          let newAvatarPath = response.avatarPath || response.avatarUrl
          
          if (newAvatarPath && newAvatarPath.includes('/api/v1/attachments/')) {
            newAvatarPath = newAvatarPath.substring(
              newAvatarPath.indexOf('/api/v1/attachments/') + '/api/v1/attachments'.length
            )
          }
          
          // 更新时间戳以强制刷新头像缓存
          avatarTimestamp.value = Date.now()
          
          emit('update:avatar', newAvatarPath)
          emit('avatar-changed', newAvatarPath)
          
          if (userStore.userInfo) {
            userStore.userInfo.avatar = newAvatarPath
          }
          
          showUploadDialog.value = false
          cancelCrop()
        }
      } catch (error: any) {
        console.error('上传失败:', error)
        
        // 检查是否是token过期
        if (error.response && error.response.status === 401) {
          try {
            await ElMessageBox.confirm('您的登录已过期，是否重新登录？', '登录过期', {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            })
            userStore.clearUserInfo()
            showUploadDialog.value = false
            router.push({ name: 'Login' })
          } catch {
            // 用户取消
          }
        } else {
          ElMessage.error(error.response?.data?.msg || '上传失败，请重试')
        }
      } finally {
        uploadLoading.value = false
      }
    }
    
    // 上传前验证（保留用于URL方式）
    const beforeUpload: UploadProps['beforeUpload'] = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt10M = file.size / 1024 / 1024 < 10
      
      if (!isImage) {
        ElMessage.error('只能上传图片文件！')
        return false
      }
      if (!isLt10M) {
        ElMessage.error('图片大小不能超过 10MB！')
        return false
      }
      
      // 生成预览
      const reader = new FileReader()
      reader.onload = (e) => {
        previewUrl.value = e.target?.result as string
      }
      reader.readAsDataURL(file)
      
      return true
    }
    
    // 上传成功
    const handleUploadSuccess = (response: any) => {
      if (response.code === 200) {
        ElMessage.success('头像上传成功！')
        // response.data 就是 UserAvatarVO 对象
        // 优先使用 avatarPath（相对路径），因为 avatarUrl 已经包含了完整前缀
        let newAvatarPath = response.avatarPath || response.avatarUrl
        
        // 如果获取到的路径包含 /api/v1/attachments，提取出真正的相对路径
        if (newAvatarPath && newAvatarPath.includes('/api/v1/attachments/')) {
          newAvatarPath = newAvatarPath.substring(newAvatarPath.indexOf('/api/v1/attachments/') + '/api/v1/attachments'.length)
          console.log('[AvatarUpload] 提取相对路径保存:', newAvatarPath)
        }
        
        // 更新时间戳以强制刷新头像缓存
        avatarTimestamp.value = Date.now()
        
        emit('update:avatar', newAvatarPath)
        emit('avatar-changed', newAvatarPath)
        
        // 更新store
        if (userStore.userInfo) {
          userStore.userInfo.avatar = newAvatarPath
        }
        
        showUploadDialog.value = false
        previewUrl.value = ''
        avatarError.value = false
      } else {
        ElMessage.error(response.message || '上传失败')
      }
    }
    
    // 上传失败
    const handleUploadError = async (error: any) => {
      console.error('上传失败:', error)
      
      // 解析错误信息
      let errorMsg = '上传失败，请重试'
      let isTokenExpired = false
      
      if (error.response) {
        const data = error.response.data
        const status = error.response.status
        
        // 检查是否是token过期
        if (status === 401 || (data && data.code === 401)) {
          isTokenExpired = true
          errorMsg = '登录已过期'
        } else if (data && data.code) {
        // 根据错误码显示具体信息
          switch (data.code) {
            case 10008:
              errorMsg = '文件不能为空'
              break
            case 10009:
              errorMsg = '文件大小不能超过10MB'
              break
            case 10010:
              errorMsg = '不支持的文件格式，仅支持 JPG、PNG、GIF、WEBP'
              break
            case 10013:
              errorMsg = '文件上传失败，请检查网络或稍后重试'
              break
            default:
              errorMsg = data.msg || data.message || '上传失败'
          }
        } else if (status === 413) {
          errorMsg = '文件太大，请选择小于10MB的图片'
        }
      }
      
      // 如果是token过期，询问是否重新登录
      if (isTokenExpired) {
        try {
          await ElMessageBox.confirm(
            '您的登录已过期，是否重新登录？',
            '登录过期',
            {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning',
              center: true
            }
          )
          
          // 用户确认重新登录
          // 清除用户信息
          userStore.clearUserInfo()
          
          // 关闭上传对话框
          showUploadDialog.value = false
          
          ElMessage.info('正在跳转到登录页...')
          
          // 跳转到登录页
          router.push({ name: 'Login' })
        } catch (error) {
          // 用户取消重新登录
          console.log('用户取消重新登录')
        }
      } else {
        // 其他错误，使用 MessageBox 显示详细错误
      ElMessageBox.alert(errorMsg, '上传失败', {
        confirmButtonText: '知道了',
        type: 'error',
        center: true
      })
      }
    }
    
    // 设置URL
    const handleSetUrl = async () => {
      if (!urlFormRef.value) return
      
      await urlFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        urlLoading.value = true
        try {
          const response = await setAvatarUrl(urlForm.value.avatarUrl)
          console.log('setAvatarUrl 响应:', response)
          
          // response 直接就是 UserAvatarVO 对象（经过 request.ts 拦截器处理）
          // 但需要检查 response 是否存在
          if (!response) {
            throw new Error('响应数据为空')
          }
          
          // 优先使用 avatarPath（相对路径），如果是外部URL则使用 avatarUrl
          const newAvatarPath = response.avatarPath || response.avatarUrl
          console.log('新头像路径:', newAvatarPath)
          
          ElMessage.success('头像设置成功！')
          
          // 更新时间戳以强制刷新头像缓存
          avatarTimestamp.value = Date.now()
          
          emit('update:avatar', newAvatarPath)
          emit('avatar-changed', newAvatarPath)
          
          // 更新store
          if (userStore.userInfo) {
            userStore.userInfo.avatar = newAvatarPath
          }
          
          showUploadDialog.value = false
          urlForm.value.avatarUrl = ''
          avatarError.value = false
        } catch (error: any) {
          console.error('设置头像URL失败:', error)
          
          // 检查是否是token过期（401错误）
          if (error.response && error.response.status === 401) {
            try {
              await ElMessageBox.confirm(
                '您的登录已过期，是否重新登录？',
                '登录过期',
                {
                  confirmButtonText: '重新登录',
                  cancelButtonText: '取消',
                  type: 'warning',
                  center: true
                }
              )
              
              // 用户确认重新登录
              userStore.clearUserInfo()
              showUploadDialog.value = false
              ElMessage.info('正在跳转到登录页...')
              router.push({ name: 'Login' })
            } catch {
              // 用户取消重新登录
              console.log('用户取消重新登录')
            }
          } else {
            // 其他错误
          ElMessage.error(error.message || '设置失败')
          }
        } finally {
          urlLoading.value = false
        }
      })
    }
    
    // 删除头像
    const handleDeleteAvatar = async () => {
      try {
        await ElMessageBox.confirm('确定要删除头像吗？', '删除确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        deleteLoading.value = true
        await deleteAvatar()
        ElMessage.success('头像已删除')
        
        // 更新时间戳以强制刷新头像缓存
        avatarTimestamp.value = Date.now()
        
        emit('update:avatar', '')
        emit('avatar-changed', '')
        
        // 更新store
        if (userStore.userInfo) {
          userStore.userInfo.avatar = ''
        }
        
        showUploadDialog.value = false
        avatarError.value = false
      } catch (error: any) {
        if (error !== 'cancel') {
          // 检查是否是token过期（401错误）
          if (error.response && error.response.status === 401) {
            try {
              await ElMessageBox.confirm(
                '您的登录已过期，是否重新登录？',
                '登录过期',
                {
                  confirmButtonText: '重新登录',
                  cancelButtonText: '取消',
                  type: 'warning',
                  center: true
                }
              )
              
              // 用户确认重新登录
              userStore.clearUserInfo()
              showUploadDialog.value = false
              ElMessage.info('正在跳转到登录页...')
              router.push({ name: 'Login' })
            } catch {
              // 用户取消重新登录
              console.log('用户取消重新登录')
            }
          } else {
            // 其他错误
          ElMessage.error(error.message || '删除失败')
          }
        }
      } finally {
        deleteLoading.value = false
      }
    }
    
    // 监听URL输入，重置预览错误状态
    watch(() => urlForm.value.avatarUrl, () => {
      urlPreviewError.value = false
    })
    
    // 组件销毁时清理裁剪器
    onBeforeUnmount(() => {
      if (cropperRef.value) {
        cropperRef.value.destroy()
        cropperRef.value = null
      }
    })
    
    return {
      showUploadDialog,
      activeTab,
      previewUrl,
      urlLoading,
      deleteLoading,
      uploadLoading,
      urlFormRef,
      urlForm,
      urlRules,
      uploadAction,
      uploadHeaders,
      displayAvatar,
      userInitial,
      avatarError,
      urlPreviewError,
      isValidUrl,
      handleAvatarError,
      handleUrlPreviewError,
      beforeUpload,
      handleUploadSuccess,
      handleUploadError,
      handleSetUrl,
      handleDeleteAvatar,
      // 裁剪相关
      cropperRef,
      cropperImageRef,
      fileInputRef,
      selectedFile,
      triggerFileSelect,
      handleFileSelect,
      rotateCropper,
      zoomCropper,
      resetCropper,
      cancelCrop,
      confirmCrop
    }
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.avatar-upload-container {
  display: inline-block;
}

.avatar-display {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    .avatar-overlay {
      opacity: 1;
    }
  }
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-img-wrapper {
  width: 100%;
  height: 100%;
  
  :deep(.lazy-image) {
    object-fit: cover;
    border-radius: 50%;
  }
  
  :deep(.placeholder) {
    border-radius: 50%;
  }
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
  color: $text-white;
  font-size: $font-size-xxxl;
  font-weight: $font-weight-bold;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  
  .upload-icon {
    font-size: $font-size-xxl;
    margin-bottom: $spacing-xs;
  }
  
  .upload-text {
    color: $text-white;
    font-size: $font-size-sm;
  }
}

.upload-tabs {
  margin-top: $spacing-lg;
}

.upload-section {
  padding: $spacing-lg 0;
}

.upload-area-wrapper {
  width: 100%;
}

.upload-area {
  text-align: center;
  padding: $spacing-xxl;
  border: 2px dashed $border-color;
  border-radius: $border-radius;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: $background-light;
  
  &:hover {
    border-color: $primary-color;
    background-color: rgba($primary-color, 0.05);
  }
}

.avatar-uploader {
  :deep(.el-upload) {
    width: 100%;
  }
  
  :deep(.el-upload-dragger) {
    width: 100%;
    padding: $spacing-xxl;
  }
}

// 裁剪器样式
.cropper-section {
  .cropper-container {
    margin-bottom: $spacing-lg;
    max-height: 400px;
    
    img {
      display: block;
      max-width: 100%;
    }
  }
  
  .cropper-controls {
    margin-bottom: $spacing-lg;
    text-align: center;
    
    .el-button-group {
      display: inline-flex;
      flex-wrap: wrap;
      gap: $spacing-xs;
    }
  }
  
  .cropper-actions {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-md;
  }
}

.upload-icon-large {
  font-size: 48px;
  color: $primary-color;
  margin-bottom: $spacing-md;
}

.upload-hint {
  p {
    margin: $spacing-xs 0;
    color: $text-primary;
    
    em {
      color: $primary-color;
      font-style: normal;
    }
  }
  
  .upload-limit {
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.preview-section {
  margin-top: $spacing-xl;
  text-align: center;
}

.preview-title {
  font-size: $font-size-md;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.preview-img {
  max-width: 200px;
  max-height: 200px;
  border-radius: $border-radius;
  box-shadow: 0 2px 8px $shadow-light;
}

.preview-img-wrapper {
  max-width: 200px;
  margin: 0 auto;
  
  :deep(.lazy-image) {
    border-radius: $border-radius;
    box-shadow: 0 2px 8px $shadow-light;
  }
}

.url-section {
  padding: $spacing-lg 0;
}

.url-preview-img {
  max-width: 200px;
  max-height: 200px;
  border-radius: $border-radius;
  box-shadow: 0 2px 8px $shadow-light;
}

.url-preview-img-wrapper {
  max-width: 200px;
  margin: 0 auto;
  
  :deep(.lazy-image) {
    border-radius: $border-radius;
    box-shadow: 0 2px 8px $shadow-light;
  }
}

.url-error {
  margin-top: $spacing-sm;
  color: $error-color;
  font-size: $font-size-sm;
}
</style>


