import { createApp } from 'vue';
import App from './App.vue';

import { createPinia } from 'pinia';
import router from './router';

// Element Plus 及样式
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

// 全局样式（可选）
import './styles/index.scss';

// 自定义消息工具
import { messageSuccess, messageError, messageWarning, messageInfo } from './utils/message';

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(ElementPlus);

// 全局挂载自定义消息函数
app.config.globalProperties.$message = {
  success: messageSuccess,
  error: messageError,
  warning: messageWarning,
  info: messageInfo,
};

app.mount('#app');