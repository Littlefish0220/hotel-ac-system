// src/api/testCases.js

// 1. 初始化房间配置
export const ROOM_INIT_CONFIG = [
  { roomNo: '101', initialTemp: 32, currentTemp: 32, targetTemp: 25, fanSpeed: 'MEDIUM', dailyRoomRate: 100 },
  { roomNo: '102', initialTemp: 28, currentTemp: 28, targetTemp: 25, fanSpeed: 'MEDIUM', dailyRoomRate: 125 },
  { roomNo: '103', initialTemp: 30, currentTemp: 30, targetTemp: 25, fanSpeed: 'MEDIUM', dailyRoomRate: 150 },
  { roomNo: '104', initialTemp: 29, currentTemp: 29, targetTemp: 25, fanSpeed: 'MEDIUM', dailyRoomRate: 200 },
  { roomNo: '105', initialTemp: 35, currentTemp: 35, targetTemp: 25, fanSpeed: 'MEDIUM', dailyRoomRate: 100 }
]

// 2. 验收测试脚本
export const TEST_SCENARIO = [
  // ★ t=0: 所有测试房间办理入住
  { timeOffset: 0, roomNo: '101', action: 'checkIn', customerName: '张三' },
  { timeOffset: 0, roomNo: '102', action: 'checkIn', customerName: '李四' },
  { timeOffset: 0, roomNo: '103', action: 'checkIn', customerName: '王五' },
  { timeOffset: 0, roomNo: '104', action: 'checkIn', customerName: '赵六' },
  { timeOffset: 0, roomNo: '105', action: 'checkIn', customerName: '钱七' },
  
  // 原有测试步骤
  { timeOffset: 0, roomNo: '101', action: 'powerOn' },
  { timeOffset: 1, roomNo: '101', targetTemp: 18 },
  { timeOffset: 1, roomNo: '102', action: 'powerOn' },
  { timeOffset: 1, roomNo: '105', action: 'powerOn' },
  { timeOffset: 2, roomNo: '103', action: 'powerOn' },
  { timeOffset: 3, roomNo: '102', targetTemp: 19 },
  { timeOffset: 3, roomNo: '104', action: 'powerOn' },
  { timeOffset: 4, roomNo: '105', targetTemp: 22 },
  { timeOffset: 5, roomNo: '101', fanSpeed: 'HIGH' },
  { timeOffset: 6, roomNo: '102', action: 'powerOff' },
  { timeOffset: 7, roomNo: '102', action: 'powerOn' },
  { timeOffset: 7, roomNo: '105', fanSpeed: 'HIGH' },
  { timeOffset: 9, roomNo: '101', targetTemp: 22 },
  { timeOffset: 9, roomNo: '104', targetTemp: 18, fanSpeed: 'HIGH' },
  { timeOffset: 11, roomNo: '102', targetTemp: 22 },
  { timeOffset: 12, roomNo: '105', fanSpeed: 'LOW' },
  { timeOffset: 14, roomNo: '101', action: 'powerOff' },
  { timeOffset: 14, roomNo: '103', targetTemp: 24, fanSpeed: 'LOW' },
  { timeOffset: 15, roomNo: '105', targetTemp: 20, fanSpeed: 'HIGH' },
  { timeOffset: 16, roomNo: '102', action: 'powerOff' },
  { timeOffset: 17, roomNo: '103', fanSpeed: 'HIGH' },
  { timeOffset: 18, roomNo: '101', action: 'powerOn' },
  { timeOffset: 18, roomNo: '104', targetTemp: 20, fanSpeed: 'MEDIUM' },
  { timeOffset: 19, roomNo: '102', action: 'powerOn' },
  { timeOffset: 20, roomNo: '105', targetTemp: 25 },
  { timeOffset: 22, roomNo: '103', action: 'powerOff' },
  { timeOffset: 23, roomNo: '105', action: 'powerOff' },
  { timeOffset: 24, roomNo: '101', action: 'powerOff' },
  { timeOffset: 25, roomNo: '102', action: 'powerOff' },
  { timeOffset: 25, roomNo: '104', action: 'powerOff' }
]

export const TEST_METADATA = {
  totalDuration: 25,
  description: '酒店空调系统验收测试',
  version: '1.0'
}

export const getMaxTimeOffset = () => {
  return Math.max(...TEST_SCENARIO.map(item => item.timeOffset))
}
