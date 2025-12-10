// src/api/mockServer.js
import { ROOM_INIT_CONFIG } from './testCases.js'

const DB_KEY = 'BUPT_HOTEL_MOCK_DB_V2';
const DETAIL_LOG_KEY = 'BUPT_HOTEL_DETAIL_LOGS';

const getPriority = (fanSpeed) => {
  if (fanSpeed === 'HIGH') return 3;
  if (fanSpeed === 'MEDIUM') return 2;
  return 1;
};

const loadDB = () => {
  const data = localStorage.getItem(DB_KEY);
  if (!data) return initDB();
  return JSON.parse(data);
};

const saveDB = (data) => {
  localStorage.setItem(DB_KEY, JSON.stringify(data));
};

const loadDetailLogs = () => {
  const data = localStorage.getItem(DETAIL_LOG_KEY);
  return data ? JSON.parse(data) : {};
};

const saveDetailLogs = (logs) => {
  localStorage.setItem(DETAIL_LOG_KEY, JSON.stringify(logs));
};

const addDetailLog = (roomNo, logEntry) => {
  const logs = loadDetailLogs();
  if (!logs[roomNo]) logs[roomNo] = [];
  logs[roomNo].push(logEntry);
  saveDetailLogs(logs);
};

const clearRoomDetailLogs = (roomNo) => {
  const logs = loadDetailLogs();
  logs[roomNo] = [];
  saveDetailLogs(logs);
};

const initDB = () => {
  const defaultState = {
    system: { isSystemOn: true, mode: 'cool', timeCounter: 0, maxLimit: 3 },
    rooms: JSON.parse(JSON.stringify(ROOM_INIT_CONFIG)).map(r => ({
      ...r,
      state: 'off',
      acFee: 0,
      sessionFee: 0,
      serviceDuration: 0,
      currentRunTime: 0,
      waitDuration: 0,
      checkInDays: 0,
      totalRoomFee: 0,
      status: 'free',
      customerName: '',
      currentSessionStart: null,
      lastRequestTime: null
    }))
  };
  saveDB(defaultState);
  return defaultState;
};

const scheduler = (db) => {
  const activeRooms = db.rooms.filter(r => r.state === 'running' || r.state === 'waiting');
  if (activeRooms.length === 0) return;

  const maxServices = db.system.maxLimit;
  const runningList = db.rooms.filter(r => r.state === 'running');
  const waitingList = db.rooms.filter(r => r.state === 'waiting');

  waitingList.sort((a, b) => getPriority(b.fanSpeed) - getPriority(a.fanSpeed));

  waitingList.forEach(waiter => {
    if (runningList.length < maxServices) {
      waiter.state = 'running';
      waiter.currentRunTime = 0;
      waiter.currentSessionStart = Date.now();
      runningList.push(waiter);
    } 
    else {
      let bestCandidate = null;
      let minScore = 9999;
      runningList.forEach(runner => {
        let score = getPriority(runner.fanSpeed) * 100;
        if (runner.currentRunTime >= 2) score -= 50; 
        if (score < minScore) { minScore = score; bestCandidate = runner; }
      });
      const waiterScore = getPriority(waiter.fanSpeed) * 100;
      if (bestCandidate && waiterScore > minScore) {
        if (bestCandidate.currentSessionStart) {
          const endTime = Date.now();
          const duration = Math.floor((endTime - bestCandidate.currentSessionStart) / 1000);
          addDetailLog(bestCandidate.roomNo, {
            requestTime: bestCandidate.lastRequestTime || bestCandidate.currentSessionStart,
            serviceStartTime: bestCandidate.currentSessionStart,
            serviceEndTime: endTime,
            duration: duration,
            fanSpeed: bestCandidate.fanSpeed,
            currentFee: bestCandidate.sessionFee,
            totalFee: bestCandidate.acFee + bestCandidate.totalRoomFee,
            targetTemp: bestCandidate.targetTemp,
            startTemp: bestCandidate.currentTemp,
            endTemp: bestCandidate.currentTemp,
            reason: '被调度等待'
          });
        }
        bestCandidate.state = 'waiting';
        bestCandidate.currentRunTime = 0;
        bestCandidate.currentSessionStart = null;
        
        waiter.state = 'running';
        waiter.currentRunTime = 0;
        waiter.currentSessionStart = Date.now();
        const idx = runningList.indexOf(bestCandidate);
        if (idx > -1) runningList.splice(idx, 1);
        runningList.push(waiter);
      }
    }
  });
};

const simulateOneMinute = () => {
  const db = loadDB();
  db.system.timeCounter++;

  db.rooms.forEach(room => {
    if (room.state === 'running') {
      room.serviceDuration += 60;
      room.currentRunTime += 1; 

      let tempChangeRate = 0;
      let costPerMinute = 0;
      if (room.fanSpeed === 'HIGH') { tempChangeRate = 1.0; costPerMinute = 1.0; } 
      else if (room.fanSpeed === 'MEDIUM') { tempChangeRate = 0.5; costPerMinute = 0.5; } 
      else { tempChangeRate = 1.0 / 3.0; costPerMinute = 1.0 / 3.0; }

      room.acFee += costPerMinute;
      room.sessionFee += costPerMinute;

      const finishService = (reason) => {
        if (room.currentSessionStart) {
          const endTime = Date.now();
          const duration = Math.floor((endTime - room.currentSessionStart) / 1000);
          addDetailLog(room.roomNo, {
            requestTime: room.lastRequestTime || room.currentSessionStart,
            serviceStartTime: room.currentSessionStart,
            serviceEndTime: endTime,
            duration: duration,
            fanSpeed: room.fanSpeed,
            currentFee: room.sessionFee,
            totalFee: room.acFee + room.totalRoomFee,
            targetTemp: room.targetTemp,
            startTemp: db.system.mode === 'cool' ? room.currentTemp + (tempChangeRate * room.currentRunTime) : room.currentTemp - (tempChangeRate * room.currentRunTime),
            endTemp: room.currentTemp,
            reason: reason
          });
        }
        room.state = 'standby';
        room.currentRunTime = 0;
        room.currentSessionStart = null;
      };

      if (db.system.mode === 'cool') {
        room.currentTemp -= tempChangeRate;
        if (room.currentTemp <= room.targetTemp) {
          room.currentTemp = room.targetTemp;
          finishService('达到目标温度');
        }
      } else {
        room.currentTemp += tempChangeRate;
        if (room.currentTemp >= room.targetTemp) {
          room.currentTemp = room.targetTemp;
          finishService('达到目标温度');
        }
      }
      room.currentTemp = Math.round(room.currentTemp * 100) / 100;
    } 
    else {
      const initT = room.initialTemp;
      const recoveryRate = 0.5;
      if (room.currentTemp < initT) {
        room.currentTemp += recoveryRate;
        if (room.currentTemp > initT) room.currentTemp = initT;
      } else if (room.currentTemp > initT) {
        room.currentTemp -= recoveryRate;
        if (room.currentTemp < initT) room.currentTemp = initT;
      }
      room.currentTemp = Math.round(room.currentTemp * 100) / 100;

      if (room.state === 'standby') {
        const threshold = 1;
        if (db.system.mode === 'cool') {
          if (room.currentTemp >= room.targetTemp + threshold) {
             room.state = 'waiting';
             room.lastRequestTime = Date.now();
          }
        } else {
          if (room.currentTemp <= room.targetTemp - threshold) {
             room.state = 'waiting';
             room.lastRequestTime = Date.now();
          }
        }
      }
      if (room.state === 'waiting') room.waitDuration += 60;
    }
  });
  scheduler(db);
  saveDB(db);
};

export const MockApi = {
  reset: () => {
    localStorage.removeItem(DB_KEY);
    localStorage.removeItem(DETAIL_LOG_KEY);
    initDB();
    return Promise.resolve({ code: 200 });
  },
  
  getStatus: () => {
    const db = loadDB();
    const roomsWithTotal = db.rooms.map(r => ({
      ...r,
      fee: Math.round((r.acFee + r.totalRoomFee) * 100) / 100
    }));
    return Promise.resolve({
      data: { rooms: roomsWithTotal, system: db.system }
    });
  },

  updateRoom: (roomNo, payload) => {
    const db = loadDB();
    const room = db.rooms.find(r => r.roomNo === roomNo);
    if (room) {
      if (payload.targetTemp !== undefined) room.targetTemp = payload.targetTemp;
      if (payload.fanSpeed !== undefined) {
        room.fanSpeed = payload.fanSpeed;
        scheduler(db);
      }
      if (payload.action === 'powerOn') {
        room.state = 'waiting';
        room.sessionFee = 0;
        room.checkInDays += 1;
        room.totalRoomFee += room.dailyRoomRate;
        room.status = 'occupied';
        room.lastRequestTime = Date.now();
        if (payload.customerName) room.customerName = payload.customerName;
        scheduler(db);
      } else if (payload.action === 'powerOff') {
        if (room.currentSessionStart) {
          const endTime = Date.now();
          const duration = Math.floor((endTime - room.currentSessionStart) / 1000);
          addDetailLog(room.roomNo, {
            requestTime: room.lastRequestTime || room.currentSessionStart,
            serviceStartTime: room.currentSessionStart,
            serviceEndTime: endTime,
            duration: duration,
            fanSpeed: room.fanSpeed,
            currentFee: room.sessionFee,
            totalFee: room.acFee + room.totalRoomFee,
            targetTemp: room.targetTemp,
            startTemp: room.currentTemp,
            endTemp: room.currentTemp,
            reason: '用户关机'
          });
        }
        room.state = 'off';
        room.currentRunTime = 0;
        room.currentSessionStart = null;
        scheduler(db);
      }
      saveDB(db);
    }
    return Promise.resolve({ code: 200 });
  },

  checkIn: (roomNo, customerName) => {
    const db = loadDB();
    const room = db.rooms.find(r => r.roomNo === roomNo);
    if (room) {
      room.status = 'occupied';
      room.customerName = customerName;
      room.checkInDays += 1;
      room.totalRoomFee += room.dailyRoomRate;
      clearRoomDetailLogs(roomNo);
      saveDB(db);
    }
    return Promise.resolve({ code: 200 });
  },

  // 【新增】只获取账单预览，不清除状态
  getBillPreview: (roomNo) => {
    const db = loadDB();
    const room = db.rooms.find(r => r.roomNo === roomNo);
    if (room) {
      const detailLogs = loadDetailLogs();
      const roomLogs = detailLogs[roomNo] || [];
      const bill = {
        roomNo: room.roomNo,
        customerName: room.customerName,
        acFee: room.acFee,
        roomFee: room.totalRoomFee,
        total: room.acFee + room.totalRoomFee,
        checkInDays: room.checkInDays,
        detailLogs: roomLogs
      };
      return Promise.resolve({ code: 200, data: bill });
    }
    return Promise.resolve({ code: 404 });
  },

  // 原有checkOut：执行退房并重置
  checkOut: (roomNo) => {
    const db = loadDB();
    const room = db.rooms.find(r => r.roomNo === roomNo);
    if (room) {
      // 1. 生成账单
      const detailLogs = loadDetailLogs();
      const roomLogs = detailLogs[roomNo] || [];
      const bill = {
        roomNo: room.roomNo,
        customerName: room.customerName,
        acFee: room.acFee,
        roomFee: room.totalRoomFee,
        total: room.acFee + room.totalRoomFee,
        checkInDays: room.checkInDays,
        detailLogs: roomLogs
      };
      
      // 2. 重置房间状态
      room.status = 'free';
      room.customerName = '';
      room.state = 'off';
      room.acFee = 0;
      room.sessionFee = 0;
      room.serviceDuration = 0;
      room.checkInDays = 0;
      room.totalRoomFee = 0;
      room.currentRunTime = 0;
      room.waitDuration = 0;
      room.currentSessionStart = null;
      room.lastRequestTime = null;
      
      // 3. 清空日志并保存
      saveDB(db);
      clearRoomDetailLogs(roomNo);
      
      return Promise.resolve({ code: 200, data: bill });
    }
    return Promise.resolve({ code: 404 });
  },

  getDetailLogs: (roomNo) => {
    const logs = loadDetailLogs();
    return Promise.resolve({ code: 200, data: logs[roomNo] || [] });
  },

  tick: () => {
    simulateOneMinute();
    const db = loadDB();
    return Promise.resolve({ code: 200, time: db.system.timeCounter });
  }
};
