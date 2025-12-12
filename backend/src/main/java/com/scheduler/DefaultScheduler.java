package com.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.model.entity.ACMode;
import com.model.entity.FanSpeed;
import com.model.entity.Room;
import com.model.entity.RoomState;
import com.repository.RoomRepository;
import com.service.AcBillingService;
import com.simulation.TemperatureModel;

public class DefaultScheduler implements Scheduler {

    private static final int MAX_SERVICE_ROOMS = 3;
    private static final int SECONDS_PER_TICK = 60;
    private static final double RECOVERY_RATE = 0.5;
    private static final double STANDBY_THRESHOLD = 1.0;

    private final RoomRepository roomRepository;
    private final TemperatureModel temperatureModel;
    private final AcBillingService billingService;

    private final Map<String, ServiceContext> allContexts = new HashMap<>();
    private int currentTimeSeconds = 0;

    public DefaultScheduler(RoomRepository roomRepository,
            TemperatureModel temperatureModel,
            AcBillingService billingService) {
        this.roomRepository = roomRepository;
        this.temperatureModel = temperatureModel;
        this.billingService = billingService;
    }

    @Override
    public void powerOn(String roomId, ACMode mode, double targetTemp, FanSpeed fanSpeed) {
        Room room = getRoomOrThrow(roomId);

        room.setState(RoomState.AC_ON);
        room.setCurrentMode(mode);
        room.setTargetTemp(targetTemp);
        room.setCurrentFanSpeed(fanSpeed);

        // 每次开机增加入住天数
        room.incrementCheckInDays();
        System.out.println("调度: 房间 " + roomId + " 开机，入住天数=" + room.getCheckInDays());

        roomRepository.save(room);

        allContexts.remove(roomId);
        ServiceContext ctx = new ServiceContext(roomId, mode, targetTemp, fanSpeed);

        // 温度预判断，决定直接进入待机还是等待
        double currentTemp = room.getCurrentTemperature();
        if (mode == ACMode.COOLING) {
            // 制冷模式：当前温度 ≤ 目标温度 → 无需运行，直接待机
            if (currentTemp <= targetTemp) {
                ctx.setRunState(ServiceContext.RunState.STANDBY);
                ctx.setStandbyStartTemp(currentTemp); // 记录待机起始温度
                System.out.println("调度: 房间 " + roomId + " 开机，当前温度已满足目标，直接进入待机");
            } else {
                ctx.setRunState(ServiceContext.RunState.WAITING);
            }
        } else {
            // 制热模式：当前温度 ≥ 目标温度 → 无需运行，直接待机
            if (currentTemp >= targetTemp) {
                ctx.setRunState(ServiceContext.RunState.STANDBY);
                ctx.setStandbyStartTemp(currentTemp); // 记录待机起始温度
                System.out.println("调度: 房间 " + roomId + " 开机，当前温度已满足目标，直接进入待机");
            } else {
                ctx.setRunState(ServiceContext.RunState.WAITING);
            }
        }

        allContexts.put(roomId, ctx);
        System.out.println("调度: 房间 " + roomId + " 开机请求（温度=" + room.getCurrentTemperature() + "℃）");
        schedule();

    }

    @Override
    public void changeTemp(String roomId, double targetTemp) {
        Room room = getRoomOrThrow(roomId);
        room.setTargetTemp(targetTemp);
        roomRepository.save(room);

        ServiceContext ctx = allContexts.get(roomId);
        if (ctx != null) {
            ctx.setTargetTemp(targetTemp);
            System.out.println("调度: 房间 " + roomId + " 调温 -> " + targetTemp + "℃");

            if (ctx.getRunState() == ServiceContext.RunState.RUNNING) {
                double currentTemp = room.getCurrentTemperature();
                ACMode mode = ctx.getMode();
                boolean isSatisfied = false;

                if (mode == ACMode.COOLING) {
                    // 制冷模式：当前温度 ≤ 新目标温度 → 满足条件
                    isSatisfied = currentTemp <= targetTemp;
                } else {
                    // 制热模式：当前温度 ≥ 新目标温度 → 满足条件
                    isSatisfied = currentTemp >= targetTemp;
                }

                if (isSatisfied) {
                    // 立即转入待机状态，结束当前服务
                    System.out.println("调度: 房间 " + roomId + " 调温后已满足目标，从运行转为待机");
                    billingService.onServiceEnd(roomId, currentTimeSeconds);
                    ctx.setRunState(ServiceContext.RunState.STANDBY);
                    ctx.setStandbyStartTemp(currentTemp);
                    ctx.resetCurrentRunTime();
                    roomRepository.save(room);
                }
            } else if (ctx.getRunState() == ServiceContext.RunState.STANDBY) {
                // 原逻辑：待机中调温重新进入等待
                ctx.setRunState(ServiceContext.RunState.WAITING);
                ctx.setStandbyStartTemp(null);
                System.out.println("调度: 房间 " + roomId + " 待机中调温，重新请求服务");
            }
            schedule();
        }
    }

    @Override
    public void changeSpeed(String roomId, FanSpeed newSpeed) {
        Room room = getRoomOrThrow(roomId);
        FanSpeed oldSpeed = room.getCurrentFanSpeed();

        room.setCurrentFanSpeed(newSpeed);
        roomRepository.save(room);

        ServiceContext ctx = allContexts.get(roomId);
        if (ctx != null) {
            if (ctx.getRunState() == ServiceContext.RunState.RUNNING) {
                billingService.onFanSpeedChange(roomId, newSpeed, currentTimeSeconds);
            }

            ctx.setFanSpeed(newSpeed);
            System.out.println("调度: 房间 " + roomId + " 调速: " + oldSpeed
                    + " -> " + newSpeed);

            schedule();
        }
    }

    @Override
    public void powerOff(String roomId) {
        ServiceContext ctx = allContexts.remove(roomId);

        if (ctx != null && ctx.getRunState() == ServiceContext.RunState.RUNNING) {
            billingService.onServiceEnd(roomId, currentTimeSeconds);
        }

        Room room = getRoomOrThrow(roomId);
        room.setState(RoomState.AC_OFF);
        room.setCurrentMode(null);
        room.setTargetTemp(null);
        room.setCurrentFanSpeed(null);
        roomRepository.save(room);

        System.out.println("调度: 房间 " + roomId + " 关机（温度="
                + room.getCurrentTemperature() + "℃）");

        schedule();
    }

    @Override
    public String getRoomScheduleState(String roomId) {
        ServiceContext ctx = allContexts.get(roomId);
        if (ctx == null)
            return "off";

        switch (ctx.getRunState()) {
            case RUNNING:
                return "running";
            case WAITING:
                return "waiting";
            case STANDBY:
                return "standby";
            default:
                return "off";
        }
    }

    @Override
    public void tick() {
        System.out.println("\n=== TICK START: 当前时间 " + currentTimeSeconds
                + "s，准备处理过去1分钟 ===");

        Map<String, ServiceContext.StateSnapshot> snapshots = new HashMap<>();
        for (Map.Entry<String, ServiceContext> entry : allContexts.entrySet()) {
            snapshots.put(entry.getKey(), entry.getValue().createSnapshot());
        }

        currentTimeSeconds += SECONDS_PER_TICK;
        System.out.println(">>> 时间推进至 " + currentTimeSeconds + "s");

        List<String> toStandbyList = new ArrayList<>();
        List<String> toWaitingList = new ArrayList<>();

        for (Room room : roomRepository.findAll()) {
            String roomId = room.getRoomId();
            ServiceContext.StateSnapshot snapshot = snapshots.get(roomId);

            if (snapshot != null) {
                switch (snapshot.runState) {
                    case RUNNING:
                        boolean reached = processRunningTick(room, snapshot);
                        if (reached)
                            toStandbyList.add(roomId);
                        break;

                    case WAITING:
                        processRecovery(room, snapshot.mode);
                        ServiceContext ctx = allContexts.get(roomId);
                        if (ctx != null)
                            ctx.addWaitSeconds(SECONDS_PER_TICK);
                        break;

                    case STANDBY:
                        processRecovery(room, snapshot.mode);
                        double currentTemp = room.getCurrentTemperature();
                        double targetTemp = snapshot.targetTemp; // 从快照获取目标温度

                        if (snapshot.mode == ACMode.COOLING) {
                            // 制冷模式：回温至 目标温度 + 阈值（25+1=26℃）时进入等待
                            if (currentTemp >= targetTemp + STANDBY_THRESHOLD) {
                                toWaitingList.add(roomId);
                                System.out.println("房间 " + roomId + " 制冷待机回温超阈值（" + targetTemp + "+" + STANDBY_THRESHOLD
                                        + "），进入等待");
                            }
                        } else {
                            // 制热模式：降温至 目标温度 - 阈值时进入等待
                            if (currentTemp <= targetTemp - STANDBY_THRESHOLD) {
                                toWaitingList.add(roomId);
                                System.out.println("房间 " + roomId + " 制热待机降温超阈值（" + targetTemp + "-" + STANDBY_THRESHOLD
                                        + "），进入等待");
                            }
                        }
                        break;
                }
            } else {
                processNaturalRecovery(room);
            }

            roomRepository.save(room);
        }

        for (Map.Entry<String, ServiceContext.StateSnapshot> entry : snapshots.entrySet()) {
            if (entry.getValue().runState == ServiceContext.RunState.RUNNING) {
                ServiceContext ctx = allContexts.get(entry.getKey());
                if (ctx != null) {
                    ctx.addRunMinutes(1);
                    ctx.addTotalServiceSeconds(SECONDS_PER_TICK);
                }
            }
        }

        for (String rid : toStandbyList) {
            ServiceContext ctx = allContexts.get(rid);
            if (ctx != null) {
                System.out.println(">>> 房间 " + rid + " 达到目标温度，转为待机");
                billingService.onServiceEnd(rid, currentTimeSeconds);
                ctx.setRunState(ServiceContext.RunState.STANDBY);
                ctx.setStandbyStartTemp(getRoomOrThrow(rid).getCurrentTemperature());
                ctx.resetCurrentRunTime();
            }
        }

        for (String rid : toWaitingList) {
            ServiceContext ctx = allContexts.get(rid);
            if (ctx != null) {
                System.out.println(">>> 房间 " + rid + " 回温超阈值，重新请求服务");
                ctx.setRunState(ServiceContext.RunState.WAITING);
                ctx.setStandbyStartTemp(null);
            }
        }

        schedule();

        System.out.println("=== TICK END: 时间 " + currentTimeSeconds + "s ===\n");
    }

    private boolean processRunningTick(Room room, ServiceContext.StateSnapshot snapshot) {
        double currentTemp = room.getCurrentTemperature();
        double targetTemp = snapshot.targetTemp;
        double changeRate = getTempChangeRate(snapshot.fanSpeed);

        boolean reached = false;

        if (snapshot.mode == ACMode.COOLING) {
            currentTemp -= changeRate;
            if (currentTemp <= targetTemp) {
                currentTemp = targetTemp;
                reached = true;
            }
        } else {
            currentTemp += changeRate;
            if (currentTemp >= targetTemp) {
                currentTemp = targetTemp;
                reached = true;
            }
        }

        room.setCurrentTemperature(formatTemp(currentTemp));

        System.out.println("房间 " + room.getRoomId() + " 运行: "
                + room.getCurrentTemperature() + "℃ (目标 " + targetTemp
                + "℃, 风速 " + snapshot.fanSpeed + ")");

        return reached;
    }

    private void processRecovery(Room room, ACMode mode) {
        double current = room.getCurrentTemperature();
        double initial = room.getInitialTemperature();

        if (Math.abs(current - initial) < 0.01)
            return;

        if (mode == ACMode.COOLING) {
            current += RECOVERY_RATE;
            if (current > initial)
                current = initial;
        } else {
            current -= RECOVERY_RATE;
            if (current < initial)
                current = initial;
        }

        room.setCurrentTemperature(formatTemp(current));
        System.out.println("房间 " + room.getRoomId() + " 回温: " + current
                + "℃ (初始 " + initial + "℃)");
    }

    private void processNaturalRecovery(Room room) {
        double current = room.getCurrentTemperature();
        double initial = room.getInitialTemperature();

        if (Math.abs(current - initial) < 0.01)
            return;

        if (current < initial) {
            current += RECOVERY_RATE;
            if (current > initial)
                current = initial;
        } else {
            current -= RECOVERY_RATE;
            if (current < initial)
                current = initial;
        }

        room.setCurrentTemperature(formatTemp(current));
    }

    private void schedule() {
        List<ServiceContext> runningList = new ArrayList<>();
        List<ServiceContext> waitingList = new ArrayList<>();

        for (ServiceContext ctx : allContexts.values()) {
            if (ctx.getRunState() == ServiceContext.RunState.RUNNING) {
                runningList.add(ctx);
            } else if (ctx.getRunState() == ServiceContext.RunState.WAITING) {
                waitingList.add(ctx);
            }
        }

        waitingList.sort((a, b) -> getPriority(b.getFanSpeed())
                - getPriority(a.getFanSpeed()));

        for (ServiceContext waiter : new ArrayList<>(waitingList)) {
            if (runningList.size() < MAX_SERVICE_ROOMS) {
                promoteToRunning(waiter);
                runningList.add(waiter);
                continue;
            }

            ServiceContext victim = findVictim(runningList, waiter);
            if (victim != null) {
                System.out.println("调度: 房间 " + waiter.getRoomId()
                        + " 抢占 " + victim.getRoomId());
                demoteToWaiting(victim);
                runningList.remove(victim);
                promoteToRunning(waiter);
                runningList.add(waiter);
            }
        }
    }

    private ServiceContext findVictim(List<ServiceContext> runningList,
            ServiceContext waiter) {
        int waiterPriority = getPriority(waiter.getFanSpeed());
        ServiceContext bestVictim = null;
        int minScore = Integer.MAX_VALUE;

        for (ServiceContext runner : runningList) {
            int runnerPriority = getPriority(runner.getFanSpeed());
            int score = runnerPriority * 100;
            if (runner.getCurrentRunMinutes() >= 2) {
                score -= 50;
            }
            if (score < minScore) {
                minScore = score;
                bestVictim = runner;
            }
        }

        int waiterScore = waiterPriority * 100;
        return (bestVictim != null && waiterScore > minScore) ? bestVictim : null;
    }

    private void promoteToRunning(ServiceContext ctx) {
        ctx.setRunState(ServiceContext.RunState.RUNNING);
        ctx.resetCurrentRunTime();
        ctx.resetWaitTime();
        billingService.onServiceStart(ctx.getRoomId(), ctx.getMode(),
                ctx.getFanSpeed(), currentTimeSeconds);
        System.out.println("调度: 房间 " + ctx.getRoomId() + " 进入服务队列");
    }

    private void demoteToWaiting(ServiceContext ctx) {
        billingService.onServiceEnd(ctx.getRoomId(), currentTimeSeconds);
        ctx.setRunState(ServiceContext.RunState.WAITING);
        ctx.resetCurrentRunTime();
        System.out.println("调度: 房间 " + ctx.getRoomId() + " 退出服务队列");
    }

    private int getPriority(FanSpeed speed) {
        if (speed == null)
            return 1;
        switch (speed) {
            case HIGH:
                return 3;
            case MEDIUM:
                return 2;
            case LOW:
                return 1;
            default:
                return 1;
        }
    }

    private double getTempChangeRate(FanSpeed speed) {
        if (speed == null)
            return 0.5;
        switch (speed) {
            case HIGH:
                return 1.0;
            case MEDIUM:
                return 0.5;
            case LOW:
                return 1.0 / 3.0;
            default:
                return 0.5;
        }
    }

    private double formatTemp(double temp) {
        return Math.round(temp * 100.0) / 100.0;
    }

    private Room getRoomOrThrow(String roomId) {
        Room room = roomRepository.findById(roomId);
        if (room == null)
            throw new IllegalArgumentException("Room not found: " + roomId);
        return room;
    }
}
