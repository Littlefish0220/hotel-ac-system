package com.scheduler;

import com.model.entity.ACMode;
import com.model.entity.FanSpeed;

public interface Scheduler {
    void powerOn(String roomId, ACMode mode, double targetTemp, FanSpeed fanSpeed);

    void changeTemp(String roomId, double targetTemp);

    void changeSpeed(String roomId, FanSpeed fanSpeed);

    void powerOff(String roomId);

    void tick();

    String getRoomScheduleState(String roomId);
}
