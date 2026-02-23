package com.yizong.cleanupsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CleanupSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleanupSystemApplication.class, args);
        System.out.println("========================================");
        System.out.println("自动化清理系统后端启动成功！");
        System.out.println("访问地址: http://localhost:9091");
        System.out.println("========================================");
    }
}
