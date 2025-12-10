package com.model.entity;

/**
 * 空调账单对象（Total_Fee_of_AC）
 *
 * 对应作业中办理结账用例里提到的 AC 总费用：
 * - 房间号
 * - 入住时间
 * - 离开时间
 * - 空调总费用
 */
public class AcBill {

    private String roomId;

    // 入住时间（秒）
    private int checkInTimeSeconds;

    // 离开时间（秒）
    private int checkOutTimeSeconds;

    // 空调总费用
    private double totalAcFee;

    public AcBill() {
    }

    public AcBill(String roomId,
            int checkInTimeSeconds,
            int checkOutTimeSeconds,
            double totalAcFee) {
        this.roomId = roomId;
        this.checkInTimeSeconds = checkInTimeSeconds;
        this.checkOutTimeSeconds = checkOutTimeSeconds;
        this.totalAcFee = totalAcFee;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getCheckInTimeSeconds() {
        return checkInTimeSeconds;
    }

    public void setCheckInTimeSeconds(int checkInTimeSeconds) {
        this.checkInTimeSeconds = checkInTimeSeconds;
    }

    public int getCheckOutTimeSeconds() {
        return checkOutTimeSeconds;
    }

    public void setCheckOutTimeSeconds(int checkOutTimeSeconds) {
        this.checkOutTimeSeconds = checkOutTimeSeconds;
    }

    public double getTotalAcFee() {
        return totalAcFee;
    }

    public void setTotalAcFee(double totalAcFee) {
        this.totalAcFee = totalAcFee;
    }

    @Override
    public String toString() {
        return "AcBill{" +
                "roomId='" + roomId + '\'' +
                ", checkInTimeSeconds=" + checkInTimeSeconds +
                ", checkOutTimeSeconds=" + checkOutTimeSeconds +
                ", totalAcFee=" + totalAcFee +
                '}';
    }
}
