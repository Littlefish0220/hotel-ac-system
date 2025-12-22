// src/api/index.js
import axios from 'axios';
import { MockApi } from './mockServer.js';

const USE_MOCK = false;
const BASE_URL = 'http://localhost:8080/api';

axios.defaults.baseURL = BASE_URL;
axios.defaults.timeout = 10000;

export const api = {
  getSystemStatus: async () => {
    if (USE_MOCK) return MockApi.getStatus();
    const response = await axios.get('/status');
    return response;
  },

  controlRoom: async (roomNo, payload) => {
    if (USE_MOCK) return MockApi.updateRoom(roomNo, payload);
    const response = await axios.post('/room/control', { roomNo, ...payload });
    return response;
  },

  sendTimeTick: async () => {
    if (USE_MOCK) return MockApi.tick();
    const response = await axios.post('/time/tick');
    return response;
  },

  resetSystem: async () => {
    if (USE_MOCK) return MockApi.reset();
    const response = await axios.post('/reset');
    return response;
  },

  changeSystemMode: async (mode) => {
    if (USE_MOCK) return { data: { code: 200, msg: '模式切换成功', mode } };
    const response = await axios.post('/system/mode', { mode });
    return response;
  },

  checkIn: async (roomNo, customerName, deposit) => {
    if (USE_MOCK) return MockApi.checkIn(roomNo, customerName);
    const response = await axios.post('/checkIn', { roomNo, customerName, deposit });
    return response;
  },

  getBillPreview: async (roomNo) => {
    if (USE_MOCK) return MockApi.getBillPreview(roomNo);
    const response = await axios.get(`/bill/preview/${roomNo}`);
    return response;
  },

  checkOut: async (roomNo) => {
    if (USE_MOCK) return MockApi.checkOut(roomNo);
    const response = await axios.post('/checkOut', { roomNo });
    return response;
  },

  getDetailLogs: async (roomNo) => {
    if (USE_MOCK) return MockApi.getDetailLogs(roomNo);
    const response = await axios.get(`/bill/details/${roomNo}`);
    return response;
  },

  exportBill: (roomNo) => {
    return axios.get(`/export/bill/${roomNo}`, { responseType: 'text' });
  },

  exportDetailCsv: (roomNo) => {
    return axios.get(`/export/detail/${roomNo}`, { responseType: 'blob' });
  },

  // 修正后的 getRealtimeBill
  getRealtimeBill: async (roomNo) => {
    if (USE_MOCK) {
      return { code: 200, data: {} };
    }
    try {
      const response = await axios.get(`/bill/realtime/${roomNo}`);
      return response.data;  // 返回 data，与 RoomStatus.vue 中期望的格式一致
    } catch (error) {
      console.error('获取实时账单失败:', error);
      throw error;
    }
  }
};
