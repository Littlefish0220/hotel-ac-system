package com.model.entity;

/**
 * 住宿订单：记录顾客在某房间的一次入住行为。
 */
public class AccommodationOrder {

    private String id; // 订单ID
    private String customerId; // 顾客ID
    private String roomId; // 房间号
    private long checkInTime; // 入住时间（时间戳）
    private Long checkOutTime; // 退房时间（可以先为 null）
    private int days; // 入住天数（简化：一次开关机等于一天）
    private double deposit; // 押金

    public AccommodationOrder(String id,
            String customerId,
            String roomId,
            long checkInTime,
            int days,
            double deposit) {
        this.id = id;
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkInTime = checkInTime;
        this.days = days;
        this.deposit = deposit;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRoomId() {
        return roomId;
    }

    public long getCheckInTime() {
        return checkInTime;
    }

    public Long getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Long checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public int getDays() {
        return days;
    }

    public double getDeposit() {
        return deposit;
    }
}
