package com.yizong.cleanupsystem.service;

import com.yizong.cleanupsystem.entity.ChargingStation;
import com.yizong.cleanupsystem.entity.CleaningCar;
import com.yizong.cleanupsystem.entity.Task;
import com.yizong.cleanupsystem.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class IntelligentDecisionService {

    private static final double MOVE_COST_PER_UNIT = 0.05;
    private static final double TASK_COMPLETION_COST = 1.0;
    private static final double SAFETY_BATTERY_PERCENTAGE = 0.05;
    private static final double MIN_EFFICIENCY_THRESHOLD = 0.15;

    @Autowired
    private TaskRepository taskRepository;

    public DecisionResult makeDecision(CleaningCar car, List<ChargingStation> stations) {
        double currentBattery = car.getCurrentBattery();
        double maxBattery = car.getBatteryCapacity();
        double safetyBattery = maxBattery * SAFETY_BATTERY_PERCENTAGE;
        
        System.out.println("小车 " + car.getName() + " 决策开始，当前电量: " + currentBattery + "/" + maxBattery);
        
        if (currentBattery <= 0) {
            System.out.println("电量为0，无法移动，状态为电量低");
            return new DecisionResult(DecisionType.IDLE, null, "电量为0，无法移动");
        }
        
        ChargingStation nearestStation = findNearestStation(car, stations);
        if (nearestStation == null) {
            return new DecisionResult(DecisionType.IDLE, null, "无可用充电桩");
        }
        
        double distanceToStation = calculateDistance(car.getCurrentX(), car.getCurrentY(), 
                nearestStation.getX(), nearestStation.getY());
        double costToStation = distanceToStation * MOVE_COST_PER_UNIT;
        
        System.out.println("最近充电桩距离: " + distanceToStation + ", 前往成本: " + costToStation);
        
        if (currentBattery <= safetyBattery) {
            System.out.println("电量过低，选择充电");
            return new DecisionResult(DecisionType.CHARGE, nearestStation, 
                    String.format("电量过低(%.1f%%)，前往充电桩充电", 
                            (currentBattery / maxBattery * 100)));
        }
        
        if (car.getCurrentTaskId() != null) {
            return new DecisionResult(DecisionType.CONTINUE_TASK, null, "继续执行当前任务");
        }
        
        double availableBattery = currentBattery - safetyBattery;
        double maxTravelDistance = availableBattery / MOVE_COST_PER_UNIT;
        
        System.out.println("可用电量: " + availableBattery + ", 最大移动距离: " + maxTravelDistance);
        
        Task potentialTask = generatePotentialTask(car, maxTravelDistance);
        if (potentialTask == null) {
            if (currentBattery < maxBattery * 0.4) {
                System.out.println("无可用任务且电量较低(" + (currentBattery / maxBattery * 100) + "%)，前往充电桩");
                return new DecisionResult(DecisionType.CHARGE, nearestStation, 
                        String.format("无任务且电量较低(%.1f%%)，前往充电桩充电", 
                                (currentBattery / maxBattery * 100)));
            }
            System.out.println("无可用任务，保持IDLE状态");
            return new DecisionResult(DecisionType.IDLE, null, 
                    "无可用任务，保持IDLE状态");
        }
        
        double distanceToTask = calculateDistance(car.getCurrentX(), car.getCurrentY(), 
                potentialTask.getX(), potentialTask.getY());
        double costToTask = distanceToTask * MOVE_COST_PER_UNIT;
        double totalTaskCost = costToTask + TASK_COMPLETION_COST;
        
        System.out.println("找到任务，距离: " + distanceToTask + ", 任务总成本: " + totalTaskCost);
        
        if (totalTaskCost > availableBattery) {
            if (currentBattery < maxBattery * 0.4) {
                System.out.println("任务超出可用电量范围且电量较低(" + (currentBattery / maxBattery * 100) + "%)，前往充电桩");
                return new DecisionResult(DecisionType.CHARGE, nearestStation, 
                        String.format("任务超出电量范围且电量较低(%.1f%%)，前往充电桩充电", 
                                (currentBattery / maxBattery * 100)));
            }
            System.out.println("任务超出可用电量范围，保持IDLE状态");
            return new DecisionResult(DecisionType.IDLE, null, 
                    "任务超出可用电量范围，保持IDLE状态");
        }
        
        double distanceFromTaskToStation = calculateDistance(potentialTask.getX(), potentialTask.getY(), 
                nearestStation.getX(), nearestStation.getY());
        double costFromTaskToStation = distanceFromTaskToStation * MOVE_COST_PER_UNIT;
        double totalRoundTripCost = totalTaskCost + costFromTaskToStation;
        
        double efficiency = calculateEfficiency(totalTaskCost, currentBattery);
        
        System.out.println("往返总成本: " + totalRoundTripCost + ", 可用电量: " + availableBattery + ", 效率: " + (efficiency * 100) + "%");
        
        if (totalRoundTripCost <= availableBattery) {
            System.out.println("接受任务，可以完成任务并返回充电桩");
            return new DecisionResult(DecisionType.ACCEPT_TASK, potentialTask, 
                    "接受任务，可以完成任务并返回充电桩");
        } else {
            double remainingAfterTask = currentBattery - totalTaskCost;
            if (remainingAfterTask >= safetyBattery) {
                System.out.println("接受任务，电量充足且保留安全电量");
                return new DecisionResult(DecisionType.ACCEPT_TASK, potentialTask, 
                        "接受任务，电量充足且保留安全电量");
            } else {
                System.out.println("完成任务后电量不足，保持IDLE状态");
                return new DecisionResult(DecisionType.IDLE, null, 
                        "完成任务后电量不足，保持IDLE状态");
            }
        }
    }

    private ChargingStation findNearestStation(CleaningCar car, List<ChargingStation> stations) {
        return stations.stream()
                .min(Comparator.comparingDouble(station -> 
                    calculateDistance(car.getCurrentX(), car.getCurrentY(), 
                            station.getX(), station.getY())))
                .orElse(null);
    }

    private double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private Task generatePotentialTask(CleaningCar car, double maxTravelDistance) {
        List<Task> availableTasks = taskRepository.findAll().stream()
                .filter(t -> t.getCarId() == null && t.getCompleteTime() == null)
                .sorted(Comparator.comparingDouble(t -> 
                    calculateDistance(car.getCurrentX(), car.getCurrentY(), t.getX(), t.getY())))
                .toList();
        
        if (availableTasks.isEmpty()) {
            return null;
        }
        
        for (Task task : availableTasks) {
            double distance = calculateDistance(car.getCurrentX(), car.getCurrentY(), task.getX(), task.getY());
            double costToTask = distance * MOVE_COST_PER_UNIT;
            double totalTaskCost = costToTask + TASK_COMPLETION_COST;
            
            System.out.println("检查任务 " + task.getId() + "，距离: " + distance + ", 成本: " + totalTaskCost + ", 最大距离: " + maxTravelDistance);
            
            if (totalTaskCost <= maxTravelDistance) {
                System.out.println("找到可达任务: " + task.getId());
                return task;
            }
        }
        
        System.out.println("没有可达任务，返回最近任务: " + availableTasks.get(0).getId());
        return availableTasks.get(0);
    }

    private double calculateEfficiency(double taskCost, double currentBattery) {
        double efficiency = 1.0 - (taskCost / currentBattery);
        return Math.max(0.0, efficiency);
    }

    public enum DecisionType {
        ACCEPT_TASK,
        CONTINUE_TASK,
        CHARGE,
        IDLE
    }

    public static class DecisionResult {
        private DecisionType type;
        private Object target;
        private String reason;

        public DecisionResult(DecisionType type, Object target, String reason) {
            this.type = type;
            this.target = target;
            this.reason = reason;
        }

        public DecisionType getType() {
            return type;
        }

        public Object getTarget() {
            return target;
        }

        public String getReason() {
            return reason;
        }
    }
}
