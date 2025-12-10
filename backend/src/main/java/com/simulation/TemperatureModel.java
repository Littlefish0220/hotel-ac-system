package com.simulation;

import com.model.entity.ACMode;
import com.model.entity.FanSpeed;
import com.model.entity.Room;

/**
 * 温度变化模型：
 * - 正在服务时：根据风速和模式改变温度（制冷降温、制热升温）
 * - 未服务时：房间温度缓慢回到初始温度（回温）
 *
 * 每次调用方法，都表示“过去了 1 分钟”的逻辑时间。
 */
public class TemperatureModel {

    /**
     * 正在服务的 1 分钟内温度变化。
     *
     * @return 是否已经达到目标温度（到达就返回 true）
     */
    public boolean stepService(Room room,
            double targetTemp,
            ACMode mode,
            FanSpeed fanSpeed) {
        double current = room.getCurrentTemp();
        double deltaPerMinute = getDeltaPerMinute(fanSpeed);

        double next;
        if (mode == ACMode.COOLING) {
            // 制冷：降温，最低不能低于目标温度
            next = current - deltaPerMinute;
            if (next < targetTemp) {
                next = targetTemp;
            }
        } else {
            // 制热：升温，最高不能高于目标温度
            next = current + deltaPerMinute;
            if (next > targetTemp) {
                next = targetTemp;
            }
        }

        room.setCurrentTemp(next);

        // 判断是否达到目标温度
        return Math.abs(next - targetTemp) < 0.001;
    }

    /**
     * 房间未被服务时的 1 分钟回温：
     * 每分钟向初始温度靠近 0.5℃。
     */
    public void stepIdle(Room room, ACMode mode) {
        double current = room.getCurrentTemp();
        double initial = room.getInitialTemp();
        double step = 0.5; // 每分钟 0.5℃

        double next = current;

        if (mode == ACMode.COOLING) {
            // 制冷后的回温：温度慢慢升高回到初始温度
            if (current < initial) {
                next = current + step;
                if (next > initial) {
                    next = initial;
                }
            }
        } else {
            // 制热后的回温：温度慢慢降低回到初始温度
            if (current > initial) {
                next = current - step;
                if (next < initial) {
                    next = initial;
                }
            }
        }

        room.setCurrentTemp(next);
    }

    /**
     * 根据风速返回每分钟温度变化量。
     * HIGH: 1℃/min
     * MEDIUM: 0.5℃/min
     * LOW: 1/3℃/min
     */
    private double getDeltaPerMinute(FanSpeed fanSpeed) {
        switch (fanSpeed) {
            case HIGH:
                return 1.0;
            case MEDIUM:
                return 0.5;
            case LOW:
            default:
                return 1.0 / 3.0;
        }
    }
}
