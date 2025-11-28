import { createApp } from 'vue';
import App from './App.vue';

import { createPinia } from 'pinia';
import router from './routes';

// Element Plus 及样式
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

// 全局样式（可选）
import './styles/index.scss';

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(ElementPlus);

app.mount('#app');