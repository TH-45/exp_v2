import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'node:url';

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 读取 .env.*（不加前缀过滤，便于扩展）
  const env = loadEnv(mode, process.cwd(), '');
  // 开发环境：把 /api 转发到 nginx 或网关
  // 示例：http://192.168.101.128
  const apiProxyTarget = env.VITE_API_PROXY_TARGET || 'http://192.168.101.128';

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    // 开发环境 source map 配置
    build: {
      sourcemap: true,  // 生成 source map 文件
    },
    // 开发服务器配置
    server: {
      sourcemapIgnoreList: false,  // 不忽略任何源文件
      proxy: {
        '/api': {
          target: apiProxyTarget,
          changeOrigin: true,
        },
      },
    },
  };
});

