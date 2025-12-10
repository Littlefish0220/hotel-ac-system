package com.model.entity;

/**
 * 住宿费账单：
 * 房间号 + 入住/离开时间 + 入住天数 + 总费用。
 */
public class AccommodationBill {

    private String id; // 账单ID
    private String roomId; // 房间号
    private long checkInTime; // 入住时间
    private long checkOutTime; // 离开时间
    private int days; // 入住天数
    private double totalFee; // 住宿总费用

    public AccommodationBill(String id,
            String roomId,
            long checkInTime,
            long checkOutTime,
            int days,
            double totalFee) {
        this.id = id;
        this.roomId = roomId;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.days = days;
        this.totalFee = totalFee;
    }

    public String getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public long getCheckInTime() {
        return checkInTime;
    }

    public long getCheckOutTime() {
        return checkOutTime;
    }

    public int getDays() {
        return days;
    }

    public double getTotalFee() {
        return totalFee;
    }
}
