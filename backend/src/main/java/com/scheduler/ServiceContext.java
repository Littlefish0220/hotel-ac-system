package com.scheduler;

import com.model.entity.ACMode;
import com.model.entity.FanSpeed;

public class ServiceContext {

    public enum RunState {
        RUNNING,
        WAITING,
        STANDBY
    }

    private String roomId;
    private ACMode mode;
    private double targetTemp;
    private FanSpeed fanSpeed;
    private RunState runState;

    private int currentRunMinutes;
    private int totalServiceSeconds;
    private int waitSeconds;
    private Double standbyStartTemp;

    public ServiceContext(String roomId, ACMode mode, double targetTemp, FanSpeed fanSpeed) {
        this.roomId = roomId;
        this.mode = mode;
        this.targetTemp = targetTemp;
        this.fanSpeed = fanSpeed;
        this.runState = RunState.WAITING;
        this.currentRunMinutes = 0;
        this.totalServiceSeconds = 0;
        this.waitSeconds = 0;
        this.standbyStartTemp = null;
    }

    // 【新增】创建当前状态的快照
    public StateSnapshot createSnapshot() {
        return new StateSnapshot(roomId, runState, fanSpeed, targetTemp, mode, standbyStartTemp);
    }

    // 【新增】状态快照类（不可变）
    public static class StateSnapshot {
        public final String roomId;
        public final RunState runState;
        public final FanSpeed fanSpeed;
        public final double targetTemp;
        public final ACMode mode;
        public final Double standbyStartTemp;

        public StateSnapshot(String roomId, RunState runState, FanSpeed fanSpeed,
                double targetTemp, ACMode mode, Double standbyStartTemp) {
            this.roomId = roomId;
            this.runState = runState;
            this.fanSpeed = fanSpeed;
            this.targetTemp = targetTemp;
            this.mode = mode;
            this.standbyStartTemp = standbyStartTemp;
        }
    }

    public void addRunMinutes(int minutes) {
        this.currentRunMinutes += minutes;
    }

    public void addTotalServiceSeconds(int seconds) {
        this.totalServiceSeconds += seconds;
    }

    public void addWaitSeconds(int seconds) {
        this.waitSeconds += seconds;
    }

    public void resetCurrentRunTime() {
        this.currentRunMinutes = 0;
    }

    public void resetWaitTime() {
        this.waitSeconds = 0;
    }

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public ACMode getMode() {
        return mode;
    }

    public void setMode(ACMode mode) {
        this.mode = mode;
    }

    public double getTargetTemp() {
        return targetTemp;
    }

    public void setTargetTemp(double targetTemp) {
        this.targetTemp = targetTemp;
    }

    public FanSpeed getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(FanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public RunState getRunState() {
        return runState;
    }

    public void setRunState(RunState runState) {
        this.runState = runState;
    }

    public int getCurrentRunMinutes() {
        return currentRunMinutes;
    }

    public int getTotalServiceSeconds() {
        return totalServiceSeconds;
    }

    public int getWaitSeconds() {
        return waitSeconds;
    }

    public Double getStandbyStartTemp() {
        return standbyStartTemp;
    }

    public void setStandbyStartTemp(Double temp) {
        this.standbyStartTemp = temp;
    }
}
