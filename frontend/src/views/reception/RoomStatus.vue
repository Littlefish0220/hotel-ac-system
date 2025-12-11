<template>
  <div class="dashboard-container">
    <div class="bg-layer-1"></div>
    <div class="bg-layer-2"></div>

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
          </div>

          <div class="card-actions">
            <el-button 
              v-if="room.status !== 'occupied'" 
              type="primary" 
              color="#11998e" 
              size="small" 
              class="action-btn"
              @click="openCheckIn(room)"
              :icon="Plus"
            >
              办理入住
            </el-button>
            
            <template v-else>
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
      title="办理入住 Check-In" 
      width="450px" 
      align-center
      class="custom-dialog"
    >
      <el-form :model="checkInForm" label-position="top" class="check-in-form">
        <el-form-item label="房间号">
          <el-input v-model="checkInForm.roomNo" disabled>
            <template #prefix>
              <el-icon><House /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="顾客姓名" required>
          <el-input 
            v-model="checkInForm.name" 
            placeholder="请输入顾客姓名"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-alert 
          title="入住提示" 
          type="info" 
          :closable="false"
          show-icon
        >
          <template #default>
            <div style="font-size: 13px;">
              房间费用：<strong>¥{{ currentRoomRate }}/天</strong><br>
              办理入住后将自动计费
            </div>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="checkInVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            color="#11998e" 
            @click="submitCheckIn"
            :icon="Check"
          >
            确认办理
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 退房弹窗 -->
    <el-dialog 
      v-model="checkOutVisible" 
      title="账单结算 Check-Out" 
      width="680px" 
      align-center
      class="custom-dialog"
    >
      <div class="bill-preview" v-if="currentBill">
        <div class="bill-header">
          <el-icon :size="40" color="#11998e"><Document /></el-icon>
          <h3>{{ currentBill.roomNo }} 房间账单</h3>
          <p class="customer-name">顾客：{{ currentBill.customerName }}</p>
        </div>
        
        <div class="bill-table">
          <div class="bill-row">
            <span class="bill-label">
              <el-icon><Refrigerator /></el-icon>
              空调使用费
            </span>
            <span class="bill-val">¥ {{ currentBill.acFee }}</span>
          </div>
          <div class="bill-row">
            <span class="bill-label">
              <el-icon><House /></el-icon>
              住宿费 ({{ currentBill.days }}天)
            </span>
            <span class="bill-val">¥ {{ currentBill.roomFee }}</span>
          </div>
          <el-divider />
          <div class="bill-row total-row">
            <span class="bill-label">应付总额</span>
            <span class="bill-val total-val">¥ {{ currentBill.total }}</span>
          </div>
        </div>
      </div>
      
      <!-- 底部按钮区域 -->
      <template #footer>
        <div class="dialog-footer-split">
          <div class="left-group">
            <span class="group-label">导出详单：</span>
            <el-button-group>
              <el-button size="small" :icon="Document" @click="exportDetail('txt')">TXT</el-button>
              <el-button size="small" :icon="Document" @click="exportDetail('csv')">CSV</el-button>
            </el-button-group>
          </div>
          
          <div class="right-group">
            <el-button @click="checkOutVisible = false">取消</el-button>
            <el-button 
              type="danger" 
              @click="submitCheckOut"
              :icon="Printer"
            >
              确认退房并清空
            </el-button>
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

const rooms = ref([])

const roomsWithStatus = computed(() => {
  return rooms.value.map(room => {
    // running 或 waiting 表示空调正在使用，房间显示为"占用"
    const isOccupied = room.state === 'running' || room.state === 'waiting'
    return {
      ...room,
      status: isOccupied ? 'occupied' : 'free'
    }
  })
})

const checkInVisible = ref(false)
const checkOutVisible = ref(false)
const selectedRoom = ref(null)

const checkInForm = reactive({ roomNo: '', name: '' })
const currentBill = reactive({ 
  roomNo: '', 
  customerName: '',
  acFee: 0, 
  roomFee: 0,
  total: 0,
  days: 0
})

const occupiedCount = computed(() => 
  rooms.value.filter(r => r.status === 'occupied').length
)
const freeCount = computed(() => 
  rooms.value.filter(r => r.status !== 'occupied').length
)
const currentRoomRate = computed(() => 
  selectedRoom.value?.dailyRoomRate || 0
)

const fetchRooms = async () => {
  try {
    const res = await api.getSystemStatus()
    console.log('[前台] 获取到的数据:', res.data) // 调试日志
    if (res && res.data && res.data.rooms) {
      rooms.value = res.data.rooms
      console.log('[前台] 房间列表:', rooms.value) // 调试日志
    }
  } catch (e) { console.error(e) }
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
    await api.checkIn(selectedRoom.value.roomNo, checkInForm.name)
    checkInVisible.value = false
    ElMessage.success(`房间 ${selectedRoom.value.roomNo} 入住成功`)
    fetchRooms()
  } catch (e) { ElMessage.error('入住办理失败') }
}

const openCheckOut = async (room) => {
  selectedRoom.value = room
  try {
    const res = await api.getBillPreview(room.roomNo)
    if(res.code === 200 && res.data) {
       const bill = res.data
       currentBill.roomNo = bill.roomNo
       currentBill.customerName = bill.customerName
       currentBill.acFee = bill.acFee
       currentBill.roomFee = bill.roomFee
       currentBill.total = bill.total
       currentBill.days = bill.days || 0
       checkOutVisible.value = true
    } else {
       ElMessage.error('无法获取账单详情')
    }
  } catch(e) {
    ElMessage.error('系统错误')
  }
}

// 修改：调用后端导出接口
const exportDetail = async (format) => {
  if (!selectedRoom.value) {
    ElMessage.warning('未选择房间')
    return
  }
  
  const roomNo = selectedRoom.value.roomNo
  
  try {
    if (format === 'txt') {
      // 调用文本账单导出接口
      const res = await api.exportBill(roomNo)
      const blob = new Blob([res.data], { type: 'text/plain;charset=utf-8' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `账单_${roomNo}.txt`
      link.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('TXT账单导出成功')
    } else if (format === 'csv') {
      // 调用CSV详单导出接口
      const res = await api.exportDetailCsv(roomNo)
      const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `详单_${roomNo}.csv`
      link.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('CSV详单导出成功')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

const submitCheckOut = async () => {
  try {
    await api.checkOut(selectedRoom.value.roomNo)
    checkOutVisible.value = false
    ElMessage.success('退房成功，房间已重置')
    fetchRooms()
  } catch (e) { ElMessage.error('退房操作失败') }
}

let timer = null
onMounted(() => {
  fetchRooms()
  timer = setInterval(fetchRooms, 2000)
})

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
/* 保持原有样式不变 */
.dashboard-container { padding: 30px; min-height: 100vh; background-color: #061e18; position: relative; color: #fff; font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif; overflow-x: hidden; }
.bg-layer-1, .bg-layer-2 { position: absolute; border-radius: 50%; filter: blur(120px); z-index: 0; pointer-events: none; }
.bg-layer-1 { width: 60vw; height: 60vw; background: linear-gradient(135deg, #11998e, #38ef7d); opacity: 0.1; top: -20%; left: -10%; animation: float1 25s infinite alternate ease-in-out; }
.bg-layer-2 { width: 50vw; height: 50vw; background: linear-gradient(135deg, #0f2027, #2c5364); opacity: 0.15; bottom: -20%; right: -10%; animation: float2 30s infinite alternate-reverse ease-in-out; }
@keyframes float1 { 0% { transform: translate(0,0); } 100% { transform: translate(10vw, 5vh); } }
@keyframes float2 { 0% { transform: translate(0,0); } 100% { transform: translate(-10vw, -5vh); } }
.glass-panel { background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(16px); border: 1px solid rgba(255, 255, 255, 0.1); box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2); color: #E5EAF3; }
.header-section { position: relative; z-index: 10; display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; }
.title-box h2 { font-size: 28px; margin: 0; text-shadow: 0 2px 10px rgba(0,0,0,0.5); }
.title-box p { margin: 5px 0 0; color: #8ebfba; font-size: 12px; letter-spacing: 2px; }
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
.occupied-card .stat-value { color: #F56C6C; } .free-card .stat-value { color: #67C23A; }
.stat-label { font-size: 13px; color: #a6b0c2; }
.room-list { position: relative; z-index: 10; }
.room-card { border-radius: 16px; margin-bottom: 24px; border: none; transition: all 0.3s ease; }
.room-card:hover { transform: translateY(-5px); box-shadow: 0 8px 40px rgba(0, 0, 0, 0.4); }
.room-card.occupied { border-left: 4px solid #F56C6C; } .room-card.free { border-left: 4px solid #11998e; }
:deep(.room-card .el-card__body) { padding: 20px; }
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

:deep(.custom-dialog .el-dialog) { background: rgba(15, 32, 39, 0.95); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.1); box-shadow: 0 8px 40px rgba(0, 0, 0, 0.5); }
:deep(.custom-dialog .el-dialog__header) { background: rgba(0, 0, 0, 0.2); border-bottom: 1px solid rgba(255, 255, 255, 0.05); padding: 20px; }
:deep(.custom-dialog .el-dialog__title) { color: #fff; font-weight: 500; font-size: 18px; }
:deep(.custom-dialog .el-dialog__body) { background: transparent; color: #fff; padding: 25px; }
:deep(.custom-dialog .el-dialog__footer) { background: rgba(0, 0, 0, 0.2); border-top: 1px solid rgba(255, 255, 255, 0.05); padding: 15px 20px; }
.check-in-form { padding: 10px 0; }
:deep(.check-in-form .el-form-item__label) { color: #a6b0c2; font-weight: 500; }
:deep(.check-in-form .el-input__wrapper) { background: rgba(0, 0, 0, 0.3); border: 1px solid rgba(255, 255, 255, 0.1); box-shadow: none; }
:deep(.check-in-form .el-input__inner) { color: #fff; }
:deep(.check-in-form .el-alert) { background: rgba(64, 158, 255, 0.1); border: 1px solid rgba(64, 158, 255, 0.2); margin-top: 15px; }
.bill-preview { padding: 10px 0; }
.bill-header { text-align: center; margin-bottom: 25px; padding-bottom: 20px; border-bottom: 2px solid rgba(255, 255, 255, 0.1); }
.bill-header h3 { margin: 15px 0 10px; font-size: 22px; color: #11998e; }
.customer-name { color: #a6b0c2; font-size: 14px; margin: 5px 0 0; }
.bill-table { background: rgba(0, 0, 0, 0.2); padding: 20px; border-radius: 12px; border: 1px solid rgba(255, 255, 255, 0.05); }
.bill-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; font-size: 15px; }
.bill-label { display: flex; align-items: center; gap: 8px; color: #a6b0c2; }
.bill-val { font-weight: 500; color: #fff; font-family: monospace; font-size: 16px; }
:deep(.bill-table .el-divider) { margin: 20px 0; background-color: rgba(255, 255, 255, 0.1); }
.total-row { margin-bottom: 0; padding-top: 15px; }
.total-row .bill-label { font-size: 17px; font-weight: bold; color: #fff; }
.total-val { font-size: 24px !important; font-weight: bold; color: #E6A23C !important; }

.dialog-footer-split {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.left-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
.group-label {
  font-size: 12px;
  color: #a6b0c2;
}
.right-group {
  display: flex;
  gap: 10px;
}
:deep(.el-button) { border-radius: 8px; padding: 10px 20px; transition: all 0.3s ease; }
:deep(.el-button:hover) { transform: translateY(-2px); }

@media (max-width: 768px) {
  .header-section { flex-direction: column; align-items: flex-start; gap: 20px; }
  .dialog-footer-split { flex-direction: column; gap: 15px; align-items: flex-end; }
  .left-group { width: 100%; justify-content: space-between; }
}
</style>
