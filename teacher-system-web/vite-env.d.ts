/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_TARGET: string
  readonly VITE_API_PREFIX: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
