package com.model.entity;

/**
 * 空调详单记录（Detail_Records_of_AC）
 *
 * 对应测试用例中每一条"详单"的一行：
 * - 房间号
 * - 请求时间
 * - 服务开始时间
 * - 服务结束时间
 * - 服务时长（秒）
 * - 风速
 * - 当前费用
 * - 累积费用
 */
public class AcDetailRecord {

    private String roomId;

    // 请求时间：用户发送本次请求的时间（单位：秒，从系统启动起算）
    private int requestTimeSeconds;

    // 本段服务的开始、结束时间（单位：秒）
    private int serviceStartTimeSeconds;
    private int serviceEndTimeSeconds;

    // 本段服务时长（秒）
    private int serviceDurationSeconds;

    // 本段服务时使用的风速
    private FanSpeed fanSpeed;

    // 本段服务产生的费用
    private double currentFee;

    // 到本段结束时为止的累积总费用
    private double totalFee;

    public AcDetailRecord() {
    }

    public AcDetailRecord(String roomId,
            int requestTimeSeconds,
            int serviceStartTimeSeconds,
            int serviceEndTimeSeconds,
            int serviceDurationSeconds,
            FanSpeed fanSpeed,
            double currentFee,
            double totalFee) {
        this.roomId = roomId;
        this.requestTimeSeconds = requestTimeSeconds;
        this.serviceStartTimeSeconds = serviceStartTimeSeconds;
        this.serviceEndTimeSeconds = serviceEndTimeSeconds;
        this.serviceDurationSeconds = serviceDurationSeconds;
        this.fanSpeed = fanSpeed;
        this.currentFee = currentFee;
        this.totalFee = totalFee;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getRequestTimeSeconds() {
        return requestTimeSeconds;
    }

    public void setRequestTimeSeconds(int requestTimeSeconds) {
        this.requestTimeSeconds = requestTimeSeconds;
    }

    public int getServiceStartTimeSeconds() {
        return serviceStartTimeSeconds;
    }

    public void setServiceStartTimeSeconds(int serviceStartTimeSeconds) {
        this.serviceStartTimeSeconds = serviceStartTimeSeconds;
    }

    public int getServiceEndTimeSeconds() {
        return serviceEndTimeSeconds;
    }

    public void setServiceEndTimeSeconds(int serviceEndTimeSeconds) {
        this.serviceEndTimeSeconds = serviceEndTimeSeconds;
    }

    public int getServiceDurationSeconds() {
        return serviceDurationSeconds;
    }

    public void setServiceDurationSeconds(int serviceDurationSeconds) {
        this.serviceDurationSeconds = serviceDurationSeconds;
    }

    public FanSpeed getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(FanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public double getCurrentFee() {
        return currentFee;
    }

    public void setCurrentFee(double currentFee) {
        this.currentFee = currentFee;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    // 【新增】兼容性方法：返回累积总费用
    public double getFee() {
        return this.totalFee;
    }

    @Override
    public String toString() {
        return "AcDetailRecord{" +
                "roomId='" + roomId + '\'' +
                ", requestTimeSeconds=" + requestTimeSeconds +
                ", serviceStartTimeSeconds=" + serviceStartTimeSeconds +
                ", serviceEndTimeSeconds=" + serviceEndTimeSeconds +
                ", serviceDurationSeconds=" + serviceDurationSeconds +
                ", fanSpeed=" + fanSpeed +
                ", currentFee=" + currentFee +
                ", totalFee=" + totalFee +
                '}';
    }
}
