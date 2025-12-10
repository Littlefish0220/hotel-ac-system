package com.service.impl;

import java.io.IOException;

import com.model.entity.Room;
import com.repository.RoomRepository;
import com.service.AcBillingService;
import com.service.AcReportService;
import com.service.BillService;
import com.service.CheckoutService;

/**
 * 退房结账实现：
 * - 生成 & 导出住宿费账单 txt；
 * - 生成空调 AC 总账单；
 * - 导出 AC 详单和 AC 账单 txt。
 */
public class CheckoutServiceImpl implements CheckoutService {

    private final RoomRepository roomRepository;
    private final BillService billService;
    private final AcBillingService acBillingService;
    private final AcReportService acReportService;

    public CheckoutServiceImpl(RoomRepository roomRepository,
            BillService billService,
            AcBillingService acBillingService,
            AcReportService acReportService) {
        this.roomRepository = roomRepository;
        this.billService = billService;
        this.acBillingService = acBillingService;
        this.acReportService = acReportService;
    }

    @Override
    public void processCheckOut(String roomId,
            int checkInTimeSeconds,
            int checkOutTimeSeconds,
            String outputDir) {

        System.out.println("=== 办理结账开始（账单部分），房间 " + roomId + " ===");

        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在: " + roomId);
        }

        System.out.println("结账：找到房间 " + roomId);

        // 1. 生成住宿费账单（房价 * 天数），并保存到仓库
        billService.generateAccommodationBill(roomId);

        // 2. 导出“住宿费账单 txt”
        billService.exportAccommodationBillTxt(roomId, outputDir);

        // 3. 生成空调 AC 总账单
        // 你的 AcBillingService 接口是 generateAcBill(String, int, int)
        acBillingService.generateAcBill(roomId, checkInTimeSeconds, checkOutTimeSeconds);

        // 4. 导出 AC 详单 + AC 账单 txt
        // 你的 AcReportService.exportAcReport(roomId, outputDir) 会抛 IOException
        try {
            acReportService.exportAcReport(roomId, outputDir);
        } catch (IOException e) {
            System.err.println("导出房间 " + roomId + " 的空调详单/账单时发生 IO 异常");
            e.printStackTrace();
        }

        System.out.println("结账：房间 " + roomId + " 的住宿费账单和空调账单已生成，详单已导出到目录 " + outputDir);
        System.out.println("请顾客核对账单金额后进行支付（此处略）。");
        System.out.println("=== 办理结账完成（账单部分），房间 " + roomId + " ===");
    }
}
