/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_TARGET: string
  readonly VITE_API_PREFIX: string
  // 更多环境变量...
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
