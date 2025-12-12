export const TEST_SCENARIO_HEAT = [
  // t=0: 入住
  { timeOffset: 0, roomNo: '101', action: 'checkIn', customerName: '孙八' },
  { timeOffset: 0, roomNo: '102', action: 'checkIn', customerName: '周九' },
  { timeOffset: 0, roomNo: '103', action: 'checkIn', customerName: '吴十' },
  { timeOffset: 0, roomNo: '104', action: 'checkIn', customerName: '郑一' },
  { timeOffset: 0, roomNo: '105', action: 'checkIn', customerName: '王二' },
  
  // t=1: 房间1开机
  { timeOffset: 1, roomNo: '101', action: 'powerOn' },
  
  // t=2: 房间1调温（不要发powerOn！），房间2开机
  { timeOffset: 2, roomNo: '101', targetTemp: 24 },  // ★ 只发调温
  { timeOffset: 2, roomNo: '102', action: 'powerOn' },
  
  // t=3: 房间3开机
  { timeOffset: 3, roomNo: '103', action: 'powerOn' },
  
  // t=4: 房间2调温，房间4/5开机
  { timeOffset: 4, roomNo: '102', targetTemp: 25 },  // ★ 只发调温
  { timeOffset: 4, roomNo: '104', action: 'powerOn' },
  { timeOffset: 4, roomNo: '105', action: 'powerOn' },
  
  // t=5: 房间3调温，房间5调速
  { timeOffset: 5, roomNo: '103', targetTemp: 28 },  // ★ 只发调温
  { timeOffset: 5, roomNo: '105', fanSpeed: 'HIGH' }, // ★ 只发调速
  
  // t=6: 房间1调速
  { timeOffset: 6, roomNo: '101', fanSpeed: 'HIGH' }, // ★ 只发调速
  
  // t=8: 房间5调温
  { timeOffset: 8, roomNo: '105', targetTemp: 24 },
  
  // t=10: 房间1调温，房间4调温+调速
  { timeOffset: 10, roomNo: '101', targetTemp: 22 },
  { timeOffset: 10, roomNo: '104', targetTemp: 21, fanSpeed: 'HIGH' },
  
  // t=12: 房间5调速
  { timeOffset: 12, roomNo: '105', fanSpeed: 'MEDIUM' },
  
  // t=13: 房间2调速
  { timeOffset: 13, roomNo: '102', fanSpeed: 'HIGH' },
  
  // t=15: 房间1关机，房间3调速
  { timeOffset: 15, roomNo: '101', action: 'powerOff' },
  { timeOffset: 15, roomNo: '103', fanSpeed: 'LOW' },
  
  // t=17: 房间5关机
  { timeOffset: 17, roomNo: '105', action: 'powerOff' },
  
  // t=18: 房间3调速
  { timeOffset: 18, roomNo: '103', fanSpeed: 'HIGH' },
  
  // t=19: 房间1重新开机，房间4调温+调速
  { timeOffset: 19, roomNo: '101', action: 'powerOn' },
  { timeOffset: 19, roomNo: '104', targetTemp: 25, fanSpeed: 'MEDIUM' },
  
  // t=21: 房间2调温+调速，房间5重新开机
  { timeOffset: 21, roomNo: '102', targetTemp: 26, fanSpeed: 'MEDIUM' },
  { timeOffset: 21, roomNo: '105', action: 'powerOn' },
  
  // t=25: 房间1/3/5关机
  { timeOffset: 25, roomNo: '101', action: 'powerOff' },
  { timeOffset: 25, roomNo: '103', action: 'powerOff' },
  { timeOffset: 25, roomNo: '105', action: 'powerOff' },
  
  // t=26: 房间2/4关机
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
