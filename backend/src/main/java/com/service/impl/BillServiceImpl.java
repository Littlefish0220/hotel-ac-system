package com.service.impl;

import com.model.entity.AccommodationBill;
import com.model.entity.AccommodationOrder;
import com.repository.AccommodationBillRepository;
import com.repository.AccommodationOrderRepository;
import com.repository.RoomRepository;
import com.service.BillService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * 住宿费账单业务实现：
 * - 根据“住宿订单中的天数 + 房间对应房价/天”生成住宿费账单
 * - 导出住宿费账单 txt 文件
 *
 * 这样做的好处：
 * - 天数不再写死，可以适配其他测试用例（只要 checkIn 时传的 days 不同）
 * - 房价按题目要求按房间号固定（101=100, 102=125…），有新房间再扩展映射即可
 */
public class BillServiceImpl implements BillService {

    private final RoomRepository roomRepository;
    private final AccommodationOrderRepository orderRepository;
    private final AccommodationBillRepository accommodationBillRepository;

    public BillServiceImpl(RoomRepository roomRepository,
            AccommodationOrderRepository orderRepository,
            AccommodationBillRepository accommodationBillRepository) {
        this.roomRepository = roomRepository;
        this.orderRepository = orderRepository;
        this.accommodationBillRepository = accommodationBillRepository;
    }

    @Override
    public AccommodationBill generateAccommodationBill(String roomId) {
        // === 1. 查该房间当前/最近的住宿订单，拿到 days ===
        // 这里假设仓库里有类似 "findByRoomId(String roomId)" 的方法，
        // 且返回 Optional<AccommodationOrder>。
        // 如果你的方法名不一样，比如叫 findLatestByRoomId / findActiveByRoomId，
        // 请把这一行改成你自己项目里的方法。
        Optional<AccommodationOrder> orderOpt = orderRepository.findByRoomId(roomId);
        AccommodationOrder order = orderOpt.orElseThrow(
                () -> new IllegalStateException("未找到房间 " + roomId + " 的住宿订单"));
        int days = order.getDays();

        // === 2. 根据房间号决定房价/天 ===
        double pricePerDay = getPricePerDayByRoomId(roomId);

        // === 3. 计算总金额 ===
        double totalAmount = pricePerDay * days;

        // === 4. 创建 AccommodationBill 实体，并保存 ===
        // 构造器签名：AccommodationBill(String id, String roomId,
        // long checkInTime, long checkOutTime,
        // int days, double totalAmount)
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        AccommodationBill bill = new AccommodationBill(
                id,
                roomId,
                now, // 这里没有真实入住/退房时间，就先占位写 now
                now,
                days,
                totalAmount);

        accommodationBillRepository.save(bill);

        System.out.println("生成住宿费账单：房间 " + roomId
                + "，天数=" + days
                + "，总费用=" + totalAmount);

        return bill;
    }

    @Override
    public void exportAccommodationBillTxt(String roomId, String outputDir) {
        // 导出时，为保证通用和正确性，直接按“订单+房价”再算一遍，
        // 不依赖 AccommodationBill 的 getter（你那边目前好像没有这些 getter）
        Optional<AccommodationOrder> orderOpt = orderRepository.findByRoomId(roomId);
        AccommodationOrder order = orderOpt.orElseThrow(
                () -> new IllegalStateException("未找到房间 " + roomId + " 的住宿订单"));
        int days = order.getDays();
        double pricePerDay = getPricePerDayByRoomId(roomId);
        double totalAmount = pricePerDay * days;

        // 确保输出目录存在
        File dir = new File(outputDir);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("导出失败：无法创建输出目录 " + outputDir);
            return;
        }

        String fileName = "room-" + roomId + "-accommodation-bill.txt";
        File file = new File(dir, fileName);

        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write("===== 酒店住宿费账单 =====\n");
            writer.write("房间号： " + roomId + "\n");
            writer.write("入住天数： " + days + " 天\n");
            writer.write("房价： " + pricePerDay + " 元/天\n");
            writer.write("--------------------------\n");
            writer.write("住宿总费用： " + totalAmount + " 元\n");
            writer.write("===========================\n");
            writer.flush();

            System.out.println("导出完成：房间 " + roomId
                    + " 的住宿费账单已写入目录 " + outputDir
                    + "，文件名=" + fileName);
        } catch (IOException e) {
            System.err.println("导出失败：房间 " + roomId
                    + " 的住宿费账单写文件异常");
            e.printStackTrace();
        }
    }

    // ====== 工具方法：房间号 -> 房价/天 ======
    // 这一段是“当前 5 个房间”的通用设置，后面如果老师加了 106/107，只要加 case 即可。

    private double getPricePerDayByRoomId(String roomId) {
        switch (roomId) {
            case "101":
                // 房间1 初始温度32℃ 房费100元/天
                return 100.0;
            case "102":
                // 房间2 初始温度28℃ 房费125元/天
                return 125.0;
            case "103":
                // 房间3 初始温度30℃ 房费150元/天
                return 150.0;
            case "104":
                // 房间4 初始温度29℃ 房费200元/天
                return 200.0;
            case "105":
                // 房间5 初始温度35℃ 房费100元/天
                return 100.0;
            default:
                // 其它房间就先给一个默认价，后面可以按需要扩展
                return 100.0;
        }
    }
}
