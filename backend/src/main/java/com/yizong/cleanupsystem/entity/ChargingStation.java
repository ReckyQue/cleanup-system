package com.yizong.cleanupsystem.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "charging_station")
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer x;

    @Column(nullable = false)
    private Integer y;

    @Column(nullable = false)
    private Integer usageCount = 0;

    @Column(nullable = false)
    private String status = "IDLE";

    @Column
    private Integer currentCarId;
}
