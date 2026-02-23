package com.yizong.cleanupsystem.controller;

import com.yizong.cleanupsystem.common.Result;
import com.yizong.cleanupsystem.entity.CleaningCar;
import com.yizong.cleanupsystem.service.CleaningCarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cleaning-cars")
@CrossOrigin
public class CleaningCarController {

    @Autowired
    private CleaningCarService cleaningCarService;

    @GetMapping
    public Result<List<CleaningCar>> findAll() {
        List<CleaningCar> cars = cleaningCarService.findAll();
        return Result.success(cars);
    }

    @GetMapping("/{id}")
    public Result<CleaningCar> findById(@PathVariable Long id) {
        CleaningCar car = cleaningCarService.findById(id);
        if (car == null) {
            return Result.error("智能小车不存在");
        }
        return Result.success(car);
    }

    @PostMapping
    public Result<CleaningCar> save(@RequestBody CleaningCar car) {
        try {
            CleaningCar savedCar = cleaningCarService.save(car);
            return Result.success("添加成功", savedCar);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            cleaningCarService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<List<CleaningCar>> search(@RequestParam String name) {
        List<CleaningCar> cars = cleaningCarService.findByNameContaining(name);
        return Result.success(cars);
    }

    @PostMapping("/{id}/assign-task")
    public Result<Void> assignTask(@PathVariable Long id, @RequestBody Map<String, Integer> coordinates) {
        try {
            Integer x = coordinates.get("x");
            Integer y = coordinates.get("y");
            if (x == null || y == null) {
                return Result.error("坐标不能为空");
            }
            if (x < 0 || x > 800 || y < 0 || y > 600) {
                return Result.error("坐标超出范围 (0-800, 0-600)");
            }
            cleaningCarService.assignTask(id, x, y);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
