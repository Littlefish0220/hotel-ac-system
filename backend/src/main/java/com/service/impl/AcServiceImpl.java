package com.service.impl;

import com.model.entity.ACMode;
import com.model.entity.FanSpeed;
import com.model.entity.Room;
import com.repository.RoomRepository;
import com.scheduler.Scheduler;
import com.service.AcService;

public class AcServiceImpl implements AcService {

    private final RoomRepository roomRepository;
    private final Scheduler scheduler;

    public AcServiceImpl(RoomRepository roomRepository, Scheduler scheduler) {
        this.roomRepository = roomRepository;
        this.scheduler = scheduler;
    }

    @Override
    public void powerOn(String roomId, ACMode mode) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在: " + roomId);
        }

        // ★ 修改：优先使用房间已设置的目标温度，如果为空才使用系统默认值
        double targetTemp;
        if (room.getTargetTemperature() != null) {
            // 房间已有目标温度，直接使用（保持 SystemContext 中设置的值）
            targetTemp = room.getTargetTemperature();
            System.out.println("[AcService] 房间 " + roomId + " 使用已设置的目标温度: " + targetTemp + "°C");
        } else {
            // 房间没有目标温度，根据系统模式设置默认值
            targetTemp = (mode == ACMode.HEATING) ? 23.0 : 25.0;
            System.out.println("[AcService] 房间 " + roomId + " 使用系统默认目标温度: " + targetTemp + "°C (模式: " + mode + ")");
        }

        FanSpeed defaultSpeed = FanSpeed.MEDIUM;

        scheduler.powerOn(roomId, mode, targetTemp, defaultSpeed);
    }

    @Override
    public void changeTemp(String roomId, double targetTemp) {
        // 温度范围限制
        if (targetTemp < 18.0)
            targetTemp = 18.0;
        if (targetTemp > 28.0)
            targetTemp = 28.0;

        scheduler.changeTemp(roomId, targetTemp);
    }

    @Override
    public void changeSpeed(String roomId, FanSpeed fanSpeed) {
        scheduler.changeSpeed(roomId, fanSpeed);
    }

    @Override
    public void powerOff(String roomId) {
        scheduler.powerOff(roomId);
    }
}
