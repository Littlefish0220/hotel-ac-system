package com.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.model.entity.ACMode;
import com.model.entity.AcBill;
import com.model.entity.AcDetailRecord;
import com.model.entity.FanSpeed;
import com.repository.AcBillRepository;
import com.repository.AcDetailRecordRepository;
import com.service.AcBillingService;

public class AcBillingServiceImpl implements AcBillingService {

    private static class Segment {
        int requestTimeSeconds;
        int serviceStartTimeSeconds;
        FanSpeed fanSpeed;
        ACMode mode;

        public Segment(int requestTime, int startTime, FanSpeed speed, ACMode mode) {
            this.requestTimeSeconds = requestTime;
            this.serviceStartTimeSeconds = startTime;
            this.fanSpeed = speed;
            this.mode = mode;
        }
    }

    private final AcDetailRecordRepository detailRecordRepository;
    private final AcBillRepository acBillRepository;

    private final Map<String, Segment> activeSegments = new HashMap<>();
    private final Map<String, Double> roomTotalFee = new HashMap<>();

    public AcBillingServiceImpl(AcDetailRecordRepository detailRecordRepository,
            AcBillRepository acBillRepository) {
        this.detailRecordRepository = detailRecordRepository;
        this.acBillRepository = acBillRepository;
    }

    @Override
    public void onServiceStart(String roomId, ACMode mode, FanSpeed fanSpeed, int currentTime) {
        if (activeSegments.containsKey(roomId)) {
            System.err.println("⚠️ 计费警告：房间 " + roomId
                    + " 已有活跃服务段，自动结束旧段");
            onServiceEnd(roomId, currentTime);
        }

        Segment seg = new Segment(currentTime, currentTime, fanSpeed, mode);
        activeSegments.put(roomId, seg);

        System.out.println("计费：房间 " + roomId + " 开始新一段服务，风速="
                + fanSpeed + "，时间=" + currentTime + " 秒");
    }

    @Override
    public void onServiceEnd(String roomId, int currentTime) {
        Segment seg = activeSegments.remove(roomId);

        if (seg == null) {
            return;
        }

        int duration = currentTime - seg.serviceStartTimeSeconds;

        if (duration <= 0) {
            System.out.println("计费：房间 " + roomId
                    + " 服务段时长≤0秒（" + duration + "s），不计费");
            return;
        }

        double ratePerMinute = getRatePerMinute(seg.fanSpeed);
        double fee = duration / 60.0 * ratePerMinute;
        fee = Math.round(fee * 100.0) / 100.0;

        double total = roomTotalFee.getOrDefault(roomId, 0.0) + fee;
        total = Math.round(total * 100.0) / 100.0;
        roomTotalFee.put(roomId, total);

        AcDetailRecord record = new AcDetailRecord(
                roomId,
                seg.requestTimeSeconds,
                seg.serviceStartTimeSeconds,
                currentTime,
                duration,
                seg.fanSpeed,
                fee,
                total);
        detailRecordRepository.save(record);

        System.out.println("计费：房间 " + roomId
                + " 结束一段服务，时长=" + duration + " 秒，风速="
                + seg.fanSpeed + "，本段费用=" + fee + "，累积费用=" + total);
    }

    @Override
    public void onFanSpeedChange(String roomId, FanSpeed newSpeed, int currentTime) {
        Segment seg = activeSegments.get(roomId);

        if (seg == null) {
            System.out.println("计费：房间 " + roomId
                    + " 当前不在服务中，风速变更不触发计费");
            return;
        }

        ACMode mode = seg.mode;

        System.out.println("计费：房间 " + roomId + " 风速切换 "
                + seg.fanSpeed + " → " + newSpeed + "，时间=" + currentTime + " 秒");

        onServiceEnd(roomId, currentTime);
        onServiceStart(roomId, mode, newSpeed, currentTime);
    }

    @Override
    public void generateAcBill(String roomId, int checkInTime, int checkOutTime) {
        if (activeSegments.containsKey(roomId)) {
            onServiceEnd(roomId, checkOutTime);
        }

        double total = roomTotalFee.getOrDefault(roomId, 0.0);
        total = Math.round(total * 100.0) / 100.0;

        AcBill bill = new AcBill(roomId, checkInTime, checkOutTime, total);
        acBillRepository.save(bill);

        roomTotalFee.remove(roomId);

        System.out.println("计费：生成房间 " + roomId + " 的空调账单，总费用=" + total + " 元");
    }

    // ========== ★ 新增：实时查询方法 ==========

    /**
     * 获取房间当前累积费用（已结算的总费用）
     */
    public double getRoomTotalFee(String roomId) {
        return roomTotalFee.getOrDefault(roomId, 0.0);
    }

    /**
     * 获取房间当前正在进行的服务段费用（本次消费）
     * @param roomId 房间号
     * @param currentTime 当前时间（秒）
     * @return 本次服务段的费用
     */
    public double getCurrentSessionFee(String roomId, int currentTime) {
        Segment seg = activeSegments.get(roomId);
        if (seg == null) {
            return 0.0; // 当前没有活跃服务段
        }

        int duration = currentTime - seg.serviceStartTimeSeconds;
        if (duration <= 0) {
            return 0.0;
        }

        double ratePerMinute = getRatePerMinute(seg.fanSpeed);
        double fee = duration / 60.0 * ratePerMinute;
        return Math.round(fee * 100.0) / 100.0;
    }

    /**
     * 清除所有计费数据（用于系统重置）
     */
    public void clearAll() {
        activeSegments.clear();
        roomTotalFee.clear();
        System.out.println("计费：所有计费数据已清空");
    }

    private double getRatePerMinute(FanSpeed fanSpeed) {
        if (fanSpeed == null)
            return 0.5;

        switch (fanSpeed) {
            case HIGH:
                return 1.0;
            case MEDIUM:
                return 0.5;
            case LOW:
                return 1.0 / 3.0;
            default:
                return 0.5;
        }
    }
}
