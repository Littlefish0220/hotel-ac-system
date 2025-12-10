// src/utils/exportUtils.js

// 专门用于 CSV/Excel 的时间格式化 (避免逗号，避免Excel科学计数法)
const formatTimeForExcel = (timestamp) => {
  if (!timestamp) return '-';
  const d = new Date(timestamp);
  const pad = (n) => n < 10 ? `0${n}` : n;
  // 格式：YYYY-MM-DD HH:mm:ss
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
};

// 用于显示的中文格式化
const formatDateTime = (timestamp) => {
  if (!timestamp) return '-';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    hour12: false
  });
};

const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins}分${secs}秒`;
};

// 导出为 TXT
export const exportToTXT = (bill) => {
  const { roomNo, customerName, acFee, roomFee, total, checkInDays, detailLogs } = bill;
  
  let content = `========================================\n`;
  content += `           酒店空调详单\n`;
  content += `========================================\n\n`;
  content += `房间号：${roomNo}\n`;
  content += `顾客姓名：${customerName}\n`;
  content += `入住天数：${checkInDays}天\n`;
  content += `空调费用：¥${acFee.toFixed(2)}\n`;
  content += `住宿费用：¥${roomFee.toFixed(2)}\n`;
  content += `总计费用：¥${total.toFixed(2)}\n`;
  content += `\n========================================\n`;
  content += `           服务详单记录\n`;
  content += `========================================\n\n`;
  
  if (detailLogs && detailLogs.length > 0) {
    detailLogs.forEach((log, index) => {
      content += `【记录 ${index + 1}】\n`;
      content += `请求时间：${formatDateTime(log.requestTime)}\n`;
      content += `服务开始：${formatDateTime(log.serviceStartTime)}\n`;
      content += `服务结束：${formatDateTime(log.serviceEndTime)}\n`;
      content += `服务时长：${formatDuration(log.duration)} (${log.duration}秒)\n`;
      content += `风速档位：${log.fanSpeed}\n`;
      content += `当前费用：¥${log.currentFee?.toFixed(2)}\n`;
      content += `累积费用：¥${log.totalFee?.toFixed(2)}\n`;
      content += `----------------------------------------\n`;
    });
  } else {
    content += `暂无服务记录\n`;
  }
  
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${roomNo}_详单_${Date.now()}.txt`;
  link.click();
  URL.revokeObjectURL(url);
};

// 导出为 Excel (CSV)
export const exportToExcel = (bill) => {
  const { roomNo, customerName, acFee, roomFee, total, checkInDays, detailLogs } = bill;
  
  // 添加 BOM 头，防止 Excel 打开乱码
  let csv = '\uFEFF';
  
  // 1. 系统住宿费账单部分
  csv += `系统住宿费账单\n`;
  csv += `房间号,顾客姓名,入住天数,空调总费用,住宿总费用,总计费用\n`;
  // 使用 \t 防止 Excel 自动格式化数字
  csv += `"${roomNo}","${customerName}",${checkInDays},${acFee.toFixed(2)},${roomFee.toFixed(2)},${total.toFixed(2)}\n\n`;
  
  // 2. 详单数据部分 (严格对应图片字段)
  csv += `空调详单数据\n`;
  csv += `序号,房间号,请求时间,服务开始时间,服务结束时间,服务时长(秒),风速,当前费用,累积费用,备注\n`;
  
  if (detailLogs && detailLogs.length > 0) {
    detailLogs.forEach((log, index) => {
      // 每一列都处理时间格式
      const reqTime = formatTimeForExcel(log.requestTime);
      const startTime = formatTimeForExcel(log.serviceStartTime);
      const endTime = formatTimeForExcel(log.serviceEndTime);

      csv += `${index + 1},`;
      csv += `"${roomNo}",`; // 房间号
      csv += `"${reqTime}",`; // 请求时间
      csv += `"${startTime}",`; // 服务开始时间
      csv += `"${endTime}",`; // 服务结束时间
      csv += `${log.duration},`; // 时长(秒)
      csv += `"${log.fanSpeed}",`; // 风速
      csv += `${log.currentFee?.toFixed(2)},`; // 当前费用
      csv += `${log.totalFee?.toFixed(2)},`; // 累积费用
      csv += `"${log.reason || '-'}"\n`; // 备注
    });
  }
  
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${roomNo}_详单_${Date.now()}.csv`;
  link.click();
  URL.revokeObjectURL(url);
};

// 导出为 PDF
export const exportToPDF = async (bill) => {
  const { roomNo, customerName, acFee, roomFee, total, checkInDays, detailLogs } = bill;
  const printWindow = window.open('', '_blank');
  let html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>${roomNo} 详单</title>
      <style>
        body { font-family: 'SimSun', Arial, sans-serif; padding: 20px; }
        h1 { text-align: center; color: #333; }
        .summary { border: 1px solid #ddd; padding: 15px; margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 12px; }
        th, td { border: 1px solid #333; padding: 6px; text-align: center; }
        th { background-color: #f2f2f2; }
      </style>
    </head>
    <body>
      <h1>酒店空调详单</h1>
      <div class="summary">
        <h3>系统住宿费账单</h3>
        <p><strong>房间号：</strong>${roomNo} &nbsp;&nbsp; <strong>顾客：</strong>${customerName}</p>
        <p><strong>入住天数：</strong>${checkInDays}天</p>
        <p><strong>费用合计：</strong>¥${total.toFixed(2)} (空调: ¥${acFee.toFixed(2)} + 住宿: ¥${roomFee.toFixed(2)})</p>
      </div>
      <h3>空调详单数据</h3>
      <table>
        <thead>
          <tr>
            <th>序号</th>
            <th>请求时间</th>
            <th>服务开始</th>
            <th>服务结束</th>
            <th>时长(秒)</th>
            <th>风速</th>
            <th>当前费用</th>
            <th>累积费用</th>
          </tr>
        </thead>
        <tbody>
  `;
  if (detailLogs && detailLogs.length > 0) {
    detailLogs.forEach((log, index) => {
      html += `
        <tr>
          <td>${index + 1}</td>
          <td>${formatDateTime(log.requestTime)}</td>
          <td>${formatDateTime(log.serviceStartTime)}</td>
          <td>${formatDateTime(log.serviceEndTime)}</td>
          <td>${log.duration}</td>
          <td>${log.fanSpeed}</td>
          <td>¥${log.currentFee?.toFixed(2)}</td>
          <td>¥${log.totalFee?.toFixed(2)}</td>
        </tr>
      `;
    });
  } else {
    html += `<tr><td colspan="8">暂无数据</td></tr>`;
  }
  html += `</tbody></table>
      <script>
        window.onload = () => { window.print(); setTimeout(() => window.close(), 500); };
      </script>
    </body></html>`;
  printWindow.document.write(html);
  printWindow.document.close();
};
