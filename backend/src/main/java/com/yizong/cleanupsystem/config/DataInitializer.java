package com.yizong.cleanupsystem.config;

import com.yizong.cleanupsystem.entity.Admin;
import com.yizong.cleanupsystem.entity.ChargingStation;
import com.yizong.cleanupsystem.entity.CleaningCar;
import com.yizong.cleanupsystem.repository.AdminRepository;
import com.yizong.cleanupsystem.repository.ChargingStationRepository;
import com.yizong.cleanupsystem.repository.CleaningCarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Autowired
    private CleaningCarRepository cleaningCarRepository;

    private Random random = new Random();

    @Override
    public void run(String... args) {
        initAdmin();
        initChargingStations();
        initCleaningCars();
    }

    private void initAdmin() {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword("123456");
            adminRepository.save(admin);
        }
    }

    private void initChargingStations() {
        if (chargingStationRepository.count() == 0) {
            for (int i = 1; i <= 4; i++) {
                ChargingStation station = new ChargingStation();
                station.setCode("CS" + String.format("%03d", i));
                station.setName("充电桩" + i);
                
                int x, y;
                if (i == 1) {
                    x = 25;
                    y = random.nextInt(550) + 25;
                } else if (i == 2) {
                    x = 775;
                    y = random.nextInt(550) + 25;
                } else if (i == 3) {
                    x = random.nextInt(750) + 25;
                    y = 25;
                } else {
                    x = random.nextInt(750) + 25;
                    y = 575;
                }
                
                station.setX(x);
                station.setY(y);
                station.setStatus("IDLE");
                station.setUsageCount(0);
                chargingStationRepository.save(station);
            }
        }
    }

    private void initCleaningCars() {
        if (cleaningCarRepository.count() == 0) {
            for (int i = 1; i <= 3; i++) {
                CleaningCar car = new CleaningCar();
                car.setCode("CC" + String.format("%03d", i));
                car.setName("智能小车" + i);
                car.setSpeed(10.0 + random.nextInt(10));
                car.setBatteryCapacity(100 + random.nextInt(100));
                car.setChargingSpeed(5 + random.nextInt(5));
                car.setCurrentBattery(random.nextInt(car.getBatteryCapacity()) + 1);
                car.setCurrentX(random.nextInt(801));
                car.setCurrentY(random.nextInt(601));
                car.setStatus("IDLE");
                car.setChargingCount(0);
                car.setCompletedTaskCount(0);
                cleaningCarRepository.save(car);
            }
        } else {
            for (CleaningCar car : cleaningCarRepository.findAll()) {
                if (car.getCurrentBattery() == null || car.getCurrentBattery() <= 0) {
                    car.setCurrentBattery(random.nextInt(car.getBatteryCapacity()) + 1);
                    cleaningCarRepository.save(car);
                }
            }
        }
    }
}
