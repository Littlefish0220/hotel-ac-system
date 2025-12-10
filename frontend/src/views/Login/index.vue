<template> 
  <div class="login-container">
    <!-- 右上角重置按钮 -->
    <div class="dev-tools">
      <el-tooltip content="清除所有数据，恢复初始测试环境" placement="bottom">
        <el-button 
          type="danger" 
          circle 
          plain
          :icon="Delete" 
          @click="handleReset" 
          style="background: rgba(245, 108, 108, 0.1); border: 1px solid rgba(245, 108, 108, 0.3);"
        />
      </el-tooltip>
    </div>

    <div class="bg-layer-1"></div>
    <div class="bg-layer-2"></div>
    <div class="bg-grid"></div>

    <div class="login-box">
      <div class="login-header">
        <div class="logo-wrapper">
           <img src="../../assets/img/logo.jpg" class="login-img" alt="Logo"/>
        </div>
        <h2 class="title">BUPT 酒店管理系统</h2>
        <p class="subtitle">COMFORT & TECHNOLOGY</p>
      </div>

      <el-card class="login-card">
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          size="large"
        >
          <!-- 1. 角色选择 -->
          <el-form-item prop="role">
            <el-select
              v-model="loginForm.role"
              placeholder="请选择登录角色"
              style="width: 100%"
              class="custom-input"
              popper-class="dark-select-dropdown" 
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon"><UserFilled /></el-icon>
              </template>
              <el-option label="顾客 (Customer)" value="customer" />
              <el-option label="前台营业员 (Front Desk)" value="receptionist" />
              <el-option label="空调管理员 (AC Admin)" value="ac_admin" />
            </el-select>
          </el-form-item>

          <!-- 2. 房间选择 -->
          <el-form-item v-if="loginForm.role === 'customer'" prop="roomNo">
             <el-select
                v-model="loginForm.roomNo"
                placeholder="请选择入住房间"
                style="width: 100%"
                class="custom-input"
                popper-class="dark-select-dropdown"
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <el-icon class="input-icon"><House /></el-icon>
                </template>
                <el-option 
                  v-for="room in allRooms" 
                  :key="room.roomNo" 
                  :label="formatRoomLabel(room)" 
                  :value="room.roomNo" 
                />
              </el-select>
          </el-form-item>
          
          <!-- 3. 账号 -->
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入账号 / 手机号"
              class="custom-input"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 4. 密码 -->
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              class="custom-input"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
              round
            >
              立即登录
            </el-button>
          </el-form-item>
          
          <div class="login-footer">
            <template v-if="loginForm.role === 'customer'">
                <el-button link class="footer-link" @click="handleRegister">注册新账户</el-button>
                <span class="divider">|</span>
                <el-button link class="footer-link">忘记密码?</el-button>
            </template>
             <template v-else>
                <span class="system-tip">内部人员登录通道</span>
            </template>
          </div>
        </el-form>
      </el-card>

      <div class="copyright">
        © 2025 BUPT Hotel System. All Rights Reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, UserFilled, House, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../api/index.js'

const router = useRouter()
const loading = ref(false)
const loginFormRef = ref(null)
const allRooms = ref([])

const loginForm = reactive({
  role: 'customer',
  username: 'admin',
  password: 'admin',
  roomNo: '' 
})

const fetchRooms = async () => {
  try {
    const res = await api.getSystemStatus()
    if (res && res.data && res.data.rooms) {
      allRooms.value = res.data.rooms
    }
  } catch (e) { console.error("Failed to fetch rooms", e) }
}

const formatRoomLabel = (room) => {
  const statusMap = { 'off': '空闲', 'running': '占用 (运行)', 'waiting': '占用 (等待)', 'standby': '占用 (待机)' }
  const statusText = statusMap[room.state] || room.state
  return `${room.roomNo} - ${statusText}`
}

const loginRules = {
  role: [{ required: true, message: '请选择登录角色', trigger: 'change' }],
  roomNo: [{ required: true, message: '请选择入住房间', trigger: 'change' }],
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  if (!loginFormRef.value) return
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      setTimeout(() => {
        loading.value = false
        ElMessage.success('登录成功')
        if (loginForm.role === 'customer') {
          sessionStorage.setItem('currentRoomId', loginForm.roomNo)
          router.push('/customer/room-control')
        } else if (loginForm.role === 'receptionist') {
          router.push('/reception/roomstatus')
        } else if (loginForm.role === 'ac_admin') {
          router.push('/admin/monitor')
        }
      }, 500)
    }
  })
}

const handleRegister = () => {
  router.push('/register')
}

// 修复：调用 API 重置
const handleReset = () => {
  ElMessageBox.confirm(
    '此操作将清除所有缓存数据，重置系统到初始状态。',
    '重置系统数据',
    {
      confirmButtonText: '立即重置',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await api.resetSystem()
      ElMessage.success('系统已重置，正在刷新...')
      setTimeout(() => {
        window.location.reload()
      }, 800)
    } catch (e) {
      ElMessage.error('重置失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchRooms()
})
</script>

<style scoped>
/* 保持所有样式不变 */
.dev-tools { position: absolute; top: 20px; right: 20px; z-index: 200; }
.login-container { height: 100vh; width: 100vw; background-color: #061e18; position: relative; overflow: hidden; display: flex; justify-content: center; align-items: center; font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif; }
.bg-layer-1, .bg-layer-2 { position: absolute; border-radius: 50%; filter: blur(120px); z-index: 0; }
.bg-layer-1 { width: 60vw; height: 60vw; background: linear-gradient(135deg, #11998e, #38ef7d); opacity: 0.15; top: -10%; left: -10%; animation: move1 20s infinite alternate ease-in-out; }
.bg-layer-2 { width: 50vw; height: 50vw; background: linear-gradient(135deg, #0f2027, #2c5364); opacity: 0.2; bottom: -10%; right: -10%; animation: move2 25s infinite alternate-reverse ease-in-out; }
.bg-grid { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background-image: linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px); background-size: 50px 50px; z-index: 0; pointer-events: none; }
@keyframes move1 { 0% { transform: translate(0,0) scale(1); } 100% { transform: translate(20vw, 10vh) scale(1.1); } }
@keyframes move2 { 0% { transform: translate(0,0) scale(1); } 100% { transform: translate(-20vw, -10vh) scale(1.2); } }
.login-box { position: relative; z-index: 10; width: 400px; max-width: 90%; animation: fadeInUp 0.8s ease-out; }
.login-header { text-align: center; margin-bottom: 30px; }
.logo-wrapper { width: 100px; height: 100px; margin: 0 auto 20px; border-radius: 50%; overflow: hidden; border: 3px solid rgba(255,255,255,0.1); box-shadow: 0 0 20px rgba(17, 153, 142, 0.4); background: #000; display: flex; align-items: center; justify-content: center; }
.login-img { width: 100%; height: 100%; object-fit: cover; }
.title { color: #fff; font-size: 26px; font-weight: 600; margin: 0; text-shadow: 0 2px 10px rgba(0,0,0,0.5); letter-spacing: 1px; }
.subtitle { color: #8ebfba; font-size: 12px; margin-top: 8px; letter-spacing: 4px; text-transform: uppercase; }
.login-card { background: rgba(255, 255, 255, 0.03); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; box-shadow: 0 15px 35px rgba(0, 0, 0, 0.4); padding: 10px; }
:deep(.el-card) { border: none; } :deep(.el-card__body) { padding: 20px 30px; }
:deep(.custom-input .el-input__wrapper), :deep(.custom-input .el-select__wrapper) { background-color: rgba(0, 0, 0, 0.25) !important; box-shadow: none !important; border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 8px; padding-left: 10px; }
:deep(.custom-input .el-input__wrapper.is-focus), :deep(.custom-input .el-select__wrapper.is-focused) { background-color: rgba(0, 0, 0, 0.4) !important; box-shadow: 0 0 0 1px #11998e !important; }
:deep(.el-input__inner) { color: #fff !important; font-weight: 500; height: 44px; }
:deep(.el-select__selected-item) { color: #fff !important; }
:deep(.el-input__inner::placeholder) { color: #888; }
.input-icon { color: #a6b0c2; margin-right: 5px; }
.login-button { width: 100%; height: 44px; font-size: 16px; font-weight: bold; letter-spacing: 2px; border: none; background: linear-gradient(90deg, #11998e, #38ef7d); box-shadow: 0 4px 15px rgba(17, 153, 142, 0.3); margin-top: 10px; transition: all 0.3s; }
.login-button:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(17, 153, 142, 0.5); background: linear-gradient(90deg, #0e857b, #2dd06e); }
.login-footer { margin-top: 10px; display: flex; justify-content: center; align-items: center; font-size: 14px; }
.footer-link { color: #a6b0c2; font-weight: normal; }
.footer-link:hover { color: #fff; text-decoration: none; }
.divider { margin: 0 10px; color: rgba(255,255,255,0.1); }
.system-tip { font-size: 12px; color: #5c626e; }
.copyright { text-align: center; margin-top: 30px; font-size: 12px; color: rgba(255,255,255,0.3); }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
@media (max-width: 480px) { .login-box { width: 92%; } .title { font-size: 22px; } }
</style>

<style>
.dark-select-dropdown.el-popper { background: rgba(16, 36, 30, 0.95) !important; border: 1px solid rgba(255, 255, 255, 0.1) !important; backdrop-filter: blur(10px); }
.dark-select-dropdown.el-popper .el-popper__arrow::before { background: rgba(16, 36, 30, 0.95) !important; border: 1px solid rgba(255, 255, 255, 0.1) !important; }
.dark-select-dropdown .el-select-dropdown__item { color: #ccc !important; }
.dark-select-dropdown .el-select-dropdown__item.is-hovering, .dark-select-dropdown .el-select-dropdown__item:hover { background-color: rgba(17, 153, 142, 0.2) !important; color: #fff !important; }
.dark-select-dropdown .el-select-dropdown__item.is-selected { color: #38ef7d !important; font-weight: bold; background-color: transparent !important; }
</style>