import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

function statusProxy(target: string, docsPath: string) {
  return {
    target,
    changeOrigin: true,
    rewrite: () => docsPath,
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/status/security': statusProxy('http://127.0.0.1:1008', '/mst-security-api-docs'),
      '/status/user': statusProxy('http://127.0.0.1:1009', '/mst-user-api-docs'),
      '/status/loader': statusProxy('http://127.0.0.1:1010', '/mst-loader-api-docs'),
      '/status/metric': statusProxy('http://127.0.0.1:1011', '/mst-metric-api-docs'),
      '/status/action': statusProxy('http://127.0.0.1:1012', '/mst-action-api-docs'),
      '/status/processor': statusProxy('http://127.0.0.1:1013', '/mst-processor-api-docs'),
      '/status/email': statusProxy('http://127.0.0.1:1014', '/mst-email-api-docs'),
      '/status/sms': statusProxy('http://127.0.0.1:1015', '/mst-sms-api-docs'),
      '/status/logger': statusProxy('http://127.0.0.1:1016', '/mst-logger-api-docs'),
      '/status/evaluation': statusProxy('http://127.0.0.1:1017', '/mst-evaluation-api-docs'),
      '/api/auth': {
        target: 'http://127.0.0.1:1008',
        changeOrigin: true,
      },
      '/api/user': {
        target: 'http://127.0.0.1:1009',
        changeOrigin: true,
      },
      '/api/role': {
        target: 'http://127.0.0.1:1009',
        changeOrigin: true,
      },
      '/api/metric': {
        target: 'http://127.0.0.1:1011',
        changeOrigin: true,
      },
      '/api/action': {
        target: 'http://127.0.0.1:1012',
        changeOrigin: true,
      },
      '/api': {
        target: 'http://127.0.0.1:1007',
        changeOrigin: true,
      },
    },
  },
})
