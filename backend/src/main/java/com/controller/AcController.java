package com.controller;

import com.model.entity.ACMode;
import com.model.entity.FanSpeed;
import com.service.AcService;

/**
 * 控制层 Controller：模拟“空调控制面板”发来的命令。
 *
 * 以后如果你做成 Web 项目，这里就会变成 @RestController + @GetMapping/@PostMapping。
 */
public class AcController {

    private final AcService acService;

    public AcController(AcService acService) {
        this.acService = acService;
    }

    public void powerOn(String roomId, ACMode mode) {
        acService.powerOn(roomId, mode);
    }

    public void changeTemp(String roomId, double targetTemp) {
        acService.changeTemp(roomId, targetTemp);
    }

    public void changeSpeed(String roomId, FanSpeed fanSpeed) {
        acService.changeSpeed(roomId, fanSpeed);
    }

    public void powerOff(String roomId) {
        acService.powerOff(roomId);
    }
}
