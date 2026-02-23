package com.yizong.cleanupsystem.service;

import com.yizong.cleanupsystem.entity.CleaningCar;
import com.yizong.cleanupsystem.entity.Task;
import com.yizong.cleanupsystem.repository.CleaningCarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class CleaningCarService {

    @Autowired
    private CleaningCarRepository cleaningCarRepository;

    @Autowired
    private TaskService taskService;

    public List<CleaningCar> findAll() {
        return cleaningCarRepository.findAll();
    }

    public CleaningCar findById(Long id) {
        return cleaningCarRepository.findById(id).orElse(null);
    }

    public CleaningCar save(CleaningCar car) {
        Random random = new Random();
        car.setCurrentBattery(random.nextInt(car.getBatteryCapacity()) + 1);
        car.setCurrentX(random.nextInt(801));
        car.setCurrentY(random.nextInt(601));
        return cleaningCarRepository.save(car);
    }

    public void delete(Long id) {
        cleaningCarRepository.deleteById(id);
    }

    public List<CleaningCar> findByNameContaining(String name) {
        return cleaningCarRepository.findAll().stream()
                .filter(c -> c.getName().contains(name))
                .toList();
    }

    public void updatePosition(Long id, Integer x, Integer y) {
        CleaningCar car = findById(id);
        if (car != null) {
            car.setCurrentX(x);
            car.setCurrentY(y);
            cleaningCarRepository.save(car);
        }
    }

    public void updateStatus(Long id, String status) {
        CleaningCar car = findById(id);
        if (car != null) {
            car.setStatus(status);
            cleaningCarRepository.save(car);
        }
    }

    public void updateBattery(Long id, Integer battery) {
        CleaningCar car = findById(id);
        if (car != null) {
            car.setCurrentBattery(battery);
            cleaningCarRepository.save(car);
        }
    }

    public void assignTask(Long carId, Integer x, Integer y) {
        CleaningCar car = findById(carId);
        if (car != null) {
            Task task = new Task();
            task.setX(x);
            task.setY(y);
            task.setCarId(carId);
            Task savedTask = taskService.save(task);
            
            car.setTargetX(x);
            car.setTargetY(y);
            car.setCurrentTaskId(savedTask.getId());
            car.setStatus("MOVING");
            cleaningCarRepository.save(car);
        }
    }
}
