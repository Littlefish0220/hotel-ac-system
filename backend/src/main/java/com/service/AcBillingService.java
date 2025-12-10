package com.service;

import com.model.entity.ACMode;
import com.model.entity.FanSpeed;

/**
 * 空调计费服务接口。
 *
 * DefaultScheduler 在发生“服务开始/结束/风速变化/关机”等事件时会调用它，
 * 按测试用例中的计费规则生成详单和账单。
 */
public interface AcBillingService {

    /**
     * 某个房间开始正式被服务（进入服务队列）。
     *
     * @param roomId      房间号
     * @param mode        当前模式（制冷/制热）
     * @param fanSpeed    当前风速
     * @param currentTime 当前系统时间（秒）
     */
    void onServiceStart(String roomId, ACMode mode, FanSpeed fanSpeed, int currentTime);

    /**
     * 某个房间结束当前一次服务段：
     * 例如被调度到等待队列、达到目标温度、关机、风速变化等。
     *
     * @param roomId      房间号
     * @param currentTime 当前系统时间（秒）
     */
    void onServiceEnd(String roomId, int currentTime);

    /**
     * 房间在服务过程中更改了风速：
     * 需要结束上一段，开始下一段（新的风速）服务。
     */
    void onFanSpeedChange(String roomId, FanSpeed newSpeed, int currentTime);

    /**
     * 退房 / 办理结账时，汇总某个房间的空调总费用并生成 AcBill。
     *
     * @param roomId       房间号
     * @param checkInTime  入住时间（秒）
     * @param checkOutTime 离开时间（秒）
     */
    void generateAcBill(String roomId, int checkInTime, int checkOutTime);
}
