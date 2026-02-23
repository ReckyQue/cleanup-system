package com.yizong.cleanupsystem.service;

import com.yizong.cleanupsystem.entity.ChargingStation;
import com.yizong.cleanupsystem.repository.ChargingStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChargingStationService {

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    public List<ChargingStation> findAll() {
        return chargingStationRepository.findAll();
    }

    public ChargingStation findById(Long id) {
        return chargingStationRepository.findById(id).orElse(null);
    }

    public ChargingStation save(ChargingStation station) {
        return chargingStationRepository.save(station);
    }

    public void delete(Long id) {
        chargingStationRepository.deleteById(id);
    }

    public List<ChargingStation> findByNameContaining(String name) {
        return chargingStationRepository.findAll().stream()
                .filter(s -> s.getName().contains(name))
                .toList();
    }
}
