import * as XLSX from 'xlsx'
import jsPDF from 'jspdf'
import 'jspdf-autotable'
import html2canvas from 'html2canvas'  // ★ 新增导入

/**
 * 导出TXT格式账单（保持不变）
 */
export function exportToTXT(billData) {
  const { roomNo, customerName, acFee, roomFee, total, checkInDays, detailLogs } = billData
  
  let content = `========================================\n`
  content += `       波普特酒店 - 结账账单\n`
  content += `========================================\n\n`
  content += `房间号      : ${roomNo}\n`
  content += `住户姓名    : ${customerName}\n`
  content += `入住天数    : ${checkInDays} 天\n`
  content += `打印时间    : ${new Date().toLocaleString('zh-CN')}\n`
  content += `\n----------------------------------------\n`
  content += `空调费用    : ¥${acFee.toFixed(2)}\n`
  content += `住宿费用    : ¥${roomFee.toFixed(2)}\n`
  content += `----------------------------------------\n`
  content += `应付总额    : ¥${total.toFixed(2)}\n`
  content += `========================================\n\n`
  
  content += `\n空调使用详单：\n`
  content += `序号 | 请求时间(s) | 开始时间(s) | 结束时间(s) | 时长(s) | 风速   | 本段费用 | 累积费用\n`
  content += `${'─'.repeat(90)}\n`
  
  if (detailLogs && detailLogs.length > 0) {
    detailLogs.forEach((log, index) => {
      const row = `${(index + 1).toString().padEnd(4)} | ` +
                  `${log.requestTimeSeconds.toString().padEnd(11)} | ` +
                  `${log.serviceStartTimeSeconds.toString().padEnd(11)} | ` +
                  `${log.serviceEndTimeSeconds.toString().padEnd(11)} | ` +
                  `${log.serviceDurationSeconds.toString().padEnd(7)} | ` +
                  `${log.fanSpeed.padEnd(6)} | ` +
                  `${log.currentFee.toFixed(2).padEnd(8)} | ` +
                  `${log.totalFee.toFixed(2)}\n`
      content += row
    })
  } else {
    content += `暂无空调使用记录\n`
  }
  
  content += `\n========================================\n`
  content += `感谢您的入住，祝您生活愉快！\n`
  content += `========================================\n`
  
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `房间${roomNo}_账单_${Date.now()}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

/**
 * 导出Excel格式详单（保持不变）
 */
export function exportToExcel(billData) {
  const { roomNo, customerName, acFee, roomFee, total, checkInDays, detailLogs } = billData
  
  const wb = XLSX.utils.book_new()
  
  const summaryData = [
    ['波普特酒店 - 结账账单'],
    [''],
    ['房间号', roomNo],
    ['住户姓名', customerName],
    ['入住天数', `${checkInDays} 天`],
    ['打印时间', new Date().toLocaleString('zh-CN')],
    [''],
    ['空调费用', `¥${acFee.toFixed(2)}`],
    ['住宿费用', `¥${roomFee.toFixed(2)}`],
    ['应付总额', `¥${total.toFixed(2)}`],
    ['']
  ]
  
  const detailHeader = ['序号', '房间号', '请求时间(s)', '服务开始时间(s)', '服务结束时间(s)', '服务时长(秒)', '风速', '本段费用(元)', '累积费用(元)']
  
  const detailData = detailLogs && detailLogs.length > 0 
    ? detailLogs.map((log, index) => [
        index + 1,
        roomNo,
        log.requestTimeSeconds,
        log.serviceStartTimeSeconds,
        log.serviceEndTimeSeconds,
        log.serviceDurationSeconds,
        log.fanSpeed,
        log.currentFee.toFixed(2),
        log.totalFee.toFixed(2)
      ])
    : [['暂无空调使用记录']]
  
  const fullData = [
    ...summaryData,
    ['空调使用详单'],
    detailHeader,
    ...detailData
  ]
  
  const ws = XLSX.utils.aoa_to_sheet(fullData)
  
  ws['!cols'] = [
    { wch: 8 }, { wch: 10 }, { wch: 12 }, { wch: 14 }, { wch: 14 }, 
    { wch: 12 }, { wch: 8 }, { wch: 12 }, { wch: 12 }
  ]
  
  XLSX.utils.book_append_sheet(wb, ws, `房间${roomNo}`)
  XLSX.writeFile(wb, `房间${roomNo}_详单_${Date.now()}.xlsx`)
}

/**
 * ★ 重写：导出PDF格式账单（支持中文）
 * 使用 html2canvas 将HTML转为图片，再插入PDF
 */
export function exportToPDF(billData) {
  const { roomNo, customerName, acFee, roomFee, total, checkInDays, detailLogs } = billData
  
  // ★ 创建临时HTML容器
  const container = document.createElement('div')
  container.style.cssText = `
    position: absolute;
    left: -9999px;
    top: 0;
    width: 750px;
    background: #ffffff;
    padding: 40px;
    color: #000000;
    font-family: 'Microsoft YaHei', 'SimSun', Arial, sans-serif;
    box-sizing: border-box;
  `
  
  // ★ 构建账单HTML内容
  let htmlContent = `
    <div style="text-align: center; margin-bottom: 30px;">
      <h1 style="color: #11998e; margin: 0 0 10px 0; font-size: 28px;">波普特酒店</h1>
      <h2 style="color: #666; margin: 0; font-size: 18px;">POPPER HOTEL - 结账账单</h2>
      <p style="color: #999; font-size: 12px; margin: 10px 0 0 0;">打印时间：${new Date().toLocaleString('zh-CN')}</p>
    </div>
    
    <div style="border: 2px solid #11998e; border-radius: 8px; padding: 20px; margin-bottom: 20px;">
      <table style="width: 100%; font-size: 14px; line-height: 1.8;">
        <tr>
          <td style="width: 30%; color: #666;">房间号：</td>
          <td style="font-weight: bold;">${roomNo}</td>
        </tr>
        <tr>
          <td style="color: #666;">住户姓名：</td>
          <td style="font-weight: bold;">${customerName}</td>
        </tr>
        <tr>
          <td style="color: #666;">入住天数：</td>
          <td style="font-weight: bold;">${checkInDays} 天</td>
        </tr>
      </table>
    </div>
    
    <div style="background: #f5f5f5; border-radius: 8px; padding: 20px; margin-bottom: 20px;">
      <table style="width: 100%; font-size: 15px; line-height: 2;">
        <tr>
          <td style="color: #666;">空调使用费：</td>
          <td style="text-align: right; font-weight: bold; color: #E6A23C;">¥${acFee.toFixed(2)}</td>
        </tr>
        <tr>
          <td style="color: #666;">住宿费用：</td>
          <td style="text-align: right; font-weight: bold; color: #E6A23C;">¥${roomFee.toFixed(2)}</td>
        </tr>
        <tr style="border-top: 2px solid #ddd;">
          <td style="color: #000; font-size: 18px; font-weight: bold; padding-top: 10px;">应付总额：</td>
          <td style="text-align: right; font-size: 22px; font-weight: bold; color: #F56C6C; padding-top: 10px;">¥${total.toFixed(2)}</td>
        </tr>
      </table>
    </div>
  `
  
  // ★ 添加详单表格（如果有数据）
  if (detailLogs && detailLogs.length > 0) {
    htmlContent += `
      <div style="margin-top: 30px;">
        <h3 style="color: #11998e; border-bottom: 2px solid #11998e; padding-bottom: 10px; margin-bottom: 15px;">空调使用详单</h3>
        <table style="width: 100%; border-collapse: collapse; font-size: 11px;">
          <thead>
            <tr style="background: #11998e; color: white;">
              <th style="padding: 8px; border: 1px solid #ddd;">序号</th>
              <th style="padding: 8px; border: 1px solid #ddd;">请求时间(s)</th>
              <th style="padding: 8px; border: 1px solid #ddd;">开始时间(s)</th>
              <th style="padding: 8px; border: 1px solid #ddd;">结束时间(s)</th>
              <th style="padding: 8px; border: 1px solid #ddd;">时长(s)</th>
              <th style="padding: 8px; border: 1px solid #ddd;">风速</th>
              <th style="padding: 8px; border: 1px solid #ddd;">本段费用</th>
              <th style="padding: 8px; border: 1px solid #ddd;">累积费用</th>
            </tr>
          </thead>
          <tbody>
    `
    
    detailLogs.forEach((log, index) => {
      const bgColor = index % 2 === 0 ? '#f9f9f9' : '#ffffff'
      htmlContent += `
        <tr style="background: ${bgColor};">
          <td style="padding: 6px; border: 1px solid #ddd; text-align: center;">${index + 1}</td>
          <td style="padding: 6px; border: 1px solid #ddd; text-align: center;">${log.requestTimeSeconds}</td>
          <td style="padding: 6px; border: 1px solid #ddd; text-align: center;">${log.serviceStartTimeSeconds}</td>
          <td style="padding: 6px; border: 1px solid #ddd; text-align: center;">${log.serviceEndTimeSeconds}</td>
          <td style="padding: 6px; border: 1px solid #ddd; text-align: center;">${log.serviceDurationSeconds}</td>
          <td style="padding: 6px; border: 1px solid #ddd; text-align: center;">${log.fanSpeed}</td>
          <td style="padding: 6px; border: 1px solid #ddd; text-align: right;">${log.currentFee.toFixed(2)}</td>
          <td style="padding: 6px; border: 1px solid #ddd; text-align: right; font-weight: bold;">${log.totalFee.toFixed(2)}</td>
        </tr>
      `
    })
    
    htmlContent += `
          </tbody>
        </table>
      </div>
    `
  }
  
  htmlContent += `
    <div style="margin-top: 40px; text-align: center; color: #999; font-size: 12px; border-top: 1px dashed #ddd; padding-top: 20px;">
      <p>感谢您的入住，祝您生活愉快！</p>
      <p>Popper Hotel © ${new Date().getFullYear()}</p>
    </div>
  `
  
  container.innerHTML = htmlContent
  document.body.appendChild(container)
  
  // ★ 使用 html2canvas 转为图片
  html2canvas(container, {
    scale: 2,  // 提高清晰度
    useCORS: true,
    backgroundColor: '#ffffff'
  }).then(canvas => {
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    
    const imgWidth = 210  // A4宽度
    const imgHeight = canvas.height * imgWidth / canvas.width
    
    // 如果内容超过一页，自动分页
    let position = 0
    if (imgHeight > 297) {
      while (position < imgHeight) {
        pdf.addImage(imgData, 'PNG', 0, -position, imgWidth, imgHeight)
        position += 297
        if (position < imgHeight) {
          pdf.addPage()
        }
      }
    } else {
      pdf.addImage(imgData, 'PNG', 0, 0, imgWidth, imgHeight)
    }
    
    pdf.save(`账单_房间${roomNo}_${Date.now()}.pdf`)
    
    // ★ 清理临时DOM
    document.body.removeChild(container)
  }).catch(error => {
    console.error('PDF生成失败:', error)
    document.body.removeChild(container)
    throw new Error('PDF生成失败，请检查浏览器控制台')
  })
}
