export const TEST_SCENARIO_HEAT = [
  // ★ t=0: 所有测试房间办理入住
  { timeOffset: 0, roomNo: '101', action: 'checkIn', customerName: '孙八' },
  { timeOffset: 0, roomNo: '102', action: 'checkIn', customerName: '周九' },
  { timeOffset: 0, roomNo: '103', action: 'checkIn', customerName: '吴十' },
  { timeOffset: 0, roomNo: '104', action: 'checkIn', customerName: '郑一' },
  { timeOffset: 0, roomNo: '105', action: 'checkIn', customerName: '王二' },
  
  // 原有测试步骤
  { timeOffset: 1, roomNo: '101', action: 'powerOn' },
  { timeOffset: 2, roomNo: '101', action: 'powerOn', targetTemp: 24 },
  { timeOffset: 2, roomNo: '102', action: 'powerOn' },
  { timeOffset: 3, roomNo: '103', action: 'powerOn' },
  { timeOffset: 4, roomNo: '102', action: 'powerOn', targetTemp: 25 },
  { timeOffset: 4, roomNo: '104', action: 'powerOn' },
  { timeOffset: 4, roomNo: '105', action: 'powerOn' },
  { timeOffset: 5, roomNo: '103', action: 'powerOn', targetTemp: 27 },
  { timeOffset: 5, roomNo: '105', action: 'powerOn', fanSpeed: 'HIGH' },
  { timeOffset: 6, roomNo: '101', action: 'powerOn', fanSpeed: 'HIGH' },
  { timeOffset: 8, roomNo: '105', action: 'powerOn', targetTemp: 24 },
  { timeOffset: 10, roomNo: '101', action: 'powerOn', targetTemp: 28 },
  { timeOffset: 10, roomNo: '104', action: 'powerOn', targetTemp: 28, fanSpeed: 'HIGH' },
  { timeOffset: 12, roomNo: '105', action: 'powerOn', fanSpeed: 'MEDIUM' },
  { timeOffset: 13, roomNo: '102', action: 'powerOn', fanSpeed: 'HIGH' },
  { timeOffset: 15, roomNo: '101', action: 'powerOff' },
  { timeOffset: 15, roomNo: '103', action: 'powerOn', fanSpeed: 'LOW' },
  { timeOffset: 17, roomNo: '105', action: 'powerOff' },
  { timeOffset: 18, roomNo: '103', action: 'powerOn', fanSpeed: 'HIGH' },
  { timeOffset: 19, roomNo: '101', action: 'powerOn' },
  { timeOffset: 19, roomNo: '104', action: 'powerOn', targetTemp: 25, fanSpeed: 'MEDIUM' },
  { timeOffset: 21, roomNo: '102', action: 'powerOn', targetTemp: 27, fanSpeed: 'MEDIUM' },
  { timeOffset: 21, roomNo: '105', action: 'powerOn' },
  { timeOffset: 25, roomNo: '101', action: 'powerOff' },
  { timeOffset: 25, roomNo: '103', action: 'powerOff' },
  { timeOffset: 25, roomNo: '105', action: 'powerOff' },
  { timeOffset: 26, roomNo: '102', action: 'powerOff' },
  { timeOffset: 26, roomNo: '104', action: 'powerOff' }
]

export const HEAT_ROOM_CONFIG = {
  '101': { initialTemp: 10, price: 100 },
  '102': { initialTemp: 15, price: 125 },
  '103': { initialTemp: 18, price: 150 },
  '104': { initialTemp: 12, price: 200 },
  '105': { initialTemp: 14, price: 100 }
}
