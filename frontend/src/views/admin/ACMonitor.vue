<template>
  <div class="monitor-container">
    <div class="bg-layer-1"></div>
    <div class="bg-layer-2"></div>

    <!-- 顶部区域 -->
    <div class="header-section">
      <div class="title-box">
        <h2>系统监控中心</h2>
        <p>SYSTEM MONITORING CENTER</p>
      </div>

      <div class="test-controls">
         <div class="time-display glass-box">
            <el-icon :class="{ 'icon-spin': isTestRunning }"><Timer /></el-icon>
            <span class="label">Sim Time:</span>
            <span class="value">{{ timeMinute }} min</span>
         </div>
         
         <div class="test-action-group">
           <el-select 
             v-model="selectedScenario" 
             placeholder="选择测试场景"
             size="default"
             :disabled="isTestRunning"
             class="scenario-selector"
             popper-class="glass-select-dropdown"
           >
             <el-option label="制冷模式测试" value="cool">
               <span class="option-content">
                 <span class="option-icon">❄️</span>
                 <span>制冷模式测试</span>
               </span>
             </el-option>
             <el-option label="制热模式测试" value="heat">
               <span class="option-content">
                 <span class="option-icon">🔥</span>
                 <span>制热模式测试</span>
               </span>
             </el-option>
           </el-select>
           
           <el-button 
              :type="isTestRunning ? 'warning' : 'danger'" 
              :loading="isTestRunning" 
              @click="isTestRunning ? stopTest() : startTest()"
              size="default"
              round
              class="action-btn"
           >
              {{ isTestRunning ? '停止测试' : '启动验收测试' }}
           </el-button>
         </div>
      </div>

      <div class="header-status">
         <div class="status-indicator" :class="{ 'active': isSystemOn }">
            <div class="indicator-dot"></div>
            <span>{{ isSystemOn ? '系统在线' : '系统待机' }}</span>
         </div>
      </div>
    </div>

    <!-- 进度条 -->
    <div class="time-progress-bar" v-if="isTestRunning">
      <div class="progress-info">
        <span>下一分钟更新</span>
        <span>{{ (10 - (progressValue / 10)).toFixed(1) }}s</span>
      </div>
      <el-progress 
        :percentage="progressValue" 
        :stroke-width="6" 
        :show-text="false" 
        color="#E6A23C"
        class="custom-progress"
      />
    </div>

    <!-- 控制面板 -->
    <el-card class="control-panel glass-panel" shadow="never">
      <div class="panel-header">
        <div class="header-left">
          <el-icon :size="22" color="#11998e"><Monitor /></el-icon>
          <span class="panel-title">主控操作台</span>
        </div>
        <el-button 
          :type="isSystemOn ? 'danger' : 'primary'" 
          :color="isSystemOn ? '#F56C6C' : '#11998e'"
          size="default"
          round
          :icon="SwitchButton"
          @click="toggleSystem"
          class="power-btn"
        >
          {{ isSystemOn ? '停止主机监控' : '启动主机监控' }}
        </el-button>
      </div>

      <div class="params-row" :class="{ 'disabled': !isSystemOn }">
        <div class="param-group mode-switcher">
          <span class="label">工作模式</span>
          <div class="mode-toggle-wrapper">
            <div 
              class="mode-toggle" 
              :class="{ 'heat-active': currentMode === 'heat' }"
              @click="toggleMode"
            >
              <div class="mode-option cool" :class="{ active: currentMode === 'cool' }">
                <span class="mode-icon">❄️</span>
                <span class="mode-text">制冷</span>
              </div>
              <div class="mode-option heat" :class="{ active: currentMode === 'heat' }">
                <span class="mode-icon">🔥</span>
                <span class="mode-text">制热</span>
              </div>
              <div class="mode-slider" :class="currentMode"></div>
            </div>
            <span class="mode-hint">{{ currentMode === 'cool' ? '制冷模式' : '制热模式' }}</span>
          </div>
        </div>
        
        <div class="divider-vertical"></div>
        
        <div class="param-group">
          <span class="label">最大服务数</span>
          <el-input-number 
            v-model="maxLimit" 
            :min="1" :max="10" 
            size="default" 
            controls-position="right"
            class="glass-input"
          />
          <span class="unit">台</span>
        </div>
      </div>
    </el-card>

    <!-- 监控内容区 (支持滚动) -->
    <div class="monitor-content" :class="{ 'offline-mode': !isSystemOn }">
      <div class="dashboard-side">
        <div class="dash-card glass-panel">
          <div class="dash-icon-box green-bg">
            <el-icon :size="28"><WindPower /></el-icon>
          </div>
          <div class="dash-info">
            <div class="dash-label">服务队列</div>
            <div class="dash-num green">{{ runningCount }}</div>
          </div>
        </div>
        
        <div class="dash-card glass-panel">
          <div class="dash-icon-box yellow-bg">
            <el-icon :size="28"><Timer /></el-icon>
          </div>
          <div class="dash-info">
            <div class="dash-label">等待队列</div>
            <div class="dash-num yellow">{{ waitingCount }}</div>
          </div>
        </div>
        
        <div class="dash-card glass-panel mode-status-card">
          <div class="dash-icon-box" :class="currentMode === 'cool' ? 'cool-bg' : 'heat-bg'">
            <span class="mode-emoji">{{ currentMode === 'cool' ? '❄️' : '🔥' }}</span>
          </div>
          <div class="dash-info">
            <div class="dash-label">运行模式</div>
            <div class="dash-num" :class="currentMode === 'cool' ? 'blue' : 'red'">
              {{ currentMode === 'cool' ? '制冷' : '制热' }}
            </div>
          </div>
        </div>
      </div>

      <div class="right-content-area">
        <!-- 表格部分 -->
        <div class="table-container glass-panel">
          <div class="table-header-row">
            <span class="table-title">实时终端状态监控</span>
            <el-button size="small" type="primary" link :icon="Refresh" @click="fetchData">刷新</el-button>
          </div>

          <el-table 
            :data="rooms" 
            style="width: 100%;"
            height="350"
            :header-cell-style="headerCellStyle"
            :cell-style="cellStyle"
            :row-style="rowStyle"
          >
             <el-table-column prop="roomNo" label="房间" width="90" fixed>
              <template #default="scope">
                <span class="room-no">{{ scope.row.roomNo }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <div class="status-tag" :class="scope.row.state">
                  <span class="dot"></span>
                  <span>{{ getStatusText(scope.row.state) }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="风速" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.fanSpeed === 'HIGH'" type="danger" effect="dark" size="small">高速</el-tag>
                <el-tag v-else-if="scope.row.fanSpeed === 'MEDIUM'" type="warning" effect="dark" size="small">中速</el-tag>
                <el-tag v-else-if="scope.row.fanSpeed === 'LOW'" type="success" effect="dark" size="small">低速</el-tag>
                <span v-else class="no-data">-</span>
              </template>
            </el-table-column>
            
            <el-table-column label="初始温度" width="100">
              <template #default="scope">
                <span class="temp-value initial">{{ scope.row.initialTemp }}°</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="currentTemp" label="当前温度" width="100">
              <template #default="scope">
                <span class="temp-value current">{{ scope.row.currentTemp }}°</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="targetTemp" label="目标温度" width="100">
               <template #default="scope">
                 <span class="temp-value target">{{ scope.row.targetTemp }}°</span>
               </template>
            </el-table-column>

            <el-table-column label="入住天数" width="100">
              <template #default="scope">
                <span class="days-value">{{ scope.row.checkInDays || 0 }} 天</span>
              </template>
            </el-table-column>

            <el-table-column label="本次消费" width="110">
              <template #default="scope">
                <span class="fee-value session">¥ {{ (scope.row.sessionFee || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="累计总费" sortable min-width="110">
              <template #default="scope">
                <span class="fee-value total">¥ {{ (scope.row.fee || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="服务时长" width="100">
              <template #default="scope">
                <span class="duration-value">{{ formatDuration(scope.row.serviceDuration) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 温度变化折线图 -->
        <div class="chart-container glass-panel">
          <div class="chart-header">
            <span class="chart-title">温度变化趋势 (基于Sim Time)</span>
            <el-select 
              v-model="selectedRoomForChart" 
              placeholder="选择房间" 
              size="small"
              class="chart-room-select"
              popper-class="glass-select-dropdown"
              @change="updateChart"
            >
              <el-option 
                v-for="room in rooms" 
                :key="room.roomNo" 
                :label="room.roomNo + ' 房间'" 
                :value="room.roomNo" 
              />
            </el-select>
          </div>
          <div id="tempChart" style="width: 100%; height: 300px;"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { SwitchButton, Refresh, Monitor, WindPower, Timer } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../api/index.js' 
import { TEST_SCENARIO } from '../../api/testCases.js'
import { TEST_SCENARIO_HEAT } from '../../api/testCasesHeat.js'
import { BillingStore } from '../../utils/billingStore.js'
import * as echarts from 'echarts'

const rooms = ref([])
const isSystemOn = ref(true)
const currentMode = ref('cool')
const maxLimit = ref(3)
const timeMinute = ref(0)
const isTestRunning = ref(false)
const progressValue = ref(0)
const selectedScenario = ref('cool')

// 图表相关
const selectedRoomForChart = ref('101') 
let chartInstance = null
const roomHistoryData = reactive({}) 
let simulationTimer = null // 前端平滑动画定时器

const runningCount = computed(() => rooms.value.filter(r => r.state === 'running').length)
const waitingCount = computed(() => rooms.value.filter(r => r.state === 'waiting').length)

const currentTestScenario = computed(() => {
  return selectedScenario.value === 'heat' ? TEST_SCENARIO_HEAT : TEST_SCENARIO
})

const headerCellStyle = {
  background: 'rgba(0,0,0,0.3)',
  color: '#a6b0c2',
  borderBottom: '1px solid rgba(255,255,255,0.08)',
  fontSize: '13px',
  fontWeight: '500'
}

const cellStyle = {
  background: 'transparent',
  color: '#e5eaf3',
  borderBottom: '1px solid rgba(255,255,255,0.05)',
  fontSize: '13px'
}

const rowStyle = {
  background: 'transparent'
}

const getStatusText = (state) => { 
  const map = { 'running': '运行', 'waiting': '等待', 'standby': '待机', 'off': '关机' }
  return map[state] || state 
}

const formatDuration = (seconds) => { 
  if (!seconds || seconds <= 0) return '-'
  const mins = Math.floor(seconds / 60)
  return `${mins}分钟` 
}

// 获取数据（只更新表格，不直接推入图表数据点）
const fetchData = async () => {
  if (!isSystemOn.value) return 
  try {
    const res = await api.getSystemStatus()
    if (res && res.data) {
      rooms.value = res.data.rooms
      isSystemOn.value = res.data.system.isSystemOn
      currentMode.value = res.data.system.mode
      timeMinute.value = res.data.system.timeCounter
      
      // 初始化数据结构（如果不存在）
      rooms.value.forEach(room => {
        if (!roomHistoryData[room.roomNo]) {
          roomHistoryData[room.roomNo] = { 
            times: [], 
            currentTemps: [], 
            targetTemps: [],
            simulatedTemp: room.currentTemp 
          }
        }
      })
    }
  } catch (e) { 
    console.error(e) 
  }
}

// ★ 核心图表逻辑：在每个 Sim Time 步进时调用
// 参数 currentTime 是当前的仿真时间 (分钟)
const updateChartDataAtTimeStep = (currentTime) => {
  if (!rooms.value || rooms.value.length === 0) return

  rooms.value.forEach(room => {
    if (!roomHistoryData[room.roomNo]) {
      roomHistoryData[room.roomNo] = { times: [], currentTemps: [], targetTemps: [] }
    }
    
    const history = roomHistoryData[room.roomNo]
    // 横轴标签：T=1, T=2...
    const timeLabel = `T=${currentTime}` 
    
    // ★ 关键修改：移除历史数据长度限制，保留所有点
    // if (history.times.length > 20) { 
    //   history.times.shift()
    //   history.currentTemps.shift()
    //   history.targetTemps.shift()
    // }
    
    // 记录数据点
    history.times.push(timeLabel)
    history.currentTemps.push(room.currentTemp)
    history.targetTemps.push(room.targetTemp || null)
  })

  // 立即刷新图表
  if (chartInstance && selectedRoomForChart.value) {
    updateChart()
  }
}

const initChart = () => {
  const chartDom = document.getElementById('tempChart')
  if (!chartDom) return
  
  chartInstance = echarts.init(chartDom)
  
  const option = {
    backgroundColor: 'transparent',
    // ★ 关键修改：开启 ECharts 自带动画，时长 1000ms（与步进时间一致），线性 easing
    animation: true,
    animationDuration: 1000, 
    animationEasing: 'linear',
    
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0,0,0,0.8)',
      borderColor: '#11998e',
      textStyle: { color: '#fff' }
    },
    legend: {
      data: ['当前温度', '目标温度'],
      textStyle: { color: '#ccc' },
      top: 0
    },
    grid: {
      top: '30px', left: '3%', right: '4%', bottom: '3%', containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: [],
      axisLine: { lineStyle: { color: '#555' } },
      axisLabel: { 
        color: '#999',
        interval: 'auto', // 自动决定显示间隔，防止所有点都挤在一起
      }
    },
    yAxis: {
      type: 'value',
      scale: true, // 自动缩放
      min: (value) => Math.floor(value.min), 
      max: (value) => Math.ceil(value.max),
      axisLine: { show: false },
      axisLabel: { color: '#999', formatter: '{value} °C' },
      splitLine: { lineStyle: { color: '#333' } }
    },
    series: [
      {
        name: '当前温度',
        type: 'line',
        showSymbol: true, 
        symbolSize: 6,
        data: [],
        itemStyle: { color: '#38ef7d' },
        lineStyle: { width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(56, 239, 125, 0.3)' },
            { offset: 1, color: 'rgba(56, 239, 125, 0)' }
          ])
        }
      },
      {
        name: '目标温度',
        type: 'line',
        step: 'start', 
        showSymbol: false,
        data: [],
        itemStyle: { color: '#e6a23c' },
        lineStyle: { type: 'dashed', width: 2 }
      }
    ]
  }
  chartInstance.setOption(option)
  window.addEventListener('resize', () => chartInstance.resize())
}

const updateChart = () => {
  if (!chartInstance || !selectedRoomForChart.value) return
  
  const data = roomHistoryData[selectedRoomForChart.value]
  if (!data || data.currentTemps.length === 0) {
    chartInstance.setOption({
      xAxis: { data: [] },
      series: [{ data: [] }, { data: [] }]
    })
    return 
  }
  
  chartInstance.setOption({
    xAxis: { data: data.times },
    series: [
      { data: data.currentTemps },
      { data: data.targetTemps }
    ]
  })
}

const toggleSystem = () => {
  if (isSystemOn.value) {
    ElMessageBox.confirm('停止主机将暂停所有监控更新，是否继续？', '监控暂停', {
      confirmButtonText: '停止监控', 
      cancelButtonText: '取消', 
      type: 'warning',
    }).then(() => {
      isSystemOn.value = false
      ElMessage.warning('主机监控已暂停')
    })
  } else {
    isSystemOn.value = true
    fetchData()
    ElMessage.success('主机监控已恢复，数据已同步')
  }
}

const toggleMode = async () => {
  if (isTestRunning.value) {
    ElMessage.warning('测试运行中，无法切换模式')
    return
  }
  
  const newMode = currentMode.value === 'cool' ? 'heat' : 'cool'
  
  try {
    await ElMessageBox.confirm(
      `切换到${newMode === 'cool' ? '制冷' : '制热'}模式将重新初始化所有房间温度，是否继续？`, 
      '模式切换确认', 
      {
        confirmButtonText: '确认切换', 
        cancelButtonText: '取消', 
        type: 'warning',
      }
    )
    
    await api.changeSystemMode(newMode)
    currentMode.value = newMode
    // 清空历史数据
    for (const key in roomHistoryData) delete roomHistoryData[key]
    
    await fetchData()
    ElMessage.success(`已切换为${newMode === 'cool' ? '制冷' : '制热'}模式，房间温度已重新初始化`)
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('模式切换失败')
      console.error(e)
    }
  }
}

let testTimer = null
let progressTimer = null
const processStepsForTime = async (targetTime) => {
  console.log(`>>> Executing Actions for SimTime: ${targetTime} min`)
  
  BillingStore.setSimTime(targetTime)
  const actions = currentTestScenario.value.filter(item => item.timeOffset === targetTime)
  
  for (const act of actions) {
    if (act.action === 'checkIn') {
      await api.checkIn(act.roomNo, act.customerName)
      BillingStore.initRoom(act.roomNo, act.customerName)
    } else if (act.action === 'powerOn') {
      BillingStore.recordEvent(act.roomNo, 'powerOn', { fanSpeed: act.fanSpeed || 'MEDIUM' }, targetTime)
      await api.controlRoom(act.roomNo, { 
        action: 'powerOn', 
        targetTemp: act.targetTemp, 
        fanSpeed: act.fanSpeed 
      })
    } else if (act.action === 'powerOff') {
      BillingStore.recordEvent(act.roomNo, 'powerOff', {}, targetTime)
      await api.controlRoom(act.roomNo, { action: 'powerOff' })
    } else if (act.fanSpeed && !act.targetTemp) {
      BillingStore.recordEvent(act.roomNo, 'fanSpeedChange', { fanSpeed: act.fanSpeed }, targetTime)
      await api.controlRoom(act.roomNo, { fanSpeed: act.fanSpeed })
    } else if (act.targetTemp && !act.fanSpeed) {
      await api.controlRoom(act.roomNo, { targetTemp: act.targetTemp })
    } else if (act.targetTemp && act.fanSpeed) {
      await api.controlRoom(act.roomNo, { 
        targetTemp: act.targetTemp, 
        fanSpeed: act.fanSpeed 
      })
    }
  }
}

const maxScenarioTime = computed(() => {
  return Math.max(...currentTestScenario.value.map(item => item.timeOffset))
})

const stopTest = () => {
  isTestRunning.value = false
  if (testTimer) clearInterval(testTimer)
  if (progressTimer) clearInterval(progressTimer)
  progressValue.value = 0
  testTimer = null
  progressTimer = null
}

const startTest = async () => {
  if (isTestRunning.value) return
  isTestRunning.value = true
  progressValue.value = 0

  BillingStore.clearAll()
  BillingStore.resetTime()
  
  const testMode = selectedScenario.value === 'heat' ? 'heat' : 'cool'
  await api.changeSystemMode(testMode)
  currentMode.value = testMode
  
  // 清空图表数据
  for (const key in roomHistoryData) delete roomHistoryData[key]
  
  await fetchData()
  // ★ 初始状态记录 T=0
  updateChartDataAtTimeStep(0)
  
  let localNextTime = 0 

  ElMessage.success(`验收测试开始（${selectedScenario.value === 'heat' ? '制热模式' : '制冷模式'}），共 ${maxScenarioTime.value + 1} 分钟用例`)

  await processStepsForTime(0)
  await fetchData()

  const loop = async () => {
    localNextTime += 1
    
    if (localNextTime > maxScenarioTime.value + 1) { 
      stopTest()
      ElMessage.success({
        message: '所有测试用例执行完毕！测试已自动结束。',
        type: 'success',
        duration: 5000,
        showClose: true
      })
      return
    }
    
    console.log(`\n========== 时间推进到 t=${localNextTime} ==========`)
    await api.sendTimeTick()
    await processStepsForTime(localNextTime)
    
    // ★ 关键修改：在每个 Sim Time 步进结束时，获取最新状态并更新图表
    await fetchData()
    updateChartDataAtTimeStep(localNextTime)
    
    progressValue.value = 0
  }

  // 10秒一个循环，代表1分钟
  testTimer = setInterval(loop, 10000)
  progressTimer = setInterval(() => {
    if (progressValue.value < 100) {
      progressValue.value += 1
    }
  }, 100)
}

let autoRefreshTimer = null
onMounted(() => {
  fetchData()
  autoRefreshTimer = setInterval(fetchData, 10000) // 后台同步：10s一次
  nextTick(() => {
    initChart()
  })
})

onUnmounted(() => {
  if (testTimer) clearInterval(testTimer)
  if (progressTimer) clearInterval(progressTimer)
  if (autoRefreshTimer) clearInterval(autoRefreshTimer)
  if (simulationTimer) clearInterval(simulationTimer)
  if (chartInstance) {
    window.removeEventListener('resize', () => chartInstance.resize())
    chartInstance.dispose()
  }
})
</script>

<style scoped>
/* 基础容器：支持纵向滚动 */
.monitor-container {
  padding: 24px 32px;
  height: 100vh; /* 固定高度 */
  background: linear-gradient(135deg, #0a1f1a 0%, #061e18 100%);
  position: relative;
  overflow: hidden; /* 隐藏最外层滚动条，使用内部滚动 */
  color: #fff;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  display: flex;
  flex-direction: column;
}

/* 背景动画层 */
.bg-layer-1, .bg-layer-2 {
  position: absolute;
  border-radius: 50%;
  filter: blur(140px);
  z-index: 0;
  pointer-events: none;
  opacity: 0.08;
}

.bg-layer-1 {
  width: 65vw;
  height: 65vw;
  background: linear-gradient(135deg, #11998e, #38ef7d);
  top: -25%;
  left: -15%;
  animation: float1 30s infinite alternate ease-in-out;
}

.bg-layer-2 {
  width: 55vw;
  height: 55vw;
  background: linear-gradient(135deg, #0f2027, #2c5364);
  bottom: -25%;
  right: -15%;
  animation: float2 35s infinite alternate-reverse ease-in-out;
}

@keyframes float1 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(8vw, 4vh) scale(1.1); }
}

@keyframes float2 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(-8vw, -4vh) scale(1.1); }
}

/* 玻璃态面板 */
.glass-panel {
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border-radius: 20px;
  color: #E5EAF3;
  margin-bottom: 20px;
}

:deep(.el-card) {
  background-color: transparent;
  border: none;
  color: inherit;
}

:deep(.el-card__body) {
  background-color: transparent;
  padding: 24px;
}

/* 顶部区域 */
.header-section {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 4px;
  flex-shrink: 0;
}

.title-box h2 {
  font-size: 32px;
  margin: 0;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 2px 12px rgba(0,0,0,0.6);
  background: linear-gradient(135deg, #11998e, #38ef7d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.title-box p {
  margin: 6px 0 0;
  color: #7a9d96;
  font-size: 11px;
  letter-spacing: 3px;
  font-weight: 500;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  border-radius: 24px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.12);
  transition: all 0.3s ease;
}

.indicator-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #606266;
  transition: all 0.3s ease;
}

.status-indicator.active .indicator-dot {
  background: #67C23A;
  box-shadow: 0 0 12px #67C23A, 0 0 24px rgba(103, 194, 58, 0.4);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.status-indicator.active span {
  color: #67C23A;
  font-weight: 500;
}

/* 测试控制区 */
.test-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  z-index: 20;
}

.glass-box {
  background: rgba(0,0,0,0.35);
  padding: 8px 18px;
  border-radius: 24px;
  border: 1px solid rgba(255,255,255,0.12);
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.glass-box .label {
  color: #999;
  font-size: 13px;
  font-weight: 500;
}

.glass-box .value {
  color: #E6A23C;
  font-weight: 700;
  font-family: 'Courier New', monospace;
  font-size: 17px;
  letter-spacing: 1px;
}

.icon-spin {
  animation: rotate 2s linear infinite;
}
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.test-action-group {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(0, 0, 0, 0.2) !important;
  padding: 8px 12px;
  border-radius: 28px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}
.scenario-selector {
  width: 180px;
}
:deep(.scenario-selector .el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.25) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 18px !important;
}
:deep(.scenario-selector .el-input__wrapper:hover) {
  border-color: rgba(255, 255, 255, 0.2) !important;
  background-color: rgba(0, 0, 0, 0.35) !important;
}
:deep(.scenario-selector .el-input__wrapper.is-focused) {
  background-color: rgba(0, 0, 0, 0.4) !important;
  box-shadow: 0 0 0 1px #11998e !important;
}
:deep(.scenario-selector .el-input__inner) {
  color: #fff !important;
  font-size: 13px;
  background: transparent !important;
}
:deep(.scenario-selector .el-input__prefix) {
  display: none;
}
:deep(.scenario-selector .el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.25) !important;
}
:deep(.scenario-selector *) {
  background-color: transparent !important;
}
.option-content {
  display: flex;
  align-items: center;
  gap: 8px;
}
.option-icon {
  font-size: 16px;
}
.action-btn {
  transition: all 0.3s ease;
  font-weight: 500;
  padding: 10px 24px;
}
.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.3);
}
.time-progress-bar {
  margin-bottom: 20px;
  background: rgba(0,0,0,0.25);
  padding: 14px 24px;
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.08);
  animation: fadeIn 0.5s ease-out;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
}
.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #999;
  font-weight: 500;
}
:deep(.custom-progress .el-progress-bar__outer) {
  background-color: rgba(255,255,255,0.12) !important;
  border-radius: 10px;
}
:deep(.custom-progress .el-progress-bar__inner) {
  border-radius: 10px;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
.control-panel {
  position: relative;
  z-index: 10;
  flex-shrink: 0;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  padding-bottom: 18px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.panel-title {
  font-size: 19px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.power-btn {
  transition: all 0.3s ease;
  font-weight: 500;
  padding: 10px 24px;
}
.power-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.35);
}
.params-row {
  display: flex;
  align-items: center;
  gap: 40px;
  flex-wrap: wrap;
  transition: all 0.3s;
}
.params-row.disabled {
  opacity: 0.35;
  pointer-events: none;
  filter: grayscale(100%);
}
.param-group {
  display: flex;
  align-items: center;
  gap: 14px;
}
.label {
  color: #a6b0c2;
  font-size: 14px;
  font-weight: 500;
  min-width: 70px;
}
.unit {
  font-size: 13px;
  color: #7a8a99;
  font-weight: 500;
}
.divider-vertical {
  width: 1px;
  height: 48px;
  background: rgba(255,255,255,0.12);
}
.mode-switcher {
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}
.mode-toggle-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.mode-toggle {
  position: relative;
  display: flex;
  background: rgba(0, 0, 0, 0.3) !important;
  border-radius: 28px;
  padding: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
}
/* ★ 移除所有状态的边框和轮廓 */
.mode-toggle,
.mode-toggle:hover,
.mode-toggle:focus,
.mode-toggle:focus-visible,
.mode-toggle:focus-within,
.mode-toggle:active {
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
}
.mode-toggle:hover {
  background: rgba(0, 0, 0, 0.4) !important;
}
.mode-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border-radius: 24px;
  z-index: 1;
  transition: all 0.3s ease;
  color: #666;
  position: relative;
}
.mode-option,
.mode-option:hover,
.mode-option:focus,
.mode-option:focus-visible,
.mode-option:active {
  background: transparent !important;
  border: none !important;
  outline: none !important;
}
.mode-option.active {
  color: #fff;
}
/* ★ 通配符强制移除 */
.mode-toggle *,
.mode-toggle *:focus,
.mode-toggle *:focus-visible,
.mode-toggle *::before,
.mode-toggle *::after {
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
}
.mode-toggle-wrapper:focus,
.mode-toggle-wrapper:focus-visible {
  outline: none !important;
}
.mode-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}
.mode-option.active .mode-icon {
  transform: scale(1.1);
}
.mode-text {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.mode-slider {
  position: absolute;
  top: 5px;
  left: 5px;
  width: calc(50% - 5px);
  height: calc(100% - 10px);
  border-radius: 24px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 0;
  background: transparent !important;
  border: none !important;  
  outline: none !important;  
}
.mode-slider.cool {
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%) !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.5), 0 0 20px rgba(64, 158, 255, 0.2) !important;
  border: none !important;
}
.mode-slider.heat {
  transform: translateX(100%);
  background: linear-gradient(135deg, #F56C6C 0%, #E6A23C 100%) !important;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.5), 0 0 20px rgba(245, 108, 108, 0.2) !important;
  border: none !important;
}
.mode-hint {
  font-size: 12px;
  color: #7a8a99;
  font-weight: 500;
  padding-left: 4px;
}
:deep(.glass-input .el-input__wrapper) {
  background-color: rgba(0,0,0,0.35) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 12px;
  transition: all 0.3s ease;
}
:deep(.glass-input .el-input__wrapper:hover) {
  border-color: rgba(255,255,255,0.25);
  background-color: rgba(0,0,0,0.45) !important;
}
:deep(.glass-input .el-input__inner) {
  color: #fff !important;
  font-weight: 500;
}
:deep(.el-input-number__decrease), 
:deep(.el-input-number__increase) {
  background-color: rgba(255,255,255,0.12) !important;
  border: none !important;
  color: #a6b0c2 !important;
  transition: all 0.3s ease;
}
:deep(.el-input-number__decrease:hover), 
:deep(.el-input-number__increase:hover) {
  background-color: rgba(255,255,255,0.2) !important;
  color: #fff !important;
}

/* ★ 修改：监控内容区域，支持滚动 */
.monitor-content {
  display: flex;
  gap: 24px;
  position: relative;
  z-index: 10;
  transition: all 0.5s ease;
  overflow-y: auto; /* 核心修改：允许垂直滚动 */
  flex: 1; /* 占满剩余高度 */
  padding-bottom: 20px;
}

/* 隐藏滚动条但保留功能 */
.monitor-content::-webkit-scrollbar {
  width: 8px;
}
.monitor-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}
.monitor-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 4px;
}
.monitor-content::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.25);
}

.offline-mode {
  filter: grayscale(100%);
  opacity: 0.5;
  pointer-events: none;
  user-select: none;
}
.dashboard-side {
  flex: 0 0 280px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  position: sticky; /* 侧边栏吸顶 */
  top: 0;
  height: fit-content;
}
.dash-card {
  display: flex;
  align-items: center;
  padding: 24px;
  gap: 20px;
  transition: all 0.3s ease;
  cursor: default;
}
.dash-card:hover {
  transform: translateX(6px);
  background: rgba(255,255,255,0.08);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}
.dash-icon-box {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}
.dash-card:hover .dash-icon-box {
  transform: scale(1.05);
}
.green-bg {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.25), rgba(103, 194, 58, 0.15));
  color: #67C23A;
}
.yellow-bg {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.25), rgba(230, 162, 60, 0.15));
  color: #E6A23C;
}
.cool-bg {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.25), rgba(64, 158, 255, 0.15));
  color: #409EFF;
}
.heat-bg {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.25), rgba(245, 108, 108, 0.15));
  color: #F56C6C;
}
.dash-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.dash-label {
  font-size: 13px;
  color: #a6b0c2;
  font-weight: 500;
  letter-spacing: 0.3px;
}
.dash-num {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -1px;
}
.dash-num.green { color: #67C23A; }
.dash-num.yellow { color: #E6A23C; }
.dash-num.blue { color: #409EFF; }
.dash-num.red { color: #F56C6C; }
.mode-emoji {
  font-size: 28px;
}
.mode-status-card {
  transition: all 0.3s ease;
}

/* 右侧内容区域 */
.right-content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow: visible; /* 允许内部元素撑开，由外部 monitor-content 滚动 */
}

/* 表格容器 */
.table-container {
  flex: none; 
  height: auto;
  padding: 24px;
}

.table-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.table-title {
  font-size: 17px;
  font-weight: 600;
  border-left: 4px solid #11998e;
  padding-left: 12px;
  letter-spacing: 0.5px;
}
:deep(.el-table) {
  --el-table-border-color: rgba(255,255,255,0.08);
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
}
:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: rgba(255, 255, 255, 0.06) !important;
}
:deep(.el-table__inner-wrapper::before) {
  display: none;
}
:deep(.el-table th.el-table__cell) {
  padding: 14px 0;
}
:deep(.el-table td.el-table__cell) {
  padding: 16px 0;
}
.room-no {
  font-weight: 700;
  font-size: 17px;
  color: #11998e;
  letter-spacing: 1px;
}
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  padding: 4px 0;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.status-tag.running { color: #67C23A; }
.status-tag.running .dot {
  background: #67C23A;
  box-shadow: 0 0 8px #67C23A;
  animation: pulse-dot 2s infinite;
}
.status-tag.waiting { color: #E6A23C; }
.status-tag.waiting .dot {
  background: #E6A23C;
  animation: blink 1.5s infinite;
}
.status-tag.off { color: #909399; }
.status-tag.off .dot { background: #909399; }
.status-tag.standby { color: #409EFF; }
.status-tag.standby .dot { background: #409EFF; }
@keyframes pulse-dot {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
.temp-value {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.5px;
}
.temp-value.initial { color: #909399; }
.temp-value.current { color: #e5eaf3; }
.temp-value.target { color: #8ebfba; }
.days-value {
  color: #67C23A;
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 0.3px;
}
.fee-value {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 0.3px;
}
.fee-value.session { color: #409EFF; }
.fee-value.total { color: #E6A23C; }
.duration-value {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}
.no-data {
  color: #666;
  font-size: 13px;
}
:deep(.el-tag) {
  border: none;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 12px;
}
:deep(.el-tag--danger) {
  background: rgba(245, 108, 108, 0.2);
  color: #F56C6C;
}
:deep(.el-tag--warning) {
  background: rgba(230, 162, 60, 0.2);
  color: #E6A23C;
}
:deep(.el-tag--success) {
  background: rgba(103, 194, 58, 0.2);
  color: #67C23A;
}

/* 图表容器样式 */
.chart-container {
  flex: none; 
  height: 350px;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  border-left: 4px solid #e6a23c;
  padding-left: 12px;
  letter-spacing: 0.5px;
  color: #e5eaf3;
}

.chart-room-select {
  width: 120px;
}

:deep(.chart-room-select .el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.25) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

:deep(.chart-room-select .el-input__inner) {
  color: #fff;
}

@media (max-width: 1400px) {
  .monitor-content { flex-direction: column; }
  .dashboard-side { 
    flex-direction: row; 
    flex: none; 
    position: static; /* 小屏取消吸顶 */
  }
  .dash-card { flex: 1; }
  .right-content-area { height: auto; }
}
@media (max-width: 768px) {
  .monitor-container { padding: 16px; }
  .header-section { flex-direction: column; gap: 16px; align-items: flex-start; }
  .test-controls { flex-direction: column; width: 100%; }
  .test-action-group { width: 100%; flex-direction: column; }
  .scenario-selector { width: 100%; }
  .dashboard-side { flex-direction: column; }
}
:deep(.el-table__body-wrapper)::-webkit-scrollbar { width: 8px; height: 8px; }
:deep(.el-table__body-wrapper)::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.2); border-radius: 4px; }
:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.2); border-radius: 4px; transition: background 0.3s ease; }
:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb:hover { background: rgba(255, 255, 255, 0.3); }
</style>
<style>
/* ★ 全局样式：下拉框 */
.glass-select-dropdown.el-popper {
  background: rgba(16, 36, 30, 0.95) !important;
  backdrop-filter: blur(10px) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.6) !important;
  border-radius: 12px !important;
}
.glass-select-dropdown.el-popper .el-popper__arrow::before {
  background: rgba(16, 36, 30, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}
.glass-select-dropdown * {
  background: transparent !important;
}
.glass-select-dropdown.el-popper {
  background: rgba(16, 36, 30, 0.95) !important;
}
.glass-select-dropdown .el-select-dropdown__item {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  color: #ccc !important;
  transition: all 0.3s ease !important;
  padding: 12px 16px !important;
  margin: 2px 8px !important;
  border-radius: 8px !important;
  background: transparent !important;
}
.glass-select-dropdown .el-select-dropdown__item:hover,
.glass-select-dropdown .el-select-dropdown__item.is-hovering {
  background-color: rgba(17, 153, 142, 0.2) !important;
  color: #fff !important;
}
.glass-select-dropdown .el-select-dropdown__item.is-selected {
  color: #38ef7d !important;
  font-weight: bold !important;
  background-color: transparent !important;
}
.glass-select-dropdown .el-select-dropdown__item .option-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
}
.glass-select-dropdown .el-select-dropdown__item.is-selected::after {
  display: none;
}
</style>
