import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')
  return {
    base: mode === 'production' ? '/hotel-reservation-system/' : '/',
    define: {
      __STATIC_PREVIEW__: JSON.stringify(env.VITE_STATIC_PREVIEW === 'true'),
      __LANKASTAY_API_BASE__: JSON.stringify(env.VITE_API_URL || (mode === 'development' ? 'http://localhost:8080' : '')),
    },
    plugins: [react()],
    server: {
      host: 'localhost',
      port: 5174,
      // Keep the frontend on the requested port rather than silently switching.
      strictPort: true,
      open: false,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
        },
        '/uploads': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})
