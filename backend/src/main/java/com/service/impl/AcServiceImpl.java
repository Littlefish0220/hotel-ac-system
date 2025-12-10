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

        double defaultTarget = 25.0;
        FanSpeed defaultSpeed = FanSpeed.MEDIUM;

        scheduler.powerOn(roomId, mode, defaultTarget, defaultSpeed);
    }

    @Override
    public void changeTemp(String roomId, double targetTemp) {
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
