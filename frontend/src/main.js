// src/main.js
import { createApp } from 'vue'
import './style.css'
import router from './router'
import App from './App.vue'

// 1. 引入 Element Plus
import ElementPlus from 'element-plus'
// 2. 引入 Element Plus 的 CSS 样式文件
import 'element-plus/dist/index.css'
// 3. 引入图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
app.use(router)
// 4. 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 5. 使用 Element Plus
app.use(ElementPlus)

app.mount('#app')
