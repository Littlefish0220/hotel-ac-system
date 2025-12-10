package com.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.config.SystemContext;
import com.model.entity.ACMode;
import com.model.entity.AcDetailRecord;
import com.model.entity.AccommodationOrder;
import com.model.entity.Customer;
import com.model.entity.FanSpeed;
import com.model.entity.Room;
import com.service.impl.AcBillingServiceImpl;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HotelApiController {

    @Autowired
    private SystemContext context;

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        List<Room> rooms = context.roomRepository.findAll();

        // 获取当前系统时间（秒）
        int currentTimeSeconds = context.globalTimeCounter * 60;

        // 获取计费服务实例
        AcBillingServiceImpl billingService = (AcBillingServiceImpl) context.billingService;

        List<Map<String, Object>> roomData = rooms.stream().map(room -> {
            Map<String, Object> data = new HashMap<>();
            String roomId = room.getRoomId();

            // 基础信息
            data.put("roomNo", roomId);
            data.put("initialTemp", room.getInitialTemperature());
            data.put("currentTemp", room.getCurrentTemperature());
            data.put("targetTemp", room.getTargetTemperature());
            data.put("fanSpeed", room.getFanSpeed() != null ? room.getFanSpeed().name() : "MEDIUM");
            data.put("dailyRoomRate", room.getPrice());
            data.put("state", getRoomState(roomId));
            data.put("status", room.isOccupied() ? "occupied" : "free");
            data.put("customerName", getCustomerName(roomId));

            // ★ 修改：直接从 Room 获取入住天数
            data.put("checkInDays", room.getCheckInDays());

            // 获取本次消费（当前活跃服务段的费用）
            double sessionFee = billingService.getCurrentSessionFee(roomId, currentTimeSeconds);
            data.put("sessionFee", sessionFee);

            // 获取累计空调费用（只包含已结算的费用）
            double acFee = billingService.getRoomTotalFee(roomId);
            data.put("acFee", acFee);

            // 计算总费用（累计空调费 + 本次消费）
            double totalAcFee = acFee + sessionFee;
            data.put("fee", totalAcFee);

            // 房费相关（用于账单预览）
            data.put("totalRoomFee", room.getCheckInDays() * room.getPrice());

            // 服务时长（秒）
            data.put("serviceDuration", 0);

            return data;
        }).collect(Collectors.toList());

        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("timeCounter", context.globalTimeCounter);
        systemInfo.put("isSystemOn", true);
        systemInfo.put("mode", "cool");
        systemInfo.put("maxLimit", 3);

        result.put("rooms", roomData);
        result.put("system", systemInfo);
        return result;
    }

    @PostMapping("/room/control")
    public Map<String, Object> controlRoom(@RequestBody Map<String, Object> payload) {
        String roomNo = (String) payload.get("roomNo");
        String action = (String) payload.get("action");
        Object tempObj = payload.get("targetTemp");
        String fanSpeedStr = (String) payload.get("fanSpeed");

        try {
            if ("powerOn".equals(action)) {
                context.acController.powerOn(roomNo, ACMode.COOLING);
            } else if ("powerOff".equals(action)) {
                context.acController.powerOff(roomNo);
            }

            if (tempObj != null) {
                Double targetTemp = Double.valueOf(tempObj.toString());
                context.acController.changeTemp(roomNo, targetTemp);
            }

            if (fanSpeedStr != null) {
                FanSpeed speed = FanSpeed.MEDIUM;
                if ("High".equalsIgnoreCase(fanSpeedStr) || "HIGH".equals(fanSpeedStr)) {
                    speed = FanSpeed.HIGH;
                } else if ("Low".equalsIgnoreCase(fanSpeedStr) || "LOW".equals(fanSpeedStr)) {
                    speed = FanSpeed.LOW;
                }
                context.acController.changeSpeed(roomNo, speed);
            }

            return Map.of("code", 200, "msg", "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", 500, "msg", "操作失败: " + e.getMessage());
        }
    }

    @PostMapping("/time/tick")
    public Map<String, Object> timeTick() {
        try {
            context.scheduler.tick();
            context.globalTimeCounter++;
            return Map.of("code", 200, "time", context.globalTimeCounter);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", 500, "msg", "时间推进失败");
        }
    }

    @PostMapping("/checkIn")
    public Map<String, Object> checkIn(@RequestBody Map<String, Object> payload) {
        String roomNo = (String) payload.get("roomNo");
        String customerName = (String) payload.get("customerName");

        try {
            String customerId = "CID_" + System.currentTimeMillis();
            context.customerController.checkIn(
                    customerId,
                    customerName,
                    "ID_" + System.currentTimeMillis(),
                    roomNo,
                    10,
                    200.0);

            return Map.of("code", 200, "msg", "入住成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", 500, "msg", "入住失败: " + e.getMessage());
        }
    }

    @GetMapping("/bill/preview/{roomNo}")
    public Map<String, Object> getBillPreview(@PathVariable String roomNo) {
        try {
            Room room = context.roomRepository.findById(roomNo);
            if (room == null) {
                return Map.of("code", 404, "msg", "房间不存在");
            }

            // 使用计费服务获取准确费用
            AcBillingServiceImpl billingService = (AcBillingServiceImpl) context.billingService;
            int currentTimeSeconds = context.globalTimeCounter * 60;

            double acFee = billingService.getRoomTotalFee(roomNo);
            double sessionFee = billingService.getCurrentSessionFee(roomNo, currentTimeSeconds);
            double totalAcFee = acFee + sessionFee;

            // ★ 修改：使用 room.getCheckInDays()
            int days = room.getCheckInDays();
            double roomFee = days * room.getPrice();
            double total = totalAcFee + roomFee;

            Map<String, Object> bill = new HashMap<>();
            bill.put("roomNo", roomNo);
            bill.put("customerName", getCustomerName(roomNo));
            bill.put("acFee", String.format("%.2f", totalAcFee));
            bill.put("roomFee", String.format("%.2f", roomFee));
            bill.put("total", String.format("%.2f", total));
            bill.put("days", days);
            bill.put("detailLogs", context.detailRepo.findByRoomId(roomNo));

            return Map.of("code", 200, "data", bill);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", 500, "msg", "获取账单失败");
        }
    }

    @GetMapping("/bill/details/{roomNo}")
    public Map<String, Object> getDetailLogs(@PathVariable String roomNo) {
        try {
            List<AcDetailRecord> logs = context.detailRepo.findByRoomId(roomNo);
            return Map.of("code", 200, "data", logs);
        } catch (Exception e) {
            return Map.of("code", 500, "data", Collections.emptyList());
        }
    }

    @PostMapping("/checkOut")
    public Map<String, Object> checkOut(@RequestBody Map<String, String> payload) {
        String roomNo = payload.get("roomNo");

        try {
            Room room = context.roomRepository.findById(roomNo);
            if (room == null) {
                return Map.of("code", 404, "msg", "房间不存在");
            }

            // 使用计费服务获取准确费用
            AcBillingServiceImpl billingService = (AcBillingServiceImpl) context.billingService;
            int currentTimeSeconds = context.globalTimeCounter * 60;

            double acFee = billingService.getRoomTotalFee(roomNo);
            double sessionFee = billingService.getCurrentSessionFee(roomNo, currentTimeSeconds);
            double totalAcFee = acFee + sessionFee;

            // ★ 修改：使用 room.getCheckInDays()
            int days = room.getCheckInDays();
            double roomFee = days * room.getPrice();

            Map<String, Object> billData = new HashMap<>();
            billData.put("roomNo", roomNo);
            billData.put("customerName", getCustomerName(roomNo));
            billData.put("acFee", totalAcFee);
            billData.put("roomFee", roomFee);
            billData.put("total", totalAcFee + roomFee);
            billData.put("checkInDays", days);
            billData.put("detailLogs", context.detailRepo.findByRoomId(roomNo));

            context.customerController.checkOut(roomNo);
            context.detailRepo.findByRoomId(roomNo).clear();

            // ★ 修改：清空入住天数
            room.setCheckInDays(0);
            context.roomRepository.save(room);

            return Map.of("code", 200, "data", billData, "msg", "退房成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", 500, "msg", "退房失败: " + e.getMessage());
        }
    }

    @PostMapping("/reset")
    public Map<String, Object> resetSystem() {
        try {
            context.resetSystem();
            return Map.of("code", 200, "msg", "系统重置成功");
        } catch (Exception e) {
            return Map.of("code", 500, "msg", "重置失败");
        }
    }

    /**
     * 使用调度器获取房间状态
     */
    private String getRoomState(String roomId) {
        return context.scheduler.getRoomScheduleState(roomId);
    }

    private String getCustomerName(String roomNo) {
        try {
            Optional<AccommodationOrder> orderOpt = context.orderRepository.findByRoomId(roomNo);
            if (!orderOpt.isPresent()) {
                return "";
            }

            String customerId = orderOpt.get().getCustomerId();
            if (customerId == null) {
                return "";
            }

            Optional<Customer> customerOpt = context.customerRepository.findById(customerId);
            if (!customerOpt.isPresent()) {
                return "";
            }

            return customerOpt.get().getName();
        } catch (Exception e) {
            return "";
        }
    }
}
