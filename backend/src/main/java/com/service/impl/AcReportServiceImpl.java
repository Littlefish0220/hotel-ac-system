package com.service.impl;

import com.model.entity.AcBill;
import com.model.entity.AcDetailRecord;
import com.repository.AcBillRepository;
import com.repository.AcDetailRecordRepository;
import com.service.AcReportService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

/**
 * 将 AC 详单和账单导出为 txt 文件的简单实现。
 */
public class AcReportServiceImpl implements AcReportService {

    private final AcDetailRecordRepository detailRecordRepository;
    private final AcBillRepository acBillRepository;

    public AcReportServiceImpl(AcDetailRecordRepository detailRecordRepository,
            AcBillRepository acBillRepository) {
        this.detailRecordRepository = detailRecordRepository;
        this.acBillRepository = acBillRepository;
    }

    @Override
    public void exportAcReport(String roomId, String outputDir) throws IOException {
        // 1. 确保输出目录存在
        File dir = new File(outputDir);
        if (!dir.exists()) {
            boolean ok = dir.mkdirs();
            if (!ok) {
                throw new IOException("无法创建输出目录: " + outputDir);
            }
        }

        // 2. 导出详单
        List<AcDetailRecord> records = detailRecordRepository.findByRoomId(roomId);
        File detailFile = new File(dir, roomId + "_ac_detail.txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(detailFile, false), true)) {
            pw.println("房间号\t请求时间(s)\t开始时间(s)\t结束时间(s)\t时长(s)\t风速\t本段费用(元)\t累积费用(元)");
            for (AcDetailRecord r : records) {
                pw.printf("%s\t%d\t%d\t%d\t%d\t%s\t%.2f\t%.2f%n",
                        r.getRoomId(),
                        r.getRequestTimeSeconds(),
                        r.getServiceStartTimeSeconds(),
                        r.getServiceEndTimeSeconds(),
                        r.getServiceDurationSeconds(),
                        r.getFanSpeed(),
                        r.getCurrentFee(),
                        r.getTotalFee());
            }
        }

        // 3. 导出总账单
        Optional<AcBill> billOpt = acBillRepository.findByRoomId(roomId);
        if (billOpt.isPresent()) {
            AcBill bill = billOpt.get();
            File billFile = new File(dir, roomId + "_ac_bill.txt");
            try (PrintWriter pw = new PrintWriter(new FileWriter(billFile, false), true)) {
                pw.println("房间号\t入住时间(s)\t离开时间(s)\t空调总费用(元)");
                pw.printf("%s\t%d\t%d\t%.2f%n",
                        bill.getRoomId(),
                        bill.getCheckInTimeSeconds(),
                        bill.getCheckOutTimeSeconds(),
                        bill.getTotalAcFee());
            }
        } else {
            System.out.println("提示：房间 " + roomId + " 暂无 AC 账单记录（可能还没退房生成）。");
        }

        System.out.println("导出完成：房间 " + roomId + " 的 AC 详单和账单已写入目录 " + outputDir);
    }
}
