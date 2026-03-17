// 1. 导入语句必须放在文件最第一行
import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'node:url';

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 读取 .env.* 环境变量
  // 第三个参数 '' 表示读取所有以 VITE_ 开头的环境变量，不加前缀过滤
  const env = loadEnv(mode, process.cwd(), '');

  // 设置 API 代理目标地址
  const apiProxyTarget = env.VITE_API_PROXY_TARGET || 'http://192.168.101.128';

  return {
    plugins: [vue()],

    resolve: {
      alias: {
        // 配置 @ 指向 src 目录
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },

    // --- 构建配置 ---
    build: {
      sourcemap: true, // 生成 source map 文件
      // 【修正点】sourcemapIgnoreList 应该放在 build 里面，而不是 server 里面
      sourcemapIgnoreList: false,
    },

    // --- 开发服务器配置 ---
    server: {
      open: false,
      proxy: {
        '/api': {
          target: apiProxyTarget,
          changeOrigin: true, // 允许跨域
          // 如果后端路径不需要 /api 前缀，可以重写路径：
          // rewrite: (path) => path.replace(/^\/api/, '')
        },
      },
    },
  };
});