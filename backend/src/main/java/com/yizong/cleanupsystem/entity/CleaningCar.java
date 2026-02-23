package com.yizong.cleanupsystem.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "cleaning_car")
public class CleaningCar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double speed;

    @Column(nullable = false)
    private Integer batteryCapacity;

    @Column(nullable = false)
    private Integer chargingSpeed;

    @Column(nullable = false)
    private Integer currentBattery;

    @Column(nullable = false)
    private Integer chargingCount = 0;

    @Column(nullable = false)
    private Integer completedTaskCount = 0;

    @Column(nullable = false)
    private String status = "IDLE";

    @Column(nullable = false)
    private Integer currentX;

    @Column(nullable = false)
    private Integer currentY;

    private Integer targetX;

    private Integer targetY;

    private Long currentTaskId;

    private Integer cleaningTime = 0;

    private String targetType;
}
