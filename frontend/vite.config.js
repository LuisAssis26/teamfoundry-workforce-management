import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  envDir: '../',
  plugins: [react()],
  server: {
    // Garantir que estamos sempre na porta esperada e com CORS liberado para evitar respostas sem MIME
    strictPort: true,
    cors: {
      origin: "*",
      methods: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    },
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET,POST,PUT,PATCH,DELETE,OPTIONS",
    },
    proxy: {
      '/api': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false,
      },
      '/auth': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false,
      },
      '/media': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false,
      },
      '/oauth2': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
