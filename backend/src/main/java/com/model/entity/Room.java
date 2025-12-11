package com.model.entity;

/**
 * 房间实体类
 */
public class Room {
    // 基础信息
    private String roomId;
    private double price;
    private final double initialTemperature; // ★ 改为 final
    private double currentTemperature;

    // 空调相关
    private Double targetTemperature;
    private FanSpeed fanSpeed = FanSpeed.MEDIUM;
    private ACMode currentMode;
    private RoomState state = RoomState.IDLE;

    // 入住信息
    private boolean occupied = false;
    private String currentCustomerId;
    private String currentOrderId;
    private int checkInDays = 0;

    // 构造函数
    public Room() {
        this.initialTemperature = 25.0; // ★ final 字段必须初始化
    }

    public Room(String roomId, double initialTemperature, double price) {
        this.roomId = roomId;
        this.initialTemperature = initialTemperature; // ✅ 构造时设置，之后不可变
        this.currentTemperature = initialTemperature;
        this.price = price;
        this.checkInDays = 0;
    }

    // ============ Getters ============

    public String getRoomId() {
        return roomId;
    }

    public String getId() {
        return roomId;
    }

    public double getPrice() {
        return price;
    }

    public double getInitialTemperature() {
        return initialTemperature;
    }

    public double getInitialTemp() {
        return initialTemperature;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    public double getCurrentTemp() {
        return currentTemperature;
    }

    public Double getTargetTemperature() {
        return targetTemperature;
    }

    public Double getTargetTemp() {
        return targetTemperature;
    }

    public FanSpeed getFanSpeed() {
        return fanSpeed;
    }

    public FanSpeed getCurrentFanSpeed() {
        return fanSpeed;
    }

    public ACMode getCurrentMode() {
        return currentMode;
    }

    public RoomState getState() {
        return state;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getCurrentCustomerId() {
        return currentCustomerId;
    }

    public String getCurrentOrderId() {
        return currentOrderId;
    }

    public int getCheckInDays() {
        return checkInDays;
    }

    // ============ Setters ============

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // ★ 删除此方法（final 字段不允许 setter）
    // public void setInitialTemperature(double initialTemperature) {
    // this.initialTemperature = initialTemperature;
    // }

    public void setCurrentTemperature(double currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public void setCurrentTemp(double temp) {
        this.currentTemperature = temp;
    }

    public void setTargetTemperature(Double targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public void setTargetTemp(double temp) {
        this.targetTemperature = temp;
    }

    public void setTargetTemp(Double temp) {
        this.targetTemperature = temp;
    }

    public void setFanSpeed(FanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public void setCurrentFanSpeed(FanSpeed speed) {
        this.fanSpeed = speed;
    }

    public void setCurrentFanSpeed(Object speed) {
        if (speed instanceof FanSpeed) {
            this.fanSpeed = (FanSpeed) speed;
        } else if (speed == null) {
            this.fanSpeed = null;
        }
    }

    public void setCurrentMode(ACMode mode) {
        this.currentMode = mode;
    }

    public void setCurrentMode(Object mode) {
        if (mode instanceof ACMode) {
            this.currentMode = (ACMode) mode;
        } else if (mode == null) {
            this.currentMode = null;
        }
    }

    public void setState(RoomState state) {
        this.state = state;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public void setCurrentCustomerId(String currentCustomerId) {
        this.currentCustomerId = currentCustomerId;
    }

    public void setCurrentOrderId(String orderId) {
        this.currentOrderId = orderId;
    }

    public void setCurrentOrderId(Object orderId) {
        if (orderId instanceof String) {
            this.currentOrderId = (String) orderId;
        } else if (orderId == null) {
            this.currentOrderId = null;
        }
    }

    public void setCheckInDays(int days) {
        this.checkInDays = days;
    }

    public void incrementCheckInDays() {
        this.checkInDays++;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId='" + roomId + '\'' +
                ", price=" + price +
                ", initialTemp=" + initialTemperature +
                ", currentTemp=" + currentTemperature +
                ", targetTemp=" + targetTemperature +
                ", fanSpeed=" + fanSpeed +
                ", mode=" + currentMode +
                ", state=" + state +
                ", occupied=" + occupied +
                ", checkInDays=" + checkInDays +
                '}';
    }
}
