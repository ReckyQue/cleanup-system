package com.yizong.cleanupsystem.service;

import com.yizong.cleanupsystem.entity.ChargingStation;
import com.yizong.cleanupsystem.entity.CleaningCar;
import com.yizong.cleanupsystem.entity.Task;
import com.yizong.cleanupsystem.repository.CleaningCarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
public class SimulationService {

    @Autowired
    private CleaningCarRepository cleaningCarRepository;

    @Autowired
    private CleaningCarService cleaningCarService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ChargingStationService chargingStationService;

    @Autowired
    private IntelligentDecisionService intelligentDecisionService;

    private Random random = new Random();

    @Scheduled(fixedRate = 1000)
    public void simulateCarMovement() {
        List<CleaningCar> cars = cleaningCarRepository.findAll();
        
        for (CleaningCar car : cars) {
            System.out.println("小车 " + car.getName() + " 当前状态: " + car.getStatus());
            switch (car.getStatus()) {
                case "IDLE":
                    handleIdleCar(car);
                    break;
                case "MOVING":
                    handleMovingCar(car);
                    break;
                case "CLEANING":
                    handleCleaningCar(car);
                    break;
                case "CHARGING":
                    handleChargingCar(car);
                    break;
                case "待回收":
                    handleLowBatteryCar(car);
                    break;
                default:
                    System.out.println("小车 " + car.getName() + " 未知状态: " + car.getStatus());
                    break;
            }
        }
    }

    private void handleIdleCar(CleaningCar car) {
        List<ChargingStation> stations = chargingStationService.findAll();
        IntelligentDecisionService.DecisionResult decision = intelligentDecisionService.makeDecision(car, stations);
        
        switch (decision.getType()) {
            case ACCEPT_TASK:
                Task task = (Task) decision.getTarget();
                acceptTask(car, task);
                break;
            case CHARGE:
                ChargingStation station = (ChargingStation) decision.getTarget();
                goToCharging(car, station);
                break;
            case CONTINUE_TASK:
            case IDLE:
            default:
                break;
        }
    }

    private void handleMovingCar(CleaningCar car) {
        if (car.getCurrentBattery() <= 0 && !"CHARGING".equals(car.getTargetType())) {
            car.setStatus("待回收");
            car.setTargetX(null);
            car.setTargetY(null);
            car.setTargetType(null);
            System.out.println("小车 " + car.getName() + " 电量为0，无法移动，状态变为待回收");
            cleaningCarRepository.save(car);
            return;
        }
        
        if (car.getTargetX() != null && car.getTargetY() != null) {
            int dx = car.getTargetX() - car.getCurrentX();
            int dy = car.getTargetY() - car.getCurrentY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance <= car.getSpeed()) {
                car.setCurrentX(car.getTargetX());
                car.setCurrentY(car.getTargetY());
                
                if (car.getCurrentTaskId() != null) {
                    car.setStatus("CLEANING");
                    car.setCleaningTime(random.nextInt(6) + 5);
                    System.out.println("小车 " + car.getName() + " 到达任务目标，开始清理，清理时间: " + car.getCleaningTime());
                } else {
                    ChargingStation nearestStation = findNearestChargingStation(car);
                    if (nearestStation != null && calculateDistance(car.getCurrentX(), car.getCurrentY(), nearestStation.getX(), nearestStation.getY()) <= 10) {
                        car.setStatus("CHARGING");
                        System.out.println("小车 " + car.getName() + " 到达充电桩，开始充电");
                    } else {
                        car.setStatus("IDLE");
                        car.setTargetX(null);
                        car.setTargetY(null);
                        System.out.println("小车 " + car.getName() + " 到达目标，无任务，状态变为IDLE");
                    }
                }
            } else {
                double ratio = car.getSpeed() / distance;
                int newX = car.getCurrentX() + (int) (dx * ratio);
                int newY = car.getCurrentY() + (int) (dy * ratio);
                
                double moveDistance = calculateDistance(car.getCurrentX(), car.getCurrentY(), newX, newY);
                double batteryCost = moveDistance * 0.08;
                
                car.setCurrentX(newX);
                car.setCurrentY(newY);
                car.setCurrentBattery((int) Math.max(0, car.getCurrentBattery() - batteryCost));
            }
            
            cleaningCarRepository.save(car);
        }
    }

    private void handleCleaningCar(CleaningCar car) {
        System.out.println("小车 " + car.getName() + " 清理状态检查，清理时间: " + car.getCleaningTime() + ", 任务ID: " + car.getCurrentTaskId());
        
        if (car.getCurrentBattery() <= 0) {
            car.setStatus("待回收");
            car.setTargetX(null);
            car.setTargetY(null);
            car.setTargetType(null);
            car.setCleaningTime(0);
            System.out.println("小车 " + car.getName() + " 电量为0，停止清理，状态变为待回收");
            cleaningCarRepository.save(car);
            return;
        }
        
        if (car.getCleaningTime() != null && car.getCleaningTime() > 0) {
            int newTime = car.getCleaningTime() - 1;
            car.setCleaningTime(newTime);
            System.out.println("小车 " + car.getName() + " 清理中，剩余时间: " + newTime);
            cleaningCarRepository.save(car);
        } else if (car.getCurrentTaskId() != null) {
            Task task = taskService.findById(car.getCurrentTaskId());
            if (task != null) {
                taskService.completeTask(task.getId());
                car.setCompletedTaskCount(car.getCompletedTaskCount() + 1);
                car.setCurrentTaskId(null);
                car.setTargetX(null);
                car.setTargetY(null);
                car.setCleaningTime(0);
                car.setStatus("IDLE");
                car.setCurrentBattery((int) Math.max(0, car.getCurrentBattery() - 1));
                System.out.println("小车 " + car.getName() + " 任务完成，状态变为IDLE，消耗1电量");
                cleaningCarRepository.save(car);
            }
        } else {
            car.setStatus("IDLE");
            car.setCleaningTime(0);
            System.out.println("小车 " + car.getName() + " 无任务，状态变为IDLE");
            cleaningCarRepository.save(car);
        }
    }

    private void handleChargingCar(CleaningCar car) {
        System.out.println("小车 " + car.getName() + " 处理充电状态，当前电量: " + car.getCurrentBattery() + "/" + car.getBatteryCapacity());
        
        ChargingStation nearestStation = findNearestChargingStation(car);
        
        if (nearestStation == null) {
            car.setStatus("IDLE");
            System.out.println("小车 " + car.getName() + " 无充电桩可用，状态变为IDLE");
            cleaningCarRepository.save(car);
            return;
        }
        
        double distance = calculateDistance(car.getCurrentX(), car.getCurrentY(), nearestStation.getX(), nearestStation.getY());
        System.out.println("小车 " + car.getName() + " 距离充电桩 " + nearestStation.getName() + ": " + distance);
        
        if (distance > 10) {
            System.out.println("小车 " + car.getName() + " 距离充电桩太远，移动到充电桩");
            car.setTargetX(nearestStation.getX());
            car.setTargetY(nearestStation.getY());
            car.setStatus("MOVING");
            cleaningCarRepository.save(car);
            return;
        }
        
        if (nearestStation.getCurrentCarId() != null && nearestStation.getCurrentCarId() != car.getId().intValue()) {
            car.setStatus("IDLE");
            System.out.println("小车 " + car.getName() + " 充电桩被占用，状态变为IDLE");
            cleaningCarRepository.save(car);
            return;
        }
        
        if (car.getCurrentBattery() < car.getBatteryCapacity()) {
            car.setCurrentBattery(Math.min(car.getBatteryCapacity(), car.getCurrentBattery() + car.getChargingSpeed()));
            System.out.println("小车 " + car.getName() + " 充电中，当前电量: " + car.getCurrentBattery() + "/" + car.getBatteryCapacity());
            
            if (nearestStation.getCurrentCarId() == null) {
                nearestStation.setCurrentCarId(car.getId().intValue());
                nearestStation.setUsageCount(nearestStation.getUsageCount() + 1);
                nearestStation.setStatus("CHARGING");
                chargingStationService.save(nearestStation);
                System.out.println("充电桩 " + nearestStation.getName() + " 开始使用，使用次数: " + nearestStation.getUsageCount());
            }
            
            cleaningCarRepository.save(car);
        } else {
            System.out.println("小车 " + car.getName() + " 充电完成，电量: " + car.getCurrentBattery() + "/" + car.getBatteryCapacity());
            car.setStatus("IDLE");
            car.setChargingCount(car.getChargingCount() + 1);
            car.setTargetX(null);
            car.setTargetY(null);
            car.setTargetType(null);
            
            if (nearestStation.getCurrentCarId() != null && nearestStation.getCurrentCarId() == car.getId().intValue()) {
                nearestStation.setCurrentCarId(null);
                nearestStation.setStatus("IDLE");
                chargingStationService.save(nearestStation);
            }
            
            cleaningCarRepository.save(car);
        }
    }

    private void handleLowBatteryCar(CleaningCar car) {
        System.out.println("小车 " + car.getName() + " 待回收，需要充电，当前电量: " + car.getCurrentBattery() + "/" + car.getBatteryCapacity());
        
        ChargingStation nearestStation = findNearestChargingStation(car);
        
        if (nearestStation == null) {
            System.out.println("小车 " + car.getName() + " 无充电桩可用，保持待回收状态");
            cleaningCarRepository.save(car);
            return;
        }
        
        double distance = calculateDistance(car.getCurrentX(), car.getCurrentY(), nearestStation.getX(), nearestStation.getY());
        System.out.println("小车 " + car.getName() + " 距离充电桩 " + nearestStation.getName() + ": " + distance);
        
        if (distance <= 10) {
            System.out.println("小车 " + car.getName() + " 已在充电桩附近，开始充电");
            car.setStatus("CHARGING");
            car.setTargetX(nearestStation.getX());
            car.setTargetY(nearestStation.getY());
            cleaningCarRepository.save(car);
        } else {
            System.out.println("小车 " + car.getName() + " 需要前往充电桩");
            car.setTargetX(nearestStation.getX());
            car.setTargetY(nearestStation.getY());
            car.setStatus("MOVING");
            car.setTargetType("CHARGING");
            cleaningCarRepository.save(car);
        }
    }

    private ChargingStation findNearestChargingStation(CleaningCar car) {
        List<ChargingStation> stations = chargingStationService.findAll();
        return stations.stream()
                .min(Comparator.comparingDouble(station -> 
                    calculateDistance(car.getCurrentX(), car.getCurrentY(), station.getX(), station.getY())))
                .orElse(null);
    }

    private double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private void goToCharging(CleaningCar car, ChargingStation station) {
        if (station != null) {
            car.setTargetX(station.getX());
            car.setTargetY(station.getY());
            car.setStatus("MOVING");
            car.setTargetType("CHARGING");
            System.out.println("小车 " + car.getName() + " 前往充电桩 " + station.getName());
            cleaningCarRepository.save(car);
        }
    }

    private void acceptTask(CleaningCar car, Task task) {
        task.setCarId(car.getId());
        taskService.save(task);
        
        car.setTargetX(task.getX());
        car.setTargetY(task.getY());
        car.setCurrentTaskId(task.getId());
        car.setStatus("MOVING");
        System.out.println("小车 " + car.getName() + " 接受任务 " + task.getId() + "，前往坐标 (" + task.getX() + ", " + task.getY() + ")");
        cleaningCarRepository.save(car);
    }

    private void createTask(CleaningCar car, Integer x, Integer y) {
        Task task = new Task();
        task.setX(x);
        task.setY(y);
        task.setCarId(car.getId());
        Task savedTask = taskService.save(task);
        
        car.setTargetX(x);
        car.setTargetY(y);
        car.setCurrentTaskId(savedTask.getId());
        car.setStatus("MOVING");
        cleaningCarRepository.save(car);
    }
}
