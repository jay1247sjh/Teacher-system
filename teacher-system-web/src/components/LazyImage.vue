<template>
  <div class="lazy-image-wrapper" :class="{ 'loaded': imageLoaded, 'error': imageError }" ref="imageWrapperRef">
    <!-- 占位图：加载前显示 -->
    <div v-if="!imageLoaded && !imageError" class="placeholder" :style="{ paddingBottom: `${aspectRatio * 100}%` }">
      <div class="placeholder-content">
        <span class="placeholder-icon">🖼️</span>
        <span v-if="showProgress && loadingProgress > 0" class="loading-progress">
          {{ loadingProgress }}%
        </span>
      </div>
    </div>

    <!-- 实际图片：进入视口后加载 -->
    <img v-if="imageLoaded && currentSrc" :src="currentSrc" :alt="alt" class="lazy-image" @load="handleImageLoad"
      @error="handleImageError" ref="imageRef" />

    <!-- 加载失败提示 -->
    <div v-if="imageError" class="error-placeholder">
      <span class="error-icon">⚠️</span>
      <span class="error-text">图片加载失败</span>
      <button v-if="retryable" class="retry-btn" @click="retryLoad">重试</button>
    </div>

    <!-- 加载动画 -->
    <div v-if="!imageLoaded && !imageError && isIntersecting" class="loading-spinner"></div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: 'LazyImage',
  props: {
    // 图片源地址
    src: {
      type: String,
      required: true
    },
    // 占位图地址（可选）
    placeholderSrc: {
      type: String,
      default: ''
    },
    // 图片描述
    alt: {
      type: String,
      default: ''
    },
    // 宽高比（用于占位，避免布局抖动）
    aspectRatio: {
      type: Number,
      default: 0.75 // 默认 4:3
    },
    // 是否可重试
    retryable: {
      type: Boolean,
      default: true
    },
    // 是否显示加载进度
    showProgress: {
      type: Boolean,
      default: false
    },
    // Intersection Observer 配置
    rootMargin: {
      type: String,
      default: '50px' // 提前 50px 开始加载
    },
    threshold: {
      type: Number,
      default: 0.01 // 1% 可见时触发
    },
    // API 基础路径
    baseURL: {
      type: String,
      default: ''
    },
    // 默认图片（加载失败时显示）
    defaultSrc: {
      type: String,
      default: ''
    }
  },
  emits: ['load', 'error'],
  data() {
    return {
      // 状态管理
      isIntersecting: false,      // 是否进入视口
      imageLoaded: false,         // 图片是否加载完成
      imageError: false,          // 是否加载失败
      loadingProgress: 0,         // 加载进度
      currentSrc: '',            // 当前显示的图片 src
      observer: null as IntersectionObserver | null,  // 观察者实例
      retryCount: 0              // 重试次数
    }
  },
  computed: {
    // 完整的图片 URL
    fullImageUrl(): string {
      if (!this.src) return ''
      
      // 如果已经是完整 URL（http/https/data），直接返回
      if (/^(https?:)?\/\//.test(this.src) || this.src.startsWith('data:')) {
        return this.src
      }
      
      // 如果是绝对路径（以 / 开头），检查是否包含完整的 API 路径
      if (this.src.startsWith('/')) {
        // 如果包含 /api/v1/attachments，说明是完整的 API 路径，需要拼接 API 目标地址
        if (this.src.includes('/api/v1/attachments/')) {
          const apiTarget = (import.meta as any).env?.VITE_API_TARGET || 'http://localhost:10001'
          console.log('[LazyImage] 检测到完整 API 路径，拼接 apiTarget:', this.src)
          return `${apiTarget}${this.src}`
        }
        // 否则直接返回，不再拼接 baseURL
        // 因为这些路径已经被父组件处理过了
        return this.src
      }
      
      // 如果有 baseURL，拼接 baseURL（仅用于相对路径）
      if (this.baseURL) {
        const base = this.baseURL.endsWith('/') ? this.baseURL.slice(0, -1) : this.baseURL
        const path = this.src.startsWith('/') ? this.src : '/' + this.src
        return base + path
      }
      
      // 否则直接返回原始路径（相对路径）
      return this.src
    }
  },
  mounted() {
    // 检查浏览器兼容性
    if ('IntersectionObserver' in window) {
      this.initObserver()
    } else {
      // 降级方案：直接加载（不懒加载）
      console.warn('[LazyImage] 浏览器不支持 IntersectionObserver，直接加载图片')
      this.loadImage()
    }
  },
  beforeUnmount() {
    // 清理观察者，防止内存泄漏
    if (this.observer) {
      this.observer.disconnect()
      this.observer = null
    }

    // 释放 Object URL（如果使用了 Blob）
    if (this.currentSrc.startsWith('blob:')) {
      URL.revokeObjectURL(this.currentSrc)
    }
  },
  watch: {
    // 监听 src 变化（支持动态切换图片）
    src(newSrc: string, oldSrc: string) {
      if (newSrc !== oldSrc) {
        // 重置状态
        this.imageLoaded = false
        this.imageError = false
        this.loadingProgress = 0
        this.currentSrc = ''

        // 如果已进入视口，重新加载
        if (this.isIntersecting) {
          this.loadImage()
        }
      }
    }
  },
  methods: {
    // ==================== 核心：Intersection Observer ====================
    /**
     * 初始化 Intersection Observer
     * 原理：
     * 1. 监听目标元素与视口（或祖先元素）的交叉状态
     * 2. 当元素进入/离开视口时触发回调
     * 3. 异步执行，不阻塞主线程，性能优于 scroll 监听
     */
    initObserver() {
      // 配置选项
      const options: IntersectionObserverInit = {
        root: null,                // null 表示相对于视口
        rootMargin: this.rootMargin, // 扩展视口范围，提前加载
        threshold: this.threshold    // 可见比例阈值
      }

      // 创建观察者实例
      this.observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
          // entry.isIntersecting: 元素是否与视口相交
          // entry.intersectionRatio: 相交比例（0-1）
          if (entry.isIntersecting) {
            console.log('[LazyImage] 图片进入视口，开始加载:', this.src)
            this.isIntersecting = true

            // 进入视口后开始加载图片
            this.loadImage()

            // 加载后停止观察（避免重复触发）
            const imageWrapper = this.$refs.imageWrapperRef as HTMLElement
            if (this.observer && imageWrapper) {
              this.observer.unobserve(imageWrapper)
            }
          }
        })
      }, options)

      // 开始观察目标元素
      const imageWrapper = this.$refs.imageWrapperRef as HTMLElement
      if (imageWrapper) {
        this.observer.observe(imageWrapper)
      }
    },

    // ==================== 图片加载逻辑 ====================
    /**
     * 使用 JavaScript 创建 Image 对象预加载
     * 优点：
     * 1. 可以监听加载事件（load/error/progress）
     * 2. 控制加载时机（而非 img 标签直接触发）
     * 3. 可以实现进度跟踪（XMLHttpRequest + Blob）
     */
    loadImage() {
      if (!this.src || this.imageLoaded) return

      const imageUrl = this.fullImageUrl
      console.log('[LazyImage] 开始加载图片:', imageUrl)

      // 如果需要加载进度，使用 XMLHttpRequest + Blob
      if (this.showProgress) {
        this.loadImageWithProgress()
      } else {
        // 简单加载：使用 Image 对象
        const img = new Image()

        // 注意：不设置 crossOrigin，避免跨域问题
        // 如果服务器支持 CORS 且需要读取图片像素数据，再启用
        // img.crossOrigin = 'anonymous'

        img.onload = () => {
          console.log('[LazyImage] Image 对象加载成功')
          this.currentSrc = imageUrl
          this.imageError = false
          this.retryCount = 0
          // 延迟一帧，确保 img 标签的 src 已更新
          requestAnimationFrame(() => {
            this.imageLoaded = true
            this.$emit('load')
          })
        }

        img.onerror = (error) => {
          console.error('[LazyImage] Image 对象加载失败:', error)
          console.error('[LazyImage] 失败的URL:', imageUrl)
          
          // 如果有默认图片且未尝试过，尝试加载默认图片
          if (this.defaultSrc && this.retryCount === 0) {
            console.log('[LazyImage] 尝试加载默认图片:', this.defaultSrc)
            this.retryCount++
            this.currentSrc = this.defaultSrc
            this.imageLoaded = true
            this.$emit('error', { useDefault: true })
          } else {
            this.imageError = true
            this.$emit('error', { useDefault: false })
          }
        }

        // 开始加载（设置 src 触发请求）
        img.src = imageUrl
      }
    },

    /**
     * 带进度的图片加载（使用 XMLHttpRequest + Blob）
     * 原理：
     * 1. 用 XHR 获取图片二进制数据（可监听进度）
     * 2. 将 Blob 转换为 Object URL
     * 3. 设置为 img.src
     */
    loadImageWithProgress() {
      const xhr = new XMLHttpRequest()
      const imageUrl = this.fullImageUrl

      // 监听下载进度
      xhr.addEventListener('progress', (event) => {
        if (event.lengthComputable) {
          const percentComplete = Math.round((event.loaded / event.total) * 100)
          this.loadingProgress = percentComplete
          console.log(`[LazyImage] 加载进度: ${percentComplete}%`)
        }
      })

      xhr.addEventListener('load', () => {
        if (xhr.status === 200) {
          // 将响应转换为 Blob
          const blob = xhr.response
          // 创建 Object URL（临时 URL）
          const objectUrl = URL.createObjectURL(blob)

          this.currentSrc = objectUrl
          this.imageError = false
          this.retryCount = 0

          requestAnimationFrame(() => {
            this.imageLoaded = true
            this.loadingProgress = 100
            this.$emit('load')
          })
        } else {
          console.error('[LazyImage] XHR 加载失败，状态码:', xhr.status)
          
          // 如果有默认图片且未尝试过，使用默认图片
          if (this.defaultSrc && this.retryCount === 0) {
            console.log('[LazyImage] 尝试加载默认图片:', this.defaultSrc)
            this.retryCount++
            this.currentSrc = this.defaultSrc
            this.imageLoaded = true
            this.$emit('error', { useDefault: true })
          } else {
            this.imageError = true
            this.$emit('error', { useDefault: false })
          }
        }
      })

      xhr.addEventListener('error', () => {
        console.error('[LazyImage] XHR 请求错误')
        
        // 如果有默认图片且未尝试过，使用默认图片
        if (this.defaultSrc && this.retryCount === 0) {
          console.log('[LazyImage] 尝试加载默认图片:', this.defaultSrc)
          this.retryCount++
          this.currentSrc = this.defaultSrc
          this.imageLoaded = true
          this.$emit('error', { useDefault: true })
        } else {
          this.imageError = true
          this.$emit('error', { useDefault: false })
        }
      })

      console.log('[LazyImage] XHR 开始请求:', imageUrl)
      xhr.open('GET', imageUrl, true)
      xhr.responseType = 'blob' // 关键：返回二进制数据
      xhr.send()
    },

    // ==================== 事件处理 ====================
    handleImageLoad() {
      console.log('[LazyImage] 图片加载完成')
      this.imageLoaded = true
      this.$emit('load')
    },

    handleImageError() {
      console.error('[LazyImage] IMG标签加载失败')
      console.error('[LazyImage] currentSrc:', this.currentSrc)
      console.error('[LazyImage] imageLoaded:', this.imageLoaded)
      console.error('[LazyImage] event:', event)
      console.log('[LazyImage] 图片加载失败')
      this.imageError = true
      this.$emit('error')
    },

    // 重试加载
    retryLoad() {
      console.log('[LazyImage] 重试加载图片')
      this.imageError = false
      this.imageLoaded = false
      this.loadingProgress = 0
      this.currentSrc = ''
      this.loadImage()
    }
  }
})
</script>

<style scoped lang="scss">
.lazy-image-wrapper {
  position: relative;
  overflow: hidden;
  background: #f5f5f5;
  border-radius: 4px;

  &.loaded {
    background: transparent;
  }

  &.error {
    background: #fff3f3;
  }
}

// 占位符样式
.placeholder {
  position: relative;
  width: 100%;
  height: 0; // 通过 padding-bottom 撑开高度
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;

  .placeholder-content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }

  .placeholder-icon {
    font-size: 32px;
    opacity: 0.3;
  }

  .loading-progress {
    font-size: 12px;
    color: #666;
    font-weight: 500;
  }
}

// 闪烁动画（骨架屏效果）
@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }

  100% {
    background-position: 200% 0;
  }
}

// 实际图片
.lazy-image {
  display: block;
  width: 100%;
  height: auto;
  opacity: 0;
  animation: fadeIn 0.3s ease-in-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

// 加载动画
.loading-spinner {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 30px;
  height: 30px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-top-color: #3498db;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

// 错误占位符
.error-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;

  .error-icon {
    font-size: 32px;
  }

  .error-text {
    font-size: 14px;
    color: #999;
  }

  .retry-btn {
    margin-top: 4px;
    padding: 4px 12px;
    background: #3498db;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 12px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #2980b9;
    }
  }
}
</style>
