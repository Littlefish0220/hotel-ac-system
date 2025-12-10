<template>
  <div class="monitor-container">
    <div class="bg-layer-1"></div>
    <div class="bg-layer-2"></div>

    <div class="header-section">
      <div class="title-box">
        <h2>系统监控中心</h2>
        <p>SYSTEM MONITORING CENTER</p>
      </div>

      <div class="test-controls">
         <div class="time-display glass-box">
            <el-icon :class="{ 'icon-spin': isTestRunning }"><Timer /></el-icon>
            <span class="label">Sim Time: </span>
            <span class="value">{{ timeMinute }} min</span>
         </div>
         <el-button 
            :type="isTestRunning ? 'warning' : 'danger'" 
            :loading="isTestRunning" 
            @click="isTestRunning ? stopTest() : startTest()"
            round
            plain
            class="action-btn"
         >
            {{ isTestRunning ? '停止测试' : '启动验收测试' }}
         </el-button>
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
        <span>Next Minute Update</span>
        <span>{{ (10 - (progressValue / 10)).toFixed(1) }}s</span>
      </div>
      <el-progress 
        :percentage="progressValue" 
        :stroke-width="4" 
        :show-text="false" 
        color="#E6A23C"
        class="custom-progress"
      />
    </div>

    <el-card class="control-panel glass-panel" shadow="never">
      <div class="panel-header">
        <div class="header-left">
          <el-icon :size="20" color="#11998e"><Monitor /></el-icon>
          <span class="panel-title">主控操作台</span>
        </div>
        <el-button 
          :type="isSystemOn ? 'danger' : 'primary'" 
          :color="isSystemOn ? '#F56C6C' : '#11998e'"
          round
          :icon="SwitchButton"
          @click="toggleSystem"
          class="power-btn"
        >
          {{ isSystemOn ? '停止主机监控' : '启动主机监控' }}
        </el-button>
      </div>

      <div class="params-row" :class="{ 'disabled': !isSystemOn }">
        <div class="param-group">
          <span class="label">工作模式</span>
          <el-radio-group v-model="currentMode" size="small" fill="#11998e">
            <el-radio-button label="cool">制冷 Cool</el-radio-button>
            <el-radio-button label="heat">制热 Heat</el-radio-button>
          </el-radio-group>
        </div>
        <div class="divider-vertical"></div>
        <div class="param-group">
          <span class="label">最大服务数</span>
          <el-input-number 
            v-model="maxLimit" 
            :min="1" :max="10" 
            size="small" 
            controls-position="right"
            class="glass-input"
          />
          <span class="unit">台</span>
        </div>
      </div>
    </el-card>

    <div class="monitor-content" :class="{ 'offline-mode': !isSystemOn }">
      <div class="dashboard-side">
        <div class="dash-card glass-panel">
          <div class="dash-icon-box green-bg">
            <el-icon><WindPower /></el-icon>
          </div>
          <div class="dash-info">
            <div class="dash-label">服务队列 (Running)</div>
            <div class="dash-num green">{{ runningCount }}</div>
          </div>
        </div>
        <div class="dash-card glass-panel">
          <div class="dash-icon-box yellow-bg">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="dash-info">
            <div class="dash-label">等待队列 (Waiting)</div>
            <div class="dash-num yellow">{{ waitingCount }}</div>
          </div>
        </div>
      </div>

      <div class="table-container glass-panel">
        <div class="table-header-row">
          <span class="table-title">实时终端状态监控</span>
          <el-button size="small" type="primary" link :icon="Refresh" @click="fetchData">刷新</el-button>
        </div>

        <el-table 
          :data="rooms" 
          style="width: 100%; --el-table-border-color: rgba(255,255,255,0.1); --el-table-bg-color: transparent; --el-table-tr-bg-color: transparent; --el-table-header-bg-color: transparent;"
          height="450"
          :header-cell-style="{ background: 'rgba(0,0,0,0.2)', color: '#a6b0c2', borderBottom: '1px solid rgba(255,255,255,0.05)' }"
          :cell-style="{ background: 'transparent', color: '#e5eaf3', borderBottom: '1px solid rgba(255,255,255,0.05)' }"
          :row-style="{ background: 'transparent' }"
        >
           <el-table-column prop="roomNo" label="房间" width="80" fixed>
            <template #default="scope">
              <span style="font-weight: bold; font-size: 16px; color: #11998e">{{ scope.row.roomNo }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <div class="status-tag" :class="scope.row.state">
                <span class="dot"></span>
                {{ getStatusText(scope.row.state) }}
              </div>
            </template>
          </el-table-column>

          <el-table-column label="风速" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.fanSpeed === 'HIGH'" type="danger" effect="dark" size="small" color="#F56C6C" style="border: none;">高速 HIGH</el-tag>
              <el-tag v-else-if="scope.row.fanSpeed === 'MEDIUM'" type="warning" effect="dark" size="small" color="#E6A23C" style="border: none;">中速 MEDIUM</el-tag>
              <el-tag v-else-if="scope.row.fanSpeed === 'LOW'" type="success" effect="dark" size="small" color="#67C23A" style="border: none;">低速 LOW</el-tag>
              <span v-else style="color: #666">-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="currentTemp" label="室温" width="80">
            <template #default="scope"><span class="font-mono">{{ scope.row.currentTemp }}°</span></template>
          </el-table-column>
          <el-table-column prop="targetTemp" label="目标" width="80">
             <template #default="scope"><span class="font-mono" style="color: #8ebfba">{{ scope.row.targetTemp }}°</span></template>
          </el-table-column>

          <el-table-column label="入住天数" width="100">
            <template #default="scope">
              <span style="color: #67C23A; font-weight: bold;">{{ scope.row.checkInDays || 0 }} 天</span>
            </template>
          </el-table-column>

          <el-table-column label="本次消费" width="110">
            <template #default="scope"><span style="color: #409EFF;">¥ {{ (scope.row.sessionFee || 0).toFixed(2) }}</span></template>
          </el-table-column>

          <el-table-column label="累计总费" sortable min-width="110">
            <template #default="scope">
              <span style="color: #E6A23C; font-weight: bold;">¥ {{ (scope.row.fee || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="时长" width="90">
            <template #default="scope"><span style="font-size: 12px; color: #909399;">{{ formatDuration(scope.row.serviceDuration) }}</span></template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref, onMounted, onUnmounted } from 'vue'
import { SwitchButton, Refresh, Monitor, WindPower, Timer } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../api/index.js' 
import { TEST_SCENARIO } from '../../api/testCases.js' 

const rooms = ref([])
const isSystemOn = ref(true)
const currentMode = ref('cool')
const maxLimit = ref(3)
const timeMinute = ref(0)
const isTestRunning = ref(false)
const progressValue = ref(0) 

const runningCount = computed(() => rooms.value.filter(r => r.state === 'running').length)
const waitingCount = computed(() => rooms.value.filter(r => r.state === 'waiting').length)
const getStatusText = (state) => { 
  const map = { 'running': '运行', 'waiting': '等待', 'standby': '待机', 'off': '关机' }
  return map[state] || state 
}
const formatDuration = (seconds) => { 
  if (!seconds || seconds <= 0) return '-'
  const mins = Math.floor(seconds / 60)
  return `${mins}m` 
}

const fetchData = async () => {
  if (!isSystemOn.value) return 
  try {
    const res = await api.getSystemStatus()
    if (res && res.data) {
      rooms.value = res.data.rooms
      isSystemOn.value = res.data.system.isSystemOn
      currentMode.value = res.data.system.mode
      timeMinute.value = res.data.system.timeCounter
    }
  } catch (e) { 
    console.error(e) 
  }
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

let testTimer = null
let progressTimer = null

const processStepsForTime = async (targetTime) => {
  console.log(`>>> Executing Actions for SimTime: ${targetTime} min`)

  const actions = TEST_SCENARIO.filter(item => item.timeOffset === targetTime)
  for (const act of actions) {
    await api.controlRoom(act.roomNo, { 
      action: act.action, 
      targetTemp: act.targetTemp,
      fanSpeed: act.fanSpeed
    })
  }
}

// 【关键修改】计算最大测试时间
const maxScenarioTime = computed(() => {
  return Math.max(...TEST_SCENARIO.map(item => item.timeOffset))
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
  
  await fetchData()
  let localNextTime = 0 

  ElMessage.success(`验收测试开始，共 ${maxScenarioTime.value + 1} 分钟用例`)

  // t=0: 执行初始操作（开机等）
  await processStepsForTime(0)
  await fetchData()

  const loop = async () => {
      localNextTime += 1
      
      // ★ 修改：测试用例结束后1分钟自动停止（而非5分钟）
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
      await fetchData()
      progressValue.value = 0
  }


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
  autoRefreshTimer = setInterval(fetchData, 1000)
})

onUnmounted(() => {
  if (testTimer) clearInterval(testTimer)
  if (progressTimer) clearInterval(progressTimer)
  if (autoRefreshTimer) clearInterval(autoRefreshTimer)
})
</script>

<style scoped>
/* 样式与原版保持一致，略微调整布局 */
.monitor-container { padding: 30px; min-height: 100vh; background-color: #061e18; position: relative; overflow: hidden; color: #fff; font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif; }
.bg-layer-1, .bg-layer-2 { position: absolute; border-radius: 50%; filter: blur(120px); z-index: 0; pointer-events: none; }
.bg-layer-1 { width: 60vw; height: 60vw; background: linear-gradient(135deg, #11998e, #38ef7d); opacity: 0.1; top: -20%; left: -10%; animation: float1 25s infinite alternate ease-in-out; }
.bg-layer-2 { width: 50vw; height: 50vw; background: linear-gradient(135deg, #0f2027, #2c5364); opacity: 0.15; bottom: -20%; right: -10%; animation: float2 30s infinite alternate-reverse ease-in-out; }
@keyframes float1 { 0% { transform: translate(0,0); } 100% { transform: translate(10vw, 5vh); } }
@keyframes float2 { 0% { transform: translate(0,0); } 100% { transform: translate(-10vw, -5vh); } }
.glass-panel { background: rgba(255, 255, 255, 0.03); backdrop-filter: blur(16px); border: 1px solid rgba(255, 255, 255, 0.08); box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2); border-radius: 16px; color: #E5EAF3; margin-bottom: 24px; }
:deep(.el-card) { background-color: transparent; border: none; color: inherit; }
:deep(.el-card__body) { background-color: transparent; }
:deep(.glass-input .el-input__wrapper) { background-color: rgba(0,0,0,0.3) !important; box-shadow: none !important; border: 1px solid rgba(255,255,255,0.1); }
:deep(.glass-input .el-input__inner) { color: #fff !important; }
:deep(.el-input-number__decrease), :deep(.el-input-number__increase), :deep(.el-input-group__append) { background-color: rgba(255,255,255,0.1) !important; border-left: 1px solid rgba(255,255,255,0.1) !important; border-right: 1px solid rgba(255,255,255,0.1) !important; color: #a6b0c2 !important; box-shadow: none !important; }
:deep(.el-radio-button__inner) { background: rgba(0,0,0,0.3); border: 1px solid rgba(255,255,255,0.1); color: #ccc; }
:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) { background-color: #11998e; border-color: #11998e; color: white; box-shadow: none; }
.header-section { position: relative; z-index: 10; display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.title-box h2 { font-size: 28px; margin: 0; text-shadow: 0 2px 10px rgba(0,0,0,0.5); }
.title-box p { margin: 5px 0 0; color: #8ebfba; font-size: 12px; letter-spacing: 2px; }
.status-indicator { display: flex; align-items: center; gap: 8px; padding: 8px 16px; border-radius: 20px; background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); }
.indicator-dot { width: 8px; height: 8px; border-radius: 50%; background: #909399; }
.active .indicator-dot { background: #67C23A; box-shadow: 0 0 10px #67C23A; }
.active span { color: #67C23A; }
.control-panel { padding: 10px; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid rgba(255,255,255,0.05); padding-bottom: 15px; }
.header-left { display: flex; align-items: center; gap: 10px; }
.panel-title { font-size: 18px; font-weight: 500; }
.params-row { display: flex; align-items: center; gap: 30px; flex-wrap: wrap; transition: all 0.3s; }
.params-row.disabled { opacity: 0.4; pointer-events: none; filter: grayscale(100%); }
.param-group { display: flex; align-items: center; gap: 12px; }
.label { color: #a6b0c2; font-size: 14px; }
.unit { font-size: 12px; color: #606266; }
.divider-vertical { width: 1px; height: 20px; background: rgba(255,255,255,0.1); }
.monitor-content { display: flex; gap: 24px; position: relative; z-index: 10; transition: all 0.5s ease; }
.dashboard-side { flex: 1; display: flex; flex-direction: column; gap: 20px; min-width: 240px; }
.dash-card { display: flex; align-items: center; padding: 20px; gap: 20px; transition: transform 0.3s; }
.dash-card:hover { transform: translateX(5px); background: rgba(255,255,255,0.08); }
.dash-icon-box { width: 50px; height: 50px; border-radius: 12px; display: flex; justify-content: center; align-items: center; font-size: 24px; }
.green-bg { background: rgba(103, 194, 58, 0.2); color: #67C23A; }
.yellow-bg { background: rgba(230, 162, 60, 0.2); color: #E6A23C; }
.dash-info { display: flex; flex-direction: column; }
.dash-label { font-size: 12px; color: #a6b0c2; margin-bottom: 4px; }
.dash-num { font-size: 28px; font-weight: bold; }
.dash-num.green { color: #67C23A; } .dash-num.yellow { color: #E6A23C; }
.table-container { flex: 3; padding: 20px; overflow: hidden; }
.table-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.table-title { font-size: 16px; font-weight: 500; border-left: 3px solid #11998e; padding-left: 10px; }
.font-mono { font-family: monospace; }
.status-tag { display: inline-flex; align-items: center; gap: 6px; font-size: 13px; }
.dot { width: 6px; height: 6px; border-radius: 50%; }
.status-tag.running { color: #67C23A; } .status-tag.running .dot { background: #67C23A; box-shadow: 0 0 5px #67C23A; }
.status-tag.waiting { color: #E6A23C; } .status-tag.waiting .dot { background: #E6A23C; animation: blink 1.5s infinite; }
.status-tag.off { color: #909399; } .status-tag.off .dot { background: #909399; }
.status-tag.standby { color: #409EFF; } .status-tag.standby .dot { background: #409EFF; }
@keyframes blink { 50% { opacity: 0.5; } }
:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) { background-color: rgba(255, 255, 255, 0.08) !important; }
:deep(.el-table__inner-wrapper::before) { display: none; }
.offline-mode { filter: grayscale(100%); opacity: 0.6; pointer-events: none; user-select: none; }
.test-controls { display: flex; align-items: center; gap: 15px; z-index: 20; }
.glass-box { background: rgba(0,0,0,0.3); padding: 5px 15px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.1); display: flex; align-items: center; gap: 8px; }
.glass-box .label { color: #aaa; font-size: 12px; }
.glass-box .value { color: #E6A23C; font-weight: bold; font-family: monospace; font-size: 16px; }
.icon-spin { animation: rotate 2s linear infinite; }
@keyframes rotate { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.time-progress-bar { margin-bottom: 20px; background: rgba(0,0,0,0.2); padding: 10px 20px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.05); animation: fadeIn 0.5s ease-out; }
.progress-info { display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 12px; color: #888; }
:deep(.custom-progress .el-progress-bar__outer) { background-color: rgba(255,255,255,0.1) !important; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.power-btn { transition: all 0.3s ease; } .power-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.3); }
.action-btn { transition: all 0.3s ease; } .action-btn:hover { transform: scale(1.05); }
</style>
