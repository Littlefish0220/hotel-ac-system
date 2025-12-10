// src/utils/SimulationAlgorithm.js
import { reactive, watch } from 'vue'

const STORAGE_KEY = 'hotel_ac_system_state'

// 1. 修改 defaultState：增加 maxLimit 字段
const defaultState = {
  isSystemOn: true,
  maxLimit: 3, // <--- 新增：默认允许 3 台同时运行
  mode: 'cool',
  rooms: [
    { roomNo: '101', state: 'off', currentTemp: 28, targetTemp: 22, fanSpeed: 'MEDIUM', fee: 0, sessionFee: 0, serviceDuration: 0, status: 'free' },
    { roomNo: '102', state: 'off', currentTemp: 29, targetTemp: 22, fanSpeed: 'LOW', fee: 0, sessionFee: 0, serviceDuration: 0, status: 'free' },
    { roomNo: '103', state: 'off', currentTemp: 27, targetTemp: 22, fanSpeed: 'HIGH', fee: 0, sessionFee: 0, serviceDuration: 0, status: 'free' },
    { roomNo: '104', state: 'off', currentTemp: 26, targetTemp: 22, fanSpeed: 'MEDIUM', fee: 0, sessionFee: 0, serviceDuration: 0, status: 'free' },
    { roomNo: '105', state: 'off', currentTemp: 30, targetTemp: 20, fanSpeed: 'HIGH', fee: 0, sessionFee: 0, serviceDuration: 0, status: 'free' }
  ]
}

const savedState = localStorage.getItem(STORAGE_KEY)
export const simulationState = reactive(savedState ? JSON.parse(savedState) : defaultState)

watch(simulationState, (newVal) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal))
}, { deep: true })

window.addEventListener('storage', (e) => {
  if (e.key === STORAGE_KEY && e.newValue) {
    Object.assign(simulationState, JSON.parse(e.newValue))
  }
})

export const initSimulation = (mode = 'cool') => {
  if (!localStorage.getItem(STORAGE_KEY)) {
    simulationState.mode = mode
    simulationState.maxLimit = 3
  }
}

// === 核心调度算法 ===
const scheduler = () => {
  // 1. 统计当前正在运行的房间
  const runningRooms = simulationState.rooms.filter(r => r.state === 'running')
  const waitingRooms = simulationState.rooms.filter(r => r.state === 'waiting')

  // 2. 如果 运行数 < 最大限制，且有房间在排队
  if (runningRooms.length < simulationState.maxLimit && waitingRooms.length > 0) {
    // 简单的先来后到策略：取出排队队列的第一个
    // (实际项目中这里可以根据风速优先级排序)
    const roomToStart = waitingRooms[0] 
    
    // 改变状态： 排队 -> 运行
    roomToStart.state = 'running'
  }
  
  // (可选高级功能) 如果运行数 > 最大限制 (比如管理员把3改成了1)，需要把多余的踢回等待队列
  if (runningRooms.length > simulationState.maxLimit) {
     const roomToKick = runningRooms[runningRooms.length - 1] // 踢掉最后一个
     roomToKick.state = 'waiting'
  }
}

export const simulateTick = (seconds) => {
  if (!simulationState.isSystemOn) return

  // step 1: 执行调度 (解决一直 Waiting 的问题)
  scheduler()

  // step 2: 执行物理模拟 (计费与温度)
  simulationState.rooms.forEach(room => {
    if (room.state === 'running') {
       // 模拟服务时长增加
       if (!room.serviceDuration) room.serviceDuration = 0
       room.serviceDuration += seconds

       const cost = 0.05 
       room.fee += cost
       if (room.sessionFee === undefined) room.sessionFee = 0
       room.sessionFee += cost
       
       // 温度变化
       if (simulationState.mode === 'cool' && room.currentTemp > room.targetTemp) {
         room.currentTemp = parseFloat((room.currentTemp - 0.1).toFixed(1))
       } else if (simulationState.mode === 'heat' && room.currentTemp < room.targetTemp) {
         room.currentTemp = parseFloat((room.currentTemp + 0.1).toFixed(1))
       }
    }
    // 待机或等待状态，温度稍微回升/下降（可选模拟）
    else {
        // ... 回温逻辑可省略
    }
  })
}
