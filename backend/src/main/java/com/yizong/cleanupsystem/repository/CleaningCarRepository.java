package com.yizong.cleanupsystem.repository;

import com.yizong.cleanupsystem.entity.CleaningCar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CleaningCarRepository extends JpaRepository<CleaningCar, Long> {
}
