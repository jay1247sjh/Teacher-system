<template>
  <div class="avatar-upload-container">
    <!-- 头像显示区域 -->
    <div class="avatar-display" @click="showUploadDialog = true">
      <img 
        v-if="displayAvatar" 
        :src="displayAvatar" 
        class="avatar-img"
        @error="handleAvatarError"
        alt="用户头像"
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
            <el-upload
              ref="uploadRef"
              class="avatar-uploader"
              :action="uploadAction"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :headers="uploadHeaders"
              accept="image/*"
              drag
            >
              <div class="upload-area">
                <el-icon class="upload-icon-large"><Upload /></el-icon>
                <div class="upload-hint">
                  <p>将图片拖到此处，或<em>点击上传</em></p>
                  <p class="upload-limit">支持 JPG、PNG、GIF、WEBP 格式，文件大小不超过 10MB</p>
                </div>
              </div>
            </el-upload>
            
            <!-- 预览区域 -->
            <div v-if="previewUrl" class="preview-section">
              <p class="preview-title">预览：</p>
              <img :src="previewUrl" class="preview-img" alt="预览" />
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
                <img :src="urlForm.avatarUrl" class="url-preview-img" alt="URL预览" @error="handleUrlPreviewError" />
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
import { defineComponent, ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type UploadProps, type FormInstance, type FormRules } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { uploadAvatarFile, setAvatarUrl, deleteAvatar } from '@/api/avatar'
import { useUserStore } from '@/store/user'

export default defineComponent({
  name: 'AvatarUpload',
  components: {
    Upload
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
    const showUploadDialog = ref(false)
    const activeTab = ref('file')
    const previewUrl = ref('')
    const urlLoading = ref(false)
    const deleteLoading = ref(false)
    const avatarError = ref(false)
    const urlPreviewError = ref(false)
    
    // URL表单
    const urlFormRef = ref<FormInstance>()
    const urlForm = ref({
      avatarUrl: ''
    })
    
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
      
      // 如果是完整URL，直接返回
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
        return avatar
      }
      
      // 相对路径，拼接基础URL
      const baseUrl = (import.meta as any).env?.VITE_ATTACHMENT_BASE_URL || ''
      return `${baseUrl}${avatar}`
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
    
    // 上传前验证
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
        const newAvatarPath = response.data?.avatarPath || response.data?.avatarUrl
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
    const handleUploadError = (error: any) => {
      console.error('上传失败:', error)
      
      // 解析错误信息
      let errorMsg = '上传失败，请重试'
      
      if (error.response) {
        const data = error.response.data
        const status = error.response.status
        
        // 根据错误码显示具体信息
        if (data && data.code) {
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
            case 401:
              errorMsg = '登录已过期，请重新登录'
              break
            default:
              errorMsg = data.msg || data.message || '上传失败'
          }
        } else if (status === 413) {
          errorMsg = '文件太大，请选择小于10MB的图片'
        } else if (status === 401) {
          errorMsg = '登录已过期，请重新登录'
        }
      }
      
      // 使用 MessageBox 显示详细错误
      ElMessageBox.alert(errorMsg, '上传失败', {
        confirmButtonText: '知道了',
        type: 'error',
        center: true
      })
    }
    
    // 设置URL
    const handleSetUrl = async () => {
      if (!urlFormRef.value) return
      
      await urlFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        urlLoading.value = true
        try {
          const response: any = await setAvatarUrl(urlForm.value.avatarUrl)
          console.log('setAvatarUrl 响应:', response)
          
          // response 直接就是 UserAvatarVO 对象（经过 request.ts 拦截器处理）
          // 但需要检查 response 是否存在
          if (!response) {
            throw new Error('响应数据为空')
          }
          
          const newAvatarPath = response.avatarPath || response.avatarUrl
          console.log('新头像路径:', newAvatarPath)
          
          ElMessage.success('头像设置成功！')
          
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
          ElMessage.error(error.message || '设置失败')
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
          ElMessage.error(error.message || '删除失败')
        }
      } finally {
        deleteLoading.value = false
      }
    }
    
    // 监听URL输入，重置预览错误状态
    watch(() => urlForm.value.avatarUrl, () => {
      urlPreviewError.value = false
    })
    
    return {
      showUploadDialog,
      activeTab,
      previewUrl,
      urlLoading,
      deleteLoading,
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
      handleDeleteAvatar
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

.avatar-uploader {
  :deep(.el-upload) {
    width: 100%;
  }
  
  :deep(.el-upload-dragger) {
    width: 100%;
    padding: $spacing-xxl;
  }
}

.upload-area {
  text-align: center;
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

.url-section {
  padding: $spacing-lg 0;
}

.url-preview-img {
  max-width: 200px;
  max-height: 200px;
  border-radius: $border-radius;
  box-shadow: 0 2px 8px $shadow-light;
}

.url-error {
  margin-top: $spacing-sm;
  color: $error-color;
  font-size: $font-size-sm;
}
</style>

