package com.yizong.cleanupsystem.service;

import com.yizong.cleanupsystem.entity.Task;
import com.yizong.cleanupsystem.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AutoTaskService {

    @Autowired
    private TaskRepository taskRepository;

    private Random random = new Random();

    @Scheduled(fixedRate = 30000)
    public void generateRandomTask() {
        int delay = random.nextInt(31) + 30;
        
        Task task = new Task();
        task.setX(random.nextInt(801));
        task.setY(random.nextInt(601));
        task.setCarId(null);
        taskRepository.save(task);
    }
}
