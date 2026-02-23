package com.yizong.cleanupsystem.controller;

import com.yizong.cleanupsystem.common.Result;
import com.yizong.cleanupsystem.entity.ChargingStation;
import com.yizong.cleanupsystem.service.ChargingStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charging-stations")
@CrossOrigin
public class ChargingStationController {

    @Autowired
    private ChargingStationService chargingStationService;

    @GetMapping
    public Result<List<ChargingStation>> findAll() {
        List<ChargingStation> stations = chargingStationService.findAll();
        return Result.success(stations);
    }

    @GetMapping("/{id}")
    public Result<ChargingStation> findById(@PathVariable Long id) {
        ChargingStation station = chargingStationService.findById(id);
        if (station == null) {
            return Result.error("充电桩不存在");
        }
        return Result.success(station);
    }

    @PostMapping
    public Result<ChargingStation> save(@RequestBody ChargingStation station) {
        try {
            ChargingStation savedStation = chargingStationService.save(station);
            return Result.success("添加成功", savedStation);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            chargingStationService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<List<ChargingStation>> search(@RequestParam String name) {
        List<ChargingStation> stations = chargingStationService.findByNameContaining(name);
        return Result.success(stations);
    }
}
