import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'node:path';

export default defineConfig({
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
        target: 'http://192.168.12.54:18180',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/bbplay-server/, '/api')
      }
    }
  }
});
