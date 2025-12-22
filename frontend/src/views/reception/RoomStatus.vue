<template>
  <div class="dashboard-container">
    <div class="bg-layer-1"></div>
    <div class="bg-layer-2"></div>

    <!-- 顶部工作台 -->
    <div class="header-section">
      <div class="title-box">
        <h2>前台工作台</h2>
        <p>RECEPTION DASHBOARD</p>
      </div>
      
      <div class="stats-cards">
        <div class="stat-card glass-panel occupied-card">
          <div class="stat-icon">
            <el-icon :size="24"><UserFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ occupiedCount }}</div>
            <div class="stat-label">当前入住</div>
          </div>
        </div>
        <div class="stat-card glass-panel free-card">
          <div class="stat-icon">
            <el-icon :size="24"><House /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ freeCount }}</div>
            <div class="stat-label">空闲可用</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 房间列表 -->
    <el-row :gutter="20" class="room-list">
      <el-col 
        v-for="room in roomsWithStatus" 
        :key="room.roomNo" 
        :xs="24" :sm="12" :md="8" :lg="6" :xl="4"
      >
        <el-card 
          class="room-card glass-panel" 
          :class="room.status"
          shadow="hover"
        >
          <div class="card-header">
            <span class="room-no">{{ room.roomNo }}</span>
            <el-tag 
              v-if="room.status === 'occupied'" 
              type="danger" 
              effect="dark" 
              round
              class="status-tag"
            >
              <el-icon><User /></el-icon>
              <span>入住中</span>
            </el-tag>
            <el-tag 
              v-else 
              type="success" 
              effect="dark" 
              round
              class="status-tag"
            >
              <el-icon><CircleCheck /></el-icon>
              <span>空闲</span>
            </el-tag>
          </div>

          <div class="card-content">
            <div class="info-row">
              <span class="label">
                <el-icon><User /></el-icon>
                顾客
              </span>
              <span class="value">{{ room.customerName || '-' }}</span>
            </div>
            
            <div class="info-row">
              <span class="label">
                <el-icon><Calendar /></el-icon>
                入住天数
              </span>
              <span class="value">{{ room.checkInDays || 0 }} 天</span>
            </div>

            <div class="info-row highlight">
              <span class="label">
                <el-icon><Wallet /></el-icon>
                当前消费
              </span>
              <span class="value fee-value">¥{{ (room.fee || 0).toFixed(2) }}</span>
            </div>

            <div class="info-row">
              <span class="label">
                <el-icon><Wallet /></el-icon>
                押金
              </span>
              <span class="value">
                ¥{{ BillingStore.getDeposit(room.roomNo) || 0 }}
              </span>
            </div>

          </div>

          <div class="card-actions">
            <!-- 入住登记按钮 -->
            <el-button 
              v-if="!room.customerName" 
              type="primary" 
              color="#11998e" 
              size="small" 
              class="action-btn"
              @click="openCheckIn(room)"
              :icon="Plus"
            >
              办理入住
            </el-button>
            
            <!-- 已入住房间的操作 -->
            <template v-else>
              <el-button 
                type="info" 
                plain 
                size="small" 
                class="action-btn"
                @click="openDetailView(room)"
                :icon="Document"
              >
                查看详单
              </el-button>
              
              <el-button 
                type="danger" 
                plain 
                size="small" 
                class="action-btn"
                @click="openCheckOut(room)"
                :icon="Finished"
              >
                结账退房
              </el-button>
            </template>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 入住弹窗 -->
    <el-dialog 
      v-model="checkInVisible" 
      width="420px" 
      align-center
      class="custom-dialog checkin-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="dialog-header-stylized">
          <el-icon><UserFilled /></el-icon>
          <span>登记入住 Check-In</span>
        </div>
      </template>

      <div class="dialog-body-content">
        <el-form :model="checkInForm" label-position="top" class="check-in-form">
          <el-form-item label="房间号 / Room No.">
            <div class="room-display-box">
              <el-icon><House /></el-icon>
              <span>{{ checkInForm.roomNo }}</span>
            </div>
          </el-form-item>
          <el-form-item label="顾客姓名 / Guest Name" required>
            <el-input 
              v-model="checkInForm.name" 
              placeholder="请输入顾客姓名"
              class="styled-input"
              clearable
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="押金金额 / Deposit">
            <el-input-number
              v-model="checkInForm.deposit"
              :min="0"
              :step="50"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
          <div class="price-notice">
            <div class="notice-label">当前房价</div>
            <div class="notice-value">¥{{ currentRoomRate }} <small>/天</small></div>
          </div>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer-bar">
          <el-button @click="checkInVisible = false" plain>取消</el-button>
          <el-button 
            type="primary" 
            color="#11998e" 
            @click="submitCheckIn"
            :icon="Check"
          >
            确认办理
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 退房弹窗 (集成饼图) -->
    <el-dialog 
      v-model="checkOutVisible" 
      width="850px" 
      align-center
      class="custom-dialog detail-dialog"
      :show-close="false"
      @opened="initPieChart"
    >
      <template #header>
        <div class="dialog-header-stylized">
          <span>账单结算 Bill Settlement</span>
        </div>
      </template>

      <div class="detail-content" v-if="currentBill.roomNo">
        <!-- 汇总区域：左侧信息 + 右侧饼图 -->
        <div class="summary-section-wrapper">
          <!-- 左侧：文字信息 -->
          <div class="summary-card left-card">
            <div class="info-grid">
              <div class="info-item">
                <span class="label">房间号：</span>
                <span class="val highlight">{{ currentBill.roomNo }}</span>
              </div>
              <div class="info-item">
                <span class="label">顾客：</span>
                <span class="val">{{ currentBill.customerName }}</span>
              </div>
              <div class="info-item">
                <span class="label">打印时间：</span>
                <span class="val">{{ new Date().toLocaleString() }}</span>
              </div>
              <div class="info-item">
                <span class="label">入住天数：</span>
                <span class="val">{{ currentBill.checkInDays }} 天</span>
              </div>
            </div>

            <div class="cost-grid">
              <div class="cost-item">
                <div class="cost-label">空调费用</div>
                <div class="cost-value">¥{{ currentBill.acFee.toFixed(2) }}</div>
              </div>
              <div class="cost-item">
                <div class="cost-label">住宿费用</div>
                <div class="cost-value">¥{{ currentBill.roomFee.toFixed(2) }}</div>
              </div>
              <div class="cost-item">
                <div class="cost-label">押金</div>
                <div class="cost-value">¥{{ currentBill.deposit.toFixed(2) }}</div>
              </div>
              <div class="cost-item total">
                <div class="cost-label">
                  {{ currentBill.refund >= 0 ? '应退金额' : '需补金额' }}
                </div>
                <div class="cost-value">
                  ¥{{ Math.abs(currentBill.refund).toFixed(2) }}
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：饼图容器 -->
          <div class="pie-chart-card">
            <div id="billPieChart" style="width: 100%; height: 100%;"></div>
          </div>
        </div>
        
        <!-- 表格区域 -->
        <div class="table-area">
          <el-table 
            :data="currentBill.detailLogs" 
            height="300"
            style="width: 100%"
            class="custom-table"
            :header-cell-style="{ background: '#7ba1a1', color: '#fff', fontWeight: 'normal' }"
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="requestTimeSeconds" label="请求时间(s)" align="center" />
            <el-table-column prop="serviceStartTimeSeconds" label="开始时间(s)" align="center" />
            <el-table-column prop="serviceEndTimeSeconds" label="结束时间(s)" align="center" />
            <el-table-column prop="serviceDurationSeconds" label="时长(秒)" align="center" />
            <el-table-column prop="fanSpeed" label="风速" align="center" />
            <el-table-column prop="currentFee" label="本段费用(元)" align="right">
              <template #default="{ row }">¥{{ row.currentFee.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="totalFee" label="累积费用(元)" align="right">
              <template #default="{ row }">¥{{ row.totalFee.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
          
          <div v-if="!currentBill.detailLogs || currentBill.detailLogs.length === 0" class="empty-placeholder">
            <el-icon :size="40"><Document /></el-icon>
            <p>暂无空调使用记录</p>
          </div>
        </div>
      </div>
      
      <!-- 底部栏 -->
      <template #footer>
        <div class="dialog-footer-bar full-width">
          <div class="left-actions">
            <span class="action-label">导出格式：</span>
            <el-button-group>
              <el-button size="small" @click="exportDetail('txt')">TXT</el-button>
              <el-button size="small" @click="exportDetail('excel')">Excel</el-button>
              <el-button size="small" @click="exportDetail('pdf')">PDF</el-button>
            </el-button-group>
          </div>
          <div class="right-actions">
            <el-button @click="checkOutVisible = false">取消</el-button>
            <el-button 
              type="danger" 
              @click="submitCheckOut"
              :icon="Finished"
            >
              确认退房并清空
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 详单查看弹窗 -->
    <el-dialog 
      v-model="detailViewVisible" 
      width="850px" 
      align-center
      class="custom-dialog detail-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="dialog-header-stylized">
          <span>空调使用详单 Detail Report</span>
        </div>
      </template>

      <div class="detail-content">
        <!-- 汇总卡片 -->
        <div class="summary-card">
          <div class="info-grid">
            <div class="info-item">
              <span class="label">房间号：</span>
              <span class="val highlight">{{ currentDetail.roomNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">顾客：</span>
              <span class="val">{{ currentDetail.customerName }}</span>
            </div>
            <div class="info-item">
              <span class="label">入住时间：</span>
              <span class="val">{{ currentDetail.checkInTime || '未记录' }}</span>
            </div>
            <div class="info-item">
              <span class="label">入住天数：</span>
              <span class="val">{{ currentDetail.checkInDays }} 天</span>
            </div>
          </div>

          <div class="cost-grid">
            <div class="cost-item">
              <div class="cost-label">空调费用：</div>
              <div class="cost-value">¥{{ currentDetail.acFee.toFixed(2) }}</div>
            </div>
            <div class="cost-item">
              <div class="cost-label">住宿费用：</div>
              <div class="cost-value">¥{{ currentDetail.roomFee.toFixed(2) }}</div>
            </div>
            <div class="cost-item total">
              <div class="cost-label">总计：</div>
              <div class="cost-value">¥{{ currentDetail.total.toFixed(2) }}</div>
            </div>
          </div>
        </div>
        
        <!-- 表格 -->
        <div class="table-area">
          <el-table 
            :data="currentDetail.detailLogs" 
            height="350"
            style="width: 100%"
            class="custom-table"
            :header-cell-style="{ background: '#7ba1a1', color: '#fff', fontWeight: 'normal', borderRight: '1px solid rgba(255,255,255,0.2)' }"
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="requestTimeSeconds" label="请求时间(s)" align="center" />
            <el-table-column prop="serviceStartTimeSeconds" label="开始时间(s)" align="center" />
            <el-table-column prop="serviceEndTimeSeconds" label="结束时间(s)" align="center" />
            <el-table-column prop="serviceDurationSeconds" label="时长(秒)" align="center" />
            <el-table-column prop="fanSpeed" label="风速" align="center">
              <template #default="{ row }">
                {{ row.fanSpeed }}
              </template>
            </el-table-column>
            <el-table-column prop="currentFee" label="本段费用(元)" align="center">
              <template #default="{ row }">¥{{ row.currentFee.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="totalFee" label="累积费用(元)" align="center">
              <template #default="{ row }">¥{{ row.totalFee.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
          
          <div v-if="!currentDetail.detailLogs || currentDetail.detailLogs.length === 0" class="empty-placeholder">
            <div class="empty-icon-box">📦</div>
            <p>暂无空调使用记录</p>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer-bar full-width">
          <div class="left-actions">
            <span class="action-label">导出格式：</span>
            <el-button-group>
              <el-button size="default" :icon="Document" @click="exportDetailFromView('txt')">TXT</el-button>
              <el-button size="default" :icon="Document" @click="exportDetailFromView('excel')">Excel</el-button>
              <el-button size="default" :icon="Printer" @click="exportDetailFromView('pdf')">PDF</el-button>
            </el-button-group>
          </div>
          
          <div class="right-actions">
            <el-button size="default" @click="detailViewVisible = false">关闭</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { 
  UserFilled, House, User, Calendar, Wallet, Plus, Finished, 
  Check, Document, Refrigerator, Printer, CircleCheck 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '../../api/index.js'
import { exportToTXT, exportToExcel, exportToPDF } from '../../utils/exportUtils.js'
import { BillingStore } from '../../utils/billingStore.js'
import * as echarts from 'echarts'

const rooms = ref([])

const roomsWithStatus = computed(() => {
  return rooms.value.map(room => {
    const isOccupied = room.status === 'occupied' || 
                       room.occupied === true || 
                       (room.customerName && room.customerName.trim() !== '')
    return {
      ...room,
      status: isOccupied ? 'occupied' : 'free'
    }
  })
})

const checkInVisible = ref(false)
const checkOutVisible = ref(false)
const selectedRoom = ref(null)

//const checkInForm = reactive({ roomNo: '', name: '' })
const checkInForm = reactive({ roomNo: '', name: '', deposit: 300})

const currentBill = reactive({ 
  roomNo: '', 
  customerName: '',
  acFee: 0, 
  roomFee: 0,
  total: 0,
  checkInDays: 0,
  detailLogs: [],
  deposit: 0,
  refund: 0 
})

const detailViewVisible = ref(false)
const currentDetail = reactive({
  roomNo: '',
  customerName: '',
  acFee: 0,
  roomFee: 0,
  total: 0,
  checkInDays: 0,
  checkInTime: '',
  detailLogs: []
})

let pieChartInstance = null

const occupiedCount = computed(() => 
  roomsWithStatus.value.filter(r => r.status === 'occupied').length
)
const freeCount = computed(() => 
  roomsWithStatus.value.filter(r => r.status !== 'occupied').length
)
const currentRoomRate = computed(() => 
  selectedRoom.value?.dailyRoomRate || 0
)

const fetchRooms = async () => {
  try {
    const res = await api.getSystemStatus()
    const data = res.data || res
    if (data && data.rooms) {
      rooms.value = data.rooms
    }
  } catch (e) { 
    console.error('[前台] 获取数据失败:', e) 
  }
}

const openCheckIn = (room) => {
  selectedRoom.value = room
  checkInForm.roomNo = room.roomNo
  checkInForm.name = ''
  checkInVisible.value = true
}

const submitCheckIn = async () => {
  if (!checkInForm.name.trim()) {
    ElMessage.warning('请输入顾客姓名')
    return
  }
  try {
    await api.checkIn(selectedRoom.value.roomNo, checkInForm.name, checkInForm.deposit)
    // 登记押金
    BillingStore.setDeposit(
      selectedRoom.value.roomNo,
      checkInForm.deposit
    )
    checkInVisible.value = false
    ElMessage.success(`房间 ${selectedRoom.value.roomNo} 入住成功，已收押金 ¥${checkInForm.deposit}`)
    fetchRooms()
  } catch (e) { 
    console.error('[入住] 失败:', e)
    ElMessage.error('入住办理失败') 
  }
}

const openDetailView = async (room) => {
  try {
    const res = await api.getRealtimeBill(room.roomNo)
    if (res.code === 200 && res.data) {
      Object.assign(currentDetail, res.data)
      detailViewVisible.value = true
    } else {
      ElMessage.warning('暂无详单数据')
    }
  } catch (e) {
    console.error('[详单] 获取失败:', e)
    ElMessage.error('获取详单失败')
  }
}

const exportDetailFromView = (format) => {
  try {
    if (format === 'txt') {
      exportToTXT(currentDetail)
      ElMessage.success('TXT账单导出成功')
    } else if (format === 'excel') {
      exportToExcel(currentDetail)
      ElMessage.success('Excel详单导出成功')
    } else if (format === 'pdf') {
      const loading = ElMessage({
        message: '正在生成PDF，请稍候...',
        type: 'info',
        duration: 0
      })
      
      exportToPDF(currentDetail)
      
      setTimeout(() => {
        loading.close()
        ElMessage.success('PDF账单导出成功')
      }, 1500)
    }
  } catch (e) {
    console.error('[导出] 失败:', e)
    ElMessage.error('导出失败')
  }
}

const openCheckOut = async (room) => {
  selectedRoom.value = room
  try {
    const response = await api.getBillPreview(room.roomNo)
    const res = response.data || response
    if (res.code === 200 && res.data) {
      const bill = res.data
      Object.assign(currentBill, {
        roomNo: bill.roomNo,
        customerName: bill.customerName || '未知',
        acFee: parseFloat(bill.acFee) || 0,
        roomFee: parseFloat(bill.roomFee) || 0,
        total: parseFloat(bill.total) || 0,
        deposit: parseFloat(bill.deposit),
        refund: parseFloat(bill.refund),
        checkInDays: bill.days || bill.checkInDays || 0,
        detailLogs: bill.detailLogs || []
      })
      checkOutVisible.value = true
    } else {
      ElMessage.error(res.msg || '无法获取账单详情')
    }
  } catch (e) {
    console.error('[退房异常]', e)
    ElMessage.error('请求失败，请检查后端服务')
  }
}

const initPieChart = () => {
  const chartDom = document.getElementById('billPieChart')
  if (!chartDom) return
  
  if (pieChartInstance) {
    pieChartInstance.dispose()
  }
  pieChartInstance = echarts.init(chartDom)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: { color: '#ccc' }
    },
    series: [
      {
        name: '费用构成',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 5,
          borderColor: '#1f2d3d', 
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold',
            color: '#fff'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: currentBill.acFee, name: '空调费', itemStyle: { color: '#e6a23c' } },
          { value: currentBill.roomFee, name: '住宿费', itemStyle: { color: '#11998e' } }
        ]
      }
    ]
  }
  
  pieChartInstance.setOption(option)
}

const exportDetail = (format) => {
  if (!currentBill.roomNo) {
    ElMessage.warning('账单数据未就绪')
    return
  }
  
  try {
    if (format === 'txt') {
      exportToTXT(currentBill)
      ElMessage.success('TXT账单导出成功')
    } else if (format === 'excel') {
      exportToExcel(currentBill)
      ElMessage.success('Excel详单导出成功')
    } else if (format === 'pdf') {
      const loading = ElMessage({
        message: '正在生成PDF，请稍候...',
        type: 'info',
        duration: 0
      })
      
      exportToPDF(currentBill)
      
      setTimeout(() => {
        loading.close()
        ElMessage.success('PDF账单导出成功')
      }, 1500)
    }
  } catch (e) {
    console.error('[导出] 失败:', e)
    ElMessage.error('导出失败: ' + e.message)
  }
}

const submitCheckOut = async () => {
  try {
    await api.checkOut(selectedRoom.value.roomNo)
    BillingStore.clearRoom(selectedRoom.value.roomNo)
    BillingStore.clearDeposit(selectedRoom.value.roomNo)
    checkOutVisible.value = false
    ElMessage.success('退房成功，房间已重置')
    
    currentBill.roomNo = ''
    currentBill.customerName = ''
    currentBill.acFee = 0
    currentBill.roomFee = 0
    currentBill.total = 0
    currentBill.deposit = 0
    currentBill.refund = 0
    currentBill.checkInDays = 0
    currentBill.detailLogs = []
    
    fetchRooms()
  } catch (e) { 
    console.error('[退房] 失败:', e)
    ElMessage.error('退房操作失败') 
  }
}

let timer = null
onMounted(() => {
  fetchRooms()
  timer = setInterval(fetchRooms, 2000)
})

onUnmounted(() => { 
  if (timer) clearInterval(timer) 
  if (pieChartInstance) pieChartInstance.dispose()
})
</script>

<style scoped>
/* =========================================
   1. Dashboard 基础背景与布局
   ========================================= */
.dashboard-container { 
  padding: 30px; 
  min-height: 100vh; 
  background-color: #061e18; 
  position: relative; 
  color: #fff; 
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif; 
  overflow-x: hidden; 
}

/* 动态背景层 */
.bg-layer-1, .bg-layer-2 { 
  position: absolute; 
  border-radius: 50%; 
  filter: blur(120px); 
  z-index: 0; 
  pointer-events: none; 
}
.bg-layer-1 { 
  width: 60vw; height: 60vw; 
  background: linear-gradient(135deg, #11998e, #38ef7d); 
  opacity: 0.1; top: -20%; left: -10%; 
  animation: float1 25s infinite alternate ease-in-out; 
}
.bg-layer-2 { 
  width: 50vw; height: 50vw; 
  background: linear-gradient(135deg, #0f2027, #2c5364); 
  opacity: 0.15; bottom: -20%; right: -10%; 
  animation: float2 30s infinite alternate-reverse ease-in-out; 
}
@keyframes float1 { 0% { transform: translate(0,0); } 100% { transform: translate(10vw, 5vh); } }
@keyframes float2 { 0% { transform: translate(0,0); } 100% { transform: translate(-10vw, -5vh); } }

/* =========================================
   2. 玻璃态卡片通用样式 (Dashboard Components)
   ========================================= */
.glass-panel { 
  background: rgba(255, 255, 255, 0.05); 
  backdrop-filter: blur(16px); 
  border: 1px solid rgba(255, 255, 255, 0.1); 
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2); 
  color: #E5EAF3; 
}

/* Header Section */
.header-section { position: relative; z-index: 10; display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; }
.title-box h2 { font-size: 28px; margin: 0; text-shadow: 0 2px 10px rgba(0,0,0,0.5); }
.title-box p { margin: 5px 0 0; color: #8ebfba; font-size: 12px; letter-spacing: 2px; }

/* Stats Cards */
.stats-cards { display: flex; gap: 20px; }
.stat-card { display: flex; align-items: center; gap: 15px; padding: 20px 25px; border-radius: 16px; transition: all 0.3s ease; }
.stat-card:hover { transform: translateY(-5px); box-shadow: 0 8px 40px rgba(0, 0, 0, 0.3); }
.occupied-card { border-left: 4px solid #F56C6C; }
.free-card { border-left: 4px solid #67C23A; }
.stat-icon { width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.occupied-card .stat-icon { background: rgba(245, 108, 108, 0.2); color: #F56C6C; }
.free-card .stat-icon { background: rgba(103, 194, 58, 0.2); color: #67C23A; }
.stat-content { display: flex; flex-direction: column; }
.stat-value { font-size: 32px; font-weight: bold; line-height: 1; margin-bottom: 5px; }
.occupied-card .stat-value { color: #F56C6C; } 
.free-card .stat-value { color: #67C23A; }
.stat-label { font-size: 13px; color: #a6b0c2; }

/* Room List Grid */
.room-list { position: relative; z-index: 10; }
.room-card { border-radius: 16px; margin-bottom: 24px; border: none; transition: all 0.3s ease; }
.room-card:hover { transform: translateY(-5px); box-shadow: 0 8px 40px rgba(0, 0, 0, 0.4); }
.room-card.occupied { border-left: 4px solid #F56C6C; } 
.room-card.free { border-left: 4px solid #11998e; }
:deep(.room-card .el-card__body) { padding: 20px; }

/* Card Internals */
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); }
.room-no { font-size: 24px; font-weight: bold; color: #11998e; text-shadow: 0 2px 10px rgba(17, 153, 142, 0.3); }
.status-tag { display: flex; align-items: center; gap: 5px; padding: 5px 12px; }
.card-content { margin-bottom: 20px; }
.info-row { display: flex; justify-content: space-between; align-items: center; font-size: 14px; color: #ced4da; margin-bottom: 12px; padding: 8px 0; }
.info-row.highlight { background: rgba(255, 255, 255, 0.03); padding: 12px; border-radius: 8px; margin-top: 10px; }
.info-row .label { display: flex; align-items: center; gap: 6px; color: #a6b0c2; }
.info-row .value { font-weight: 500; color: #fff; }
.fee-value { font-size: 18px; font-weight: bold; color: #E6A23C !important; font-family: monospace; }
.card-actions { display: flex; gap: 10px; padding-top: 10px; }
.action-btn { flex: 1; border-radius: 8px; transition: all 0.3s ease; }
.action-btn:hover { transform: scale(1.05); }

/* =========================================
   3. 弹窗通用样式 (Element Plus Override)
   ========================================= */
:deep(.custom-dialog.el-dialog) { 
  background: rgba(19, 36, 40, 0.95); /* 深色磨砂背景 */
  backdrop-filter: blur(25px); 
  border: 1px solid rgba(255, 255, 255, 0.15); 
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6); 
  border-radius: 16px;
  overflow: hidden;
}
:deep(.custom-dialog .el-dialog__header) { 
  margin: 0; padding: 0; 
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}
:deep(.custom-dialog .el-dialog__body) { 
  background: transparent; 
  color: #fff; 
  padding: 0; /* 移除默认内边距，由内容控制 */
}
:deep(.custom-dialog .el-dialog__footer) { 
  padding: 0; 
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(0, 0, 0, 0.2);
}

/* 弹窗自定义 Header */
.dialog-header-stylized {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 24px;
  font-size: 18px;
  font-weight: 500;
  color: #fff;
  letter-spacing: 0.5px;
}

/* 底部操作栏 */
.dialog-footer-bar {
  display: flex;
  justify-content: flex-end;
  padding: 15px 24px;
  gap: 12px;
}
.dialog-footer-bar.full-width {
  justify-content: space-between;
  align-items: center;
}
.left-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.action-label {
  font-size: 13px;
  color: #a6b0c2;
}

/* =========================================
   4. 入住登记弹窗样式
   ========================================= */
.checkin-dialog .dialog-body-content {
  padding: 30px 40px;
}
.check-in-form :deep(.el-form-item__label) { 
  color: #a6b0c2; 
  font-weight: 500; 
  padding-bottom: 8px;
}
.room-display-box {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(17, 153, 142, 0.15);
  border: 1px solid rgba(17, 153, 142, 0.3);
  padding: 10px 15px;
  border-radius: 8px;
  color: #11998e;
  font-weight: bold;
  font-size: 16px;
}
:deep(.styled-input .el-input__wrapper) { 
  background: rgba(255, 255, 255, 0.05); 
  border: 1px solid rgba(255, 255, 255, 0.1); 
  box-shadow: none; 
  padding: 8px 12px;
}
:deep(.styled-input .el-input__wrapper:hover),
:deep(.styled-input .el-input__wrapper.is-focus) {
  border-color: #11998e;
  background: rgba(255, 255, 255, 0.08);
}
:deep(.styled-input .el-input__inner) { 
  color: #fff; 
  font-size: 15px;
}
.price-notice {
  margin-top: 25px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px dashed rgba(255,255,255,0.15);
}
.notice-label { font-size: 13px; color: #a6b0c2; }
.notice-value { font-size: 18px; color: #E6A23C; font-weight: bold; font-family: monospace; }
.notice-value small { font-size: 12px; color: #888; font-weight: normal; }

/* =========================================
   5. 详单/账单弹窗样式
   ========================================= */
.detail-dialog .detail-content {
  padding: 24px;
}

/* 汇总信息卡片 */
.summary-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 25px 30px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.summary-section-wrapper {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.summary-card.left-card {
  flex: 2;
  margin-bottom: 0;
}

.pie-chart-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  padding: 10px;
  min-width: 250px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px 40px;
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}
.info-item .label {
  color: #a6b0c2;
  width: 80px;
}
.info-item .val {
  color: #fff;
  font-weight: 600;
}
.info-item .val.highlight {
  font-size: 16px;
}

/* 费用展示区 */
.cost-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 25px;
}

.cost-item {
  text-align: center;
}

.cost-label {
  font-size: 13px;
  color: #a6b0c2;
  margin-bottom: 8px;
}

.cost-value {
  font-size: 20px;
  font-weight: bold;
  color: #e6a23c;
  font-family: 'Georgia', serif;
  letter-spacing: 0.5px;
}

.cost-item.total .cost-value {
  font-size: 26px;
  color: #e6a23c;
}

/* 表格区域 */
.table-area {
  background: transparent;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 自定义表格样式 */
:deep(.custom-table) {
  --el-table-border-color: rgba(255, 255, 255, 0.1);
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #7ba1a1;
  background: transparent !important;
  color: #fff;
}

:deep(.custom-table th.el-table__cell) {
  background-color: #7ba1a1 !important;
  color: #fff !important;
  font-weight: 500;
  border-bottom: none;
  height: 45px;
}

:deep(.custom-table tr) {
  background-color: rgba(255, 255, 255, 0.02);
}

:deep(.custom-table .el-table__row--striped td.el-table__cell) {
  background-color: rgba(255, 255, 255, 0.06);
}

:deep(.custom-table td.el-table__cell) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

:deep(.custom-table .cell) {
  padding: 0 8px;
  font-size: 13px;
}

/* 空状态占位 */
.empty-placeholder {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #a6b0c2;
  background: rgba(0,0,0,0.1);
}
.empty-icon-box {
  font-size: 48px;
  opacity: 0.5;
  margin-bottom: 10px;
}

/* 按钮样式微调 */
:deep(.el-button) { 
  border-radius: 6px; 
  font-weight: 500;
}
:deep(.el-button-group .el-button) {
  border-radius: 0;
  border-color: rgba(255,255,255,0.2);
  background: rgba(255,255,255,0.05);
  color: #fff;
}
:deep(.el-button-group .el-button:first-child) { border-top-left-radius: 6px; border-bottom-left-radius: 6px; }
:deep(.el-button-group .el-button:last-child) { border-top-right-radius: 6px; border-bottom-right-radius: 6px; }
:deep(.el-button-group .el-button:hover) {
  background: rgba(255,255,255,0.15);
  color: #fff;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .header-section { flex-direction: column; align-items: flex-start; gap: 20px; }
  .dialog-footer-bar.full-width { flex-direction: column; gap: 15px; align-items: stretch; }
  .left-actions { justify-content: space-between; }
  .info-grid { grid-template-columns: 1fr; gap: 10px; }
  .cost-grid { flex-direction: column; gap: 15px; align-items: flex-start; }
  .summary-section-wrapper { flex-direction: column; }
}
</style>
