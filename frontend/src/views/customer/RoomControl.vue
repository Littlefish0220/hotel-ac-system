<template>
  <div class="room-container">
    <div class="bg-layer-1"></div>
    <div class="bg-layer-2"></div>
    <div class="bg-grid"></div>

    <div class="control-wrapper" v-if="myRoom">
      <div class="header-bar">
        <div class="room-info">
          <span class="room-label">ROOM</span>
          <span class="room-number">{{ myRoom.roomNo }}</span>
        </div>
        <div class="system-status">
          <div class="status-badge" :class="myRoom.state">
            <span class="dot"></span>
            {{ getStatusText(myRoom.state) }}
          </div>
        </div>
      </div>

      <div class="thermostat-section">
        <!-- 注意：mode 这里暂时用本地变量代替，或者从 api 返回的 systemState 里取 -->
        <div class="thermostat-ring" :class="{ 'active': myRoom.state !== 'off', 'heating': currentMode === 'heat' }">
          <div class="inner-content">
            <div class="current-label">当前室温</div>
            <div class="current-temp">{{ myRoom.currentTemp }}<span class="unit">°C</span></div>
            
            <div class="target-display" v-if="myRoom.state !== 'off'">
              <span class="target-label">设定目标</span>
              <span class="target-val">{{ myRoom.targetTemp }}°</span>
            </div>
            <div class="off-display" v-else>
              OFF
            </div>
          </div>
          <div class="glow-effect" v-if="myRoom.state === 'running'"></div>
        </div>
      </div>

      <div class="stats-row glass-panel">
        <div class="stat-item">
          <el-icon :size="20" color="#11998e"><Money /></el-icon>
          <div class="stat-text">
            <span class="label">累计消费</span>
            <span class="value fee">¥ {{ (myRoom.fee || 0).toFixed(2) }}</span>
          </div>
        </div>
        <div class="divider"></div>
        <div class="stat-item">
          <el-icon :size="20" color="#409EFF"><Timer /></el-icon>
          <div class="stat-text">
            <span class="label">运行模式</span>
            <span class="value">{{ currentMode === 'cool' ? '制冷 Cool' : '制热 Heat' }}</span>
          </div>
        </div>
      </div>

      <div class="controls-area" :class="{ 'disabled': myRoom.state === 'off' }">
        
        <div class="control-group">
          <span class="group-title">温度设定</span>
          <div class="temp-btns">
            <button class="glass-btn icon-btn" @click="adjustTemp(-1)" :disabled="myRoom.state === 'off'">
              <el-icon><Minus /></el-icon>
            </button>
            <div class="temp-bar">
              <div class="progress" :style="{ width: getTempProgress + '%' }"></div>
            </div>
            <button class="glass-btn icon-btn" @click="adjustTemp(1)" :disabled="myRoom.state === 'off'">
              <el-icon><Plus /></el-icon>
            </button>
          </div>
        </div>

        <div class="control-group">
          <span class="group-title">风速调节</span>
          <div class="fan-selector">
            <div 
              class="fan-option" 
              :class="{ 'selected': myRoom.fanSpeed === 'LOW' }"
              @click="changeFan('LOW')"
            >
              <span>低</span>
              <div class="bars"><div class="b1"></div></div>
            </div>
            <div 
              class="fan-option" 
              :class="{ 'selected': myRoom.fanSpeed === 'MEDIUM' }"
              @click="changeFan('MEDIUM')"
            >
              <span>中</span>
              <div class="bars"><div class="b1"></div><div class="b2"></div></div>
            </div>
            <div 
              class="fan-option" 
              :class="{ 'selected': myRoom.fanSpeed === 'HIGH' }"
              @click="changeFan('HIGH')"
            >
              <span>高</span>
              <div class="bars"><div class="b1"></div><div class="b2"></div><div class="b3"></div></div>
            </div>
          </div>
        </div>
      </div>

      <div class="footer-action">
        <button class="power-switch" :class="{ 'on': myRoom.state !== 'off' }" @click="togglePower">
          <el-icon :size="28"><SwitchButton /></el-icon>
          <span>{{ myRoom.state !== 'off' ? '关闭空调' : '开启空调' }}</span>
        </button>
      </div>

    </div>

    <div v-else class="loading-state">
      <el-icon class="is-loading" size="40"><Loading /></el-icon>
      <p>正在连接中央控制系统...</p>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { SwitchButton, Plus, Minus, Money, Timer, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '../../api/index.js' // 【修改：引入API】

const CURRENT_ROOM_ID = sessionStorage.getItem('currentRoomId') || '101'
const myRoom = ref(null) // 【修改：响应式引用，而非计算属性】
const currentMode = ref('cool') // 本地存储系统模式

// --- 核心：数据同步 ---
const refreshData = async () => {
  try {
    const res = await api.getSystemStatus()
    if (res && res.data) {
      currentMode.value = res.data.system.mode
      const found = res.data.rooms.find(r => r.roomNo === CURRENT_ROOM_ID)
      if (found) {
        myRoom.value = found
      }
    }
  } catch (e) { console.error(e) }
}

// --- 辅助逻辑 ---
const getTempProgress = computed(() => {
  if (!myRoom.value) return 0
  const min = 16, max = 30
  const val = myRoom.value.targetTemp
  return ((val - min) / (max - min)) * 100
})

const getStatusText = (state) => {
  const map = {
    'running': '运行中 Running',
    'waiting': '排队中 Waiting',
    'standby': '保温中 Standby',
    'off': '已关机 OFF'
  }
  return map[state] || state
}

// --- 交互逻辑 (改为调用 API) ---
const togglePower = async () => {
  if (!myRoom.value) return
  const action = myRoom.value.state === 'off' ? 'powerOn' : 'powerOff'
  
  await api.controlRoom(CURRENT_ROOM_ID, { action })
  
  if (action === 'powerOn') ElMessage.success('开机请求已发送')
  else ElMessage.info('已关机')
  
  refreshData() // 立即刷新一次
}

const adjustTemp = async (delta) => {
  if (!myRoom.value) return
  let newVal = myRoom.value.targetTemp + delta
  if (newVal >= 16 && newVal <= 30) {
    await api.controlRoom(CURRENT_ROOM_ID, { targetTemp: newVal })
    refreshData()
  }
}

const changeFan = async (speed) => {
  if (myRoom.value.state === 'off') return
  await api.controlRoom(CURRENT_ROOM_ID, { fanSpeed: speed })
  refreshData()
}

// --- 初始化与轮询 ---
let timer = null
onMounted(() => {
  refreshData()
  timer = setInterval(refreshData, 1000) // 每秒同步一次状态
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
/* 样式保持不变 */
.room-container { min-height: 100vh; width: 100%; background-color: #061e18; position: relative; overflow: hidden; display: flex; justify-content: center; align-items: center; font-family: 'Helvetica Neue', Helvetica, sans-serif; color: #fff; }
.bg-layer-1 { position: absolute; top: -10%; left: -10%; width: 60vw; height: 60vw; background: radial-gradient(circle, #11998e 0%, transparent 70%); opacity: 0.2; filter: blur(80px); animation: pulse 10s infinite alternate; }
.bg-layer-2 { position: absolute; bottom: -10%; right: -10%; width: 50vw; height: 50vw; background: radial-gradient(circle, #0f2027 0%, transparent 70%); opacity: 0.3; filter: blur(80px); }
.bg-grid { position: absolute; inset: 0; background-image: linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px); background-size: 40px 40px; pointer-events: none; }
@keyframes pulse { from { opacity: 0.1; transform: scale(1); } to { opacity: 0.25; transform: scale(1.1); } }
.control-wrapper { position: relative; z-index: 10; width: 420px; max-width: 90%; background: rgba(255, 255, 255, 0.03); backdrop-filter: blur(25px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 30px; padding: 30px; box-shadow: 0 20px 50px rgba(0,0,0,0.5); display: flex; flex-direction: column; gap: 25px; }
.header-bar { display: flex; justify-content: space-between; align-items: center; }
.room-info { display: flex; flex-direction: column; }
.room-label { font-size: 10px; color: #8ebfba; letter-spacing: 2px; }
.room-number { font-size: 24px; font-weight: bold; font-family: monospace; }
.status-badge { padding: 6px 12px; border-radius: 20px; font-size: 12px; background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); display: flex; align-items: center; gap: 6px; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: #666; }
.status-badge.running { color: #67C23A; border-color: rgba(103,194,58,0.3); }
.status-badge.running .dot { background: #67C23A; box-shadow: 0 0 8px #67C23A; }
.status-badge.waiting { color: #E6A23C; }
.status-badge.waiting .dot { background: #E6A23C; animation: blink 1s infinite; }
.thermostat-section { display: flex; justify-content: center; padding: 10px 0; }
.thermostat-ring { width: 220px; height: 220px; border-radius: 50%; position: relative; background: linear-gradient(145deg, rgba(255,255,255,0.05), rgba(0,0,0,0.2)); border: 5px solid rgba(255,255,255,0.05); display: flex; justify-content: center; align-items: center; transition: all 0.5s; }
.thermostat-ring.active { border-color: rgba(17, 153, 142, 0.3); box-shadow: 0 0 30px rgba(17, 153, 142, 0.1); }
.thermostat-ring.heating.active { border-color: rgba(245, 108, 108, 0.3); box-shadow: 0 0 30px rgba(245, 108, 108, 0.1); }
.glow-effect { position: absolute; inset: -5px; border-radius: 50%; border: 2px solid transparent; border-top-color: #38ef7d; border-right-color: #11998e; animation: spin 3s linear infinite; }
.heating .glow-effect { border-top-color: #fab6b6; border-right-color: #f56c6c; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.inner-content { text-align: center; z-index: 2; }
.current-label { font-size: 12px; color: #888; margin-bottom: 5px; }
.current-temp { font-size: 56px; font-weight: 200; letter-spacing: -2px; line-height: 1; }
.unit { font-size: 24px; margin-left: 5px; color: #aaa; }
.target-display { margin-top: 10px; padding: 5px 15px; background: rgba(0,0,0,0.2); border-radius: 15px; display: inline-block;}
.target-label { font-size: 10px; color: #888; margin-right: 5px; }
.target-val { color: #38ef7d; font-weight: bold; }
.heating .target-val { color: #F56C6C; }
.off-display { font-size: 24px; color: #555; font-weight: bold; letter-spacing: 2px; }
.glass-panel { background: rgba(0, 0, 0, 0.2); border-radius: 16px; padding: 15px; display: flex; align-items: center; justify-content: space-around; }
.stat-item { display: flex; align-items: center; gap: 10px; }
.stat-text { display: flex; flex-direction: column; }
.stat-text .label { font-size: 10px; color: #888; }
.stat-text .value { font-size: 14px; font-weight: bold; }
.value.fee { color: #E6A23C; }
.divider { width: 1px; height: 30px; background: rgba(255,255,255,0.1); }
.controls-area { display: flex; flex-direction: column; gap: 20px; transition: opacity 0.3s; }
.controls-area.disabled { opacity: 0.3; pointer-events: none; }
.group-title { font-size: 12px; color: #888; margin-bottom: 8px; display: block; padding-left: 5px; }
.temp-btns { display: flex; align-items: center; gap: 15px; }
.glass-btn { width: 44px; height: 44px; border-radius: 12px; border: none; background: rgba(255,255,255,0.08); color: #fff; cursor: pointer; display: flex; justify-content: center; align-items: center; transition: all 0.2s; }
.glass-btn:active { transform: scale(0.95); background: rgba(255,255,255,0.15); }
.temp-bar { flex: 1; height: 6px; background: rgba(255,255,255,0.1); border-radius: 3px; overflow: hidden; }
.progress { height: 100%; background: linear-gradient(90deg, #11998e, #38ef7d); transition: width 0.3s; }
.fan-selector { display: flex; background: rgba(0,0,0,0.2); border-radius: 12px; padding: 4px; user-select: none; }
.fan-option { flex: 1; text-align: center; padding: 10px 0; border-radius: 8px; cursor: pointer; display: flex; flex-direction: column; align-items: center; gap: 4px; color: #888; transition: all 0.3s; }
.fan-option span { font-size: 12px; }
.bars { display: flex; gap: 2px; align-items: flex-end; height: 10px; }
.bars div { width: 3px; background: #555; border-radius: 2px; }
.b1 { height: 4px; } .b2 { height: 7px; } .b3 { height: 10px; }
.fan-option.selected { background: rgba(255,255,255,0.1); color: #fff; shadow: 0 2px 10px rgba(0,0,0,0.2); }
.fan-option.selected .bars div { background: #38ef7d; }
.footer-action { margin-top: 10px; }
.power-switch { width: 100%; padding: 18px; border-radius: 16px; border: none; cursor: pointer; font-size: 16px; font-weight: bold; letter-spacing: 1px; display: flex; align-items: center; justify-content: center; gap: 10px; background: linear-gradient(135deg, #2c3e50, #4ca1af); color: #ccc; box-shadow: 0 10px 20px rgba(0,0,0,0.3); transition: all 0.3s; }
.power-switch.on { background: linear-gradient(135deg, #11998e, #38ef7d); color: #fff; box-shadow: 0 10px 30px rgba(17, 153, 142, 0.4); }
.power-switch:active { transform: scale(0.98); }
.loading-state { text-align: center; color: #8ebfba; padding-top: 100px; }
</style>