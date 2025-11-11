import {createApp} from "vue";
import App from "./App.vue";
import './styles/main.scss';
import router from "./router";
import pinia from "./store";

// 引入 Element Plus
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

const app = createApp(App);

// 注册 Pinia（必须在router之前注册）
app.use(pinia);

// 注册 Element Plus
app.use(ElementPlus, {
    locale: zhCn,
});

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
}

app.use(router);

app.mount("#app");
