import { createRouter, createWebHashHistory } from 'vue-router' 
// <!-- // 1. 引入你的登录组件
// // 假设你刚才的代码保存在 src/views/login/index.vue
// // 如果你的文件名不一样，请修改 import 路径 -->
import Login from '../views/Login/index.vue'
import RoomStatus from '../views/reception/RoomStatus.vue'
import ACMonitor from '../views/admin/ACMonitor.vue'
import RoomControl from '../views/customer/RoomControl.vue'

const routes = [
  {
    path: '/',
    redirect: '/login' // 访问根路径时，自动跳转到 /login
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/reception/roomstatus',
    name: 'RoomStatus',
    component: RoomStatus
  },
  {
  path: '/admin/monitor',
  name: 'ACMonitor',
  component: ACMonitor
  },
  {
  path: '/customer/room-control',
  name: 'RoomControl',
  component: RoomControl
  }
]

const router = createRouter({
  history: createWebHashHistory(), // 使用 HTML5 模式
  routes
})

export default router
