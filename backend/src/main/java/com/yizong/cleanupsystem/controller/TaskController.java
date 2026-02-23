package com.yizong.cleanupsystem.controller;

import com.yizong.cleanupsystem.common.Result;
import com.yizong.cleanupsystem.entity.Task;
import com.yizong.cleanupsystem.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public Result<List<Task>> findAll() {
        List<Task> tasks = taskService.findAll();
        return Result.success(tasks);
    }

    @GetMapping("/completed")
    public Result<List<Task>> findCompleted() {
        List<Task> tasks = taskService.findCompletedTasks();
        return Result.success(tasks);
    }

    @GetMapping("/pending")
    public Result<List<Task>> findPending() {
        List<Task> tasks = taskService.findPendingTasks();
        return Result.success(tasks);
    }

    @GetMapping("/in-progress")
    public Result<List<Task>> findInProgress() {
        List<Task> tasks = taskService.findInProgressTasks();
        return Result.success(tasks);
    }

    @GetMapping("/{id}")
    public Result<Task> findById(@PathVariable Long id) {
        Task task = taskService.findById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        return Result.success(task);
    }

    @PostMapping
    public Result<Task> save(@RequestBody Task task) {
        try {
            Task savedTask = taskService.save(task);
            return Result.success("创建成功", savedTask);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public Result<Void> completeTask(@PathVariable Long id) {
        try {
            taskService.completeTask(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping
    public Result<Void> deleteAll() {
        try {
            taskService.deleteAll();
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/pending")
    public Result<Void> deletePendingTasks() {
        try {
            taskService.deletePendingTasks();
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/in-progress")
    public Result<Void> deleteInProgressTasks() {
        try {
            taskService.deleteInProgressTasks();
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/completed")
    public Result<Void> deleteCompletedTasks() {
        try {
            taskService.deleteCompletedTasks();
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
