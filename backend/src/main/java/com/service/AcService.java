package com.service;

import com.model.entity.ACMode;
import com.model.entity.FanSpeed;

/**
 * 使用空调用例对外暴露的业务接口（Service 层）。
 *
 * 对应 SSD 中的系统事件：
 * - PowerOn(RoomId, CurrentRoomTemp)
 * - ChangeTemp(RoomId, TargetTemp)
 * - ChangeSpeed(RoomId, FanSpeed)
 * - PowerOff(RoomId)
 */
public interface AcService {

    void powerOn(String roomId, ACMode mode);

    void changeTemp(String roomId, double targetTemp);

    void changeSpeed(String roomId, FanSpeed fanSpeed);

    void powerOff(String roomId);
}
