// src/api/index.js
import axios from 'axios';
import { MockApi } from './mockServer.js';

const USE_MOCK = false;  // 使用真实后端
const BASE_URL = 'http://localhost:8080/api';

// 配置 axios 默认设置
axios.defaults.baseURL = BASE_URL;
axios.defaults.timeout = 10000;

export const api = {
  // 获取系统状态
  getSystemStatus: async () => {
    if (USE_MOCK) {
      return MockApi.getStatus();
    }
    try {
      const response = await axios.get('/status');
      return response;
    } catch (error) {
      console.error('获取系统状态失败:', error);
      throw error;
    }
  },

  // 房间控制
  controlRoom: async (roomNo, payload) => {
    if (USE_MOCK) {
      return MockApi.updateRoom(roomNo, payload);
    }
    try {
      const response = await axios.post('/room/control', { roomNo, ...payload });
      return response;
    } catch (error) {
      console.error('房间控制失败:', error);
      throw error;
    }
  },

  // 时间推进
  sendTimeTick: async () => {
    if (USE_MOCK) {
      return MockApi.tick();
    }
    try {
      const response = await axios.post('/time/tick');
      return response;
    } catch (error) {
      console.error('时间推进失败:', error);
      throw error;
    }
  },

  // 系统重置
  resetSystem: async () => {
    if (USE_MOCK) {
      return MockApi.reset();
    }
    try {
      const response = await axios.post('/reset');
      return response;
    } catch (error) {
      console.error('系统重置失败:', error);
      throw error;
    }
  },

  // 前台入住
  checkIn: async (roomNo, customerName) => {
    if (USE_MOCK) {
      return MockApi.checkIn(roomNo, customerName);
    }
    try {
      const response = await axios.post('/checkIn', { roomNo, customerName });
      return response;
    } catch (error) {
      console.error('入住办理失败:', error);
      throw error;
    }
  },

  // 获取账单预览
  getBillPreview: async (roomNo) => {
    if (USE_MOCK) {
      return MockApi.getBillPreview(roomNo);
    }
    try {
      const response = await axios.get(`/bill/preview/${roomNo}`);
      return response;
    } catch (error) {
      console.error('获取账单失败:', error);
      throw error;
    }
  },

  // 前台退房
  checkOut: async (roomNo) => {
    if (USE_MOCK) {
      return MockApi.checkOut(roomNo);
    }
    try {
      const response = await axios.post('/checkOut', { roomNo });
      return response;
    } catch (error) {
      console.error('退房失败:', error);
      throw error;
    }
  },

  // 获取详单日志
  getDetailLogs: async (roomNo) => {
    if (USE_MOCK) {
      return MockApi.getDetailLogs(roomNo);
    }
    try {
      const response = await axios.get(`/bill/details/${roomNo}`);
      return response;
    } catch (error) {
      console.error('获取详单失败:', error);
      throw error;
    }
  }
};
