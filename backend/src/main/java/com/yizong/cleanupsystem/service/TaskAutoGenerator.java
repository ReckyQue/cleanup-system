package com.yizong.cleanupsystem.service;

import com.yizong.cleanupsystem.entity.Task;
import com.yizong.cleanupsystem.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class TaskAutoGenerator {

    @Autowired
    private TaskRepository taskRepository;

    private final Random random = new Random();
    private long nextTaskTime = System.currentTimeMillis() + getRandomDelay();

    private long getRandomDelay() {
        return 30000 + random.nextInt(30001);
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void generateAutoTask() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime >= nextTaskTime) {
            Task task = new Task();
            task.setX(random.nextInt(801));
            task.setY(random.nextInt(601));
            task.setCreateTime(LocalDateTime.now());
            task.setCarId(null);
            task.setCompleteTime(null);
            task.setDuration(0L);
            
            taskRepository.save(task);
            
            System.out.println("自动生成新任务，坐标: (" + task.getX() + ", " + task.getY() + ")");
            
            nextTaskTime = currentTime + getRandomDelay();
            System.out.println("下一个任务将在 " + (getRandomDelay() / 1000) + " 秒后生成");
        }
    }
}