import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'node:path';

export default defineConfig({
  base: '/bbplay-ui/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  build: {
    outDir: 'bbplay-ui'
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      '/bbplay-server': {
        target: 'http://icittest.keymesh.com.cn:11006',
        changeOrigin: true
      }
    }
  }
});
