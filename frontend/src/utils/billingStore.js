/**
 * 前端计费数据存储
 * 用于在前端保存每个房间的详单数据，以便随时导出
 */
class BillingStoreClass {
  constructor() {
    // 存储结构：{ roomNo: { logs: [], checkInTime: timestamp, ... } }
    this.roomData = {}
    this.simTime = 0  // ★ 新增

  }
  // ★ 新增：设置模拟时间
  setSimTime(minutes) {
    this.simTime = minutes
  }
  // ★ 新增：重置时间
  resetTime() {
    this.simTime = 0
  }
  // ★ 新增：记录事件
  recordEvent(roomNo, eventType, params = {}, timeMinute) {
    if (!this.roomData[roomNo]) {
      this.initRoom(roomNo, '')
    }
    
    const event = {
      type: eventType,
      timeMinute: timeMinute,
      timeSeconds: timeMinute * 60,
      timestamp: Date.now(),
      ...params
    }
    
    if (!this.roomData[roomNo].events) {
      this.roomData[roomNo].events = []
    }
    this.roomData[roomNo].events.push(event)
    
    console.log(`[BillingStore] 房间${roomNo} 记录事件:`, event)
  }

    
  /**
   * 初始化房间数据
   */
  initRoom(roomNo, customerName) {
    if (!this.roomData[roomNo]) {
      this.roomData[roomNo] = {
        roomNo,
        customerName,
        checkInTime: Date.now(),
        logs: [],
        totalAcFee: 0
      }
    }
  }

  /**
   * 添加详单记录
   */
  addLog(roomNo, logData) {
    if (!this.roomData[roomNo]) {
      this.initRoom(roomNo, '')
    }
    
    this.roomData[roomNo].logs.push({
      requestTimeSeconds: logData.requestTimeSeconds || 0,
      serviceStartTimeSeconds: logData.serviceStartTimeSeconds || 0,
      serviceEndTimeSeconds: logData.serviceEndTimeSeconds || 0,
      serviceDurationSeconds: logData.serviceDurationSeconds || 0,
      fanSpeed: logData.fanSpeed || 'MEDIUM',
      currentFee: logData.currentFee || 0,
      totalFee: logData.totalFee || 0
    })
    
    // 更新总费用
    this.roomData[roomNo].totalAcFee = logData.totalFee || 0
  }

  /**
   * 获取房间详单
   */
  getRoomLogs(roomNo) {
    return this.roomData[roomNo]?.logs || []
  }

  /**
   * 获取房间总空调费用
   */
  getRoomAcFee(roomNo) {
    return this.roomData[roomNo]?.totalAcFee || 0
  }

  /**
   * 导出账单数据
   */
  exportBillData(roomNo, roomInfo) {
    const logs = this.getRoomLogs(roomNo)
    const acFee = roomInfo.fee || 0
    const roomFee = (roomInfo.checkInDays || 0) * (roomInfo.dailyRoomRate || 0)
    
    return {
      roomNo,
      customerName: roomInfo.customerName || '',
      acFee,
      roomFee,
      total: acFee + roomFee,
      checkInDays: roomInfo.checkInDays || 0,
      detailLogs: logs
    }
  }

  /**
   * 清空房间数据
   */
  clearRoom(roomNo) {
    delete this.roomData[roomNo]
  }

  /**
   * 清空所有数据
   */
  clearAll() {
    this.roomData = {}
  }
}

export const BillingStore = new BillingStoreClass()
