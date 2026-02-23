package com.yizong.cleanupsystem.service;

import com.yizong.cleanupsystem.entity.CleaningCar;
import com.yizong.cleanupsystem.entity.ChargingStation;
import com.yizong.cleanupsystem.entity.Task;
import com.yizong.cleanupsystem.repository.CleaningCarRepository;
import com.yizong.cleanupsystem.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CleaningCarRepository cleaningCarRepository;

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task save(Task task) {
        task.setCreateTime(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public void completeTask(Long id) {
        Task task = findById(id);
        if (task != null) {
            task.setCompleteTime(LocalDateTime.now());
            long duration = ChronoUnit.SECONDS.between(task.getCreateTime(), task.getCompleteTime());
            task.setDuration(duration);
            taskRepository.save(task);
        }
    }

    public List<Task> findCompletedTasks() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getCompleteTime() != null)
                .toList();
    }

    public List<Task> findPendingTasks() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getCompleteTime() == null && t.getCarId() == null)
                .toList();
    }

    public List<Task> findInProgressTasks() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getCompleteTime() == null && t.getCarId() != null)
                .toList();
    }

    public void deleteAll() {
        taskRepository.deleteAll();
    }

    @Transactional
    public void deletePendingTasks() {
        List<Task> pendingTasks = findPendingTasks();
        for (Task task : pendingTasks) {
            taskRepository.delete(task);
        }
    }

    @Transactional
    public void deleteInProgressTasks() {
        List<Task> inProgressTasks = findInProgressTasks();
        for (Task task : inProgressTasks) {
            Long carId = task.getCarId();
            if (carId != null) {
                CleaningCar car = cleaningCarRepository.findById(carId).orElse(null);
                if (car != null) {
                    car.setCurrentTaskId(null);
                    car.setStatus("IDLE");
                    car.setTargetX(null);
                    car.setTargetY(null);
                    car.setTargetType(null);
                    car.setCleaningTime(0);
                    cleaningCarRepository.save(car);
                }
            }
            taskRepository.delete(task);
        }
    }

    @Transactional
    public void deleteCompletedTasks() {
        List<Task> completedTasks = findCompletedTasks();
        for (Task task : completedTasks) {
            taskRepository.delete(task);
        }
    }
}
