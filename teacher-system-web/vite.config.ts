import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    proxy: {
      '/api/v1': {
        target: process.env.VITE_API_TARGET,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/v1/, '/api/v1')
      }
    }
  }
})
